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
package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.util.ArrayList;
import java.util.List;

public class OpenMpiProcessMap {
	
	static public class AppContext {
		int index;
		String app = new String();
		int num_procs;
		List<String> argv = new ArrayList<String>();
		List<String> env = new ArrayList<String>();
		String workDir = new String();
		int num_maps;
	}
	
	static public class MappedNode {
		int cell;
		int launch_id;
		String username;
		String nodename;
		int num_procs;
		boolean overSubscribed;
		String daemon_name;
		List<MappedProc> procs = new ArrayList<MappedProc>();
	}
	
	static public class MappedProc {
		String name = new String();
		int rank;
		int pid;
		int app_context_index;
		
		public String getName() {
			return name;
		}

		public int getPID() {
			return pid;
		}
		
		public int getRank() {
			return rank;
		}
	}

	int pid;
	String hostname;
	int map_for_job;
	int job_id = 0;
	public enum MappingMode { byslot, bynode, none } 
	MappingMode mapping_mode = MappingMode.none;
	int starting_vpid = 0;
	int vpid_range = 0;
	int num_app_contexts = 0;
	List<AppContext> appContexts = new ArrayList<AppContext>();

	int num_nodes;
	List<MappedNode> mappedNodes = new ArrayList<MappedNode>();

	
}
