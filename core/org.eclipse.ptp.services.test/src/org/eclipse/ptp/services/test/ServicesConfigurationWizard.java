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
package org.eclipse.ptp.services.test;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.ui.widgets.ServiceConfigurationSelectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ServicesConfigurationWizard extends Wizard {

	private class SelectServiceConfigurationPage extends WizardPage {

		private ServiceConfigurationSelectionWidget serviceConfigWidget;

		public SelectServiceConfigurationPage(String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription("Select or modify a service configuration"); //$NON-NLS-1$
		}

		public void createControl(Composite parent) {
	        Composite composite = new Composite(parent, SWT.NULL);
	        composite.setFont(parent.getFont());
	        composite.setLayout(new GridLayout());
	        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	        
	        serviceConfigWidget = new ServiceConfigurationSelectionWidget(composite, SWT.NONE);
	        GridData data = new GridData(GridData.FILL_BOTH);
	        data.heightHint = 200;
	        serviceConfigWidget.setLayoutData(data);
	        serviceConfigWidget.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					handleSelectionChanged(event);
				}
	        });

			setControl(composite);
			setPageComplete(false);
		}
		
		public void handleSelectionChanged(SelectionChangedEvent event) {
			IServiceConfiguration config = null;
			ISelection selection = event.getSelection();
			if (!selection.isEmpty() && selection instanceof ITreeSelection) {
				ITreeSelection treeSelection = (ITreeSelection)selection;
				TreePath path = treeSelection.getPaths()[0];
				config = (IServiceConfiguration)path.getFirstSegment();
			}
			
			setPageComplete(config != null);
		}
	}

	/*
	 * Constructor used when creating a new resource manager.
	 */
	public ServicesConfigurationWizard() {
		setForcePreviousAndNextButtons(true);
		addPage(new SelectServiceConfigurationPage("Service Configurations")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		return true;
	}
}
