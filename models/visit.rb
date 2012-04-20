class Visit
  include Mongoid::Document
  include Mongoid::Timestamps # adds created_at and updated_at fields
  include Geocoder::Model::Mongoid

  # field <name>, :type => <type>, :default => <value>
  field :coordinates, :type => Array
  field :address, :default => ""

  belongs_to :link

  # You can define indexes on documents using the index macro:
  # index :field <, :unique => true>

  # You can create a composite key in mongoid to replace the default id using the key macro:
  # key :field <, :another_field, :one_more ....>

  geocoded_by :address               # can also be an IP address

  reverse_geocoded_by :coordinates

  after_validation :geocode,          # auto-fetch coordinates
                   :reverse_geocode  # auto-fetch address
end
