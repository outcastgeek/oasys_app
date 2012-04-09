module OmniauthInitializer
  def self.registered(app)
    app.use OmniAuth::Builder do
      #provider :developer unless Padrino.env == :production
      # provider :twitter, 'consumer_key', 'consumer_secret'
      # provider :facebook, 'app_id', 'app_secret'
      provider :twitter, 'fGCQrLc3k7S8umcajONA', 'r2Pb8zfqDULrkiCVZP07ifARntg98xPVqyYaP4dk'
      provider :facebook, '342c542e9ee479421d95f2a81b9db878', 'f2f7c53a2226b1dc5c982593dc9afaf6'
    end

  end
end
