
Resque::Server.use(Rack::Auth::Basic) do |user, password|
  password == "Lambert2012"
end
