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
package org.eclipse.ptp.debug.internal.ui.views.variable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.internal.ui.PVariableManager;
import org.eclipse.ptp.debug.internal.ui.PVariableManager.PVariableInfo;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * @author Clement chu
 */
public class PVariableDialog extends Dialog {
	public static final int NEW_MODE = 0;
	public static final int EDIT_MODE = 1;
	protected Text varText = null;
	protected Table varTable = null;
	protected Button checkBtn = null;
	protected PVariableView view = null;
	protected int mode = NEW_MODE;

	public PVariableDialog(PVariableView view) {
		this(view, NEW_MODE);
	}
	public PVariableDialog(PVariableView view, int mode) {
		super(view.getSite().getShell());
		this.view = view;
		this.mode = mode;
	}
	public void configureShell(Shell newShell) {
		newShell.setText(PVariableMessages.getString("PVariablesDialog.title"));
		super.configureShell(newShell);
	}
	/** Get OK Button
     * @return
     */
    public Button getOkButton() {
        return getButton(IDialogConstants.OK_ID);
    }
	private void createOthersSection(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.verticalSpacing = 25;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		checkBtn = new Button(comp, SWT.CHECK);
		checkBtn.setText(PVariableMessages.getString("PVariablesDialog.checkBtn"));
		checkBtn.setSelection(true);
	}

    /** Display the available variable in debugger
	 * @param parent
	 */
	private void createVarSection(Composite parent) {
		Group aGroup = new Group(parent, SWT.BORDER);
		aGroup.setText(PVariableMessages.getString("PVariablesDialog.varSection"));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		aGroup.setLayout(layout);
		aGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		
		Label availLabel = new Label(aGroup, SWT.NONE);
		availLabel.setText(PVariableMessages.getString("PVariablesDialog.availVar"));
		availLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
		
		varTable = new Table(aGroup, SWT.CHECK | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 300;
        gd.heightHint = 150;
		//gd.verticalSpan = 30;
		varTable.setLayoutData(gd);
		varTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((TableItem)e.item).getChecked()) {
					varText.setText("");
				}
				updateButtons();
			}
		});

		Label custLabel = new Label(aGroup, SWT.NONE);
		custLabel.setText(PVariableMessages.getString("PVariablesDialog.custVar"));
		custLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
		varText = new Text(aGroup, SWT.BORDER | SWT.NONE);
		varText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		varText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (varText.getText().length() > 0) {
					TableItem[] items = varTable.getItems();
					for (int i=0; i<items.length; i++) {
						items[i].setChecked(false);
					}
				}
				updateButtons();
			}
		});
		varText.setFocus();
	}
	private boolean anyTableItemChecked(Table table) {
		TableItem[] items = table.getItems();
		for (int i=0; i<items.length; i++) {
			if (items[i].getChecked())
				return true;
		}
		return false;
	}
	private void updateButtons() {
		boolean enabled = true;
		if (getSelectedVariables().length == 0) {
			enabled = false;
		}
		getOkButton().setEnabled(enabled);
	}
	/** Initialize the content in new section and variable section
	 * 
	 */
	protected void initContent() {
		PVariableInfo jVar = null;
		if (mode == EDIT_MODE) {
			ISelection selection = view.getSelection();
			if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
				jVar = (PVariableInfo)((IStructuredSelection)selection).getFirstElement();
				varText.setText(jVar.getName());
				checkBtn.setSelection(jVar.isEnabled());
			}
		}

		TableItem item = null;		
		for (String var : getAvailableVariables()) {
			item = new TableItem(varTable, SWT.NONE);
			item.setText(var);
			if (jVar != null) {
				item.setChecked(jVar.getName().equals(var));
			}
		}		
	}
	/** Get variables from Debug View
	 * @return variable names
	 */
	protected String[] getAvailableVariables() {
		ISelection selection = view.getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		if (selection.isEmpty()) {
			return new String[0];
		}
		if (selection instanceof IStructuredSelection) {
			Object target = ((IStructuredSelection)selection).getFirstElement();
			try {
				if (target instanceof IStackFrame) {
					return getVariables((IStackFrame)target);
				}
				else if (target instanceof IThread) {
					return getVariables(((IThread)target).getTopStackFrame());
				}
				else if (target instanceof IPDebugTarget) {
					IThread[] threads = ((IPDebugTarget)target).getThreads();
					if (threads.length > 0) {
						return getVariables(threads[0].getTopStackFrame());
					}
				}
			} catch (DebugException e) {
				return new String[0];
			}
		}
		return new String[0];
	}
	private String[] getVariables(IStackFrame frame) throws DebugException {
		if (frame == null) 
			return new String[0];
		
		IVariable[] vars = frame.getVariables();
		String[] varTexts = new String[vars.length];
		for (int j=0; j<vars.length; j++) {
			varTexts[j] = vars[j].getName();
		}
		return varTexts;
	}
	/** Create vertical space
	 * @param parent
	 * @param space
	 */
	protected void createVerticalSpan(Composite parent, int space) {
		Label label = new Label(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = space;
		label.setLayoutData(gd);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	public Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		switch(mode) {
		case NEW_MODE:
			getOkButton().setText("Create");
			break;
		case EDIT_MODE:
			getOkButton().setText("Edit");
			break;
		}
		updateButtons();
		return control;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);

		createVarSection(composite);
		createVerticalSpan(composite, 2);
		createOthersSection(composite);

		initContent();
		return composite;
	}
	protected String[] getSelectedAvailableVariables() {
		List<String> vars = new ArrayList<String>();
		TableItem[] items = varTable.getItems();
		for (int i=0; i<items.length; i++) {
			if (items[i].getChecked())
				vars.add(items[i].getText());
		}
		return (String[])vars.toArray(new String[0]);
	}
	protected String[] getSelectedVariables() {
		String[] vars = getSelectedAvailableVariables();
		if (vars.length == 0) {
			if (varText.getText().length() > 0) {
				return new String[] { varText.getText() };
			}
		}
		return vars;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		PVariableManager jobMgr = view.getUIManager().getJobVariableManager();
		IPJob job = view.getUIManager().getJob();
		String[] vars = getSelectedVariables();
		boolean checked = checkBtn.getSelection();

		switch(mode) {
		case NEW_MODE:
			//check duplicate variable
			for (int i=0; i<vars.length; i++) {
				try {
					jobMgr.addVariable(job, vars[i], checked);
				}
				catch (CoreException e) {
					PTPDebugUIPlugin.errorDialog("Duplicate variable", e.getStatus());
				}
			}
			break;
		case EDIT_MODE:
			ISelection selection = view.getSelection();
			if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
				PVariableInfo jVar = (PVariableInfo)((IStructuredSelection)selection).getFirstElement();
				for (String var : vars) {
					String newvar = jVar.getName().equals(var)?null:var;
					try {
						jobMgr.updateVariable(job, jVar.getName(), newvar, checked);
					}
					catch (CoreException e) {
						
					}
				}
			}
			break;
		}
		super.okPressed();
	}
}
