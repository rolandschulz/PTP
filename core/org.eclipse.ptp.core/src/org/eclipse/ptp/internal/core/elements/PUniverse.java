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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.jobs.IJobStatus;

public class PUniverse extends Parent implements IPUniverse {
	private static final int RMID_SHIFT = 24;

	private static IAttribute<?, ?, ?>[] getDefaultAttributes(String name) {
		StringAttribute nameAttr = ElementAttributes.getNameAttributeDefinition().create(name);
		return new IAttribute[] { nameAttr };
	}

	private int nextResourceManagerId = 1;
	private final Map<String, IPResourceManager> resourceManagers = Collections
			.synchronizedMap(new HashMap<String, IPResourceManager>());
	protected String NAME_TAG = "universe "; //$NON-NLS-1$

	public PUniverse() {
		/* '1' because this is the only universe */
		super("1", null, getDefaultAttributes("TheUniverse")); //$NON-NLS-1$ //$NON-NLS-2$
		// setOutputStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPUniverse#addResourceManager(java.lang.String, java.lang.String)
	 */
	public IPResourceManager addResourceManager(String name, String controlId) {
		IPResourceManager rm = new PResourceManager(ModelManager.getInstance().getUniverse(), name, controlId);
		resourceManagers.put(controlId, rm);
		return rm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPUniverse#getJob(org.eclipse.ptp.core.jobs.IJobControl, java.lang.String)
	 */
	public IPJob getJob(IJobControl control, String jobId) {
		IPResourceManager rm = getResourceManager(control.getControlId());
		if (rm != null) {
			return rm.getJobById(jobId);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelPresentation#getJob(IJobStatus)
	 */
	public IPJob getJob(IJobStatus status) {
		IPResourceManager rm = getResourceManager(status.getControlId());
		if (rm != null) {
			return rm.getJobById(status.getControlId());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IPUniverseControl# getNextResourceManagerId()
	 */
	public synchronized String getNextResourceManagerId() {
		return Integer.toString(nextResourceManagerId++ << RMID_SHIFT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelPresentation#getNode(java.lang.String, java.lang.String)
	 */
	public IPNode getNode(IJobControl control, String nodeId) {
		IPResourceManager rm = getResourceManager(control.getControlId());
		if (rm != null) {
			return rm.getNodeById(nodeId);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPUniverse#getResourceManager(java.lang.String)
	 */
	public IPResourceManager getResourceManager(String controlId) {
		return resourceManagers.get(controlId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPUniverse#getResourceManagers()
	 */
	public IPResourceManager[] getResourceManagers() {
		return resourceManagers.values().toArray(new IPResourceManager[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPUniverse#removeResourceManager(java.lang.String)
	 */
	public void removeResourceManager(String controlId) {
		resourceManagers.remove(controlId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java .util.Map)
	 */
	@Override
	protected void doAddAttributeHook(AttributeManager attrs) {
	}
}