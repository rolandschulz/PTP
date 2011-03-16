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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.data.FileMatch;
import org.eclipse.ptp.rm.jaxb.core.data.Regex;
import org.eclipse.ptp.rm.jaxb.core.data.Validator;
import org.eclipse.ptp.rm.jaxb.core.data.impl.RegexImpl;
import org.eclipse.ptp.rm.jaxb.core.exceptions.UnsatisfiedMatchException;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
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

	public static String getValueString(Control uiElement) {
		assert uiElement != null;
		String s = null;

		if (uiElement instanceof Label) {
			Label c = (Label) uiElement;
			s = c.getText();
		}
		if (uiElement instanceof Text) {
			Text c = (Text) uiElement;
			s = c.getText();
		}
		if (uiElement instanceof Combo) {
			Combo c = (Combo) uiElement;
			s = getSelected(c);
		}
		if (uiElement instanceof Spinner) {
			Spinner c = (Spinner) uiElement;
			s = ZEROSTR + c.getSelection();
		}
		if (uiElement instanceof Button) {
			Button c = (Button) uiElement;
			int style = c.getStyle();
			if ((style == (style | SWT.CHECK)) || (style == (style | SWT.RADIO))) {
				s = ZEROSTR + c.getSelection();
			}
		}

		if (s != null) {
			s = s.trim();
			return (s.length() == 0 ? null : s);
		}
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

	public static void setValue(Control uiElement, String value) {
		assert uiElement != null;
		if (value == null) {
			value = ZEROSTR;
		}

		if (uiElement instanceof Label) {
			Label c = (Label) uiElement;
			c.setText(value);
		}
		if (uiElement instanceof Text) {
			Text c = (Text) uiElement;
			c.setText(value);
		}
		if (uiElement instanceof Combo) {
			Combo c = (Combo) uiElement;
			select(c, value);
		}
		if (uiElement instanceof Spinner) {
			Spinner c = (Spinner) uiElement;
			c.setSelection(Integer.parseInt(value));
		}
		if (uiElement instanceof Button) {
			Button c = (Button) uiElement;
			int style = c.getStyle();
			if ((style == (style | SWT.CHECK)) || (style == (style | SWT.RADIO))) {
				c.setSelection(Boolean.parseBoolean(value));
			}
		}
	}

	/**
	 * If the text is empty and there is a default value, resets the widget
	 * value to its default.
	 */
	public static void validate(Control c, String defaultV) {
		String value = getValueString(c);
		if (value == null && defaultV != null) {
			setValue(c, defaultV);
		}
	}

	/**
	 * If validation fails, resets the widget value to its default and throws an
	 * exception.
	 */
	public static void validate(Control c, Validator v, String defaultV, IRemoteFileManager fileManager) throws Exception {
		String value = getValueString(c);
		Regex reg = v.getRegex();
		String error = v.getErrorMessage();
		if (error == null) {
			error = ZEROSTR;
		}
		if (reg != null && !validateAgainstRegex(reg, value)) {
			throw new UnsatisfiedMatchException(error + CO + SP + reg.getExpression() + CM + SP + value);
		} else {
			FileMatch match = v.getFileInfo();
			try {
				if (match != null && !validate(match, value, fileManager)) {
					throw new UnsatisfiedMatchException(error + CO + SP + value);
				}
			} catch (CoreException ce) {
				throw new UnsatisfiedMatchException(ce);
			}
		}
	}

	private static boolean validate(FileMatch match, String value, IRemoteFileManager fileManager) throws CoreException {
		if (fileManager == null) {
			return false;
		}
		IFileStore rres = fileManager.getResource(value);
		IFileInfo info = rres.fetchInfo(EFS.NONE, new NullProgressMonitor());
		if (!info.exists()) {
			return false;
		}
		if (match.isIsDirectory() != info.isDirectory()) {
			return false;
		}
		Long len = match.getLength();
		if (len != null && len != info.getLength()) {
			return false;
		}

		/*
		 * Date comparison?
		 */

		for (String s : match.getAttribute()) {
		}
		return false;
	}

	private static boolean validateAgainstRegex(Regex reg, String value) {
		return new RegexImpl(reg).getMatched(value) != null;
	}
}
