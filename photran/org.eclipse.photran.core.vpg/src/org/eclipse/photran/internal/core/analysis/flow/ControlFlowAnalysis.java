/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Fotzler, UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.parser.ASTAllStopStmtNode;
import org.eclipse.photran.internal.core.parser.ASTArithmeticIfStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAssignStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAssignedGotoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAssociateConstructNode;
import org.eclipse.photran.internal.core.parser.ASTBlockConstructNode;
import org.eclipse.photran.internal.core.parser.ASTComputedGotoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCriticalConstructNode;
import org.eclipse.photran.internal.core.parser.ASTCycleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTElseConstructNode;
import org.eclipse.photran.internal.core.parser.ASTElseIfConstructNode;
import org.eclipse.photran.internal.core.parser.ASTElseWhereConstructNode;
import org.eclipse.photran.internal.core.parser.ASTEndFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndModuleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndProgramStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTExitStmtNode;
import org.eclipse.photran.internal.core.parser.ASTForallConstructNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTGotoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTIfConstructNode;
import org.eclipse.photran.internal.core.parser.ASTIfStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLblRefListNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTMaskedElseWhereConstructNode;
import org.eclipse.photran.internal.core.parser.ASTModuleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTReturnStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSelectTypeConstructNode;
import org.eclipse.photran.internal.core.parser.ASTStopStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTWhereConstructNode;
import org.eclipse.photran.internal.core.parser.ASTWhereStmtNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.parser.IExecutableConstruct;
import org.eclipse.photran.internal.core.parser.IObsoleteActionStmt;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.vpg.PhotranVPGWriter;

/**
 * Generates a control flow graph of an AST.
 * <p>
 * This implementation performs two passes over the AST. The first pass collects all labels,
 * assigned labels, and loops mapped to the exit statement nodes contained in their body. This data
 * is necessary for constructs which behave similarly to a GOTO. The second pass builds the Control
 * Flow Graph.
 * <p>
 * FIXME: Enable in production (see {@link PhotranVPGWriter#TEMP_____ENABLE_FLOW_ANALYSIS}). This
 * is temporarily disabled for several reasons:
 * <ol>
 * <li>We need to check the impact on performance (indexing time)
 * <li>Note: The select case, if, do, and where constructs are not structured correctly in the AST.
 * For this patch, If/where should work correctly, but select case/old style do loops should not.
 * There are open bugs for these problems in Bugzilla.
 * <li>There is a small problem with the handling of if constructs: It works correctly when there
 * is an else clause, but when there isn't an else clause, there should also be an edge from the
 * if-then statement to the statement following the if construct. (This will be easy to fix after I
 * fix the AST.)
 * </ol>
 * 
 * @author Matthew Fotzler
 */
public class ControlFlowAnalysis extends ASTVisitorWithLoops
{
    public static void analyze(String filename, ASTExecutableProgramNode ast)
    {
        final HashMap<String, IActionStmt> labels = new HashMap<String, IActionStmt>();
        final HashMap<String, String> assignedLabels = new HashMap<String, String>();
        final HashMap<ASTProperLoopConstructNode, Set<ASTExitStmtNode>> exitsList = new HashMap<ASTProperLoopConstructNode, Set<ASTExitStmtNode>>();
        ast.accept(new ASTVisitorWithLoops()
        {
            @Override
            public void visitASTAssignStmtNode(ASTAssignStmtNode node)
            {
                assignedLabels.put(node.getVariableName().getText(), node.getAssignedLblRef()
                    .getLabel().getText());
            }

            @Override
            public void visitASTProperLoopConstructNode(ASTProperLoopConstructNode node)
            {
                exitsList.put(node, node.getExits());

                traverseChildren(node);
            }

            @Override
            public void visitIActionStmt(IActionStmt node)
            {
                if (node.getLabel() != null) labels.put(node.getLabel().getText(), node);
            }
        });
        ast.accept(new ControlFlowAnalysis(filename, labels, assignedLabels, exitsList));
    }

    private Set<IASTNode> predecessors = Collections.<IASTNode> emptySet();

    private HashMap<String, IActionStmt> labels = new HashMap<String, IActionStmt>();

    private HashMap<String, String> assignedLabels = new HashMap<String, String>();

    private HashMap<ASTProperLoopConstructNode, Set<ASTExitStmtNode>> exitsList = new HashMap<ASTProperLoopConstructNode, Set<ASTExitStmtNode>>();

    private Set<IASTNode> handled = new HashSet<IASTNode>(128);

    private ControlFlowAnalysis(String filename, HashMap<String, IActionStmt> labels,
        HashMap<String, String> assignedLabels,
        HashMap<ASTProperLoopConstructNode, Set<ASTExitStmtNode>> exitsList)
    {
        this.labels = labels;
        this.assignedLabels = assignedLabels;
        this.exitsList = exitsList;
        new PhotranTokenRef(filename, Integer.MAX_VALUE, 0);
    }

    private void createFlow(IASTNode pred, IASTNode toNode)
    {
        PhotranTokenRef from = pred.findFirstToken().getTokenRef();
        PhotranTokenRef to = toNode.findFirstToken().getTokenRef();

        PhotranVPG.getProvider().createFlow(from, to);
    }

    private void flowTo(IASTNode toNode)
    {
        for (IASTNode pred : predecessors)
            createFlow(pred, toNode);

        predecessors = Collections.<IASTNode> singleton(toNode);
        handled.add(toNode);
    }

    private void flowToExit(IASTNode node)
    {
        flowTo(node);
        predecessors = Collections.<IASTNode> emptySet();
    }
    
    @Override
    public void visitIActionStmt(IActionStmt node)
    {
        if (!handled.contains(node)) visitIExecutableConstruct(node);
    }

    @Override
    public void visitIExecutableConstruct(IExecutableConstruct node)
    {
        if (!handled.contains(node))
        {
            flowTo(node);
            handled.add(node);
        }
    }

    @Override
    public void visitIObsoleteActionStmt(IObsoleteActionStmt node)
    {
        if (!handled.contains(node)) visitIActionStmt(node);
    }

    @Override
    public void visitASTAllStopStmtNode(ASTAllStopStmtNode node)
    {
        flowTo(node);
        this.predecessors = Collections.<IASTNode> emptySet();
    }

    @Override
    public void visitASTArithmeticIfStmtNode(ASTArithmeticIfStmtNode node)
    {
        flowTo(node);

        String label = node.getFirst().getLabel().getText();
        IASTNode destination = labels.get(label);
        if (destination != null) createFlow(node, destination);
        
        label = node.getSecond().getLabel().getText();
        destination = labels.get(label);
        if (destination != null) createFlow(node, destination);
        
        label = node.getThird().getLabel().getText();
        destination = labels.get(label);
        if (destination != null) createFlow(node, destination);

        this.predecessors = Collections.<IASTNode> emptySet();
    }

    @Override
    public void visitASTAssignedGotoStmtNode(ASTAssignedGotoStmtNode node)
    {
        flowTo(node);

        String label = assignedLabels.get(node.getVariableName().getText());
        IASTNode destination = labels.get(label);
        if (destination != null) createFlow(node, destination);

        this.predecessors = Collections.<IASTNode> emptySet();
    }

    @Override
    public void visitASTAssociateConstructNode(ASTAssociateConstructNode node)
    {
        flowTo(node);
        traverseChildren(node);
    }

    @Override
    public void visitASTBlockConstructNode(ASTBlockConstructNode node)
    {
        flowTo(node);
        traverseChildren(node);
    }

    @Override
    public void visitASTComputedGotoStmtNode(ASTComputedGotoStmtNode node)
    {
        flowTo(node);

        for(ASTLblRefListNode lblnode : node.getLblRefList())
        {
            String label = lblnode.getLabel().getText();
            IASTNode destination = labels.get(label);
            if(destination != null) createFlow(node, destination);
        }
    }

    @Override
    public void visitASTCriticalConstructNode(ASTCriticalConstructNode node)
    {
        flowTo(node);
        traverseChildren(node);
    }

    @Override
    public void visitASTCycleStmtNode(ASTCycleStmtNode node)
    {
        flowTo(node);

        createFlow(node, node.findNearestAncestor(ASTProperLoopConstructNode.class));

        this.predecessors = Collections.<IASTNode> emptySet();
    }

    @Override
    public void visitASTElseConstructNode(ASTElseConstructNode node)
    {
        flowTo(node);

        if (node.getConditionalBody() != null)
            node.getConditionalBody().accept(this);
    }

    @Override
    public void visitASTElseIfConstructNode(ASTElseIfConstructNode node)
    {
        flowTo(node);

        Set<IASTNode> oldpredecessors = new HashSet<IASTNode>();
        Set<IASTNode> conditionalpreds = new HashSet<IASTNode>();

        oldpredecessors.addAll(this.predecessors);
        node.getConditionalBody().accept(this);
        conditionalpreds.addAll(this.predecessors);
        this.predecessors = oldpredecessors;
        if (node.getElseIfConstruct() != null)
        {
            node.getElseIfConstruct().accept(this);
            conditionalpreds.addAll(this.predecessors);
        }
        if (node.getElseConstruct() != null)
        {
            node.getElseConstruct().accept(this);
            conditionalpreds.addAll(this.predecessors);
        }

        this.predecessors = conditionalpreds;
    }

    @Override
    public void visitASTElseWhereConstructNode(ASTElseWhereConstructNode node)
    {
        flowTo(node);
        node.getWhereBodyConstructBlock().accept(this);
    }

    @Override
    public void visitASTEndFunctionStmtNode(ASTEndFunctionStmtNode node)
    {
        flowToExit(node);
    }

    @Override
    public void visitASTEndModuleStmtNode(ASTEndModuleStmtNode node)
    {
        flowToExit(node);
    }

    @Override
    public void visitASTEndProgramStmtNode(ASTEndProgramStmtNode node)
    {
        flowToExit(node);
    }

    @Override
    public void visitASTEndSubroutineStmtNode(ASTEndSubroutineStmtNode node)
    {
        flowToExit(node);
    }

    @Override
    public void visitASTExitStmtNode(ASTExitStmtNode node)
    {
        flowToExit(node);
    }

    @Override
    public void visitASTForallConstructNode(ASTForallConstructNode node)
    {
        flowTo(node);
        node.getForallBody().accept(this);
        flowTo(node);
    }

    @Override
    public void visitASTFunctionStmtNode(ASTFunctionStmtNode node)
    {
        this.predecessors = Collections.<IASTNode> emptySet();
        flowTo(node);

        traverseChildren(node);
    }

    @Override
    public void visitASTGotoStmtNode(ASTGotoStmtNode node)
    {
        flowTo(node);

        String label = node.getGotoLblRef().getLabel().getText();
        IASTNode destination = labels.get(label);
        if (destination != null) createFlow(node, destination);

        this.predecessors = Collections.<IASTNode> emptySet();
    }

    // @Override
    // public void visitASTCaseConstructNode(ASTCaseConstructNode node)
    // {
    // final HashSet<ASTCaseStmtNode> caseNodes = new HashSet<ASTCaseStmtNode>();
    // Set<IASTNode> casepredecessors = new HashSet<IASTNode>();
    // Set<IASTNode> newpredecessors = new HashSet<IASTNode>();
    //
    // flowTo(node);
    //
    // casepredecessors.add(node);
    //
    // node.getSelectCaseBody().accept(new ASTVisitor(){
    // @Override
    // public void visitASTCaseStmtNode(ASTCaseStmtNode node)
    // {
    // caseNodes.add(node);
    // }
    // });
    //
    // for(ASTCaseStmtNode casenode : caseNodes)
    // {
    // this.predecessors = casepredecessors;
    // casenode.accept(this);
    // newpredecessors.addAll(this.predecessors);
    // }
    //
    // this.predecessors = newpredecessors;
    // }
    //
    // @Override
    // public void visitASTCaseStmtNode(ASTCaseStmtNode node)
    // {
    // flowTo(node);
    //
    // node.getBody().accept(this);
    // }

    @Override
    public void visitASTIfConstructNode(ASTIfConstructNode node)
    {
        flowTo(node);

        Set<IASTNode> oldpredecessors = new HashSet<IASTNode>();
        Set<IASTNode> conditionalpreds = new HashSet<IASTNode>();

        oldpredecessors.addAll(this.predecessors);
        node.getConditionalBody().accept(this);
        conditionalpreds.addAll(this.predecessors);
        this.predecessors = oldpredecessors;
        if (node.getElseIfConstruct() != null)
        {
            node.getElseIfConstruct().accept(this);
            conditionalpreds.addAll(this.predecessors);
        }
        if (node.getElseConstruct() != null)
        {
            node.getElseConstruct().accept(this);
            conditionalpreds.addAll(this.predecessors);
        }

        this.predecessors = conditionalpreds;
    }

    @Override
    public void visitASTIfStmtNode(ASTIfStmtNode node)
    {
        flowTo(node);

        Set<IASTNode> predsBeforeStmt = this.predecessors; // == { node }
        if (node.getActionStmt() != null)
            node.getActionStmt().accept(this);
        Set<IASTNode> predsAfterStmt = this.predecessors;

        Set<IASTNode> newPredecessors = new HashSet<IASTNode>();
        newPredecessors.addAll(predsBeforeStmt);
        newPredecessors.addAll(predsAfterStmt);
        this.predecessors = newPredecessors;
    }

    @Override
    public void visitASTMainProgramNode(ASTMainProgramNode node)
    {
        this.predecessors = Collections.<IASTNode> emptySet();
        flowTo(node);
        traverseChildren(node);
    }

    @Override
    public void visitASTMaskedElseWhereConstructNode(ASTMaskedElseWhereConstructNode node)
    {
        flowTo(node);

        Set<IASTNode> oldpredecessors = new HashSet<IASTNode>();
        Set<IASTNode> newpredecessors = new HashSet<IASTNode>();

        oldpredecessors.addAll(this.predecessors);
        node.getWhereBodyConstructBlock().accept(this);
        newpredecessors.addAll(this.predecessors);
        this.predecessors = oldpredecessors;
        if (node.getMaskedElseWhereConstruct() != null)
        {
            node.getMaskedElseWhereConstruct().accept(this);
            newpredecessors.addAll(this.predecessors);
        }
        if (node.getElseWhereConstruct() != null)
        {
            node.getElseWhereConstruct().accept(this);
            newpredecessors.addAll(this.predecessors);
        }

        this.predecessors = newpredecessors;
    }

    @Override
    public void visitASTModuleStmtNode(ASTModuleStmtNode node)
    {
        this.predecessors = Collections.<IASTNode> emptySet();
        flowTo(node);
        traverseChildren(node);
    }

    @Override
    public void visitASTProperLoopConstructNode(ASTProperLoopConstructNode node)
    {
        HashSet<IASTNode> newpredecessors = new HashSet<IASTNode>();

        flowTo(node);
        node.getBody().accept(this);
        flowTo(node);

        newpredecessors.addAll(this.predecessors);

        if (exitsList.get(node) != null) for (ASTExitStmtNode exitnode : exitsList.get(node))
        {
            if (isPredecessor(node, exitnode)) newpredecessors.add(exitnode);
        }
        this.predecessors = newpredecessors;
    }

    protected boolean isPredecessor(ASTProperLoopConstructNode node, ASTExitStmtNode exitnode)
    {
        if (exitnode.getName() == null) return true;

        if (node.getLoopHeader().getName() == null) return false;

        if (exitnode.getName().getText().equals(node.getLoopHeader().getName().getText()))
            return true;
        else
            return false;
    }

    @Override
    public void visitASTReturnStmtNode(ASTReturnStmtNode node)
    {
        flowToExit(node);
    }

    @Override
    public void visitASTSelectTypeConstructNode(ASTSelectTypeConstructNode node)
    {
        HashSet<IASTNode> newpredecessors = new HashSet<IASTNode>();
        flowTo(node);

        Set<IASTNode> oldpredecessors = this.predecessors;
        for (IASTNode innerNode : node.getSelectTypeBody())
        {
            this.predecessors = oldpredecessors;
            innerNode.accept(this);
            newpredecessors.addAll(this.predecessors);
        }

        this.predecessors = newpredecessors;
    }

    @Override
    public void visitASTStopStmtNode(ASTStopStmtNode node)
    {
        flowToExit(node);
    }

    @Override
    public void visitASTSubroutineStmtNode(ASTSubroutineStmtNode node)
    {
        this.predecessors = Collections.<IASTNode> emptySet();
        flowTo(node);

        traverseChildren(node);
    }

    @Override
    public void visitASTWhereConstructNode(ASTWhereConstructNode node)
    {
        flowTo(node);

        Set<IASTNode> oldpredecessors = new HashSet<IASTNode>();
        Set<IASTNode> newpredecessors = new HashSet<IASTNode>();

        oldpredecessors.addAll(this.predecessors);
        node.getWhereBodyConstructBlock().accept(this);
        newpredecessors.addAll(this.predecessors);
        this.predecessors = oldpredecessors;
        if (node.getMaskedElseWhereConstruct() != null)
        {
            node.getMaskedElseWhereConstruct().accept(this);
            newpredecessors.addAll(this.predecessors);
        }
        if (node.getElseWhereConstruct() != null)
        {
            node.getElseWhereConstruct().accept(this);
            newpredecessors.addAll(this.predecessors);
        }

        this.predecessors = newpredecessors;
    }

    @Override
    public void visitASTWhereStmtNode(ASTWhereStmtNode node)
    {
        flowTo(node);
        node.getAssignmentStmt().accept(this);
    }
}
