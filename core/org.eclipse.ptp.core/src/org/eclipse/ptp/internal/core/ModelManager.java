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
package org.eclipse.ptp.internal.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.IParallelModelListener;
import org.eclipse.ptp.core.MonitoringSystemChoices;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.internal.core.CoreUtils;
import org.eclipse.ptp.internal.core.CoreMessages;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.NamedEntity;
import org.eclipse.ptp.rtsystem.ompi.OMPIControlSystem;
import org.eclipse.ptp.rtsystem.ompi.OMPIJNIBroker;
import org.eclipse.ptp.rtsystem.ompi.OMPIMonitoringSystem;
import org.eclipse.ptp.rtsystem.simulation.SimProcess;
import org.eclipse.ptp.rtsystem.simulation.SimulationControlSystem;
import org.eclipse.ptp.rtsystem.simulation.SimulationMonitoringSystem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.progress.IProgressService;

public class ModelManager implements IModelManager, IRuntimeListener {
	protected List listeners = new ArrayList(2);

	protected int currentState = STATE_EXIT;

	// protected IPMachine machine = null;
	protected IPJob processRoot = null;

	protected IPUniverse universe = null;

	protected boolean isPerspectiveOpen = false;

	protected ILaunchConfiguration config = null;

	protected IControlSystem controlSystem = null;
	protected IMonitoringSystem monitoringSystem = null;

	public boolean isParallelPerspectiveOpen() {
		return isPerspectiveOpen;
	}

	public void setPTPConfiguration(ILaunchConfiguration config) {
		this.config = config;
	}

	public ILaunchConfiguration getPTPConfiguration() {
		return config;
	}

	public ModelManager() {
		PTPCorePlugin.getDefault().addPerspectiveListener(perspectiveListener);
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		int MSChoiceID = preferences.getInt(PreferenceConstants.MONITORING_SYSTEM_SELECTION);
		String MSChoice = MonitoringSystemChoices.getMSNameByID(MSChoiceID);
		
		System.out.println("Your Monitoring System Choice: '"+MSChoice+"'");

		refreshMonitoringSystem(MSChoiceID);
	}
	
	public IControlSystem getControlSystem() {
		return controlSystem;
	}
	
	public IMonitoringSystem getMonitoringSystem() {
		return monitoringSystem;
	}
	
	public void refreshMonitoringSystem(int ID)
	{
		if(ID == MonitoringSystemChoices.SIMULATED_ID) {
			universe = new PUniverse();
			/* load up the control and monitoring systems for the simulation */
			monitoringSystem = new SimulationMonitoringSystem();
			controlSystem = new SimulationControlSystem();
			monitoringSystem.startup();
			controlSystem.startup();
			setupMS();
			fireEvent(null, EVENT_MONITORING_SYSTEM_CHANGE);
		}
		else if(ID == MonitoringSystemChoices.ORTE) {
			universe = new PUniverse();
			/* load up the control and monitoring systems for OMPI */
			OMPIJNIBroker jnibroker = new OMPIJNIBroker();
			monitoringSystem = new OMPIMonitoringSystem(jnibroker);
			controlSystem = new OMPIControlSystem(jnibroker);
			monitoringSystem.startup();
			controlSystem.startup();
			setupMS();
			fireEvent(null, EVENT_MONITORING_SYSTEM_CHANGE);
		}
		else {
			CoreUtils.showErrorDialog("Monitoring System Error", "Invalid monitoring system selected.  Set using the PTP preferences page.", null);
		}
	}
	
	/* setup the monitoring system */
	public void setupMS() 
	{
		String[] ne = monitoringSystem.getMachines();
		for (int i = 0; i < ne.length; i++) {
			PMachine mac;

			System.out.println("MACHINE: " + ne[i]);

			mac = new PMachine(universe, ne[i], ne[i].substring(new String(
					"machine").length()));

			universe.addChild(mac);

			String[] ne2 = monitoringSystem.getNodes(ne[i]);
			for (int j = 0; j < ne2.length; j++) {
				PNode node;
				System.out.println("\tNODE: "+j);
				node = new PNode(mac, ne2[j], "" + j + "", j);
				node.setAttrib(AttributeConstants.ATTRIB_NODE_USER, 
						monitoringSystem.getNodeAttribute(ne2[j],
						AttributeConstants.ATTRIB_NODE_USER));
				node.setAttrib(AttributeConstants.ATTRIB_NODE_GROUP, 
						monitoringSystem.getNodeAttribute(ne2[j],
						AttributeConstants.ATTRIB_NODE_GROUP));
				node.setAttrib(AttributeConstants.ATTRIB_NODE_STATE, 
						monitoringSystem.getNodeAttribute(ne2[j],
						AttributeConstants.ATTRIB_NODE_STATE));
				node.setAttrib(AttributeConstants.ATTRIB_NODE_MODE, 
						monitoringSystem.getNodeAttribute(ne2[j],
						AttributeConstants.ATTRIB_NODE_MODE));

				mac.addChild(node);
			}
		}
		ne = controlSystem.getJobs();
		if(ne != null) {
			for (int i = 0; i < ne.length; i++) {
				PJob job;

				System.out.println("JOB: " + ne[i]);

				int x = 0;
				try {
					x = (new Integer(ne[i].substring(3))).intValue();
				} catch (NumberFormatException e) {
				}
				job = new PJob(universe, ne[i], "" + (PJob.BASE_OFFSET + x) + "", x);
				universe.addChild(job);
				getProcsForNewJob(ne[i], job);
			}
		}

		monitoringSystem.addRuntimeListener(this);
		controlSystem.addRuntimeListener(this);
	}

	/* given a Job, this contacts the monitoring system and populates the runtime 
	 * model with the processes that correspond to that Job
	 */
	private void getProcsForNewJob(String nejob, IPJob job) {
		String[] ne = controlSystem.getProcesses(nejob);
		if (ne != null)
			System.out.println("getProcsForNewJob:" + nejob + " - #procs = "
					+ ne.length);
		for (int j = 0; ne != null && j < ne.length; j++) {
			IPProcess proc;
			// System.out.println("process name = "+ne[j]);
			
			String pids = controlSystem.getProcessAttribute(ne[j], AttributeConstants.ATTRIB_PROCESS_PID);
			int pid = -1;
			try {
				pid = (Integer.valueOf(pids)).intValue();
			} catch(Exception e) {
			}
			
			if (controlSystem instanceof SimulationControlSystem)
				proc = new SimProcess(job, ne[j], "" + j + "", "" + pid + "", j, "-1", "", "");
			else
				proc = new PProcess(job, ne[j], "" + j + "", "" + pid + "", j, "-1", "", "");
			
			job.addChild(proc);

			String pname = proc.getElementName();
			String nname = controlSystem.getProcessAttribute(pname, AttributeConstants.ATTRIB_PROCESS_NODE_NAME);
			String mname = monitoringSystem.getNodeMachineName(nname);
			// System.out.println("Process "+pname+" running on node:");
			// System.out.println("\t"+nname);
			// System.out.println("\tand that's running on machine: "+mname);
			IPMachine mac = universe.findMachineByName(mname);
			if (mac != null) {
				IPNode node = mac.findNodeByName(nname);
				if (node != null) {
					// System.out.println("**** THIS NODE IS WHERE THIS PROCESS
					// IS RUNNING!");
					/*
					 * this sets the data member in both classes stating that
					 * this process is running on this node and telling this
					 * node that it now has a child process running on it.
					 */
					proc.setNode(node);
				}
			}
			String status = controlSystem.getProcessAttribute(ne[j], AttributeConstants.ATTRIB_PROCESS_STATUS);
			proc.setStatus(status);
		}
	}

	private void refreshJobStatus(String nejob) {
		// System.out.println("refreshJobStatus("+nejob+")");
		IPJob job = universe.findJobByName(nejob);
		if (job != null) {
			IPProcess[] procs = job.getProcesses();
			if (procs != null) {
				for (int i = 0; i < procs.length; i++) {
					String procName = procs[i].getElementName();
					String status = controlSystem.getProcessAttribute(procName, AttributeConstants.ATTRIB_PROCESS_STATUS);
					// System.out.println("Status = "+status+" on process -
					// "+procName);
					procs[i].setStatus(status);
					String signal = controlSystem.getProcessAttribute(procName, AttributeConstants.ATTRIB_PROCESS_SIGNAL);
					String exitCode = controlSystem.getProcessAttribute(procName, AttributeConstants.ATTRIB_PROCESS_EXIT_CODE);
					if (!signal.equals(""))
						procs[i].setSignalName(signal);
					if (!exitCode.equals(""))
						procs[i].setExitCode(exitCode);
				}
			}
			fireEvent(job, EVENT_UPDATED_STATUS);
		}
	}

	public void runtimeNodeStatusChange(String ne) {
		/* so let's find which node this is */
		IPNode n = universe.findNodeByName(ne);
		if (n != null) {
			n.setAttrib("state", monitoringSystem.getNodeAttribute(ne, "state"));
			fireEvent(n, EVENT_SYS_STATUS_CHANGE);
		}
	}

	public void runtimeProcessOutput(String ne, String output) {
		IPProcess p = universe.findProcessByName(ne);
		if (p != null) {
			p.addOutput(output);
		}
	}

	public void runtimeJobExited(String ne) {
		refreshJobStatus(ne);
		IPJob job = universe.findJobByName(ne);

		fireEvent(job, EVENT_ALL_PROCESSES_STOPPED);
		fireState(STATE_STOPPED);
		clearUsedMemory();

		/*
		 * process.setExitCode(pdes[i].getExitCode());
		 * process.setSignalName(pdes[i].getSignalName());
		 * process.setStatus(pdes[i].getStatus()); fireEvent(process,
		 * EVENT_EXEC_STATUS_CHANGE); if (process.getParent().isAllStop()) {
		 * fireEvent(process.getParent(), ALL_PROCESSES_STOPPED); if
		 * (processRoot.isAllStop()) { fireState(STATE_STOPPED);
		 * clearUsedMemory(); } }
		 */
	}

	public void runtimeJobStateChanged(String ne) {
		refreshJobStatus(ne);
	}

	public void runtimeNewJob(String ne) {
		IPJob job = universe.findJobByName(ne);
		/* if it already existed, then destroy it first */
		if (job != null) {
			IPProcess[] procs = job.getProcesses();
			for (int i = 0; i < procs.length; i++) {
				IPNode node = procs[i].getNode();
				node.removeChild(procs[i]);
			}
			job.removeChildren();
			universe.removeChild(job);
		}

		PJob pjob;

		int x = 0;
		try {
			x = (new Integer(ne.substring(3))).intValue();
		} catch (NumberFormatException e) {
		}
		pjob = new PJob(universe, ne, "" + (PJob.BASE_OFFSET + x) + "", x);

		universe.addChild(pjob);
		getProcsForNewJob(ne, pjob);

		fireEvent(job, EVENT_UPDATED_STATUS);
	}

	private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {
		public void perspectiveOpened(IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
			if (perspective.getId().equals(CoreUtils.PPerspectiveFactory_ID)) {
				isPerspectiveOpen = true;
			}
		}

		public void perspectiveActivated(IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
			System.out.println("Active: " + perspective.getId());
			if (perspective.getId().equals(CoreUtils.PPerspectiveFactory_ID)) {
				isPerspectiveOpen = true;
				System.out.println("Active: " + perspective.getId());
			}
		}

		public void perspectiveChanged(IWorkbenchPage page,
				IPerspectiveDescriptor perspective, String changeId) {
			// if (perspective.getId().equals(CoreUtils.PPerspectiveFactory_ID))
			// System.out.println("Changed: " + perspective.getId());
		}
	};

	public void shutdown() {
		PTPCorePlugin.getDefault().removePerspectiveListener(perspectiveListener);
		perspectiveListener = null;
		listeners.clear();
		listeners = null;
		controlSystem.shutdown();
		monitoringSystem.shutdown();
	}

	public void addParallelLaunchListener(IParallelModelListener listener) {
		listeners.add(listener);
	}

	public void removeParallelLaunchListener(IParallelModelListener listener) {
		listeners.remove(listener);
	}

	protected synchronized void fireState(int state) {
		setCurrentState(state);
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			IParallelModelListener listener = (IParallelModelListener) i.next();
			switch (state) {
			case STATE_START:
				listener.start();
				System.out
						.println("++++++++++++ Started ++++++++++++++");
				break;
			case STATE_RUN:
				listener.run();
				break;
			case STATE_EXIT:
				System.out.println("++++++++++++ Exit ++++++++++++++");
				listener.exit();
				break;
			case STATE_ABORT:
				listener.abort();
				break;
			case STATE_STOPPED:
				System.out.println("++++++++++++ Stopped ++++++++++++++");
				listener.stopped();
			}
		}
	}

	protected synchronized void fireEvent(Object object, int event) {
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			IParallelModelListener listener = (IParallelModelListener) i.next();
			switch (event) {
			case EVENT_MONITORING_SYSTEM_CHANGE:
				listener.monitoringSystemChangeEvent(object);
				break;
			case EVENT_EXEC_STATUS_CHANGE:
				listener.execStatusChangeEvent(object);
				break;
			case EVENT_SYS_STATUS_CHANGE:
				listener.sysStatusChangeEvent(object);
				break;
			case EVENT_PROCESS_OUTPUT:
				listener.processOutputEvent(object);
				break;
			case EVENT_ERROR:
				listener.errorEvent(object);
				break;
			case EVENT_UPDATED_STATUS:
				listener.updatedStatusEvent();
				break;
			case EVENT_ALL_PROCESSES_STOPPED:
				listener.execStatusChangeEvent(object);
				break;
			}
		}
	}

	public int getCurrentState() {
		return currentState;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}

	protected String renderLabel(String name) {
		String format = CoreMessages
				.getResourceString("ModelManager.{0}_({1})");
		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[] { name, timestamp });
	}
	
	public synchronized void abortJob(String jobName) throws CoreException {
		/* we have a job name, so let's find it in the Universe - if it exists */
		IPJob j = getUniverse().findJobByName(jobName);
		if(j == null) {
			System.err.println("ERROR: tried to delete a job that was not found '"+jobName+"'");
			return;
		}
		controlSystem.terminateJob(j);
		System.out
				.println("***** NEED TO REFRESH JOB STATUS HERE in abortJob() of ModelManager ONCE WE KNOW THE JOBID!");
		// refreshJobStatus(nejob);
		fireState(STATE_ABORT);
	}

	//protected IPJob myjob = null;

	public IPJob run(final ILaunch launch, File workingDirectory,
			String[] envp, final JobRunConfiguration jobRunConfig, IProgressMonitor monitor)
			throws CoreException {
		/*
		 * PORT IProgressMonitor subMonitor = new SubProgressMonitor(monitor,
		 * 5); subMonitor.beginTask("Executing job", 10);
		 * subMonitor.subTask("Creating MPI session"); createMPISession();
		 * subMonitor.worked(2); subMonitor.subTask("Executing run command");
		 * mpirun(args); subMonitor.worked(2); subMonitor.subTask("Remove all
		 * processes"); processRoot.removeAllProcesses(); clearUsedMemory();
		 * subMonitor.worked(2);
		 * DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
		 * //DebugPlugin.newProcess(launch, session.getSessionProcess(),
		 * renderLabel("mpictrl")); subMonitor.subTask("Executing sys status
		 * command"); mpisysstatus(); subMonitor.worked(2);
		 * subMonitor.subTask("Executing status command"); mpistatus();
		 */

		/*
		 * what if I had already run a job? Better clean that up first! this is
		 * a hack - we should remove this one day
		 */
		/*
		if (myjob != null) {
			IPProcess[] procs = myjob.getProcesses();
			for (int i = 0; i < procs.length; i++) {
				IPNode node = procs[i].getNode();
				node.removeChild(procs[i]);
			}
			myjob.removeChildren();
			universe.removeChild(myjob);
			myjob = null;
		}
		*/

		String nejob = controlSystem.run(jobRunConfig);
		if (nejob != null) {
			PJob job;

			int x = 0;
			try {
				x = (new Integer(nejob.substring(3))).intValue();
			} catch (NumberFormatException e) {
			}
			job = new PJob(universe, nejob, "" + (PJob.BASE_OFFSET + x) + "", x);
			if(jobRunConfig.isDebug()) job.setDebug();

		//	myjob = job;
			universe.addChild(job);
			getProcsForNewJob(nejob, job);
			fireState(STATE_RUN);
			
			return job;
		}
		
		return null;
	}

	protected void clearUsedMemory() {
		System.out.println("********** clearUsedMemory");
		Runtime rt = Runtime.getRuntime();
		long isFree = rt.freeMemory();
		long wasFree;
		do {
			wasFree = isFree;
			rt.gc();
			isFree = rt.freeMemory();
		} while (isFree > wasFree);
		rt.runFinalization();
	}

	public IPUniverse getUniverse() {
		return universe;
	}
}