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
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;

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
}
