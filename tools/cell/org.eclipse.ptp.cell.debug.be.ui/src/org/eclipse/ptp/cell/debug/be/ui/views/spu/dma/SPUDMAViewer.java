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
 * SPU DMA Queue viewer.
 *
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class SPUDMAViewer extends TableViewer {

	// Column properties
	private static final String CP_DMA_OPCODE = "dma_opcode"; //$NON-NLS-1$
	private static final String CP_DMA_TAG = "dma_tag"; //$NON-NLS-1$
	private static final String CP_DMA_TID = "dma_tid"; //$NON-NLS-1$
	private static final String CP_DMA_RID = "dma_rid"; //$NON-NLS-1$
	private static final String CP_DMA_EA = "dma_ea"; //$NON-NLS-1$
	private static final String CP_DMA_LSA = "dma_lsa"; //$NON-NLS-1$
	private static final String CP_DMA_SIZE = "dma_size"; //$NON-NLS-1$
	private static final String CP_DMA_LSTADDR = "dma_lstaddr"; //$NON-NLS-1$
	private static final String CP_DMA_LSTSIZE = "dma_lstsize"; //$NON-NLS-1$
	private static final String CP_DMA_ERROR_P = "dma_error_p"; //$NON-NLS-1$

	// Column labels
	private static final String CL_DMA_OPCODE = SPUDMAMessages.getString("SPUDMAViewer.0"); //$NON-NLS-1$
	private static final String CL_DMA_TAG = SPUDMAMessages.getString("SPUDMAViewer.1"); //$NON-NLS-1$
	private static final String CL_DMA_TID = SPUDMAMessages.getString("SPUDMAViewer.2"); //$NON-NLS-1$
	private static final String CL_DMA_RID = SPUDMAMessages.getString("SPUDMAViewer.3"); //$NON-NLS-1$
	private static final String CL_DMA_EA = SPUDMAMessages.getString("SPUDMAViewer.4"); //$NON-NLS-1$
	private static final String CL_DMA_LSA = SPUDMAMessages.getString("SPUDMAViewer.5"); //$NON-NLS-1$
	private static final String CL_DMA_SIZE = SPUDMAMessages.getString("SPUDMAViewer.6"); //$NON-NLS-1$
	private static final String CL_DMA_LSTADDR = SPUDMAMessages.getString("SPUDMAViewer.7"); //$NON-NLS-1$
	private static final String CL_DMA_LSTSIZE = SPUDMAMessages.getString("SPUDMAViewer.8"); //$NON-NLS-1$
	private static final String CL_DMA_ERROR_P = SPUDMAMessages.getString("SPUDMAViewer.9"); //$NON-NLS-1$

	/**
	 * Constructor for SPUMailboxViewer
	 * 
	 * @param parent
	 * @param style
	 */
	public SPUDMAViewer( Composite parent, int style ) {
		super( parent, style );
		Table table = getTable();
		table.setLinesVisible( true );
		table.setHeaderVisible( true );		
		table.setLayoutData( new GridData( GridData.FILL_BOTH ) );

		// Create the table columns
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		TableColumn[] columns = table.getColumns();
		columns[0].setResizable( true );
		columns[1].setResizable( false );
		columns[2].setResizable( false );
		columns[3].setResizable( false );
		columns[4].setResizable( false );
		columns[5].setResizable( false );
		columns[6].setResizable( false );
		columns[7].setResizable( false );
		columns[8].setResizable( false );
		columns[9].setResizable( false );

		columns[0].setText( CL_DMA_OPCODE );
		columns[1].setText( CL_DMA_TAG );
		columns[2].setText( CL_DMA_TID );
		columns[3].setText( CL_DMA_RID );
		columns[4].setText( CL_DMA_EA );
		columns[5].setText( CL_DMA_LSA );
		columns[6].setText( CL_DMA_SIZE );
		columns[7].setText( CL_DMA_LSTADDR );
		columns[8].setText( CL_DMA_LSTSIZE );
		columns[9].setText( CL_DMA_ERROR_P );

		PixelConverter pc = new PixelConverter( parent );
		columns[0].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[1].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[2].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[3].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[4].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[5].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[6].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[7].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[8].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[9].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		
		setColumnProperties( new String[]{ CP_DMA_OPCODE, CP_DMA_TAG, CP_DMA_TID, CP_DMA_RID, CP_DMA_EA, CP_DMA_LSA, CP_DMA_SIZE, CP_DMA_LSTADDR, CP_DMA_LSTSIZE, CP_DMA_ERROR_P } );
	}
}
