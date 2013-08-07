// Copyright (c) 2013 IBM Corporation and others. All rights reserved. 
// This program and the accompanying materials are made available under the 
// terms of the Eclipse Public License v1.0s which accompanies this distribution, 
// and is available at http://www.eclipse.org/legal/epl-v10.html

package org.eclipse.ptp.rm.ibm.lsf.ui.widgets;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.ibm.lsf.ui.LSFCommand;
import org.eclipse.ptp.rm.jaxb.control.ui.IWidgetDescriptor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class QueueQueryControl extends LSFQueryControl {

	private static final String queryCommand[] = { "bqueues", "-w" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Create the custom widget for the JAXB ui. In this case the widget is a
	 * push button that pops up a dialog with a list of active queues when the
	 * button is pushed.
	 * 
	 * @param parent
	 *            : Container for the widget
	 * @param wd
	 *            : Information about the custom widget
	 */
	public QueueQueryControl(Composite parent, final IWidgetDescriptor wd) {
		super(parent, wd);
		queryTitle = Messages.JobQueueTitle;
	}

	@Override
	protected void configureQueryButton(Button button,
			final IRemoteConnection connection) {
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			/**
			 * Handle button press event. Pop up a dialog listing job queues. If the user
			 * selects a queue and clicks the ok button notify listeners that this
			 * widget has been modified.
			 * 
			 * @param e: The selection event
			 */
			public void widgetSelected(SelectionEvent e) {
				int selection;

				getQueryResponse(connection);
			}
		});
	}

	/**
	 * Issue the 'bqueues' command to query the available queues and set up the
	 * column heading and queue data arrays.
	 * 
	 * @param connection
	 *            : Connection to the remote system
	 */
	@Override
	protected void getQueryResponse(IRemoteConnection connection) {
		queueQuery = new LSFCommand(Messages.QueueCommandDesc, connection, queryCommand);
		queueQuery.setUser(true);
		queueQuery.addJobChangeListener(jobListener);
		queueQuery.schedule();
	}
}
