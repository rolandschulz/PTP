package org.eclipse.ptp.debug.external.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
	
	public static void main(String[] args) {
		System.out.println("Hello World");
		
		PrintWriter out = null;
		BufferedReader networkIn = null;
		String input = "some input";
		
		try {
			Socket socket = new Socket("localhost", 5000);
			networkIn = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			
			out.println(input);
			out.flush();
			
			System.out.println("From server: " + networkIn.readLine());
			
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try {
				if (networkIn != null)
					networkIn.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
			}
		}
		
		System.out.println("Hello World End");
	}
}