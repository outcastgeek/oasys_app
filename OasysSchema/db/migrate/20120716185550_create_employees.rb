class CreateEmployees < ActiveRecord::Migration
  def change
    create_table :employees do |t|
      t.string :first_name
      t.string :last_name
      t.string :username
      t.string :email
      t.boolean :active

      t.timestamps
    end
  end
end
