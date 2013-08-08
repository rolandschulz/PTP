// Copyright (c) 2013 IBM Corporation and others. All rights reserved. 
// This program and the accompanying materials are made available under the 
// terms of the Eclipse Public License v1.0s which accompanies this distribution, 
// and is available at http://www.eclipse.org/legal/epl-v10.html

package org.eclipse.ptp.rm.ibm.lsf.ui.widgets;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.ibm.lsf.ui.LSFCommand;
import org.eclipse.ptp.rm.jaxb.control.ui.IWidgetDescriptor2;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ApplicationQueryControl extends LSFQueryControl {
	private static final String queryCommand[] = {"bapp", "-w"}; //$NON-NLS-1$ //$NON-NLS-2$

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
	public ApplicationQueryControl(Composite parent, final IWidgetDescriptor2 wd) {
		super(parent, wd);
		queryTitle = Messages.ApplicationQueryTitle;
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
				getQueryResponse(connection);
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
	protected void getQueryResponse(IRemoteConnection connection) {
		try {
			IStatus runStatus;
			LSFCommand command;
			
			command = new LSFCommand(Messages.ApplicationCommandDesc, connection, queryCommand);
			widgetDescriptor.getLaunchConfigurationDialog().run(true, true, command);
			runStatus = command.getRunStatus();
			processCommandResponse(command, runStatus);
		} catch (InvocationTargetException e) {
			// Do nothing
		} catch (InterruptedException e) {
			// Do nothing
		}
	}
}
