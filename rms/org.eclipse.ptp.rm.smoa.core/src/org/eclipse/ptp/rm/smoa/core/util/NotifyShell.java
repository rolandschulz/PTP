/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Class implementing a dialog independent to GUI actions, and not modal one.
 * (Java's MessageBox has no ability to be non-modal)
 */

public class NotifyShell {

	/**
	 * Open a non-modal hint-like dialog
	 */
	public static void open(final String title, final String message) {

		assert (title != null || message != null);

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				final Shell s = new Shell(SWT.SHELL_TRIM | SWT.ON_TOP);
				final GridLayout layout = new GridLayout();
				s.setLayout(layout);
				s.setLocation(10, 10);

				// Title
				if (title != null) {
					final Label t = new Label(s, SWT.WRAP);
					t.setText(title);
					final FontData fd = t.getFont().getFontData()[0];
					fd.setStyle(fd.getStyle() | SWT.BOLD);
					t.setFont(new Font(t.getDisplay(), fd));
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				}

				// Message
				if (message != null) {
					final Label msg = new Label(s, SWT.WRAP);
					msg.setLayoutData(new GridData(GridData.FILL_BOTH));
					msg.setText(message);
				}

				// add button for closing
				final Button ok = new Button(s, SWT.PUSH);
				ok.setText("Ok"); //$NON-NLS-1$
				ok.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				ok.addSelectionListener(new SelectionListener() {

					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						s.close();
					}
				});

				// layout properly
				s.pack();

				// correct if too wide
				final int newX = Math.min(s.getSize().x, s.getDisplay()
						.getClientArea().width - 20);
				if (newX != s.getSize().x) {
					s.setSize(newX, s.computeSize(newX, SWT.DEFAULT).y);
				}

				// show
				s.setVisible(true);
			}
		});
	}

	private NotifyShell() {
	}
}
