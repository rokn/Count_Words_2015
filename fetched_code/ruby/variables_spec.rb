require 'spec_helper'

describe "Variables" do
  let(:user) { create(:user) }
  before { login_as(user) }

  describe "specific runners" do
    before do
      @project = FactoryGirl.create :empty_project
      @project.team << [user, :master]
    end

    it "creates variable", js: true do
      visit namespace_project_variables_path(@project.namespace, @project)
      click_on "Add a variable"
      fill_in "Key", with: "SECRET_KEY"
      fill_in "Value", with: "SECRET_VALUE"
      click_on "Save changes"

      expect(page).to have_content("Variables were successfully updated.")
      expect(@project.variables.count).to eq(1)
    end
  end
end
