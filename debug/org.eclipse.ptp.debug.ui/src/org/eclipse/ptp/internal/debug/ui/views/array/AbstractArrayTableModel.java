package org.eclipse.ptp.internal.debug.ui.views.array;

import org.eclipse.ptp.internal.debug.ui.views.AbstractCTableCellEditor;
import org.eclipse.ptp.internal.debug.ui.views.CTableCellRenderer;
import org.eclipse.ptp.internal.debug.ui.views.ICTableModel;

/**
 * @author clement
 *
 */
public abstract class AbstractArrayTableModel implements ICTableModel {
	protected int colWidth = 30;
	protected int rowHeight = 20;
	protected int rows = 1;
	protected int cols = 1;
	
	public void setContentAt(int col, int row, Object value) {}
	public void setColumnWidth(int col, int value) {
		colWidth = value;
	}
	public void setRowHeight(int value) {
		if (value < getRowHeightMinimum()) {
			value = getRowHeightMinimum();
		}
		rowHeight = value;
	}
	public AbstractCTableCellEditor getCellEditor(int col, int row) {
		return null;
	}
	public int getFixedRowCount() {
		return 1;
	}
	public int getFixedColumnCount() {
		return 1;
	}
	public int getColumnWidth(int col) {
		return colWidth;
	}
	public int getRowHeight() {
		return rowHeight;
	}
	public boolean isColumnResizable(int col) {
		return true;
	}
	public int getFirstRowHeight() {
		return 20;
	}
	public boolean isRowResizable() {
		return false;
	}
	public int getRowHeightMinimum() {
		return 18;
	}
	public int getColumnWidthMinimum() {
		return 20;
	}
	public CTableCellRenderer getCellRenderer(int col, int row) {
		return CTableCellRenderer.defaultRenderer;
	}
	public int getRowCount() {
		return rows;
	}
	public int getColumnCount() {
		return cols;
	}
}
