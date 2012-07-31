# This file should contain all the record creation needed to seed the database with its default values.
# The data can then be loaded with the rake db:seed (or created alongside the db with db:setup).
#
# Examples:
#
#   cities = City.create([{ :name => 'Chicago' }, { :name => 'Copenhagen' }])
#   Mayor.create(:name => 'Emanuel', :city => cities.first)

employees = Employee.create([
                             {
                               :active => true,
                               :email => "john.doe@oasys_corp.com",
                               :first_name => "john",
                               :last_name => "doe",
                               :username => "JohnnyDough",
                               :password => "password1",
                               :uniq => "unique1",
                               :provider => "provider1"
                             },
                             {
                               :active => true,
                               :email => "lambert.lambert@oasys_corp.com",
                               :first_name => "lambert",
                               :last_name => "lambert",
                               :username => "LambyLamby",
                               :password => "password2",
                               :uniq => "unique2",
                               :provider => "provider2"
                             }
                            ])

today = Date.today

payroll_cycle = PayrollCycle.create(:check_date => today + 28.days,
                                    :direct_deposit_date => today + 30.days,
                                    :end_date => today,
                                    :payroll_cycle_number => 4,
                                    :payroll_cycle_year => 2012,
                                    :start_date => 2.weeks.ago)

project = Project.create(:client => "Wells Fargo",
                         :description =>"Write a lot of code and Train people!!!!",
                         :name => "TDD geek")

timesheet = TimeSheet.create({
                               :employee_id => employees.first.object_id,
                               :payroll_cycle_id => payroll_cycle.object_id,
                               :start_date => 7.days.ago,
                               :end_date => today + 7.days
                             },
                             :without_protection => true)

work_segments = WorkSegment.create([
                                    {
                                      :project_id => project.object_id,
                                      :timesheet_id => timesheet.object_id,
                                      :payroll_cycle_id => payroll_cycle.object_id,
                                      :employee_id => employees.first.object_id,
                                      :date => 4.days.ago,
                                      :hours => 8.00
                                    },
                                    {
                                      :project_id => project.object_id,
                                      :timesheet_id => timesheet.object_id,
                                      :payroll_cycle_id => payroll_cycle.object_id,
                                      :employee_id => employees.first.object_id,
                                      :date => 8.days.ago,
                                      :hours => 12.00
                                    }
                                   ],
                                   :without_protection => true)



