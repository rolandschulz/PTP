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

package org.eclipse.ptp.rtsystem.ompi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.RuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeJobStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeProcessOutputEvent;


public class OMPIControlSystem implements IControlSystem, IProxyRuntimeEventListener {
	private Process orted_process = null;
	private Vector knownJobs = null;
	
	protected List listeners = new ArrayList(2);
	
	private OMPIProxyRuntimeClient proxy = null;

	public OMPIControlSystem(OMPIProxyRuntimeClient proxy) {
		this.proxy = proxy;
	}
	
	private boolean failed_init = false;
	
	public void startup() {
		knownJobs = new Vector();
		
		/* start the daemon using JNI */
		//jniBroker.OMPIStartDaemon(ompi_bin_path, orted_path, split_path[split_path.length - 1], split_args);
		
		/* start the daemon using Java */
		//OMPIStartORTEd(orted_full);
		
		proxy.addRuntimeEventListener(this);
		
		/*
		int rc = jniBroker.OMPIInit();
		System.out.println("OMPI Init() return code = "+rc);
		if(rc != 0) {
			String error_msg = jniBroker.OMPIGetError();
			CoreUtils.showErrorDialog("OMPI Runtime Initialization Error", error_msg, null);
			failed_init = true;
			return;
		}*/

		//startProgressMaker();
	}
	
	/* this is how we can/would start the daemon from Java.  Call this if you really
	 * want to do this, but for OMPI it's probably required to start it through
	 * JNI.
	 */
	/*
	private void OMPIStartORTEd(String cmd)
	{
		try {
			orted_process = Runtime.getRuntime().exec(cmd);
		} catch(IOException e) {
			String err = "Some error occurred trying to spawn the ORTEd (ORTE daemon).  Check the "+
				"PTP/OPen MPI preferences page and be certain that the path and arguments "+
				"are correct.";
			System.err.println(err);
			CoreUtils.showErrorDialog("Failed to Spawn ORTED", err, null);
			failed_init = true;
		}
	}
	*/
    
	/*
	public void startProgressMaker() {
		if(!jniBroker.libraryLoaded()) {
			System.err.println("Unable to startup OMPI Control System because of a failed "+
					"library load.");
			return;
		}
		Thread progressThread = new Thread("PTP RTE OMPI Progress Thread") {
			public void run() {
				jniBroker.OMPIProgress();
			}
		};
		progressThread.start();
	}
	*/
	
	/* returns the new job name that it started - unique */
	public int run(JobRunConfiguration jobRunConfig) {
		int jobID = -1;
		System.out.println("JAVA OMPI: run() with args:\n"+jobRunConfig.toString());

		List argList = new ArrayList();
		
		argList.add("execName");
		argList.add(jobRunConfig.getExecName());
		String path = jobRunConfig.getPathToExec();
		if (path != null) {
			argList.add("pathToExec");
			argList.add(path);
		}
		argList.add("numOfProcs");
		argList.add(Integer.toString(jobRunConfig.getNumberOfProcesses()));
		argList.add("procsPerNode");
		argList.add(Integer.toString(jobRunConfig.getNumberOfProcessesPerNode()));
		argList.add("firstNodeNum");
		argList.add(Integer.toString(jobRunConfig.getFirstNodeNumber()));
		
		String dir = jobRunConfig.getWorkingDir();
		if (dir != null) {
			argList.add("workingDir");
			argList.add(dir);
		}
		String[] args = jobRunConfig.getArguments();
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				argList.add("progArg");
				argList.add(args[i]);
			}
		}
		String[] env = jobRunConfig.getEnvironment();
		if (env != null) {
			for (int i = 0; i < env.length; i++) {
				argList.add("progEnv");
				argList.add(env[i]);
			}
		}
		
		if (jobRunConfig.isDebug()) {
			argList.add("debuggerPath");
			argList.add(jobRunConfig.getDebuggerPath());
			String[] dbgArgs = jobRunConfig.getDebuggerArgs();
			if (dbgArgs != null) {
				for (int i = 0; i < dbgArgs.length; i++) {
					argList.add("debuggerArg");
					argList.add(dbgArgs[i]);
				}
			}
		}
		
		try {
			jobID = proxy.runJob((String[])argList.toArray(new String[0]));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return jobID;
	}

	public void terminateJob(IPJob job) {
		if(job == null) {
			System.err.println("ERROR: Tried to abort a null job.");
			return;
		}
		
		int jobID = job.getJobNumberInt();

		if(jobID >= 0) {
			System.out.println("OMPIControlSystem: abortJob() with name "+job.toString()+" and ID "+jobID);
			try {
				proxy.terminateJob(jobID);
			} catch(IOException e) {
				e.printStackTrace();
			}
		//	jniBroker.OMPITerminateJob(jobID);
		}
		else {
			System.err.println("ERROR: Tried to abort a null job.");
		}
	}
	
	public String[] getJobs() 
	{
		Object a[];
		//System.out.println("JAVA OMPI: getJobs() called");

		if(knownJobs == null) {
			System.out.println("NULL JOBS!");
			return null;
		}
		a = knownJobs.toArray();
		if(a == null) return null;
		if(a.length == 0) return null;
		return (String[])a;
	}

	/* get the processes pertaining to a certain job */
	public String[] getProcesses(IPJob job) 
	{
		int jobID = job.getJobNumberInt();
		int numProcs = -1;
		
		/* need to check is jobName is a valid job name */
		try {
			numProcs = proxy.getJobProcesses(jobID);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		if(numProcs <= 0) return null;

		String[] ne = new String[numProcs];
		
		for(int i=0; i<numProcs; i++) {
			ne[i] = new String("job"+jobID+"_process"+i);
		}
		
		return ne;
	}
	
	public String[] getAllProcessesAttributes(IPJob job, String[] attribs)
	{
		String[] values = null;
		
		try {
			int jobID = job.getJobNumberInt();
			values = proxy.getAllProcessesAttribuesBlocking(jobID, attribs);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return values;
	}
	
	public String[] getProcessAttributes(IPProcess proc, String[] attrib)
	{
		String[] values = null;
		
		IPJob job = proc.getJob();
		
		try {
			int jobID = job.getJobNumberInt();
			int procID = proc.getTaskId();
			values = proxy.getProcessAttributesBlocking(jobID, procID, attrib);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		/* this part is hacked right now until we get this out of ORTE */
		//if(attrib.equals(AttributeConstants.ATTRIB_PROCESS_NODE_NAME)) {
		//	return "machine0_node0";
		//}
		
		return values;
	}

	public void addRuntimeListener(IRuntimeListener listener) {
		listeners.add(listener);
	}

	public void removeRuntimeListener(IRuntimeListener listener) {
		listeners.remove(listener);
	}

	protected synchronized void fireEvent(String ID, RuntimeEvent event) {
		if (listeners == null)
			return;
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			IRuntimeListener listener = (IRuntimeListener) i.next();
			switch (event.getEventNumber()) {
			case RuntimeEvent.EVENT_NODE_STATUS_CHANGE:
				listener.runtimeNodeStatusChange(ID);
				break;
			case RuntimeEvent.EVENT_PROCESS_OUTPUT:
				listener.runtimeProcessOutput(ID, event.getText());
				break;
			case RuntimeEvent.EVENT_JOB_EXITED:
				listener.runtimeJobExited(ID);
				break;
			case RuntimeEvent.EVENT_JOB_STATE_CHANGED:
				listener.runtimeJobStateChanged(ID, event.getText());
				break;
			case RuntimeEvent.EVENT_NEW_JOB:
				listener.runtimeNewJob(ID);
				break;
			}
		}
	}

	public void shutdown() {
//		if(!jniBroker.libraryLoaded()) {
//			System.err.println("Unable to startup OMPI Control System because of a failed "+
//					"library load.");
//			return;
//		}
		System.out.println("OMPIControlSystem: shutdown() called");
//		
//		/* shutdown/kill the ORTE daemon */
//		jniBroker.OMPIShutdown();
//		
//		/* finalize the registry - yes, it's odd it is in this order */
//		jniBroker.OMPIFinalize();
//		
//		/*
//		if(orted_process != null) {
//			System.out.println("DESTROY ORTED!");
//			orted_process.destroy();
//			orted_process = null;
//			orted_process = null;
//		}
//		*/
		listeners.clear();
		listeners = null;
	}

    public synchronized void handleEvent(IProxyRuntimeEvent e) {
        //System.out.println("OMPIControlSystem got event: " + e.toString());
        if(e instanceof ProxyRuntimeJobStateEvent) {
        		RuntimeEvent re = new RuntimeEvent(RuntimeEvent.EVENT_JOB_STATE_CHANGED);
        		int state = ((ProxyRuntimeJobStateEvent)e).getJobState();
        		String stateStr = IPProcess.ERROR;
        		
        		switch(state) {
        			case 1: case 3:
        				stateStr = IPProcess.STARTING;
        				break;
        			case 4:
        				stateStr = IPProcess.RUNNING;
        				break;
        			case 8: case 9:
        				stateStr = IPProcess.EXITED;
        		}
        		
        		re.setText(stateStr);
        		fireEvent("job"+((ProxyRuntimeJobStateEvent)e).getJobID(), re);
        }
        else if(e instanceof ProxyRuntimeProcessOutputEvent) {
        		RuntimeEvent re = new RuntimeEvent(RuntimeEvent.EVENT_PROCESS_OUTPUT);
        		int jobID = ((ProxyRuntimeProcessOutputEvent)e).getJobID();
        		int procID = ((ProxyRuntimeProcessOutputEvent)e).getProcessID();
        		String text = ((ProxyRuntimeProcessOutputEvent)e).getText();
        		
        		re.setText(text);
        		fireEvent("job"+jobID+"_process"+procID, re);
        }
    }
}
