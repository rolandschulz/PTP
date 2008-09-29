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
package org.eclipse.ptp.cell.debug.be.ui.views.spu.event;

import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * SPU event viewer.
 *
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class SPUEventViewer extends TableViewer {

	// Column properties
	private static final String CP_EVENT_NAME = "event_name"; //$NON-NLS-1$
	private static final String CP_EVENT_VALUE = "event_value"; //$NON-NLS-1$
	

	// Column labels
	private static final String CL_EVENT_NAME = SPUEventMessages.getString("SPUEventViewer.0"); //$NON-NLS-1$
	private static final String CL_EVENT_VALUE = SPUEventMessages.getString("SPUEventViewer.1"); //$NON-NLS-1$
	
	/**
	 * Constructor for SPUEventViewer
	 * 
	 * @param parent
	 * @param style
	 */
	public SPUEventViewer( Composite parent, int style ) {
		super( parent, style );
		Table table = getTable();
		table.setLinesVisible( true );
		table.setHeaderVisible( true );		
		table.setLayoutData( new GridData( GridData.FILL_BOTH ) );

		// Create the table columns
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		TableColumn[] columns = table.getColumns();
		columns[0].setResizable( false );
		columns[1].setResizable( false );

		columns[0].setText( CL_EVENT_NAME );
		columns[1].setText( CL_EVENT_VALUE );

		PixelConverter pc = new PixelConverter( parent );
		columns[0].setWidth( pc.convertWidthInCharsToPixels( 30 ) );
		columns[1].setWidth( pc.convertWidthInCharsToPixels( 30 ) );


		setColumnProperties( new String[]{ CP_EVENT_NAME, CP_EVENT_VALUE } );
	}
}
