package org.eclipse.photran.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
  
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditorActionContributor;

/**
 * TODO-Nicholas or Cheah: Comment this.  Where was is copied from, and what does it do?
 * 
 * @author Cheah or Nicholas?
 */
public class FortranEditorActionContributor extends TextEditorActionContributor
{
	protected AbstractFortranEditor editor = null;

	/*@Override*/
	public void setActiveEditor(IEditorPart part)
	{
		super.setActiveEditor(part);
		editor = part instanceof AbstractFortranEditor ? (AbstractFortranEditor)part : null;
	}

	/*@Override*/
	public void contributeToMenu(IMenuManager menu)
	{
		super.contributeToMenu(menu);
		
//		Action openParseTreeView = new Action("Open &Parse Tree")
//		{
//			@Override
//			public void run()
//			{
//				if (editor == null) return;
//				String doc = editor.getDocumentProvider().getDocument(null).get();
//				System.out.println(new RefactoringFortranProcessor().abcdefg);
//			}
//		};
//		
//		IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
//		if (navigateMenu != null) {
//			navigateMenu.appendToGroup(IWorkbenchActionConstants.OPEN_EXT, openParseTreeView);
//			navigateMenu.setVisible(true);
//		}
	}
}