
require 'resque-history'

class PayrollCycleCreator
  extend Resque::Plugins::History

  @max_history = 50 # max number of histories to be kept
  @queue = :payroll_cycle_queue
  @logger = Rails.logger

  def self.perform
    @logger.debug "Creating payroll cycles for employees...."
  end
end
