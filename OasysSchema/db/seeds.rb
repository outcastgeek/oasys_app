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
                               :username => "JohnnyDough"
                             },
                             {
                               :active => true,
                               :email => "lambert.lambert@oasys_corp.com",
                               :first_name => "lambert",
                               :last_name => "lambert",
                               :username => "LambyLamby"
                             }
                            ])

today = Date.today

payroll_cycle = PayrollCycle.create(:check_date => today + 28.days,
                                    :direct_deposit_date => today + 30.days,
                                    :end_date => today,
                                    :payroll_cycle => 4,
                                    :payroll_cycle_year => 2012,
                                    :start_date => 2.weeks.ago)

project = Project.new(:client => "Wells Fargo",
                      :description =>"Write a lot of code and Train people!!!!",
                      :name => "TDD geek")

