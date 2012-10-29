package com.outcastgeek.distributed;

import akka.zeromq.Bind;
import akka.zeromq.Connect;
import akka.zeromq.Frame;
import akka.zeromq.Listener;
import akka.zeromq.Subscribe;
import akka.zeromq.ZMQMessage;
import akka.zeromq.ZeroMQExtension;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Akka {

	public static ActorRef createPubSocket(ActorSystem actorSystem,
			String endpoint) {
		ActorRef pubSocket = ZeroMQExtension.get(actorSystem).newPubSocket(
				new Bind(endpoint));

		return pubSocket;
	}

	public static ActorRef createSubSocket(ActorSystem actorSystem,
			String endpoint, ActorRef listener, String topic) {
		ActorRef subSocket = ZeroMQExtension.get(actorSystem).newSubSocket(
				new Connect(endpoint), new Listener(listener),
				new Subscribe(topic));

		return subSocket;
	}
	
	public static void publish(ActorRef actor, String topic, String message) {
		ZMQMessage zmqMessage = new ZMQMessage(new Frame(topic), new Frame(
				message));
		actor.tell(zmqMessage);
	}
}
