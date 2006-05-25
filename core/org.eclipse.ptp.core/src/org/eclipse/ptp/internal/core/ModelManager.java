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

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.ControlSystemChoices;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.IParallelModelListener;
import org.eclipse.ptp.core.MonitoringSystemChoices;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.IRuntimeProxy;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.ompi.OMPIControlSystem;
import org.eclipse.ptp.rtsystem.ompi.OMPIMonitoringSystem;
import org.eclipse.ptp.rtsystem.ompi.OMPIProxyRuntimeClient;
import org.eclipse.ptp.rtsystem.simulation.SimulationControlSystem;
import org.eclipse.ptp.rtsystem.simulation.SimulationMonitoringSystem;

public class ModelManager implements IModelManager, IRuntimeListener {
	protected List listeners = new ArrayList(2);

	protected int currentState = STATE_EXIT;

	// protected IPMachine machine = null;
	protected IPJob processRoot = null;

	protected IPUniverse universe = null;

	//protected boolean isPerspectiveOpen = false;

	protected ILaunchConfiguration config = null;

	protected IControlSystem controlSystem = null;
	protected IMonitoringSystem monitoringSystem = null;
	protected IRuntimeProxy runtimeProxy = null;

	private int currentControlSystem = -1;
	private int currentMonitoringSystem = -1;
	
	/*
	public boolean isParallelPerspectiveOpen() {
		return isPerspectiveOpen;
	}
	*/

	public void setPTPConfiguration(ILaunchConfiguration config) {
		this.config = config;
	}

	public ILaunchConfiguration getPTPConfiguration() {
		return config;
	}

	public ModelManager() {
		//PTPCorePlugin.getDefault().addPerspectiveListener(perspectiveListener);
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		int MSChoiceID = preferences.getInt(PreferenceConstants.MONITORING_SYSTEM_SELECTION);
		String MSChoice = MonitoringSystemChoices.getMSNameByID(MSChoiceID);
		int CSChoiceID = preferences.getInt(PreferenceConstants.CONTROL_SYSTEM_SELECTION);
		String CSChoice = ControlSystemChoices.getCSNameByID(CSChoiceID);

		System.out.println("Your Control System Choice: '"+CSChoice+"'");
		System.out.println("Your Monitoring System Choice: '"+MSChoice+"'");
		
		if(ControlSystemChoices.getCSArrayIndexByID(CSChoiceID) == -1 || MonitoringSystemChoices.getMSArrayIndexByID(MSChoiceID) == -1) {
			int MSI = MonitoringSystemChoices.ORTE;
			int CSI = ControlSystemChoices.ORTE;
			Preferences p = PTPCorePlugin.getDefault().getPluginPreferences();
			p.setValue(PreferenceConstants.MONITORING_SYSTEM_SELECTION, MSI);
			p.setValue(PreferenceConstants.CONTROL_SYSTEM_SELECTION, CSI);
			PTPCorePlugin.getDefault().savePluginPreferences();

			//PTPCorePlugin.errorDialog("Default Runtime System Set", "No previous (or invalid) control or monitoring system selected.\n\nDefault systems set to Open Runtime Environment (ORTE).  To change, use the Window->Preferences->PTP preferences page.", null);
			System.err.println("No previous (or invalid) control or monitoring system selected.\n\nDefault systems set to Open Runtime Environment (ORTE).  To change, use the Window->Preferences->PTP preferences page.");
			
			MSChoiceID = preferences.getInt(PreferenceConstants.MONITORING_SYSTEM_SELECTION);
			MSChoice = MonitoringSystemChoices.getMSNameByID(MSChoiceID);
			CSChoiceID = preferences.getInt(PreferenceConstants.CONTROL_SYSTEM_SELECTION);
			CSChoice = ControlSystemChoices.getCSNameByID(CSChoiceID);

			System.out.println("Your Control System Choice: '"+CSChoice+"'");
			System.out.println("Your Monitoring System Choice: '"+MSChoice+"'");
		}
		//refreshRuntimeSystems(CSChoiceID, MSChoiceID);
	}
	
	public IControlSystem getControlSystem() {
		return controlSystem;
	}
	
	public int getControlSystemID() { return currentControlSystem; }
	
	public IMonitoringSystem getMonitoringSystem() {
		return monitoringSystem;
	}
	
	public int getMonitoringSystemID() { return currentMonitoringSystem; }
	
	public void refreshRuntimeSystems(IProgressMonitor monitor, boolean force) throws CoreException {
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		int MSChoiceID = preferences.getInt(PreferenceConstants.MONITORING_SYSTEM_SELECTION);
		int CSChoiceID = preferences.getInt(PreferenceConstants.CONTROL_SYSTEM_SELECTION);
		int curMSID = getControlSystemID();
		int curCSID = getMonitoringSystemID();
		if(force || curMSID != MSChoiceID || curCSID != CSChoiceID) {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			refreshRuntimeSystems(CSChoiceID, MSChoiceID, monitor);
		}
	}
	
	public void refreshRuntimeSystems(int controlSystemID, int monitoringSystemID, IProgressMonitor monitor) throws CoreException {
		System.err.println("refreshRuntimeSystems");
		try {
			monitor.beginTask("Refreshing runtime system...", 200);
			/*
			 * Shutdown runtime if it is already active
			 */
			System.out.println("SHUTTING DOWN CONTROL/MONITORING/PROXY systems where appropriate");
			if (controlSystem != null) {
				monitor.subTask("Shutting down control system...");
				controlSystem.shutdown();
				controlSystem = null;
				monitor.worked(10);
			}
			if (monitoringSystem != null) {
				monitor.subTask("Shutting down monitor system...");
				monitoringSystem.shutdown();
				monitoringSystem = null;
				monitor.worked(10);
			}
			if (runtimeProxy != null) {
				monitor.subTask("Shutting down runtime proxy...");
				runtimeProxy.shutdown();
				runtimeProxy = null;
				monitor.worked(10);
			}
			if (monitor.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			monitor.worked(10);
	
			if(monitoringSystemID == MonitoringSystemChoices.SIMULATED && controlSystemID == ControlSystemChoices.SIMULATED) {
				/* load up the control and monitoring systems for the simulation */
				monitor.subTask("Starting simulation...");
				monitoringSystem = new SimulationMonitoringSystem();
				monitor.worked(10);
				controlSystem = new SimulationControlSystem();
				monitor.worked(10);
				runtimeProxy = null;
			}
			else if(monitoringSystemID == MonitoringSystemChoices.ORTE && controlSystemID == ControlSystemChoices.ORTE) {
				/* load up the control and monitoring systems for OMPI */
				monitor.subTask("Starting OMPI proxy runtime...");
				runtimeProxy = new OMPIProxyRuntimeClient(this);
				monitor.worked(10);
				
				if(!runtimeProxy.startup(monitor)) {
					System.err.println("Failed to start up the proxy runtime.");
					runtimeProxy = null;
					if (monitor.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
					//PTPCorePlugin.errorDialog("Failed to start OMPI proxy runtime",
					//	"There was an error starting the OMPI proxy runtime.  The path to 'ptp_orte_proxy' or 'orted' "+
					//	"may have been incorrect.  The 'orted' binary MUST be in your PATH to be found by 'ptp_orte_proxy'.  "+
					//	"Try checking the console log or error logs for more detailed information.\n\nDefaulting to "+
					//	"Simulation mode.  To change this, use the PTP preferences page.", null);
					
					/*
					int MSI = MonitoringSystemChoices.SIMULATED;
					int CSI = ControlSystemChoices.SIMULATED;
					Preferences p = PTPCorePlugin.getDefault().getPluginPreferences();
					p.setValue(PreferenceConstants.MONITORING_SYSTEM_SELECTION, MSI);
					p.setValue(PreferenceConstants.CONTROL_SYSTEM_SELECTION, CSI);
					PTPCorePlugin.getDefault().savePluginPreferences();
					if(!(monitoringSystem instanceof SimulationMonitoringSystem) || 
					   !(controlSystem instanceof SimulationControlSystem)) {
						refreshRuntimeSystems(ControlSystemChoices.SIMULATED, MonitoringSystemChoices.SIMULATED, monitor);
					}
					*/
					throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
							"There was an error starting the OMPI proxy runtime.  The path to 'ptp_orte_proxy' or 'orted' "+
							"may have been incorrect.  The 'orted' binary MUST be in your PATH to be found by 'ptp_orte_proxy'.  "+
							"Try checking the console log or error logs for more detailed information.",
							null));
				}
				monitor.subTask("Starting OMPI monitoring system...");
				monitoringSystem = new OMPIMonitoringSystem((OMPIProxyRuntimeClient)runtimeProxy);
				monitor.worked(10);
				monitor.subTask("Starting OMPI control system...");
				controlSystem = new OMPIControlSystem((OMPIProxyRuntimeClient)runtimeProxy);
				monitor.worked(10);
			}
			else {
				throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Invalid monitoring/control system selected.  Set using the PTP preferences page.", null));
			}
	
			universe = new PUniverse();
			monitor.subTask("Starting up monitor system...");
			monitoringSystem.startup();
			monitor.worked(10);
			monitor.subTask("Starting up control system...");
			controlSystem.startup();
			monitor.worked(10);
			try {
				monitor.subTask("Setup the monitoring system...");
				setupMS(new SubProgressMonitor(monitor, 150));
			} catch (CoreException e) {
				universe.removeChildren();
				throw e;
			}
	
			monitor.worked(10);
			fireEvent(null, EVENT_MONITORING_SYSTEM_CHANGE);
			currentControlSystem = controlSystemID;
			currentMonitoringSystem = monitoringSystemID;
		} finally {
			if (!monitor.isCanceled())
				monitor.done();
		}
	}

	/* setup the monitoring system */
	public void setupMS(IProgressMonitor monitor) throws CoreException {
		String[] ne = monitoringSystem.getMachines();		
		monitor.beginTask("", ne.length * 2);
		for (int i = 0; i < ne.length; i++) {
			monitor.setTaskName("Creating machines...");
			if (monitor.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}

			String ids = ne[i].substring(new String("machine").length());
			int machID = (new Integer(ids)).intValue();
			
			PMachine mac = new PMachine(universe, ne[i], machID);
			universe.addChild(mac);

			monitor.internalWorked(1);
			String[] ne2 = monitoringSystem.getNodes(mac);
			System.out.println("MACHINE: " + ne[i]+" - #nodes = "+ne2.length);

			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
			subMonitor.beginTask("", ne2.length);
			subMonitor.setTaskName("Creating nodes...");
			if(monitoringSystem instanceof OMPIMonitoringSystem) {
				int num_attribs = 5;
				String[] keys = new String[] {
					AttributeConstants.ATTRIB_NODE_NAME,
					AttributeConstants.ATTRIB_NODE_USER,
					AttributeConstants.ATTRIB_NODE_GROUP,
					AttributeConstants.ATTRIB_NODE_STATE,
					AttributeConstants.ATTRIB_NODE_MODE
				};
				String[] attribs = monitoringSystem.getAllNodesAttributes(mac, keys);
				if (attribs == null || attribs.length == 0) {
					return;
				}

				for (int j = 0; j < attribs.length; j++)
					System.out.println("*** attribs[" + j + "] = " + attribs[j]);

				for (int j = 0; j < ne2.length; j++) {
					if (subMonitor.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
					//node = new PNode(mac, ne2[j], "" + j + "", j);
					String nodename = ""+j+"";
					if(attribs.length > (j * num_attribs)) {
						nodename = attribs[(j * num_attribs)];
					}
					PNode node = new PNode(mac, ne2[j], "" + j + "", j);
					node.setAttribute(AttributeConstants.ATTRIB_NODE_NAME, nodename);
					System.out.println("NodeName According to ORTE = '"+node.getAttribute(AttributeConstants.ATTRIB_NODE_NAME)+"'");
					System.out.println("\t#attribs returned: "+attribs.length);
					
					if(attribs.length > (j * num_attribs) + 1) {
						node.setAttribute(AttributeConstants.ATTRIB_NODE_USER,
								attribs[(j * num_attribs) + 1]);
					}
					else {
						node.setAttribute(AttributeConstants.ATTRIB_NODE_USER, "UNKNOWN");
					}
					
					if(attribs.length > (j * num_attribs) + 2) {
						node.setAttribute(AttributeConstants.ATTRIB_NODE_GROUP,
								attribs[(j * num_attribs) + 2]);
					}
					else {
						node.setAttribute(AttributeConstants.ATTRIB_NODE_GROUP, "UNKNOWN");
					}
					
					if(attribs.length > (j * num_attribs) + 3) {
						node.setAttribute(AttributeConstants.ATTRIB_NODE_STATE,
							attribs[(j * num_attribs) + 3]);
					}
					else {
						node.setAttribute(AttributeConstants.ATTRIB_NODE_STATE, "up");
					}
					
					if(attribs.length > (j * num_attribs) + 4) {
						node.setAttribute(AttributeConstants.ATTRIB_NODE_MODE,
							attribs[(j * num_attribs) + 4]);
					}
					else {
						node.setAttribute(AttributeConstants.ATTRIB_NODE_MODE, "73");
					}					
					mac.addChild(node);
					subMonitor.worked(1);
				}
			}
			else if (monitoringSystem instanceof SimulationMonitoringSystem) {
				for(int j=0; j<ne2.length; j++) {
					if (subMonitor.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
					//System.out.println("node "+j);
					PNode node = new PNode(mac, ne2[j], ""+j+"", j);
					node.setAttribute(AttributeConstants.ATTRIB_NODE_NAME, 
							monitoringSystem.getNodeAttributes(node, new String[] {AttributeConstants.ATTRIB_NODE_NAME})[0]);
					node.setAttribute(AttributeConstants.ATTRIB_NODE_USER, 
							monitoringSystem.getNodeAttributes(node, new String[] {AttributeConstants.ATTRIB_NODE_USER})[0]);
					node.setAttribute(AttributeConstants.ATTRIB_NODE_GROUP, 
							monitoringSystem.getNodeAttributes(node, new String[] {AttributeConstants.ATTRIB_NODE_GROUP})[0]);
					node.setAttribute(AttributeConstants.ATTRIB_NODE_STATE, 
							monitoringSystem.getNodeAttributes(node, new String[] {AttributeConstants.ATTRIB_NODE_STATE})[0]);
					node.setAttribute(AttributeConstants.ATTRIB_NODE_MODE, 
							monitoringSystem.getNodeAttributes(node, new String[] {AttributeConstants.ATTRIB_NODE_MODE})[0]);
					
					mac.addChild(node);
				}
			}
		}
		ne = controlSystem.getJobs();
		if(ne != null) {
			for (int i = 0; i < ne.length; i++) {
				monitor.setTaskName("Creating jobs...");
				if (monitor.isCanceled()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}

				System.out.println("JOB: " + ne[i]);
				int x = 0;
				try {
					x = (new Integer(ne[i].substring(3))).intValue();
				} catch (NumberFormatException e) {
				}
				PJob job = new PJob(universe, ne[i], "" + (PJob.BASE_OFFSET + x) + "", x);
				universe.addChild(job);
				
				String[] ne2 = controlSystem.getProcesses(job);
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				subMonitor.beginTask("", ne2.length);
				subMonitor.setTaskName("Creating processes...");
				for (int j = 0; j < ne2.length; j++) {		
					if (subMonitor.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
					IPProcess proc = new PProcess(job, ne[i]+"_process"+j, "" + j + "", "0", j, IPProcess.STARTING, "", "");
					job.addChild(proc);
					subMonitor.worked(1);
				}	
				try {
					getProcsStatusForNewJob(ne[i], job, new SubProgressMonitor(monitor, ne2.length));
				} catch (CoreException e) {
					universe.deleteJob(job);
					return;
				}
			}
		}
		monitor.worked(1);
		monitoringSystem.addRuntimeListener(this);
		controlSystem.addRuntimeListener(this);		
		monitor.done();
	}

	/* given a Job, this contacts the monitoring system and populates the runtime 
	 * model with the processes that correspond to that Job
	 */
	private void getProcsStatusForNewJob(String nejob, IPJob job, IProgressMonitor monitor) throws CoreException {
		//String[] ne = controlSystem.getProcesses(job);
		//IPProcess procs[] = job.getSortedProcesses();
		//if(procs == null) return;
		int numProcs = job.totalProcesses();
		
		if(numProcs <= 0) return;
		
		System.out.println("getProcsStatusForNewJob:" + nejob + " - #procs = " + numProcs);
		
		int num_attribs = 2;
		String[] keys = new String[] {
			AttributeConstants.ATTRIB_PROCESS_PID,
			AttributeConstants.ATTRIB_PROCESS_NODE_NAME
		};
		String[] attribs = controlSystem.getAllProcessesAttributes(job, keys);
		for(int i=0; i<attribs.length; i++) 
			System.out.println("*** attribs["+i+"] = "+attribs[i]);
		
		monitor.beginTask("", numProcs);
		monitor.setTaskName("Initialing the processes...");
		for (int i = 0; i < numProcs; i++) {
			if (monitor.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			
			IPProcess proc = job.findProcessByName(nejob+"_process"+i);
			if(proc == null) {
				System.err.println("*** ERROR: Unable to find process #"+i+" on job "+nejob);
				continue;
			}
			
			//String[] vals = controlSystem.getProcessAttributes(job, proc, AttributeConstants.ATTRIB_PROCESS_PID + " " + AttributeConstants.ATTRIB_PROCESS_NODE_NAME);	
			proc.setPid(attribs[i * num_attribs]);
			String nname = attribs[(i * num_attribs) + 1];
			if(nname.equals("localhost")) nname = "0";

			/* this is a hack until I get this working correctly w/ the monitoring system! */
			nname = new String("machine0_node"+nname);
			
			//String nname = controlSystem.getProcessAttribute(job, proc, AttributeConstants.ATTRIB_PROCESS_NODE_NAME);
			//String mname = monitoringSystem.getNodeMachineName(nname);
			// System.out.println("Process "+pname+" running on node:");
			// System.out.println("\t"+nname);
			// System.out.println("\tand that's running on machine: "+mname);
			IPNode node = universe.findNodeByName(nname);
			if (node == null) {
				node = universe.findNodeByHostname(attribs[(i * num_attribs) + 1]);
				if (node == null) {
					throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "No available node found.", null));
				}
			}
				// System.out.println("**** THIS NODE IS WHERE THIS PROCESS IS RUNNING!");
				/*
				 * this sets the data member in both classes stating that
				 * this process is running on this node and telling this
				 * node that it now has a child process running on it.
				 */
			proc.setNode(node);
			//String status = controlSystem.getProcessAttribute(job, proc, AttributeConstants.ATTRIB_PROCESS_STATUS);
			//proc.setStatus(status);
			monitor.worked(1);
		}
		if (monitor.isCanceled()) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		monitor.done();
	}

	private void refreshJobStatus(String nejob) {
		// System.out.println("refreshJobStatus("+nejob+")");
		System.err.println("TODO: refreshJobStatus("+nejob+") called - finish this.");
		/*
		IPJob job = universe.findJobByName(nejob);
		if (job != null) {
			IPProcess[] procs = job.getProcesses();
			if (procs != null) {
				for (int i = 0; i < procs.length; i++) {
					String status = controlSystem.getProcessAttribute(job, procs[i], AttributeConstants.ATTRIB_PROCESS_STATUS);
					// System.out.println("Status = "+status+" on process -
					// "+procName);
					procs[i].setStatus(status);
					String signal = controlSystem.getProcessAttribute(job, procs[i], AttributeConstants.ATTRIB_PROCESS_SIGNAL);
					String exitCode = controlSystem.getProcessAttribute(job, procs[i], AttributeConstants.ATTRIB_PROCESS_EXIT_CODE);
					if (!signal.equals(""))
						procs[i].setSignalName(signal);
					if (!exitCode.equals(""))
						procs[i].setExitCode(exitCode);
				}
			}
			fireEvent(job, EVENT_UPDATED_STATUS);
		}
		*/
	}

	public void runtimeNodeGeneralChange(String ne, String key, String value) {
		System.out.println("ModelManager.runtimeNodeGeneralName - node '"+ne+", key='"+key+"', value='"+value+"'");
		IPNode n = universe.findNodeByName(ne);
		if(n != null) {
			System.out.print("\t before, val = "+n.getAttribute(key));
			n.setAttribute(key, value);
			System.out.println("\t after, val = "+n.getAttribute(key));
			fireEvent(n, EVENT_SYS_STATUS_CHANGE);
		}
	}
	
	public void runtimeNodeStatusChange(String ne) {
		/* so let's find which node this is */
		IPNode n = universe.findNodeByName(ne);
		if (n != null) {
			try {
				n.setAttribute(AttributeConstants.ATTRIB_NODE_STATE, monitoringSystem.getNodeAttributes(n, new String[] {AttributeConstants.ATTRIB_NODE_STATE}));
				fireEvent(n, EVENT_SYS_STATUS_CHANGE);
			} catch(CoreException e) {
				PTPCorePlugin.errorDialog("Fatal PTP Monitoring System Error",
					"The PTP Monitoring System is down.", null);
				return;
			}
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
		fireState(STATE_STOPPED, null);
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

	public void runtimeJobStateChanged(String nejob, String state) {
		System.out.println("*********** JOB STATE CHANGE: "+state+" (job = "+nejob+")");
		IPJob job = universe.findJobByName(nejob);
		if (job != null) {
			IPProcess[] procs = job.getProcesses();
			if (procs != null) {
				for (int i = 0; i < procs.length; i++) {
					procs[i].setStatus(state);
				}
			}
			fireEvent(job, EVENT_UPDATED_STATUS);
		}
		//refreshJobStatus(ne);
	}
	
	public void runtimeNewJob(String ne) {
		if(!controlSystem.isHealthy()) {
			PTPCorePlugin.errorDialog("Fatal PTP Control System Error",
				"The PTP Control System is down.", null);
			return;
		}
		
		IProgressMonitor monitor = new NullProgressMonitor();		
		
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
		
		String[] ne2;
		
		try {
			ne2 = controlSystem.getProcesses(pjob);
		} catch(CoreException e) {
			PTPCorePlugin.errorDialog("Fatal PTP Control System Error",
					"The PTP Control System is down.", null);
			return;
		}
		
		for (int j = 0; j < ne2.length; j++) {		
			IPProcess proc;
			
			proc = new PProcess(pjob, ne+"_process"+j, "" + j + "", "0", j, IPProcess.STARTING, "", "");
			pjob.addChild(proc);
		}	
		
		try {
			getProcsStatusForNewJob(ne, pjob, new SubProgressMonitor(monitor, ne2.length));
		} catch (CoreException e) {
			universe.deleteJob(job);
			return;
		}
		fireEvent(job, EVENT_UPDATED_STATUS);
	}

	/*
	private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {
		public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			if (perspective.getId().equals(CoreUtils.PPerspectiveFactory_ID)) {
				isPerspectiveOpen = true;
			}
		}
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			System.out.println("PERSPECTIVE: Active: " + perspective.getId());
			if (perspective.getId().equals(CoreUtils.PPerspectiveFactory_ID)) {
				isPerspectiveOpen = true;
				System.out.println("MYPERSPECTIVE: Active: " + perspective.getId());
			}
		}
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
			if (perspective.getId().equals(CoreUtils.PPerspectiveFactory_ID))
				System.out.println("PERSPECTIVE: Changed: " + perspective.getId());
		}
	};
	*/

	public void shutdown() {
		//PTPCorePlugin.getDefault().removePerspectiveListener(perspectiveListener);
		//perspectiveListener = null;
		listeners.clear();
		listeners = null;
		if(monitoringSystem != null)
			monitoringSystem.shutdown();
		if(controlSystem != null)
			controlSystem.shutdown();
		if (runtimeProxy != null)
			runtimeProxy.shutdown();
	}

	public void addParallelLaunchListener(IParallelModelListener listener) {
		listeners.add(listener);
	}

	public void removeParallelLaunchListener(IParallelModelListener listener) {
		listeners.remove(listener);
	}

	protected synchronized void fireState(int state, String arg) {
		setCurrentState(state);
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			IParallelModelListener listener = (IParallelModelListener) i.next();
			switch (state) {
			case STATE_START:
				listener.start();
				System.out.println("++++++++++++ Started ++++++++++++++");
				break;
			case STATE_RUN:
				listener.run(arg);
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
		try {
			controlSystem.terminateJob(j);
		} catch(CoreException e) {
			PTPCorePlugin.errorDialog("Fatal PTP Control System Error",
					"The PTP Control System is down.", null);
			return;
		}
		System.out
				.println("***** NEED TO REFRESH JOB STATUS HERE in abortJob() of ModelManager ONCE WE KNOW THE JOBID!");
		refreshJobStatus(jobName);
		fireState(STATE_ABORT, null);
	}

	//protected IPJob myjob = null;

	public IPJob run(final ILaunch launch, final JobRunConfiguration jobRunConfig, IProgressMonitor monitor) throws CoreException {
		monitor.setTaskName("Creating the job...");
		int jobID = controlSystem.run(jobRunConfig);
		if (jobID < 0)
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "cannot create a job.", null));
		
		System.out.println("ModelManager.run() - new JobID = "+jobID);
		return newJob(jobID, jobRunConfig.getNumberOfProcesses(), jobRunConfig.isDebug(), monitor);
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

	private IPJob newJob(int jobID, int numProcesses, boolean debug, IProgressMonitor monitor) throws CoreException {
		String jobName = "job"+jobID;

		System.out.println("MODEL MANAGER: newJob("+jobID+")");
		
		PJob job = new PJob(universe, jobName, "" + (PJob.BASE_OFFSET + jobID) + "", jobID);		
		if (debug)
			job.setDebug();
		
		universe.addChild(job);
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		monitor.beginTask("", numProcesses);
		monitor.setTaskName("Creating processes....");
		/* we know that we succeeded, so we can create this many procs in the job.  we just
		 * need to run getProcsStatusForNewJob() to fill in the status later
		 */
		for (int i = 0; i < numProcesses; i++) {		
			// System.out.println("process name = "+ne[j]);
			IPProcess proc = new PProcess(job, jobName+"_process"+i, "" + i + "", "0", i, IPProcess.STARTING, "", "");
			job.addChild(proc);			
		}	
		
		try {
			monitor.internalWorked(0);
			monitor.subTask("Setting process status...");
			getProcsStatusForNewJob(jobName, job, new SubProgressMonitor(monitor, numProcesses));
		} catch (CoreException e) {
			controlSystem.terminateJob(job);
			universe.deleteJob(job);
			throw e;
		}
		
		fireState(STATE_RUN, jobName);
		return job;
	}
}