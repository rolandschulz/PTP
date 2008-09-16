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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPILaunchAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.Parameters;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.Parameters.Parameter;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManager;
import org.eclipse.ptp.rm.mpi.openmpi.ui.OpenMPIUIPlugin;
import org.eclipse.ptp.utils.ui.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class OpenMPIRMLaunchConfigurationDynamicTab extends
	AbstractRMLaunchConfigurationDynamicTab {
	
	private class DataSource {
		private int numProcs;
		private boolean bySlot;
		private boolean noOversubscribe;
		private boolean noLocal;
		private boolean usePrefix;
		private boolean useHostFile;
		private boolean useHostList;
		private boolean useDefArgs;
		private boolean useDefParams;
		private String prefix;
		private String hostFile;
		private String hostList;
		private String args;
		private Map<String, String> params;
		private RMLaunchValidation validation;

		private final int numProcsDefault = 1;
		private final boolean bySlotDefault = false;
		private final boolean noOversubscribeDefault = false;
		private final boolean noLocalDefault = false;
		private final boolean usePrefixDefault = false;
		private final boolean useHostFileDefault = false;
		private final boolean useHostListDefault = false;
		private final boolean useDefArgsDefault = true;
		private final boolean useDefParamsDefault = true;
		private final String prefixDefault = null;
		private final String hostFileDefault = EMPTY_STRING;
		private final String hostListDefault = EMPTY_STRING;
		private final String argsDefault = EMPTY_STRING;
		private final Map<String, String> paramsDefault = new HashMap<String, String>();

		public DataSource() {
		}
		
		public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
			resetValidation();
			loadConfig(configuration);
			validateLocal();
			validateGlobal();
			copyToFields();
			return validation;
		}
		
		public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
			resetValidation();
			copyFromFields();
			validateLocal();
			validateGlobal();
			storeConfig(configuration);
			return validation;
		}
		
		public RMLaunchValidation validateFields() {
			resetValidation();
			copyFromFields();
			validateLocal();
			validateGlobal();
			return validation;
		}

		private void applyText(Text t, String s) {
			if (s == null) t.setText(EMPTY_STRING);
			else t.setText(s);
		}

		private void copyFromFields() {
			numProcs = numProcsSpinner.getSelection(); 
			bySlot = bySlotButton.getSelection();
			noOversubscribe = noOversubscribeButton.getSelection();
			noLocal = noLocalButton.getSelection();
			usePrefix = usePrefixButton.getSelection();
			prefix = extractText(prefixText);
			useHostFile = hostFileButton.getSelection();
			hostFile = extractText(hostFileText);
			useHostList = hostListButton.getSelection();
			hostList = extractText(hostListText);
			useDefArgs = useArgsDefaultsButton.getSelection();
			args = extractText(argsText);
			useDefParams = useParamsDefaultsButton.getSelection();
			
			params.clear();
			for (Object object : paramsViewer.getCheckedElements()) {
				if (object instanceof Parameter) {
					Parameter param = (Parameter)object;
					params.put(param.getName(), param.getValue());
				}
			}
		}

		private void copyToFields() {
			numProcsSpinner.setSelection(numProcs);
			bySlotButton.setSelection(bySlot);
			noOversubscribeButton.setSelection(noOversubscribe);
			noLocalButton.setSelection(noLocal);
			usePrefixButton.setSelection(usePrefix);
			applyText(prefixText, prefix);
			applyText(hostFileText, hostFile);
			hostFileButton.setSelection(useHostFile);
			applyText(hostListText, hostListToText(hostList));
			hostListButton.setSelection(useHostList);
			applyText(argsText, args);
			useArgsDefaultsButton.setSelection(useDefArgs);
			useParamsDefaultsButton.setSelection(useDefParams);
			
			if (ompiParameters != null) {
				for (Entry<String, String>param : params.entrySet()) {
					Parameter p = ompiParameters.getParameter(param.getKey());
					if (p != null) {
						p.setValue(param.getValue());
						paramsViewer.setChecked(p, true);
						paramsViewer.update(p, null);
					}
				}
			}
		}
		
		private String extractText(Text text) {
			String s = text.getText().trim();
			return (s.length() == 0 ? null : s);
		}

		private void loadConfig(ILaunchConfiguration configuration) {
			try {
				numProcs = configuration.getAttribute(ATTR_NUMPROCS, numProcsDefault);
				bySlot = configuration.getAttribute(ATTR_BYSLOT, bySlotDefault);
				noOversubscribe = configuration.getAttribute(ATTR_NOOVERSUBSCRIBE, noOversubscribeDefault);
				noLocal = configuration.getAttribute(ATTR_NOLOCAL, noLocalDefault);
				usePrefix = configuration.getAttribute(ATTR_USEPREFIX, usePrefixDefault);
				prefix = configuration.getAttribute(ATTR_PREFIX, prefixDefault);
				hostFile = configuration.getAttribute(ATTR_HOSTFILE, hostFileDefault);
				useHostFile = configuration.getAttribute(ATTR_USEHOSTFILE, useHostFileDefault);
				hostList = configuration.getAttribute(ATTR_HOSTLIST, hostListDefault);
				useHostList = configuration.getAttribute(ATTR_USEHOSTLIST, useHostListDefault);
				args = configuration.getAttribute(ATTR_ARGUMENTS, argsDefault);
				useDefArgs = configuration.getAttribute(ATTR_USEDEFAULTARGUMENTS, useDefArgsDefault);
				useDefParams = configuration.getAttribute(ATTR_USEDEFAULTPARAMETERS, useDefParamsDefault);
				params = configuration.getAttribute(ATTR_PARAMETERS, paramsDefault);
			} catch (CoreException e) {
				validation = new RMLaunchValidation(false, e.getMessage());
			}
		}

		private void loadDefaultConfig() {
			numProcs = numProcsDefault;
			bySlot = bySlotDefault;
			noOversubscribe = noOversubscribeDefault;
			noLocal = noLocalDefault;
			usePrefix = usePrefixDefault;
			prefix = prefixDefault;
			hostFile = hostFileDefault;
			useHostFile = useHostFileDefault;
			hostList = hostListDefault;
			useHostList = useHostListDefault;
			args = argsDefault;
			useDefArgs = useDefArgsDefault;
			useDefParams = useDefParamsDefault;
			params = paramsDefault;
		}

		private void resetValidation() {
			validation = new RMLaunchValidation(true, "");
		}
		
		private void storeConfig(ILaunchConfigurationWorkingCopy configuration) {
			configuration.setAttribute(ATTR_NUMPROCS, numProcs);
			configuration.setAttribute(ATTR_BYSLOT, bySlot);
			configuration.setAttribute(ATTR_NOOVERSUBSCRIBE, noOversubscribe);
			configuration.setAttribute(ATTR_NOLOCAL, noLocal);
			configuration.setAttribute(ATTR_USEPREFIX, usePrefix);
			configuration.setAttribute(ATTR_PREFIX, prefix);
			configuration.setAttribute(ATTR_USEHOSTFILE, useHostFile);
			configuration.setAttribute(ATTR_HOSTFILE, hostFile);
			configuration.setAttribute(ATTR_USEHOSTLIST, useHostList);
			configuration.setAttribute(ATTR_HOSTLIST, hostList);
			configuration.setAttribute(ATTR_USEDEFAULTARGUMENTS, useDefArgs);
			configuration.setAttribute(ATTR_ARGUMENTS, args);
			configuration.setAttribute(ATTR_USEDEFAULTPARAMETERS, useDefParams);
			configuration.setAttribute(ATTR_PARAMETERS, params);
		}

		private void validateGlobal() {
			// Nothing yet.
		}

		private void validateLocal()  {
			if (numProcs < 1) {
				validation = new RMLaunchValidation(false, "Must specify at least one process");
			}
			if (usePrefix && prefix == null) {
				validation = new RMLaunchValidation(false, "Prefix cannot be empty");
			}
			if (useHostFile && hostFile == null) {
				validation = new RMLaunchValidation(false, "Must provide a host file");
			}
			if (useHostList && hostList == null) {
				validation = new RMLaunchValidation(false, "Must provide at least one host name");
			}
			if (useDefArgs && args == null) {
				validation = new RMLaunchValidation(false, "Arguments cannot be empty");
			}
			if (!useDefParams) {
				for (Object object : paramsViewer.getCheckedElements()) {
					if (object instanceof Parameter) {
						Parameter param = (Parameter)object;
						if (param.getValue().equals("")) {
							validation = new RMLaunchValidation(false, "Parameter value cannot be empty");						
						}
					}
				}
			}
		}
	}
	
	private class WidgetListener implements ModifyListener, SelectionListener, ICheckStateListener {
		private boolean listenerEnabled = true;

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
		 */
		public void checkStateChanged(CheckStateChangedEvent event) {
			if (listenerEnabled) {
				Object source = event.getSource();
				if (source == paramsViewer) {
					fireContentsChanged();
					updateControls();
				}
			}
		}
		
		public void disable() { listenerEnabled = false; }
		public void enable() { listenerEnabled = true; }

		public void modifyText(ModifyEvent evt) {
			if (! listenerEnabled) return;
			Object source = evt.getSource();
			if (source == prefixText || 
					source == numProcsSpinner ||
					source == hostFileText ||
					source == hostListText) {
				fireContentsChanged();
				updateControls();
			} else {
				assert false;
			}
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
			// Empty.
		}
		
		public void widgetSelected(SelectionEvent e) {
			if (! listenerEnabled) return;
			Object source = e.getSource();
			if (source == bySlotButton || 
					source == noOversubscribeButton || 
					source == noLocalButton || 
					source == usePrefixButton ||
					source == hostFileButton ||
					source == hostListButton ||
					source == useArgsDefaultsButton ||
					source == useParamsDefaultsButton) {
				fireContentsChanged();
				updateControls();
			} else {
				assert false;
			}
		}
	}
	
	private static final String ATTR_BASE = OpenMPIUIPlugin.PLUGIN_ID + ".launchAttributes";
	private static final String ATTR_NUMPROCS = ATTR_BASE + ".numProcs";
	private static final String ATTR_BYSLOT = ATTR_BASE + ".bySlot";
	private static final String ATTR_NOOVERSUBSCRIBE = ATTR_BASE + ".noOversubscribe";
	private static final String ATTR_NOLOCAL = ATTR_BASE + ".noLocal";
	private static final String ATTR_PREFIX = ATTR_BASE + ".prefix";
	private static final String ATTR_USEPREFIX = ATTR_BASE + ".usePrefix";
	private static final String ATTR_HOSTFILE = ATTR_BASE + ".hostFile";
	private static final String ATTR_USEHOSTFILE = ATTR_BASE + ".useHostFile";
	private static final String ATTR_HOSTLIST = ATTR_BASE + ".hostList";
	private static final String ATTR_USEHOSTLIST = ATTR_BASE + ".useHostList";
	private static final String ATTR_ARGUMENTS = ATTR_BASE + ".arguments";
	private static final String ATTR_USEDEFAULTARGUMENTS = ATTR_BASE + ".useDefaultArguments";
	
	private static final String ATTR_PARAMETERS = ATTR_BASE + ".parameters";
	private static final String ATTR_USEDEFAULTPARAMETERS = ATTR_BASE + ".useDefaultParameters";
	private Composite control;
	private Spinner numProcsSpinner;
	private Text prefixText;
	private Text hostFileText;
	private Text hostListText;
	private Text argsText;
	private Button bySlotButton;
	private Button noOversubscribeButton;
	private Button noLocalButton;
	private Button usePrefixButton;
	private Button hostFileButton;
	private Button hostListButton;
	private Button useArgsDefaultsButton;
	private Button useParamsDefaultsButton;
	private CheckboxTableViewer paramsViewer;
	private Table paramsTable;
	private String launchArgs = EMPTY_STRING;
	
	Parameters ompiParameters;
	
	WidgetListener widgetListener = new WidgetListener();
		
	DataSource dataSource = new DataSource();

	public OpenMPIRMLaunchConfigurationDynamicTab(IResourceManager rm) {
		ompiParameters = ((OpenMPIResourceManager)rm).getParameters();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#canSave(org.eclipse.swt.widgets.Control, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		return dataSource.validateFields();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public void createControl(Composite parent,	IResourceManager rm, IPQueue queue) {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		control.setLayout(layout);

		final TabFolder tabFolder = new TabFolder(control, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TabItem simpleTabItem = new TabItem(tabFolder, SWT.NONE);
		simpleTabItem.setText("Simple");

		final Composite simpleComposite = new Composite(tabFolder, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		simpleComposite.setLayout(layout);
		simpleTabItem.setControl(simpleComposite);

		Label label  = new Label(simpleComposite, SWT.NONE);
		label.setText("Number of processes:");

		numProcsSpinner = new Spinner(simpleComposite, SWT.BORDER);
		numProcsSpinner.addModifyListener(widgetListener);
		numProcsSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		final Group optionsGroup = new Group(simpleComposite, SWT.NONE);
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		optionsGroup.setText("Options");
		layout = new GridLayout();
		layout.numColumns = 3;
		optionsGroup.setLayout(layout);

		bySlotButton = new Button(optionsGroup, SWT.CHECK);
		bySlotButton.addSelectionListener(widgetListener);
		bySlotButton.setText("By slot");

		noOversubscribeButton = new Button(optionsGroup, SWT.CHECK);
		noOversubscribeButton.addSelectionListener(widgetListener);
		noOversubscribeButton.setText("No oversubscribe");

		noLocalButton = new Button(optionsGroup, SWT.CHECK);
		noLocalButton.addSelectionListener(widgetListener);
		noLocalButton.setText("No local");

		usePrefixButton = new Button(optionsGroup, SWT.CHECK);
		usePrefixButton.addSelectionListener(widgetListener);
		usePrefixButton.setText("Prefix:");

		prefixText = new Text(optionsGroup, SWT.BORDER);
		prefixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		prefixText.addModifyListener(widgetListener);

		final Group hostGroup = new Group(simpleComposite, SWT.NONE);
		hostGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		hostGroup.setText("Hosts");
		layout = new GridLayout();
		layout.numColumns = 3;
		hostGroup.setLayout(layout);

		hostFileButton = new Button(hostGroup, SWT.CHECK);
		hostFileButton.addSelectionListener(widgetListener);
		hostFileButton.setText("Host file:");

		hostFileText = new Text(hostGroup, SWT.BORDER);
		hostFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		hostFileText.addModifyListener(widgetListener);

		final Button browseButton = new Button(hostGroup, SWT.NONE);
		browseButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		browseButton.addSelectionListener(widgetListener);
		PixelConverter pixelconverter = new PixelConverter(control);
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.widthHint = pixelconverter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		browseButton.setLayoutData(gd);
		browseButton.setText("Browse");

		hostListButton = new Button(hostGroup, SWT.CHECK);
		hostListButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		hostListButton.addSelectionListener(widgetListener);
		hostListButton.setText("Host list:");

		hostListText = new Text(hostGroup, SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 20;
		hostListText.setLayoutData(gd);
		hostListText.addModifyListener(widgetListener);

		final TabItem advancedTabItem = new TabItem(tabFolder, SWT.NONE);
		advancedTabItem.setText("Advanced");

		final Composite advancedComposite = new Composite(tabFolder, SWT.NONE);
		advancedComposite.setLayout(new GridLayout());
		advancedTabItem.setControl(advancedComposite);

		final Group argumentsGroup = new Group(advancedComposite, SWT.NONE);
		argumentsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		layout = new GridLayout();
		layout.numColumns = 3;
		argumentsGroup.setLayout(layout);
		argumentsGroup.setText("Launch arguments");

		useArgsDefaultsButton = new Button(argumentsGroup, SWT.CHECK);
		useArgsDefaultsButton.setSelection(true);
		final GridData gd_useArgsDefaultsButton = new GridData();
		useArgsDefaultsButton.setLayoutData(gd_useArgsDefaultsButton);
		useArgsDefaultsButton.addSelectionListener(widgetListener);

		label = new Label(argumentsGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		label.setText("Use default arguments");
		new Label(argumentsGroup, SWT.NONE); // filler

		label = new Label(argumentsGroup, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Arguments:");

		argsText = new Text(argumentsGroup, SWT.BORDER);
		argsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		argsText.setEnabled(false);

		final Group ompiParameteresGroup = new Group(advancedComposite, SWT.NONE);
		ompiParameteresGroup.setText("MCA Parameters");
		layout = new GridLayout();
		layout.numColumns = 3;
		ompiParameteresGroup.setLayout(layout);
		ompiParameteresGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		useParamsDefaultsButton = new Button(ompiParameteresGroup, SWT.CHECK);
		useParamsDefaultsButton.addSelectionListener(widgetListener);
		useParamsDefaultsButton.setSelection(true);

		label = new Label(ompiParameteresGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		label.setText("Use default parameters");
		new Label(ompiParameteresGroup, SWT.NONE); // filler

		paramsViewer = CheckboxTableViewer.newCheckList(ompiParameteresGroup, SWT.CHECK|SWT.FULL_SELECTION);
		paramsViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				if (inputElement != null && inputElement instanceof Parameters) {
					Parameters params = (Parameters)inputElement;
					return params.getParameters();
				}
				return null;
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		paramsViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((Parameter)j1).getName().compareTo(((Parameter)j2).getName());
			}
		});
		paramsViewer.addCheckStateListener(widgetListener);
		paramsViewer.setAllChecked(false);

		// Enable cursor keys in table
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(paramsViewer, 
				new FocusCellOwnerDrawHighlighter(paramsViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(paramsViewer) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};
		TableViewerEditor.create(paramsViewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
		
		paramsTable = paramsViewer.getTable();
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		paramsTable.setLayoutData(gd);
		paramsTable.setLinesVisible(true);
		paramsTable.setHeaderVisible(true);
		paramsTable.setEnabled(false);
		// Disable cell item selection
		paramsTable.addListener(SWT.EraseItem, new Listener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				event.detail &= ~SWT.SELECTED;
			}
		});

		addColumns();
		
		if (ompiParameters != null) {
			paramsViewer.setInput(ompiParameters);
		}
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#getAttributes(org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IAttribute<?,?,?>[] getAttributes(IResourceManager rm, IPQueue queue,
			ILaunchConfiguration configuration) throws CoreException {
		dataSource.loadConfig(configuration);

		List<IAttribute<?,?,?>> attrs = new ArrayList<IAttribute<?,?,?>>();
		try {
			attrs.add(JobAttributes.getNumberOfProcessesAttributeDefinition().create(dataSource.numProcs));
		} catch (IllegalValueException e) {
			// TODO: Handle this exception?
			Assert.isTrue(false);
		} 
		attrs.add(OpenMPILaunchAttributes.getLaunchArgumentsAttributeDefinition().create(dataSource.args));
		
		return attrs.toArray(new IAttribute<?,?,?>[attrs.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#getControl()
	 */
	public Control getControl() {
		return control;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#initializeFrom(org.eclipse.swt.widgets.Control, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		widgetListener.disable();
		RMLaunchValidation validation = dataSource.initializeFrom(control, rm, queue, configuration);
		updateControls();
		widgetListener.enable();
		return validation;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#isValid(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration configuration,
			IResourceManager rm, IPQueue queue) {
		return dataSource.validateFields();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		return dataSource.performApply(configuration, rm, queue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		// TODO: Set default to number of processes equal to number of hosts in the RM.
		configuration.setAttribute(ATTR_NUMPROCS, dataSource.numProcsDefault);
		configuration.setAttribute(ATTR_BYSLOT, dataSource.bySlotDefault);
		configuration.setAttribute(ATTR_NOOVERSUBSCRIBE, dataSource.noOversubscribeDefault);
		configuration.setAttribute(ATTR_NOLOCAL, dataSource.noLocalDefault);
		configuration.setAttribute(ATTR_USEPREFIX, dataSource.usePrefixDefault);
		configuration.setAttribute(ATTR_PREFIX, dataSource.prefixDefault);
		return new RMLaunchValidation(true, "");
	}

	/**
	 * Add columns to the table viewer
	 */
	private void addColumns() {
		/*
		 * Name column
		 */
		final TableViewerColumn column1 = new TableViewerColumn(paramsViewer, SWT.NONE);
		column1.setLabelProvider(new ColumnLabelProvider(){
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
			 */
			@Override
			public String getText(Object element) {
				if (element instanceof Parameter) {
					String name = ((Parameter)element).getName();
					return name;
				}
				return null;
			}
			
		});
		column1.getColumn().setResizable(true);
		column1.getColumn().setText("Name");
	
		/*
		 * Value column
		 */
		final TableViewerColumn column2 = new TableViewerColumn(paramsViewer, SWT.NONE);
		column2.setLabelProvider(new ColumnLabelProvider(){
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
			 */
			@Override
			public String getText(Object element) {
				if (element instanceof Parameter) {
					return ((Parameter)element).getValue();
				}
				return null;
			}
			
		});
		column2.setEditingSupport(new EditingSupport(paramsViewer) {
			@Override
			protected boolean canEdit(Object element) {
				return !((Parameter)element).isReadOnly() && paramsViewer.getChecked(element);
			}
			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(paramsTable);
			}
			@Override
			protected Object getValue(Object element) {
				return ((Parameter)element).getValue();
			}
			@Override
			protected void setValue(Object element, Object value) {
				((Parameter)element).setValue((String)value);
				getViewer().update(element, null);
				fireContentsChanged();
				updateControls();
			}
		});
		column2.getColumn().setResizable(true);
		column2.getColumn().setText("Value");
		
		paramsTable.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle area = paramsTable.getClientArea();
				//Point size = paramsTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				ScrollBar vBar = paramsTable.getVerticalBar();
				int width = area.width - paramsTable.computeTrim(0,0,0,0).width - vBar.getSize().x;
				paramsTable.getColumn(1).setWidth(width/3);
				paramsTable.getColumn(0).setWidth(width - paramsTable.getColumn(1).getWidth());
			}
		});

	}

   /**
	 * Convert a comma separated list into one host per line
	 * 
	 * @param list
	 * @return
	 */
	private String hostListToText(String list) {
		String result = "";
		String[] values = list.split(",");
		for (int i = 0; i < values.length; i++) {
			if (!values[i].equals("")) {
				if (i > 0) {
					result += "\r";
				}
				result += values[i];
			}
		}
		return result;
	}
	
	/**
	 * Make string suitable for passing as an argument
	 * 
	 * @param s
	 * @return
	 */
	private String fixString(String s) {
		if (s == null) {
			return "\"\"";
		}
		return "\"" + s + "\"";
	}
	
	/**
	 * If we're using default arguments, compute what they
	 * should be.
	 */
	private void updateArguments() {
		if (dataSource.useDefArgs) {
			launchArgs = "-np " + dataSource.numProcs;
			if (dataSource.bySlot) {
				launchArgs += " -byslot";
			} 
			if (dataSource.noOversubscribe) {
				launchArgs += " -nooversubscribe";
			}
			if (dataSource.noLocal) {
				launchArgs += " -nolocal";
			}
			if (dataSource.usePrefix) {
				launchArgs += " --prefix " + fixString(dataSource.prefix);
			}
			if (dataSource.useHostFile) {
				launchArgs += " -hostfile " + fixString(dataSource.hostFile);
			}
			if (dataSource.useHostList) {
				launchArgs += " -host " + fixString(dataSource.hostList);
			}
			
			if (!dataSource.useDefParams) {
				for (Entry<String, String> param : dataSource.params.entrySet()) {
					launchArgs += " -mca " + param.getKey() + " " + fixString(param.getValue());
				}
			}
		} else {
			launchArgs = dataSource.args;
		}

		argsText.setText(launchArgs);
	}

	/**
	 * Update state of controls based on current selections
	 */
	public void updateControls() {
		prefixText.setEnabled(usePrefixButton.getSelection());
		argsText.setEnabled(!useArgsDefaultsButton.getSelection());
		paramsTable.setEnabled(!useParamsDefaultsButton.getSelection());
		updateArguments();
	}
}
