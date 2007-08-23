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
package org.eclipse.photran.core.vpg;

import java.io.Serializable;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.Token;

import bz.over.vpg.TokenRef;

/**
 * A reference to a token in a Fortran AST (used by the VPG).
 * 
 * @author Jeff Overbey
 */
public class PhotranTokenRef extends TokenRef<Token> implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public PhotranTokenRef(String filename, int offset, int length)
	{
		super(filename, offset, length);
		if (filename == null) throw new IllegalArgumentException();
	}
	
	public PhotranTokenRef(IFile file, int offset, int length)
	{
		this(PhotranVPG.getFilenameForIFile(file), offset, length);
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
}
