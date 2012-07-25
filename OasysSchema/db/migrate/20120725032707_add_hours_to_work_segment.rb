class AddHoursToWorkSegment < ActiveRecord::Migration
  def change
    add_column :work_segments, :hours, :float
  end
end
