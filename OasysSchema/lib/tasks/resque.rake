
require 'resque/tasks'
require 'resque_scheduler/tasks'

task "resque:setup" => :environment do
  ENV['QUEUE'] = '*'
  ENV['COUNT'] = '128'
  ENV['VERBOSE '] = '0'
  ENV['VVERBOSE '] = '0'
end

