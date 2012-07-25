class RemoveStartTimeAndEndTimeFromWorkSegment < ActiveRecord::Migration
  def up
    remove_column :work_segments, :start_time
    remove_column :work_segments, :end_time
  end

  def down
    add_column :work_segments, :end_time, :date
    add_column :work_segments, :start_time, :date
  end
end
