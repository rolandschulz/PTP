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
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.ui.actions.FortranEditorASTActionDelegate;
import org.eclipse.photran.internal.ui.actions.OpenDeclaration;
import org.eclipse.photran.internal.ui.search.FortranFindReferencesSearchQuery.SearchScope;

/**
 * Based on {@link OpenDeclaration}
 * 
 * @author ?
 */
public abstract class FortranFindReferencesActionDelegate extends FortranEditorASTActionDelegate
{
    public static class FileActionDelegate extends FortranFindReferencesActionDelegate
    {
        @Override protected SearchScope getSearchScope()
        {
            return SearchScope.FILE;
        }
    }
    
    public static class ProjectActionDelegate extends FortranFindReferencesActionDelegate
    {
        @Override protected SearchScope getSearchScope()
        {
            return SearchScope.PROJECT;
        }
    }
    
    public static class WorkspaceActionDelegate extends FortranFindReferencesActionDelegate
    {
        @Override protected SearchScope getSearchScope()
        {
            return SearchScope.WORKSPACE;
        }
    }
    
    public void run(IProgressMonitor progressMonitor)
        throws InvocationTargetException, InterruptedException
    {
        try
        {
            if (!PhotranVPG.getInstance().doesProjectHaveRefactoringEnabled(getFortranEditor().getIFile()))
                throw new Exception(Messages.FortranFindReferencesActionDelegate_PleaseEnableAnalysisAndRefactoring);
            
        	//progressMonitor.beginTask("Waiting for background work to complete (Photran indexer)", IProgressMonitor.UNKNOWN);

        	//PhotranVPG.getInstance().ensureVPGIsUpToDate(progressMonitor);
        	
            Token token = findEnclosingToken(getAST(), getFortranEditor().getSelection());
            if (token == null || token.getTerminal() != Terminal.T_IDENT)
            	throw new Exception(Messages.FortranFindReferencesActionDelegate_PleaseSelectAnIdentifier);

			List<Definition> defs = token.resolveBinding();            
			if (defs.isEmpty())
				throw new Exception(Messages.FortranFindReferencesActionDelegate_DefinitionCouldNotBeFound);

            Definition selectedDef =
                defs.size() > 1 ? openSelectionDialog(defs)
                                : defs.get(0);
			
			// This may be null if the user cancelled the selection dialog.
			if (selectedDef != null && token.getPhysicalFile() != null && token.getPhysicalFile().getIFile() != null)
				FortranFindReferencesSearchQuery.searchForReference(selectedDef, getSearchScope(), token.getPhysicalFile().getIFile());
        }
        catch (Exception e)
        {
        	String message = e.getMessage();
        	if (message == null) message = e.getClass().getName();
        	MessageDialog.openError(getFortranEditor().getShell(), Messages.FortranFindReferencesActionDelegate_ErrorTitle, message);
        }
        finally
        {
        	progressMonitor.done();
        }
	}
	
	protected abstract SearchScope getSearchScope();
}
