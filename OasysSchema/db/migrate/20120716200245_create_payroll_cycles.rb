class CreatePayrollCycles < ActiveRecord::Migration
  def change
    create_table :payroll_cycles do |t|
      t.integer :payroll_cycle_year
      t.integer :payroll_cycle_number
      t.date :start_date
      t.date :end_date
      t.date :direct_deposit_date
      t.date :check_date

      t.timestamps
    end
  end
end
