require 'rake'

require 'ant'
ant_import

require "ftools"
require 'net/http'
require 'rbconfig'

is_windows = (RbConfig::CONFIG['host_os'] =~ /mswin|mingw|cygwin/)

def delete_all(*wildcards)
  wildcards.each do |wildcard|
    Dir[wildcard].each do |fn|
      next if ! File.exist?(fn)
      if File.directory?(fn)
        Dir["#{fn}/*"].each do |subfn|
          next if subfn=='.' || subfn=='..'
          delete_all(subfn)
        end
        puts "Deleting directory #{fn}"
        Dir.delete(fn)
      else
        puts "Deleting file #{fn}"
        File.delete(fn)
      end
    end
  end
end

def download_file(root_url, path_to_file, filename)
  Net::HTTP.start(root_url) do |http|
    resp = http.get(path_to_file)
    open(filename, "wb") do |file|
        file.write(resp.body)
    end
  end
  puts "Done downloading #{filename}."
end

def download_file_incr(file_url)
  f = open(file_url)
  begin
    http.request_get(file_url) do |resp|
        resp.read_body do |segment|
            f.write(segment)
        end
    end
  ensure
    f.close()
  end
end

desc "Clean all but deps!!!!"
task "clean:butdeps" do
  ant do
    #delete :dir => "target/classes"
    #mkdir :dir => "target/classes"
    delete :dir => "log"
    mkdir :dir => "log"
    delete :dir => "logs"
    mkdir :dir => "logs"
    delete :dir => "src/main/webapp/static/js/outcastgeekui"
    delete :dir => "src/main/webapp/static/js/mywebapp"
    delete :dir => "src/main/webapp/static/js/WEB-INF"
    delete :dir => "src/main/webapp/static/gwt-unitCache"
    delete :dir => "src/main/webapp/static/js/out"
    delete :dir => "target/out"
    delete :dir => "out"
    delete :dir => "target/node"
  end
  ["/tmp/uploads", "tmp", "work", "**/*.zip",  "**/*~", "**/*.hprof"].each do |pattern|
    delete_all(pattern)
  end
end

desc "Clean Workspace!!!!"
task "clean:workspace" do
  ant do
    delete :dir => "target"
    delete :dir => "log"
    delete :dir => "logs"
    delete :dir => "vendor"
    delete :dir => "src/main/webapp/static/js/outcastgeekui"
    delete :dir => "src/main/webapp/static/js/mywebapp"
    delete :dir => "src/main/webapp/static/js/WEB-INF"
    delete :dir => "src/main/webapp/static/gwt-unitCache"
    delete :dir => "src/main/webapp/static/js/out"
    delete :dir => "target/out"
    delete :dir => "out"
    delete :dir => "target/node"
  end
  ["/tmp/uploads", "tmp", "bin", "work", "**/*.zip", "**/*.class", "**/*~", "**/*.hprof"].each do |pattern|
    delete_all(pattern)
  end
end

desc "Package and Add All Java dependencies"
task "deps:all" do
  unless is_windows
    sh "~/jruby -S buildr upgrade.avenue:deps"
  else
    sh "jruby -S buildr upgrade.avenue:deps"
  end
=begin
  ["target/lib/clojure-1.2*"].each do |pattern| #This is a temporary hack to remove unneeded dependencies
    delete_all(pattern)
  end
=end
end

desc "Downloads all Sources for the Dependencies"
task "sources:all" do
  unless is_windows
    sh "~/jruby -S buildr artifacts:sources"
  else
    sh "jruby -S buildr artifacts:sources"
  end
end

task "clj_comp" do
  ["target/lib/clojure-1.2*", "target/lib/jetty-6.1*.jar", "target/lib/spring*3.0.6*.jar"].each do |pattern|
    delete_all(pattern)
  end
  ant do
    @build_path = ["src/main/clojure"] unless @build_path
    source_files = @build_path.collect do |d|
      Dir.glob("#{d}/**/*.clj").select do |clj_file|
        classfile = 'target/classes/' + clj_file.sub(".clj", ".class")
        File.exist?(classfile) ? File.stat(clj_file).mtime > File.stat(classfile).mtime : true
      end
    end.flatten
    source_file_list = source_files.join ' '
    namespaces = source_files.map do |f|
      f.sub("src/main/clojure/", "").sub(".clj", "").gsub("/", ".")
    end.join ' '
    java  :classname => "clojure.lang.Compile", :fork => true, :failonerror => true do
      sysproperty :key => "clojure.compile.path", :value => "target/classes"
      classpath do
        pathelement :location => "src/main/clojure"
        pathelement :location => "src/main/resources"
        pathelement :location => "target/classes"
        fileset :dir => "target/lib" do
          include :name => "*.jar"
        end
      end
      arg :line => "#{namespaces}"
    end
  end
end

#task "compile" => ["clean:butdeps", "deps:all", "compileSimpleJS", "compileSimpleNodeJS"] do #This one has issues with goog!!!!
#task "compile" => ["clean:butdeps", "deps:all", "compileOptimizedJS"] do
#task "compile" => ["clean:butdeps", "deps:all", "compileSimpleJS"] do
#task "compile" => ["clean:butdeps", "deps:all", "compilePrettyJS"] do
#task "compile" => ["clean:butdeps", "deps:all", "compileDebugJS"] do
task "compile" => ["clean:butdeps", "deps:all", "progEnhancement", "clj_comp"] do
  puts "Done compiling!!!!"
end

task "jar" do
  sh "~/jruby -S buildr package"
  puts "Done Jarring!"
end

task "uberjar" => ["recompile", "jar"] do
  sh "~/jruby -S buildr package"
  puts "Done UberJarring!"
end

task "run" do
  sh "ant run"
end

task "start" do
  sh "ant start"
end

task "runServices" do
  ant do
    java :classname => "com.outcastgeek.services.web.Services", :fork => false, :failonerror => true do
      classpath do
        pathelement :location => "target/classes"
        pathelement :location => "target/lib"
        pathelement :location => "src/main/resources"
        pathelement :location => "src/main/webapp"
        fileset :dir => "target/lib" do
          include :name => "*.jar"
        end
      end
      arg :line => "Aleph 8998 web.xml 1800"
    end
  end
end
#task "runServer", [:server, :port, :webXml, :frequency] do |t, args|
task "runServer" do
  server, port, webXml, frequency = ENV['server'] || 'Jetty', ENV['port'] || '9998', ENV['webXml'] || 'web.xml', ENV['frequency'] || '1800'
  ant do
    java :classname => "com.outcastgeek.services.web.Services", :fork => false, :failonerror => true do
      classpath do
        pathelement :location => "target/classes"
        pathelement :location => "target/lib"
        pathelement :location => "src/main/resources"
        pathelement :location => "src/main/webapp"
        fileset :dir => "target/lib" do
          include :name => "*.jar"
        end
      end
      arg :line => "#{server} #{port} #{webXml} #{frequency}"
      #arg :line => "#{args.server} #{args.port} #{args.webXml} #{args.frequency}"
      #arg :line => "Jetty 9998 web.xml 1800"
    end
  end
end

task "cljRepl" do
  ant do
    java :classname => "clojure.main", :fork => false, :failonerror => true do
    #java :jar => "target/lib/clojure-1.3.0.jar", :fork => true, :failonerror => true do
      classpath do
        pathelement :location => "target/classes"
        pathelement :location => "target/lib"
        pathelement :location => "src/main/java"
        pathelement :location => "src/main/scala"
        pathelement :location => "src/main/resources"
        pathelement :location => "src/main/clojure"
        fileset :dir => "target/lib" do
          include :name => "*.jar"
        end
      end
    end
  end
end

task "launch" do
  unless is_windows
    sh "./starCLJ.sh"
  else
    sh "./startCLJ.bat"
  end
end

def compile_javascript(is_windows, optimization)
  Dir.glob("src/main/cljs/*").select do |input|
    if File.directory?(input)
      puts "Compiling **/*.cljs from directory #{input}"
      output = input.sub("src/main/cljs/", "")
      ant do
        delete :dir => "src/main/webapp/static/js/out"
        mkdir :dir => "src/main/webapp/static/js/out"
        delete :dir => "src/main/webapp/static/js/out/#{output}"
        mkdir :dir => "src/main/webapp/static/js/out/#{output}"
      end
      unless is_windows
        sh "./cljs/bin/cljsc #{input} #{optimization} > ./src/main/webapp/static/js/out/#{output}/#{output}.js"
      else
        sh "./cljs/bin/cljsc.bat #{input} #{optimization} > ./src/main/webapp/static/js/out/#{output}/#{output}.js"
      end
    else
      puts "---------------------------------------------------------------------------------"
    end
  end
  ant do
    copy :todir => "src/main/webapp/static/js/out" do
      fileset :dir => "out"
    end
    #copy :file => "cljs/closure/library/closure/goog/base.js", :todir => "src/main/webapp/static/js/out"
    #copy :file => "cljs/closure/library/closure/goog/deps.js", :todir => "src/main/webapp/static/js/out"
  end
end

def compile_nodejs(is_windows, optimization)
  Dir.glob("src/main/nodecljs/*").select do |input|
    if File.directory?(input)
      puts "Compiling **/*.cljs from directory #{input}"
      output = input.sub("src/main/nodecljs/", "")
      ant do
        delete :dir => "target/out"
        mkdir :dir => "target/out"
        delete :dir => "target/out/#{output}"
        mkdir :dir => "target/out/#{output}"
        delete :dir => "target/node"
        mkdir :dir => "target/node"
      end
      unless is_windows
        sh "./cljs/bin/cljsc #{input} #{optimization} > ./target/out/#{output}/#{output}.js"
      else
        sh "./cljs/bin/cljsc.bat #{input} #{optimization} > ./target/out/#{output}/#{output}.js"
      end
    else
      puts "---------------------------------------------------------------------------------"
    end
  end
  ant do
    copy :todir => "target/out" do
      fileset :dir => "out"
    end
    copy :todir => "target/node" do
      fileset :dir => "src/main/nodejs"
    end
  end
end

task "compileDebugJS" do
  compile_javascript(is_windows, "")
end

task "compilePrettyJS" do
  compile_javascript(is_windows, "{:optimizations :simple :pretty-print true}")
end

task "compileSimpleJS" do
  compile_javascript(is_windows, "{:optimizations :simple}")
end

task "compileOptimizedJS" do
  compile_javascript(is_windows, "{:optimizations :advanced}")
end

task "compileSimpleNodeJS" do
  compile_nodejs(is_windows, "{:optimizations :simple :pretty-print true :target :nodejs}")
end

task "compileOptimizedNodeJS" do
  compile_nodejs(is_windows, "{:optimizations :advanced}")
end

task "progEnhancement" => :build do
  puts "Done generating client code."
end

task "debugClient" => :hosted do
  puts "Done debugging client code."
end

task "cljsREPL" do
  unless is_windows
    sh "./cljs/script/repl"
  else
    sh "./cljs/script/repl.bat"
  end
end
