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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTEndDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLabelDoStmtNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter.Strategy;

/**
 * 
 * @author Tim
 */
public class InterchangeLoopsRefactoring extends SingleFileFortranRefactoring
{
    
    private StatementSequence selection = null;
    private ASTProperLoopConstructNode outerLoop = null;
    private ASTProperLoopConstructNode innerLoop = null;
 
    /**
     * @param file
     * @param selection
     */
    public InterchangeLoopsRefactoring(IFile file, ITextSelection selection)
    {
        super(file, selection);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring#doCheckFinalConditions(org.eclipse.ltk.core.refactoring.RefactoringStatus, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring#doCheckInitialConditions(org.eclipse.ltk.core.refactoring.RefactoringStatus, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        
        // Ensure that partial loops won't be extracted
        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());
        setLoops();
        //If there is only 1 loop, don't do anything
        if(outerLoop == null || innerLoop == null || outerLoop == innerLoop)
            fail("Please select nested loops to refactor.");
    }

    protected void setLoops()
    {
        outerLoop = getLoopNode(this.astOfFileInEditor, this.selectedRegionInEditor);
        if(outerLoop != null)
            innerLoop = getLoopNode(outerLoop.getBody().findFirstToken(),
                                    outerLoop.getBody().findLastToken());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring#doCreateChange(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        swapHeaders();
        swapEndDoStmt();
        swapComments();
        
        Reindenter.reindent(outerLoop, this.astOfFileInEditor, Strategy.REINDENT_EACH_LINE);
        //Reindenter.reindent(innerLoop, this.astOfFileInEditor);
        
        this.addChangeFromModifiedAST(this.fileInEditor, pm);
    }
    
    protected void swapHeaders()
    {
        ASTLabelDoStmtNode outerHeader = outerLoop.getLoopHeader();
        ASTLabelDoStmtNode innerHeader = innerLoop.getLoopHeader();
        
        /*innerLoop.replaceChild(innerHeader, outerHeader);
        outerLoop.replaceChild(outerHeader, innerHeader);
        
        outerHeader.setParent(innerLoop);
        innerHeader.setParent(outerLoop);*/
        swap(outerHeader, innerHeader);
    }
    
    protected void swapEndDoStmt()
    {
        ASTEndDoStmtNode outerEnd = outerLoop.getEndDoStmt();
        ASTEndDoStmtNode innerEnd = innerLoop.getEndDoStmt();
        
        /*outerEnd.setParent(innerLoop);
        innerEnd.setParent(outerLoop);
        
        innerLoop.replaceChild(innerEnd, outerEnd);
        outerLoop.replaceChild(outerEnd, innerEnd);*/
        swap(outerEnd, innerEnd);
    }
    
    protected void swapComments()
    {
        Token outerLoopDoToken = outerLoop.getLoopHeader().findFirstToken();
        Token innerLoopDoToken = innerLoop.getLoopHeader().findFirstToken();
        
        String outerLoopWhiteText = outerLoopDoToken.getWhiteBefore();
        String innerLoopWhiteText = innerLoopDoToken.getWhiteBefore();
        
        innerLoopDoToken.setWhiteBefore(outerLoopWhiteText);
        outerLoopDoToken.setWhiteBefore(innerLoopWhiteText);
    }

    protected void swap(ASTNode outerElement, ASTNode innerElement)
    {
        outerElement.setParent(innerLoop);
        innerElement.setParent(outerLoop);
        
        innerLoop.replaceChild(innerElement, outerElement);
        outerLoop.replaceChild(outerElement, innerElement); 
    }
    

    /* (non-Javadoc)
     * @see org.eclipse.ltk.core.refactoring.Refactoring#getName()
     */
    @Override
    public String getName()
    {
        return "Interchange Loops";
    }

}
