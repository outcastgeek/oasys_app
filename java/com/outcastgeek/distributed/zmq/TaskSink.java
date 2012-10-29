package com.outcastgeek.distributed.zmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class TaskSink {

	public static void main(String[] args) {

		// Prepare our context and socket
		Context context = ZMQ.context(1);
		Socket receiver = context.socket(ZMQ.PULL);
		receiver.bind("tcp://*:5558");

		// Wait for start of batch
		String string = new String(receiver.recv(0));

		// Start our clock now
		long tstart = System.currentTimeMillis();

		// Process 100 confirmations
		int total_msec = 0; // Total calculated cost in msecs
		for (int task_nbr = 1; task_nbr <= 100; task_nbr++) {
			String strinG = new String(receiver.recv(0)).trim();
			if ((task_nbr / 10) * 10 == task_nbr) {
				System.out.print(":");
			} else {
				System.out.print(".");
			}
			System.out.flush();
		}
		// Calculate and report duration of batch
		long tend = System.currentTimeMillis();

		System.out.println("Total elapsed time: " + (tend - tstart) + " msec");
	}
}
