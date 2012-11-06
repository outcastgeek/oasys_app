
require 'celluloid'

class TimeServer
  include Celluloid

  def time
    "The time is: #{Time.now}"
  end
end

TimeServer.supervise_as :time_server
