# Controller for viewing a file's blame
class Projects::BlobController < Projects::ApplicationController
  include ExtractsPath
  include CreatesCommit
  include ActionView::Helpers::SanitizeHelper

  # Raised when given an invalid file path
  class InvalidPathError < StandardError; end

  before_action :require_non_empty_project, except: [:new, :create]
  before_action :authorize_download_code!
  before_action :authorize_edit_tree!, only: [:new, :create, :edit, :update, :destroy]
  before_action :assign_blob_vars
  before_action :commit, except: [:new, :create]
  before_action :blob, except: [:new, :create]
  before_action :from_merge_request, only: [:edit, :update]
  before_action :require_branch_head, only: [:edit, :update]
  before_action :editor_variables, except: [:show, :preview, :diff]

  def new
    commit unless @repository.empty?
  end

  def create
    create_commit(Files::CreateService, success_notice: "The file has been successfully created.",
                                        success_path: namespace_project_blob_path(@project.namespace, @project, File.join(@target_branch, @file_path)),
                                        failure_view: :new,
                                        failure_path: namespace_project_new_blob_path(@project.namespace, @project, @ref))
  end

  def show
  end

  def edit
    @last_commit = Gitlab::Git::Commit.last_for_path(@repository, @ref, @path).sha
  end

  def update
    after_edit_path =
      if from_merge_request && @target_branch == @ref
        diffs_namespace_project_merge_request_path(from_merge_request.target_project.namespace, from_merge_request.target_project, from_merge_request) +
          "#file-path-#{hexdigest(@path)}"
      else
        namespace_project_blob_path(@project.namespace, @project, File.join(@target_branch, @path))
      end

    create_commit(Files::UpdateService, success_path: after_edit_path,
                                        failure_view: :edit,
                                        failure_path: namespace_project_blob_path(@project.namespace, @project, @id))
  end

  def preview
    @content = params[:content]
    diffy = Diffy::Diff.new(@blob.data, @content, diff: '-U 3', include_diff_info: true)
    @diff_lines = Gitlab::Diff::Parser.new.parse(diffy.diff.scan(/.*\n/))

    render layout: false
  end

  def destroy
    create_commit(Files::DeleteService, success_notice: "The file has been successfully deleted.",
                                        success_path: namespace_project_tree_path(@project.namespace, @project, @target_branch),
                                        failure_view: :show,
                                        failure_path: namespace_project_blob_path(@project.namespace, @project, @id))
  end

  def diff
    @form = UnfoldForm.new(params)
    @lines = @blob.data.lines[@form.since - 1..@form.to - 1]

    if @form.bottom?
      @match_line = ''
    else
      lines_length = @lines.length - 1
      line = [@form.since, lines_length].join(',')
      @match_line = "@@ -#{line}+#{line} @@"
    end

    render layout: false
  end

  private

  def blob
    @blob ||= @repository.blob_at(@commit.id, @path)

    if @blob
      @blob
    else
      if tree = @repository.tree(@commit.id, @path)
        if tree.entries.any?
          redirect_to namespace_project_tree_path(@project.namespace, @project, File.join(@ref, @path)) and return
        end
      end

      return render_404
    end
  end

  def commit
    @commit = @repository.commit(@ref)

    return render_404 unless @commit
  end

  def assign_blob_vars
    @id = params[:id]
    @ref, @path = extract_ref(@id)

  rescue InvalidPathError
    render_404
  end

  def from_merge_request
    # If blob edit was initiated from merge request page
    @from_merge_request ||= MergeRequest.find_by(id: params[:from_merge_request_id])
  end

  def editor_variables
    @target_branch = params[:target_branch]

    @file_path =
      if action_name.to_s == 'create'
        if params[:file].present?
          params[:file_name] = params[:file].original_filename
        end
        File.join(@path, params[:file_name])
      else
        @path
      end

    if params[:file].present?
      params[:content] = Base64.encode64(params[:file].read)
      params[:encoding] = 'base64'
    end

    @commit_params = {
      file_path: @file_path,
      commit_message: params[:commit_message],
      file_content: params[:content],
      file_content_encoding: params[:encoding]
    }
  end
end
