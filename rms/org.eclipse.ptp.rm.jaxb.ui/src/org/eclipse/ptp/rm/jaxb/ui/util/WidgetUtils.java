/*******************************************************************************
 * Copyright (c) 2010 University of Illinois 
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 *                  - modified, eliminated unused methods 05/11/2010
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.xml.JAXBUtils;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

/**
 * A set of convenience wrappers around JFace and SWT widget construction
 * routines.
 * 
 * @author arossi
 */
public class WidgetUtils implements IJAXBNonNLSConstants {
	public static final Color DKBL = Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE);
	public static final Color DKMG = Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA);
	public static final Color DKRD = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);

	private WidgetUtils() {
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

	public static Group createAnonymousNonFillingGroup(Composite parent, int columns) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 1;
		data.verticalAlignment = SWT.LEFT;
		data.grabExcessVerticalSpace = false;

		GridLayout layout = new GridLayout();
		layout.numColumns = columns;
		layout.verticalSpacing = 1;

		Group group = new Group(parent, SWT.SHADOW_NONE | SWT.NO_TRIM);
		group.setLayout(layout);
		group.setLayoutData(data);

		return group;
	}

	public static Button createButton(Composite parent, String buttonText, Image image, int style, int colSpan, boolean fill,
			SelectionListener l) {

		Button button = new Button(parent, style);
		button.setText(buttonText);
		if (image != null) {
			button.setImage(image);
		}

		if (l != null) {
			button.addSelectionListener(l);
		}

		GridData data = new GridData();
		if (fill) {
			data.horizontalAlignment = SWT.FILL;
		}
		data.grabExcessHorizontalSpace = false;
		data.horizontalSpan = colSpan;
		button.setLayoutData(data);

		return button;
	}

	/**
	 * Convenience method for creating a button widget.
	 * 
	 * @param parent
	 * @param label
	 * @param type
	 * @return the button widget
	 */
	public static Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	/**
	 * Convenience method for creating a check button widget.
	 * 
	 * @param parent
	 * @param label
	 * @return the check button widget
	 */
	public static Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}

	public static Composite createComposite(Composite parent, int columns) {
		GridLayout layout = new GridLayout();
		layout.numColumns = columns;
		layout.verticalSpacing = 1;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(layout);
		return composite;
	}

	public static Group createFillingGroup(Composite parent, String text, int columns, int colSpan, boolean verticalFill) {
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = verticalFill;
		if (colSpan != -1) {
			data.horizontalSpan = colSpan;
		}

		GridLayout layout = new GridLayout();
		layout.numColumns = columns;
		layout.verticalSpacing = 9;

		Group group = new Group(parent, SWT.NO_TRIM | SWT.SHADOW_NONE);
		if (text != null) {
			group.setText(text);
		}
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

	/**
	 * Convenience method for creating a grid layout.
	 * 
	 * @param columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return the new grid layout
	 */
	public static GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	public static Combo createItemCombo(Composite container, String labelString, String[] items, String initial, String tooltip,
			boolean fill, ModifyListener listener, int colSpan) {
		if (labelString != null) {
			Label label = new Label(container, SWT.NONE);
			label.setText(labelString);
			if (tooltip != null) {
				label.setToolTipText(tooltip);
			}
		}

		GridData data = new GridData();
		if (fill) {
			data.horizontalAlignment = SWT.FILL;
		}
		data.grabExcessHorizontalSpace = false;
		if (colSpan != -1) {
			data.horizontalSpan = colSpan;
		}
		data.widthHint = 100;

		Combo combo = new Combo(container, SWT.BORDER);
		combo.setItems(items);
		combo.setLayoutData(data);
		if (initial != null) {
			combo.setText(initial);
		}
		if (listener != null) {
			combo.addModifyListener(listener);
		}
		return combo;
	}

	public static Label createLabel(Composite container, String text, int style, int colSpan) {
		GridData data = new GridData();
		data.horizontalSpan = colSpan;

		Label label = new Label(container, style);
		if (text == null) {
			text = ZEROSTR;
		}
		label.setText(text.trim());
		label.setLayoutData(data);
		return label;
	}

	/**
	 * Creates the dialog when the target "Options..." button is selected.
	 * Override if you want to provide your own dialog.
	 * 
	 * @param parent
	 *            the parent composite to contain the dialog area
	 * @return the target options string
	 */
	public static String createOptionsDialog(Shell shell, String initialOptions) {
		InputDialog dialog = new InputDialog(shell, Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_14,
				Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_15, initialOptions, null);
		if (dialog.open() == Dialog.OK) {
			return dialog.getValue();
		}
		return initialOptions;
	}

	/**
	 * Creates an new radio button instance and sets the default layout data.
	 * 
	 * @param group
	 *            the composite in which to create the radio button
	 * @param label
	 *            the string to set into the radio button
	 * @param value
	 *            the string to identify radio button
	 * @return the new radio button
	 */
	public static Button createRadioButton(Composite parent, String label, String value, SelectionListener listener) {
		Button button = createButton(parent, label, SWT.RADIO | SWT.LEFT);
		button.setData((null == value) ? label : value);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		button.setLayoutData(data);
		if (null != listener) {
			button.addSelectionListener(listener);
		}
		return button;
	}

	public static Spinner createSpinner(Composite container, String labelString, int min, int max, int initial, int colSpan,
			boolean fill, ModifyListener listener) {
		if (labelString != null) {
			GridData data = new GridData();
			Label label = new Label(container, SWT.NONE);
			label.setText(labelString);
			label.setLayoutData(data);
		}

		GridData data = new GridData();
		if (fill) {
			data.horizontalAlignment = SWT.FILL;
		}
		data.grabExcessHorizontalSpace = false;
		data.horizontalSpan = colSpan;

		Spinner s = new Spinner(container, SWT.NONE);
		s.setMaximum(max);
		s.setMinimum(min);
		s.setSelection(initial);
		s.setLayoutData(data);
		if (listener != null) {
			s.addModifyListener(listener);
		}
		return s;
	}

	public static Text createText(Composite container, String initialValue, boolean fill, ModifyListener listener, Color color) {

		GridData data = new GridData();
		if (fill) {
			data.horizontalAlignment = SWT.FILL;
		}
		data.grabExcessHorizontalSpace = true;
		Text text = new Text(container, SWT.BORDER);
		if (color != null) {
			text.setBackground(color);
		}
		text.setLayoutData(data);
		if (initialValue != null) {
			text.setText(initialValue);
		}
		if (listener != null) {
			text.addModifyListener(listener);
		}
		return text;
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

	public static void openIDEEditor(final String file) throws IOException {
		final URL fUrl = FileLocator.toFileURL(JAXBUtils.getURL(file));
		new UIJob(Messages.WidgetUtils_openIDEEditor) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					URI uri = fUrl.toURI();
					File fileToOpen = new File(uri);
					if (fileToOpen.exists() && fileToOpen.isFile()) {
						IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
						IDE.openEditorOnFileStore(page, fileStore);
					}
				} catch (Throwable e) {
					return new Status(Status.ERROR, JAXBUIPlugin.PLUGIN_ID, Status.ERROR, e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
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

	/**
	 * @param style
	 * @param space
	 * @return
	 */
	public static GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}
}
