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
package org.eclipse.ptp.debug.internal.ui.views.array;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.ptp.debug.internal.ui.views.PTabItem;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Clement chu
 *
 */
public class ArrayTabItem extends PTabItem {
	private final int MAX_CHECKED = 2;
	private final int TABLE_COL_INDEX = 0;
	private final int TABLE_ROW_INDEX = 1;
	private SashForm sashForm = null;
	private Table colTable = null;
	private Table rowTable = null;
	private ScrolledComposite tableSC = null;
	private ScrolledComposite comboSC = null;
	private List selectedButtons = new ArrayList(2);
	private Combo[] comboBoxes = new Combo[0];
	private IVariable variable = null;

	public ArrayTabItem(ArrayView view, String tabText) {
		super(view.getTabFolder(), tabText);
		setControl();
	}
	
	public void init(final IVariable variable) {
		this.variable = variable;
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					List varList = new ArrayList();
					setVariables(varList, variable.getValue());
					setupComboBoxes((Integer[])varList.toArray(new Integer[varList.size()]));
				} catch (DebugException e) {
					displayError(e);
				}
			}
		});
	}
	
	public void setVariables(List varList, IValue value) throws DebugException {		
		if (value.hasVariables()) {
			IVariable[] vars = value.getVariables();
			varList.add(new Integer(vars.length));
			setVariables(varList, vars[0].getValue());
		}
	}
	
	protected void dispose() {
		clearContext();
		variable = null;
		comboBoxes = new Combo[0];
	}
	protected void clearContext() {
		disposeTable(true);
		selectedButtons.clear();
	}

	public void displayTab() {
		if (displayError == false)		
			fPageBook.showPage(sashForm);
	}
	
	public void createTabPage(Composite parent) {
		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayout(new FillLayout(SWT.VERTICAL));
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		createComboBoxes(sashForm);
		createTableComposite(sashForm);
		sashForm.setWeights(new int[] { 2,5 });
	}
	public void createComboBoxes(Composite parent) {
		comboSC = new ScrolledComposite(parent, SWT.V_SCROLL);
		Composite comboComp = new Composite(comboSC, SWT.NONE);
		comboComp.setLayout(new GridLayout(2, false));
		comboComp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		comboSC.setContent(comboComp);
		comboSC.setExpandVertical(true);
	    comboSC.setExpandHorizontal(true);
	}
	protected void createTableComposite(Composite parent) {
		tableSC = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite tableComp = new Composite(tableSC, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		tableComp.setLayout(layout);
		tableComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableSC.setContent(tableComp);
		tableSC.setExpandVertical(true);
	    tableSC.setExpandHorizontal(true);
	}
	protected void createRowTable(Composite parent) {
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(1));
		rowTable = new Table(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.NONE);
		rowTable.setLayout(tableLayout);
		rowTable.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		rowTable.setHeaderVisible(true);
		
		TableColumn rowColumn = new TableColumn(rowTable, SWT.RIGHT);
		rowColumn.setMoveable(false);
		rowColumn.setResizable(false);

		rowTable.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		rowTable.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		colTable.setSelection(rowTable.getSelectionIndex());
	    	}
	    });
	}
	protected void createColumnTable(Composite parent) {
		colTable = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);
		colTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		colTable.setHeaderVisible(true);
		colTable.setLinesVisible(true);		
	}
	public void disposeTable(boolean resetAll) {
		if (colTable != null && !colTable.isDisposed()) {
			colTable.removeAll();
			if (resetAll) {
				colTable.dispose();
				colTable = null;
			}
		}
		if (rowTable != null && !rowTable.isDisposed()) {
			if (resetAll) {
				rowTable.removeAll();
				rowTable.dispose();
				rowTable = null;
			}
		}
	}
	public void createTable(int row, int col) {
		createRowTable((Composite)tableSC.getContent());
		createColumnTable((Composite)tableSC.getContent());
		fillRowHeaders(row);
		fillColumnHeaders(col);
	}
	
	public void updateTable(final boolean resetAll) {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				disposeTable(resetAll);
				if (!selectedButtons.isEmpty()) {
					int rowIndex = getRowIndex();
					int colIndex = getColumnIndex();
					int row = rowIndex==-1?1:comboBoxes[rowIndex].getItemCount();
					int col = colIndex==-1?1:comboBoxes[colIndex].getItemCount();
					if (resetAll)
						createTable(row, col);

					try {
						for (int i=0; i<row; i++) {
							TableItem item = new TableItem(colTable, SWT.NONE);
							for (int j=0; j<col; j++) {
								item.setText(j, getValueString(rowIndex, i, colIndex, j));
							}
						}
					} catch (DebugException e) {
						displayError(e);
					}
				}
				if (resetAll)
					tableSC.setMinSize(tableSC.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});
	}
	public int getRowIndex() {
		return getIndex(TABLE_ROW_INDEX);
	}
	public int getColumnIndex() {
		return getIndex(TABLE_COL_INDEX);
	}
	public int getIndex(int index) {
		if (selectedButtons.size() > index) {
			Button button = (Button)selectedButtons.get(index);
			return ((Integer)button.getData()).intValue();
		}
		return -1;
	}
	
	public String getValueString(int rowIndex, int row, int colIndex, int col) throws DebugException {
		IVariable var = variable;
		for (int i=0; i<comboBoxes.length; i++) {
			IVariable[] vars = var.getValue().getVariables();
			if (vars.length == 0)
				throw new DebugException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No Value found", null));
			
			if (i == rowIndex)
				var = vars[row];
			else if (i == colIndex)
				var = vars[col];
			else {
				var = vars[comboBoxes[i].getSelectionIndex()];
			}
		}
		return var.getValue().getValueString();
	}
	public void fillRowHeaders(int row) {
		TableItem item = null;
		for (int i=0; i<row; i++) {
			item = new TableItem(rowTable, SWT.NONE);
			item.setText(""+i);
		}
		rowTable.getColumn(0).setWidth(getTextSize(rowTable, ""+row).x - 3);
	}
	public void fillColumnHeaders(int col) {
		TableColumn[] columns = new TableColumn[col];
		TableColumn column = null;
		for (int i=0; i<columns.length; i++) {
			column = new TableColumn(colTable, SWT.CENTER);
			column.setText(""+i);
			column.setWidth(30);
		}
	}
	
	public void setupComboBoxes(Integer[] vars) {
		selectedButtons.clear();
		Composite comboComp = (Composite)comboSC.getContent();
	    Button button = null;
	    comboBoxes = new Combo[vars.length];
	    for (int i=0; i<vars.length; i++) {
	    	button = new Button(comboComp, SWT.CHECK);
	    	int total = vars[i].intValue();
	    	button.setText((i+1) + ":");
	    	button.setData(new Integer(i));
	    	button.addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		Object obj = e.getSource();
		    		if (obj instanceof Button) {
		    			checkSelected((Button)obj);
		    		}
		    	}
		    });
	    	comboBoxes[i] = new Combo(comboComp, SWT.READ_ONLY);
	    	comboBoxes[i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		    for (int j=0; j<total; j++) {
		    	comboBoxes[i].add(""+j);
		    }
		    comboBoxes[i].addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		updateTable(false);
		    	}
		    });
		    comboBoxes[i].select(0);
	    }
	    comboSC.setMinSize(comboComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	protected void checkSelected(Button button) {
		comboBoxes[((Integer)button.getData()).intValue()].setEnabled(!button.getSelection());
		if (selectedButtons.contains(button)) {
			selectedButtons.remove(button);
		} 
		else {
			if (MAX_CHECKED == selectedButtons.size()) {
				Button selectedButton = (Button)selectedButtons.remove(MAX_CHECKED - 1);
				selectedButton.setSelection(false);
				comboBoxes[((Integer)selectedButton.getData()).intValue()].setEnabled(true);
			}
			selectedButtons.add(button);
		}
		updateTable(true);
	}
}
