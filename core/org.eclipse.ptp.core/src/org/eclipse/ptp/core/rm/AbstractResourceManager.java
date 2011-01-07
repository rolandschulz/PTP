/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.core.rm;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.rm.exceptions.ResourceManagerException;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;

/**
 * @since 5.0
 */
public abstract class AbstractResourceManager implements IResourceManager, IResourceManagerMenuContribution {
	private final ListenerList fListeners = new ListenerList();
	private final IJobTemplateFactory fJobTemplateFactory;
	private IResourceManagerConfiguration fRMConfig;
	private SessionStatus fStatus = SessionStatus.STOPPED;

	public AbstractResourceManager(IJobTemplateFactory jobTemplateFactory, IResourceManagerConfiguration config) {
		fJobTemplateFactory = jobTemplateFactory;
		fRMConfig = config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.rm.IResourceManager#addListener(org.eclipse.ptp.
	 * core.elements.listeners.IResourceManagerListener)
	 */
	public void addListener(IResourceManagerListener listener) {
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.rm.IResourceManager#createJobTemplate()
	 */
	public IJobTemplate createJobTemplate() throws ResourceManagerException {
		return fJobTemplateFactory.createJobTemplate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.rm.IResourceManager#deleteJobTemplate(org.eclipse
	 * .ptp.core.rm.IJobTemplate)
	 */
	public void deleteJobTemplate(IJobTemplate jobTemplate) throws ResourceManagerException {
		fJobTemplateFactory.deleteJobTemplate(jobTemplate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) {
			return this;
		}
		if (adapter == IResourceManagerConfiguration.class) {
			return getConfiguration();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.rm.IResourceManager#getConfiguration()
	 */
	public synchronized IResourceManagerConfiguration getConfiguration() {
		return fRMConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.rm.IResourceManager#getDescription()
	 */
	public String getDescription() {
		return getConfiguration().getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.rm.IResourceManager#getName()
	 */
	public String getName() {
		return getConfiguration().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.rm.IResourceManager#getResourceManagerId()
	 */
	public String getResourceManagerId() {
		return getConfiguration().getResourceManagerId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.rm.IResourceManager#getSessionStatus()
	 */
	public synchronized SessionStatus getSessionStatus() {
		return fStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.rm.IResourceManager#getUniqueName()
	 */
	public String getUniqueName() {
		return getConfiguration().getUniqueName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.rm.IResourceManager#removeListener(org.eclipse.ptp
	 * .core.elements.listeners.IResourceManagerListener)
	 */
	public void removeListener(IResourceManagerListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.rm.IResourceManager#setConfiguration(org.eclipse
	 * .ptp.rmsystem.IResourceManagerConfiguration)
	 */
	public synchronized void setConfiguration(IResourceManagerConfiguration config) {
		fRMConfig = config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.rm.IResourceManager#setStatus(org.eclipse.ptp.core
	 * .rm.IResourceManager.SessionStatus)
	 */
	public synchronized void setStatus(SessionStatus status) {
		fStatus = status;
	}
}
