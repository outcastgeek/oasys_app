#!/bin/bash

#export JAVA_HOME="/usr/lib/jvm/jdk1.7.0_01"

export JAVA_OPTS="-Djava.awt.headless=true -XX:CompileThreshold=4 -XX:+AggressiveOpts -XX:+UseCompressedOops -XX:MaxHeapFreeRatio=70 -XX:MinHeapFreeRatio=40 -XX:HeapDumpPath=./log -XX:+HeapDumpOnOutOfMemoryError -Xms128m -Xmx128M -XX:MaxPermSize=128m -XX:ReservedCodeCacheSize=128m -Xss512k -XX:+UseParallelGC -XX:ParallelGCThreads=24 -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:./log/gc.log -XX:+DisableExplicitGC"

#export JRUBY_OPTS="--1.9 -Xinvokedynamic.constants=true"
#JRUBY_OPTS="--1.9 -J-Djruby.compile.mode=FORCE"

case $1 in
  Server)
    #~/jruby --1.9 -S ./bin/trinidad --threadsafe -p 3000 --config   # it uses config/trinidad.yml
    ~/jruby --1.9 -J-Djruby.compile.mode=JIT -S ./bin/trinidad --threadsafe -p 3000 --config   # it uses config/trinidad.yml
    #ulimit -n 16384; ~/jruby --1.9 -J-Djruby.compile.mode=JIT -S ./bin/trinidad --threadsafe -p 3000 --config   # it uses config/trinidad.yml
    ;;
  Bundle)
      bundle install --binstubs=bin --path=vendor/bundle
      ;;
  JBundle)
      ~/jruby --1.9 -S bundle install --binstubs=bin --path=vendor/bundle
      ;;
  Task)
    ~/jruby --1.9 -S rake $2 $3 $4 $5
    ;;
  Run)
    ~/jruby --1.9 -S $2 $3 $4 $5 $6 $7 $8
    ;;
  Repl)
    ~/jruby --1.9 -S rake cljRepl
    ;;
  esac
exit 0
