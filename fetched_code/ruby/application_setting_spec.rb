# == Schema Information
#
# Table name: application_settings
#
#  id                           :integer          not null, primary key
#  default_projects_limit       :integer
#  signup_enabled               :boolean
#  signin_enabled               :boolean
#  gravatar_enabled             :boolean
#  sign_in_text                 :text
#  created_at                   :datetime
#  updated_at                   :datetime
#  home_page_url                :string(255)
#  default_branch_protection    :integer          default(2)
#  twitter_sharing_enabled      :boolean          default(TRUE)
#  restricted_visibility_levels :text
#  version_check_enabled        :boolean          default(TRUE)
#  max_attachment_size          :integer          default(10), not null
#  default_project_visibility   :integer
#  default_snippet_visibility   :integer
#  restricted_signup_domains    :text
#  user_oauth_applications      :boolean          default(TRUE)
#  after_sign_out_path          :string(255)
#  session_expire_delay         :integer          default(10080), not null
#  import_sources               :text
#  help_page_text               :text
#  admin_notification_email     :string(255)
#  shared_runners_enabled       :boolean          default(TRUE), not null
#  max_artifacts_size           :integer          default(100), not null
#  runners_registration_token   :string(255)
#

require 'spec_helper'

describe ApplicationSetting, models: true do
  let(:setting) { ApplicationSetting.create_from_defaults }

  it { expect(setting).to be_valid }

  describe 'validations' do
    let(:http)  { 'http://example.com' }
    let(:https) { 'https://example.com' }
    let(:ftp)   { 'ftp://example.com' }

    it { is_expected.to allow_value(nil).for(:home_page_url) }
    it { is_expected.to allow_value(http).for(:home_page_url) }
    it { is_expected.to allow_value(https).for(:home_page_url) }
    it { is_expected.not_to allow_value(ftp).for(:home_page_url) }

    it { is_expected.to allow_value(nil).for(:after_sign_out_path) }
    it { is_expected.to allow_value(http).for(:after_sign_out_path) }
    it { is_expected.to allow_value(https).for(:after_sign_out_path) }
    it { is_expected.not_to allow_value(ftp).for(:after_sign_out_path) }
  end

  context 'restricted signup domains' do
    it 'set single domain' do
      setting.restricted_signup_domains_raw = 'example.com'
      expect(setting.restricted_signup_domains).to eq(['example.com'])
    end

    it 'set multiple domains with spaces' do
      setting.restricted_signup_domains_raw = 'example.com *.example.com'
      expect(setting.restricted_signup_domains).to eq(['example.com', '*.example.com'])
    end

    it 'set multiple domains with newlines and a space' do
      setting.restricted_signup_domains_raw = "example.com\n *.example.com"
      expect(setting.restricted_signup_domains).to eq(['example.com', '*.example.com'])
    end

    it 'set multiple domains with commas' do
      setting.restricted_signup_domains_raw = "example.com, *.example.com"
      expect(setting.restricted_signup_domains).to eq(['example.com', '*.example.com'])
    end
  end
end
