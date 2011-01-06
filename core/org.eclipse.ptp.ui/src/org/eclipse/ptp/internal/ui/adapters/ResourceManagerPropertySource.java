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
package org.eclipse.ptp.internal.ui.adapters;

import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class ResourceManagerPropertySource extends PElementPropertySource {

	private final IPResourceManager resourceManager;

	public ResourceManagerPropertySource(IPResourceManager resourceManager) {
		super(resourceManager);
		
		this.resourceManager = resourceManager;
		addDescriptor(new PropertyDescriptor("rm.machines", "num machines")); //$NON-NLS-1$ //$NON-NLS-2$
		addDescriptor(new PropertyDescriptor("rm.queues", "num queues")); //$NON-NLS-1$ //$NON-NLS-2$
		
	}

	public Object getPropertyValue(Object id) {
		Object value = super.getPropertyValue(id);
		if (value != null) {
			return value;
		}
		if ("rm.machines".equals(id)) { //$NON-NLS-1$
			final IPMachine[] machines = resourceManager.getMachines();
			return Integer.toString(machines.length);
		}
		if ("rm.queues".equals(id)) { //$NON-NLS-1$
			final IPQueue[] queues = resourceManager.getQueues();
			return Integer.toString(queues.length);
		}
		return null;
	}

}

