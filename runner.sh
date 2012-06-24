export JAVA_HOME="/usr/lib/jvm/jdk1.7.0_01"

export CLASSPATH=./lib:./classes

export JAVA_OPTS="-Djava.awt.headless=true -server -XX:CompileThreshold=4 -XX:+AggressiveOpts -XX:+UseCompressedOops -XX:MaxHeapFreeRatio=70 -XX:MinHeapFreeRatio=40 -XX:HeapDumpPath=./logs -XX:+HeapDumpOnOutOfMemoryError -Xms128m -Xmx128M -XX:MaxPermSize=128m -XX:+UseParallelGC -XX:ParallelGCThreads=24 -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:./logs/gc.log -XX:+DisableExplicitGC"

#export JRUBY_OPTS="--1.9 -Xinvokedynamic.constants=true"

#~/jruby -S rake runJetty

java "com.outcastgeek.services.web.Services"  -cp ./lib/*.jar:./classes/*

