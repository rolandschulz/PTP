/*
 * Created on Aug 31, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.fdt.internal.ui.search.actions;

import org.eclipse.fdt.core.search.ICSearchScope;
import org.eclipse.fdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.fdt.internal.ui.editor.FortranEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

public class WorkingSetFindAction extends FindAction {

	private FindAction findAction;
	
	public WorkingSetFindAction(FortranEditor editor, FindAction action, String string) {
		super ( editor );
		this.findAction = action;
		setText(string); //$NON-NLS-1$
	}

	/**
	 * @param site
	 * @param action
	 * @param string
	 */
	public WorkingSetFindAction(IWorkbenchSite site,FindAction action, String string) {
		super(site);
		this.findAction = action;
		setText(string); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.ui.search.actions.FindAction#getScopeDescription()
	 */
	protected String getScopeDescription() {
		return findAction.getScopeDescription();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.ui.search.actions.FindAction#getScope()
	 */
	protected ICSearchScope getScope() {
		// TODO Auto-generated method stub
		return findAction.getScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.ui.search.actions.FindAction#getLimitTo()
	 */
	protected LimitTo getLimitTo() {
		return findAction.getLimitTo();
	}
	
	public void run() {
		ISelection sel = getSelection();
		
		if (sel instanceof IStructuredSelection){
			findAction.run((IStructuredSelection) sel);
		}
		else if  (sel instanceof ITextSelection){
			findAction.run((ITextSelection) sel);
		}
		
	}

}
