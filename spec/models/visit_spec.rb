require 'spec_helper'

describe "Visit Model" do
  let(:visit) { Visit.new }
  it 'can be created' do
    visit.should_not be_nil
  end
end
