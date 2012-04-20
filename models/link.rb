class Link
  include Mongoid::Document
  include Mongoid::Timestamps # adds created_at and updated_at fields

  # field <name>, :type => <type>, :default => <value>
  field :lidentifier, :type => String

  has_one :url
  has_many :visits

  # You can define indexes on documents using the index macro:
  # index :field <, :unique => true>

  # You can create a composite key in mongoid to replace the default id using the key macro:
  # key :field <, :another_field, :one_more ....>

  def self.shorten(original, custom=nil)
    url = Url.first(:conditions => {:original => original})
    return url.link if url
    link = nil
    if custom
      raise 'Someone has already taken this custom URL, sorry' unless Link.first(:conditions => {:identifier => custom}).nil?
      raise 'This custom URL is not allowed because of profanity' if DIRTY_WORDS.include? custom
      link = Link.new(:lidentifier => custom)
      link.url = Url.create(:original => original)
      link.save
    else
      link = create_link(original)
    end
    return link
  end

  private

  def self.create_link(original)
    url = Url.create(:original => original)
    if Link.first(:lidentifier => url.id.to_s(36)).nil? or !DIRTY_WORDS.include? url.id.to_s(36)
      link = Link.new(:lidentifier => url.id.to_s(36))
      link.url = url
      link.save
      return link
    else
      create_link(original)
    end
  end
end
