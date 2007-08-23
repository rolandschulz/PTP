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
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * Implements the Open Declaration action that appears in the Navigate menu and in the Fortran editor's pop-up menu.
 * <p>
 * When the cursor is positioned over an identifier, the declaration of that identifier is located, and the cursor
 * is positioned at the declaration (a new editor window is opened if the declaration is located in another file).
 * If multiple declarations are found, a dialog is opened so that the user can see the various declarations and
 * decide which to open.
 * 
 * @author Jeff Overbey
 */
public class OpenDeclaration extends FortranEditorASTActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
        	progressMonitor.beginTask("Waiting for background work to complete (synchronizing Fortran virtual program graph)", IProgressMonitor.UNKNOWN);

        	List<Definition> defs = resolveBinding();

            if (defs == null || defs.isEmpty())
                MessageDialog.openError(getFortranEditor().getShell(), "Error", "Unable to locate declaration");
            else if (defs.size() == 1)
                openEditorOn(defs.get(0));
            else
                openEditorOn(openSelectionDialog(defs));
        }
        catch (Exception e)
        {
        	String message = e.getMessage();
        	if (message == null) message = e.getClass().getName();
        	MessageDialog.openError(getFortranEditor().getShell(), "Error", message);
        }
        finally
        {
        	progressMonitor.done();
        }
    }
    
    private List<Definition> resolveBinding() throws Exception
    {
    	// TODO: No need to parse; this is all in VPG edges
    	
        IFortranAST ast = getAST();
        
        Token token = findEnclosingToken(ast, getFortranEditor().getSelection());
        if (token == null || token.getTerminal() != Terminal.T_IDENT)
            throw new Exception("Please select an identifier.");

        return token.resolveBinding();
    }

    private void openEditorOn(Definition def) throws PartInitException
    {
        if (def == null) return; // Dialog canceled
        
        IMarker marker = def.createMarker();
        if (marker == null) return;
        if (marker == null)
            MessageDialog.openError(getFortranEditor().getShell(), "Error", "Unable to create marker");
        else
        	IDE.openEditor(getFortranEditor().getEditorSite().getPage(), marker, true);
    }
}
