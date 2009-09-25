/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.ptp.services.ui.widgets;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Standard widget for selecting a service configuration.
 * 
 * Displays a tree view of service configurations so that the user can easily
 * see what services and providers are available in the configuration.
 * 
 */
public class ServiceConfigurationSelectionWidget extends Composite {
	/**
	 * Comparator class used to sort service configurations in ascending order
	 * by name
	 * 
	 */
	private class ServiceConfigurationComparator extends ViewerComparator {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public int compare(Viewer viewer, Object o1, Object o2) {
			if (o1 instanceof IServiceConfiguration && o2 instanceof IServiceConfiguration) {
				return ((IServiceConfiguration)o1).getName().compareToIgnoreCase(((IServiceConfiguration)o2).getName());
			}
			
			return super.compare(viewer, o1, o2);
		}
	}
	
	private TreeViewer fViewer;
	private Button fAddButton;
	private Button fRemoveButton;
	private Button fRenameButton;

	private IServiceModelManager fManager = ServiceModelManager.getInstance();
	
	private IServiceConfiguration fSelectedConfig;

	public ServiceConfigurationSelectionWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Composite labelComp = new Composite(this, SWT.NONE);
		labelComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		labelComp.setLayout(new GridLayout(1, false));
		Label label = new Label(labelComp, SWT.NONE);
		label.setText("Service Configurations:");

		Composite treeComp = new Composite(this, SWT.NONE);
		treeComp.setLayout(new GridLayout(1, false));
		treeComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		fViewer = new TreeViewer(treeComp, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		fViewer.setContentProvider(new WorkbenchContentProvider());
		fViewer.setLabelProvider(new WorkbenchLabelProvider());
		fViewer.setComparator(new ServiceConfigurationComparator());
		fViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		fViewer.getTree().setLinesVisible(true);
		fViewer.getTree().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				boolean enable = (fViewer.getTree().getSelectionCount() > 0);
				fRemoveButton.setEnabled(enable);
				fRenameButton.setEnabled(enable);
				if (enable) {
					fSelectedConfig = (IServiceConfiguration)fViewer.getTree().getSelection()[0];
				} else {
					fSelectedConfig = null;
				}
			}
		});
		fViewer.setInput(ServiceModelManager.getInstance());
		
		Composite buttonsComp = new Composite(this, SWT.NONE);
		buttonsComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		buttonsComp.setLayout(new GridLayout(1, false));
	
		fAddButton = new Button(buttonsComp, SWT.PUSH);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 110;
		fAddButton.setLayoutData(data);
		fAddButton.setText("Add...");
		fAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				InputDialog dialog = new InputDialog(fAddButton.getShell(), "Add Service Configuration", 
						"Enter a name for the new service configuration:", null, null);
				if (dialog.open() == InputDialog.OK) {
					IServiceConfiguration config = fManager.newServiceConfiguration(dialog.getValue());
					fManager.addConfiguration(config);
					fViewer.refresh();
				}
			}
		});
		
		fRemoveButton = new Button(buttonsComp, SWT.PUSH);
		fRemoveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		fRemoveButton.setText("Remove");
		fRemoveButton.setEnabled(false);
		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				ITreeSelection selection = (ITreeSelection)fViewer.getSelection();
				if (!selection.isEmpty()) {
					IServiceConfiguration config = (IServiceConfiguration)selection.getFirstElement();
					boolean doRemove = MessageDialog.openConfirm(fRemoveButton.getShell(), "Remove Service Configuration",
							NLS.bind("Are you sure you want to remove the service configuration \"{0}\"?", config.getName()));
					if (doRemove) {
						fManager.remove(config);
						fViewer.refresh();
					}
				}
			}
		});
		
		fRenameButton = new Button(buttonsComp, SWT.PUSH);
		fRenameButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		fRenameButton.setText("Rename");
		fRenameButton.setEnabled(false);
		fRenameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				ITreeSelection selection = (ITreeSelection)fViewer.getSelection();
				if (!selection.isEmpty()) {
					InputDialog dialog = new InputDialog(fAddButton.getShell(), "Rename Service Configuration", 
							"Enter a new name for the new service configuration:", null, null);
					if (dialog.open() == InputDialog.OK) {
						IServiceConfiguration config = (IServiceConfiguration)selection.getFirstElement();
						config.setName(dialog.getValue());
						fViewer.update(config, null);
					}
				}
			}
		});
	}

	/**
	 * Return the service configuration selected by the user
	 * 
	 * @return Selected service configuration
	 */
	public IServiceConfiguration getSelectedConfiguration() {
		return fSelectedConfig;
	}
}
