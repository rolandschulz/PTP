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
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.ui.ide.IDE;

/**
 * The Select Enclosing Scope action in the Refactoring/(Debugging) menu.
 * 
 * @author Jeff Overbey
 */
public class SelectEnclosingScope extends FortranEditorASTActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
        	progressMonitor.beginTask(Messages.SelectEnclosingScope_WaitingForBackgroundWorkToComplete, IProgressMonitor.UNKNOWN);
        	
            progressMonitor.subTask(Messages.SelectEnclosingScope_Parsing);
            Token token = findEnclosingToken(getAST(), getFortranEditor().getSelection());
            if (token == null) throw new Exception(Messages.SelectEnclosingScope_PleaseSelectAKeywordOrIdentifier);


            ScopingNode scope = token.getEnclosingScope();
            if (scope == null) throw new Exception(Messages.SelectEnclosingScope_NoEnclosingScope);
            
            IMarker marker = scope.createMarker();
            if (marker == null) throw new Exception(Messages.SelectEnclosingScope_NoTokensInScope);
            
        	IDE.openEditor(getFortranEditor().getEditorSite().getPage(), marker, true);
        }
        catch (Exception e)
        {
        	String message = e.getMessage();
        	if (message == null) message = e.getClass().getName();
        	MessageDialog.openError(getFortranEditor().getShell(), Messages.SelectEnclosingScope_ErrorTitle, message);
        }
        finally
        {
        	progressMonitor.done();
        }
    }
}
