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

import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.parser.ASTContinueStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndIfStmtNode;
import org.eclipse.photran.internal.core.parser.ASTGotoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTIfConstructNode;
import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;

/**
 * Remove branching to END IF statement from outside its IF ... END IF block. Such branching should
 * be replaced with branching to CONTINUE statement that immediately follows the END IF statement.
 * If the END IF statement is followed by a CONTINUE statement then outside GOTO branches should
 * target the CONTINUE statement. If one does not exist the refactoring will insert one and target
 * it. GOTOs inside the selected IF block are not re-targeted.
 * 
 * User Selection Requirements: The labeled END IF that they want considered for the refactoring.
 * 
 * @author Rita Chow (chow15), Jerry Hsiao (jhsiao2), Mark Mozolewski (mozolews), Chamil Wijenayaka
 *         (wijenay2), Nicola Hall (nfhall2)
 */
public class RemoveBranchToEndIfRefactoring extends FortranEditorRefactoring
{
    private ASTEndIfStmtNode selectedEndIfNode;
    private ASTIfConstructNode selectedIfConstructNode;
    private ASTContinueStmtNode continueAfterIfStmtNode;
    private LinkedList<ASTGotoStmtNode> gotoStatementWithEndIfLabel;
    private LinkedList<ASTGotoStmtNode> selectedGotoStatementWithEndIfLabel;

    /**
     * Preconditions that are checked before the refactoring is applied are: 1) END IF selected must
     * be labeled and part of an IF block. 2) GOTO from either inside the selected IF block or
     * outside the selected IF block must target the label of the END IF selected. 3) must be more
     * GOTOs than just in local selected END IF scope.
     * 
     */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        // Check if selected text is a portion within an END IF statement with label.
        selectedEndIfNode = findEnclosingNode(this.astOfFileInEditor, this.selectedRegionInEditor, ASTEndIfStmtNode.class);
        if (selectedEndIfNode == null || selectedEndIfNode.getLabel() == null)
            fail(Messages.RemoveBranchToEndIfRefactoring_PleaseSelectLabeledEndIfStatement);

        // Check if selected END IF is within an IF block.
        selectedIfConstructNode = selectedEndIfNode.findNearestAncestor(ASTIfConstructNode.class);
        if (selectedIfConstructNode == null)
            fail(Messages.RemoveBranchToEndIfRefactoring_NoEnclosingIfConstruct);

        // Check if selected scope contains a branch.
        LinkedList<ASTGotoStmtNode> gotoStatements = getGotoNodes(ScopingNode.getLocalScope(selectedEndIfNode));
        if (gotoStatements.isEmpty())
            fail(Messages.RemoveBranchToEndIfRefactoring_NoGotoStatements);

        // Check if contains a GOTO statement with selected END IF label.
        gotoStatementWithEndIfLabel = findGotoForLabel(gotoStatements, selectedEndIfNode.getLabel().getText());
        if (gotoStatementWithEndIfLabel.size() == 0)
            fail(Messages.RemoveBranchToEndIfRefactoring_NoGotoStatementReferencingThisLabel);

        // Check to see if there are branches outside of selected if block to selected END IF label.
        LinkedList<ASTGotoStmtNode> selectedGotoStatements = getGotoNodes(selectedIfConstructNode);
        selectedGotoStatementWithEndIfLabel = findGotoForLabel(selectedGotoStatements,
            selectedEndIfNode.getLabel().getText());
        if (selectedGotoStatementWithEndIfLabel.size() == gotoStatementWithEndIfLabel.size())
            fail(Messages.RemoveBranchToEndIfRefactoring_BranchToImmediateEndIf);

        continueAfterIfStmtNode = continueAfterIfStmt(selectedIfConstructNode);
    }

    /**
     * There are no final conditions.
     */
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // No final conditions
    }

    /**
     * Separate refactroing methods based on if there is a CONTINUE statement after the selected END
     * IF (if so target it) or if no CONTINUE statement exists after the END IF block (insert one
     * with unique label and have GOTO statements outside the IF block target it.).
     */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        if (continueAfterIfStmtNode == null)
            changeNoContinueAfterEndIf(); // User Story #2
        else
            changeGotoLabelToContinueLabel(); // User Story #1

        // User Story 3 checked in doCheckInitialConditions
        this.addChangeFromModifiedAST(this.fileInEditor, pm);
    }

    /**
     * Main refactoring code for condition when CONTINUE statement follows the END..IF that was
     * selected for refactoring. The logic will renumber/retarget any GOTO statements in the GOTO to
     * that of the CONINUE label. (Per FORTRAN language standard the CONTINUE statement must have a
     * label). If any GOTO statement inside the selected END..IF targets the END..IF selected for
     * the Refactoring then the label of the END..IF will not be removed and that inner GOTO will
     * still target it. If there are no inner GOTOs targeting the END..IF label then the END..IF
     * label will be removed as part of the refactoring.
     * 
     */
    private void changeGotoLabelToContinueLabel()
    {
        // Check all GOTO statements in the entire PROGRAM to see if they target the END..IF label
        // number selected during the Refactoring. If so then retarget/renumber them to the
        // CONTINUE block that follows. (Note: Inner GOTO statements that that target the END..IF
        // statement are not retargeted/renumbered.)
        for (ASTGotoStmtNode gotoNode : gotoStatementWithEndIfLabel)
        {
            // Do not re-target GOTO statements from within the selected END..IF for refactoring.
            if (!selectedGotoStatementWithEndIfLabel.contains(gotoNode))
            { // Goto targeted to our selected IF..ENDIF block
                gotoNode.getGotoLblRef().getLabel()
                    .setText(continueAfterIfStmtNode.getLabel().getText()); // Use existing
            }
        }

        // Label of selected END..IF for Refactoring is only removed after all other GOTOs have been
        // retargeted and if no inner GOTO statements target its label.
        if (selectedGotoStatementWithEndIfLabel.isEmpty())
        {
            selectedEndIfNode.setLabel(null);
            selectedEndIfNode.findFirstToken().setWhiteBefore(
                selectedIfConstructNode.findFirstToken().getWhiteBefore()); // reindenter alt.
        }
    }

    /**
     * Modify Fortran code to add a CONTINUE statement after labeled END IF then remove END IF label
     * only if there are no GOTO statements within the selected IF statement that target that END IF
     * statement.
     * 
     */
    private void changeNoContinueAfterEndIf()
    {
        // build the CONTINUE node
        if (continueAfterIfStmtNode != null) { return; }

        @SuppressWarnings("unchecked")
        ASTListNode<ASTNode> listNode = (ASTListNode<ASTNode>)selectedIfConstructNode.getParent();

        // build the CONTINUE statement program source code
        String programString = "program p\n" + selectedEndIfNode.getLabel().getText() + " CONTINUE" //$NON-NLS-1$ //$NON-NLS-2$
            + EOL + "end program"; //$NON-NLS-1$
        ASTMainProgramNode programNode = (ASTMainProgramNode)parseLiteralProgramUnit(programString);
        ASTContinueStmtNode continueStmt = (ASTContinueStmtNode)programNode.getBody().get(0);

        // insert into AST
        continueStmt.setParent(selectedIfConstructNode.getParent());
        listNode.insertAfter(selectedIfConstructNode, continueStmt);

        // clear label on END IF statement
        if (selectedGotoStatementWithEndIfLabel.size() == 0)
        {
            selectedEndIfNode.setLabel(null);
            // correct indentation
            selectedEndIfNode.findFirstToken().setWhiteBefore(
                selectedIfConstructNode.findFirstToken().getWhiteBefore());
        }
        else
        {
            // Grab all labels
            LinkedList<IActionStmt> actionStmts = getActionStmts(ScopingNode
                .getLocalScope(selectedEndIfNode));

            // Calculate unique label
            String label = getUniqueLabel(actionStmts);

            // Set continue label to new label
            continueStmt.getLabel().setText(label);

            // Set all goto statements to new label
            for (ASTGotoStmtNode node : gotoStatementWithEndIfLabel)
            {
                if (!selectedGotoStatementWithEndIfLabel.contains(node))
                {
                    node.getGotoLblRef().getLabel().setText(label);
                }
            }
        }
    }

    /**
     * Build a list of all GOTO node types from the starting node.
     * 
     * @param startNode Node to start the search.
     * 
     * @return LinkedList of GOTO nodes.
     */
    private LinkedList<ASTGotoStmtNode> getGotoNodes(IASTNode startNode)
    {
        if (startNode == null) { return null; }

        /*
         * Unable to find all goto statements when there are do loops in startNode, so need to
         * search in the do loops separately.
         */
        final LinkedList<ASTGotoStmtNode> gotoNodes = getGotoStmtsInAllProperLoopConstructs(startNode);

        startNode.accept(new ASTVisitor()
        {
            @Override
            public void visitASTGotoStmtNode(ASTGotoStmtNode node)
            {
                gotoNodes.add(node);
            }
        });

        return gotoNodes;
    }

    /**
     * Build a list of all proper-loop-construct node types from the starting node.
     * 
     * @param startNode Node to start the search.
     * @return LinkedList of proper-loop-construct nodes.
     */
    private LinkedList<ASTGotoStmtNode> getGotoStmtsInAllProperLoopConstructs(IASTNode startNode)
    {
        final LinkedList<ASTGotoStmtNode> gotoNodes = new LinkedList<ASTGotoStmtNode>();
        final LinkedList<ASTProperLoopConstructNode> loopNodes = getProperLoopConstructs(startNode);

        for (ASTProperLoopConstructNode loop : loopNodes)
        {
            for (IASTNode node : loop.getBody())
            {
                node.accept(new ASTVisitor()
                {
                    @Override
                    public void visitASTGotoStmtNode(ASTGotoStmtNode node)
                    {
                        gotoNodes.add(node);
                    }
                });
            }
        }

        return gotoNodes;
    }

    /**
     * Find GOTO node(s) based on matching label.
     * 
     * @param gotos List of GOTO statements to check for matching label.
     * @param label Label to match against.
     * @return List of GOTO nodes that target the specified label.
     */
    private LinkedList<ASTGotoStmtNode> findGotoForLabel(LinkedList<ASTGotoStmtNode> gotos,
        String label)
    {
        if (gotos == null) { return new LinkedList<ASTGotoStmtNode>(); }

        LinkedList<ASTGotoStmtNode> gotoWithLabel = new LinkedList<ASTGotoStmtNode>();
        for (ASTGotoStmtNode gotoNode : gotos)
        {
            if (gotoNode.getGotoLblRef().getLabel().getText().contentEquals(label))
            {
                gotoWithLabel.add(gotoNode);
            }
        }

        return gotoWithLabel;
    }

    /**
     * Check for CONTINUE statement after a if construct node
     * 
     * @param ifStmt If construct node.
     * @return continue statement node or null.
     */
    private static ASTContinueStmtNode continueAfterIfStmt(ASTIfConstructNode ifStmt)
    {
        if (ifStmt == null) { return null; }

        @SuppressWarnings("unchecked")
        ASTListNode<ASTNode> list = (ASTListNode<ASTNode>)ifStmt.getParent();

        for (int i = 0; i < list.size() - 1; i++)
        {
            if (list.get(i) != null)
            {
                if (list.get(i).equals(ifStmt))
                {
                    if (list.get(i + 1) instanceof ASTContinueStmtNode) { return (ASTContinueStmtNode)list
                        .get(i + 1); }
                }
            }
        }

        return null;
    }

    /**
     * Generate a unique label based on the labels passed in (unique in that find the largest and
     * add 10)
     * 
     * @param actionStmts List of statements that may be labeled to consider for a unique label.
     * @return Unique label value.
     */
    private String getUniqueLabel(LinkedList<IActionStmt> actionStmts)
    {
        int label = Integer.parseInt(selectedEndIfNode.getLabel().getText());
        for (IActionStmt stmt : actionStmts)
        {
            if (stmt.getLabel() != null)
            {
                int currentLabel = Integer.parseInt(stmt.getLabel().getText());
                if (currentLabel > label)
                {
                    label = currentLabel;
                }
            }
        }
        label += 10;

        return String.valueOf(label);
    }

    /**
     * Find Action statement nodes in the tree from the starting search node.
     * 
     * @param startNode Starting node from which to perform the search.
     * @return LinkedList of action statement nodes that are found.
     */
    private LinkedList<IActionStmt> getActionStmts(IASTNode startNode)
    {
        final LinkedList<IActionStmt> actionStmts = getActionStmtsInAllProperLoopConstructs();
        startNode.accept(new ASTVisitor()
        {
            @Override
            public void visitIActionStmt(IActionStmt node)
            {
                actionStmts.add(node);
            }
        });

        return actionStmts;
    }

    /**
     * Find Action statement nodes in entire file in editor.
     * 
     * @return LinkedList of action statement nodes found.
     */
    private LinkedList<IActionStmt> getActionStmtsInAllProperLoopConstructs()
    {
        final LinkedList<IActionStmt> actionStmts = new LinkedList<IActionStmt>();
        final LinkedList<ASTProperLoopConstructNode> loopNodes = getProperLoopConstructs(ScopingNode
            .getLocalScope(selectedEndIfNode));

        for (ASTProperLoopConstructNode loop : loopNodes)
        {
            for (IASTNode node : loop.getBody())
            {
                node.accept(new ASTVisitor()
                {
                    @Override
                    public void visitIActionStmt(IActionStmt node)
                    {
                        actionStmts.add(node);
                    }
                });
            }
        }

        return actionStmts;
    }

    /**
     * Find all proper loop constructs in the tree from the starting search node.
     * 
     * @param startNode Starting node from which to perform the search.
     * @return LinkedList of proper loop construct nodes found.
     */
    private LinkedList<ASTProperLoopConstructNode> getProperLoopConstructs(IASTNode startNode)
    {
        final LinkedList<ASTProperLoopConstructNode> loopNodes = new LinkedList<ASTProperLoopConstructNode>();

        // Change AST to represent DO-loops as ASTProperLoopConstructNodes
        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());

        startNode.accept(new ASTVisitorWithLoops()
        {
            @Override
            public void visitASTProperLoopConstructNode(ASTProperLoopConstructNode node)
            {
                loopNodes.add(node);
            }
        });

        return loopNodes;
    }

    /**
     * Provide GUI refactoring label.
     */
    @Override
    public String getName()
    {
        return Messages.RemoveBranchToEndIfRefactoring_Name;
    }
}
