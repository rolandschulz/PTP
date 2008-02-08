/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.ui.wizards;

import java.io.IOException;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PEResourceManagerOptionWizardPage extends RMConfigurationWizardPage
{
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

		selectedFile = remoteService.getFileManager(remoteConnection).browseDirectory(PEResourceManagerOptionWizardPage.this.getShell(),
			Messages.getString("PEDialogs.librarySelectorTitle"), "/").toString();
		if (selectedFile != null) {
		    libOverridePath.setText(selectedFile);
		}
	    } else {
		performOk(e.getSource());
	    }
	}

	public void modifyText(ModifyEvent e)
	{
	    performOk(e.getSource());
	}
    }

    public PEResourceManagerOptionWizardPage(RMConfigurationWizard wizard)
    {
	super(wizard, Messages.getString("PEDialogs.InvocationOptionsTitle"));
	setTitle(Messages.getString("PEDialogs.InvocationOptionsTitle"));
	setDescription(Messages.getString("PEDialogs.InvocationOptions"));
	confWizard = getConfigurationWizard();
	config = (PEResourceManagerConfiguration) confWizard.getConfiguration();
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
    @Override
    public void createControl(Composite parent)
    {
	GridLayout layout;
	GridLayout libPathLayout;
	GridLayout modeLayout;
	GridData gd;
	Preferences preferences;
	String preferenceValue;

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
	performOk(null);
	setControl(optionsPane);
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
    protected boolean performOk(Object eventSource)
    {
	String options;
	String traceOpt;

	options = "";
	setErrorMessage(null);
	if (loadLevelerOption.getSelection()) {
	    String widgetValue;
	    String multiclusterMode;
	    int interval;

	    options = options + "--useloadleveler=y ";
	    config.setUseLoadLeveler(PEPreferenceConstants.OPTION_YES);
	    options = options + "--multicluster=";
	    if (llModeDefault.getSelection()) {
		multiclusterMode = "d";
	    } else if (llModeLocal.getSelection()) {
		multiclusterMode = "n";
	    } else {
		multiclusterMode = "y";
	    }
	    options = options + multiclusterMode + " ";
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
		options = options + "--node_polling_min=" + widgetValue + " ";
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
		options = options + "--node_polling_max=" + widgetValue + " ";
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
		options = options + "--job_polling=" + widgetValue + " ";
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
		options = options + "--lib_override=" + widgetValue + " ";
	    }
	    config.setLibraryOverride(widgetValue);
	} else {
	    config.setUseLoadLeveler(PEPreferenceConstants.OPTION_NO);
	}
	traceOpt = debugLevel.getText();
	if (traceOpt.length() > 0) {
	    options = options + "--trace=" + traceOpt + " ";
	    config.setDebugLevel(traceOpt);
	}
	if (suspendOption.getSelection()) {
	    options = options + "--suspend_at_startup ";
	    config.setSuspendProxy(PEPreferenceConstants.OPTION_YES);
	} else {
	    config.setSuspendProxy(PEPreferenceConstants.OPTION_NO);
	}
	if (runMiniproxy.getSelection()) {
	    options = options + "--runMiniproxy ";
	    config.setRunMiniproxy(PEPreferenceConstants.OPTION_YES);
	} else {
	    config.setRunMiniproxy(PEPreferenceConstants.OPTION_NO);
	}
	config.setInvocationOptions(options);
	return true;
    }
}
