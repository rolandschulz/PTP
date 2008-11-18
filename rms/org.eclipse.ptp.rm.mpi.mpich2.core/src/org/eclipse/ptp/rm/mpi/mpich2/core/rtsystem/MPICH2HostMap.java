/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2HostMap {
	public static class Host {
		public final static int NO_ERRORS = 0;
		public final static int ERR_NUM_SLOTS = 1 << 1;
		public final static int ERR_MAX_NUM_SLOTS = 1 << 2;
		public final static int ERR_UNKNOWN_ATTR = 1 << 3;

		private String name = null;
		private String addr = null;
		private String port = null;
		private int numProcessors = 0;
		private int maxNumProcessors = 0;
		private int errors = NO_ERRORS;

		public Host(String hostname, String address, String port) {
			this.name = hostname;
			this.addr = address;
			this.port = port;
		}

		public int getNumProcessors() {
			return numProcessors;
		}

		public int getMaxNumProcessors() {
			return maxNumProcessors;
		}

		public String getName() {
			return name;
		}
		
		public String getAddress() {
			return addr;
		}

		public String getPort() {
			return port;
		}

		public int getErrors() {
			return errors;
		}

		public void setNumProcessors(int numProcessors) {
			this.numProcessors = numProcessors;
		}

		public void setMaxNumProcessors(int maxNumProcessors) {
			this.maxNumProcessors = maxNumProcessors;
		}

		public void addErrors(int errors) {
			this.errors |= errors;
		}
	}

	List<Host> hosts = new ArrayList<Host>();
	Map<String, Host> hostNameToHost = new HashMap<String, Host>();
	boolean hasErrors = false;

	protected void addHost(Host host) {
		hosts.add(host);
		hostNameToHost.put(host.getName(), host);
	}

	public Host[] getHosts() {
		return hosts.toArray(new Host[hosts.size()]);
	}

	public Iterator<Host> getHostIterator() {
		return hosts.listIterator();
	}

	public Host getHostByName(String name) {
		return hostNameToHost.get(name);
	}

	public void addHost(String name, String address, String port) {
		MPICH2HostMap.Host host = new MPICH2HostMap.Host(name, address, port);
		addHost(host);
	}

	public int count() {
		return hosts.size();
	}

	public boolean hasParseErrors() {
		return hasErrors;
	}
}
