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

package org.eclipse.ptp.rtmodel.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.rtmodel.JobRunConfiguration;
import org.eclipse.ptp.rtmodel.IRuntimeListener;
import org.eclipse.ptp.rtmodel.IRuntimeModel;
import org.eclipse.ptp.rtmodel.RuntimeEvent;

public class SimulationRuntimeModel implements IRuntimeModel {

	/* define the number of machines here */
	final static int numMachines = 4;

	/*
	 * define how many nodes each machine has - the array length must equal
	 * numMachines
	 */
	final static int[] numNodes = { 256, 256, 256, 512 };

	protected HashMap nodeMap;

	protected HashMap nodeUserMap;

	protected HashMap nodeGroupMap;

	protected HashMap nodeModeMap;

	protected HashMap nodeStateMap;
	
	protected HashMap processMap;
	
	protected int numJobs = -1;

	protected List listeners = new ArrayList(2);

	protected String spawned_app_state = null;

	protected int spawned_num_procs = 0;

	protected int spawned_procs_per_node = 0;

	protected int spawned_first_node = 0;

	protected String spawned_app_signal = new String("");

	protected String spawned_app_exit_code = new String("");

	protected Thread runningAppEventsThread = null;

	protected Thread runningAppFinishThread = null;

	public SimulationRuntimeModel() {
		nodeMap = new HashMap();
		for (int i = 0; i < numMachines; i++) {
			String s = new String("machine" + i);
			nodeMap.put(s, new Integer(numNodes[i]));
		}

		processMap = new HashMap();
		
		int totnodes = 0;
		for (int i = 0; i < numMachines; i++) {
			totnodes += numNodes[i];
		}
		nodeUserMap = new HashMap();
		nodeGroupMap = new HashMap();
		nodeModeMap = new HashMap();
		nodeStateMap = new HashMap();

		/* machine 3 has a bunch of nodes w/ different states */
		for (int i = 0; i < numNodes[3]; i++) {
			String s = new String("machine3_node" + i);
			int r = ((int) (Math.random() * 100));
			if (r < 10)
				nodeStateMap.put(s, new String("error"));
			else if (r < 30)
				nodeStateMap.put(s, new String("down"));
			else {
				nodeStateMap.put(s, new String("up"));
			}
		}
		int nstart = ((int) (Math.random() * 50));
		int nlen = ((int) (Math.random() * 40 + 10));
		String nmode = new String(((int) (Math.random() * 2)) == 0 ? "0111"
				: "0100");
		int gstart = ((int) (Math.random() * 50 + (nstart + nlen)));
		int glen = ((int) (Math.random() * 100 + 40));
		String gmode = new String(((int) (Math.random() * 2)) == 0 ? "0111"
				: "0100");
		int cstart = ((int) (Math.random() * 50 + (nstart + nlen + gstart + glen)));
		int clen = ((int) (Math.random() * 75 + 25));
		String cmode = new String(((int) (Math.random() * 2)) == 0 ? "0111"
				: "0100");
		for (int i = 0; i < numNodes[3]; i++) {
			String s = new String("machine3_node" + i);
			if (i >= nstart && i <= (nstart + nlen)) {
				nodeUserMap.put(s, new String(System.getProperty("user.name")));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, nmode);
			} else if (i >= gstart && i <= (gstart + glen)) {
				nodeUserMap.put(s, new String("gwatson"));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, gmode);
			} else if (i >= cstart && i <= (cstart + clen)) {
				nodeUserMap.put(s, new String("crasmussen"));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, cmode);
			} else {
				nodeUserMap.put(s, new String(""));
				nodeGroupMap.put(s, new String(""));
				nodeModeMap.put(s, new String("0111"));
			}

		}

		for (int i = 0; i < numNodes[0]; i++) {
			String s = new String("machine0_node" + i);
			nodeStateMap.put(s, new String("up"));
			nodeUserMap.put(s, new String(System.getProperty("user.name")));
			nodeGroupMap.put(s, new String("ptp"));
			nodeModeMap.put(s, new String("0100"));
		}

		for (int i = 0; i < numNodes[1]; i++) {
			String s = new String("machine1_node" + i);
			nodeStateMap.put(s, new String("up"));
			if (i < 32) {
				nodeUserMap.put(s, new String(System.getProperty("user.name")));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, new String("0100"));
			} else if (i < 64) {
				nodeUserMap.put(s, new String(""));
				nodeGroupMap.put(s, new String(""));
				nodeModeMap.put(s, new String("0111"));
			} else if (i < 128) {
				nodeUserMap.put(s, new String("wjones"));
				nodeGroupMap.put(s, new String("parl"));
				nodeModeMap.put(s, new String("0100"));
			} else {
				nodeUserMap.put(s, new String("jsmith"));
				nodeGroupMap.put(s, new String("awhere"));
				nodeModeMap.put(s, new String("0111"));
			}
		}

		/*
		 * setup machine[2]'s hardware - a machine w/ a bunch of nodes w/
		 * different state
		 */
		for (int i = 0; i < numNodes[3]; i++) {
			String s = new String("machine2_node" + i);
			int r = ((int) (Math.random() * 100));
			if (r < 3) {
				nodeStateMap.put(s, new String("error"));
			} else if (r < 6)
				nodeStateMap.put(s, new String("down"));
			else {
				nodeStateMap.put(s, new String("up"));
				nodeUserMap.put(s, new String(System.getProperty("user.name")));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, new String("0100"));
			}
		}
	}

	/* returns the new job name that it started - unique */
	public String run(JobRunConfiguration jobRunConfig) {
		if (spawned_app_state != null
				&& (spawned_app_state.equals(IPProcess.STARTING) || spawned_app_state
						.equals(IPProcess.RUNNING))) {
			System.out
					.println("Another job already running, unable to start a new one.");
			return null;
		}

		numJobs++;
		final String s = new String("job" + numJobs);

		spawned_num_procs = spawned_procs_per_node = spawned_first_node = 0;

		this.spawned_num_procs = jobRunConfig.getNumberOfProcesses();
		this.spawned_procs_per_node = jobRunConfig.getNumberOfProcessesPerNode();
		this.spawned_first_node = jobRunConfig.getFirstNodeNumber();

		processMap.put(s, new Integer(spawned_num_procs));

		spawned_app_state = IPProcess.RUNNING;
		spawned_app_exit_code = new String("");
		spawned_app_signal = new String("");

		/* UNCOMMENT THIS IF YOU WANT THE JOB YOU SPAWN TO PRINT RANDOM TEXT OUTPUT 
		 * AND RANDOMLY EXIT AFTER 30SECS OR SO
		Runnable runningAppEventsRunnable = new Runnable() {
			public void run() {
				String job = new String("job" + numJobs);
				int numProcsInJob = ((Integer) (processMap.get(job)))
						.intValue();

				while (true) {
					try {
						Thread.sleep(1000 + ((int) (Math.random() * 3000)));
					} catch (Exception e) {
					}
					if (!spawned_app_state.equals(IPProcess.RUNNING))
						return;
					for (int i = 0; i < numProcsInJob; i++) {
						RuntimeEvent event = new RuntimeEvent(
								RuntimeEvent.EVENT_PROCESS_OUTPUT);
						event.setText((int) (Math.random() * 10000)
								+ " random text");
						fireEvent(new String("job" + numJobs + "_process"
								+ i), event);
					}
				}
			}
		};
		runningAppEventsThread = new Thread(runningAppEventsRunnable);
		runningAppEventsThread.start();

		Runnable runningAppFinishRunnable = new Runnable() {
			public void run() {
				try {
					Thread.sleep(30000);
				} catch (Exception e) {
				}
				if (!spawned_app_state.equals(IPProcess.RUNNING))
					return;

				spawned_app_state = IPProcess.EXITED;
				spawned_app_exit_code = new String("0");

				System.out
						.println("Simulating spawned application terminating normally.");

				processMap.remove(s);
				fireEvent(new String("job" + numJobs), new RuntimeEvent(
						RuntimeEvent.EVENT_JOB_EXITED));

			}
		};

		runningAppFinishThread = new Thread(runningAppFinishRunnable);
		runningAppFinishThread.start();
		*/

		return new String(s);
	}

	public void abortJob(String jobName) {
		spawned_app_state = IPProcess.EXITED_SIGNALLED;
		spawned_app_signal = new String("SIGTERM");
		String s = new String("job" + numJobs);
		processMap.remove(s);
	}

	public void addRuntimeListener(IRuntimeListener listener) {
		listeners.add(listener);
	}

	public void removeRuntimeListener(IRuntimeListener listener) {
		listeners.remove(listener);
	}

	protected synchronized void fireEvent(String ne, RuntimeEvent event) {
		if (listeners == null)
			return;
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			IRuntimeListener listener = (IRuntimeListener) i.next();
			switch (event.getEventNumber()) {
			case RuntimeEvent.EVENT_NODE_STATUS_CHANGE:
				listener.runtimeNodeStatusChange(ne);
				break;
			case RuntimeEvent.EVENT_PROCESS_OUTPUT:
				listener.runtimeProcessOutput(ne, event.getText());
				break;
			case RuntimeEvent.EVENT_JOB_EXITED:
				listener.runtimeJobExited(ne);
				break;
			case RuntimeEvent.EVENT_JOB_STATE_CHANGED:
				listener.runtimeJobStateChanged(ne);
				break;
			case RuntimeEvent.EVENT_NEW_JOB:
				listener.runtimeNewJob(ne);
				break;
			}
		}
	}

	public void shutdown() {
		listeners.clear();
		listeners = null;
	}

	public String[] getMachines() {
		int i = 0;
		Set set = nodeMap.keySet();
		String[] ne = new String[set.size()];
		Iterator it = set.iterator();

		while (it.hasNext()) {
			String key = (String) it.next();
			ne[i++] = new String(key);
		}

		return ne;
	}

	/* get the nodes pertaining to a certain machine */
	public String[] getNodes(String machineName) {
		/* find this machineName in the map - if it's there */
		if (!nodeMap.containsKey(machineName))
			return null;

		int n = ((Integer) (nodeMap.get(machineName))).intValue();

		String[] ne = new String[n];

		for (int i = 0; i < ne.length; i++) {
			/* prepend this node name with the machine name */
			ne[i] = new String(machineName + "_node" + i);
		}

		return ne;
	}

	public String[] getJobs() {
		int i = 0;
		Set set = processMap.keySet();
		String[] ne = new String[set.size()];
		Iterator it = set.iterator();

		while (it.hasNext()) {
			String key = (String) it.next();
			ne[i++] = new String(key);
		}

		return ne;
	}

	/* get the processes pertaining to a certain job */
	public String[] getProcesses(String jobName) {
		/* find this machineName in the map - if it's there */
		if (!processMap.containsKey(jobName))
			return null;

		int n = ((Integer) (processMap.get(jobName))).intValue();

		String[] ne = new String[n];

		for (int i = 0; i < ne.length; i++) {
			/* prepend this node name with the machine name */
			ne[i] = new String(jobName + "_process" + i);
		}

		return ne;
	}

	/*
	 * this is a major kludge, sorry but this is a dummy implementation anyway
	 * so hack this up if you want to change the process to node mapping - this
	 * assumes a certain number of jobs with a set number of processes in each
	 * job
	 */
	public String getProcessNodeName(String procName) {
		// System.out.println("getProcessNodeName("+procName+")");
		String job = procName.substring(0, procName.indexOf("process") - 1);
		// System.out.println("job = "+job);
		// String job = procName.substring(0, 4);
		/* ok this is coming from the fake job */
		if (job.equals("job" + numJobs)) {
			int numProcsInJob = ((Integer) (processMap.get(job))).intValue();
			String s = procName.substring(procName.indexOf("process") + 7,
					procName.length());
			// System.out.println("proc # = "+s);
			int procNum = -1;
			try {
				procNum = (new Integer(s)).intValue();
			} catch (NumberFormatException e) {
			}
			if (procNum != -1) {
				return "machine0_node"
						+ (spawned_first_node + (procNum / spawned_procs_per_node));
			}
		}

		return "";
	}

	public String getProcessStatus(String procName) {
		String job = procName.substring(0, 4);
		if (job.equals("job" + numJobs)) {
			// System.out.println("PROCSTATE = "+spawned_app_state);
			return spawned_app_state;
		}
		return "-1";
	}

	public String getProcessExitCode(String procName) {
		String job = procName.substring(0, 4);
		if (job.equals("job" + numJobs)) {
			// System.out.println("PROCSTATE = "+spawned_app_state);
			return spawned_app_exit_code;
		}
		return "-1";
	}

	public String getProcessSignal(String procName) {
		String job = procName.substring(0, 4);
		if (job.equals("job" + numJobs)) {
			// System.out.println("PROCSTATE = "+spawned_app_state);
			return spawned_app_signal;
		}
		return "-1";
	}

	public String getNodeMachineName(String nodeName) {
		int idx;
		idx = nodeName.indexOf("_");
		if (idx >= 0) {
			return nodeName.substring(0, idx);
		}
		return "";
	}

	public String getNodeAttribute(String nodeName, String attrib) {
		String s = null;
		if (attrib.equals("state")) {
			s = (String) nodeStateMap.get(nodeName);
		} else if (attrib.equals("mode")) {
			s = (String) nodeModeMap.get(nodeName);
		} else if (attrib.equals("user")) {
			s = (String) nodeUserMap.get(nodeName);
		} else if (attrib.equals("group")) {
			s = (String) nodeGroupMap.get(nodeName);
		}
		return s;
	}
}
