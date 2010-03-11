/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.rdt.ui.wizards.DStoreServerWidget.FieldModifier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class DStoreEnvironmentPage extends Composite {
	public class EnvInputDialog extends Dialog {
	    private Text variableText;
	    private Text valueText;
	    
	    private String variable;
	    private String value;

	    public EnvInputDialog(Shell parentShell) {
	        super(parentShell);
	    }

	    public String getValue() {
	        return value;
	    }

	    public String getVariable() {
	        return variable;
	    }

	    /*
	     * (non-Javadoc) Method declared on Dialog.
	     */
	    protected void buttonPressed(int buttonId) {
	        if (buttonId == IDialogConstants.OK_ID) {
	            value = valueText.getText();
	            variable = variableText.getText();
	        } else {
	            value = null;
	            variable = null;
	        }
	        super.buttonPressed(buttonId);
	    }

	    /*
	     * (non-Javadoc)
	     * 
	     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	     */
	    protected void createButtonsForButtonBar(Composite parent) {
	        // create OK and Cancel buttons by default
	        createButton(parent, IDialogConstants.OK_ID,
	                IDialogConstants.OK_LABEL, true);
	        createButton(parent, IDialogConstants.CANCEL_ID,
	                IDialogConstants.CANCEL_LABEL, false);
	    }
	    
	    /*
	     * (non-Javadoc) Method declared on Dialog.
	     */
	    protected Control createDialogArea(Composite parent) {
	        // create composite
	        Composite composite = (Composite) super.createDialogArea(parent);
	        // create message
            Label variableLabel = new Label(composite, SWT.WRAP);
            variableLabel.setText("Variable"); //$NON-NLS-1$
            GridData data = new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_CENTER);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            variableLabel.setLayoutData(data);
            variableLabel.setFont(parent.getFont());

            variableText = new Text(composite, SWT.SINGLE | SWT.BORDER);
            variableText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
	                | GridData.HORIZONTAL_ALIGN_FILL));
            Label valueLabel = new Label(composite, SWT.WRAP);
            valueLabel.setText("Variable"); //$NON-NLS-1$
            data = new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_CENTER);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            valueLabel.setLayoutData(data);
            valueLabel.setFont(parent.getFont());

            valueText = new Text(composite, SWT.SINGLE | SWT.BORDER);
            valueText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
	                | GridData.HORIZONTAL_ALIGN_FILL));
	        applyDialogFont(composite);
	        return composite;
	    }
	}

	///private final Label label;
	private Table table;
	private Button addButton;
	private Button removeButton;
	
	private ListenerList modifyListeners = new ListenerList();
	
	public DStoreEnvironmentPage(Composite parent, int style) {
		super(parent, style);
		
		GridLayout layout = new GridLayout(1, false);
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group group = new Group(this, SWT.NONE);
		group.setText("Environment Variables"); //$NON-NLS-1$
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite tableComp = new Composite(group, SWT.NONE);
		tableComp.setLayout(new GridLayout(2, false));
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		table = new Table(tableComp, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn col1 = new TableColumn(table, SWT.NONE);
		col1.setText("Variable"); //$NON-NLS-1$
		col1.setWidth(100);
		col1.setResizable(true);
		
		TableColumn col2 = new TableColumn(table, SWT.NONE);
		col2.setText("Value"); //$NON-NLS-1$
		col2.setWidth(200);
		col2.setResizable(true);

		Composite buttonComp = new Composite(group, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, false));
		buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		addButton = new Button(buttonComp, SWT.NONE);
		addButton.setText("Add..."); //$NON-NLS-1$
		GridData addButtonData = new GridData(SWT.FILL, SWT.FILL, false, false);
		addButtonData.widthHint = 80;
		addButton.setLayoutData(addButtonData);
		addButton.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAddButtonSelected();
			}
		});

		removeButton = new Button(buttonComp, SWT.NONE);
		removeButton.setText("Remove"); //$NON-NLS-1$
		GridData removeButtonData = new GridData(SWT.FILL, SWT.FILL, false, false);
		removeButtonData.widthHint = 80;
		removeButton.setLayoutData(removeButtonData);
		removeButton.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemoveButtonSelected();
			}		
		});

	}
	
	public void addModifyListener(ModifyListener listener) {
		modifyListeners.add(listener);
	}
	
	public void removeModifyListener(ModifyListener listener) {
		modifyListeners.remove(listener);
	}
	
	public String getEnvironment() {
		String env = ""; //$NON-NLS-1$
		
		for (int i = 0; i < table.getItemCount(); i++) {
			if (i > 0) {
				env += "\n"; //$NON-NLS-1$
			}
			TableItem item = table.getItem(i);
			env += item.getText(0) + "=" + item.getText(1); //$NON-NLS-1$
		}
		
		return env;
	}
	
	public void setEnvironment(String env) {
		table.clearAll();
		if (env != null) {
			for (String envs : env.split("\n")) { //$NON-NLS-1$
				String[] envVar = envs.split("="); //$NON-NLS-1$
				if (envVar.length == 2) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(0, envVar[0]);
					item.setText(1, envVar[1]);
				}
			}
		}
	}
	
	protected void handleAddButtonSelected() {
		EnvInputDialog dialog = new EnvInputDialog(getShell()); 
		if (dialog.open() != Window.OK) {
			return;
		}
		
		String var = dialog.getVariable();
		String value = dialog.getValue();
		
		if (var != null && value != null && var.length() > 0 && value.length() >0) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, var.trim());
			item.setText(1, value.trim());
		}
		
		Event e = new Event();
		e.data = FieldModifier.VALUE_DSTORE_ENV;
		notifyListeners(new ModifyEvent(e));
	}
	
	protected void handleRemoveButtonSelected() {
		int index = table.getSelectionIndex();
		if (index >= 0) {
			table.remove(index);
		}
	}
	
	private void notifyListeners(ModifyEvent e) {
		for (Object listener : modifyListeners.getListeners()) {
			((ModifyListener)listener).modifyText(e);
		}
	}
}
