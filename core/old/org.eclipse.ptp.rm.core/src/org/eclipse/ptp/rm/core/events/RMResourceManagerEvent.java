/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.rm.core.events;

import org.eclipse.ptp.rm.core.IRMResourceManager;
import org.eclipse.ptp.rm.core.attributes.IAttrDesc;

/**
 * Determine type of changed in the IRMResourceManager's state. The type can be
 * ADDED, MODIFIED, or REMOVED
 * 
 * @author rsqrd
 * 
 */
public abstract class RMResourceManagerEvent {
	/**
	 * Elements have been added
	 */
	public static final int ADDED = 2;

	/**
	 * Elements have been modified
	 */
	public static final int MODIFIED = 1;

	/**
	 * Elements have been removed
	 */
	public static final int REMOVED = 3;

	private final IRMResourceManager resourceManager;

	private int type;

	private final IAttrDesc[] modifiedAttributeDescriptions;

	private final boolean statusChanged;

	public RMResourceManagerEvent(IAttrDesc[] modifiedAttributes,
			boolean statusChanged, IRMResourceManager manager, int type) {
		resourceManager = manager;
		this.modifiedAttributeDescriptions = (IAttrDesc[]) (modifiedAttributes == null ? null
				: modifiedAttributes.clone());
		this.statusChanged = statusChanged;
		this.type = type;
	}

	/**
	 * @return what the set of modified attributes' descriptions are
	 */
	public IAttrDesc[] getModifiedAttributeDescriptions() {
		return (IAttrDesc[]) (modifiedAttributeDescriptions == null ? null
				: modifiedAttributeDescriptions.clone());
	}

	/**
	 * @return the resource manager that has had a state change
	 */
	public IRMResourceManager getResourceManager() {
		return resourceManager;
	}

	/**
	 * @return the type of state changed<br>
	 *         ADDED<br>
	 *         MODIFIED<br>
	 *         REMOVED
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return whether the element's status has changed
	 */
	public boolean isStatusChanged() {
		return statusChanged;
	}
}
