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
package org.eclipse.fdt.core.parser;


/**
 * @author jcamelon
 *
 */
public class DefaultProblemHandler
{

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.IProblemHandler#ruleOnProblem(org.eclipse.fdt.core.parser.IProblem)
	 */
	public static boolean ruleOnProblem( IProblem p, ParserMode mode )
	{
		if( p == null ) return true; 
		if( p.checkCategory( IProblem.SCANNER_RELATED ) || p.checkCategory( IProblem.PREPROCESSOR_RELATED ))
		{
			switch( p.getID() )
			{
				case IProblem.PREPROCESSOR_INVALID_DIRECTIVE: 
					return false;
				case IProblem.PREPROCESSOR_POUND_ERROR: 
				case IProblem.PREPROCESSOR_UNBALANCE_CONDITION:
				case IProblem.PREPROCESSOR_INVALID_MACRO_DEFN:
//				case IProblem.PREPROCESSOR_MACRO_USAGE_ERROR:
				case IProblem.PREPROCESSOR_MACRO_PASTING_ERROR:
				case IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR:
				case IProblem.SCANNER_UNEXPECTED_EOF:
				case IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN:
					if( mode == ParserMode.COMPLETE_PARSE )
						return false;
				default:
					return true;
			}
		}
		return true; 
	}

}
