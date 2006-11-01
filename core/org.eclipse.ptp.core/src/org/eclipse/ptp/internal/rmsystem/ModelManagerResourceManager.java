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
package org.eclipse.ptp.internal.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.IModelListener;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IModelPresentation;
import org.eclipse.ptp.core.events.IModelEvent;
import org.eclipse.ptp.core.events.IModelRuntimeNotifierEvent;
import org.eclipse.ptp.core.events.IModelSysChangedEvent;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.ResourceManagerStatus;
import org.eclipse.ptp.rtsystem.IControlSystem;

/**
 * @author rsqrd
 *
 */
public class ModelManagerResourceManager extends AbstractResourceManager implements IModelListener {
	
	private static final NullManager NULL_MANAGER = new NullManager();
	
	private final IModelManager manager;

	public ModelManagerResourceManager(IModelManager manager, IResourceManagerConfiguration config) {
		super(config);
		this.manager = manager;
		manager.addModelListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doStart()
	 */
	protected void doStart() throws CoreException {
		//manager.refreshRuntimeSystems(null, true);
		//setStatus(ResourceManagerStatus.STARTED, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doStop()
	 */
	protected void doStop() throws CoreException {
		manager.shutdown();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#getControlSystem()
	 */
	public IControlSystem getControlSystem() {
		return manager.getControlSystem();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#getModelPresentation()
	 */
	public IModelPresentation getModelPresentation() {
//		if (!getStatus().equals(ResourceManagerStatus.STARTED)) {
//			return NULL_MANAGER;
//		}
		return manager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getModelManager()
	 */
	public IModelManager getModelManager() {
//		if (!getStatus().equals(ResourceManagerStatus.STARTED)) {
//			return NULL_MANAGER;
//		}
		return manager;
	}

	public void modelEvent(IModelEvent event) {
		if (event instanceof IModelRuntimeNotifierEvent) {
			IModelRuntimeNotifierEvent runtimeEvent = (IModelRuntimeNotifierEvent)event;
			int eventType = runtimeEvent.getType();
			switch (runtimeEvent.getStatus()) {
			case IModelRuntimeNotifierEvent.RUNNING:
				setStatus(ResourceManagerStatus.STARTED, false);
				break;
			case IModelRuntimeNotifierEvent.STARTED:
				setStatus(ResourceManagerStatus.STARTED, false);
				break;
			}
			if (eventType == IModelRuntimeNotifierEvent.TYPE_RESOURCEMANAGER) {
				switch (runtimeEvent.getStatus()) {
				case IModelRuntimeNotifierEvent.STOPPED:
					manager.removeModelListener(this);
					setStatus(ResourceManagerStatus.STOPPED, false);
					break;
				case IModelRuntimeNotifierEvent.ABORTED:
					manager.removeModelListener(this);
					setStatus(ResourceManagerStatus.STOPPED, false);
					break;
				}
			}
		}
		else if (event instanceof IModelSysChangedEvent) {
			IModelSysChangedEvent sysEvent = (IModelSysChangedEvent)event;
			switch (sysEvent.getType()) {
			case IModelSysChangedEvent.MONITORING_SYS_CHANGED:
				setStatus(ResourceManagerStatus.STARTED, false);
				break;
			case IModelSysChangedEvent.SYS_STATUS_CHANGED:
				break;
			}
		}
	}

}
