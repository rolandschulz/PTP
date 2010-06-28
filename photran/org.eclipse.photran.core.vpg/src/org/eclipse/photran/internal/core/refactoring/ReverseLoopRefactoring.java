/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTIntConstNode;
import org.eclipse.photran.internal.core.parser.ASTLoopControlNode;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;

/**
 * Refactoring to reverse a DO-loop.
 * <p>
 * For example, this will change <tt>DO I = 1, 5, 2</tt> into <tt>DO I = 5, 1, -2</tt>. 
 * 
 * @author Ashley Kasza
 */
public class ReverseLoopRefactoring extends FortranEditorRefactoring
{
    private ASTProperLoopConstructNode doLoop = null;

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        
        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());
        doLoop = getLoopNode(this.astOfFileInEditor, this.selectedRegionInEditor);
        if(doLoop == null){fail(Messages.ReverseLoopRefactoring_SelectDoLoop);}  
        
        
    }
    
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // No final preconditions
    }
    
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        changeDoLoopHeader();
        Reindenter.reindent(doLoop, this.astOfFileInEditor, Strategy.REINDENT_EACH_LINE);
        this.addChangeFromModifiedAST(this.fileInEditor, pm);
        vpg.releaseAST(this.fileInEditor);
    }

    private void changeDoLoopHeader()
    {
        ASTLoopControlNode doLoopControlNode = doLoop.getLoopHeader().getLoopControl();
        
        IExpr low = doLoopControlNode.getLb(); // "lower bound" as in the first number to appear, not necessarily the lower number
        IExpr high = doLoopControlNode.getUb();
        IExpr step = doLoopControlNode.getStep(); // null if there isn't one.
        
        doLoopControlNode.setUb(low);
        doLoopControlNode.setLb(high);
        doLoopControlNode.setStep(updateStep(step));
    }
    
    protected ASTIntConstNode updateStep(IExpr step)
    {
        if (step == null)
        {
            // The loop was incrementing by 1, so change to decrement by 1
            ASTIntConstNode i = new ASTIntConstNode();
            i.setIntConst(new Token(null, ",-1")); //$NON-NLS-1$
            return i;
        }
        else
        {
            ASTIntConstNode j = new ASTIntConstNode();
            j.setIntConst(new Token(null, "-(" + step.toString() + ")")); //$NON-NLS-1$ //$NON-NLS-2$
            return j;
        }
    }
    
    
    @Override
    public String getName()
    {
        return Messages.ReverseLoopRefactoring_Name;
    }
}
