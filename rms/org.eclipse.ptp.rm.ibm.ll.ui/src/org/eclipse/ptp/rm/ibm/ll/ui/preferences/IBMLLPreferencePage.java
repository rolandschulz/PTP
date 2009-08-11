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

import java.io.File;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLPreferenceConstants;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLPreferenceManager;
import org.eclipse.ptp.rm.ibm.ll.ui.internal.ui.Messages;
import org.eclipse.ptp.rm.remote.ui.preferences.AbstractRemotePreferencePage;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class IBMLLPreferencePage extends AbstractRemotePreferencePage {

	private Text proxyLibraryTextWidget = null;
	private EventMonitor libraryListener = null; /* validate library name */
	private Button libraryBrowseButton = null;

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

	private Text proxyTemplateTextWidget = null;
	private EventMonitor templateListener = null; /* validate template file name */
	private Button proxyTemplateNeverRadioButton = null;
	private Button proxyTemplateAlwaysRadioButton = null;
	private Button templateBrowseButton = null;

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

	public boolean performOk() {
//		System.err.println("preferences: performOk entered...");
		updatePreferencePage();
		return super.performOk();
	}

	private class EventMonitor implements SelectionListener, ModifyListener {
		public EventMonitor() {
//			System.err.println("preferences: EventMonitor");
		}

		public void widgetDefaultSelected(SelectionEvent e) {
//			System.err.println("preferences: widgetDefaultSelected");
		}

		public void widgetSelected(SelectionEvent e) {
//			System.err.println("preferences: widgetSelected");
	
				Object source = e.getSource();
				if (source == libraryBrowseButton) {
//					System.err.println("preferences: libraryBrowseButton");
					DirectoryDialog dialog = new DirectoryDialog(getShell());
					String selectedPath = dialog.open();
					if (selectedPath != null)
						proxyLibraryTextWidget.setText(selectedPath);
				} 
				else if (source == templateBrowseButton) {
//					System.err.println("preferences: templateBrowseButton");
					FileDialog dialog = new FileDialog(getShell());
					String correctPath = getFieldContent(proxyTemplateTextWidget.getText().trim());
					if (correctPath != null) {
						File path = new File(correctPath);
						if (path.exists())
							dialog.setFilterPath(path.isFile() ? correctPath : path
									.getParent());
					}

					String selectedPath = dialog.open();
					if (selectedPath != null)
						proxyTemplateTextWidget.setText(selectedPath);
				}
		}

		public void widgetModified(SelectionEvent e) {
//			System.err.println("preferences: widgetModified");
		}

		public void modifyText(ModifyEvent e) {
//			System.err.println("preferences: modifyText");
//			System.err.println("widget entered is "+ e.widget.toString());
			if (e.widget.equals(proxyPollingJob)) {
//				System.err.println("widget entered is proxyPollingJob");
			}
			if (e.widget.equals(proxyPollingNodeMin)) {
//				System.err.println("widget entered is proxyPollingNodeMin");
				int min_value = proxyPollingNodeMin.getSelection();
				int max_value = proxyPollingNodeMax.getSelection();
				if (min_value > max_value) {
					proxyPollingNodeMax.setSelection(min_value);
				}
				
			}
			if (e.widget.equals(proxyPollingNodeMax)) {
//				System.err.println("widget entered is proxyPollingNodeMax");
				int min_value = proxyPollingNodeMin.getSelection();
				int max_value = proxyPollingNodeMax.getSelection();
				if (max_value < min_value) {
					proxyPollingNodeMin.setSelection(max_value);
				}
			}
			if (e.widget.equals(proxyLibraryTextWidget)) {
//				System.err.println("widget entered is proxyLibraryTextWidget");
				String correctPath = getFieldContent(proxyLibraryTextWidget.getText().trim());
				if (correctPath != null) {
					File path = new File(correctPath);
					if (path.exists() && (path.isDirectory())) {
						setErrorMessage(null);
						setValid(true);
					}
					else {
						setErrorMessage(Messages
								.getString("Invalid.llLibraryPath"));
						setValid(false);
					}
				}
			}
			if (e.widget.equals(proxyTemplateTextWidget)) {
//				System.err.println("widget entered is proxyTemplateTextWidget");
				String correctPath = getFieldContent(proxyTemplateTextWidget.getText().trim());
				if (correctPath != null) {
					File path = new File(correctPath);
					if (path.exists() && (path.isFile())) {
						setErrorMessage(null);
						setValid(true);
					}
					else {
						setErrorMessage(Messages
								.getString("Invalid.llJobCommandFileTemplate"));
						setValid(false);
					}
				}
				
			}
			

		}
	}

	@Override
	public Preferences getPreferences() {
		return IBMLLPreferenceManager.getPreferences();
	}

	@Override
	public void savePreferences() {
		IBMLLPreferenceManager.savePreferences();
	}

	/**
	 * Create the widgets for preference settings. This method calls the
	 * superclass createContents() method to create the default widgets for the
	 * panel, then creates the additional widgets needed.
	 * 
	 * @param parent -
	 *            The parent widget for this pane
	 * 
	 * @return the top level widget for the preference pane
	 */
	protected Control createContents(Composite parent) {
		Control preferencePane;
		Group proxyLibraryGroup = null;
		Group proxyOptionsGroup = null;
		Group proxyDebugGroup = null;
		Group guiOptionsGroup = null;
		Group proxyMulticlusterGroup = null;
		Group proxyTemplateGroup = null;
		Group proxyTemplateOptionsGroup = null;
		Group proxyPollingGroup = null;
		Preferences preferences = null;
		libraryBrowseButton = null;
		templateBrowseButton = null;
		
		String preferenceValue;

		preferences = getPreferences();
		eventMonitor = new EventMonitor();
		preferencePane = super.createContents(parent);

		// *********************************************************************
		// Alternate LoadLeveler Library Install Location
		// *********************************************************************
		proxyLibraryGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyLibraryGroup.setLayout(createGridLayout(3, false, 0, 0));
		proxyLibraryGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL,
				5));
		proxyLibraryGroup.setText(Messages
				.getString("IBMLLPrefWizPage.proxyLibraryGroupLabel"));

		new Label(proxyLibraryGroup, SWT.NONE).setText(Messages
				.getString("IBMLLPrefWizPage.proxyLibraryLabel"));

		proxyLibraryTextWidget = new Text(proxyLibraryGroup, SWT.SINGLE
				| SWT.BORDER);
		proxyLibraryTextWidget.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		libraryListener = new EventMonitor();
		proxyLibraryTextWidget.addModifyListener(libraryListener);
		proxyLibraryTextWidget
				.setText(preferences
						.getString(IBMLLPreferenceConstants.PROXY_LOADLEVELER_LIBRARY_PATH));
		proxyLibraryTextWidget.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.proxyLibraryToolTip"));

		 libraryBrowseButton =
		 SWTUtil.createPushButton(proxyLibraryGroup, Messages
		 .getString("IBMLLPrefWizPage.browseButton"),
		 null);
		 libraryBrowseButton.addSelectionListener(libraryListener);

		// *********************************************************************
		// Check box group for proxy messages
		// *********************************************************************
		proxyOptionsGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyOptionsGroup.setLayout(new RowLayout());
		proxyOptionsGroup.setText(Messages
				.getString("IBMLLPrefWizPage.proxyOptionsGroupLabel"));

		proxyTraceMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyTraceMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.ProxyTraceMessageLabel"));
		proxyTraceMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.ProxyTraceMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.PROXY_TRACE_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyTraceMessageButton.setSelection(true);
		}
		// proxyTraceMessageButton.addSelectionListener(eventMonitor);

		proxyInfoMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyInfoMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.ProxyInfoMessageLabel"));
		proxyInfoMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.ProxyInfoMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.PROXY_INFO_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyInfoMessageButton.setSelection(true);
		}
		// proxyInfoMessageButton.addSelectionListener(eventMonitor);

		proxyWarningMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyWarningMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.ProxyWarningMessageLabel"));
		proxyWarningMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.ProxyWarningMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.PROXY_WARNING_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyWarningMessageButton.setSelection(true);
		}
		// proxyWarningMessageButton.addSelectionListener(eventMonitor);

		proxyErrorMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyErrorMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.ProxyErrorMessageLabel"));
		proxyErrorMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.ProxyErrorMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.PROXY_ERROR_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyErrorMessageButton.setSelection(true);
		}
		// proxyErrorMessageButton.addSelectionListener(eventMonitor);

		proxyFatalMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyFatalMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.ProxyFatalMessageLabel"));
		proxyFatalMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.ProxyFatalMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.PROXY_FATAL_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyFatalMessageButton.setSelection(true);
		}
		// proxyFatalMessageButton.addSelectionListener(eventMonitor);

		proxyArgsMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyArgsMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.ProxyArgsMessageLabel"));
		proxyArgsMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.ProxyArgsMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.PROXY_ARGS_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyArgsMessageButton.setSelection(true);
		}
		// proxyArgsMessageButton.addSelectionListener(eventMonitor);

		// *********************************************************************
		// Check box group for proxy debug
		// *********************************************************************
		proxyDebugGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyDebugGroup.setLayout(new RowLayout());
		proxyDebugGroup.setText(Messages
				.getString("IBMLLPrefWizPage.proxyDebugGroupLabel"));

		proxyDebugLoopButton = new Button(proxyDebugGroup, SWT.CHECK);
		proxyDebugLoopButton.setText(Messages
				.getString("IBMLLPrefWizPage.ProxyDebugLoopLabel"));
		proxyDebugLoopButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.ProxyDebugLoopToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.PROXY_DEBUG_LOOP);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyDebugLoopButton.setSelection(true);
		}
		// proxyDebugLoopButton.addSelectionListener(eventMonitor);

		// *********************************************************************
		// Check box group for gui messages
		// *********************************************************************
		guiOptionsGroup = new Group((Composite) preferencePane, SWT.NONE);
		guiOptionsGroup.setLayout(new RowLayout());
		guiOptionsGroup.setText(Messages
				.getString("IBMLLPrefWizPage.guiOptionsGroupLabel"));

		guiTraceMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiTraceMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.GuiTraceMessageLabel"));
		guiTraceMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.GuiTraceMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.GUI_TRACE_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiTraceMessageButton.setSelection(true);
		}
		// guiTraceMessageButton.addSelectionListener(eventMonitor);

		guiInfoMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiInfoMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.GuiInfoMessageLabel"));
		guiInfoMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.GuiInfoMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.GUI_INFO_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiInfoMessageButton.setSelection(true);
		}
		// guiInfoMessageButton.addSelectionListener(eventMonitor);

		guiWarningMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiWarningMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.GuiWarningMessageLabel"));
		guiWarningMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.GuiWarningMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.GUI_WARNING_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiWarningMessageButton.setSelection(true);
		}
		// guiWarningMessageButton.addSelectionListener(eventMonitor);

		guiErrorMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiErrorMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.GuiErrorMessageLabel"));
		guiErrorMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.GuiErrorMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.GUI_ERROR_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiErrorMessageButton.setSelection(true);
		}
		// guiErrorMessageButton.addSelectionListener(eventMonitor);

		guiFatalMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiFatalMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.GuiFatalMessageLabel"));
		guiFatalMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.GuiFatalMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.GUI_FATAL_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiFatalMessageButton.setSelection(true);
		}
		// guiFatalMessageButton.addSelectionListener(eventMonitor);

		guiArgsMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiArgsMessageButton.setText(Messages
				.getString("IBMLLPrefWizPage.GuiArgsMessageLabel"));
		guiArgsMessageButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.GuiArgsMessageToolTip"));
		preferenceValue = preferences
				.getString(IBMLLPreferenceConstants.GUI_ARGS_MESSAGE);
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiArgsMessageButton.setSelection(true);
		}
		// guiArgsMessageButton.addSelectionListener(eventMonitor);

		// *********************************************************************
		// Radio box group for multicluster
		// *********************************************************************
		proxyMulticlusterGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyMulticlusterGroup.setLayout(new RowLayout());
		proxyMulticlusterGroup.setText(Messages
				.getString("IBMLLPrefWizPage.proxyMulticlusterGroupLabel"));

		proxyLLDefaultRadioButton = new Button(proxyMulticlusterGroup,
				SWT.RADIO);
		proxyLLDefaultRadioButton
				.setText(Messages
						.getString("IBMLLPrefWizPage.proxyMulticlusterDefaultLabel"));
		preferenceValue = preferences.getString(IBMLLPreferenceConstants.PROXY_DEFAULT_MULTICLUSTER);
        if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
        	proxyLLDefaultRadioButton.setSelection(true);
        }
		proxyLLDefaultRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		// proxyLLDefaultRadioButton.addSelectionListener(radioSelected);
		proxyLLDefaultRadioButton
				.setToolTipText(Messages
						.getString("IBMLLPrefWizPage.proxyMulticlusterDefaultToolTip"));

		proxyForceLocalRadioButton = new Button(proxyMulticlusterGroup,
				SWT.RADIO);
		proxyForceLocalRadioButton
				.setText(Messages
						.getString("IBMLLPrefWizPage.proxyMulticlusterForceLocalLabel"));
		preferenceValue = preferences.getString(IBMLLPreferenceConstants.PROXY_FORCE_LOCAL);
        if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
        	proxyForceLocalRadioButton.setSelection(true);
        }
		proxyForceLocalRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		// proxyForceLocalRadioButton.addSelectionListener(radioSelected);
		proxyForceLocalRadioButton
				.setToolTipText(Messages
						.getString("IBMLLPrefWizPage.proxyMulticlusterForceLocalToolTip"));

		proxyForceMulticlusterRadioButton = new Button(proxyMulticlusterGroup,
				SWT.RADIO);
		proxyForceMulticlusterRadioButton
				.setText(Messages
						.getString("IBMLLPrefWizPage.proxyMulticlusterForceMulticlusterLabel"));
		preferenceValue = preferences.getString(IBMLLPreferenceConstants.PROXY_FORCE_MULTICLUSTER);
        if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
        	proxyForceMulticlusterRadioButton.setSelection(true);
        }
		proxyForceMulticlusterRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		// proxyForceMulticlusterRadioButton.addSelectionListener(radioSelected);
		proxyForceMulticlusterRadioButton
				.setToolTipText(Messages
						.getString("IBMLLPrefWizPage.proxyMulticlusterForceMulticlusterToolTip"));

		// *********************************************************************
		// Template name group
		// *********************************************************************
		proxyTemplateGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyTemplateGroup.setLayout(createGridLayout(3, false, 0, 0));
		proxyTemplateGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL,
				5));
		proxyTemplateGroup.setText(Messages
				.getString("IBMLLPrefWizPage.proxyTemplateGroupLabel"));

		new Label(proxyTemplateGroup, SWT.NONE).setText(Messages
				.getString("IBMLLPrefWizPage.proxyTemplateLabel"));

		proxyTemplateTextWidget = new Text(proxyTemplateGroup, SWT.SINGLE
				| SWT.BORDER);
		proxyTemplateTextWidget.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		templateListener = new EventMonitor();
		proxyTemplateTextWidget.addModifyListener(templateListener);
		proxyTemplateTextWidget
				.setText(preferences
						.getString(IBMLLPreferenceConstants.PROXY_LOADLEVELER_TEMPLATE_FILE));
		proxyTemplateTextWidget.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.proxyTemplateToolTip"));

		 templateBrowseButton =
		 SWTUtil.createPushButton(proxyTemplateGroup,
		 Messages.getString("IBMLLPrefWizPage.browseButton"),
		 null);
		 templateBrowseButton.addSelectionListener(templateListener);

		// *********************************************************************
		// Template options group
		// *********************************************************************
		proxyTemplateOptionsGroup = new Group((Composite) preferencePane,
				SWT.NONE);
		proxyTemplateOptionsGroup.setLayout(new RowLayout());
		proxyTemplateOptionsGroup
				.setText(Messages
						.getString("IBMLLPrefWizPage.proxyTemplateOptionsGroupLabel"));

		proxyTemplateNeverRadioButton = new Button(proxyTemplateOptionsGroup,
				SWT.RADIO);
		proxyTemplateNeverRadioButton.setText(Messages
				.getString("IBMLLPrefWizPage.proxyTemplateNeverLabel"));
		proxyTemplateNeverRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		preferenceValue = preferences.getString(IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_NEVER);
        if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
        	proxyTemplateNeverRadioButton.setSelection(true);
        }
		// proxyTemplateNeverRadioButton.addSelectionListener(radioSelected);
		proxyTemplateNeverRadioButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.proxyTemplateNeverToolTip"));

		proxyTemplateAlwaysRadioButton = new Button(proxyTemplateOptionsGroup,
				SWT.RADIO);
		proxyTemplateAlwaysRadioButton.setText(Messages
				.getString("IBMLLPrefWizPage.proxyTemplateAlwaysLabel"));
		preferenceValue = preferences.getString(IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_ALWAYS);
        if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
        	proxyTemplateAlwaysRadioButton.setSelection(true);
        }
		proxyTemplateAlwaysRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		// proxyTemplateAlwaysRadioButton.addSelectionListener(radioSelected);
		proxyTemplateAlwaysRadioButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.proxyTemplateAlwaysToolTip"));

		// *********************************************************************
		// Polling options group
		// *********************************************************************
		proxyPollingGroup = new Group((Composite) preferencePane, SWT.NONE);
		proxyPollingGroup.setLayout(new RowLayout());
		proxyPollingGroup.setText(Messages
				.getString("IBMLLPrefWizPage.proxyPollingGroupLabel"));

		new Label(proxyPollingGroup, SWT.NONE).setText(Messages
				.getString("IBMLLPrefWizPage.proxyPollingNodeMinLabel"));

		proxyPollingNodeMin = new Spinner(proxyPollingGroup, SWT.READ_ONLY);
		proxyPollingNodeMin.setIncrement(10);
		proxyPollingNodeMin.setMinimum(30);
		proxyPollingNodeMin.setMaximum(300);
		proxyPollingNodeMin.setPageIncrement(50);
		proxyPollingNodeMin.setSelection(preferences.getInt(IBMLLPreferenceConstants.PROXY_MIN_NODE_POLLING));
		proxyPollingNodeMin.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.proxyPollingNodeMinToolTip"));
		proxyPollingNodeMin.addModifyListener(eventMonitor);

		new Label(proxyPollingGroup, SWT.NONE).setText(Messages
				.getString("IBMLLPrefWizPage.proxyPollingNodeMaxLabel"));

		proxyPollingNodeMax = new Spinner(proxyPollingGroup, SWT.READ_ONLY);
		proxyPollingNodeMax.setIncrement(10);
		proxyPollingNodeMax.setMinimum(30);
		proxyPollingNodeMax.setMaximum(500);
		proxyPollingNodeMax.setPageIncrement(50);
		proxyPollingNodeMax.setSelection(preferences.getInt(IBMLLPreferenceConstants.PROXY_MAX_NODE_POLLING));
		proxyPollingNodeMax.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.proxyPollingNodeMaxToolTip"));
		proxyPollingNodeMax.addModifyListener(eventMonitor);

		new Label(proxyPollingGroup, SWT.NONE).setText(Messages
				.getString("IBMLLPrefWizPage.proxyPollingJobLabel"));

		proxyPollingJob = new Spinner(proxyPollingGroup, SWT.READ_ONLY);
		proxyPollingJob.setIncrement(10);
		proxyPollingJob.setMinimum(30);
		proxyPollingJob.setMaximum(300);
		proxyPollingJob.setPageIncrement(50);
		proxyPollingJob.setSelection(preferences.getInt(IBMLLPreferenceConstants.PROXY_JOB_POLLING));
		proxyPollingJob.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyPollingJobToolTip"));
		proxyPollingJob.addModifyListener(eventMonitor);

		return preferencePane;
	}

	/**
	 * Update the preferences store with the values set in the widgets managed
	 * by this class.
	 */
	protected void updatePreferencePage() {
		Preferences preferences;

		preferences = getPreferences();

		if (proxyLibraryTextWidget != null) {
			preferences.setValue(
					IBMLLPreferenceConstants.PROXY_LOADLEVELER_LIBRARY_PATH,
					proxyLibraryTextWidget.getText().trim());
		}

		if (proxyTraceMessageButton != null) {
			if (proxyTraceMessageButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_TRACE_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_TRACE_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyInfoMessageButton != null) {
			if (proxyInfoMessageButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_INFO_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_INFO_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyWarningMessageButton != null) {
			if (proxyWarningMessageButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_WARNING_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_WARNING_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyErrorMessageButton != null) {
			if (proxyErrorMessageButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_ERROR_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_ERROR_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyFatalMessageButton != null) {
			if (proxyFatalMessageButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_FATAL_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_FATAL_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyArgsMessageButton != null) {
			if (proxyArgsMessageButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_ARGS_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_ARGS_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyDebugLoopButton != null) {
			if (proxyDebugLoopButton.getSelection()) {
				preferences.setValue(IBMLLPreferenceConstants.PROXY_DEBUG_LOOP,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(IBMLLPreferenceConstants.PROXY_DEBUG_LOOP,
						IBMLLPreferenceConstants.LL_NO);
			}
		}


		if (guiTraceMessageButton != null) {
			if (guiTraceMessageButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.GUI_TRACE_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.GUI_TRACE_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiInfoMessageButton != null) {
			if (guiInfoMessageButton.getSelection()) {
				preferences.setValue(IBMLLPreferenceConstants.GUI_INFO_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(IBMLLPreferenceConstants.GUI_INFO_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiWarningMessageButton != null) {
			if (guiWarningMessageButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.GUI_WARNING_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.GUI_WARNING_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiErrorMessageButton != null) {
			if (guiErrorMessageButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.GUI_ERROR_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.GUI_ERROR_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiFatalMessageButton != null) {
			if (guiFatalMessageButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.GUI_FATAL_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.GUI_FATAL_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (guiArgsMessageButton != null) {
			if (guiArgsMessageButton.getSelection()) {
				preferences.setValue(IBMLLPreferenceConstants.GUI_ARGS_MESSAGE,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(IBMLLPreferenceConstants.GUI_ARGS_MESSAGE,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyLLDefaultRadioButton != null) {
			if (proxyLLDefaultRadioButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_DEFAULT_MULTICLUSTER,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_DEFAULT_MULTICLUSTER,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyForceLocalRadioButton != null) {
			if (proxyForceLocalRadioButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_FORCE_LOCAL,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_FORCE_LOCAL,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyForceMulticlusterRadioButton != null) {
			if (proxyForceMulticlusterRadioButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_FORCE_MULTICLUSTER,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_FORCE_MULTICLUSTER,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyTemplateTextWidget != null) {
			preferences.setValue(
					IBMLLPreferenceConstants.PROXY_LOADLEVELER_TEMPLATE_FILE,
					proxyTemplateTextWidget.getText().trim());
		}

		if (proxyTemplateNeverRadioButton != null) {
			if (proxyTemplateNeverRadioButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_NEVER,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_NEVER,
						IBMLLPreferenceConstants.LL_NO);
			}
		}

		if (proxyTemplateAlwaysRadioButton != null) {
			if (proxyTemplateAlwaysRadioButton.getSelection()) {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_ALWAYS,
						IBMLLPreferenceConstants.LL_YES);
			} else {
				preferences.setValue(
						IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_ALWAYS,
						IBMLLPreferenceConstants.LL_NO);
			}
		}
		
		if (proxyPollingNodeMin != null) {
			preferences.setValue(IBMLLPreferenceConstants.PROXY_MIN_NODE_POLLING,proxyPollingNodeMin.getSelection());
		}
		
		if (proxyPollingNodeMax != null) {
			preferences.setValue(IBMLLPreferenceConstants.PROXY_MAX_NODE_POLLING,proxyPollingNodeMax.getSelection());
		}
		
		if (proxyPollingJob != null) {
			preferences.setValue(IBMLLPreferenceConstants.PROXY_JOB_POLLING,proxyPollingJob.getSelection());
		}

	}
	
	protected String getFieldContent(String text) 
	{
		if (text.trim().length() == 0 || text.equals(""))
			return null;

		return text;
	}

}
