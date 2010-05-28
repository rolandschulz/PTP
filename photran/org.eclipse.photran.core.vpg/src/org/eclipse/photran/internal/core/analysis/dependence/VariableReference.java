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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.photran.internal.core.analysis.loops.GenericASTVisitorWithLoops;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTVarOrFnRefNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.analysis.dependence.IVariableReference;

/**
 * Describes a reference to a scalar variable or a reference into an array variable where
 * each subscript is a linear function.
 * <p>
 * This class is intended to describe scalar variable references as well as array references
 * such as A(3), A(I), A(3+I), A(2*I+1), A(3, 4), and A(I, J, K) where each subscript is
 * a function of the form <i>m*x+b</i> where <i>m</i> and <i>b</i> are integer constants
 * and <i>x</i> is a scalar variable.
  * <p>
 * THIS IS PRELIMINARY AND EXPERIMENTAL.  IT IS NOT APPROPRIATE FOR PRODUCTION USE.
*
 * @author Jeff Overbey
 */
public /*was package-private*/ class VariableReference implements IVariableReference
{
    /**
     * Represents a linear function <i>m*x+b</i>, where <i>m</i> and <i>b</i> are integer
     * constants and <i>x</i> is a scalar variable.
     *
     * @author Jeff Overbey
     */
    public static class LinearFunction
    {
        /** The value of <i>m</i>, where this object represents the linear function <i>m*x+b</i> */
        public final int slope;

        /**
         * The (canonicalized) name of the variable <i>x</i>,
         * where this object represents the linear function <i>m*x+b</i>
         */
        public final String variable;

        /** The value of <i>b</i>, where this object represents the linear function <i>m*x+b</i> */
        public final int y_intercept;

        private LinearFunction(int slope, String variable, int y_intercept)
        {
            super();
            this.slope = slope;
            this.variable = variable;
            this.y_intercept = y_intercept;
        }

        /** Factory method */
        public static LinearFunction from(String slope, String variable, String y_intercept)
        {
            try
            {
                int    m = slope == null || slope.equals("") ? 1 : Integer.parseInt(slope); //$NON-NLS-1$
                String x = variable == null ? null : PhotranVPG.canonicalizeIdentifier(variable);
                int    b = y_intercept == null || y_intercept.equals("") ? 0 : Integer.parseInt(y_intercept); //$NON-NLS-1$
                return new LinearFunction(
                    m,
                    x,
                    b);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }

        /** Array factory method */
        public static LinearFunction[] fromList(IASTListNode<? extends IASTNode> list)
        {
            if (list == null) return null;

            LinearFunction[] result = new LinearFunction[list.size()];
            for (int i = 0; i < list.size(); i++)
            {
                LinearFunction thisIndex = fromNode(list.get(i));
                if (thisIndex == null) return null;
                result[i] = thisIndex;
            }
            return result;
        }

        /*
         * Regex notes:
         *     \S matches any non-whitespace character
         *     \d matches [0-9]
         *     \w matches [A-Za-z_0-9]
         *     Parentheses define a capturing group (capturing groups are numbered below)
         */
        //                                            123                        4                      5
        //                                              ===m===         *        ==x==         +        ==b==
        private static Pattern mxb = Pattern.compile("((-?(\\d+)[ \t]*\\*[ \t]*)?(\\w+)[ \t]*\\+[ \t]*)?(\\d+)"); //$NON-NLS-1$
        //                                             12                        3
        //                                              ===m===         *        ==x==
        private static Pattern mx  = Pattern.compile( "((-?\\d+)[ \t]*\\*[ \t]*)?(\\w+)"); //$NON-NLS-1$
        //                                            1                    23                        4
        //                                            ==b==         +       ===m===         *        ==x==
        private static Pattern bmx = Pattern.compile("(\\d+)[ \t]*\\+[ \t]*((-?\\d+)[ \t]*\\*[ \t]*)?(\\w+)"); //$NON-NLS-1$
        //                                                    1     2               3
        //                                            -       ==x==          +      ==b==
        private static Pattern nxb = Pattern.compile("-[ \\t]*(\\w+)([ \t]*\\+[ \t]*(\\d+))?"); //$NON-NLS-1$
        //                                            1                           2
        //                                            ==b==         +      -      ==x==
        private static Pattern bnx = Pattern.compile("(\\d+)[ \t]*\\+[ \t]*-[ \t]*(\\w+)"); //$NON-NLS-1$

        /** Factory method */
        public static LinearFunction fromNode(IASTNode node)
        {
            String str = node.toString().trim();
            if (str.startsWith(",")) str = str.substring(1); //$NON-NLS-1$
            str = str.trim();

            Matcher m;

            m = mxb.matcher(str);
            if (m.matches()) return LinearFunction.from(m.group(3), m.group(4), m.group(5));

            m = mx.matcher(str);
            if (m.matches()) return LinearFunction.from(m.group(2), m.group(3), null);

            m = bmx.matcher(str);
            if (m.matches()) return LinearFunction.from(m.group(3), m.group(4), m.group(1));

            m = nxb.matcher(str);
            if (m.matches()) return LinearFunction.from("-1", m.group(1), m.group(3)); //$NON-NLS-1$

            m = bnx.matcher(str);
            if (m.matches()) return LinearFunction.from("-1", m.group(2), m.group(1)); //$NON-NLS-1$

            return null;
        }

        @Override public String toString()
        {
            if (variable == null)
                return Integer.toString(y_intercept);
            else
                return slope + "*" + variable + "+" + y_intercept; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /** The AST node representing the variable access */
    public final IASTNode node;

    /** The (canonicalized) name of the variable being referenced */
    public final String variable;

    /** The indices into that variable, or <code>null</code> if the variable is scalar or the indices are
     *  not linear functions of the expected form */
    public final LinearFunction[] indices;

    /** True iff this reference is a write; false if it is a read */
    protected final boolean isWrite;

    private VariableReference(IASTNode node, String array, LinearFunction[] indices, boolean isWrite)
    {
        this.node = node;
        this.variable = array;
        this.indices = indices;
        this.isWrite = isWrite;
    }

    /** Factory method */
    public static VariableReference fromLHS(ASTAssignmentStmtNode node)
    {
        String lhsVar = PhotranVPG.canonicalizeIdentifier(node.getLhsVariable().getName().getText());

        LinearFunction[] lhsIndices = null;
        if (node.getLhsExprList() != null)
            lhsIndices = LinearFunction.fromList(node.getLhsExprList());
        else if (node.getLhsNameList() != null)
            lhsIndices = LinearFunction.fromList(node.getLhsNameList());

        return new VariableReference(node, lhsVar, lhsIndices, true);
    }

    /** Factory method */
    public static Collection<VariableReference> fromRHS(ASTAssignmentStmtNode node)
    {
        return VariableReference.fromExpr(node.getRhs(), false);
    }

    /** Factory method */
    public static Collection<VariableReference> fromExpr(IExpr expr, final boolean isWrite)
    {
        final ArrayList<VariableReference> result = new ArrayList<VariableReference>();
        expr.accept(new GenericASTVisitorWithLoops()
        {
            @Override
            public void visitASTVarOrFnRefNode(ASTVarOrFnRefNode node)
            {
                if (node.getName() == null
                    || node.getFunctionArgList() != null
                    || node.getDerivedTypeComponentRef() != null
                    || node.getComponentSectionSubscriptList() != null
                    || node.getSubstringRange() != null)
                {
                    return;
                }

                String name = PhotranVPG.canonicalizeIdentifier(node.getName().getName().getText());
                LinearFunction[] indices = LinearFunction.fromList(node.getPrimarySectionSubscriptList());
                result.add(new VariableReference(node, name, indices, isWrite));
            }
        });
        return result;
    }

    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(variable);
        if (indices != null)
        {
            sb.append("("); //$NON-NLS-1$
            for (int i = 0; i < indices.length; i++)
                sb.append((i > 0 ? ", " : "") + indices[i]); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(")"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    @Override public boolean equals(Object o)
    {
        if (!this.getClass().equals(o.getClass())) return false;

        return this.toString().equals(o.toString()); // Cheat -- for testing only
    }

    @Override public int hashCode()
    {
        return toString().hashCode();
    }

    /** @return true iff this variable reference represents a read, rather than a write, of the variable */
    public boolean isRead()
    {
        return !isWrite;
    }

    /** @return true iff this variable reference represents a write, rather than a read, of the variable */
    public boolean isWrite()
    {
        return isWrite;
    }

    /** @return true if this variable is a scalar or an array access with the subscripts not of the expected form */
    public boolean isScalar()
    {
        return indices == null;
    }
}