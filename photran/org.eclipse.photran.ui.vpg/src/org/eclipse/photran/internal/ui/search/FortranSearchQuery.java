/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Ed Swartz (Nokia)
 *    Quillback: Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 *******************************************************************************/
package org.eclipse.photran.internal.ui.search;

import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.ui.search.SearchMatch;
import org.eclipse.rephraserengine.ui.search.SearchQuery;
import org.eclipse.rephraserengine.ui.search.SearchResult;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.ui.internal.Workbench;

/**
 * An implementation of {@link ISearchQuery} that performs searches using
 * Photran's VPG.  Based on org.eclipse.cdt.internal.ui.search.PDOMSearchQuery
 * from CDT 5.0.
 *
 * @author Doug Schaefer
 * @author Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 * @author Kurt Hendle
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class FortranSearchQuery extends SearchQuery<SearchResult>
{
    // First bit after the FINDs in PDOMSearchQuery.
    public static final int FIND_PROGRAM = 0x10;
    public static final int FIND_FUNCTION = 0x20;
    public static final int FIND_VARIABLE = 0x40;
    public static final int FIND_SUBROUTINE = 0x100;
    public static final int FIND_COMMON_BLOCK = 0x200;
    public static final int FIND_MODULE = 0x400;
    public static final int FIND_ALL_TYPES
        = FIND_PROGRAM | FIND_FUNCTION | FIND_VARIABLE
        | FIND_SUBROUTINE | FIND_COMMON_BLOCK | FIND_MODULE;

    public static final int FIND_REFERENCES = 0x01;
    public static final int FIND_DECLARATIONS = 0x02;
    public static final int FIND_ALL_OCCURANCES = FIND_REFERENCES | FIND_DECLARATIONS;

    private TreeSet<String> projectsWithRefactoringDisabled = new TreeSet<String>();

    public FortranSearchQuery(
            List<IResource> scope,
            String scopeDesc,
            String patternDescription,
            String patternRegex,
            int flags)
    {
        super(scope, scopeDesc, patternDescription, patternRegex, flags);
    }
    
    @Override protected SearchResult createInitialSearchResult()
    {
        return new SearchResult(this);
    }

    @Override protected boolean shouldProcess(IResource resource)
    {
        return !shouldNotProcess(resource);
    }

    private boolean shouldNotProcess(IResource resource)
    {
        return resource == null
            || resource instanceof IProject && !PhotranVPG.getInstance().shouldProcessProject((IProject)resource)
            || resource instanceof IFile && !PhotranVPG.getInstance().shouldProcessFile((IFile)resource);
    }

    private TreeSet<PhotranTokenRef> matchesToAddLater;

    @Override protected void prepareToSearch(IProgressMonitor monitor)
    {
        PhotranVPG.getInstance().ensureVPGIsUpToDate(monitor);
        this.matchesToAddLater = new TreeSet<PhotranTokenRef>();
    }

    @Override protected void search(IFile file)
    {
        IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(file);
        if (ast == null) return;

        Token searchToken = new Token(Terminal.T_IDENT, patternRegex);
        searchToken.setFile(file);
        searchToken.setContainerFile(file);

        if (ast.getRoot().getEmptyProgram() == null) { // manuallyResolve errs on empty file
            for (ScopingNode sNode : ast.getRoot().getAllContainedScopes()) {
                List<PhotranTokenRef> refs = sNode.manuallyResolve(searchToken);
                for (PhotranTokenRef ref : refs) {
                    if (ref.getOffset() >= 0 && ref.getLength() >= 0) {
                        foundDefinition(ref);
                    }
                }
            }
        }
    }

    /**
     * Filters search results based on whether this identifier is a reference or
     * a declaration
     * @param ref The reference to the current token
     */
    private void foundDefinition(PhotranTokenRef ref)
    {
        Token token = ref.findToken();
        for (Definition def : token.resolveBinding()) {
            if (shouldAccept(def)) {
                if ((searchFlags & FIND_DECLARATIONS) != 0) {
                    foundMatch(token);
                }
                if ((searchFlags & FIND_REFERENCES) != 0) {
                    for(PhotranTokenRef rref : def.findAllReferences(true)) {
                        foundMatch(rref);
                    }
                }
            }
        }
    }
    /**
     * Filters this search based on the current search criteria
     * @param def The definition for this identifier
     * @return true if definition matches the current search restrictions
     *         false otherwise
     */
    private boolean shouldAccept(Definition def) {
        //ignores references in projects which do not have refactoring enabled
        if(!PhotranVPG.getInstance().doesProjectHaveRefactoringEnabled(def.getTokenRef().getFile())){
            projectsWithRefactoringDisabled.add(def.getTokenRef().getFile().getProject().getName());
            return false;
        }

        if ((searchFlags & FIND_PROGRAM) != 0 &&
            def.isMainProgram()) {
            return true;
        }
        if ((searchFlags & FIND_FUNCTION) != 0 &&
            def.getClassification() == Definition.Classification.FUNCTION) {
            return true;
        }
        if ((searchFlags & FIND_VARIABLE) != 0 &&
            def.isLocalVariable()) {
            return true;
        }
        if ((searchFlags & FIND_SUBROUTINE) != 0 &&
            def.getClassification() == Definition.Classification.SUBROUTINE) {
            return true;
        }
        if ((searchFlags & FIND_MODULE) != 0 &&
            def.getClassification() == Definition.Classification.MODULE) {
            return true;
        }
        if ((searchFlags & FIND_COMMON_BLOCK) != 0 &&
            def.isCommon()) {
            return true;
        }

        return false;
    }

    private void foundMatch(Token token) {
        addSearchResultFromTokenRef(token.getTokenRef());
    }

    private void foundMatch(PhotranTokenRef ref) {
        matchesToAddLater.add(ref);
    }

    @Override protected int numPasses()
    {
        return 2;
    }

    /**
     * @param passNum pass number (2, 3, 4, ...)
     * @param monitor
     */
    @Override protected void runAdditionalSearchPass(int passNum, IProgressMonitor pm)
    {
        pm.beginTask("Adding references", matchesToAddLater.size());
        
        String lastFilename = null;
        for (PhotranTokenRef tokenRef : matchesToAddLater) {
            if (!tokenRef.getFilename().equals(lastFilename)) {
                lastFilename = tokenRef.getFilename();
                pm.subTask("Adding references in " + lastFilename.substring(lastFilename.lastIndexOf('/')+1));
            }
            pm.worked(1);
            
            addSearchResultFromTokenRef(tokenRef);
        }
        
        pm.done();
    }
    
    private void addSearchResultFromTokenRef(PhotranTokenRef tokenRef) {
        FortranSearchQuery.addSearchResultFromTokenRef(tokenRef, (SearchResult)getSearchResult());
    }

    public static void addSearchResultFromTokenRef(PhotranTokenRef tokenRef, SearchResult searchResult) {
        Token token = tokenRef.findTokenOrReturnNull();
        if (token != null) {
            SearchMatch match = new SearchMatch(token.getIFile(),
                token.getFileOffset(),
                token.getLength());
            searchResult.addMatch(match);
        }
    }
    
    @Override protected void finishSearch()
    {
        if (!PhotranVPG.inTestingMode()
            && Workbench.getInstance().getWorkbenchWindowCount() > 0
            && !projectsWithRefactoringDisabled.isEmpty())
        {
            Workbench.getInstance().getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    StringBuilder projects = new StringBuilder();
                    for (String project : projectsWithRefactoringDisabled)
                    {
                        projects.append(project);
                        projects.append('\n');
                    }

                    MessageDialog.openWarning(
                        Workbench.getInstance().getActiveWorkbenchWindow().getShell(),
                        "Warning",
                        "References in the following projects have been excluded from the search" +
                        " results because Fortran analysis/refactoring is disabled:\n\n" + projects +
                        "\nPlease enable Fortran analysis/refactoring for these projects if you wish" +
                        " for their references to show in search results.");
                }
            });
        }
    }
}
