/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cldt.debug.internal.ui.propertypages; 

import java.text.MessageFormat;

import org.eclipse.cldt.debug.core.model.ICSignal;
import org.eclipse.cldt.debug.internal.ui.dialogfields.SelectionButtonDialogField;
import org.eclipse.cldt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
 
/**
 * The property page for a signal.
 */
public class SignalPropertyPage extends PropertyPage {

	private SelectionButtonDialogField fPassButton;
	private SelectionButtonDialogField fStopButton;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents( Composite parent ) {
		noDefaultAndApplyButton();
		Composite composite = new Composite( parent, SWT.NONE );
		Font font = parent.getFont();
		composite.setFont( font );
		composite.setLayout( new GridLayout() );
		composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

		// Create description field
		try {
			String description = getSignal().getDescription();
			Label label = new Label( composite, SWT.WRAP );
			label.setText( MessageFormat.format( PropertyPageMessages.getString( "SignalPropertyPage.0" ), new String[] { description } ) ); //$NON-NLS-1$
			GridData data = new GridData( GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER );
			data.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
			label.setLayoutData( data );
			label.setFont( font );
		}
		catch( DebugException e1 ) {
		}

		// Create pass button
		try {
			boolean pass = getSignal().isPassEnabled();
			fPassButton = new SelectionButtonDialogField( SWT.CHECK );
			fPassButton.setLabelText( PropertyPageMessages.getString( "SignalPropertyPage.1" ) ); //$NON-NLS-1$
			fPassButton.setSelection( pass );
			fPassButton.setEnabled( getSignal().canModify() );
			fPassButton.doFillIntoGrid( composite, 1 );
		}
		catch( DebugException e ) {
		}

		// Create stop button
		try {
			boolean stop = getSignal().isStopEnabled();
			fStopButton = new SelectionButtonDialogField( SWT.CHECK );
			fStopButton.setLabelText( PropertyPageMessages.getString( "SignalPropertyPage.2" ) ); //$NON-NLS-1$
			fStopButton.setSelection( stop );
			fStopButton.setEnabled( getSignal().canModify() );
			fStopButton.doFillIntoGrid( composite, 1 );
		}
		catch( DebugException e ) {
		}

		setValid( true );
		return composite;
	}

	protected SelectionButtonDialogField getPassButton() {
		return fPassButton;
	}

	protected SelectionButtonDialogField getStopButton() {
		return fStopButton;
	}

	public ICSignal getSignal() {
		return (ICSignal)getElement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean result = super.performOk();
		if ( result ) {
			try {
				setSignalProperties();
			}
			catch( DebugException e ) {
				DebugUIPlugin.errorDialog( getShell(), PropertyPageMessages.getString( "SignalPropertyPage.3" ), PropertyPageMessages.getString( "SignalPropertyPage.4" ), e.getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return result;
	}

	private void setSignalProperties() throws DebugException {
		final MultiStatus ms = new MultiStatus( CDebugUIPlugin.getUniqueIdentifier(),
												DebugException.REQUEST_FAILED,
												PropertyPageMessages.getString( "SignalPropertyPage.5" ), //$NON-NLS-1$
												null );
		BusyIndicator.showWhile( Display.getCurrent(), 
							new Runnable() {
								public void run() {
									if ( !getSignal().canModify() )
										return;
									if ( getPassButton() != null ) { 
										try {
											getSignal().setPassEnabled( getPassButton().isSelected() );
										}
										catch( DebugException e ) {
											ms.merge( e.getStatus() );
										}
									}
									if ( getStopButton() != null ) { 
										try {
											getSignal().setStopEnabled( getStopButton().isSelected() );
										}
										catch( DebugException e ) {
											ms.merge( e.getStatus() );
										}
									}
								}
							} );
		if ( !ms.isOK() ) {
			throw new DebugException( ms );
		}
	}
}
