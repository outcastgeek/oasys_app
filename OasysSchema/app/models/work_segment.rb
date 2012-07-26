class WorkSegment < ActiveRecord::Base
  belongs_to :project
  belongs_to :timesheet
  belongs_to :payroll_cycle
  belongs_to :employee
  attr_accessible :date, :hours
end
