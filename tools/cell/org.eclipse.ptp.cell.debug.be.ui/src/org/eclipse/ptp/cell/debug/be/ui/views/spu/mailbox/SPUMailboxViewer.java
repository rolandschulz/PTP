/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */

package org.eclipse.ptp.cell.debug.be.ui.views.spu.mailbox;

import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * SPU Mailbox viewer.
 *
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class SPUMailboxViewer extends TableViewer {

	// Column properties
	private static final String CP_MAILBOX_NAME = "mbox_name"; //$NON-NLS-1$
	private static final String CP_MAILBOX_INDEX = "mbox_index"; //$NON-NLS-1$
	private static final String CP_MAILBOX_VALUE = "mbox_value"; //$NON-NLS-1$

	// Column labels
	private static final String CL_MAILBOX_NAME = SPUMailboxMessages.getString( "SignalsViewer.4" ); //$NON-NLS-1$
	private static final String CL_MAILBOX_INDEX = SPUMailboxMessages.getString( "SignalsViewer.5" ); //$NON-NLS-1$
	private static final String CL_MAILBOX_VALUE = SPUMailboxMessages.getString( "SignalsViewer.6" ); //$NON-NLS-1$

	/**
	 * Constructor for SPUMailboxViewer
	 * 
	 * @param parent
	 * @param style
	 */
	public SPUMailboxViewer( Composite parent, int style ) {
		super( parent, style );
		Table table = getTable();
		table.setLinesVisible( true );
		table.setHeaderVisible( true );		
		table.setLayoutData( new GridData( GridData.FILL_BOTH ) );

		// Create the table columns
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		TableColumn[] columns = table.getColumns();
		columns[0].setResizable( true );
		columns[1].setResizable( false );
		columns[2].setResizable( false );

		columns[0].setText( CL_MAILBOX_NAME );
		columns[1].setText( CL_MAILBOX_INDEX );
		columns[2].setText( CL_MAILBOX_VALUE );


		PixelConverter pc = new PixelConverter( parent );
		columns[0].setWidth( pc.convertWidthInCharsToPixels( 20 ) );
		columns[1].setWidth( pc.convertWidthInCharsToPixels( 10 ) );
		columns[2].setWidth( pc.convertWidthInCharsToPixels( 30 ) );
		
		setColumnProperties( new String[]{ CP_MAILBOX_NAME, CP_MAILBOX_INDEX, CP_MAILBOX_VALUE } );
	}
}
