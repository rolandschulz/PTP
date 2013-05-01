package org.eclipse.ptp.internal.debug.ui.views;

/**
 * @author clement
 *
 */
public interface ICTableModel {
	Object getContentAt(int col, int row);
	AbstractCTableCellEditor getCellEditor(int col, int row);
	int getRowCount();
	int getFixedRowCount();
	int getColumnCount();
	int getFixedColumnCount();
	int getColumnWidth(int col);
	boolean isColumnResizable(int col);
	void setColumnWidth(int col, int value);
	int getRowHeight();
	boolean isRowResizable();
	int getRowHeightMinimum();
	int getColumnWidthMinimum();
	void setRowHeight(int value);
	CTableCellRenderer getCellRenderer(int col, int row);
	void setContentAt(int col, int row, Object value);
}
