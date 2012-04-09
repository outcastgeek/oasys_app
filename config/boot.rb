# Defines our constants
PADRINO_ENV  = ENV['PADRINO_ENV'] ||= ENV['RACK_ENV'] ||= 'development'  unless defined?(PADRINO_ENV)
PADRINO_ROOT = File.expand_path('../..', __FILE__) unless defined?(PADRINO_ROOT)

PADRINO_LOGGER = {
    #:production  => { :log_level => :warn, :stream => :to_file },
    :production  => { :log_level => :debug, :stream => :to_file },
    #:development => { :log_level => :debug, :stream => :stdout },
    :development => { :log_level => :debug, :stream => :stdout, :format_datetime => ' ' },
    :test        => { :log_level => :fatal, :stream => :null }
}

# Load our dependencies
require 'rubygems' unless defined?(Gem)
require 'bundler/setup'
Bundler.require(:default, PADRINO_ENV)

##
# Enable devel logging
#
# Padrino::Logger::Config[:development][:log_level]  = :devel
# Padrino::Logger::Config[:development][:log_static] = true
#

##
# Add your before load hooks here
#
Padrino.before_load do
end

##
# Add your after load hooks here
#
Padrino.after_load do
end

Padrino.load!
