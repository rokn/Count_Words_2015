class Commit
  extend ActiveModel::Naming

  include ActiveModel::Conversion
  include Participable
  include Mentionable
  include Referable
  include StaticModel

  attr_mentionable :safe_message, pipeline: :single_line
  participant :author, :committer, :notes

  attr_accessor :project

  # Safe amount of changes (files and lines) in one commit to render
  # Used to prevent 500 error on huge commits by suppressing diff
  #
  # User can force display of diff above this size
  DIFF_SAFE_FILES  = 100 unless defined?(DIFF_SAFE_FILES)
  DIFF_SAFE_LINES  = 5000 unless defined?(DIFF_SAFE_LINES)

  # Commits above this size will not be rendered in HTML
  DIFF_HARD_LIMIT_FILES = 1000 unless defined?(DIFF_HARD_LIMIT_FILES)
  DIFF_HARD_LIMIT_LINES = 50000 unless defined?(DIFF_HARD_LIMIT_LINES)

  class << self
    def decorate(commits, project)
      commits.map do |commit|
        if commit.kind_of?(Commit)
          commit
        else
          self.new(commit, project)
        end
      end
    end

    # Calculate number of lines to render for diffs
    def diff_line_count(diffs)
      diffs.reduce(0) { |sum, d| sum + d.diff.lines.count }
    end

    # Truncate sha to 8 characters
    def truncate_sha(sha)
      sha[0..7]
    end
  end

  attr_accessor :raw

  def initialize(raw_commit, project)
    raise "Nil as raw commit passed" unless raw_commit

    @raw = raw_commit
    @project = project
  end

  def id
    @raw.id
  end

  def ==(other)
    (self.class === other) && (raw == other.raw)
  end

  def self.reference_prefix
    '@'
  end

  # Pattern used to extract commit references from text
  #
  # The SHA can be between 6 and 40 hex characters.
  #
  # This pattern supports cross-project references.
  def self.reference_pattern
    %r{
      (?:#{Project.reference_pattern}#{reference_prefix})?
      (?<commit>\h{6,40})
    }x
  end

  def self.link_reference_pattern
    super("commit", /(?<commit>\h{6,40})/)
  end

  def to_reference(from_project = nil)
    if cross_project_reference?(from_project)
      project.to_reference + self.class.reference_prefix + self.id
    else
      self.id
    end
  end

  def reference_link_text(from_project = nil)
    if cross_project_reference?(from_project)
      project.to_reference + self.class.reference_prefix + self.short_id
    else
      self.short_id
    end
  end

  def diff_line_count
    @diff_line_count ||= Commit::diff_line_count(self.diffs)
    @diff_line_count
  end

  # Returns a string describing the commit for use in a link title
  #
  # Example
  #
  #   "Commit: Alex Denisov - Project git clone panel"
  def link_title
    "Commit: #{author_name} - #{title}"
  end

  # Returns the commits title.
  #
  # Usually, the commit title is the first line of the commit message.
  # In case this first line is longer than 100 characters, it is cut off
  # after 80 characters and ellipses (`&hellp;`) are appended.
  def title
    title = safe_message

    return no_commit_message if title.blank?

    title_end = title.index("\n")
    if (!title_end && title.length > 100) || (title_end && title_end > 100)
      title[0..79] << "…"
    else
      title.split("\n", 2).first
    end
  end

  # Returns the commits description
  #
  # cut off, ellipses (`&hellp;`) are prepended to the commit message.
  def description
    title_end = safe_message.index("\n")
    @description ||=
      if (!title_end && safe_message.length > 100) || (title_end && title_end > 100)
        "…" << safe_message[80..-1]
      else
        safe_message.split("\n", 2)[1].try(:chomp)
      end
  end

  def description?
    description.present?
  end

  def hook_attrs(with_changed_files: false)
    path_with_namespace = project.path_with_namespace

    data = {
      id: id,
      message: safe_message,
      timestamp: committed_date.xmlschema,
      url: "#{Gitlab.config.gitlab.url}/#{path_with_namespace}/commit/#{id}",
      author: {
        name: author_name,
        email: author_email
      }
    }

    if with_changed_files
      data.merge!(repo_changes)
    end

    data
  end

  # Discover issues should be closed when this commit is pushed to a project's
  # default branch.
  def closes_issues(current_user = self.committer)
    Gitlab::ClosingIssueExtractor.new(project, current_user).closed_by_message(safe_message)
  end

  def author
    @author ||= User.find_by_any_email(author_email.downcase)
  end

  def committer
    @committer ||= User.find_by_any_email(committer_email.downcase)
  end

  def parents
    @parents ||= parent_ids.map { |id| project.commit(id) }
  end

  def parent
    @parent ||= project.commit(self.parent_id) if self.parent_id
  end

  def notes
    project.notes.for_commit_id(self.id)
  end

  def method_missing(m, *args, &block)
    @raw.send(m, *args, &block)
  end

  def respond_to_missing?(method, include_private = false)
    @raw.respond_to?(method, include_private) || super
  end

  # Truncate sha to 8 characters
  def short_id
    @raw.short_id(7)
  end

  def ci_commit
    project.ci_commit(sha)
  end

  def status
    ci_commit.try(:status) || :not_found
  end

  private

  def repo_changes
    changes = { added: [], modified: [], removed: [] }

    diffs.each do |diff|
      if diff.deleted_file
        changes[:removed] << diff.old_path
      elsif diff.renamed_file || diff.new_file
        changes[:added] << diff.new_path
      else
        changes[:modified] << diff.new_path
      end
    end

    changes
  end
end
