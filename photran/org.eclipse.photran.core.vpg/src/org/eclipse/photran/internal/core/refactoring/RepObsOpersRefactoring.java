/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UFSM - Universidade Federal de Santa Maria (www.ufsm.br)
 *     UNIJUI - Universidade Regional do Noroeste do Estado do Rio Grande do Sul (www.unijui.edu.br)
 *     UIUC (modified to use MultipleFileFortranRefactoring)
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.parser.ASTOperatorNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranResourceRefactoring;

/**
 * Refactoring to replace obsolete operators in Fortran files.
 *
 * @author Bruno B. Boniati
 * @author Jeff Overbey
 */
public class RepObsOpersRefactoring extends FortranResourceRefactoring
{
    @Override
    public String getName()
    {
        return "Replace Obsolete Operators";
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        removeFixedFormFilesFrom(this.selectedFiles, status);
        removeCpreprocessedFilesFrom(this.selectedFiles, status);
    }

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

    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status, IProgressMonitor pm) throws Error
    {
        try
        {
            if (ast == null) return;

            OperatorReplacingVisitor replacer = new OperatorReplacingVisitor();
            ast.accept(replacer);
            if (replacer.changedAST) // Do not include the file in the list of changes unless it actually changed
                addChangeFromModifiedAST(file, pm);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    private static final class OperatorReplacingVisitor extends GenericASTVisitor
    {
        private boolean changedAST = false;

        @Override
        public void visitASTNode(IASTNode node)
        {
            if (node instanceof ASTOperatorNode)
                replaceOperatorIn((ASTOperatorNode)node);

            traverseChildren(node);
        }

        private void replaceOperatorIn(ASTOperatorNode op)
        {
            if (op.hasLtOp()) setText(op, "<");
            if (op.hasLeOp()) setText(op, "<=");
            if (op.hasEqOp()) setText(op, "==");
            if (op.hasNeOp()) setText(op, "/=");
            if (op.hasGtOp()) setText(op, ">");
            if (op.hasGeOp()) setText(op, ">=");
        }

        private void setText(ASTOperatorNode op, String newText)
        {
            op.findFirstToken().setText(newText);
            changedAST = true;
        }
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
    }
}

