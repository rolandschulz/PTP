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

package org.eclipse.ptp.rtsystem.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.RuntimeEvent;

public class SimulationControlSystem implements IControlSystem {


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

	protected HashMap processMap;

	public SimulationControlSystem() {
	}
	
	public void startup() {
		processMap = new HashMap();
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
			String s = procName.substring(procName.indexOf("process") + 7,
					procName.length());
			// System.out.println("proc # = "+s);
			int procNum = -1;
			try {
				procNum = (new Integer(s)).intValue();
			} catch (NumberFormatException e) {
			}
			if (procNum != -1) {
				return "machine0_node" + spawned_first_node + (procNum / spawned_procs_per_node);
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
	
	public int getProcessPID(String procName) {
		return ((int)(Math.random() * 10000)) + 1000;
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
}
