package org.eclipse.ptp.rdt.sync.unison.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FakeSSH {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String cmd = "";
		int portnum = Integer.parseInt(args[5]);
		for (int i = 6; i < args.length; i++){ // not adding the first argument
												// because it is the host
			
			cmd += args[i] + " ";		
		}
		
		cmd += "\n";
		try {
			Socket socket = new Socket("localhost", portnum);
			OutputStream outputStream = socket.getOutputStream();
			InputStream inputStream = socket.getInputStream();
			outputStream.write(cmd.getBytes());
			StreamCopyThread t1 = new StreamCopyThread(System.in, outputStream);
			t1.start();
			Thread t2 = new StreamCopyThread(inputStream, System.out);
			t2.start();
			t2.join();
			System.err.println("fakessh done");
			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
