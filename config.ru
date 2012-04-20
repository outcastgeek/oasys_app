#!/usr/bin/env rackup
# encoding: utf-8

# This file can be used to start Padrino,
# just execute it from the command line.

require File.expand_path("../config/boot.rb", __FILE__)

#https://github.com/petebrowne/sprockets-sass/issues/7
#https://gist.github.com/2174026
#http://www.simonecarletti.com/blog/2011/09/using-sprockets-without-a-railsrack-project/
#https://github.com/petebrowne/sprockets-sass

require 'sprockets'
require 'compass'
require 'sprockets-sass'
require 'bootstrap-sass'
require 'sass'
require 'compass'
require 'closure-compiler'
require 'digest/md5'
#require 'digest/sha1'

project_root = File.expand_path(File.dirname(__FILE__))

map '/assets' do
  environment  = Sprockets::Environment.new(project_root) do |env|
    env.logger = Logger.new(STDOUT)
  end
  environment.append_path 'app/assets/javascripts'
  environment.append_path 'app/assets/stylesheets'
  environment.append_path 'app/assets/images'

  # Adds Twitter Bootstrap Javascripts
  environment.append_path Compass::Frameworks['bootstrap'].templates_directory + '/../vendor/assets/javascripts'

  environment.digest_class = Digest::MD5
  #environment.digest_class = Digest::SHA1
  #environment.css_compressor = YUI::CssCompressor.new
  environment.js_compressor  = Closure::Compiler.new(:compilation_level => 'SIMPLE_OPTIMIZATIONS') if PADRINO_ENV == 'production'
  #environment.js_compressor  = Closure::Compiler.new(:compilation_level => 'ADVANCED_OPTIMIZATIONS')

  Sprockets::Helpers.configure do |config|
    config.environment = environment
    config.prefix      = '/assets'
    #config.digest      = true
    config.public_path = './public'
  end

  run environment
end

map '/' do
  run Padrino.application
end

#run Padrino.application
