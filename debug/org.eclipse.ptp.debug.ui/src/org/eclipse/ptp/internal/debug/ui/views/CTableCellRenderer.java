package org.eclipse.ptp.internal.debug.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * @author clement
 *
 */
public class CTableCellRenderer {
	public static CTableCellRenderer defaultRenderer = new CTableCellRenderer();
	protected Display m_Display = null;
	protected Map<String, Point> stringExtentCache = new HashMap<String, Point>();

	public CTableCellRenderer() {
		m_Display = Display.getCurrent();
	}
	public void dispose() {
		stringExtentCache.clear();
	}
	private Point getStringExtentCache(GC gc, String text) {
		Point pt = stringExtentCache.get(text);
		if (pt == null) {
			pt = gc.stringExtent(text);
			stringExtentCache.put(text, pt);
		}
		return pt;
	}
	public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed) {
		return gc.stringExtent(content.toString()).x + 8;
	}
	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean fixed, boolean clicked) {
		String text = content.toString();
		if (fixed) {
			if (col == 0 && row == 0)
				text = ""; //$NON-NLS-1$

			rect.height += 1;
			rect.width += 1;
			gc.setForeground(m_Display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
			if (clicked) {
				drawText(gc, text, rect.x, rect.y, rect.width, rect.height, 1, 1, m_Display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), m_Display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
			} else {
				drawText(gc, text, rect.x, rect.y, rect.width, rect.height, 1, 1, m_Display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), m_Display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			}
		}
		else {
			Color textColor;
			Color backColor;
			Color vBorderColor;
			Color hBorderColor;
			if (focus) {
				textColor = m_Display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
				backColor = (m_Display.getSystemColor(SWT.COLOR_LIST_SELECTION));
				vBorderColor = m_Display.getSystemColor(SWT.COLOR_LIST_SELECTION);
				hBorderColor = m_Display.getSystemColor(SWT.COLOR_LIST_SELECTION);
			} else {
				textColor = m_Display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
				backColor = m_Display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
				vBorderColor = m_Display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
				hBorderColor = m_Display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			}
			gc.setForeground(hBorderColor);
			gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
			gc.setForeground(vBorderColor);
			gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height);
			gc.setBackground(backColor);
			gc.setForeground(textColor);
			gc.fillRectangle(rect);
			drawText(gc, text, rect.x + 3, rect.y, rect.width - 3, rect.height, 0, 0);
		}
	}
	private void drawText(GC gc, String text, int x, int y, int w, int h, int leftMargin, int topMargin, Color face, Color shadow) {
		Color prevForeground = gc.getForeground();
		Color prevBackground = gc.getBackground();
		try {
			gc.setBackground(face);
			gc.setForeground(shadow);
			gc.drawRectangle(x-1, y-1, w, h);
			//gc.fillRectangle(x + 1, y + 1, 1 + leftMargin, h);
			//gc.fillRectangle(x + 1, y + 1, w - 2, topMargin + 1);
			gc.setForeground(prevForeground);
			drawText(gc, text, x, y, w, h, leftMargin, topMargin);
		} finally {
			gc.setForeground(prevForeground);
			gc.setBackground(prevBackground);
		}
	}
	private void drawText(GC gc, String text, int x, int y, int w, int h, int leftMargin, int topMargin) {
		Point pt = getStringExtentCache(gc, text);
		if (pt.x > w-leftMargin) {
			text = "..."; //$NON-NLS-1$
			pt = getStringExtentCache(gc, text);
		}
		gc.drawText(text, x+(w-pt.x)/2+leftMargin, y+(h-pt.y)/2+topMargin);
	}
}
