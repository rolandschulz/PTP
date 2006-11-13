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

import org.eclipse.ptp.rmsystem.IResourceManager;

/**
 * Interface class for a model of a machine. Machines are comprised of nodes
 * which may have processes running on them. The Machine model is intented to be
 * a hardware representation of the machine with methods to get information like
 * the architecture and related interested bits. The model allows for the
 * Machine to have any number of nodes (even a single one to represent a
 * uniprocessor machine).
 * 
 * @author Nathan DeBardeleben
 */
public interface IPMachine extends IPElement {
	/**
	 * Returns an array of all the Nodes that this Machine is comprised of. May
	 * return null if there are none.
	 * 
	 * @return The Nodes in this Machine - <code>null</code> if there are
	 *         none.
	 */
	public IPNode[] getNodes();
	
	public String getMachineId();
	
	/**
	 * Returns a sorted array of all the Nodes in this Machine. May return null
	 * if there are none. The type of sorting is left open to the implementers
	 * of this interface.
	 * 
	 * @return The Nodes in this Machine, sorted - <code>null</code> if there
	 *         are none.
	 */
	public IPNode[] getSortedNodes();

	/**
	 * Given a node number as a String attempts to find it as a Node object
	 * contained in this Machine. If found, the Node object is returned.
	 * Otherwise, <code>null</code> is returned.
	 * 
	 * @param nodeNumber
	 *            The Node number to find in this Machine, as a String
	 * @return The Node object if found, else <code>null</code>
	 */
	public IPNode findNode(String nodeNumber);

	/**
	 * Given a Node name attempts to find it as a Node object contained in this
	 * Machine. If found, the Node object is returned. Otherwise,
	 * <code>null</code> is returned.
	 * 
	 * @param nname
	 *            The Node name to find in this Machine
	 * @return The Node object if found, else <code>null</code>
	 */
	public IPNode findNodeByName(String nname);

	/**
	 * Returns an array of all the Processes that are on Nodes of this Machine.
	 * May return null if there are none.
	 * 
	 * @return The Processes on this Machine - <code>null</code> if there are
	 *         none.
	 */
	public IPProcess[] getProcesses();

	/**
	 * Returns a sorted array of all the Processes on Nodes of this Machine. May
	 * return <code>null</code> if there are none. The type of sorting is left
	 * open to the implementers of this interface.
	 * 
	 * @return The Processes on this Machine, sorted - <code>null</code> if
	 *         there are none.
	 */
	public IPProcess[] getSortedProcesses();

	/**
	 * Counts all the Nodes associated with this Machine and returns that as an
	 * <code>int</code>.
	 * 
	 * @return The number of Nodes in this Machine
	 */
	public int totalNodes();

	/**
	 * Counts all the Processes on the Nodes of this Machine and returns that as
	 * an <code>int</code>.
	 * 
	 * @return The number of Processes in this Machine
	 */
	public int totalProcesses();

	/**
	 * Removes all Processes on this Machine. <br>
	 * TODO: <i>EXAMINE THIS - DO WE WANT THIS FUNCTION AT THE MACHINE LEVEL?</i>
	 */
	public void removeAllProcesses();

	/**
	 * Returns the parent Universe of this Machine.
	 * 
	 * @return The Universe this Machine is in
	 */
	public IPUniverse getUniverse();

	/**
	 * Sets the architecture of this machine. At this time there are no
	 * standards for how this String should be formatted and the String is
	 * really only used for printing out information about this Machine.
	 * 
	 * @param arch
	 *            The architecture of this machine
	 */
	public void setArch(String arch);

	/**
	 * Returns the architecture of this Machine.
	 * 
	 * @return The architecture of this Machine
	 */
	public String getArch();
	
	/**
	 * @return
	 */
	public String getName();

	public String getIDString();
	
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
	 * @return the machine's parent resource manager
	 */
	public IResourceManager getResourceManager();

}
