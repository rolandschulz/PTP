package org.eclipse.ptp.debug.external.test;

import org.eclipse.ptp.debug.external.gdbserver.RemoteDebugManager;


public class RemoteDebugManagerTesting {
	public static void main(String[] args) {
		System.out.println("Hello World");
		
		RemoteDebugManager dManager = new RemoteDebugManager();
		dManager.serve();
		
		System.out.println("Hello World End");
	}
}