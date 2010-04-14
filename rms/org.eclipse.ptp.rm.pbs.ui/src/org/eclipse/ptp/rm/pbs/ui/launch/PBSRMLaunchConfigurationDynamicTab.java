/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     Dieter Krachtus, University of Heidelberg
 *     Roland Schulz, University of Tennessee
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.launch;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
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
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.pbs.core.PBSJobAttributes;
import org.eclipse.ptp.rm.pbs.core.PBSNodeAttributes;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class PBSRMLaunchConfigurationDynamicTab extends BaseRMLaunchConfigurationDynamicTab {

	private class DataSource extends RMLaunchConfigurationDynamicTabDataSource {
		private boolean useDefArgs;
		private String args;
		private boolean useDefParams;
		private Map<String, String> params;

		protected DataSource(BaseRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.ui.utils.DataSource#copyFromFields()
		 */
		@Override
		protected void copyFromFields() throws ValidationException {
			useDefArgs = useArgsDefaultsButton.getSelection();
			args = extractText(argsText);

			useDefParams = useParamsDefaultsButton.getSelection();
			params.clear();
			for (Object object : paramsViewer.getCheckedElements()) {
				// if (object instanceof Parameter) {
				// Parameter param = (Parameter) object;
				// params.put(param.getName(), param.getValue());
				// }
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.ui.utils.DataSource#copyToFields()
		 */
		@Override
		protected void copyToFields() {
			applyText(argsText, args);
			useArgsDefaultsButton.setSelection(useDefArgs);
			useParamsDefaultsButton.setSelection(useDefParams);

			// if (info != null) {
			// for (Entry<String, String> param : params.entrySet()) {
			// Parameter p = info.getParameter(param.getKey());
			// if (p != null) {
			// p.setValue(param.getValue());
			// paramsViewer.setChecked(p, true);
			// paramsViewer.update(p, null);
			// }
			// }
			// }
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.ui.utils.DataSource#copyToStorage()
		 */
		@Override
		protected void copyToStorage() {
			// getConfigurationWorkingCopy().setAttribute(
			// OpenMPILaunchConfiguration.ATTR_USEDEFAULTARGUMENTS,
			// useDefArgs);
			// getConfigurationWorkingCopy().setAttribute(
			// OpenMPILaunchConfiguration.ATTR_ARGUMENTS, args);
			// getConfigurationWorkingCopy().setAttribute(
			// OpenMPILaunchConfiguration.ATTR_USEDEFAULTPARAMETERS,
			// useDefParams);
			// getConfigurationWorkingCopy().setAttribute(
			// OpenMPILaunchConfiguration.ATTR_PARAMETERS, params);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.ui.utils.DataSource#loadDefault()
		 */
		@Override
		protected void loadDefault() {
			// args = OpenMPILaunchConfigurationDefaults.ATTR_ARGUMENTS;
			// useDefArgs =
			// OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS;
			// useDefParams =
			// OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTPARAMETERS;
			// params = OpenMPILaunchConfigurationDefaults.ATTR_PARAMETERS;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.ui.utils.DataSource#loadFromStorage()
		 */
		@Override
		protected void loadFromStorage() {
			// try {
			// args = getConfiguration().getAttribute(
			// OpenMPILaunchConfiguration.ATTR_ARGUMENTS,
			// OpenMPILaunchConfigurationDefaults.ATTR_ARGUMENTS);
			// useDefArgs = getConfiguration()
			// .getAttribute(
			// OpenMPILaunchConfiguration.ATTR_USEDEFAULTARGUMENTS,
			// OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS);
			// useDefParams = getConfiguration()
			// .getAttribute(
			// OpenMPILaunchConfiguration.ATTR_USEDEFAULTPARAMETERS,
			// OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTPARAMETERS);
			// params = getConfiguration().getAttribute(
			// OpenMPILaunchConfiguration.ATTR_PARAMETERS,
			// OpenMPILaunchConfigurationDefaults.ATTR_PARAMETERS);
			// } catch (CoreException e) {
			// // TODO handle exception?
			// }
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.ui.utils.DataSource#validateLocal()
		 */
		@Override
		protected void validateLocal() throws ValidationException {
			// if (!useDefArgs && args == null)
			// throw new ValidationException(
			// Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyArguments);
			// if (!useDefParams) {
			// for (Object object : paramsViewer.getCheckedElements()) {
			// if (object instanceof Parameter) {
			// Parameter param = (Parameter) object;
			//						if (param.getValue().equals("")) //$NON-NLS-1$
			// throw new ValidationException(
			// Messages.AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyParameter);
			// }
			// }
			// }
		}
	}

	private class WidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener implements ICheckStateListener {
		public WidgetListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org
		 * .eclipse.jface.viewers.CheckStateChangedEvent)
		 */
		public void checkStateChanged(CheckStateChangedEvent event) {
			if (isEnabled()) {
				Object source = event.getSource();
				if (source == paramsViewer) {
					fireContentsChanged();
					updateControls();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.rm.ui.utils.WidgetListener#doWidgetSelected(org.eclipse
		 * .swt.events.SelectionEvent)
		 */
		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			if (e.getSource() == paramsViewer) {
				updateControls();
			} else {
				super.doWidgetSelected(e);
			}
		}
	}

	protected Composite control;
	protected Button useArgsDefaultsButton;
	protected Text argsText;
	protected Button useParamsDefaultsButton;
	protected CheckboxTableViewer paramsViewer;
	protected Table paramsTable;
	protected AttributeManager attributes = new AttributeManager();

	public PBSRMLaunchConfigurationDynamicTab(IResourceManager rm) {
		addAttributes(PBSNodeAttributes.getDefaultAttributeDefinitions());
		addAttributes(PBSJobAttributes.getDefaultAttributeDefinitions());
	}

	/**
	 * @param attrDefs
	 */
	private void addAttributes(IAttributeDefinition<?, ?, ?>[] attrDefs) {
		for (IAttributeDefinition<?, ?, ?> iAttributeDefinition : attrDefs) {
			try {
				attributes.addAttribute(iAttributeDefinition.create());
			} catch (IllegalValueException e) {
				// Should not happen with default values
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ptp.core.elements.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		final Group jobAttributesGroup = new Group(control, SWT.NONE);
		jobAttributesGroup.setText(Messages.PBSRMLaunchConfigurationDynamicTab_0);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		jobAttributesGroup.setLayout(layout);
		jobAttributesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		paramsViewer = CheckboxTableViewer.newCheckList(jobAttributesGroup, SWT.CHECK | SWT.FULL_SELECTION);
		paramsViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
				// Empty implementation.
			}

			public Object[] getElements(Object inputElement) {
				return attributes.getAttributes();
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// Empty implementation.
				System.err.println("PBSRMLaunchConfigurationDynamicTab#IStructuredContentProvider#inputChanged"); //$NON-NLS-1$
			}
		});
		paramsViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((IAttribute<?, ?, ?>) j1).getDefinition().getName().compareTo(
						((IAttribute<?, ?, ?>) j2).getDefinition().getName());
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
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		paramsTable.setLayoutData(gd);
		paramsTable.setLinesVisible(true);
		paramsTable.setHeaderVisible(true);
		paramsTable.setEnabled(true);
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

		paramsViewer.setInput(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #getAttributes(org.eclipse.ptp.core.elements.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	public IAttribute<?, ?, ?>[] getAttributes(IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #getControl()
	 */
	public Control getControl() {
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#getImage
	 * ()
	 */
	@Override
	public Image getImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#getText
	 * ()
	 */
	@Override
	public String getText() {
		return Messages.PBSRMLaunchConfigurationDynamicTab_1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.core.elements.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		// configuration.setAttribute(
		// OpenMPILaunchConfiguration.ATTR_USEDEFAULTARGUMENTS,
		// OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS);
		// configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_ARGUMENTS,
		// OpenMPILaunchConfigurationDefaults.ATTR_ARGUMENTS);
		// configuration.setAttribute(
		// OpenMPILaunchConfiguration.ATTR_USEDEFAULTPARAMETERS,
		// OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTPARAMETERS);
		// configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_PARAMETERS,
		// OpenMPILaunchConfigurationDefaults.ATTR_PARAMETERS);
		return new RMLaunchValidation(true, null);
	}

	@Override
	public void updateControls() {
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
				if (element instanceof IAttribute<?, ?, ?>) {
					String name = ((IAttribute<?, ?, ?>) element).getDefinition().getName();
					return name;
				}
				return null;
			}

		});
		column1.getColumn().setResizable(true);
		column1.getColumn().setText(Messages.PBSRMLaunchConfigurationDynamicTab_2);

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
				if (element instanceof IAttribute<?, ?, ?>) {
					return ((IAttribute<?, ?, ?>) element).getValueAsString();
				}
				return null;
			}

		});
		column2.setEditingSupport(new EditingSupport(paramsViewer) {
			@Override
			protected boolean canEdit(Object element) {
				return paramsViewer.getChecked(element);
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(paramsTable);
			}

			@Override
			protected Object getValue(Object element) {
				return ((IAttribute<?, ?, ?>) element).getValueAsString();
			}

			@Override
			protected void setValue(Object element, Object value) {
				try {
					((IAttribute<?, ?, ?>) element).setValueAsString((String) value);
				} catch (IllegalValueException e) {
					return;
				}
				getViewer().update(element, null);
				fireContentsChanged();
				updateControls();
			}
		});
		column2.getColumn().setResizable(true);
		column2.getColumn().setText(Messages.PBSRMLaunchConfigurationDynamicTab_3);

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

	/**
	 * @return
	 */
	private DataSource getLocalDataSource() {
		return (DataSource) super.getDataSource();
	}

	/**
	 * @return
	 */
	private WidgetListener getLocalListener() {
		return (WidgetListener) super.getListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * createDataSource()
	 */
	@Override
	protected RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		return new DataSource(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * createListener()
	 */
	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		return new WidgetListener(this);
	}

}