/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.internal.ssh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remotetools.core.AuthToken;
import org.eclipse.ptp.remotetools.core.IRemoteConnection;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteOperation;
import org.eclipse.ptp.remotetools.core.KeyAuthToken;
import org.eclipse.ptp.remotetools.core.PasswdAuthToken;
import org.eclipse.ptp.remotetools.exception.LocalPortBoundException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.internal.common.RemoteTunnel;
import org.eclipse.ptp.remotetools.internal.core.ConnectionProperties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * The connection to a remote host.
 * Manages a pool of connection used to open execution channels, since some SSH server
 * showed to impose a limit of execution channels per connection.
 *
 * @author Richard Maciel and Daniel Felix Ferber.
 */
public class Connection implements IRemoteConnection {
	/**
	 * Data structure to access the ssh library.
	 */
	private JSch jsch;

	/**
	 * A connection to the remote host. The default connections is always created, but more
	 * connections may be added to the pool on demand.
	 */
	Session defaultSession;
	private AuthToken authToken;
	//private String password;
	private String username;
	private String hostname;
	private int port;
	private int timeout;
	private String cipherType;
	private SSHUserInfo sshuserinfo = new SSHUserInfo();

	/**
	 * All executions managers created for this connection.
	 */
	private ArrayList executionManagers;
	/**
	 * Tunnels to remote host.
	 */
	Set tunnels;

	/**
	 * Executions on remote host.
	 */
	Set executions;
	/**
	 * Default sftp channel shared by all executions managers and file tools.
	 */
	ChannelSftp sftpChannel;
	/**
	 * Hashtable that keeps all remote executions that can be killed.
	 * The table is indexed by Internal PID.
	 */
	Hashtable activeProcessTable;
	/**
	 * The internal identification number for the next remote execution.
	 */
	private int nextInternalPID;
	/**
	 * The control connection.
	 */
	private ControlChannel controlChannel;
	/**
	 * A job that watches for finished executions.
	 */
	private ExecutionObserver executionObserver;

	/**
	 * Array of all connections and how many pty channels were used.
	 */
	ArrayList connectionPool = new ArrayList();
	private class ConnectionSlot {
		Session session = null;
		int numberUsedChannels = 0;
		ConnectionSlot(Session session) {
			this.session = session;
		}
		ConnectionSlot(Session session, int initialLoad) {
			this.session = session;
			this.numberUsedChannels = initialLoad;
		}
	}
	/**
	 * Maps a channel to the connection where it was created.
	 */
	HashMap channelToConnectioPool;

	/**
	 * Locks used on synchronized operations.
	 */
	protected ConnectionLocks connectionLocks;

	RemotePortForwardingPool forwardingPool;

	/**
	 * Default constructor
	 *
	 * @param authToken
	 * @param hostname
	 * @param port
	 * @param timeout
	 * @param cipherType
	 */
	public Connection(AuthToken authToken, String hostname, int port, String cipherType, int timeout) {
		this.jsch = new JSch();
		this.authToken = authToken;
		this.username = authToken.getUsername();
		this.hostname = hostname;
		this.port = port;
		this.timeout = timeout;
		this.cipherType = cipherType;

		// Convert information for the UserInfo class used by JSch
		if(authToken instanceof PasswdAuthToken) {
			sshuserinfo.isPasswdBased = true;
			sshuserinfo.password = ((PasswdAuthToken)authToken).getPassword();
		} else if(authToken instanceof KeyAuthToken) {
			KeyAuthToken token = (KeyAuthToken)authToken;
			sshuserinfo.isPasswdBased = false;
			sshuserinfo.passphrase = token.getPassphrase();
		} else {
			throw new RuntimeException(Messages.Connection_AuthenticationTypeNotSupported);
		}
	}

	public Connection(AuthToken authToken, String hostname, int port, String cipherType) {
		this(authToken, hostname, port, CipherTypes.CIPHER_DEFAULT, ConnectionProperties.defaultTimeout);
	}

	public Connection(AuthToken authToken, String hostname) {
		this(authToken, hostname, ConnectionProperties.defaultPort,
				CipherTypes.CIPHER_DEFAULT, ConnectionProperties.defaultTimeout);
	}

	public Connection(AuthToken authToken, String hostname, int port) {
		this(authToken, hostname, port, CipherTypes.CIPHER_DEFAULT, ConnectionProperties.defaultTimeout);
	}

	public synchronized void connect() throws RemoteConnectionException {
		this.nextInternalPID = 0;

		/*
		 * Insert key information if necessary
		 */
		if(authToken instanceof KeyAuthToken) {
			KeyAuthToken token = (KeyAuthToken)authToken;
			try {
				jsch.addIdentity(token.getKeyPath().getAbsolutePath());
			} catch (JSchException e) {
				throw new RemoteConnectionException(Messages.Connection_Connect_InvalidPrivateKey, e);
			}
		}

		/*
		 * Create session.
		 */
		try {
			defaultSession = jsch.getSession(username, hostname, port);
			defaultSession.setUserInfo(sshuserinfo);
		} catch (JSchException e) {
			disconnect();
			throw new RemoteConnectionException(Messages.Connection_Connect_FailedCreateSession, e);
		}

		setSessionCipherType(defaultSession);

		/*
		 * Connect to remote host.
		 */
		try {
			defaultSession.connect(timeout);
		} catch (JSchException e) {
			disconnect();
			throw new RemoteConnectionException(Messages.Connection_Connect_FailedConnect, e);
		} catch (Exception e) {
			disconnect();
			throw new RemoteConnectionException(Messages.Connection_Connect_FailedUnsupportedKeySize, e);
		}

		/*
		 * Create control execution channel.
		 */
		try {
			controlChannel = new ControlChannel(this);
			controlChannel.open();
		} catch (RemoteConnectionException e) {
			disconnect();
			throw new RemoteConnectionException(Messages.Connection_Connect_FailedCreateControlChannel, e);
		}

		/*
		 * Create convenience sftp channel.
		 */
		try {
			sftpChannel = (ChannelSftp) defaultSession.openChannel("sftp"); //$NON-NLS-1$
			sftpChannel.connect();
		} catch (JSchException e) {
			disconnect();
			throw new RemoteConnectionException(Messages.Connection_Connect_FailedCreateSFTPConnection, e);
		}

		/*
		 * Create observer job but do not schedule it
		 */
		executionObserver = new ExecutionObserver(this);
		executionObserver.setPriority(Job.DECORATE);
		executionObserver.setSystem(true);

		/*
		 * The default session cannot be fully used for connection pool,
		 * since some channels are already using pty.
		 */
		connectionPool = new ArrayList();
		ConnectionSlot slot = new ConnectionSlot(defaultSession, ConnectionProperties.initialDefaultSessionLoad);
		connectionPool.add(slot);

		executionManagers = new ArrayList();
		tunnels = new HashSet();
		executions = new HashSet();
		activeProcessTable = new Hashtable();
		channelToConnectioPool = new HashMap();
		forwardingPool = new RemotePortForwardingPool(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.remotetools.IRemoteConnection#getRemoteExecutionManager()
	 */
	public synchronized IRemoteExecutionManager createRemoteExecutionManager() throws RemoteConnectionException {
		ExecutionManager e =  new ExecutionManager(this);
		executionManagers.add(e);
		return e;
	}

	synchronized protected void releaseExcutionManager(ExecutionManager manager) {
		executionManagers.remove(manager);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.remotetools.IRemoteConnection#disconnect()
	 */
	public synchronized void disconnect() {
		/*
		 * First, cancel all ongoing executions and tunnels created by managers, by closing their execution managers.
		 */
		if (executionManagers != null) {
			Iterator iterator = executionManagers.iterator();
			while (iterator.hasNext()) {
				ExecutionManager manager = (ExecutionManager) iterator.next();
				manager.close();
				iterator = executionManagers.iterator();
			}
		}
		if (executionObserver != null) {
			executionObserver.cancel();
		}

		/*
		 * Then, close channels created by the connection.
		 */
		if (controlChannel != null) {
			controlChannel.close();
		}
		if (sftpChannel != null) {
			sftpChannel.disconnect();
		}

		/*
		 * Close all connections from the pool.
		 */
		if (connectionPool != null) {
			Iterator iterator = connectionPool.iterator();
			while (iterator.hasNext()) {
				ConnectionSlot slot = (ConnectionSlot) iterator.next();
				slot.session.disconnect();
			}
		}

		/*
		 * Reset internal state variables.
		 */
		defaultSession = null;
		sftpChannel = null;
		controlChannel = null;
		connectionPool = null;
		executionObserver = null;
		executions = null;
		executionManagers = null;
		tunnels = null;
		if (forwardingPool != null) {
			forwardingPool.disconnect();
			forwardingPool = null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.remotetools.IRemoteConnection#isConnected()
	 */
	public synchronized boolean isConnected() {
		if (defaultSession == null) {
			return false;
		} else {
			return defaultSession.isConnected();
		}
	}

	public String getHostname() {
		return hostname;
	}

	/*public String getPassword() {
		return password;
	}*/

	public int getPort() {
		return port;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getUsername() {
		return username;
	}

	/**
	 * This class is required by the jsch library.
	 *
	 * @author Richard Maciel
	 *
	 */
	private class SSHUserInfo implements UserInfo, UIKeyboardInteractive {
		public String password;
		public String passphrase;
		public boolean isPasswdBased;

		private SSHUserInfo() { }

		public String getPassword() {
			return password;
		}

		public boolean promptYesNo(String str) {
			// Always accept host identity
			return true;
		}

		public String getPassphrase() {
			return passphrase;
		}

		public boolean promptPassphrase(String message) {
			return !isPasswdBased;
		}

		public boolean promptPassword(String message) {
			return isPasswdBased;
		}

		public void showMessage(String message) {
		}

		public String[] promptKeyboardInteractive(final String destination,
				final String name, final String instruction,
				final String[] prompt, final boolean[] echo) {
			if (prompt.length != 1
					|| echo[0] != false
					|| password == null) {
				return null;
			}
			String[] response = new String[1];
			response[0] = password;
			return response;
		}

	}

	/**
	 * Creates a new execution channel. The channel may or may not be managed by the connection pool.
	 * All execution channels that require PTY must be managed by the pool, except really special cases,
	 * like the control channel and default SFTP channel.
	 * Channels without PTY may be managed, by it is recommended no to be. This will allocate them into the
	 * default session, since an unlimited number of not PTY channels can be allocated into the default session.
	 * @param isInConnectionPool As described.
	 * @return The requested execution channel.
	 * @throws RemoteConnectionException If the allocation of new channel failed of if it was not possible
	 * to create a new ssh session for the new channel.
	 */
	protected ChannelExec createExecChannel(boolean isInConnectionPool) throws RemoteConnectionException {
		if (isInConnectionPool) {
			/*
			 * Search for the first available connection slot or create a new one if all are full.
			 */
			ConnectionSlot suggestedSlot = null;
			Iterator iterator = connectionPool.iterator();
			while (iterator.hasNext()) {
				ConnectionSlot slot = (ConnectionSlot) iterator.next();
				if (slot.numberUsedChannels < ConnectionProperties.maxChannelsPerConnection) {
					suggestedSlot = slot;
					break;
				}
			}
			if (suggestedSlot == null) {
				suggestedSlot = createConnecitonSlot();
			}
			/*
			 * Create the channel and update the pool.
			 */
			ChannelExec channel;
			try {
				channel = (ChannelExec) suggestedSlot.session.openChannel("exec"); //$NON-NLS-1$
			} catch (JSchException e) {
				throw new RemoteConnectionException(Messages.Connection_CreateExecChannel_FailedCreateNewExecChannel, e);
			}
			suggestedSlot.numberUsedChannels++;
			channelToConnectioPool.put(channel, suggestedSlot);
			return channel;
		} else {
			/*
			 * Create a channel on the default channel.
			 */
			try {
				return (ChannelExec) defaultSession.openChannel("exec"); //$NON-NLS-1$
			} catch (JSchException e) {
				throw new RemoteConnectionException(Messages.Connection_CreateExecChannel_FailedCreateNewExecChannel, e);
			}
		}
	}

	/**
	 * Remove a channel from the pool, leaving the slot available for another channel.
	 * @param channel
	 */
	protected void releaseChannel(Channel channel) {
		/*
		 * The channel may or may not be in the connection pool, depending how it was created.
		 * Ant any case, always disconnect the channel.
		 */
		channel.disconnect();
		ConnectionSlot slot = (ConnectionSlot) channelToConnectioPool.remove(channel);
		if (slot != null) {
			slot.numberUsedChannels--;
		}
	}

	/**
	 * Create a forwarding from a remote port to a local port.
	 * @param localPort
	 * @param addressOnRemoteHost
	 * @param portOnRemoteHost
	 * @return
	 * @throws RemoteConnectionException
	 */
	protected RemoteTunnel createTunnel(int localPort, String addressOnRemoteHost, int portOnRemoteHost)
			throws RemoteConnectionException, LocalPortBoundException {
		RemoteTunnel tunnel = new RemoteTunnel(localPort, portOnRemoteHost, addressOnRemoteHost);
		if (tunnels.contains(tunnel)) {
			throw new LocalPortBoundException(Messages.Connection_CreateTunnel_TunnelPortAlreadyAlloced);
		}

		try {
			defaultSession.setPortForwardingL(tunnel.getLocalPort(), tunnel.getAddressOnRemoteHost(), tunnel
					.getPortOnRemoteHost());
		} catch (JSchException e) {
			if(e.getMessage().matches("PortForwardingL: local port .* is already registered.")) { //$NON-NLS-1$
				// Selected local port is already bound.
				throw new LocalPortBoundException(Messages.Connection_CreateTunnel_TunnelPortAlreadyAlloced);
			}
			throw new RemoteConnectionException(Messages.Connection_CreateTunnel_FailedCreateTunnel, e);
		}

		tunnels.add(tunnel);
		return tunnel;
	}

	/**
	 * Release the forwarding of the remote port.
	 * @param tunnel
	 * @throws RemoteConnectionException
	 */
	protected void releaseTunnel(RemoteTunnel tunnel) throws RemoteConnectionException {
		if (!tunnels.contains(tunnel)) {
			throw new RemoteConnectionException(Messages.Connection_ReleaseTunnel_PortNotAllocedForTunnel);
		}

		try {
			RemoteTunnel remoteTunnel = tunnel;
			defaultSession.delPortForwardingL(remoteTunnel.getLocalPort());
		} catch (JSchException e) {
			throw new RemoteConnectionException(Messages.Connection_ReleaseTunnel_FailedRemoveTunnel, e);
		}

		tunnels.remove(tunnel);
	}

	/**
	 * Gets the SFTP channel that may be used by the internal implementation to do file system operations
	 * on the remote host.
	 * @return
	 */
	protected ChannelSftp getDefaultSFTPChannel() {
		return sftpChannel;
	}


	protected synchronized int createNextPIID() {
		return ++nextInternalPID % Integer.MAX_VALUE;
	}

	protected synchronized void setPID(int piid, int pid) {
		// Look for the object which key is PIID.
		KillableExecution rce = (KillableExecution) activeProcessTable.get(new Integer(piid));
		if (rce != null) {
			// Process could be already finished and removed fro mthe table.
			rce.setPID(pid);
		}
	}

	protected synchronized void registerObservedExecution(IRemoteOperation operation) {
		if (operation instanceof KillableExecution) {
			KillableExecution killableExecution = (KillableExecution) operation;
			activeProcessTable.put(new Integer(killableExecution.getInternaID()), killableExecution);
			executionObserver.newCommand();
		}
	}

	protected synchronized void unregisterObservedExecution(IRemoteOperation operation) {
		if (operation instanceof KillableExecution) {
			KillableExecution killableExecution = (KillableExecution) operation;
			activeProcessTable.remove(new Integer(killableExecution.getInternaID()));
		}
	}

	protected String getKillablePrefix(KillableExecution execution) {
		return controlChannel.getKillablePrefix(execution.getInternaID());
	}

	/**
	 * Sends a KILL signal to the remote killable execution.
	 * @param execution
	 */
	protected void killExecution(KillableExecution execution) {
		controlChannel.killRemoteProcess(execution.getPID());
	}

	/**
	 * Open a new connection to the remote host and add this connection to the pool.
	 * @return A pool entry.
	 * @throws RemoteConnectionException
	 */
	private ConnectionSlot createConnecitonSlot() throws RemoteConnectionException {

		/*
		 * Create a Jsch session, with the same authentication values as the default session.
		 */
		Session newSession = null;
		try {
			newSession = jsch.getSession(username, hostname, port);
			newSession.setUserInfo(sshuserinfo);
			setSessionCipherType(newSession);
		} catch (JSchException e) {
			throw new RemoteConnectionException(Messages.Connection_CreateConnectionSlot_FailedCreateNewSession, e);
		}

		/*
		 * Connect to remote host.
		 */
		try {
			newSession.connect(timeout);
		} catch (JSchException e) {
			throw new RemoteConnectionException(Messages.Connection_CreateConnectionSlot_FailedConnectNewSession, e);
		}

		/*
		 * Create slot and add to the pool.
		 */
		ConnectionSlot slot = new ConnectionSlot(newSession);
		connectionPool.add(slot);
		return slot;
	}

	/**
	 * Select a cipher for the session based on the cipherType attribute
	 *
	 * @param session Session that will have its cipher altered.
	 */
	private void setSessionCipherType(Session session) {
		/*
		 * If the user selected a cipher other than the default, setup the cipher
		 *
		 */
		// TODO: Let the user select a list of ciphers, instead of only one
		if(!cipherType.equals(CipherTypes.CIPHER_DEFAULT)) {

			// Verify if the cipher is supported. Throw an exception if it isnt.
			if(!CipherTypes.getCipherTypesMap().containsKey(cipherType)) {
				// TODO: Throw a real exception, not a runtime one
				throw new RuntimeException(Messages.Connection_SetCipherType_CipherNotSupported);
			}

			// Set the session's cipher
			Hashtable config=new Hashtable();
			config.put("cipher.s2c", cipherType); //$NON-NLS-1$
			config.put("cipher.c2s", cipherType); //$NON-NLS-1$

			session.setConfig(config);
		}
	}

	/**
	 * Performs a sanity test to make sure that the connection is alive and has a valid state.
	 * <p>
	 * The connection may get dropped due some external interference, like loosing physical
	 * access to the remote machine.
	 * Or the connection may drop some channel, as it may be caused by a misbehavior
	 * of the remote SSH server or by a bug in the local SSH implementation.
	 * <p>
	 * If some problem is detected, then an {@link RemoteConnectionException} is thrown.
	 * Else, the method returns.
	 *
	 * @throws RemoteConnectionException The connection was entirely dropped or some channel got lost.
	 */
	protected void test() throws RemoteConnectionException {
		/*
		 * Check all SSH sessions
		 */
		Iterator iterator = connectionPool.iterator();
		while (iterator.hasNext()) {
			ConnectionSlot slot = (ConnectionSlot) iterator.next();
			if (! slot.session.isConnected()) {
				throw new RemoteConnectionException("SSH connection to remote host was lost");
			}
		}

		/*
		 * Check SFTP channel.
		 */
		if (! sftpChannel.isConnected()) {
			throw new RemoteConnectionException("SFTP connection to remote host was lost");
		}

		/*
		 * Check control channel.
		 */
		if (! controlChannel.shell.isConnected()) {
			throw new RemoteConnectionException("Control channel connection to remote host was lost");
		}

	}
}