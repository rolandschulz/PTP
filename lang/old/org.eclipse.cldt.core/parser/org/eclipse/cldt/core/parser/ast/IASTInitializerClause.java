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
package org.eclipse.cldt.core.parser.ast;

import java.util.Iterator;

import org.eclipse.cldt.core.parser.Enum;
import org.eclipse.cldt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cldt.core.parser.ITokenDuple;

/**
 * @author jcamelon
 */
public interface IASTInitializerClause extends ISourceElementCallbackDelegate{

	public class Kind extends Enum  
	{
        public static final Kind ASSIGNMENT_EXPRESSION = new Kind( 1 );
		public static final Kind INITIALIZER_LIST      = new Kind( 2 );
		public static final Kind EMPTY                 = new Kind( 3 );
		public static final Kind DESIGNATED_INITIALIZER_LIST = new Kind( 4 );
		public static final Kind DESIGNATED_ASSIGNMENT_EXPRESSION = new Kind( 5 );

		/**
		 * @param enumValue
		 */
		protected Kind(int enumValue) {
			super(enumValue);
		}
	}
	
	public Kind getKind(); 
	public Iterator getInitializers(); 
	public IASTExpression getAssigmentExpression(); 
	public Iterator getDesignators(); 
	
	public void setOwnerVariableDeclaration( IASTVariable declaration );
	public IASTVariable getOwnerVariableDeclaration();
	/**
	 * @param finalDuple
	 * @return
	 */
	public IASTExpression findExpressionForDuple(ITokenDuple finalDuple) throws ASTNotImplementedException;
	
}
