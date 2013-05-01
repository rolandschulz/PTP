package org.eclipse.ptp.internal.debug.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * @author clement
 *
 */
public abstract class AbstractCTableCellEditor {
	protected CTable m_Table;
	protected Rectangle m_Rect;
	protected int m_Row;
	protected int m_Col;
	protected Control m_Control;
	protected String toolTip;
	protected ICTableModel m_Model;
	
	public void dispose() {
		if (m_Control != null) {
			m_Control.dispose();
			m_Control = null;
		}
	}
	public void open(CTable table, int col, int row, Rectangle rect) {
		m_Table = table;
		m_Model = table.getModel();
		m_Rect = rect;
		m_Row = row;
		m_Col = col;
		if (m_Control == null) {
			m_Control = createControl();
			m_Control.setToolTipText(toolTip);
			m_Control.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent arg0) {
					close(true);
				}
			});
		}
		setBounds(m_Rect);
		GC gc = new GC(m_Table);
		m_Table.drawCell(gc, m_Col, m_Row);
		gc.dispose();
	}
	public void close(boolean save) {
		m_Table.m_CellEditor = null;
		// m_Control.setVisible(false);
		GC gc = new GC(m_Table);
		m_Table.drawCell(gc, m_Col, m_Row);
		gc.dispose();
		this.dispose();
	}
	public boolean isFocused() {
		if (m_Control == null)
			return false;
		return m_Control.isFocusControl();
	}
	public void setBounds(Rectangle rect) {
		if (m_Control != null)
			m_Control.setBounds(rect);
	}
	protected void onKeyPressed(KeyEvent e) {
		if ((e.character == '\r') && ((e.stateMask & SWT.SHIFT) == 0)) {
			close(true);
		} else if (e.character == SWT.ESC) {
			close(false);
		} else {
			m_Table.scrollToFocus();
		}
	}
	protected void onTraverse(TraverseEvent e) {
		close(true);
	}
	public void setToolTipText(String toolTip) {
		this.toolTip = toolTip;
	}
	protected abstract Control createControl();
}
