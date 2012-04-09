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
  end
  ["**/*.jar"].each do |pattern|
    delete_all(pattern)
  end
end

task "deps:all" => ["clean:butdeps", "ivy-retrieve"] do
  ["lib/clojure-1.2*", "lib/jetty-6.1*.jar", "lib/spring*3.0.6*.jar"].each do |pattern|
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
