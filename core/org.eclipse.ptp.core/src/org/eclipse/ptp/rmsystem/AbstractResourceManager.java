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
package org.eclipse.ptp.rmsystem;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.events.IJobChangedEvent;
import org.eclipse.ptp.core.listeners.IJobListener;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.internal.core.ModelManager;
import org.eclipse.ptp.internal.core.elements.PResourceManager;
import org.eclipse.ptp.internal.core.events.JobChangedEvent;

/**
 * @author rsqrd
 * @since 5.0
 * 
 */
public abstract class AbstractResourceManager implements IResourceManagerControl {
	private final PResourceManager fPResourceManager;
	private final IPUniverse fUniverse;
	private final ListenerList fJobListeners = new ListenerList();
	private final Map<String, IJobStatus> fJobStatus = new HashMap<String, IJobStatus>();
	private final IModelManager fModelManager = PTPCorePlugin.getDefault().getModelManager();

	private IResourceManagerConfiguration fConfig;
	private String fState;

	/**
	 * @since 5.0
	 */
	public AbstractResourceManager(IPUniverse universe, IResourceManagerConfiguration config) {
		fConfig = config;
		fUniverse = universe;
		fPResourceManager = new PResourceManager(universe, this);
		universe.addResourceManager(fPResourceManager);
		fState = STOPPED_STATE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl#addJobListener(org.eclipse
	 * .ptp.core.listeners.IJobListener)
	 */
	/**
	 * @since 5.0
	 */
	public void addJobListener(IJobListener listener) {
		fJobListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl#control(java.lang.String
	 * , java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 5.0
	 */
	public void control(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		doControlJob(jobId, operation, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#dispose()
	 */
	public void dispose() {
		try {
			stop();
		} catch (CoreException e) {
		}
		doDispose();
		fUniverse.removeResourceManager(fPResourceManager);
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
		if (adapter == IPResourceManager.class) {
			return fPResourceManager;
		}
		if (adapter == IResourceManagerConfiguration.class) {
			return getConfiguration();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getConfiguration
	 * ()
	 */
	public IResourceManagerConfiguration getConfiguration() {
		synchronized (this) {
			return fConfig;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getDescription
	 * ()
	 */
	public String getDescription() {
		StringAttributeDefinition descAttrDef = ResourceManagerAttributes.getDescriptionAttributeDefinition();
		StringAttribute descAttr = fPResourceManager.getAttribute(descAttrDef);
		if (descAttr != null) {
			return descAttr.getValue();
		}
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl#getJobStatus(java.lang
	 * .String)
	 */
	/**
	 * @since 5.0
	 */
	public IJobStatus getJobStatus(String jobId) {
		synchronized (fJobStatus) {
			IJobStatus status = fJobStatus.get(jobId);
			if (status == null) {
				status = new IJobStatus() {
					public String getJobId() {
						return null;
					}

					public ILaunchConfiguration getLaunchConfiguration() {
						return null;
					}

					public String getState() {
						return UNDETERMINED;
					}

					public String getStateDetail() {
						return getState();
					}

					public IStreamsProxy getStreamsProxy() {
						return null;
					}
				};
			}
			return status;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getName()
	 */
	public String getName() {
		return getConfiguration().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#
	 * getResourceManagerId()
	 */
	public String getResourceManagerId() {
		return getConfiguration().getResourceManagerId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getState()
	 */
	/**
	 * @since 5.0
	 */
	public synchronized String getState() {
		return fState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getUniqueName
	 * ()
	 */
	public String getUniqueName() {
		return getConfiguration().getUniqueName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl#removeJobListener(org
	 * .eclipse.ptp.core.listeners.IJobListener)
	 */
	/**
	 * @since 5.0
	 */
	public void removeJobListener(IJobListener listener) {
		fJobListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#setConfiguration
	 * (org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	public void setConfiguration(IResourceManagerConfiguration config) {
		synchronized (this) {
			fConfig = config;
		}

		/*
		 * Update attributes from the new configuration
		 */
		AttributeManager attrs = new AttributeManager();

		StringAttributeDefinition nameAttrDef = ElementAttributes.getNameAttributeDefinition();
		StringAttribute nameAttr = fPResourceManager.getAttribute(nameAttrDef);
		if (nameAttr != null) {
			try {
				nameAttr.setValue(config.getName());
				attrs.addAttribute(nameAttr);
			} catch (IllegalValueException e) {
			}
		}
		StringAttributeDefinition descAttrDef = ResourceManagerAttributes.getDescriptionAttributeDefinition();
		StringAttribute descAttr = fPResourceManager.getAttribute(descAttrDef);
		if (descAttr != null) {
			try {
				descAttr.setValue(config.getDescription());
				attrs.addAttribute(descAttr);
			} catch (IllegalValueException e) {
			}
		}

		fireResourceManagerChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl#setState(org.eclipse
	 * .ptp.core.elements.attributes.ResourceManagerAttributes.State)
	 */
	/**
	 * @since 5.0
	 */
	public synchronized void setState(String state) {
		fState = state;
		fireResourceManagerChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#start(IProgressMonitor monitor)
	 */
	/**
	 * @since 5.0
	 */
	public void start(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		if (getState().equals(STOPPED_STATE) || getState().equals(ERROR_STATE)) {
			setState(STARTING_STATE);
			monitor.subTask(Messages.AbstractResourceManager_1 + getName());
			try {
				doStartup(subMon.newChild(100));
			} catch (CoreException e) {
				setState(ERROR_STATE);
				throw e;
			}
			if (monitor.isCanceled()) {
				setState(STOPPED_STATE);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#stop()
	 */
	/**
	 * @since 5.0
	 */
	public void stop() throws CoreException {
		if (getState().equals(ERROR_STATE)) {
			setState(STOPPED_STATE);
			cleanUp();
		} else if (getState().equals(STARTING_STATE) || getState().equals(STARTED_STATE)) {
			try {
				doShutdown();
			} finally {
				setState(STOPPED_STATE);
				cleanUp();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#submitJob(org.eclipse.
	 * debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.core.attributes.AttributeManager,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 5.0
	 */
	public String submitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		IJobStatus status = doSubmitJob(configuration, mode, monitor);
		synchronized (fJobStatus) {
			fJobStatus.put(status.getJobId(), status);
		}
		return status.getJobId();
	}

	/**
	 * Remove all the model elements below the RM. This is called when the RM
	 * shuts down and ensures that everything is cleaned up properly.
	 */
	protected void cleanUp() {
		doCleanUp();
		synchronized (fJobStatus) {
			fJobStatus.clear();
		}
		fPResourceManager.cleanUp();
	}

	/**
	 * Perform any cleanup activities
	 */
	protected abstract void doCleanUp();

	/**
	 * Control a job.
	 * 
	 * @param jobId
	 *            ID of job to control
	 * @param operation
	 *            operation to perform on job
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 * @since 5.0
	 */
	protected abstract void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException;

	/**
	 * Perform any activities prior to disposing of the resource manager.
	 */
	protected abstract void doDispose();

	/**
	 * Stop the resource manager subsystem.
	 * 
	 * @throws CoreException
	 */
	protected abstract void doShutdown() throws CoreException;

	/**
	 * Start the resource manager subsystem.
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void doStartup(IProgressMonitor monitor) throws CoreException;

	/**
	 * Submit a job to the resource manager. Returns a job ID that represents
	 * the submitted job. Throws a core exception if there was an error
	 * submitting the job or if the progress monitor was canceled.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param mode
	 *            launch mode
	 * @param monitor
	 *            progress monitor
	 * @return job status representing the status of the submitted job
	 * @throws CoreException
	 * @since 5.0
	 */
	protected abstract IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Notify listeners when a job has changed.
	 * 
	 * @param jobId
	 *            ID of job that has changed
	 * @since 5.0
	 */
	protected void fireJobChanged(String jobId) {
		IJobChangedEvent e = new JobChangedEvent(this, jobId);

		for (Object listener : fJobListeners.getListeners()) {
			((IJobListener) listener).handleEvent(e);
		}
	}

	/**
	 * Fire an event to notify that some attributes have changed
	 * 
	 * @param attrs
	 *            attributes that have changed
	 * @since 5.0
	 */
	protected void fireResourceManagerChanged() {
		((ModelManager) fModelManager).fireResourceManagerChanged(this);
	}

	/**
	 * Propagate IResourceManagerErrorEvent to listener
	 * 
	 * @param message
	 * @since 5.0
	 */
	protected void fireResourceManagerError(String message) {
		((ModelManager) fModelManager).fireResourceManagerError(this, message);
	}

	/**
	 * @since 5.0
	 */
	protected IPResourceManager getPResourceManager() {
		return fPResourceManager;
	}
}