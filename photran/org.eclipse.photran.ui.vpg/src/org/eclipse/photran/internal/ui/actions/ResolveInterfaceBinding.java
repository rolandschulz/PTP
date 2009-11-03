/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
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
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * Implements the Resolve Interface Binding action in the Refactor/(Debugging) menu.
 * 
 * @author Jeff Overbey
 */
public class ResolveInterfaceBinding extends FortranEditorASTActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
        	progressMonitor.beginTask("Waiting for background work to complete (Photran indexer)", IProgressMonitor.UNKNOWN);

            Token token = findEnclosingToken(getAST(), getFortranEditor().getSelection());
            if (token == null) throw new Exception("Please select a token.");

            Definition def = PhotranVPG.getInstance().getDefinitionFor(token.getTokenRef());
            if (def == null) throw new Exception("Please select the identifier in a subprogram declaration.");

            openSelectionDialog(def.resolveInterfaceBinding());
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
