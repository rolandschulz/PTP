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
import java.util.Set;
import java.util.Vector;

import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.RuntimeEvent;

public class SimulationControlSystem implements IControlSystem {


	protected int numJobs = -1;

	protected List listeners = new ArrayList(2);

	protected Thread runningAppEventsThread = null;

	protected Thread runningAppFinishThread = null;

	protected HashMap processMap;
	
	protected Vector simJobs = null;

	public SimulationControlSystem() {
		simJobs = new Vector();
	}
	
	public void startup() {
		processMap = new HashMap();
	}
	
	/* returns the new job name that it started - unique */
	public int run(JobRunConfiguration jobRunConfig) {
		/*
		if (spawned_app_state != null
				&& (spawned_app_state.equals(IPProcess.STARTING) || spawned_app_state
						.equals(IPProcess.RUNNING))) {
			System.out
					.println("Another job already running, unable to start a new one.");
			return null;
		}
		*/

		numJobs++;
		final String s = new String("job" + numJobs);
		
		SimJobState ss = new SimJobState();
		ss.jobname = s;
		ss.spawned_num_procs = ss.spawned_procs_per_node = ss.spawned_first_node = 0;

		ss.machine_name = jobRunConfig.getMachineName();
		ss.spawned_num_procs = jobRunConfig.getNumberOfProcesses();
		ss.spawned_procs_per_node = jobRunConfig.getNumberOfProcessesPerNode();
		ss.spawned_first_node = jobRunConfig.getFirstNodeNumber();

		processMap.put(s, new Integer(ss.spawned_num_procs));

		ss.spawned_app_state = IPProcess.STARTING;
		ss.spawned_app_exit_code = new String("");
		ss.spawned_app_signal = new String("");
		
		simJobs.addElement(ss);

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

		return numJobs;
	}

	public void terminateJob(IPJob jobName) {
		String tname = (String)jobName.getAttribute(AttributeConstants.ATTRIB_NAME);
		
		IPProcess[] ps = jobName.getProcesses();
		for(int i=0; i<ps.length; i++) {
			ps[i].setTerminated(true);
			if(ps[i] instanceof SimProcess) {
				((java.lang.Process)ps[i]).destroy();
			}
		}
		
		for(int i=0; i<simJobs.size(); i++) {
			SimJobState ss = (SimJobState)simJobs.elementAt(i);
			/* found */
			if(ss.jobname.equals(tname)) {
				/* only do these if we really want to DELETE the job, not just set its state at terminated */
				//simJobs.remove(i);
				//processMap.remove(tname);
				ss.spawned_app_state = IPProcess.EXITED_SIGNALLED;
				ss.spawned_app_signal = new String("SIGTERM");
				return;
			}
		}

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
	public String[] getProcesses(IPJob job) {
		String jobName = job.toString();
		System.out.println("getProcesses("+jobName+")");
		/* find this machineName in the map - if it's there */
		if (!processMap.containsKey(jobName)) {
			System.out.println("getProcesses - null - can't find this job!");
			return null;
		}

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
	private String getProcessNodeName(String procName) {
		// System.out.println("getProcessNodeName("+procName+")");
		String job = procName.substring(0, procName.indexOf("process") - 1);

		for(int i=0; i<simJobs.size(); i++) {
			SimJobState ss = (SimJobState)simJobs.elementAt(i);
			if(ss.jobname.equals(job)) {
				String s = procName.substring(procName.indexOf("process") + 7,
						procName.length());
				int procNum = -1;
				try {
					procNum = (new Integer(s)).intValue();
				} catch(NumberFormatException e) {
				}
				if(procNum != -1) {
					return ss.machine_name + "_node" + (ss.spawned_first_node + (procNum / ss.spawned_procs_per_node));
				}
			}
		}

		return "";
	}

	private String getProcessStatus(String procName) {
		String job = procName.substring(0, 4);
		for(int i=0; i<simJobs.size(); i++) {
			SimJobState ss = (SimJobState)simJobs.elementAt(i);
			if(ss.jobname.equals(job))
				return ss.spawned_app_state;
		}

		return "-1";
	}

	private String getProcessExitCode(String procName) {
		String job = procName.substring(0, 4);
		for(int i=0; i<simJobs.size(); i++) {
			SimJobState ss = (SimJobState)simJobs.elementAt(i);
			if(ss.jobname.equals(job))
				return ss.spawned_app_exit_code;
		}

		return "-1";
	}

	private String getProcessSignal(String procName) {
		String job = procName.substring(0, 4);
		for(int i=0; i<simJobs.size(); i++) {
			SimJobState ss = (SimJobState)simJobs.elementAt(i);
			if(ss.jobname.equals(job))
				return ss.spawned_app_signal;
		}

		return "-1";
	}
	
	public String[] getProcessAttribute(IPJob job, IPProcess proc, String attrib)
	{
		String procName = proc.getElementName();
		
		System.out.println("JAVA OMPI: getProcessAttribute(" + procName + ", "
				+ attrib + ") called");
		String s = null;

		if (attrib.equals(AttributeConstants.ATTRIB_PROCESS_PID)) {
			s = ""+((int)(Math.random() * 10000)) + 1000+"";
		} else if (attrib.equals(AttributeConstants.ATTRIB_PROCESS_EXIT_CODE)) {
			s = getProcessExitCode(procName);
		} else if (attrib.equals(AttributeConstants.ATTRIB_PROCESS_SIGNAL)) {
			s = getProcessSignal(procName);
		} else if (attrib.equals(AttributeConstants.ATTRIB_PROCESS_STATUS)) {
			s = getProcessStatus(procName);
		} else if (attrib.equals(AttributeConstants.ATTRIB_PROCESS_NODE_NAME)) {
			s = getProcessNodeName(procName);
		}
		
		String[] s2 = new String[1];
		s2[0] = s;
		return s2;
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
				listener.runtimeJobStateChanged(ne, "<SIMULATED>");
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
