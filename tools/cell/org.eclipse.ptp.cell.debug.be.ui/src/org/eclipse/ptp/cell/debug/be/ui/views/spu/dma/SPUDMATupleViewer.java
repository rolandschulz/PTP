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

package org.eclipse.ptp.cell.debug.be.ui.views.spu.dma;

import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * SPU DMA Status viewer.
 *
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class SPUDMATupleViewer extends TableViewer {

	// Column properties
	private static final String CP_DMA_NAME = "dma_name"; //$NON-NLS-1$
	private static final String CP_DMA_VALUE = "dma_value"; //$NON-NLS-1$

	// Column labels
	private static final String CL_DMA_NAME = SPUDMAMessages.getString( "SignalsViewer.4" ); //$NON-NLS-1$
	private static final String CL_DMA_VALUE = SPUDMAMessages.getString( "SignalsViewer.6" ); //$NON-NLS-1$

	/**
	 * Constructor for SPUMailboxViewer
	 * 
	 * @param parent
	 * @param style
	 */
	public SPUDMATupleViewer( Composite parent, int style ) {
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
		columns[1].setResizable( false );

		columns[0].setText( CL_DMA_NAME );
		columns[1].setText( CL_DMA_VALUE );

		PixelConverter pc = new PixelConverter( parent );
		columns[0].setWidth( pc.convertWidthInCharsToPixels( 45 ) );
		columns[1].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		
		setColumnProperties( new String[]{ CP_DMA_NAME, CP_DMA_VALUE } );
	}
}
