class Project < ActiveRecord::Base
  attr_accessible :client, :description, :name
end
