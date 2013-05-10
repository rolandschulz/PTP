/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.internal.etfw.ui;

//import java.io.File;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.etfw.ETFWUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess;
import org.eclipse.ptp.etfw.toolopts.IToolUITab;
import org.eclipse.ptp.etfw.toolopts.ToolPaneListener;
import org.eclipse.ptp.internal.etfw.Activator;
import org.eclipse.ptp.internal.etfw.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * Defines the tab of the performance-analysis launch configuration system where
 * performance-analysis options are selected
 * 
 * @author wspear
 * 
 */
public class ExternalToolSelectionTab extends AbstractLaunchConfigurationTab implements IToolLaunchConfigurationConstants {
	/**
	 * Listens for action in tool options panes
	 * 
	 * @author wspear
	 * 
	 */
	protected class OptionsPaneListener extends ToolPaneListener {
		OptionsPaneListener(IToolUITab tool) {
			super(tool);
		}

		@Override
		protected void localAction() {
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * Listen for activity in the performance tool combo-box, or other options
	 * 
	 * @author wspear
	 * 
	 */
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
		public void modifyText(ModifyEvent evt) {

			updateLaunchConfigurationDialog();
		}

		public void propertyChange(PropertyChangeEvent event) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetSelected(SelectionEvent e) {

			final Object source = e.getSource();

			if (source.equals(addWorkflowB)) {
				addWorkflow();
			}
			if (source.equals(removeWorkflowB)) {
				removeWorkflow();
			}

			if (source.equals(toolTypes)) {

				final String selectedTool = toolTypes.getItem(toolTypes.getSelectionIndex());

				loadPanesForTool(selectedTool);// ,configuration);
				initializePanesForTool(selectedTool, localConfig);

			}

			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * Produces a new GridLayout based on provided arguments
	 * 
	 * @param columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected static GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	/**
	 * Creates a new GridData based on provided style and space arguments
	 * 
	 * @param style
	 * @param space
	 * @return
	 */
	protected static GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}

	// private static final IPreferenceStore pstore =
	// ETFWUtils.getDefault().getPreferenceStore();
	/**
	 * Determines if the launch configuration associated with this tab has
	 * access to the PTP
	 */
	protected boolean noPTP = false;

	protected final ExternalToolProcess[] tools = ETFWUtils.getTools();// null;

	protected Combo toolTypes;
	protected Button buildonlyCheck;

	// protected Button nocleanCheck;

	protected Button analyzeonlyCheck;

	protected Button noParallelRun;

	// protected Button relocateTools;

	protected Button addWorkflowB;

	protected Button removeWorkflowB;

	// private boolean toolHasPane(String toolName, String paneName){
	// if(panes!=null){
	// for(int i=0;i<panes.length;i++){
	// if(panes[i].paneName.equals(paneName)){
	// if(panes[i].toolName.equals(toolName))
	// return true;
	// else
	// return false;
	// }
	// }
	// }
	// return false;
	// }

	protected Button keepprofsCheck;

	protected final IToolUITab[] panes = ETFWUtils.getToolPanes();

	protected WidgetListener listener = new WidgetListener();

	private TabFolder tabParent = null;

	ILaunchConfiguration localConfig = null;

	/**
	 * Sets weather or not it is possible to initiate a parallel launch from
	 * this tab
	 * 
	 * @param noPar
	 *            Availability of the PTP to this tab's launch configuration
	 *            delegate
	 */
	public ExternalToolSelectionTab(boolean noPar) {
		noPTP = noPar;
	}

	private void addWorkflow() {
		final FileDialog dialog = new FileDialog(getShell());

		dialog.setText(Messages.ExternalToolSelectionTab_SelectToolDefXMLFile);

		final String out = getFieldContent(dialog.open());

		if (out == null) {
			return;
		}

		IFileStore test = null;
		// try {
		test = EFS.getLocalFileSystem().getStore(new File(out).toURI());
		// test = EFS.getLocalFileSystem().getStore(new URI(out));
		// } catch (URISyntaxException e) {
		// e.printStackTrace();
		// }

		// File test = new File(out);
		if (test == null || !test.fetchInfo().exists() || test.fetchInfo().isDirectory()) {
			return;
		}

		// Preferences preferences = ETFWUtils.getDefault().getPluginPreferences();
		final IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);

		final IPreferencesService service = Platform.getPreferencesService();
		String fiList = service.getString(Activator.PLUGIN_ID, XMLLOCID, "", null);

		// String fiList = preferences.getString(XMLLOCID);

		final String[] x = fiList.split(",,,"); //$NON-NLS-1$
		final LinkedHashSet<String> files = new LinkedHashSet<String>();
		for (final String element : x) {
			files.add(element);
		}
		files.add(out);

		fiList = ""; //$NON-NLS-1$

		final Iterator<String> fit = files.iterator();

		while (fit.hasNext()) {
			fiList += fit.next();
			if (fit.hasNext()) {
				fiList += ",,,"; //$NON-NLS-1$
			}
		}
		preferences.put(XMLLOCID, fiList);// XMLLoc.getText());
		ETFWUtils.refreshTools(Activator.getDefault().getPreferenceStore());
		warnXMLChange();

	}

	/**
	 * Generates the UI for the analyis tab, consisting of sub-tabs which may be
	 * dynamically generated
	 * 
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		final Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		final FillLayout topLayout = new FillLayout();
		comp.setLayout(topLayout);
		tabParent = new TabFolder(comp, SWT.BORDER);

		/*
		 * 
		 * Analysis Options: TAU Makefile options and PAPI counter selection
		 */
		final TabItem toolTab = new TabItem(tabParent, SWT.NULL);
		toolTab.setText(Messages.ExternalToolSelectionTab_ToolSelection);

		final ScrolledComposite scrollTool = new ScrolledComposite(tabParent, SWT.V_SCROLL);

		final Composite toolComp = new Composite(scrollTool, SWT.NONE);
		toolTab.setControl(scrollTool);

		toolComp.setLayout(createGridLayout(1, false, 0, 0));
		toolComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		/*
		 * The actual controls of AnaComp
		 */
		createVerticalSpacer(toolComp, 1);

		final Composite toolComboComp = new Composite(toolComp, SWT.NONE);
		toolComboComp.setLayout(createGridLayout(2, false, 0, 0));
		toolComboComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label makeLab = new Label(toolComboComp, 0);
		makeLab.setText(Messages.ExternalToolSelectionTab_SelectTool);

		toolTypes = new Combo(toolComboComp, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		toolTypes.addSelectionListener(listener);
		addWorkflowB = new Button(toolComboComp, SWT.NONE);
		addWorkflowB.setText(Messages.ExternalToolSelectionTab_AddWorkflowXMLFile);
		addWorkflowB.addSelectionListener(listener);
		removeWorkflowB = new Button(toolComboComp, SWT.NONE);
		removeWorkflowB.setText(Messages.ExternalToolSelectionTab_RemoveWorkflowXMLFile);
		removeWorkflowB.addSelectionListener(listener);
		createVerticalSpacer(toolComp, 1);

		buildonlyCheck = createCheckButton(toolComp, Messages.ExternalToolSelectionTab_BuildInstrumentedExecutable);
		buildonlyCheck.addSelectionListener(listener);

		analyzeonlyCheck = createCheckButton(toolComp, Messages.ExternalToolSelectionTab_SelectExistingPerfData);
		analyzeonlyCheck.addSelectionListener(listener);
		// nocleanCheck = createCheckButton(toolComp,
		// "Keep instrumented executable");
		// nocleanCheck.addSelectionListener(listener);

		toolComp.pack();
		final int toolCompHeight = toolComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		scrollTool.setContent(toolComp);
		scrollTool.setMinSize(400, toolCompHeight);
		scrollTool.setExpandHorizontal(true);
		scrollTool.setExpandVertical(true);

	}

	protected String getFieldContent(IntegerFieldEditor editorField) {
		return getFieldContent(editorField.getStringValue());
	}

	/**
	 * Treats empty strings as null
	 * 
	 * @param text
	 * @return Contents of text, or null if text is the empty string
	 */
	protected String getFieldContent(String text) {
		if (text == null || (text.trim().length() == 0) || text.equals("")) { //$NON-NLS-1$
			return null;
		}

		return text;
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return Messages.ExternalToolSelectionTab_PerfAnalysis;
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {

		toolTypes.removeAll();
		// toolTypes.add("TAU");

		if (tools == null || tools.length == 0) {
			toolTypes.add(Messages.ExternalToolSelectionTab_SpecValidToolConfFile);
			if (tools == null) {
				toolTypes.select(0);
				return;
			}
		}

		for (final ExternalToolProcess tool : tools) {
			toolTypes.add(tool.toolName);
		}
		toolTypes.select(0);

		try {
			int toolDex;
			toolDex = toolTypes.indexOf(configuration.getAttribute(SELECTED_TOOL, "")); //$NON-NLS-1$
			if (toolDex >= 0) {
				toolTypes.select(toolDex);
			} else {
				toolTypes.select(0);
				// This means the available tools have changed!
				if (configuration.getAttribute(SELECTED_TOOL, "").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
					// This makes sure the tool panes are initialized
					if (tools.length > 0) {
						loadPanesForTool(toolTypes.getItem(toolTypes.getSelectionIndex()));
						initializePanesForTool(toolTypes.getItem(toolTypes.getSelectionIndex()), configuration);
						localConfig = configuration;
					}
					updateLaunchConfigurationDialog();
				}
			}

			buildonlyCheck.setSelection(configuration.getAttribute(BUILDONLY, false));
			analyzeonlyCheck.setSelection(configuration.getAttribute(ANALYZEONLY, false));
			// nocleanCheck.setSelection(configuration.getAttribute(NOCLEAN,
			// false));

		} catch (final CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// This handles the case where a tool is previously selected, but the panes are not yet initialized
		loadPanesForTool(toolTypes.getItem(toolTypes.getSelectionIndex()));
		initializePanesForTool(toolTypes.getItem(toolTypes.getSelectionIndex()), configuration);
		localConfig = configuration;
	}

	private void initializePanesForTool(String tool, ILaunchConfiguration configuration) {
		if (panes != null) {
			for (int i = 0; i < panes.length; i++) {
				if (panes[i].getToolName() == null || !panes[i].getToolName().equals(tool)) {
					continue;
				}
				panes[i].OptUpdate();
				try {
					panes[i].initializePane(configuration);
				} catch (final CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid
	 * (org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);

		return true;
	}

	private void loadPanesForTool(String tool) {// , ILaunchConfiguration configuration){
		/*
		 * Dynamic Panes
		 */

		TabItem optionTab = null;
		ScrolledComposite scrollOption = null;
		Composite optionComp = null;
		int optionCompHeight = 400;

		if (panes != null) {

			TabItem[] tabs = null;
			if (tabParent != null) {
				tabs = tabParent.getItems();
			}
			for (int i = 0; i < tabs.length; i++) {
				if (!tabs[i].getText().equals(Messages.ExternalToolSelectionTab_ToolSelection)) {
					tabs[i].dispose();
				}
			}

			for (int i = 0; i < panes.length; i++) {
				if (panes[i] == null || panes[i].isVirtual() || panes[i].isEmbedded() || panes[i].getToolName() == null
						|| !panes[i].getToolName().equals(tool)) {
					continue;
				}
				optionTab = new TabItem(tabParent, SWT.NULL);
				optionTab.setText(panes[i].getName());

				scrollOption = new ScrolledComposite(tabParent, SWT.V_SCROLL);

				optionComp = new Composite(scrollOption, SWT.NONE);
				optionTab.setControl(scrollOption);

				optionComp.setLayout(createGridLayout(1, false, 0, 0));
				optionComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

				panes[i].makeToolPane(optionComp, new OptionsPaneListener(panes[i]));

				optionComp.pack();
				optionCompHeight = optionComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

				scrollOption.setContent(optionComp);
				scrollOption.setMinSize(400, optionCompHeight);
				scrollOption.setExpandHorizontal(true);
				scrollOption.setExpandVertical(true);

				// if (panes != null)
				// for (int i = 0; i < panes.length; i++) {
				panes[i].OptUpdate();
				// try {
				// panes[i].initializePane(configuration);
				// } catch (CoreException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }

			}
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		final String selectedtool = toolTypes.getItem(toolTypes.getSelectionIndex());

		configuration.setAttribute(SELECTED_TOOL, selectedtool);

		if (tools != null && tools.length >= 1) {

			configuration.setAttribute(USE_EXEC_UTIL, tools[toolTypes.getSelectionIndex()].prependExecution);
			configuration.setAttribute(EXTOOL_RECOMPILE, tools[toolTypes.getSelectionIndex()].recompile);

		}

		configuration.setAttribute(BUILDONLY, buildonlyCheck.getSelection());

		configuration.setAttribute(ANALYZEONLY, analyzeonlyCheck.getSelection());
		// System.out.println("Performing Apply in ETSTab!");
		if (panes != null) {
			for (int i = 0; i < panes.length; i++) {
				if (panes[i].getToolName() == null || !panes[i].getToolName().equals(selectedtool)) {
					continue;
				}
				panes[i].performApply(configuration);

				String paneOpts = panes[i].getOptionString();
				if (paneOpts == null) {
					paneOpts = "";
				}
				configuration.setAttribute(panes[i].getConfigID(), paneOpts);
				configuration.setAttribute(panes[i].getConfigVarID(), panes[i].getVarMap());
			}
		}

		localConfig = configuration;
	}

	private void removeWorkflow() {
		// Preferences preferences = Activator.getDefault().getPluginPreferences();
		final IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		final IPreferencesService service = Platform.getPreferencesService();
		String fiList = service.getString(Activator.PLUGIN_ID, XMLLOCID, "", null);
		// String fiList = preferences.getString(XMLLOCID);

		final String[] x = fiList.split(",,,"); //$NON-NLS-1$
		final LinkedHashSet<String> files = new LinkedHashSet<String>();
		for (final String element : x) {
			files.add(element);
		}

		final ArrayContentProvider acp = new ArrayContentProvider();
		// acp.getElements(x);

		final ListDialog ld = new ListDialog(getShell());

		ld.setContentProvider(acp);
		ld.setBlockOnOpen(true);
		ld.setLabelProvider(new LabelProvider());
		ld.setInput(x);
		ld.setHelpAvailable(false);
		ld.setTitle(Messages.ExternalToolSelectionTab_RemoveWorkflowFiles);
		ld.open();
		if (ld.getReturnCode() == Window.CANCEL) {
			return;
		}
		final Object[] y = ld.getResult();
		for (final Object element : y) {
			files.remove(element);
		}

		fiList = ""; //$NON-NLS-1$

		final Iterator<String> fit = files.iterator();

		while (fit.hasNext()) {
			fiList += fit.next();
			if (fit.hasNext()) {
				fiList += ",,,"; //$NON-NLS-1$
			}
		}
		preferences.put(XMLLOCID, fiList);// XMLLoc.getText());
		ETFWUtils.refreshTools(Activator.getDefault().getPreferenceStore());
		warnXMLChange();
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if (panes != null) {
			for (final IToolUITab pane : panes) {
				pane.setDefaults(configuration);
			}
		}
	}

	// /**
	// * @see ILaunchConfigurationTab#getImage()
	// */
	// public Image getImage() {
	// return
	// LaunchImages.getImage("org.eclipse.ptp.etfw.tau.core.tauLogo.gif");
	// }

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog (ILaunchConfigurationDialog)
	 */
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	public void updateComboFromSelection() {
		System.out.println("change startup"); //$NON-NLS-1$
	}

	private void warnXMLChange() {
		MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				Messages.ExternalToolSelectionTab_TAUWarning, Messages.ExternalToolSelectionTab_ChancesNotEffectUntil);
	}
}