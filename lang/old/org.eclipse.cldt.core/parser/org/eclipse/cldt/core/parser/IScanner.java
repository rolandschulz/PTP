/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.core.parser;

import java.util.Map;

import org.eclipse.cldt.core.parser.ast.IASTFactory;
import org.eclipse.cldt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cldt.internal.core.parser.scanner2.ILocationResolver;

/**
 * @author jcamelon
 *
 */
public interface IScanner  {
	
	public static final int tPOUNDPOUND = -6;
	public static final int tPOUND      = -7;
	
	public void setOffsetBoundary( int offset );
	public void setContentAssistMode( int offset );
	public void setASTFactory( IASTFactory f );
	
	public void addDefinition(char[] key, char[] value); 
	public Map 				getDefinitions();
	public String[] getIncludePaths();

	public IToken nextToken() throws EndOfFileException;
			
	public int  getCount();
	public boolean isOnTopContext();
	public CharArrayObjectMap getRealDefinitions();
	public void cancel();
	public char[] getMainFilename();
	
	public ILocationResolver getLocationResolver();
}
