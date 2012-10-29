package com.outcastgeek.distributed.zmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class TaskWork {
	
	public static void main(String[] args) throws InterruptedException {
		
		Context context = ZMQ.context(1);
		
	    //  Socket to receive messages on
		Socket receiver = context.socket(ZMQ.PULL);
		receiver.connect("tcp://localhost:5557");
		
		// Socket to send messages to
		Socket sender = context.socket(ZMQ.PUSH);
		sender.connect("tcp://localhost:5558");
		
		// Process tasks forever
		while(true) {
			String string = new String(receiver.recv(0)).trim();
			Long nsec = Long.parseLong(string);
//			Long nsec = Long.parseLong(string) * 1000;
			// Simple progress indicator for the viewer
			System.out.flush();
			System.out.print(string + '.');
			
			// Do the work
			Thread.sleep(nsec);
			
			// Send results to sink
			sender.send("".getBytes(), 0);
		}
	}
}
