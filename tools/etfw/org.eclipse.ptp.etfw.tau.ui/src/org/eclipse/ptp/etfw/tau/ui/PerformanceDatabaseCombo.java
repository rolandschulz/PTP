/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.tau.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.etfw.tau.perfdmf.PerfDMFUIPlugin;
import org.eclipse.ptp.etfw.tau.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractWidget;
import org.eclipse.ptp.rm.jaxb.control.ui.IWidgetDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * This is a re-implementation of part of the TAUDataTab for selecting the performance database and makes it available to the
 * TAU JAXB tool implementation.
 * 
 * @TODO Finish implementing this advanced tab and test that it works correctly.
 * 
 * @see TAUDataTab
 * 
 * @author Chris Navarro
 * 
 */
public class PerformanceDatabaseCombo extends AbstractWidget {

	private final Combo combo;

	public PerformanceDatabaseCombo(Composite parent, IWidgetDescriptor wd) {
		super(parent, wd);

		setLayout(new GridLayout(1, false));

		combo = new Combo(this, SWT.READ_ONLY);
		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		String[] dbs = null;
		try {
			dbs = PerfDMFUIPlugin.getPerfDMFView().getDatabaseNames();
		} catch (java.lang.NoClassDefFoundError e) {
			MessageDialog.openError(this.getShell(), Messages.PerformanceDatabaseCombo_TauJarDialogTitle,
					Messages.PerformanceDatabaseCombo_TauJarsNotFound);
		}

		if (dbs == null || dbs.length < 1) {
			combo.setItems(new String[] { Messages.PerformanceDatabaseCombo_NoDatabasesAvailable });
		} else {
			combo.setItems(dbs);
		}
		combo.select(0);

	}

	public Combo getCombo() {
		return combo;
	}

}
