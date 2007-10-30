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
package org.eclipse.ptp.remotetools.environment.generichost.core;


import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.environment.control.SSHTargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.extension.ITargetVariables;
import org.eclipse.ptp.remotetools.environment.generichost.Activator;

/**
 * Controls an instance of a target created from the Environment.
 * @author Daniel Felix Ferber
 * @since 1.2
 */
public class TargetControl extends SSHTargetControl implements ITargetControl, ITargetVariables {

	/**
	 * Configuration provided to the target control.
	 */
	ConfigFactory configFactory;
	TargetConfig currentTargetConfig;
	
	/**
	 * Name of the target.
	 */
	//String name;
	
	/**
	 * BackReference to the target element
	 */
	ITargetElement targetElement;
	
	/**
	 * Current connection state.
	 */
	private int state;
	private static final int NOT_OPERATIONAL = 1;
	private static final int CONNECTING = 2;
	private static final int CONNECTED = 3;
	private static final int DISCONNECTING = 4;
	
	/**
	 * Default cipher id
	 */
	public static final String DEFAULT_CIPHER = RemotetoolsPlugin.CIPHER_DEFAULT;
	
	/**
	 * Creates a target control for Remote Cell Box. If some attribute related to the environment has an invalid
	 * format, an exception is thrown. Simulator attributes are not checked yet, but will be checked when the target is
	 * created (launched).
	 * 
	 * @param attributes
	 *            Configuration attributes
	 * @param element
	 *            Name for the target (displayed in GUI)
	 * @throws CoreException
	 *             Some attribute is not valid
	 */
	public TargetControl(ITargetElement element) throws CoreException {
		super();
		state = NOT_OPERATIONAL;
		targetElement = element;
		configFactory = new ConfigFactory(targetElement.getAttributes());
		currentTargetConfig = configFactory.createTargetConfig();
	}

	/**
	 * Connect to the remote cell box. On every error or possible failure, an exception
	 * (CoreException) is thrown, whose (multi)status describe the error(s) that prevented creating the target control.
	 * 
	 * @param monitor
	 *            Progress indicator or <code>null</code>
	 * @return Always true.
	 * @throws CoreException
	 *             Some attribute is not valid, the simulator cannot be launched, the ssh failed to connect.
	 */
	public boolean create(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.TargetControl_create_MonitorConnecting, 1);
		/*
		 *  Connect to the remote Cell Box
		 */
		if(currentTargetConfig.isPasswordAuth()) {
			setConnectionParameters(
					new SSHParameters(
							currentTargetConfig.getConnectionAddress(),
							currentTargetConfig.getConnectionPort(),
							currentTargetConfig.getLoginUserName(),
							currentTargetConfig.getLoginPassword(),
							currentTargetConfig.getCipherType(),
							currentTargetConfig.getConnectionTimeout()*1000
					)
				);
		} else {
			setConnectionParameters(
					new SSHParameters(
							currentTargetConfig.getConnectionAddress(),
							currentTargetConfig.getConnectionPort(),
							currentTargetConfig.getLoginUserName(),
							currentTargetConfig.getKeyPath(),
							currentTargetConfig.getKeyPassphrase(),
							currentTargetConfig.getCipherType(),
							currentTargetConfig.getConnectionTimeout()*1000
					)
				);
		}
		
		try {
			setState(CONNECTING);
			super.create(monitor);
			setState(CONNECTED);
			monitor.worked(1);
		} catch (CoreException e) {
			disconnect();
			setState(NOT_OPERATIONAL);
		}
		monitor.done();
		return true;
	}

	private synchronized void setState(int state) {
		this.state = state;
	}

	public boolean kill(IProgressMonitor monitor) throws CoreException {
		try {
			setState(DISCONNECTING);
			super.kill(monitor);
		} finally {
			setState(NOT_OPERATIONAL);
		}
		return true;
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
		throw new CoreException(new Status(IStatus.ERROR, getPluginId(), 0,
				Messages.TargetControl_resume_CannotResume, null));
	}

	public boolean stop(IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, getPluginId(), 0,
				Messages.TargetControl_stop_CannotPause, null));
	}

	/**
	 * Sets new attributes.
	 * <p>Only allowed when the Cell Simulator is not running. The simulator attributes will only be parsed the next time the simulator is started.
	 */
	public void updateConfiguration() throws CoreException {
		//targetElement.setName(name);
		configFactory = new ConfigFactory(targetElement.getAttributes());
		currentTargetConfig = configFactory.createTargetConfig();
	}

	public String getName() {
		return targetElement.getName();
	}

	protected String getPluginId() {
		return Activator.PLUGIN_ID;
	}
	
	public TargetSocket createTargetSocket(int port) {
		Assert.isTrue(isConnected());
		TargetSocket socket = new TargetSocket();
		socket.host = currentTargetConfig.getConnectionAddress();
		socket.port = port;
		return socket;
	}

	public String getSystemWorkspace() {
		return currentTargetConfig.getSystemWorkspace();
	}

	public void destroy() throws CoreException {
		// TODO Implementar destruicao do environment controlado
		//System.out.println("Teste destroy cellbox");
		
		// End all jobs, if possible, then disconnect
		try {
			terminateJobs(null);
		} finally {
			disconnect();
		}
		
		//setState(NOT_OPERATIONAL);
	}
}
