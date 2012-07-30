package com.outcastgeek.web.server.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
//import org.eclipse.jetty.server.handler.DebugHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

public class Jetty {
    
    private static Logger jettyLog = LoggerFactory.getLogger(Jetty.class); 
    
    private static Server server;

    public static void runServer(int portNumber, String webXml) throws Exception {
        server = new Server();
        
        Connector connector = new SelectChannelConnector();
        connector.setPort(portNumber);
        jettyLog.info("Jetty Server port: {}", portNumber);
        connector.setMaxIdleTime(60000);
        
        server.setConnectors(new Connector[] { connector });
        server.setThreadPool(new QueuedThreadPool(Runtime.getRuntime().availableProcessors() * 4));
        
        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setResourceBase(new File("resources/public").getAbsolutePath());
        context.setDescriptor(new File("resources/WEB-INF/" + webXml).getAbsolutePath());
//        context.setResourceBase(new File("src/main/webapp").getAbsolutePath());
//        context.setDescriptor(new File("src/main/webapp/WEB-INF/" + webXml).getAbsolutePath());
        context.setContextPath("/");
        context.setParentLoaderPriority(true);
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        
//        String jetty_home = System.getProperty("jetty.home","..");
//        
//        WebAppContext webapp = new WebAppContext();
//        webapp.setContextPath("/");
//        webapp.setWar(jetty_home+"/webapps/test.war");
//        webapp.setWar(args[1]);
        
        HandlerCollection handlers = new HandlerCollection();
        DefaultHandler defaultHandler = new DefaultHandler();
        //RequestLogHandler requestLogHandler = new RequestLogHandler();
        //DebugHandler debugHandler = new DebugHandler();
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase(".");
        handlers.setHandlers(new Handler[]{context, /*webapp,*/ defaultHandler, /*requestLogHandler, debugHandler,*/ resourceHandler});
        
        defaultHandler.setServer(server);
        defaultHandler.setShowContexts(true);
        defaultHandler.setServeIcon(true);
        
        //NCSARequestLog requestLog = new NCSARequestLog("./logs/jetty-yyyy_mm_dd.request.log");
        //requestLog.setRetainDays(90);
        //requestLog.setAppend(true);
        //requestLog.setExtended(true);
        //requestLog.setLogTimeZone("GMT");
        //requestLogHandler.setRequestLog(requestLog);
        
        //debugHandler.setServer(server);
        
        server.setHandler(handlers);
        server.setStopAtShutdown(true);
        Thread monitor = new MonitorThread();
        monitor.start();
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            jettyLog.error(e.getMessage());
            server.dump();
            server.destroy();
        }
    }

    private static class MonitorThread extends Thread {
        
        private static Logger jettyMonitorThreadLog = LoggerFactory.getLogger(MonitorThread.class);

        private ServerSocket socket;

        public MonitorThread() {
            setDaemon(true);
            setName("StopMonitor");
            try {
                socket = new ServerSocket(8079, 1, InetAddress.getByName("127.0.0.1"));
            } catch(Exception e) {
                jettyMonitorThreadLog.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            jettyMonitorThreadLog.info("*** running jetty 'stop' thread");
            Socket accept;
            try {
                accept = socket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
                reader.readLine();
                jettyMonitorThreadLog.info("*** stopping jetty embedded server");
                server.stop();
                accept.close();
                socket.close();
            } catch(Exception e) {
                jettyMonitorThreadLog.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
    
}
