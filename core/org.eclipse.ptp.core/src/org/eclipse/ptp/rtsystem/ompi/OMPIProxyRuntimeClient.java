package org.eclipse.ptp.rtsystem.ompi;

import java.io.IOException;

import org.eclipse.ptp.rtsystem.proxy.ProxyRuntimeClient;

public class OMPIProxyRuntimeClient extends ProxyRuntimeClient {

	public OMPIProxyRuntimeClient(String host, int port) {
		super(host, port);
	}

	public void startDaemon(String ompi_bin_path, String orted_path, String orted_bin, String[] args) throws IOException {
		String str_arg, arg_array_as_string;
		
		arg_array_as_string = new String("");
		
		for(int i=0; i<args.length; i++) {
			arg_array_as_string = arg_array_as_string + " " + args[i];
		}
		
		str_arg = ompi_bin_path + " " + orted_path + "  " + orted_bin + arg_array_as_string;
		
		sendCommand("STARTDAEMON", str_arg);
	}
}
