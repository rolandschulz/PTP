/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core;

public interface IPNode extends IPElement {
	/* node state attribute values */
	public static final String NODE_STATE_UP = "up";
	public static final String NODE_STATE_DOWN = "down";
	public static final String NODE_STATE_ERROR = "error";
	public static final String NODE_STATE_UNKNOWN = "unknown";

	/**
	 * Returns an array of all the Processes residing on this Node. May return <code>null</code> if there are none.
	 * 
	 * @return All Processes residing on this Node or <code>null</code> if there are none.
	 */
	public IPProcess[] getProcesses();
	
	/**
	 * Returns a sorted array of all the Processes residing on this Node. May return <code>null</code> if there are none. How the Processes are sorted is left open to the implementer of this interface.
	 * 
	 * @return All Processes residing on this Node, sorted or <code>null</code> if there are none.
	 */
	public IPProcess[] getSortedProcesses();
	/**
	 * Returns the node number of this Node as a String.
	 * 
	 * @return The node number of this Node as a String
	 */
	public String getNodeNumber();
	/**
	 * Finds a Process given a <code>processNumber</code> which resides on this Node. If found, the Process object is returned, else <code>null</code> is returned.
	 * 
	 * @param processNumber
	 *            The Process number to find
	 * @return The Process if found, else <code>null</code>
	 */
	public int getNodeNumberInt();
	/**
	 * @param processNumber
	 * @return
	 */
	public IPProcess findProcess(String processNumber);
	/**
	 * Returns the parent Machine that this Node is contained within.
	 * 
	 * @return The Machine this Node is part of
	 */
	public IPMachine getMachine();
	/**
	 * Returns an array of Jobs that have Processes running on this Node. May return <code>null</code> if there are none.
	 * 
	 * @return Array of Jobs that have Processes running on this Node.
	 */
	public IPJob[] getJobs();

	/**
	 * Searches for an attribute on the Element given a key.  
	 * The resulting attribute Object is returned.  The returned may be null if the attribute
	 * was not found.
	 * 
	 * @param key String key of the attribute to look for
	 * @return Object of the attribute or null if not found
	 */
	public Object getAttribute(String key);

	/**
	 * Sets an attribute given a key and Object.
	 * 
	 * @param key String key of the attribute
	 * @param Object of the attribute
	 */
	public void setAttribute(String key, Object o);
	
	/**
	 * Get all the keys of all job attributes.
	 * 
	 * @return A string array containing the keys
	 */
	public String[] getAttributeKeys();

	/**
	 * @return
	 */
	public int getNumProcesses();

	/**
	 * @return
	 */
	public String getName();

	/**
	 * @return
	 */
	public String getIDString();

	/**
	 * @return
	 */
	public boolean isAllStop();

	/**
	 * @return
	 */
	public boolean hasChildProcesses();
}