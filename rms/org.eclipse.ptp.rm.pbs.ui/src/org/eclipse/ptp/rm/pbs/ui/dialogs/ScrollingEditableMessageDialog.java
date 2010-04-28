/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation; 2010 University of Illinois. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * Contributors: IBM Corporation - initial API and implementation Contributors:
 * Albert L. Rossi - implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Displays text contents for editing/saving.
 * Note: the font used is fixed to be Courier, which should be available
 * on Mac, Windows and Linux platforms.
 * 
 * @author arossi
 */
public class ScrollingEditableMessageDialog extends MessageDialog
{
	public static final int			DEFAULT_INDEX	= 0;
	public static final String[]	DEFAULT_LABELS	=
																{ "Close" };

	protected Button					okButton;
	protected Text						scrollable;
	// INPUT
	protected String					title;
	protected String					value;

	public ScrollingEditableMessageDialog( Shell parentShell,
														String name,
														String value )
	{
		this( parentShell,
				name,
				value,
				name,
				null,
				MessageDialog.NONE,
				DEFAULT_LABELS,
				DEFAULT_INDEX );
	}

	public ScrollingEditableMessageDialog( Shell parentShell,
														String name,
														String value,
														String dialogTitle,
														Image dialogTitleImage,
														int dialogImageType,
														String[] dialogButtonLabels,
														int defaultIndex )
	{
		super(	parentShell,
					dialogTitle,
					dialogTitleImage,
					name,
					dialogImageType,
					dialogButtonLabels,
					defaultIndex );
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public int open()
	{
		createDialogArea( getParentShell() );
		return super.open();
	}

	@Override
	protected void buttonPressed( int buttonId )
	{
		if ( buttonId == IDialogConstants.OK_ID ) {
			value = scrollable.getText();
		} else {
			value = null;
		}
		super.buttonPressed( buttonId );
	}

	@Override
	protected void configureShell( Shell shell )
	{
		super.configureShell( shell );
		if ( title != null ) {
			shell.setText( title );
		}
	}

	@Override
	protected void createButtonsForButtonBar( Composite parent )
	{
		// create OK and Cancel buttons by default
		okButton = createButton(	parent,
											IDialogConstants.OK_ID,
											IDialogConstants.OK_LABEL,
											true );
		createButton(	parent,
							IDialogConstants.CANCEL_ID,
							IDialogConstants.CANCEL_LABEL,
							false );
		// do this here because setting the text will set enablement on the ok
		// button
		scrollable.setFocus();
		if ( value != null ) {
			scrollable.setText( value );
			scrollable.selectAll();
		}
	}

	@Override
	protected Control createDialogArea( Composite parent )
	{
		Control c = super.createDialogArea( parent );
		GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL );
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = 550;
		data.widthHint = convertWidthInCharsToPixels( 160 );

		scrollable = new Text( parent, SWT.BORDER | SWT.MULTI | SWT.WRAP
				| SWT.H_SCROLL | SWT.V_SCROLL );
		scrollable.setLayoutData( data );
		scrollable.setEditable( true );
		Display d = Display.getCurrent();
		// three fonts for Mac, Linux, Windows ...
		FontData[][] f =
		{ d.getFontList( "Courier", true ), d.getFontList( "Courier", false ),
				d.getFontList( "Courier", true ),
				d.getFontList( "Courier", false ),
				d.getFontList( "Courier", true ), d.getFontList( "Courier", false ) };
		int i = 0;
		for ( ; i < f.length; i++ ) {
			if ( f[i].length > 0 ) {
				scrollable.setFont( new Font( d, f[i] ) );
				break;
			}
		}
		if ( i == f.length ) applyDialogFont( scrollable );
		return c;
	}
}
