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
package org.eclipse.ptp.rtsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.rmsystem.IResourceManagerMonitor;

/**
 * @author greg
 * @since 5.0
 * 
 */
public abstract class AbstractRuntimeResourceManager extends AbstractResourceManager {

	private IRuntimeSystem fRuntimeSystem = null;

	/**
	 * @since 5.0
	 */
	public AbstractRuntimeResourceManager(IResourceManagerConfiguration config, IResourceManagerControl control,
			IResourceManagerMonitor monitor) {
		super(config, control, monitor);
	}

	/**
	 * create a new runtime system
	 * 
	 * @return the new runtime system
	 * @throws CoreException
	 *             TODO
	 */
	protected abstract IRuntimeSystem doCreateRuntimeSystem() throws CoreException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doDispose()
	 */
	@Override
	protected void doDispose() {
		super.doDispose();
		// TODO should call fRuntimeSystem.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doShutdown()
	 */
	@Override
	protected void doShutdown() throws CoreException {
		CoreException exception = null;
		try {
			if (fRuntimeSystem != null) {
				fRuntimeSystem.shutdown();
			}
		} catch (CoreException e) {
			// Catch exception so we can shut down control and monitor anyway
			exception = e;
		}
		super.doShutdown();
		if (exception != null) {
			throw exception;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#doStartup(org.eclipse
	 * .core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		monitor.subTask(Messages.AbstractRuntimeResourceManager_5);

		fRuntimeSystem = doCreateRuntimeSystem();

		super.doStartup(subMon.newChild(50));

		try {
			fRuntimeSystem.startup(subMon.newChild(50));
		} catch (CoreException e) {
			doShutdown();
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#fireJobChanged(java.
	 * lang.String)
	 */
	@Override
	protected void fireJobChanged(String jobId) {
		super.fireJobChanged(jobId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#fireResourceManagerChanged
	 * ()
	 */
	@Override
	protected void fireResourceManagerChanged() {
		super.fireResourceManagerChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#fireResourceManagerError
	 * (java.lang.String)
	 */
	@Override
	protected void fireResourceManagerError(String message) {
		super.fireResourceManagerError(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#fireResourceManagerStarted
	 * ()
	 */
	@Override
	protected void fireResourceManagerStarted() {
		super.fireResourceManagerStarted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#fireResourceManagerStopped
	 * ()
	 */
	@Override
	protected void fireResourceManagerStopped() {
		super.fireResourceManagerStopped();
	}

	/**
	 * @return the runtimeSystem
	 * @since 5.0
	 */
	protected IRuntimeSystem getRuntimeSystem() {
		return fRuntimeSystem;
	}

}