class PayrollCycle < ActiveRecord::Base
  attr_accessible :check_date, :direct_deposit_date, :end_date, :payroll_cycle_number, :payroll_cycle_year, :start_date
end
