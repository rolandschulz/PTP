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
package org.eclipse.ptp.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author Clement chu
 *
 */
public abstract class AbstractPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	protected IWorkbench fWorkbench;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		fWorkbench = workbench;
	}
	
	/** 
	 * Get Workbench
	 * @return
	 */
	protected IWorkbench getWorkbench() {
		return fWorkbench;
	}
	
	/** 
	 * Create group composite
	 * @param parent
	 * @param numColumns
	 * @param makeBalance
	 * @param labelText
	 * @return
	 */
	protected Composite createGroupComposite(Composite parent, int numColumns, boolean makeBalance, String labelText) {
        Group comp = new Group(parent, SWT.SHADOW_ETCHED_IN);
        GridLayout layout = new GridLayout(numColumns, makeBalance);
        comp.setLayout(layout);
        layout.marginLeft = 2;
        layout.marginTop = 2;
        layout.marginRight = 2;
        layout.marginBottom = 2;
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = SWT.CENTER ;
        gd.horizontalAlignment = SWT.FILL;
        comp.setLayoutData(gd);
        comp.setText(labelText);
        comp.setFont(parent.getFont());
        return comp;
    }
	
	/** 
	 * Create composite
	 * @param parent
	 * @param numColumns
	 * @return
	 */
	protected Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(numColumns, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		return composite;
	}	
	
	/** 
	 * Create check type button
	 * @param parent
	 * @param label
	 * @return
	 */
	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}
	
	/** 
	 * Create normal button
	 * @param parent
	 * @param label
	 * @param type
	 * @return
	 */
	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return button;
	}
	
	/** 
	 * Create combo box
	 * @param parent
	 * @param label
	 * @param data
	 * @param selectedData
	 * @return
	 */
	protected Combo createCombo(Composite parent, String label, String[] data, String selectedData) {
		createLabel(parent, label, SWT.DEFAULT, SWT.DEFAULT, SWT.LEFT);
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setItems(data);
		int selectedIndex = combo.indexOf(selectedData);
		combo.select((selectedIndex<0)?0:selectedIndex);		
		return combo;
	}
	
	/** 
	 * Create label
	 * @param parent
	 * @param text
	 * @param widthHint
	 * @param heightHint
	 * @param style
	 * @return
	 */
	protected Label createLabel(Composite parent, String text, int widthHint, int heightHint, int style) {
		Label label = new Label(parent, style);		
		label.setText(text);
		GridData gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = widthHint;
		gd.heightHint = heightHint;
		label.setLayoutData(gd);
		return label;
	}	
	
	/** 
	 * Create spacer
	 * @param composite
	 * @param columnSpan
	 */
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
		
	/** 
	 * Get pages
	 * @return
	 */
	protected IWorkbenchPage[] getPages() {
		final List<IWorkbenchPage> pages = new ArrayList<IWorkbenchPage>();
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				if (fWorkbench != null) {
					IWorkbenchWindow[] windows = fWorkbench.getWorkbenchWindows();
					for (int i=0; i<windows.length; i++) {
						pages.add(windows[i].getActivePage());
					}
				}
			}
		} );
		return (IWorkbenchPage[])pages.toArray(new IWorkbenchPage[0]);
	}   

	/** 
	 * Store preference values
	 */
	protected abstract void storeValues();
	
	/** 
	 * Set preference values
	 */
	protected abstract void setValues();
}
