package com.outcastgeek.util;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.core.server.JournalType;
import org.hornetq.jms.client.HornetQJMSConnectionFactory;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueServer {

	private static Logger logger = LoggerFactory.getLogger(QueueServer.class);

	public static ConnectionFactory createConnectionFactory(String confUrl,
			String queueHost, int queuePortNumber) {
		// Prepare configuration objects
		String nettyConnector = NettyConnectorFactory.class.getName();
		Map<String, Object> transportParams = new HashMap<String, Object>();
		transportParams.put(TransportConstants.HOST_PROP_NAME, queueHost);
		transportParams.put(TransportConstants.PORT_PROP_NAME, queuePortNumber);
		TransportConfiguration transpConf = new TransportConfiguration(nettyConnector,
				transportParams);

		// Create connection factory
		HornetQJMSConnectionFactory hornetQJMSConnectionFactory = new HornetQJMSConnectionFactory(
				true, transpConf);

		return hornetQJMSConnectionFactory;
	}

	public static JMSServerManager createJMSServerManager(
			HornetQServer hornetQServer, String configUrl) throws Exception {
		JMSServerManager jmsServerManager = new JMSServerManagerImpl(
				hornetQServer, configUrl);

		return jmsServerManager;
	}

	public static HornetQServer createQueueServer(String confUrl,
			String queueHost, int queuePortNumber) {
		// Load configuration
		FileConfiguration configuration = new FileConfiguration();
		configuration.setConfigurationUrl(confUrl);

		try {
			configuration.start();
		} catch (Exception e) {
			logger.error("Could not start Queue Server Configuration", e);
		}

		configuration.setPersistenceEnabled(true);
		configuration.setJournalType(JournalType.NIO);
		configuration.setThreadPoolMaxSize(Runtime.getRuntime()
				.availableProcessors());
		configuration.setClusterUser("guest");
		configuration.setClusterPassword("guest");

		// Prepare configuration objects
		Map<String, Object> transportParams = new HashMap<String, Object>();
		transportParams.put(TransportConstants.HOST_PROP_NAME, queueHost);
		transportParams.put(TransportConstants.PORT_PROP_NAME, queuePortNumber);

		// Acceptor Configuration
		String nettyAcceptor = NettyAcceptorFactory.class.getName();
		String inVmAcceptor = InVMAcceptorFactory.class.getName();

		TransportConfiguration nettyTranspAcceptorConf = new TransportConfiguration(
				nettyAcceptor, transportParams);
		TransportConfiguration inVMTranspAcceptorConf = new TransportConfiguration(
				inVmAcceptor, transportParams);

		configuration.getAcceptorConfigurations().clear();
		configuration.getAcceptorConfigurations().add(nettyTranspAcceptorConf);
		configuration.getAcceptorConfigurations().add(inVMTranspAcceptorConf);

		// Connector Configuration
		String nettyConnector = NettyConnectorFactory.class.getName();
		String inVMConnector = InVMConnectorFactory.class.getName();

		TransportConfiguration nettyTranspConnectorConf = new TransportConfiguration(
				nettyConnector, transportParams);
		TransportConfiguration inVMTranspConnectorConf = new TransportConfiguration(
				inVMConnector, transportParams);

		configuration.getConnectorConfigurations().clear();
		configuration.getConnectorConfigurations().put("netty",
				nettyTranspConnectorConf);
		configuration.getConnectorConfigurations().put("in-vm",
				inVMTranspConnectorConf);

		// Create server
		HornetQServer server = HornetQServers.newHornetQServer(configuration);

		return server;
	}
	// http://docs.jboss.org/hornetq/2.2.2.Final/user-manual/en/html/embedding-hornetq.html
	// http://stackoverflow.com/questions/6705380/how-to-change-the-port-in-an-embedded-hornetq-programmatically
	// https://github.com/hornetq/hornetq/blob/master/examples/core/embedded-remote/src/org/hornetq/core/example/EmbeddedServer.java
	// http://stackoverflow.com/questions/7291648/how-to-make-a-queue-persisted-in-hornetq-2-2-5-core-client
}
