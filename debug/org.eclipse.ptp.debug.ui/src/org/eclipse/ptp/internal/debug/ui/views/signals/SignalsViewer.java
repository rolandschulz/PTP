/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.ui.views.signals;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.internal.debug.ui.PixelConverter;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author Clement chu
 */
public class SignalsViewer extends TableViewer {

	// String constants
	protected static final String YES_VALUE = Messages.SignalsViewer_0;
	protected static final String NO_VALUE = Messages.SignalsViewer_1;

	// Column properties
	private static final String CP_NAME = "name"; //$NON-NLS-1$
	private static final String CP_PASS = "pass"; //$NON-NLS-1$
	private static final String CP_SUSPEND = "suspend"; //$NON-NLS-1$
	private static final String CP_DESCRIPTION = "description"; //$NON-NLS-1$

	// Column labels
	private static final String CL_NAME = Messages.SignalsViewer_2;
	private static final String CL_PASS = Messages.SignalsViewer_3;
	private static final String CL_SUSPEND = Messages.SignalsViewer_4;
	private static final String CL_DESCRIPTION = Messages.SignalsViewer_5;

	/**
	 * Constructor for SignalsViewer
	 * 
	 * @param parent
	 * @param style
	 */
	public SignalsViewer(Composite parent, int style) {
		super(parent, style);
		Table table = getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);		
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the table columns
		new TableColumn(table, SWT.NULL);
		new TableColumn(table, SWT.NULL);
		new TableColumn(table, SWT.NULL);
		new TableColumn(table, SWT.NULL);
		TableColumn[] columns = table.getColumns();
		columns[0].setResizable(true);
		columns[1].setResizable(false);
		columns[2].setResizable(false);
		columns[3].setResizable(true);

		columns[0].setText(CL_NAME);
		columns[1].setText(CL_PASS);
		columns[2].setText(CL_SUSPEND);
		columns[3].setText(CL_DESCRIPTION);

		PixelConverter pc = new PixelConverter(parent);
		columns[0].setWidth(pc.convertWidthInCharsToPixels(20));
		columns[1].setWidth(pc.convertWidthInCharsToPixels(15));
		columns[2].setWidth(pc.convertWidthInCharsToPixels(15));
		columns[3].setWidth(pc.convertWidthInCharsToPixels(50));

		setColumnProperties(new String[]{ CP_NAME, CP_PASS, CP_SUSPEND, CP_DESCRIPTION });
	}
}


