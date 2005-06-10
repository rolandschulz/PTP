package org.eclipse.ptp.debug.external.test;

import org.eclipse.ptp.debug.external.gdbserver.GDBServer;



public class GDBServerTesting {
	
	public static void main(String[] args) {
		System.out.println("Hello World");
		
		try {
			GDBServer gdbserver = new GDBServer("localhost", 3000,
					"/home/donny/gdb/test", null);
			Thread.sleep(3000);
			gdbserver.run();
			
		} catch (Exception e) {
			System.out.println("EXCEPTION: " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Hello World End");
	}
}