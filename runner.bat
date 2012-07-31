::!/bin/bash

::export JAVA_HOME="/usr/lib/jvm/jdk1.7.0_01"

::set CLASSPATH=.\lib:.\classes

::@echo off
::SETLOCAL enabledelayedexpansion
 
::FOR %%F IN (lib/*.jar) DO (
::  SET cp=!cp!;lib/%%F%
::)

::export JAVA_OPTS="-Djava.awt.headless=true -server -XX:CompileThreshold=4 -XX:+AggressiveOpts -XX:+UseCompressedOops -XX:MaxHeapFreeRatio=70 -XX:MinHeapFreeRatio=40 -XX:HeapDumpPath=./logs -XX:+HeapDumpOnOutOfMemoryError -Xms128m -Xmx128M -XX:MaxPermSize=128m -XX:+UseParallelGC -XX:ParallelGCThreads=24 -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:./logs/gc.log -XX:+DisableExplicitGC"

::java -Djava.awt.headless=true -server -XX:CompileThreshold=4 -XX:+AggressiveOpts -XX:+UseCompressedOops -XX:MaxHeapFreeRatio=70 -XX:MinHeapFreeRatio=40 -XX:HeapDumpPath=./logs -XX:+HeapDumpOnOutOfMemoryError -Xms128m -Xmx128M -XX:MaxPermSize=128m -XX:+UseParallelGC -XX:ParallelGCThreads=24 -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:./logs/gc.log -XX:+DisableExplicitGC -jar target/oasys_corp-1.0.0-SNAPSHOT-standalone.jar %1 %2

::java -Djava.awt.headless=true -server -XX:CompileThreshold=4 -XX:+AggressiveOpts -XX:+UseCompressedOops -XX:MaxHeapFreeRatio=70 -XX:MinHeapFreeRatio=40 -XX:HeapDumpPath=./logs -XX:+HeapDumpOnOutOfMemoryError -Xms128m -Xmx128M -XX:MaxPermSize=128m -XX:+UseParallelGC -XX:ParallelGCThreads=24 -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:./logs/gc.log -XX:+DisableExplicitGC -jar oasys_corp-1.0.0-SNAPSHOT-standalone.jar %1 %2

::For Windows
java -Djava.awt.headless=true -server -XX:CompileThreshold=4 -XX:+AggressiveOpts -XX:MaxHeapFreeRatio=70 -XX:MinHeapFreeRatio=40 -XX:HeapDumpPath=./logs -XX:+HeapDumpOnOutOfMemoryError -Xms128m -Xmx128M -XX:MaxPermSize=128m -XX:+UseParallelGC -XX:ParallelGCThreads=24 -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:./logs/gc.log -XX:+DisableExplicitGC -jar oasys_corp-1.0.0-SNAPSHOT-standalone.jar %1 %2 %3 %4

::java -cp "oasys_corp-1.0.0-SNAPSHOT.jar;%cp%" -Djava.awt.headless=true -server -XX:CompileThreshold=4 -XX:+AggressiveOpts -XX:MaxHeapFreeRatio=70 -XX:MinHeapFreeRatio=40 -XX:HeapDumpPath=./logs -XX:+HeapDumpOnOutOfMemoryError -Xms128m -Xmx128M -XX:MaxPermSize=128m -XX:+UseParallelGC -XX:ParallelGCThreads=24 -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:./logs/gc.log -XX:+DisableExplicitGC -jar oasys_corp-1.0.0-SNAPSHOT-standalone.jar com.outcastgeek.services.web.Services %1 %2 %3 %4
