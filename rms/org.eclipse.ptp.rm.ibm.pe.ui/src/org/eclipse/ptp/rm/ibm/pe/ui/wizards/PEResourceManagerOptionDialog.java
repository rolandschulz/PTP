/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.ui.wizards;

import java.io.IOException;
import java.util.StringTokenizer;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.rm.ibm.pe.core.PEPreferenceConstants;
import org.eclipse.ptp.rm.ibm.pe.core.PEPreferenceManager;
import org.eclipse.ptp.rm.ibm.pe.core.rmsystem.PEResourceManagerConfiguration;
import org.eclipse.ptp.rm.ibm.pe.ui.internal.ui.Messages;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PEResourceManagerOptionDialog extends TitleAreaDialog
{
    private static final String SUSPEND_AT_STARTUP_OPTION = "--suspend_at_startup ";
    private static final String USE_LOADLEVELER_OPTION = "--useloadleveler ";
    private static final String MULTICLUSTER_OPTION = "--multicluster";
    private static final String NODE_POLL_MIN_OPTION = "--node_polling_min";
    private static final String NODE_POLL_MAX_OPTION = "--node_polling_max";
    private static final String JOB_POLL_OPTION = "--job_polling";
    private static final String LIB_OVERRIDE_OPTION = "--lib_override";
    private static final String TRACE_OPTION = "--trace";
    private static final String RUN_MINIPROXY_OPTION = "--runMiniproxy";
    private Composite optionsPane;
    private Button loadLevelerOption;
    private Button suspendOption;
    private Button runMiniproxy;
    private Button libOverrideBrowse;
    private Button llModeLocal;
    private Button llModeMulticluster;
    private Button llModeDefault;
    private Text nodePollMinInterval;
    private Text nodePollMaxInterval;
    private Text jobPollInterval;
    private Text libOverridePath;
    private Group llModeGroup;
    private Composite llOverrideBox;
    private Combo debugLevel;
    private Label llLabel;
    private Label debugLabel;
    private Label suspendLabel;
    private Label runMiniproxyLabel;
    private Label libOverrideLabel;
    private Label llModeLabel;
    private Label nodePollMinLabel;
    private Label nodePollMaxLabel;
    private Label jobPollLabel;
    private Shell parentShell;
    private String proxyOptions;
    private PEResourceManagerConfiguration config;
    private RMConfigurationWizard confWizard;
    private IRemoteServices remoteService;
    private IRemoteConnection remoteConnection;
    private IRemoteConnectionManager connMgr;

    private EventMonitor eventMonitor;

    private class EventMonitor implements SelectionListener, ModifyListener
    {
	public EventMonitor()
	{
	}

	public void widgetDefaultSelected(SelectionEvent e)
	{
	}

	public void widgetSelected(SelectionEvent e)
	{
	    if (e.getSource() == loadLevelerOption) {
		if (loadLevelerOption.getSelection()) {
		    setLLWidgetEnableState(true);
		} else {
		    setLLWidgetEnableState(false);
		}
	    }
	    if (e.getSource() == libOverrideBrowse) {
		String selectedFile;

		selectedFile = remoteService.getFileManager(remoteConnection).browseDirectory(PEResourceManagerOptionDialog.this.parentShell,
			Messages.getString("PEDialogs.librarySelectorTitle"), "/").toString();
		if (selectedFile != null) {
		    libOverridePath.setText(selectedFile);
		}
	    } else {
		validateInput(e.getSource());
	    }
	}

	public void modifyText(ModifyEvent e)
	{
	    validateInput(e.getSource());
	}
    }

    public PEResourceManagerOptionDialog(Shell parent, RMConfigurationWizard wizard, String initialOptions)
    {
	super(parent);
	parentShell = parent;
	confWizard = wizard;
	config = (PEResourceManagerConfiguration) confWizard.getConfiguration();
	create();
    }

    /**
     * Get the preferences object for the PE implementation
     * 
     * @return the preferences object
     */
    public Preferences getPreferences()
    {
	return PEPreferenceManager.getPreferences();
    }

    /**
     * Create the widgets for this proxy configuration page and set their values
     * to the last saved values for this resource manager
     * 
     * @param parent - The parent widget for this class
     */
    protected Control createDialogArea(Composite parent)
    {
	GridLayout layout;
	GridLayout libPathLayout;
	GridLayout modeLayout;
	GridData gd;
	Preferences preferences;
	String preferenceValue;

	setTitle(Messages.getString("PEDialogs.InvocationOptionsTitle"));
	eventMonitor = new EventMonitor();
	preferences = getPreferences();
	optionsPane = new Composite(parent, SWT.NONE);
	layout = new GridLayout(2, true);
	optionsPane.setLayout(layout);

	llLabel = new Label(optionsPane, SWT.NONE);
	llLabel.setText(Messages.getString("PEDialogs.LoadLevelerOptionLabel"));
	loadLevelerOption = new Button(optionsPane, SWT.CHECK);
	preferenceValue = config.getUseLoadLeveler();
	if ((preferenceValue != null) && (preferenceValue.equals(PEPreferenceConstants.OPTION_YES))) {
	    loadLevelerOption.setSelection(true);
	}
	llModeLabel = new Label(optionsPane, SWT.NONE);
	llModeLabel.setText(Messages.getString("PEDialogs.LLRunMode"));
	llModeGroup = new Group(optionsPane, SWT.SHADOW_ETCHED_IN);
	modeLayout = new GridLayout(2, true);
	llModeGroup.setLayout(modeLayout);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.grabExcessHorizontalSpace = true;
	llModeGroup.setLayoutData(gd);
	llModeLocal = new Button(llModeGroup, SWT.RADIO);
	llModeLocal.setText(Messages.getString("PEDialogs.llModeLocal"));
	llModeMulticluster = new Button(llModeGroup, SWT.RADIO);
	llModeMulticluster.setText(Messages.getString("PEDialogs.llModeMulticluster"));
	llModeDefault = new Button(llModeGroup, SWT.RADIO);
	llModeDefault.setText(Messages.getString("PEDialogs.llModeDefault"));
	llModeDefault.setSelection(false);
	llModeLocal.setSelection(false);
	llModeMulticluster.setSelection(false);
	preferenceValue = config.getLoadLevelerMode();
	if (preferenceValue != null) {
	    if (preferenceValue.equals("y")) {
		llModeMulticluster.setSelection(true);
	    } else if (preferenceValue.equals("n")) {
		llModeLocal.setSelection(true);
	    }
	    else {
		llModeDefault.setSelection(true);
	    }
	} 
	else {
	    llModeDefault.setSelection(true);
	}

	nodePollMinLabel = new Label(optionsPane, SWT.NONE);
	nodePollMinLabel.setText(Messages.getString("PEDialogs.minNodePollInterval"));
	nodePollMinInterval = new Text(optionsPane, SWT.NONE);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.grabExcessHorizontalSpace = true;
	nodePollMinInterval.setLayoutData(gd);
	preferenceValue = config.getNodeMinPollInterval();
	if (preferenceValue != null) {
	    nodePollMinInterval.setText(preferenceValue);
	}

	nodePollMaxLabel = new Label(optionsPane, SWT.NONE);
	nodePollMaxLabel.setText(Messages.getString("PEDialogs.maxNodePollInterval"));
	nodePollMaxInterval = new Text(optionsPane, SWT.NONE);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.grabExcessHorizontalSpace = true;
	nodePollMaxInterval.setLayoutData(gd);
	preferenceValue = config.getNodeMaxPollInterval();
	if (preferenceValue != null) {
	    nodePollMaxInterval.setText(preferenceValue);
	}

	jobPollLabel = new Label(optionsPane, SWT.NONE);
	jobPollLabel.setText(Messages.getString("PEDialogs.jobPollInterval"));
	jobPollInterval = new Text(optionsPane, SWT.NONE);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.grabExcessHorizontalSpace = true;
	jobPollInterval.setLayoutData(gd);
	preferenceValue = config.getJobPollInterval();
	if (preferenceValue != null) {
	    jobPollInterval.setText(preferenceValue);
	}

	libOverrideLabel = new Label(optionsPane, SWT.NONE);
	libOverrideLabel.setText(Messages.getString("PEDialogs.libOverrideLabel"));

	llOverrideBox = new Composite(optionsPane, SWT.NONE);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.grabExcessHorizontalSpace = true;
	llOverrideBox.setLayoutData(gd);
	libPathLayout = new GridLayout(2, false);
	libPathLayout.marginLeft = 0;
	libPathLayout.marginRight = 0;
	libPathLayout.marginWidth = 0;
	llOverrideBox.setLayout(libPathLayout);
	libOverridePath = new Text(llOverrideBox, SWT.NONE);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.grabExcessHorizontalSpace = true;
	libOverridePath.setLayoutData(gd);
	libOverrideBrowse = new Button(llOverrideBox, SWT.PUSH);
	libOverrideBrowse.setText(Messages.getString("PEDialogs.browse"));
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.grabExcessHorizontalSpace = false;
	libOverrideBrowse.setLayoutData(gd);
	preferenceValue = config.getLibraryOverride();
	if (preferenceValue != null) {
	    libOverridePath.setText(preferenceValue);
	}

	runMiniproxyLabel = new Label(optionsPane, SWT.NONE);
	runMiniproxyLabel.setText(Messages.getString("PEDialogs.MiniproxyLabel"));
	runMiniproxy = new Button(optionsPane, SWT.CHECK);
	preferenceValue = config.getRunMiniproxy();
	if ((preferenceValue != null) && (preferenceValue.equals(PEPreferenceConstants.OPTION_YES))) {
	    runMiniproxy.setSelection(true);
	}

	debugLabel = new Label(optionsPane, SWT.NONE);
	debugLabel.setText(Messages.getString("PEDialogs.TraceLevelLabel"));
	debugLevel = new Combo(optionsPane, SWT.READ_ONLY);
	debugLevel.add(PEPreferenceConstants.TRACE_NOTHING);
	debugLevel.add(PEPreferenceConstants.TRACE_FUNCTION);
	debugLevel.add(PEPreferenceConstants.TRACE_DETAIL);
	preferenceValue = config.getDebugLevel();
	if (preferenceValue != null) {
	    debugLevel.setText(preferenceValue);
	}

	suspendLabel = new Label(optionsPane, SWT.NULL);
	suspendLabel.setText(Messages.getString("PEDialogs.SuspendLabel"));
	suspendOption = new Button(optionsPane, SWT.CHECK);
	preferenceValue = config.getSuspendProxy();
	// suspendOption is used to specify that the proxy should suspend after it has recognized this
	// option so that a debugger can attach to it. Since this is a debugging aid, there is no
	// preferences setting for this flag (proxies should never suspend at startup by default)
	if ((preferenceValue != null) && (preferenceValue.equals(PEPreferenceConstants.OPTION_YES))) {
	    suspendOption.setSelection(true);
	}

	if (!loadLevelerOption.getSelection()) {
	    setLLWidgetEnableState(false);
	}
	loadLevelerOption.addSelectionListener(eventMonitor);
	suspendOption.addSelectionListener(eventMonitor);
	llModeLocal.addSelectionListener(eventMonitor);
	llModeMulticluster.addSelectionListener(eventMonitor);
	llModeDefault.addSelectionListener(eventMonitor);
	nodePollMinInterval.addModifyListener(eventMonitor);
	nodePollMaxInterval.addModifyListener(eventMonitor);
	jobPollInterval.addModifyListener(eventMonitor);
	libOverridePath.addModifyListener(eventMonitor);
	libOverrideBrowse.addSelectionListener(eventMonitor);
	runMiniproxy.addSelectionListener(eventMonitor);
	suspendOption.addSelectionListener(eventMonitor);
	debugLevel.addSelectionListener(eventMonitor);
		// Ensure that the starting values for this dialog are valid and that options contained in this
		// dialog are stored in the option string even if no changes are made.
	validateInput(null);
	return optionsPane;
    }

    	/**
    	 * Set the widget enable state for these widgets based on whether the use LoadLeveler checkbox
    	 * is checked
    	 * 
    	 * @param state
    	 */
    private void setLLWidgetEnableState(boolean state)
    {
	llModeLocal.setEnabled(state);
	llModeMulticluster.setEnabled(state);
	llModeDefault.setEnabled(state);
	nodePollMinInterval.setEnabled(state);
	nodePollMaxInterval.setEnabled(state);
	jobPollInterval.setEnabled(state);
	libOverridePath.setEnabled(state);
	libOverrideBrowse.setEnabled(state);
    }

    /**
     * Retrieve the settings specified on this panel, validate those values, 
     * and create the string containing proxy invocation options corresponding to those values
     * 
     * @return Status indicating successful completion
     */
    protected boolean validateInput(Object eventSource)
    {
	String traceOpt;

	proxyOptions = "";
	setErrorMessage(null);
	if (loadLevelerOption.getSelection()) {
	    String widgetValue;
	    String multiclusterMode;
	    int interval;

	    proxyOptions = proxyOptions + USE_LOADLEVELER_OPTION;
	    config.setUseLoadLeveler(PEPreferenceConstants.OPTION_YES);
	    proxyOptions = proxyOptions + MULTICLUSTER_OPTION + "=";
	    if (llModeDefault.getSelection()) {
		multiclusterMode = "d";
	    } else if (llModeLocal.getSelection()) {
		multiclusterMode = "n";
	    } else {
		multiclusterMode = "y";
	    }
	    proxyOptions = proxyOptions + multiclusterMode + " ";
	    config.setLoadLevelerMode(multiclusterMode);
	    widgetValue = nodePollMinInterval.getText().trim();
	    if (widgetValue.length() > 0) {
		try {
		    interval = Integer.valueOf(widgetValue);
		}
		catch (NumberFormatException e) {
		    setErrorMessage(Messages.getString("PEDialogs.invalidMinPollInterval"));
		    return false;
		}
		proxyOptions = proxyOptions + NODE_POLL_MIN_OPTION + "=" + widgetValue + " ";
	    }
	    config.setNodeMinPollInterval(widgetValue);
	    widgetValue = nodePollMaxInterval.getText().trim();
	    if (widgetValue.length() > 0) {
		try {
		    interval = Integer.valueOf(widgetValue);
		}
		catch (NumberFormatException e) {
		    setErrorMessage(Messages.getString("PEDialogs.invalidMaxPollInterval"));
		    return false;
		}
		proxyOptions = proxyOptions + NODE_POLL_MAX_OPTION + "=" + widgetValue + " ";
	    }
	    config.setNodeMaxPollInterval(widgetValue);
	    widgetValue = jobPollInterval.getText().trim();
	    if (widgetValue.length() > 0) {
		try {
		    interval = Integer.valueOf(widgetValue);
		}
		catch (NumberFormatException e) {
		    setErrorMessage(Messages.getString("PEDialogs.invalidJobPollInterval"));
		    return false;
		}
		proxyOptions = proxyOptions + JOB_POLL_OPTION + "=" + widgetValue + " ";
	    }
	    config.setJobPollInterval(widgetValue);
	    widgetValue = libOverridePath.getText().trim();
	    if ((widgetValue.length() > 0) && (eventSource == libOverridePath)) {
		IFileStore remoteResource;
		IFileInfo fileInfo;
		IPath testPath;

		remoteService = PTPRemotePlugin.getDefault().getRemoteServices(config.getRemoteServicesId());
		connMgr = remoteService.getConnectionManager();
		remoteConnection = connMgr.getConnection(config.getConnectionName());
		testPath = new Path(widgetValue);
		if (!testPath.isValidPath(widgetValue)) {
		    setErrorMessage(Messages.getString("PEDialogs.invalidLibraryPath"));
		}
		try {
		    remoteResource = remoteService.getFileManager(remoteConnection).getResource(testPath,
			    new NullProgressMonitor());
		    fileInfo = remoteResource.fetchInfo();
		    if ((!fileInfo.exists()) || (! fileInfo.isDirectory())) {
			setErrorMessage(Messages.getString("PEDialogs.invalidLibraryPath"));
		    }
		}
		catch (IOException e) {
		    setErrorMessage(Messages.getString("Invalid.remoteConnectionError") + " " + e.getMessage());
		}
		proxyOptions = proxyOptions + LIB_OVERRIDE_OPTION + "=" + widgetValue + " ";
	    }
	    config.setLibraryOverride(widgetValue);
	} else {
	    config.setUseLoadLeveler(PEPreferenceConstants.OPTION_NO);
	}
	traceOpt = debugLevel.getText();
	if (traceOpt.length() > 0) {
	    proxyOptions = proxyOptions + TRACE_OPTION + "=" + traceOpt + " ";
	    config.setDebugLevel(traceOpt);
	}
	if (suspendOption.getSelection()) {
	    proxyOptions = proxyOptions + SUSPEND_AT_STARTUP_OPTION;
	    config.setSuspendProxy(PEPreferenceConstants.OPTION_YES);
	} else {
	    config.setSuspendProxy(PEPreferenceConstants.OPTION_NO);
	}
	if (runMiniproxy.getSelection()) {
	    proxyOptions = proxyOptions + RUN_MINIPROXY_OPTION + " ";
	    config.setRunMiniproxy(PEPreferenceConstants.OPTION_YES);
	} else {
	    config.setRunMiniproxy(PEPreferenceConstants.OPTION_NO);
	}
	config.setInvocationOptions(proxyOptions);
	return true;
    }
    
    public String getValue()
    {
	return proxyOptions;
    }
    
    public void setInitialOptions(String initialOptions)
    {
	StringTokenizer options;
	
	options = new StringTokenizer(initialOptions, " ");
	config.setSuspendProxy(PEPreferenceConstants.OPTION_NO);
	config.setRunMiniproxy(PEPreferenceConstants.OPTION_NO);
	while (options.hasMoreTokens()) {
	    String currentToken[];
	    
	    currentToken = options.nextToken().split("=");
	    if (currentToken.length == 1) {
		if (currentToken[0].equals(SUSPEND_AT_STARTUP_OPTION)) {
		    config.setSuspendProxy(PEPreferenceConstants.OPTION_YES);
		}
		else if (currentToken[0].equals(RUN_MINIPROXY_OPTION)) {
		    config.setRunMiniproxy(PEPreferenceConstants.OPTION_YES);
		}    
	    }
	    else {
		if (currentToken[0].equals(USE_LOADLEVELER_OPTION)) {
		    config.setUseLoadLeveler(currentToken[1]);
		}
		else if (currentToken[0].equals(MULTICLUSTER_OPTION)) {
		    config.setLoadLevelerMode(currentToken[1]);
		}
		else if (currentToken[0].equals(NODE_POLL_MIN_OPTION)) {
		    config.setNodeMinPollInterval(currentToken[1]);
		}
		else if (currentToken[0].equals(NODE_POLL_MAX_OPTION)) {
		    config.setNodeMaxPollInterval(currentToken[1]);
		}
		else if (currentToken[0].equals(JOB_POLL_OPTION)) {
		    config.setJobPollInterval(currentToken[1]);
		}
		else if (currentToken[0].equals(LIB_OVERRIDE_OPTION)) {
		    config.setLibraryOverride(currentToken[1]);
		}
	    }
	}
    }
}
