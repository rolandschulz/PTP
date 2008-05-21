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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.photran.internal.core.parser.ASTArraySpecNode;
import org.eclipse.photran.internal.core.parser.ASTAssumedShapeSpecListNode;
import org.eclipse.photran.internal.core.parser.ASTAssumedSizeSpecNode;
import org.eclipse.photran.internal.core.parser.ASTDeferredShapeSpecListNode;
import org.eclipse.photran.internal.core.parser.ASTExplicitShapeSpecNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;

/**
 * An array specification: A "smart" representation of an ArraySpec AST node.
 * 
 * @author Jeff Overbey
 * 
 * @see Dimension
 */
public class ArraySpec implements Serializable
{
	private static final long serialVersionUID = 1L;
	
    protected List<Dimension> dimensions = new LinkedList<Dimension>();
    protected boolean assumedOrDeferredShape = false;

    // # R513 see 16
    // <ArraySpec> ::=
    //     <ExplicitShapeSpecList>
    //   | <AssumedSizeSpec>
    //   | <AssumedShapeSpecList>
    //   | <DeferredShapeSpecList>

    public ArraySpec(ASTArraySpecNode spec)
    {
        if (spec.getExplicitShapeSpecList() != null)
            parseExplicitShapeArray(spec.getExplicitShapeSpecList());
        else if (spec.getAssumedSizeSpec() != null)
            parseAssumedSizeArray(spec.getAssumedSizeSpec());
        else if (spec.getAssumedShapeSpecList() != null)
            parseAssumedShapeArray(spec.getAssumedShapeSpecList());
        else if (spec.getDeferredShapeSpecList() != null)
            parseDeferredShapeArray(spec.getDeferredShapeSpecList());
        else
            throw new Error("Unexpected entity in <ArraySpec>");
    }

    //    # R514
    //    <ExplicitShapeSpecList> ::=
    //        <ExplicitShapeSpec>
    //      | @:<ExplicitShapeSpecList> T_COMMA <ExplicitShapeSpec>

    private void parseExplicitShapeArray(IASTListNode<ASTExplicitShapeSpecNode> node)
    {
        for (int i = 0; i < node.size(); i++)
            dimensions.add(0, new Dimension(node.get(i)));
    }
    
    //    # R519
    //    <AssumedSizeSpec> ::=
    //        T_ASTERISK
    //      | <LowerBound> T_COLON T_ASTERISK
    //      | <ExplicitShapeSpecList> T_COMMA T_ASTERISK
    //      | <ExplicitShapeSpecList> T_COMMA <LowerBound> T_COLON T_ASTERISK

    private void parseAssumedSizeArray(ASTAssumedSizeSpecNode assumedSizeSpec)
    {
        assumedOrDeferredShape = true;
    }

    //    <AssumedShapeSpecList> ::=
    //        <LowerBound> T_COLON
    //      | <DeferredShapeSpecList> T_COMMA <LowerBound> T_COLON
    //      | @:<AssumedShapeSpecList> T_COMMA <AssumedShapeSpec>

    private void parseAssumedShapeArray(IASTListNode<ASTAssumedShapeSpecListNode> assumedShapeSpecList)
    {
        assumedOrDeferredShape = true;
    }

    //    # R518
    //    <DeferredShapeSpecList> ::=
    //        <DeferredShapeSpec>
    //      | @:<DeferredShapeSpecList> T_COMMA <DeferredShapeSpec>
    //
    //    <DeferredShapeSpec> ::= T_COLON

    private void parseDeferredShapeArray(IASTListNode<ASTDeferredShapeSpecListNode> deferredShapeSpecList)
    {
        assumedOrDeferredShape = true;
    }

    /** @return true iff this array has an explicit shape and each dimension has constant bounds */
    public boolean hasFixedConstantDimensions()
    {
        if (assumedOrDeferredShape) return false;
        
        for (Dimension d : dimensions)
            if (!d.hasConstantBounds())
                return false;
        
        return true;
    }
    
    /** @return the dimensions of this array */
    public Iterable<Dimension> getDimensions()
    {
        return dimensions;
    }
    
    /** @return the rank of this array */
    public int getRank()
    {
        return dimensions.size();
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for (int i = 0; i < dimensions.size(); i++)
        {
            if (i > 0) sb.append(", ");
            sb.append(dimensions.get(i).toString());
        }
        sb.append(")");
        return sb.toString();
    }
    
    @Override public boolean equals(Object other)
    {
        if (!(other instanceof ArraySpec)) return false;
        
        ArraySpec o = (ArraySpec)other;
        return this.dimensions.equals(o.dimensions) && this.assumedOrDeferredShape == o.assumedOrDeferredShape;
    }
    
    @Override public int hashCode()
    {
        return dimensions.hashCode() + (assumedOrDeferredShape ? 1 : 0);
    }
}
