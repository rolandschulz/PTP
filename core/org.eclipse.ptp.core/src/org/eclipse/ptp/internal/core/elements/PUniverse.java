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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;

public class PUniverse extends Parent implements IPUniverseControl {
	private static final int RMID_SHIFT = 24;

	private static IAttribute<?, ?, ?>[] getDefaultAttributes(String name) {
		StringAttribute nameAttr = ElementAttributes.getNameAttributeDefinition().create(name);
		return new IAttribute[] { nameAttr };
	}

	private int nextResourceManagerId = 1;
	private final List<IResourceManagerControl> resourceManagers = Collections
			.synchronizedList(new LinkedList<IResourceManagerControl>());
	protected String NAME_TAG = "universe "; //$NON-NLS-1$

	public PUniverse() {
		/* '1' because this is the only universe */
		super("1", null, getDefaultAttributes("TheUniverse")); //$NON-NLS-1$ //$NON-NLS-2$
		// setOutputStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPUniverseControl#addResourceManager
	 * (org.eclipse.ptp.core.elementcontrols.IResourceManagerControl)
	 */
	public void addResourceManager(IResourceManagerControl addedManager) {
		resourceManagers.add(addedManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPUniverseControl#addResourceManagers
	 * (org.eclipse.ptp.core.elementcontrols.IResourceManagerControl[])
	 */
	public void addResourceManagers(IResourceManagerControl[] addedManagers) {
		for (IResourceManagerControl rm : addedManagers) {
			addResourceManager(rm);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java
	 * .util.Map)
	 */
	@Override
	protected void doAddAttributeHook(AttributeManager attrs) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IPUniverseControl#
	 * getNextResourceManagerId()
	 */
	public synchronized String getNextResourceManagerId() {
		return Integer.toString(nextResourceManagerId++ << RMID_SHIFT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IPUniverseControl#
	 * getResourceManagerControls()
	 */
	public IResourceManagerControl[] getResourceManagerControls() {
		return resourceManagers.toArray(new IResourceManagerControl[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPUniverse#getResourceManagers()
	 */
	public IResourceManagerControl[] getResourceManagers() {
		return getResourceManagerControls();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPUniverseControl#removeResourceManager
	 * (org.eclipse.ptp.core.elements.IResourceManagerControl)
	 */
	public void removeResourceManager(IResourceManagerControl removedManager) {
		resourceManagers.remove(removedManager);
	}
}