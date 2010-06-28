/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.parser.ASTArithmeticIfStmtNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranResourceRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;

/**
 * Refactoring to remove arithmetic if statements in Fortran files.
 *
 * @author Matthew Fotzler
 */
public class RemoveArithmeticIfRefactoring extends FortranResourceRefactoring
{
    @Override
    public String getName()
    {
        return Messages.RemoveArithmeticIfRefactoring_Name;
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
                    status.addError(Messages.bind(Messages.RemoveArithmeticIfRefactoring_Error, file.getName()));
                makeChangesTo(file, ast, status, pm);
                vpg.releaseAST(file);
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status, IProgressMonitor pm)
    {
        if (ast == null) return;

        List<ASTArithmeticIfStmtNode> nodesToReplace = findNodesToReplace(ast);
        
        if (!nodesToReplace.isEmpty())
        {
            for (ASTArithmeticIfStmtNode node : nodesToReplace)
                replaceNode(node, ast);
            
            addChangeFromModifiedAST(file, pm);
        }
    }

    private List<ASTArithmeticIfStmtNode> findNodesToReplace(IFortranAST ast)
    {
        ArithmeticIfRemovingVisitor replacer = new ArithmeticIfRemovingVisitor();
        ast.accept(replacer);
        return replacer.nodes;
    }

    private static final class ArithmeticIfRemovingVisitor extends GenericASTVisitor
    {
        private ArrayList<ASTArithmeticIfStmtNode> nodes = new ArrayList<ASTArithmeticIfStmtNode>();

        @Override
        public void visitASTNode(IASTNode node)
        {    
            if (node instanceof ASTArithmeticIfStmtNode)
                nodes.add((ASTArithmeticIfStmtNode)node);

            traverseChildren(node);
        }
    }

    private void replaceNode(ASTArithmeticIfStmtNode node, IFortranAST ast)
    {
        String first = node.getFirst().getLabel().getText();
        String second = node.getSecond().getLabel().getText();
        String third = node.getThird().getLabel().getText();
        String conditionVariable = node.getExpr().toString();
        String newNodeString = node.findFirstToken().getWhiteBefore();
        newNodeString += 
            "if(" + conditionVariable + "< 0) then" + node.findLastToken().getWhiteBefore() +  //$NON-NLS-1$ //$NON-NLS-2$
            "\ngoto " +  first +  //$NON-NLS-1$
            "\nelse if(" + conditionVariable + " == 0) then " + //$NON-NLS-1$ //$NON-NLS-2$
            "\ngoto " + second +  //$NON-NLS-1$
            "\nelse " + //$NON-NLS-1$
            "\ngoto " + third +  //$NON-NLS-1$
            "\nend if"; //$NON-NLS-1$
        
        IASTNode newnode = parseLiteralStatement(newNodeString);
        node.replaceWith(newnode);
        Reindenter.reindent(newnode, ast, Strategy.REINDENT_EACH_LINE);
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
    }
}

