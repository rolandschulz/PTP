/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.ui.launch;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.mpi.openmpi.core.launch.OpenMPILaunchConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.launch.OpenMPILaunchConfigurationDefaults;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.OmpiInfo;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.Parameters.Parameter;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManager;
import org.eclipse.ptp.rm.mpi.openmpi.ui.OpenMPIUIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class AdvancedOpenMpiRMLaunchConfigurationDynamicTab extends BaseRMLaunchConfigurationDynamicTab {

	protected Composite control;
	protected Button useArgsDefaultsButton;
	protected Text argsText;
	protected Button useParamsDefaultsButton;
	protected CheckboxTableViewer paramsViewer;
	protected Table paramsTable;
	protected OmpiInfo info;

	/**
	 * @since 2.0
	 */
	public AdvancedOpenMpiRMLaunchConfigurationDynamicTab(IResourceManager rm, ILaunchConfigurationDialog dialog) {
		super(dialog);
		info = ((OpenMPIResourceManager) rm).getOmpiInfo();
	}

	private class WidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener implements ICheckStateListener {
		public WidgetListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			if (e.getSource() == paramsViewer || e.getSource() == useArgsDefaultsButton) {
				updateControls();
			} else {
				super.doWidgetSelected(e);
			}
		}

		public void checkStateChanged(CheckStateChangedEvent event) {
			if (isEnabled()) {
				Object source = event.getSource();
				if (source == paramsViewer) {
					fireContentsChanged();
					updateControls();
				}
			}
		}
	}

	private class DataSource extends RMLaunchConfigurationDynamicTabDataSource {
		private boolean useDefArgs;
		private String args;
		private boolean useDefParams;
		private Map<String, String> params;

		protected DataSource(BaseRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			useDefArgs = useArgsDefaultsButton.getSelection();
			args = extractText(argsText);

			useDefParams = useParamsDefaultsButton.getSelection();
			params.clear();
			for (Object object : paramsViewer.getCheckedElements()) {
				if (object instanceof Parameter) {
					Parameter param = (Parameter) object;
					params.put(param.getName(), param.getValue());
				}
			}
		}

		@Override
		protected void copyToFields() {
			applyText(argsText, args);
			useArgsDefaultsButton.setSelection(useDefArgs);
			useParamsDefaultsButton.setSelection(useDefParams);

			if (info != null) {
				for (Entry<String, String> param : params.entrySet()) {
					Parameter p = info.getParameter(param.getKey());
					if (p != null) {
						p.setValue(param.getValue());
						paramsViewer.setChecked(p, true);
						paramsViewer.update(p, null);
					}
				}
			}
		}

		@Override
		protected void copyToStorage() {
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_USEDEFAULTARGUMENTS, useDefArgs);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_ARGUMENTS, args);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_USEDEFAULTPARAMETERS, useDefParams);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_PARAMETERS, params);
		}

		@Override
		protected void loadDefault() {
			args = OpenMPILaunchConfigurationDefaults.ATTR_ARGUMENTS;
			useDefArgs = OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS;
			useDefParams = OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTPARAMETERS;
			params = OpenMPILaunchConfigurationDefaults.ATTR_PARAMETERS;

		}

		@SuppressWarnings("unchecked")
		@Override
		protected void loadFromStorage() {
			try {
				args = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_ARGUMENTS,
						OpenMPILaunchConfigurationDefaults.ATTR_ARGUMENTS);
				useDefArgs = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_USEDEFAULTARGUMENTS,
						OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS);
				useDefParams = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_USEDEFAULTPARAMETERS,
						OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTPARAMETERS);
				params = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_PARAMETERS,
						OpenMPILaunchConfigurationDefaults.ATTR_PARAMETERS);
			} catch (CoreException e) {
				// TODO handle exception?
				OpenMPIUIPlugin.log(e);
			}
		}

		@Override
		protected void validateLocal() throws ValidationException {
			if (!useDefArgs && args == null) {
				throw new ValidationException(Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyArguments);
			}
			if (!useDefParams) {
				for (Object object : paramsViewer.getCheckedElements()) {
					if (object instanceof Parameter) {
						Parameter param = (Parameter) object;
						if (param.getValue().equals("")) { //$NON-NLS-1$
							throw new ValidationException(
									Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyParameter);
						}
					}
				}
			}
		}

		protected boolean getUseDefArgs() {
			return useDefArgs;
		}
	}

	@Override
	protected RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		return new DataSource(this);
	}

	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		return new WidgetListener(this);
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getText() {
		return Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Title;
	}

	/**
	 * @since 2.0
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		final Group argumentsGroup = new Group(control, SWT.NONE);
		argumentsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		argumentsGroup.setLayout(layout);
		argumentsGroup.setText(Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Label_LaunchArguments);

		useArgsDefaultsButton = new Button(argumentsGroup, SWT.CHECK);
		useArgsDefaultsButton.setText(Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Label_DefaultArguments);
		// useArgsDefaultsButton.setSelection(true);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		useArgsDefaultsButton.setLayoutData(gd);
		useArgsDefaultsButton.addSelectionListener(getListener());

		Label label = new Label(argumentsGroup, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Label_Arguments);

		argsText = new Text(argumentsGroup, SWT.BORDER);
		argsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// argsText.setEnabled(false);

		final Group ompiParameteresGroup = new Group(control, SWT.NONE);
		ompiParameteresGroup.setText(Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Label_MCAParameters);
		layout = new GridLayout();
		layout.numColumns = 2;
		ompiParameteresGroup.setLayout(layout);
		ompiParameteresGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		useParamsDefaultsButton = new Button(ompiParameteresGroup, SWT.CHECK);
		useParamsDefaultsButton.addSelectionListener(getListener());
		useParamsDefaultsButton.setText(Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Label_DefaultMCAParameters);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		useParamsDefaultsButton.setLayoutData(gd);
		// useParamsDefaultsButton.setSelection(true);

		paramsViewer = CheckboxTableViewer.newCheckList(ompiParameteresGroup, SWT.CHECK | SWT.FULL_SELECTION);
		paramsViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
				// Empty implementation.
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement != null && inputElement instanceof OmpiInfo) {
					OmpiInfo info = (OmpiInfo) inputElement;
					return info.getParameters();
				}
				return null;
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// Empty implementation.
			}
		});
		paramsViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((Parameter) j1).getName().compareTo(((Parameter) j2).getName());
			}
		});
		paramsViewer.addCheckStateListener(getLocalListener());
		paramsViewer.setAllChecked(false);

		// Enable cursor keys in table
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(paramsViewer,
				new FocusCellOwnerDrawHighlighter(paramsViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(paramsViewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};
		TableViewerEditor.create(paramsViewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		paramsTable = paramsViewer.getTable();
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		paramsTable.setLayoutData(gd);
		paramsTable.setLinesVisible(true);
		paramsTable.setHeaderVisible(true);
		paramsTable.setEnabled(false);
		// Disable cell item selection
		paramsTable.addListener(SWT.EraseItem, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.
			 * widgets.Event)
			 */
			public void handleEvent(Event event) {
				event.detail &= ~SWT.SELECTED;
			}
		});

		addColumns();

		if (info != null) {
			paramsViewer.setInput(info);
		}

	}

	/**
	 * Add columns to the table viewer
	 */
	private void addColumns() {
		/*
		 * Name column
		 */
		final TableViewerColumn column1 = new TableViewerColumn(paramsViewer, SWT.NONE);
		column1.setLabelProvider(new ColumnLabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang
			 * .Object)
			 */
			@Override
			public String getText(Object element) {
				if (element instanceof Parameter) {
					String name = ((Parameter) element).getName();
					return name;
				}
				return null;
			}

		});
		column1.getColumn().setResizable(true);
		column1.getColumn().setText(Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_PArameterTable_Column_Name);

		/*
		 * Value column
		 */
		final TableViewerColumn column2 = new TableViewerColumn(paramsViewer, SWT.NONE);
		column2.setLabelProvider(new ColumnLabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang
			 * .Object)
			 */
			@Override
			public String getText(Object element) {
				if (element instanceof Parameter) {
					return ((Parameter) element).getValue();
				}
				return null;
			}

		});
		column2.setEditingSupport(new EditingSupport(paramsViewer) {
			@Override
			protected boolean canEdit(Object element) {
				return !((Parameter) element).isReadOnly() && paramsViewer.getChecked(element);
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(paramsTable);
			}

			@Override
			protected Object getValue(Object element) {
				return ((Parameter) element).getValue();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((Parameter) element).setValue((String) value);
				getViewer().update(element, null);
				fireContentsChanged();
				updateControls();
			}
		});
		column2.getColumn().setResizable(true);
		column2.getColumn().setText(Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_ParameterTable_Column_Value);

		paramsTable.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle area = paramsTable.getClientArea();
				// Point size = paramsTable.computeSize(SWT.DEFAULT,
				// SWT.DEFAULT);
				ScrollBar vBar = paramsTable.getVerticalBar();
				int width = area.width - paramsTable.computeTrim(0, 0, 0, 0).width - vBar.getSize().x;
				paramsTable.getColumn(1).setWidth(width / 3);
				paramsTable.getColumn(0).setWidth(width - paramsTable.getColumn(1).getWidth());
			}
		});

	}

	public Control getControl() {
		return control;
	}

	/**
	 * @since 2.0
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_USEDEFAULTARGUMENTS,
				OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_ARGUMENTS, OpenMPILaunchConfigurationDefaults.ATTR_ARGUMENTS);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_USEDEFAULTPARAMETERS,
				OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTPARAMETERS);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_PARAMETERS, OpenMPILaunchConfigurationDefaults.ATTR_PARAMETERS);
		return new RMLaunchValidation(true, null);
	}

	@Override
	public void updateControls() {
		argsText.setEnabled(!useArgsDefaultsButton.getSelection());
		paramsTable.setEnabled(!useParamsDefaultsButton.getSelection());
		if (getLocalDataSource().getUseDefArgs()) {
			String launchArgs = ""; //$NON-NLS-1$
			try {
				launchArgs = OpenMPILaunchConfiguration.calculateArguments(getLocalDataSource().getConfiguration());
			} catch (CoreException e) {
				// ignore
			}
			argsText.setText(launchArgs);
		}
	}

	private DataSource getLocalDataSource() {
		return (DataSource) super.getDataSource();
	}

	private WidgetListener getLocalListener() {
		return (WidgetListener) super.getListener();
	}

}
