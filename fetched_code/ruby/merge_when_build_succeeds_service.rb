module MergeRequests
  class MergeWhenBuildSucceedsService < MergeRequests::BaseService
    # Marks the passed `merge_request` to be merged when the build succeeds or
    # updates the params for the automatic merge
    def execute(merge_request)
      merge_request.merge_params.merge!(params)

      # The service is also called when the merge params are updated.
      already_approved = merge_request.merge_when_build_succeeds?

      unless already_approved
        merge_request.merge_when_build_succeeds = true
        merge_request.merge_user                = @current_user

        SystemNoteService.merge_when_build_succeeds(merge_request, @project, @current_user, merge_request.last_commit)
      end

      merge_request.save
    end

    # Triggers the automatic merge of merge_request once the build succeeds
    def trigger(build)
      merge_requests = merge_request_from(build)

      merge_requests.each do |merge_request|
        next unless merge_request.merge_when_build_succeeds?

        if merge_request.ci_commit && merge_request.ci_commit.success? && merge_request.mergeable?
          MergeWorker.perform_async(merge_request.id, merge_request.merge_user_id, merge_request.merge_params)
        end
      end
    end

    # Cancels the automatic merge
    def cancel(merge_request)
      if merge_request.merge_when_build_succeeds? && merge_request.open?
        merge_request.reset_merge_when_build_succeeds
        SystemNoteService.cancel_merge_when_build_succeeds(merge_request, @project, @current_user)

        success
      else
        error("Can't cancel the automatic merge", 406)
      end
    end

    private

    def merge_request_from(build)
      merge_requests = @project.origin_merge_requests.opened.where(source_branch: build.ref).to_a
      merge_requests += @project.fork_merge_requests.opened.where(source_branch: build.ref).to_a

      merge_requests.uniq.select(&:source_project)
    end
  end
end
