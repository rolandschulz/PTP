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

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTIntConstNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;

/**
 * Aligns the bounds of two loops, and then fuses them into a single loop. Only applies to loops with integer values as bounds. Also, be
 * aware of using the iteration variable for reads, writes, or parameters.
 * @author Ashley Kasza
 */
public class FuseLoopsRefactoring extends FortranEditorRefactoring
{
    private ArrayList<ASTProperLoopConstructNode> loopList;
    private ASTProperLoopConstructNode firstDoLoop;
    private ASTProperLoopConstructNode secondDoLoop;
    private int firstNormal, secondNormal;

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());

        loopList = new ArrayList<ASTProperLoopConstructNode>();

        firstDoLoop = getLoopNode(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (firstDoLoop == null)
        {
            fail(Messages.FuseLoopsRefactoring_NoSecondLoopErrorMsg);
        }

        secondDoLoop = getSecondLoopToAlign(firstDoLoop);
        if (secondDoLoop == null)
        {
            fail(Messages.FuseLoopsRefactoring_NoSecondLoopErrorMsg);
        }
        if (checkForIncorrectBounds())
        {
            fail(Messages.FuseLoopsRefactoring_SelectLoopWithIntegers);
        }
        try
        {
            firstDoLoop.getStepInt();
            secondDoLoop.getStepInt();
        }
        catch (NumberFormatException e)
        {
            fail(Messages.FuseLoopsRefactoring_InvalidStepError);
        }
        if (!checkLoopCompatibility(firstDoLoop, secondDoLoop))
        {
            fail(Messages.FuseLoopsRefactoring_IncompatibleLoopErorrMessage);
        }
        if (checkForLabels(firstDoLoop.getBody()) || checkForLabels(secondDoLoop.getBody()))
        {
            fail(Messages.FuseLoopsRefactoring_SelectLoopsWithoutLabels);
        }
        if (checkForCycleExit(firstDoLoop.getBody()) || checkForCycleExit(secondDoLoop.getBody()))
        {
            fail(Messages.FuseLoopsRefactoring_CycleExitFails);
        }
    }

    @SuppressWarnings("unchecked")
    private ASTProperLoopConstructNode getSecondLoopToAlign(ASTProperLoopConstructNode loop)
    {
        ScopingNode scope = ScopingNode.getLocalScope(loop);
        IASTListNode<IExecutionPartConstruct> body = (IASTListNode<IExecutionPartConstruct>)scope
            .getOrCreateBody();
        findAllLoopsInScope(body);
        int i = loopList.indexOf(loop);
        return loopList.get(i + 1);

    }

    private void findAllLoopsInScope(IASTNode node)
    {
        node.accept(new ASTVisitorWithLoops()
        {
            @Override
            public void visitASTProperLoopConstructNode(ASTProperLoopConstructNode node)
            {
                loopList.add(node);
            }
        });
    }

    private boolean checkForIncorrectBounds()
    {
        if (!(firstDoLoop.getLowerBoundIExpr() instanceof ASTIntConstNode)
            || !(firstDoLoop.getUpperBoundIExpr() instanceof ASTIntConstNode)
            || !(secondDoLoop.getLowerBoundIExpr() instanceof ASTIntConstNode)
            || !(secondDoLoop.getUpperBoundIExpr() instanceof ASTIntConstNode)) return true;
        return false;
    }

    /**
     * Checks for any labels in the body
     * @param body - body of the loop to check
     * @return true if there are labels, false if not
     */
    private boolean checkForLabels(IASTListNode<IExecutionPartConstruct> body)
    {
        for (int i = 0; i < body.size(); i++)
        {
            if ((body.get(i)).findFirstToken().getTerminal() == Terminal.T_ICON){ return true; }
        }
        return false;
    }
    
    private boolean checkForCycleExit(IASTNode node)
    {
        FindKeywordVisitor findCycleExit = new FindKeywordVisitor();
        node.accept(findCycleExit);
        boolean theanswer = findCycleExit.getHasCycleExit();
        return theanswer;
    }

    public class FindKeywordVisitor extends ASTVisitorWithLoops
    {
        private boolean hasCycleExit;
        public FindKeywordVisitor()
        {
            super();
            hasCycleExit = false;
        }
        public boolean getHasCycleExit()
        {
            return hasCycleExit;
        }
        @Override
        public void visitToken(Token token)
        {
            if (token.getTerminal() == Terminal.T_EXIT || token.getTerminal() == Terminal.T_CYCLE)
            {
                hasCycleExit = true;
            }
        }
    }
    
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // no final conditions
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        normalizeLoopIterations(firstDoLoop, secondDoLoop);
        fuseLoops(firstDoLoop, secondDoLoop);
        Reindenter.reindent(firstDoLoop, this.astOfFileInEditor, Strategy.REINDENT_EACH_LINE);

        this.addChangeFromModifiedAST(this.fileInEditor, pm);

        vpg.releaseAST(this.fileInEditor);
    }

    void normalizeLoopIterations(ASTProperLoopConstructNode first, ASTProperLoopConstructNode second)
    {
        int fLow = first.getLowerBoundInt();
        int fHigh = first.getUpperBoundInt();
        int sLow = second.getLowerBoundInt();
        int sHigh = second.getUpperBoundInt();
        int fStep = first.getStepInt();
        int sStep = second.getStepInt();

        firstNormal = fHigh - fLow;
        firstNormal = firstNormal / fStep;
        secondNormal = sHigh - sLow;
        secondNormal = secondNormal / sStep;
        replaceIndexVariableNameWithUpdated(first.getBody(), fStep, fLow, first.getIndexVariable()
            .getText());
        replaceIndexVariableNameWithUpdated(second.getBody(), sStep, sLow, second.getIndexVariable()
            .getText());

        first.setLowerBoundIExpr(0);
        second.setLowerBoundIExpr(0);
        first.setUpperBoundIExpr(firstNormal);
        second.setUpperBoundIExpr(secondNormal);
        first.setStepInt(1);
        second.setStepInt(1);

    }

    private void replaceIndexVariableNameWithUpdated(IASTNode node, final int stepNum, final int low,
        final String iterationName)
    {
        node.accept(new ASTVisitorWithLoops()
        {
            @Override
            public void visitToken(Token token)
            {
                if (token.getTerminal() == Terminal.T_IDENT
                    && token.getText().equalsIgnoreCase(iterationName))
                {
                    // replace i with iteration step i*step+lb
                    String s = "(" + token.getText() + "*" + Integer.toString(stepNum); //$NON-NLS-1$ //$NON-NLS-2$
                    s = s + "+" + Integer.toString(low) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    token.replaceWith(s);
                }
            }
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void fuseLoops(ASTProperLoopConstructNode first, ASTProperLoopConstructNode second)
    {
        IASTListNode firstBody = first.getBody();
        IASTListNode secondBody = parseLiteralStatementSequence(second.getBody().toString());
        replaceIndexVariableName(secondBody, (second.getIndexVariable().getText()),
            (first.getIndexVariable().getText()));
        firstBody.add(secondBody);

        second.removeFromTree();

    }

    private void replaceIndexVariableName(IASTNode node, final String name,
        final String replacer)
    {
        node.accept(new ASTVisitorWithLoops()
        {
            @Override
            public void visitToken(Token token)
            {
                if (token.getTerminal() == Terminal.T_IDENT && (token.getText()).equals(name))
                {
                    token.setText(replacer);
                }
            }
        });
    }

    private boolean checkLoopCompatibility(ASTProperLoopConstructNode first,
        ASTProperLoopConstructNode second)
    {
        calculateNormalizedLoopBounds(first, second);
        if (firstNormal == secondNormal) { return true; }
        return false;
    }

    private void calculateNormalizedLoopBounds(ASTProperLoopConstructNode first,
        ASTProperLoopConstructNode second)
    {
        int fLow = first.getLowerBoundInt();
        int fHigh = first.getUpperBoundInt();
        int sLow = second.getLowerBoundInt();
        int sHigh = second.getUpperBoundInt();
        int fStep = first.getStepInt();
        int sStep = second.getStepInt();
        firstNormal = fHigh - fLow;
        firstNormal = firstNormal / fStep;
        secondNormal = sHigh - sLow;
        secondNormal = secondNormal / sStep;
    }

    @Override
    public String getName()
    {
        return Messages.FuseLoopsRefactoring_LoopFusionName;
    }
}
