
require 'resque-history'

class MyJob
  extend Resque::Plugins::History

  @max_history = 50 # max number of histories to be kept
  @queue = :my_job_queue
  @logger = Rails.logger

  def self.perform(name)
    @logger.debug "Hello, #{name}!!!"
    @logger.debug "This job was rocessed at #{Time.now}!"
  end
end
