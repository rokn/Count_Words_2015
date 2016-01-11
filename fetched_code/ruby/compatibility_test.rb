require 'cases/helper'

module ActiveRecord
  class Migration
    class CompatibilityTest < ActiveRecord::TestCase
      attr_reader :connection
      self.use_transactional_tests = false

      def setup
        super
        @connection = ActiveRecord::Base.connection
        @verbose_was = ActiveRecord::Migration.verbose
        ActiveRecord::Migration.verbose = false

        connection.create_table :testings do |t|
          t.column :foo, :string, :limit => 100
          t.column :bar, :string, :limit => 100
        end
      end

      teardown do
        connection.drop_table :testings rescue nil
        ActiveRecord::Migration.verbose = @verbose_was
      end

      def test_migration_doesnt_remove_named_index
        connection.add_index :testings, :foo, :name => "custom_index_name"

        migration = Class.new(ActiveRecord::Migration[4.2]) {
          def version; 101 end
          def migrate(x)
            remove_index :testings, :foo
          end
        }.new

        assert connection.index_exists?(:testings, :foo, name: "custom_index_name")
        assert_raise(StandardError) { ActiveRecord::Migrator.new(:up, [migration]).migrate }
        assert connection.index_exists?(:testings, :foo, name: "custom_index_name")
      end
    end
  end
end
