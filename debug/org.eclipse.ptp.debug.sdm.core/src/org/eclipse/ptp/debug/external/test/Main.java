/*
 * Created on Jan 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ptp.debug.external.test;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.event.EExit;

/**
 * @author donny
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Main {

	public static void main(String[] args) {
		System.out.println("Hello World");
		final boolean stopObserver = false;
		
		Observer observer = new Observer() {
			public void update(Observable arg0, Object arg1) {
				if (arg1 instanceof EExit) {
					System.out.println("Exit Event Received");
				} else {
					System.out.println("Unknown Event");
				}
				
			};
		};
		
		try {
			int marker = 0;
			
			DebugSession debug = new DebugSession();			Thread.sleep(3000); System.out.println("marker " + marker++);
			
			debug.addDebuggerObserver(observer);
			
			debug.load("/home/donny/tmp/gt3_debugging_test/test"); 		Thread.sleep(3000); System.out.println("marker " + marker++);

			debug.breakpoint("main"); 							Thread.sleep(3000); System.out.println("marker " + marker++);
			
			debug.run(); 										Thread.sleep(3000); System.out.println("marker " + marker++);

			debug.cont(); 										Thread.sleep(3000); System.out.println("marker " + marker++);

			debug.exit();										Thread.sleep(3000); System.out.println("marker " + marker++);
			
		} catch (Exception e) {
			//System.out.println("EXCEPTION: " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Hello World End");
	}
}
