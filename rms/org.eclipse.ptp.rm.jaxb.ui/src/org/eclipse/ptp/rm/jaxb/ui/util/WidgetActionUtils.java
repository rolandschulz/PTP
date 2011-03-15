/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.rm.jaxb.core.data.Validator;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.ui.utils.DataSource.ValidationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class WidgetActionUtils implements IJAXBUINonNLSConstants {

	private WidgetActionUtils() {
	}

	public static void errorMessage(Shell s, Throwable e, String message, String title, boolean causeTrace) {
		String append = e == null ? ZEROSTR : e.getMessage();
		Throwable t = e == null ? null : e.getCause();
		String lineSep = LINE_SEP;
		if (causeTrace) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			if (t != null) {
				t.printStackTrace(pw);
				append = sw.toString();
			}
		} else if (t != null) {
			append = t.getMessage();
		}
		MessageDialog.openError(s, title, message + lineSep + lineSep + append);
	}

	public static String getSelected(Combo combo) {
		if (combo.getItemCount() == 0) {
			return combo.getText();
		}
		int i = combo.getSelectionIndex();
		if (i < 0) {
			return combo.getText();
		}
		return combo.getItem(i);
	}

	public static String getValueString(Control c) {

		return null;
	}

	public static boolean isSettable(Control uiElement) {
		if (uiElement instanceof Label) {
			return true;
		}
		if (uiElement instanceof Text) {
			return true;
		}
		if (uiElement instanceof Combo) {
			return true;
		}
		if (uiElement instanceof Spinner) {
			return true;
		}
		if (uiElement instanceof Button) {
			Button b = (Button) uiElement;
			int style = b.getStyle();
			return (style == (style | SWT.CHECK)) || (style == (style | SWT.RADIO));
		}
		return false;
	}

	public static String openInputDialog(Shell shell, String message, String title, String original) {
		InputDialog nameDialog = new InputDialog(shell, message, title, original, null);
		if (nameDialog.open() != Window.CANCEL) {
			return nameDialog.getValue();
		}
		return null;
	}

	public static String select(Combo combo, String name) {
		String[] items = combo.getItems();
		if (items.length == 0) {
			return ZEROSTR;
		}
		int i = 0;
		for (; i < items.length; i++) {
			if (items[i].equals(name)) {
				combo.select(i);
				break;
			}
		}
		if (i == items.length) {
			i = 0;
			combo.select(i);
		}
		return combo.getItem(i);
	}

	public static void setValue(Control c, String dfltV) {
		if (c instanceof Text) {

		} else if (c instanceof Combo) {

		}

	}

	/**
	 * If the text is empty and there is a default value, resets the widget
	 * value to its default.
	 */
	public static void validate(Control c, String string) {
		// TODO Auto-generated method stub

	}

	/**
	 * If validation fails, resets the widget value to its default
	 */
	public static void validate(Control c, Validator v, String defaultValue) throws ValidationException {
		// TODO

	}

	/**
	 * Facility to set the string value of a {@link Text} widget. Copied from
	 * DataSource.
	 * 
	 * @param t
	 *            The {@link Text} widget.
	 * @param s
	 *            The new string value.
	 */
	private static void applyText(Combo t, String s) {
		assert t != null;
		if (s == null) {
			t.setText(ZEROSTR);
		} else {
			t.setText(s);
		}
	}

	/**
	 * Facility to set the string value of a {@link Text} widget. Copied from
	 * DataSource.
	 * 
	 * @param t
	 *            The {@link Text} widget.
	 * @param s
	 *            The new string value.
	 */
	private static void applyText(Text t, String s) {
		assert t != null;
		if (s == null) {
			t.setText(ZEROSTR);
		} else {
			t.setText(s);
		}
	}

	/**
	 * Facility to get string value of a {@link Combo} widget, or null if the
	 * widget is empty.
	 * 
	 * @param text
	 *            The widget
	 * @return The string value of widget or null widget is empty.
	 */
	private static String extractText(Combo text) {
		assert text != null;
		String s = text.getText().trim();
		return (s.length() == 0 ? null : s);
	}

	/**
	 * Facility to get string value of a {@link Text} widget, or null if the
	 * widget is empty.
	 * 
	 * @param text
	 *            The widget
	 * @return The string value of widget or null widget is empty.
	 */
	private static String extractText(Text text) {
		assert text != null;
		String s = text.getText().trim();
		return (s.length() == 0 ? null : s);
	}
}
