class Account
  include Mongoid::Document
  include Mongoid::Timestamps # adds created_at and updated_at fields

  # field <name>, :type => <type>, :default => <value>
  field :name, :type => String
  field :email, :type => String
  field :role, :type => String
  field :uid, :type => String
  field :provider, :type => String

  # You can define indexes on documents using the index macro:
  # index :field <, :unique => true>

  # You can create a composite key in mongoid to replace the default id using the key macro:
  # key :field <, :another_field, :one_more ....>

  def self.create_with_omniauth(auth)
    create! do |account|
      account.provider = auth["provider"]
      account.uid      = auth["uid"]
      account.email    = auth["name"]
      account.email    = auth["user_info"]["email"] if auth["user_info"] # we get this only from FB
      account.role     = "users"
    end
  end

  def self.find_by_id(id)
    find(id) rescue nil
  end
end
