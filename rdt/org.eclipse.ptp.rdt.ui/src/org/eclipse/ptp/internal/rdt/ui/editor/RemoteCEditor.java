package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.internal.rdt.ui.RDTHelpContextIds;
import org.eclipse.ptp.internal.rdt.ui.actions.OpenViewActionGroup;
import org.eclipse.ptp.internal.rdt.ui.search.actions.SelectionSearchGroup;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;


/**
 * Remote enabled version of the CEditor.
 * 
 * If this editor is opened on a file from a remote project then it will
 * use remote versions of certain actions. 
 * 
 * If this editor is opened on a file from a local project then it defaults
 * back to normal CEditor behavior.
 * 
 * 
 * @author Mike Kucera
 */
public class RemoteCEditor extends CEditor {
	
	
	/**
	 * Returns true if the input translation unit comes from
	 * a remote project. Also returns true if the editor
	 * is opened on an external translation unit that was navigated
	 * to from a remote resource.
	 */
	private boolean isRemote() {
		IProject project = getInputCElement().getCProject().getProject();
		return RemoteNature.hasRemoteNature(project);
	}
	
	
	// use our remote versions of these actions.
	
	@Override
	protected ActionGroup createSelectionSearchGroup() {
		return isRemote()  
				? new SelectionSearchGroup(this)
				: super.createSelectionSearchGroup();
	}
	
	@Override
	protected ActionGroup createOpenViewActionGroup() {
		return isRemote()  
				? new OpenViewActionGroup(this)  
				: super.createOpenViewActionGroup();
	}
	/**
	 * This method overrides the CEditor createPartControl method in order to set the help for the Remote
	 * C/C++ editor.
	 */
	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, RDTHelpContextIds.REMOTE_C_CPP_EDITOR);
	}
	
	/**
	 * Override so that the remote version of the
	 * outline page is used.
	 */
	@Override
	public CContentOutlinePage getOutlinePage() {
		if(isRemote()) {
			if (fOutlinePage == null) {
				fOutlinePage = new RemoteCContentOutlinePage(this);
				fOutlinePage.addSelectionChangedListener(this);
			}
			setOutlinePageInput(fOutlinePage, getEditorInput());
			return fOutlinePage;
		}
		else {
			return super.getOutlinePage();
		}
	}

	
	
}
