/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Standard widget for selecting a service configuration.
 * 
 * Displays a table view of service configurations so that the user can easily
 * see what services and providers are available in the configuration.
 * 
 * Provides "Add...", "Remove", and "Rename" buttons to allow the creation,
 * removal and renaming of service configurations. These buttons can be
 * enabled/disabled by using the <code>buttons</code> constructor parameter.
 * 
 * The <code>excluded</code> constructor parameter can be used to supply a list
 * of configurations to be exclude from the list.
 * 
 * The whole control can be enabled/disabled using the <code>setEnabled</code> method.
 * 
 */
public class ServiceConfigurationSelectionWidget extends Composite implements ISelectionProvider {
	/**
	 * Comparator class used to sort service configurations in ascending order
	 * by name
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
	
	/**
	 * Content provider for service configurations
	 */
	private class ServiceConfigurationContentProvider extends WorkbenchContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getChildren(java.lang.Object)
		 */
		@Override
		public Object[] getChildren(Object element) {
			List<IServiceConfiguration> children = new ArrayList<IServiceConfiguration>();
			if (fDisplayConfigs == null) {
				children.addAll(ServiceModelManager.getInstance().getConfigurations());
			} else {
				children.addAll(Arrays.asList(fDisplayConfigs));
			}
			if (fExcludedConfigs != null) {
				children.removeAll(fExcludedConfigs);
			}
			return children.toArray();
		}
	}

	private final static int TABLE_WIDTH = 400;
	private final static int TABLE_HEIGHT = 250;
	private final static int BUTTON_WIDTH = 110;
	
	private final ListenerList fSelectionListeners = new ListenerList();
	private final IServiceModelManager fManager = ServiceModelManager.getInstance();
	
	private TableViewer fTableViewer;
	private Table fTable;
	private TableColumnLayout fTableLayout;
	private Button fAddButton;
	private Button fRemoveButton;
	private Button fRenameButton;
	private Button fSelectAllButton;
	private Button fDeselectAllButton;
	
	private ISelection fSelection;
	private boolean fEnabled = true;
	private boolean fButtonsVisible = true;
	private boolean fUseCheckboxes = false;
	private Set<IService> fServices = null;
	private Set<IServiceConfiguration> fExcludedConfigs = null;
	private IServiceConfiguration[] fDisplayConfigs = null;
	private IServiceConfiguration fSelectedConfig = null;

	public ServiceConfigurationSelectionWidget(Composite parent, int style) {
		this (parent, style, null, null, true);
	}
	
	public ServiceConfigurationSelectionWidget(Composite parent, int style, 
			Set<IServiceConfiguration> excluded, Set<IService> services, boolean enableButtons) {
		super(parent, style);
		
		fServices = services;
		fUseCheckboxes = ((style & SWT.CHECK) == SWT.CHECK);
		
		GridLayout bodyLayout;
		if (enableButtons && !fUseCheckboxes) {
			bodyLayout = new GridLayout(2, false);
		} else {
			bodyLayout = new GridLayout(1, false);
		}
		bodyLayout.marginHeight = 0;
		bodyLayout.marginWidth = 0;
		setLayout(bodyLayout);
		setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		Composite tableComposite = new Composite(this, SWT.NONE);
		fTableLayout = new TableColumnLayout();
		tableComposite.setLayout(fTableLayout);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		if (fUseCheckboxes) {
			fTable = new Table(tableComposite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			fTableViewer = new CheckboxTableViewer(fTable);
			((CheckboxTableViewer)fTableViewer).addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					updateControls();
				}
			});
		} else {
			fTableViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
			fTable = fTableViewer.getTable();
		}
		
		fTable.setLayout(new TableLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = TABLE_HEIGHT;
		data.widthHint = TABLE_WIDTH;
		fTable.setLayoutData(data);
		
		/*
		 * Only add headers if there is more than one column
		 */
		if (fServices != null) {
			fTable.setHeaderVisible(true);
		}

		fTableViewer.setContentProvider(new ServiceConfigurationContentProvider());
		fTableViewer.setLabelProvider(new WorkbenchLabelProvider());
		fTableViewer.setComparator(new ServiceConfigurationComparator());
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				notifySelection(fTableViewer.getSelection());
				updateControls();
			}
		});
		
		createColumns();
		
		fTableViewer.setInput(this);
		
		if (enableButtons && !fUseCheckboxes) {
			Composite buttonsComp = new Composite(this, SWT.NONE);
			buttonsComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			buttonsComp.setLayout(new GridLayout(1, false));
		
			fAddButton = new Button(buttonsComp, SWT.PUSH);
			data = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			data.widthHint = BUTTON_WIDTH;
			fAddButton.setLayoutData(data);
			fAddButton.setText(Messages.ServiceConfigurationSelectionWidget_1);
			fAddButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					InputDialog dialog = new InputDialog(fAddButton.getShell(), Messages.ServiceConfigurationSelectionWidget_2, 
							Messages.ServiceConfigurationSelectionWidget_3, null, null);
					if (dialog.open() == InputDialog.OK) {
						IServiceConfiguration config = fManager.newServiceConfiguration(dialog.getValue());
						fManager.addConfiguration(config);
						fTableViewer.refresh();
					}
				}
			});
			
			fRemoveButton = new Button(buttonsComp, SWT.PUSH);
			fRemoveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			fRemoveButton.setText(Messages.ServiceConfigurationSelectionWidget_4);
			fRemoveButton.setEnabled(false);
			fRemoveButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					IStructuredSelection selection = (IStructuredSelection)fTableViewer.getSelection();
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
							fTableViewer.refresh();
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
					IStructuredSelection selection = (IStructuredSelection)fTableViewer.getSelection();
					if (!selection.isEmpty()) {
						InputDialog dialog = new InputDialog(fAddButton.getShell(), Messages.ServiceConfigurationSelectionWidget_12, 
								Messages.ServiceConfigurationSelectionWidget_13, null, null);
						if (dialog.open() == InputDialog.OK) {
							IServiceConfiguration config = (IServiceConfiguration)selection.getFirstElement();
							config.setName(dialog.getValue());
							fTableViewer.update(config, null);
						}
					}
				}
			});
		}
		
		if (enableButtons && fUseCheckboxes) {
			Composite buttonsComp = new Composite(this, SWT.NONE);
			buttonsComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			buttonsComp.setLayout(new GridLayout(2, false));
			
			fSelectAllButton = new Button(buttonsComp, SWT.PUSH);
			data = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			fSelectAllButton.setLayoutData(data);
			fSelectAllButton.setText(Messages.ServiceConfigurationSelectionWidget_5);
			fSelectAllButton.setEnabled(false);
			fSelectAllButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					setAllChecked(true);
				}
			});
		
			fDeselectAllButton = new Button(buttonsComp, SWT.PUSH);
			data = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			fDeselectAllButton.setLayoutData(data);
			fDeselectAllButton.setText(Messages.ServiceConfigurationSelectionWidget_6);
			fDeselectAllButton.setEnabled(false);
			fDeselectAllButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					setAllChecked(false);
				}
			});
		}
	
		fExcludedConfigs = excluded;
		fButtonsVisible = enableButtons;
		updateControls();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.add(listener);
	}
	
	/**
	 * Gets the elements that have been checked in the viewer
	 * 
	 * @return array containing the elements that are checked
	 */
	public IServiceConfiguration[] getCheckedServiceConfigurations() {
		if (fUseCheckboxes) {
			Object[] elements = ((CheckboxTableViewer)fTableViewer).getCheckedElements();
			return Arrays.asList(elements).toArray(new IServiceConfiguration[0]);
		}
		return new IServiceConfiguration[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getEnabled()
	 */
	@Override
	public boolean getEnabled() {
		return fEnabled;
	}
	
	/**
	 * Return the service configuration selected by the user
	 * 
	 * @return Selected service configuration
	 */
	public IServiceConfiguration getSelectedConfiguration() {
		return fSelectedConfig;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return fSelection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.remove(listener);
	}

	/**
	 * Sets all elements in the viewer to the given checked state.
	 * 
	 * @param state
	 */
	public void setAllChecked(boolean state) {
		if (fUseCheckboxes) {
			((CheckboxTableViewer)fTableViewer).setAllChecked(state);
			notifySelection(fTableViewer.getSelection());
		}
	}

	/**
	 * Set the service configurations to display in the viewer. Passing null
	 * will display all known configurations (default).
	 * 
	 * @param configurations configurations to display, or null to display all
	 */
	public void setConfigurations(IServiceConfiguration[] configurations) {
		fDisplayConfigs = configurations;
		fTableViewer.refresh();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		fEnabled = enabled;
		updateControls();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		fSelection = selection;
	}
	
	/**
	 * Add the named column to the viewer
	 * 
	 * @param colName name to use for the column heading
	 * @return the column
	 */
	private TableViewerColumn addColumn(String colName) {
		TableViewerColumn column = new TableViewerColumn(fTableViewer, SWT.NONE);
		column.getColumn().setResizable(true);
		column.getColumn().setMoveable(true);
		column.getColumn().setText(colName);
		PixelConverter converter = new PixelConverter(fTableViewer.getControl());
		int colWidth = converter.convertWidthInCharsToPixels(colName.length() + 1);
		fTableLayout.setColumnData(column.getColumn(), new ColumnWeightData(10, colWidth));
		return column;
	}

	/**
	 * Create the columns in the viewer. Always creates at least one column, the service
	 * configuration name. The other columns are determined by the set of services
	 * passed to the constructor.
	 */
	private void createColumns() {
		TableViewerColumn firstColumn = addColumn(Messages.ServiceConfigurationSelectionWidget_7);
		firstColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				return ((IServiceConfiguration)element).getName();
			}
		});
		
		if (fServices != null) {
			SortedSet<IService> services = new TreeSet<IService>(new Comparator<IService>() {
				public int compare(IService o1, IService o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (IService service : fServices) {
				services.add(service);
			}
			
			for (IService service : services) {
				String name = service.getName() + Messages.ServiceConfigurationSelectionWidget_8;
				final TableViewerColumn column = addColumn(name);
				column.getColumn().setData(service);
				column.setLabelProvider(new ColumnLabelProvider(){
					@Override
					public String getText(Object element) {
						IServiceConfiguration config = ((IServiceConfiguration)element);
						IService service = (IService)column.getColumn().getData();
						IServiceProvider provider = config.getServiceProvider(service);
						if (provider == null || provider.equals(service.getNullProvider())) {
							return Messages.ServiceConfigurationSelectionWidget_14;
						}
						return provider.getName();
					}
				});
			}
		}
	}
	
	/**
	 * Notify all listeners of the selection.
	 * 
	 * @param e event that was generated by the selection
	 */
	private void notifySelection(ISelection selection) {
		if (!selection.isEmpty()) {
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			fSelectedConfig = (IServiceConfiguration)structuredSelection.getFirstElement();
		} else {
			fSelectedConfig = null;
		}
		setSelection(selection);
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		for (Object listener : fSelectionListeners.getListeners()) {
			((ISelectionChangedListener) listener).selectionChanged(event);
		}
	}
	
	/**
	 * Update buttons when something changes
	 */
	private void updateControls() {
		fTable.setEnabled(fEnabled);
		if (!fUseCheckboxes && fButtonsVisible) {
			fAddButton.setEnabled(fEnabled);
			boolean enabled = fEnabled && getSelectedConfiguration() != null;
			fRemoveButton.setEnabled(enabled);
			fRenameButton.setEnabled(enabled);
		} else if (fButtonsVisible) {
			fSelectAllButton.setEnabled(fEnabled);
			fDeselectAllButton.setEnabled(fEnabled);
		}
	}
}
