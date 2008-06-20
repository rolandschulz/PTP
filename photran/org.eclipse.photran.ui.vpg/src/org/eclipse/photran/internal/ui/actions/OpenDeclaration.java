/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
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
 */
public class OpenDeclaration extends FortranEditorASTActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        AbstractFortranEditor editor = getFortranEditor();
        TextSelection selection = (TextSelection)editor.getSelection();
        Shell shell = editor.getShell();
        IWorkbenchPage page = editor.getEditorSite().getPage();
        
        FortranEditorTasks tasks = FortranEditorTasks.instance(editor);
        tasks.addASTTask(new OpenDeclarationASTTask(editor, selection, shell, page));
        tasks.getRunner().runTasks(false);
    }
    
    private static class OpenDeclarationASTTask implements IFortranEditorASTTask
    {
        private AbstractFortranEditor editor;
        private TextSelection selection;
        private Shell shell;
        private IWorkbenchPage page;
        
        public OpenDeclarationASTTask(AbstractFortranEditor editor, TextSelection selection, Shell shell, IWorkbenchPage page)
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
            if (defMap == null) return true;
            
            final Definition def = defMap.lookup(selection, tokenList);

            // Run this in the UI thread
            Display.getDefault().asyncExec(new Runnable()
            {
                public void run()
                {
                    if (def == null)
                        MessageDialog.openError(shell, "Error", "Unable to locate declaration");
                    else
                        openEditorOn(def);
                }
            });

            // Remove this task
            return false;
        }

        private void openEditorOn(Definition def)
        {
            if (def == null) return; // Selection dialog canceled
            
            try
            {
                IMarker marker = def.createMarker();
                if (marker == null) return;
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
