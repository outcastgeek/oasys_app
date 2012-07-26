class AddUniqueAndProviderToEmployee < ActiveRecord::Migration
  def change
    add_column :employees, :unique, :string
    add_column :employees, :provider, :string
  end
end
