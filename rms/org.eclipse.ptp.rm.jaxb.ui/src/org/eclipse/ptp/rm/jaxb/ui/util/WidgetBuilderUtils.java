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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnDataType;
import org.eclipse.ptp.rm.jaxb.core.data.FontType;
import org.eclipse.ptp.rm.jaxb.ui.JAXBRMUIConstants;
import org.eclipse.ptp.rm.jaxb.ui.cell.AttributeViewerEditingSupport;
import org.eclipse.ptp.rm.jaxb.ui.providers.TableDataContentProvider;
import org.eclipse.ptp.rm.jaxb.ui.providers.TreeDataContentProvider;
import org.eclipse.ptp.rm.jaxb.ui.providers.ViewerDataCellLabelProvider;
import org.eclipse.ptp.rm.jaxb.ui.sorters.AttributeViewerSorter;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Convenience methods for constructing and configuring widgets.
 * 
 * @author arossi
 * 
 */
public class WidgetBuilderUtils {

	private static final FontRegistry fonts = new FontRegistry();

	private WidgetBuilderUtils() {
	}

	public static TableColumn addTableColumn(final TableViewer viewer, String columnName, int style, int width,
			final SelectionAdapter s) {
		Table t = viewer.getTable();
		TableColumn c = new TableColumn(t, style);
		c.setText(columnName);
		c.setWidth(width);
		c.addSelectionListener(s);
		return c;
	}

	/**
	 * Tries to set monospace text on text area.
	 * 
	 * @param text
	 *            of dialog or tab
	 */
	public static void applyMonospace(Text text) {
		// Courier exists on Mac, Linux, Windows ...
		FontType fd = new FontType();
		fd.setName(JAXBRMUIConstants.COURIER);
		fd.setSize(14);
		fd.setStyle(JAXBRMUIConstants.NORMAL);
		Font font = getFont(fd);
		if (font != null) {
			text.setFont(font);
			Dialog.applyDialogFont(text);
		}
	}

	/**
	 * @param parent
	 * @param layoutData
	 * @param label
	 * @param style
	 * @param listener
	 * @return button
	 */
	public static Button createButton(Composite parent, Object layoutData, String label, Integer style, SelectionListener listener) {
		Button button = new Button(parent, style);
		if (label != null) {
			button.setText(label);
		}
		if (layoutData == null) {
			layoutData = createGridData(JAXBRMUIConstants.DEFAULT, 1);
		}
		button.setLayoutData(layoutData);
		if (null != listener) {
			button.addSelectionListener(listener);
		}
		return button;
	}

	/**
	 * @param parent
	 * @param label
	 * @param style
	 * @return button
	 */
	public static Button createButton(Composite parent, String label, Integer style) {
		return createButton(parent, null, label, style, null);
	}

	/**
	 * @param parent
	 * @param label
	 * @param style
	 * @param listener
	 * @return button
	 */
	public static Button createButton(Composite parent, String label, Integer style, SelectionListener listener) {
		return createButton(parent, null, label, style, listener);
	}

	/**
	 * s * @param parent
	 * 
	 * @param label
	 * @param listener
	 * @return check button
	 */
	public static Button createCheckButton(Composite parent, String label, SelectionListener listener) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT, listener);
	}

	/**
	 * @param parent
	 * @param style
	 * @param data
	 * @param items
	 * @param initialValue
	 * @param label
	 * @param tooltip
	 * @param listener
	 * @return combo
	 */
	public static Combo createCombo(Composite parent, Integer style, Object data, String[] items, String initialValue,
			String label, String tooltip, Object listener) {
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
			data = createGridData(JAXBRMUIConstants.DEFAULT, 1);
		}
		combo.setLayoutData(data);
		if (initialValue != null) {
			combo.setText(initialValue);
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

	/**
	 * @param parent
	 * @param columns
	 * @return composite
	 */
	public static Composite createComposite(Composite parent, Integer columns) {
		GridLayout layout = createGridLayout(columns, false, JAXBRMUIConstants.DEFAULT, 1, JAXBRMUIConstants.DEFAULT,
				JAXBRMUIConstants.DEFAULT);
		return createComposite(parent, SWT.NONE, layout, null);
	}

	/**
	 * @param parent
	 * @param style
	 * @param layout
	 * @param layoutData
	 * @return composite
	 */
	public static Composite createComposite(Composite parent, Integer style, Layout layout, Object layoutData) {
		Composite composite = new Composite(parent, style);
		if (layout != null) {
			composite.setLayout(layout);
		}
		if (layoutData == null) {
			layoutData = createGridData(JAXBRMUIConstants.DEFAULT, 1);
		}
		composite.setData(layoutData);
		return composite;
	}

	/**
	 * @param type
	 * @param height
	 * @param width
	 * @param spacing
	 * @return fill layout
	 */
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

	/**
	 * @param align
	 * @param denominator
	 * @param numerator
	 * @param offset
	 * @return font attachment
	 */
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

	/**
	 * @param height
	 * @param width
	 * @param top
	 * @param bottom
	 * @param left
	 * @param right
	 * @return form data
	 */
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

	/**
	 * @param height
	 * @param width
	 * @param top
	 * @param bottom
	 * @param left
	 * @param right
	 * @param spacing
	 * @return form layout
	 */
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

	/**
	 * @param style
	 * @param grabExcessHorizontal
	 * @param grabExcessVertical
	 * @param widthHint
	 * @param heightHint
	 * @param horizontalSpan
	 * @param verticalSpan
	 * @return grid data
	 */
	public static GridData createGridData(Integer style, Boolean grabExcessHorizontal, Boolean grabExcessVertical,
			Integer widthHint, Integer heightHint, Integer horizontalSpan, Integer verticalSpan) {
		return createGridData(style, grabExcessHorizontal, grabExcessVertical, widthHint, heightHint, JAXBRMUIConstants.DEFAULT,
				JAXBRMUIConstants.DEFAULT, horizontalSpan, verticalSpan, JAXBRMUIConstants.DEFAULT, JAXBRMUIConstants.DEFAULT,
				JAXBRMUIConstants.DEFAULT, JAXBRMUIConstants.DEFAULT);
	}

	/**
	 * @param style
	 * @param grabExcessHorizontal
	 * @param grabExcessVertical
	 * @param widthHint
	 * @param heightHint
	 * @param minimumWidth
	 * @param minimumHeight
	 * @param horizontalSpan
	 * @param verticalSpan
	 * @param horizonalAlign
	 * @param verticalAlign
	 * @param horizontalIndent
	 * @param verticalIndent
	 * @return grid data
	 */
	public static GridData createGridData(Integer style, Boolean grabExcessHorizontal, Boolean grabExcessVertical,
			Integer widthHint, Integer heightHint, Integer minimumWidth, Integer minimumHeight, Integer horizontalSpan,
			Integer verticalSpan, Integer horizonalAlign, Integer verticalAlign, Integer horizontalIndent, Integer verticalIndent) {
		GridData data = null;
		if (null != style) {
			if (style == JAXBRMUIConstants.DEFAULT) {
				data = new GridData();
			} else {
				data = new GridData(style);
			}
		} else {
			data = new GridData();
		}

		if (grabExcessHorizontal != null) {
			data.grabExcessHorizontalSpace = grabExcessHorizontal;
		}
		if (grabExcessVertical != null) {
			data.grabExcessVerticalSpace = grabExcessVertical;
		}
		if (null != widthHint && widthHint != JAXBRMUIConstants.DEFAULT) {
			data.widthHint = widthHint;
		}
		if (null != heightHint && heightHint != JAXBRMUIConstants.DEFAULT) {
			data.heightHint = heightHint;
		}
		if (null != minimumWidth && minimumWidth != JAXBRMUIConstants.DEFAULT) {
			data.minimumWidth = minimumWidth;
		}
		if (null != minimumHeight && minimumHeight != JAXBRMUIConstants.DEFAULT) {
			data.minimumHeight = minimumHeight;
		}
		if (null != horizontalSpan && horizontalSpan != JAXBRMUIConstants.DEFAULT) {
			data.horizontalSpan = horizontalSpan;
		}
		if (null != verticalSpan && verticalSpan != JAXBRMUIConstants.DEFAULT) {
			data.verticalSpan = verticalSpan;
		}
		if (null != horizonalAlign && horizonalAlign != JAXBRMUIConstants.DEFAULT) {
			data.horizontalAlignment = horizonalAlign;
		}
		if (null != verticalAlign && verticalAlign != JAXBRMUIConstants.DEFAULT) {
			data.verticalAlignment = verticalAlign;
		}
		if (null != horizontalIndent && horizontalIndent != JAXBRMUIConstants.DEFAULT) {
			data.horizontalIndent = horizontalIndent;
		}
		if (null != verticalIndent && verticalIndent != JAXBRMUIConstants.DEFAULT) {
			data.verticalIndent = verticalIndent;
		}
		return data;
	}

	/**
	 * @param style
	 * @param cols
	 * @return grid data
	 */
	public static GridData createGridData(Integer style, Integer cols) {
		return createGridData(style, false, false, JAXBRMUIConstants.DEFAULT, JAXBRMUIConstants.DEFAULT, cols,
				JAXBRMUIConstants.DEFAULT);
	}

	/**
	 * Sets style to GridData.FILL_BOTH, grabExcessHorizontal and
	 * grabExcessVertical both to true.
	 * 
	 * @param widthHint
	 * @param heightHint
	 * @param cols
	 * @return grid data
	 */
	public static GridData createGridDataFill(Integer widthHint, Integer heightHint, Integer cols) {
		return createGridData(GridData.FILL_BOTH, true, true, widthHint, heightHint, cols, JAXBRMUIConstants.DEFAULT);
	}

	/**
	 * Sets style to GridData.FILL_BOTH, grabExcessHorizontal to true.
	 * 
	 * @param cols
	 * @return grid data
	 */
	public static GridData createGridDataFillH(Integer cols) {
		return createGridData(GridData.FILL_HORIZONTAL, true, false, JAXBRMUIConstants.DEFAULT, JAXBRMUIConstants.DEFAULT, cols,
				JAXBRMUIConstants.DEFAULT);
	}

	/**
	 * @param columns
	 * @param makeColumnsEqualWidth
	 * @return grid layout
	 */
	public static GridLayout createGridLayout(Integer columns, Boolean makeColumnsEqualWidth) {
		return createGridLayout(columns, makeColumnsEqualWidth, JAXBRMUIConstants.DEFAULT, JAXBRMUIConstants.DEFAULT,
				JAXBRMUIConstants.DEFAULT, JAXBRMUIConstants.DEFAULT);
	}

	/**
	 * @param columns
	 * @param makeColumnsEqualWidth
	 * @param horizontalSpacing
	 * @param verticalSpacing
	 * @param marginWidth
	 * @param marginHeight
	 * @return
	 */
	public static GridLayout createGridLayout(Integer columns, Boolean makeColumnsEqualWidth, Integer horizontalSpacing,
			Integer verticalSpacing, Integer marginWidth, Integer marginHeight) {
		return createGridLayout(columns, makeColumnsEqualWidth, horizontalSpacing, verticalSpacing, marginWidth, marginHeight,
				JAXBRMUIConstants.DEFAULT, JAXBRMUIConstants.DEFAULT, JAXBRMUIConstants.DEFAULT, JAXBRMUIConstants.DEFAULT);
	}

	/**
	 * @param columns
	 * @param makeColumnsEqualWidth
	 * @param horizontalSpacing
	 * @param verticalSpacing
	 * @param marginWidth
	 * @param marginHeight
	 * @param marginLeft
	 * @param marginRight
	 * @param marginTop
	 * @param marginBottom
	 * @return grid layout
	 */
	public static GridLayout createGridLayout(Integer columns, Boolean makeColumnsEqualWidth, Integer horizontalSpacing,
			Integer verticalSpacing, Integer marginWidth, Integer marginHeight, Integer marginLeft, Integer marginRight,
			Integer marginTop, Integer marginBottom) {
		GridLayout gridLayout = new GridLayout();
		if (columns != null) {
			gridLayout.numColumns = columns;
		}
		if (makeColumnsEqualWidth != null) {
			gridLayout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		}
		if (null != horizontalSpacing && horizontalSpacing != JAXBRMUIConstants.DEFAULT) {
			gridLayout.horizontalSpacing = horizontalSpacing;
		}
		if (null != verticalSpacing && verticalSpacing != JAXBRMUIConstants.DEFAULT) {
			gridLayout.verticalSpacing = verticalSpacing;
		}
		if (null != marginWidth && marginWidth != JAXBRMUIConstants.DEFAULT) {
			gridLayout.marginWidth = marginWidth;
		}
		if (null != marginHeight && marginHeight != JAXBRMUIConstants.DEFAULT) {
			gridLayout.marginHeight = marginHeight;
		}
		if (null != marginLeft && marginLeft != JAXBRMUIConstants.DEFAULT) {
			gridLayout.marginLeft = marginLeft;
		}
		if (null != marginRight && marginRight != JAXBRMUIConstants.DEFAULT) {
			gridLayout.marginRight = marginRight;
		}
		if (null != marginTop && marginTop != JAXBRMUIConstants.DEFAULT) {
			gridLayout.marginTop = marginTop;
		}
		if (null != marginBottom && marginBottom != JAXBRMUIConstants.DEFAULT) {
			gridLayout.marginBottom = marginBottom;
		}
		return gridLayout;
	}

	/**
	 * @param parent
	 * @param style
	 * @param layout
	 * @param layoutData
	 * @return group
	 */
	public static Group createGroup(Composite parent, Integer style, Layout layout, Object layoutData) {
		return createGroup(parent, style, layout, layoutData, null);
	}

	/**
	 * @param parent
	 * @param style
	 * @param layout
	 * @param layoutData
	 * @param text
	 *            title label, if any
	 * @return group
	 */
	public static Group createGroup(Composite parent, Integer style, Layout layout, Object layoutData, String text) {
		Group group = new Group(parent, style);
		if (layout != null) {
			group.setLayout(layout);
		}
		if (layoutData == null) {
			layoutData = createGridData(JAXBRMUIConstants.DEFAULT, 1);
		}
		group.setLayoutData(layoutData);
		if (text != null) {
			group.setText(text);
		}
		return group;
	}

	/**
	 * @param container
	 * @param text
	 * @param style
	 * @param columnSpan
	 * @return label
	 */
	public static Label createLabel(Composite container, String text, Integer style, Integer columnSpan) {
		GridData data = createGridData(JAXBRMUIConstants.DEFAULT, columnSpan);
		return createLabel(container, text, style, data);
	}

	/**
	 * @param container
	 * @param text
	 * @param style
	 * @param layoutData
	 * @return label
	 */
	public static Label createLabel(Composite container, String text, Integer style, Object layoutData) {
		Label label = new Label(container, style);
		if (text == null) {
			text = JAXBRMUIConstants.ZEROSTR;
		}
		label.setText(text.trim());
		if (layoutData == null) {
			layoutData = createGridData(JAXBRMUIConstants.DEFAULT, 1);
		}
		if (layoutData != null) {
			label.setLayoutData(layoutData);
		}

		return label;
	}

	/**
	 * Sets the layout data to grid with style GridData.FILL_HORIZONTAL.
	 * 
	 * @param parent
	 * @param label
	 * @param listener
	 * @return push button
	 */
	public static Button createPushButton(Composite parent, String label, SelectionListener listener) {
		Button button = SWTUtil.createPushButton(parent, label, null);
		GridData data = createGridData(GridData.FILL_HORIZONTAL, 1);
		button.setLayoutData(data);
		if (null != listener) {
			button.addSelectionListener(listener);
		}
		return button;
	}

	/**
	 * @param parent
	 * @param label
	 * @param initialValue
	 * @param listener
	 * @return radio button
	 */
	public static Button createRadioButton(Composite parent, String label, String initialValue, SelectionListener listener) {
		Button button = createButton(parent, label, SWT.RADIO | SWT.LEFT, listener);
		button.setData((null == initialValue) ? label : initialValue);
		return button;
	}

	/**
	 * @param height
	 * @param width
	 * @param exclude
	 * @return row data
	 */
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

	/**
	 * @param center
	 * @param fill
	 * @param justify
	 * @param pack
	 * @param marginHeight
	 * @param marginWidth
	 * @param marginTop
	 * @param marginBottom
	 * @param marginLeft
	 * @param marginRight
	 * @param spacing
	 * @return row layout
	 */
	public static RowLayout createRowLayout(Boolean center, Boolean fill, Boolean justify, Boolean pack, Integer marginHeight,
			Integer marginWidth, Integer marginTop, Integer marginBottom, Integer marginLeft, Integer marginRight, Integer spacing) {
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
		if (marginHeight != null) {
			layout.marginHeight = marginHeight;
		}
		if (marginWidth != null) {
			layout.marginWidth = marginWidth;
		}
		if (marginTop != null) {
			layout.marginTop = marginTop;
		}
		if (marginBottom != null) {
			layout.marginBottom = marginBottom;
		}
		if (marginLeft != null) {
			layout.marginLeft = marginLeft;
		}
		if (marginRight != null) {
			layout.marginRight = marginRight;
		}
		if (spacing != null) {
			layout.spacing = spacing;
		}
		return layout;
	}

	/**
	 * @param parent
	 * @param layoutData
	 * @param label
	 * @param minimum
	 * @param maximum
	 * @param initialValue
	 * @param listener
	 * @return spinner
	 */
	public static Spinner createSpinner(Composite parent, Object layoutData, String label, Integer minimum, Integer maximum,
			Integer initialValue, ModifyListener listener) {
		if (label != null) {
			createLabel(parent, label, SWT.RIGHT, 1);
		}

		Spinner s = new Spinner(parent, SWT.NONE);
		if (maximum != null) {
			s.setMaximum(maximum);
		}
		if (minimum != null) {
			s.setMinimum(minimum);
		}
		if (initialValue != null) {
			s.setSelection(initialValue);
		}
		if (layoutData == null) {
			layoutData = createGridData(JAXBRMUIConstants.DEFAULT, 1);
		}
		s.setLayoutData(layoutData);
		if (listener != null) {
			s.addModifyListener(listener);
		}
		return s;
	}

	/**
	 * @param folder
	 * @param style
	 * @param text
	 * @param tooltip
	 * @param index
	 * @return tab item
	 */
	public static CTabItem createTabItem(CTabFolder folder, Integer style, String text, String tooltip, Integer index) {
		CTabItem item = new CTabItem(folder, style, index);
		item.setText(text);
		item.setToolTipText(tooltip);
		return item;
	}

	/**
	 * @param parent
	 * @param style
	 * @param layoutData
	 * @return table
	 */
	public static Table createTable(Composite parent, Integer style, Object layoutData) {
		Integer cols = null;
		if (layoutData != null) {
			if (layoutData instanceof GridData) {
				GridData gd = (GridData) layoutData;
				cols = gd.horizontalSpan;
			}
		} else {
			layoutData = createGridData(JAXBRMUIConstants.DEFAULT, cols);
		}
		if (style == null) {
			style = SWT.None;
		}
		if (cols == null) {
			cols = 1;
		}
		Table t = new Table(parent, style);
		t.setLayoutData(layoutData);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		return t;
	}

	/**
	 * @param parent
	 * @param style
	 * @param layoutData
	 * @param readOnly
	 * @param initialValue
	 * @return text
	 */
	public static Text createText(Composite parent, Integer style, Object layoutData, Boolean readOnly, String initialValue) {
		return createText(parent, style, layoutData, readOnly, initialValue, null, null);
	}

	/**
	 * @param parent
	 * @param style
	 * @param layoutData
	 * @param readOnly
	 * @param initialValue
	 * @param listener
	 * @param color
	 * @return
	 */
	public static Text createText(Composite parent, Integer style, Object layoutData, Boolean readOnly, String initialValue,
			ModifyListener listener, Color color) {
		Text text = new Text(parent, style);
		if (layoutData == null) {
			layoutData = createGridData(JAXBRMUIConstants.DEFAULT, 1);
		}
		text.setLayoutData(layoutData);
		if (readOnly != null) {
			text.setEditable(!readOnly);
		}
		if (color != null) {
			text.setBackground(color);
		}
		if (initialValue != null) {
			text.setText(initialValue);
		}
		if (listener != null) {
			text.addModifyListener(listener);
		}
		return text;
	}

	/**
	 * @param parent
	 * @param style
	 * @param layoutData
	 * @return tree
	 */
	public static Tree createTree(Composite parent, Integer style, Object layoutData) {
		Integer cols = null;
		if (layoutData != null) {
			if (layoutData instanceof GridData) {
				GridData gd = (GridData) layoutData;
				cols = gd.horizontalSpan;
			}
		} else {
			layoutData = createGridData(JAXBRMUIConstants.DEFAULT, cols);
		}
		if (style == null) {
			style = SWT.None;
		}
		if (cols == null) {
			cols = 1;
		}
		Tree t = new Tree(parent, style);
		t.setLayoutData(layoutData);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		return t;
	}

	/**
	 * Translates string representation of SWT color into int code.
	 * 
	 * @param color
	 *            string representation
	 * @return SWT code
	 */
	public static Color getColor(String color) {
		int swtColor = SWT.COLOR_BLACK;
		if (JAXBRMUIConstants.COLOR_BLACK.equals(color)) {
			swtColor = SWT.COLOR_BLACK;
		} else if (JAXBRMUIConstants.COLOR_WHITE.equals(color)) {
			swtColor = SWT.COLOR_WHITE;
		} else if (JAXBRMUIConstants.COLOR_RED.equals(color)) {
			swtColor = SWT.COLOR_RED;
		} else if (JAXBRMUIConstants.COLOR_DARK_RED.equals(color)) {
			swtColor = SWT.COLOR_DARK_RED;
		} else if (JAXBRMUIConstants.COLOR_GREEN.equals(color)) {
			swtColor = SWT.COLOR_GREEN;
		} else if (JAXBRMUIConstants.COLOR_DARK_GREEN.equals(color)) {
			swtColor = SWT.COLOR_DARK_GREEN;
		} else if (JAXBRMUIConstants.COLOR_YELLOW.equals(color)) {
			swtColor = SWT.COLOR_YELLOW;
		} else if (JAXBRMUIConstants.COLOR_DARK_YELLOW.equals(color)) {
			swtColor = SWT.COLOR_DARK_YELLOW;
		} else if (JAXBRMUIConstants.COLOR_BLUE.equals(color)) {
			swtColor = SWT.COLOR_BLUE;
		} else if (JAXBRMUIConstants.COLOR_DARK_BLUE.equals(color)) {
			swtColor = SWT.COLOR_DARK_BLUE;
		} else if (JAXBRMUIConstants.COLOR_MAGENTA.equals(color)) {
			swtColor = SWT.COLOR_MAGENTA;
		} else if (JAXBRMUIConstants.COLOR_DARK_MAGENTA.equals(color)) {
			swtColor = SWT.COLOR_DARK_MAGENTA;
		} else if (JAXBRMUIConstants.COLOR_CYAN.equals(color)) {
			swtColor = SWT.COLOR_CYAN;
		} else if (JAXBRMUIConstants.COLOR_DARK_CYAN.equals(color)) {
			swtColor = SWT.COLOR_DARK_CYAN;
		} else if (JAXBRMUIConstants.COLOR_GRAY.equals(color)) {
			swtColor = SWT.COLOR_GRAY;
		} else if (JAXBRMUIConstants.COLOR_DARK_GRAY.equals(color)) {
			swtColor = SWT.COLOR_DARK_GRAY;
		} else if (JAXBRMUIConstants.COLOR_INFO_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_INFO_BACKGROUND;
		} else if (JAXBRMUIConstants.COLOR_INFO_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_INFO_FOREGROUND;
		} else if (JAXBRMUIConstants.COLOR_LIST_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_LIST_BACKGROUND;
		} else if (JAXBRMUIConstants.COLOR_LIST_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_LIST_FOREGROUND;
		} else if (JAXBRMUIConstants.COLOR_LIST_SELECTION.equals(color)) {
			swtColor = SWT.COLOR_LIST_SELECTION;
		} else if (JAXBRMUIConstants.COLOR_LIST_SELECTION_TEXT.equals(color)) {
			swtColor = SWT.COLOR_LIST_SELECTION_TEXT;
		} else if (JAXBRMUIConstants.COLOR_TITLE_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_BACKGROUND;
		} else if (JAXBRMUIConstants.COLOR_TITLE_BACKGROUND_GRADIENT.equals(color)) {
			swtColor = SWT.COLOR_TITLE_BACKGROUND_GRADIENT;
		} else if (JAXBRMUIConstants.COLOR_TITLE_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_FOREGROUND;
		} else if (JAXBRMUIConstants.COLOR_TITLE_INACTIVE_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_INACTIVE_BACKGROUND;
		} else if (JAXBRMUIConstants.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT.equals(color)) {
			swtColor = SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT;
		} else if (JAXBRMUIConstants.COLOR_TITLE_INACTIVE_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_INACTIVE_FOREGROUND;
		} else if (JAXBRMUIConstants.COLOR_WIDGET_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_BACKGROUND;
		} else if (JAXBRMUIConstants.COLOR_WIDGET_BORDER.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_BORDER;
		} else if (JAXBRMUIConstants.COLOR_WIDGET_DARK_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_DARK_SHADOW;
		} else if (JAXBRMUIConstants.COLOR_WIDGET_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_FOREGROUND;
		} else if (JAXBRMUIConstants.COLOR_WIDGET_HIGHLIGHT_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW;
		} else if (JAXBRMUIConstants.COLOR_WIDGET_LIGHT_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_LIGHT_SHADOW;
		} else if (JAXBRMUIConstants.COLOR_WIDGET_NORMAL_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_NORMAL_SHADOW;
		}
		/*
		 * don't have to deallocate, as these are system colors
		 */
		return Display.getDefault().getSystemColor(swtColor);
	}

	/**
	 * Uses font registry.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.data.FontDescriptor
	 * @see org.eclipse.jface.resource.FontRegistry
	 * @see org.eclipse.swt.graphics.FontData
	 * 
	 * @param fontDescriptor
	 *            JAXB data element for font
	 * @return font
	 */
	public static Font getFont(FontType fontDescriptor) {
		String key = fontDescriptor.getName() + fontDescriptor.getSize() + fontDescriptor.getStyle();
		FontData[] data = fonts.getFontData(key);
		if (data == null) {
			data = new FontData[] { new FontData(fontDescriptor.getName(), fontDescriptor.getSize(),
					getStyle(fontDescriptor.getStyle())) };
			fonts.put(key, data);
		}
		return fonts.get(key);
	}

	/**
	 * Translates the string representation of OR'd style values into SWT int
	 * code.
	 * 
	 * Calls {@link #getStyle(String[])}.
	 * 
	 * @param style
	 *            string representation
	 * @return SWT code
	 */
	public static int getStyle(String style) {
		if (style == null || JAXBRMUIConstants.ZEROSTR.equals(style)) {
			return SWT.NONE;
		}
		return getStyle(style.split(JAXBRMUIConstants.REGPIP));
	}

	/**
	 * For consistency in treating <code>null</code> or undefined defaults on
	 * loading, this method ensures the first element of the combo is a
	 * JAXBRMUIConstants.ZEROSTR. It also removes blank items.
	 * 
	 * @param items
	 * @return adjusted array of combo items
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String[] normalizeComboItems(String[] items) {
		List<String> list = new ArrayList(Arrays.asList(items));
		for (Iterator<String> s = list.iterator(); s.hasNext();) {
			String item = s.next().trim();
			if (JAXBRMUIConstants.ZEROSTR.equals(item) || JAXBRMUIConstants.LINE_SEP.equals(item)) {
				s.remove();
			}
		}
		list.add(0, JAXBRMUIConstants.ZEROSTR);
		return list.toArray(new String[0]);
	}

	/**
	 * For preprocessing tooltip text from messed up XML text elements.
	 * 
	 * @param text
	 *            to be adjusted
	 * @return text without internal tabs or line breaks.
	 */
	public static String removeTabOrLineBreak(String text) {
		if (text == null) {
			return null;
		}
		if (JAXBRMUIConstants.ZEROSTR.equals(text)) {
			return JAXBRMUIConstants.ZEROSTR;
		}
		StringBuffer newLine = new StringBuffer();
		int strln = text.length();
		char lastChar = 0;
		for (int i = 0; i < strln; i++) {
			char c = text.charAt(i);
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
				if (lastChar != JAXBRMUIConstants.SP.charAt(0)) {
					newLine.append(JAXBRMUIConstants.SP);
					lastChar = JAXBRMUIConstants.SP.charAt(0);
				}
				break;
			default:
				newLine.append(c);
				lastChar = c;
			}
		}
		return newLine.toString();
	}

	/**
	 * Configures the CheckboxTableViewer. Calls
	 * {@link #setupCommon(ColumnViewer, List, ISelectionChangedListener, boolean)}
	 * and
	 * {@link #setupSpecific(CheckboxTableViewer, List, Boolean, boolean, boolean)}
	 * .
	 * 
	 * @param viewer
	 * @param columnData
	 *            JAXB data element describing viewer columns
	 * @param listener
	 * @param sortByName
	 * @param tooltip
	 * @param headerVisible
	 * @param linesVisible
	 */
	public static void setupAttributeTable(final CheckboxTableViewer viewer, List<ColumnDataType> columnData,
			ISelectionChangedListener listener, boolean sortByName, boolean tooltip, boolean headerVisible, boolean linesVisible) {
		setupSpecific(viewer, columnData, sortByName, headerVisible, linesVisible);
		setupCommon(viewer, columnData, listener, tooltip);
	}

	/**
	 * Configures the CheckboxTreeViewer. Calls
	 * {@link #setupCommon(ColumnViewer, List, ISelectionChangedListener, boolean)}
	 * and
	 * {@link #setupSpecific(CheckboxTreeViewer, List, Boolean, boolean, boolean)}
	 * .
	 * 
	 * @param viewer
	 * @param columnData
	 *            JAXB data element describing viewer columns
	 * @param listener
	 * @param sortByName
	 * @param tooltip
	 * @param headerVisible
	 * @param linesVisible
	 */
	public static void setupAttributeTree(final CheckboxTreeViewer viewer, List<ColumnDataType> columnData,
			ISelectionChangedListener listener, boolean sortByName, boolean tooltip, boolean headerVisible, boolean linesVisible) {
		setupSpecific(viewer, columnData, sortByName, headerVisible, linesVisible);
		setupCommon(viewer, columnData, listener, tooltip);
	}

	/**
	 * Creates adapter with sorter for viewer.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.sorters.AttributeViewerSorter
	 * 
	 * @param viewer
	 * @return adapter for the name column
	 */
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

	/**
	 * Translates each array item into the integer code, using a cumulative
	 * logical OR.
	 * 
	 * @param style
	 *            array of string from split on '|'
	 * @return SWT style code
	 */
	private static int getStyle(String[] style) {
		int swt = 0;

		for (String s : style) {
			s = s.trim();
			if (JAXBRMUIConstants.ARROW.equals(s)) {
				swt |= SWT.ARROW;
			}
			if (JAXBRMUIConstants.BACKGROUND.equals(s)) {
				swt |= SWT.BACKGROUND;
			}
			if (JAXBRMUIConstants.BALLOON.equals(s)) {
				swt |= SWT.BALLOON;
			}
			if (JAXBRMUIConstants.BAR.equals(s)) {
				swt |= SWT.BAR;
			}
			if (JAXBRMUIConstants.BEGINNING.equals(s)) {
				swt |= SWT.BEGINNING;
			}
			if (JAXBRMUIConstants.BORDER.equals(s)) {
				swt |= SWT.BORDER;
			}
			if (JAXBRMUIConstants.BORDER_DASH.equals(s)) {
				swt |= SWT.BORDER_DASH;
			}
			if (JAXBRMUIConstants.BORDER_DOT.equals(s)) {
				swt |= SWT.BORDER_DOT;
			}
			if (JAXBRMUIConstants.BORDER_SOLID.equals(s)) {
				swt |= SWT.BORDER_SOLID;
			}
			if (JAXBRMUIConstants.BOTTOM.equals(s)) {
				swt |= SWT.BOTTOM;
			}
			if (JAXBRMUIConstants.CASCADE.equals(s)) {
				swt |= SWT.CASCADE;
			}
			if (JAXBRMUIConstants.CENTER.equals(s)) {
				swt |= SWT.CENTER;
			}
			if (JAXBRMUIConstants.CHECK.equals(s)) {
				swt |= SWT.CHECK;
			}
			if (JAXBRMUIConstants.DIALOG_TRIM.equals(s)) {
				swt |= SWT.DIALOG_TRIM;
			}
			if (JAXBRMUIConstants.DOWN.equals(s)) {
				swt |= SWT.DOWN;
			}
			if (JAXBRMUIConstants.DROP_DOWN.equals(s)) {
				swt |= SWT.DROP_DOWN;
			}
			if (JAXBRMUIConstants.FILL.equals(s)) {
				swt |= SWT.FILL;
			}
			if (JAXBRMUIConstants.FILL_BOTH.equals(s)) {
				swt |= GridData.FILL_BOTH;
			}
			if (JAXBRMUIConstants.FILL_EVEN_ODD.equals(s)) {
				swt |= SWT.FILL_EVEN_ODD;
			}
			if (JAXBRMUIConstants.FILL_HORIZONTAL.equals(s)) {
				swt |= GridData.FILL_HORIZONTAL;
			}
			if (JAXBRMUIConstants.FILL_VERTICAL.equals(s)) {
				swt |= GridData.FILL_VERTICAL;
			}
			if (JAXBRMUIConstants.FILL_WINDING.equals(s)) {
				swt |= SWT.FILL_WINDING;
			}
			if (JAXBRMUIConstants.FOREGROUND.equals(s)) {
				swt |= SWT.FOREGROUND;
			}
			if (JAXBRMUIConstants.FULL_SELECTION.equals(s)) {
				swt |= SWT.FULL_SELECTION;
			}
			if (JAXBRMUIConstants.H_SCROLL.equals(s)) {
				swt |= SWT.H_SCROLL;
			}
			if (JAXBRMUIConstants.HORIZONTAL.equals(s)) {
				swt |= SWT.HORIZONTAL;
			}
			if (JAXBRMUIConstants.LEAD.equals(s)) {
				swt |= SWT.LEAD;
			}
			if (JAXBRMUIConstants.LEFT.equals(s)) {
				swt |= SWT.LEFT;
			}
			if (JAXBRMUIConstants.LEFT_TO_RIGHT.equals(s)) {
				swt |= SWT.LEFT_TO_RIGHT;
			}
			if (JAXBRMUIConstants.LINE_CUSTOM.equals(s)) {
				swt |= SWT.LINE_CUSTOM;
			}
			if (JAXBRMUIConstants.LINE_DASH.equals(s)) {
				swt |= SWT.LINE_DASH;
			}
			if (JAXBRMUIConstants.LINE_DASHDOT.equals(s)) {
				swt |= SWT.LINE_DASHDOT;
			}
			if (JAXBRMUIConstants.LINE_DASHDOTDOT.equals(s)) {
				swt |= SWT.LINE_DASHDOTDOT;
			}
			if (JAXBRMUIConstants.LINE_DOT.equals(s)) {
				swt |= SWT.LINE_DOT;
			}
			if (JAXBRMUIConstants.LINE_SOLID.equals(s)) {
				swt |= SWT.LINE_SOLID;
			}
			if (JAXBRMUIConstants.MODELESS.equals(s)) {
				swt |= SWT.MODELESS;
			}
			if (JAXBRMUIConstants.MULTI.equals(s)) {
				swt |= SWT.MULTI;
			}
			if (JAXBRMUIConstants.NO.equals(s)) {
				swt |= SWT.NO;
			}
			if (JAXBRMUIConstants.NO_BACKGROUND.equals(s)) {
				swt |= SWT.NO_BACKGROUND;
			}
			if (JAXBRMUIConstants.NO_FOCUS.equals(s)) {
				swt |= SWT.NO_FOCUS;
			}
			if (JAXBRMUIConstants.NO_MERGE_PAINTS.equals(s)) {
				swt |= SWT.NO_MERGE_PAINTS;
			}
			if (JAXBRMUIConstants.NO_RADIO_GROUP.equals(s)) {
				swt |= SWT.NO_RADIO_GROUP;
			}
			if (JAXBRMUIConstants.NO_REDRAW_RESIZE.equals(s)) {
				swt |= SWT.NO_REDRAW_RESIZE;
			}
			if (JAXBRMUIConstants.NO_SCROLL.equals(s)) {
				swt |= SWT.NO_SCROLL;
			}
			if (JAXBRMUIConstants.NO_TRIM.equals(s)) {
				swt |= SWT.NO_TRIM;
			}
			if (JAXBRMUIConstants.NONE.equals(s)) {
				swt |= SWT.NONE;
			}
			if (JAXBRMUIConstants.NORMAL.equals(s)) {
				swt |= SWT.NORMAL;
			}
			if (JAXBRMUIConstants.ON_TOP.equals(s)) {
				swt |= SWT.ON_TOP;
			}
			if (JAXBRMUIConstants.OPEN.equals(s)) {
				swt |= SWT.OPEN;
			}
			if (JAXBRMUIConstants.POP_UP.equals(s)) {
				swt |= SWT.POP_UP;
			}
			if (JAXBRMUIConstants.PRIMARY_MODAL.equals(s)) {
				swt |= SWT.PRIMARY_MODAL;
			}
			if (JAXBRMUIConstants.PUSH.equals(s)) {
				swt |= SWT.PUSH;
			}
			if (JAXBRMUIConstants.RADIO.equals(s)) {
				swt |= SWT.RADIO;
			}
			if (JAXBRMUIConstants.READ_ONLY.equals(s)) {
				swt |= SWT.READ_ONLY;
			}
			if (JAXBRMUIConstants.RESIZE.equals(s)) {
				swt |= SWT.RESIZE;
			}
			if (JAXBRMUIConstants.RIGHT.equals(s)) {
				swt |= SWT.RIGHT;
			}
			if (JAXBRMUIConstants.RIGHT_TO_LEFT.equals(s)) {
				swt |= SWT.RIGHT_TO_LEFT;
			}
			if (JAXBRMUIConstants.SCROLL_LINE.equals(s)) {
				swt |= SWT.SCROLL_LINE;
			}
			if (JAXBRMUIConstants.SCROLL_LOCK.equals(s)) {
				swt |= SWT.SCROLL_LOCK;
			}
			if (JAXBRMUIConstants.SCROLL_PAGE.equals(s)) {
				swt |= SWT.SCROLL_PAGE;
			}
			if (JAXBRMUIConstants.SHADOW_ETCHED_IN.equals(s)) {
				swt |= SWT.SHADOW_ETCHED_IN;
			}
			if (JAXBRMUIConstants.SHADOW_ETCHED_OUT.equals(s)) {
				swt |= SWT.SHADOW_ETCHED_OUT;
			}
			if (JAXBRMUIConstants.SHADOW_IN.equals(s)) {
				swt |= SWT.SHADOW_IN;
			}
			if (JAXBRMUIConstants.SHADOW_NONE.equals(s)) {
				swt |= SWT.SHADOW_NONE;
			}
			if (JAXBRMUIConstants.SHADOW_OUT.equals(s)) {
				swt |= SWT.SHADOW_OUT;
			}
			if (JAXBRMUIConstants.SHELL_TRIM.equals(s)) {
				swt |= SWT.SHELL_TRIM;
			}
			if (JAXBRMUIConstants.SHORT.equals(s)) {
				swt |= SWT.SHORT;
			}
			if (JAXBRMUIConstants.SIMPLE.equals(s)) {
				swt |= SWT.SIMPLE;
			}
			if (JAXBRMUIConstants.SINGLE.equals(s)) {
				swt |= SWT.SINGLE;
			}
			if (JAXBRMUIConstants.SMOOTH.equals(s)) {
				swt |= SWT.SMOOTH;
			}
			if (JAXBRMUIConstants.TITLE.equals(s)) {
				swt |= SWT.TITLE;
			}
			if (JAXBRMUIConstants.TOGGLE.equals(s)) {
				swt |= SWT.TOGGLE;
			}
			if (JAXBRMUIConstants.TOP.equals(s)) {
				swt |= SWT.TOP;
			}
			if (JAXBRMUIConstants.UP.equals(s)) {
				swt |= SWT.UP;
			}
			if (JAXBRMUIConstants.V_SCROLL.equals(s)) {
				swt |= SWT.V_SCROLL;
			}
			if (JAXBRMUIConstants.VERTICAL.equals(s)) {
				swt |= SWT.VERTICAL;
			}
			if (JAXBRMUIConstants.WRAP.equals(s)) {
				swt |= SWT.WRAP;
			}
			if (JAXBRMUIConstants.YES.equals(s)) {
				swt |= SWT.YES;
			}
		}
		return swt;
	}

	/**
	 * Configure parts of viewer common to Table and Tree types.
	 * 
	 * @param viewer
	 * @param columnData
	 *            JAXB data element describing viewer columns
	 * @param listener
	 * @param tooltip
	 */
	private static void setupCommon(final ColumnViewer viewer, List<ColumnDataType> columnData, ISelectionChangedListener listener,
			boolean tooltip) {
		String[] columnProperties = new String[columnData.size()];
		for (int i = 0; i < columnData.size(); i++) {
			ColumnDataType columnDescriptor = columnData.get(i);
			columnProperties[i] = columnDescriptor.getName();
		}
		viewer.setColumnProperties(columnProperties);
		if (tooltip) {
			ColumnViewerToolTipSupport.enableFor(viewer);
		}
		if (listener != null) {
			viewer.addSelectionChangedListener(listener);
		}
		viewer.setLabelProvider(new ViewerDataCellLabelProvider(columnData));
	}

	/**
	 * Configure parts of viewer specific to Table type.
	 * 
	 * @param viewer
	 * @param columnData
	 *            JAXB data element describing viewer columns
	 * @param sortOnName
	 * @param headerVisible
	 * @param linesVisible
	 */
	private static void setupSpecific(final CheckboxTableViewer viewer, List<ColumnDataType> columnData, Boolean sortOnName,
			boolean headerVisible, boolean linesVisible) {
		for (int i = 0; i < columnData.size(); i++) {
			ColumnDataType columnDescriptor = columnData.get(i);
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
			if (JAXBRMUIConstants.UNDEFINED != columnDescriptor.getWidth()) {
				column.setWidth(columnDescriptor.getWidth());
			}
			if (null != columnDescriptor.getAlignment()) {
				column.setAlignment(getStyle(columnDescriptor.getAlignment()));
			}
			if (JAXBRMUIConstants.COLUMN_NAME.equals(name)) {
				if (sortOnName != null) {
					if (sortOnName) {
						column.addSelectionListener(getAttributeViewerSelectionAdapter(viewer));
					}
				}
			}
			if (JAXBRMUIConstants.COLUMN_VALUE.equals(columnDescriptor.getName())) {
				viewerColumn.setEditingSupport(new AttributeViewerEditingSupport(viewer));
			}
		}
		viewer.setContentProvider(new TableDataContentProvider());
		viewer.getTable().setHeaderVisible(headerVisible);
		viewer.getTable().setLinesVisible(linesVisible);
	}

	/**
	 * Configure parts of viewer specific to Tree type.
	 * 
	 * @param viewer
	 * @param columnData
	 *            JAXB data element describing viewer columns
	 * @param sortOnName
	 * @param headerVisible
	 * @param linesVisible
	 */
	private static void setupSpecific(final CheckboxTreeViewer viewer, List<ColumnDataType> columnData, Boolean sortOnName,
			boolean headerVisible, boolean linesVisible) {
		for (int i = 0; i < columnData.size(); i++) {
			ColumnDataType columnDescriptor = columnData.get(i);
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
			if (JAXBRMUIConstants.UNDEFINED != columnDescriptor.getWidth()) {
				column.setWidth(columnDescriptor.getWidth());
			}
			if (null != columnDescriptor.getAlignment()) {
				column.setAlignment(getStyle(columnDescriptor.getAlignment()));
			}
			if (JAXBRMUIConstants.COLUMN_NAME.equals(name)) {
				if (sortOnName != null) {
					if (sortOnName) {
						column.addSelectionListener(getAttributeViewerSelectionAdapter(viewer));
					}
				}
			}
			if (JAXBRMUIConstants.COLUMN_VALUE.equals(columnDescriptor.getName())) {
				viewerColumn.setEditingSupport(new AttributeViewerEditingSupport(viewer));
			}
		}
		viewer.setContentProvider(new TreeDataContentProvider());
		viewer.getTree().setHeaderVisible(headerVisible);
		viewer.getTree().setLinesVisible(linesVisible);
	}
}
