/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @deprecated
 */
public class DebugPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
	static public final int SIMULATOR = 100;
	
	static private String[] DebuggerNameList = new String[] {
			"Debug Simulator" 
	};
	
	public static final String EMPTY_STRING = "";

	protected Combo combo = null;

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener { 
		public void widgetSelected(SelectionEvent e) {
			updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
			updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			updatePreferencePage();
		}
	}

	protected WidgetListener listener = new WidgetListener();

	protected Control createContents(Composite parent) { 
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.makeColumnsEqualWidth = true;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;

		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);

		createChooseDebuggerContents(composite);

		defaultSetting();
		return composite;
	}

	private void createChooseDebuggerContents(Composite parent) { 
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.makeColumnsEqualWidth = true;
		gridLayout.marginHeight = 10;
		gridLayout.marginWidth = 10;

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		
		aGroup.setLayout(gridLayout);
		aGroup.setLayoutData(gridData);
		aGroup.setText("Debugger");
		
		combo = new Combo(aGroup, SWT.READ_ONLY);
		combo.setLayoutData(gridData);
		combo.setItems(DebuggerNameList);
		combo.addSelectionListener(listener);
	}

	protected void defaultSetting() { 
		combo.select(0);
	}
	
	public void performDefaults() { 
		defaultSetting();
		updateApplyButton();
	}

	public boolean performOk() { 
		return true;
	}

	protected boolean isValidDebuggerChoice() { 
		int intchoice = combo.getSelectionIndex();
		if(intchoice != 1) {
			setErrorMessage("The debugger is not yet implemented.");
			setValid(false);
			return false;
		}
		
		return true;
	}

	protected void updatePreferencePage() { 
		setErrorMessage(null);
		setMessage(null);

		if (!isValidDebuggerChoice())
			return;
		
		setValid(true);
	}

	public void init(IWorkbench workbench) {
	}
}