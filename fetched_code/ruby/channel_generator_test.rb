require 'generators/generators_test_helper'
require 'rails/generators/channel/channel_generator'

class ChannelGeneratorTest < Rails::Generators::TestCase
  include GeneratorsTestHelper
  tests Rails::Generators::ChannelGenerator

  def test_channel_is_created
    run_generator ['chat']

    assert_file "app/channels/chat_channel.rb" do |channel|
      assert_match(/class ChatChannel < ApplicationCable::Channel/, channel)
    end

    assert_file "app/assets/javascripts/channels/chat.coffee" do |channel|
      assert_match(/App.cable.subscriptions.create "ChatChannel"/, channel)
    end
  end

  def test_channel_asset_is_not_created_when_skip_assets_is_passed
    run_generator ['chat', '--skip-assets']

    assert_file "app/channels/chat_channel.rb" do |channel|
      assert_match(/class ChatChannel < ApplicationCable::Channel/, channel)
    end

    assert_no_file "app/assets/javascripts/channels/chat.coffee"
  end
end
