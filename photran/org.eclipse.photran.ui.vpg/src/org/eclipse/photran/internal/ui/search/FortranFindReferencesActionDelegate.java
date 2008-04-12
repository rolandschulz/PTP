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
package org.eclipse.photran.internal.ui.search;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.ui.actions.FortranEditorASTActionDelegate;
import org.eclipse.photran.internal.ui.actions.OpenDeclaration;
import org.eclipse.photran.internal.ui.search.ReferenceSearch.SearchScope;

/**
 * Based on {@link OpenDeclaration}
 * 
 * @author ?
 */
public abstract class FortranFindReferencesActionDelegate
    extends FortranEditorASTActionDelegate
{
    public void run(IProgressMonitor progressMonitor)
        throws InvocationTargetException, InterruptedException
    {
        try
        {
        	progressMonitor.beginTask("Waiting for background work to complete (synchronizing Fortran virtual program graph)", IProgressMonitor.UNKNOWN);

        	PhotranVPG.getInstance().ensureVPGIsUpToDate(progressMonitor);
        	
            Token token = findEnclosingToken(getAST(), getFortranEditor().getSelection());
            if (token == null || token.getTerminal() != Terminal.T_IDENT)
            	throw new Exception("Please select an identifier.");

			List<Definition> defs = token.resolveBinding();            
			if (defs.isEmpty())
				throw new Exception("No definition could be found for the selected token.");

            Definition selectedDef =
                defs.size() > 1 ? openSelectionDialog(defs)
                                : defs.get(0);
			
			// This may be null if the user cancelled the selection dialog.
			if (selectedDef != null)
				ReferenceSearch.searchForReference(selectedDef, getSearchScope(), token.getFile());
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
	
	protected abstract SearchScope getSearchScope();
}
