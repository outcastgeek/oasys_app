class TimeSheet < ActiveRecord::Base
  belongs_to :employee
  belongs_to :payroll_cycle
  # attr_accessible :title, :body
end
