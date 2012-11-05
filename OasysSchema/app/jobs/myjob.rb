
module MyJob
  #include Celluloid
  #include Celluloid::Logger

  @queue = :my_job_queue
  def self.perform()
    # Do anything here, like access models, etc
    puts "Doing my job"
    #info "Doing my job"
  end
end
