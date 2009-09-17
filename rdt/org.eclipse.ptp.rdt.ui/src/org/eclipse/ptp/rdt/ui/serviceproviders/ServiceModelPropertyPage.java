/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService;
import org.eclipse.ptp.internal.rdt.ui.RDTHelpContextIds;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.wizards.ConfigureRemoteServices;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.progress.UIJob;

/**
 * Remote project property page for configuring service providers
 * @author vkong
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @deprecated
 */
public class ServiceModelPropertyPage extends PropertyPage {
	
	private PropertyPageServiceModelWidget fModelWidget;
	
	// TODO this is a hack
	private String indexLocation = null;
	
	public ServiceModelPropertyPage() {
		fModelWidget = new PropertyPageServiceModelWidget();
		fModelWidget.setConfigChangeListener(new Listener() {
			public void handleEvent(Event event) {
				setValid(fModelWidget.isConfigured());
			}			
		});

		
	}

	
	private IIndexServiceProvider getIndexServiceProvider(String id) {
		return (IIndexServiceProvider) fModelWidget.getProviderIDToProviderMap().get(id);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control table = fModelWidget.createContents(parent);

		if (getElement() instanceof IProject)
			fModelWidget.updateServicesTable((IProject) getElement());
		else if (getElement() instanceof ICProject )
			fModelWidget.updateServicesTable(((ICProject) getElement()).getProject());
		
		// TODO This is a hack! 
		// TODO This should be handled with events when we move to the new service model.
		// get the initial value for the index location
		IIndexServiceProvider isp = getIndexServiceProvider(RemoteCIndexServiceProvider.ID);
		if(isp != null) {
			indexLocation = isp.getIndexLocation();
		}
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, RDTHelpContextIds.SERVICE_MODEL_PROPERTY_PAGE);
		
		return table;
	}

	@Override
	public boolean performOk() {  // called when OK or Apply is pressed
		Object element = getElement();
		final IProject project;
		if (element instanceof IProject)
			project = (IProject) element;
		else 
			project = ((ICProject) element).getProject();
		
		ConfigureRemoteServices.configure(project, fModelWidget.getServiceIDToSelectedProviderID(), fModelWidget.getProviderIDToProviderMap(), new NullProgressMonitor());
		
		final IIndexServiceProvider isp = getIndexServiceProvider(RemoteCIndexServiceProvider.ID);
		if(isp != null) {
			final String newIndexLocation = isp.getIndexLocation();
			if(indexLocation != null && !indexLocation.equals(newIndexLocation)) {
				final IIndexLifecycleService indexService = isp.getIndexLifeCycleService();
				
				Job job = new Job(Messages.getString("ServiceModelPropertyPage.0")) { //$NON-NLS-1$
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						final String actualLocation =  indexService.moveIndexFile(project.getName(), newIndexLocation, monitor);
						if(!actualLocation.equals(newIndexLocation)) {
							//isp.setIndexLocation(indexLocation); // set it back to the old location
							UIJob uijob = new UIJob(Messages.getString("ServiceModelPropertyPage.0")) { //$NON-NLS-1$
								@Override
								public IStatus runInUIThread( IProgressMonitor monitor) {
									MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("ServiceModelPropertyPage.1"), Messages.getString("ServiceModelPropertyPage.2", newIndexLocation, actualLocation));  //$NON-NLS-1$ //$NON-NLS-2$
									isp.setIndexLocation(actualLocation);
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
		
		return true;
	}

	@Override
	protected void performDefaults() {
		// TODO restore default using configuration strings
		super.performDefaults();
	}

	@Override
	public boolean isValid() {
		return fModelWidget.isConfigured();
	}
	

}
