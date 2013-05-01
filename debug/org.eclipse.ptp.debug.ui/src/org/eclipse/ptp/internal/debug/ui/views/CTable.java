package org.eclipse.ptp.internal.debug.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * @author clement
 *
 */
public class CTable extends Canvas {
	protected GC m_GC;
	protected Display m_Display;
	protected List<Point> m_Selection;
	protected AbstractCTableCellEditor m_CellEditor;
	protected ICTableModel m_Model;
	protected int m_LeftColumn;
	protected int m_TopRow;
	// visiable
	protected int m_RowsVisible;
	protected int m_RowsFullyVisible;
	protected int m_ColumnsVisible;
	protected int m_ColumnsFullyVisible;
	// selection
	protected boolean m_RowSelectionMode;
	protected boolean m_MultiSelectMode;
	protected int m_FocusRow;
	protected int m_FocusCol;
	protected int m_ClickColumnIndex;
	protected int m_ClickRowIndex;
	// resize
	protected int m_ResizeColumnIndex;
	protected int m_ResizeColumnLeft;
	protected int m_ResizeRowIndex;
	protected int m_ResizeRowTop;
	protected int m_NewRowSize;
	// Listener
	protected List<ICTableCellSelectionListener> cellSelectionListeners;
	protected List<ICTableCellResizeListener> cellResizeListeners;
	public CTable(Composite parent, int style) {
		super(parent, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | style);
		init();
	}
	private void init() {
		m_GC = new GC(this);
		m_Display = Display.getCurrent();
		m_Selection = new ArrayList<Point>();
		m_CellEditor = null;
		m_RowSelectionMode = false;
		m_MultiSelectMode = false;
		m_TopRow = 0;
		m_LeftColumn = 0;
		m_FocusRow = 0;
		m_FocusCol = 0;
		m_RowsVisible = 0;
		m_RowsFullyVisible = 0;
		m_ColumnsVisible = 0;
		m_ColumnsFullyVisible = 0;
		m_ResizeColumnIndex = -1;
		m_ResizeRowIndex = -1;
		m_ResizeRowTop = -1;
		m_NewRowSize = -1;
		m_ResizeColumnLeft = -1;
		m_ClickColumnIndex = -1;
		m_ClickRowIndex = -1;
		cellSelectionListeners = new ArrayList<ICTableCellSelectionListener>();
		cellResizeListeners = new ArrayList<ICTableCellResizeListener>();
		createListeners();
	}
	protected void createListeners() {
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				onPaint(event);
			}
		});
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				redraw();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				onMouseDown(e);
			}
			public void mouseUp(MouseEvent e) {
				onMouseUp(e);
			}
			public void mouseDoubleClick(MouseEvent e) {
				onMouseDoubleClick(e);
			}
		});
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				onMouseMove(e);
			}
		});
		if (getVerticalBar() != null) {
			getVerticalBar().addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					m_TopRow = getVerticalBar().getSelection();
					redraw();
				}
			});
		}
		if (getHorizontalBar() != null) {
			getHorizontalBar().addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					m_LeftColumn = getHorizontalBar().getSelection();
					redraw();
				}
			});
		}
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				onKeyDown(e);
			}
		});
	}
	public ICTableModel getModel() {
		return m_Model;
	}
	public void setModel(ICTableModel model) {
		m_Model = model;
		reset();
	}
	public Rectangle getVisibleArea() {
		doCalculations();
		return new Rectangle(m_LeftColumn, m_TopRow, m_ColumnsFullyVisible, m_RowsFullyVisible);
	}
	public void reset() {
		m_FocusCol = -1;
		m_FocusRow = -1;
		clearSelectionWithoutRedraw();
	}
	protected int getFixedWidth() {
		int width = 0;
		for (int i = 0; i < m_Model.getFixedColumnCount(); i++)
			width += m_Model.getColumnWidth(i);
		return width;
	}
	protected int getColumnLeft(int index) {
		if (index < m_Model.getFixedColumnCount()) {
			int x = 0;
			for (int i = 0; i < index; i++) {
				x += m_Model.getColumnWidth(i);
			}
			return x;
		}
		if (index < m_LeftColumn)
			return -1;
		int x = getFixedWidth();
		for (int i = m_LeftColumn; i < index; i++) {
			x += m_Model.getColumnWidth(i);
		}
		return x;
	}
	protected int getColumnRight(int index) {
		if (index < 0)
			return 0;
		return getColumnLeft(index) + m_Model.getColumnWidth(index);
	}
	protected int getLastColumnRight() {
		return getColumnRight(m_Model.getColumnCount() - 1);
	}
	protected void doCalculations() {
		if (m_Model == null) {
			ScrollBar sb = getHorizontalBar();
			if (sb != null) {
				sb.setVisible(false);
				sb.setMinimum(0);
				sb.setMaximum(1);
				sb.setPageIncrement(1);
				sb.setThumb(1);
				sb.setSelection(1);
			}
			sb = getVerticalBar();
			if (sb != null) {
				sb.setVisible(false);
				sb.setMinimum(0);
				sb.setMaximum(1);
				sb.setPageIncrement(1);
				sb.setThumb(1);
				sb.setSelection(1);
			}
			return;
		}
		int m_RowHeight = m_Model.getRowHeight();
		Rectangle rect = getClientArea();
		if (m_LeftColumn < m_Model.getFixedColumnCount()) {
			m_LeftColumn = m_Model.getFixedColumnCount();
		}
		if (m_TopRow < m_Model.getFixedRowCount()) {
			m_TopRow = m_Model.getFixedRowCount();
		}
		int fixedHeight = m_RowHeight + (m_Model.getFixedRowCount() - 1) * m_Model.getRowHeight();
		m_ColumnsVisible = 0;
		m_ColumnsFullyVisible = 0;
		if (m_Model.getColumnCount() > m_Model.getFixedColumnCount()) {
			int runningWidth = getColumnLeft(m_LeftColumn);
			for (int col = m_LeftColumn; col < m_Model.getColumnCount(); col++) {
				if (runningWidth < rect.width + rect.x)
					m_ColumnsVisible++;
				runningWidth += m_Model.getColumnWidth(col);
				if (runningWidth < rect.width + rect.x)
					m_ColumnsFullyVisible++;
				else
					break;
			}
		}
		ScrollBar sb = getHorizontalBar();
		if (sb != null) {
			//TODO: assume all columns has the same width
			boolean visiable = !((m_Model.getColumnCount()*m_Model.getColumnWidth(0)+5 < rect.width) && (m_LeftColumn == m_Model.getFixedColumnCount()));
			sb.setVisible(visiable);
			if (visiable) {
				if (m_Model.getColumnCount() <= m_Model.getFixedColumnCount()) {
					sb.setMinimum(0);
					sb.setMaximum(1);
					sb.setPageIncrement(1);
					sb.setThumb(1);
					sb.setSelection(1);
				} else {
					sb.setMinimum(m_Model.getFixedColumnCount());
					sb.setMaximum(m_Model.getColumnCount());
					sb.setIncrement(1);
					sb.setPageIncrement(2);
					sb.setThumb(m_ColumnsFullyVisible);
					sb.setSelection(m_LeftColumn);
				}
			}
		}
		m_RowsFullyVisible = Math.max(0, (rect.height - fixedHeight) / m_RowHeight);
		m_RowsFullyVisible = Math.min(m_RowsFullyVisible, m_Model.getRowCount() - m_Model.getFixedRowCount());
		m_RowsFullyVisible = Math.max(0, m_RowsFullyVisible);
		m_RowsVisible = m_RowsFullyVisible + 1;
		if (m_TopRow + m_RowsFullyVisible > m_Model.getRowCount()) {
			m_TopRow = Math.max(m_Model.getFixedRowCount(), m_Model.getRowCount() - m_RowsFullyVisible);
		}
		if (m_TopRow + m_RowsFullyVisible >= m_Model.getRowCount()) {
			m_RowsVisible--;
		}
		sb = getVerticalBar();
		if (sb != null) {
			boolean visiable = !((m_Model.getRowCount()*m_Model.getRowHeight()+5 < rect.height) && (m_TopRow == m_Model.getFixedRowCount()));
			sb.setVisible(visiable);
			if (visiable) {
				if (m_Model.getRowCount() <= m_Model.getFixedRowCount()) {
					sb.setMinimum(0);
					sb.setMaximum(1);
					sb.setPageIncrement(1);
					sb.setThumb(1);
					sb.setSelection(1);
				} else {
					sb.setMinimum(m_Model.getFixedRowCount());
					sb.setMaximum(m_Model.getRowCount());
					sb.setPageIncrement(m_RowsVisible);
					sb.setIncrement(1);
					sb.setThumb(m_RowsFullyVisible);
					sb.setSelection(m_TopRow);
				}
			}
		}
	}
	public Rectangle getCellRect(int col, int row) {
		int m_RowHeight = m_Model.getRowHeight();
		if ((col < 0) || (col >= m_Model.getColumnCount()))
			return new Rectangle(-1, -1, 0, 0);
		int x = getColumnLeft(col) + 1;
		int y;
		if (row == 0)
			y = 0;
		else if (row < m_Model.getFixedRowCount())
			y = row * m_RowHeight;
		else
			y = (m_Model.getFixedRowCount() + row - m_TopRow) * m_RowHeight;
		int width = m_Model.getColumnWidth(col) - 1;
		int height = m_RowHeight - 1;
		if (row == 0)
			height = m_RowHeight - 1;
		return new Rectangle(x, y, width, height);
	}
	protected boolean canDrawCell(int col, int row, Rectangle clipRect) {
		Rectangle r = getCellRect(col, row);
		return canDrawCell(r, clipRect);
	}
	protected boolean canDrawCell(Rectangle r, Rectangle clipRect) {
		if (r.y + r.height < clipRect.y)
			return false;
		if (r.y > clipRect.y + clipRect.height)
			return false;
		if (r.x + r.width < clipRect.x)
			return false;
		if (r.x > clipRect.x + clipRect.width)
			return false;
		return true;
	}
	public void redraw() {
		doCalculations();
		super.redraw();
	}
	protected void onPaint(PaintEvent event) {
		Rectangle rect = getClientArea();
		GC gc = event.gc;
		if (m_Model != null) {
			drawCells(gc, gc.getClipping(), 0, m_Model.getFixedColumnCount(), 0, m_Model.getFixedRowCount());
			drawCells(gc, gc.getClipping(), m_LeftColumn, m_Model.getColumnCount(), 0, m_Model.getFixedRowCount());
			drawCells(gc, gc.getClipping(), 0, m_Model.getFixedColumnCount(), m_TopRow, m_TopRow + m_RowsVisible);
			drawCells(gc, gc.getClipping(), m_LeftColumn, m_Model.getColumnCount(), m_TopRow, m_TopRow + m_RowsVisible);
			drawBottomSpace(gc);
		} else {
			gc.fillRectangle(rect);
		}
	}
	// Bottom-Space
	protected void drawBottomSpace(GC gc) {
		Rectangle r = getClientArea();
		if (m_Model.getRowCount() > 0) {
			r.y = (m_Model.getFixedRowCount() + m_RowsVisible) * m_Model.getRowHeight() + 1;
		}
		gc.setForeground(m_Display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.drawLine(0, r.y - 2, getLastColumnRight(), r.y - 2);
		gc.drawLine(getLastColumnRight(), 0, getLastColumnRight(), r.y - 2);
	}
	public void redraw(Rectangle cellsToRedraw) {
		redraw(cellsToRedraw.x, cellsToRedraw.y, cellsToRedraw.width, cellsToRedraw.height);
	}
	public void redraw(int firstCol, int firstRow, int numOfCols, int numOfRows) {
		Rectangle clipRect = getClientArea();
		GC gc = new GC(this);
		drawCells(gc, clipRect, firstCol, firstCol + numOfCols, firstRow, firstRow + numOfRows);
		gc.dispose();
	}
	protected void drawCells(GC gc, Rectangle clipRect, int fromCol, int toCol, int fromRow, int toRow) {
		int cnt = 0;
		Rectangle r;
		if (m_CellEditor != null) {
			if (!isCellVisible(m_CellEditor.m_Col, m_CellEditor.m_Row)) {
				Rectangle hide = new Rectangle(-101, -101, 100, 100);
				m_CellEditor.setBounds(hide);
			} else {
				m_CellEditor.setBounds(getCellRect(m_CellEditor.m_Col, m_CellEditor.m_Row));
			}
		}
		for (int row = fromRow; row < toRow; row++) {
			r = getCellRect(0, row);
			if (r.y + r.height < clipRect.y) {
				continue;
			}
			if (r.y > clipRect.y + clipRect.height) {
				break;
			}
			for (int col = fromCol; col < toCol; col++) {
				r = getCellRect(col, row);
				if (r.x > clipRect.x + clipRect.width) {
					break;
				}
				if (canDrawCell(col, row, clipRect)) {
					drawCell(gc, col, row);
					cnt++;
				}
			}
		}
	}
	protected void drawCell(GC gc, int col, int row) {
		if ((row < 0) || (row >= m_Model.getRowCount())) {
			return;
		}
		Rectangle rect = getCellRect(col, row);
		m_Model.getCellRenderer(col, row).drawCell(gc, rect, col, row, m_Model.getContentAt(col, row), showAsSelected(col, row), col < m_Model.getFixedColumnCount() || row < m_Model.getFixedRowCount(), col == m_ClickColumnIndex && row == m_ClickRowIndex);
	}
	protected boolean showAsSelected(int col, int row) {
		// A cell with an open editor should be drawn without focus
		if (m_CellEditor != null) {
			if (col == m_CellEditor.m_Col && row == m_CellEditor.m_Row)
				return false;
		}
		return isCellSelected(col, row);
	}
	protected void drawRow(GC gc, int row) {
		Rectangle clipRect = getClientArea();
		drawCells(gc, clipRect, 0, m_Model.getFixedColumnCount(), row, row + 1);
		drawCells(gc, clipRect, m_LeftColumn, m_Model.getColumnCount(), row, row + 1);
	}
	protected void drawCol(GC gc, int col) {
		Rectangle clipRect = getClientArea();
		drawCells(gc, clipRect, col, col + 1, 0, m_Model.getFixedRowCount());
		drawCells(gc, clipRect, col, col + 1, m_TopRow, m_TopRow + m_RowsVisible);
	}
	protected int getColumnForResize(int x, int y) {
		if (m_Model == null)
			return -1;
		
		if ((y <= 0) || (y >= m_Model.getFixedRowCount() * m_Model.getRowHeight()))
			return -1;
		if (x < getFixedWidth() + 3) {
			for (int i = 0; i < m_Model.getFixedColumnCount(); i++)
				if (Math.abs(x - getColumnRight(i)) < 3) {
					if (m_Model.isColumnResizable(i))
						return i;
					return -1;
				}
		}
		for (int i = m_LeftColumn; i < m_Model.getColumnCount(); i++) {
			int left = getColumnLeft(i);
			int right = left + m_Model.getColumnWidth(i);
			if (Math.abs(x - right) < 3) {
				if (m_Model.isColumnResizable(i))
					return i;
				return -1;
			}
			if ((x >= left + 3) && (x <= right - 3))
				break;
		}
		return -1;
	}
	protected int getRowForResize(int x, int y) {
		if (m_Model == null || !m_Model.isRowResizable())
			return -1;
		if ((x <= 0) || (x >= getFixedWidth()))
			return -1;
		if (y < m_Model.getRowHeight())
			return -1;
		int row = (y / m_Model.getRowHeight());
		int rowY = (row + 1) * m_Model.getRowHeight();
		if (Math.abs(rowY - y) < 3)
			return row;
		return -1;
	}
	/**
	 * Returns the number of the row that is present at position y or -1, if out
	 * of area.
	 * 
	 * @param y
	 * @return int
	 */
	public int calcRowNum(int y) {
		if (m_Model == null)
			return -1;
		if (y < m_Model.getRowHeight())
			return (m_Model.getFixedRowCount() == 0 ? m_TopRow : 0);
		y -= m_Model.getRowHeight();
		int row = 1 + (y / m_Model.getRowHeight());
		if ((row < 0) || (row >= m_Model.getRowCount()))
			return -1;
		if (row >= m_Model.getFixedRowCount())
			return m_TopRow + row - m_Model.getFixedRowCount();
		return row;
	}
	/**
	 * Returns the number of the column that is present at position x or -1, if
	 * out of area.
	 * 
	 * @param y
	 * @return int
	 */
	public int calcColumnNum(int x) {
		if (m_Model == null)
			return -1;
		int col = 0;
		int z = 0;
		for (int i = 0; i < m_Model.getFixedColumnCount(); i++) {
			if ((x >= z) && (x <= z + m_Model.getColumnWidth(i))) {
				return i;
			}
			z += m_Model.getColumnWidth(i);
		}
		col = -1;
		z = getFixedWidth();
		for (int i = m_LeftColumn; i < m_Model.getColumnCount(); i++) {
			if ((x >= z) && (x <= z + m_Model.getColumnWidth(i))) {
				col = i;
				break;
			}
			z += m_Model.getColumnWidth(i);
		}
		return col;
	}
	public boolean isCellVisible(int col, int row) {
		if (m_Model == null)
			return false;
		return ((col >= m_LeftColumn && col < m_LeftColumn + m_ColumnsVisible && row >= m_TopRow && row < m_TopRow + m_RowsVisible) || (col < m_Model.getFixedColumnCount() && row < m_Model.getFixedRowCount()));
	}
	public boolean isCellFullyVisible(int col, int row) {
		if (m_Model == null)
			return false;
		return ((col >= m_LeftColumn && col < m_LeftColumn + m_ColumnsFullyVisible && row >= m_TopRow && row < m_TopRow + m_RowsFullyVisible) || (col < m_Model.getFixedColumnCount() && row < m_Model.getFixedRowCount()));
	}
	public boolean isRowVisible(int row) {
		if (m_Model == null)
			return false;
		return ((row >= m_TopRow && row < m_TopRow + m_RowsVisible) || row < m_Model.getFixedRowCount());
	}
	public boolean isRowFullyVisible(int row) {
		if (m_Model == null)
			return false;
		return ((row >= m_TopRow && row < m_TopRow + m_RowsFullyVisible) || row < m_Model.getFixedRowCount());
	}
	protected void focusCell(int col, int row, int stateMask) {
		GC gc = new GC(this);
		if (m_CellEditor != null)
			m_CellEditor.close(true);

		if (row >= m_Model.getFixedRowCount() && (col >= m_Model.getFixedColumnCount() || m_RowSelectionMode)) {
			if ((stateMask & SWT.CTRL) == 0 && (stateMask & SWT.SHIFT) == 0) {
				// case: no modifier key
				boolean redrawAll = (m_Selection.size() > 1);
				int oldFocusRow = m_FocusRow;
				int oldFocusCol = m_FocusCol;
				clearSelectionWithoutRedraw();
				addToSelection(col, row);
				m_FocusRow = row;
				m_FocusCol = col;
				if (redrawAll) {
					redraw();
				}
				else if (m_RowSelectionMode) {
					if (isRowVisible(oldFocusRow))
						drawRow(gc, oldFocusRow);
					if (isRowVisible(m_FocusRow))
						drawRow(gc, m_FocusRow);
					
					drawBottomSpace(gc);
				}
				else {
					if (isCellVisible(oldFocusCol, oldFocusRow))
						drawCell(gc, oldFocusCol, oldFocusRow);
					if (isCellVisible(m_FocusCol, m_FocusRow))
						drawCell(gc, m_FocusCol, m_FocusRow);

					drawBottomSpace(gc);
				}
			}
			else if ((stateMask & SWT.CTRL) != 0) {
				// case: CTRL key pressed
				if (toggleSelection(col, row)) {
					m_FocusCol = col;
					m_FocusRow = row;
				}
				if (m_RowSelectionMode) {
					drawRow(gc, row);
				}
				else {
					drawCell(gc, col, row);
				}
			}
			else if ((stateMask & SWT.SHIFT) != 0) {
				// case: SHIFT key pressed
				if (m_RowSelectionMode) {
					if (row < m_FocusRow) {
						// backward selection
						while (row != m_FocusRow) {
							addToSelection(0, --m_FocusRow);
						}
					}
					else {
						// forward selection
						while (row != m_FocusRow) {
							addToSelection(0, ++m_FocusRow);
						}
					}
				}
				else  {// cell selection mode
					if (row < m_FocusRow || (row == m_FocusRow && col < m_FocusCol)) {
						// backward selection
						while (row != m_FocusRow || col != m_FocusCol) {
							m_FocusCol--;
							if (m_FocusCol < m_Model.getFixedColumnCount()) {
								m_FocusCol = m_Model.getColumnCount();
								m_FocusRow--;
							}
							addToSelection(m_FocusCol, m_FocusRow);
						}
					}
					else {
						// forward selection
						while (row != m_FocusRow || col != m_FocusCol) {
							m_FocusCol++;
							if (m_FocusCol == m_Model.getColumnCount()) {
								m_FocusCol = m_Model.getFixedColumnCount();
								m_FocusRow++;
							}
							addToSelection(m_FocusCol, m_FocusRow);
						}
					}
				}
				redraw();
			}
			// notify non-fixed cell listeners
			fireCellSelection(col, row, stateMask);
		}
		else {
			// a fixed cell was focused
			drawCell(gc, col, row);
			// notify fixed cell listeners
			fireFixedCellSelection(col, row, stateMask);
		}
		gc.dispose();
	}
	protected void onMouseDown(MouseEvent e) {
		if (e.button == 1) {
			// deactivateEditor(true);
			setCapture(true);
			// Resize column?
			int columnIndex = getColumnForResize(e.x, e.y);
			if (columnIndex >= 0) {
				m_ResizeColumnIndex = columnIndex;
				m_ResizeColumnLeft = getColumnLeft(columnIndex);
				return;
			}
			// Resize row?
			int rowIndex = getRowForResize(e.x, e.y);
			if (rowIndex >= 0) {
				m_ResizeRowIndex = rowIndex;
				m_ResizeRowTop = rowIndex * m_Model.getRowHeight();
				m_NewRowSize = m_Model.getRowHeight();
				return;
			}
		}
		// focus change
		int col = calcColumnNum(e.x);
		int row = calcRowNum(e.y);
		if (col == -1 || row == -1)
			return;
		m_ClickColumnIndex = col;
		m_ClickRowIndex = row;
		focusCell(col, row, e.stateMask);
	}
	protected void onMouseMove(MouseEvent e) {
		if (m_Model == null)
			return;
		// show resize cursor?
		if ((m_ResizeColumnIndex != -1) || (getColumnForResize(e.x, e.y) >= 0))
			setCursor(new Cursor(m_Display, SWT.CURSOR_SIZEWE));
		else if ((m_ResizeRowIndex != -1) || (getRowForResize(e.x, e.y) >= 0))
			setCursor(new Cursor(m_Display, SWT.CURSOR_SIZENS));
		else
			setCursor(null);
		if (e.button == 1) {
			// extend selection?
			if (m_ClickColumnIndex != -1 && m_MultiSelectMode) {
				int row = calcRowNum(e.y);
				int col = calcColumnNum(e.x);
				if (row >= m_Model.getFixedRowCount() && col >= m_Model.getFixedColumnCount()) {
					m_ClickColumnIndex = col;
					m_ClickRowIndex = row;
					focusCell(col, row, (e.stateMask | SWT.SHIFT));
				}
			}
		}
		// column resize?
		if (m_ResizeColumnIndex != -1) {
			Rectangle rect = getClientArea();
			int oldSize = m_Model.getColumnWidth(m_ResizeColumnIndex);
			if (e.x > rect.x + rect.width - 1)
				e.x = rect.x + rect.width - 1;
			int newSize = e.x - m_ResizeColumnLeft;
			if (newSize < m_Model.getColumnWidthMinimum())
				newSize = m_Model.getColumnWidthMinimum();

			int leftX = getColumnLeft(m_ResizeColumnIndex);
			int rightX = getColumnRight(m_ResizeColumnIndex);
			m_Model.setColumnWidth(m_ResizeColumnIndex, newSize);
			newSize = m_Model.getColumnWidth(m_ResizeColumnIndex);
			GC gc = new GC(this);
			gc.copyArea(rightX, 0, rect.width - rightX, rect.height, leftX + newSize, 0);
			drawCol(gc, m_ResizeColumnIndex);
			if (newSize < oldSize) {
				int delta = oldSize - newSize;
				redraw(rect.width - delta, 0, delta, rect.height, false);
			}
			gc.dispose();
		}
		// row resize?
		if (m_ResizeRowIndex != -1) {
			Rectangle rect = getClientArea();
			GC gc = new GC(this);
			// calculate new size
			if (e.y > rect.y + rect.height - 1)
				e.y = rect.y + rect.height - 1;
			m_NewRowSize = e.y - m_ResizeRowTop;
			if (m_NewRowSize < m_Model.getRowHeightMinimum())
				m_NewRowSize = m_Model.getRowHeightMinimum();

			gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			int lineEnd = getColumnRight(m_LeftColumn + m_ColumnsVisible - 1);
			int m_LineX = rect.x + 1;
			int m_LineY = m_ResizeRowTop + m_NewRowSize - 1;
			gc.drawLine(m_LineX, m_LineY, rect.x + lineEnd, m_LineY);
			gc.dispose();
		}
	}
	protected void onMouseUp(MouseEvent e) {
		if (m_Model == null)
			return;
		setCapture(false);
		if (m_ResizeColumnIndex != -1) {
			fireColumnResize(m_ResizeColumnIndex, m_Model.getColumnWidth(m_ResizeColumnIndex));
			m_ResizeColumnIndex = -1;
			redraw();
		}
		if (m_ResizeRowIndex != -1) {
			m_ResizeRowIndex = -1;
			m_Model.setRowHeight(m_NewRowSize);
			fireRowResize(m_NewRowSize);
			redraw();
		}
		if (m_ClickColumnIndex != -1) {
			int col = m_ClickColumnIndex;
			int row = m_ClickRowIndex;
			m_ClickColumnIndex = -1;
			m_ClickRowIndex = -1;
			if (m_CellEditor == null) {
				drawCell(new GC(this), col, row);
			}
		}
	}
	protected void onKeyDown(KeyEvent e) {
		boolean focusChanged = false;
		int newFocusRow = m_FocusRow;
		int newFocusCol = m_FocusCol;
		if (m_Model == null)
			return;
		if ((e.character == ' ') || (e.character == '\r')) {
			openEditorInFocus();
			return;
		} else if (e.keyCode == SWT.HOME) {
			newFocusCol = m_Model.getFixedColumnCount();
			if (newFocusRow == -1)
				newFocusRow = m_Model.getFixedRowCount();
			focusChanged = true;
		} else if (e.keyCode == SWT.END) {
			newFocusCol = m_Model.getColumnCount() - 1;
			if (newFocusRow == -1)
				newFocusRow = m_Model.getFixedRowCount();
			focusChanged = true;
		} else if (e.keyCode == SWT.ARROW_LEFT) {
			if (!m_RowSelectionMode) {
				if (newFocusCol > m_Model.getFixedColumnCount())
					newFocusCol--;
			}
			focusChanged = true;
		} else if (e.keyCode == SWT.ARROW_RIGHT) {
			if (!m_RowSelectionMode) {
				if (newFocusCol == -1) {
					newFocusCol = m_Model.getFixedColumnCount();
					newFocusRow = m_Model.getFixedRowCount();
				} else if (newFocusCol < m_Model.getColumnCount() - 1)
					newFocusCol++;
			}
			focusChanged = true;
		} else if (e.keyCode == SWT.ARROW_DOWN) {
			if (newFocusRow == -1) {
				newFocusRow = m_Model.getFixedRowCount();
				newFocusCol = m_Model.getFixedColumnCount();
			} else if (newFocusRow < m_Model.getRowCount() - 1)
				newFocusRow++;
			focusChanged = true;
		} else if (e.keyCode == SWT.ARROW_UP) {
			if (newFocusRow > m_Model.getFixedRowCount())
				newFocusRow--;
			focusChanged = true;
		} else if (e.keyCode == SWT.PAGE_DOWN) {
			newFocusRow += m_RowsVisible - 1;
			if (newFocusRow >= m_Model.getRowCount())
				newFocusRow = m_Model.getRowCount() - 1;
			if (newFocusCol == -1)
				newFocusCol = m_Model.getFixedColumnCount();
			focusChanged = true;
		} else if (e.keyCode == SWT.PAGE_UP) {
			newFocusRow -= m_RowsVisible - 1;
			if (newFocusRow < m_Model.getFixedRowCount())
				newFocusRow = m_Model.getFixedRowCount();
			if (newFocusCol == -1)
				newFocusCol = m_Model.getFixedColumnCount();
			focusChanged = true;
		}
		if (focusChanged) {
			focusCell(newFocusCol, newFocusRow, e.stateMask);
			if (!isCellFullyVisible(m_FocusCol, m_FocusRow))
				scrollToFocus();
		}
	}
	protected void onMouseDoubleClick(MouseEvent e) {
		if (m_Model == null)
			return;
		if (e.button == 1) {
			if (e.y < (m_Model.getFixedRowCount() * m_Model.getRowHeight())) {
				// double click in header area
				int columnIndex = getColumnForResize(e.x, e.y);
				resizeColumnOptimal(columnIndex);
				return;
			}
			//FIXME: assume column 0 is fixed column, problem may occur on 2 fixed columns 
			else if (e.x < m_Model.getColumnWidth(0) + ((m_Model.getFixedColumnCount() - 1) * m_Model.getColumnWidth(0))) {
				// double click in header area
				return;
			}
			else
				openEditorInFocus();
		}
	}
	public int resizeColumnOptimal(int column) {
		if (column >= 0 && column < m_Model.getColumnCount()) {
			int optWidth = 5;
			for (int i = 0; i < m_Model.getFixedRowCount(); i++) {
				int width = m_Model.getCellRenderer(column, i).getOptimalWidth(m_GC, column, i, m_Model.getContentAt(column, i), true);
				if (width > optWidth)
					optWidth = width;
			}
			for (int i = m_TopRow; i < m_TopRow + m_RowsVisible; i++) {
				int width = m_Model.getCellRenderer(column, i).getOptimalWidth(m_GC, column, i, m_Model.getContentAt(column, i), true);
				if (width > optWidth)
					optWidth = width;
			}
			m_Model.setColumnWidth(column, optWidth);
			redraw();
			return optWidth;
		}
		return -1;
	}
	public void openEditorInFocus() {
		m_CellEditor = m_Model.getCellEditor(m_FocusCol, m_FocusRow);
		if (m_CellEditor != null) {
			Rectangle r = getCellRect(m_FocusCol, m_FocusRow);
			m_CellEditor.open(this, m_FocusCol, m_FocusRow, r);
		}
	}
	protected void scrollToFocus() {
		boolean change = false;
		if (getVerticalBar() != null) {
			if (m_FocusRow < m_TopRow) {
				m_TopRow = m_FocusRow;
				change = true;
			}
			if (m_FocusRow >= m_TopRow + m_RowsFullyVisible) {
				m_TopRow = m_FocusRow - m_RowsFullyVisible + 1;
				change = true;
			}
		}
		if (getHorizontalBar() != null) {
			if (m_FocusCol < m_LeftColumn) {
				m_LeftColumn = m_FocusCol;
				change = true;
			}
			if (m_FocusCol >= m_LeftColumn + m_ColumnsFullyVisible) {
				int oldLeftCol = m_LeftColumn;
				Rectangle rect = getClientArea();
				while (m_LeftColumn < m_FocusCol && getColumnRight(m_FocusCol) > rect.width + rect.x) {
					m_LeftColumn++;
				}
				change = (oldLeftCol != m_LeftColumn);
			}
		}
		if (change)
			redraw();
	}
	protected void fireCellSelection(int col, int row, int statemask) {
		for (int i = 0; i < cellSelectionListeners.size(); i++) {
			((ICTableCellSelectionListener) cellSelectionListeners.get(i)).cellSelected(col, row, statemask);
		}
	}
	protected void fireFixedCellSelection(int col, int row, int statemask) {
		for (int i = 0; i < cellSelectionListeners.size(); i++) {
			((ICTableCellSelectionListener) cellSelectionListeners.get(i)).fixedCellSelected(col, row, statemask);
		}
	}
	protected void fireColumnResize(int col, int newSize) {
		for (int i = 0; i < cellResizeListeners.size(); i++) {
			((ICTableCellResizeListener) cellResizeListeners.get(i)).columnResized(col, newSize);
		}
	}
	protected void fireRowResize(int newSize) {
		for (int i = 0; i < cellResizeListeners.size(); i++) {
			((ICTableCellResizeListener) cellResizeListeners.get(i)).rowResized(newSize);
		}
	}
	public void addCellSelectionListener(ICTableCellSelectionListener listener) {
		cellSelectionListeners.add(listener);
	}
	public boolean removeCellSelectionListener(ICTableCellSelectionListener listener) {
		return cellSelectionListeners.remove(listener);
	}
	public boolean removeCellResizeListener(ICTableCellResizeListener listener) {
		return cellResizeListeners.remove(listener);
	}
	public void setRowSelectionMode(boolean rowSelectMode) {
		m_RowSelectionMode = rowSelectMode;
		clearSelection();
	}
	public void setMultiSelectionMode(boolean multiSelectMode) {
		m_MultiSelectMode = multiSelectMode;
		clearSelection();
	}
	public boolean isRowSelectMode() {
		return m_RowSelectionMode;
	}
	public boolean isMultiSelectMode() {
		return m_MultiSelectMode;
	}
	protected void clearSelectionWithoutRedraw() {
		m_Selection.clear();
	}
	public void clearSelection() {
		clearSelectionWithoutRedraw();
		m_FocusCol = -1;
		m_FocusRow = -1;
		if (m_MultiSelectMode)
			redraw();
	}
	protected boolean toggleSelection(int col, int row) {
		if (m_MultiSelectMode) {
			Point pt = new Point(col, row);
			if (m_Selection.contains(pt)) {
				m_Selection.remove(pt);
				return false;
			} else {
				m_Selection.add(pt);
				return true;
			}
		}
		return false;
	}
	protected void addToSelection(int col, int row) {
		if (m_MultiSelectMode) {
			if (m_RowSelectionMode)
				col = 0;
			Point pt = new Point(col, row);
			if (!m_Selection.contains(pt)) {
				m_Selection.add(pt);
			}
		}
	}
	public void setSelection(int col, int row, boolean scroll) {
		if (col < m_Model.getColumnCount() && col >= m_Model.getFixedColumnCount() && row < m_Model.getRowCount() && row >= m_Model.getFixedRowCount()) {
			focusCell(col, row, SWT.None);
			if (scroll) {
				scrollToFocus();
			}
		}
	}
	public boolean isCellSelected(int col, int row) {
		if (!m_MultiSelectMode) {
			if (m_RowSelectionMode)
				return (row == m_FocusRow);
			return (col == m_FocusCol && row == m_FocusRow);
		}
		if (m_RowSelectionMode)
			col = 0;
		return m_Selection.contains(new Point(col, row));
	}
	public int[] getRowSelection() {
		if (!m_RowSelectionMode)
			return null;
		if (!m_MultiSelectMode) {
			if (m_FocusRow < 0)
				return new int[0];
			int[] tmp = new int[1];
			tmp[0] = m_FocusRow;
			return tmp;
		}
		Point[] pts = m_Selection.toArray(new Point[0]);
		int[] erg = new int[pts.length];
		for (int i = 0; i < erg.length; i++) {
			erg[i] = pts[i].y;
		}
		return erg;
	}
	public Point[] getCellSelection() {
		if (m_RowSelectionMode)
			return null;
		if (!m_MultiSelectMode) {
			if (m_FocusRow < 0 || m_FocusCol < 0)
				return new Point[0];
			Point[] tmp = new Point[1];
			tmp[0] = new Point(m_FocusCol, m_FocusRow);
			return tmp;
		}
		return (Point[]) m_Selection.toArray(new Point[0]);
	}
}
