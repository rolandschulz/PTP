/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.refactoring.infrastructure.MultipleFileFortranRefactoring;

/**
 * Refactoring to unify case of all keywords in Fortran files.
 *
 * @author Kurt Hendle
 */
public class KeywordCaseRefactoring extends MultipleFileFortranRefactoring
{
    private boolean lowerCase = true;   //true for lower case, false for upper case

    @Override
    public String getName()
    {
        return "Change Keyword Case";
    }

    public void setLowerCase(boolean value)
    {
        this.lowerCase = value;
    }

    /** borrowed from RepObsOpersRefactoring.java */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        //removeFixedFormFilesFrom(this.selectedFiles, status);
        removeCpreprocessedFilesFrom(this.selectedFiles, status);
    }

    /** borrowed from RepObsOpersRefactoring.java */
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        try
        {
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if (ast == null)
                    status.addError("One of the selected files (" + file.getName() +") cannot be parsed.");
                makeChangesTo(file, ast, status, pm);
                vpg.releaseAST(file);
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    /** modeled after RepObsOpersRefactoring.java */
    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status, IProgressMonitor pm) throws Error
    {
        try
        {
            if (ast == null) return;

            CaseChangingVisitor replacer = new CaseChangingVisitor();
            replacer.lowerCase = this.lowerCase;
            ast.accept(replacer);
            if (replacer.changedAST) // Do not include the file in changes unless actually changed
                addChangeFromModifiedAST(file, pm);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    /** borrowed from RepObsOpersRefactoring.java */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
    }

    private static final class CaseChangingVisitor extends GenericASTVisitor
    {
        private boolean changedAST = false;
        private boolean lowerCase;

        @Override
        public void visitToken(Token node)
        {
            Terminal term = node.getTerminal();

            if (term == Terminal.T_IDENT || term == Terminal.T_PCON ||
                term == Terminal.T_FCON || term == Terminal.T_BCON ||
                term == Terminal.T_ZCON || term == Terminal.T_SCON ||
                term == Terminal.T_DCON || term == Terminal.T_XCON ||
                term == Terminal.T_OCON || term == Terminal.T_HCON)
            {
                //ignore these 10 terminals (identifiers and constants)
            }
            else
                changeCaseOf(node);
        }

        private void changeCaseOf(Token node)
        {
            if(lowerCase)
                node.findFirstToken().setText(node.getText().toLowerCase());
            else
                node.findFirstToken().setText(node.getText().toUpperCase());

            changedAST = true;
        }
    }
}
