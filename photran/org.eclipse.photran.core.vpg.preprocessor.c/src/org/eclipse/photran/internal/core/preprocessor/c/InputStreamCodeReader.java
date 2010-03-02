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
package org.eclipse.photran.internal.core.preprocessor.c;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.parser.CodeReader;

public class InputStreamCodeReader extends CodeReader 
{

	public InputStreamCodeReader(String filename, InputStream stream)
			throws IOException 
	{
		super(filename, convertToCharBuffer(stream));
		
		// TODO Auto-generated constructor stub
	}
	
	public InputStreamCodeReader(String filename, String charSet, InputStream stream)
			throws IOException 
	{
		//We are dropping out the charSet variable because it is used internally by 
		// CodeReader to read from the FileStream. Since we want to read from the 
		// InputStream, and we do it byte-wise, we don't really care for encoding
		super(filename, convertToCharBuffer(stream));
		// TODO Auto-generated constructor stub
	}
	
	private static char[] convertToCharBuffer(InputStream stream)
	{
		int b = 0;
		StringBuffer aggregate = new StringBuffer();
		while(b != -1)
		{
			try 
			{
				b = stream.read();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				b = -1;
				break;
			}
			if(b != -1)
			{
				aggregate.append((char)b);
			}
		}
		return aggregate.toString().toCharArray();
	}

}
