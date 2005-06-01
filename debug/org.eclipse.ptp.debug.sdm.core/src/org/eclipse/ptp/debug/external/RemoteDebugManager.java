package org.eclipse.ptp.debug.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteDebugManager implements Runnable {
	ServerSocket inferiorServer, superiorServer;
	Socket inferiorSocket, superiorSocket;
	Thread inferiorThread, superiorThread;
	OutputStreamWriter inferiorOut, superiorOut;
	BufferedReader inferiorIn, superiorIn;
	
	Thread manager;
	
	boolean inferiorConnected = false;
	boolean superiorConnected = false;
	
	public RemoteDebugManager() {
		try {
			inferiorServer = new ServerSocket(3000); // gdbserver
			superiorServer = new ServerSocket(4000); // gdb
		} catch (IOException e) {
		}
	}
		
	public void serve() {
		manager = new Thread(this);
		manager.start();
	}
	
	public void destroy() {
		if (manager.isAlive()) manager.destroy();
		if (inferiorThread.isAlive()) inferiorThread.destroy();
		if (superiorThread.isAlive()) superiorThread.destroy();
		try {
			if (inferiorSocket != null) inferiorSocket.close();
			if (superiorSocket != null) superiorSocket.close();
			if (inferiorIn != null) inferiorIn.close();
			if (superiorIn != null) superiorIn.close();
			if (inferiorOut != null) inferiorOut.close();
			if (superiorOut != null) superiorOut.close();
		} catch (IOException e) {
		}
	}
	
	public boolean isInferiorConnected() {
		return inferiorConnected;
	}

	public boolean isSuperiorConnected() {
		return superiorConnected;
	}
	
	public void run() {
		if (inferiorThread == Thread.currentThread()) {
			try {
				char[] chrs = new char[10240];
				int size;
				while ((size = inferiorIn.read(chrs)) != -1) {
					//System.out.println("from gdbserver: " + String.valueOf(chrs));
					superiorOut.write(chrs, 0, size);
					superiorOut.flush();
				}	
			} catch (IOException e) {
			} finally {
				try {
					if (inferiorSocket != null) inferiorSocket.close();
					if (inferiorIn != null) inferiorIn.close();
					if (superiorOut != null) superiorOut.close();
				} catch (IOException e) {
				}
				System.out.println("End of Thread1");
			}
		} else if (superiorThread == Thread.currentThread()) {
			try {
				char[] chrs = new char[10240];
				int size;
				while ((size = superiorIn.read(chrs)) != -1) {
					//System.out.println("from gdb: " + String.valueOf(chrs));
					inferiorOut.write(chrs, 0, size);
					inferiorOut.flush();
				}	
			} catch (IOException e) {
			} finally {
				try {
					if (superiorSocket != null) superiorSocket.close();
					if (superiorIn != null) superiorIn.close();
					if (inferiorOut != null) inferiorOut.close();
				} catch (IOException e) {
				}
				System.out.println("End of Thread2");
			}
		} else if (manager == Thread.currentThread()) {
			try {
				inferiorSocket = inferiorServer.accept();
				inferiorConnected = true;
				superiorSocket = superiorServer.accept();
				superiorConnected = true;
				
				inferiorOut = new OutputStreamWriter(inferiorSocket.getOutputStream());
				inferiorIn = new BufferedReader(new InputStreamReader(inferiorSocket.getInputStream()));
				
				superiorOut = new OutputStreamWriter(superiorSocket.getOutputStream());
				superiorIn = new BufferedReader(new InputStreamReader(superiorSocket.getInputStream()));

				System.out.println("Start the threads");
				
				inferiorThread = new Thread(this);
				superiorThread = new Thread(this);
				
				inferiorThread.start();
				superiorThread.start();
			} catch (IOException e) {
				System.out.println(e);
			}
			System.out.println("End of DebugManager Thread");
		}
	}
}