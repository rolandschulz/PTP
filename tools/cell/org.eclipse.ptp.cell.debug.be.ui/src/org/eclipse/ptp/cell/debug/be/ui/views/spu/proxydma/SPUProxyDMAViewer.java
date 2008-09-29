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

package org.eclipse.ptp.cell.debug.be.ui.views.spu.proxydma;

import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * SPU Proxy DMA Queue viewer.
 *
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class SPUProxyDMAViewer extends TableViewer {

	// Column properties
	private static final String CP_PROXYDMA_OPCODE = "proxydma_opcode"; //$NON-NLS-1$
	private static final String CP_PROXYDMA_TAG = "proxydma_tag"; //$NON-NLS-1$
	private static final String CP_PROXYDMA_TID = "proxydma_tid"; //$NON-NLS-1$
	private static final String CP_PROXYDMA_RID = "proxydma_rid"; //$NON-NLS-1$
	private static final String CP_PROXYDMA_EA = "proxydma_ea"; //$NON-NLS-1$
	private static final String CP_PROXYDMA_LSA = "proxydma_lsa"; //$NON-NLS-1$
	private static final String CP_PROXYDMA_SIZE = "proxydma_size"; //$NON-NLS-1$
	private static final String CP_PROXYDMA_LSTADDR = "proxydma_lstaddr"; //$NON-NLS-1$
	private static final String CP_PROXYDMA_LSTSIZE = "proxydma_lstsize"; //$NON-NLS-1$
	private static final String CP_PROXYDMA_ERROR_P = "proxydma_error_p"; //$NON-NLS-1$

	// Column labels
	private static final String CL_PROXYDMA_OPCODE = SPUProxyDMAMessages.getString("SPUProxyDMAViewer.0"); //$NON-NLS-1$
	private static final String CL_PROXYDMA_TAG = SPUProxyDMAMessages.getString("SPUProxyDMAViewer.1"); //$NON-NLS-1$
	private static final String CL_PROXYDMA_TID = SPUProxyDMAMessages.getString("SPUProxyDMAViewer.2"); //$NON-NLS-1$
	private static final String CL_PROXYDMA_RID = SPUProxyDMAMessages.getString("SPUProxyDMAViewer.3"); //$NON-NLS-1$
	private static final String CL_PROXYDMA_EA = SPUProxyDMAMessages.getString("SPUProxyDMAViewer.4"); //$NON-NLS-1$
	private static final String CL_PROXYDMA_LSA = SPUProxyDMAMessages.getString("SPUProxyDMAViewer.5"); //$NON-NLS-1$
	private static final String CL_PROXYDMA_SIZE = SPUProxyDMAMessages.getString("SPUProxyDMAViewer.6"); //$NON-NLS-1$
	private static final String CL_PROXYDMA_LSTADDR = SPUProxyDMAMessages.getString("SPUProxyDMAViewer.7"); //$NON-NLS-1$
	private static final String CL_PROXYDMA_LSTSIZE = SPUProxyDMAMessages.getString("SPUProxyDMAViewer.8"); //$NON-NLS-1$
	private static final String CL_PROXYDMA_ERROR_P = SPUProxyDMAMessages.getString("SPUProxyDMAViewer.9"); //$NON-NLS-1$

	/**
	 * Constructor for SPUMailboxViewer
	 * 
	 * @param parent
	 * @param style
	 */
	public SPUProxyDMAViewer( Composite parent, int style ) {
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

		columns[0].setText( CL_PROXYDMA_OPCODE );
		columns[1].setText( CL_PROXYDMA_TAG );
		columns[2].setText( CL_PROXYDMA_TID );
		columns[3].setText( CL_PROXYDMA_RID );
		columns[4].setText( CL_PROXYDMA_EA );
		columns[5].setText( CL_PROXYDMA_LSA );
		columns[6].setText( CL_PROXYDMA_SIZE );
		columns[7].setText( CL_PROXYDMA_LSTADDR );
		columns[8].setText( CL_PROXYDMA_LSTSIZE );
		columns[9].setText( CL_PROXYDMA_ERROR_P );

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
		
		setColumnProperties( new String[]{ CP_PROXYDMA_OPCODE, CP_PROXYDMA_TAG, CP_PROXYDMA_TID, CP_PROXYDMA_RID, CP_PROXYDMA_EA, CP_PROXYDMA_LSA, CP_PROXYDMA_SIZE, CP_PROXYDMA_LSTADDR, CP_PROXYDMA_LSTSIZE, CP_PROXYDMA_ERROR_P } );
	}
}
