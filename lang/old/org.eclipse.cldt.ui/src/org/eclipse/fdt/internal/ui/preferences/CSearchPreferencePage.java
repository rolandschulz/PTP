/**********************************************************************
Copyright (c) 2002, 2004 IBM Rational Software and others.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
 IBM Rational Software - Initial Contribution
**********************************************************************/

package org.eclipse.fdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.fdt.core.FortranCorePlugin;
import org.eclipse.fdt.internal.core.search.indexing.SourceIndexer;
import org.eclipse.fdt.internal.ui.search.CSearchPage;
import org.eclipse.fdt.ui.FortranUIPlugin;
import org.eclipse.fdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CSearchPreferencePage extends PreferencePage
		implements
			IWorkbenchPreferencePage {
	
	private Combo fExternLinks;
	private Button fExternEnabled;
	
	protected OverlayPreferenceStore fOverlayStore;
	private Text fTextControl;
	
	private static final String TIMEOUT_VALUE = "20000"; //$NON-NLS-1$
	
	public CSearchPreferencePage(){
		setPreferenceStore(FortranUIPlugin.getDefault().getPreferenceStore());
		fOverlayStore  = createOverlayStore();
	}

	private OverlayPreferenceStore createOverlayStore() {
		ArrayList overlayKeys = new ArrayList();		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CSearchPage.EXTERNALMATCH_ENABLED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, CSearchPage.EXTERNALMATCH_VISIBLE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, SourceIndexer.FDT_INDEXER_TIMEOUT));
	
        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();
		
		initializeDialogUnits(parent);
		
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= 0;
		layout.verticalSpacing= convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		result.setLayout(layout);
		
		Group indexerTimeoutGroup= new Group(result, SWT.NONE);
		indexerTimeoutGroup.setLayout(new GridLayout());
		indexerTimeoutGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		indexerTimeoutGroup.setText(PreferencesMessages.getString("CSearchPreferences.IndexerTimeout.IndexerTimeoutGroup")); //$NON-NLS-1$
		
		fTextControl = (Text) addTextField( indexerTimeoutGroup, PreferencesMessages.getString("CSearchPreferences.IndexerTimeout.Timeout"),"TimeOut",6,0,true); //$NON-NLS-1$ //$NON-NLS-2$
	
		initialize(); 
		
		return result;
	
	}
	
	private void initialize(){
		fTextControl.setText(fOverlayStore.getString(SourceIndexer.FDT_INDEXER_TIMEOUT));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Combo createComboBox( Composite parent, String label, String[] items, String selection )
	{
		ControlFactory.createLabel( parent, label );
		Combo combo = ControlFactory.createSelectCombo( parent, items, selection );
		combo.setLayoutData( new GridData() );
		return combo;
	}
	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Button createCheckButton( Composite parent, String label )
	{
		Button button = new Button( parent, SWT.CHECK | SWT.LEFT );
		button.setText( label );
		// FieldEditor GridData
		GridData data = new GridData();
		button.setLayoutData( data );
		return button;
	}
	
	private Control addTextField(Composite composite, String label, String key, int textLimit, int indentation, boolean isNumber) {

		Label labelControl = new Label(composite, SWT.NONE);
		labelControl.setText(label);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indentation;
		labelControl.setLayoutData(gd);

		Text textControl = new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);

		return textControl;
	}
	
	/*
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		
		String timeOut = fTextControl.getText();
		try {
			// Check the string number
			Integer.parseInt(timeOut);
		} catch (NumberFormatException ex){
			timeOut = TIMEOUT_VALUE;
		}
		
		fOverlayStore.setValue(SourceIndexer.FDT_INDEXER_TIMEOUT, timeOut);
		fOverlayStore.propagate();
		
//		Store IProblem Marker value in FortranCorePlugin Preferences 
		Preferences prefs = FortranCorePlugin.getDefault().getPluginPreferences();
		
		prefs.setValue(SourceIndexer.FDT_INDEXER_TIMEOUT,timeOut);
		FortranCorePlugin.getDefault().savePluginPreferences();
		
		return true;
	}
	
	/**
	 * @param store
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(SourceIndexer.FDT_INDEXER_TIMEOUT,TIMEOUT_VALUE);
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fOverlayStore.loadDefaults();
		initialize();
		super.performDefaults();
	}

}
