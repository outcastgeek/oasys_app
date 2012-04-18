require 'rake'

require 'ant'
ant_import

#Dir.glob('tasks/*.rake').each { |r| import r }
#Dir.glob('tasks/*.rake').each { |r| require r }

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

def compile_javascript(is_windows, optimization)
  Dir.glob("src/main/cljs/*").select do |input|
    if File.directory?(input)
      puts "Compiling **/*.cljs from directory #{input}"
      output = input.sub("src/main/cljs/", "")
      ant do
        delete :dir => "public/javascripts/compiled/#{output}"
        mkdir :dir => "public/javascripts/compiled/#{output}"
      end
      unless is_windows
        sh "chmod +x ./clojurescript/bin/cljsc"
        sh "./clojurescript/bin/cljsc #{input} #{optimization} > ./public/javascripts/compiled/#{output}/#{output}.js"
      else
        sh "./clojurescript/bin/cljsc.bat #{input} #{optimization} > ./public/javascripts/compiled/#{output}/#{output}.js"
      end
    else
      puts "---------------------------------------------------------------------------------"
    end
  end
  ant do
    copy :todir => "public/javascripts/compiled" do
      fileset :dir => "out"
    end
    #copy :file => "cljs/closure/library/closure/goog/base.js", :todir => "src/main/webapp/static/js/out"
    #copy :file => "cljs/closure/library/closure/goog/deps.js", :todir => "src/main/webapp/static/js/out"
  end
end

desc "Clean all but deps!!!!"
task "clean:butdeps" do
  ant do
    delete :dir => "log"
    mkdir :dir => "log"
    mkdir :dir => "bin"
    mkdir :dir => "vendor"
    mkdir :dir => "vendor/bundle"
    mkdir :dir => "classes"
    mkdir :dir => "ivy"
    delete :dir => "public/javascripts/compiled"
    mkdir :dir => "public/javascripts/compiled"
    delete :dir => "out"
    mkdir :dir => "out"
  end
  ["/tmp/uploads", "tmp", "work", "**/*.zip",  "**/*~", "**/*.hprof", "work"].each do |pattern|
    delete_all(pattern)
  end
end

desc "Clean Workspace!!!!"
task "clean:workspace" => "clean:butdeps" do
  ant do
    delete :dir => "ivy"
    delete :dir => "classes"
    delete :dir => "bin"
    delete :dir => "vendor"
    delete :dir => "out"
    delete :dir => "clojurescript"
    delete :dir => ".sass-cache"
  end
  ["**/*.jar"].each do |pattern|
    delete_all(pattern)
  end
end

task "compile_js" => "clean:butdeps" do
  #compile_javascript(is_windows, "{:optimizations :simple :pretty-print true}")
  #compile_javascript(is_windows, "{:optimizations :simple}")
  compile_javascript(is_windows, "{:optimizations :advanced}")
end

task "deps:all" => ["clean:butdeps", "ivy-retrieve"] do
  ["lib/clojure-1.2*", "lib/clojure-1.3*", "lib/jetty-6.1*.jar", "lib/spring*3.0.6*.jar"].each do |pattern|
    delete_all(pattern)
  end
  ant do
    taskdef :resource => "scala/tools/ant/antlib.xml" do
      classpath do
        pathelement :location => "classes"
        pathelement :location => "lib"
        fileset :dir => "lib" do
          include :name => "*.jar"
        end
      end
    end
    scalac :srcdir => "src", :destdir => "classes" do
      include :name => "**/*.scala"
      include :name => "**/*.java"
      classpath do
        pathelement :location => "classes"
        pathelement :location => "lib"
        fileset :dir => "lib" do
          include :name => "*.jar"
        end
      end
    end
    @build_path = ["src/main/clojure"] unless @build_path
    source_files = @build_path.collect do |d|
      Dir.glob("#{d}/**/*.clj").select do |clj_file|
        classfile = 'classes/' + clj_file.sub(".clj", ".class")
        File.exist?(classfile) ? File.stat(clj_file).mtime > File.stat(classfile).mtime : true
      end
    end.flatten
    source_file_list = source_files.join ' '
    namespaces = source_files.map do |f|
      f.sub("src/main/clojure/", "").sub(".clj", "").gsub("/", ".")
    end.join ' '
    java  :classname => "clojure.lang.Compile", :fork => true, :failonerror => true do
      sysproperty :key => "clojure.compile.path", :value => "classes"
      classpath do
        pathelement :location => "src/main/clojure"
        pathelement :location => "src/main/resources"
        pathelement :location => "classes"
        fileset :dir => "lib" do
          include :name => "*.jar"
        end
      end
      arg :line => "#{namespaces}"
    end
  end
  puts "All Done!!!!"
end
