/******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *     Roland Schulz, University of Tennessee
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.internal.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.core.AuthToken;
import org.eclipse.ptp.remotetools.core.IRemoteConnection;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteOperation;
import org.eclipse.ptp.remotetools.core.KeyAuthToken;
import org.eclipse.ptp.remotetools.core.PasswdAuthToken;
import org.eclipse.ptp.remotetools.core.messages.Messages;
import org.eclipse.ptp.remotetools.exception.LocalPortBoundException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.internal.common.RemoteTunnel;
import org.eclipse.ptp.remotetools.internal.core.ConnectionProperties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SocketFactory;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * The connection to a remote host. Manages a pool of connection used to open
 * execution channels, since some SSH server showed to impose a limit of
 * execution channels per connection.
 * 
 * @author Richard Maciel and Daniel Felix Ferber.
 */
public class Connection implements IRemoteConnection {
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
	 * This class is required by the JSch library.
	 * 
	 * JSch will call {@link #promptKeyboardInteractive} until the password is
	 * correct or it times out. We only allow it to try once, then return null
	 * in order to speed up the timeout.
	 * 
	 * TODO: this should prompt the user for a password if {@link
	 * @promptKeyboardInteractive} is called twice, since their password is
	 * wrong for some reason.
	 * 
	 * @author Richard Maciel
	 * 
	 */
	private class SSHUserInfo implements UserInfo, UIKeyboardInteractive {
		private String password;
		private String passphrase;
		private boolean isPasswdBased;
		private boolean firstTry = true;

		private SSHUserInfo() {
		}

		public String getPassphrase() {
			return passphrase;
		}

		public String getPassword() {
			if (firstTry) {
				firstTry = false;
				return password;
			}
			return null;
		}

		public String[] promptKeyboardInteractive(final String destination, final String name, final String instruction,
				final String[] prompt, final boolean[] echo) {
			if (prompt.length != 1 || echo[0] != false || password == null) {
				return null;
			}
			String[] response = new String[1];
			response[0] = password;
			if (firstTry) {
				firstTry = false;
				return response;
			}
			return null;
		}

		public boolean promptPassphrase(String message) {
			return !isPasswdBased;
		}

		public boolean promptPassword(String message) {
			return isPasswdBased;
		}

		public boolean promptYesNo(String str) {
			// Always accept host identity
			return true;
		}

		public void reset() {
			firstTry = true;
		}

		public void setPassphrase(String passphrase) {
			this.passphrase = passphrase;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public void setUsePassword(boolean usePassword) {
			this.isPasswdBased = usePassword;
		}

		public void showMessage(String message) {
		}
	}

	/**
	 * By default the Jsch connect method ignores the timeout argument which
	 * prevents any way of interrupting the connection (e.g. with a progress
	 * monitor). We use a proxy so that we can connect to a remote host with a
	 * timeout.
	 */
	private class SSHProxy implements Proxy {
		private final Socket fSocket = new Socket();

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.jcraft.jsch.Proxy#connect(com.jcraft.jsch.SocketFactory,
		 * java.lang.String, int, int)
		 */
		public void connect(SocketFactory socket_factory, String host, int port, int timeout) throws Exception {
			InetSocketAddress addr = new InetSocketAddress(host, port);
			fSocket.connect(addr, timeout);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.jcraft.jsch.Proxy#getInputStream()
		 */
		public InputStream getInputStream() {
			try {
				return fSocket.getInputStream();
			} catch (IOException e) {
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.jcraft.jsch.Proxy#getOutputStream()
		 */
		public OutputStream getOutputStream() {
			try {
				return fSocket.getOutputStream();
			} catch (IOException e) {
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.jcraft.jsch.Proxy#getSocket()
		 */
		public Socket getSocket() {
			return fSocket;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.jcraft.jsch.Proxy#close()
		 */
		public void close() {
			try {
				fSocket.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	/**
	 * Data structure to access the ssh library.
	 */
	private final IJSchService jsch = RemotetoolsPlugin.getDefault().getJSchService();
	/**
	 * A connection to the remote host. The default connections is always
	 * created, but more connections may be added to the pool on demand.
	 */
	private Session defaultSession;
	private String fUsername;
	private String fHostname;
	private int fPort;
	private int fTimeout;
	private String fCipherType;

	private final SSHUserInfo sshuserinfo = new SSHUserInfo();

	/**
	 * The execution managers created for this connection.
	 */
	private IRemoteExecutionManager executionManager = null;
	/**
	 * Tunnels to remote host.
	 */
	private final Set<RemoteTunnel> tunnels = new HashSet<RemoteTunnel>();

	/**
	 * sftp channel pool shared by all executions managers and file tools.
	 */
	private static final int SFTP_POOLSIZE = 3;

	private final ArrayBlockingQueue<ChannelSftp> sftpChannelPool = new ArrayBlockingQueue<ChannelSftp>(SFTP_POOLSIZE);

	/**
	 * Hashtable that keeps all remote executions that can be killed. The table
	 * is indexed by Internal PID.
	 */
	private final Hashtable<Integer, KillableExecution> activeProcessTable = new Hashtable<Integer, KillableExecution>();
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
	private final ArrayList<ConnectionSlot> connectionPool = new ArrayList<ConnectionSlot>();

	/**
	 * Maps a channel to the connection where it was created.
	 */
	private final HashMap<Channel, ConnectionSlot> channelToConnectioPool = new HashMap<Channel, ConnectionSlot>();

	/**
	 * Locks used on synchronized operations.
	 */
	protected ConnectionLocks connectionLocks;

	private RemotePortForwardingPool forwardingPool;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteConnection#connect(org.eclipse
	 * .ptp.remotetools.core.AuthToken, java.lang.String, int, java.lang.String,
	 * int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public synchronized void connect(AuthToken authToken, String hostname, int port, String cipherType, int timeout,
			IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);

		try {
			this.nextInternalPID = 0;

			fUsername = authToken.getUsername();

			// Convert information for the UserInfo class used by JSch
			if (authToken instanceof PasswdAuthToken) {
				sshuserinfo.setUsePassword(true);
				sshuserinfo.setPassword(((PasswdAuthToken) authToken).getPassword());
			} else if (authToken instanceof KeyAuthToken) {
				KeyAuthToken token = (KeyAuthToken) authToken;
				sshuserinfo.setUsePassword(false);
				sshuserinfo.setPassphrase(token.getPassphrase());
				try {
					jsch.getJSch().addIdentity(token.getKeyPath().getAbsolutePath());
				} catch (JSchException e) {
					throw new RemoteConnectionException(e.getMessage());
				}
			} else {
				throw new RuntimeException(Messages.Connection_AuthenticationTypeNotSupported);
			}

			fHostname = hostname;

			fPort = port;
			if (fPort == 0) {
				fPort = ConnectionProperties.defaultPort;
			}

			fCipherType = cipherType;
			if (fCipherType == null) {
				fCipherType = CipherTypes.CIPHER_DEFAULT;
			}

			fTimeout = timeout;
			if (fTimeout == 0) {
				fTimeout = ConnectionProperties.defaultTimeout;
			}

			/*
			 * Create session.
			 */
			try {
				defaultSession = jsch.createSession(fHostname, fPort, fUsername);
				sshuserinfo.reset();
				defaultSession.setUserInfo(sshuserinfo);
				defaultSession.setServerAliveInterval(300000);
				defaultSession.setServerAliveCountMax(6);
				defaultSession.setProxy(new SSHProxy());
			} catch (JSchException e) {
				disconnect();
				throw new RemoteConnectionException(e.getMessage());
			}

			setSessionCipherType(defaultSession);

			/*
			 * Connect to remote host. Try connecting at 1 sec intervals to
			 * allow the connection to be cancelled. Note that a timeout of 0
			 * implies infinite (or until cancelled).
			 */
			int connTimeout = fTimeout;
			int tryTimeout = connTimeout > 0 ? 1000 : connTimeout;

			while (!defaultSession.isConnected() && !progress.isCanceled() && connTimeout >= 0) {
				try {
					defaultSession.connect(1000);
				} catch (JSchException e) {
					connTimeout -= tryTimeout;
				} catch (Exception e) {
					disconnect();
					throw new RemoteConnectionException(e.getMessage());
				}
			}

			if (connTimeout < 0) {
				disconnect();
				throw new RemoteConnectionException(NLS.bind(Messages.Connection_Connect_FailedConnect, fHostname));
			}

			if (progress.isCanceled()) {
				disconnect();
				throw new RemoteConnectionException(Messages.Connection_Operation_cancelled_by_user);
			}

			/*
			 * Create control execution channel.
			 */
			if (controlChannel == null) {
				controlChannel = new ControlChannel(this);
			}
			try {
				controlChannel.open(progress.newChild(10));
			} catch (RemoteConnectionException e) {
				disconnect();
				throw new RemoteConnectionException(e.getMessage());
			}

			/*
			 * Create sft pool.
			 */
			try {
				for (int i = 0; i < SFTP_POOLSIZE; i++) {
					ChannelSftp sftp = (ChannelSftp) defaultSession.openChannel("sftp"); //$NON-NLS-1$
					sftp.connect();
					boolean bInterrupted = Thread.interrupted();
					while (sftp != null) {
						try {
							sftpChannelPool.put(sftp);
							sftp = null;
						} catch (InterruptedException e) {
							// System.out.println("Connection.connect: InterruptedException ignored");
							bInterrupted = true;
						}
					}
					if (bInterrupted)
						Thread.currentThread().interrupt(); // set interrupt
															// state
				}
			} catch (JSchException e) {
				throw new RemoteConnectionException(e.getMessage());
			}

			/*
			 * The default session cannot be fully used for connection pool,
			 * since some channels are already using pty.
			 */
			ConnectionSlot slot = new ConnectionSlot(defaultSession, ConnectionProperties.initialDefaultSessionLoad);
			connectionPool.add(slot);

			if (forwardingPool == null) {
				forwardingPool = new RemotePortForwardingPool(this);
			}

			/*
			 * Reset cancel flag
			 */
			if (executionManager != null) {
				executionManager.resetCancel();
			}

			/*
			 * Create observer thread and start it
			 */
			executionObserver = new ExecutionObserver(this);
			executionObserver.start();
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.IRemoteConnection#getRemoteExecutionManager()
	 */
	public synchronized IRemoteExecutionManager createRemoteExecutionManager() throws RemoteConnectionException {
		if (executionManager == null) {
			executionManager = new ExecutionManager(this);
		}
		return executionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.IRemoteConnection#disconnect()
	 */
	public synchronized void disconnect() {
		/*
		 * First, cancel all ongoing executions and tunnels created by managers,
		 * by closing the execution manager.
		 */
		if (executionManager != null) {
			executionManager.close();
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

		boolean bInterrupted = Thread.interrupted();
		while (!sftpChannelPool.isEmpty()) {
			try {
				sftpChannelPool.take().disconnect();
			} catch (InterruptedException e) {
				bInterrupted = true;
			}
		}
		sftpChannelPool.clear();
		if (bInterrupted) {
			Thread.currentThread().interrupt(); // set interrupt state
		}

		/*
		 * Close all connections from the pool.
		 */
		for (ConnectionSlot slot : connectionPool) {
			slot.session.disconnect();
		}
		connectionPool.clear();

		/*
		 * Reset internal state variables.
		 */
		defaultSession = null;

		if (forwardingPool != null) {
			forwardingPool.disconnect();
		}
	}

	public String getHostname() {
		return fHostname;
	}

	public int getPort() {
		return fPort;
	}

	public Session getSession() {
		return defaultSession;
	}

	/*
	 * public String getPassword() { return password; }
	 */

	/**
	 * Gets the SFTP channel that may be used by the internal implementation to
	 * do file system operations on the remote host. Gets a new channel if the
	 * default has been closed for some reason.
	 * 
	 * @return default channel
	 * @throws RemoteConnectionException
	 */
	public ChannelSftp getSFTPChannel() throws RemoteConnectionException {

		// System.out.println("channelPool.size() -> " +
		// sftpChannelPool.size());
		// System.out.println("thread: "+Thread.currentThread().getName());
		try {
			// if (sftpChannelPool.size() == 0)
			// System.err.println("SFTPChannelPool currently empty. Thread will have to wait till other thread releases a channel.");
			ChannelSftp channel = null;
			boolean bInterrupted = Thread.interrupted();
			while (channel == null) {
				try {
					channel = sftpChannelPool.take();
				} catch (InterruptedException e) {
					// System.out.println("getSFTPChannel: InterruptedException ignored");
					bInterrupted = true;
				}
			}
			if (bInterrupted) {
				Thread.currentThread().interrupt(); // set interrupt state
			}
			// System.out.println("channel: "+channel.isConnected()+","+channel.isEOF());
			if (!channel.isConnected()) {
				channel = (ChannelSftp) defaultSession.openChannel("sftp"); //$NON-NLS-1$
				channel.connect();
			}
			return channel;
		} catch (JSchException e) {
			throw new RemoteConnectionException(Messages.Connection_Connect_FailedCreateSFTPConnection, e);
		}
	}

	public int getTimeout() {
		return fTimeout;
	}

	public String getUsername() {
		return fUsername;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.IRemoteConnection#isConnected()
	 */
	public synchronized boolean isConnected() {
		if (defaultSession == null) {
			return false;
		}

		try {
			test();
		} catch (RemoteConnectionException e) {
			return false;
		}

		return defaultSession.isConnected();
	}

	/**
	 * Releases the SFTP channel
	 * 
	 * @param channel
	 *            channel to release
	 */
	public void releaseSFTPChannel(ChannelSftp channel) {
		// System.out.println("release, channelPool.size() -> " +
		// sftpChannelPool.size());
		// System.out.println("thread: "+Thread.currentThread().getName());

		boolean bInterrupted = Thread.interrupted();
		while (channel != null) {
			try {
				sftpChannelPool.put(channel);
				channel = null; // Successful returned
			} catch (InterruptedException e) {
				// System.out.println("releaseSFTPChannel: InterruptedException ignored");
				bInterrupted = true;
			}
		}
		if (bInterrupted) {
			Thread.currentThread().interrupt(); // set interrupt state
		}
	}

	/**
	 * Open a new connection to the remote host and add this connection to the
	 * pool.
	 * 
	 * @return A pool entry.
	 * @throws RemoteConnectionException
	 */
	private ConnectionSlot createConnectionSlot() throws RemoteConnectionException {

		/*
		 * Create a Jsch session, with the same authentication values as the
		 * default session.
		 */
		Session newSession = null;
		try {
			newSession = jsch.createSession(fHostname, fPort, fUsername);
			sshuserinfo.reset();
			newSession.setUserInfo(sshuserinfo);
			newSession.setServerAliveInterval(300000);
			newSession.setServerAliveCountMax(6);
			setSessionCipherType(newSession);
		} catch (JSchException e) {
			throw new RemoteConnectionException(Messages.Connection_CreateConnectionSlot_FailedCreateNewSession, e);
		}

		/*
		 * Connect to remote host.
		 */
		try {
			newSession.connect(fTimeout);
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
	 * @param session
	 *            Session that will have its cipher altered.
	 */
	private void setSessionCipherType(Session session) {
		/*
		 * If the user selected a cipher other than the default, setup the
		 * cipher
		 */
		// TODO: Let the user select a list of ciphers, instead of only one
		if (!fCipherType.equals(CipherTypes.CIPHER_DEFAULT)) {

			// Verify if the cipher is supported. Throw an exception if it isnt.
			if (!CipherTypes.getCipherTypesMap().containsKey(fCipherType)) {
				// TODO: Throw a real exception, not a runtime one
				throw new RuntimeException(Messages.Connection_SetCipherType_CipherNotSupported);
			}

			// Set the session's cipher
			Hashtable<String, String> config = new Hashtable<String, String>();
			config.put("cipher.s2c", fCipherType); //$NON-NLS-1$
			config.put("cipher.c2s", fCipherType); //$NON-NLS-1$

			session.setConfig(config);
		}
	}

	/**
	 * Creates a new execution channel. The channel may or may not be managed by
	 * the connection pool. All execution channels that require PTY must be
	 * managed by the pool, except really special cases, like the control
	 * channel and default SFTP channel. Channels without PTY may be managed, by
	 * it is recommended no to be. This will allocate them into the default
	 * session, since an unlimited number of not PTY channels can be allocated
	 * into the default session.
	 * 
	 * @param isInConnectionPool
	 *            As described.
	 * @return The requested execution channel.
	 * @throws RemoteConnectionException
	 *             If the allocation of new channel failed of if it was not
	 *             possible to create a new ssh session for the new channel.
	 */
	protected ChannelExec createExecChannel(boolean isInConnectionPool) throws RemoteConnectionException {
		if (isInConnectionPool) {
			/*
			 * Search for the first available connection slot or create a new
			 * one if all are full.
			 */
			ConnectionSlot suggestedSlot = null;
			for (ConnectionSlot slot : connectionPool) {
				if (slot.numberUsedChannels < ConnectionProperties.maxChannelsPerConnection) {
					suggestedSlot = slot;
					break;
				}
			}
			if (suggestedSlot == null) {
				suggestedSlot = createConnectionSlot();
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

	protected synchronized int createNextPIID() {
		return ++nextInternalPID % Integer.MAX_VALUE;
	}

	/**
	 * Create a forwarding from a remote port to a local port.
	 * 
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
			defaultSession.setPortForwardingL(tunnel.getLocalPort(), tunnel.getAddressOnRemoteHost(), tunnel.getPortOnRemoteHost());
		} catch (JSchException e) {
			if (e.getMessage().matches("PortForwardingL: local port .* is already registered.")) { //$NON-NLS-1$
				// Selected local port is already bound.
				throw new LocalPortBoundException(Messages.Connection_CreateTunnel_TunnelPortAlreadyAlloced);
			}
			throw new RemoteConnectionException(Messages.Connection_CreateTunnel_FailedCreateTunnel, e);
		}

		tunnels.add(tunnel);
		return tunnel;
	}

	protected Hashtable<Integer, KillableExecution> getActiveProcessTable() {
		return activeProcessTable;
	}

	protected RemotePortForwardingPool getForwardingPool() {
		return forwardingPool;
	}

	protected String getKillablePrefix(KillableExecution execution) {
		return controlChannel.getKillablePrefix(execution.getInternaID());
	}

	/**
	 * Sends a KILL signal to the remote killable execution.
	 * 
	 * @param execution
	 */
	protected void killExecution(KillableExecution execution) {
		controlChannel.killRemoteProcess(execution.getPID());
	}

	protected void registerObservedExecution(IRemoteOperation operation) {
		if (operation instanceof KillableExecution) {
			KillableExecution killableExecution = (KillableExecution) operation;
			synchronized (getActiveProcessTable()) {
				getActiveProcessTable().put(new Integer(killableExecution.getInternaID()), killableExecution);
			}
		}
	}

	/**
	 * Remove a channel from the pool, leaving the slot available for another
	 * channel.
	 * 
	 * @param channel
	 */
	protected void releaseChannel(Channel channel) {
		/*
		 * The channel may or may not be in the connection pool, depending how
		 * it was created. Ant any case, always disconnect the channel.
		 */
		channel.disconnect();
		ConnectionSlot slot = channelToConnectioPool.remove(channel);
		if (slot != null) {
			slot.numberUsedChannels--;
		}
	}

	/**
	 * Release the forwarding of the remote port.
	 * 
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

	protected void setPID(int piid, int pid) {
		// Look for the object which key is PIID.
		synchronized (getActiveProcessTable()) {
			KillableExecution exec = getActiveProcessTable().get(new Integer(piid));
			if (exec != null) {
				// Process could be already finished and removed fro mthe table.
				exec.setPID(pid);
			}
		}
	}

	/**
	 * Performs a sanity test to make sure that the connection is alive and has
	 * a valid state.
	 * <p>
	 * The connection may get dropped due some external interference, like
	 * loosing physical access to the remote machine. Or the connection may drop
	 * some channel, as it may be caused by a misbehavior of the remote SSH
	 * server or by a bug in the local SSH implementation.
	 * <p>
	 * If some problem is detected, then an {@link RemoteConnectionException} is
	 * thrown. Else, the method returns.
	 * 
	 * @throws RemoteConnectionException
	 *             The connection was entirely dropped or some channel got lost.
	 */
	protected synchronized void test() throws RemoteConnectionException {
		/*
		 * Check all SSH sessions
		 */
		for (ConnectionSlot slot : connectionPool) {
			if (!slot.session.isConnected()) {
				throw new RemoteConnectionException(Messages.Connection_0);
			}
		}

		/*
		 * Check control channel.
		 */
		if (controlChannel == null || !controlChannel.isConnected()) {
			throw new RemoteConnectionException(Messages.Connection_1);
		}

	}

	protected void unregisterObservedExecution(IRemoteOperation operation) {
		if (operation instanceof KillableExecution) {
			KillableExecution killableExecution = (KillableExecution) operation;
			synchronized (getActiveProcessTable()) {
				getActiveProcessTable().remove(new Integer(killableExecution.getInternaID()));
			}
		}
	}
}