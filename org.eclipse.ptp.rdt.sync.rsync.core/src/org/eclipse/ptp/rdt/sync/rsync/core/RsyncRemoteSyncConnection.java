package org.eclipse.ptp.rdt.sync.rsync.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.core.IRemoteSyncConnection;
import org.eclipse.ptp.rdt.sync.core.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.rsync.core.CommandRunner.CommandResults;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

public class RsyncRemoteSyncConnection implements IRemoteSyncConnection {
	// private final static String remoteProjectName = "eclipse_auto";
	public final static String ID = "org.eclipse.ptp.rdt.sync.rsync.core.RsyncRemoteSyncConnection";//$NON-NLS-1$
	private final IRemoteConnection connection;
	private final String localDirectory;
	private final String remoteDirectory;
	private final SyncFileFilter fileFilter;

	/* Create a remote sync connection using Rsync. */
	public RsyncRemoteSyncConnection(IRemoteConnection conn, String localDir, String remoteDir, SyncFileFilter filter,
			IProgressMonitor monitor) {
		connection = conn;
		localDirectory = localDir;
		remoteDirectory = remoteDir;
		fileFilter = filter;

	}

	/**
	 * @return the remoteDirectory
	 */
	public String getRemoteDirectory() {
		return remoteDirectory;
	}

	/**
	 * @return the localDirectory
	 */
	public String getLocalDirectory() {
		return localDirectory;
	}

	/**
	 * @return the connection (IRemoteConnection)
	 */
	public IRemoteConnection getConnection() {
		return connection;
	}

	public void close() {
		return;
	}

	public void syncLocalToRemote(IProgressMonitor monitor) throws RemoteSyncException {
		// command to be run excluding exclusions.
		String[] commandLtoR = { "rsync", "-avvvze", "java -cp " + getFakeSSHLocation().getPath() + " org.eclipse.ptp.rdt.sync.rsync.core.FakeSSH", localDirectory + "/",
				connection.getAddress() + ":" + remoteDirectory };
		ArrayList<String> cLR = new ArrayList<String>();

		// load arguments from original command into ArrayList
		for (String arg : commandLtoR) {
			cLR.add(arg);
		}

		String[] filesToExclude = new String[getFilesToBeExcluded().size()];
		filesToExclude = getFilesToBeExcluded().toArray(filesToExclude);

		// add exclusions from filesToExclude to cLR, which then contains all arguments.
		for (String argtoExclude : filesToExclude) {
			cLR.add("--exclude");
			cLR.add(argtoExclude);
		}

		// execute the command
		String[] command = new String[cLR.size()];
		command = cLR.toArray(command);

		try {
			executeLocalCommandWithConnection(command);

		} catch (IOException e) {
			throw new RemoteSyncException(e);
		} catch (InterruptedException e) {
			throw new RemoteSyncException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteSyncException(e);
		}
	}

	private URL getFakeSSHLocation() throws RemoteSyncException {
		URL binFolder = null;
		try {
			binFolder = FileLocator.find(new URL("platform:/plugin/org.eclipse.ptp.rdt.sync.rsync.core/bin"));
			if (binFolder != null) {
				binFolder = FileLocator.toFileURL(binFolder);
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (binFolder == null) {
			throw new RemoteSyncException("binary not found");
		}
		return binFolder;
	}

	public void syncRemoteToLocal(IProgressMonitor monitor) throws RemoteSyncException {
		// String[] commandRtoL = {"rsync", "--ignore-existing", "-avze" ,"java -cp" + FakeSSHLocation +
		// Integer.toString(connection.getPort()), connection.getUsername() + "@" + connection.getAddress() + ":" + remoteDirectory
		// + "/", localDirectory};
		String[] commandRtoL = { "rsync", "--ignore-existing", "-avvvze", "java -cp " + getFakeSSHLocation().getPath() + " org.eclipse.ptp.rdt.sync.rsync.core.FakeSSH",
				connection.getAddress() + ":" + remoteDirectory + "/", localDirectory };

		try {
			executeLocalCommandWithConnection(commandRtoL);

		} catch (IOException e) {
			throw new RemoteSyncException(e);
		} catch (InterruptedException e) {
			throw new RemoteSyncException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteSyncException(e);
		}
	}

	/* Returns an ArrayList of strings containing the file exclusion arguments */
	private ArrayList<String> getFilesToBeExcluded() {
		File dir = new File(localDirectory);
		ArrayList<String> filesToBeExcluded = new ArrayList<String>();
		String[] fileList = dir.list();
		for (String file : fileList) {
			if (fileFilter.shouldIgnore(file)) {
				filesToBeExcluded.add(file);
			}

		}
		return filesToBeExcluded;
	}

	public boolean pathFilter(String path) {
		return false;
	}

	public void pathChanged(IResourceDelta delta) throws RemoteSyncException {

	}

	public CommandResults executeLocalCommandWithConnection(String[] localCommand) throws RemoteSyncException,
			InterruptedException, IOException, RemoteConnectionException {

		ServerSocket serverSocket = new ServerSocket(6565); // Should be 0 (any free) and than communicated
		Process p = Runtime.getRuntime().exec(localCommand);
		Socket clientSocket = serverSocket.accept();

		InputStream socketInput = clientSocket.getInputStream();
		OutputStream socketOutput = clientSocket.getOutputStream();

		int chr;
		String remoteCommand = "";
		while ((chr = socketInput.read()) != '\n') { // using really slow unbuffered read - because it is only a single line and we
														// need to make sure not to read too much
			remoteCommand += (char) chr;
		}
		System.out.println(remoteCommand);

		CommandResults commandResults = CommandRunner.executeRemoteCommand(connection, remoteCommand, null, null, socketInput,
				socketOutput, null);

		p.waitFor();
		serverSocket.close();
		clientSocket.close();
		return commandResults;
	}
}
