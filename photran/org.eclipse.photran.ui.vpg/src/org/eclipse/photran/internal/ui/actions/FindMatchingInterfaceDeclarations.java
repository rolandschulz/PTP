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
 * Implements the Find Matching Interface Declarations action in the Refactor/(Debugging) menu.
 * 
 * @author Jeff Overbey
 */
public class FindMatchingInterfaceDeclarations extends FortranEditorASTActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
        	progressMonitor.beginTask(Messages.FindMatchingInterfaceDeclarations_WaitingForBackgroundWorkToComplete, IProgressMonitor.UNKNOWN);

            Token token = findEnclosingToken(getAST(), getFortranEditor().getSelection());
            if (token == null) throw new Exception(Messages.FindMatchingInterfaceDeclarations_PleaseSelectAToken);

            Definition def = PhotranVPG.getInstance().getDefinitionFor(token.getTokenRef());
            if (def == null) throw new Exception(Messages.FindMatchingInterfaceDeclarations_PleaseSelectIdentifierInASubprogram);

            openSelectionDialog(def.findMatchingDeclarationsInInterfaces());
        }
        catch (Exception e)
        {
        	String message = e.getMessage();
        	if (message == null) message = e.getClass().getName();
        	MessageDialog.openError(getFortranEditor().getShell(), Messages.FindMatchingInterfaceDeclarations_ErrorTitle, message);
        }
        finally
        {
        	progressMonitor.done();
        }
    }
}
