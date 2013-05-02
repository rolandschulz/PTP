/*******************************************************************************
 * Copyright (c) 2011, 2012 University of Illinois.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 	Jeff Overbey - Environment Manager support
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.core.data.FontType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

/**
 * Convenience methods for constructing and configuring widgets.
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 */
public class WidgetBuilderUtils {

	private static final FontRegistry fonts = new FontRegistry();

	/**
	 * Tries to set monospace text on text area.
	 * 
	 * @param text
	 *            of dialog or tab
	 */
	public static void applyMonospace(Text text) {
		// Courier exists on Mac, Linux, Windows ...
		FontType fd = new FontType();
		fd.setName(JAXBUIConstants.COURIER);
		fd.setSize(14);
		fd.setStyle(JAXBUIConstants.NORMAL);
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
		return createButton(parent, label, SWT.CHECK, listener);
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
		GridLayout layout = createGridLayout(columns, false, JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT,
				JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT);
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
		composite.setLayout(layout);
		composite.setLayoutData(layoutData);
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
		new GridData();
		return createGridData(style, grabExcessHorizontal, grabExcessVertical, widthHint, heightHint, JAXBUIConstants.DEFAULT,
				JAXBUIConstants.DEFAULT, horizontalSpan, verticalSpan, JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT,
				JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT);
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
			if (style == JAXBUIConstants.DEFAULT) {
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
		if (null != widthHint && widthHint != JAXBUIConstants.DEFAULT) {
			data.widthHint = widthHint;
		}
		if (null != heightHint && heightHint != JAXBUIConstants.DEFAULT) {
			data.heightHint = heightHint;
		}
		if (null != minimumWidth && minimumWidth != JAXBUIConstants.DEFAULT) {
			data.minimumWidth = minimumWidth;
		}
		if (null != minimumHeight && minimumHeight != JAXBUIConstants.DEFAULT) {
			data.minimumHeight = minimumHeight;
		}
		if (null != horizontalSpan && horizontalSpan != JAXBUIConstants.DEFAULT) {
			data.horizontalSpan = horizontalSpan;
		}
		if (null != verticalSpan && verticalSpan != JAXBUIConstants.DEFAULT) {
			data.verticalSpan = verticalSpan;
		}
		if (null != horizonalAlign && horizonalAlign != JAXBUIConstants.DEFAULT) {
			data.horizontalAlignment = horizonalAlign;
		}
		if (null != verticalAlign && verticalAlign != JAXBUIConstants.DEFAULT) {
			data.verticalAlignment = verticalAlign;
		}
		if (null != horizontalIndent && horizontalIndent != JAXBUIConstants.DEFAULT) {
			data.horizontalIndent = horizontalIndent;
		}
		if (null != verticalIndent && verticalIndent != JAXBUIConstants.DEFAULT) {
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
		return createGridData(style, false, false, JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT, cols, JAXBUIConstants.DEFAULT);
	}

	/**
	 * Sets style to GridData.FILL_BOTH, grabExcessHorizontal and grabExcessVertical both to true.
	 * 
	 * @param widthHint
	 * @param heightHint
	 * @param cols
	 * @return grid data
	 */
	public static GridData createGridDataFill(Integer widthHint, Integer heightHint, Integer cols) {
		return createGridData(GridData.FILL_BOTH, true, true, widthHint, heightHint, cols, JAXBUIConstants.DEFAULT);
	}

	/**
	 * Sets style to GridData.FILL_BOTH, grabExcessHorizontal to true.
	 * 
	 * @param cols
	 * @return grid data
	 */
	public static GridData createGridDataFillH(Integer cols) {
		return createGridData(GridData.FILL_HORIZONTAL, true, false, JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT, cols,
				JAXBUIConstants.DEFAULT);
	}

	/**
	 * @param columns
	 * @param makeColumnsEqualWidth
	 * @return grid layout
	 */
	public static GridLayout createGridLayout(Integer columns, Boolean makeColumnsEqualWidth) {
		return createGridLayout(columns, makeColumnsEqualWidth, JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT,
				JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT);
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
				JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT, JAXBUIConstants.DEFAULT);
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
		if (null != horizontalSpacing && horizontalSpacing != JAXBUIConstants.DEFAULT) {
			gridLayout.horizontalSpacing = horizontalSpacing;
		}
		if (null != verticalSpacing && verticalSpacing != JAXBUIConstants.DEFAULT) {
			gridLayout.verticalSpacing = verticalSpacing;
		}
		if (null != marginWidth && marginWidth != JAXBUIConstants.DEFAULT) {
			gridLayout.marginWidth = marginWidth;
		}
		if (null != marginHeight && marginHeight != JAXBUIConstants.DEFAULT) {
			gridLayout.marginHeight = marginHeight;
		}
		if (null != marginLeft && marginLeft != JAXBUIConstants.DEFAULT) {
			gridLayout.marginLeft = marginLeft;
		}
		if (null != marginRight && marginRight != JAXBUIConstants.DEFAULT) {
			gridLayout.marginRight = marginRight;
		}
		if (null != marginTop && marginTop != JAXBUIConstants.DEFAULT) {
			gridLayout.marginTop = marginTop;
		}
		if (null != marginBottom && marginBottom != JAXBUIConstants.DEFAULT) {
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
		group.setLayout(layout);
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
		GridData data = createGridData(JAXBUIConstants.DEFAULT, columnSpan);
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
			text = JAXBUIConstants.ZEROSTR;
		}
		label.setText(text.trim());
		label.setLayoutData(layoutData);
		return label;
	}

	/**
	 * Creates and returns a new push button with the given label and/or image.
	 * 
	 * @param parent
	 *            parent control
	 * @param label
	 *            button label or <code>null</code>
	 * @return a new push button
	 */
	private static Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);
		setButtonDimensionHint(button);
		return button;
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
		Button button = createPushButton(parent, label);
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		button.setLayoutData(gd);
		if (null != listener) {
			button.addSelectionListener(listener);
		}
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
	 * @param wrap
	 * @param marginHeight
	 * @param marginWidth
	 * @param marginTop
	 * @param marginBottom
	 * @param marginLeft
	 * @param marginRight
	 * @param spacing
	 * @return row layout
	 */
	public static RowLayout createRowLayout(String type, Boolean center, Boolean fill, Boolean justify, Boolean pack, Boolean wrap,
			Integer marginHeight, Integer marginWidth, Integer marginTop, Integer marginBottom, Integer marginLeft,
			Integer marginRight, Integer spacing) {
		RowLayout layout = new RowLayout();
		if (type != null) {
			layout.type = getStyle(type);
		}
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
		if (wrap != null) {
			layout.wrap = wrap;
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
	public static Spinner createSpinner(Composite parent, int style, Object layoutData, String label, Integer minimum,
			Integer maximum, Integer initialValue, ModifyListener listener) {
		if (label != null) {
			createLabel(parent, label, SWT.NONE, 1);
		}

		Spinner s = new Spinner(parent, style);
		if (maximum != null) {
			s.setMaximum(maximum);
		}
		if (minimum != null) {
			s.setMinimum(minimum);
		}
		if (initialValue != null) {
			s.setSelection(initialValue);
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
		if (layoutData instanceof GridData) {
			GridData gd = (GridData) layoutData;
			cols = gd.horizontalSpan;
		}
		if (style == null) {
			style = SWT.None;
		}
		if (cols == null) {
			cols = 1;
		}
		Table t = new Table(parent, style);
		t.setLayoutData(layoutData);
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
		if (layoutData instanceof GridData) {
			GridData gd = (GridData) layoutData;
			cols = gd.horizontalSpan;
		}
		if (style == null) {
			style = SWT.None;
		}
		if (cols == null) {
			cols = 1;
		}
		Tree t = new Tree(parent, style);
		t.setLayoutData(layoutData);
		return t;
	}

	/**
	 * Returns a width hint for a button control.
	 */
	private static int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
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
		if (JAXBUIConstants.COLOR_BLACK.equals(color)) {
			swtColor = SWT.COLOR_BLACK;
		} else if (JAXBUIConstants.COLOR_WHITE.equals(color)) {
			swtColor = SWT.COLOR_WHITE;
		} else if (JAXBUIConstants.COLOR_RED.equals(color)) {
			swtColor = SWT.COLOR_RED;
		} else if (JAXBUIConstants.COLOR_DARK_RED.equals(color)) {
			swtColor = SWT.COLOR_DARK_RED;
		} else if (JAXBUIConstants.COLOR_GREEN.equals(color)) {
			swtColor = SWT.COLOR_GREEN;
		} else if (JAXBUIConstants.COLOR_DARK_GREEN.equals(color)) {
			swtColor = SWT.COLOR_DARK_GREEN;
		} else if (JAXBUIConstants.COLOR_YELLOW.equals(color)) {
			swtColor = SWT.COLOR_YELLOW;
		} else if (JAXBUIConstants.COLOR_DARK_YELLOW.equals(color)) {
			swtColor = SWT.COLOR_DARK_YELLOW;
		} else if (JAXBUIConstants.COLOR_BLUE.equals(color)) {
			swtColor = SWT.COLOR_BLUE;
		} else if (JAXBUIConstants.COLOR_DARK_BLUE.equals(color)) {
			swtColor = SWT.COLOR_DARK_BLUE;
		} else if (JAXBUIConstants.COLOR_MAGENTA.equals(color)) {
			swtColor = SWT.COLOR_MAGENTA;
		} else if (JAXBUIConstants.COLOR_DARK_MAGENTA.equals(color)) {
			swtColor = SWT.COLOR_DARK_MAGENTA;
		} else if (JAXBUIConstants.COLOR_CYAN.equals(color)) {
			swtColor = SWT.COLOR_CYAN;
		} else if (JAXBUIConstants.COLOR_DARK_CYAN.equals(color)) {
			swtColor = SWT.COLOR_DARK_CYAN;
		} else if (JAXBUIConstants.COLOR_GRAY.equals(color)) {
			swtColor = SWT.COLOR_GRAY;
		} else if (JAXBUIConstants.COLOR_DARK_GRAY.equals(color)) {
			swtColor = SWT.COLOR_DARK_GRAY;
		} else if (JAXBUIConstants.COLOR_INFO_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_INFO_BACKGROUND;
		} else if (JAXBUIConstants.COLOR_INFO_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_INFO_FOREGROUND;
		} else if (JAXBUIConstants.COLOR_LIST_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_LIST_BACKGROUND;
		} else if (JAXBUIConstants.COLOR_LIST_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_LIST_FOREGROUND;
		} else if (JAXBUIConstants.COLOR_LIST_SELECTION.equals(color)) {
			swtColor = SWT.COLOR_LIST_SELECTION;
		} else if (JAXBUIConstants.COLOR_LIST_SELECTION_TEXT.equals(color)) {
			swtColor = SWT.COLOR_LIST_SELECTION_TEXT;
		} else if (JAXBUIConstants.COLOR_TITLE_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_BACKGROUND;
		} else if (JAXBUIConstants.COLOR_TITLE_BACKGROUND_GRADIENT.equals(color)) {
			swtColor = SWT.COLOR_TITLE_BACKGROUND_GRADIENT;
		} else if (JAXBUIConstants.COLOR_TITLE_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_FOREGROUND;
		} else if (JAXBUIConstants.COLOR_TITLE_INACTIVE_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_INACTIVE_BACKGROUND;
		} else if (JAXBUIConstants.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT.equals(color)) {
			swtColor = SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT;
		} else if (JAXBUIConstants.COLOR_TITLE_INACTIVE_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_TITLE_INACTIVE_FOREGROUND;
		} else if (JAXBUIConstants.COLOR_WIDGET_BACKGROUND.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_BACKGROUND;
		} else if (JAXBUIConstants.COLOR_WIDGET_BORDER.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_BORDER;
		} else if (JAXBUIConstants.COLOR_WIDGET_DARK_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_DARK_SHADOW;
		} else if (JAXBUIConstants.COLOR_WIDGET_FOREGROUND.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_FOREGROUND;
		} else if (JAXBUIConstants.COLOR_WIDGET_HIGHLIGHT_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW;
		} else if (JAXBUIConstants.COLOR_WIDGET_LIGHT_SHADOW.equals(color)) {
			swtColor = SWT.COLOR_WIDGET_LIGHT_SHADOW;
		} else if (JAXBUIConstants.COLOR_WIDGET_NORMAL_SHADOW.equals(color)) {
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
	 * Translates the string representation of OR'd style values into SWT int code.
	 * 
	 * Calls {@link #getStyle(String[])}.
	 * 
	 * @param style
	 *            string representation
	 * @return SWT code
	 */
	public static int getStyle(String style) {
		if (style == null || JAXBUIConstants.ZEROSTR.equals(style)) {
			return SWT.NONE;
		}
		return getStyle(style.split(JAXBUIConstants.REGPIP));
	}

	/**
	 * Translates each array item into the integer code, using a cumulative logical OR.
	 * 
	 * @param style
	 *            array of string from split on '|'
	 * @return SWT style code
	 */
	private static int getStyle(String[] style) {
		int swt = 0;

		for (String s : style) {
			s = s.trim();
			if (JAXBUIConstants.ARROW.equals(s)) {
				swt |= SWT.ARROW;
			}
			if (JAXBUIConstants.BACKGROUND.equals(s)) {
				swt |= SWT.BACKGROUND;
			}
			if (JAXBUIConstants.BALLOON.equals(s)) {
				swt |= SWT.BALLOON;
			}
			if (JAXBUIConstants.BAR.equals(s)) {
				swt |= SWT.BAR;
			}
			if (JAXBUIConstants.BEGINNING.equals(s)) {
				swt |= SWT.BEGINNING;
			}
			if (JAXBUIConstants.BORDER.equals(s)) {
				swt |= SWT.BORDER;
			}
			if (JAXBUIConstants.BORDER_DASH.equals(s)) {
				swt |= SWT.BORDER_DASH;
			}
			if (JAXBUIConstants.BORDER_DOT.equals(s)) {
				swt |= SWT.BORDER_DOT;
			}
			if (JAXBUIConstants.BORDER_SOLID.equals(s)) {
				swt |= SWT.BORDER_SOLID;
			}
			if (JAXBUIConstants.BOTTOM.equals(s)) {
				swt |= SWT.BOTTOM;
			}
			if (JAXBUIConstants.CASCADE.equals(s)) {
				swt |= SWT.CASCADE;
			}
			if (JAXBUIConstants.CENTER.equals(s)) {
				swt |= SWT.CENTER;
			}
			if (JAXBUIConstants.CHECK.equals(s)) {
				swt |= SWT.CHECK;
			}
			if (JAXBUIConstants.DIALOG_TRIM.equals(s)) {
				swt |= SWT.DIALOG_TRIM;
			}
			if (JAXBUIConstants.DOWN.equals(s)) {
				swt |= SWT.DOWN;
			}
			if (JAXBUIConstants.DROP_DOWN.equals(s)) {
				swt |= SWT.DROP_DOWN;
			}
			if (JAXBUIConstants.FILL.equals(s)) {
				swt |= SWT.FILL;
			}
			if (JAXBUIConstants.FILL_BOTH.equals(s)) {
				swt |= GridData.FILL_BOTH;
			}
			if (JAXBUIConstants.FILL_EVEN_ODD.equals(s)) {
				swt |= SWT.FILL_EVEN_ODD;
			}
			if (JAXBUIConstants.FILL_HORIZONTAL.equals(s)) {
				swt |= GridData.FILL_HORIZONTAL;
			}
			if (JAXBUIConstants.FILL_VERTICAL.equals(s)) {
				swt |= GridData.FILL_VERTICAL;
			}
			if (JAXBUIConstants.FILL_WINDING.equals(s)) {
				swt |= SWT.FILL_WINDING;
			}
			if (JAXBUIConstants.FOREGROUND.equals(s)) {
				swt |= SWT.FOREGROUND;
			}
			if (JAXBUIConstants.FULL_SELECTION.equals(s)) {
				swt |= SWT.FULL_SELECTION;
			}
			if (JAXBUIConstants.H_SCROLL.equals(s)) {
				swt |= SWT.H_SCROLL;
			}
			if (JAXBUIConstants.HORIZONTAL.equals(s)) {
				swt |= SWT.HORIZONTAL;
			}
			if (JAXBUIConstants.LEAD.equals(s)) {
				swt |= SWT.LEAD;
			}
			if (JAXBUIConstants.LEFT.equals(s)) {
				swt |= SWT.LEFT;
			}
			if (JAXBUIConstants.LEFT_TO_RIGHT.equals(s)) {
				swt |= SWT.LEFT_TO_RIGHT;
			}
			if (JAXBUIConstants.LINE_CUSTOM.equals(s)) {
				swt |= SWT.LINE_CUSTOM;
			}
			if (JAXBUIConstants.LINE_DASH.equals(s)) {
				swt |= SWT.LINE_DASH;
			}
			if (JAXBUIConstants.LINE_DASHDOT.equals(s)) {
				swt |= SWT.LINE_DASHDOT;
			}
			if (JAXBUIConstants.LINE_DASHDOTDOT.equals(s)) {
				swt |= SWT.LINE_DASHDOTDOT;
			}
			if (JAXBUIConstants.LINE_DOT.equals(s)) {
				swt |= SWT.LINE_DOT;
			}
			if (JAXBUIConstants.LINE_SOLID.equals(s)) {
				swt |= SWT.LINE_SOLID;
			}
			if (JAXBUIConstants.MODELESS.equals(s)) {
				swt |= SWT.MODELESS;
			}
			if (JAXBUIConstants.MULTI.equals(s)) {
				swt |= SWT.MULTI;
			}
			if (JAXBUIConstants.NO.equals(s)) {
				swt |= SWT.NO;
			}
			if (JAXBUIConstants.NO_BACKGROUND.equals(s)) {
				swt |= SWT.NO_BACKGROUND;
			}
			if (JAXBUIConstants.NO_FOCUS.equals(s)) {
				swt |= SWT.NO_FOCUS;
			}
			if (JAXBUIConstants.NO_MERGE_PAINTS.equals(s)) {
				swt |= SWT.NO_MERGE_PAINTS;
			}
			if (JAXBUIConstants.NO_RADIO_GROUP.equals(s)) {
				swt |= SWT.NO_RADIO_GROUP;
			}
			if (JAXBUIConstants.NO_REDRAW_RESIZE.equals(s)) {
				swt |= SWT.NO_REDRAW_RESIZE;
			}
			if (JAXBUIConstants.NO_SCROLL.equals(s)) {
				swt |= SWT.NO_SCROLL;
			}
			if (JAXBUIConstants.NO_TRIM.equals(s)) {
				swt |= SWT.NO_TRIM;
			}
			if (JAXBUIConstants.NONE.equals(s)) {
				swt |= SWT.NONE;
			}
			if (JAXBUIConstants.NORMAL.equals(s)) {
				swt |= SWT.NORMAL;
			}
			if (JAXBUIConstants.ON_TOP.equals(s)) {
				swt |= SWT.ON_TOP;
			}
			if (JAXBUIConstants.OPEN.equals(s)) {
				swt |= SWT.OPEN;
			}
			if (JAXBUIConstants.POP_UP.equals(s)) {
				swt |= SWT.POP_UP;
			}
			if (JAXBUIConstants.PRIMARY_MODAL.equals(s)) {
				swt |= SWT.PRIMARY_MODAL;
			}
			if (JAXBUIConstants.PUSH.equals(s)) {
				swt |= SWT.PUSH;
			}
			if (JAXBUIConstants.RADIO.equals(s)) {
				swt |= SWT.RADIO;
			}
			if (JAXBUIConstants.READ_ONLY.equals(s)) {
				swt |= SWT.READ_ONLY;
			}
			if (JAXBUIConstants.RESIZE.equals(s)) {
				swt |= SWT.RESIZE;
			}
			if (JAXBUIConstants.RIGHT.equals(s)) {
				swt |= SWT.RIGHT;
			}
			if (JAXBUIConstants.RIGHT_TO_LEFT.equals(s)) {
				swt |= SWT.RIGHT_TO_LEFT;
			}
			if (JAXBUIConstants.SCROLL_LINE.equals(s)) {
				swt |= SWT.SCROLL_LINE;
			}
			if (JAXBUIConstants.SCROLL_LOCK.equals(s)) {
				swt |= SWT.SCROLL_LOCK;
			}
			if (JAXBUIConstants.SCROLL_PAGE.equals(s)) {
				swt |= SWT.SCROLL_PAGE;
			}
			if (JAXBUIConstants.SHADOW_ETCHED_IN.equals(s)) {
				swt |= SWT.SHADOW_ETCHED_IN;
			}
			if (JAXBUIConstants.SHADOW_ETCHED_OUT.equals(s)) {
				swt |= SWT.SHADOW_ETCHED_OUT;
			}
			if (JAXBUIConstants.SHADOW_IN.equals(s)) {
				swt |= SWT.SHADOW_IN;
			}
			if (JAXBUIConstants.SHADOW_NONE.equals(s)) {
				swt |= SWT.SHADOW_NONE;
			}
			if (JAXBUIConstants.SHADOW_OUT.equals(s)) {
				swt |= SWT.SHADOW_OUT;
			}
			if (JAXBUIConstants.SHELL_TRIM.equals(s)) {
				swt |= SWT.SHELL_TRIM;
			}
			if (JAXBUIConstants.SHORT.equals(s)) {
				swt |= SWT.SHORT;
			}
			if (JAXBUIConstants.SIMPLE.equals(s)) {
				swt |= SWT.SIMPLE;
			}
			if (JAXBUIConstants.SINGLE.equals(s)) {
				swt |= SWT.SINGLE;
			}
			if (JAXBUIConstants.SMOOTH.equals(s)) {
				swt |= SWT.SMOOTH;
			}
			if (JAXBUIConstants.TITLE.equals(s)) {
				swt |= SWT.TITLE;
			}
			if (JAXBUIConstants.TOGGLE.equals(s)) {
				swt |= SWT.TOGGLE;
			}
			if (JAXBUIConstants.TOP.equals(s)) {
				swt |= SWT.TOP;
			}
			if (JAXBUIConstants.UP.equals(s)) {
				swt |= SWT.UP;
			}
			if (JAXBUIConstants.V_SCROLL.equals(s)) {
				swt |= SWT.V_SCROLL;
			}
			if (JAXBUIConstants.VERTICAL.equals(s)) {
				swt |= SWT.VERTICAL;
			}
			if (JAXBUIConstants.WRAP.equals(s)) {
				swt |= SWT.WRAP;
			}
			if (JAXBUIConstants.YES.equals(s)) {
				swt |= SWT.YES;
			}
		}
		return swt;
	}

	/**
	 * For consistency in treating <code>null</code> or undefined defaults on loading, this method ensures the first element of the
	 * combo is a JAXBRMUIConstants.ZEROSTR. It also removes blank items.
	 * 
	 * @param items
	 * @return adjusted array of combo items
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String[] normalizeComboItems(String[] items) {
		List<String> list = new ArrayList(Arrays.asList(items));
		for (Iterator<String> s = list.iterator(); s.hasNext();) {
			String item = s.next().trim();
			if (JAXBUIConstants.ZEROSTR.equals(item) || JAXBUIConstants.LINE_SEP.equals(item)) {
				s.remove();
			}
		}
		list.add(0, JAXBUIConstants.ZEROSTR);
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
		if (JAXBUIConstants.ZEROSTR.equals(text)) {
			return JAXBUIConstants.ZEROSTR;
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
				if (lastChar != JAXBUIConstants.SP.charAt(0)) {
					newLine.append(JAXBUIConstants.SP);
					lastChar = JAXBUIConstants.SP.charAt(0);
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
	 * Sets width and height hint for the button control. <b>Note:</b> This is
	 * a NOP if the button's layout data is not an instance of <code>GridData</code>.
	 * 
	 * @param the
	 *            button for which to set the dimension hint
	 */
	public static void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd = button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData) gd).widthHint = getButtonWidthHint(button);
			((GridData) gd).horizontalAlignment = GridData.FILL;
		}
	}

	private WidgetBuilderUtils() {
	}
}
