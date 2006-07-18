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
package org.eclipse.ptp.debug.internal.ui.views.variable;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.debug.internal.ui.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author Clement chu
 */
public class PVariableViewer extends TableViewer {

	// String constants
	protected static final String YES_VALUE = PVariableMessages.getString("PExpressionViewer.yes");
	protected static final String NO_VALUE = PVariableMessages.getString("PExpressionViewer.no");

	// Column properties
	private static final String CP_NAME = "name";
	private static final String CP_JOB = "job";
	private static final String CP_SET = "set";

	// Column labels
	private static final String CL_NAME = PVariableMessages.getString("PExpressionViewer.name");
	private static final String CL_JOB = PVariableMessages.getString("PExpressionViewer.job");
	private static final String CL_SET = PVariableMessages.getString("PExpressionViewer.set");

	/**
	 * Constructor for PExpressionViewer
	 * 
	 * @param parent
	 * @param style
	 */
	public PVariableViewer(Composite parent, int style) {
		super(parent, style);
		Table table = getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);		
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the table columns
		new TableColumn(table, SWT.NULL);
		new TableColumn(table, SWT.NULL);
		new TableColumn(table, SWT.NULL);
		TableColumn[] columns = table.getColumns();
		columns[0].setResizable(true);
		columns[1].setResizable(true);
		columns[2].setResizable(true);

		columns[0].setText(CL_NAME);
		columns[1].setText(CL_JOB);
		columns[2].setText(CL_SET);

		columns[0].addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				getSorter();
				refresh();
			}
		});
		columns[1].addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				getSorter();
				refresh();
			}
		});
		columns[2].addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				getSorter();
				refresh();
			}
		});

		PixelConverter pc = new PixelConverter(parent);
		columns[0].setWidth(pc.convertWidthInCharsToPixels(20));
		columns[1].setWidth(pc.convertWidthInCharsToPixels(20));
		columns[2].setWidth(pc.convertWidthInCharsToPixels(20));

		setColumnProperties(new String[]{ CP_NAME, CP_JOB, CP_SET });
	}
}
