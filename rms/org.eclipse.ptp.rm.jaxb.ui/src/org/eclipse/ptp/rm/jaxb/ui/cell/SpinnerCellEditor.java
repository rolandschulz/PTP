package org.eclipse.ptp.rm.jaxb.ui.cell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

public class SpinnerCellEditor extends CellEditor {

	private Spinner spinner;

	private static final int defaultStyle = SWT.NONE;

	public SpinnerCellEditor() {
		setStyle(defaultStyle);
	}

	public SpinnerCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	public SpinnerCellEditor(Composite parent, int style) {
		this(parent, style, null, null);
	}

	public SpinnerCellEditor(Composite parent, int style, Integer min, Integer max) {
		super(parent, style);
		spinner.setMinimum(min);
		spinner.setMaximum(max);
	}

	public SpinnerCellEditor(Composite parent, Integer min, Integer max) {
		this(parent, defaultStyle, min, max);
	}

	@Override
	protected Control createControl(Composite parent) {
		spinner = new Spinner(parent, getStyle());
		return spinner;
	}

	@Override
	protected Object doGetValue() {
		return spinner.getSelection();
	}

	@Override
	protected void doSetFocus() {
		spinner.setFocus();
	}

	@Override
	protected void doSetValue(Object value) {
		Assert.isTrue(value instanceof Integer);
		spinner.setSelection((Integer) value);
	}
}
