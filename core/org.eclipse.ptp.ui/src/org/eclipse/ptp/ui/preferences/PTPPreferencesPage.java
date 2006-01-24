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
 *******************************************************************************/
package org.eclipse.ptp.ui.preferences;

import java.io.File;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.ControlSystemChoices;
import org.eclipse.ptp.core.MonitoringSystemChoices;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.internal.core.CoreMessages;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PTPPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String EMPTY_STRING = "";
	protected Text outputDirText = null;
	protected Button browseButton = null;
	protected IntegerFieldEditor storeLineField = null;
	protected Combo comboCS = null;
	protected Combo comboMS = null;
	private String outputDIR = EMPTY_STRING;
	private String defaultOutputDIR = "/tmp";
	private int storeLine = PreferenceConstants.DEF_STORE_LINE;
	private int MSChoiceID = -1;
	private int CSChoiceID = -1;
	private int lastMSChoiceID = -1;
	private int lastCSChoiceID = -1;

	public PTPPreferencesPage() {
		setPreferenceStore(PTPCorePlugin.getDefault().getPreferenceStore());
		// setDescription(CoreMessages.getResourceString("PTPPreferencesPage.preferencesDescription"));
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton)
				handleOutputDirectoryBrowseButtonSelected();
			else
				updatePreferencePage();
		}
		public void modifyText(ModifyEvent evt) {
			updatePreferencePage();
		}
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePreferencePage();
		}
	}

	protected WidgetListener listener = new WidgetListener();

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayout(1, true, 0, 0));
		composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		createChooseRSContents(composite);
		// createMPICTRLContents(composite);
		createOutputContents(composite);
		loadSaved();
		defaultSetting();
		return composite;
	}
	private void createOutputContents(Composite parent) {
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText(CoreMessages.getResourceString("PTPPreferencesPage.group_output"));
		Composite outputComposite = new Composite(aGroup, SWT.NONE);
		outputComposite.setLayout(createGridLayout(3, false, 0, 0));
		outputComposite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		new Label(outputComposite, SWT.NONE).setText(CoreMessages.getResourceString("PTPPreferencesPage.output_text"));
		outputDirText = new Text(outputComposite, SWT.SINGLE | SWT.BORDER);
		outputDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		outputDirText.addModifyListener(listener);
		browseButton = SWTUtil.createPushButton(outputComposite, CoreMessages.getResourceString("PTPPreferencesPage.browseButton"), null);
		browseButton.addSelectionListener(listener);
		Composite lineComposite = new Composite(aGroup, SWT.NONE);
		lineComposite.setLayout(new FillLayout());
		lineComposite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		storeLineField = new IntegerFieldEditor(PreferenceConstants.STORE_LINE, CoreMessages.getResourceString("PTPPreferencesPage.store_line_text"), lineComposite);
		storeLineField.setPropertyChangeListener(listener);
		storeLineField.setEmptyStringAllowed(false);
	}
	private void createChooseRSContents(Composite parent) {
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText(CoreMessages.getResourceString("PTPPreferencesPage.group_rs"));
		new Label(aGroup, SWT.NONE).setText(CoreMessages.getResourceString("PTPPreferencesPage.group_cs"));
		comboCS = new Combo(aGroup, SWT.READ_ONLY);
		comboCS.setItems(ControlSystemChoices.getCSStrings());
		comboCS.addSelectionListener(listener);
		new Label(aGroup, SWT.NONE).setText(CoreMessages.getResourceString("PTPPreferencesPage.group_ms"));
		comboMS = new Combo(aGroup, SWT.READ_ONLY);
		comboMS.setItems(MonitoringSystemChoices.getMSStrings());
		comboMS.addSelectionListener(listener);
	}
	protected void defaultSetting() {
		comboMS.select(MonitoringSystemChoices.getMSArrayIndexByID(MSChoiceID));
		comboCS.select(ControlSystemChoices.getCSArrayIndexByID(CSChoiceID));
		outputDirText.setText(outputDIR);
		storeLineField.setStringValue(String.valueOf(storeLine));
	}
	private void loadSaved() {
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		// System.out.println("PREFS:");
		// String[] foo = preferences.defaultPropertyNames();
		// for(int i=0; i<foo.length; i++) {
		// System.out.println("DEFAULT["+i+"] = "+foo[i]);
		// }
		// foo = preferences.propertyNames();
		// for(int i=0; i<foo.length; i++) {
		// System.out.println("non-default prop["+i+"] = "+foo[i]);
		// }
		outputDIR = preferences.getString(PreferenceConstants.OUTPUT_DIR);
		if (outputDIR.equals(""))
			outputDIR = defaultOutputDIR;
		if (outputDIR != null)
			outputDirText.setText(outputDIR);
		storeLine = preferences.getInt(PreferenceConstants.STORE_LINE);
		storeLineField.setStringValue(String.valueOf(storeLine));
		MSChoiceID = preferences.getInt(PreferenceConstants.MONITORING_SYSTEM_SELECTION);
		comboMS.select(MonitoringSystemChoices.getMSArrayIndexByID(MSChoiceID));
		lastMSChoiceID = MSChoiceID;
		CSChoiceID = preferences.getInt(PreferenceConstants.CONTROL_SYSTEM_SELECTION);
		comboCS.select(ControlSystemChoices.getCSArrayIndexByID(CSChoiceID));
		lastCSChoiceID = CSChoiceID;
	}
	/* do stuff on init() of preferences, if anything */
	public void init(IWorkbench workbench) {}
	public void dispose() {
		super.dispose();
	}
	public void performDefaults() {
		defaultSetting();
		updateApplyButton();
	}
	private void store() {
		MSChoiceID = MonitoringSystemChoices.getMSIDByIndex(comboMS.getSelectionIndex());
		CSChoiceID = ControlSystemChoices.getCSIDByIndex(comboCS.getSelectionIndex());
		outputDIR = outputDirText.getText();
		storeLine = storeLineField.getIntValue();
	}
	public boolean performOk() {
		store();
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		preferences.setValue(PreferenceConstants.MONITORING_SYSTEM_SELECTION, MSChoiceID);
		preferences.setValue(PreferenceConstants.CONTROL_SYSTEM_SELECTION, CSChoiceID);
		preferences.setValue(PreferenceConstants.OUTPUT_DIR, outputDIR);
		preferences.setValue(PreferenceConstants.STORE_LINE, storeLine);
		PTPCorePlugin.getDefault().savePluginPreferences();
		/*
		 * IModelManager manager = PTPCorePlugin.getDefault().getModelManager(); 
		 * if (manager.isParallelPerspectiveOpen() && (lastMSChoiceID != MSChoiceID || lastCSChoiceID != CSChoiceID)) {
		 * manager.refreshRuntimeSystems(CSChoiceID, MSChoiceID); }
		 */
		PTPUIPlugin.refreshRuntimeSystem();
		File outputDirPath = new File(outputDIR);
		if (!outputDirPath.exists())
			outputDirPath.mkdir();
		lastCSChoiceID = CSChoiceID;
		lastMSChoiceID = MSChoiceID;
		return true;
	}
	protected void handleOutputDirectoryBrowseButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setText(CoreMessages.getResourceString("PTPPreferencesPage.Select_Output_Directory"));
		String currectDirPath = getFieldContent(outputDirText.getText());
		if (currectDirPath != null) {
			File path = new File(currectDirPath);
			if (path.exists())
				dialog.setFilterPath(currectDirPath);
		}
		String selectedDirPath = dialog.open();
		if (selectedDirPath != null)
			outputDirText.setText(selectedDirPath);
	}
	/**
	 * Returns true if the selected monitoring system is a valid selection. We allow the system to display MS choices which are not yet complete or enabled yet.
	 */
	protected boolean isValidMSSetting() {
		int intchoice = comboMS.getSelectionIndex();
		if (intchoice > 1) {
			setErrorMessage("Sorry, that monitoring system choice is not yet implemented.");
			setValid(false);
			return false;
		} else if (intchoice < 0) {
			setErrorMessage("Select a monitoring system.");
			setValid(false);
			return false;
		}
		return true;
	}
	/**
	 * Returns true if the selected control system is a valid selection. We allow the system to display CS choices which are not yet complete or enabled yet.
	 */
	protected boolean isValidCSSetting() {
		int intchoice = comboCS.getSelectionIndex();
		if (intchoice > 1) {
			setErrorMessage("Sorry, that control system choice is not yet implemented.");
			setValid(false);
			return false;
		} else if (intchoice < 0) {
			setErrorMessage("Select a control system.");
			setValid(false);
			return false;
		}
		return true;
	}
	/**
	 * Checks that the selected control and monitoring systems are able to work together. Returns true if they are a valid pair, else false.
	 */
	protected boolean isValidRSSetting() {
		int CS = comboCS.getSelectionIndex();
		int MS = comboMS.getSelectionIndex();
		int CSID = ControlSystemChoices.getCSIDByIndex(CS);
		int MSID = MonitoringSystemChoices.getMSIDByIndex(MS);
		if (CSID == ControlSystemChoices.SIMULATED && MSID == MonitoringSystemChoices.SIMULATED)
			return true;
		if (CSID == ControlSystemChoices.ORTE && MSID == MonitoringSystemChoices.ORTE)
			return true;
		setErrorMessage("Sorry, that combination of control and monitoring systems it not valid.");
		setValid(false);
		return false;
	}
	protected boolean isValidOutputSetting() {
		String name = getFieldContent(outputDirText.getText());
		if (name == null) {
			setErrorMessage(CoreMessages.getResourceString("PTPPreferencesPage.Incorrect_Output_directory"));
			setValid(false);
			return false;
		}
		File path = new File(name);
		if (!path.exists()) {
			File parent = path.getParentFile();
			if (parent == null || !parent.exists()) {
				setErrorMessage(CoreMessages.getResourceString("PTPPreferencesPage.Incorrect_Output_directory"));
				setValid(false);
				return false;
			}
		}
		if (!storeLineField.isValid()) {
			setErrorMessage(storeLineField.getErrorMessage());
			setValid(false);
			return false;
		}
		return true;
	}
	protected void updatePreferencePage() {
		setErrorMessage(null);
		setMessage(null);
		if (!isValidCSSetting())
			return;
		if (!isValidMSSetting())
			return;
		if (!isValidRSSetting())
			return;
		if (!isValidOutputSetting())
			return;
		setValid(true);
	}
	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;
		return text;
	}
	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}
	protected GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1)
			gd = new GridData();
		else
			gd = new GridData(style);
		gd.horizontalSpan = space;
		return gd;
	}
}
