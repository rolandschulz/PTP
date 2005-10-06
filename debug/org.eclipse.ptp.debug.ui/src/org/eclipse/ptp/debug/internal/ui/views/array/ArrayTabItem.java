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

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.debug.internal.ui.dialogs.RangeDialog;
import org.eclipse.ptp.debug.internal.ui.views.PTabFolder;
import org.eclipse.ptp.debug.internal.ui.views.PTabItem;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

/**
 * @author Clement chu
 *
 */
public class ArrayTabItem extends PTabItem {
	private final int COL_TYPE = 0;
	private final int ROW_TYPE = 1;
	private SashForm sashForm = null;
	private Table colTable = null;
	private Table rowTable = null;
	private ScrolledComposite tableSC = null;
	private ScrolledComposite comboSC = null;
	private Combo[] comboBoxes = new Combo[0];
	private IVariable variable = null;
	private int startRow = 0;
	private int startCol = 0;
	private int endRow = 0;
	private int endCol = 0;
	private Button selectedRowButton = null;
	private Button selectedColButton = null;

	public ArrayTabItem(PTabFolder view, String tabText) {
		super(view, tabText);
		setControl();
	}
	
	public void init(final IVariable variable) {
		this.variable = variable;
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor pmonitor) throws InvocationTargetException {
				if (pmonitor == null)
					pmonitor = new NullProgressMonitor();
				
				pmonitor.beginTask(MessageFormat.format("{0}", new String[]{ ArrayMessages.getString("ArrayTabItem.initVariable")}), 10);
				try {
					List varList = new ArrayList();
					if (!setVariables(varList, variable.getValue(), pmonitor))
						throw new InvocationTargetException(new Exception(ArrayMessages.getString("ArrayTabItem.cancel")));
					
					pmonitor.worked(3);
					setupComboBoxes((Integer[])varList.toArray(new Integer[varList.size()]), pmonitor);
				} catch (DebugException e) {
					throw new InvocationTargetException(e);
				} finally {
					pmonitor.done();
				}
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(this, runnable, null);
		} catch(InterruptedException e) {
			displayError(e);
		} catch (InvocationTargetException e1) {
			displayError(e1);
		}
	}
	
	public boolean setVariables(List varList, IValue value, IProgressMonitor monitor) throws DebugException {
		if (monitor.isCanceled())
			return false;
		
		if (value.hasVariables()) {
			IVariable[] vars = value.getVariables();
			varList.add(new Integer(vars.length));
			return setVariables(varList, vars[0].getValue(), monitor);
		}
		return true;
	}
	
	protected void dispose() {
		clearContext();
		variable = null;
		comboBoxes = new Combo[0];
	}
	protected void clearContext() {
		disposeTable();
		selectedRowButton = null;
		selectedColButton = null;
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
		//sashForm.setWeights(new int[] { 2,7 });
	}
	public void createComboBoxes(Composite parent) {
		comboSC = new ScrolledComposite(parent, SWT.V_SCROLL);
		Composite comboComp = new Composite(comboSC, SWT.NONE);
		comboComp.setLayout(new GridLayout(3, false));
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
	protected void createRowTable(Composite parent, boolean showColHeader) {
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(1));
		rowTable = new Table(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.NONE);
		rowTable.setLayout(tableLayout);
		rowTable.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		rowTable.setHeaderVisible(showColHeader);
		rowTable.setEnabled(false);
		rowTable.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		TableColumn rowColumn = new TableColumn(rowTable, SWT.RIGHT);
		rowColumn.setMoveable(false);
		rowColumn.setResizable(false);
	}
	protected void createColumnTable(Composite parent, boolean showColHeader) {
		colTable = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);
		colTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		colTable.setHeaderVisible(showColHeader);
		colTable.setLinesVisible(true);
		colTable.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.MouseDoubleClick)
					resetRangeAction();
			}
		});
		colTable.setMenu(createPopupMenu());
	}
	protected Menu createPopupMenu() {
		Menu menu = new Menu(colTable);
		MenuItem mItem = new MenuItem(menu, SWT.PUSH);
		mItem.setText(ArrayMessages.getString("ArrayTabItem.resetRange"));
		mItem.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		resetRangeAction();
	    	}
	    });
		return menu;
	}
	
	public boolean isTableDisposed() {
		return ((colTable == null || colTable.isDisposed()) && (rowTable == null || rowTable.isDisposed()));
	}
	
	public void disposeTable() {
		if (colTable != null && !colTable.isDisposed()) {
			colTable.removeAll();
			colTable.dispose();
			colTable = null;
		}
		if (rowTable != null && !rowTable.isDisposed()) {
			rowTable.removeAll();
			rowTable.dispose();
			rowTable = null;
		}
		if (tableSC != null && !tableSC.isDisposed())
			tableSC.setMinSize(tableSC.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	public void createTable(int sRow, int eRow, int sCol, int eCol, boolean showColHeader, boolean showRowHeader, IProgressMonitor monitor) {
		if (showRowHeader) {
			createRowTable((Composite)tableSC.getContent(), showColHeader);//no row header if there is no column header
			fillRowHeaders(sRow, eRow, monitor);
		}
		createColumnTable((Composite)tableSC.getContent(), showColHeader);
		if (showColHeader)
			fillColumnHeaders(sCol, eCol, monitor);
		tableSC.setMinSize(tableSC.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	public void updateTable() {
		updateTable(getIndex(selectedColButton), getIndex(selectedRowButton));
	}
	public void updateTable(int colIndex, int rowIndex) {
		if (colIndex == -1 && rowIndex == -1) {
			PTPDebugUIPlugin.errorDialog(fPageBook.getShell(), ArrayMessages.getString("ArrayTabItem.noIndexErrTitle"), ArrayMessages.getString("ArrayTabItem.noIndexErrMsg"), new Exception());
			return;
		}
		updateTableContent(colIndex, rowIndex);
	}
	private void updateTableContent(final int colIndex, final int rowIndex) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				if (monitor == null)
					monitor = new NullProgressMonitor();
				
				if (isTableDisposed())
					createTable(startRow, endRow, startCol, endCol, colIndex>-1, rowIndex>-1, monitor);
				
				colTable.removeAll();				
				monitor.beginTask(MessageFormat.format("{0}", new String[]{ ArrayMessages.getString("ArrayTabItem.updateTable")}), (endRow - startRow) * (endCol - startCol));
				try {
					for (int i=startRow; i<endRow; i++) {
						TableItem item = new TableItem(colTable, SWT.NONE);
						int colPos = 0;
						for (int j=startCol; j<endCol; j++) {
							if (monitor.isCanceled()) {
								throw new InterruptedException(ArrayMessages.getString("ArrayTabItem.cancel"));
							}
							try {
								item.setText(colPos++, getValueString(rowIndex, i, colIndex, j));
								monitor.worked(1);
							} catch (DebugException e) {
								throw new InvocationTargetException(e);
							}
						}
					}
				} finally {
					monitor.subTask(ArrayMessages.getString("ArrayTabItem.done"));
					monitor.done();
					tableSC.setFocus();
				}
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(this, runnable, null);
		} catch(InterruptedException e) {
			disposeTable();
			displayError(e);
		} catch (InvocationTargetException e1) {
			disposeTable();
			displayError(e1);
		}
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
			else
				var = vars[comboBoxes[i].getSelectionIndex()];
		}
		return var.getValue().getValueString();
	}
	public void fillRowHeaders(int sRow, int eRow, IProgressMonitor monitor) {
		IProgressMonitor subMonitor = monitor;
		subMonitor.beginTask(ArrayMessages.getString("ArrayTabItem.initRow"), (eRow - sRow));
		TableItem item = null;
		for (int i=sRow; i<eRow; i++) {
			item = new TableItem(rowTable, SWT.NONE);
			item.setText(""+i);
			subMonitor.worked(1);
		}
		rowTable.getColumn(0).setWidth(getTextSize(rowTable, ""+eRow).x - 2);
		subMonitor.done();
	}
	public void fillColumnHeaders(int sCol, int eCol, IProgressMonitor monitor) {
		IProgressMonitor subMonitor = monitor;
		subMonitor.beginTask(ArrayMessages.getString("ArrayTabItem.initCol"), (eCol - sCol));
		TableColumn column = null;
		for (int i=sCol; i<eCol; i++) {
			column = new TableColumn(colTable, SWT.CENTER);
			column.setText(""+i);
			column.setWidth(30);
			column.setMoveable(false);
			column.setResizable(true);
			subMonitor.worked(1);
		}
		subMonitor.done();
	}
	
	public void setupComboBoxes(Integer[] vars, IProgressMonitor monitor) {
		IProgressMonitor subMonitor = monitor;
		subMonitor.beginTask(ArrayMessages.getString("ArrayTabItem.initCombobox"), vars.length);
		
		Composite comboComp = (Composite)comboSC.getContent();
		new Label(comboComp, SWT.READ_ONLY).setText(ArrayMessages.getString("ArrayTabItem.colHead"));
		new Label(comboComp, SWT.READ_ONLY).setText(ArrayMessages.getString("ArrayTabItem.rowHead"));
		Button showTableButton = new Button(comboComp, SWT.ARROW | SWT.RIGHT);
		showTableButton.setToolTipText(ArrayMessages.getString("ArrayTabItem.showTable"));
		showTableButton.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		updateTable();
	    	}
	    });

		Button rowButton = null;
		Button colButton = null;
	    comboBoxes = new Combo[vars.length];
	    for (int i=0; i<vars.length; i++) {
	    	int total = vars[i].intValue();
	    	colButton = new Button(comboComp, SWT.CHECK);
	    	colButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
	    	colButton.setData(new Integer(i));
	    	colButton.addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		setColButton((Button)e.getSource());
		    	}
		    });
	    	rowButton = new Button(comboComp, SWT.CHECK);
	    	rowButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
	    	rowButton.setData(new Integer(i));
	    	rowButton.addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		setRowButton((Button)e.getSource());
		    	}
		    });
	    	
	    	comboBoxes[i] = new Combo(comboComp, SWT.READ_ONLY);
	    	comboBoxes[i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

	    	subMonitor.subTask(ArrayMessages.getString("ArrayTabItem.creatingCombobox"));
			SubProgressMonitor subTotalMonitor = new SubProgressMonitor(subMonitor, total);
		    for (int j=0; j<total; j++) {
		    	comboBoxes[i].add(""+j);
		    	subTotalMonitor.worked(1);
		    }
		    subTotalMonitor.done();
		    comboBoxes[i].select(0);
		    comboBoxes[i].addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		updateTable();
		    	}
		    });
		    subMonitor.worked(1);
	    }
	    subMonitor.done();
	    Point pt = comboComp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    comboSC.setMinSize(pt);
	    sashForm.setWeights(new int [] { (pt.x + 30), view.getTabFolder().getClientArea().width });
	}
	private void setEnableComboBox(Button button, boolean isEnable) {
		int index = getIndex(button);
		if (index > -1) 
			comboBoxes[index].setEnabled(isEnable);
	}
	private void unCheckedButton(int type) {
		checkedButton(type, null);
		switch(type) {
		case COL_TYPE:
			startCol = 0;
			endCol = 1;
			break;
		case ROW_TYPE:
			startRow = 0;
			endRow = 1;
			break;
		}
	}
	private void checkedButton(int type, Button button) {
		switch(type) {
		case COL_TYPE:
			if (selectedColButton != null) {
				selectedColButton.setSelection(false);
				setEnableComboBox(selectedColButton, true);
			}
			selectedColButton = button;
			break;
		case ROW_TYPE:
			if (selectedRowButton != null) {
				selectedRowButton.setSelection(false);
				setEnableComboBox(selectedRowButton, true);
			}
			selectedRowButton = button;
			break;
		}
		setEnableComboBox(button, false);
	}
	private boolean openRangeDialog(int colIndex, int rowIndex, int type) {
		int totalCol = (colIndex>-1)?comboBoxes[colIndex].getItemCount():1;
		int totalRow = (rowIndex>-1)?comboBoxes[rowIndex].getItemCount():1;
		
		int tmpStartCol = startCol;
		int tmpEndCol = endCol;
		int tmpStartRow = startRow;
		int tmpEndRow = endRow;
		switch(type) {
		case COL_TYPE:
			tmpStartCol = 0;
			tmpEndCol = totalCol;
			break;
		case ROW_TYPE:
			tmpStartRow = 0;
			tmpEndRow = totalRow;
			break;
		}
		RangeDialog rangeDialog = new RangeDialog(tableSC.getShell(), totalCol, totalRow);
		if (rangeDialog.open(colIndex>-1, rowIndex>-1, tmpStartCol, tmpEndCol,  tmpStartRow, tmpEndRow) == Window.OK) {
			startCol = rangeDialog.getFromCol();
			endCol = rangeDialog.getToCol();
			startRow = rangeDialog.getFromRow();
			endRow = rangeDialog.getToRow();
			return true;
		}
		return false;
	}
	private void setRowButton(Button button) {
		boolean checked = button.getSelection();
		int colIndex = getIndex(selectedColButton);
		int rowIndex = getIndex(button);
		if (!checked) {
			unCheckedButton(ROW_TYPE);
			disposeTable();
			return;
		}
		button.setSelection(!checked);
		if (openRangeDialog(colIndex, rowIndex, ROW_TYPE)) {
			if (colIndex == rowIndex)
				unCheckedButton(COL_TYPE);
			checkedButton(ROW_TYPE, button);
			button.setSelection(checked);
			disposeTable();
		}
	}
	private void setColButton(Button button) {
		boolean checked = button.getSelection();
		int colIndex = getIndex(button);
		int rowIndex = getIndex(selectedRowButton);
		if (!checked) {
			unCheckedButton(COL_TYPE);
			disposeTable();
			return;
		}
		button.setSelection(!checked);
		if (openRangeDialog(colIndex, rowIndex, COL_TYPE)) {
			if (colIndex == rowIndex)
				unCheckedButton(ROW_TYPE);
			checkedButton(COL_TYPE, button);
			button.setSelection(checked);
			disposeTable();
		}
	}	
	private int getIndex(Button button) {
		if (button == null)
			return -1;
		return ((Integer)button.getData()).intValue();
	}
	protected void resetRangeAction() {
		int colIndex = getIndex(selectedColButton);
		int rowIndex = getIndex(selectedRowButton);
		if (openRangeDialog(colIndex, rowIndex, -1)) {
			disposeTable();
			updateTable(colIndex, rowIndex);
		}
	}
}
