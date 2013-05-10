/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw.tau.papitest;

//import org.eclipse.core.resources.ResourcesPlugin;
import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.etfw.tau.Activator;
import org.eclipse.ptp.etfw.tau.messages.Messages;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Launches a splash screen to prompt for global and tau-specific options for
 * building/launching applications
 * 
 * @author wspear
 * 
 */
public class PAPISplash extends Dialog {

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
		public void modifyText(ModifyEvent e) {
			// TODO Auto-generated method stub

		}

		public void propertyChange(PropertyChangeEvent event) {
			// TODO Auto-generated method stub

		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			final Object source = e.getSource();
			if (source == browsePapi) {
				handleXMLBrowseButtonSelected();
			}
		}
	}

	Text papiPath;
	Button browsePapi;

	Button[] papiCountRadios;

	protected WidgetListener listener = new WidgetListener();

	protected PAPISplash(Shell parentShell) {
		super(parentShell);

	}

	/**
	 * Defines the UI of the dialog, including options for enabling autorefresh,
	 * disabling autobuild and using the internal builder on AIX
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);

		// Label introlabel = new Label(composite, SWT.NONE);
		// introlabel.setText("Sp");
		final Composite papiCom = new Composite(parent, SWT.NONE);
		papiCom.setLayout(createGridLayout(1, false, 0, 0));
		papiCom.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		final Label tauarchComment = new Label(papiCom, SWT.WRAP);
		tauarchComment.setText(Messages.PAPISplash_PapiBin);
		papiPath = new Text(papiCom, SWT.BORDER | SWT.SINGLE);
		papiPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		papiPath.setText(Activator.getDefault().getPreferenceStore().getString(TestPAPI.papiLocationSelectionVar));

		browsePapi = new Button(papiCom, SWT.PUSH);
		browsePapi.setText(Messages.PAPISplash_Browse);
		browsePapi.addSelectionListener(listener);

		papiCountRadios = new Button[2];
		papiCountRadios[0] = new Button(parent, SWT.RADIO);
		papiCountRadios[0].setText(Messages.PAPISplash_PresetCounters);
		papiCountRadios[1] = new Button(parent, SWT.RADIO);
		papiCountRadios[1].setText(Messages.PAPISplash_NativeCounters);
		// papiCountRadios[2] = new Button(parent,SWT.RADIO);
		// papiCountRadios[2].setText("PAPI-C Selector");
		final int pType = Activator.getDefault().getPreferenceStore().getInt(TestPAPI.papiCounterTypeVar);
		if (pType < papiCountRadios.length) {
			papiCountRadios[pType].setSelection(true);
		}
		return composite;
	}

	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	protected void handleXMLBrowseButtonSelected() {
		final DirectoryDialog dialog = new DirectoryDialog(getShell());
		File path = null;
		final String correctPath = papiPath.getText();
		if (correctPath != null) {
			path = new File(correctPath);
			if (path.exists()) {
				dialog.setFilterPath(correctPath);
			}
		}

		dialog.setText(Messages.PAPISplash_SelectPapiBin);
		papiPath.setText(dialog.open());

	}

	/**
	 * Sets the selected options upon user confirmation
	 */
	@Override
	protected void okPressed() {

		Activator.getDefault().getPreferenceStore().setValue(TestPAPI.papiLocationSelectionVar, papiPath.getText());
		int papiType = 0;
		if (papiCountRadios[1].getSelection()) {
			papiType = 1;
		}
		// else if(papiCountRadios[2].getSelection())
		// papiType=2;
		Activator.getDefault().getPreferenceStore().setValue(TestPAPI.papiCounterTypeVar, papiType);

		super.okPressed();
	}

	protected GridData spanGridData(int style, int space) {
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
