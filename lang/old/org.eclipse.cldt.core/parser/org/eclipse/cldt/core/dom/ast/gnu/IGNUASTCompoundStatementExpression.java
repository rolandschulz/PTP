/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cldt.core.dom.ast.gnu;

import org.eclipse.cldt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cldt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cldt.core.dom.ast.IASTExpression;

/**
 * @author jcamelon
 */
public interface IGNUASTCompoundStatementExpression extends IASTExpression {

    public static final ASTNodeProperty STATEMENT = new ASTNodeProperty( "Statement"); //$NON-NLS-1$
    public IASTCompoundStatement getCompoundStatement();
    public void setCompoundStatement( IASTCompoundStatement statement );
    
}