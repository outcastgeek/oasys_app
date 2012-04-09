OasysCorp.controllers :index do
  # get :index, :map => "/foo/bar" do
  #   session[:foo] = "bar"
  #   render 'index'
  # end

  # get :sample, :map => "/sample/url", :provides => [:any, :js] do
  #   case content_type
  #     when :js then ...
  #     else ...
  # end

  # get :foo, :with => :id do
  #   "Maps to url '/foo/#{params[:id]}'"
  # end

  # get "/example" do
  #   "Hello world!"
  # end

  get :index, :map => '/' do
    render 'index/index'
  end

  get :careers, :map => '/careers' do
    "Careers"
  end

  get :about, :map => '/about' do
    "About"
  end

  get :contact, :map => '/contact' do
    "Contact"
  end

  get :profile do
    content_type :text
    current_account.to_yaml
  end

  get :destroy do
    set_current_account(nil)
    redirect url(:index)
  end

  get :auth, :map => '/auth/:provider/callback' do
    auth    = request.env["omniauth.auth"]
    account = Account.where(:provider => auth["provider"], :uid => auth["uid"]).first || Account.create_with_omniauth(auth)
    set_current_account(account)
    redirect "http://" + request.env["HTTP_HOST"] + url_for(:profile)
  end
end
