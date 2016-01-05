module ActiveRecord
  class Migration
    module Compatibility # :nodoc: all
      V5_0 = Current

      module FourTwoShared
        module TableDefinition
          def timestamps(*, **options)
            options[:null] = true if options[:null].nil?
            super
          end
        end

        def create_table(table_name, options = {})
          if block_given?
            super(table_name, options) do |t|
              class << t
                prepend TableDefinition
              end
              yield t
            end
          else
            super
          end
        end

        def add_timestamps(*, **options)
          options[:null] = true if options[:null].nil?
          super
        end

        def index_exists?(table_name, column_name, options = {})
          column_names = Array(column_name).map(&:to_s)
          options[:name] =
            if options.key?(:name).present?
              options[:name].to_s
            else
              index_name(table_name, column: column_names)
            end
          super
        end

        def remove_index(table_name, options = {})
          index_name = index_name_for_remove(table_name, options)
          execute "DROP INDEX #{quote_column_name(index_name)} ON #{quote_table_name(table_name)}"
        end

        private

        def index_name_for_remove(table_name, options = {})
          index_name = index_name(table_name, options)

          unless index_name_exists?(table_name, index_name, true)
            if options.is_a?(Hash) && options.has_key?(:name)
              options_without_column = options.dup
              options_without_column.delete :column
              index_name_without_column = index_name(table_name, options_without_column)

              return index_name_without_column if index_name_exists?(table_name, index_name_without_column, false)
            end

            raise ArgumentError, "Index name '#{index_name}' on table '#{table_name}' does not exist"
          end

          index_name
        end
      end

      class V4_2 < V5_0
        # 4.2 is defined as a module because it needs to be shared with
        # Legacy. When the time comes, V5_0 should be defined straight
        # in its class.
        include FourTwoShared
      end

      module Legacy
        include FourTwoShared

        def run(*)
          ActiveSupport::Deprecation.warn \
            "Directly inheriting from ActiveRecord::Migration is deprecated. " \
            "Please specify the Rails release the migration was written for:\n" \
            "\n" \
            "  class #{self.class.name} < ActiveRecord::Migration[4.2]"
          super
        end
      end
    end
  end
end
