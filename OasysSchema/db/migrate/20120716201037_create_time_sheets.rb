class CreateTimeSheets < ActiveRecord::Migration
  def change
    create_table :time_sheets do |t|
      t.references :employee
      t.references :payroll_cycle

      t.timestamps
    end
    add_index :time_sheets, :employee_id
    add_index :time_sheets, :payroll_cycle_id
  end
end
