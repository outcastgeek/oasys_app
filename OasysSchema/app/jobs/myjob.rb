
require 'resque-history'

class MyJob
  extend Resque::Plugins::History

  @max_history = 50 # max number of histories to be kept
  @queue = :my_job_queue
  @logger = Rails.logger
  def self.perform()
    # Do anything here, like access models, etc
    @logger.info "Doing my job"
  end
end
