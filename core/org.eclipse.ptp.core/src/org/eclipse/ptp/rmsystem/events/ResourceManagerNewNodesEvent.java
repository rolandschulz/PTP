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
/**
 * 
 */
package org.eclipse.ptp.rmsystem.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.rmsystem.IResourceManager;

/**
 * @author rsqrd
 *
 */
public class ResourceManagerNewNodesEvent implements
		IResourceManagerNewNodesEvent {

	private final List<IPNode> newNodes;
	private final IResourceManager rm;

	public ResourceManagerNewNodesEvent(IResourceManager manager, List<IPNode> nodes) {
		this.rm = manager;
		this.newNodes = new ArrayList<IPNode>(nodes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.events.IResourceManagerNewNodesEvent#getNewNodes()
	 */
	public Collection<IPNode> getNewNodes() {
		return Collections.unmodifiableList(newNodes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.events.IResourceManagerNewNodesEvent#getSource()
	 */
	public IResourceManager getSource() {
		return rm;
	}

}
