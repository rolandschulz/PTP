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
package org.eclipse.ptp.cell.environment.remotesimulator.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.cell.environment.remotesimulator.Activator;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.core.AuthToken;
import org.eclipse.ptp.remotetools.core.IRemoteConnection;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteTunnel;
import org.eclipse.ptp.remotetools.core.KeyAuthToken;
import org.eclipse.ptp.remotetools.core.PasswdAuthToken;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.environment.control.SSHTargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.extension.ITargetVariables;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.LocalPortBoundException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;

/**
 * Controls an instance of a target created from the Environment.
 * @author Daniel Felix Ferber
 * @since 1.2.0
 */
public class TargetControl extends SSHTargetControl implements
		ITargetControl, ITargetVariables {

	//private static int ACTUAL_TUNNEL_PORT = Parameters.getNUMERIC_MIN_TUNNEL_PORT();
	
	/**
	 * Configuration provided to the target control.
	 */
	//Map attributes;
	TargetConfig currentTargetConfig;
	
	/**
	 * Default cipher type for remote host connection
	 */
	public static String DEFAULT_HOST_CIPHER = RemotetoolsPlugin.CIPHER_DEFAULT;
	
	/**
	 * Default cipher type for simulator connection
	 */
	public static String DEFAULT_SIMULATOR_CIPHER = RemotetoolsPlugin.CIPHER_BLOWFISH;
	
	/**
	 * Back-Reference for the TargetElement
	 */
	ITargetElement targetElement;
	
	IRemoteConnection remoteHostConnection;
	IRemoteExecutionManager remoteHostManager;
	int nextPort = 0;
	Map tunnels = null;
	
	/**
	 * Current connection state.
	 */
	private int state = NOT_OPERATIONAL;
	private static final int NOT_OPERATIONAL = 1;
	private static final int CONNECTING = 2;
	private static final int CONNECTED = 3;
	private static final int DISCONNECTING = 4;
	
	/**
	 * Creates a target control for Remote Cell Simulator. If some attribute related to the environment has an
	 * invalid format, an exception is thrown. Simulator attributes are not checked yet, but will be checked when the
	 * target is created (launched).
	 * 
	 * @param attributes
	 *            Configuration attributes
	 * @param name
	 *            Name for the target (displayed in GUI)
	 * @throws CoreException
	 *             Some attribute is not valid
	 */
	public TargetControl(ITargetElement element) throws CoreException {
		super();
		targetElement = element;
		// Set new map from the ConfigFactory, if none exists
		if(targetElement.getAttributes() == null) {
			targetElement.setAttributes(ConfigFactory.createDefaultConfig());
		}
		/*if (targetElement.getAttributes() != null) {
			this.attributes = new HashMap(attributes);
		} else {
			this.attributes = ConfigFactory.createDefaultConfig();
		}*/
		
		checkControlConfig();
		createConsoles();
	}
	
	private synchronized void setState(int state) {
		this.state = state;
	}
	
	/**
	 * Connect to the remote host, then connect to the simulator on the remote box. 
	 * On every error or possible failure, an exception
	 * (CoreException) is thrown, whose (multi)status describe the error(s) that prevented creating the target control.
	 * 
	 * @param monitor
	 *            Progress indicator or <code>null</code>
	 * @return Always true.
	 * @throws CoreException
	 *             Some attribute is not valid, the simulator cannot be launched, the ssh failed to connect.
	 */
	public boolean create(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.TargetControl_RemoteSimulatorConnection, 2);
		
		/*
		 * Create target control configuration and simulator configuration.
		 */
		ControlAttributes controlAttributes = 
			new ControlAttributes(targetElement.getAttributes());
		ConfigFactory configFactory = new ConfigFactory(controlAttributes);
		currentTargetConfig = configFactory.createTargetConfig();

		setState(CONNECTING);
		monitor.subTask(Messages.TargetControl_RemoteHostConnection);
		IRemoteTunnel tunnel = null;
		try {
			tunnel = connectRemoteHost();
		} catch (CoreException e) {
			disconnectRemoteHost();
			setState(NOT_OPERATIONAL);
			throw e;
		}
		monitor.worked(1);
		
		/*
		 * Configure the environment to connect over the tunnel.
		 */
		try {
			monitor.subTask(Messages.TargetControl_RemoteSimulatorConnection);
			if(currentTargetConfig.isSimulatorIsPasswordAuth()) {
				setConnectionParameters(
					new SSHParameters(
							"localhost", //$NON-NLS-1$
							tunnel.getLocalPort(),
							currentTargetConfig.getSimulatorLoginUserName(), 
							currentTargetConfig.getSimulatorLoginPassword(),
							currentTargetConfig.getSimulatorCipherType(),
							currentTargetConfig.getSimulatorConnectionTimeout() * 1000
					)
				);
			} else {
				setConnectionParameters(
					new SSHParameters(
							"localhost", //$NON-NLS-1$
							tunnel.getLocalPort(),
							currentTargetConfig.getSimulatorLoginUserName(), 
							currentTargetConfig.getSimulatorKeyPath(),
							currentTargetConfig.getSimulatorPassphrase(),
							currentTargetConfig.getSimulatorCipherType(),
							currentTargetConfig.getSimulatorConnectionTimeout() * 1000
					)
				);
			}
			super.create(monitor);
			monitor.worked(1);
			setState(CONNECTED);
		} catch (CoreException e) {
			disconnect();
			disconnectRemoteHost();
			setState(NOT_OPERATIONAL);
			throw e;
		} finally {			
			monitor.done();
		}
		return true;
	}

	private void disconnectRemoteHost() {
		if (tunnels != null) {
			tunnels = null;
		}
		if (remoteHostManager != null) {
			remoteHostManager.close();
			remoteHostManager = null;			
		}
		if (remoteHostConnection != null) {
			remoteHostConnection.disconnect();
			remoteHostConnection = null;
		}
	}

	private IRemoteTunnel connectRemoteHost() throws CoreException {
		/*
		 *  Connect to the remote host
		 */
		try {
			AuthToken token;
			
			if(currentTargetConfig.isRemoteIsPasswordAuth()) {
				token = new PasswdAuthToken(currentTargetConfig.getRemoteLoginUserName(), 
							currentTargetConfig.getRemoteLoginPassword());
			} else {
				token = new KeyAuthToken(currentTargetConfig.getRemoteLoginUserName(), 
							new File(currentTargetConfig.getRemoteKeyPath()), 
							currentTargetConfig.getRemoteKeyPassphrase());
			}
			
			remoteHostConnection = RemotetoolsPlugin.createSSHConnection(
					token,
					currentTargetConfig.getRemoteConnectionAddress(),
					currentTargetConfig.getRemoteConnectionPort(),
					currentTargetConfig.getRemoteCipherType(),
					currentTargetConfig.getRemoteConnectionTimeout() * 1000);
			
			remoteHostConnection.connect();
			remoteHostManager = remoteHostConnection.createRemoteExecutionManager();
		} catch (RemoteConnectionException e) {
			disconnectRemoteHost();
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,1, Messages.TargetControl_CouldNotConnectToRemoteHost, e));			
		}
		
		/*
		 * Create tunnel to simulator
		 */
		IRemoteTunnel tunnel;
		try {
			nextPort = 0;
			tunnels = new HashMap();
			tunnel = createTunnel(currentTargetConfig.getSimulatorConnectionPort());
		} catch (Exception e) {
			disconnectRemoteHost();
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,1, Messages.TargetControl_CouldNotConnectToSimulatorOnRemoteHost, e));			
		}  
		return tunnel;
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
		 * If there is no tunnel, create a new one.
		 */
		/*if (nextPort == 0) {
			nextPort = ACTUAL_TUNNEL_PORT++;//currentTargetConfig.getTunnelPortMin();
		} else if (nextPort > Parameters.NUMERIC_MAX_TUNNEL_PORT) {
			ACTUAL_TUNNEL_PORT = Parameters.NUMERIC_MIN_TUNNEL_PORT;
			nextPort = ACTUAL_TUNNEL_PORT++;			
		}
		int currentPort = nextPort++;*/

		IRemoteTunnel tunnel = remoteHostManager.createTunnel(
				currentTargetConfig.getSimulatorConnectionAddress(), 
				remotePort
		);
		tunnels.put(remotePortInteger, tunnel);
		return tunnel;
	}
	
	public boolean kill(IProgressMonitor monitor) throws CoreException {
		try {
			setState(DISCONNECTING);
			super.kill(monitor);
		} finally {
			disconnectRemoteHost();
			setState(NOT_OPERATIONAL);
		}
		return true;
	}
	
	public String getName() {
		return targetElement.getName();
	}
	
	public synchronized int query() {
		switch (state) {
		case NOT_OPERATIONAL:
			return ITargetStatus.STOPPED;
		case CONNECTING:
		case DISCONNECTING:
			return ITargetStatus.STARTED;
		case CONNECTED:
			if (isConnected()) {
				return ITargetStatus.RESUMED;
			} else {
				return ITargetStatus.STARTED;
			}
		default:
			return ITargetStatus.STOPPED;
		}
	}

	public boolean resume(IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,0, Messages.TargetControl_CannotResumeRemoteSimulator, null));
	}

	public boolean stop(IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,0, Messages.TargetControl_CannotPauseRemoteSimulator, null));
	}

	/**
	 * Sets new attributes.
	 * <p>Only allowed when the Cell Simulator is not running. The simulator attributes will only be parsed the next time the simulator is started.
	 */
	public void updateConfiguration() throws CoreException {
		//targetElement.setName(name);
		//targetElement.setAttributes(attributes);

		checkControlConfig();
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
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,0, Messages.TargetControl_CouldNotCreateSocket, e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.control.SSHCellTargetControl#getPluginId()
	 */
	protected String getPluginId() {
		return Activator.PLUGIN_ID;
	}

	private void checkControlConfig() throws CoreException {
		ControlAttributes controlAttributes = 
			new ControlAttributes(targetElement.getAttributes());
		ConfigFactory configFactory = new ConfigFactory(controlAttributes);
		IStatus status = configFactory.checkTargetConfig();
		if (! status.isOK()) {
			throw new CoreException(status);
		}
	}

	private void createConsoles() {
	}

	public String getSystemWorkspace() {
		return currentTargetConfig.getSystemWorkspace();
	}

	public void destroy() throws CoreException {
		// TODO Implementar destruicao do environment controlado
		//System.out.println("Teste destroy attach to cellsim");
		
//		 End all jobs, if possible, then disconnect 
		// from the simulator, then from the remote host
		try {
			terminateJobs(null);
		} finally {
			disconnect();
			disconnectRemoteHost();
		}
	}
}
