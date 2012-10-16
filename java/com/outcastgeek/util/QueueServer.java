package com.outcastgeek.util;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.core.server.JournalType;
import org.hornetq.jms.client.HornetQJMSConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueServer {

	Logger logger = LoggerFactory.getLogger(QueueServer.class);

	public static ConnectionFactory createConnectionFactory(String confUrl,
			int queuePortNumber) {
		// Prepare configuration objects
		String netty = NettyAcceptorFactory.class.getName();
		Map<String, Object> transportParams = new HashMap<String, Object>();
		transportParams.put(TransportConstants.HOST_PROP_NAME, "localhost");
		transportParams.put(TransportConstants.PORT_PROP_NAME, queuePortNumber);
		TransportConfiguration transpConf = new TransportConfiguration(netty,
				transportParams);

		// Create connection factory
		HornetQJMSConnectionFactory hornetQJMSConnectionFactory = new HornetQJMSConnectionFactory(
				true, transpConf);

		return hornetQJMSConnectionFactory;
	}

	public static HornetQServer createQueueServer(String confUrl, int queuePortNumber) {
		// Load configuration
		FileConfiguration configuration = new FileConfiguration();
		configuration.setConfigurationUrl(confUrl);
		configuration.setPersistenceEnabled(true);
		configuration.setJournalType(JournalType.NIO);
		
		// Prepare configuration objects
		String netty = NettyAcceptorFactory.class.getName();
		Map<String, Object> transportParams = new HashMap<String, Object>();
		transportParams.put(TransportConstants.HOST_PROP_NAME, "localhost");
		transportParams.put(TransportConstants.PORT_PROP_NAME,
				queuePortNumber);
		TransportConfiguration transpConf = new TransportConfiguration(
				netty, transportParams);
		
		configuration.getAcceptorConfigurations().clear();
		
		// add configuration (clearing before didn't helped either))
		configuration.getAcceptorConfigurations().add(transpConf);
		
		// Create server
		HornetQServer server = HornetQServers
				.newHornetQServer(configuration);
		
		return server;
	}
	// http://stackoverflow.com/questions/6705380/how-to-change-the-port-in-an-embedded-hornetq-programmatically
	// https://github.com/hornetq/hornetq/blob/master/examples/core/embedded-remote/src/org/hornetq/core/example/EmbeddedServer.java
	// http://stackoverflow.com/questions/7291648/how-to-make-a-queue-persisted-in-hornetq-2-2-5-core-client
}
