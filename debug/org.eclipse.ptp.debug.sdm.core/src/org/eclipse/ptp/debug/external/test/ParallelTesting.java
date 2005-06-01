package org.eclipse.ptp.debug.external.test;

import org.eclipse.ptp.debug.external.DebugSession;



public class ParallelTesting {
	
	public static void main(String[] args) {
		System.out.println("Hello World");
		
		try {
			int marker = 0;
			String slaveGrp = "slave";
			String masterGrp = "master";
			
			String allGrp = "all";
			
			DebugSession debug = new DebugSession();			Thread.sleep(3000); System.out.println("marker " + marker++);
			
			debug.load("/home/donny/mpich_test/hello", 3); 		Thread.sleep(3000); System.out.println("marker " + marker++);

			debug.defSet(slaveGrp, new int[] {1, 2}); 			Thread.sleep(3000); System.out.println("marker " + marker++);
			
			debug.defSet(masterGrp, new int[] {0}); 			Thread.sleep(3000); System.out.println("marker " + marker++);
			
			debug.breakpoint("main"); 							Thread.sleep(3000); System.out.println("marker " + marker++);
			
			debug.breakpointSet(slaveGrp, "27"); 							Thread.sleep(3000); System.out.println("marker " + marker++);
			
			debug.breakpointSet(masterGrp, "30"); 							Thread.sleep(3000); System.out.println("marker " + marker++);
			
			debug.run(); 										Thread.sleep(3000); System.out.println("marker " + marker++);

			debug.cont(); 										Thread.sleep(3000); System.out.println("marker " + marker++);

			debug.cont(); 										Thread.sleep(3000); System.out.println("marker " + marker++);

			debug.exit();										Thread.sleep(3000); System.out.println("marker " + marker++);
			
		} catch (Exception e) {
			System.out.println("EXCEPTION: " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Hello World End");
	}
}