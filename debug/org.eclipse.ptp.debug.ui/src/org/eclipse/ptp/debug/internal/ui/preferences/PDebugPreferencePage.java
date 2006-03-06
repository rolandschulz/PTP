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
package org.eclipse.ptp.debug.internal.ui.preferences;

import java.text.MessageFormat;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.internal.ui.PDebugModelPresentation;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.preferences.AbstractPerferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Clement chu
 *
 */
public class PDebugPreferencePage extends AbstractPerferencePage {
	private Button fPathsButton = null;
	private Button fRegisteredProcessButton = null;
	private IntegerFieldEditor commandTimeoutField = null;
	private Button updateVariableButton = null;
	
    protected class WidgetListener implements IPropertyChangeListener {
    	public void propertyChange(PropertyChangeEvent event) {
	    	setValid(isValid());	
	    }
    }
    protected WidgetListener listener = new WidgetListener();
    
	public PDebugPreferencePage() {
		super();
		setPreferenceStore(PTPDebugUIPlugin.getDefault().getPreferenceStore());		
		setDescription(PreferenceMessages.getString("PDebugPreferencePage.desc"));
	}
	protected Control createContents(Composite parent) {
		//TODO ignored help
		//getWorkbench().getHelpSystem().setHelp(getControl(), IPDebugHelpContextIds.P_DEBUG_PREFERENCE_PAGE);
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		createSpacer(composite, 1);
		createViewSettingPreferences(composite);
		createSpacer(composite, 1);
		createCommunicationPreferences(composite);
		createSpacer(composite, 1);
		createOtherDebugSetting(composite);
		setValues();
		return composite;
	}
	protected void createOtherDebugSetting(Composite parent) {
		updateVariableButton = createCheckButton(parent, PreferenceMessages.getString("PDebugPreferencePage.enableUpdateVariableSuspend"));
	}
	protected void createViewSettingPreferences(Composite parent) {
		Composite comp = createGroupComposite(parent, 1, false, PreferenceMessages.getString("PDebugPreferencePage.default1"));
		fPathsButton = createCheckButton(comp, PreferenceMessages.getString("PDebugPreferencePage.default2"));
		fRegisteredProcessButton = createCheckButton(comp, PreferenceMessages.getString("PDebugPreferencePage.registerProcess"));
		fRegisteredProcessButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				boolean isChecked = fRegisteredProcessButton.getSelection();
				if (!isChecked) {
					MessageDialog.openWarning(getShell(), PreferenceMessages.getString("PDebugPreferencePage.warningTitle1"), PreferenceMessages.getString("PDebugPreferencePage.warning1"));
				}
			}
		});
	}
	protected void createCommunicationPreferences(Composite parent) {
		Composite comp = createGroupComposite(parent, 1, false, PreferenceMessages.getString("PDebugPreferencePage.communication_group"));
		Composite spacingComposite = new Composite(comp, SWT.NONE);
		spacingComposite.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		spacingComposite.setLayoutData(data);
		
		commandTimeoutField = new IntegerFieldEditor(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT, PreferenceMessages.getString("PDebugPreferencePage.command_timeout"), spacingComposite);
		commandTimeoutField.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		commandTimeoutField.setValidRange(IPDebugConstants.MIN_REQUEST_TIMEOUT, IPDebugConstants.MAX_REQUEST_TIMEOUT);
		String minValue = Integer.toString(IPDebugConstants.MIN_REQUEST_TIMEOUT);
		String maxValue = Integer.toString(IPDebugConstants.MAX_REQUEST_TIMEOUT);
		commandTimeoutField.setErrorMessage(MessageFormat.format(PreferenceMessages.getString("PDebugPreferencePage.timeoutError"), new String[]{ minValue, maxValue }));
		commandTimeoutField.setEmptyStringAllowed(false);
		commandTimeoutField.setPropertyChangeListener(listener);
	}
	public void performDefaults() { 
		IPreferenceStore store = getPreferenceStore();
		fPathsButton.setSelection(store.getDefaultBoolean(IPDebugConstants.PREF_SHOW_FULL_PATHS));
		fRegisteredProcessButton.setSelection(store.getDefaultBoolean(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0));
		commandTimeoutField.setStringValue(String.valueOf(store.getDefaultInt(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT)));
		updateVariableButton.setSelection(store.getDefaultBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES));
		super.performDefaults();
	}
	
	public boolean performOk() {
		storeValues();
		PTPDebugUIPlugin.getDefault().savePluginPreferences();
		refreshView();
		return true;
	}
	
	protected void setValues() {
		IPreferenceStore store = getPreferenceStore();
		fPathsButton.setSelection(store.getBoolean(IPDebugConstants.PREF_SHOW_FULL_PATHS));
		fRegisteredProcessButton.setSelection(store.getBoolean(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0));
		commandTimeoutField.setStringValue(String.valueOf(store.getInt(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT)));
		updateVariableButton.setSelection(store.getBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES));
	}
	protected void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IPDebugConstants.PREF_SHOW_FULL_PATHS, fPathsButton.getSelection());
		store.setValue(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0, fRegisteredProcessButton.getSelection());
		store.setValue(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT, commandTimeoutField.getIntValue());
		store.setValue(IPDebugConstants.PREF_UPDATE_VARIABLES, updateVariableButton.getSelection());
	}
	public boolean isValid() {
		setErrorMessage(null);
		setMessage(null);
		if (!commandTimeoutField.isValid()) {
			setErrorMessage(commandTimeoutField.getErrorMessage());
			return false;
		}
		return true;
	}
	
    protected void refreshView() {
    	IWorkbenchPage[] pages = getPages();
    	for (int i=0; i<pages.length; i++) {
			IViewPart part = pages[i].findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
			if (part != null) {
				IDebugView adapter = (IDebugView)part.getAdapter(IDebugView.class);
				if (adapter != null) {				
					Viewer viewer = adapter.getViewer();
					IDebugModelPresentation pres = adapter.getPresentation(PTPDebugCorePlugin.getUniqueIdentifier());
					if (pres != null) {
						pres.setAttribute(PDebugModelPresentation.DISPLAY_FULL_PATHS, fPathsButton.getSelection()?Boolean.TRUE:Boolean.FALSE);
					}
					if (viewer instanceof StructuredViewer) {
						final StructuredViewer structViewer = (StructuredViewer)viewer;
						BusyIndicator.showWhile( structViewer.getControl().getDisplay(), new Runnable() {
							public void run() {
								structViewer.refresh();
							}
						} );
					}
				}
			}
    	}
	}    
}
