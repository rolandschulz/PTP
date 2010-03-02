/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.preprocessor.c.CppHelper;
import org.eclipse.photran.internal.core.preprocessor.c.IToken;
import org.eclipse.photran.internal.core.preprocessor.c.OffsetLimitReachedException;

/**
 * An InputStream to read tokens obtained from the CPP. This class will
 * also make a ProducerMap when constructed that can be used to find
 * directives and macros.
 * 
 * @author Matthew Michelotti
 */
public class CPreprocessingInputStream extends InputStream
{
	/**ProducerMap constructed from the CPP tokens*/
	private ProducerMap producerMap;
	
	private IncludeMap includeMap;

	/**current CPP token being read*/
	private IToken curToken;
	/**full image of curToken*/
	private String curTokenImage;
	/**the offset in curTokenImage to be read next*/
	private int curTokenOffset = 0;
	
	/**
	 * Basic constructor. Parses the whole file in the constructor
	 * and turns the result into CPP tokens. A ProducerMap is made
	 * from these tokens. The tokens are remembered, so that they
	 * can be read as an input stream.
	 * @param filename - name of file that inputStream corresponds to
	 * @param inputStream - InputStream to read file data from
	 * @throws IOException
	 */
	public CPreprocessingInputStream(IFile file, String filename, InputStream inputStream) throws IOException
	{
		try
		{
			CppHelper cpp = new CppHelper(file, filename, inputStream);
			curToken = cpp.getRemainingTokens();
			curTokenImage = CppHelper.getFullImage(curToken);
			producerMap = new ProducerMap(curToken);
			includeMap = new IncludeMap(curToken, file);
		}catch(OffsetLimitReachedException e) {throw new Error(e);}
	}
	
	@Override public int read() throws IOException
	{
		if(curToken == null) return -1;
		while(curTokenOffset >= curTokenImage.length()) {
			curToken = curToken.getNext();
			if(curToken == null) return -1;
			curTokenImage = CppHelper.getFullImage(curToken);
			curTokenOffset = 0;
		}
		char result = curTokenImage.charAt(curTokenOffset);
		curTokenOffset++;
		
		return result;
	}
	
	/**@return the ProducerMap object made at construction*/
	public ProducerMap getProducerMap() {
		return producerMap;
	}
	
	public IncludeMap getIncludeMap() {
		return includeMap;
	}
}
