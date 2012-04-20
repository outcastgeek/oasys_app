TinyUrl.controllers :urltiny do
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
    render 'urltiny/index'
  end

  post :index, :map => '/' do
    uri = URI::parse(params[:original])
    custom = params[:custom] || nil
    #custom = params[:custom].empty? nil : params[:custom]
    raise "Invalid URL" unless uri.kind_of? URI::HTTP or uri.kind_of? URI::HTTPS
    @link = Link.shorten(params[:original], custom)
    render 'urltiny/index'
  end

  ['/info/:short_url', '/info/:short_url/:num_of_days', '/info/:short_url/:num_of_days/:map'].each do |path|
    get path do
      @link = Link.first(:lidentifier => params[:short_url])
      raise 'This link is not defined yet' unless @link
      @num_of_days = (params[:num_of_days] || 15).to_i
      render 'urltiny/info'
    end
  end

  get :shorturl, :map => '/:short_url' do
    link = Link.first(:lidentifier => params[:short_url])
    link.visits << Visit.create(:address => get_remote_ip(request.env))
    link.save
    redirect link.url.original, 301
  end

  def get_remote_ip(env)
    if addr = env['HTTP_X_FORWARDED_FOR']
      addr.split(',').first.strip
    else
      env['REMOTE_ADDR']
    end
  end

end
