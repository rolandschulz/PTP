package org.eclipse.ptp.rdt.sync.unison.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;


import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.unison.core.CommandRunner;
import org.eclipse.ptp.rdt.sync.unison.core.CommandRunner.CommandResults;
import org.eclipse.ptp.rdt.sync.unison.core.IRemoteSyncConnection;
import org.eclipse.ptp.rdt.sync.unison.core.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.unison.core.SyncFileFilter;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

public class UnisonRemoteSyncConnection implements IRemoteSyncConnection{
//	private final static String remoteProjectName = "eclipse_auto";
	public final static String ID = "org.eclipse.ptp.rdt.sync.unison.core.UnisonRemoteSyncConnection";//$NON-NLS-1$
	private final IRemoteConnection connection;
	private final String localDirectory;
	private final String remoteDirectory;
	private final SyncFileFilter fileFilter;

	
/* Create a remote sync connection using Unison. */
	public UnisonRemoteSyncConnection(IRemoteConnection conn, String localDir, String remoteDir, SyncFileFilter filter, IProgressMonitor monitor){
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
		synchronize(monitor);
	}
	private URL getFakeSSHLocation() throws RemoteSyncException {
		URL binFolder = null;
		try {
			binFolder = FileLocator.find(new URL("platform:/plugin/org.eclipse.ptp.rdt.sync.unison.core/bin"));
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
		synchronize(monitor);	
	}
	private void synchronize(IProgressMonitor monitor){	
		try{
			ServerSocket serverSocket = new ServerSocket(0);
			int portnum = serverSocket.getLocalPort();

			String[] command = {"unison", "-batch", localDirectory, "ssh://" + connection.getUsername() + "@" + connection.getAddress() + "/" + remoteDirectory, "-sshcmd", getFakeSSHLocation().getPath() + "org/eclipse/ptp/rdt/sync/unison/core/FakeSSH.sh", "-sshargs", Integer.toString(portnum)};
			ArrayList<String> tempArray = new ArrayList<String>(Arrays.asList(command));
			//Add files to exclude to -ignore argument
			LinkedList<String> filesToExclude = getFilesToBeExcluded();
			if(!filesToExclude.isEmpty()){
				String tempString = filesToExclude.removeFirst();			
				for(String argtoExclude : filesToExclude){
					tempString = tempString.concat("," + argtoExclude);
				}
				tempArray.add("-ignore");
				tempArray.add("Path {" + tempString + "}");
			}	
		String[] finalCommand = new String[tempArray.size()];
		finalCommand = tempArray.toArray(command);
		executeLocalCommandWithConnection(finalCommand, serverSocket);
		serverSocket.close();
		
		} catch (IOException e){
			e.printStackTrace();
		} catch (InterruptedException e){
			e.printStackTrace();
		} catch (RemoteSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*  Returns an ArrayList of strings containing the file exclusion arguments */
	private LinkedList<String> getFilesToBeExcluded(){
		File dir = new File(localDirectory);
		LinkedList<String> filesToBeExcluded = new LinkedList<String>();
		String[] fileList = dir.list();
		for(String file : fileList){
			if(fileFilter.shouldIgnore(file)){
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
	public CommandResults executeLocalCommandWithConnection(String[] localCommand, ServerSocket serverSocket) throws RemoteSyncException,
	InterruptedException, IOException, RemoteConnectionException {

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

		CommandResults commandResults = CommandRunner.executeRemoteCommand(connection, remoteCommand, null, null, socketInput,
				socketOutput, null);

		p.waitFor();
		clientSocket.close();
		return commandResults;
	}
}

