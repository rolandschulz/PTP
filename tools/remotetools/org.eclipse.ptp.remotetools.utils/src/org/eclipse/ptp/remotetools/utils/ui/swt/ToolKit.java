/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.utils.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * Set of useful GUI constructing methods.
 * Useful for creating GUI blocks that recur frequently
 * while building form (like Launcher tabs, Dialogs/Wizard tabs).
 * @author Daniel Felix Ferber
 * @since 1.1
 */
public class ToolKit {
	
	/**
	 * Creates a label and a text box and add them to the parent.
	 * The label is on the left of the text box.
	 * The box size fill available space.
	 * A ":" is automatically added to the label.
	 * Note: does not create a row for both widgets.
	 * 
	 * @param parent Composite that will be added to.
	 * @param labelString Label for the Text box
	 * @param valueString Initial value for the TextBox
	 * @return The text box.
	 */
	public static Text createTextWithLabel(
			Composite parent,
			String labelString, 
			String valueString) {
		return createTextWithLabel(parent, labelString, valueString, 0);
	}

	public static Text createTextWithLabel(
			Composite parent,
			String labelString, 
			String valueString,
			int numberOfChars) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelString + ":");
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		if (valueString == null) valueString = "";
		text.setText(valueString);
		if (numberOfChars <= 0) {
			text.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		} else {
			GC gc = new GC (text);
			FontMetrics fm = gc.getFontMetrics ();
			int width = numberOfChars * fm.getAverageCharWidth ();
			int height = fm.getHeight ();
			gc.dispose ();
			GridData layout = new GridData();
			layout.widthHint = width;
			layout.minimumWidth = width;
			text.setLayoutData(layout);
		}
		return text;
	}

	public static Text createPasswordWithLabel(
			Composite parent,
			String labelString, 
			String valueString) {
		Label label = new Label(parent,SWT.NONE);
		label.setText(labelString + ":");
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		if (valueString == null) valueString = "";
		text.setText(valueString);
		text.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		return text;
	}
	
	/**
	 * Creates a row containing a label and a text box.
	 * Adds this row to the parent.
	 * The label is on the left of the text box.
	 * The methods is called "Short" because the label on the left
	 * makes the available space for the text box 'shorter' as if the
	 * label were on the top of the text box.
	 * This layout is more space saving that the layout from createTextRow().
	 * The box size fill available space in the row.
	 * A ":" is automatically added to the label.
	 * 
	 * @param parent Composite that the row will be added to.
	 * @param labelString Label for the Text box
	 * @param valueString Initial value for the TextBox
	 * @return The text box.
	 */	
	public static Text createShortTextRow(
			Composite parent,
			String labelString, 
			String valueString) {

		GridLayout rowLayout = new GridLayout();
		rowLayout.numColumns = 2;
		rowLayout.marginWidth = 0;
		rowLayout.marginHeight = 0;
		Composite row = new Composite(parent, SWT.NONE);
		row.setLayout(rowLayout);
		row.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return createTextWithLabel(row, labelString, valueString);
	}
	
	/**
	 * Creates a row containing a label and a text box.
	 * Adds this row to the parent.
	 * The label is on the left of the text box.
	 * The methods is called "Short" because the label on the left
	 * makes the available space for the text box 'shorter' as if the
	 * label were on the top of the text box.
	 * The box size fill available space in the row.
	 * A ":" is automatically added to the label.
	 * 
	 * @param parent Composite that the row will be added to.
	 * @param labelString Label for the Text box
	 * @param valueString Initial value for the TextBox
	 * @return The text box.
	 */
	public static Text createTextRow(
			Composite parent,
			String labelString, 
			String valueString) {
		return createTextRow(parent, labelString, valueString, 0);
	}

	public static Text createTextRow(
			Composite parent,
			String labelString, 
			String valueString,
			int numRows) {
		GridLayout rowLayout = new GridLayout();
		rowLayout.verticalSpacing = 1;
		rowLayout.marginWidth = 0;
		rowLayout.marginHeight = 0;
		Composite row = new Composite(parent, SWT.NONE);
		row.setLayout(rowLayout);
		row.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(row, SWT.NONE);
		label.setText(labelString + ":");
		
		Text text = null;
		if (numRows == 0) {
			text = new Text(row, SWT.SINGLE | SWT.BORDER);
			text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		} else {
			text = new Text(row, SWT.MULTI | SWT.BORDER);
			GC gc = new GC (text);
			FontMetrics fm = gc.getFontMetrics ();
			int height = fm.getHeight () * numRows;
			gc.dispose ();
			GridData layout = new GridData(GridData.FILL_HORIZONTAL);
			layout.heightHint = height;
			layout.minimumWidth = SWT.DEFAULT;
			text.setLayoutData(layout);
		}
		if (valueString == null) valueString = "";
		text.setText(valueString);

		return text;
	}
	
	public static Text createFileFieldRow(
			Composite parent, 
			String labelString, 
			String valueString, 
			String title) {
		
		GridLayout rowLayout = new GridLayout();
		rowLayout.verticalSpacing = 1;
		rowLayout.marginWidth = 0;
		rowLayout.marginHeight = 0;
		Composite row = new Composite(parent, SWT.NONE);
		row.setLayout(rowLayout);
		row.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(row, SWT.NONE);
		label.setText(labelString + ":");
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		Composite item = new Composite(row, SWT.NONE);
		item.setLayout(layout);
		item.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Text text = new Text(item, SWT.SINGLE | SWT.BORDER);
		if (valueString == null) valueString = "";
		text.setText(valueString);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button button = new Button(item, SWT.PUSH);
		button.setText("Browse");
		button.addSelectionListener(new FileButtonSelectionListener(text, title));
		
		return text;
	}
	
	public static Text createDirectoryFieldRow(
			Composite parent, 
			String labelString, 
			String valueString, 
			String title,
			String message) {
		
		GridLayout rowLayout = new GridLayout();
		rowLayout.verticalSpacing = 1;
		rowLayout.marginWidth = 0;
		rowLayout.marginHeight = 0;
		Composite row = new Composite(parent, SWT.NONE);
		row.setLayout(rowLayout);
		row.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(row, SWT.NONE);
		label.setText(labelString + ":");
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		Composite item = new Composite(row, SWT.NONE);
		item.setLayout(layout);
		item.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Text text = new Text(item, SWT.SINGLE | SWT.BORDER);
		if (valueString == null) valueString = "";
		text.setText(valueString);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button button = new Button(item, SWT.PUSH);
		button.setText("Browse");
		button.addSelectionListener(new DirectoryButtonSelectionListener(text, title, message));
		
		return text;
	}

	/**
	 * Create a group where new rows can be added.
	 * Group is a box that surrounds other components. The group
	 * may have a title.
	 * 
	 * @param parent Composite that the row will be added to.
	 * @param title The text shown as title.
	 * @return
	 */
	public static Group createGroup(Composite parent, String title) {
		return createGroup(parent, title, 1, false);
	}
	
	/**
	 * Create a group where new rows can be added in more that one column.
	 * Group is a box that surrounds other components. The group
	 * may have a title.
	 * This layout is more space saving that only one row. Useful for
	 * many check boxes, small text boxes (use createTextWithLabel).
	 * 
	 * @param parent Composite that the row will be added to.
	 * @param title The text shown as title.
	 * @return
	 */
	public static Group createGroup(Composite parent, String title, int columns, boolean equalWidth) {
		GridLayout groupLayout = new GridLayout();
		groupLayout.verticalSpacing = 3;
		groupLayout.horizontalSpacing = 3;
		groupLayout.numColumns = columns;
		groupLayout.makeColumnsEqualWidth = equalWidth;
		Group group = new Group(parent, SWT.NONE);
		group.setText(title);
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return group;
	}

	public static List createListBoxRow(
			Composite parent,
			String labelString) {

		GridLayout rowLayout = new GridLayout();
		rowLayout.verticalSpacing = 1;
		rowLayout.marginWidth = 0;
		rowLayout.marginHeight = 0;
		Composite row = new Composite(parent, SWT.NONE);
		row.setLayout(rowLayout);
		row.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(row, SWT.NONE);
		label.setText(labelString + ":");
		
		List combo = new List(row, SWT.SINGLE);
		//combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return combo;
	}

	public static Combo createShortDropDownRow(Composite parent, String labelString) {
		GridLayout rowLayout = new GridLayout();
		rowLayout.verticalSpacing = 1;
		rowLayout.marginWidth = 0;
		rowLayout.marginHeight = 0;
		rowLayout.numColumns = 2;
		Composite row = new Composite(parent, SWT.NONE);
		row.setLayout(rowLayout);
		row.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(row, SWT.NONE);
		label.setText(labelString + ":");
		
		Combo combo = new Combo(row, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return combo;
	}

	public static Label createLabelRow(Composite parent, String string) {
		return createLabelRow(parent, string, 1);
	}

	public static Label createLabelRow(Composite parent, String string, int colSpan) {
		Label row = new Label(parent, SWT.NONE);
		row.setText(string);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = colSpan;
		row.setLayoutData(gridData);
		return row;
	}	
}
