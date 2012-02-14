/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.ui.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @since 4.1
 *
 */
public class RemoteDevelopmentPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IntegerFieldEditor indexerErrorLimit;
	
	private IPropertyChangeListener validityChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updateValidState();
			}
        }
    };
	
	public void init(IWorkbench workbench){
		setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
	}
	
	private Composite createComposite(Composite parent, int span, int numColumns) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = span;
		composite.setLayoutData(data);
		return composite;
	}

	/**
	 * Creates bottomGroup control and sets the default layout data.
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @return the newly-created composite
	 */
	private Group createGroup(Composite parent, int span, int numColumns, String text) {

		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(text);
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return group;
	}
	
	protected Control createContents(Composite parent) {
		Composite composite_tab = createComposite(parent, 1, 1);
		
		Group ErrorReportingDialogsGroup = createGroup(composite_tab, 1, 1, Messages.RemoteDevPreferencePage_ErrorReportingGroupLabel);
		Composite comp= new Composite(ErrorReportingDialogsGroup, SWT.NONE);
		indexerErrorLimit = createIndexErrorLimit(comp);
		
		GridData data = (GridData)indexerErrorLimit.getTextControl( comp ).getLayoutData();
		data.horizontalAlignment = GridData.BEGINNING;
		data.widthHint = convertWidthInCharsToPixels( 6 );
		
		initializeValues();

		return composite_tab;
	}
	
	private IntegerFieldEditor createIndexErrorLimit(Composite group) {
		IntegerFieldEditor result= new IntegerFieldEditor(PreferenceConstants.INDEXER_ERRORS_DISPLAY_LIMIT, Messages.RemoteDevPreferencePage_IndexErrorLimitLabel, group, 4);
		result.setValidRange(1, 1000);
		result.setPropertyChangeListener(validityChangeListener);
		return result;
	}
	
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		indexerErrorLimit.setStringValue(String.valueOf(store.getInt(PreferenceConstants.INDEXER_ERRORS_DISPLAY_LIMIT)));
	}


	protected void performDefaults() {
		super.performDefaults();
		initalizeDefaults();
	}
	
	private void initalizeDefaults() {
		IPreferenceStore store = getPreferenceStore();
		indexerErrorLimit.setStringValue(String.valueOf(store.getInt(PreferenceConstants.INDEXER_ERRORS_DISPLAY_LIMIT)));
	}
	
	public boolean performOk() {
		storeValues();
		return true;
	}

	private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(PreferenceConstants.INDEXER_ERRORS_DISPLAY_LIMIT, indexerErrorLimit.getIntValue());
	}
	
	private void updateValidState() {
    	if (!indexerErrorLimit.isValid()) {
    		setErrorMessage(indexerErrorLimit.getErrorMessage());
    		setValid(false);
		} else {
			setErrorMessage(null);
    		setValid(true);    		
    	}
    }
	
}
