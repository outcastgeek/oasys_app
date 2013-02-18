#!/bin/bash

#export JAVA_HOME="/usr/lib/jvm/jdk1.7.0_01"

export JAVA_OPTS="-Djava.awt.headless=true -server -XX:CompileThreshold=4 -XX:+AggressiveOpts -XX:+UseCompressedOops -XX:MaxHeapFreeRatio=70 -XX:MinHeapFreeRatio=40 -XX:HeapDumpPath=./logs -XX:+HeapDumpOnOutOfMemoryError -Xms128m -Xmx128M -XX:MaxPermSize=128m -XX:+UseParallelGC -XX:ParallelGCThreads=24 -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:./logs/gc.log -XX:+DisableExplicitGC"

#export JRUBY_OPTS="--1.9 -Xinvokedynamic.constants=true"

case $1 in
  One)
    ~/jruby -S rake runServer server=Jetty port=9992 webXml=web.xml frequency=1800
    ;;
  Two)
    ~/jruby -S rake runServer server=Jetty port=9994 webXml=web.xml frequency=1800
    ;;
  Three)
    ~/jruby -S rake runServer server=Jetty port=9996 webXml=web.xml frequency=1800
    ;;
  Four)
    ~/jruby -S rake runServer server=Jetty port=9998 webXml=web.xml frequency=1800
    ;;
  Task)
    ~/jruby -S rake $2 $3 $4 $5
    ;;
  Repl)
    ~/jruby -S rake cljRepl
    ;;
  esac
exit 0
