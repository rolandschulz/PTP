/*******************************************************************************
 * Copyright (c) 2007, 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation (JO)
 *            Updated to resolve external subprograms (KH)
 *******************************************************************************/
package org.eclipse.photran.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.ui.editor.FortranEditor;
import org.eclipse.photran.internal.ui.editor_vpg.DefinitionMap;
import org.eclipse.photran.internal.ui.editor_vpg.FortranEditorTasks;
import org.eclipse.photran.internal.ui.editor_vpg.IFortranEditorASTTask;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;

/**
 * Implements the Open Declaration action that appears in the Navigate menu and in the Fortran editor's pop-up menu.
 * <p>
 * When the cursor is positioned over an identifier, the declaration of that identifier is located, and the cursor
 * is positioned at the declaration (a new editor window is opened if the declaration is located in another file).
 * If multiple declarations are found, a dialog is opened so that the user can see the various declarations and
 * decide which to open.
 * 
 * TODO: Open Declaration does not work on module names in USE statements since these are not in the DefinitionMap 
 * 
 * @author Jeff Overbey
 * @author Kurt Hendle
 */
public class OpenDeclaration extends FortranEditorASTActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        FortranEditor editor = getFortranEditor();
        Shell shell = editor.getShell();
        
        if (PhotranVPG.getInstance().doesProjectHaveRefactoringEnabled(editor.getIFile()))
        {
            TextSelection selection = (TextSelection)editor.getSelection();
            IWorkbenchPage page = editor.getEditorSite().getPage();
            
            FortranEditorTasks tasks = FortranEditorTasks.instance(editor);
            tasks.addASTTask(new OpenDeclarationASTTask(editor, selection, shell, page));
            tasks.getRunner().runTasks(false);
        }
        else
        {
            MessageDialog.openError(shell, "Error",
                "Please enable analysis and refactoring in the project properties.");
        }
    }
    
    private class OpenDeclarationASTTask implements IFortranEditorASTTask
    {
        @SuppressWarnings("unused") private FortranEditor editor;
        private TextSelection selection;
        private Shell shell;
        private IWorkbenchPage page;
        
        public OpenDeclarationASTTask(FortranEditor editor, TextSelection selection, Shell shell, IWorkbenchPage page)
        {
            this.editor = editor;
            this.selection = selection;
            this.shell = shell;
            this.page = page;
        }
        
        // This runs outside the UI thread
        public boolean handle(ASTExecutableProgramNode ast,
                           TokenList tokenList,
                           DefinitionMap<Definition> defMap)
        {
            // If defMap has not been created, we can't do this now, so we'll run this task the next
            // time the editor is reconciled; hopefully the defMap will have been created by then
            if (defMap == null) return true;
            
            Definition def = defMap.lookup(selection, tokenList);
            if (def == null)
                def = chooseExternalDef(PhotranVPG.getInstance().findAllExternalSubprogramsNamed(PhotranVPG.canonicalizeIdentifier(selection.getText())));
            else if (def.isExternal() || def.isImplicitExternalSubprogram())
                def = chooseExternalDef(PhotranVPG.getInstance().findAllExternalSubprogramsNamed(def.getCanonicalizedName()));

            showDeclInUIThread(def);

            // Remove this task so it will not be run the next time the editor is reconciled
            return false;
        }
        
        private Definition chooseExternalDef(final ArrayList<Definition> defList)
        {
            class ChooseDefinition implements Runnable
            {
                Definition def;
                
                public void run()
                {
                    if (defList.size() > 1)
                        def = openSelectionDialog(defList);
                    else if (defList.size() == 1 && defList.get(0).getTokenRef().getOffset() >= 0)
                        def = defList.get(0);
                    else
                        def = null;
                }
            };
            
            ChooseDefinition chooseDefinition = new ChooseDefinition();
            Display.getDefault().syncExec(chooseDefinition);
            return chooseDefinition.def;
        }
        
        private void showDeclInUIThread(final Definition def)
        {
            Display.getDefault().syncExec(new Runnable()
            {
                public void run()
                {
                    if (def == null)
                        MessageDialog.openError(shell, "Error", "Unable to locate declaration");
                    else
                        openEditorOn(def);
                }
            });
        }

        private void openEditorOn(Definition def)
        {
            if (def == null) return; // Selection dialog canceled
            
            try
            {
                IMarker marker = def.createMarker();
                if (marker == null)
                    MessageDialog.openError(shell, "Error", "Unable to create marker");
                else
                    IDE.openEditor(page, marker, true);
            }
            catch (Exception e)
            {
                String message = e.getMessage();
                if (message == null) message = e.getClass().getName();
                MessageDialog.openError(shell, "Error", message);
            }
        }
    }
}
