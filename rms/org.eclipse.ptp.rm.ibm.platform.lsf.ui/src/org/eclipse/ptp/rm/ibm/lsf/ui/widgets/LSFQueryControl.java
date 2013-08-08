// Copyright (c) 2013 IBM Corporation and others. All rights reserved. 
// This program and the accompanying materials are made available under the 
// terms of the Eclipse Public License v1.0s which accompanies this distribution, 
// and is available at http://www.eclipse.org/legal/epl-v10.html

package org.eclipse.ptp.rm.ibm.lsf.ui.widgets;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.ibm.lsf.ui.LSFCommand;
import org.eclipse.ptp.rm.jaxb.control.ui.IWidgetDescriptor2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public abstract class LSFQueryControl extends Composite {

	protected String columnLabels[];
	protected Vector<String[]> commandResponse;
	protected LSFQueryDialog dialog;
	private final List<ModifyListener> modifyListeners = new LinkedList<ModifyListener>();
	protected String selectedValues = ""; //$NON-NLS-1$
	protected String queryTitle;
	protected final IWidgetDescriptor2 widgetDescriptor;


	/**
	 * Create a custom UI widget (pushbutton) in the JAXB UI
	 * @param parent - Parent of this widget
	 * @param wd - Widget attributes
	 */
	public LSFQueryControl(Composite parent, final IWidgetDescriptor2 wd) {
		super(parent, wd.getStyle());
		widgetDescriptor = wd;
		setLayout(new FillLayout());
		setLayoutData(wd.getLayoutData());
		Button button = new Button(this, SWT.PUSH);
		button.setText(wd.getFixedText());
		button.setToolTipText(wd.getToolTipText());
		commandResponse = new Vector<String[]>();
		configureQueryButton(button, wd.getRemoteConnection());
	}

	/**
	 * Add a listener for modifications to this widget.
	 * 
	 * @param listener
	 */
	public void addModifyListener(ModifyListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		modifyListeners.add(listener);
	}

	protected abstract void configureQueryButton(Button button,
			IRemoteConnection connection);

	protected abstract void getQueryResponse(IRemoteConnection connection);

	/**
	 * Get the selected application name
	 * 
	 * @return Application name
	 */
	public String getSelectedValue() {
		return selectedValues;
	}

	/**
	 * Notify modification listeners that this widget has been modified
	 */
	protected void notifyListeners() {
		for (final ModifyListener listener : modifyListeners) {
			listener.modifyText(null);
		}
	}

	/**
	 * Remove the listener from the list of modify listeners
	 * 
	 * @param listener
	 *            : The listener to remove
	 */
	public void removeModifyListener(ModifyListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		modifyListeners.remove(listener);
	}

	/**
	 * Set the initial (default) value of this widget
	 * 
	 * @param value
	 *            Value to set
	 */
	public void setSelectedValues(String value) {
		selectedValues = value;
	}

	/**
	 * Indicate if a value was selected from the list widget in the dialog
	 * displayed for this widget
	 * 
	 * @return Flag indicating a value was selected from the list in the dialog
	 *         box
	 */
	public boolean widgetSelected() {
		if (selectedValues == null) {
			return false;
		}
		return true;
	}
	
	protected void processCommandResponse(LSFCommand command, IStatus runStatus) {
		int selection;
		
		switch (runStatus.getSeverity()) {
		case IStatus.OK:
			dialog = new LSFQueryDialog(getShell(), queryTitle,
					command.getColumnLabels(), command
							.getCommandResponse(), true);
			dialog.setSelectedValue(selectedValues);
			selection = dialog.open();
			if (selection == 0) {
				selectedValues = dialog.getSelectedValues();
				notifyListeners();
			}
			break;
		case IStatus.INFO:
			MessageDialog.openInformation(getShell(),
					Messages.InformationalMessage,
					runStatus.getMessage());
			break;
		case IStatus.WARNING:
			MessageDialog.openInformation(getShell(),
					Messages.WarningMessageLabel,
					runStatus.getMessage());
			break;
		case IStatus.ERROR:
			MessageDialog.openInformation(getShell(),
					Messages.ErrorMessage,
					runStatus.getMessage());
			break;
		}
	}
}
