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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.parser.ASTOperatorNode;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.MultipleFileFortranRefactoring;

/**
 * Refactoring to replace obsolete operators in Fortran files.
 * 
 * @author Bruno B. Boniati
 * @author Jeff Overbey
 */
public class RepObsOpersRefactoring extends MultipleFileFortranRefactoring
{
    public RepObsOpersRefactoring(ArrayList<IFile> myFiles)
    {
        super(myFiles);
    }
    
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
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
    }
    
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        try
        {
            for (IFile file : selectedFiles)
                makeChangesTo(file, pm);
        }
        finally
        {
            vpg.releaseAllASTs();
        }    
    }

    private void makeChangesTo(IFile file, IProgressMonitor pm) throws Error
    {
        try
        {
            OperatorReplacingVisitor replacer = new OperatorReplacingVisitor();
            vpg.acquirePermanentAST(file).accept(replacer);
            if (replacer.changedAST) // Do not include the file in the list of changes unless it actually changed
                addChangeFromModifiedAST(file, pm);
            vpg.releaseAST(file);
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
}

