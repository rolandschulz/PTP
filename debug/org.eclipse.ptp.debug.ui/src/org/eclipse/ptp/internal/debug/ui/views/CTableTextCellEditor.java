package org.eclipse.ptp.internal.debug.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * @author clement
 *
 */
public class CTableTextCellEditor extends AbstractCTableCellEditor {
	private Text m_Text;
	private boolean editable;
	public CTableTextCellEditor(boolean editable) {
		this.editable = editable;
	}
	public void open(CTable table, int col, int row, Rectangle rect) {
		super.open(table, col, row, rect);
		m_Text.setText(m_Model.getContentAt(m_Col, m_Row).toString());
		m_Text.selectAll();
		m_Text.setVisible(true);
		m_Text.setFocus();
	}
	public void close(boolean save) {
		if (save)
			m_Model.setContentAt(m_Col, m_Row, m_Text.getText());
		super.close(save);
		m_Text = null;
	}
	protected Control createControl() {
		m_Text = new Text(m_Table, SWT.NONE);
		m_Text.setEditable(editable);
		m_Text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				try {
					onKeyPressed(e);
				} catch (Exception ex) {}
			}
		});
		m_Text.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent arg0) {
				onTraverse(arg0);
			}
		});
		return m_Text;
	}
	public void setBounds(Rectangle rect) {
		super.setBounds(new Rectangle(rect.x, rect.y + (rect.height - 15) / 2 + 1, rect.width, 15));
	}
}
