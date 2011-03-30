/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 *  M Venkataramana - set up editing: http://eclipse.dzone.com/users/venkat_r_m
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewer;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.core.data.Style;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.cell.AttributeViewerEditingSupport;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerRowData;
import org.eclipse.ptp.rm.jaxb.ui.data.ColumnDescriptor;
import org.eclipse.ptp.rm.jaxb.ui.providers.TableDataContentProvider;
import org.eclipse.ptp.rm.jaxb.ui.providers.TableDataLabelProvider;
import org.eclipse.ptp.rm.jaxb.ui.providers.TreeDataContentProvider;
import org.eclipse.ptp.rm.jaxb.ui.providers.TreeDataLabelProvider;
import org.eclipse.ptp.rm.jaxb.ui.sorters.AttributeViewerSorter;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

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
		GridData data = createGridData(GridData.FILL_HORIZONTAL, true, false, 100, DEFAULT, cols, DEFAULT);
		return createCombo(parent, SWT.BORDER, data, items, initial, labelString, tooltip, listener);
	}

	public static Composite createComposite(Composite parent, int columns) {
		GridLayout layout = createGridLayout(columns, false, DEFAULT, 1, DEFAULT, DEFAULT);
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

	public static GridData createGridData(int style, boolean grabH, boolean grabV, int wHint, int hHint, int hSpan, int vSpan) {
		return createGridData(style, grabH, grabV, wHint, hHint, DEFAULT, DEFAULT, hSpan, vSpan, DEFAULT, DEFAULT);
	}

	public static GridData createGridData(int style, boolean grabH, boolean grabV, int wHint, int hHint, int minW, int minH,
			int hSpan, int vSpan, int hAlign, int vAlign) {
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
		if (minW != DEFAULT) {
			data.minimumWidth = minW;
		}
		if (minH != DEFAULT) {
			data.minimumHeight = minH;
		}
		if (hSpan != DEFAULT) {
			data.horizontalSpan = hSpan;
		}
		if (vSpan != DEFAULT) {
			data.verticalSpan = vSpan;
		}
		if (hAlign != DEFAULT) {
			data.horizontalAlignment = hAlign;
		}
		if (vAlign != DEFAULT) {
			data.verticalAlignment = vAlign;
		}
		return data;
	}

	public static GridData createGridData(int style, int cols) {
		return createGridData(style, false, false, DEFAULT, DEFAULT, cols, DEFAULT);
	}

	public static GridData createGridDataFill(int wHint, int hHint, int cols) {
		return createGridData(GridData.FILL_BOTH, true, true, wHint, hHint, cols, DEFAULT);
	}

	public static GridData createGridDataFillH(int cols) {
		return createGridData(GridData.FILL_HORIZONTAL, true, false, DEFAULT, DEFAULT, cols, DEFAULT);
	}

	public static GridLayout createGridLayout(int columns, boolean equal) {
		return createGridLayout(columns, equal, DEFAULT, DEFAULT, DEFAULT, DEFAULT);
	}

	public static GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		return createGridLayout(columns, isEqual, mh, mw, DEFAULT, DEFAULT);
	}

	public static GridLayout createGridLayout(int columns, boolean isEqual, int hSpace, int vSpace, int mw, int mh) {
		return createGridLayout(columns, isEqual, hSpace, vSpace, mw, mh, DEFAULT, DEFAULT, DEFAULT, DEFAULT);
	}

	public static GridLayout createGridLayout(int columns, boolean isEqual, int hSpace, int vSpace, int mw, int mh, int mL, int mR,
			int mT, int mB) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		if (hSpace != DEFAULT) {
			gridLayout.horizontalSpacing = hSpace;
		}
		if (vSpace != DEFAULT) {
			gridLayout.verticalSpacing = vSpace;
		}
		if (mw != DEFAULT) {
			gridLayout.marginWidth = mw;
		}
		if (mh != DEFAULT) {
			gridLayout.marginHeight = mh;
		}
		if (mL != DEFAULT) {
			gridLayout.marginLeft = mL;
		}
		if (mR != DEFAULT) {
			gridLayout.marginRight = mR;
		}
		if (mT != DEFAULT) {
			gridLayout.marginTop = mT;
		}
		if (mB != DEFAULT) {
			gridLayout.marginBottom = mB;
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

	public static Spinner createSpinner(Composite parent, GridData data, String label, int min, int max, int initial,
			ModifyListener listener) {
		if (label != null) {
			createLabel(parent, label, SWT.RIGHT, 1);
		}

		Spinner s = new Spinner(parent, SWT.NONE);
		s.setMaximum(max);
		s.setMinimum(min);
		s.setSelection(initial);
		s.setLayoutData(data);
		if (listener != null) {
			s.addModifyListener(listener);
		}
		return s;
	}

	public static TabItem createTabItem(TabFolder folder, int style, String text, String tooltip, int index) {
		TabItem item = new TabItem(folder, style, index);
		item.setText(text);
		item.setToolTipText(tooltip);
		return item;
	}

	public static Table createTable(Composite parent, int style, GridData data) {
		int cols = data.horizontalSpan;
		int wHint = data.widthHint;
		return createTable(parent, style, cols, wHint, data);
	}

	public static Table createTable(Composite parent, int style, int cols, int wHint, GridData data) {
		Table t = new Table(parent, style);
		t.setLayoutData(data);
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
		GridData data = createGridData(fill ? GridData.FILL_HORIZONTAL : DEFAULT, true, false, DEFAULT, DEFAULT, DEFAULT, DEFAULT);
		return createText(parent, SWT.BORDER, data, readOnly, initialValue, listener, color);
	}

	public static Tree createTree(Composite parent, int style, GridData data) {
		int cols = data.horizontalSpan;
		int wHint = data.widthHint;
		return createTree(parent, style, cols, wHint, data);
	}

	public static Tree createTree(Composite parent, int style, int cols, int wHint, GridData data) {
		Tree t = new Tree(parent, style);
		t.setLayoutData(data);
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

	public static List<ColumnDescriptor> getColumnDescriptors(AttributeViewer descriptor) {
		List<ColumnData> data = descriptor.getColumnData();
		List<ColumnDescriptor> desc = new ArrayList<ColumnDescriptor>();
		for (ColumnData d : data) {
			desc.add(new ColumnDescriptor(d));
		}
		return desc;
	}

	public static int getStyle(String style) {
		if (style == null) {
			return SWT.NONE;
		}
		return getStyle(style.split(PIP));
	}

	public static int getStyle(Style style) {
		if (style == null) {
			return SWT.NONE;
		}
		return getStyle(style.getTag().toArray(new String[0]));
	}

	public static void setupAttributeTable(final CheckboxTableViewer viewer, List<ColumnDescriptor> columnDescriptors,
			ISelectionChangedListener listener, boolean sortName) {
		setupSpecific(viewer, columnDescriptors, sortName);
		setupCommon(viewer, columnDescriptors, listener);
	}

	public static void setupAttributeTree(final CheckboxTreeViewer viewer, List<ColumnDescriptor> columnDescriptors,
			ISelectionChangedListener listener, boolean sortName) {
		setupSpecific(viewer, columnDescriptors, sortName);
		setupCommon(viewer, columnDescriptors, listener);
	}

	private static SelectionAdapter getAttributeViewerSelectionAdapter(final ColumnViewer viewer) {
		return new SelectionAdapter() {
			private boolean toggle = false;

			@Override
			public void widgetSelected(SelectionEvent e) {
				AttributeViewerSorter sorter = new AttributeViewerSorter();
				if (toggle) {
					sorter.toggle();
				}
				viewer.setSorter(sorter);
				toggle = !toggle;
			}
		};
	}

	private static int getStyle(String[] style) {
		int swt = 0;

		for (String s : style) {
			s = s.trim();

			if (ARROW.equals(s)) {
				swt |= SWT.ARROW;
			}
			if (BACKGROUND.equals(s)) {
				swt |= SWT.BACKGROUND;
			}
			if (BALLOON.equals(s)) {
				swt |= SWT.BALLOON;
			}
			if (BAR.equals(s)) {
				swt |= SWT.BAR;
			}
			if (BEGINNING.equals(s)) {
				swt |= SWT.BEGINNING;
			}
			if (BORDER.equals(s)) {
				swt |= SWT.BORDER;
			}
			if (BORDER_DASH.equals(s)) {
				swt |= SWT.BORDER_DASH;
			}
			if (BORDER_DOT.equals(s)) {
				swt |= SWT.BORDER_DOT;
			}
			if (BORDER_SOLID.equals(s)) {
				swt |= SWT.BORDER_SOLID;
			}
			if (BOTTOM.equals(s)) {
				swt |= SWT.BOTTOM;
			}
			if (CASCADE.equals(s)) {
				swt |= SWT.CASCADE;
			}
			if (CENTER.equals(s)) {
				swt |= SWT.CENTER;
			}
			if (CHECK.equals(s)) {
				swt |= SWT.CHECK;
			}
			if (DIALOG_TRIM.equals(s)) {
				swt |= SWT.DIALOG_TRIM;
			}
			if (DOWN.equals(s)) {
				swt |= SWT.DOWN;
			}
			if (DROP_DOWN.equals(s)) {
				swt |= SWT.DROP_DOWN;
			}
			if (FILL.equals(s)) {
				swt |= SWT.FILL;
			}
			if (FILL_EVEN_ODD.equals(s)) {
				swt |= SWT.FILL_EVEN_ODD;
			}
			if (FILL_WINDING.equals(s)) {
				swt |= SWT.FILL_WINDING;
			}
			if (FOREGROUND.equals(s)) {
				swt |= SWT.FOREGROUND;
			}
			if (FULL_SELECTION.equals(s)) {
				swt |= SWT.FULL_SELECTION;
			}
			if (H_SCROLL.equals(s)) {
				swt |= SWT.H_SCROLL;
			}
			if (HORIZONTAL.equals(s)) {
				swt |= SWT.HORIZONTAL;
			}
			if (LEAD.equals(s)) {
				swt |= SWT.LEAD;
			}
			if (LEFT.equals(s)) {
				swt |= SWT.LEFT;
			}
			if (LEFT_TO_RIGHT.equals(s)) {
				swt |= SWT.LEFT_TO_RIGHT;
			}
			if (LINE_CUSTOM.equals(s)) {
				swt |= SWT.LINE_CUSTOM;
			}
			if (LINE_DASH.equals(s)) {
				swt |= SWT.LINE_DASH;
			}
			if (LINE_DASHDOT.equals(s)) {
				swt |= SWT.LINE_DASHDOT;
			}
			if (LINE_DASHDOTDOT.equals(s)) {
				swt |= SWT.LINE_DASHDOTDOT;
			}
			if (LINE_DOT.equals(s)) {
				swt |= SWT.LINE_DOT;
			}
			if (LINE_SOLID.equals(s)) {
				swt |= SWT.LINE_SOLID;
			}
			if (MODELESS.equals(s)) {
				swt |= SWT.MODELESS;
			}
			if (MULTI.equals(s)) {
				swt |= SWT.MULTI;
			}
			if (NO.equals(s)) {
				swt |= SWT.NO;
			}
			if (NO_BACKGROUND.equals(s)) {
				swt |= SWT.NO_BACKGROUND;
			}
			if (NO_FOCUS.equals(s)) {
				swt |= SWT.NO_FOCUS;
			}
			if (NO_MERGE_PAINTS.equals(s)) {
				swt |= SWT.NO_MERGE_PAINTS;
			}
			if (NO_RADIO_GROUP.equals(s)) {
				swt |= SWT.NO_RADIO_GROUP;
			}
			if (NO_REDRAW_RESIZE.equals(s)) {
				swt |= SWT.NO_REDRAW_RESIZE;
			}
			if (NO_SCROLL.equals(s)) {
				swt |= SWT.NO_SCROLL;
			}
			if (NO_TRIM.equals(s)) {
				swt |= SWT.NO_TRIM;
			}
			if (NONE.equals(s)) {
				swt |= SWT.NONE;
			}
			if (NORMAL.equals(s)) {
				swt |= SWT.NORMAL;
			}
			if (ON_TOP.equals(s)) {
				swt |= SWT.ON_TOP;
			}
			if (OPEN.equals(s)) {
				swt |= SWT.OPEN;
			}
			if (POP_UP.equals(s)) {
				swt |= SWT.POP_UP;
			}
			if (PRIMARY_MODAL.equals(s)) {
				swt |= SWT.PRIMARY_MODAL;
			}
			if (PUSH.equals(s)) {
				swt |= SWT.PUSH;
			}
			if (RADIO.equals(s)) {
				swt |= SWT.RADIO;
			}
			if (READ_ONLY.equals(s)) {
				swt |= SWT.READ_ONLY;
			}
			if (RESIZE.equals(s)) {
				swt |= SWT.RESIZE;
			}
			if (RIGHT.equals(s)) {
				swt |= SWT.RIGHT;
			}
			if (RIGHT_TO_LEFT.equals(s)) {
				swt |= SWT.RIGHT_TO_LEFT;
			}
			if (SCROLL_LINE.equals(s)) {
				swt |= SWT.SCROLL_LINE;
			}
			if (SCROLL_LOCK.equals(s)) {
				swt |= SWT.SCROLL_LOCK;
			}
			if (SCROLL_PAGE.equals(s)) {
				swt |= SWT.SCROLL_PAGE;
			}
			if (SHADOW_ETCHED_IN.equals(s)) {
				swt |= SWT.SHADOW_ETCHED_IN;
			}
			if (SHADOW_ETCHED_OUT.equals(s)) {
				swt |= SWT.SHADOW_ETCHED_OUT;
			}
			if (SHADOW_IN.equals(s)) {
				swt |= SWT.SHADOW_IN;
			}
			if (SHADOW_NONE.equals(s)) {
				swt |= SWT.SHADOW_NONE;
			}
			if (SHADOW_OUT.equals(s)) {
				swt |= SWT.SHADOW_OUT;
			}
			if (SHELL_TRIM.equals(s)) {
				swt |= SWT.SHELL_TRIM;
			}
			if (SHORT.equals(s)) {
				swt |= SWT.SHORT;
			}
			if (SIMPLE.equals(s)) {
				swt |= SWT.SIMPLE;
			}
			if (SINGLE.equals(s)) {
				swt |= SWT.SINGLE;
			}
			if (SMOOTH.equals(s)) {
				swt |= SWT.SMOOTH;
			}
			if (TITLE.equals(s)) {
				swt |= SWT.TITLE;
			}
			if (TOGGLE.equals(s)) {
				swt |= SWT.TOGGLE;
			}
			if (TOP.equals(s)) {
				swt |= SWT.TOP;
			}
			if (UP.equals(s)) {
				swt |= SWT.UP;
			}
			if (V_SCROLL.equals(s)) {
				swt |= SWT.V_SCROLL;
			}
			if (VERTICAL.equals(s)) {
				swt |= SWT.VERTICAL;
			}
			if (WRAP.equals(s)) {
				swt |= SWT.WRAP;
			}
			if (YES.equals(s)) {
				swt |= SWT.YES;
			}
		}

		return swt;
	}

	private static void setupCommon(final ColumnViewer viewer, List<ColumnDescriptor> columnDescriptors,
			ISelectionChangedListener listener) {
		String[] columnProperties = new String[columnDescriptors.size()];
		for (int i = 0; i < columnDescriptors.size(); i++) {
			ColumnDescriptor columnDescriptor = columnDescriptors.get(i);
			columnProperties[i] = columnDescriptor.getColumnName();
		}
		viewer.setColumnProperties(columnProperties);
		viewer.addSelectionChangedListener(listener);
	}

	private static void setupSpecific(final CheckboxTableViewer viewer, List<ColumnDescriptor> columnDescriptors, boolean sortName) {
		for (int i = 0; i < columnDescriptors.size(); i++) {
			ColumnDescriptor columnDescriptor = columnDescriptors.get(i);
			TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn column = viewerColumn.getColumn();
			String name = columnDescriptor.getColumnName();
			column.setText(name);
			if (columnDescriptor.isWidthSpecified()) {
				column.setWidth(columnDescriptor.getWidth());
			}
			if (COLUMN_NAME.equals(name)) {
				column.addSelectionListener(getAttributeViewerSelectionAdapter(viewer));

			}
			if (COLUMN_VALUE.equals(columnDescriptor.getColumnName())) {
				viewerColumn.setEditingSupport(new AttributeViewerEditingSupport(viewer));
			}
		}
		viewer.setContentProvider(new TableDataContentProvider());
		viewer.setLabelProvider(new TableDataLabelProvider(columnDescriptors));
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				try {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					List<?> selected = selection.toList();
					for (Object o : selected) {
						AttributeViewerRowData row = (AttributeViewerRowData) o;
						boolean checked = row.isVisible();
						viewer.setChecked(row, !checked);
						row.setVisible(!checked);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				AttributeViewerRowData row = (AttributeViewerRowData) event.getElement();
				row.setVisible(event.getChecked());
			}
		});
		viewer.getTable().setHeaderVisible(true);
	}

	private static void setupSpecific(final CheckboxTreeViewer viewer, List<ColumnDescriptor> columnDescriptors, boolean sortName) {
		for (int i = 0; i < columnDescriptors.size(); i++) {
			ColumnDescriptor columnDescriptor = columnDescriptors.get(i);
			TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
			TreeColumn column = viewerColumn.getColumn();
			String name = columnDescriptor.getColumnName();
			column.setText(name);
			if (columnDescriptor.isWidthSpecified()) {
				column.setWidth(columnDescriptor.getWidth());
			}
			if (COLUMN_NAME.equals(name)) {
				column.addSelectionListener(getAttributeViewerSelectionAdapter(viewer));

			}
			if (COLUMN_VALUE.equals(columnDescriptor.getColumnName())) {
				viewerColumn.setEditingSupport(new AttributeViewerEditingSupport(viewer));
			}
		}
		viewer.setContentProvider(new TreeDataContentProvider());
		viewer.setLabelProvider(new TreeDataLabelProvider(columnDescriptors));
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				try {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					List<?> selected = selection.toList();
					for (Object o : selected) {
						AttributeViewerRowData row = (AttributeViewerRowData) o;
						boolean checked = row.isVisible();
						viewer.setChecked(row, !checked);
						row.setVisible(!checked);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				AttributeViewerRowData row = (AttributeViewerRowData) event.getElement();
				row.setVisible(event.getChecked());
			}
		});
		viewer.getTree().setHeaderVisible(true);
	}
}
