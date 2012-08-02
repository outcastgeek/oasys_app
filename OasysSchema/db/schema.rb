# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20120731185518) do

  create_table "employees", :force => true do |t|
    t.string   "first_name"
    t.string   "last_name"
    t.string   "username"
    t.string   "email"
    t.boolean  "active"
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
    t.string   "provider"
    t.string   "uniq"
    t.string   "password"
  end

  create_table "payroll_cycles", :force => true do |t|
    t.integer  "payroll_cycle_year"
    t.integer  "payroll_cycle_number"
    t.date     "start_date"
    t.date     "end_date"
    t.date     "direct_deposit_date"
    t.date     "check_date"
    t.datetime "created_at",           :null => false
    t.datetime "updated_at",           :null => false
  end

  create_table "projects", :force => true do |t|
    t.string   "name"
    t.string   "client"
    t.text     "description"
    t.datetime "created_at",  :null => false
    t.datetime "updated_at",  :null => false
  end

  create_table "time_sheets", :force => true do |t|
    t.integer  "employee_id"
    t.integer  "payroll_cycle_id"
    t.datetime "created_at",       :null => false
    t.datetime "updated_at",       :null => false
    t.date     "start_date"
    t.date     "end_date"
  end

  add_index "time_sheets", ["employee_id"], :name => "index_time_sheets_on_employee_id"
  add_index "time_sheets", ["payroll_cycle_id"], :name => "index_time_sheets_on_payroll_cycle_id"

  create_table "work_segments", :force => true do |t|
    t.integer  "project_id"
    t.integer  "timesheet_id"
    t.integer  "payroll_cycle_id"
    t.integer  "employee_id"
    t.date     "date"
    t.datetime "created_at",       :null => false
    t.datetime "updated_at",       :null => false
    t.float    "hours"
  end

  add_index "work_segments", ["employee_id"], :name => "index_work_segments_on_employee_id"
  add_index "work_segments", ["payroll_cycle_id"], :name => "index_work_segments_on_payroll_cycle_id"
  add_index "work_segments", ["project_id"], :name => "index_work_segments_on_project_id"
  add_index "work_segments", ["timesheet_id"], :name => "index_work_segments_on_timesheet_id"

end
