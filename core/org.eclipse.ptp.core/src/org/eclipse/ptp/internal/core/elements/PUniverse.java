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
package org.eclipse.ptp.internal.core.elements;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;

public class PUniverse extends PElement implements IPUniverseControl {
	private static final int RMID_SHIFT = 24;
	private int nextResourceManagerId = 1;
	private final List<IResourceManagerControl> resourceManagers =
		new LinkedList<IResourceManagerControl>();
	protected String NAME_TAG = "universe ";
	
	public PUniverse() {
		/* '1' because this is the only universe */
		super("1", null, P_UNIVERSE, getDefaultAttributes("TheUniverse"));
		// setOutputStore();
	}
	
	private static IAttribute[] getDefaultAttributes(String name) {
		IAttribute nameAttr = null;
		
		try {
			 nameAttr = ElementAttributes.getNameAttributeDefinition().create(name);
		} catch (IllegalValueException e) {
		}
		
		return new IAttribute[]{nameAttr};
	}

	public synchronized void addResourceManager(IResourceManagerControl addedManager) {
		resourceManagers.add(addedManager);
	}

	public synchronized void addResourceManagers(IResourceManagerControl[] addedManagers) {
		for (int i=0; i<addedManagers.length; ++i) {
			addResourceManager(addedManagers[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPUniverse#findResourceManagerById(java.lang.String)
	 */
	public synchronized IResourceManager getResourceManager(String id) {
		for (IResourceManager resourceManager : resourceManagers) {
			if (resourceManager.getID().equals(id)) {
				return resourceManager;
			}
		}
		return null;
	}
	
	public int getNextResourceManagerId() {
		return (nextResourceManagerId++ << RMID_SHIFT);
	}
	
	/**
	 * @return all of the resource managers
	 */
	public synchronized IResourceManagerControl[] getResourceManagerControls() {
		return resourceManagers.toArray(new IResourceManagerControl[0]);
	}

	/**
	 * @return all of the resource managers
	 */
	public synchronized IResourceManager[] getResourceManagers() {
		return (IResourceManager[]) getResourceManagerControls();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPElementControl#hasChildren()
	 */
	public boolean hasChildren() {
		return !resourceManagers.isEmpty();
	}

	public synchronized void removeResourceManager(IResourceManager removedManager) {
		resourceManagers.remove(removedManager);
	}

	public void removeResourceManagers(IResourceManager[] removedRMs) {
		for (int i=0; i<removedRMs.length; ++i) {
			removeResourceManager(removedRMs[i]);
		}
	}
}