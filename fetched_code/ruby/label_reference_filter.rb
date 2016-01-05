require 'banzai'

module Banzai
  module Filter
    # HTML filter that replaces label references with links.
    class LabelReferenceFilter < ReferenceFilter
      # Public: Find label references in text
      #
      #   LabelReferenceFilter.references_in(text) do |match, id, name|
      #     "<a href=...>#{Label.find(id)}</a>"
      #   end
      #
      # text - String text to search.
      #
      # Yields the String match, an optional Integer label ID, and an optional
      # String label name.
      #
      # Returns a String replaced with the return of the block.
      def self.references_in(text)
        text.gsub(Label.reference_pattern) do |match|
          yield match, $~[:label_id].to_i, $~[:label_name]
        end
      end

      def self.referenced_by(node)
        { label: LazyReference.new(Label, node.attr("data-label")) }
      end

      def call
        replace_text_nodes_matching(Label.reference_pattern) do |content|
          label_link_filter(content)
        end

        replace_link_nodes_with_href(Label.reference_pattern) do |link, text|
          label_link_filter(link, link_text: text)
        end
      end

      # Replace label references in text with links to the label specified.
      #
      # text - String text to replace references in.
      #
      # Returns a String with label references replaced with links. All links
      # have `gfm` and `gfm-label` class names attached for styling.
      def label_link_filter(text, link_text: nil)
        project = context[:project]

        self.class.references_in(text) do |match, id, name|
          params = label_params(id, name)

          if label = project.labels.find_by(params)
            url = url_for_label(project, label)
            klass = reference_class(:label)
            data = data_attribute(
              original: link_text || match,
              project: project.id,
              label: label.id
            )

            text = link_text || render_colored_label(label)

            %(<a href="#{url}" #{data}
                 class="#{klass}">#{escape_once(text)}</a>)
          else
            match
          end
        end
      end

      def url_for_label(project, label)
        h = Gitlab::Application.routes.url_helpers
        h.namespace_project_issues_url( project.namespace, project, label_name: label.name,
                                                                    only_path:  context[:only_path])
      end

      def render_colored_label(label)
        LabelsHelper.render_colored_label(label)
      end

      # Parameters to pass to `Label.find_by` based on the given arguments
      #
      # id   - Integer ID to pass. If present, returns {id: id}
      # name - String name to pass. If `id` is absent, finds by name without
      #        surrounding quotes.
      #
      # Returns a Hash.
      def label_params(id, name)
        if name
          { name: name.tr('"', '') }
        else
          { id: id }
        end
      end
    end
  end
end
