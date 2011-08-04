package org.eclipse.ptp.rdt.sync.unison.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


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
//		//command to be run excluding exclusions.
//		String[] commandLtoR = {"rsync", "-avze","ssh -p " + Integer.toString(connection.getPort()),localDirectory + "/",connection.getUsername() + "@" + connection.getAddress() + ":" + remoteDirectory/*, "--exclude"*/};
//		ArrayList<String> cLR = new ArrayList<String>();
//		
//		//load arguments from original command into ArrayList
//		for(String arg : commandLtoR){
//			cLR.add(arg);
//		}
//		
//		
//		String[] filesToExclude = new String[getFilesToBeExcluded().size()];
//		filesToExclude = getFilesToBeExcluded().toArray(filesToExclude);
//		
//		//add exclusions from filesToExclude to cLR, which then contains all arguments.
//		for(String argtoExclude : filesToExclude){
//			cLR.add("--exclude");
//			cLR.add(argtoExclude);
//		}
//		
//		//execute the command
//		String[] tempArray = new String[cLR.size()];
//		tempArray = cLR.toArray(tempArray);
//		try {
//			Process p = Runtime.getRuntime().exec(tempArray);
//			p.waitFor();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	public void syncRemoteToLocal(IProgressMonitor monitor) throws RemoteSyncException {
//		String[] commandRtoL = {"rsync", "--ignore-existing", "-avze" ,"ssh -p " + Integer.toString(connection.getPort()), connection.getUsername() + "@" + connection.getAddress() + ":" + remoteDirectory + "/", localDirectory};
//		
//		try {
//			Process p = Runtime.getRuntime().exec(commandRtoL);
//			p.waitFor();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
	}
	private void synchronize(IProgressMonitor monitor){
		ArrayList<String> argList = new ArrayList<String>();
		
		String[] filesToExclude = new String[getFilesToBeExcluded().size()];
		filesToExclude = getFilesToBeExcluded().toArray(filesToExclude);
		
		//add exclusions from filesToExclude to cLR, which then contains all arguments.
		for(String argtoExclude : filesToExclude){
			argList.add(argtoExclude + ",");
		}
		
		String tempString ="";
		for(String string : argList){
			 tempString = tempString.concat(string);
		}
		String[] command = {"unison", "-batch","-ignore", "Path {" + tempString + "}", localDirectory, "ssh://" + connection.getUsername() + "@" + connection.getAddress() + "/" + remoteDirectory};
		
		
		try{
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
		} catch (IOException e){
			e.printStackTrace();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}
	
	/*  Returns an ArrayList of strings containing the file exclusion arguments */
	private ArrayList<String> getFilesToBeExcluded(){
		File dir = new File(localDirectory);
		ArrayList<String> filesToBeExcluded = new ArrayList<String>();
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

