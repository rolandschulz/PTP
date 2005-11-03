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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.ControlSystemChoices;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.MonitoringSystemChoices;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEventListener;
import org.eclipse.ptp.core.proxy.event.ProxyConnectedEvent;
import org.eclipse.ptp.internal.core.CoreUtils;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.NamedEntity;
import org.eclipse.ptp.rtsystem.RuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.ProxyRuntimeClient;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;


public class OMPIControlSystem implements IControlSystem, IProxyRuntimeEventListener{
	private Process orted_process = null;
	private Vector knownJobs = null;
	
	protected List listeners = new ArrayList(2);
	
	private OMPIProxyRuntimeClient proxy = null;

	public OMPIControlSystem(OMPIProxyRuntimeClient proxy) {
		this.proxy = proxy;
	}
	
	private boolean failed_init = false;
	
    private synchronized void wait_for_event() {
        try {
            wait();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
	public void startup() {
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		String orted_path = preferences.getString(PreferenceConstants.ORTE_ORTED_PATH);
		System.out.println("ORTED path = ."+orted_path+".");
		if(orted_path == "") {
			String err = "Some error occurred trying to spawn the ORTEd (ORTE daemon).  Check the "+
				"PTP/Open MPI preferences page and be certain that the path and arguments "+
				"are correct.";
			System.err.println(err);
			CoreUtils.showErrorDialog("ORTEd Start Failure", err, null);
			return;
		}
		
		knownJobs = new Vector();
		
		String ompi_bin_path = orted_path.substring(0, orted_path.lastIndexOf("/"));
		
		String orted_args = preferences.getString(PreferenceConstants.ORTE_ORTED_ARGS);
		String orted_full = orted_path + " " + orted_args;
		System.out.println("ORTED = "+orted_full);
		/* start the orted */
		String[] split_args = orted_args.split("\\s");
		for (int x=0; x<split_args.length; x++)
	         System.out.println("["+x+"] = "+split_args[x]);
		String[] split_path = orted_path.split("\\/");
		for(int x=0; x<split_path.length; x++)
			System.out.println("["+x+"] = "+split_path[x]);
		
		/* start the daemon using JNI */
		//jniBroker.OMPIStartDaemon(ompi_bin_path, orted_path, split_path[split_path.length - 1], split_args);
		
		/* start the daemon using Java */
		//OMPIStartORTEd(orted_full);
		
		proxy.addEventListener(this);
		
		try {
			proxy.startDaemon(ompi_bin_path, orted_path, split_path[split_path.length - 1], split_args);
			System.out.println("Control SYSTEM: startDaemon command issued!");
		} catch(IOException e) {
			System.err.println("Exception starting daemon. :(");
			System.exit(1);
		}
		
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
	public String run(JobRunConfiguration jobRunConfig) {
		int jobID = -1;
		System.out.println("JAVA OMPI: run() with args:\n"+jobRunConfig.toString());

		String[] args = new String[8];
		args[0] = "pathToExecutable";
		args[1] = jobRunConfig.getPathToExec();
		args[2] = "numberOfProcesses";
		args[3] = ""+jobRunConfig.getNumberOfProcesses()+"";
		args[4] = "numberOfProcessesPerNode";
		args[5] = ""+jobRunConfig.getNumberOfProcessesPerNode()+"";
		args[6] = "firstNodeNumber";
		args[7] = ""+jobRunConfig.getFirstNodeNumber()+"";
		/*
		try {
			proxy.startDaemon(ompi_bin_path, orted_path, split_path[split_path.length - 1], split_args);
			System.out.println("Control SYSTEM: startDaemon command issued!");
		} catch(IOException e) {
			System.err.println("Exception starting daemon. :(");
			System.exit(1);
		}*/
		
		try {
			proxy.run(jobRunConfig.getPathToExec(), jobRunConfig.getNumberOfProcesses(), false);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
		
		//jobID = jniBroker.OMPIRun(args);
//		if(jobID == -1) {
//			/* error occurred */
//			//String error_msg = jniBroker.OMPIGetError();
//			//CoreUtils.showErrorDialog("OMPI Parallel Run/Spawn Error", error_msg, null);
//			return null;
//		}
//		else {
//			/* the job creation worked - we have a new job, tell the caller the new job name */
//			String s = new String("job"+jobID);
//			knownJobs.addElement(s);
//			return s;
//		}
	}

	public void terminateJob(IPJob job) {
		if(job == null) {
			System.err.println("ERROR: Tried to abort a null job.");
			return;
		}
		
		String jobIDs = job.getJobNumber();
		int jobID = -1;
		try {
			jobID = Integer.parseInt(jobIDs);
		} catch (NumberFormatException e) {
			jobID = -1;
		}
		if(jobID >= 0) {
			System.out.println("JAVA OMPI: abortJob() with name "+job.toString()+" and ID "+jobID);
		//	jniBroker.OMPITerminateJob(jobID);
		}
		else {
			System.err.println("ERROR: Tried to abort a null job.");
		}
	}
	
	public String[] getJobs() {
		Object a[];
		System.out.println("JAVA OMPI: getJobs() called");

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
	public String[] getProcesses(String jobName) {
		System.out.println("JAVA OMPI: getProcesses(" + jobName + ") called");

		/* need to check is jobName is a valid job name */

		String[] ne = new String[1];
		ne[0] = new String("job0_process0");
		return ne;
	}
	
	public String getProcessAttribute(String procName, String attrib)
	{
		System.out.println("JAVA OMPI: getProcessAttribute(" + procName + ", "
				+ attrib + ") called");
		String s = null;

		if (attrib.equals(AttributeConstants.ATTRIB_PROCESS_PID)) {
			s = ""+((int)(Math.random() * 10000)) + 1000+"";
		} else if (attrib.equals(AttributeConstants.ATTRIB_PROCESS_EXIT_CODE)) {
			s = "-1";
		} else if (attrib.equals(AttributeConstants.ATTRIB_PROCESS_SIGNAL)) {
			s = "-1";
		} else if (attrib.equals(AttributeConstants.ATTRIB_PROCESS_STATUS)) {
			s = "-1";
		} else if (attrib.equals(AttributeConstants.ATTRIB_PROCESS_NODE_NAME)) {
			s = "machine0_node0";
		}
		return s;
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
				listener.runtimeJobStateChanged(ID);
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
        // TODO Auto-generated method stub
        System.out.println("got event: " + e.toString());
        /*
        if (e.getEventID() == IProxyDebugEvent.EVENT_DBG_INIT) {
            numServers = ((ProxyDebugInitEvent)e).getNumServers();
            System.out.println("num servers = " + numServers);
        }
        */
        //this.events.addItem(e);
        notifyAll();
    }
}
