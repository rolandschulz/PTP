package org.eclipse.ptp.internal.debug.ui.views;

/**
 * @author clement
 *
 */
public interface ICTableCellSelectionListener {
	void cellSelected(int col, int row, int statemask);
	void fixedCellSelected(int col, int row, int statemask);
}
