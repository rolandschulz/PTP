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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.proxy.event.ProxyDisconnectedEvent;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.RuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeDisconnectedEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeJobStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeProcessOutputEvent;


public class OMPIControlSystem implements IControlSystem, IProxyRuntimeEventListener {
	private Vector knownJobs = null;
	
	protected List listeners = new ArrayList(2);
	
	private OMPIProxyRuntimeClient proxy = null;
	private boolean proxyDead = true;

	public OMPIControlSystem(OMPIProxyRuntimeClient proxy) {
		this.proxy = proxy;
		if(proxy != null) proxyDead = false;
	}
	
	public boolean isHealthy() { return !proxyDead; }
	
	public void startup() {
		knownJobs = new Vector();
		
		proxy.addRuntimeEventListener(this);
	}
	
	/* returns the new job name that it started - unique */
	public int run(JobRunConfiguration jobRunConfig) throws CoreException {
		int jobID = -1;
		System.out.println("JAVA OMPI: run() with args:\n"+jobRunConfig.toString());
		
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Control system is shut down", null));
		}

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
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
				"Control system is shut down, proxy exception.  The proxy may have crashed or been killed.", null));
		}
		
		return jobID;
	}

	public void terminateJob(IPJob job) throws CoreException {
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Control system is shut down", null));
		}
		
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
				throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
					"Control system is shut down, proxy exception.  The proxy may have crashed or been killed.", null));
			}
		}
		else {
			System.err.println("ERROR: Tried to abort a null job.");
		}
	}
	
	public String[] getJobs() throws CoreException 
	{
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Control system is shut down", null));
		}
		
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
	public String[] getProcesses(IPJob job) throws CoreException 
	{
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Control system is shut down", null));
		}
		
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
	
	public String[] getAllProcessesAttributes(IPJob job, String[] attribs) throws CoreException
	{
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Control system is shut down", null));
		}
		
		String[] values = null;
		
		try {
			int jobID = job.getJobNumberInt();
			values = proxy.getAllProcessesAttribuesBlocking(jobID, attribs);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return values;
	}
	
	public String[] getProcessAttributes(IPProcess proc, String[] attrib) throws CoreException
	{
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Control system is shut down", null));
		}
		
		String[] values = null;
		
		IPJob job = proc.getJob();
		
		try {
			int jobID = job.getJobNumberInt();
			int procID = proc.getTaskId();
			values = proxy.getProcessAttributesBlocking(jobID, procID, attrib);
		} catch(IOException e) {
			e.printStackTrace();
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Control system is shut down, proxy exception", null));
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
		System.out.println("OMPIControlSystem: shutdown() called");
		listeners.clear();
		listeners = null;
	}

    public synchronized void handleEvent(IProxyRuntimeEvent e) {
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
		
        else if(e instanceof ProxyRuntimeErrorEvent) {
			System.err.println("Fatal error from proxy: '"+((ProxyRuntimeErrorEvent)e).getErrorMessage()+"'");
			int errorCode = ((ProxyRuntimeErrorEvent)e).getErrorCode();
			String errorMsg = ((ProxyRuntimeErrorEvent)e).getErrorMessage();
			PTPCorePlugin.errorDialog("Fatal PTP Control System Error",
					"There was a fatal PTP Control System error (ERROR CODE: "+errorCode+").\n"+
					"Error message: \""+errorMsg+"\"\n\n"+
					"Control System is now disabled.", null);
			proxyDead = true;
		}
        
        	else if(e instanceof ProxyRuntimeDisconnectedEvent) {
        		boolean is_error = ((ProxyRuntimeDisconnectedEvent)e).wasError();
        		System.out.println("Proxy Disconnected.");
        		proxyDead = true;
        		if(is_error) {
        			PTPCorePlugin.errorDialog("Fatal PTP Control System Error",
        					"There was a fatal PTP Control System error.  The proxy "+
        					"server disconnected with an error.\n\n"+
        					"Control System is now disabled.", null);
        		}
        		//PTPCorePlugin.errorDialog("Fatal PTP Control System Error",
        		//		"The "
        		
        	}
        	
    }
}
