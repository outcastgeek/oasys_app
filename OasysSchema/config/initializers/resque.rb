
rails_root = Rails.root || File.dirname(__FILE__) + '/../..'
rails_env = Rails.env || 'development'

resque_config = YAML.load_file(rails_root.to_s + '/config/resque.yml')
Resque.redis = resque_config[rails_env]
Resque.redis.namespace = "OutcastgeekJobs"
Resque.schedule = YAML.load_file(rails_root.to_s + '/config/resque_schedule.yml') # load the schedule
Dir["#{Rails.root}/app/workers/*.rb"].each { |file| require file }
Dir["#{Rails.root}/app/jobs/*.rb"].each { |file| require file }
