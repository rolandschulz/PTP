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
package org.eclipse.ptp.debug.internal.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IPVariableManager;
import org.eclipse.ptp.debug.internal.ui.actions.RemoveAllPVariableActionDelegate;
import org.eclipse.ptp.debug.internal.ui.actions.RemovePVariableActionDelegate;
import org.eclipse.ptp.debug.internal.ui.actions.UpdateVariablesActionDelegate;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Clement chu
 *
 */
public class PTPVariablesDialog extends Dialog {
	protected ListViewer listViewer = null;
	protected IPVariableManager manager = null;
	protected IPJob current_job = null;

	public PTPVariablesDialog(Shell parent) {
		super(parent);
		manager = PTPDebugCorePlugin.getPVariableManager();
		current_job = PTPDebugUIPlugin.getDefault().getUIDebugManager().getCurrentJob();
	}
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(DialogMessages.getString("PTPVariablesDialog.name"));
		shell.setSize(300, 400);
	}
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		Composite result = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		result.setLayout(layout);
		result.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING));
		new Label(result, SWT.NONE).setText(DialogMessages.getString("PTPVariablesDialog.label"));
		listViewer = new ListViewer(result, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		listViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
		listViewer.getList().setFont(parent.getFont());
		listViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof String) {
					return (String) element;
				}
				return "";
			}
		});
		listViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {}
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof String[])
					return (String[]) inputElement;
				return new String[0];
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		listViewer.setInput(manager.getVariables(current_job));
		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				String selection = getSelection();
	    		if (selection != null) {
	    			RemovePVariableActionDelegate.doAction(null, selection);
	    			listViewer.setInput(manager.getVariables(current_job));
	    		}			
			}
		});
		applyDialogFont(composite);
		
		createMenu(listViewer.getList());
		return composite;
	}
	protected void createMenu(Control control) {
	    Menu menu = new Menu(getShell(), SWT.POP_UP);
	    MenuItem addWatchMItem = new MenuItem(menu, SWT.PUSH);
	    addWatchMItem.setText("New Watchpoint");
	    addWatchMItem.addSelectionListener(new SelectionListener() {
	    	public void widgetSelected(SelectionEvent e) {
	    		//TODO
	    	}
	    	public void widgetDefaultSelected(SelectionEvent e) {}	    	
	    });
	    MenuItem removeSelectedVarMItem = new MenuItem(menu, SWT.PUSH);
	    removeSelectedVarMItem.setText("Remove variable");
	    removeSelectedVarMItem.addSelectionListener(new SelectionListener() {
	    	public void widgetSelected(SelectionEvent e) {
	    		String selection = getSelection();
	    		if (selection != null) {
	    			RemovePVariableActionDelegate.doAction(null, selection);
	    			listViewer.setInput(manager.getVariables(current_job));
	    		}
	    	}
	    	public void widgetDefaultSelected(SelectionEvent e) {}	    	
	    });
	    
	    new MenuItem(menu, SWT.SEPARATOR);
	    
	    MenuItem updateAllMItem = new MenuItem(menu, SWT.PUSH);
	    updateAllMItem.setText("Refresh All");
	    updateAllMItem.addSelectionListener(new SelectionListener() {
	    	public void widgetSelected(SelectionEvent e) {
	    		UpdateVariablesActionDelegate.doAction(null);
	    	}
	    	public void widgetDefaultSelected(SelectionEvent e) {}	    	
	    });
	    
	    MenuItem removeAllMItem = new MenuItem(menu, SWT.PUSH);
	    removeAllMItem.setText("Remove All");
	    removeAllMItem.addSelectionListener(new SelectionListener() {
	    	public void widgetSelected(SelectionEvent e) {
    			RemoveAllPVariableActionDelegate.doAction(null);
    			listViewer.setInput(manager.getVariables(current_job));
	    	}
	    	public void widgetDefaultSelected(SelectionEvent e) {}	    	
	    });

	    menu.addMenuListener(new MenuListener() {
	    	public void menuHidden(MenuEvent e) {}
	    	public void menuShown(MenuEvent e) {
	    	    boolean hasMoreVar = manager.hasVariable(current_job);
	    	    String selection = getSelection();
	    		Menu menu = (Menu)e.getSource();
	    		if (menu != null) {
	    			menu.getItem(1).setEnabled(selection != null);
	    	    	menu.getItem(2).setEnabled(hasMoreVar);	    	    	
	    	    	menu.getItem(3).setEnabled(hasMoreVar);	    	    	
	    		}
	    	}	    	
	    });	    
	    control.setMenu(menu);	    
	}
	protected String getSelection() {
		ISelection selection = listViewer.getSelection();
		if (!selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				return (String)((IStructuredSelection) selection).getFirstElement();
			}
		}
		return null;
	}
}
