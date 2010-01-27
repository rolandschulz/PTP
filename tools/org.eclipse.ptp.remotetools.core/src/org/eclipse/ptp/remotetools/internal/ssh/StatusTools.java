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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteStatusTools;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;


/**
 * Concrete class responsible for returning status data from the remote host
 * 
 * @author Richard Maciel
 *
 */
public class StatusTools implements IRemoteStatusTools {
	private ExecutionManager manager;
	private UserInformation userInfoCache;
	
	/**
	 * This class is responsible for caching information that is "stable" enough to be stored and retrieved
	 * instead of generated every time.  
	 *
	 * @author Richard Maciel
	 *
	 */
	class UserInformation {
		private Integer userID;
		private Set<Integer> groupIDSet;
		private String username;
		
		public UserInformation(Integer userID, Set<Integer> groupIDSet, String username) {
			this.userID = userID;
			this.groupIDSet = groupIDSet;
			this.username = username;
		}

		/**
		 * @return the userID
		 */
		public Integer getUserID() {
			return userID;
		}

		/**
		 * @return the groupIDSet
		 */
		public Set<Integer> getGroupIDSet() {
			return groupIDSet;
		}

		/**
		 * @return the username
		 */
		public String getUsername() {
			return username;
		}
	}
	
	protected StatusTools(ExecutionManager manager) {
		this.manager = manager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteStatusTools#getRemotePortsInUse(int)
	 */
	public Set<Integer> getRemotePortsInUse(int protocol) throws RemoteConnectionException, RemoteOperationException, CancelException {
		// Uses the following shell script to get a list of used ports in the
		// remote host:
		// cat /proc/net/<transport protocol>
		// Then, the data coming from the command is filtered using java commands equivalent to the
		// following shell command:
		// cat /proc/net/tcp | tail -n +2 | sed -r 's/^\s+//g' | cut -d ' ' -f 2 | cut -d ':' -f 2

		
		HashSet<Integer> portSet = new HashSet<Integer>(); 
		String protoStr = null, protoStr6 = null;
		if(protocol == IRemoteStatusTools.PROTO_UDP) {
			protoStr = "cat /proc/net/udp"; //$NON-NLS-1$
			protoStr6 = "cat /proc/net/udp6"; //$NON-NLS-1$
		} else if (protocol == IRemoteStatusTools.PROTO_TCP){
			protoStr = "cat /proc/net/tcp"; //$NON-NLS-1$
			protoStr6 = "cat /proc/net/tcp6"; //$NON-NLS-1$
		} 
		
		try {
			IRemoteExecutionTools remExecTools = manager.getExecutionTools(); 
			
			// Filter information from files.
			/*
			 * "protoOutput" and "proto6Output" are the 
			 * outputs of the above commands, WITHOUT the header 
			 * that is being wiped out by the "replaceFirst" 
			 * method. Thus, the resulting string
			 * "rawProcOutput" contains only valid connection
			 * entries for both ipv4 and ipv6 and must be
			 * totally processed on the following loop.
			 * 
			 */
			String protoOutput = remExecTools.executeWithOutput(protoStr).
				replaceFirst("^\\p{Space}*sl.*inode\\p{Space}*\\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
			String proto6Output = remExecTools.executeWithOutput(protoStr6).
				replaceFirst("^\\p{Space}*sl.*inode\\p{Space}*\\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
			
			String rawProcOutput = protoOutput.concat(proto6Output); 
			
			String [] rawProcLines = rawProcOutput.split("\n"); //$NON-NLS-1$
			
			for(int i=0; i < rawProcLines.length; i++) {
				// Trim the beginning of each line, and split the string using
				// whitespace as separator.
				String [] procFields = rawProcLines[i].trim().split(" "); //$NON-NLS-1$

				// Get the second field and split it using ':' as separator.
				// Get the second field.
				String [] addrFields = procFields[1].split(":"); //$NON-NLS-1$
				String allocedPort = addrFields[1];

				// Convert the value to an integer (it's on base 16) 
				// and put it into the set.
				portSet.add(Integer.valueOf(allocedPort, 16));
			}
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e);
		}
		
		return portSet;
	}

	private static int PASSWD_USERNAME_FIELD = 0;
	private static int PASSWD_USERID_FIELD = 2;
	private static int PASSWD_GROUPID_FIELD = 3;
	private static int PASSWD_HOMEDIR_FIELD = 5;
	
	private static int GROUP_GROUPID_FIELD = 2;
	private static int GROUP_USERLIST_FIELD = 3;
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteStatusTools#getGroupIDSet()
	 */
	public Set<Integer> getGroupIDSet() throws RemoteConnectionException, RemoteOperationException, CancelException {
//		 If there's already information on cache, use it
		if(userInfoCache == null) {
			userInfoCache = fetchRemoteUserInfo();
		}			
		return userInfoCache.getGroupIDSet();
	}

	/**
	 * An implementation of the {@link getGroupIDSet} method that only uses the cat command on the remote machine
	 * to extract desired data. Specifically, it retrieves information from the /etc/passwd looking and /etc/group files
	 * and extract the information about the groups of the user whose username comes from the {@link getUsername} method
	 * 
	 * @return
	 * @throws RemoteConnectionException
	 * @throws RemoteOperationException
	 * @throws CancelException
	 */
	private Set<Integer> getGroupIDSet_CatBased() throws RemoteConnectionException, RemoteOperationException, CancelException {
		String [] passwdFields = getPasswdFields(); 
		String username = passwdFields[PASSWD_USERNAME_FIELD].trim();
		String strPasswdGroupID = passwdFields[PASSWD_GROUPID_FIELD].trim();
		Set<Integer> groupIDSet = new HashSet<Integer>();
		
		// Put the the group from the passd file into the list (if not blank)
		if(!strPasswdGroupID.trim().equals("")) { //$NON-NLS-1$
			groupIDSet.add(new Integer(Integer.parseInt(strPasswdGroupID)));
		}
		
		/*
		 * Get the group file and search for the groups that the user belongs
		 * to.
		 */
		// Cat the /etc/group file and split it in lines
		IRemoteExecutionTools remExecTools = manager.getExecutionTools();
		try {
			String rawGroup = remExecTools.executeWithOutput("cat /etc/group"); //$NON-NLS-1$
			String [] rawGroupLines = rawGroup.split("\\n"); //$NON-NLS-1$
			
			for(int i=0; i < rawGroupLines.length; i++) {
				// Last field can be non-existant, so use a negative parameter in split to
				// make sure it becomes a field
				String [] groupFields = rawGroupLines[i].split(":", -1); //$NON-NLS-1$
				// Search for the username in the list of users of the group.
				// IF the username belongs to the group, insert it into the list.
				if(isUsernameInList(username, groupFields[GROUP_USERLIST_FIELD].split(","))) { //$NON-NLS-1$
					groupIDSet.add(new Integer(Integer.parseInt(groupFields[GROUP_GROUPID_FIELD].trim())));
				}
			}
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e);
		}
		// Returns the set
		return groupIDSet;
	}
	
	/**
	 * Given a list of usernames, verifies if user belongs to it.
	 * 
	 * @param username
	 * @param usernameList
	 * @return
	 */
	private boolean isUsernameInList(String username, String [] usernameList) {
		for(int i=0; i < usernameList.length; i++) {
			if(usernameList[i].trim().equals(username)) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteStatusTools#getUserID()
	 */
	public int getUserID() throws RemoteConnectionException, RemoteOperationException, CancelException {
		// If there's already information on cache, use it
		if(userInfoCache == null) {
			userInfoCache = fetchRemoteUserInfo();
		}			
		return userInfoCache.getUserID().intValue();
	}
	
	/**
	 * An implementation of the {@link getUserID} method that only uses the cat command on the remote machine
	 * to extract desired data. Specifically it reads information from the /etc/passwd file and parses it to
	 * retrieve the user id.
	 * 
	 * @return
	 * @throws RemoteConnectionException
	 * @throws RemoteOperationException
	 * @throws CancelException
	 */
	public int getUserID_CatBased() throws RemoteConnectionException, RemoteOperationException, CancelException {
//		 Get the field userid from the result of the getPasswdFields method
		// and return it.
		String strUserID = getPasswdFields()[PASSWD_USERID_FIELD]; 
		return Integer.parseInt(strUserID);
	}
	
	/**
	 * Get the line that has the user information from the passwd file
	 * 
	 * @return String [] A vector containing the fields or null if no user matches
	 * @throws RemoteOperationException 
	 * @throws CancelException 
	 * @throws RemoteConnectionException 
	 */
	private String [] getPasswdFields() throws RemoteOperationException, RemoteConnectionException, CancelException {
		String currentUsername = getUsername_WhoamiBased();
		IRemoteExecutionTools remExecTools = manager.getExecutionTools();
		
		try {
			// Get lines from the passwd
			String rawPasswd = remExecTools.executeWithOutput("cat /etc/passwd"); //$NON-NLS-1$
			String [] rawPasswdLines = rawPasswd.split("\\n"); //$NON-NLS-1$
			
			// Look for the string that matches the value from the currentUsername
			// That string is on the first line 
			for(int i=0; i < rawPasswdLines.length; i++) {
				// Make sure it includes the last field even if it's blank.
				String [] passwdFields = rawPasswdLines[i].split(":", -1); //$NON-NLS-1$
				if(passwdFields[PASSWD_USERNAME_FIELD].equals(currentUsername))
					return passwdFields;
			}
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e);
		}
		
		throw new RuntimeException(Messages.RemoteStatusTools_GetPasswdFields_NoUsernameInPasswdFile);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteStatusTools#getUsername()
	 */
	public String getUsername() throws RemoteConnectionException, RemoteOperationException,
		CancelException {
		if(userInfoCache == null) {
			userInfoCache = fetchRemoteUserInfo();
		}			
		return userInfoCache.getUsername();
	}
	
	/**
	 * * An implementation of the {@link getUsername} method that uses the whoami command on the remote machine
	 * to extract username information. 
	 * 
	 * @return
	 * @throws RemoteConnectionException
	 * @throws RemoteOperationException
	 * @throws CancelException
	 */
	private String getUsername_WhoamiBased() throws RemoteConnectionException, RemoteOperationException,
	CancelException {
//		 Get information from the whoami command
		try {
			IRemoteExecutionTools remExecTools = manager.getExecutionTools();
			String rawWhoAmI = remExecTools.executeWithOutput("whoami"); //$NON-NLS-1$
			return rawWhoAmI.trim();
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e);
		}
	}

	/**
	 * A method designed to parse the /etc/group file to find the list
	 * of groups that a user is enrolled in. 
	 * 
	 * @param username The name of the user that we are searching for enrollments.
	 * @return A list of groups that username belongs to.
	 * @throws RemoteConnectionException 
	 * @throws RemoteOperationException 
	 */
	private Set<Integer> readGroupList(String username) throws RemoteConnectionException, RemoteOperationException {
		Set<Integer> groupList = new HashSet<Integer>();

		BufferedReader etcGroupFile = null;
		
		try {
			IRemoteFileTools ft = manager.getRemoteFileTools();
			InputStream is = ft.getInputStream("/etc/group",null); //$NON-NLS-1$
			etcGroupFile = new BufferedReader(new InputStreamReader(is ));
			
			String line = etcGroupFile.readLine();

			while (line != null) {
				String[] fields = line.split(":"); //$NON-NLS-1$

				if (fields[0].trim().compareTo(username) == 0) {
					groupList.add(new Integer(fields[2]));
				} else if (fields.length == 4) {

					String[] users = fields[3].split(","); //$NON-NLS-1$
					for (int i = 0; i < users.length; i++) {

						if (users[i].trim().compareTo(username) == 0) {
							groupList.add(new Integer(fields[2]));
						}
					}
				}

				line = etcGroupFile.readLine();
			}
		} catch (IOException ioe) {
			throw new RemoteOperationException(ioe);
		} catch (CancelException ce) {
			throw new RemoteOperationException(ce);
		} catch (NumberFormatException nfe) {
			//Maybe the format of this /etc/group file put the fields in a
			//different order.
			throw new RemoteOperationException(nfe);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			//This case is going to happen if the format is not the expected one.
			throw new RemoteOperationException(aioobe);
		}
		finally {
			try {
			    if (etcGroupFile != null) { 
			    	etcGroupFile.close(); 
			    }
			} catch (IOException ioe) {throw new RemoteOperationException(ioe);}
		} 
		return groupList;
	}
	
	/**
	 * Return all the user information from the remote machine.
	 * This operation is a bit slow and should be used only once per RemoteStatusTools object.
	 * 
	 * @throws RemoteConnectionException
	 * @throws RemoteOperationException
	 * @throws CancelException
	 * @return 
	 */
	private UserInformation fetchRemoteUserInfo() throws RemoteConnectionException, RemoteOperationException,
		CancelException {
		try {
			//Set of groups that this user belongs to.
			Set<Integer> groupList;
			
			IRemoteExecutionTools remExecTools = manager.getExecutionTools();
			// Execute command to return the username, uid and the group list
			String rawUserInfo = remExecTools.executeWithOutput("echo `id -un`:`id -u`:`id -G`"); //$NON-NLS-1$
			
			String [] userInfoFields = rawUserInfo.trim().split(":", -1);  //$NON-NLS-1$
			
			//With systems using BusyBox, id has no argument -G
			//so we must parse /etc/group in this case
			if (userInfoFields[2].trim().equalsIgnoreCase("")) { //$NON-NLS-1$
				groupList = readGroupList(userInfoFields[0]);
			} else {
				String[] groupFields = userInfoFields[2].split(" ", -1); //$NON-NLS-1$

				groupList = new HashSet<Integer>();

				for (int i = 0; i < groupFields.length; i++) {
					// Convert groups to Integer and put into the Set
					groupList.add(new Integer(groupFields[i]));
				}
			}
			
			return new UserInformation(new Integer(userInfoFields[1]), groupList, userInfoFields[0]);
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e);
		}
	}
	
	public long getTime() throws RemoteConnectionException, RemoteOperationException, CancelException {
		try {
			IRemoteExecutionTools execTools = manager.getExecutionTools();
			String rawData = execTools.executeWithOutput("date +'%s'"); //$NON-NLS-1$
			rawData = rawData.trim();
			return Long.parseLong(rawData)*1000;
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e);
		}
	}
	
}
