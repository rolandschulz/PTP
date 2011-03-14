package org.eclipse.ptp.rm.jaxb.ui.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class WidgetBuilderUtils implements IJAXBUINonNLSConstants {

	private WidgetBuilderUtils() {
	}

	public static TableColumn addTableColumn(final TableViewer viewer, final String columnName, int style, SelectionListener l) {
		Table t = viewer.getTable();

		TableColumn c = new TableColumn(t, style);
		c.setText(columnName);
		if (l != null) {
			c.addSelectionListener(l);
		}
		return c;
	}

	public static void applyMonospace(Text text) {
		Display d = Display.getCurrent();
		// three fonts for Mac, Linux, Windows ...
		FontData[][] f = { d.getFontList(COURIER, true), d.getFontList(COURIER, false), d.getFontList(COURIER, true),
				d.getFontList(COURIER, false), d.getFontList(COURIER, true), d.getFontList(COURIER, false) };
		int i = 0;
		for (; i < f.length; i++) {
			if (f[i].length > 0) {
				text.setFont(new Font(d, f[i]));
				break;
			}
		}
		if (i == f.length) {
			Dialog.applyDialogFont(text);
		}
	}

	public static Button createButton(Composite parent, GridData data, String label, int type, SelectionListener listener) {
		Button button = new Button(parent, type);
		button.setText(label);
		if (data == null) {
			data = createGridData(GridData.FILL_HORIZONTAL, DEFAULT);
		}
		button.setLayoutData(data);
		if (null != listener) {
			button.addSelectionListener(listener);
		}
		return button;
	}

	public static Button createButton(Composite parent, String label, int type) {
		return createButton(parent, null, label, type, null);
	}

	// public static Spinner createSpinner(Composite container, String
	// labelString, int min, int max, int initial, int colSpan,
	// boolean fill, ModifyListener listener) {
	// if (labelString != null) {
	// GridData data = new GridData();
	// Label label = new Label(container, SWT.NONE);
	// label.setText(labelString);
	// label.setLayoutData(data);
	// }
	//
	// GridData data = new GridData();
	// if (fill) {
	// data.horizontalAlignment = SWT.FILL;
	// }
	// data.grabExcessHorizontalSpace = false;
	// data.horizontalSpan = colSpan;
	//
	// Spinner s = new Spinner(container, SWT.NONE);
	// s.setMaximum(max);
	// s.setMinimum(min);
	// s.setSelection(initial);
	// s.setLayoutData(data);
	// if (listener != null) {
	// s.addModifyListener(listener);
	// }
	// return s;
	// }

	public static Button createButton(Composite parent, String label, int type, SelectionListener listener) {
		return createButton(parent, null, label, type, listener);
	}

	public static Button createCheckButton(Composite parent, String label, SelectionListener listener) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT, listener);
	}

	public static Combo createCombo(Composite parent, int style, GridData data, Object listener) {
		return createCombo(parent, style, data, new String[0], null, null, null, listener);
	}

	public static Combo createCombo(Composite parent, int style, GridData data, String[] items, String initial, String label,
			String tooltip, Object listener) {
		if (label != null) {
			Label comboLabel = createLabel(parent, label, SWT.RIGHT, 1);
			if (tooltip != null) {
				comboLabel.setToolTipText(tooltip);
			}
		}
		Combo combo = new Combo(parent, style);
		combo.setItems(items);
		combo.setLayoutData(data);
		if (initial != null) {
			combo.setText(initial);
		}
		if (listener != null) {
			if (listener instanceof ModifyListener) {
				combo.addModifyListener((ModifyListener) listener);
			} else if (listener instanceof SelectionListener) {
				combo.addSelectionListener((SelectionListener) listener);
			}
		}
		return combo;
	}

	public static Combo createCombo(Composite parent, int cols, String[] items, String initial, String labelString, String tooltip,
			Object listener) {
		GridData data = createGridData(GridData.FILL_HORIZONTAL, true, false, 100, DEFAULT, cols);
		return createCombo(parent, SWT.BORDER, data, items, initial, labelString, tooltip, listener);
	}

	public static Composite createComposite(Composite parent, int columns) {
		GridLayout layout = createGridLayout(columns, false, DEFAULT, DEFAULT, 1, DEFAULT);
		return createComposite(parent, SWT.NONE, layout, null);
	}

	public static Composite createComposite(Composite parent, int style, GridLayout layout, GridData data) {
		Composite composite = new Composite(parent, style);
		composite.setLayout(layout);
		if (data != null) {
			composite.setData(data);
		}
		return composite;
	}

	public static GridData createGridData(int style, boolean grabH, boolean grabV, int wHint, int hHint, int cols) {
		GridData data = new GridData();
		if (style == DEFAULT) {
			data = new GridData();
		} else {
			data = new GridData(style);
		}
		data.grabExcessHorizontalSpace = grabH;
		data.grabExcessVerticalSpace = grabV;
		if (wHint != DEFAULT) {
			data.widthHint = wHint;
		}
		if (hHint != DEFAULT) {
			data.heightHint = hHint;
		}
		if (cols != DEFAULT) {
			data.horizontalSpan = cols;
		}
		return data;
	}

	public static GridData createGridData(int style, int cols) {
		return createGridData(style, false, false, DEFAULT, DEFAULT, cols);
	}

	public static GridData createGridDataFill(int wHint, int hHint, int cols) {
		return createGridData(GridData.FILL_BOTH, true, true, wHint, hHint, cols);
	}

	public static GridData createGridDataFillH(int cols) {
		return createGridData(GridData.FILL_HORIZONTAL, true, false, DEFAULT, DEFAULT, cols);
	}

	public static GridLayout createGridLayout(int columns, boolean equal) {
		return createGridLayout(columns, equal, DEFAULT, DEFAULT, DEFAULT, DEFAULT);
	}

	public static GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		return createGridLayout(columns, isEqual, mh, mw, DEFAULT, DEFAULT);
	}

	public static GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw, int vSpace, int hSpace) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		if (mh != DEFAULT) {
			gridLayout.marginHeight = mh;
		}
		if (mw != DEFAULT) {
			gridLayout.marginWidth = mw;
		}
		if (vSpace != DEFAULT) {
			gridLayout.verticalSpacing = vSpace;
		}
		if (hSpace != DEFAULT) {
			gridLayout.horizontalSpacing = hSpace;
		}
		return gridLayout;
	}

	public static Group createGroup(Composite parent, int style, GridLayout layout, GridData data) {
		return createGroup(parent, style, layout, data, null);
	}

	public static Group createGroup(Composite parent, int style, GridLayout layout, GridData data, String text) {
		Group group = new Group(parent, style);
		group.setLayout(layout);
		group.setLayoutData(data);
		if (text != null) {
			group.setText(text);
		}
		return group;
	}

	public static Label createLabel(Composite container, String text, int style, GridData data) {
		Label label = new Label(container, style);
		if (text == null) {
			text = ZEROSTR;
		}
		label.setText(text.trim());
		if (data == null) {
			data = createGridData(DEFAULT, 1);
		}
		label.setLayoutData(data);
		return label;
	}

	public static Label createLabel(Composite container, String text, int style, int colSpan) {
		GridData data = createGridData(DEFAULT, colSpan);
		return createLabel(container, text, style, data);
	}

	public static Button createPushButton(Composite parent, String label, SelectionListener listener) {
		Button button = SWTUtil.createPushButton(parent, label, null);
		GridData data = createGridData(GridData.FILL_HORIZONTAL, 1);
		button.setLayoutData(data);
		if (null != listener) {
			button.addSelectionListener(listener);
		}
		return button;
	}

	public static Button createRadioButton(Composite parent, String label, String value, SelectionListener listener) {
		Button button = createButton(parent, label, SWT.RADIO | SWT.LEFT, listener);
		button.setData((null == value) ? label : value);
		return button;
	}

	public static Table createTable(Composite parent, int style, int cols, int wHint, GridData data) {
		Table t = new Table(parent, style);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		t.setLayoutData(data);
		TableLayout layout = new TableLayout();
		for (int i = 0; i < cols; i++) {
			layout.addColumnData(new ColumnPixelData(wHint / cols));
		}
		t.setLayout(layout);
		return t;
	}

	public static Text createText(Composite parent, int options, GridData data, boolean readOnly, String initialContents) {
		return createText(parent, options, data, readOnly, initialContents, null, null);
	}

	public static Text createText(Composite parent, int options, GridData data, boolean readOnly, String initialContents,
			ModifyListener listener, Color color) {
		Text text = new Text(parent, options);
		text.setLayoutData(data);
		text.setEditable(!readOnly);
		if (color != null) {
			text.setBackground(color);
		}
		text.setLayoutData(data);
		if (initialContents != null) {
			text.setText(initialContents);
		}
		if (listener != null) {
			text.addModifyListener(listener);
		}
		return text;
	}

	public static Text createText(Composite parent, String initialValue, boolean fill, boolean readOnly, ModifyListener listener,
			Color color) {
		GridData data = createGridData(fill ? GridData.FILL_HORIZONTAL : DEFAULT, true, false, DEFAULT, DEFAULT, DEFAULT);
		return createText(parent, SWT.BORDER, data, readOnly, initialValue, listener, color);
	}

	/**
	 * Normalizes text for display to fit into lines of the given length,
	 * without further tabs or breaks. This is useful for labels and read-only
	 * text messages.
	 * 
	 * @param length
	 * @param text
	 */
	public static String fitToLineLength(int length, String text) {
		if (text == null) {
			return null;
		}
		if (length < 1) {
			length = Integer.MAX_VALUE;
		}
		StringBuffer newLine = new StringBuffer();
		int strln = text.length();
		int current = 0;
		char lastChar = 0;
		for (int i = 0; i < strln; i++) {
			char c = text.charAt(i);
			switch (c) {
			case '\t':
			case ' ':
			case '\n':
			case '\r':
				if (current >= length) {
					newLine.append(LINE_SEP);
					current = 0;
				} else if (lastChar != SP.charAt(0)) {
					newLine.append(SP);
					current++;
					lastChar = SP.charAt(0);
				}
				break;
			default:
				newLine.append(c);
				current++;
				lastChar = c;
			}
		}
		return newLine.toString();
	}
}
