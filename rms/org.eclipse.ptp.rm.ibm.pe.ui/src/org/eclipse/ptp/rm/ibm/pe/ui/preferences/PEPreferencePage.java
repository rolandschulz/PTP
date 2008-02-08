/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *  
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.ibm.pe.core.PEPreferenceConstants;
import org.eclipse.ptp.rm.ibm.pe.core.PEPreferenceManager;
import org.eclipse.ptp.rm.ibm.pe.ui.internal.ui.Messages;
import org.eclipse.ptp.rm.remote.ui.preferences.AbstractRemotePreferencePage;
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
import org.eclipse.swt.widgets.Text;

public class PEPreferencePage extends AbstractRemotePreferencePage
{

    private Button loadLevelerOption;
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
    private Combo traceOptions;
    private Label loadLevelerLabel;
    private Label traceLabel;
    private Label runMiniproxyLabel;
    private Label libOverrideLabel;
    private Label llModeLabel;
    private Label nodePollMinLabel;
    private Label nodePollMaxLabel;
    private Label jobPollLabel;
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
	    if (loadLevelerOption.getSelection()) {
		setLLWidgetEnableState(true);
	    } else {
		setLLWidgetEnableState(false);
	    }
	    updatePreferencePage();
	}

	public void modifyText(ModifyEvent e)
	{
	    updatePreferencePage();
	}
    }

    @Override
    public Preferences getPreferences()
    {
	return PEPreferenceManager.getPreferences();
    }

    @Override
    public void savePreferences()
    {
	PEPreferenceManager.savePreferences();
    }

    /**
     * Create the widgets for preference settings. This method calls the superclass createContents() method to create
     * the default widgets for the panel, then creates the additional widgets needed.
     * 
     * @param parent - The parent widget for this pane
     * 
     * @return the top level widget for the preference pane
     */
    protected Control createContents(Composite parent)
    {
	Control preferencePane;
	GridLayout layout;
	GridLayout libPathLayout;
	GridData gd;
	Group optionsGroup;
	Preferences preferences;
	String preferenceValue;

	preferences = getPreferences();
	eventMonitor = new EventMonitor();
	preferencePane = super.createContents(parent);
	optionsGroup = new Group((Composite) preferencePane, SWT.SHADOW_ETCHED_IN);
	layout = new GridLayout(2, true);
	optionsGroup.setLayout(layout);
	optionsGroup.setText(Messages.getString("PEDialogs.GroupLabel"));
	loadLevelerLabel = new Label(optionsGroup, SWT.NONE);
	loadLevelerLabel.setText(Messages.getString("PEDialogs.LoadLevelerOptionLabel"));
	loadLevelerOption = new Button(optionsGroup, SWT.CHECK);
	preferenceValue = preferences.getString(PEPreferenceConstants.LOAD_LEVELER_OPTION);
	if (preferenceValue.equals(PEPreferenceConstants.OPTION_YES)) {
	    loadLevelerOption.setSelection(true);
	}

	llModeLabel = new Label(optionsGroup, SWT.NONE);
	llModeLabel.setText(Messages.getString("PEDialogs.LLRunMode"));
	llModeGroup = new Group(optionsGroup, SWT.SHADOW_ETCHED_IN);
	layout = new GridLayout(2, true);
	llModeGroup.setLayout(layout);
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
	preferenceValue = preferences.getString(PEPreferenceConstants.LOAD_LEVELER_MODE);
	if (preferenceValue.equals("y")) {
	    llModeMulticluster.setSelection(true);
	} else if (preferenceValue.equals("n")) {
	    llModeLocal.setSelection(true);
	} else {
	    llModeDefault.setSelection(true);
	}

	nodePollMinLabel = new Label(optionsGroup, SWT.NONE);
	nodePollMinLabel.setText(Messages.getString("PEDialogs.minNodePollInterval"));
	nodePollMinInterval = new Text(optionsGroup, SWT.NONE);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.grabExcessHorizontalSpace = true;
	nodePollMinInterval.setLayoutData(gd);
	preferenceValue = preferences.getString(PEPreferenceConstants.NODE_MIN_POLL_INTERVAL);
	nodePollMinInterval.setText(preferenceValue);

	nodePollMaxLabel = new Label(optionsGroup, SWT.NONE);
	nodePollMaxLabel.setText(Messages.getString("PEDialogs.maxNodePollInterval"));
	nodePollMaxInterval = new Text(optionsGroup, SWT.NONE);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.grabExcessHorizontalSpace = true;
	nodePollMaxInterval.setLayoutData(gd);
	preferenceValue = preferences.getString(PEPreferenceConstants.NODE_MAX_POLL_INTERVAL);
	nodePollMaxInterval.setText(preferenceValue);

	jobPollLabel = new Label(optionsGroup, SWT.NONE);
	jobPollLabel.setText(Messages.getString("PEDialogs.jobPollInterval"));
	jobPollInterval = new Text(optionsGroup, SWT.NONE);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.grabExcessHorizontalSpace = true;
	jobPollInterval.setLayoutData(gd);
	preferenceValue = preferences.getString(PEPreferenceConstants.JOB_POLL_INTERVAL);
	jobPollInterval.setText(preferenceValue);

	libOverrideLabel = new Label(optionsGroup, SWT.NONE);
	libOverrideLabel.setText(Messages.getString("PEDialogs.libOverrideLabel"));

	llOverrideBox = new Composite(optionsGroup, SWT.NONE);
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
	preferenceValue = preferences.getString(PEPreferenceConstants.LIBRARY_OVERRIDE);
	libOverridePath.setText(preferenceValue);

	runMiniproxyLabel = new Label(optionsGroup, SWT.NONE);
	runMiniproxyLabel.setText(Messages.getString("PEDialogs.MiniproxyLabel"));
	runMiniproxy = new Button(optionsGroup, SWT.CHECK);
	preferenceValue = preferences.getString(PEPreferenceConstants.RUN_MINIPROXY);
	if (preferenceValue.equals(PEPreferenceConstants.OPTION_YES)) {
	    runMiniproxy.setSelection(true);
	}
	loadLevelerOption.addSelectionListener(eventMonitor);
	traceLabel = new Label(optionsGroup, SWT.NONE);
	traceLabel.setText(Messages.getString("PEDialogs.TraceLevelLabel"));
	traceOptions = new Combo(optionsGroup, SWT.READ_ONLY);
	traceOptions.add(PEPreferenceConstants.TRACE_NOTHING);
	traceOptions.add(PEPreferenceConstants.TRACE_FUNCTION);
	traceOptions.add(PEPreferenceConstants.TRACE_DETAIL);
	traceOptions.setText(preferences.getString(PEPreferenceConstants.TRACE_LEVEL));
	traceOptions.addSelectionListener(eventMonitor);

	if (!loadLevelerOption.getSelection()) {
	    setLLWidgetEnableState(false);
	}
	llModeLocal.addSelectionListener(eventMonitor);
	llModeMulticluster.addSelectionListener(eventMonitor);
	llModeDefault.addSelectionListener(eventMonitor);
	nodePollMinInterval.addModifyListener(eventMonitor);
	nodePollMaxInterval.addModifyListener(eventMonitor);
	jobPollInterval.addModifyListener(eventMonitor);
	libOverridePath.addModifyListener(eventMonitor);
	libOverrideBrowse.addSelectionListener(eventMonitor);

	return preferencePane;
    }

    /**
     * Set the widget enable state for these widgets based on whether the use LoadLeveler checkbox is checked
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
     * Update the preferences store with the values set in the widgets managed by this class.
     */
    protected void updatePreferencePage()
    {
	Preferences preferences;

	super.updatePreferencePage();
	preferences = getPreferences();
	if (loadLevelerOption != null) {
	    if (loadLevelerOption.getSelection()) {
		int interval;
		String widgetValue;

		preferences.setValue(PEPreferenceConstants.LOAD_LEVELER_OPTION, PEPreferenceConstants.OPTION_YES);
		if (llModeDefault.getSelection()) {
		    widgetValue = "d";
		} else if (llModeLocal.getSelection()) {
		    widgetValue = "n";
		} else {
		    widgetValue = "y";
		}
		preferences.setValue(PEPreferenceConstants.LOAD_LEVELER_MODE, widgetValue);
		widgetValue = nodePollMinInterval.getText().trim();
		if (widgetValue.length() > 0) {
		    try {
			interval = Integer.valueOf(widgetValue);
		    }
		    catch (NumberFormatException e) {
			setErrorMessage(Messages.getString("PEDialogs.invalidMinPollInterval"));
			return;
		    }
		}
		preferences.setValue(PEPreferenceConstants.NODE_MIN_POLL_INTERVAL, widgetValue);
		widgetValue = nodePollMaxInterval.getText().trim();
		if (widgetValue.length() > 0) {
		    try {
			interval = Integer.valueOf(widgetValue);
		    }
		    catch (NumberFormatException e) {
			setErrorMessage(Messages.getString("PEDialogs.invalidMaxPollInterval"));
			return;
		    }
		}
		preferences.setValue(PEPreferenceConstants.NODE_MAX_POLL_INTERVAL, widgetValue);
		widgetValue = jobPollInterval.getText().trim();
		if (widgetValue.length() > 0) {
		    try {
			interval = Integer.valueOf(widgetValue);
		    }
		    catch (NumberFormatException e) {
			setErrorMessage(Messages.getString("PEDialogs.invalidJobPollInterval"));
			return;
		    }
		}
		preferences.setValue(PEPreferenceConstants.JOB_POLL_INTERVAL, widgetValue);
		widgetValue = libOverridePath.getText().trim();
		preferences.setValue(PEPreferenceConstants.LIBRARY_OVERRIDE, widgetValue);
	    } else {
		preferences.setValue(PEPreferenceConstants.LOAD_LEVELER_OPTION, PEPreferenceConstants.OPTION_NO);
	    }
	}
	if (traceOptions != null) {
	    preferences.setValue(PEPreferenceConstants.TRACE_LEVEL, traceOptions.getText());
	}
	if (runMiniproxy != null) {
	    if (runMiniproxy.getSelection()) {
		preferences.setValue(PEPreferenceConstants.RUN_MINIPROXY, PEPreferenceConstants.OPTION_YES);
	    } else {
		preferences.setValue(PEPreferenceConstants.RUN_MINIPROXY, PEPreferenceConstants.OPTION_NO);
	    }
	}
    }
}