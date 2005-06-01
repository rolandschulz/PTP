package org.eclipse.ptp.debug.external.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer implements Runnable {
	public static void main(String[] args) {
		System.out.println("Hello World");
		Socket socket1 = null;
		Socket socket2 = null;
		
		try {
			String str;
			char[] chrs = new char[10240];
			int size;
			
			ServerSocket server1 = new ServerSocket(3000); // gdbserver
			socket1 = server1.accept();
			System.out.println("connection from gdbserver");
			
			ServerSocket server2 = new ServerSocket(4000); // gdb
			socket2 = server2.accept();
			System.out.println("connection from gdb");
			
			OutputStreamWriter out1 = new OutputStreamWriter(socket1.getOutputStream());
			BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
			
			OutputStreamWriter out2 = new OutputStreamWriter(socket2.getOutputStream());
			BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
			
			//Thread processThread = new Thread(this);
			//Thread inReadThread = new Thread(this);

			while (true) {
				if (in1.ready()) {
					size = in1.read(chrs);
					//System.out.println("from gdbserver: " + String.valueOf(chrs));
					out2.write(chrs, 0, size);
					out2.flush();
				}
				else if (in2.ready()) {
					size = in2.read(chrs);
					//System.out.println("from gdb: " + String.valueOf(chrs));
					out1.write(chrs, 0, size);
					out1.flush();
				}
				else if (!in1.ready() && !in2.ready())
					break;
			}
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try {
				if (socket1 != null) socket1.close();
				if (socket2 != null) socket2.close();
			} catch (IOException e) {
			}
		}
		
		System.out.println("Hello World End");
	}

	public void run() {
		
	}
}