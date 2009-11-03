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

import org.eclipse.photran.internal.core.analysis.dependence.VariableReference.LinearFunction;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.parser.ASTIntConstNode;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * A utility class describing a perfect nest of DO-loops.
 * 
 * @author Jeff Overbey
 */
public /*was package-private*/ class PerfectLoopNest
{
    /** The loops in the nest, from outermost to innermost */
    private ArrayList<ASTProperLoopConstructNode> loopNest;
    
    private int n;
    private int[] L;
    private int[] U;
    
    public PerfectLoopNest(ASTProperLoopConstructNode perfectLoopNest)
    {
        constructLoopNest(perfectLoopNest);
        n = loopNest.size();
        setBounds();
    }

    private void setBounds()
    {
        L = new int[n]; 
        U = new int[n]; 
        
        for (int i = 0; i < n; i++)
        {
            L[i] = lb(loopNest.get(i));
            U[i] = ub(loopNest.get(i));
        }
    }

    private void constructLoopNest(ASTProperLoopConstructNode perfectLoopNest)
    {
        this.loopNest = new ArrayList<ASTProperLoopConstructNode>();
        
        ASTProperLoopConstructNode thisLoop = perfectLoopNest;
        do
        {
            loopNest.add(thisLoop);
            
            if (thisLoop.getBody().size() == 1
                && thisLoop.getBody().get(0) instanceof ASTProperLoopConstructNode)
                thisLoop = (ASTProperLoopConstructNode)thisLoop.getBody().get(0);
            else
                thisLoop = null;
        }
        while (thisLoop != null);
    }

    private int lb(ASTProperLoopConstructNode loop)
    {
        return intConstValueOr(Integer.MIN_VALUE, loop.getLoopHeader().getLoopControl().getLb());
    }

    private int ub(ASTProperLoopConstructNode loop)
    {
        return intConstValueOr(Integer.MAX_VALUE, loop.getLoopHeader().getLoopControl().getUb());
    }

    private int intConstValueOr(int defaultValue, IExpr expr)
    {
        if (expr instanceof ASTIntConstNode)
            return Integer.parseInt(((ASTIntConstNode)expr).getIntConst().getText());
        else
            return defaultValue;
    }

    public ASTProperLoopConstructNode outermostLoop()
    {
        return loopNest.get(0);
    }

    public ASTProperLoopConstructNode innermostLoop()
    {
        return loopNest.get(loopNest.size()-1);
    }
    
    public IASTListNode<IExecutionPartConstruct> getBody()
    {
        return innermostLoop().getBody();
    }
    
    public int getNumberOfLoops()
    {
        return loopNest.size();
    }
    
    public boolean containsDoWhileLoops()
    {
        for (int i = 0; i < loopNest.size(); i++)
            if (loopNest.get(i).isDoWhileLoop())
                return true;
        
        return false;
    }
    
    /** @return the name of the indexing variable for the given loop (1..n) */
    public String getIndexVariable(int i)
    {
        if (i <= 0 || i > getNumberOfLoops())
            throw new IllegalArgumentException("Cannot retrieve index variable for loop " + i +
                                               " in a " + getNumberOfLoops() + "-loop nest");
        
        ASTProperLoopConstructNode targetLoop = loopNest.get(i-1);
        if (targetLoop.isDoWhileLoop())
            return null;
        else
            return PhotranVPG.canonicalizeIdentifier(targetLoop.getIndexVariable().getText());
    }

    /*
     * TODO: L and U use min and max integer values, which may not be correct; also,
     *       this assumes that the loop is normalized, or at least has a positive step
     */
    public boolean testForDependenceUsing(IDependenceTester tester,
        VariableReference from,
        VariableReference to,
        Direction[] direction)
    {
        int[] a = coefficients(from);
        int[] b = coefficients(to);
        return tester.test(n, L, U, a, b, direction);
    }

    private int[] coefficients(VariableReference var)
    {
        if (var.indices.length > 1)
            throw new DependenceTestFailure("Only single subscripts are currently supported");

        LinearFunction fn = var.indices[0];
        
        int targetLoop = loopWithIndexVariable(fn.variable);
        if (targetLoop == 0)
            throw new DependenceTestFailure("Linear function of non-index variable");

        int[] result = new int[n+1];
        result[0] = fn.y_intercept;
        result[targetLoop] = fn.slope;
        return result;
    }

    /** @return the loop (1..n) with the given variable as its indexing variable */
    private int loopWithIndexVariable(String variable)
    {
        for (int i = 1; i <= n; i++)
            if (variable.equals(getIndexVariable(i)))
                return i;
        
        return 0;
    }
}