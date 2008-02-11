/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.types;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import org.eclipse.photran.internal.core.parser.ASTExplicitShapeSpecNode;
import org.eclipse.photran.internal.core.parser.ASTExpressionNode;
import org.eclipse.photran.internal.core.parser.Parser.CSTNode;

/**
 * Contains the <b>lower and upper bounds</b> of one dimension of an explicit-shape array.
 * 
 * @author Jeff Overbey
 * 
 * @see ArraySpec
 */
public class Dimension implements Serializable
{
	private static final long serialVersionUID = 1L;
	
    private String lboundAsString, uboundAsString;

    //    <ExplicitShapeSpec> ::=
    //        <LowerBound> T_COLON <UpperBound>
    //      | <UpperBound>

    public Dimension(ASTExplicitShapeSpecNode node)
    {
        ASTExpressionNode lbound = node.getLb();
        ASTExpressionNode ubound = node.getUb();
        
        lboundAsString = lbound == null ? null : getSourceCodeFromASTNode(lbound);
        uboundAsString = ubound == null ? null : getSourceCodeFromASTNode(ubound);
    }
   
    /** @return true iff both the upper bound and lower bound are integer constants */
    public boolean hasConstantBounds()
    {
        return uboundAsString != null && isLiteralInteger(lboundAsString) && isLiteralInteger(uboundAsString);
    }

    private boolean isLiteralInteger(String string)
    {
        if (string == null) return true;
        
        string = string.trim();
        for (int i = 0, len = string.length(); i < len; i++)
            if (!Character.isDigit(string.charAt(i)))
                return false;
        return true;
    }

    /** @return the constant lower bound (assumes <code>hasConstantBounds</code> returned true) */
    public int getConstantLowerBound()
    {
        return lboundAsString == null ? 1 : Integer.parseInt(lboundAsString.trim());
    }
    
    /** @return the constant upper bound (assumes <code>hasConstantBounds</code> returned true) */
    public int getConstantUpperBound()
    {
        return Integer.parseInt(uboundAsString.trim());
    }
    
    // Copied from SourcePrinter; refactor somehow
    private static String getSourceCodeFromASTNode(CSTNode node)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        node.printOn(new PrintStream(out), null);
        return out.toString();
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        if (lboundAsString != null)
        {
            sb.append(lboundAsString);
            sb.append(":");
        }
        sb.append(uboundAsString);
        return sb.toString();
    }
    
    @Override public boolean equals(Object other)
    {
        if (!(other instanceof Dimension)) return false;
        
        Dimension o = (Dimension)other;
        return equals(this.lboundAsString, o.lboundAsString) && equals(this.uboundAsString, o.uboundAsString);
    }
    
    private boolean equals(Object a, Object b)
    {
        if (a == null && b == null)
            return true;
        else if (a != null && b != null)
            return a.equals(b);
        else
            return false;
    }

    @Override public int hashCode()
    {
        return hashCode(lboundAsString) + hashCode(uboundAsString);
    }

    private int hashCode(Object o)
    {
        return o == null ? 0 : o.hashCode();
    }
}
