// Copyright (c) 2013 IBM Corporation and others. All rights reserved. 
// This program and the accompanying materials are made available under the 
// terms of the Eclipse Public License v1.0s which accompanies this distribution, 
// and is available at http://www.eclipse.org/legal/epl-v10.html

package org.eclipse.ptp.rm.ibm.lsf.ui.widgets;

import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.ptp.rm.ibm.lsf.ui.model.TableContentsProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class LSFQueryDialog extends Dialog {

	private class TableSelectionListener implements ISelectionChangedListener {

		@Override
		/**
		 * Handle notification event when user selects a table row. In this case
		 * save the name of the selected application.
		 * 
		 * @param e: Notification event
		 */
		public void selectionChanged(SelectionChangedEvent arg0) {
			Object obj;

			obj = ((IStructuredSelection) arg0.getSelection())
					.getFirstElement();
			selectedValue = ((String[]) obj)[0];
		}
	}

	protected String columnLabels[];
	protected Vector<String[]> commandResponse;
	private String dialogTitle;
	protected Shell parentShell;
	private Table queryTable;
	private String selectedValue;

	private TableSelectionListener tableSelectionListener;

	LSFQueryDialog(Shell parent, String title, String labels[],
			Vector<String[]> response) {
		super(parent);
		parentShell = parent;
		dialogTitle = title;
		columnLabels = labels;
		commandResponse = response;
	}

	/**
	 * Handle configuration of the shell. In this case, set the dialog title
	 * 
	 * @param shell
	 *            : The dialog shell
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(dialogTitle);
	}

	/**
	 * Create the dialog area of the dialog. This area is a table containing the
	 * output of a 'bapp -w' command
	 * 
	 * @param parent
	 *            : The parent for the dialog control area
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		TableViewer viewer;
		TableContentsProvider contentsProvider;
		Composite composite;
		TableViewerColumn viewColumns[];
		TableColumnLayout tableLayout;
		int n;

		composite = (Composite) super.createDialogArea(parent);
		tableLayout = new TableColumnLayout();
		composite.setLayout(tableLayout);
		viewer = new TableViewer(composite, SWT.SINGLE | SWT.V_SCROLL
				| SWT.H_SCROLL);
		queryTable = viewer.getTable();
		queryTable.clearAll();
		queryTable.setHeaderVisible(true);
		queryTable.setLinesVisible(true);
		/*
		 * Create columns corresponding to command output. The number of columns
		 * created is determined by the number of blank-delimited strings in the
		 * columnLabels array, which assumes the heading line of the command
		 * output has a single blank delimited string for each column heading
		 */
		viewColumns = new TableViewerColumn[columnLabels.length];
		for (int i = 0; i < viewColumns.length; i++) {
			TableColumn tableColumn;

			viewColumns[i] = new TableViewerColumn(viewer, SWT.NONE);
			tableColumn = viewColumns[i].getColumn();
			tableLayout.setColumnData(tableColumn, new ColumnWeightData(1, 75,
					true));
			tableColumn.setText(columnLabels[i]);
			tableColumn.setResizable(true);
			tableColumn.setMoveable(true);
		}
		tableSelectionListener = new TableSelectionListener();
		viewer.addSelectionChangedListener(tableSelectionListener);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		contentsProvider = new TableContentsProvider();
		viewer.setLabelProvider(contentsProvider);
		viewer.setInput(commandResponse);
		n = queryTable.getItemCount();
		for (int i = 0; i < n; i++) {
			TableItem rowData;

			rowData = queryTable.getItem(i);
			if (rowData.getText(0).equals(selectedValue)) {
				queryTable.select(i);
				break;
			}
		}
		return composite;
	}

	/**
	 * Return the value selected from the table
	 * 
	 * @return Selected value
	 */
	public String getSelectedValue() {
		return selectedValue;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	/**
	 * Set an initial selection value for this widget
	 * 
	 * @param value
	 *            Initial selection value
	 */
	public void setSelectedValue(String value) {
		selectedValue = value;
	}
}
