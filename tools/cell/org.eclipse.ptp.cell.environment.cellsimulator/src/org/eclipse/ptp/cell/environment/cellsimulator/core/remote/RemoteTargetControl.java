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
package org.eclipse.ptp.cell.environment.cellsimulator.core.remote;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.AbstractTargetControl;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.ConnectionConfig;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.TargetConfig;
import org.eclipse.ptp.cell.environment.cellsimulator.core.local.LocalConfigurationBean;
import org.eclipse.ptp.cell.simulator.core.AbstractSimulatorConfiguration;
import org.eclipse.ptp.cell.simulator.core.ISimulatorListener;
import org.eclipse.ptp.cell.simulator.core.IllegalConfigurationException;
import org.eclipse.ptp.cell.ui.progress.ICancelCallback;
import org.eclipse.ptp.cell.ui.progress.ProgressQueue;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.core.AuthToken;
import org.eclipse.ptp.remotetools.core.IRemoteConnection;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteTunnel;
import org.eclipse.ptp.remotetools.core.KeyAuthToken;
import org.eclipse.ptp.remotetools.core.PasswdAuthToken;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.extension.ITargetVariables;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.LocalPortBoundException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;

public class RemoteTargetControl extends AbstractTargetControl implements ITargetVariables {

	protected static final int REMOTE_CONNECT_EVENT = 10000;
	protected static final int REMOTE_FORWARDING_EVENT = 10001;
	protected static final int REMOTE_DISCONNECT_EVENT = 10002;
	
	RemoteConfigFactory factory;

	IRemoteConnection remoteHostConnection;
	IRemoteExecutionManager remoteHostManager;
	Map tunnels = null;

	private IRemoteTunnel shhTunnel;
	private IRemoteTunnel javaApiTunnel;
	private IRemoteTunnel consoleTunnel;
	
	/**
	 * Current connection state.
	 */
	private int state = NOT_OPERATIONAL;
	private static final int NOT_OPERATIONAL = 1;
	private static final int CONNECTING = 2;
	private static final int CONNECTED = 3;
	private static final int DISCONNECTING = 4;
	
	/**
	 * Default cipher id for remote host
	 */
	public static final String DEFAULT_HOST_CIPHER = RemotetoolsPlugin.CIPHER_DEFAULT;
	
	class RemoteHostDisconnector implements ISimulatorListener {
		boolean enabled = false;
		RemoteTargetControl control;
		
		public RemoteHostDisconnector(RemoteTargetControl control) {
			this.control = control;
			this.control.simulatorControl.addListener(this);
		}
		
		void enable() {
			enabled = true;
		}
		void disable() {
			enabled = false;
		}

		public void lifecycleStateChanged(int state) {
			if (!enabled) {
				return;
			}
			if (state == ISimulatorListener.TERMINATED) {
				control.disconnectRemoteHost();
				control.setState(NOT_OPERATIONAL);
			}
			
		}

		public void progressChanged(int progress) {
			
		}

		public void simulationStatus(int status) {
			
		}
	}
	RemoteHostDisconnector remoteHostDisconnector = new RemoteHostDisconnector(this);
	
	public RemoteTargetControl(ITargetElement element) throws CoreException {
		super(element);
		
		/*
		 * Create only target configuration, but not simulator configuration.
		 * Simulator config is verified only when creating the target.
		 * The remote connection config is created (and discarted) in order to validate parameters.
		 */
		RemoteConfigurationBean configurationBean = 
			new RemoteConfigurationBean(targetElement.getAttributes());
		factory = new RemoteConfigFactory(configurationBean);
		setCurrentTargetConfig(factory.createTargetConfig());
		
		/*
		 * Guarantee to show the consoles according to preferences.
		 */
		updateConsoles();
	}
	
	private synchronized void setState(int state) {
		this.state = state;
	}
	
	protected class LaunchCancelCallback implements ICancelCallback {
		RemoteTargetControl control;
		
		public LaunchCancelCallback(RemoteTargetControl control) {
			super();
			this.control = control;
		}

		public void cancel(boolean byUser) {
//			synchronized (this.control) {
//				releaseResumePauseStrategy();
//				disconnect();
				releaseSimulator();
//				disconnectRemoteHost();
//			}
		}
	}
	
	public boolean create(IProgressMonitor monitor) throws CoreException {

		/*
		 * Create target control configuration and simulator configuration.
		 * The simulator configuration is first created without RemoteExecutionManager
		 */
		setCurrentTargetConfig(factory.createTargetConfig());
		RemoteSimulatorConfiguration currentSimulatorParameters = factory.createRemoteSimulatorParameters(null);
		ConnectionConfig currentConnectionConfig = factory.createRemoteConnectionConfig();
		
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
		progressQueue.setCancelMessage(Messages.RemoteTargetControl_LocalCellSimulatorLaunchCancelation);
		progressQueue.setInterruptMessage(Messages.RemoteTargetControl_LocalCellSimulatorLaunchInterruption);
		progressQueue.setTaskName(Messages.RemoteTargetControl_LaunchLocalCellSimulator);
		progressQueue.addWait(-1, Messages.RemoteTargetControl_Preparing, 0);
		progressQueue.addWait(REMOTE_CONNECT_EVENT, Messages.RemoteTargetControl_RemoteHostconnection, 5);
		progressQueue.addWait(REMOTE_FORWARDING_EVENT, Messages.RemoteTargetControl_ForwardingRemoteConnection, 1);
		populateProgressQueueWithSimulatorLaunch(progressQueue);
		populateProgressQueueWithConnection(progressQueue);
		progressQueue.addWait(-2, Messages.RemoteTargetControl_LaunchCompleted, 0);
		progressQueue.setCancelCallBack(new LaunchCancelCallback(this));
		progressQueue.start();
		
		if (progressQueue.isCancelled()) {
			throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.RemoteTargetControl_RemoteSimulatorLaunchCancelled, null));
		}
		
		try {
			/*
			 * Connecto to the remote host and create tunnels. The simulator
			 * configuration needs to be created again, now with the
			 * RemoteExecutionManager. Verification is done to validate
			 * parameters on the remote host. The simulator configuration will
			 * then be updated with new information obtained after preparing the
			 * port forwarding.
			 */
			setState(CONNECTING);
			connectRemoteHost(progressQueue, currentConnectionConfig);
			currentSimulatorParameters = factory.createRemoteSimulatorParameters(remoteHostManager);
			try {
				currentSimulatorParameters.verify();
			} catch (IllegalConfigurationException e) {
				IStatus status = new Status(IStatus.ERROR, CellSimulatorTargetPlugin
						.getDefault().getBundle().getSymbolicName(), 0,
						NLS.bind(Messages.RemoteTargetControl_InvalidParameter, e.getMessage()), e);
				throw new CoreException(status);
			}
			preparePortForwarding(progressQueue, currentConnectionConfig, currentSimulatorParameters, getCurrentTargetConfig());
			setState(CONNECTED);
//			System.out.println(currentSimulatorParameters);
			
			
			/*
			 * Launch simulator.
			 */
			launchSimulator(progressQueue, currentSimulatorParameters);
			remoteHostDisconnector.enable();
			
			/*
			 * Connect SSH to the simulator.
			 */
			TargetConfig currentTargetConfig = getCurrentTargetConfig();
			SSHParameters parameters = new SSHParameters(
					"localhost", //$NON-NLS-1$
					shhTunnel.getLocalPort(), 
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
			disconnectRemoteHost();
			setState(NOT_OPERATIONAL);
			if (e instanceof CoreException) {
				throw (CoreException)e;
			} else {
				throw new CoreException(new Status(IStatus.ERROR, getPluginId(), 0, NLS.bind(Messages.RemoteTargetControl_UnexpectedErrorLaunchingSimulator, e), e));
			}
		} finally {
			/*
			 * In any case, we need to close the progress monitor.
			 */
			monitor.done();
		}
	}
	
	private void connectRemoteHost(ProgressQueue progressQueue, ConnectionConfig currentConnectionConfig) throws CoreException {
		/*
		 *  Connect to the remote host
		 */
		progressQueue.notifyOperationStarted(REMOTE_CONNECT_EVENT);
		
		try {
			AuthToken token;
			
			if (progressQueue.isCancelled()) {
				throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.RemoteTargetControl_RemoteSimulatorLaunchCancelledDuringConnection, null));
			}

			if(currentConnectionConfig.isPasswordAuth()) {
				token = new PasswdAuthToken(currentConnectionConfig.getLoginUserName(), 
						currentConnectionConfig.getLoginPassword());
			} else {
				token = new KeyAuthToken(currentConnectionConfig.getLoginUserName(), 
							new File(currentConnectionConfig.getKeyPath()), 
							currentConnectionConfig.getKeyPassphrase());
			}
			
			remoteHostConnection = RemotetoolsPlugin.createSSHConnection(
					token,
					currentConnectionConfig.getConnectionAddress(),
					currentConnectionConfig.getConnectionPort(),
					currentConnectionConfig.getCipherType(),
					currentConnectionConfig.getConnectionTimeout() * 1000);
			
			remoteHostConnection.connect();

			if (progressQueue.isCancelled()) {
				throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.RemoteTargetControl_RemoteSimulatorLaunchCancelledDuringConnection, null));
			}

			remoteHostManager = remoteHostConnection.createRemoteExecutionManager();

			if (progressQueue.isCancelled()) {
				throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.RemoteTargetControl_RemoteSimulatorLaunchCancelledDuringConnection, null));
			}

			progressQueue.notifyOperationCompleted(REMOTE_CONNECT_EVENT);
			
			// Create new map of tunnels
			tunnels = new HashMap();
		} catch (RemoteConnectionException e) {
			disconnectRemoteHost();
			throw new CoreException(new Status(IStatus.ERROR, CellSimulatorTargetPlugin.PLUGIN_ID,1, Messages.RemoteTargetControl_CouldNotConnectToRemoteHost, e));			
		}
	}
	
	private void preparePortForwarding(ProgressQueue progressQueue, ConnectionConfig currentConnectionConfig, AbstractSimulatorConfiguration simulatorParameters, TargetConfig targetConfig) throws CoreException {
		try {
			progressQueue.notifyOperationStarted(REMOTE_FORWARDING_EVENT);
			
			if (progressQueue.isCancelled()) {
				throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.RemoteTargetControl_RemoteSimulatorLaunchCancelledDuringPortForwarding, null));
			}

			// SSH
			shhTunnel = remoteHostManager.createTunnel(simulatorParameters.getIpSimulator(), targetConfig.getLoginPort());
			if (progressQueue.isCancelled()) {
				throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.RemoteTargetControl_RemoteSimulatorLaunchCancelledDuringPortForwarding, null));
			}
			
			// Java API
			if (simulatorParameters.doJavaApiSocketInit()) {
				javaApiTunnel = remoteHostManager.createTunnel(currentConnectionConfig.getConnectionAddress(), simulatorParameters.getJavaApiPort());
				simulatorParameters.setJavaApiSocketHost(null); // localhost
				simulatorParameters.setJavaApiSocketPort(javaApiTunnel.getLocalPort()); // localhost
			}
			if (progressQueue.isCancelled()) {
				throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.RemoteTargetControl_RemoteSimulatorLaunchCancelledDuringPortForwarding, null));
			}
			
			// Console
			if (simulatorParameters.doConsoleSocketInit()) {
				consoleTunnel = remoteHostManager.createTunnel(currentConnectionConfig.getConnectionAddress(), simulatorParameters.getConsolePort());
				simulatorParameters.setConsoleSocketHost(null); // localhost
				simulatorParameters.setConsoleSocketPort(consoleTunnel.getLocalPort()); // localhost
			}
			if (progressQueue.isCancelled()) {
				throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.RemoteTargetControl_RemoteSimulatorLaunchCancelledDuringPortForwarding, null));
			}

			progressQueue.notifyOperationCompleted(REMOTE_FORWARDING_EVENT);

		} catch (RemoteConnectionException e) {
			disconnectRemoteHost();
			throw new CoreException(new Status(IStatus.ERROR, CellSimulatorTargetPlugin.PLUGIN_ID,1, Messages.RemoteTargetControl_CouldNotCreatePortForward, e));			
		} catch (LocalPortBoundException e) {
			disconnectRemoteHost();
			throw new CoreException(new Status(IStatus.ERROR, CellSimulatorTargetPlugin.PLUGIN_ID,1, Messages.RemoteTargetControl_CouldNotCreatePortForward, e));			
		} catch (CancelException e) {
			disconnectRemoteHost();
			throw new CoreException(new Status(IStatus.ERROR, CellSimulatorTargetPlugin.PLUGIN_ID,1, Messages.RemoteTargetControl_CouldNotCreatePortForward, e));			
		}
	}

	private void disconnectRemoteHost() {
		remoteHostDisconnector.disable();
		shhTunnel = null;
		javaApiTunnel = null;
		consoleTunnel = null;
		tunnels = null;
		if (remoteHostManager != null) {
			try {
				remoteHostManager.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			remoteHostManager = null;			
		}
		if (remoteHostConnection != null) {
			try {
				remoteHostConnection.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
			remoteHostConnection = null;
		}
	}

	protected class ShutdownCancelCallback implements ICancelCallback {
		RemoteTargetControl control;
		
		public ShutdownCancelCallback(RemoteTargetControl control) {
			super();
			this.control = control;
		}

		public void cancel(boolean byUser) {
//			synchronized (this.control) { 
//				releaseResumePauseStrategy();
//				disconnect();
				releaseSimulator();
//			}
		}
	}

	public boolean kill(IProgressMonitor monitor) throws CoreException {

		if (query() == ITargetStatus.STOPPED) {
			throw new CoreException(new Status(IStatus.ERROR,
					CellSimulatorTargetPlugin.PLUGIN_ID, 1,
					Messages.RemoteTargetControl_SimulatorNotLaunched,
					null));
		}
		
		if (simulatorControl.getStatus().isNotRunning()) {
			/*
			 * Zombee connection. Disconnect.
			 */
			disconnectRemoteHost();
			setState(NOT_OPERATIONAL);
			return true;
		}

		releaseResumePauseStrategy();
		
		/*
		 * Kill simulator if it is not running nor paused. No jobs are expected
		 * to be running in this state.
		 */
		if (!simulatorControl.getStatus().isOperational()) {
			disconnect();
			simulatorControl.kill();
			disconnectRemoteHost();
			return true;
		}

		/*
		 * Create the progress queue.
		 */
		ProgressQueue progressQueue = new ProgressQueue(monitor);
		progressQueue.setCancelMessage(Messages.RemoteTargetControl_LocalCellSimulatorLaunchShutdownCancelation);
		progressQueue.setInterruptMessage(Messages.RemoteTargetControl_LocalCellSimulatorForceShutdown);
		progressQueue.setTaskName(Messages.RemoteTargetControl_LocalCellSimulatorShutdown);
		progressQueue.addWait(-1, Messages.RemoteTargetControl_ShuttingDown, 0);
		populateProgressQueueWithSimulatorShutdown(progressQueue);
		progressQueue.addWait(-2, Messages.RemoteTargetControl_ShutDownCompleted, 1);
		progressQueue.addWait(-3, Messages.RemoteTargetControl_CleaningUp, 1);
		progressQueue.addWait(REMOTE_CONNECT_EVENT, Messages.RemoteTargetControl_Disconnecting, 1);
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
			
			/*
			 * Disconnect remote host.
			 */
			setState(DISCONNECTING);
			disconnectRemoteHost();
			progressQueue.notifyOperationCompleted(REMOTE_DISCONNECT_EVENT);
			setState(NOT_OPERATIONAL);

			progressQueue.finish();
		} catch (Exception e) {
			/*
			 * Guarantee that all operations are undone.
			 */
			progressQueue.interrupt();
			releaseResumePauseStrategy();
			disconnect();
			releaseSimulator();
			disconnectRemoteHost();
		} finally {
			monitor.done();
			setState(NOT_OPERATIONAL);
		}

		return true;
	}

public synchronized int query() {
		if (state == CONNECTING) {
			return ITargetStatus.STARTED;
		} else if (state == CONNECTED) {
			if (simulatorControl.getStatus().isOperational()) {
				if (! isConnected()) {
					return ITargetStatus.STARTED;				
				} else {
					if (simulatorControl.getStatus().isPaused()) {
						return ITargetStatus.PAUSED;
					} else {
						return ITargetStatus.RESUMED;
					}
				}
			} else {
				return ITargetStatus.STARTED;
			}
		} else if (state == DISCONNECTING) {
			return ITargetStatus.STARTED;
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
		RemoteConfigurationBean configurationBean = 
			new RemoteConfigurationBean(targetElement.getAttributes());
		factory = new RemoteConfigFactory(configurationBean);
		setCurrentTargetConfig(factory.createTargetConfig());
		
		updateConsoles();
	}
		
	private IRemoteTunnel createTunnel(int remotePort) throws RemoteConnectionException, LocalPortBoundException, CancelException {
		
		/*
		 * First, check if there is already a tunnel for the required port.
		 */
		Integer remotePortInteger = new Integer(remotePort);
		if (tunnels.containsKey(remotePortInteger)) {
			return (IRemoteTunnel) tunnels.get(remotePortInteger);
		}

		/*
		 * If not, create the tunnel.
		 */
		IRemoteTunnel tunnel = remoteHostManager.createTunnel(
				simulatorControl.getParameters().getIpSimulator(), 
				remotePort
		);
		tunnels.put(remotePortInteger, tunnel);
		return tunnel;
	}

	
	public TargetSocket createTargetSocket(int port) throws CoreException {
		Assert.isTrue(isConnected());
		try {
			IRemoteTunnel tunnel = createTunnel(port);
			TargetSocket socket = new TargetSocket();
			socket.host = "localhost"; //$NON-NLS-1$
			socket.port = tunnel.getLocalPort();
			return socket;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, CellSimulatorTargetPlugin.PLUGIN_ID,0, Messages.RemoteTargetControl_CouldNotCreateSocket, e));
		}
	}
	
	public String getSystemWorkspace() {
		IPreferenceStore store = CellSimulatorTargetPlugin.getDefault().getPreferenceStore();
		return store.getString(LocalConfigurationBean.ATTR_PREFERENCES_PREFIX + LocalConfigurationBean.ATTR_SYSTEM_WORKSPACE);
	}

	public void destroy() throws CoreException {
		// TODO Implementar destruicao do environment controlado
		//System.out.println("destroy Remote Target");
		
		
		if (query() == ITargetStatus.STOPPED) {
			return;
		}
		
		if (simulatorControl.getStatus().isNotRunning()) {
			/*
			 * Zombee connection. Disconnect.
			 */
			disconnectRemoteHost();
			setState(NOT_OPERATIONAL);
			return;
		}

		releaseResumePauseStrategy();
		
		/*
		 * Kill simulator if it is not running nor paused. No jobs are expected
		 * to be running in this state.
		 */
		if (!simulatorControl.getStatus().isOperational()) {
			disconnect();
			simulatorControl.kill();
			disconnectRemoteHost();
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
			
			/*
			 * Disconnect remote host.
			 */
			setState(DISCONNECTING);
			disconnectRemoteHost();
			//progressQueue.notifyOperationCompleted(REMOTE_DISCONNECT_EVENT);
			setState(NOT_OPERATIONAL);

			//progressQueue.finish();
		} catch (Exception e) {
			/*
			 * Guarantee that all operations are undone.
			 */
			//progressQueue.interrupt();
			//releaseResumePauseStrategy();
			disconnect();
			releaseSimulator();
			disconnectRemoteHost();
		} finally {
			//monitor.done();
			setState(NOT_OPERATIONAL);
		}
		
	}
}
