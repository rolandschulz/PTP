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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.binding.VariableAccess;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTIntConstNode;
import org.eclipse.photran.internal.core.parser.ASTLoopControlNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;
import org.eclipse.rephraserengine.core.refactorings.UserInputBoolean;
import org.eclipse.rephraserengine.core.refactorings.UserInputString;

/**
 * Refactoring used to unroll a loop a certain number of times or completely.
 * 
 * @author Ashley Kasza
 */
public class UnrollLoopRefactoring extends FortranEditorRefactoring
{
    private String LOOP_UPPER_BOUND = "loopUpperBound"; //$NON-NLS-1$

    private ASTProperLoopConstructNode doLoop = null;

    // user inputs
    private boolean isCompleteUnrolling = false;

    private int iterationStep = 0;

    private boolean check = true;

    //protected boolean variableIsWritten;

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());

        // get loop node from the ast at the selected region
        doLoop = getLoopNode(this.astOfFileInEditor, this.selectedRegionInEditor);
        // fail if no do loop was selected
        if (doLoop == null)
        {
            fail(Messages.ReverseLoopRefactoring_SelectDoLoop);
        }

        IASTListNode<IExecutionPartConstruct> body = doLoop.getBody();
        if (checkForLabels(body))
        {
            fail(Messages.UnrollLoopRefactoring_cannotUnrollLoopWithLabel);
        }
        try
        {
            doLoop.getStepInt();
        }
        catch (NumberFormatException e)
        {
            fail(Messages.UnrollLoopRefactoring_InvalidStepError);
        }
        if (!(doLoop.getUpperBoundIExpr() instanceof ASTIntConstNode))
        {
            if(!checkLoopUpperBoundsNameAvailable())
                fail(Messages.UnrollLoopRefactoring_unableToCreateUpperBound);
        }
        if(checkIndexVariableWrite(doLoop.getBody()))
            fail(Messages.bind(Messages.UnrollLoopRefactoring_LoopWritesToIndexVariable, doLoop.getIndexVariable().getText()));
    }
    
    private boolean checkIndexVariableWrite(IASTNode node)
    {
        FindNameVisitor findNameUse = new FindNameVisitor(doLoop.getIndexVariable().getText());
        node.accept(findNameUse);
        return findNameUse.getNameIsUsed();
        
    }
    public class FindNameVisitor extends ASTVisitorWithLoops
    {
        private boolean indexIsWritten;
        private String indexVariableName;
        public FindNameVisitor(String name)
        {
            super();
            indexIsWritten = false;
            indexVariableName = name;
        }
        public boolean getNameIsUsed()
        {
            return indexIsWritten;
        }
        @Override
        public void visitToken(Token token)
        {
            if (token.getTerminal() == Terminal.T_IDENT && token.getText().equals(indexVariableName))
            {
                if((token.getVariableAccessType()).equals(VariableAccess.WRITE))
                {
                    indexIsWritten = true;
                }
            }
        }
    }
    
    private boolean checkLoopUpperBoundsNameAvailable(){
        boolean canUse;
        for (int i = 1; i <= 10; i++)
        {
            canUse = true;
            ScopingNode scope = ScopingNode.getLocalScope(doLoop);
            List<Definition> defList = scope.getAllDefinitions();
            for (Definition d : defList)
            {
                if (d.getCanonicalizedName().equals(LOOP_UPPER_BOUND.toLowerCase()))
                {
                    LOOP_UPPER_BOUND = "loopUpperBound" + Integer.toString(i); //$NON-NLS-1$
                    canUse = false;
                }
            }
            if (canUse == true)
            {
                //break;
                return true;
            }
        }
        return false;
    }
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {

        if ((!(doLoop.getLowerBoundIExpr() instanceof ASTIntConstNode) || !(doLoop
            .getUpperBoundIExpr() instanceof ASTIntConstNode)) && isCompleteUnrolling)
        {
            fail(Messages.UnrollLoopRefactoring_SelectLoopWithExplicitBound);
        }
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        ASTLoopControlNode doLoopControlNode = doLoop.getLoopHeader().getLoopControl();
        IASTListNode<IExecutionPartConstruct> doLoopBody = doLoop.getBody();
        // Must get the parent here before any changes are made.
        IASTNode doLoopParent = doLoop.getParent();

        if (isCompleteUnrolling)
        {

            completeLoopUnrolling(doLoopControlNode, doLoopBody); 

        }
        else
        {
            numberedLoopUnrolling(doLoopBody, doLoopControlNode);

        }

        Reindenter.reindent(doLoopParent, this.astOfFileInEditor, Strategy.REINDENT_EACH_LINE);

        this.addChangeFromModifiedAST(this.fileInEditor, pm);

        vpg.releaseAST(this.fileInEditor);

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

            if ((body.get(i)).findFirstToken().getTerminal() == Terminal.T_ICON)
            {// instanceof ASTLabelNode){
                return true;
            }
        }
        return false;
    }

    /***********************************************************
     * Functions used for unrolling a certain number of times.
     ***********************************************************/

    /**
     * Function copies the body of the loop n times, replacing "i" with 1-n every time it copies
     * @param doLoopBody - the body of the loop that's being unrolled
     * @param doLoopControlNode - the bounds and step of the loop
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void numberedLoopUnrolling(IASTListNode<IExecutionPartConstruct> doLoopBody,
        ASTLoopControlNode doLoopControlNode)
    {
        if (!(doLoop.getUpperBoundIExpr() instanceof ASTIntConstNode))
        {
            evalExpressionBeforeLoop();
            // upper = parseLiteralExpression(LOOP_UPPER_BOUND);
            doLoopControlNode.setUb(parseLiteralExpression(LOOP_UPPER_BOUND));
        }
        boolean checkAllStatements = false;
        IASTListNode newBody = (IASTListNode<IExecutionPartConstruct>)(doLoopBody.clone());
        IASTListNode dummyBody = null;
        String iterationName = (doLoopControlNode.getVariableName()).getText();
        // IExpr stepNode = doLoopControlNode.getStep();
        int step = 1;
        int checkBound = 0;
        if (doLoop.getLowerBoundIExpr() instanceof ASTIntConstNode
            && doLoop.getUpperBoundIExpr() instanceof ASTIntConstNode)
        {
            int high = doLoop.getUpperBoundInt();
            int low = doLoop.getLowerBoundInt();
            checkBound = (high - low + 1) % (step * iterationStep);
        }else{
            checkAllStatements = true;
        }
        
        step = doLoop.getStepInt();
        // System.out.println(step);
        for (int i = 1; i < iterationStep; i++)
        {
            // replace "i" with 1-#, and add it to the AST
            dummyBody = replaceIndexVariableNameInBody(doLoop.getBody(), iterationName, (i) * step);

            if (check == true && (i == checkBound || checkAllStatements))
            {
                dummyBody.add(0, includeBoundsCheck(i * step, iterationName));
            }
            newBody.addAll(dummyBody);
        }
        doLoopBody.replaceWith(newBody);
        doLoop.setStepInt(step * iterationStep);
        // changeDoLoopStep(doLoopControlNode);
    }

    /**
     * Adds an if statement before each loop body to check bounds
     * @param step - by how much a loop steps
     * @return
     */
    private IASTNode includeBoundsCheck(int indexPlus, String iterationName)
    {
        IASTNode checkNode = null;
        IExpr upper = doLoop.getUpperBoundIExpr();
        String checkBounds = "if(" + iterationName + "+" + indexPlus; //$NON-NLS-1$ //$NON-NLS-2$
        if (doLoop.getStepInt() > 0)
        { // loop increments
            checkBounds += ">" + upper.findFirstToken().getText();//$NON-NLS-1$
        }
        else
        { // loop decrements
            checkBounds += "<" + upper.findFirstToken().getText(); //$NON-NLS-1$
        }
        checkBounds += ") exit"; //$NON-NLS-1$
        checkNode = parseLiteralStatementSequence(checkBounds);
        return checkNode;
    }

    /**
     * take an expression out of the upper bound and evals it before the loop.
     * @param ub - upper bound of a loop
     */
    @SuppressWarnings("unchecked")
    private void evalExpressionBeforeLoop()
    {

        ScopingNode scope = ScopingNode.getLocalScope(doLoop);
        IASTListNode<IASTNode> body = (IASTListNode<IASTNode>)scope.getOrCreateBody();
        String upperBound = LOOP_UPPER_BOUND + " = " + doLoop.getUpperBoundIExpr().toString(); //$NON-NLS-1$
        int upperBoundIndx = (body).indexOf(doLoop);
        IASTNode upperBoundNode = parseLiteralStatement(upperBound);
        body.add(upperBoundIndx, upperBoundNode);
        String declarationString = "integer :: " + LOOP_UPPER_BOUND; //$NON-NLS-1$
        int insertionIndex = findIndexToInsertTypeDeclaration(body);
        body.add(insertionIndex, parseLiteralStatement(declarationString));
    }

    /**
     * Function to visit all statements in node and replace instances of "i"
     * @param node - node to search through
     * @param name - instance name to look for (usually "i")
     * @param iteration - number to replace "i" with
     */

    @SuppressWarnings("rawtypes")
    private IASTListNode replaceIndexVariableNameInBody(IASTNode node, final String name,
        final int iteration)
    {
        IASTListNode newBody = (IASTListNode)(node.clone());
        newBody.accept(new ASTVisitorWithLoops()
        {
            @Override
            public void visitToken(Token token)
            {
                if (token.getTerminal() == Terminal.T_IDENT && (token.getText()).equals(name))
                {
                    String s2 = token.getText();
                    s2 = "(" + s2 + "+" + Integer.toString(iteration) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    token.replaceWith(s2);
                }
            }

        });
        return newBody;
    }


    /********************************************************
     * Functions for completely unrolling a loop
     ********************************************************/

    /**
     * Determines the number of times to copy the body, then copies the body and replaces the
     * iteration variable with 0-#
     * @param doLoopControlNode is the bounds and step of the loop
     * @param doLoopBody is all the statements contained in the do loop
     */

    private void completeLoopUnrolling(ASTLoopControlNode doLoopControlNode,
        IASTListNode<IExecutionPartConstruct> doLoopBody)
    {
        Token varName = doLoopControlNode.getVariableName();
        String iterationName = varName.getText(); // iterationName = i in do i=1,5
        int l = doLoop.getLowerBoundInt();
        int h = doLoop.getUpperBoundInt();

        int step = 1;
        step = doLoop.getStepInt();
        IASTListNode<IExecutionPartConstruct> dummyBody = null;
        IASTListNode<IExecutionPartConstruct> newBody = null;

        newBody = replaceIndexVariableNameWithConst(doLoopBody, l, iterationName);
        int counter = 1;
        int bound = Math.abs(h - l);
        int j = l;
        while (counter <= bound)
        {
            j += step;
            dummyBody = replaceIndexVariableNameWithConst(doLoopBody, j, iterationName);
            newBody.addAll(dummyBody);
            counter += Math.abs(step);
        }
        doLoop.replaceWith(newBody);
    }

    /**
     * Function for finding all instances of "i" and changing them to a specific number
     * @param node - the node to search through
     * @param stepNum - the number used to replace "i"
     */
    @SuppressWarnings("unchecked")
    private IASTListNode<IExecutionPartConstruct> replaceIndexVariableNameWithConst(IASTNode node,
        final int stepNum, final String iterationName)
    {
        IASTListNode<IExecutionPartConstruct> newBody = (IASTListNode<IExecutionPartConstruct>)node.clone();
        newBody.accept(new ASTVisitorWithLoops()
        {
            @Override
            public void visitToken(Token token)
            {
                if (token.getTerminal() == Terminal.T_IDENT
                    && token.getText().equalsIgnoreCase(iterationName))
                {
                    // replace i with iteration step
                    String s = Integer.toString(stepNum);
                    token.setText(s);
                }
            }
        });
        return newBody;
    }

    /****************************
     * User Input functions
     ***************************/

    @UserInputString(label = "Enter unrolling count ", defaultValueMethod = "getSuggestedUnrollingCount")
    public void setLoopUnrollNumber(String input)
    {
        iterationStep = Integer.parseInt(input);
    }

    public String getSuggestedUnrollingCount()
    {
        return "4"; //$NON-NLS-1$
    }

    @UserInputBoolean(label = "Complete unrolling")
    public void setComplete(boolean isComplete)
    {
        isCompleteUnrolling = isComplete;
    }

    @UserInputBoolean(label = "Include bounds checking", defaultValue = true)
    public void setBoundsChecking(boolean boundsCheck)
    {
        check = boundsCheck;
    }

    @Override
    public String getName()
    {
        return Messages.UnrollLoopRefactoring_LoopUnrollingName;
    }

}
