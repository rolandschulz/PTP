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

/**
 * A Universe represents a user's view of the world.  It is comprised of a
 * set of Machines and Jobs.  This class has helper methods to get access
 * to those sets as well as other things down inside those sets (such as
 * finding a node, which may require searching all machines).
 * 
 * @author      Nathan DeBardeleben
 * @see         IPMachine
 * @see         IPJob
 */
public interface IPUniverse extends IPElement {
	/**
	 * Returns all the Machines that are visible by this Universe, or null
	 * if there are none.
	 * 
	 * @return	An array of all Machines in this Universe, null if there are none
	 */
	public IPMachine[] getMachines();
	
	/**
	 * Returns all the Machines that are visible by this Universe sorted by
	 * their name, or null if there are none.
	 * 
	 * @return	A sorted array of all Machines in this Universe, null if there are none
	 */
	public IPMachine[] getSortedMachines();
	
	/**
	 * Given a Machine name, returns the Machine object if it can be found
	 * in this Universe.  Returns null if it cannot be found.
	 * 
	 * @param mname	A name of a Machine to search for in this Universe
	 * @return		The Machine object if found, else null
	 */
	public IPMachine findMachineByName(String mname);
	
	/**
	 * Returns all the Jobs that are visible by this Universe, or null if
	 * there are none.
	 * 
	 * @return	An array of all Jobs in this Universe, null if there are none
	 */
	public IPJob[] getJobs();
	
	/**
	 * Returns all the Jobs that are visible bythis Universe sorted by their
	 * name, or null if there are none.
	 * 
	 * @return	A sorted array of all Jobs in this Universe, null if there are none
	 */
	public IPJob[] getSortedJobs();
	
	/**
	 * Given a Job name, returns a Job object if the Job can be found in this
	 * Universe.  Returns null if it cannot be found.
	 * 
	 * @param jname	A name of the Job to search for in this Universe
	 * @return		The Job object if found, else null
	 */
	public IPJob findJobByName(String jname);
	
	/**
	 * Given a Node name, returns the Node object located on one of the 
	 * Machines in this Universe.  Returns null if the Node cannot be found.
	 * This method traverses the Machines.
	 * 
	 * @param nname	A name of a Job to search for in this Universe
	 * @return		The Node object if found, else null
	 */
	public IPNode findNodeByName(String nname);
	
	/**
	 * Given a Process name, returns the Process object located on one of the
	 * Machines in this Universe.  Returns null if the Node cannot be found.
	 * This method traverses the Jobs.
	 * 
	 * @param pname	A name of a Process to esarch for in this Universe
	 * @return		The Process object if found, else null
	 */
	public IPProcess findProcessByName(String pname);
	
	/**
	 * If the model or UI or whatever needs to write output this returns a
	 * directory there can do it in.
	 * <br>TODO: <i>SLATED FOR REMOVAL</i>
	 * 
	 * @return	Directory output can be written to.
	 */
	public String getOutputStoreDirectory();
	
	/**
	 * Returns the current line number we have stored up to in an output
	 * file.
	 * <br>TODO: <i>SLATED FOR REMOVAL</i>
	 * 
	 * @return	The current line output has been written to
	 */
	public int getStoreLine();
}
