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
package org.eclipse.ptp.internal.debug.ui.views.variable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.internal.debug.ui.PixelConverter;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Clement chu
 */
public class PVariableCheckboxTableViewer extends TableViewer implements ICheckable {
	private ListenerList checkStateListeners = new ListenerList();
	private ICheckProvider checkProvider = null;
	
	// String constants
	protected static final String YES_VALUE = Messages.PVariableCheckboxTableViewer_0;
	protected static final String NO_VALUE = Messages.PVariableCheckboxTableViewer_1;

	// Column properties
	private static final String CP_NAME = "name"; //$NON-NLS-1$
	private static final String CP_JOB = "job"; //$NON-NLS-1$

	// Column labels
	private static final String CL_NAME = Messages.PVariableCheckboxTableViewer_2;
	private static final String CL_JOB = Messages.PVariableCheckboxTableViewer_3;

	/**
	 * Constructor for PExpressionViewer
	 * 
	 * @param parent
	 * @param style
	 */
	public PVariableCheckboxTableViewer(Composite parent, int style) {
		super(parent, SWT.CHECK | style);
		Table table = getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the table columns
		new TableColumn(table, SWT.NULL);
		new TableColumn(table, SWT.NULL);
		new TableColumn(table, SWT.NULL);
		TableColumn[] columns = table.getColumns();
		columns[0].setResizable(false);
		columns[1].setResizable(true);
		columns[2].setResizable(true);

		columns[0].setText(""); //$NON-NLS-1$
		columns[1].setText(CL_NAME);
		columns[2].setText(CL_JOB);

		columns[0].addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				((PVariableViewerSorter)getSorter()).setColumn(0);
				refresh();
			}
		});
		columns[1].addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				((PVariableViewerSorter)getSorter()).setColumn(1);
				refresh();
			}
		});
		columns[2].addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				((PVariableViewerSorter)getSorter()).setColumn(2);
				refresh();
			}
		});

		PixelConverter pc = new PixelConverter(parent);
		columns[0].setWidth(pc.convertWidthInCharsToPixels(3));
		columns[1].setWidth(pc.convertWidthInCharsToPixels(20));
		columns[2].setWidth(pc.convertWidthInCharsToPixels(20));

		setColumnProperties(new String[]{ "", CP_NAME, CP_JOB }); //$NON-NLS-1$
	}
	
    public void addCheckStateListener(ICheckStateListener listener) {
        checkStateListeners.add(listener);
    }
    public void removeCheckStateListener(ICheckStateListener listener) {
        checkStateListeners.remove(listener);
    }
    
    public void setCheckProvider(ICheckProvider checkProvider) {
    	this.checkProvider = checkProvider;
    }
    
    private void fireCheckStateChanged(final CheckStateChangedEvent event) {
        Object[] array = checkStateListeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            final ICheckStateListener l = (ICheckStateListener) array[i];
            SafeRunnable.run(new SafeRunnable() {
                public void run() {
                    l.checkStateChanged(event);
                }
            });
        }
    }
    public void handleSelect(SelectionEvent event) {
        if (event.detail == SWT.CHECK) {
            super.handleSelect(event); // this will change the current selection

            TableItem item = (TableItem) event.item;
            Object data = item.getData();
            if (data != null) {
                fireCheckStateChanged(new CheckStateChangedEvent(this, data, item.getChecked()));
            }
        } else {
			super.handleSelect(event);
		}
    }
    public boolean getChecked(Object element) {
        Widget widget = findItem(element);
        if (widget instanceof TableItem) {
            return ((TableItem) widget).getChecked();
        }
        return false;
    }
    public boolean setChecked(Object element, boolean state) {
        Assert.isNotNull(element);
        Widget widget = findItem(element);
        if (widget instanceof TableItem) {
            ((TableItem) widget).setChecked(state);
            return true;
        }
        return false;
    }	
	protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
		super.doUpdateItem(widget, element, fullMap);
		if (widget instanceof TableItem) {
			if (checkProvider != null) {
				((TableItem)widget).setChecked(checkProvider.isCheck(element));
			}
		}
	}
}
