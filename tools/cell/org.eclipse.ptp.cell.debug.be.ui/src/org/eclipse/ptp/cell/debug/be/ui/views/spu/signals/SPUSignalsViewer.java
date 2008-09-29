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

package org.eclipse.ptp.cell.debug.be.ui.views.spu.signals;

import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * SPU Signals viewer.
 *
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class SPUSignalsViewer extends TableViewer {

	// Column properties
	private static final String CP_SIGNAL_NAME = "signal_name"; //$NON-NLS-1$
	private static final String CP_SIGNAL_VALUE = "signal_value"; //$NON-NLS-1$

	// Column labels
	private static final String CL_SIGNAL_NAME = SPUSignalsMessages.getString( "SignalsViewer.4" ); //$NON-NLS-1$
	private static final String CL_SIGNAL_VALUE = SPUSignalsMessages.getString( "SignalsViewer.5" ); //$NON-NLS-1$

	/**
	 * Constructor for SPUSignalsViewer
	 * 
	 * @param parent
	 * @param style
	 */
	public SPUSignalsViewer( Composite parent, int style ) {
		super( parent, style );
		Table table = getTable();
		table.setLinesVisible( true );
		table.setHeaderVisible( true );		
		table.setLayoutData( new GridData( GridData.FILL_BOTH ) );

		// Create the table columns
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		TableColumn[] columns = table.getColumns();
		columns[0].setResizable( true );
		columns[1].setResizable( true );

		columns[0].setText( CL_SIGNAL_NAME );
		columns[1].setText( CL_SIGNAL_VALUE );

		PixelConverter pc = new PixelConverter( parent );
		columns[0].setWidth( pc.convertWidthInCharsToPixels( 30 ) );
		columns[1].setWidth( pc.convertWidthInCharsToPixels( 30 ) );

		setColumnProperties( new String[]{ CP_SIGNAL_NAME, CP_SIGNAL_VALUE } );
	}
}
