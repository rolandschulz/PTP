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
package org.eclipse.fdt.internal.core.parser.ast.complete;

import org.eclipse.fdt.core.parser.ast.IASTDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTScope;
import org.eclipse.fdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.fdt.internal.core.parser.pst.ISymbol;
import org.eclipse.fdt.internal.core.parser.pst.ISymbolOwner;
import org.eclipse.fdt.internal.core.parser.pst.ITypeInfo;
import org.eclipse.fdt.internal.core.parser.pst.ParserSymbolTableError;
import org.eclipse.fdt.internal.core.parser.pst.TypeInfoProvider;

/**
 * @author jcamelon
 *
 */
public abstract class ASTSymbol extends ASTSymbolOwner implements ISymbolOwner, IASTDeclaration
{
	
    public ASTSymbol( ISymbol symbol )
    {
        super(symbol);
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTScopedElement#getOwnerScope()
     */
    public IASTScope getOwnerScope()
    {
    	if( symbol.getContainingSymbol() != null )
    		return (IASTScope)symbol.getContainingSymbol().getASTExtension().getPrimaryDeclaration();
    	return null;
    }

    public IContainerSymbol getLookupQualificationSymbol() throws LookupError {
    	ISymbol sym = getSymbol();
    	IContainerSymbol result = null;
    	TypeInfoProvider provider = sym.getSymbolTable().getTypeInfoProvider();
		ITypeInfo info = null;
		
		try{
			info = sym.getTypeInfo().getFinalType( provider );
		} catch( ParserSymbolTableError e ){
			throw new LookupError();
		}
		
		if( info.isType( ITypeInfo.t_type ) && info.getTypeSymbol() != null && info.getTypeSymbol() instanceof IContainerSymbol )
			result = (IContainerSymbol) info.getTypeSymbol();
		else if( sym instanceof IContainerSymbol )
			result = (IContainerSymbol) sym;
	
		provider.returnTypeInfo( info );
		return result;
    }
    
    public boolean shouldFilterLookupResult( ISymbol sym ){
    	boolean result = false;
    	TypeInfoProvider provider = sym.getSymbolTable().getTypeInfoProvider();
    	ITypeInfo info = null;
    	try{
			info = getSymbol().getTypeInfo().getFinalType( provider );
		} catch( ParserSymbolTableError e ){
			return true;
		}
		
		if( info.checkBit( ITypeInfo.isConst ) && !sym.getTypeInfo().checkBit( ITypeInfo.isConst ) )
			result = true;
		
		if( info.checkBit( ITypeInfo.isVolatile ) && !sym.getTypeInfo().checkBit( ITypeInfo.isVolatile ) )
			result = true;
		
		provider.returnTypeInfo( info );
		return result;
		
    }
}
