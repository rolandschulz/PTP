package org.eclipse.ptp.rdt.sync.unison.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;


import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.unison.core.IRemoteSyncConnection;
import org.eclipse.ptp.rdt.sync.unison.core.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.unison.core.SyncFileFilter;
import org.eclipse.ptp.remote.core.IRemoteConnection;

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
	public void syncRemoteToLocal(IProgressMonitor monitor) throws RemoteSyncException {
		synchronize(monitor);	
	}
	private void synchronize(IProgressMonitor monitor){		
		String[] command = {"unison", "-batch", localDirectory, "ssh://" + connection.getUsername() + "@" + connection.getAddress() + "/" + remoteDirectory};
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
		try{
			Process p = Runtime.getRuntime().exec(tempArray.toArray(new String[0]));
			p.waitFor();
		} catch (IOException e){
			e.printStackTrace();
		} catch (InterruptedException e){
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
}

