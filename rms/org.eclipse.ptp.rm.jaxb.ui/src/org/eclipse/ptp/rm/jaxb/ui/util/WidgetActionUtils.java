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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class WidgetActionUtils implements IJAXBUINonNLSConstants {

	private WidgetActionUtils() {
	}

	/**
	 * Create a dialog that allows the user to select a file in the workspace.
	 * 
	 * @return selected file
	 */
	public static String browseWorkspace(Shell shell) {
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		FileDialog dialog = new FileDialog(shell);
		dialog.setFileName(path.toFile().getAbsolutePath());
		return dialog.open();
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
		if (combo.isDisposed()) {
			return ZEROSTR;
		}
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
		if (combo.isDisposed()) {
			return ZEROSTR;
		}
		int style = combo.getStyle();
		boolean readOnly = style == (style | SWT.READ_ONLY);
		String[] items = combo.getItems();
		if (items.length == 0) {
			return ZEROSTR;
		}
		int i = 0;
		if (name == null) {
			name = ZEROSTR;
		}
		for (; i < items.length; i++) {
			if (items[i].equals(name)) {
				combo.select(i);
				break;
			}
		}

		if (i == items.length) {
			if (readOnly) {
				i = 0;
			} else {
				List<String> newItems = new ArrayList<String>();
				for (String item : items) {
					newItems.add(item.trim());
				}
				newItems.add(name.trim());
				combo.setItems(newItems.toArray(new String[0]));
				i = newItems.size() - 1;
			}
		}
		combo.select(i);
		return combo.getItem(i);
	}

	public static void validate(String value, Validator v, IRemoteFileManager fileManager) throws Exception {
		Regex reg = v.getRegex();
		String error = v.getErrorMessage();

		if (error == null) {
			error = ZEROSTR;
		}

		if (reg != null && new RegexImpl(reg).getMatched(value) == null) {
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

	public static void warningMessage(Shell s, String message, String title) {
		MessageDialog.openWarning(s, title, message + LINE_SEP);
	}

	private static int getEfsAttributeValue(String efsAttrStr) {
		int attributes = 0;
		String[] split = efsAttrStr.split(PIP);
		for (String s : split) {
			s = s.trim();
			if (ATTRIBUTE_READ_ONLY.equals(s)) {
				attributes |= EFS.ATTRIBUTE_READ_ONLY;
			} else if (ATTRIBUTE_IMMUTABLE.equals(s)) {
				attributes |= EFS.ATTRIBUTE_IMMUTABLE;
			} else if (ATTRIBUTE_OWNER_READ.equals(s)) {
				attributes |= EFS.ATTRIBUTE_OWNER_READ;
			} else if (ATTRIBUTE_OWNER_WRITE.equals(s)) {
				attributes |= EFS.ATTRIBUTE_OWNER_WRITE;
			} else if (ATTRIBUTE_OWNER_EXECUTE.equals(s)) {
				attributes |= EFS.ATTRIBUTE_OWNER_EXECUTE;
			} else if (ATTRIBUTE_GROUP_READ.equals(s)) {
				attributes |= EFS.ATTRIBUTE_GROUP_READ;
			} else if (ATTRIBUTE_GROUP_WRITE.equals(s)) {
				attributes |= EFS.ATTRIBUTE_GROUP_WRITE;
			} else if (ATTRIBUTE_GROUP_EXECUTE.equals(s)) {
				attributes |= EFS.ATTRIBUTE_GROUP_EXECUTE;
			} else if (ATTRIBUTE_OTHER_READ.equals(s)) {
				attributes |= EFS.ATTRIBUTE_OTHER_READ;
			} else if (ATTRIBUTE_OTHER_WRITE.equals(s)) {
				attributes |= EFS.ATTRIBUTE_OTHER_WRITE;
			} else if (ATTRIBUTE_OTHER_EXECUTE.equals(s)) {
				attributes |= EFS.ATTRIBUTE_OTHER_EXECUTE;
			} else if (ATTRIBUTE_EXECUTABLE.equals(s)) {
				attributes |= EFS.ATTRIBUTE_EXECUTABLE;
			} else if (ATTRIBUTE_ARCHIVE.equals(s)) {
				attributes |= EFS.ATTRIBUTE_ARCHIVE;
			} else if (ATTRIBUTE_HIDDEN.equals(s)) {
				attributes |= EFS.ATTRIBUTE_HIDDEN;
			} else if (ATTRIBUTE_SYMLINK.equals(s)) {
				attributes |= EFS.ATTRIBUTE_SYMLINK;
			} else if (ATTRIBUTE_LINK_TARGET.equals(s)) {
				attributes |= EFS.ATTRIBUTE_LINK_TARGET;
			}
		}
		return attributes;
	}

	private static Long getTimeInMillis(String dateTime) {
		DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		try {
			Date d = formatter.parse(dateTime);
			return new Long(d.getTime());
		} catch (ParseException pe) {
			return null;
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
		String date = match.getLastModifiedAfter();
		if (date != null) {
			Long t = getTimeInMillis(date);
			if (t != null && t > info.getLastModified()) {
				return false;
			}
		}

		date = match.getLastModifiedBefore();
		if (date != null) {
			Long t = getTimeInMillis(date);
			if (t != null && t < info.getLastModified()) {
				return false;
			}
		}

		String attributes = match.getEfsAttributes();
		int a = getEfsAttributeValue(attributes);
		if (!info.getAttribute(a)) {
			return false;
		}

		return true;
	}
}
