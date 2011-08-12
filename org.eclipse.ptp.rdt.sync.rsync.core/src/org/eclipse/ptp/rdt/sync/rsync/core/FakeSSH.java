package org.eclipse.ptp.rdt.sync.rsync.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FakeSSH {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String cmd = "";
		int portnum = Integer.parseInt(args[1]);
		

		for (int i = 3; i < args.length; i++){ // not adding the first argument
												// because it is the host
			
			cmd += args[i] + " ";
			
		}

		cmd += "\n";
		File fakesshfile = new File("/home/lnj/TestSSH/fakesshout");
        try {
			fakesshfile.createNewFile();
	        BufferedWriter out = new BufferedWriter(new FileWriter("/home/lnj/TestSSH/fakesshout", true));
	        out.write(portnum + "\n");
	        out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Socket socket = new Socket("localhost", portnum);

			OutputStream outputStream = socket.getOutputStream();
			InputStream inputStream = socket.getInputStream();
			outputStream.write(cmd.getBytes());
			// System.err.println(cmd.getBytes().length + "," + cmd.length());

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
