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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
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
public class VPGSearchQuery implements ISearchQuery {

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

    protected ReferenceSearchResult result = new ReferenceSearchResult(this);

    private String scopeDesc;
    private String patternStr;
    private List<IResource> scope;
    private int searchFlags;
    private String origPatternStr;
    private TreeSet<String> projectsWithRefactoringDisabled = new TreeSet<String>();

    public VPGSearchQuery(
            List<IResource> scope,
            String scopeDesc,
            String patternStr,
            int flags,
            boolean isRegex) {

        this.scope = scope;
        this.scopeDesc = scopeDesc;
        searchFlags = flags;

        // remove spurious whitespace, which will make the search fail 100% of the time
        this.origPatternStr = patternStr.trim();
        this.patternStr = convertPattern(isRegex, origPatternStr);
    }
    /**
     * @param isRegex is this a regular expression or not
     * @param patternStr the string to be converted
     * @return the converted string
     * If the input string is a regex, compile it and return the same string.
     * If the input string is not a regex (e.g. a Glob), convert the Glob to regex syntax
     * and pass
     */
    private String convertPattern(boolean isRegex, String patternStr) {
        if (isRegex) {
            Pattern.compile(patternStr);
            return patternStr;
        }
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < patternStr.length(); ++i) {
            char c = patternStr.charAt(i);
            switch (c) {
            case '*':
                buff.append(".*");
                break;
            case '?':
                buff.append(".");
                break;
            case '$':
                buff.append("\\$");
                break;
            default:
                if (!Character.toString(c).matches("[0-9a-zA-Z._]")) {
                    throw new PatternSyntaxException("Illegal character in pattern string", patternStr, i+1);
                }
                buff.append("" + c);
            }
        }
        return buff.toString();
    }

    public String getLabel() {
        return "'"+origPatternStr+"' - " + result.getMatchCount() + " occurence(s) in " + scopeDesc;
    }
    public boolean canRerun() {
        return true;
    }

    public boolean canRunInBackground() {
        return true;
    }

    public ISearchResult getSearchResult() {
        return result;
    }

    protected static boolean shouldNotProcess(IResource resource)
    {
        return resource == null
            || resource instanceof IProject && !PhotranVPG.getInstance().shouldProcessProject((IProject)resource)
            || resource instanceof IFile && !PhotranVPG.getInstance().shouldProcessFile((IFile)resource);
    }

    /**
     * An IResourceVisitor to just count the number of nodes that we'll visit when searching through
     * the given resources.
     *
     * @author Quillback
     */
    private class VPGCountResourceVisitor implements IResourceVisitor {
        private int[] counter;
        public VPGCountResourceVisitor(int[] counter) {
            this.counter = counter;
        }
        public boolean visit(IResource resource) {
            if (shouldNotProcess(resource))
            {
                return false;
            }
            else
            {
                counter[0]++;
                return !(resource instanceof IFile);
            }
        }
    }

    /**
     * An IResourceVisitor to search through the given resources for the identifier patternStr, and
     * return results in the ReferenceSearchResult returned by getSearchResult().
     *
     * @author Quillback
     */
    private class VPGSearchResourceVisitor implements IResourceVisitor {
        private IProgressMonitor monitor;
        private TreeSet<PhotranTokenRef> matchesToAddLater;
        
        public VPGSearchResourceVisitor(IProgressMonitor monitor) {
            this.monitor = monitor;
            this.matchesToAddLater = new TreeSet<PhotranTokenRef>();
        }
        
        public boolean visit(IResource resource) {
            if (shouldNotProcess(resource)) return false;

            monitor.worked(1);

            if (! (resource instanceof IFile)) {
                return true;
            }
            
            monitor.subTask("Searching " + resource.getName());

            IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST((IFile)resource);
            if (ast == null) {
                return false;
            }

            Token searchToken = new Token(Terminal.T_IDENT, patternStr);
            searchToken.setFile((IFile)resource);
            searchToken.setContainerFile((IFile)resource);

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
            return false;
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

        public void addPostponedSearchResults(IProgressMonitor pm) {
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
            VPGSearchQuery.addSearchResultFromTokenRef(tokenRef, (ReferenceSearchResult)getSearchResult());
        }
    }

    public static void addSearchResultFromTokenRef(PhotranTokenRef tokenRef, ReferenceSearchResult searchResult) {
        Token token = tokenRef.findTokenOrReturnNull();
        if (token != null) {
            VPGSearchMatch match = new VPGSearchMatch(token.getIFile(),
                token.getFileOffset(),
                token.getLength());
            searchResult.addMatch(match);
        }
    }

    /**
     * Runs this search query, adding the results to the search result
     */
    public final IStatus run(IProgressMonitor monitor) {
        final int[] counter = new int[1];
        try {
            PhotranVPG.getInstance().ensureVPGIsUpToDate(new SubProgressMonitor(monitor, 0));

            result.removeAll();

            //monitor.subTask("Counting resources in " + scopeDesc);

            counter[0] = 0;
            VPGCountResourceVisitor countVisitor = new VPGCountResourceVisitor(counter);
            for (IResource resource : scope) {
                resource.accept(countVisitor);
            }

            monitor.beginTask("Searching for " + origPatternStr + " in " + scopeDesc, counter[0]*2);

            VPGSearchResourceVisitor visitor = new VPGSearchResourceVisitor(monitor);
            for (IResource resource : scope) {
                resource.accept(visitor);
            }
            visitor.addPostponedSearchResults(new SubProgressMonitor(monitor, counter[0]));

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

        } catch (CoreException e) {
            return e.getStatus();
        }

        return Status.OK_STATUS;
    }
}
