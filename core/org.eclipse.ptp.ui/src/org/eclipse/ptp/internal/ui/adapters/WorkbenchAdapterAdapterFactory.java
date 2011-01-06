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
/**
 * 
 */
package org.eclipse.ptp.internal.ui.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.rm.IResourceManager;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

public class WorkbenchAdapterAdapterFactory implements IAdapterFactory {

	private static final UniverseWorkbenchAdapter UNIVERSE_WORKBENCH_ADAPTER = new UniverseWorkbenchAdapter();
	private static final PResourceManagerWorkbenchAdapter RESOURCE_MANAGER_WORKBENCH_ADAPTER = new PResourceManagerWorkbenchAdapter();
	private static final PMachineWorkbenchAdapter MACHINE_WORKBENCH_ADAPTER = new PMachineWorkbenchAdapter();
	private static final PQueueWorkbenchAdapter QUEUE_WORKBENCH_ADAPTER = new PQueueWorkbenchAdapter();
	private static final PNodeWorkbenchAdapter NODE_WORKBENCH_ADAPTER = new PNodeWorkbenchAdapter();
	private static final PJobWorkbenchAdapter JOB_WORKBENCH_ADAPTER = new PJobWorkbenchAdapter();
	private static final ResourceManagerWorkbenchAdapter RM_WORKBENCH_ADAPTER = new ResourceManagerWorkbenchAdapter();

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IWorkbenchAdapter.class || adapterType == IWorkbenchAdapter2.class) {
			if (adaptableObject instanceof IPUniverse) {
				return UNIVERSE_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IPResourceManager) {
				return RESOURCE_MANAGER_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IPMachine) {
				return MACHINE_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IPNode) {
				return NODE_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IPQueue) {
				return QUEUE_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IPJob) {
				return JOB_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IResourceManager) {
				return RM_WORKBENCH_ADAPTER;
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class, IWorkbenchAdapter2.class };
	}

}
