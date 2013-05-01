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
package org.eclipse.ptp.internal.debug.ui.views.array;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPVariable;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ptp.internal.debug.ui.views.CTable;
import org.eclipse.ptp.internal.debug.ui.views.ICTableCellSelectionListener;
import org.eclipse.ptp.internal.debug.ui.views.PTabFolder;
import org.eclipse.ptp.internal.debug.ui.views.PTabItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;

/**
 * @author Clement chu
 * 
 */
public class ArrayTabItem extends PTabItem {
	private final String COL_TYPE = "col"; //$NON-NLS-1$
	private final String ROW_TYPE = "row"; //$NON-NLS-1$
	private SashForm sashForm = null;
	private CTable cTable = null;
	private Composite leftContent = null;
	private ArrayTableModel arrayModel = null;
	private IPVariable variable = null;
	private boolean reloadVariable = false;
	String type;
	String name;

	public ArrayTabItem(PTabFolder view, String tabText, IPVariable variable) {
		super(view, tabText);
		this.variable = variable;
		reloadVariable = false;
		setControl();
		// view.getTabFolder().setMenu(createPopupMenu());
	}

	public void init(final IPVariable variable) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				if (monitor == null) {
					monitor = new NullProgressMonitor();
				}

				int dim = 0;
				monitor.beginTask(Messages.ArrayTabItem_0, 10);
				try {
					name = variable.getName();
					type = variable.getReferenceTypeName();

					IValue value = variable.getValue();
					while (value.hasVariables()) {
						IVariable[] vars = value.getVariables();
						for (IVariable var : vars) {
							value = var.getValue();
							break;
						}
						fillDimensionPanel(vars.length - 1);
						dim++;
						monitor.worked(3);
					}
				} catch (DebugException e) {
					throw new InvocationTargetException(e);
				} finally {
					arrayModel = new ArrayTableModel(dim);
					monitor.done();
				}
			}
		};
		try {
			run(false, false, runnable);
		} catch (InterruptedException e) {
			displayError(e);
		} catch (InvocationTargetException e1) {
			displayError(e1);
		}
	}

	@Override
	protected void dispose() {
		view.closeTab(getTabItem().getText());
		resetTable();
		variable = null;
		getTabItem().dispose();
	}

	@Override
	public void clearContext() {
		for (Control control : leftContent.getChildren()) {
			if (control instanceof Button) {
				((Button) control).setSelection(false);
			} else if (control instanceof Spinner) {
				((Spinner) control).setSelection(0);
			}
		}
		resetTable();
		cTable.redraw();
	}

	public void setReloadVariable(boolean reload) {
		this.reloadVariable = reload;
	}

	@Override
	public void displayTab() {
		if (!displayError) {
			fPageBook.showPage(sashForm);
		}
	}

	@Override
	public void createTabPage(Composite parent) {
		sashForm = new SashForm(parent, SWT.HORIZONTAL | SWT.BORDER);
		sashForm.setLayout(new FillLayout(SWT.HORIZONTAL));
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.SASH_WIDTH = 2;
		sashForm.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		leftPanel(sashForm);
		rightPanel(sashForm);
		// sashForm.setMaximizedControl(cTable);
		sashForm.setWeights(new int[] { 1, 3 });
	}

	protected Menu createPopupMenu() {
		Menu menu = new Menu(cTable);
		MenuItem mItem = new MenuItem(menu, SWT.PUSH);
		mItem.setText(Messages.ArrayTabItem_1);
		mItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		return menu;
	}

	public void updateTable(int colIndex, int rowIndex) {
		if (colIndex == -1 && rowIndex == -1) {
			PTPDebugUIPlugin.errorDialog(fPageBook.getShell(), Messages.ArrayTabItem_2, new Exception(Messages.ArrayTabItem_3));
			return;
		}
	}

	private GridLayout createGridLayout(int column, boolean balance, int marginWidth, int marginHeight) {
		GridLayout layout = new GridLayout(column, balance);
		layout.marginWidth = marginWidth;
		layout.marginHeight = marginHeight;
		return layout;
	}

	private GridData createHorizontaGridData(int hozAlign, int width) {
		GridData gd = new GridData(hozAlign, SWT.BEGINNING, (hozAlign == SWT.FILL), false);
		gd.widthHint = width;
		return gd;
	}

	private void leftPanel(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		leftContent = new Composite(sc, SWT.NONE);
		leftContent.setLayout(createGridLayout(3, false, 3, 3));
		leftContent.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		sc.setContent(leftContent);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);

		new Label(leftContent, SWT.READ_ONLY).setText("col"); //$NON-NLS-1$
		new Label(leftContent, SWT.READ_ONLY).setText("row"); //$NON-NLS-1$
		Label sliceLabel = new Label(leftContent, SWT.READ_ONLY);
		sliceLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false));
		sliceLabel.setText(Messages.ArrayTabItem_4);
		/*
		 * Composite composite = new Composite(content, SWT.NONE);
		 * composite.setLayout(createGridLayout(3, false, 0, 0));
		 * composite.setLayoutData(createHorizontaGridData(SWT.FILL, 150));
		 * Label minLabel = new Label(composite, SWT.READ_ONLY);
		 * minLabel.setText("Min");
		 * minLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false));
		 * new Label(composite, SWT.READ_ONLY).setText("");
		 * Label maxLabel = new Label(composite, SWT.READ_ONLY);
		 * maxLabel.setText("Max");
		 * maxLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false));
		 */
		sc.setMinSize(120, SWT.DEFAULT);
	}

	private void clearAllCheckboxes(String name) {
		for (Control control : leftContent.getChildren()) {
			if (control instanceof Button) {
				if (control.getToolTipText().equals(name)) {
					((Button) control).setSelection(false);
				}
			}
		}
	}

	private void reload() throws CoreException {
		IPStackFrame frame = view.getStackFrame();
		for (IVariable var : frame.getVariables()) {
			if (var instanceof IPVariable) {
				IPVariable pvar = (IPVariable) var;
				String pname = var.getName();
				String ptype = var.getReferenceTypeName();
				if (pname.equals(name) && ptype.equals(type)) {
					variable = pvar;
					name = pname;
					type = ptype;
					reloadVariable = false;
					break;
				}
			}
		}
		if (reloadVariable) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					Messages.ArrayTabItem_5, null));
		}
	}

	public void refreshTable() {
		if (reloadVariable) {
			try {
				reload();
			} catch (CoreException e) {
				PTPDebugUIPlugin.errorDialog(fPageBook.getShell(), Messages.ArrayTabItem_2, e.getStatus());
				return;
			}
		}
		if (arrayModel != null) {
			setDimensionValues();
			if (arrayModel.isAvailable()) {
				cTable.setModel(arrayModel);
				arrayModel.initial();
			} else {
				resetTable();
				cTable.redraw();
			}
		}
	}

	private void resetTable() {
		arrayModel.reset();
		cTable.setModel(null);
	}

	private void setDimensionValues() {
		int[][] values = new int[arrayModel.getDimension()][3];
		int fCol = 1;
		int fRow = 1;
		int pos = 0;
		arrayModel.setColumnCount(1);
		arrayModel.setRowCount(1);
		for (Control control : leftContent.getChildren()) {
			if (control instanceof Button) {
				if (control.getToolTipText().equals(COL_TYPE)) {
					values[pos][0] = ((Button) control).getSelection() ? 1 : 0;
					continue;
				}
				if (control.getToolTipText().equals(ROW_TYPE)) {
					values[pos][1] = ((Button) control).getSelection() ? 1 : 0;
					continue;
				}
			}
			if (control instanceof Spinner) {
				if (values[pos][0] == 1) {
					values[pos][2] = ((Spinner) control).getMaximum();
					fCol = ((Spinner) control).getSelection() + 1;
					arrayModel.setColumnCount(values[pos][2] + 1);
				} else if (values[pos][1] == 1) {
					values[pos][2] = ((Spinner) control).getMaximum();
					fRow = ((Spinner) control).getSelection() + 1;
					arrayModel.setRowCount(values[pos][2] + 1);
				} else {
					values[pos][2] = ((Spinner) control).getSelection();
				}
				pos++;
				continue;
			}
		}
		arrayModel.setSelection(fCol, fRow);
		arrayModel.setValues(values);
	}

	private void fillDimensionPanel(int size) {
		// Slider slider = new Slider(parent, SWT.HORIZONTAL);
		final Button colBtn = new Button(leftContent, SWT.CHECK);
		colBtn.setToolTipText(COL_TYPE);
		colBtn.setLayoutData(createHorizontaGridData(SWT.CENTER, 20));
		final Button rowBtn = new Button(leftContent, SWT.CHECK);
		rowBtn.setLayoutData(createHorizontaGridData(SWT.CENTER, 20));
		rowBtn.setToolTipText(ROW_TYPE);

		colBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = colBtn.getSelection();
				clearAllCheckboxes(COL_TYPE);
				colBtn.setSelection(selected);

				if (selected) {
					if (rowBtn.getSelection()) {
						arrayModel.reset();
						rowBtn.setSelection(false);
					}
				}
				refreshTable();
			}
		});
		rowBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = rowBtn.getSelection();
				clearAllCheckboxes(ROW_TYPE);
				rowBtn.setSelection(selected);

				if (selected) {
					if (colBtn.getSelection()) {
						arrayModel.reset();
						colBtn.setSelection(false);
					}
				}
				refreshTable();
			}
		});
		final Spinner spinner = new Spinner(leftContent, SWT.NONE | SWT.BORDER);
		spinner.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		spinner.setMinimum(0);
		spinner.setMaximum(size);
		spinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!colBtn.getSelection() && !rowBtn.getSelection()) {
					arrayModel.reset();
				}
				refreshTable();
			}
		});
		leftContent.pack(true);

		/*
		 * Composite composite = new Composite(parent, SWT.NONE);
		 * composite.setLayout(createGridLayout(3, false, 0, 0));
		 * composite.setLayoutData(createHorizontaGridData(SWT.FILL, 150));
		 * final Spinner minSpinner = new Spinner(composite, SWT.NONE | SWT.BORDER);
		 * minSpinner.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		 * new Label(composite, SWT.READ_ONLY).setText("/");
		 * final Spinner maxSpinner = new Spinner(composite, SWT.NONE | SWT.BORDER);
		 * maxSpinner.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		 * 
		 * minSpinner.addSelectionListener(new SelectionAdapter() {
		 * public void widgetSelected(SelectionEvent e) {
		 * int value = minSpinner.getSelection();
		 * if (value >= maxSpinner.getSelection()) {
		 * minSpinner.setSelection(maxSpinner.getSelection()-1);
		 * }
		 * }
		 * });
		 * maxSpinner.addSelectionListener(new SelectionAdapter() {
		 * public void widgetSelected(SelectionEvent e) {
		 * int value = maxSpinner.getSelection();
		 * if (value <= minSpinner.getSelection()) {
		 * maxSpinner.setSelection(minSpinner.getSelection()+1);
		 * }
		 * }
		 * });
		 * minSpinner.setMinimum(0);
		 * minSpinner.setMaximum(total/2);
		 * maxSpinner.setMinimum(total/2+1);
		 * maxSpinner.setMaximum(total);
		 * minSpinner.setSelection(0);
		 * maxSpinner.setSelection(total/2+1);
		 */
	}

	private void rightPanel(Composite parent) {
		cTable = new CTable(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		cTable.addCellSelectionListener(new ICTableCellSelectionListener() {
			public void cellSelected(int col, int row, int statemask) {
				if (leftContent != null) {
					boolean isCol = false;
					boolean isRow = false;
					for (Control control : leftContent.getChildren()) {
						if (control instanceof Button) {
							if (control.getToolTipText().equals(COL_TYPE)) {
								isCol = ((Button) control).getSelection();
								continue;
							}
							if (control.getToolTipText().equals(ROW_TYPE)) {
								isRow = ((Button) control).getSelection();
								continue;
							}
						}
						if (control instanceof Spinner) {
							if (isCol || isRow) {
								((Spinner) control).setSelection((isCol ? col : row) - 1);
								continue;
							}
						}
					}
				}
			}

			public void fixedCellSelected(int col, int row, int statemask) {
			}
		});
		// cTable.setRowSelectionMode(true);
		// cTable.setMultiSelectionMode(true);
	}

	private void showTable() {
		cTable.redraw();
		cTable.setSelection(arrayModel.getSelectedColumn(), arrayModel.getSelectedRow(), true);
	}

	class ArrayTableModel extends AbstractArrayTableModel {
		private int[][] values;
		private int fCol = 1;
		private int fRow = 1;
		private final int dimension;
		private Object[][] data = null;

		public ArrayTableModel(int dimension) {
			this.dimension = dimension;
		}

		public Object getContentAt(int col, int row) {
			if (col == getFixedColumnCount() - 1) {
				return "" + (row - 1); //$NON-NLS-1$
			}
			if (row == getFixedRowCount() - 1) {
				return "" + (col - 1); //$NON-NLS-1$
			}

			if (data[col][row] == null) {
				return ""; //$NON-NLS-1$
			}
			return data[col][row];
		}

		public void setRowCount(int rows) {
			this.rows = rows + getFixedRowCount();
		}

		public void setColumnCount(int cols) {
			this.cols = cols + getFixedColumnCount();
		}

		public void reset() {
			fCol = getFixedColumnCount();
			fRow = getFixedRowCount();
			this.values = null;
			this.data = null;
		}

		public void setValues(int[][] values) {
			this.values = values;
		}

		public int getDimension() {
			return dimension;
		}

		public boolean isAvailable() {
			if (values == null) {
				return false;
			}

			boolean isCol = false;
			boolean isRow = false;
			for (int i = 0; i < dimension; i++) {
				isCol = isCol ? true : (values[i][0] == 1);
				isRow = isRow ? true : (values[i][1] == 1);
			}
			if (dimension == 1) {
				return !(!isCol && !isRow);
			}
			return (isCol && isRow);
		}

		public void setSelection(int col, int row) {
			this.fCol = col;
			this.fRow = row;
		}

		public int getSelectedColumn() {
			return fCol;
		}

		public int getSelectedRow() {
			return fRow;
		}

		private String getValueString(int col, int row) throws DebugException {
			IVariable var = variable;
			for (int i = 0; i < dimension; i++) {
				IVariable[] vars = var.getValue().getVariables();
				if (vars.length == 0) {
					throw new DebugException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
							Messages.ArrayTabItem_6, null));
				}

				if (values[i][0] == 1) {
					var = vars[col - 1];
				} else if (values[i][1] == 1) {
					var = vars[row - 1];
				} else {
					var = vars[values[i][2]];
				}
			}
			return var.getValue().getValueString();
		}

		public void initial() {
			if (data != null) {
				showTable();
				return;
			}

			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					// WorkbenchJob uiJob = new WorkbenchJob("Retrieving values...") {
					// public IStatus runInUIThread(IProgressMonitor monitor) {
					if (monitor == null) {
						monitor = new NullProgressMonitor();
					}
					monitor.beginTask(Messages.ArrayTabItem_7, (cols * rows));
					data = new Object[cols][rows];
					try {
						for (int c = getFixedColumnCount(); c < cols; c++) {
							for (int r = getFixedRowCount(); r < rows; r++) {
								try {
									data[c][r] = getValueString(c, r);
									monitor.worked(1);
								} catch (DebugException ex) {
									throw new InvocationTargetException(ex);
									// return ex.getStatus();
								}
							}
						}
					} finally {
						monitor.done();
					}
					showTable();
					// return Status.OK_STATUS;
				}
			};
			/*
			 * PlatformUI.getWorkbench().getProgressService().showInDialog(fPageBook.getShell(), uiJob);
			 * uiJob.setSystem(false);
			 * uiJob.schedule();
			 */
			try {
				run(false, false, runnable);
			} catch (InterruptedException e) {
				displayError(e);
			} catch (InvocationTargetException e1) {
				displayError(e1);
			}
		}
	}
}
