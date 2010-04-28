/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * A set of convenience wrappers around jface and swt widget construction
 * routines.
 * 
 * @author arossi
 * 
 */
public class WidgetUtils {
	private WidgetUtils() {
	}

	public static TableColumn addTableColumn(final TableViewer viewer, final String columnName, int style) {
		Table t = viewer.getTable();

		TableColumn c = new TableColumn(t, style);
		c.setText(columnName);

		return c;
	}

	public static Button createButton(Composite parent, String buttonText, Image image, int style, int colSpan, SelectionListener l) {

		Button button = new Button(parent, style);
		button.setText(buttonText);
		if (image != null) {
			button.setImage(image);
		}

		if (l != null)
			button.addSelectionListener(l);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = colSpan;
		button.setLayoutData(data);

		return button;

	}

	public static Composite createComposite(Composite parent, int cols, int x, int y) {
		ScrolledComposite scroller = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scroller.setLayoutData(new GridData(GridData.FILL_BOTH));
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);

		Composite main = new Composite(scroller, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 1;
		data.verticalAlignment = SWT.LEFT;
		data.grabExcessVerticalSpace = false;
		main.setLayoutData(data);

		GridLayout layout = new GridLayout();
		main.setLayout(layout);
		layout.numColumns = cols;
		layout.verticalSpacing = 1;
		scroller.setContent(main);
		scroller.setMinSize(x, y);
		return main;
	}

	public static Composite createContainer(Composite parent, String name, boolean fill, int minHeight) {
		Group container = new Group(parent, SWT.NONE);
		if (name != null)
			container.setText(name);

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.minimumHeight = minHeight;
		data.heightHint = minHeight;

		if (fill) {
			data.verticalAlignment = SWT.FILL;
			data.grabExcessVerticalSpace = true;
		} else {
			data.verticalAlignment = SWT.CENTER;
		}

		container.setLayoutData(data);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;

		return container;
	}

	public static Group createFillingGroup(Composite parent, String text, int columns, int colSpan, boolean verticalFill) {
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = verticalFill;
		if (colSpan != -1)
			data.horizontalSpan = colSpan;

		GridLayout layout = new GridLayout();
		layout.numColumns = columns;
		layout.verticalSpacing = 9;

		Group group = new Group(parent, SWT.SHADOW_NONE);
		if (text != null)
			group.setText(text);
		group.setLayout(layout);
		group.setLayoutData(data);

		return group;
	}

	public static Table createFillingTable(Composite parent, int numColumns, int suggestedWidth, int colSpan, int tableStyle) {
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.widthHint = 200;
		data.horizontalSpan = colSpan;
		data.heightHint = 150;

		Table t = new Table(parent, tableStyle);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		t.setLayoutData(data);

		TableLayout layout = new TableLayout();
		for (int i = 0; i < numColumns; i++) {
			layout.addColumnData(new ColumnPixelData(suggestedWidth / numColumns));
		}
		t.setLayout(layout);

		return t;
	}

	public static Combo createItemCombo(Composite container, String labelString, String[] items, String initial, String tooltip,
			ModifyListener listener, int colSpan) {
		if (labelString != null) {
			Label label = new Label(container, SWT.NONE);
			label.setText(labelString);
			if (tooltip != null) {
				label.setToolTipText(tooltip);
			}
		}

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		data.horizontalSpan = colSpan;

		Combo combo = new Combo(container, SWT.BORDER);
		combo.setItems(items);
		combo.setLayoutData(data);
		if (initial != null)
			combo.setText(initial);
		if (listener != null)
			combo.addModifyListener(listener);

		return combo;
	}

	public static Label createLabel(Composite container, String text, int style, int colSpan) {
		GridData data = new GridData();
		data.horizontalSpan = colSpan;

		Label label = new Label(container, style);
		if (text == null)
			text = ""; //$NON-NLS-1$
		label.setText(text.trim());
		label.setLayoutData(data);

		return label;
	}

	public static Text createLabelledText(Composite container, String labelString, String initialValue, int colSpan,
			ModifyListener listener, Color color) {
		Label label = new Label(container, SWT.NONE);
		label.setText(labelString);
		if (color != null)
			label.setBackground(color);

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		data.horizontalSpan = colSpan;

		Text text = new Text(container, SWT.BORDER);
		if (color != null)
			text.setBackground(color);
		text.setLayoutData(data);
		if (initialValue != null)
			text.setText(initialValue);
		if (listener != null)
			text.addModifyListener(listener);

		return text;
	}

	public static Spinner createSpinner(Composite container, String labelString, int min, int max, int initial, int colSpan,
			ModifyListener listener) {
		if (labelString != null) {
			GridData data = new GridData();

			Label label = new Label(container, SWT.NONE);
			label.setText(labelString);
			label.setLayoutData(data);
		}

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		data.horizontalSpan = colSpan;

		Spinner s = new Spinner(container, SWT.NONE);
		s.setMaximum(max);
		s.setMinimum(min);
		s.setSelection(initial);
		s.setLayoutData(data);
		if (listener != null)
			s.addModifyListener(listener);

		return s;
	}

	public static Text createText(Composite container, String initialValue, ModifyListener listener, Color color) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		Text text = new Text(container, SWT.BORDER);
		if (color != null)
			text.setBackground(color);
		text.setLayoutData(data);
		if (initialValue != null)
			text.setText(initialValue);
		if (listener != null)
			text.addModifyListener(listener);

		return text;
	}

	public static void errorMessage(Shell s, Throwable e, String message, String title, boolean causeTrace) {
		Throwable t = e.getCause();
		String lineSep = System.getProperty("line.separator"); //$NON-NLS-1$
		String append = ""; //$NON-NLS-1$
		if (causeTrace) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			if (t != null) {
				t.printStackTrace(pw);
				append = sw.toString();
			}
		} else {
			if (t != null)
				append = t.getMessage();
		}
		MessageDialog.openError(s, title, message + lineSep + e.getMessage() + lineSep + append);
	}
}
