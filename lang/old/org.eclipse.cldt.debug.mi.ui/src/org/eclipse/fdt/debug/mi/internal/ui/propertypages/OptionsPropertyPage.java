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
package org.eclipse.fdt.debug.mi.internal.ui.propertypages; 

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.fdt.debug.core.cdi.CDIException;
import org.eclipse.fdt.debug.core.cdi.ICDISession;
import org.eclipse.fdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.fdt.debug.mi.core.cdi.Session;
import org.eclipse.fdt.utils.ui.controls.ControlFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
 
/**
 * The property page for the gdb/mi-based debugger options.
 */
public class OptionsPropertyPage extends PropertyPage {

	private Button fRefreshRegistersButton;

	private Button fRefreshSolibsButton;

	/** 
	 * Constructor for OptionsPropertyPage. 
	 */
	public OptionsPropertyPage() {
		noDefaultAndApplyButton();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents( Composite parent ) {
		Composite comp = ControlFactory.createComposite( parent, 1 );
		fRefreshRegistersButton = createCheckButton( comp, PropertyMessages.getString( "OptionsPropertyPage.0" ) ); //$NON-NLS-1$
		fRefreshSolibsButton = createCheckButton( comp, PropertyMessages.getString( "OptionsPropertyPage.1" ) ); //$NON-NLS-1$
		initialize();
		return comp;
	}

	private Button createCheckButton( Composite parent, String label ) {
		Button button = new Button( parent, SWT.CHECK | SWT.LEFT );
		button.setText( label );
		// FieldEditor GridData
		GridData data = new GridData();
		button.setLayoutData( data );
		return button;
	}

	private void initialize() {
		boolean regUpdate = true;
		boolean solibUpdate = true;
		ICDISession session = (ICDISession)getElement().getAdapter( ICDISession.class );
		if ( session instanceof Session ) {
			regUpdate = ((Session)session).getRegisterManager().isAutoUpdate();
			solibUpdate = ((Session)session).getSharedLibraryManager().isAutoUpdate();
		}
		fRefreshRegistersButton.setSelection( regUpdate );
		fRefreshSolibsButton.setSelection( solibUpdate );
		
	}

	protected void performApply() {
		storeValues();
		super.performApply();
	}

	public boolean performOk() {
		storeValues();
		return super.performOk();
	}

	private void storeValues() {
		ICDISession session = (ICDISession)getElement().getAdapter( ICDISession.class );
		final ICDITarget target = (ICDITarget)getElement().getAdapter( ICDITarget.class );
		if ( session instanceof Session ) {
			final boolean regUpdate = fRefreshRegistersButton.getSelection();
			final boolean solibUpdate = fRefreshSolibsButton.getSelection();
			final Session miSession = ((Session)session);
			miSession.getRegisterManager().setAutoUpdate( regUpdate );
			miSession.getSharedLibraryManager().setAutoUpdate( solibUpdate );
			if ( target.isSuspended() && (regUpdate || solibUpdate) ) {
				DebugPlugin.getDefault().asyncExec( new Runnable() {
					
					public void run() {
						if ( target.isSuspended() ) {
							if ( regUpdate ) {
								try {
									miSession.getRegisterManager().update();
								}
								catch( CDIException e ) {
								}
							}
							if ( solibUpdate ) {
								try {
									miSession.getSharedLibraryManager().update();
								}
								catch( CDIException e ) {
								}
							}
						}
					}
				} );
			}
		}		
	}
}
