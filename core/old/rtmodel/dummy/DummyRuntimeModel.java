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

package org.eclipse.ptp.rtmodel.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.internal.core.PUniverse;
import org.eclipse.ptp.launch.core.IParallelLaunchListener;
import org.eclipse.ptp.launch.internal.AbstractParallelLaunchConfigurationDelegate;
import org.eclipse.ptp.rtmodel.IRuntimeListener;
import org.eclipse.ptp.rtmodel.IRuntimeModel;
import org.eclipse.ptp.rtmodel.NamedEntity;
import org.eclipse.ptp.rtmodel.RuntimeEvent;

public class DummyRuntimeModel implements IRuntimeModel {
	
	/* define the number of machines here */
	final static int numMachines = 4;
	/* define how many nodes each machine has - the array length must equal
	 * numMachines
	 */
	final static int[] numNodes = { 256, 64, 128, 512 };
	protected HashMap nodeMap;
	protected HashMap nodeUserMap;
	protected HashMap nodeGroupMap;
	protected HashMap nodeModeMap;
	protected HashMap nodeStateMap;
	
	/* define the number of jobs here */
	final static int numFakeJobs = 2;
	/* define how many processes are in each job - the array length must
	 * equal numJobs
	 */
	final static int[] numFakeProcesses = { 3, 5 };
	protected HashMap processMap;
	
	protected List listeners = new ArrayList(2);
	
	protected String spawned_app_state = null;
	protected int spawned_num_procs = 0;
	protected int spawned_procs_per_node = 0;
	protected int spawned_first_node = 0;
	
	protected Thread runningAppEventsThread = null;
	protected Thread runningAppFinishThread = null;
	
	public DummyRuntimeModel() {
		nodeMap = new HashMap();
		for(int i=0; i<numMachines; i++) {
			String s = new String("machine"+i);
			nodeMap.put(s, new Integer(numNodes[i]));
		}

		processMap = new HashMap();
		for(int i=0; i<numFakeJobs; i++) {
			String s = new String("job"+i);
			processMap.put(s, new Integer(numFakeProcesses[i]));
		}
		
		int totnodes = 0;
		for(int i=0; i<numMachines; i++) {
			totnodes += numNodes[i];
		}
		nodeUserMap = new HashMap();
		nodeGroupMap = new HashMap();
		nodeModeMap = new HashMap();
		nodeStateMap = new HashMap();
		
		/* machine 3 has a bunch of nodes w/ different states */
		for(int i=0; i<numNodes[3]; i++) {
			String s = new String("machine3_node"+i);
			int r = ((int)(Math.random() * 100));
			if(r < 10) nodeStateMap.put(s, new String("error"));
			else if(r < 30) nodeStateMap.put(s, new String("down"));
			else {
				nodeStateMap.put(s, new String("up"));
				/*
				r = ((int)(Math.random() * 100));
				if(r < 10) {
					nodeUserMap.put(s, new String("ndebard"));
					nodeGroupMap.put(s, new String("ptp"));
				}
				else if(r < 20) {
					nodeUserMap.put(s, new String("gwatson"));
					nodeGroupMap.put(s, new String("ptp"));
				}
				else if(r < 30) {
					nodeUserMap.put(s, new String("crasmussen"));
					nodeGroupMap.put(s, new String("ptp"));
				}
				*/
			}
		}
		int nstart = ((int)(Math.random() * 50));
		int nlen = ((int)(Math.random() * 40 + 10));
		String nmode = new String(((int)(Math.random() * 2)) == 0 ? "0111" : "0100");
		int gstart = ((int)(Math.random() * 50 + (nstart + nlen)));
		int glen = ((int)(Math.random() * 100 + 40));
		String gmode = new String(((int)(Math.random() * 2)) == 0 ? "0111" : "0100");
		int cstart = ((int)(Math.random() * 50 + (nstart + nlen + gstart + glen)));
		int clen = ((int)(Math.random() * 75 + 25));
		String cmode = new String(((int)(Math.random() * 2)) == 0 ? "0111" : "0100");
		for(int i=0; i<numNodes[3]; i++) {
			String s = new String("machine3_node"+i);
			if(i >= nstart && i <= (nstart + nlen)) {
				nodeUserMap.put(s, new String(System.getProperty("user.name")));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, nmode);
			}
			else if(i >= gstart && i <= (gstart + glen)) {
				nodeUserMap.put(s, new String("gwatson"));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, gmode);
			}
			else if(i >= cstart && i <= (cstart + clen)) {
				nodeUserMap.put(s, new String("crasmussen"));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, cmode);
			}
			else {
				nodeUserMap.put(s, new String(""));
				nodeGroupMap.put(s, new String(""));
				nodeModeMap.put(s, new String("0111"));
			}
			
		}
		
		for(int i=0; i<numMachines - 1; i++) {
			for(int j=0; j<numNodes[i]; j++) {
				String s = new String("machine"+i+"_node"+j);
				nodeStateMap.put(s, new String("up"));
				nodeUserMap.put(s, new String("root"));
				nodeGroupMap.put(s, new String("root"));
				nodeModeMap.put(s, new String("0111"));
			}
		}
		
		startDummyEventGeneration();
	}
	
	/* returns the new job name that it started - unique */
	public NamedEntity run(String[] args) {
		if(spawned_app_state != null && (spawned_app_state.equals(IPProcess.STARTING) || spawned_app_state.equals(IPProcess.RUNNING))) {
			System.out.println("Another job already running, unable to start a new one.");
			return null;
		}
		/*
		System.out.println("DummyRTM.run("+args+")");
		for(int i=0; i<args.length; i++) {
			System.out.println("\targs["+i+"] = "+args[i]);
		}*/
		
		/* the next job will logically be the number of fake jobs
		 * we've already spawned, like fakeJob = 2 would take
		 * up elements 0 and 1, so '2' would be next!
		 */
		String s = new String("job"+numFakeJobs);

		spawned_num_procs = spawned_procs_per_node = spawned_first_node = 0;
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equals(AbstractParallelLaunchConfigurationDelegate.NUM_PROC)) {
				this.spawned_num_procs = (new Integer(args[i+1]).intValue());
				i++;
			}
			else if(args[i].equals(AbstractParallelLaunchConfigurationDelegate.PROC_PER_NODE)) {
				this.spawned_procs_per_node = (new Integer(args[i+1]).intValue());
				i++;
			}
			else if(args[i].equals(AbstractParallelLaunchConfigurationDelegate.START_NODE)) {
				this.spawned_first_node = (new Integer(args[i+1]).intValue());
				i++;
			}
		}
		
		processMap.put(s, new Integer(spawned_num_procs));
		
		spawned_app_state = IPProcess.RUNNING;
		
		Runnable runningAppEventsRunnable = new Runnable() {
			public void run() {
				String job = new String("job"+numFakeJobs);
				int numProcsInJob = ((Integer)(processMap.get(job))).intValue();
			
				while(true) {
					try {
						Thread.sleep(1000 + ((int)(Math.random() * 3000)));
					} catch(Exception e) {	
					}
					if(!spawned_app_state.equals(IPProcess.RUNNING)) return;
					for(int i=0; i<numProcsInJob; i++) {
						RuntimeEvent event = new RuntimeEvent(RuntimeEvent.EVENT_PROCESS_OUTPUT);
						event.setText((int)(Math.random() * 10000)+" random text");
						fireEvent(new NamedEntity("job"+numFakeJobs+"_process"+i), event);
					}
				}
			}
		};
		runningAppEventsThread = new Thread(runningAppEventsRunnable);
		runningAppEventsThread.start();
		
		Runnable runningAppFinishRunnable = new Runnable() {
			public void run() {
				try {
					Thread.sleep(10000);
				} catch(Exception e) { 
				}
				if(!spawned_app_state.equals(IPProcess.RUNNING)) return;
				
				spawned_app_state = IPProcess.EXITED;
				
				System.out.println("Application terminated normally.");
				
				fireEvent(new NamedEntity("job"+numFakeJobs), new RuntimeEvent(RuntimeEvent.EVENT_JOB_EXITED));
			}
		};
		
		runningAppFinishThread = new Thread(runningAppFinishRunnable);
		runningAppFinishThread.start();
		
		return new NamedEntity(s);
	}
	
	public NamedEntity abortJob() {
		spawned_app_state = IPProcess.EXITED_SIGNALLED;
		String s = new String("job"+numFakeJobs);
		processMap.remove(s);
		return new NamedEntity(s);
	}
	
	protected void startDummyEventGeneration() {
		Runnable runnable = new Runnable() {
			public void run() {
				for(int i=0; i<0; i++) {
					try {
						Thread.sleep(10000);
					} catch(Exception e) {	
					}
					System.out.println("10000 passed!");
					int rmac = (int)(Math.random() * numMachines);
					int rnod = (int)(Math.random() * numNodes[rmac]);
					fireEvent(new NamedEntity("machine"+rmac+"_node"+rnod), 
						new RuntimeEvent(RuntimeEvent.EVENT_NODE_STATUS_CHANGE));
				}
			}
		};
		new Thread(runnable).start();
	}
	
	public void addRuntimeListener(IRuntimeListener listener) {
		listeners.add(listener);
	}

	public void removeRuntimeListener(IRuntimeListener listener) {
		listeners.remove(listener);
	}
	
	protected synchronized void fireEvent(NamedEntity ne, RuntimeEvent event) {
		if(listeners == null) return;
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
			}
		}
	}
	
	public void shutdown() {
		listeners.clear();
		listeners = null;
	}

	public NamedEntity[] getMachines() {
		int i = 0;
		Set set = nodeMap.keySet();
		NamedEntity[] ne = new NamedEntity[set.size()];
		Iterator it = set.iterator();
		
		while(it.hasNext()) {
			String key = (String)it.next();
			ne[i++] = new NamedEntity(key);
		}
		
		return ne;
	}

	/* get the nodes pertaining to a certain machine */
	public NamedEntity[] getNodes(String machineName) {
		/* find this machineName in the map - if it's there */
		if(!nodeMap.containsKey(machineName)) return null;
		
		int n = ((Integer)(nodeMap.get(machineName))).intValue();
		
		NamedEntity[] ne = new NamedEntity[n];
		
		for(int i=0; i<ne.length; i++) {
			/* prepend this node name with the machine name */
			ne[i] = new NamedEntity(machineName+"_node"+i);
		}
		
		return ne;
	}
	
	public NamedEntity[] getJobs() {
		int i = 0;
		Set set = processMap.keySet();
		NamedEntity[] ne = new NamedEntity[set.size()];
		Iterator it = set.iterator();
		
		while(it.hasNext()) {
			String key = (String)it.next();
			ne[i++] = new NamedEntity(key);
		}
		
		return ne;
	}
	
	/* get the processes pertaining to a certain job */
	public NamedEntity[] getProcesses(String jobName) {
		/* find this machineName in the map - if it's there */
		if(!processMap.containsKey(jobName)) return null;
		
		int n = ((Integer)(processMap.get(jobName))).intValue();
		
		NamedEntity[] ne = new NamedEntity[n];
		
		for(int i=0; i<ne.length; i++) {
			/* prepend this node name with the machine name */
			ne[i] = new NamedEntity(jobName+"_process"+i);
		}
		
		return ne;
	}
	
	/* this is a major kludge, sorry but this is a dummy implementation anyway
	 * so hack this up if you want to change the process to node mapping - this
	 * assumes a certain number of jobs with a set number of processes in each
	 * job
	 */
	public String getProcessNodeName(String procName) {
		//System.out.println("getProcessNodeName("+procName+")");
		String job = procName.substring(0, procName.indexOf("process")-1);
		//System.out.println("job = "+job);
		//String job = procName.substring(0, 4);
		/* ok this is coming from the fake job */
		if(job.equals("job"+numFakeJobs)) {
			int numProcsInJob = ((Integer)(processMap.get(job))).intValue();
			String s = procName.substring(procName.indexOf("process")+7, procName.length());
			//System.out.println("proc # = "+s);
			int procNum = -1;
			try {
				procNum = (new Integer(s)).intValue();
			} 
			catch(NumberFormatException e) {	
			}
			if(procNum != -1) {
				return "machine0_node"+(spawned_first_node+(procNum / spawned_procs_per_node));
			}
		}
		
		if(procName.equals("job0_process0")) return "machine1_node0";
		if(procName.equals("job0_process1")) return "machine1_node0";
		if(procName.equals("job0_process2")) return "machine1_node1";
		if(procName.equals("job1_process0")) return "machine1_node2";
		if(procName.equals("job1_process1")) return "machine1_node1";
		if(procName.equals("job1_process2")) return "machine2_node0";
		if(procName.equals("job1_process3")) return "machine2_node1";
		if(procName.equals("job1_process4")) return "machine2_node2";
		return "";
	}
	
	public String getProcessStatus(String procName) {
		String job = procName.substring(0, 4);
		/* ok, this is coming from a fake job */
		if(job.equals("job"+numFakeJobs)) {
			//System.out.println("PROCSTATE = "+spawned_app_state);
			return spawned_app_state;
		}
		return "-1";
	}
	
	public String getNodeMachineName(String nodeName) {
		int idx;
		idx = nodeName.indexOf("_");
		if(idx >= 0) {
			return nodeName.substring(0, idx);
		}
		return "";
	}
	
	public String getNodeAttribute(String nodeName, String attrib) {
		String s = null;
		if(attrib.equals("state")) {
			s = (String)nodeStateMap.get(nodeName);
		}
		else if(attrib.equals("mode")) {
			s = (String)nodeModeMap.get(nodeName);
		}
		else if(attrib.equals("user")) {
			s = (String)nodeUserMap.get(nodeName);
		}
		else if(attrib.equals("group")) {
			s = (String)nodeGroupMap.get(nodeName);
		}
		return s;
	}
}
