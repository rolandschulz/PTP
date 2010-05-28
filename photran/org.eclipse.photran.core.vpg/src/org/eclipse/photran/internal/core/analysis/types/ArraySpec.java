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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.photran.internal.core.parser.ASTArraySpecNode;
import org.eclipse.photran.internal.core.parser.ASTAssumedShapeSpecListNode;
import org.eclipse.photran.internal.core.parser.ASTAssumedSizeSpecNode;
import org.eclipse.photran.internal.core.parser.ASTDeferredShapeSpecListNode;
import org.eclipse.photran.internal.core.parser.ASTExplicitShapeSpecNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.vpg.IPhotranSerializable;
import org.eclipse.photran.internal.core.vpg.PhotranVPGSerializer;

/**
 * A smarter, persistable representation of an {@link ASTArraySpecNode}, i.e., an <i>ArraySpec</i>
 * in the Fortran grammar.
 * 
 * @author Jeff Overbey
 * 
 * @see Dimension
 */
public class ArraySpec implements IPhotranSerializable
{
	private static final long serialVersionUID = 1L;
	
    // ***WARNING*** If any fields change, the serialization methods (below) must also change!
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
            throw new Error("Unexpected entity in <ArraySpec>"); //$NON-NLS-1$
    }

    //    # R514
    //    <ExplicitShapeSpecList> ::=
    //        <ExplicitShapeSpec>
    //      | @:<ExplicitShapeSpecList> T_COMMA <ExplicitShapeSpec>

    private void parseExplicitShapeArray(IASTListNode<ASTExplicitShapeSpecNode> node)
    {
        for (int i = 0; i < node.size(); i++)
            dimensions.add(new Dimension(node.get(i)));
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

    @Override public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("("); //$NON-NLS-1$
        for (int i = 0; i < dimensions.size(); i++)
        {
            if (i > 0) sb.append(","); //$NON-NLS-1$
            sb.append(dimensions.get(i).toString());
        }
        sb.append(")"); //$NON-NLS-1$
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

    ////////////////////////////////////////////////////////////////////////////////
    // IPhotranSerializable Implementation
    ////////////////////////////////////////////////////////////////////////////////

//  protected List<Dimension> dimensions = new LinkedList<Dimension>();
//  protected boolean assumedOrDeferredShape = false;

    private ArraySpec(List<Dimension> dimensions, boolean assumedOrDeferredShape)
    {
        this.dimensions = dimensions;
        this.assumedOrDeferredShape = assumedOrDeferredShape;
    }
    
    public void writeTo(OutputStream out) throws IOException
    {
        PhotranVPGSerializer.serialize(dimensions.size(), out);
        
        for (Dimension dim : dimensions)
            PhotranVPGSerializer.serialize(dim, out);
        
        PhotranVPGSerializer.serialize(assumedOrDeferredShape, out);
    }
    
    public static ArraySpec readFrom(InputStream in) throws IOException
    {
        int numDimensions = PhotranVPGSerializer.deserialize(in);
        
        ArrayList<Dimension> dimensions = new ArrayList<Dimension>(numDimensions);
        for (int i = 0; i < numDimensions; i++)
            dimensions.add((Dimension)PhotranVPGSerializer.deserialize(in));
        
        boolean assumedOrDeferredShape = PhotranVPGSerializer.deserialize(in);
        
        return new ArraySpec(dimensions, assumedOrDeferredShape);
    }
    
    public char getSerializationCode()
    {
        return PhotranVPGSerializer.CLASS_ARRAYSPEC;
    }
}
