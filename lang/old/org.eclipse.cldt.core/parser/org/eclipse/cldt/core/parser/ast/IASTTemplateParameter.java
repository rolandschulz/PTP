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

import org.eclipse.cldt.core.parser.Enum;
import org.eclipse.cldt.core.parser.ISourceElementCallbackDelegate;

/**
 * @author jcamelon
 *
 */
public interface IASTTemplateParameter  extends IASTTemplateParameterList, IASTTypeSpecifier, ISourceElementCallbackDelegate {

	public class ParamKind extends Enum
	{
		public static final ParamKind CLASS = new ParamKind( 1 );
		public static final ParamKind TYPENAME = new ParamKind( 2 );
		public static final ParamKind TEMPLATE_LIST = new ParamKind( 3 );
		public static final ParamKind PARAMETER = new ParamKind( 4 );

        /**
         * @param enumValue
         */
        protected ParamKind(int enumValue)
        {
            super(enumValue);
        }
	
	}
	
	public ParamKind getTemplateParameterKind(); 
	public String        getIdentifier(); 
	public String		 getDefaultValueIdExpression();
	public IASTParameterDeclaration getParameterDeclaration();
}
