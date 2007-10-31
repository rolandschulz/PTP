package org.eclipse.ptp.debug.internal.ui.views;

/**
 * @author clement
 *
 */
public interface ICTableCellResizeListener {
	void rowResized(int newHeight);
	void columnResized(int col, int newWidth);
}
