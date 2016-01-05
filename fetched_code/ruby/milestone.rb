# == Schema Information
#
# Table name: milestones
#
#  id          :integer          not null, primary key
#  title       :string(255)      not null
#  project_id  :integer          not null
#  description :text
#  due_date    :date
#  created_at  :datetime
#  updated_at  :datetime
#  state       :string(255)
#  iid         :integer
#

class Milestone < ActiveRecord::Base
  # Represents a "No Milestone" state used for filtering Issues and Merge
  # Requests that have no milestone assigned.
  MilestoneStruct = Struct.new(:title, :name, :id)
  None = MilestoneStruct.new('No Milestone', 'No Milestone', 0)
  Any = MilestoneStruct.new('Any Milestone', '', -1)

  include InternalId
  include Sortable
  include StripAttribute

  belongs_to :project
  has_many :issues
  has_many :merge_requests
  has_many :participants, through: :issues, source: :assignee

  scope :active, -> { with_state(:active) }
  scope :closed, -> { with_state(:closed) }
  scope :of_projects, ->(ids) { where(project_id: ids) }

  validates :title, presence: true
  validates :project, presence: true

  strip_attributes :title

  state_machine :state, initial: :active do
    event :close do
      transition active: :closed
    end

    event :activate do
      transition closed: :active
    end

    state :closed

    state :active
  end

  alias_attribute :name, :title

  class << self
    def search(query)
      query = "%#{query}%"
      where("title like ? or description like ?", query, query)
    end
  end

  def expired?
    if due_date
      due_date.past?
    else
      false
    end
  end

  def open_items_count
    self.issues.opened.count + self.merge_requests.opened.count
  end

  def closed_items_count
    self.issues.closed.count + self.merge_requests.closed_and_merged.count
  end

  def total_items_count
    self.issues.count + self.merge_requests.count
  end

  def percent_complete
    ((closed_items_count * 100) / total_items_count).abs
  rescue ZeroDivisionError
    0
  end

  def expires_at
    if due_date
      if due_date.past?
        "expired at #{due_date.stamp("Aug 21, 2011")}"
      else
        "expires at #{due_date.stamp("Aug 21, 2011")}"
      end
    end
  end

  def can_be_closed?
    active? && issues.opened.count.zero?
  end

  def is_empty?
    total_items_count.zero?
  end

  def author_id
    nil
  end

  # Sorts the issues for the given IDs.
  #
  # This method runs a single SQL query using a CASE statement to update the
  # position of all issues in the current milestone (scoped to the list of IDs).
  #
  # Given the ids [10, 20, 30] this method produces a SQL query something like
  # the following:
  #
  #     UPDATE issues
  #     SET position = CASE
  #       WHEN id = 10 THEN 1
  #       WHEN id = 20 THEN 2
  #       WHEN id = 30 THEN 3
  #       ELSE position
  #     END
  #     WHERE id IN (10, 20, 30);
  #
  # This method expects that the IDs given in `ids` are already Fixnums.
  def sort_issues(ids)
    pairs = []

    ids.each_with_index do |id, index|
      pairs << id
      pairs << index + 1
    end

    conditions = 'WHEN id = ? THEN ? ' * ids.length

    issues.where(id: ids).
      update_all(["position = CASE #{conditions} ELSE position END", *pairs])
  end
end
