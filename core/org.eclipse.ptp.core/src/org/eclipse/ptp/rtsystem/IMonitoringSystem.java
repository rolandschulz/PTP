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

package org.eclipse.ptp.rtsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;

/**
 * A Monitoring System is a portion of a runtime system that handles status and information about
 * the system (generally things outside the control of the runtime system).  This includes
 * what machines are available, the nodes for those machines, the status of those nodes, etc.
 * Monitoring Systems also can fire events, specifically {@link RuntimeEvent}s.
 * 
 * @author Nathan DeBardeleben
 *
 */
public interface IMonitoringSystem {
	/**
	 * Gets all the machines visible to this Monitoring System and returns their names
	 * as a {@link String} array of the form [ "machine0", "machine1", "etc" ]
	 * 
	 * @return the machines visible to the monitoring system
	 * @throws CoreException 
	 */
	public String[] getMachines() throws CoreException;

	/**
	 * Gets the names of all the nodes contained in a specified {@link IPMachine}.
	 * The nodes must be named of the form "machinename_nodename" like
	 * [ "machine0_node0", "machine0_node1", "etc" ]
	 * 
	 * @param machine the machine to look for nodes on
	 * @return a list of the node names
	 * @throws CoreException 
	 */
	public String[] getNodes(IPMachine machine) throws CoreException;

	/**
	 * Gets the name of the machine given 
	 * @param nodeName
	 * @return
	 * @throws CoreException 
	 */
	public String getNodeMachineName(String nodeName) throws CoreException;
	
	public String[] getAllNodesAttributes(IPMachine machine, String[] attribs) throws CoreException;

	public String[] getNodeAttributes(IPNode node, String[] attribs) throws CoreException;

	/* event stuff */
	public void addRuntimeListener(IRuntimeListener listener);

	public void removeRuntimeListener(IRuntimeListener listener);

	public void startup();
	
	public void shutdown();
}
