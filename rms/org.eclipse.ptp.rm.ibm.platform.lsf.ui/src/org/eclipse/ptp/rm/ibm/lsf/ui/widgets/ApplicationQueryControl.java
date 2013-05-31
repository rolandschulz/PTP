// Copyright (c) 2013 IBM Corporation and others. All rights reserved. 
// This program and the accompanying materials are made available under the 
// terms of the Eclipse Public License v1.0s which accompanies this distribution, 
// and is available at http://www.eclipse.org/legal/epl-v10.html

package org.eclipse.ptp.rm.ibm.lsf.ui.widgets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.rm.jaxb.control.ui.IWidgetDescriptor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ApplicationQueryControl extends LSFQueryControl {

	/**
	 * Create the custom widget for the JAXB ui. In this case the widget is a
	 * push button that pops up a dialog with a list of active applications when
	 * the button is pushed.
	 * 
	 * @param parent
	 *            : Container for the widget
	 * @param wd
	 *            : Information about the custom widget
	 */
	public ApplicationQueryControl(Composite parent, final IWidgetDescriptor wd) {
		super(parent, wd);
	}

	@Override
	protected void configureQueryButton(Button button,
			final IRemoteConnection connection) {
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			/**
			 * Handle button press event. Pop up a dialog listing applications. If the user
			 * selects an application and clicks the ok button notify listeners that this
			 * widget has been modified.
			 * 
			 * @param e: The selection event
			 */
			public void widgetSelected(SelectionEvent e) {
				int selection;

				if (getQueryResponse(connection)) {
					dialog = new LSFQueryDialog(getShell(),
							Messages.ApplicationQueryControl_0, columnLabels,
							commandResponse, false);
					dialog.setSelectedValue(selectedValues);
					selection = dialog.open();
					if (selection == 0) {
						selectedValues = dialog.getSelectedValues();
						notifyListeners();
					}
				}
			}
		});
	}

	/**
	 * Issue the 'bapp' command to query the application list and set up the
	 * column heading and application data arrays.
	 * 
	 * @param connection
	 *            : Connection to the remote system
	 */
	@Override
	protected boolean getQueryResponse(IRemoteConnection connection) {
		IRemoteServices remoteServices;
		IRemoteProcessBuilder processBuilder;
		IRemoteProcess process;

		remoteServices = connection.getRemoteServices();
		processBuilder = remoteServices.getProcessBuilder(connection, "bapp", "-w"); //$NON-NLS-1$ //$NON-NLS-2$
		process = null;
		try {
			BufferedReader reader;
			String data;
			boolean headerLine;

			process = processBuilder.start();
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				// Do nothing
			}
			if (process.exitValue() == 0) {
				String columnData[];

				/*
				 * Read stderr and check for "No application profiles found."
				 * as the first line of output. Subsequent lines are ignored.
				 */
				reader = new BufferedReader(new InputStreamReader(
						process.getErrorStream()));
				data = reader.readLine();
				headerLine = true;
				while (data != null) {
					if (headerLine) {
						if (data.equals("No application profiles found.")) { //$NON-NLS-1$
							MessageDialog.openWarning(getShell(),
									Messages.ApplicationQueryControl_2,
									Messages.ApplicationQueryControl_3);
							reader.close();
							return false;
						}
						headerLine = false;
					}
					data = reader.readLine();
				}
				reader.close();

				/*
				 * Read stdout and tokenize each line of data into an array of
				 * blank-delimited strings. The first line of output is the
				 * column headings. Subsequent lines are reservation data.
				 */
				reader = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				data = reader.readLine();
				headerLine = true;
				commandResponse.clear();
				while (data != null) {
					if (headerLine) {
						columnLabels = data.split(" +"); //$NON-NLS-1$
						headerLine = false;
					} else {
						columnData = data.split(" +"); //$NON-NLS-1$
						commandResponse.add(columnData);
					}
					data = reader.readLine();
				}
				reader.close();
			}
		} catch (IOException e) {
			MessageDialog.openError(getShell(), "Error", //$NON-NLS-1$
					"Error querying reservations:\n" + e.getMessage()); //$NON-NLS-1$
			return false;
		}
		return true;
	}
}
