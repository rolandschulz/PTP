/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cldt.debug.internal.ui.preferences;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cldt.debug.core.CDebugCorePlugin;
import org.eclipse.cldt.debug.core.ICDebugConstants;
import org.eclipse.cldt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cldt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cldt.debug.internal.ui.PixelConverter;
import org.eclipse.cldt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cldt.debug.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cldt.debug.internal.ui.dialogfields.IListAdapter;
import org.eclipse.cldt.debug.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cldt.debug.internal.ui.dialogfields.SelectionButtonDialogField;
import org.eclipse.cldt.debug.internal.ui.wizards.AddSourceLocationWizard;
import org.eclipse.cldt.debug.ui.CDebugUIPlugin;
import org.eclipse.cldt.debug.ui.sourcelookup.SourceListDialogField;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The "Source Code Locations" preference page.
 */
public class SourcePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, Observer {

	private SourceListDialogField fSourceListField;

	private SelectionButtonDialogField fSearchForDuplicateFiles;

	private boolean fChanged = false;

	public SourcePreferencePage() {
		super();
		setPreferenceStore( CDebugUIPlugin.getDefault().getPreferenceStore() );
		setDescription( PreferenceMessages.getString( "SourcePreferencePage.0" ) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents( Composite parent ) {
		WorkbenchHelp.setHelp( getControl(), ICDebugHelpContextIds.SOURCE_PREFERENCE_PAGE );
		fSourceListField = createSourceListField();
		fSearchForDuplicateFiles = createSearchForDuplicateFilesButton();
		Composite control = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		control.setLayout( layout );
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		control.setLayoutData( data );
		control.setFont( JFaceResources.getDialogFont() );
		PixelConverter converter = new PixelConverter( control );
		fSourceListField.doFillIntoGrid( control, 3 );
		LayoutUtil.setHorizontalSpan( fSourceListField.getLabelControl( null ), 2 );
		LayoutUtil.setWidthHint( fSourceListField.getLabelControl( null ), converter.convertWidthInCharsToPixels( 40 ) );
		LayoutUtil.setHorizontalGrabbing( fSourceListField.getListControl( null ) );
		fSearchForDuplicateFiles.doFillIntoGrid( control, 3 );
		setValues();
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init( IWorkbench workbench ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update( Observable o, Object arg ) {
		setChanged( true );
	}

	private SourceListDialogField createSourceListField() {
		SourceListDialogField field = new SourceListDialogField( PreferenceMessages.getString( "SourcePreferencePage.1" ), //$NON-NLS-1$
				new IListAdapter() {

					public void customButtonPressed( DialogField f, int index ) {
						sourceButtonPressed( index );
					}

					public void selectionChanged( DialogField f ) {
					}
				} );
		field.addObserver( this );
		return field;
	}

	private SelectionButtonDialogField createSearchForDuplicateFilesButton() {
		SelectionButtonDialogField button = new SelectionButtonDialogField( SWT.CHECK );
		button.setLabelText( PreferenceMessages.getString( "SourcePreferencePage.2" ) ); //$NON-NLS-1$
		button.setDialogFieldListener( new IDialogFieldListener() {

			public void dialogFieldChanged( DialogField field ) {
				setChanged( true );
			}
		} );
		return button;
	}

	protected void sourceButtonPressed( int index ) {
		switch( index ) {
			case 0: // Add...
				if ( addSourceLocation() )
					setChanged( true );
				break;
			case 2: // Up
			case 3: // Down
			case 5: // Remove
				setChanged( true );
				break;
		}
	}

	protected boolean isChanged() {
		return fChanged;
	}

	protected void setChanged( boolean changed ) {
		fChanged = changed;
	}

	private boolean addSourceLocation() {
		AddSourceLocationWizard wizard = new AddSourceLocationWizard( getSourceLocations() );
		WizardDialog dialog = new WizardDialog( getShell(), wizard );
		if ( dialog.open() == Window.OK ) {
			fSourceListField.addElement( wizard.getSourceLocation() );
			return true;
		}
		return false;
	}

	public ICSourceLocation[] getSourceLocations() {
		return (fSourceListField != null) ? fSourceListField.getSourceLocations() : new ICSourceLocation[0];
	}

	public void setSourceLocations( ICSourceLocation[] locations ) {
		if ( fSourceListField != null )
			fSourceListField.setElements( Arrays.asList( locations ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		setSourceLocations( new ICSourceLocation[0] );
		setSearchForDuplicateFiles( false );
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		storeValues();
		CDebugCorePlugin.getDefault().savePluginPreferences();
		return true;
	}

	private boolean searchForDuplicateFiles() {
		return (fSearchForDuplicateFiles != null) ? fSearchForDuplicateFiles.isSelected() : false;
	}

	private void setSearchForDuplicateFiles( boolean search ) {
		if ( fSearchForDuplicateFiles != null )
			fSearchForDuplicateFiles.setSelection( search );
	}

	private void setValues() {
		setSourceLocations( CDebugCorePlugin.getDefault().getCommonSourceLocations() );
		setSearchForDuplicateFiles( CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_SEARCH_DUPLICATE_FILES ) );
	}

	private void storeValues() {
		CDebugCorePlugin.getDefault().saveCommonSourceLocations( getSourceLocations() );
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_SEARCH_DUPLICATE_FILES, searchForDuplicateFiles() );
	}
}
