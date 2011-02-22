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
package org.eclipse.photran.internal.core.vpg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.rephraserengine.core.util.OffsetLength;
import org.eclipse.rephraserengine.core.vpg.NodeRef;
import org.eclipse.rephraserengine.core.vpg.VPG;

/**
 * A reference to a token in a Fortran AST (used by the VPG).
 * 
 * @author Jeff Overbey
 * 
 * @see PhotranVPG
 */
public class PhotranTokenRef extends NodeRef<Token> implements IPhotranSerializable //, Comparable<PhotranTokenRef>
{
	private static final long serialVersionUID = 1L;
	
    // ***WARNING*** If any fields change, the serialization methods (below) must also change!

	public PhotranTokenRef(String filename, int offset, int length)
	{
		super(filename, offset, length);
		if (filename == null) throw new IllegalArgumentException();
	}
	
	public PhotranTokenRef(IFile file, int offset, int length)
	{
		this(file == null ? "" : PhotranVPG.getFilenameForIFile(file), offset, length); //$NON-NLS-1$
	}
	
	public PhotranTokenRef(PhotranTokenRef copyFrom)
	{
		super(copyFrom);
	}

    public PhotranTokenRef(String filename, OffsetLength ol)
    {
        this(filename, ol.getOffset(), ol.getLength());
    }

    public IFile getFile()
	{
		return PhotranVPG.getIFileForFilename(getFilename());
	}
    
    @Override public Token getASTNode()
    {
        return findToken();
    }
	
	public Token findToken()
	{
		try
		{
	        IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(getFilename());
	        if (ast == null)
	            return null;
	        else
	            return ast.findTokenByStreamOffsetLength(getOffset(), getLength());
		}
		catch (Exception e)
		{
			throw new Error(e);
		}
	}
	
	public Token findTokenOrReturnNull()
	{
		return findToken();
	}
	
	public String getText()
	{
		return findToken().getText();
	}

    public int compareTo(PhotranTokenRef that)
    {
        // Compare lexicographically as an ordered triple (filename, offset, length)
        int result = this.getFilename().compareTo(that.getFilename());
        if (result == 0) result = Integer.valueOf(this.getOffset()).compareTo(that.getOffset());
        if (result == 0) result = Integer.valueOf(this.getLength()).compareTo(that.getLength());
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////
    // AST Mapping
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns a list of the tokens pointed at by an edge extending from the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES} to process all edges, regardless
     *                 of type
     * @since 3.0
     */
    @SuppressWarnings("unchecked")
    @Override public Iterable<PhotranTokenRef> followOutgoing(int edgeType)
    {
        return super.followOutgoing(edgeType);
    }

    public Iterable<PhotranTokenRef> followOutgoing(EdgeType edgeType)
    {
        return super.followOutgoing(edgeType.ordinal());
    }

    /**
     * Returns a list of the tokens which have an edges pointing at the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES} to process all edges, regardless
     *                 of type
     * @since 3.0
     */
    @SuppressWarnings("unchecked")
    @Override public Iterable<PhotranTokenRef> followIncoming(int edgeType)
    {
        return super.followIncoming(edgeType);
    }

    public Iterable<PhotranTokenRef> followIncoming(EdgeType edgeType)
    {
        return super.followIncoming(edgeType.ordinal());
    }
    
    public <R extends Serializable> R getAnnotation(AnnotationType annotationType)
    {
        return super.getAnnotation(annotationType.ordinal());
    }

    @SuppressWarnings("unchecked")
    @Override protected PhotranVPG getVPG()
    {
        return PhotranVPG.getInstance();
    }

    ////////////////////////////////////////////////////////////////////////////////
    // IPhotranSerializable Implementation
    ////////////////////////////////////////////////////////////////////////////////
    
    public static PhotranTokenRef readFrom(InputStream in) throws IOException
    {
        String filename = PhotranVPGSerializer.deserialize(in);
        int offset = PhotranVPGSerializer.deserialize(in);
        int length = PhotranVPGSerializer.deserialize(in);
        return new PhotranTokenRef(filename, offset, length);
    }
    
    public void writeTo(OutputStream out) throws IOException
    {
        PhotranVPGSerializer.serialize(getFilename(), out);
        PhotranVPGSerializer.serialize(getOffset(), out);
        PhotranVPGSerializer.serialize(getLength(), out);
    }
    
    public char getSerializationCode()
    {
        return PhotranVPGSerializer.CLASS_TOKENREF;
    }
}
