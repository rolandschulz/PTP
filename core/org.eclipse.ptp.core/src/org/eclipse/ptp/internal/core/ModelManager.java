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
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.IParallelModelListener;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.internal.core.CoreUtils;
import org.eclipse.ptp.internal.core.CoreMessages;
import org.eclipse.ptp.preferences.MonitoringSystemChoices;
import org.eclipse.ptp.preferences.PTPPreferencesPage;
import org.eclipse.ptp.rtmodel.IRuntimeListener;
import org.eclipse.ptp.rtmodel.IRuntimeModel;
import org.eclipse.ptp.rtmodel.JobRunConfiguration;
import org.eclipse.ptp.rtmodel.NamedEntity;
import org.eclipse.ptp.rtmodel.ompi.OMPIRuntimeModel;
import org.eclipse.ptp.rtmodel.simulation.SimulationRuntimeModel;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.progress.IProgressService;

public class ModelManager implements IModelManager, IRuntimeListener {

	/*
	 * protected CommandFactory factory = new CommandFactory();
	 */

	protected List listeners = new ArrayList(2);

	protected int currentState = STATE_EXIT;

	// protected IPMachine machine = null;
	protected IPJob processRoot = null;

	protected IPUniverse universe = null;

	protected boolean isPerspectiveOpen = false;

	protected ILaunchConfiguration config = null;

	protected IRuntimeModel runtimeModel = null;

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
		int MSChoiceID = preferences.getInt(PTPPreferencesPage.MONITORING_SYSTEM_SELECTION);
		String MSChoice = MonitoringSystemChoices.getMSNameByID(MSChoiceID);
		
		System.out.println("Your Monitoring System Choice: '"+MSChoice+"'");

		refreshMonitoringSystem(MSChoiceID);
	}
	
	public void refreshMonitoringSystem(int ID)
	{
		if(ID == MonitoringSystemChoices.SIMULATED_ID) {
			execSimulatedMS();
			fireEvent(null, EVENT_MONITORING_SYSTEM_CHANGE);
		}
		else if(ID == MonitoringSystemChoices.ORTE) {
			execORTEMS();
			fireEvent(null, EVENT_MONITORING_SYSTEM_CHANGE);
		}
		else {
			System.out.println("No valid monitoring system selected, see the preferences page!");
		}
	}

	/* test out the dummy (simulation) RTM (runtime model) */
	public void execSimulatedMS()
	{
		System.out.println("Launch Manager: Testing function");

		universe = new PUniverse();

		runtimeModel = new SimulationRuntimeModel();
		String[] ne = runtimeModel.getMachines();
		for (int i = 0; i < ne.length; i++) {
			PMachine mac;

			System.out.println("MACHINE: " + ne[i]);

			mac = new PMachine(universe, ne[i], ne[i].substring(new String(
					"machine").length()));

			universe.addChild(mac);

			String[] ne2 = runtimeModel.getNodes(ne[i]);
			for (int j = 0; j < ne2.length; j++) {
				PNode node;
				node = new PNode(mac, ne2[j], "" + j + "");
				node.setAttrib("user", runtimeModel.getNodeAttribute(ne2[j],
						"user"));
				node.setAttrib("group", runtimeModel.getNodeAttribute(ne2[j],
						"group"));
				node.setAttrib("state", runtimeModel.getNodeAttribute(ne2[j],
						"state"));
				node.setAttrib("mode", runtimeModel.getNodeAttribute(ne2[j],
						"mode"));

				mac.addChild(node);
			}
		}
		ne = runtimeModel.getJobs();
		for (int i = 0; ne != null && i < ne.length; i++) {
			PJob job;

			System.out.println("JOB: " + ne[i]);

			int x = 0;
			try {
				x = (new Integer(ne[i].substring(3))).intValue();
			} catch (NumberFormatException e) {
			}
			job = new PJob(universe, ne[i], "" + (PJob.BASE_OFFSET + x) + "");
			universe.addChild(job);
			getProcsForNewJob(ne[i], job);

		}

		runtimeModel.addRuntimeListener(this);
	}

	/* test out the OMPI (Open MPI) RTM (runtime model) */
	public void execORTEMS() 
	{
		System.out.println("Model Manager - testing function: OMPI RTM");

		universe = new PUniverse();

		runtimeModel = new OMPIRuntimeModel();
		String[] ne = runtimeModel.getMachines();
		for (int i = 0; i < ne.length; i++) {
			PMachine mac;

			System.out.println("MACHINE: " + ne[i]);

			mac = new PMachine(universe, ne[i], ne[i].substring(new String(
					"machine").length()));

			universe.addChild(mac);

			String[] ne2 = runtimeModel.getNodes(ne[i]);
			for (int j = 0; j < ne2.length; j++) {
				PNode node;
				node = new PNode(mac, ne2[j], "" + j + "");
				node.setAttrib("user", runtimeModel.getNodeAttribute(ne2[j],
						"user"));
				node.setAttrib("group", runtimeModel.getNodeAttribute(ne2[j],
						"group"));
				node.setAttrib("state", runtimeModel.getNodeAttribute(ne2[j],
						"state"));
				node.setAttrib("mode", runtimeModel.getNodeAttribute(ne2[j],
						"mode"));

				mac.addChild(node);
			}
		}
		ne = runtimeModel.getJobs();
		for (int i = 0; i < ne.length; i++) {
			PJob job;

			System.out.println("JOB: " + ne[i]);

			int x = 0;
			try {
				x = (new Integer(ne[i].substring(3))).intValue();
			} catch (NumberFormatException e) {
			}
			job = new PJob(universe, ne[i], "" + (PJob.BASE_OFFSET + x) + "");
			universe.addChild(job);
			getProcsForNewJob(ne[i], job);

		}

		runtimeModel.addRuntimeListener(this);
	}

	private void getProcsForNewJob(String nejob, IPJob job) {
		String[] ne = runtimeModel.getProcesses(nejob);
		if (ne != null)
			System.out.println("getProcsForNewJob:" + nejob + " - #procs = "
					+ ne.length);
		for (int j = 0; ne != null && j < ne.length; j++) {
			PProcess proc;
			// System.out.println("process name = "+ne[j]);
			int pid = ((int) (Math.random() * 10000)) + 1000;
			proc = new PProcess(job, ne[j], "" + j + "", "" + pid + "", "-1",
					"", "");
			job.addChild(proc);

			String pname = proc.getElementName();
			String nname = runtimeModel.getProcessNodeName(pname);
			String mname = runtimeModel.getNodeMachineName(nname);
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
			String status = runtimeModel.getProcessStatus(ne[j]);
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
					String status = runtimeModel.getProcessStatus(procName);
					// System.out.println("Status = "+status+" on process -
					// "+procName);
					procs[i].setStatus(status);
					String signal = runtimeModel.getProcessSignal(procName);
					String exitCode = runtimeModel.getProcessExitCode(procName);
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
			n.setAttrib("state", runtimeModel.getNodeAttribute(ne, "state"));
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
		pjob = new PJob(universe, ne, "" + (PJob.BASE_OFFSET + x) + "");

		universe.addChild(pjob);
		getProcsForNewJob(ne, pjob);

		fireEvent(job, EVENT_UPDATED_STATUS);
	}

	private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {
		public void perspectiveClosed(IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
			if (perspective.getId().equals(CoreUtils.PPerspectiveFactory_ID)) {
				isPerspectiveOpen = false;
				// System.out.println("Close: " + perspective.getId());
				IWorkbench workbench = PTPCorePlugin.getDefault()
						.getWorkbench();
				IProgressService progressService = workbench
						.getProgressService();

				final IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException {
						if (!monitor.isCanceled()) {
							try {
								mpiexit();
							} catch (CoreException e) {
								throw new InvocationTargetException(e);
							}
						}
					}
				};
				try {
					progressService.busyCursorWhile(runnable);
				} catch (InterruptedException e) {
					System.out.println("Closing Parallel Perspective: "
							+ e.getMessage());
				} catch (InvocationTargetException e2) {
					System.out.println("Closing Parallel Perspective: "
							+ e2.getMessage());
				}
			}
		}

		public void perspectiveOpened(IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
			if (perspective.getId().equals(CoreUtils.PPerspectiveFactory_ID)) {
				isPerspectiveOpen = true;

				// System.out.println("Open: " + perspective.getId());
				/*
				 * if (page instanceof WorkbenchPage) { Perspective perspect =
				 * ((WorkbenchPage)page).findPerspective(perspective);
				 * IActionSetDescriptor[] actionSetDesciptors =
				 * perspect.getActionSets(); List visibleActionSets = new
				 * ArrayList(); for (int i=0; i <actionSetDesciptors.length;
				 * i++) { if
				 * (!actionSetDesciptors[i].getId().equals(NewSearchUI.ACTION_SET_ID))
				 * visibleActionSets.add(actionSetDesciptors[i]); }
				 * IActionSetDescriptor[] newActionSets = new
				 * IActionSetDescriptor[visibleActionSets.size()];
				 * visibleActionSets.toArray(newActionSets);
				 * perspect.setActionSets(newActionSets); }
				 */

				/*
				 * try { createMPISession(); } catch (CoreException e) {
				 * System.out.println("Cannot creation MPI session: " +
				 * e.getMessage()); }
				 */
			}
		}

		public void perspectiveActivated(IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
			System.out.println("Active: " + perspective.getId());
			if (perspective.getId().equals(CoreUtils.PPerspectiveFactory_ID)) {
				isPerspectiveOpen = true;
				System.out.println("Active: " + perspective.getId());
				try {
					createMPISession();
				} catch (CoreException e) {
					System.out.println("Cannot creation MPI session: "
							+ e.getMessage());
				}
			}
		}

		public void perspectiveChanged(IWorkbenchPage page,
				IPerspectiveDescriptor perspective, String changeId) {
			// if (perspective.getId().equals(CoreUtils.PPerspectiveFactory_ID))
			// System.out.println("Changed: " + perspective.getId());
		}
	};

	private Observer mpiObserver = new Observer() {
		public synchronized void update(Observable o, Object arg) {
			/*
			 * if (arg instanceof MIExecStatusChangeEvent) {
			 * MIExecStatusChangeEvent ev = (MIExecStatusChangeEvent) arg;
			 * MIProcessDescription[] pdes = ev.getMIProcessDescription(); for
			 * (int i = 0; i < pdes.length; i++) { IPProcess process =
			 * getProcessRoot().findProcess(pdes[i].getRank()); if (process !=
			 * null) { process.setExitCode(pdes[i].getExitCode());
			 * process.setSignalName(pdes[i].getSignalName());
			 * process.setStatus(pdes[i].getStatus()); fireEvent(process,
			 * EVENT_EXEC_STATUS_CHANGE); if (process.getParent().isAllStop()) {
			 * fireEvent(process.getParent(), ALL_PROCESSES_STOPPED); if
			 * (processRoot.isAllStop()) { fireState(STATE_STOPPED);
			 * clearUsedMemory(); } } break; }
			 * //System.out.println("================== exit code: " + //
			 * pd.getExitCode() + ", node: " + pd.getNode() + ", pid: " // +
			 * pd.getPid() + ", status: " + pd.getStatus() + ", rank: // " +
			 * pd.getRank()); } } else if (arg instanceof MIProcessOutputEvent) {
			 * MIProcessOutputEvent ev = (MIProcessOutputEvent) arg; IPProcess
			 * process =
			 * getProcessRoot().findProcess(String.valueOf(ev.getProcNumber()));
			 * if (process != null) { if
			 * (process.getStatus().equals(IPProcess.STARTING)) {
			 * process.setStatus(IPProcess.RUNNING); fireEvent(process,
			 * EVENT_EXEC_STATUS_CHANGE); } process.addOutput(ev.getOutput());
			 * //fireEvent(process, EVENT_PROCESS_OUTPUT); }
			 * //System.out.println("+++++++++++++++++++++ node: " + //
			 * ev.getProcNumber() + ", output: " + ev.getOutput()); } else if
			 * (arg instanceof MISysStatusChangeEvent) { try { mpisysstatus(); }
			 * catch (CoreException e) { System.out.println("+++++++ Observer -
			 * mpisysstatus err: " + e.getMessage()); } fireEvent(null,
			 * EVENT_SYS_STATUS_CHANGE); } else if (arg instanceof MIErrorEvent) {
			 * String err = ((MIErrorEvent) arg).getMessage();
			 * System.out.println("MIErrorEvent: " + err); /* try { mpiabort(); }
			 * catch (CoreException e) { } finally {
			 * setCurrentState(STATE_ERROR);
			 * //CoreUtils.showErrorDialog("MIErrorEvent", err, null); }
			 */
			/*
			 * fireEvent(err, EVENT_ERROR); }
			 */
		}
	};

	public void shutdown() {
		PTPCorePlugin.getDefault().removePerspectiveListener(
				perspectiveListener);
		clearAll();
		mpiObserver = null;
		perspectiveListener = null;
		listeners.clear();
		listeners = null;
		runtimeModel.shutdown();
	}

	public void clearAll() {
		/*
		 * if (session == null) return; removeConsole();
		 * session.deleteObservers(); session = null;
		 * processRoot.removeAllProcesses(); processRoot = null;
		 */
	}

	/*
	 * private void terminateDebugProcess() { if (debugProcess != null &&
	 * !debugProcess.isTerminated()) { try { debugProcess.terminate(); } catch
	 * (DebugException e) { System.out.println("LaunchManager -
	 * terminateDebugProcess: " + e.getMessage()); } finally { debugProcess =
	 * null; } } }
	 */

	/*
	 * public MISession getSession() { return session; }
	 */

	public IPJob getProcessRoot() {
		return processRoot;
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
						.println("++++++++++++ Started mpictrl ++++++++++++++");
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
				.getResourceString("LaunchManager.{0}_({1})");
		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[] { name, timestamp });
	}

	protected void isSessionExist() throws CoreException {
		/*
		 * if (session == null) { createMPISession(); //Status status = new
		 * Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(),
		 * IStatus.INFO, "No MI session is created", null); //throw new
		 * CoreException(status); }
		 */
	}

	public boolean isMPIRuning() {
		return true;
		/*
		 * return (session != null || (processRoot != null &&
		 * processRoot.hasChildren()));
		 */
	}

	public boolean hasProcessRunning() {
		if (isMPIRuning() && !processRoot.isAllStop())
			return true;

		return false;
	}

	public synchronized void mpirun(String[] args) throws CoreException {
		isSessionExist();

		/*
		 * if (getCurrentState() != STATE_ERROR && hasProcessRunning()) { Status
		 * status = new Status(IStatus.ERROR,
		 * PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, "Some processes
		 * are stilling running", null); throw new CoreException(status); }
		 */

		/*
		 * try { MIExecRun execRun = factory.createMIExecRun(args);
		 * session.postCommand(execRun); fireState(STATE_RUN); } catch
		 * (MIException e) { Status status = new Status(IStatus.ERROR,
		 * PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, e.getMessage(),
		 * e); throw new CoreException(status); }
		 */
	}

	public synchronized void mpisysstatus() throws CoreException {
		isSessionExist();

		/*
		 * try { MISysStatus sysStatus = new MISysStatus();
		 * session.postCommand(sysStatus);
		 * updateProcessInfo(sysStatus.getMISysStatusInfo().getMISystemDescription());
		 * fireEvent(null, EVENT_SYS_STATUS_CHANGE); } catch (MIException e) {
		 * Status status = new Status(IStatus.ERROR,
		 * PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, "Cannot exec SYS
		 * STATUS command", e); throw new CoreException(status); }
		 */
	}

	public synchronized void mpistatus() throws CoreException {
		isSessionExist();

		/*
		 * try { MIExecStatus execStatus = factory.createMIExecStatus();
		 * session.postCommand(execStatus);
		 * updateProcessInfo(execStatus.getMIExecStatusInfo().getMIProcessDescription());
		 * fireEvent(null, EVENT_UPDATED_STATUS); } catch (MIException e) {
		 * Status status = new Status(IStatus.ERROR,
		 * PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, "Cannot exec
		 * STATUS command", e); throw new CoreException(status); }
		 */
	}

	public synchronized void mpiabort() throws CoreException {
		isSessionExist();

		runtimeModel.abortJob("foo");
		System.out
				.println("***** NEED TO REFRESH JOB STATUS HERE in mpiabort() of ModelManager ONCE WE KNOW THE JOBID!");
		// refreshJobStatus(nejob);
		fireState(STATE_ABORT);
		/*
		 * try { MIExecAbort execAbort = factory.createMIExecAbort();
		 * session.postCommand(execAbort); fireState(STATE_ABORT); } catch
		 * (MIException e) { Status status = new Status(IStatus.ERROR,
		 * PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, "Cannot exec ABORT
		 * command", e); throw new CoreException(status); }
		 */
	}

	public synchronized void mpiexit() throws CoreException {
		isSessionExist();

		try {
			// MIExit gdbExit = factory.createMIExit();
			// session.postCommand(gdbExit);
			/*
			 * session.terminate();
			 */
			if (getCurrentState() != STATE_EXIT)
				fireState(STATE_EXIT);
		} catch (Exception e) {
			Status status = new Status(IStatus.ERROR, PTPCorePlugin
					.getUniqueIdentifier(), IStatus.INFO,
					"Cannot exec EXIT command", e);
			throw new CoreException(status);
		} finally {
			clearAll();
		}
	}

	public synchronized void createMPISession() throws CoreException {
		/*
		 * if (session == null) { MIPlugin miPlugin =
		 * MIPlugin.getDefault(PTPCorePlugin.getDefault().getPluginPreferences()); //
		 * Turn of the debugging output //miPlugin.setDebugging(true); try {
		 * session = miPlugin.createSession(); session.addObserver(mpiObserver);
		 * processRoot = new PMachine(session); fireState(STATE_START);
		 * createConsole(); waitFor(); } catch (MIException e) { Status status =
		 * new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(),
		 * IStatus.INFO,
		 * CoreMessages.getResourceString("LaunchManager.Exception_occurred_executing_command_line"),
		 * e); throw new CoreException(status); } catch (IOException e) { Status
		 * status = new Status(IStatus.ERROR,
		 * PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO,
		 * CoreMessages.getResourceString("LaunchManager.Exception_occurred_executing_command_line"),
		 * e); throw new CoreException(status); } }
		 */
	}

	protected IPJob myjob = null;

	public void execMI(final ILaunch launch, File workingDirectory,
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

		String nejob = runtimeModel.run(jobRunConfig);
		if (nejob != null) {
			PJob job;

			int x = 0;
			try {
				x = (new Integer(nejob.substring(3))).intValue();
			} catch (NumberFormatException e) {
			}
			job = new PJob(universe, nejob, "" + (PJob.BASE_OFFSET + x) + "");

			myjob = job;
			universe.addChild(job);
			getProcsForNewJob(nejob, job);
			fireState(STATE_RUN);
		}
	}

	private void updateProcessInfo(Object[] objects) {
		/*
		 * for (int i = 0; i < objects.length; i++) { if (objects instanceof
		 * MIProcessDescription[]) { MIProcessDescription[] pDesc =
		 * (MIProcessDescription[]) objects; IPNode pNode =
		 * processRoot.findNode(pDesc[i].getNode()); if (pNode == null) { pNode =
		 * new PNode(processRoot, pDesc[i].getNode()); pNode.addChild(new
		 * PProcess(pNode, pDesc[i].getRank(), pDesc[i].getPid(),
		 * pDesc[i].getStatus(), pDesc[i].getExitCode(),
		 * pDesc[i].getSignalName())); processRoot.addChild(pNode); } else {
		 * pNode.addChild(new PProcess(pNode, pDesc[i].getRank(),
		 * pDesc[i].getPid(), pDesc[i].getStatus(), pDesc[i].getExitCode(),
		 * pDesc[i].getSignalName())); } } else if (objects instanceof
		 * MISystemDescription[]) { MISystemDescription[] sDesc =
		 * (MISystemDescription[]) objects; IPNode pNode =
		 * processRoot.findNode(sDesc[i].getNode()); if (pNode == null) { pNode =
		 * new PNode(processRoot, sDesc[i].getNode(), sDesc[i].getBprocUser(),
		 * sDesc[i].getBprocGroup(), sDesc[i].getBprocState(),
		 * sDesc[i].getBprocMode()); processRoot.addChild(pNode); } else {
		 * pNode.setGroup(sDesc[i].getBprocGroup());
		 * pNode.setUser(sDesc[i].getBprocUser());
		 * pNode.setState(sDesc[i].getBprocState());
		 * pNode.setMode(sDesc[i].getBprocMode()); } } }
		 */
	}

	private void waitFor() {
		Thread waitForThread = new Thread("Wait for finish") {
			public void run() {
				/*
				 * try { if (!session.isTerminated())
				 * session.getGDBProcess().waitFor(); } catch
				 * (InterruptedException ie) { // clear interrupted state
				 * Thread.interrupted(); } finally {
				 */
				System.out.println("Launch Manager Exit");
				clearAll();
				// if (getCurrentState() != STATE_EXIT)
				// fireState(STATE_EXIT);
				/*
				 * }
				 */
			}
		};
		waitForThread.start();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.core.ILaunchManager#getMachine()
	 */
	public IPMachine getMachine() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.core.ILaunchManager#getUniverse()
	 */
	public IPUniverse getUniverse() {
		return universe;
	}
}