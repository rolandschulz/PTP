/*******************************************************************************
 * Copyright (c) 2010 Rita Chow, Nicola Hall, Jerry Hsiao, Mark Mozolewski, Chamil Wijenayaka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rita Chow - Initial Implementation
 *    Nicola Hall - Initial Implementation
 *    Jerry Hsiao - Initial Implementation
 *    Mark Mozolewski - Initial Implementation
 *    Chamil Wijenayaka - Initial Implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDoConstructNode;
import org.eclipse.photran.internal.core.parser.ASTIfConstructNode;
import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;

/**
 * Remove Real and Double Precision Loop Counter Refactoring:
 * 
 * This refactoring will take a controlled DO loop, i.e. on that specifies the starting and ending
 * values for the control loop index and transform it to an uncontrolled do loop where the same loop
 * index and values are manually inserted into to the loop. If a explicit step size is specified is
 * used, otherwise an implicit one is inserted. The refactoring checks the start/end values of the
 * loop control variable to determine if the loop control variable should increment or decrement.
 * 
 * User Selection Requirements: A controlled DO loop statement to be considered for refactoring.
 * 
 * @author Rita Chow (chow15), Nicola Hall (nfhall2), Jerry Hsiao (jhsiao2), Mark Mozolewski
 *         (mozolews), Chamil Wijenayaka (wijenay2)
 */
public class RemoveRealAndDoublePrecisionLoopCountersRefactoring extends FortranEditorRefactoring
{
    private ASTProperLoopConstructNode selectedDoLoopNode = null;

    private boolean shouldReplaceWithDoWhileLoop = false;

    /**
     * Provide GUI refactoring label.
     */
    @Override
    public String getName()
    {
        return Messages.RemoveRealAndDoublePrecisionLoopCountersRefactoring_Name;
    }

    /**
     * Set method for DO or DO WHILE refactoring selection variable.
     * 
     * @param value Set tracking variable for DO or DO WHILE refactoring selection.
     */
    public void setShouldReplaceWithDoWhileLoop(boolean value)
    {
        this.shouldReplaceWithDoWhileLoop = value;
    }

    /**
     * Precondition checks: Check if project has refactoring enabled.
     */
    /**
     * The following initial conditions are checked (a failure of any will result in a
     * fail-initial): - A controlled DO loop must be selected by the user. - Loop control variables
     * of a controlled DO loop must be declared as type REAL or DOUBLE PRECISION only.
     */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        // Change AST to represent DO-loops as ASTProperLoopConstructNodes
        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());

        // Identify selected DO loop.
        selectedDoLoopNode = findEnclosingNode(this.astOfFileInEditor, this.selectedRegionInEditor, ASTProperLoopConstructNode.class);

        // Check for invalid selection (DO statement not selected)
        if (selectedDoLoopNode == null)
        {
            fail(Messages.RemoveRealAndDoublePrecisionLoopCountersRefactoring_PleaseSelectADoLoop);
        }

        // Check uncontrolled DO loop selected.
        if (selectedDoLoopNode.getLoopHeader().getLoopControl() == null)
        {
            fail(Messages.RemoveRealAndDoublePrecisionLoopCountersRefactoring_PleaseSelectACountedDoLoop);
        }

        // Find all REAL and DOUBLE declared variables.
        List<Definition> bindings = selectedDoLoopNode.getLoopHeader().getLoopControl().getVariableName().resolveBinding();
        if (bindings.size() != 1)
            fail(Messages.RemoveRealAndDoublePrecisionLoopCountersRefactoring_NoUniqueDeclaration);
        
        Type type = bindings.get(0).getType();
        if (!type.equals(Type.REAL) && !type.equals(Type.DOUBLEPRECISION))
            fail(Messages.bind(Messages.RemoveRealAndDoublePrecisionLoopCountersRefactoring_NotRealOrDoublePrecision, type.toString()));
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
    }

    /**
     * After initial conditions checks pass there is a proper controlled DO loop variable to
     * refactor. The refactoring consists of building a number of AST node elements and inserting
     * them in to the program to do the work that the controlled DO loop would have done. This
     * includes: 1) Initial loop control variable assignment (starting value). 2)
     * Increment/Decrement control variable by specified amount (implicit or explicit step size) 3)
     * Check inside DO loop to see if loop variable limit specified is exceeded.
     */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        String ifCheckLb = selectedDoLoopNode.getLoopHeader().getLoopControl().getLb().toString();
        String ifCheckUb = selectedDoLoopNode.getLoopHeader().getLoopControl().getUb().toString();
        String ifCheckVar = selectedDoLoopNode.getLoopHeader().getLoopControl().getVariableName()
            .getText().toString();

        // Implicit step size check.
        String ifCheckStep = null;
        if (selectedDoLoopNode.getLoopHeader().getLoopControl().getStep() != null)
        {
            ifCheckStep = selectedDoLoopNode.getLoopHeader().getLoopControl().getStep().toString();
        }
        else
        {
            ifCheckStep = " 1"; //$NON-NLS-1$
        }

        String ifCheckIncrDecr = ""; //$NON-NLS-1$
        String ifCheckStr = ""; //$NON-NLS-1$

        // Work with both as double type.
        double dLb = Double.valueOf(ifCheckLb.trim()).doubleValue();
        double dUb = Double.valueOf(ifCheckUb.trim()).doubleValue();

        // Check for > or < IF check.
        if (dLb < dUb)
        {
            if (shouldReplaceWithDoWhileLoop)
                ifCheckStr = ifCheckVar + " <=" + ifCheckUb; //$NON-NLS-1$
            else
                ifCheckStr = ifCheckVar + " >" + ifCheckUb; //$NON-NLS-1$
            ifCheckIncrDecr = " +"; //$NON-NLS-1$
        }
        else
        {
            if (shouldReplaceWithDoWhileLoop)
                ifCheckStr = ifCheckVar + " >=" + ifCheckUb; //$NON-NLS-1$
            else
                ifCheckStr = ifCheckVar + " <" + ifCheckUb; //$NON-NLS-1$
            ifCheckIncrDecr = " -"; //$NON-NLS-1$
        }

        // AST modification for refactoring.
        if (shouldReplaceWithDoWhileLoop)
            insertNewDoWhileLoop(ifCheckLb, ifCheckVar, ifCheckStep, ifCheckIncrDecr, ifCheckStr);
        else
            insertNewDoLoop(ifCheckLb, ifCheckVar, ifCheckStep, ifCheckIncrDecr, ifCheckStr);

        // Finalize
        this.addChangeFromModifiedAST(this.fileInEditor, pm);
    }

    /**
     * With provided strings this method builds the new AST nodes for the initial variable
     * assignment, increment/decrement statement, and IF DO loop break statement. They are then
     * inserted in to the proper hierarchy of the program AST structure. (See main refactoring
     * comments at the top of the file for a list of steps performed.)
     * 
     * @param ifCheckLb Lower bound (starting value) of loop variable.
     * @param ifCheckVar Loop control variable name.
     * @param ifCheckStep Loop counter variable step size (implicitly or explicitly declared)
     * @param ifCheckIncrDecr (+/-) Depending on loop variable count direction.
     * @param ifCheckStr The IF check condition to break the DO loop.
     */
    private void insertNewDoLoop(String ifCheckLb, String ifCheckVar, String ifCheckStep,
        String ifCheckIncrDecr, String ifCheckStr)
    {
        // Build initial value assignment node.
        insertInitialCounterAssignment(ifCheckVar, ifCheckLb);

        // Remove DO control
        int sizeOfLoopControlString = selectedDoLoopNode.getLoopHeader().getLoopControl()
            .toString().length();
        String replaceLoopControlStringWith = ""; //$NON-NLS-1$
        for (int i = 0; i < sizeOfLoopControlString; i++)
        {
            replaceLoopControlStringWith += " "; //$NON-NLS-1$
        }
        selectedDoLoopNode.getLoopHeader().setLoopControl(null);
        selectedDoLoopNode
            .getLoopHeader()
            .findLastToken()
            .setWhiteBefore(
                replaceLoopControlStringWith
                    + selectedDoLoopNode.getLoopHeader().findLastToken().getWhiteBefore());

        // Build increment assignment node.
        ASTAssignmentStmtNode incrDecrNode = insertCounterAssignment(ifCheckVar, ifCheckIncrDecr,
            ifCheckStep);

        // Build IF node for checking DO limits.
        IExecutionPartConstruct lastNodeInDoLoopBody = selectedDoLoopNode.getBody().get(selectedDoLoopNode.getBody().size() - 1);
        String initialIndent = ScopingNode.getLocalScope(selectedDoLoopNode).getBody().findFirstToken().getWhiteBefore();
        initialIndent = initialIndent.substring(initialIndent.lastIndexOf('\n')+1);
        String programString = ""; //$NON-NLS-1$
        programString = "program p\n" + lastNodeInDoLoopBody.findFirstToken().getWhiteBefore() //$NON-NLS-1$
            + "IF(" + ifCheckStr + ") THEN\n" //$NON-NLS-1$ //$NON-NLS-2$
            + lastNodeInDoLoopBody.findFirstToken().getWhiteBefore()
            + initialIndent
            + "EXIT\n" //$NON-NLS-1$
            + lastNodeInDoLoopBody.findFirstToken().getWhiteBefore()
            + "END IF" + EOL + "end program"; //$NON-NLS-1$ //$NON-NLS-2$
        ASTMainProgramNode programNode = (ASTMainProgramNode)parseLiteralProgramUnit(programString);
        ASTIfConstructNode ifNode = (ASTIfConstructNode)programNode.getBody().get(0);

        // Insert IF node into AST
        ifNode.setParent(selectedDoLoopNode.getParent());
        selectedDoLoopNode.getBody().insertAfter(incrDecrNode, ifNode);
        //Reindenter.reindent(ifNode, astOfFileInEditor, Strategy.REINDENT_EACH_LINE);
    }

    /**
     * With provided strings this method builds the new AST nodes for the initial variable
     * assignment, increment/decrement statement, and DO WHILE loop check statement. They are then
     * inserted in to the proper hierarchy of the program AST structure. (See main refactoring
     * comments at the top of the file for a list of steps performed.)
     * 
     * @param ifCheckLb Lower bound (starting value) of loop variable.
     * @param ifCheckVar Loop control variable name.
     * @param ifCheckStep Loop counter variable step size (implicitly or explicitly declared)
     * @param ifCheckIncrDecr (+/-) Depending on loop variable count direction.
     * @param ifCheckStr The IF check condition to break the DO loop.
     */
    private void insertNewDoWhileLoop(String ifCheckLb, String ifCheckVar, String ifCheckStep,
        String ifCheckIncrDecr, String ifCheckStr)
    {
        // Build initial value assignment node.
        insertInitialCounterAssignment(ifCheckVar, ifCheckLb);

        // Build increment assignment node.
        insertCounterAssignment(ifCheckVar, ifCheckIncrDecr, ifCheckStep);

        // Change do loop to while do loop
        String programString = ""; //$NON-NLS-1$
        programString = "program p\n" //$NON-NLS-1$
            + selectedDoLoopNode.getLoopHeader().findFirstToken().getWhiteBefore() + "DO WHILE (" //$NON-NLS-1$
            + ifCheckStr + ")" //$NON-NLS-1$
            + selectedDoLoopNode.getLoopHeader().findLastToken().getWhiteBefore() + EOL
            + "EXIT\nENDDO" + EOL + "end program"; //$NON-NLS-1$ //$NON-NLS-2$

        ASTMainProgramNode programNode = (ASTMainProgramNode)parseLiteralProgramUnit(programString);
        ASTDoConstructNode doConstructNode = (ASTDoConstructNode)programNode.getBody().get(0);
        selectedDoLoopNode.setLoopHeader(doConstructNode.getLabelDoStmt());
        selectedDoLoopNode.getLoopHeader().findLastToken()
            .setWhiteBefore(selectedDoLoopNode.getLoopHeader().findLastToken().getWhiteBefore());
    }

    /**
     * Creates an assignment statement node for the initial loop counter value assignment and
     * inserts it before the selected DO loop of the refactoring.
     * 
     * @param ifCheckVar Loop control variable name.
     * @param ifCheckLb Lower bound (starting value) of loop variable.
     */
    private void insertInitialCounterAssignment(String ifCheckVar, String ifCheckLb)
    {
        String programString = "program p\n" + selectedDoLoopNode.findFirstToken().getWhiteBefore() + ifCheckVar + //$NON-NLS-1$
            " =" + ifCheckLb + EOL + "end program"; //$NON-NLS-1$ //$NON-NLS-2$
        ASTMainProgramNode programNode = (ASTMainProgramNode)parseLiteralProgramUnit(programString);
        ASTAssignmentStmtNode initValNode = (ASTAssignmentStmtNode)programNode.getBody().get(0);

        // Insert initial value assignment node into AST
        initValNode.setParent(selectedDoLoopNode.getParent());
        @SuppressWarnings("unchecked")
        ASTListNode<ASTNode> doNode = (ASTListNode<ASTNode>)selectedDoLoopNode.getParent();
        doNode.insertBefore(selectedDoLoopNode, initValNode);
        
        //Reindenter.reindent(initValNode, astOfFileInEditor, Strategy.REINDENT_EACH_LINE);
    }

    /**
     * Creates an assignment statement node for the loop counter variable assignment and inserts it
     * after the selected DO loop of the refactoring.
     * 
     * @param ifCheckVar Loop control variable name.
     * @param ifCheckIncrDecr (+/-) Depending on loop variable count direction.
     * @param ifCheckStep Loop counter variable step size (implicitly or explicitly declared)
     */
    private ASTAssignmentStmtNode insertCounterAssignment(String ifCheckVar,
        String ifCheckIncrDecr, String ifCheckStep)
    {
        IExecutionPartConstruct lastNodeInDoLoopBody = selectedDoLoopNode.getBody().get(
            selectedDoLoopNode.getBody().size() - 1);
        String programString = ""; //$NON-NLS-1$
        programString = "program p\n" + lastNodeInDoLoopBody.findFirstToken().getWhiteBefore() + ifCheckVar + //$NON-NLS-1$
            " = " + ifCheckVar + ifCheckIncrDecr + ifCheckStep + EOL + "end program"; //$NON-NLS-1$ //$NON-NLS-2$
        ASTMainProgramNode programNode = (ASTMainProgramNode)parseLiteralProgramUnit(programString);
        ASTAssignmentStmtNode incrDecrNode = (ASTAssignmentStmtNode)programNode.getBody().get(0);

        // Insert increment/decrement assignment node into AST
        incrDecrNode.setParent(selectedDoLoopNode.getParent());

        selectedDoLoopNode.getBody().insertAfter(lastNodeInDoLoopBody, incrDecrNode);
        //Reindenter.reindent(incrDecrNode, astOfFileInEditor, Strategy.REINDENT_EACH_LINE);

        return incrDecrNode;
    }
}