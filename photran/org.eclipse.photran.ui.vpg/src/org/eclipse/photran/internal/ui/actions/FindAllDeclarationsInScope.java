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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;

/**
 * Implements the Find All Declarations in Scope action in the Refactor/(Debugging) menu.
 * 
 * @author Jeff Overbey
 */
public class FindAllDeclarationsInScope extends FortranEditorASTActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
        	progressMonitor.beginTask("Waiting for background work to complete (Photran indexer)", IProgressMonitor.UNKNOWN);

            Token token = findEnclosingToken(getAST(), getFortranEditor().getSelection());
            if (token == null) throw new Exception("Please select a token.");

            ScopingNode scope = token.getEnclosingScope();
            if (scope == null) throw new Exception("No enclosing scope.");

            openSelectionDialog(scope.getAllDefinitions());
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
}
