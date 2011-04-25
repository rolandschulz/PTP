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
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.ll.ui.preferences;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLCorePlugin;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLPreferenceConstants;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLPreferenceManager;
import org.eclipse.ptp.rm.ibm.ll.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.preferences.AbstractRemoteRMPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class IBMLLPreferencePage extends AbstractRemoteRMPreferencePage {

	private class EventMonitor implements SelectionListener, ModifyListener {
		public EventMonitor() {
			// System.err.println("preferences: EventMonitor");
		}

		public void modifyText(ModifyEvent e) {
			// System.err.println("preferences: modifyText");
			// System.err.println("widget entered is "+ e.widget.toString());
			if (e.widget.equals(proxyPollingJob)) {
				// System.err.println("widget entered is proxyPollingJob");
			}
			if (e.widget.equals(proxyPollingNodeMin)) {
				// System.err.println("widget entered is proxyPollingNodeMin");
				int min_value = proxyPollingNodeMin.getSelection();
				int max_value = proxyPollingNodeMax.getSelection();
				if (min_value > max_value) {
					proxyPollingNodeMax.setSelection(min_value);
				}

			}
			if (e.widget.equals(proxyPollingNodeMax)) {
				// System.err.println("widget entered is proxyPollingNodeMax");
				int min_value = proxyPollingNodeMin.getSelection();
				int max_value = proxyPollingNodeMax.getSelection();
				if (max_value < min_value) {
					proxyPollingNodeMin.setSelection(max_value);
				}
			}

		}

		public void widgetDefaultSelected(SelectionEvent e) {
			// System.err.println("preferences: widgetDefaultSelected");
		}

		public void widgetModified(SelectionEvent e) {
			// System.err.println("preferences: widgetModified");
		}

		public void widgetSelected(SelectionEvent e) {
			// System.err.println("preferences: widgetSelected");

			e.getSource();
		}
	}

	private final EventMonitor libraryListener = null; /* validate library name */
	private Button proxyTraceMessageButton = null;
	private Button proxyInfoMessageButton = null;
	private Button proxyWarningMessageButton = null;
	private Button proxyErrorMessageButton = null;
	private Button proxyFatalMessageButton = null;
	private Button proxyArgsMessageButton = null;
	private Button proxyDebugLoopButton = null;
	private Button proxyForceLocalRadioButton = null;
	private Button proxyForceMulticlusterRadioButton = null;
	private Button proxyLLDefaultRadioButton = null;
	private Button proxyTemplateNeverRadioButton = null;
	private Button proxyTemplateAlwaysRadioButton = null;
	private Spinner proxyPollingNodeMin = null;
	private Spinner proxyPollingNodeMax = null;
	private Spinner proxyPollingJob = null;
	private Button guiTraceMessageButton = null;
	private Button guiInfoMessageButton = null;
	private Button guiWarningMessageButton = null;
	private Button guiErrorMessageButton = null;
	private Button guiFatalMessageButton = null;
	private Button guiArgsMessageButton = null;
	private EventMonitor eventMonitor = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.preferences.AbstractRemoteRMPreferencePage#
	 * getPreferenceQualifier()
	 */
	@Override
	public String getPreferenceQualifier() {
		return IBMLLCorePlugin.getUniqueIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.preferences.AbstractRemoteRMPreferencePage#performOk
	 * ()
	 */
	@Override
	public boolean performOk() {
		// System.err.println("preferences: performOk entered...");
		updatePreferencePage();
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.preferences.AbstractRemoteRMPreferencePage#
	 * savePreferences()
	 */
	@Override
	public void savePreferences() {
		IBMLLPreferenceManager.savePreferences();
	}

	/**
	 * Create the widgets for preference settings. This method calls the
	 * superclass createContents() method to create the default widgets for the
	 * panel, then creates the additional widgets needed.
	 * 
	 * @param parent
	 *            - The parent widget for this pane
	 * 
	 * @return the top level widget for the preference pane
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control preferencePane;
		Group proxyOptionsGroup = null;
		Group proxyDebugGroup = null;
		Group guiOptionsGroup = null;
		Group proxyMulticlusterGroup = null;
		Group proxyTemplateOptionsGroup = null;
		Group proxyPollingGroup = null;

		String preferenceValue;

		eventMonitor = new EventMonitor();
		preferencePane = super.createContents(parent);

		// *********************************************************************
		// Check box group for proxy messages
		// *********************************************************************
		proxyOptionsGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyOptionsGroup.setLayout(new RowLayout());
		proxyOptionsGroup.setText(Messages.getString("IBMLLPrefWizPage.proxyOptionsGroupLabel")); //$NON-NLS-1$

		proxyTraceMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyTraceMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyTraceMessageLabel")); //$NON-NLS-1$
		proxyTraceMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyTraceMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences
				.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_TRACE_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyTraceMessageButton.setSelection(true);
		}
		// proxyTraceMessageButton.addSelectionListener(eventMonitor);

		proxyInfoMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyInfoMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyInfoMessageLabel")); //$NON-NLS-1$
		proxyInfoMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyInfoMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_INFO_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyInfoMessageButton.setSelection(true);
		}
		// proxyInfoMessageButton.addSelectionListener(eventMonitor);

		proxyWarningMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyWarningMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyWarningMessageLabel")); //$NON-NLS-1$
		proxyWarningMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyWarningMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(),
				IBMLLPreferenceConstants.PROXY_WARNING_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyWarningMessageButton.setSelection(true);
		}
		// proxyWarningMessageButton.addSelectionListener(eventMonitor);

		proxyErrorMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyErrorMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyErrorMessageLabel")); //$NON-NLS-1$
		proxyErrorMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyErrorMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences
				.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_ERROR_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyErrorMessageButton.setSelection(true);
		}
		// proxyErrorMessageButton.addSelectionListener(eventMonitor);

		proxyFatalMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyFatalMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyFatalMessageLabel")); //$NON-NLS-1$
		proxyFatalMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyFatalMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences
				.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FATAL_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyFatalMessageButton.setSelection(true);
		}
		// proxyFatalMessageButton.addSelectionListener(eventMonitor);

		proxyArgsMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyArgsMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyArgsMessageLabel")); //$NON-NLS-1$
		proxyArgsMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyArgsMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_ARGS_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyArgsMessageButton.setSelection(true);
		}
		// proxyArgsMessageButton.addSelectionListener(eventMonitor);

		// *********************************************************************
		// Check box group for proxy debug
		// *********************************************************************
		proxyDebugGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyDebugGroup.setLayout(new RowLayout());
		proxyDebugGroup.setText(Messages.getString("IBMLLPrefWizPage.proxyDebugGroupLabel")); //$NON-NLS-1$

		proxyDebugLoopButton = new Button(proxyDebugGroup, SWT.CHECK);
		proxyDebugLoopButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyDebugLoopLabel")); //$NON-NLS-1$
		proxyDebugLoopButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyDebugLoopToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_DEBUG_LOOP);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyDebugLoopButton.setSelection(true);
		}
		// proxyDebugLoopButton.addSelectionListener(eventMonitor);

		// *********************************************************************
		// Check box group for gui messages
		// *********************************************************************
		guiOptionsGroup = new Group((Composite) preferencePane, SWT.NONE);
		guiOptionsGroup.setLayout(new RowLayout());
		guiOptionsGroup.setText(Messages.getString("IBMLLPrefWizPage.guiOptionsGroupLabel")); //$NON-NLS-1$

		guiTraceMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiTraceMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiTraceMessageLabel")); //$NON-NLS-1$
		guiTraceMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiTraceMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_TRACE_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiTraceMessageButton.setSelection(true);
		}
		// guiTraceMessageButton.addSelectionListener(eventMonitor);

		guiInfoMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiInfoMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiInfoMessageLabel")); //$NON-NLS-1$
		guiInfoMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiInfoMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_INFO_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiInfoMessageButton.setSelection(true);
		}
		// guiInfoMessageButton.addSelectionListener(eventMonitor);

		guiWarningMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiWarningMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiWarningMessageLabel")); //$NON-NLS-1$
		guiWarningMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiWarningMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences
				.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_WARNING_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiWarningMessageButton.setSelection(true);
		}
		// guiWarningMessageButton.addSelectionListener(eventMonitor);

		guiErrorMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiErrorMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiErrorMessageLabel")); //$NON-NLS-1$
		guiErrorMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiErrorMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_ERROR_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiErrorMessageButton.setSelection(true);
		}
		// guiErrorMessageButton.addSelectionListener(eventMonitor);

		guiFatalMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiFatalMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiFatalMessageLabel")); //$NON-NLS-1$
		guiFatalMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiFatalMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_FATAL_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiFatalMessageButton.setSelection(true);
		}
		// guiFatalMessageButton.addSelectionListener(eventMonitor);

		guiArgsMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiArgsMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiArgsMessageLabel")); //$NON-NLS-1$
		guiArgsMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiArgsMessageToolTip")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_ARGS_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiArgsMessageButton.setSelection(true);
		}
		// guiArgsMessageButton.addSelectionListener(eventMonitor);

		// *********************************************************************
		// Radio box group for multicluster
		// *********************************************************************
		proxyMulticlusterGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyMulticlusterGroup.setLayout(new RowLayout());
		proxyMulticlusterGroup.setText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterGroupLabel")); //$NON-NLS-1$

		proxyLLDefaultRadioButton = new Button(proxyMulticlusterGroup, SWT.RADIO);
		proxyLLDefaultRadioButton.setText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterDefaultLabel")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(),
				IBMLLPreferenceConstants.PROXY_DEFAULT_MULTICLUSTER);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyLLDefaultRadioButton.setSelection(true);
		}
		proxyLLDefaultRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		// proxyLLDefaultRadioButton.addSelectionListener(radioSelected);
		proxyLLDefaultRadioButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterDefaultToolTip")); //$NON-NLS-1$

		proxyForceLocalRadioButton = new Button(proxyMulticlusterGroup, SWT.RADIO);
		proxyForceLocalRadioButton.setText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterForceLocalLabel")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FORCE_LOCAL);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyForceLocalRadioButton.setSelection(true);
		}
		proxyForceLocalRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		// proxyForceLocalRadioButton.addSelectionListener(radioSelected);
		proxyForceLocalRadioButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterForceLocalToolTip")); //$NON-NLS-1$

		proxyForceMulticlusterRadioButton = new Button(proxyMulticlusterGroup, SWT.RADIO);
		proxyForceMulticlusterRadioButton.setText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterForceMulticlusterLabel")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(),
				IBMLLPreferenceConstants.PROXY_FORCE_MULTICLUSTER);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyForceMulticlusterRadioButton.setSelection(true);
		}
		proxyForceMulticlusterRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		// proxyForceMulticlusterRadioButton.addSelectionListener(radioSelected);
		proxyForceMulticlusterRadioButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.proxyMulticlusterForceMulticlusterToolTip")); //$NON-NLS-1$

		// *********************************************************************
		// Template options group
		// *********************************************************************
		proxyTemplateOptionsGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyTemplateOptionsGroup.setLayout(new RowLayout());
		proxyTemplateOptionsGroup.setText(Messages.getString("IBMLLPrefWizPage.proxyTemplateOptionsGroupLabel")); //$NON-NLS-1$

		proxyTemplateNeverRadioButton = new Button(proxyTemplateOptionsGroup, SWT.RADIO);
		proxyTemplateNeverRadioButton.setText(Messages.getString("IBMLLPrefWizPage.proxyTemplateNeverLabel")); //$NON-NLS-1$
		proxyTemplateNeverRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(),
				IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_NEVER);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyTemplateNeverRadioButton.setSelection(true);
		}
		// proxyTemplateNeverRadioButton.addSelectionListener(radioSelected);
		proxyTemplateNeverRadioButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyTemplateNeverToolTip")); //$NON-NLS-1$

		proxyTemplateAlwaysRadioButton = new Button(proxyTemplateOptionsGroup, SWT.RADIO);
		proxyTemplateAlwaysRadioButton.setText(Messages.getString("IBMLLPrefWizPage.proxyTemplateAlwaysLabel")); //$NON-NLS-1$
		preferenceValue = Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(),
				IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_ALWAYS);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyTemplateAlwaysRadioButton.setSelection(true);
		}
		proxyTemplateAlwaysRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		// proxyTemplateAlwaysRadioButton.addSelectionListener(radioSelected);
		proxyTemplateAlwaysRadioButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyTemplateAlwaysToolTip")); //$NON-NLS-1$

		// *********************************************************************
		// Polling options group
		// *********************************************************************
		proxyPollingGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyPollingGroup.setLayout(new RowLayout());
		proxyPollingGroup.setText(Messages.getString("IBMLLPrefWizPage.proxyPollingGroupLabel")); //$NON-NLS-1$

		new Label(proxyPollingGroup, SWT.NONE).setText(Messages.getString("IBMLLPrefWizPage.proxyPollingNodeMinLabel")); //$NON-NLS-1$

		proxyPollingNodeMin = new Spinner(proxyPollingGroup, SWT.READ_ONLY);
		proxyPollingNodeMin.setIncrement(10);
		proxyPollingNodeMin.setMinimum(30);
		proxyPollingNodeMin.setMaximum(300);
		proxyPollingNodeMin.setPageIncrement(50);
		proxyPollingNodeMin.setSelection(Preferences.getInt(IBMLLCorePlugin.getUniqueIdentifier(),
				IBMLLPreferenceConstants.PROXY_MIN_NODE_POLLING));
		proxyPollingNodeMin.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyPollingNodeMinToolTip")); //$NON-NLS-1$
		proxyPollingNodeMin.addModifyListener(eventMonitor);

		new Label(proxyPollingGroup, SWT.NONE).setText(Messages.getString("IBMLLPrefWizPage.proxyPollingNodeMaxLabel")); //$NON-NLS-1$

		proxyPollingNodeMax = new Spinner(proxyPollingGroup, SWT.READ_ONLY);
		proxyPollingNodeMax.setIncrement(10);
		proxyPollingNodeMax.setMinimum(30);
		proxyPollingNodeMax.setMaximum(500);
		proxyPollingNodeMax.setPageIncrement(50);
		proxyPollingNodeMax.setSelection(Preferences.getInt(IBMLLCorePlugin.getUniqueIdentifier(),
				IBMLLPreferenceConstants.PROXY_MAX_NODE_POLLING));
		proxyPollingNodeMax.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyPollingNodeMaxToolTip")); //$NON-NLS-1$
		proxyPollingNodeMax.addModifyListener(eventMonitor);

		new Label(proxyPollingGroup, SWT.NONE).setText(Messages.getString("IBMLLPrefWizPage.proxyPollingJobLabel")); //$NON-NLS-1$

		proxyPollingJob = new Spinner(proxyPollingGroup, SWT.READ_ONLY);
		proxyPollingJob.setIncrement(10);
		proxyPollingJob.setMinimum(30);
		proxyPollingJob.setMaximum(300);
		proxyPollingJob.setPageIncrement(50);
		proxyPollingJob.setSelection(Preferences.getInt(IBMLLCorePlugin.getUniqueIdentifier(),
				IBMLLPreferenceConstants.PROXY_JOB_POLLING));
		proxyPollingJob.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyPollingJobToolTip")); //$NON-NLS-1$
		proxyPollingJob.addModifyListener(eventMonitor);

		return preferencePane;
	}

	@Override
	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals("")) {
			return null;
		}

		return text;
	}

	/**
	 * Update the preferences store with the values set in the widgets managed
	 * by this class.
	 */
	@Override
	protected void updatePreferencePage() {

		if (proxyTraceMessageButton != null) {
			if (proxyTraceMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_TRACE_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_TRACE_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyInfoMessageButton != null) {
			if (proxyInfoMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_INFO_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_INFO_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyWarningMessageButton != null) {
			if (proxyWarningMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WARNING_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WARNING_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyErrorMessageButton != null) {
			if (proxyErrorMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_ERROR_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_ERROR_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyFatalMessageButton != null) {
			if (proxyFatalMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FATAL_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FATAL_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyArgsMessageButton != null) {
			if (proxyArgsMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_ARGS_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_ARGS_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyDebugLoopButton != null) {
			if (proxyDebugLoopButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_DEBUG_LOOP,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_DEBUG_LOOP,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiTraceMessageButton != null) {
			if (guiTraceMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_TRACE_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_TRACE_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiInfoMessageButton != null) {
			if (guiInfoMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_INFO_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_INFO_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiWarningMessageButton != null) {
			if (guiWarningMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_WARNING_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_WARNING_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiErrorMessageButton != null) {
			if (guiErrorMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_ERROR_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_ERROR_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiFatalMessageButton != null) {
			if (guiFatalMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_FATAL_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_FATAL_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiArgsMessageButton != null) {
			if (guiArgsMessageButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_ARGS_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_ARGS_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyLLDefaultRadioButton != null) {
			if (proxyLLDefaultRadioButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_DEFAULT_MULTICLUSTER,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_DEFAULT_MULTICLUSTER,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyForceLocalRadioButton != null) {
			if (proxyForceLocalRadioButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FORCE_LOCAL,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FORCE_LOCAL,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyForceMulticlusterRadioButton != null) {
			if (proxyForceMulticlusterRadioButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FORCE_MULTICLUSTER,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FORCE_MULTICLUSTER,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyTemplateNeverRadioButton != null) {
			if (proxyTemplateNeverRadioButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_NEVER,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_NEVER,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyTemplateAlwaysRadioButton != null) {
			if (proxyTemplateAlwaysRadioButton.getSelection()) {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_ALWAYS,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				Preferences.setString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_ALWAYS,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyPollingNodeMin != null) {
			Preferences.setInt(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_MIN_NODE_POLLING,
					proxyPollingNodeMin.getSelection());
		}

		if (proxyPollingNodeMax != null) {
			Preferences.setInt(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_MAX_NODE_POLLING,
					proxyPollingNodeMax.getSelection());
		}

		if (proxyPollingJob != null) {
			Preferences.setInt(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_JOB_POLLING,
					proxyPollingJob.getSelection());
		}

	}
}
