/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.remotetools.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.AddressInUseException;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.exception.UnableToForwardPortException;
import org.eclipse.ptp.remote.remotetools.core.messages.Messages;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemotePortForwarding;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.core.ITargetElementStatus;
import org.eclipse.ptp.remotetools.environment.core.ITargetEventListener;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.LocalPortBoundException;
import org.eclipse.ptp.remotetools.exception.PortForwardingException;

/**
 * @since 5.0
 */
public class RemoteToolsConnection implements IRemoteConnection {
	private String fWorkingDir = null;
	private Map<String, String> fEnv = null;
	private Map<String, String> fProperties = null;

	private final String fConnName;
	private final IRemoteServices fRemoteServices;
	private final ITargetElement fTargetElement;
	private final ITargetControl fTargetControl;
	private final ListenerList fListeners = new ListenerList();

	private final ITargetEventListener fTargetEventListener = new ITargetEventListener() {
		public void handleStateChangeEvent(int event, ITargetElement element) {
			if (element.getName().equals(fConnName) && event == ITargetElementStatus.STOPPED) {
				doClose();
			}
		}
	};

	/**
	 * @since 5.0
	 */
	public RemoteToolsConnection(String name, ITargetElement element, IRemoteServices services) throws CoreException {
		fTargetElement = element;
		fTargetControl = element.getControl();
		fConnName = name;
		fRemoteServices = services;
		TargetEnvironmentManager targetMgr = EnvironmentPlugin.getDefault().getTargetsManager();
		targetMgr.addModelEventListener(fTargetEventListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#addConnectionChangeListener
	 * (org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#close()
	 */
	public synchronized void close() {
		if (isOpen()) {
			try {
				fTargetControl.kill();
			} catch (CoreException e) {
				// Ignore
			}
			doClose();
		}
	}

	/**
	 * Create a new execution manager. Required because script execution closes channel after execution.
	 * 
	 * @return execution manager
	 * @throws org.eclipse.ptp.remotetools.exception.RemoteConnectionException
	 */
	public IRemoteExecutionManager createExecutionManager() throws org.eclipse.ptp.remotetools.exception.RemoteConnectionException {
		return fTargetControl.createExecutionManager();
	}

	/**
	 * Remove element from remote tools environment and dispose of any additional resources. NOTE: must only be called if the
	 * connection is closed!
	 */
	public void dispose() {
		TargetEnvironmentManager targetMgr = EnvironmentPlugin.getDefault().getTargetsManager();
		targetMgr.removeModelEventListener(fTargetEventListener);
		fTargetElement.getType().removeElement(fTargetElement);
		fListeners.clear();
	}

	/**
	 * Notify all fListeners when this connection's status changes.
	 * 
	 * @param event
	 */
	public void fireConnectionChangeEvent(final IRemoteConnection connection, final int type) {
		IRemoteConnectionChangeEvent event = new IRemoteConnectionChangeEvent() {
			public IRemoteConnection getConnection() {
				return connection;
			}

			public int getType() {
				return type;
			}
		};
		for (Object listener : fListeners.getListeners()) {
			((IRemoteConnectionChangeListener) listener).connectionChanged(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardLocalPort(int, java.lang.String, int)
	 */
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnection_connectionNotOpen);
		}
		try {
			fTargetControl.getExecutionManager().createTunnel(localPort, fwdAddress, fwdPort);
		} catch (LocalPortBoundException e) {
			throw new AddressInUseException(e.getMessage());
		} catch (org.eclipse.ptp.remotetools.exception.RemoteConnectionException e) {
			throw new RemoteConnectionException(e.getMessage());
		} catch (CancelException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardLocalPort(java.lang .String, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnection_connectionNotOpen);
		}
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		try {
			progress.beginTask(Messages.RemoteToolsConnection_forwarding, 10);
			/*
			 * Start with a different port number, in case we're doing this all on localhost.
			 */
			int localPort = fwdPort + 1;

			/*
			 * Try to find a free port on the remote machine. This take a while, so allow it to be canceled. If we've tried all
			 * ports (which could take a very long while) then bail out.
			 */
			while (!progress.isCanceled()) {
				try {
					forwardLocalPort(localPort, fwdAddress, fwdPort);
				} catch (AddressInUseException e) {
					if (++localPort == fwdPort) {
						throw new UnableToForwardPortException(Messages.RemoteToolsConnection_remotePort);
					}
					progress.worked(1);
				}
				return localPort;
			}
			return -1;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardRemotePort(int, java.lang.String, int)
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnection_connectionNotOpen);
		}
		try {
			fTargetControl.getExecutionManager().getPortForwardingTools().forwardRemotePort(remotePort, fwdAddress, fwdPort);
		} catch (org.eclipse.ptp.remotetools.exception.RemoteConnectionException e) {
			throw new RemoteConnectionException(e.getMessage());
		} catch (CancelException e) {
			throw new RemoteConnectionException(e.getMessage());
		} catch (PortForwardingException e) {
			throw new AddressInUseException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardRemotePort(java. lang.String, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnection_connectionNotOpen);
		}
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		try {
			progress.beginTask(Messages.RemoteToolsConnection_forwarding, 10);
			/*
			 * Start with a different port number, in case we're doing this all on localhost.
			 */
			int remotePort = fwdPort + 1;
			/*
			 * Try to find a free port on the remote machine. This take a while, so allow it to be canceled. If we've tried all
			 * ports (which could take a very long while) then bail out.
			 */
			while (!progress.isCanceled()) {
				try {
					forwardRemotePort(remotePort, fwdAddress, fwdPort);
					return remotePort;
				} catch (AddressInUseException e) {
					if (++remotePort == fwdPort) {
						throw new UnableToForwardPortException(Messages.RemoteToolsConnection_remotePort);
					}
					progress.worked(1);
				}
			}
			return -1;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getAddress()
	 */
	public String getAddress() {
		return fTargetControl.getConfig().getConnectionAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		return fTargetControl.getConfig().getAttributes().getAttributesAsMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getEnv()
	 */
	public Map<String, String> getEnv() {
		if (fEnv == null) {
			fEnv = new HashMap<String, String>();

			IRemoteExecutionManager exeMgr = null;
			try {
				exeMgr = createExecutionManager();
			} catch (org.eclipse.ptp.remotetools.exception.RemoteConnectionException e) {
				// Ignore
			}
			if (exeMgr != null) {
				try {
					String env = exeMgr.getExecutionTools().executeWithOutput("printenv").trim(); //$NON-NLS-1$
					String[] vars = env.split("\n"); //$NON-NLS-1$
					for (String var : vars) {
						String[] kv = var.split("="); //$NON-NLS-1$
						if (kv.length == 2) {
							fEnv.put(kv[0], kv[1]);
						}
					}
				} catch (Exception e) {
					// Ignore
				}
			}
		}
		return Collections.unmodifiableMap(fEnv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getEnv(java.lang.String)
	 */
	public String getEnv(String name) {
		getEnv();
		return fEnv.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getName()
	 */
	public String getName() {
		return fConnName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getPort()
	 */
	public int getPort() {
		return fTargetControl.getConfig().getConnectionPort();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getProperty(java.lang.String )
	 */
	public String getProperty(String key) {
		loadProperties();
		return fProperties.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getRemoteServices()
	 */
	/**
	 * @since 4.0
	 */
	public IRemoteServices getRemoteServices() {
		return fRemoteServices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getUsername()
	 */
	public String getUsername() {
		return fTargetControl.getConfig().getLoginUsername();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#getWorkingDirectory()
	 */
	/**
	 * @since 4.0
	 */
	public String getWorkingDirectory() {
		if (!isOpen()) {
			return "/"; //$NON-NLS-1$
		}
		if (fWorkingDir == null) {
			fWorkingDir = getPwd();
			if (fWorkingDir == null) {
				return "/"; //$NON-NLS-1$
			}
		}
		return fWorkingDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#isOpen()
	 */
	public synchronized boolean isOpen() {
		return fTargetControl.query() == ITargetStatus.RESUMED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#open()
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor progress = SubMonitor.convert(monitor, 2);
		try {
			if (!isOpen()) {
				progress.beginTask(Messages.RemoteToolsConnection_open, 2);
				try {
					fTargetControl.create(progress.newChild(1));
				} catch (CoreException e) {
					throw new RemoteConnectionException(e.getMessage());
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#removeConnectionChangeListener
	 * (org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#removePortForwarding(int)
	 */
	public void removePortForwarding(int port) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnection_connectionNotOpen);
		}
		try {
			IRemotePortForwarding portForwarding = fTargetControl.getExecutionManager().getPortForwardingTools()
					.getRemotePortForwarding(port);
			fTargetControl.getExecutionManager().getPortForwardingTools().releaseForwarding(portForwarding);
		} catch (Exception e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setAddress(java.lang.String )
	 */
	public void setAddress(String address) {
		fTargetControl.getConfig().setConnectionAddress(address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setAttribute(java.lang. String, java.lang.String)
	 */
	/**
	 * @since 5.0
	 */
	public void setAttribute(String key, String value) {
		fTargetControl.getConfig().setAttribute(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setName(java.lang.String)
	 */
	public void setName(String name) {
		fTargetElement.setName(name);
		fireConnectionChangeEvent(this, IRemoteConnectionChangeEvent.CONNECTION_RENAMED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setPassword(java.lang.String )
	 */
	/**
	 * @since 5.0
	 */
	public void setPassword(String password) {
		fTargetControl.getConfig().setLoginPassword(password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setPort(int)
	 */
	public void setPort(int port) {
		fTargetControl.getConfig().setConnectionPort(port);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setUsername(java.lang.String )
	 */
	public void setUsername(String userName) {
		fTargetControl.getConfig().setLoginUsername(userName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#setWorkingDirectory(java .lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public void setWorkingDirectory(String path) {
		if (new Path(path).isAbsolute()) {
			fWorkingDir = path;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#supportsTCPPortForwarding()
	 */
	public boolean supportsTCPPortForwarding() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = getName() + " [" + getUsername() + "@" + getAddress(); //$NON-NLS-1$ //$NON-NLS-2$
		if (getPort() >= 0) {
			str += ":" + getPort(); //$NON-NLS-1$
		}
		return str + "]"; //$NON-NLS-1$
	}

	/**
	 * Actions to perform when connection closes
	 */
	private void doClose() {
		/*
		 * Force refresh of working directory when connection is closed. See bug 383033.
		 */
		fWorkingDir = null;
	}

	/**
	 * Get the result of executing a pwd command.
	 * 
	 * @return current working directory
	 */
	private String getPwd() {
		IRemoteExecutionManager exeMgr = null;
		try {
			exeMgr = createExecutionManager();
		} catch (org.eclipse.ptp.remotetools.exception.RemoteConnectionException e) {
			// Ignore
		}
		if (exeMgr != null) {
			try {
				return exeMgr.getExecutionTools().executeWithOutput("pwd").trim(); //$NON-NLS-1$
			} catch (Exception e) {
				// Ignore
			}
		}
		return null;
	}

	/**
	 * 
	 * Load the following hard-coded properties at runtime:
	 * 
	 * <dl>
	 * <dt>file.separator
	 * <dd>File separator character of the (remote) connection. Hardcoded "/" (forward slash).
	 * <dt>path.separator
	 * <dd>Path separator character of the (remote) connection. Hardcoded ":" (colon).
	 * <dt>line.separator
	 * <dd>Line separator character of the (remote) connection. Hardcoded "\n" (new-line).
	 * <dt>user.home
	 * <dd>User home directory on the (remote) connection.
	 * <dt>os.name
	 * <dd>Operating system name of the (remote) connection.
	 * For example, given results from the "uname" command:
	 * <ul>
	 * <li>Linux</li>
	 * <li>AIX</li>
	 * <li>Mac OS X - if results equal "Darwin" then results from "sw_vers -productName"</li>
	 * <li>everything else - results from "uname" command</li>
	 * </ul>
	 * <dt>os.version
	 * <dd>Operating system version of the (remote) connection.
	 * For example:
	 * <ul>
	 * <li>For Linux - results from "uname -r" such as "2.6.32-279.2.1.el6.x86_64"</li>
	 * <li>For AIX - results from "oslevel" such as "7.1.0.0"</li>
	 * <li>For Mac OS X - results from "sw_vers -productVersion" such as "10.8.3"</li>
	 * <li>For everything else - "unknown"</li>
	 * </ul>
	 * <dt>os.arch
	 * <dd>Machine architecture of the (remote) connection.
	 * For example:
	 * <ul>
	 * <li>For Linux - results from "uname -m" such as "x86_64"</li>
	 * <li>For AIX - if results from "uname -p" equals "powerpc"
	 * <ul style="list-style: none;">
	 * <li>then if "prtconf -k" contains "64-bit" then "ppc64" else "ppc"</li>
	 * <li>else the result from "uname -p"</li>
	 * </ul>
	 * </li>
	 * <li>For Mac OS X - if results from "uname -m" equals "i386"
	 * <ul style="list-style: none;">
	 * <li>then if results from "sysctl -n hw.optional.x86_64" equals "1" then "x86_64" else the results from "uname -m"</li>
	 * <li>else the results from "uname -m"</li>
	 * </ul>
	 * </li>
	 * <li>For everything else - "unknown"</li>
	 * </ul>
	 * <dl>
	 *  
	 */
	private void loadProperties() {
		if (fProperties == null) {
			fProperties = new HashMap<String, String>();
			fProperties.put(FILE_SEPARATOR_PROPERTY, "/"); //$NON-NLS-1$
			fProperties.put(PATH_SEPARATOR_PROPERTY, ":"); //$NON-NLS-1$
			fProperties.put(LINE_SEPARATOR_PROPERTY, "\n"); //$NON-NLS-1$
			fProperties.put(USER_HOME_PROPERTY, getPwd());

			try {
				IRemoteExecutionManager exeMgr = createExecutionManager();
				if (exeMgr != null) {
					String osVersion;
					String osArch;
					String osName = exeMgr.getExecutionTools().executeWithOutput("uname").trim(); //$NON-NLS-1$
					if (osName.equalsIgnoreCase("Linux")) { //$NON-NLS-1$
						osArch = exeMgr.getExecutionTools().executeWithOutput("uname -m").trim(); //$NON-NLS-1$
						osVersion = exeMgr.getExecutionTools().executeWithOutput("uname -r").trim(); //$NON-NLS-1$
					} else if (osName.equalsIgnoreCase("Darwin")) { //$NON-NLS-1$
						osName = exeMgr.getExecutionTools().executeWithOutput("sw_vers -productName").trim(); //$NON-NLS-1$
						osVersion = exeMgr.getExecutionTools().executeWithOutput("sw_vers -productVersion").trim(); //$NON-NLS-1$
						osArch = exeMgr.getExecutionTools().executeWithOutput("uname -m").trim(); //$NON-NLS-1$
						if (osArch.equalsIgnoreCase("i386")) { //$NON-NLS-1$
							String opt = exeMgr.getExecutionTools().executeWithOutput("sysctl -n hw.optional.x86_64").trim(); //$NON-NLS-1$
							if (opt.equals("1")) { //$NON-NLS-1$
								osArch = "x86_64"; //$NON-NLS-1$
							}
						}
					} else if (osName.equalsIgnoreCase("AIX")) { //$NON-NLS-1$
						osArch = exeMgr.getExecutionTools().executeWithOutput("uname -p").trim(); //$NON-NLS-1$
						osVersion = exeMgr.getExecutionTools().executeWithOutput("oslevel").trim(); //$NON-NLS-1$
						if (osArch.equalsIgnoreCase("powerpc")) { //$NON-NLS-1$
							/* Make the architecture match what Linux produces: either ppc or ppc64 */
							osArch = "ppc"; //$NON-NLS-1$
							/* Get Kernel type either 32-bit or 64-bit */
							String opt = exeMgr.getExecutionTools().executeWithOutput("prtconf -k").trim(); //$NON-NLS-1$
							if (opt.indexOf("64-bit") > 0) { //$NON-NLS-1$
								osArch += "64"; //$NON-NLS-1$
							}
						}
					} else {
						osVersion = "unknown"; //$NON-NLS-1$
						osArch = "unknown"; //$NON-NLS-1$
					}
					fProperties.put(OS_NAME_PROPERTY, osName);
					fProperties.put(OS_VERSION_PROPERTY, osVersion);
					fProperties.put(OS_ARCH_PROPERTY, osArch);
				}
			} catch (Exception e) {
				// Ignore
			}
		}
	}
}
