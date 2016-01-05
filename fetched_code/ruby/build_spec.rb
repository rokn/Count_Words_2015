# == Schema Information
#
# Table name: builds
#
#  id                 :integer          not null, primary key
#  project_id         :integer
#  status             :string(255)
#  finished_at        :datetime
#  trace              :text
#  created_at         :datetime
#  updated_at         :datetime
#  started_at         :datetime
#  runner_id          :integer
#  commit_id          :integer
#  coverage           :float
#  commands           :text
#  job_id             :integer
#  name               :string(255)
#  deploy             :boolean          default(FALSE)
#  options            :text
#  allow_failure      :boolean          default(FALSE), not null
#  stage              :string(255)
#  trigger_request_id :integer
#

require 'spec_helper'

describe Ci::Build, models: true do
  let(:project) { FactoryGirl.create :empty_project }
  let(:commit) { FactoryGirl.create :ci_commit, project: project }
  let(:build) { FactoryGirl.create :ci_build, commit: commit }

  it { is_expected.to validate_presence_of :ref }

  it { is_expected.to respond_to :trace_html }

  describe :first_pending do
    let(:first) { FactoryGirl.create :ci_build, commit: commit, status: 'pending', created_at: Date.yesterday }
    let(:second) { FactoryGirl.create :ci_build, commit: commit, status: 'pending' }
    before { first; second }
    subject { Ci::Build.first_pending }

    it { is_expected.to be_a(Ci::Build) }
    it('returns with the first pending build') { is_expected.to eq(first) }
  end

  describe :create_from do
    before do
      build.status = 'success'
      build.save
    end
    let(:create_from_build) { Ci::Build.create_from build }

    it 'there should be a pending task' do
      expect(Ci::Build.pending.count(:all)).to eq 0
      create_from_build
      expect(Ci::Build.pending.count(:all)).to be > 0
    end
  end

  describe :ignored? do
    subject { build.ignored? }

    context 'if build is not allowed to fail' do
      before { build.allow_failure = false }

      context 'and build.status is success' do
        before { build.status = 'success' }

        it { is_expected.to be_falsey }
      end

      context 'and build.status is failed' do
        before { build.status = 'failed' }

        it { is_expected.to be_falsey }
      end
    end

    context 'if build is allowed to fail' do
      before { build.allow_failure = true }

      context 'and build.status is success' do
        before { build.status = 'success' }

        it { is_expected.to be_falsey }
      end

      context 'and build.status is failed' do
        before { build.status = 'failed' }

        it { is_expected.to be_truthy }
      end
    end
  end

  describe :trace do
    subject { build.trace_html }

    it { is_expected.to be_empty }

    context 'if build.trace contains text' do
      let(:text) { 'example output' }
      before { build.trace = text }

      it { is_expected.to include(text) }
      it { expect(subject.length).to be >= text.length }
    end

    context 'if build.trace hides token' do
      let(:token) { 'my_secret_token' }

      before do
        build.project.update_attributes(runners_token: token)
        build.update_attributes(trace: token)
      end

      it { is_expected.to_not include(token) }
    end
  end

  # TODO: build timeout
  # describe :timeout do
  #   subject { build.timeout }
  #
  #   it { is_expected.to eq(commit.project.timeout) }
  # end

  describe :options do
    let(:options) do
      {
        image: "ruby:2.1",
        services: [
          "postgres"
        ]
      }
    end

    subject { build.options }
    it { is_expected.to eq(options) }
  end

  # TODO: allow_git_fetch
  # describe :allow_git_fetch do
  #   subject { build.allow_git_fetch }
  #
  #   it { is_expected.to eq(project.allow_git_fetch) }
  # end

  describe :project do
    subject { build.project }

    it { is_expected.to eq(commit.project) }
  end

  describe :project_id do
    subject { build.project_id }

    it { is_expected.to eq(commit.project_id) }
  end

  describe :project_name do
    subject { build.project_name }

    it { is_expected.to eq(project.name) }
  end

  describe :extract_coverage do
    context 'valid content & regex' do
      subject { build.extract_coverage('Coverage 1033 / 1051 LOC (98.29%) covered', '\(\d+.\d+\%\) covered') }

      it { is_expected.to eq(98.29) }
    end

    context 'valid content & bad regex' do
      subject { build.extract_coverage('Coverage 1033 / 1051 LOC (98.29%) covered', 'very covered') }

      it { is_expected.to be_nil }
    end

    context 'no coverage content & regex' do
      subject { build.extract_coverage('No coverage for today :sad:', '\(\d+.\d+\%\) covered') }

      it { is_expected.to be_nil }
    end

    context 'multiple results in content & regex' do
      subject { build.extract_coverage(' (98.39%) covered. (98.29%) covered', '\(\d+.\d+\%\) covered') }

      it { is_expected.to eq(98.29) }
    end

    context 'using a regex capture' do
      subject { build.extract_coverage('TOTAL      9926   3489    65%', 'TOTAL\s+\d+\s+\d+\s+(\d{1,3}\%)') }

      it { is_expected.to eq(65) }
    end
  end

  describe :variables do
    context 'returns variables' do
      subject { build.variables }

      let(:predefined_variables) do
        [
          { key: :CI_BUILD_NAME, value: 'test', public: true },
          { key: :CI_BUILD_STAGE, value: 'stage', public: true },
        ]
      end

      let(:yaml_variables) do
        [
          { key: :DB_NAME, value: 'postgres', public: true }
        ]
      end

      before { build.update_attributes(stage: 'stage') }

      it { is_expected.to eq(predefined_variables + yaml_variables) }

      context 'for tag' do
        let(:tag_variable) do
          [
            { key: :CI_BUILD_TAG, value: 'master', public: true }
          ]
        end

        before { build.update_attributes(tag: true) }

        it { is_expected.to eq(tag_variable + predefined_variables + yaml_variables) }
      end

      context 'and secure variables' do
        let(:secure_variables) do
          [
            { key: 'SECRET_KEY', value: 'secret_value', public: false }
          ]
        end

        before do
          build.project.variables << Ci::Variable.new(key: 'SECRET_KEY', value: 'secret_value')
        end

        it { is_expected.to eq(predefined_variables + yaml_variables + secure_variables) }

        context 'and trigger variables' do
          let(:trigger) { FactoryGirl.create :ci_trigger, project: project }
          let(:trigger_request) { FactoryGirl.create :ci_trigger_request_with_variables, commit: commit, trigger: trigger }
          let(:trigger_variables) do
            [
              { key: :TRIGGER_KEY, value: 'TRIGGER_VALUE', public: false }
            ]
          end
          let(:predefined_trigger_variable) do
            [
              { key: :CI_BUILD_TRIGGERED, value: 'true', public: true }
            ]
          end

          before do
            build.trigger_request = trigger_request
          end

          it { is_expected.to eq(predefined_variables + predefined_trigger_variable + yaml_variables + secure_variables + trigger_variables) }
        end
      end
    end
  end

  describe :can_be_served? do
    let(:runner) { FactoryGirl.create :ci_specific_runner }

    before { build.project.runners << runner }

    context 'runner without tags' do
      it 'can handle builds without tags' do
        expect(build.can_be_served?(runner)).to be_truthy
      end

      it 'cannot handle build with tags' do
        build.tag_list = ['aa']
        expect(build.can_be_served?(runner)).to be_falsey
      end
    end

    context 'runner with tags' do
      before { runner.tag_list = ['bb', 'cc'] }

      it 'can handle builds without tags' do
        expect(build.can_be_served?(runner)).to be_truthy
      end

      it 'can handle build with matching tags' do
        build.tag_list = ['bb']
        expect(build.can_be_served?(runner)).to be_truthy
      end

      it 'cannot handle build with not matching tags' do
        build.tag_list = ['aa']
        expect(build.can_be_served?(runner)).to be_falsey
      end
    end
  end

  describe :any_runners_online? do
    subject { build.any_runners_online? }

    context 'when no runners' do
      it { is_expected.to be_falsey }
    end

    context 'if there are runner' do
      let(:runner) { FactoryGirl.create :ci_specific_runner }

      before do
        build.project.runners << runner
        runner.update_attributes(contacted_at: 1.second.ago)
      end

      it { is_expected.to be_truthy }

      it 'that is inactive' do
        runner.update_attributes(active: false)
        is_expected.to be_falsey
      end

      it 'that is not online' do
        runner.update_attributes(contacted_at: nil)
        is_expected.to be_falsey
      end

      it 'that cannot handle build' do
        expect_any_instance_of(Ci::Build).to receive(:can_be_served?).and_return(false)
        is_expected.to be_falsey
      end

    end
  end

  describe :show_warning? do
    subject { build.show_warning? }

    %w(pending).each do |state|
      context "if commit_status.status is #{state}" do
        before { build.status = state }

        it { is_expected.to be_truthy }

        context "and there are specific runner" do
          let(:runner) { FactoryGirl.create :ci_specific_runner, contacted_at: 1.second.ago }

          before do
            build.project.runners << runner
            runner.save
          end

          it { is_expected.to be_falsey }
        end
      end
    end

    %w(success failed canceled running).each do |state|
      context "if commit_status.status is #{state}" do
        before { build.status = state }

        it { is_expected.to be_falsey }
      end
    end
  end

  describe :download_url do
    subject { build.download_url }

    it "should be nil if artifact doesn't exist" do
      build.update_attributes(artifacts_file: nil)
      is_expected.to be_nil
    end

    it 'should be nil if artifact exist' do
      gif = fixture_file_upload(Rails.root + 'spec/fixtures/banana_sample.gif', 'image/gif')
      build.update_attributes(artifacts_file: gif)
      is_expected.to_not be_nil
    end
  end

  describe :repo_url do
    let(:build) { FactoryGirl.create :ci_build }
    let(:project) { build.project }

    subject { build.repo_url }

    it { is_expected.to be_a(String) }
    it { is_expected.to end_with(".git") }
    it { is_expected.to start_with(project.web_url[0..6]) }
    it { is_expected.to include(build.token) }
    it { is_expected.to include('gitlab-ci-token') }
    it { is_expected.to include(project.web_url[7..-1]) }
  end

  def create_mr(build, commit, factory: :merge_request, created_at: Time.now)
    FactoryGirl.create(factory,
                       source_project_id: commit.gl_project_id,
                       target_project_id: commit.gl_project_id,
                       source_branch: build.ref,
                       created_at: created_at)
  end

  describe :merge_request do
    context 'when a MR has a reference to the commit' do
      before do
        @merge_request = create_mr(build, commit, factory: :merge_request)

        commits = [double(id: commit.sha)]
        allow(@merge_request).to receive(:commits).and_return(commits)
        allow(MergeRequest).to receive_message_chain(:includes, :where, :reorder).and_return([@merge_request])
      end

      it 'returns the single associated MR' do
        expect(build.merge_request.id).to eq(@merge_request.id)
      end
    end

    context 'when there is not a MR referencing the commit' do
      it 'returns nil' do
        expect(build.merge_request).to be_nil
      end
    end

    context 'when more than one MR have a reference to the commit' do
      before do
        @merge_request = create_mr(build, commit, factory: :merge_request)
        @merge_request.close!
        @merge_request2 = create_mr(build, commit, factory: :merge_request)

        commits = [double(id: commit.sha)]
        allow(@merge_request).to receive(:commits).and_return(commits)
        allow(@merge_request2).to receive(:commits).and_return(commits)
        allow(MergeRequest).to receive_message_chain(:includes, :where, :reorder).and_return([@merge_request, @merge_request2])
      end

      it 'returns the first MR' do
        expect(build.merge_request.id).to eq(@merge_request.id)
      end
    end

    context 'when a Build is created after the MR' do
      before do
        @merge_request = create_mr(build, commit, factory: :merge_request_with_diffs)
        commit2 = FactoryGirl.create :ci_commit, project: project
        @build2 = FactoryGirl.create :ci_build, commit: commit2

        commits = [double(id: commit.sha), double(id: commit2.sha)]
        allow(@merge_request).to receive(:commits).and_return(commits)
        allow(MergeRequest).to receive_message_chain(:includes, :where, :reorder).and_return([@merge_request])
      end

      it 'returns the current MR' do
        expect(@build2.merge_request.id).to eq(@merge_request.id)
      end
    end

  end
end
