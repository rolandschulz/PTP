/*
 * Created on Mar 8, 2005
 *
 */
package org.eclipse.ptp.debug.external.gdbserver;

import java.io.IOException;

import org.eclipse.ptp.debug.external.utils.ExecHelper;
import org.eclipse.ptp.debug.external.utils.ExecProcessor;



/**
 * @author donny
 *  
 */
public class GDBServer implements ExecProcessor {
	private String hostname;
	private int portnum;
	private String[] command;
	
	public GDBServer(String host, int port, String prog, String[] args) {
		hostname = host;
		portnum = port;
		String[] tmp;
		
		if (args != null) {
			tmp = new String[1 + args.length];
			tmp[0] = prog;
			System.arraycopy(args, 0, tmp, 1, args.length);
		} else {
			tmp = new String[] {prog};
		}
		
		String[] gdbserver = new String[3];
		gdbserver[0] = "/home/donny/gdb/local/bin/gdbserver";
		gdbserver[1] = "-e";
		gdbserver[2] = host + ":" + port;
		
		command = new String[gdbserver.length + tmp.length];
		System.arraycopy(gdbserver, 0, command, 0, gdbserver.length);
		System.arraycopy(tmp, 0, command, gdbserver.length, tmp.length);
	}
	
	public void run() {
		try {
			ExecHelper.exec(this, command);
		} catch (IOException e) {
		}
	}

	public void processNewInput(String input) {
		if (input.length() != 0)
			System.out.println("--- " + input);
	}

	public void processNewError(String error) {
		if (error.length() != 0)
			System.out.println("!!! " + error);
	}

	public void processEnded(int exitValue) {
		System.out.println("Process has ended: " + exitValue);
	}
}
