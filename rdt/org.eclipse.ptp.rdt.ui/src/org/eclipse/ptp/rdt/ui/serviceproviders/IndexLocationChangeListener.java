/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.serviceproviders;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelEvent;
import org.eclipse.ptp.services.core.IServiceModelEventListener;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;


/**
 * This listener gets fired when the user changes the index
 * location. It calls the index lifecycle service to move
 * the index file on the server to the new location. 
 *
 */
public class IndexLocationChangeListener implements IServiceModelEventListener {

	private static final IndexLocationChangeListener instance = new IndexLocationChangeListener();
	
	private IndexLocationChangeListener() {}
	
	
	public static void startListening() {
		ServiceModelManager.getInstance().addEventListener(instance, IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED);
	}
	
	public static void stopListening() {
		ServiceModelManager.getInstance().removeEventListener(instance);
	}
	
	
	/**
	 * Handle the event.
	 */
	public void handleEvent(IServiceModelEvent event) {
		if(event.getType() != IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED)
			return;

		final ServiceModelManager smm = ServiceModelManager.getInstance();
		IService service = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
		IServiceConfiguration config = (IServiceConfiguration) event.getSource();
		if(config.isDisabled(service))
			return;
		
		// Do nothing if the config is not actually part of the model yet.
		Set<IProject> projects = smm.getProjectsForConfiguration(config); 
		if(projects == null || projects.isEmpty())
			return;

		IServiceProvider sp = config.getServiceProvider(service);
		if(!(sp instanceof IIndexServiceProvider))
			return;
		final IIndexServiceProvider provider = (IIndexServiceProvider) sp;
		
		IServiceProvider oldProvider = event.getOldProvider();
		if(!(oldProvider instanceof IIndexServiceProvider))
			return;
		
		String oldIndexLocation = ((IIndexServiceProvider)oldProvider).getIndexLocation();
			
		// if the index location has changed
		if(!oldIndexLocation.equals(provider.getIndexLocation())) {
			final IIndexLifecycleService indexService = provider.getIndexLifeCycleService();
			Set<IProject> scopes = smm.getProjectsForConfiguration(config);
			for(IProject project : scopes) {
				final String scope = project.getName();
				
				// Could be a long running operation so spawn a job to do it
				Job job = new Job(Messages.getString("ServiceModelPropertyPage.0")) { //$NON-NLS-1$
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						final String newIndexLocation = provider.getIndexLocation();
						final String actualLocation = indexService.moveIndexFile(scope, newIndexLocation, monitor);
						
						// if the move fails display an error pop-up
						if(actualLocation == null || !actualLocation.equals(newIndexLocation)) {
							
							// The dialog can only be launched from the UI thread so we need to create a UIJob to do it
							UIJob uijob = new UIJob(Messages.getString("ServiceModelPropertyPage.0")) { //$NON-NLS-1$
								@Override
								public IStatus runInUIThread( IProgressMonitor monitor) {
									MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("ServiceModelPropertyPage.1"), Messages.getString("ServiceModelPropertyPage.2", newIndexLocation, actualLocation));  //$NON-NLS-1$ //$NON-NLS-2$
									provider.setIndexLocation(actualLocation);
									try {
										smm.saveModelConfiguration();
									} catch (IOException e) {
										RDTLog.logError(e);
									}
									return Status.OK_STATUS;
								}
							};
							uijob.schedule();
							
						}
						return Status.OK_STATUS;
					} 
				};
				job.schedule();
			}
		}
	}

}
