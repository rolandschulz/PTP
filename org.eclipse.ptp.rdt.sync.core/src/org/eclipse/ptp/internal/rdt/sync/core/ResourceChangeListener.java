/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.core.ProjectNotConfiguredException;

public class ResourceChangeListener {
	private static final IServiceModelManager fServiceModel = ServiceModelManager.getInstance();
	private static final IService fSyncService = fServiceModel.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);

	private ResourceChangeListener() {
	}

	public static void startListening() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
	}

	public static void stopListening() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
	}

	private static class SynchronizeJob extends Job {
		private final ISyncServiceProvider fSyncProvider;
		private final IResourceDelta fDelta;

		public SynchronizeJob(IResourceDelta delta, ISyncServiceProvider provider) {
			super(Messages.ResourceChangeListener_jobName);
			fDelta = delta;
			fSyncProvider = provider;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
		 * IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(Messages.ResourceChangeListener_jobName, 1);
			try {
				fSyncProvider.synchronize(fDelta, monitor, SyncFlag.NO_FORCE);
			} catch (CoreException e) {
				System.out.println("sync failed: " + e.getLocalizedMessage()); //$NON-NLS-1$
				e.printStackTrace();
			} finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	};

	private static IResourceChangeListener resourceListener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			for (IResourceDelta delta : event.getDelta().getAffectedChildren()) {
				IProject project = delta.getResource().getProject();
				if (project != null && RemoteSyncNature.hasNature(project)) {
					IServiceConfiguration config = null;
					try {
						config = fServiceModel.getActiveConfiguration(project);
					} catch (ProjectNotConfiguredException ex) {
						//can be ignored because should only happen when project was just created
					}
					if (config != null) {
						ISyncServiceProvider provider = (ISyncServiceProvider) config.getServiceProvider(fSyncService);
						if (provider != null) {
							SynchronizeJob job = new SynchronizeJob(event.getDelta(), provider);
							job.schedule();
						}
					}
				}
			}
		}
	};
}
