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

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.cell.AttributeViewerEditingSupport;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerCellData;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.providers.TableDataContentProvider;
import org.eclipse.ptp.rm.jaxb.ui.providers.TreeDataContentProvider;
import org.eclipse.ptp.rm.jaxb.ui.providers.ViewerDataLabelProvider;
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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
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

	public static TableColumn addTableColumn(final TableViewer viewer, final String columnName, Integer style, SelectionListener l) {
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

	public static Button createButton(Composite parent, Object data, String label, Integer type, SelectionListener listener) {
		Button button = new Button(parent, type);
		button.setText(label);
		if (data == null) {
			data = createGridData(DEFAULT, 1);
		}
		button.setLayoutData(data);
		if (null != listener) {
			button.addSelectionListener(listener);
		}
		return button;
	}

	public static Button createButton(Composite parent, Object data, String label, Integer type, SelectionListener listener,
			String bg, String fg) {
		Button b = createButton(parent, data, label, type, listener);
		if (bg != null) {
			b.setBackground(getColor(bg));
		}
		if (fg != null) {
			b.setBackground(getColor(fg));
		}
		return b;
	}

	public static Button createButton(Composite parent, String label, Integer type) {
		return createButton(parent, null, label, type, null);
	}

	public static Button createButton(Composite parent, String label, Integer type, SelectionListener listener) {
		return createButton(parent, null, label, type, listener);
	}

	public static Button createCheckButton(Composite parent, String label, SelectionListener listener) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT, listener);
	}

	public static Combo createCombo(Composite parent, Integer style, Object data, Object listener) {
		return createCombo(parent, style, data, new String[0], null, null, null, listener);
	}

	public static Combo createCombo(Composite parent, Integer style, Object data, String[] items, String initial, String label,
			String tooltip, Object listener) {
		if (label != null) {
			Label comboLabel = createLabel(parent, label, SWT.RIGHT, 1);
			if (tooltip != null) {
				comboLabel.setToolTipText(tooltip);
			}
		}
		Combo combo = new Combo(parent, style);
		if (items != null) {
			combo.setItems(items);
		}
		if (data == null) {
			data = createGridData(DEFAULT, 1);
		}
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

	public static Combo createCombo(Composite parent, Integer cols, String[] items, String initial, String labelString,
			String tooltip, Object listener) {
		GridData data = createGridData(GridData.FILL_HORIZONTAL, true, false, 100, DEFAULT, cols, DEFAULT);
		return createCombo(parent, SWT.BORDER, data, items, initial, labelString, tooltip, listener);
	}

	public static Composite createComposite(Composite parent, Integer columns) {
		GridLayout layout = createGridLayout(columns, false, DEFAULT, 1, DEFAULT, DEFAULT);
		return createComposite(parent, SWT.NONE, layout, null);
	}

	public static Composite createComposite(Composite parent, Integer style, Layout layout, Object data) {
		Composite composite = new Composite(parent, style);
		if (layout != null) {
			composite.setLayout(layout);
		}
		if (data == null) {
			data = createGridData(DEFAULT, 1);
		}
		composite.setData(data);
		return composite;
	}

	public static FillLayout createFillLayout(String type, Integer height, Integer width, Integer spacing) {
		FillLayout layout = new FillLayout();
		if (type != null) {
			layout.type = getStyle(type);
		}
		if (height != null) {
			layout.marginHeight = height;
		}
		if (width != null) {
			layout.marginWidth = width;
		}
		if (spacing != null) {
			layout.spacing = spacing;
		}
		return layout;
	}

	public static FormAttachment createFormAttachment(String align, Integer denominator, Integer numerator, Integer offset) {
		FormAttachment fa = new FormAttachment();
		if (align != null) {
			fa.alignment = getStyle(align);
		}
		if (denominator != null) {
			fa.denominator = denominator;
		}
		if (numerator != null) {
			fa.numerator = numerator;
		}
		if (offset != null) {
			fa.offset = offset;
		}
		return fa;
	}

	public static FormData createFormData(Integer height, Integer width, FormAttachment top, FormAttachment bottom,
			FormAttachment left, FormAttachment right) {
		FormData data = new FormData();
		if (height != null) {
			data.height = height;
		}
		if (width != null) {
			data.width = width;
		}
		if (top != null) {
			data.top = top;
		}
		if (bottom != null) {
			data.bottom = bottom;
		}
		if (left != null) {
			data.left = left;
		}
		if (right != null) {
			data.right = right;
		}
		return data;
	}

	public static FormLayout createFormLayout(Integer height, Integer width, Integer top, Integer bottom, Integer left,
			Integer right, Integer spacing) {
		FormLayout layout = new FormLayout();
		if (height != null) {
			layout.marginHeight = height;
		}
		if (width != null) {
			layout.marginWidth = width;
		}
		if (top != null) {
			layout.marginTop = top;
		}
		if (bottom != null) {
			layout.marginBottom = bottom;
		}
		if (left != null) {
			layout.marginLeft = left;
		}
		if (right != null) {
			layout.marginRight = right;
		}
		if (spacing != null) {
			layout.spacing = spacing;
		}
		return layout;
	}

	public static GridData createGridData(Integer style, Boolean grabH, Boolean grabV, Integer wHint, Integer hHint, Integer hSpan,
			Integer vSpan) {
		return createGridData(style, grabH, grabV, wHint, hHint, DEFAULT, DEFAULT, hSpan, vSpan, DEFAULT, DEFAULT);
	}

	public static GridData createGridData(Integer style, Boolean grabH, Boolean grabV, Integer wHint, Integer hHint, Integer minW,
			Integer minH, Integer hSpan, Integer vSpan, Integer hAlign, Integer vAlign) {
		GridData data = null;
		if (null != style) {
			if (style == DEFAULT) {
				data = new GridData();
			} else {
				data = new GridData(style);
			}
		} else {
			data = new GridData();
		}

		if (grabH != null) {
			data.grabExcessHorizontalSpace = grabH;
		}
		if (grabV != null) {
			data.grabExcessVerticalSpace = grabV;
		}
		if (null != wHint && wHint != DEFAULT) {
			data.widthHint = wHint;
		}
		if (null != hHint && hHint != DEFAULT) {
			data.heightHint = hHint;
		}
		if (null != minW && minW != DEFAULT) {
			data.minimumWidth = minW;
		}
		if (null != minH && minH != DEFAULT) {
			data.minimumHeight = minH;
		}
		if (null != hSpan && hSpan != DEFAULT) {
			data.horizontalSpan = hSpan;
		}
		if (null != vSpan && vSpan != DEFAULT) {
			data.verticalSpan = vSpan;
		}
		if (null != hAlign && hAlign != DEFAULT) {
			data.horizontalAlignment = hAlign;
		}
		if (null != vAlign && vAlign != DEFAULT) {
			data.verticalAlignment = vAlign;
		}
		return data;
	}

	public static GridData createGridData(Integer style, Integer cols) {
		return createGridData(style, false, false, DEFAULT, DEFAULT, cols, DEFAULT);
	}

	public static GridData createGridDataFill(Integer wHint, Integer hHint, Integer cols) {
		return createGridData(GridData.FILL_BOTH, true, true, wHint, hHint, cols, DEFAULT);
	}

	public static GridData createGridDataFillH(Integer cols) {
		return createGridData(GridData.FILL_HORIZONTAL, true, false, DEFAULT, DEFAULT, cols, DEFAULT);
	}

	public static GridLayout createGridLayout(Integer columns, Boolean equal) {
		return createGridLayout(columns, equal, DEFAULT, DEFAULT, DEFAULT, DEFAULT);
	}

	public static GridLayout createGridLayout(Integer columns, Boolean isEqual, Integer mh, Integer mw) {
		return createGridLayout(columns, isEqual, mh, mw, DEFAULT, DEFAULT);
	}

	public static GridLayout createGridLayout(Integer columns, Boolean isEqual, Integer hSpace, Integer vSpace, Integer mw,
			Integer mh) {
		return createGridLayout(columns, isEqual, hSpace, vSpace, mw, mh, DEFAULT, DEFAULT, DEFAULT, DEFAULT);
	}

	public static GridLayout createGridLayout(Integer columns, Boolean isEqual, Integer hSpace, Integer vSpace, Integer mw,
			Integer mh, Integer mL, Integer mR, Integer mT, Integer mB) {
		GridLayout gridLayout = new GridLayout();
		if (columns != null) {
			gridLayout.numColumns = columns;
		}
		if (isEqual != null) {
			gridLayout.makeColumnsEqualWidth = isEqual;
		}
		if (null != hSpace && hSpace != DEFAULT) {
			gridLayout.horizontalSpacing = hSpace;
		}
		if (null != vSpace && vSpace != DEFAULT) {
			gridLayout.verticalSpacing = vSpace;
		}
		if (null != mw && mw != DEFAULT) {
			gridLayout.marginWidth = mw;
		}
		if (null != mh && mh != DEFAULT) {
			gridLayout.marginHeight = mh;
		}
		if (null != mL && mL != DEFAULT) {
			gridLayout.marginLeft = mL;
		}
		if (null != mR && mR != DEFAULT) {
			gridLayout.marginRight = mR;
		}
		if (null != mT && mT != DEFAULT) {
			gridLayout.marginTop = mT;
		}
		if (null != mB && mB != DEFAULT) {
			gridLayout.marginBottom = mB;
		}
		return gridLayout;
	}

	public static Group createGroup(Composite parent, Integer style, Layout layout, Object data) {
		return createGroup(parent, style, layout, data, null);
	}

	public static Group createGroup(Composite parent, Integer style, Layout layout, Object data, String text) {
		Group group = new Group(parent, style);
		if (layout != null) {
			group.setLayout(layout);
		}
		if (data == null) {
			data = createGridData(DEFAULT, 1);
		}
		group.setLayoutData(data);
		if (text != null) {
			group.setText(text);
		}
		return group;
	}

	public static Label createLabel(Composite container, String text, Integer style, Integer colSpan) {
		GridData data = createGridData(DEFAULT, colSpan);
		return createLabel(container, text, style, data);
	}

	public static Label createLabel(Composite container, String text, Integer style, Object data) {
		Label label = new Label(container, style);
		if (text == null) {
			text = ZEROSTR;
		}
		label.setText(text.trim());
		if (data == null) {
			data = createGridData(DEFAULT, 1);
		}
		if (data != null) {
			label.setLayoutData(data);
		}

		return label;
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

	public static RowData createRowData(Integer height, Integer width, Boolean exclude) {
		RowData data = new RowData();
		if (height != null) {
			data.height = height;
		}
		if (width != null) {
			data.width = width;
		}
		if (exclude != null) {
			data.exclude = exclude;
		}
		return data;
	}

	public static RowLayout createRowLayout(Boolean center, Boolean fill, Boolean justify, Boolean pack, Integer height,
			Integer width, Integer top, Integer bottom, Integer left, Integer right, Integer spacing) {
		RowLayout layout = new RowLayout();
		if (center != null) {
			layout.center = center;
		}
		if (fill != null) {
			layout.fill = fill;
		}
		if (justify != null) {
			layout.justify = justify;
		}
		if (pack != null) {
			layout.pack = pack;
		}
		if (height != null) {
			layout.marginHeight = height;
		}
		if (width != null) {
			layout.marginWidth = width;
		}
		if (top != null) {
			layout.marginTop = top;
		}
		if (bottom != null) {
			layout.marginBottom = bottom;
		}
		if (left != null) {
			layout.marginLeft = left;
		}
		if (right != null) {
			layout.marginRight = right;
		}
		if (spacing != null) {
			layout.spacing = spacing;
		}
		return layout;
	}

	public static Spinner createSpinner(Composite parent, Object data, String label, Integer min, Integer max, Integer initial,
			ModifyListener listener) {
		if (label != null) {
			createLabel(parent, label, SWT.RIGHT, 1);
		}

		Spinner s = new Spinner(parent, SWT.NONE);
		if (max != null) {
			s.setMaximum(max);
		}
		if (min != null) {
			s.setMinimum(min);
		}
		if (initial != null) {
			s.setSelection(initial);
		}
		if (data == null) {
			data = createGridData(DEFAULT, 1);
		}
		s.setLayoutData(data);
		if (listener != null) {
			s.addModifyListener(listener);
		}
		return s;
	}

	public static TabItem createTabItem(TabFolder folder, Integer style, String text, String tooltip, Integer index) {
		TabItem item = new TabItem(folder, style, index);
		item.setText(text);
		item.setToolTipText(tooltip);
		return item;
	}

	public static Table createTable(Composite parent, Integer style, Integer cols, Integer wHint, Object data) {
		if (style == null) {
			style = SWT.None;
		}
		if (cols == null) {
			cols = 1;
		}
		Table t = new Table(parent, style);
		if (data == null) {
			data = createGridData(DEFAULT, cols);
		}
		t.setLayoutData(data);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		return t;
	}

	public static Table createTable(Composite parent, Integer style, Object data) {
		Integer cols = 1;
		Integer wHint = 50;
		if (data != null && data instanceof GridData) {
			GridData gd = (GridData) data;
			cols = gd.horizontalSpan;
			wHint = gd.widthHint;
		}
		return createTable(parent, style, cols, wHint, data);
	}

	public static Text createText(Composite parent, Integer options, Object data, Boolean readOnly, String initialContents) {
		return createText(parent, options, data, readOnly, initialContents, null, null);
	}

	public static Text createText(Composite parent, Integer options, Object data, Boolean readOnly, String initialContents,
			ModifyListener listener, Color color) {
		Text text = new Text(parent, options);
		if (data == null) {
			data = createGridData(DEFAULT, 1);
		}
		text.setLayoutData(data);
		if (readOnly != null) {
			text.setEditable(!readOnly);
		}
		if (color != null) {
			text.setBackground(color);
		}
		if (initialContents != null) {
			text.setText(initialContents);
		}
		if (listener != null) {
			text.addModifyListener(listener);
		}
		return text;
	}

	public static Text createText(Composite parent, String initialValue, Boolean fill, Boolean readOnly, ModifyListener listener,
			Color color) {
		GridData data = createGridData(fill ? GridData.FILL_HORIZONTAL : DEFAULT, true, false, DEFAULT, DEFAULT, DEFAULT, DEFAULT);
		return createText(parent, SWT.BORDER, data, readOnly, initialValue, listener, color);
	}

	public static Tree createTree(Composite parent, Integer style, Integer cols, Integer wHint, Object data) {
		Tree t = new Tree(parent, style);
		if (cols == null) {
			cols = 1;
		}
		if (data == null) {
			data = createGridData(DEFAULT, cols);
		}
		t.setLayoutData(data);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		return t;
	}

	public static Tree createTree(Composite parent, Integer style, Object data) {
		Integer wHint = null;
		Integer cols = null;
		if (data instanceof GridData) {
			GridData gd = (GridData) data;
			cols = gd.horizontalSpan;
			wHint = gd.widthHint;
		}
		return createTree(parent, style, cols, wHint, data);
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

	public static Color getColor(String color) {
		int swtColor = SWT.COLOR_BLACK;
		if (COLOR_BLACK.equals(color)) {
			swtColor = SWT.COLOR_BLACK;
		} else if (COLOR_WHITE.equals(color)) {
			swtColor = SWT.COLOR_WHITE;
		} else if (COLOR_RED.equals(color)) {
			swtColor = SWT.COLOR_RED;
		} else if (COLOR_DARK_RED.equals(color)) {
			swtColor = SWT.COLOR_DARK_RED;
		} else if (COLOR_GREEN.equals(color)) {
			swtColor = SWT.COLOR_GREEN;
		} else if (COLOR_DARK_GREEN.equals(color)) {
			swtColor = SWT.COLOR_DARK_GREEN;
		} else if (COLOR_YELLOW.equals(color)) {
			swtColor = SWT.COLOR_YELLOW;
		} else if (COLOR_DARK_YELLOW.equals(color)) {
			swtColor = SWT.COLOR_DARK_YELLOW;
		} else if (COLOR_BLUE.equals(color)) {
			swtColor = SWT.COLOR_BLUE;
		} else if (COLOR_DARK_BLUE.equals(color)) {
			swtColor = SWT.COLOR_DARK_BLUE;
		} else if (COLOR_MAGENTA.equals(color)) {
			swtColor = SWT.COLOR_MAGENTA;
		} else if (COLOR_DARK_MAGENTA.equals(color)) {
			swtColor = SWT.COLOR_DARK_MAGENTA;
		} else if (COLOR_CYAN.equals(color)) {
			swtColor = SWT.COLOR_CYAN;
		} else if (COLOR_DARK_CYAN.equals(color)) {
			swtColor = SWT.COLOR_DARK_CYAN;
		} else if (COLOR_GRAY.equals(color)) {
			swtColor = SWT.COLOR_GRAY;
		} else if (COLOR_DARK_GRAY.equals(color)) {
			swtColor = SWT.COLOR_DARK_GRAY;
		} else if (COLOR_INFO_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_INFO_BACKGROUND;
		} else if (COLOR_INFO_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_INFO_FOREGROUND;
		} else if (COLOR_LIST_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_LIST_BACKGROUND;
		} else if (COLOR_LIST_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_LIST_FOREGROUND;
		} else if (COLOR_LIST_SELECTION.equals(color)) {
			swtColor = SWT.COLOR_LIST_SELECTION;
		} else if (COLOR_LIST_SELECTION_TEXT.equals(color)) {
			swtColor = SWT.COLOR_LIST_SELECTION_TEXT;
		} else if (COLOR_TITLE_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_BACKGROUND;
		} else if (COLOR_TITLE_BACKGROUND_GRADIENT.equals(color)) {
			swtColor = SWT.COLOR_TITLE_BACKGROUND_GRADIENT;
		} else if (COLOR_TITLE_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_FOREGROUND;
		} else if (COLOR_TITLE_INACTIVE_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_INACTIVE_BACKGROUND;
		} else if (COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT.equals(color)) {
			swtColor = SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT;
		} else if (COLOR_TITLE_INACTIVE_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_INACTIVE_FOREGROUND;
		} else if (COLOR_WIDGET_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_BACKGROUND;
		} else if (COLOR_WIDGET_BORDER.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_BORDER;
		} else if (COLOR_WIDGET_DARK_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_DARK_SHADOW;
		} else if (COLOR_WIDGET_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_FOREGROUND;
		} else if (COLOR_WIDGET_HIGHLIGHT_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW;
		} else if (COLOR_WIDGET_LIGHT_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_LIGHT_SHADOW;
		} else if (COLOR_WIDGET_NORMAL_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_NORMAL_SHADOW;
		}
		/*
		 * don't have to deallocate, as these are system colors
		 */
		return Display.getDefault().getSystemColor(swtColor);
	}

	public static int getStyle(String style) {
		if (style == null || ZEROSTR.equals(style)) {
			return SWT.NONE;
		}
		return getStyle(style.split(OPENSQ + PIP + CLOSSQ));
	}

	public static void setupAttributeTable(final CheckboxTableViewer viewer, List<ColumnData> columnDescriptors,
			ISelectionChangedListener listener, boolean sortName, boolean tooltip, boolean header, boolean lines) {
		setupSpecific(viewer, columnDescriptors, sortName, header, lines);
		setupCommon(viewer, columnDescriptors, listener, tooltip);
	}

	public static void setupAttributeTree(final CheckboxTreeViewer viewer, List<ColumnData> columnDescriptors,
			ISelectionChangedListener listener, boolean sortName, boolean tooltip, boolean header, boolean lines) {
		setupSpecific(viewer, columnDescriptors, sortName, header, lines);
		setupCommon(viewer, columnDescriptors, listener, tooltip);
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

	private static ICheckStateListener getCheckStateListener(final ICheckable viewer) {
		return new ICheckStateListener() {
			public synchronized void checkStateChanged(CheckStateChangedEvent event) {
				try {
					Object target = event.getElement();
					boolean checked = viewer.getChecked(target);
					IStructuredSelection selection = (IStructuredSelection) ((Viewer) viewer).getSelection();
					List<?> selected = selection.toList();
					if (selected.isEmpty()) {
						if (target instanceof AttributeViewerCellData) {
							AttributeViewerCellData data = (AttributeViewerCellData) target;
							data.setSelected(checked);
						} else {
							viewer.setChecked(target, false);
						}
					} else {
						for (Object o : selected) {
							if (o instanceof AttributeViewerCellData) {
								AttributeViewerCellData data = (AttributeViewerCellData) o;
								data.setSelected(checked);
								viewer.setChecked(data, checked);
							} else {
								viewer.setChecked(o, false);
							}
						}
					}
				} catch (Throwable t) {
					JAXBUIPlugin.log(t);
				}
				WidgetActionUtils.refreshViewer((Viewer) viewer);
			}
		};
	}

	private static IDoubleClickListener getDoubleClickListener(final Viewer viewer) {
		return new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				try {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					Object first = selection.getFirstElement();
					if (first instanceof AttributeViewerCellData) {
						AttributeViewerCellData row = (AttributeViewerCellData) first;
						String tooltip = row.getTooltip();
						if (tooltip != null) {
							MessageDialog.openInformation(viewer.getControl().getShell(), Messages.Tooltip, tooltip);
						}
					}
				} catch (Throwable t) {
					JAXBUIPlugin.log(t);
				}
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
			if (FILL_BOTH.equals(s)) {
				swt |= GridData.FILL_BOTH;
			}
			if (FILL_EVEN_ODD.equals(s)) {
				swt |= SWT.FILL_EVEN_ODD;
			}
			if (FILL_HORIZONTAL.equals(s)) {
				swt |= GridData.FILL_HORIZONTAL;
			}
			if (FILL_VERTICAL.equals(s)) {
				swt |= GridData.FILL_VERTICAL;
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

	private static void setupCommon(final ColumnViewer viewer, List<ColumnData> columnData, ISelectionChangedListener listener,
			boolean tooltip) {
		String[] columnProperties = new String[columnData.size()];
		for (int i = 0; i < columnData.size(); i++) {
			ColumnData columnDescriptor = columnData.get(i);
			columnProperties[i] = columnDescriptor.getName();
		}
		viewer.setColumnProperties(columnProperties);
		if (tooltip) {
			viewer.addDoubleClickListener(getDoubleClickListener(viewer));
			viewer.getControl().setToolTipText(Messages.ViewerTooltipActivation);
		}
		if (listener != null) {
			viewer.addSelectionChangedListener(listener);
		}
		viewer.setLabelProvider(new ViewerDataLabelProvider(columnData));
		ICheckable checkable = (ICheckable) viewer;
		checkable.addCheckStateListener(getCheckStateListener(checkable));
	}

	private static void setupSpecific(final CheckboxTableViewer viewer, List<ColumnData> columnData, Boolean sortName,
			boolean header, boolean lines) {
		for (int i = 0; i < columnData.size(); i++) {
			ColumnData columnDescriptor = columnData.get(i);
			TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn column = viewerColumn.getColumn();
			String name = columnDescriptor.getName();
			column.setText(name);
			column.setMoveable(columnDescriptor.isMoveable());
			column.setResizable(columnDescriptor.isResizable());
			String tt = columnDescriptor.getTooltip();
			if (tt != null) {
				column.setToolTipText(tt);
			}
			if (UNDEFINED != columnDescriptor.getWidth()) {
				column.setWidth(columnDescriptor.getWidth());
			}
			if (null != columnDescriptor.getAlignment()) {
				column.setAlignment(getStyle(columnDescriptor.getAlignment()));
			}
			if (COLUMN_NAME.equals(name)) {
				if (sortName != null) {
					if (sortName) {
						column.addSelectionListener(getAttributeViewerSelectionAdapter(viewer));
					}
				}
			}
			if (COLUMN_VALUE.equals(columnDescriptor.getName())) {
				viewerColumn.setEditingSupport(new AttributeViewerEditingSupport(viewer, columnDescriptor));
			}
		}
		viewer.setContentProvider(new TableDataContentProvider());
		viewer.getTable().setHeaderVisible(header);
		viewer.getTable().setLinesVisible(lines);
	}

	private static void setupSpecific(final CheckboxTreeViewer viewer, List<ColumnData> columnData, Boolean sortName,
			boolean header, boolean lines) {
		for (int i = 0; i < columnData.size(); i++) {
			ColumnData columnDescriptor = columnData.get(i);
			TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
			TreeColumn column = viewerColumn.getColumn();
			String name = columnDescriptor.getName();
			column.setText(name);
			column.setMoveable(columnDescriptor.isMoveable());
			column.setResizable(columnDescriptor.isResizable());
			String tt = columnDescriptor.getTooltip();
			if (tt != null) {
				column.setToolTipText(tt);
			}
			if (UNDEFINED != columnDescriptor.getWidth()) {
				column.setWidth(columnDescriptor.getWidth());
			}
			if (null != columnDescriptor.getAlignment()) {
				column.setAlignment(getStyle(columnDescriptor.getAlignment()));
			}
			if (COLUMN_NAME.equals(name)) {
				if (sortName != null) {
					if (sortName) {
						column.addSelectionListener(getAttributeViewerSelectionAdapter(viewer));
					}
				}
			}
			if (COLUMN_VALUE.equals(columnDescriptor.getName())) {
				viewerColumn.setEditingSupport(new AttributeViewerEditingSupport(viewer, columnDescriptor));
			}
		}
		viewer.setContentProvider(new TreeDataContentProvider());
		viewer.getTree().setHeaderVisible(header);
		viewer.getTree().setLinesVisible(lines);
	}
}
