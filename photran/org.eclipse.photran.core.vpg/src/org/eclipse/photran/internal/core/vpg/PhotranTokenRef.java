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

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.rephraserengine.core.vpg.TokenRef;

/**
 * A reference to a token in a Fortran AST (used by the VPG).
 * 
 * @author Jeff Overbey
 */
public class PhotranTokenRef extends TokenRef<Token> implements IPhotranSerializable, Comparable<PhotranTokenRef>
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
		this(file == null ? "" : PhotranVPG.getFilenameForIFile(file), offset, length);
	}
	
	public PhotranTokenRef(PhotranTokenRef copyFrom)
	{
		super(copyFrom);
	}

	public IFile getFile()
	{
		return PhotranVPG.getIFileForFilename(getFilename());
	}
	
	public Token findToken()
	{
		try
		{
			return PhotranVPG.getInstance().findToken(this);
		}
		catch (Exception e)
		{
			throw new Error(e);
		}
	}
	
	public Token findTokenOrReturnNull()
	{
		return PhotranVPG.getInstance().findToken(this);
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
