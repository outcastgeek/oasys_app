class AddUniqToEmployee < ActiveRecord::Migration
  def change
    add_column :employees, :uniq, :string
  end
end
