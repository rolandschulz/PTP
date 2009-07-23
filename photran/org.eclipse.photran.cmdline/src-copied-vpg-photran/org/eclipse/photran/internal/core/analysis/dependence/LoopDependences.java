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
package org.eclipse.photran.internal.core.analysis.dependence;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.GenericASTVisitorWithLoops;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;

/**
 * Computes a set of dependences in a perfect loop nest.
 * <p>
 * THIS IS PRELIMINARY AND EXPERIMENTAL.  IT IS NOT APPROPRIATE FOR PRODUCTION USE.
 * 
 * @author Jeff Overbey
 */
public class LoopDependences
{
    private IDependenceTester tester;
    private PerfectLoopNest loopNest;
    private ArrayList<VariableReference> varRefs;
    private ArrayList<Dependence> dependences;
    
    private LoopDependences(ASTProperLoopConstructNode perfectLoopNest, IDependenceTester tester)
    {
        this.tester = tester;
        this.loopNest = new PerfectLoopNest(perfectLoopNest);
        this.varRefs = new ArrayList<VariableReference>();
        this.dependences = new ArrayList<Dependence>();
    }
    
    /** Factory method */
    public static LoopDependences computeFor(ASTProperLoopConstructNode perfectLoopNest, IDependenceTester tester) throws DependenceTestFailure
    {
        return new LoopDependences(perfectLoopNest, tester).collect();
    }
    
    private LoopDependences collect()
    {
        if (loopNest.containsDoWhileLoops())
            throw new DependenceTestFailure("The loop nest contains a do-while loop");
        
        collectReadsAndWrites();
        collectDependences();
        
        return this;
    }

    private void collectReadsAndWrites()
    {
        loopNest.getBody().accept(new Visitor());
    }

    private class Visitor extends GenericASTVisitorWithLoops
    {
        private IExecutionPartConstruct lastNodeSuccessfullyHandled = null;
        
        @Override public void visitASTAssignmentStmtNode(ASTAssignmentStmtNode node)
        {
            if (node.getDerivedTypeComponentRef() != null)
                throw new DependenceTestFailure("The loop contains an assignment to a derived type component");
            
            varRefs.addAll(VariableReference.fromRHS(node));
            varRefs.add(VariableReference.fromLHS(node));
            
            lastNodeSuccessfullyHandled = node;
        }
        
        @Override public void visitIExecutionPartConstruct(IExecutionPartConstruct node)
        {
            if (node != lastNodeSuccessfullyHandled)
                throw new DependenceTestFailure("The loop contains an " + node.getClass().getSimpleName());
        }
    }

    public Collection<VariableReference> getReads()
    {
        ArrayList<VariableReference> result = new ArrayList<VariableReference>(varRefs.size());
        for (VariableReference ref : varRefs)
            if (!ref.isWrite)
                result.add(ref);
        
        return result;
    }

    public Collection<VariableReference> getWrites()
    {
        ArrayList<VariableReference> result = new ArrayList<VariableReference>(varRefs.size());
        for (VariableReference ref : varRefs)
            if (ref.isWrite)
                result.add(ref);
        
        return result;
    }

    private void collectDependences()
    {
        for (int i = 0; i < varRefs.size(); i++)
        {
            VariableReference from = varRefs.get(i);
            for (int j = 0; j < varRefs.size(); j++)
            {
                if (j != i)
                {
                    VariableReference to = varRefs.get(j);
                    testForDependence(from, to);
                }
            }
        }
    }

    private void testForDependence(VariableReference from, VariableReference to)
    {
        if (from.variable.equals(to.variable))
        {
            if (from.isWrite || to.isWrite)
            {
                if (from.isScalar()
                    || to.isScalar()
                    || loopNest.testForDependenceUsing(tester, from, to, new Direction[] {}))
                {
                    markDependence(from, to);
                }
            }
        }
    }

    private void markDependence(VariableReference from, VariableReference to)
    {
        dependences.add(new Dependence(from, to));
    }

    /** @return all of the dependences in the given loop */
    public Collection<Dependence> getDependences()
    {
        return dependences;
    }

    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Reads: ");
        sb.append(getReads());
        sb.append("\nWrites: ");
        sb.append(getWrites());
        sb.append("\n\nDependences:");
        for (Dependence dep : dependences)
            sb.append("\n    " + dep);
        return sb.toString();
    }
}