
set JAVA_HOME=C:\Program Files\Java\jdk1.7.0

set JAVA_OPTS="-Djava.awt.headless=true -XX:CompileThreshold=4 -XX:+AggressiveOpts -XX:MaxHeapFreeRatio=70 -XX:MinHeapFreeRatio=40 -XX:HeapDumpPath=./logs -XX:+HeapDumpOnOutOfMemoryError -Xms128m -Xmx128M -XX:MaxPermSize=128m -XX:+UseParallelGC -XX:ParallelGCThreads=8 -XX:+DisableExplicitGC"

::set JRUBY_OPTS="--1.9 -Xinvokedynamic.constants=true"

set var=%1

if "%var%"=="One" goto :One
if "%var%"=="Two" goto :Two
if "%var%"=="Three" goto :Three
if "%var%"=="Four" goto :Four
if "%var%"=="Task" goto :Task
if "%var%"=="Repl" goto :Repl

:One
jruby -S rake runServer server=Jetty port=9992 webXml=web.xml frequency=1800
goto :EOF

:Two
jruby -S rake runServer server=Jetty port=9994 webXml=web.xml frequency=1800
goto :EOF


:Three
jruby -S rake runServer server=Jetty port=9996 webXml=web.xml frequency=1800
goto :EOF

:Four
jruby -S rake runServer server=Jetty port=9998 webXml=web.xml frequency=1800
goto :EOF

:Task
jruby -S rake %2 %3 %4 %5
goto :EOF

:Repl
jruby -S rake cljRepl
goto :EOF

:EOF
