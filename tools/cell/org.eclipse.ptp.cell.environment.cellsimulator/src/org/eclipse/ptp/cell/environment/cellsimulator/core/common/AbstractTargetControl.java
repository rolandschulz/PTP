/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.environment.cellsimulator.core.common;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.simulator.SimulatorPlugin;
import org.eclipse.ptp.cell.simulator.core.ISimulatorControl;
import org.eclipse.ptp.cell.simulator.core.ISimulatorListener;
import org.eclipse.ptp.cell.simulator.core.ISimulatorParameters;
import org.eclipse.ptp.cell.simulator.core.IllegalConfigurationException;
import org.eclipse.ptp.cell.simulator.core.SimulatorException;
import org.eclipse.ptp.cell.simulator.core.SimulatorKilledException;
import org.eclipse.ptp.cell.simulator.core.SimulatorOperationException;
import org.eclipse.ptp.cell.simulator.core.SimulatorTerminatedException;
import org.eclipse.ptp.cell.ui.console.TerminalToConsoleBridge;
import org.eclipse.ptp.cell.ui.progress.ProgressQueue;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.environment.control.SSHTargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;


/**
 * Controls an instance of a target created from the Environment.
 * @author Daniel Felix Ferber
 * @since 1.2
 */
public abstract class AbstractTargetControl extends SSHTargetControl implements ITargetControl {

	protected static final int SIMULATOR_SHIFT = 1000;

	protected static final int SIMULATOR_EVENT = 4000;
	
	protected static final int DEPLOY_EVENT = 2000;

	protected static final int CONNECT_EVENT = 3000;
	
	/**
	 * Default cipher id for simulator
	 */
	public static final String DEFAULT_SIMULATOR_CIPHER = RemotetoolsPlugin.CIPHER_BLOWFISH;

	/**
	 * Console where linux terminal from simulated Linux is shown.
	 */
	protected IOConsole linuxConsole;

	/** 
	 * Connects the simulator control to the Linux console.
	 */
	protected TerminalToConsoleBridge linuxBridge;

	/**
	 * Console where the simulator stdout/stdin is shown.
	 */
	protected IOConsole tclConsole;

	/** 
	 * Connects the simulator control to the simulator stdout/stdin console.
	 */
	protected TerminalToConsoleBridge tclBridge;

	/**
	 * Configuration provided to create the target control.
	 */
	protected TargetConfig currentTargetConfig;
	
	/**
	 * Configuration provided to launch the simulator
	 */
	protected ISimulatorControl simulatorControl = SimulatorPlugin.getDefault().createSimulatorControl();
	protected StopResumeLogic stopResumeLogic;
	
	/**
	 * Name of the target.
	 */
	//protected String name;
	
	/**
	 * Back-Reference to the TargetElement
	 */
	protected ITargetElement targetElement;
	
	public AbstractTargetControl(ITargetElement element) throws CoreException {
		super();
		targetElement = element;
	}
	
	public String getName() {
		return targetElement.getName();
	}

	protected String getPluginId() {
		return CellSimulatorTargetPlugin.getDefault().getBundle().getSymbolicName();
	}
	
	protected void setCurrentTargetConfig(TargetConfig currentTargetConfig) {
		this.currentTargetConfig = currentTargetConfig;
	}
	
	protected TargetConfig getCurrentTargetConfig() {
		return currentTargetConfig;
	}
	
	// removed until proven to be useful
//	protected void setCurrentSimulatorParameters(
//			ISimulatorParameters currentSimulatorParameters) {
//		this.currentSimulatorParameters = currentSimulatorParameters;
//	}
//	
//	protected ISimulatorParameters getCurrentSimulatorParameters() {
//		return currentSimulatorParameters;
//	}
	
	protected void populateProgressQueueWithSimulatorLaunch(ProgressQueue queue) {
		queue.addWait(SIMULATOR_EVENT, Messages.AbstractTargetControl_PreparingSimulatorPlugin, 1);
		queue.addWait(DEPLOY_EVENT, Messages.AbstractTargetControl_PreparingToDeploy, 1);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.LAUNCH, Messages.AbstractTargetControl_StartingSimulator, 1);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.INIT_PARSE, Messages.AbstractTargetControl_ParsingLaunchScript, 1);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.INIT_CHECK, Messages.AbstractTargetControl_ValidatingParameters, 1);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.INIT_CONFIGURE, Messages.AbstractTargetControl_CreatingMachine, 1);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.INIT_BOGUSNET, Messages.AbstractTargetControl_StartingVirtualNetwork, 1);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.INIT_CONSOLE, Messages.AbstractTargetControl_ConnectingLinuxConsole, 1);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.INIT_CONFIGURED, Messages.AbstractTargetControl_SimulatorReadyToBoot, 1);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.BOOT_BIOS, Messages.AbstractTargetControl_BootingBIOS, 5);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.BOOT_LINUX, Messages.AbstractTargetControl_LoadingKernel, 10);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.BOOT_SYSTEM, Messages.AbstractTargetControl_LoadingOS, 30);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.BOOT_CONFIGURE, Messages.AbstractTargetControl_ConfiguringOS, 10);
	}
	
	protected void populateProgressQueueWithSimulatorShutdown(ProgressQueue queue) {
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.SHUTDOWN_START, Messages.AbstractTargetControl_KillingSystemProcesses, 1);
		queue.addWait(SIMULATOR_SHIFT + ISimulatorListener.SHUTDOWN_COMPLETE, Messages.AbstractTargetControl_CleaningUp, 1);
	}
	
	protected void populateProgressQueueWithConnection(ProgressQueue queue) {
		queue.addWait(CONNECT_EVENT, Messages.AbstractTargetControl_ConnectingThroughSSH, 1);		
	}

	protected class SimulatorLaunchListener implements ISimulatorListener {
		ProgressQueue queue;
		
		public SimulatorLaunchListener(ProgressQueue queue) {
			super();
			this.queue = queue;
		}

		public void lifecycleStateChanged(int state) {
			if (state == DEPLOYING) {
				queue.notifyOperationStarted(DEPLOY_EVENT);
			}
		}

		public void progressChanged(int progress) {
			queue.notifyOperationStarted(SIMULATOR_SHIFT + progress);
		}

		public void simulationStatus(int status) {
			// void
		}
		
		
	}
	
	protected void launchSimulator(ProgressQueue progressQueue, ISimulatorParameters parameters) throws CoreException {
		try {
			progressQueue.notifyOperationStarted(SIMULATOR_EVENT);
			/*
			 * Launch simulator controller. Blocks until the simulator is
			 * operational. Meanwhile, the simulator listener will update the
			 * progress monitor. If the launch fails, the launch method raises a
			 * SimulatorRuntimeException that describes the failure.
			 */
			SimulatorLaunchListener listener = new SimulatorLaunchListener(progressQueue);
			try {
				simulatorControl.addListener(listener);
				try {
					simulatorControl.launch(parameters);
				} catch (SimulatorOperationException e) {
					if (e.getReason() == SimulatorOperationException.EXISTING_SIMULATOR_INSTANCE) {
						class Asker implements Runnable {
							boolean yes_no = false;
							public void run() {
//								Display c = Display.getCurrent();
//								Shell shell = c.getActiveShell();
								yes_no = MessageDialog.openQuestion(null, 
										Messages.AbstractTargetControl_SimulatorRunningQuestion, 
										Messages.AbstractTargetControl_ProblemWithSimulator);
							}
						}
						Asker asker = new Asker();
						Display c = Display.getDefault();
						c.syncExec(asker);
						if (asker.yes_no) {
							// launch again
							simulatorControl.clear();
							simulatorControl.launch(parameters);
						} else {
							throw e;
						}
					} else {
						throw e;
					}
				}
			} catch (IllegalConfigurationException e) {
				IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin
						.getDefault().getBundle().getSymbolicName(), 0,
						NLS.bind(Messages.AbstractTargetControl_InvalidParameter, e.getMessage()), e);
				throw new CoreException(status);
			} catch (SimulatorTerminatedException e) {
				IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin
						.getDefault().getBundle().getSymbolicName(), 0,
						NLS.bind(Messages.AbstractTargetControl_ProblemLaunchingSimulator, e.getMessage()), e);
				throw new CoreException(status);
			} catch (SimulatorKilledException e) {
				IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin
						.getDefault().getBundle().getSymbolicName(), 0,
						NLS.bind(Messages.AbstractTargetControl_SimulatorCanceled, e.getMessage()), e);
				throw new CoreException(status);
			} catch (SimulatorException e) {
				IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin
						.getDefault().getBundle().getSymbolicName(), 0,
						NLS.bind(Messages.AbstractTargetControl_SimulatorLaunchFailed, e.getMessage()), e);
				throw new CoreException(status);
			} catch (Exception e) {
				IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin
						.getDefault().getBundle().getSymbolicName(), 0,
						NLS.bind(Messages.AbstractTargetControl_UnhandledException, e.getMessage()), e);
				throw new CoreException(status);
			} finally {
				simulatorControl.removeListener(listener);
			}
		} catch (CoreException e) {
			simulatorControl.kill();
			throw e;
		}
	}
	
	protected void releaseSimulator() {
		simulatorControl.kill();
	}
	
	protected class SimulatorshutdownListener implements ISimulatorListener {
		ProgressQueue queue;
		boolean finished = false;
		
		public SimulatorshutdownListener(ProgressQueue queue) {
			super();
			this.queue = queue;
		}

		public synchronized void lifecycleStateChanged(int state) {
			if (state == TERMINATED) {
				if (! finished){
					queue.notifyOperationCompleted(SIMULATOR_SHIFT + ISimulatorListener.SHUTDOWN_COMPLETE);
					finished = true;
				}
			}
		}

		public synchronized void progressChanged(int progress) {
			if (progress == SHUTDOWN_COMPLETE) {
				finished = true;
			}
			queue.notifyOperationStarted(SIMULATOR_SHIFT + progress);
		}

		public void simulationStatus(int status) {
			// void
		}
		
		
	}
	
	protected void kindlyReleaseSimulator(ProgressQueue progressQueue) throws CoreException {
		if (! simulatorControl.getStatus().isOperational()) {
			simulatorControl.kill();
		}
		
		ISimulatorListener listener = new SimulatorshutdownListener(progressQueue);
		try {
			simulatorControl.addListener(listener);
			simulatorControl.shutdown();
		} catch (SimulatorException e) {
			throw new CoreException(new Status(Status.ERROR, CellSimulatorTargetPlugin.getDefault().getBundle().getSymbolicName(), 0, Messages.AbstractTargetControl_SimulatorShutDownError, e));
		} finally {
			simulatorControl.removeListener(listener);
		}
	}
	
	// removed until proven to be useful
//	protected void connectToSimulator(ProgressQueue progressQueue, String username, String password, int timeout) throws CoreException {
//		/*
//		 *  Connect to the simulated Linux.
//		 */
//		try {
//			progressQueue.notifyOperationStarted(CONNECT_EVENT);
//			setConnectionParameters(new SSHParameters(
//					currentSimulatorParameters.getIpSimulator(),
//					currentTargetConfig.getLoginPort(), currentTargetConfig
//							.getLoginUserName(), currentTargetConfig
//							.getLoginPassword(), currentTargetConfig
//							.getLoginTimeout() * 1000)
//			);
//			super.create(progressQueue.getMonitor());
//			progressQueue.notifyOperationCompleted(CONNECT_EVENT);
//		} catch (CoreException e) {
//			/*
//			 * No special action required. Clean up is done by the
//			 * propagated catch.
//			 */
//			throw e;
//		}		
//	}
	
	protected synchronized void launchResumePauseStrategy(ProgressQueue progressQueue) throws CoreException {
		/*
		 * We will pause the simulator after a successful connection.
		 */
		try {
			simulatorControl.pause();
		} catch (SimulatorException e) {
			IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin
					.getDefault().getBundle().getSymbolicName(), 0,
					Messages.AbstractTargetControl_SimulatorPauseFailed, e);
			throw new CoreException(status);
		}
		
		stopResumeLogic = new StopResumeLogic(simulatorControl, this);
		stopResumeLogic.activate();
	}
	
	protected synchronized void releaseResumePauseStrategy() {
		if (stopResumeLogic != null) {
			stopResumeLogic.deactivate();
			stopResumeLogic = null;
		}
	}
	
//	protected void shutdownSimulator(ProgressQueue progressQueue) throws CoreException {
//		stopResumeLogic.deactivate();
//		stopResumeLogic = null;
//
//		super.kill(progressQueue.getMonitor());
//
//		/*
//		 * Simulator may have been terminated (eg. by cancel). Then no
//		 * graceful shutdown is required.
//		 */
//		if (simulatorControl.getStatus().isOperational()) {
//			try {
//				simulatorControl.shutdown();
//			} catch (SimulatorException e) {
//				// Ignore
//				simulatorControl.kill();
//			}
//		} else {
//			simulatorControl.kill();
//		}
//	}
	
	public boolean resume(IProgressMonitor monitor) throws CoreException {
		if (query() == ITargetStatus.RESUMED) {
			// Ignore
		} else if (query() == ITargetStatus.PAUSED) {
			try {
				simulatorControl.resume();
			} catch (SimulatorException e) {
				IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin.getDefault()
						.getBundle().getSymbolicName(), 0,
						Messages.AbstractTargetControl_SimulatorResumeFailed, e);
				throw new CoreException(status);
			}
		} else {
			IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin.getDefault()
					.getBundle().getSymbolicName(), 0,
					Messages.AbstractTargetControl_CannotResumeSimulator, null);
			throw new CoreException(status);			
		}
		return true;
	}

	public boolean stop(IProgressMonitor monitor) throws CoreException {
		if (query() == ITargetStatus.RESUMED) {
			try {
				simulatorControl.pause();
			} catch (SimulatorException e) {
				IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin.getDefault()
						.getBundle().getSymbolicName(), 0,
						Messages.AbstractTargetControl_SimulatorPauseFailed, e);
				throw new CoreException(status);
			}
		} else if (query() == ITargetStatus.PAUSED) {
			// Ignore
		} else {
			IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin.getDefault()
					.getBundle().getSymbolicName(), 0,
					Messages.AbstractTargetControl_CannotResumeSimulator, null);
			throw new CoreException(status);			
		}
		return true;
	}

	protected void resetConsoles() {
		if (linuxConsole != null) {
			linuxConsole.clearConsole();
		}
		if (tclConsole != null) {
			tclConsole.clearConsole();
		}
		if (currentTargetConfig.isConsoleShowLinux()) {
			if (linuxConsole != null) {
				IConsoleManager consoleManager = ConsolePlugin.getDefault()
						.getConsoleManager();
				consoleManager.showConsoleView(linuxConsole);
			}
		} else if (currentTargetConfig.isConsoleShowSimulator()) {
			if (tclConsole != null) {
				IConsoleManager consoleManager = ConsolePlugin.getDefault()
						.getConsoleManager();
				consoleManager.showConsoleView(tclConsole);
			}
		}
	}
	
	protected void updateConsoles() {
		IConsoleManager consoleManager = ConsolePlugin.getDefault()
				.getConsoleManager();

		/*
		 * The target configuration is only available after the simulator as
		 * been created for the first time. Although the attributes are
		 * available on the constructor call, the current config is created only
		 * after checking attributes on creating the simulator target..
		 */
		if (currentTargetConfig != null) {

			/*
			 * Add consoles if the are required by the configuration.
			 */
			if ((linuxConsole == null)
					&& currentTargetConfig.isConsoleShowLinux()) {
				linuxConsole = new IOConsole(Messages.AbstractTargetControl_LinuxConsoleMessage + getName(),
						null);
				linuxBridge = new TerminalToConsoleBridge(simulatorControl
						.getLinuxTerminal(), linuxConsole, Messages.AbstractTargetControl_LinuxConsole);
				consoleManager.addConsoles(new IConsole[] { linuxConsole });
			}

			if ((tclConsole == null)
					&& currentTargetConfig.isConsoleShowSimulator()) {
				tclConsole = new IOConsole(Messages.AbstractTargetControl_TCLConsoleMessage + getName(), null);
				tclBridge = new TerminalToConsoleBridge(simulatorControl
						.getProcessTerminal(), tclConsole, Messages.AbstractTargetControl_ProcessConsole);
				consoleManager.addConsoles(new IConsole[] { tclConsole });
			}

			/*
			 * Remove existing consoles if they are not required anymore.
			 */
			if ((linuxConsole != null)
					&& (!currentTargetConfig.isConsoleShowLinux())) {
				consoleManager.removeConsoles(new IConsole[] { linuxConsole });
				linuxBridge.disconnect();
				linuxBridge = null;
				linuxConsole = null;
			}
			if ((tclConsole != null)
					&& (!currentTargetConfig.isConsoleShowSimulator())) {
				consoleManager.removeConsoles(new IConsole[] { tclConsole });
				tclBridge.disconnect();
				tclBridge = null;
				tclConsole = null;
			}
		}
	}

	protected void connectSimulator(IProgressMonitor monitor, ProgressQueue progressQueue) throws CoreException {
		try{
			progressQueue.notifyOperationStarted(CONNECT_EVENT);

			disconnect();
			while (true) {
				if ( ! simulatorControl.getStatus().isOperational()) {
					disconnect();
					throw new CoreException(new Status(IStatus.ERROR, getPluginId(), 0, Messages.AbstractTargetControl_SimulatorExited, null));
				}
				if (progressQueue.isCancelled()) {
					throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.AbstractTargetControl_ConnectionCanceled, null));
				}
				try {
					connect();
					break;
				} catch (RemoteConnectionException e) {
					monitor.subTask(NLS.bind(Messages.AbstractTargetControl_Failed, e.getMessage()));
					/*
					 * Ignore failure, unfortunately, it is not possible to know the reason.
					 */
					disconnect();
				}
			
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.AbstractTargetControl_ConnectionCanceled, null));
				}
				monitor.subTask(Messages.AbstractTargetControl_TryingAgain);
			}
			progressQueue.notifyOperationCompleted(CONNECT_EVENT);

		} catch (CoreException e) {
			disconnect();
			throw e;
		} catch (Exception e) {
			disconnect();
			throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.AbstractTargetControl_ConnectionFailed, e));			
		}
	}

}
