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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.messages.Messages;
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
public class ServiceConfigurationSelectionWidget extends Composite implements ISelectionProvider {
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
	
	private class ServiceContentProvider extends WorkbenchContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getChildren(java.lang.Object)
		 */
		@Override
		public Object[] getChildren(Object element) {
			if (element instanceof IServiceConfiguration) {
				IServiceConfiguration config = (IServiceConfiguration)element;
				Set<Object> children = new HashSet<Object>();
				for (IService service : config.getServices()) {
					children.add(config.getServiceProvider(service));
				}
				for (IService service : fManager.getServices()) {
					if (config.isDisabled(service)) {
						children.add(service);
					}
				}
				return children.toArray();
			}
			if (element instanceof IServiceModelManager && fExcludedConfigs != null) {
				Set<IServiceConfiguration> children = ((IServiceModelManager)element).getConfigurations();
				children.removeAll(fExcludedConfigs);
				return children.toArray();
			}
			return super.getChildren(element);
		}
	}
	
	private TreeViewer fViewer;
	private Button fAddButton;
	private Button fRemoveButton;
	private Button fRenameButton;
	
	private ISelection fSelection;
	private boolean fEnabled = true;
	private Set<IServiceConfiguration> fExcludedConfigs;
	private boolean fButtonsVisible = true;
	
	private final ListenerList fSelectionListeners = new ListenerList();
	private final IServiceModelManager fManager = ServiceModelManager.getInstance();
	
	private IServiceConfiguration fSelectedConfig = null;

	public ServiceConfigurationSelectionWidget(Composite parent, int style) {
		this (parent, style, null, true);
	}
	
	public ServiceConfigurationSelectionWidget(Composite parent, int style, 
			Set<IServiceConfiguration> excluded, boolean buttons) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Composite labelComp = new Composite(this, SWT.NONE);
		labelComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		labelComp.setLayout(new GridLayout(1, false));
		Label label = new Label(labelComp, SWT.NONE);
		label.setText(Messages.ServiceConfigurationSelectionWidget_0);

		Composite treeComp = new Composite(this, SWT.NONE);
		treeComp.setLayout(new GridLayout(1, false));
		treeComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		fViewer = new TreeViewer(treeComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		fViewer.setContentProvider(new ServiceContentProvider());
		fViewer.setLabelProvider(new WorkbenchLabelProvider());
		fViewer.setComparator(new ServiceConfigurationComparator());
		fViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		fViewer.getTree().setLinesVisible(true);
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = fViewer.getSelection();
				if (!selection.isEmpty()) {
					ITreeSelection treeSelection = (ITreeSelection)selection;
					TreePath path = treeSelection.getPaths()[0];
					fSelectedConfig = (IServiceConfiguration)path.getFirstSegment();
				} else {
					fSelectedConfig = null;
				}
				updateControls();
				notifySelection(fViewer.getSelection());
			}
			
		});
		fViewer.setInput(ServiceModelManager.getInstance());
		
		if (buttons) {
			Composite buttonsComp = new Composite(this, SWT.NONE);
			buttonsComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			buttonsComp.setLayout(new GridLayout(1, false));
		
			fAddButton = new Button(buttonsComp, SWT.PUSH);
			GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			data.widthHint = 110;
			fAddButton.setLayoutData(data);
			fAddButton.setText(Messages.ServiceConfigurationSelectionWidget_1);
			fAddButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					InputDialog dialog = new InputDialog(fAddButton.getShell(), Messages.ServiceConfigurationSelectionWidget_2, 
							Messages.ServiceConfigurationSelectionWidget_3, null, null);
					if (dialog.open() == InputDialog.OK) {
						IServiceConfiguration config = fManager.newServiceConfiguration(dialog.getValue());
						fManager.addConfiguration(config);
						fViewer.refresh();
					}
				}
			});
			
			fRemoveButton = new Button(buttonsComp, SWT.PUSH);
			fRemoveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			fRemoveButton.setText(Messages.ServiceConfigurationSelectionWidget_4);
			fRemoveButton.setEnabled(false);
			fRemoveButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					ITreeSelection selection = (ITreeSelection)fViewer.getSelection();
					if (!selection.isEmpty()) {
						Object[] configs = (Object[])selection.toArray();
						String names = ""; //$NON-NLS-1$
						for (int i = 0; i < configs.length; i++) {
							if (i > 0) {
								names += ", "; //$NON-NLS-1$
							}
							names += "\"" + ((IServiceConfiguration)configs[i]).getName() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
						}
						boolean doRemove = MessageDialog.openConfirm(fRemoveButton.getShell(), Messages.ServiceConfigurationSelectionWidget_9,
								NLS.bind(Messages.ServiceConfigurationSelectionWidget_10, names));
						if (doRemove) {
							for (Object config : configs) {
								fManager.remove((IServiceConfiguration)config);
							}
							fViewer.refresh();
						}
					}
				}
			});
			
			fRenameButton = new Button(buttonsComp, SWT.PUSH);
			fRenameButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			fRenameButton.setText(Messages.ServiceConfigurationSelectionWidget_11);
			fRenameButton.setEnabled(false);
			fRenameButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					ITreeSelection selection = (ITreeSelection)fViewer.getSelection();
					if (!selection.isEmpty()) {
						InputDialog dialog = new InputDialog(fAddButton.getShell(), Messages.ServiceConfigurationSelectionWidget_12, 
								Messages.ServiceConfigurationSelectionWidget_13, null, null);
						if (dialog.open() == InputDialog.OK) {
							IServiceConfiguration config = (IServiceConfiguration)selection.getFirstElement();
							config.setName(dialog.getValue());
							fViewer.update(config, null);
						}
					}
				}
			});
		}
		
		fExcludedConfigs = excluded;
		fButtonsVisible = buttons;
	}
	
	private void updateControls() {
		fViewer.getTree().setEnabled(fEnabled);
		if (fButtonsVisible) {
			fAddButton.setEnabled(fEnabled);
			boolean enabled = fEnabled && getSelectedConfiguration() != null;
			fRemoveButton.setEnabled(enabled);
			fRenameButton.setEnabled(enabled);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getEnabled()
	 */
	@Override
	public boolean getEnabled() {
		return fEnabled;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		fEnabled = enabled;
	}

	/**
	 * Adds the listener to the collection of listeners who will
	 * be notified when the users selects a service configuration
	 * </p>
	 * @param listener the listener that will be notified of the selection
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.add(listener);
	}

	/**
	 * Return the service configuration selected by the user
	 * 
	 * @return Selected service configuration
	 */
	public IServiceConfiguration getSelectedConfiguration() {
		return fSelectedConfig;
	}
	
	/**
	 * Removes the listener from the collection of listeners who will
	 * be notified when a service configuration is selected by the user.
	 *
	 * @param listener the listener which will no longer be notified
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.remove(listener);
	}
	
	/**
	 * Notify all listeners of the selection.
	 * 
	 * @param e event that was generated by the selection
	 */
	private void notifySelection(ISelection selection) {
		setSelection(selection);
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		for (Object listener : fSelectionListeners.getListeners()) {
			((ISelectionChangedListener) listener).selectionChanged(event);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return fSelection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		fSelection = selection;
	}
}
