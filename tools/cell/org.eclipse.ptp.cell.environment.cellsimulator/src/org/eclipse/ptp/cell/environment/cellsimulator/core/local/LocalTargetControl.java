/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.cellsimulator.core.local;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.AbstractTargetControl;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.TargetConfig;
import org.eclipse.ptp.cell.ui.progress.ICancelCallback;
import org.eclipse.ptp.cell.ui.progress.ProgressQueue;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.extension.ITargetVariables;

public class LocalTargetControl extends AbstractTargetControl implements ITargetVariables {

	LocalConfigFactory factory;
	private LocalConfigurationBean configurationBean;
	
	public LocalTargetControl(ITargetElement element) throws CoreException {
		super(element);
		
		configurationBean = new LocalConfigurationBean(targetElement.getAttributes(), targetElement.getId());
		factory = new LocalConfigFactory(configurationBean, element.getId());
		setCurrentTargetConfig(factory.createTargetConfig());
		
		/*
		 * Guarantee to show the consoles according to preferences.
		 */
		updateConsoles();
	}
	
	protected class LaunchCancelCallback implements ICancelCallback {
		LocalTargetControl control;
		
		public LaunchCancelCallback(LocalTargetControl control) {
			super();
			this.control = control;
		}

		public void cancel(boolean byUser) {
//			synchronized (this.control) {
				releaseSimulator();
//			}
		}
	}
	
	public boolean create(IProgressMonitor monitor) throws CoreException {

		/*
		 * Create target control configuration and simulator configuration.
		 */
		setCurrentTargetConfig(factory.createTargetConfig());
		LocalSimulatorConfiguration currentSimulatorParameters = factory.createLocalSimulatorParameters();
		
		/*
		 * Guarantee to show the consoles according to preferences.
		 * Also clear the consoles and force to show the most relevant one.
		 */		
		updateConsoles();
		resetConsoles();
		
		/*
		 * Create the progress queue.
		 */
		ProgressQueue progressQueue = new ProgressQueue(monitor);
		progressQueue.setCancelMessage(Messages.LocalTargetControl_LocalCellSimulatorLaunchCancelation);
		progressQueue.setInterruptMessage(Messages.LocalTargetControl_LocalCellSimulatorLaunchInterruption);
		progressQueue.setTaskName(Messages.LocalTargetControl_LaunchLocalCellSimulator);
		progressQueue.addWait(-1, Messages.LocalTargetControl_Preparing, 0);
		populateProgressQueueWithSimulatorLaunch(progressQueue);
		populateProgressQueueWithConnection(progressQueue);
		progressQueue.addWait(-2, Messages.LocalTargetControl_LaunchCompleted, 0);
		progressQueue.setCancelCallBack(new LaunchCancelCallback(this));
		progressQueue.start();
		
		/*
		 * Fill automatic parameters, if necessary.
		 */
		String targetID = factory.getTargetID();
		LocalLaunchAutomaticAttributeGenerator generator = LocalLaunchAutomaticAttributeGenerator.getAutomaticAttributeGenerator();
		if (currentTargetConfig.getDoAutomaticNetworkConfiguration()) {
			String ipSimulator = generator.getSimulatorAddress(targetID);
			String ipHost = generator.getHostAddress(targetID);
			String macSimulator = generator.getMacAddress(targetID);
			factory.completeSimulatorParametersWithNetworkConfig(currentSimulatorParameters, ipSimulator, macSimulator, ipHost);
		} else {
			/*
			 * Nothing to do, since fields were already filled with values that
			 * were specified manually by the user.
			 */
		}
		
		if (currentTargetConfig.getDoAutomaticPortConfiguration()) {
			int javaApiPort = generator.getJavaAPIPort(targetID);
			int consolePort = generator.getConsolePort(targetID);
			factory.completeSimulatorParametersWithConsoleConfig(currentSimulatorParameters, consolePort);
			factory.completeSimulatorParametersWithJavaApiConfig(currentSimulatorParameters, javaApiPort);			
		} else {
			factory.completeSimulatorParametersWithConsoleConfig(currentSimulatorParameters);
			factory.completeSimulatorParametersWithJavaApiConfig(currentSimulatorParameters);
		}
//		System.out.println(currentSimulatorParameters);
		
		if (progressQueue.isCancelled()) {
			throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.LocalTargetControl_LocalCellSimulatorLaunchCancelled, null));
		}
		
		try {
			/*
			 * Launch simulator.
			 */
			launchSimulator(progressQueue, currentSimulatorParameters);
			
			
			/*
			 * Connect SSH to the simulator.
			 */
			TargetConfig currentTargetConfig = getCurrentTargetConfig();
			SSHParameters parameters = new SSHParameters(
					currentSimulatorParameters.getIpSimulator(),
					currentTargetConfig.getLoginPort(), 
					currentTargetConfig.getLoginUserName(), 
					currentTargetConfig.getLoginPassword(), 
					currentTargetConfig.getSimulatorCipherType(),
					currentTargetConfig.getLoginTimeout() * 1000
					);
			setConnectionParameters(parameters);
			
			connectSimulator(monitor, progressQueue);
			
			/*
			 * Activate the pause/resume logic, with the simulator paused at first.
			 */
			launchResumePauseStrategy(progressQueue);
			
			progressQueue.finish();
			return true;
		} catch (Exception e) {
			/*
			 * Guarantee that all operations are undone.
			 */
 			progressQueue.interrupt();
			releaseResumePauseStrategy();
			disconnect();
			releaseSimulator();
			if (e instanceof CoreException) {
				throw (CoreException)e;
			} else {
				throw new CoreException(new Status(IStatus.ERROR, getPluginId(), 0, NLS.bind(Messages.LocalTargetControl_UnexpectedErrorLaunchingSimulator, e), e));
			}
		} finally {
			/*
			 * In any case, we need to close the progress monitor.
			 */
			monitor.done();
		}
	}
	
	protected class ShutdownCancelCallback implements ICancelCallback {
		LocalTargetControl control;
		
		public ShutdownCancelCallback(LocalTargetControl control) {
			super();
			this.control = control;
		}

		public void cancel(boolean byUser) {
//			synchronized (this.control) { 
//				releaseResumePauseStrategy();
//				disconnect();
				releaseSimulator();
			}
//		}
	}

	public boolean kill(IProgressMonitor monitor) throws CoreException {

		if (query() == ITargetStatus.STOPPED) {
			throw new CoreException(new Status(IStatus.ERROR,
					CellSimulatorTargetPlugin.PLUGIN_ID, 1,
					Messages.LocalTargetControl_SimulatorNotLaunched,
					null));
		}

		releaseResumePauseStrategy();
		
		/*
		 * Kill simulator if it is not running nor paused. No jobs are expected
		 * to be running in this state.
		 */
		if (!simulatorControl.getStatus().isOperational()) {
			disconnect();
			simulatorControl.kill();
			return true;
		}

		/*
		 * Create the progress queue.
		 */
		ProgressQueue progressQueue = new ProgressQueue(monitor);
		progressQueue.setCancelMessage(Messages.LocalTargetControl_LocalCellSimulatorShutdownCancelation);
		progressQueue.setInterruptMessage(Messages.LocalTargetControl_LocalCellSimulatorForceShutdown);
		progressQueue.setTaskName(Messages.LocalTargetControl_LocalCellSimulatorShutdown);
		progressQueue.addWait(-1, Messages.LocalTargetControl_ShuttingDown, 0);
		populateProgressQueueWithSimulatorShutdown(progressQueue);
		progressQueue.addWait(-2, Messages.LocalTargetControl_ShutDownCompleted, 1);
		progressQueue.addWait(-3, Messages.LocalTargetControl_CleaningUp, 1);
		progressQueue.setCancelCallBack(new ShutdownCancelCallback(this));
		progressQueue.start();

		try {
			/*
			 * Try to gracefully terminate all running jobs. Might this fail, then guarantee to disconnect.
			 */
			try {
				simulatorControl.resume();
				terminateJobs(monitor);
			} finally {
				disconnect();
			}
	
			/*
			 * If possible, use a kind shutdown by halting the simulated host.
			 */
			kindlyReleaseSimulator(progressQueue);
			
			progressQueue.finish();
		} catch (Exception e) {
			/*
			 * Guarantee that all operations are undone.
			 */
			progressQueue.interrupt();
			releaseResumePauseStrategy();
			disconnect();
			releaseSimulator();
		} finally {
			monitor.done();
		}

		return true;
	}

public synchronized int query() {
		if (simulatorControl.getStatus().isLaunching()) {
			return ITargetStatus.STARTED;
		} else if (simulatorControl.getStatus().isShuttingDown()) {
			return ITargetStatus.STARTED;
		} else if (simulatorControl.getStatus().isNotRunning()) {
			return ITargetStatus.STOPPED;
		} else if (simulatorControl.getStatus().isOperational()) {
			if (! isConnected()) {
				return ITargetStatus.STARTED;				
			} else {
				if (simulatorControl.getStatus().isPaused()) {
					return ITargetStatus.PAUSED;
				} else {
					return ITargetStatus.RESUMED;
				}
			}
		} else  {
			return ITargetStatus.STOPPED;
		}
	}
	
	public void updateConfiguration()
			throws CoreException {
	
		/*
		 * Replace only target configuration, but not simulator configuration,
		 * since the simulator may be running. Simulator config is verified only
		 * when creating the target.
		 */
		configurationBean = 
			new LocalConfigurationBean(targetElement.getAttributes(), targetElement.getId());
		factory = new LocalConfigFactory(configurationBean, targetElement.getId());
		setCurrentTargetConfig(factory.createTargetConfig());
		
		updateConsoles();
	}
	
	public TargetSocket createTargetSocket(int port) {
		TargetSocket targetSocket = new TargetSocket();
		targetSocket.host = simulatorControl.getParameters().getIpSimulator();
		targetSocket.port = port;
		return targetSocket;
	}

	public String getSystemWorkspace() {
		IPreferenceStore store = CellSimulatorTargetPlugin.getDefault().getPreferenceStore();
		return store.getString(LocalConfigurationBean.ATTR_PREFERENCES_PREFIX + LocalConfigurationBean.ATTR_SYSTEM_WORKSPACE);
	}

	public void destroy() throws CoreException {
		// TODO Implementar destruicao do environment controlado
		//System.out.println("Teste destroy localsimulator");
		
		if (query() == ITargetStatus.STOPPED) {
			return;
		}

		//releaseResumePauseStrategy();
		
		/*
		 * Kill simulator if it is not running nor paused. No jobs are expected
		 * to be running in this state.
		 */
		if (!simulatorControl.getStatus().isOperational()) {
			disconnect();
			simulatorControl.kill();
			return;
		}

		try {
			/*
			 * Try to gracefully terminate all running jobs. Might this fail, then guarantee to disconnect.
			 */
			try {
				simulatorControl.resume();
				terminateJobs(null);
			} finally {
				disconnect();
			}
	
			/*
			 * If possible, use a kind shutdown by halting the simulated host.
			 */
			//kindlyReleaseSimulator(progressQueue);
			simulatorControl.shutdown();
			
			//progressQueue.finish();
		} catch (Exception e) {
			/*
			 * Guarantee that all operations are undone.
			 */
			//progressQueue.interrupt();
			releaseResumePauseStrategy();
			disconnect();
			releaseSimulator();
		} finally {
			//monitor.done();
		}
	}
}
