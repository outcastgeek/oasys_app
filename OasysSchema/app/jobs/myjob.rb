
require 'celluloid'
require 'resque-history'

class MyJob
  extend Resque::Plugins::History

  @actor = Celluloid::Actor[:time_server]
  @max_history = 50 # max number of histories to be kept
  @queue = :my_job_queue
  @logger = Rails.logger
  def self.perform()
    # Do anything here, like access models, etc
    @logger.info "Doing my job"
    @logger.info @actor.time
  end
end
