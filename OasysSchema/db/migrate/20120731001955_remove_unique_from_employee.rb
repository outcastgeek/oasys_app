class RemoveUniqueFromEmployee < ActiveRecord::Migration
  def up
    remove_column :employees, :unique
  end

  def down
    add_column :employees, :unique, :string
  end
end
