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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Clement chu
 * 
 */
public abstract class VariableDialog extends Dialog {
	protected IStackFrame frame = null;
	protected ListViewer listViewer = null;
	protected IVariable selectedVariable = null;

	public VariableDialog(Shell parent, IStackFrame frame) {
		super(parent);
		this.frame = frame;
	}
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(DialogMessages.getString("VariableDialog.name"));
	}
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		Composite result = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		result.setLayout(layout);
		result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		new Label(result, SWT.NONE).setText(DialogMessages.getString("VariableDialog.label"));
		listViewer = new ListViewer(result, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		listViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
		listViewer.getList().setFont(parent.getFont());
		listViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof IVariable) {
					try {
						return ((IVariable) element).getName();
					} catch (DebugException e) {
						return "";
					}
				}
				return "";
			}
		});
		listViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {}
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Object[])
					return (Object[]) inputElement;
				return new Object[0];
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		listViewer.addFilter(getViewFilter());
		try {
			listViewer.setInput(frame.getVariables());
		} catch (DebugException e) {
			listViewer.setInput(null);
		}
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				getOkButton().setEnabled(!event.getSelection().isEmpty());
			}
		});
		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				buttonPressed(IDialogConstants.OK_ID);
			}
		});
		applyDialogFont(composite);
		return composite;
	}
	public void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getOkButton().setEnabled(false);
	}
	protected abstract ViewerFilter getViewFilter();
	public Button getOkButton() {
		return getButton(IDialogConstants.OK_ID);
	}
	public IVariable getSelectedVariable() {
		return selectedVariable;
	}
	protected void buttonPressed(int buttonId) {
		selectedVariable = null;
		if (buttonId == IDialogConstants.OK_ID) {
			ISelection selection = listViewer.getSelection();
			if (!selection.isEmpty()) {
				if (selection instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection) selection).getFirstElement();
					if (obj instanceof IVariable)
						selectedVariable = (IVariable) obj;
				}
			}
		}
		super.buttonPressed(buttonId);
	}
}
