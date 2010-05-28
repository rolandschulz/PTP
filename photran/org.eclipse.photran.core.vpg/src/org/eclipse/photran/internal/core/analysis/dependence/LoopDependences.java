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
import org.eclipse.rephraserengine.core.analysis.dependence.Dependence;
import org.eclipse.rephraserengine.core.analysis.dependence.DependenceTestFailure;
import org.eclipse.rephraserengine.core.analysis.dependence.Direction;
import org.eclipse.rephraserengine.core.analysis.dependence.IDependenceTester;

/**
 * Computes a set of dependences in a perfect loop nest.
 * <p>
 * THIS IS PRELIMINARY AND EXPERIMENTAL.  IT IS NOT APPROPRIATE FOR PRODUCTION USE.
 *
 * @author Jeff Overbey
 */
public class LoopDependences
{
    private IDependenceTester[] testers;
    private PerfectLoopNest loopNest;
    private ArrayList<VariableReference> varRefs;
    private ArrayList<Dependence> dependences;

    private LoopDependences(ASTProperLoopConstructNode perfectLoopNest, IDependenceTester... testers)
    {
        this.testers = testers;
        this.loopNest = new PerfectLoopNest(perfectLoopNest);
        this.varRefs = new ArrayList<VariableReference>();
        this.dependences = new ArrayList<Dependence>();
    }

    /** Factory method */
    public static LoopDependences computeFor(
            ASTProperLoopConstructNode perfectLoopNest,
            IDependenceTester... testers)
        throws DependenceTestFailure
    {
        return new LoopDependences(perfectLoopNest, testers).collect();
    }

    private LoopDependences collect()
    {
        if (loopNest.containsDoWhileLoops())
            throw new DependenceTestFailure(Messages.LoopDependences_LoopNestContainsADoWhileLoop);

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
                throw new DependenceTestFailure(Messages.LoopDependences_LoopContainsAnAssignmentToADerivedTypeComponent);

            varRefs.addAll(VariableReference.fromRHS(node));
            varRefs.add(VariableReference.fromLHS(node));

            lastNodeSuccessfullyHandled = node;
        }

        @Override public void visitIExecutionPartConstruct(IExecutionPartConstruct node)
        {
            if (node != lastNodeSuccessfullyHandled)
                throw new DependenceTestFailure(Messages.bind(Messages.LoopDependences_LoopContains, node.getClass().getSimpleName()));
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
                    || loopNest.testForDependenceUsing(testers, from, to, new Direction[] {}))
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
        sb.append("Reads: "); //$NON-NLS-1$
        sb.append(getReads());
        sb.append("\nWrites: "); //$NON-NLS-1$
        sb.append(getWrites());
        sb.append("\n\nDependences:"); //$NON-NLS-1$
        for (Dependence dep : dependences)
            sb.append("\n    " + dep); //$NON-NLS-1$
        return sb.toString();
    }
}