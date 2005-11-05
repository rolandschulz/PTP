/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.rtsystem.proxy;

import java.io.IOException;

public class ProxyRuntimeClient extends AbstractProxyRuntimeClient {
	public ProxyRuntimeClient(String host, int port) {
		super(host, port);
	}
	
    public void sendCommand(String cmd) throws IOException {
        super.sendCommand(cmd);
    }

	public void run(String prog, int numProcs, boolean debug) throws IOException {
		sendCommand("RUN", "\""+ prog + "\" " + Integer.toString(numProcs) + " " + Boolean.toString(debug));
	}
	
	public void getJobs() throws IOException {
		sendCommand("GETJOBS");
	}
	
	public void getProcesses(int jobId) throws IOException {
		sendCommand("GETPROCS", Integer.toString(jobId));
	}
	
	public void getProcessAttribute(int procId, String key) throws IOException {
		sendCommand("GETPATTR", Integer.toString(procId) + " " + key);
	}
	
	public void getMachines() throws IOException {
		sendCommand("GETMACHS");
	}

	public void getNodes(int machId) throws IOException {
		sendCommand("GETNODES", Integer.toString(machId));
	}

	public void getNodeAttribute(int nodeId, String key) throws IOException {
		sendCommand("GETNATTR", Integer.toString(nodeId) + " " + key);
	}

	public void getNodeMachineID(int nodeId) throws IOException {
		sendCommand("GETNMID", Integer.toString(nodeId));
	}

}
