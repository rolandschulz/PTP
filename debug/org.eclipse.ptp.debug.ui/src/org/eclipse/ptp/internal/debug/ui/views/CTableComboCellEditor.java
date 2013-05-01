package org.eclipse.ptp.internal.debug.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * @author clement
 *
 */
public class CTableComboCellEditor extends AbstractCTableCellEditor {
	private CCombo m_Combo;
	private String m_Items[];
	public void open(CTable table, int row, int col, Rectangle rect) {
		super.open(table, row, col, rect);
		m_Combo.setFocus();
		m_Combo.setText((String) m_Model.getContentAt(m_Col, m_Row));
	}
	public void close(boolean save) {
		if (save)
			m_Model.setContentAt(m_Col, m_Row, m_Combo.getText());
		super.close(save);
		m_Combo = null;
	}
	protected Control createControl() {
		m_Combo = new CCombo(m_Table, SWT.READ_ONLY);
		m_Combo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		if (m_Items != null)
			m_Combo.setItems(m_Items);
		m_Combo.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				try {
					onKeyPressed(e);
				} catch (Exception ex) {}
			}
		});
		return m_Combo;
	}
	public void setBounds(Rectangle rect) {
		super.setBounds(new Rectangle(rect.x, rect.y + 1, rect.width, rect.height - 2));
	}
	public void setItems(String items[]) {
		m_Items = items;
	}
}
