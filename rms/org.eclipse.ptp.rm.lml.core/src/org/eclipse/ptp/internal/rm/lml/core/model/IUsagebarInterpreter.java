/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.core.model;

/**
 * A UsagebarInterpreter provides functions for interpreting lml-usagebars.
 * One instance of an UsagebarInterpreter deals with exactly one usagebar.
 * This interface defines the API for converting node-ID into CPU-ID.
 * It is used as mapping between node- and CPU-ID.
 * 
 * @author karbach
 * 
 */
public interface IUsagebarInterpreter {

	/**
	 * Searches for last CPU-ID within the node with ID node.
	 * 
	 * @param node
	 *            id of a node, for which the biggest CPU-ID within this node is searched
	 * @return biggest CPU-ID within the passed node
	 */
	public int getLastCPUinNode(int node);

	/**
	 * @return count of nodes within the usagebar
	 */
	public int getNodeCount();

	/**
	 * Returns the number of nodes covered by all CPU with ID 0 to CPUcount.
	 * 
	 * @param CPUcount
	 *            Id of a cpu within an usagebar.
	 * @return nodes' count covered by the CPU before and including CPUcount
	 */
	public int getNodecountAtCpu(int CPUcount);

}
