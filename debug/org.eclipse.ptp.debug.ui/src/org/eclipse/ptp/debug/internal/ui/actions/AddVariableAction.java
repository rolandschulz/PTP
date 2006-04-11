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
package org.eclipse.ptp.debug.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.debug.internal.ui.PDebugImage;
import org.eclipse.ptp.debug.internal.ui.dialogs.ArrayVariableDialog;
import org.eclipse.ptp.debug.internal.ui.views.PTabFolder;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author Clement chu
 *
 */
public class AddVariableAction extends Action {
	protected PTabFolder folder = null;
	public static final String name = "Add Variable"; 

	/** Constructor
	 * @param folder
	 */
	public AddVariableAction(PTabFolder folder) {
		super(name, IAction.AS_PUSH_BUTTON);
		setImageDescriptor(PDebugImage.ID_ICON_ADD_VAR_NORMAL);
		setToolTipText(name);
		this.folder = folder;
	}
    /** Get shell
     * @return
     */
    public Shell getShell() {
        return folder.getViewSite().getShell();
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		final IStackFrame[] stackFrame = new IStackFrame[1];
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					stackFrame[0] = getStackFrame();
				} catch (CoreException e) {
					PTPDebugUIPlugin.errorDialog(getShell(), "Error", e.getStatus());
				}
			}
		} );
		try {
			openDialog(stackFrame[0]);
		} catch(DebugException e) {
			PTPDebugUIPlugin.errorDialog(getShell(), "Error", e.getStatus());
		}
	}
	
	/** Get stack frame
	 * @return
	 * @throws CoreException
	 */
	protected IStackFrame getStackFrame() throws CoreException {
		IWorkbenchWindow activeWindow = PTPDebugUIPlugin.getActiveWorkbenchWindow();
		if (activeWindow == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, "No active window found", null));
		
		IWorkbenchPage page = activeWindow.getActivePage();
		if (page == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, "No active page found", null));	

		IViewPart part = page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (part != null) {
			IDebugView adapter = (IDebugView)part.getAdapter(IDebugView.class);
			if (adapter != null) {				
				ISelection selection = adapter.getViewer().getSelection();
				if (selection instanceof StructuredSelection) {
					Object obj = ((StructuredSelection)selection).getFirstElement();
					if (obj instanceof IStackFrame) {
						return (IStackFrame)obj;
					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, "No stack frame found", null));
	}
	
	/** Open array variable dialog
	 * @param frame
	 * @throws DebugException
	 */
	protected void openDialog(IStackFrame frame) throws DebugException {
		if (frame != null) {
			ArrayVariableDialog dialog = new ArrayVariableDialog(getShell(), frame);
			if (dialog.open() == Window.OK) {
				IVariable variable = dialog.getSelectedVariable();
				if (variable != null) {
					folder.createTabItem(variable.getName(), variable);
				}
			}
		}
	}
}
