class CreateWorkSegments < ActiveRecord::Migration
  def change
    create_table :work_segments do |t|
      t.references :project
      t.references :timesheet
      t.references :payroll_cycle
      t.references :employee
      t.date :date
      t.date :start_time
      t.time :end_time

      t.timestamps
    end
    add_index :work_segments, :project_id
    add_index :work_segments, :timesheet_id
    add_index :work_segments, :payroll_cycle_id
    add_index :work_segments, :employee_id
  end
end
