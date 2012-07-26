class Employee < ActiveRecord::Base
  attr_accessible :active, :email, :first_name, :last_name, :username, :unique, :provider
end
