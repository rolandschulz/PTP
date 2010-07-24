/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.core.elements.events;

import java.util.Collection;

import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;

/**
 * This event is generated when the attributes on one or more nodes have
 * changed. It is a bulk event that is sent to child listeners on the source
 * machine.
 * 
 * @see org.eclipse.ptp.core.elements.listeners.IMachineChildListener
 */
public interface IChangedNodeEvent {

	/**
	 * Get the nodes that have changed
	 * 
	 * @return nodes that have changed
	 */
	public Collection<IPNode> getNodes();

	/**
	 * Get the source of the event
	 * 
	 * @return the source of the event
	 */
	public IPMachine getSource();
}
