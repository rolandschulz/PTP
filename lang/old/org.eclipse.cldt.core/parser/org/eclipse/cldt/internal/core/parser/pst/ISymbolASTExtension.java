/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cldt.internal.core.parser.pst;


import java.util.Iterator;

import org.eclipse.cldt.internal.core.parser.ast.complete.ASTNode;
import org.eclipse.cldt.internal.core.parser.ast.complete.ASTSymbol;

/**
 * @author jcamelon
 *
 */
public interface ISymbolASTExtension extends ISymbolOwner
{
	public class ExtensionException extends Exception
	{
	}
	
	
	public ASTNode       getPrimaryDeclaration();
	public IExtensibleSymbol getExtensibleSymbol();
	public Iterator        getAllDefinitions();
	public void            addDefinition( ASTSymbol definition ) throws ExtensionException; 	

}
