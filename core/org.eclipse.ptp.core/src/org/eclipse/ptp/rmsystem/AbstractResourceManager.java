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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.jobs.IJobListener;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.core.messages.Messages;

/**
 * @author rsqrd
 * @since 5.0
 * 
 */
@Deprecated
public abstract class AbstractResourceManager implements IResourceManager {
	private final IPResourceManager fPResourceManager;
	private final AbstractResourceManagerControl fResourceManagerControl;
	private final AbstractResourceManagerMonitor fResourceManagerMonitor;

	private AbstractResourceManagerConfiguration fConfig;
	private String fState;

	/**
	 * @since 5.0
	 */
	public AbstractResourceManager(AbstractResourceManagerConfiguration config, AbstractResourceManagerControl control,
			AbstractResourceManagerMonitor monitor) {
		fConfig = config;
		fResourceManagerControl = control;
		fResourceManagerMonitor = monitor;
		fPResourceManager = ModelManager.getInstance().getUniverse().addResourceManager(config.getName(), this.getUniqueName());
		fState = STOPPED_STATE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#addJob(org.eclipse.ptp.rmsystem.IJobStatus)
	 */
	/**
	 * @since 6.0
	 */
	public void addJob(IJobStatus status) {
		JobManager.getInstance().fireJobAdded(status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#addJobListener(org.eclipse .ptp.core.listeners.IJobListener)
	 */
	/**
	 * @since 6.0
	 */
	public void addJobListener(IJobListener listener) {
		JobManager.getInstance().addListener(fConfig.getUniqueName(), listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#control(java.lang.String , java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 5.0
	 */
	public void control(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		fResourceManagerControl.control(jobId, operation, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#dispose()
	 */
	public void dispose() {
		doDispose();
		ModelManager.getInstance().getUniverse().removeResourceManager(fPResourceManager.getControlId());
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
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getConfiguration()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerConfiguration getConfiguration() {
		return fConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getControl()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerControl getControl() {
		return fResourceManagerControl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#getControlConfiguration ()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerComponentConfiguration getControlConfiguration() {
		return fResourceManagerControl.getControlConfiguration();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobControl#getControlId()
	 */
	/**
	 * @since 6.0
	 */
	public String getControlId() {
		return getConfiguration().getUniqueName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getDescription()
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
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#getJobStatus(java.lang .String, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 6.0
	 */
	public IJobStatus getJobStatus(String jobId, boolean force, IProgressMonitor monitor) {
		return fResourceManagerControl.getJobStatus(jobId, force, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#getJobStatus(java.lang .String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 6.0
	 */
	public IJobStatus getJobStatus(String jobId, IProgressMonitor monitor) {
		return fResourceManagerControl.getJobStatus(jobId, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getMonitor()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerMonitor getMonitor() {
		return fResourceManagerMonitor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#getMonitorConfiguration ()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerComponentConfiguration getMonitorConfiguration() {
		return fResourceManagerMonitor.getMonitorConfiguration();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getName()
	 */
	public String getName() {
		return getConfiguration().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getResourceManagerId()
	 */
	public String getResourceManagerId() {
		return getConfiguration().getResourceManagerId();
	}

	/**
	 * @since 5.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getState()
	 */
	public synchronized String getState() {
		return fState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getUniqueName()
	 */
	public String getUniqueName() {
		return getControlId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#removeJobListener(org .eclipse.ptp.core.listeners.IJobListener)
	 */
	/**
	 * @since 6.0
	 */
	public void removeJobListener(IJobListener listener) {
		JobManager.getInstance().removeListener(fConfig.getUniqueName(), listener);
	}

	/**
	 * Set the configuration for this resource manager. This will replace the existing configuration with a new configuration. The
	 * method is responsible for dealing with any saved state that needs to be cleaned up.
	 * 
	 * @param config
	 *            the new configuration
	 * @since 5.0
	 */
	public void setConfiguration(AbstractResourceManagerConfiguration config) {
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

	/**
	 * @since 5.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#setState(java.lang.String )
	 */
	public synchronized void setState(String state) {
		fState = state;
		fireResourceManagerChanged();
	}

	/**
	 * @since 5.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#start(org.eclipse.core .runtime.IProgressMonitor)
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
				doShutdown(); // make sure both components are shut down
				setState(STOPPED_STATE);
			}
		}
	}

	/**
	 * @since 5.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#stop()
	 */
	public void stop() throws CoreException {
		if (getState().equals(ERROR_STATE)) {
			setState(STOPPED_STATE);
		} else if (getState().equals(STARTING_STATE) || getState().equals(STARTED_STATE)) {
			try {
				doShutdown();
			} finally {
				setState(STOPPED_STATE);
			}
		}
	}

	/**
	 * @since 5.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#submitJob(org.eclipse .debug.core.ILaunchConfiguration,
	 * java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public String submitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return fResourceManagerControl.submitJob(configuration, mode, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#updateJob(org.eclipse.ptp.rmsystem.IJobStatus)
	 */
	/**
	 * @since 6.0
	 */
	public void updateJob(IJobStatus status) {
		JobManager.getInstance().fireJobChanged(status);
	}

	/**
	 * Perform any activities prior to disposing of the resource manager.
	 */
	protected void doDispose() {
		fResourceManagerControl.dispose();
		fResourceManagerMonitor.dispose();
	}

	/**
	 * Stop the resource manager control and monitor subsystems. This should only be overridden if the implementor is providing
	 * different control and monitor implementations.
	 * 
	 * @throws CoreException
	 *             this exception should be thrown if the shutdown encountered an error for any reason. This will not halt shutdown
	 *             of the resource manager, but may produce a message to the user.
	 */
	protected void doShutdown() throws CoreException {
		CoreException exception = null;
		try {
			fResourceManagerControl.stop();
		} catch (CoreException e) {
			// Catch exception so we can shut down monitor anyway
			exception = e;
		}
		fResourceManagerMonitor.stop();
		if (exception != null) {
			throw exception;
		}
	}

	/**
	 * Start the resource manager control and monitor subsystems. This should only be overridden if the implementor is providing
	 * different control and monitor implementations.
	 * 
	 * @param monitor
	 *            progress monitor indicating startup progress and for cancelling startup
	 * @throws CoreException
	 *             this exception should be thrown if the startup encountered an error for any reason. This will halt shutdown of
	 *             the resource manager, and may produce a message to the user.
	 */
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);

		fResourceManagerControl.start(subMon.newChild(50));

		try {
			fResourceManagerMonitor.start(subMon.newChild(50));
		} catch (CoreException e) {
			doShutdown();
			throw e;
		}
	}

	/**
	 * Fire an event to notify that the resource manager has changed state
	 * 
	 * @since 5.0
	 */
	protected void fireResourceManagerChanged() {
		ModelManager.getInstance().fireResourceManagerChanged(this);
	}

	/**
	 * Fire an event to notify that an error has ocurred in the resource manager
	 * 
	 * @param message
	 * @since 5.0
	 */
	protected void fireResourceManagerError(String message) {
		setState(ERROR_STATE);
		ModelManager.getInstance().fireResourceManagerError(this, message);
	}

	/**
	 * Fire an event to notify that the resource manager has started
	 * 
	 * @since 5.0
	 */
	protected void fireResourceManagerStarted() {
		setState(STARTED_STATE);
	}

	/**
	 * Fire an event to notify that the resource manager has stopped
	 * 
	 * @since 5.0
	 */
	protected void fireResourceManagerStopped() {
		setState(STOPPED_STATE);
	}

}