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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.internal.ui.PJobVariableManager;
import org.eclipse.ptp.debug.internal.ui.PJobVariableManager.JobVariable;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.model.IElementHandler;
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
	protected Table setTable = null;
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
		comp.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		
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
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		aGroup.setLayout(layout);
		aGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label(aGroup, SWT.NONE).setText(PVariableMessages.getString("PVariablesDialog.availVar"));
		
		varTable = new Table(aGroup, SWT.CHECK | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 300;
        gd.heightHint = 200;
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

		Composite comp2 = new Composite(aGroup, SWT.NONE);
		comp2.setLayout(new GridLayout(2, false));
		comp2.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		new Label(comp2, SWT.NONE).setText(PVariableMessages.getString("PVariablesDialog.custVar"));
		varText = new Text(comp2, SWT.BORDER | SWT.NONE);
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
	/** Create section for adding new variable
	 * @param parent
	 */
	private void createSetSection(Composite parent) {
		Group aGroup = new Group(parent, SWT.BORDER);
		aGroup.setText(PVariableMessages.getString("PVariablesDialog.setSection"));
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		aGroup.setLayout(layout);
		aGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		setTable = new Table(aGroup, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 300;
        gd.heightHint = 100;
		//gd.verticalSpan = 25;
		setTable.setLayoutData(gd);
		setTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem item = (TableItem)e.item;
				if (item.getChecked()) {
					if (item.getText().equals(IElementHandler.SET_ROOT_ID)) {
						setCheckTableItems(false);
					}
					else {
						//uncheck root if checked others
						setTable.getItem(0).setChecked(false);
					}
				}
				updateButtons();
			}	
		});
	}
	/** Set Set table items checked or not
	 * @param checked check or not
	 */
	private void setCheckTableItems(boolean checked) {
		TableItem[] items = setTable.getItems();
		for (int i=1; i<items.length; i++) {
			items[i].setChecked(checked);
		}
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
		if (getSelectedSets().length == 0) {
			enabled = false;
		}
		getOkButton().setEnabled(enabled);
	}
	/** Initialize the content in new section and variable section
	 * 
	 */
	protected void initContent() {
		JobVariable jVar = null;
		if (mode == EDIT_MODE) {
			ISelection selection = view.getSelection();
			if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
				jVar = (JobVariable)((IStructuredSelection)selection).getFirstElement();
				varText.setText(jVar.getVar());
				checkBtn.setSelection(jVar.isEnable());
			}
		}

		String[] sets = view.getUIManager().getSets(view.getUIManager().getCurrentJobId());
		TableItem item = null;		
		for (int i=0; i<sets.length; i++) {
			item = new TableItem(setTable, SWT.NONE);
			item.setText(sets[i]);
			if (jVar != null) {
				String[] selectedSets = jVar.getSets();
				for (int j=0; j<selectedSets.length; j++) {
					item.setChecked(selectedSets[j].equals(sets[i]));
				}
			}
		}

		String[] vars = getAvailableVariables();
		for (int i=0; i<vars.length; i++) {
			item = new TableItem(varTable, SWT.NONE);
			item.setText(vars[i]);
			if (jVar != null) {
				item.setChecked(jVar.getVar().equals(vars[i]));
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
		createSetSection(composite);
		createOthersSection(composite);

		initContent();
		
		return composite;
	}
	protected String[] getSelectedSets() {
		List sets = new ArrayList();
		TableItem[] items = setTable.getItems();
		for (int i=0; i<items.length; i++) {
			if (items[i].getChecked())
				sets.add(items[i].getText());
		}
		return (String[])sets.toArray(new String[0]);
	}
	protected String[] getSelectedAvailableVariables() {
		List vars = new ArrayList();
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
		PJobVariableManager jobMgr = view.getUIManager().getJobVariableManager();
		IPJob job = view.getUIManager().getCurrentJob();
		String[] vars = getSelectedVariables();
		String[] sets = getSelectedSets();
		boolean checked = checkBtn.getSelection();

		switch(mode) {
		case NEW_MODE:
			//check duplicate variable
			for (int i=0; i<vars.length; i++) {
				if (jobMgr.isContainVariable(job, vars[i])) {
					PTPDebugUIPlugin.errorDialog("Duplicate variable", new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "Variable (" + vars[i] + ") is added already.", null));
					return;
				}
			}
			for (int i=0; i<vars.length; i++) {
				jobMgr.addJobVariable(job, vars[i], sets, checked);
			}
			break;
		case EDIT_MODE:
			ISelection selection = view.getSelection();
			if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
				JobVariable varInfo = (JobVariable)((IStructuredSelection)selection).getFirstElement();
				jobMgr.removeJobVariable(job.getIDString(), varInfo.getVar());
				for (int i=0; i<vars.length; i++) {
					jobMgr.addJobVariable(job, vars[i], sets, checked);
				}
			}
			break;
		}
		super.okPressed();
	}
}
