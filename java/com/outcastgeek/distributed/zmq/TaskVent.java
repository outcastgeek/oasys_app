package com.outcastgeek.distributed.zmq;

import java.io.IOException;
import java.util.Random;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class TaskVent {

	public static void main(String[] args) throws InterruptedException,
			IOException {

		Context context = ZMQ.context(1);

		// Socket to send messages on
		Socket sender = context.socket(ZMQ.PUSH);
		sender.bind("tcp://*:5557");

		System.out.println("Press Enter when the workers are ready: ");
		System.in.read();
		System.out.println("Sending tasks to workers...\n");

		// The first message is "0" and signals start of batch
		sender.send("0\u0000".getBytes(), 0);

		// Initialize random number generator
		Random srandom = new Random(System.currentTimeMillis());

		// Send 100 tasks
		int total_msec = 0; // Total expected cost in msecs
		for (int i = 1; i <= 100; i++) {
			// Random workload from 1 to 100msecs
			int workload = srandom.nextInt(100) + 1;
			total_msec += workload;
			System.out.print(workload + ".");
			String string = String.format("%d\u0000", workload);
			sender.send(string.getBytes(), 0);
		}
		System.out.println("Total expected cost: " + total_msec + " msec");

		Thread.sleep(1000); // Give 0MQ time to deliver
	}
}
