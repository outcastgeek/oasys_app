require 'spec_helper'

describe "Link Model" do
  let(:link) { Link.new }
  it 'can be created' do
    link.should_not be_nil
  end
end
