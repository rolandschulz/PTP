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
package org.eclipse.cldt.internal.core.parser.token;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cldt.core.parser.FortranKeywordSetKey;
import org.eclipse.cldt.core.parser.FortranKeywords;

/**
 * @author jcamelon
 */
public class FortranKeywordSets {

	public static Set getKeywords( FortranKeywordSetKey kind )
	{
		if( kind == FortranKeywordSetKey.EMPTY )
			return EMPTY_TABLE;
		if( kind == FortranKeywordSetKey.DECLARATION )
			return DECLARATION_FORTRAN;
		if( kind == FortranKeywordSetKey.STATEMENT )
			return STATEMENT_FORTRAN;
		if( kind == FortranKeywordSetKey.EXPRESSION )
			return EXPRESSION_FORTRAN;
		if( kind == FortranKeywordSetKey.ALL )
			return ALL_FORTRAN;
		if( kind == FortranKeywordSetKey.KEYWORDS )
			return FortranKeywords_FORTRAN;
		if( kind == FortranKeywordSetKey.TYPES )
			return TYPES_FORTRAN;
		//TODO finish this
		return null;
	}
	
	private static final Set EMPTY_TABLE = new HashSet(0);
	
	private static final Set DECLARATION_FORTRAN;
	static
	{
		DECLARATION_FORTRAN = new TreeSet();
		DECLARATION_FORTRAN.add( FortranKeywords.BLOCK );
		DECLARATION_FORTRAN.add( FortranKeywords.COMMON);
		DECLARATION_FORTRAN.add( FortranKeywords.DATA);
		DECLARATION_FORTRAN.add( FortranKeywords.DIMENSION);
		DECLARATION_FORTRAN.add( FortranKeywords.EQUIVALENCE);
		DECLARATION_FORTRAN.add( FortranKeywords.EXTERNAL);
		DECLARATION_FORTRAN.add( FortranKeywords.FORMAT);
		DECLARATION_FORTRAN.add( FortranKeywords.IMPLICIT);
		DECLARATION_FORTRAN.add( FortranKeywords.INTERFACE);
		DECLARATION_FORTRAN.add( FortranKeywords.INTRINSIC);
		DECLARATION_FORTRAN.add( FortranKeywords.MAP);
		DECLARATION_FORTRAN.add( FortranKeywords.PARAMETER);
		DECLARATION_FORTRAN.add( FortranKeywords.PROGRAM);
		DECLARATION_FORTRAN.add( FortranKeywords.RECORD);
		DECLARATION_FORTRAN.add( FortranKeywords.UNION);
		DECLARATION_FORTRAN.add( FortranKeywords.STRUCTURE);
		DECLARATION_FORTRAN.add( FortranKeywords.SUBROUTINE);
	}
				
	private static final Set EXPRESSION_FORTRAN;
	static
	{
		EXPRESSION_FORTRAN = new TreeSet();
		EXPRESSION_FORTRAN.add( FortranKeywords.BYTE );
		EXPRESSION_FORTRAN.add( FortranKeywords.CHARACTER );
		EXPRESSION_FORTRAN.add( FortranKeywords.COMPLEX );
		EXPRESSION_FORTRAN.add( FortranKeywords.DOUBLE );
		EXPRESSION_FORTRAN.add( FortranKeywords.INTEGER );
		EXPRESSION_FORTRAN.add( FortranKeywords.LOGICAL );
		EXPRESSION_FORTRAN.add( FortranKeywords.PRECISION );
		EXPRESSION_FORTRAN.add( FortranKeywords.REAL );
		
	}
			
	private static final Set STATEMENT_FORTRAN;
	static 
	{
		STATEMENT_FORTRAN= new TreeSet(); 
		STATEMENT_FORTRAN.addAll( DECLARATION_FORTRAN );
		STATEMENT_FORTRAN.addAll( EXPRESSION_FORTRAN );
		STATEMENT_FORTRAN.add( FortranKeywords.CALL );
		STATEMENT_FORTRAN.add( FortranKeywords.CASE );
		STATEMENT_FORTRAN.add( FortranKeywords.CLOSE );
		STATEMENT_FORTRAN.add( FortranKeywords.CONTINUE );
		STATEMENT_FORTRAN.add( FortranKeywords.CYCLE );
		STATEMENT_FORTRAN.add( FortranKeywords.DO );
		STATEMENT_FORTRAN.add( FortranKeywords.ELSE);
		STATEMENT_FORTRAN.add( FortranKeywords.END);
		STATEMENT_FORTRAN.add( FortranKeywords.ENDFILE);
		STATEMENT_FORTRAN.add( FortranKeywords.ENTRY);
		STATEMENT_FORTRAN.add( FortranKeywords.EXIT);
		STATEMENT_FORTRAN.add( FortranKeywords.FUNCTION);
		STATEMENT_FORTRAN.add( FortranKeywords.GOTO);
		STATEMENT_FORTRAN.add( FortranKeywords.IF);
		STATEMENT_FORTRAN.add( FortranKeywords.FUNCTION);
		STATEMENT_FORTRAN.add( FortranKeywords.INQUIRE);
		STATEMENT_FORTRAN.add( FortranKeywords.OPEN);
		STATEMENT_FORTRAN.add( FortranKeywords.PAUSE);
		STATEMENT_FORTRAN.add( FortranKeywords.PRINT);
		STATEMENT_FORTRAN.add( FortranKeywords.READ);
		STATEMENT_FORTRAN.add( FortranKeywords.RETURN);
		STATEMENT_FORTRAN.add( FortranKeywords.REWIND);
		STATEMENT_FORTRAN.add( FortranKeywords.SAVE);
		STATEMENT_FORTRAN.add( FortranKeywords.SELECT);
		STATEMENT_FORTRAN.add( FortranKeywords.STOP);
		STATEMENT_FORTRAN.add( FortranKeywords.THEN);
		STATEMENT_FORTRAN.add( FortranKeywords.WRITE);
		STATEMENT_FORTRAN.add( FortranKeywords.WHILE);
	}
			
	private static final Set ALL_FORTRAN;
	static
	{
		ALL_FORTRAN= new TreeSet(); 
		ALL_FORTRAN.add( FortranKeywords.ALLOCATE);
		ALL_FORTRAN.add( FortranKeywords.ASSIGN);
		ALL_FORTRAN.add( FortranKeywords.BACKSPACE);
		ALL_FORTRAN.add( FortranKeywords.BLOCK);
		ALL_FORTRAN.add( FortranKeywords.BYTE);
		ALL_FORTRAN.add( FortranKeywords.CALL);
		ALL_FORTRAN.add( FortranKeywords.CASE);
		ALL_FORTRAN.add( FortranKeywords.CHARACTER);
		ALL_FORTRAN.add( FortranKeywords.CLOSE);
		ALL_FORTRAN.add( FortranKeywords.COMMON);
		ALL_FORTRAN.add( FortranKeywords.COMPLEX);
		ALL_FORTRAN.add( FortranKeywords.CYCLE);
		ALL_FORTRAN.add( FortranKeywords.DATA);
		ALL_FORTRAN.add( FortranKeywords.DEALLOCATE);
		ALL_FORTRAN.add( FortranKeywords.DIMENSION);
		ALL_FORTRAN.add( FortranKeywords.DO);
		ALL_FORTRAN.add( FortranKeywords.DOUBLE);
		ALL_FORTRAN.add( FortranKeywords.ELSE);
		ALL_FORTRAN.add( FortranKeywords.END);
		ALL_FORTRAN.add( FortranKeywords.ENDFILE);
		ALL_FORTRAN.add( FortranKeywords.ENTRY);
		ALL_FORTRAN.add( FortranKeywords.EQUIVALENCE);
		ALL_FORTRAN.add( FortranKeywords.EXIT);
		ALL_FORTRAN.add( FortranKeywords.EXTERNAL);
		ALL_FORTRAN.add( FortranKeywords.FORMAT);
		ALL_FORTRAN.add( FortranKeywords.FUNCTION);
		ALL_FORTRAN.add( FortranKeywords.GOTO);
		ALL_FORTRAN.add( FortranKeywords.IF);
		ALL_FORTRAN.add( FortranKeywords.IMPLICIT);
		ALL_FORTRAN.add( FortranKeywords.INCLUDE);
		ALL_FORTRAN.add( FortranKeywords.INQUIRE);
		ALL_FORTRAN.add( FortranKeywords.INTEGER);
		ALL_FORTRAN.add( FortranKeywords.INTERFACE);
		ALL_FORTRAN.add( FortranKeywords.INTRINSIC);
		ALL_FORTRAN.add( FortranKeywords.LOCKING);
		ALL_FORTRAN.add( FortranKeywords.LOGICAL);
		ALL_FORTRAN.add( FortranKeywords.MAP);
		ALL_FORTRAN.add( FortranKeywords.NAMELIST);
		ALL_FORTRAN.add( FortranKeywords.OPEN);
		ALL_FORTRAN.add( FortranKeywords.PARAMETER);
		ALL_FORTRAN.add( FortranKeywords.PAUSE);
		ALL_FORTRAN.add( FortranKeywords.PRECISION);
		ALL_FORTRAN.add( FortranKeywords.PRINT);
		ALL_FORTRAN.add( FortranKeywords.PROGRAM);
		ALL_FORTRAN.add( FortranKeywords.READ);
		ALL_FORTRAN.add( FortranKeywords.REAL);
		ALL_FORTRAN.add( FortranKeywords.RECORD);
		ALL_FORTRAN.add( FortranKeywords.RETURN);
		ALL_FORTRAN.add( FortranKeywords.REWIND);
		ALL_FORTRAN.add( FortranKeywords.SAVE);
		ALL_FORTRAN.add( FortranKeywords.SELECT);
		ALL_FORTRAN.add( FortranKeywords.STOP);
		ALL_FORTRAN.add( FortranKeywords.STRUCTURE);
		ALL_FORTRAN.add( FortranKeywords.SUBROUTINE);
		ALL_FORTRAN.add( FortranKeywords.THEN);
		ALL_FORTRAN.add( FortranKeywords.TO);
		ALL_FORTRAN.add( FortranKeywords.UNION);
		ALL_FORTRAN.add( FortranKeywords.WHILE);
		ALL_FORTRAN.add( FortranKeywords.WRITE);
	}
	
	private static Set FortranKeywords_FORTRAN;
	static
	{
		FortranKeywords_FORTRAN= new TreeSet();
		FortranKeywords_FORTRAN.add( FortranKeywords.ALLOCATE);
		FortranKeywords_FORTRAN.add( FortranKeywords.ASSIGN);
		FortranKeywords_FORTRAN.add( FortranKeywords.BACKSPACE);
		FortranKeywords_FORTRAN.add( FortranKeywords.BLOCK);
		FortranKeywords_FORTRAN.add( FortranKeywords.BYTE);
		FortranKeywords_FORTRAN.add( FortranKeywords.CALL);
		FortranKeywords_FORTRAN.add( FortranKeywords.CASE);
		FortranKeywords_FORTRAN.add( FortranKeywords.CHARACTER);
		FortranKeywords_FORTRAN.add( FortranKeywords.CLOSE);
		FortranKeywords_FORTRAN.add( FortranKeywords.COMMON);
		FortranKeywords_FORTRAN.add( FortranKeywords.COMPLEX);
		FortranKeywords_FORTRAN.add( FortranKeywords.CYCLE);
		FortranKeywords_FORTRAN.add( FortranKeywords.DATA);
		FortranKeywords_FORTRAN.add( FortranKeywords.DEALLOCATE);
		FortranKeywords_FORTRAN.add( FortranKeywords.DIMENSION);
		FortranKeywords_FORTRAN.add( FortranKeywords.DO);
		FortranKeywords_FORTRAN.add( FortranKeywords.DOUBLE);
		FortranKeywords_FORTRAN.add( FortranKeywords.ELSE);
		FortranKeywords_FORTRAN.add( FortranKeywords.END);
		FortranKeywords_FORTRAN.add( FortranKeywords.ENDFILE);
		FortranKeywords_FORTRAN.add( FortranKeywords.ENTRY);
		FortranKeywords_FORTRAN.add( FortranKeywords.EQUIVALENCE);
		FortranKeywords_FORTRAN.add( FortranKeywords.EXIT);
		FortranKeywords_FORTRAN.add( FortranKeywords.EXTERNAL);
		FortranKeywords_FORTRAN.add( FortranKeywords.FORMAT);
		FortranKeywords_FORTRAN.add( FortranKeywords.FUNCTION);
		FortranKeywords_FORTRAN.add( FortranKeywords.GOTO);
		FortranKeywords_FORTRAN.add( FortranKeywords.IF);
		FortranKeywords_FORTRAN.add( FortranKeywords.IMPLICIT);
		FortranKeywords_FORTRAN.add( FortranKeywords.INCLUDE);
		FortranKeywords_FORTRAN.add( FortranKeywords.INQUIRE);
		FortranKeywords_FORTRAN.add( FortranKeywords.INTEGER);
		FortranKeywords_FORTRAN.add( FortranKeywords.INTERFACE);
		FortranKeywords_FORTRAN.add( FortranKeywords.INTRINSIC);
		FortranKeywords_FORTRAN.add( FortranKeywords.LOCKING);
		FortranKeywords_FORTRAN.add( FortranKeywords.LOGICAL);
		FortranKeywords_FORTRAN.add( FortranKeywords.MAP);
		FortranKeywords_FORTRAN.add( FortranKeywords.NAMELIST);
		FortranKeywords_FORTRAN.add( FortranKeywords.OPEN);
		FortranKeywords_FORTRAN.add( FortranKeywords.PARAMETER);
		FortranKeywords_FORTRAN.add( FortranKeywords.PAUSE);
		FortranKeywords_FORTRAN.add( FortranKeywords.PRECISION);
		FortranKeywords_FORTRAN.add( FortranKeywords.PRINT);
		FortranKeywords_FORTRAN.add( FortranKeywords.PROGRAM);
		FortranKeywords_FORTRAN.add( FortranKeywords.READ);
		FortranKeywords_FORTRAN.add( FortranKeywords.REAL);
		FortranKeywords_FORTRAN.add( FortranKeywords.RECORD);
		FortranKeywords_FORTRAN.add( FortranKeywords.RETURN);
		FortranKeywords_FORTRAN.add( FortranKeywords.REWIND);
		FortranKeywords_FORTRAN.add( FortranKeywords.SAVE);
		FortranKeywords_FORTRAN.add( FortranKeywords.SELECT);
		FortranKeywords_FORTRAN.add( FortranKeywords.STOP);
		FortranKeywords_FORTRAN.add( FortranKeywords.STRUCTURE);
		FortranKeywords_FORTRAN.add( FortranKeywords.SUBROUTINE);
		FortranKeywords_FORTRAN.add( FortranKeywords.THEN);
		FortranKeywords_FORTRAN.add( FortranKeywords.TO);
		FortranKeywords_FORTRAN.add( FortranKeywords.UNION);
		FortranKeywords_FORTRAN.add( FortranKeywords.WHILE);
		FortranKeywords_FORTRAN.add( FortranKeywords.WRITE);
	}
	

	private static final Set TYPES_FORTRAN;
	static
	{
		TYPES_FORTRAN = new TreeSet();
		TYPES_FORTRAN.add( FortranKeywords.BYTE );
		TYPES_FORTRAN.add( FortranKeywords.CHARACTER );
		TYPES_FORTRAN.add( FortranKeywords.COMPLEX );
		TYPES_FORTRAN.add( FortranKeywords.DOUBLE );
		TYPES_FORTRAN.add( FortranKeywords.INTEGER );
		TYPES_FORTRAN.add( FortranKeywords.LOGICAL );
		TYPES_FORTRAN.add( FortranKeywords.PRECISION );
		TYPES_FORTRAN.add( FortranKeywords.REAL );
	}
}
