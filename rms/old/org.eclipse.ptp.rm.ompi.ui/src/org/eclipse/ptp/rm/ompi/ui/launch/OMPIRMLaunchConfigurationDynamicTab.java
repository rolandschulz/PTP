/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ompi.ui.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.ptp.rm.ompi.core.OMPIAttributes;
import org.eclipse.ptp.rm.ompi.core.parameters.Parameters;
import org.eclipse.ptp.rm.ompi.core.parameters.Parameters.Parameter;
import org.eclipse.ptp.rm.ompi.core.rmsystem.OMPIResourceManager;
import org.eclipse.ptp.rm.ompi.ui.Activator;
import org.eclipse.ptp.ui.utils.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class OMPIRMLaunchConfigurationDynamicTab extends
		AbstractRMLaunchConfigurationDynamicTab {
	
	private static final String ATTR_BASE = Activator.PLUGIN_ID + ".launchAttributes";
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
	
	private static final RMLaunchValidation success = new RMLaunchValidation(true, "");
	
	private Text numProcsText;
	private Text prefixText;
	private Text hostFileText;
	private Text hostListText;
	private Text argsText;
	private Button bySlotButton;
	private Button noOversubscribeButton;
	private Button noLocalButton;
	private Button prefixButton;
	private Button hostFileButton;
	private Button hostListButton;
	private Button useArgsDefaultsButton;
	private Button useParamsDefaultsButton;
	private CheckboxTableViewer paramsViewer;
	private Table paramsTable;
	private Composite control;

	private String numProcs = "1";
	private String prefix = EMPTY_STRING;
	private String hostFile = EMPTY_STRING;
	private String hostList = EMPTY_STRING;
	private String launchArgs = EMPTY_STRING;
	private boolean initializing;

	public OMPIRMLaunchConfigurationDynamicTab(IResourceManager rm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#canSave(org.eclipse.swt.widgets.Control, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		try {
			int n = Integer.parseInt(numProcs);
			if (n < 1) {
				return new RMLaunchValidation(false, "Must specify at least one process");
			}
		} catch (NumberFormatException e) {
			return new RMLaunchValidation(false, "Number of Processes: " + e.getMessage());
		}
		if (prefixButton.getSelection() && prefix.equals("")) {
			return new RMLaunchValidation(false, "Prefix cannot be empty");
		}
		if (hostFileButton.getSelection() && hostFile.equals("")) {
			return new RMLaunchValidation(false, "Must provide a host file");
		}
		if (hostListButton.getSelection() && hostList.equals("")) {
			return new RMLaunchValidation(false, "Must provide at least one host name");
		}
		if (useArgsDefaultsButton.getSelection() && launchArgs.equals("")) {
			return new RMLaunchValidation(false, "Arguments cannot be empty");
		}
		if (!useParamsDefaultsButton.getSelection()) {
			for (Object object : paramsViewer.getCheckedElements()) {
				if (object instanceof Parameter) {
					Parameter param = (Parameter)object;
					if (param.getValue().equals("")) {
						return new RMLaunchValidation(false, "Parameter value cannot be empty");						
					}
				}
			}
		}
		return success;
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

		numProcsText = new Text(simpleComposite, SWT.BORDER);
		numProcsText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				numProcs = numProcsText.getText();
				refreshAll();
			}
		});
		numProcsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		final Group optionsGroup = new Group(simpleComposite, SWT.NONE);
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		optionsGroup.setText("Options");
		layout = new GridLayout();
		layout.numColumns = 3;
		optionsGroup.setLayout(layout);

		bySlotButton = new Button(optionsGroup, SWT.CHECK);
		bySlotButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				refreshAll();
			}
		});
		bySlotButton.setText("By slot");

		noOversubscribeButton = new Button(optionsGroup, SWT.CHECK);
		noOversubscribeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				refreshAll();
			}
		});
		noOversubscribeButton.setText("No oversubscribe");

		noLocalButton = new Button(optionsGroup, SWT.CHECK);
		noLocalButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				refreshAll();
			}
		});
		noLocalButton.setText("No local");

		prefixButton = new Button(optionsGroup, SWT.CHECK);
		prefixButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				refreshAll();
			}
		});
		prefixButton.setText("Prefix:");

		prefixText = new Text(optionsGroup, SWT.BORDER);
		prefixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		prefixText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				prefix = prefixText.getText();
				refreshAll();
			}
		});

		final Group hostGroup = new Group(simpleComposite, SWT.NONE);
		hostGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		hostGroup.setText("Hosts");
		layout = new GridLayout();
		layout.numColumns = 3;
		hostGroup.setLayout(layout);

		hostFileButton = new Button(hostGroup, SWT.CHECK);
		hostFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				refreshAll();
			}
		});
		hostFileButton.setText("Host file:");

		hostFileText = new Text(hostGroup, SWT.BORDER);
		hostFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		hostFileText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				hostFile = hostFileText.getText();
				refreshAll();
			}
		});

		final Button browseButton = new Button(hostGroup, SWT.NONE);
		browseButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
			}
		});
		PixelConverter pixelconverter = new PixelConverter(control);
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.widthHint = pixelconverter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		browseButton.setLayoutData(gd);
		browseButton.setText("Browse");

		hostListButton = new Button(hostGroup, SWT.CHECK);
		hostListButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		hostListButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				refreshAll();
			}
		});
		hostListButton.setText("Host list:");

		hostListText = new Text(hostGroup, SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 50;
		hostListText.setLayoutData(gd);
		hostListText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				hostList = textToHostList(hostListText.getText());
				refreshAll();
			}
		});

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
		useArgsDefaultsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				refreshAll();
			}
		});

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
		useParamsDefaultsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				refreshAll();
			}
		});
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
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((Parameter)j1).getName().compareTo(((Parameter)j2).getName());
			}
		});
		paramsViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				refreshAll();
			}
		});
		paramsViewer.setAllChecked(false);

		// Enable cursor keys in table
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(paramsViewer, 
				new FocusCellOwnerDrawHighlighter(paramsViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(paramsViewer) {
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
		paramsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#getAttributes(org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IAttribute<?,?,?>[] getAttributes(IResourceManager rm, IPQueue queue,
			ILaunchConfiguration configuration) throws CoreException {
		List<IAttribute<?,?,?>> attrs = new ArrayList<IAttribute<?,?,?>>();
		try {
			attrs.add(JobAttributes.getNumberOfProcessesAttributeDefinition().create(numProcs));
		} catch (IllegalValueException e) {
		} 
		attrs.add(OMPIAttributes.getLaunchArgumentsAttributeDefinition().create(launchArgs));
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
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm,
			IPQueue queue, ILaunchConfiguration configuration) {
		int savedNumProcs;
		boolean savedBySlot;
		boolean savedNoOversubscribe;
		boolean savedNoLocal;
		boolean savedUsePrefix;
		boolean savedUseHostFile;
		boolean savedUseHostList;
		boolean savedUseDefArgs;
		boolean savedUseDefParams;
		String savedPrefix;
		String savedHostFile;
		String savedHostList;
		String savedArgs;
		Map<String, String> savedParams;
		
		try {
			savedNumProcs = configuration.getAttribute(ATTR_NUMPROCS, 1);
			savedBySlot = configuration.getAttribute(ATTR_BYSLOT, false);
			savedNoOversubscribe = configuration.getAttribute(ATTR_NOOVERSUBSCRIBE, false);
			savedNoLocal = configuration.getAttribute(ATTR_NOLOCAL, false);
			savedPrefix = configuration.getAttribute(ATTR_PREFIX, EMPTY_STRING);
			savedUsePrefix = configuration.getAttribute(ATTR_USEPREFIX, false);
			savedHostFile = configuration.getAttribute(ATTR_HOSTFILE, EMPTY_STRING);
			savedUseHostFile = configuration.getAttribute(ATTR_USEHOSTFILE, false);
			savedHostList = configuration.getAttribute(ATTR_HOSTLIST, EMPTY_STRING);
			savedUseHostList = configuration.getAttribute(ATTR_USEHOSTLIST, false);
			savedArgs = configuration.getAttribute(ATTR_ARGUMENTS, EMPTY_STRING);
			savedUseDefArgs = configuration.getAttribute(ATTR_USEDEFAULTARGUMENTS, false);
			savedUseDefParams = configuration.getAttribute(ATTR_USEDEFAULTPARAMETERS, false);
			savedParams = configuration.getAttribute(ATTR_PARAMETERS, (Map<String, String>)null);
		} catch (CoreException e) {
			return new RMLaunchValidation(false, e.getMessage());
		}
		
		initializing = true;

		numProcsText.setText(Integer.toString(savedNumProcs));
		bySlotButton.setSelection(savedBySlot);
		noOversubscribeButton.setSelection(savedNoOversubscribe);
		noLocalButton.setSelection(savedNoLocal);
		prefixText.setText(savedPrefix);
		prefixButton.setSelection(savedUsePrefix);
		hostFileText.setText(savedHostFile);
		hostFileButton.setSelection(savedUseHostFile);
		String text = hostListToText(savedHostList);
		if (!hostList.equals("") && text.equals("")) {
			initializing = false;
			return new RMLaunchValidation(false, "Host list is not valid");
		}
		hostListText.setText(text);
		hostListButton.setSelection(savedUseHostList);
		argsText.setText(savedArgs);
		useArgsDefaultsButton.setSelection(savedUseDefArgs);
		useParamsDefaultsButton.setSelection(savedUseDefParams);
		
		Parameters params = ((OMPIResourceManager)rm).getParameters();
		if (params != null) {
			paramsViewer.setInput(params);
	
			/*
			 * Update parameters with saved values
			 */
			if (!savedUseDefParams) {
				if (savedParams != null) {
					for (String name : savedParams.keySet()) {
						Parameter param = params.getParameter(name);
						if (param != null) {
							param.setValue(savedParams.get(name));
							System.out.println("restoring " + param.getName() + " to " + savedParams.get(name));
							paramsViewer.setChecked(param, true);
							paramsViewer.update(param, null);
						}
					}
				}
			}
		}
		
		initializing = false;
		
		updateControls();
		
		return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#isValid(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration configuration,
			IResourceManager rm, IPQueue queue) {
		try {
			int n = Integer.parseInt(numProcs);
			if (n < 1) {
				return new RMLaunchValidation(false, "Must specify at least one process");
			}
		} catch (NumberFormatException e) {
			return new RMLaunchValidation(false, "Number of Processes: " + e.getMessage());
		}
		if (prefixButton.getSelection() && prefix.equals("")) {
			return new RMLaunchValidation(false, "Prefix cannot be empty");
		}
		if (hostFileButton.getSelection() && hostFile.equals("")) {
			return new RMLaunchValidation(false, "Must provide a host file");
		}
		if (hostListButton.getSelection() && hostList.equals("")) {
			return new RMLaunchValidation(false, "Must provide at least one host name");
		}
		if (useArgsDefaultsButton.getSelection() && launchArgs.equals("")) {
			return new RMLaunchValidation(false, "Arguments cannot be empty");
		}
		if (!useParamsDefaultsButton.getSelection()) {
			for (Object object : paramsViewer.getCheckedElements()) {
				if (object instanceof Parameter) {
					Parameter param = (Parameter)object;
					if (param.getValue().equals("")) {
						return new RMLaunchValidation(false, "Parameter value cannot be empty");						
					}
				}
			}
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		configuration.setAttribute(ATTR_NUMPROCS, Integer.parseInt(numProcs));
		configuration.setAttribute(ATTR_BYSLOT, bySlotButton.getSelection());
		configuration.setAttribute(ATTR_NOOVERSUBSCRIBE, noOversubscribeButton.getSelection());
		configuration.setAttribute(ATTR_NOLOCAL, noLocalButton.getSelection());
		configuration.setAttribute(ATTR_USEPREFIX, prefixButton.getSelection());
		configuration.setAttribute(ATTR_PREFIX, prefix);
		configuration.setAttribute(ATTR_USEHOSTFILE, hostFileButton.getSelection());
		configuration.setAttribute(ATTR_HOSTFILE, hostFile);
		configuration.setAttribute(ATTR_USEHOSTLIST, hostListButton.getSelection());
		configuration.setAttribute(ATTR_HOSTLIST, hostList);
		configuration.setAttribute(ATTR_USEDEFAULTARGUMENTS, useArgsDefaultsButton.getSelection());
		configuration.setAttribute(ATTR_ARGUMENTS, launchArgs);
		configuration.setAttribute(ATTR_USEDEFAULTPARAMETERS, useParamsDefaultsButton.getSelection());
		
		if (!useParamsDefaultsButton.getSelection()) {
			Map<String, String> params = new HashMap<String, String>();
			for (Object object : paramsViewer.getCheckedElements()) {
				if (object instanceof Parameter) {
					Parameter param = (Parameter)object;
					params.put(param.getName(), param.getValue());
				}
			}
			configuration.setAttribute(ATTR_PARAMETERS, params);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		configuration.setAttribute(ATTR_NUMPROCS, Integer.parseInt(numProcs));
		configuration.setAttribute(ATTR_BYSLOT, false);
		configuration.setAttribute(ATTR_NOOVERSUBSCRIBE, false);
		configuration.setAttribute(ATTR_NOLOCAL, false);
		configuration.setAttribute(ATTR_USEPREFIX, false);
		configuration.setAttribute(ATTR_PREFIX, prefix);
		configuration.setAttribute(ATTR_USEHOSTFILE, false);
		configuration.setAttribute(ATTR_HOSTFILE, hostFile);
		configuration.setAttribute(ATTR_USEHOSTLIST, false);
		configuration.setAttribute(ATTR_HOSTLIST, hostList);
		configuration.setAttribute(ATTR_USEDEFAULTARGUMENTS, true);
		configuration.setAttribute(ATTR_ARGUMENTS, launchArgs);
		configuration.setAttribute(ATTR_USEDEFAULTPARAMETERS, true);
		configuration.setAttribute(ATTR_PARAMETERS, (Map<String, String>)null);
		return success;
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
				refreshAll();
			}
		});
		column2.getColumn().setResizable(true);
		column2.getColumn().setText("Value");
		
		paramsTable.addControlListener(new ControlAdapter() {
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
	 * Refresh the whole UI. Be careful we don't do it when
	 * initializing widgets.
	 */
	private void refreshAll() {
		if (!initializing) {
			updateControls();
			fireContentsChanged();
		}		
	}

	/**
	 * Convert whatever is entered in the host list text box into
	 * a comma separated list.
	 * 
	 * @param text
	 * @return comma separated list
	 */
	private String textToHostList(String text) {
		String result = "";
		String[] values = text.trim().split("[ \r,]");
		
		for (int i = 0; i < values.length; i++) {
			if (!values[i].equals("")) {
				if (i > 0) {
					result += ",";
				}
				result += values[i];
			}
		}
		
		return result;
	}
	

	/**
	 * If we're using default arguments, compute what they
	 * should be.
	 */
	private void updateArgs() {
		if (useArgsDefaultsButton.getSelection()) {
			launchArgs = "-np " + numProcs;
			if (bySlotButton.getSelection()) {
				launchArgs += " -byslot";
			} 
			if (noOversubscribeButton.getSelection()) {
				launchArgs += " -nooversubscribe";
			}
			if (noLocalButton.getSelection()) {
				launchArgs += " -nolocal";
			}
			if (prefixButton.getSelection()) {
				launchArgs += " --prefix " + prefix;
			}
			if (hostFileButton.getSelection()) {
				launchArgs += " -hostfile " + hostFile;
			}
			if (hostListButton.getSelection()) {
				launchArgs += " -host " + hostList;
			}
			
			if (!useParamsDefaultsButton.getSelection()) {
				for (Object object : paramsViewer.getCheckedElements()) {
					if (object instanceof Parameter) {
						Parameter param = (Parameter)object;
						launchArgs += " -mca " + param.getName() + " " + param.getValue();
					}
				}
			}
			
			argsText.setText(launchArgs);
		}
	}

	
    /**
	 * Update state of controls based on current selections
	 */
	private void updateControls() {
		prefixText.setEnabled(prefixButton.getSelection());
		hostFileText.setEnabled(hostFileButton.getSelection());
		hostListText.setEnabled(hostListButton.getSelection());
		argsText.setEnabled(!useArgsDefaultsButton.getSelection());
		paramsTable.setEnabled(!useParamsDefaultsButton.getSelection());
		updateArgs();
	} 
}
