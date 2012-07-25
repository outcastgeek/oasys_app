class AddStartDateAndEndDateToTimeSheet < ActiveRecord::Migration
  def change
    add_column :time_sheets, :start_date, :date
    add_column :time_sheets, :end_date, :date
  end
end
