/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *           (Jeff Overbey) Modified to use Reader rather than InputStream
 *******************************************************************************/
package org.eclipse.photran.internal.core.preprocessor.c;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.cdt.core.parser.CodeReader;

public class ReaderBasedCodeReader extends CodeReader 
{

	public ReaderBasedCodeReader(String filename, Reader stream)
			throws IOException 
	{
		super(filename, convertToCharBuffer(stream));
	}
	
	private static char[] convertToCharBuffer(Reader stream)
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
		try
        {
            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
		return aggregate.toString().toCharArray();
	}
}
