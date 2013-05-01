package org.eclipse.ptp.internal.debug.ui.views;

/**
 * @author clement
 *
 */
public interface ICTableCellResizeListener {
	void rowResized(int newHeight);
	void columnResized(int col, int newWidth);
}
