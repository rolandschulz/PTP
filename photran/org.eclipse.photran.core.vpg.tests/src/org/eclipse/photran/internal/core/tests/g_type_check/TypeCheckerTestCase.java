/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.tests.g_type_check;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.analysis.types.TypeChecker;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.parser.Parser;

/**
 * Fortran type checker unit tests
 *
 * @author Stoyan Gaydarov
 */
public class TypeCheckerTestCase extends TestCase
{

    /** Parses a Fortran expression.  Does not bind identifiers, etc. */
    private Type getType(String expression) throws Exception
    {
        // We can only parse entire programs, so we embed the expression in
        // a parseable program, then extract the expression from the program's AST

        String program = "a = " + expression + "\nend";
        ASTExecutableProgramNode ast = new Parser().parse(LexerFactory.createLexer(new ByteArrayInputStream(program.getBytes()), null, "<literal text>", SourceForm.UNPREPROCESSED_FREE_FORM, true /*false*/));
        assertNotNull(ast);
        ASTMainProgramNode mainProg = (ASTMainProgramNode)ast.getProgramUnitList().get(0);
        ASTAssignmentStmtNode assignmentStmt = (ASTAssignmentStmtNode)mainProg.getBody().get(0);
        IExpr exprNode = assignmentStmt.getRhs();
        return TypeChecker.getTypeOf(exprNode);
    }

    private String character = "\"Stoyan\""; // used for character type

    public void testConstantExpressionTypes() throws Exception
    {
        assertEquals(Type.INTEGER, getType("3"));
        assertEquals(Type.REAL, getType("3.0"));
        assertEquals(Type.CHARACTER, getType(character));
        assertEquals(Type.DOUBLEPRECISION, getType("3.0D+00"));
        assertEquals(Type.COMPLEX, getType("(3,3)"));
        assertEquals(Type.LOGICAL, getType(".true."));
    }

    public void testUnaryExpressionTypes() throws Exception
    {
        assertEquals(Type.INTEGER, getType("-3"));
        assertEquals(Type.INTEGER, getType("+3"));
        assertEquals(Type.TYPE_ERROR, getType(".NOT. 3"));

        assertEquals(Type.REAL, getType("-3.0"));
        assertEquals(Type.REAL, getType("+3.0"));
        assertEquals(Type.TYPE_ERROR, getType(".NOT. 3.0"));

        assertEquals(Type.DOUBLEPRECISION, getType("-3.0D+00"));
        assertEquals(Type.DOUBLEPRECISION, getType("+3.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType(".NOT. 3.0D+00"));

        assertEquals(Type.COMPLEX, getType("-(3,3)"));
        assertEquals(Type.COMPLEX, getType("+(3,3)"));
        assertEquals(Type.TYPE_ERROR, getType(".NOT. (3,3)"));

        assertEquals(Type.TYPE_ERROR, getType("-" + character)); //??
        assertEquals(Type.TYPE_ERROR, getType("+" + character)); //??
        assertEquals(Type.TYPE_ERROR, getType(".NOT. " + character));

        assertEquals(Type.TYPE_ERROR, getType("-.true."));
        assertEquals(Type.TYPE_ERROR, getType("+.true."));
        assertEquals(Type.LOGICAL, getType(".NOT. .true."));
    }

    public void testConcatOperations() throws Exception
    {
        assertEquals(Type.UNKNOWN, getType(character + " // " + "1"));
        assertEquals(Type.UNKNOWN, getType(character + " // " + "3.2"));
        assertEquals(Type.UNKNOWN, getType(character + " // " + "43.3D+00"));
        assertEquals(Type.UNKNOWN, getType(character + " // " + "(6,7)"));
        assertEquals(Type.UNKNOWN, getType(character + " // " + ".true."));
        assertEquals(Type.CHARACTER, getType(character + " // " + character));
    }

    public void testGEOperator() throws Exception
    {
        testComparasonOperations(" .GE. ");
    }

    public void testGTOperator() throws Exception
    {
        testComparasonOperations(" .GT. ");
    }

    public void testLEOperator() throws Exception
    {
        testComparasonOperations(" .LE. ");
    }

    public void testLTOperator() throws Exception
    {
        testComparasonOperations(" .LT. ");
    }

    public void testNEOperator() throws Exception
    {
        testComparasonOperations(" .NE. ");
    }

    public void testEQOperator() throws Exception
    {
        testComparasonOperations(" .EQ. ");
    }

    private void testComparasonOperations(String op) throws Exception
    {
        if(op.equals(" .EQ. ") || op.equals(" .NE. "))
        {
            assertEquals(Type.LOGICAL, getType("8" + op + "(4,5)"));
            assertEquals(Type.LOGICAL, getType("3.6" + op + "(3,8)"));
            assertEquals(Type.LOGICAL, getType("3.2D+00" + op + "(4,9)"));
            assertEquals(Type.LOGICAL, getType("(4,8)" + op + "7"));
            assertEquals(Type.LOGICAL, getType("(4,3)" + op + "5.4"));
            assertEquals(Type.LOGICAL, getType("(4,6)" + op + "(4,9)"));
            assertEquals(Type.LOGICAL, getType("(3,0)" + op + "3.0D+00"));
        }
        else
        {
            assertEquals(Type.TYPE_ERROR, getType("8" + op + "(4,5)"));
            assertEquals(Type.TYPE_ERROR, getType("3.6" + op + "(3,8)"));
            assertEquals(Type.TYPE_ERROR, getType("3.2D+00" + op + "(4,9)"));
            assertEquals(Type.TYPE_ERROR, getType("(4,8)" + op + "7"));
            assertEquals(Type.TYPE_ERROR, getType("(4,3)" + op + "5.4"));
            assertEquals(Type.TYPE_ERROR, getType("(4,6)" + op + "(4,9)"));
            assertEquals(Type.TYPE_ERROR, getType("(3,0)" + op + "3.0D+00"));
        }

        assertEquals(Type.LOGICAL, getType("9" + op + "3"));
        assertEquals(Type.LOGICAL, getType("5" + op + "3.0"));
        assertEquals(Type.LOGICAL, getType("4" + op + "2.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType("3" + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType("1" + op + character));

        assertEquals(Type.LOGICAL, getType("3.0" + op + "7"));
        assertEquals(Type.LOGICAL, getType("3.3" + op + "3.5"));
        assertEquals(Type.LOGICAL, getType("4.0" + op + "3.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType("3.2" + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType("4.1" + op + character));

        assertEquals(Type.LOGICAL, getType("3.1D+00" + op + "7"));
        assertEquals(Type.LOGICAL, getType("4.0D+00" + op + "5.4"));
        assertEquals(Type.LOGICAL, getType("5.0D+00" + op + "3.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType("3.8D+00" + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType("2.1D+00" + op + character));

        assertEquals(Type.TYPE_ERROR, getType("(3,3)" + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType("(2,1)" + op + character));

        assertEquals(Type.TYPE_ERROR, getType(".true." + op + "7"));
        assertEquals(Type.TYPE_ERROR, getType(".true." + op + "5.4"));
        assertEquals(Type.TYPE_ERROR, getType(".true." + op + "(4,9)"));
        assertEquals(Type.TYPE_ERROR, getType(".true." + op + "3.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType(".true." + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType(".true." + op + character));

        assertEquals(Type.TYPE_ERROR, getType(character + op + "7"));
        assertEquals(Type.TYPE_ERROR, getType(character + op + "5.4"));
        assertEquals(Type.TYPE_ERROR, getType(character + op + "(4,9)"));
        assertEquals(Type.TYPE_ERROR, getType(character + op + "3.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType(character + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType(character + op + "c"));

    }

    public void testLogicalOperations() throws Exception
    {

        assertEquals(Type.LOGICAL, getType(".true. .AND. .true."));
        assertEquals(Type.TYPE_ERROR, getType("1 .AND. .true."));
        assertEquals(Type.TYPE_ERROR, getType("4.3 .AND. .true."));
        assertEquals(Type.TYPE_ERROR, getType("4.2D+00 .AND. .true."));
        assertEquals(Type.TYPE_ERROR, getType("(3,4) .AND. .true."));
        assertEquals(Type.TYPE_ERROR, getType(character + " .AND. .true."));

        assertEquals(Type.LOGICAL, getType(".true. .OR. .true."));
        assertEquals(Type.TYPE_ERROR, getType("1 .OR. .true."));
        assertEquals(Type.TYPE_ERROR, getType("4.3 .OR. .true."));
        assertEquals(Type.TYPE_ERROR, getType("4.2D+00 .OR. .true."));
        assertEquals(Type.TYPE_ERROR, getType("(3,4) .OR. .true."));
        assertEquals(Type.TYPE_ERROR, getType(character + " .OR. .true."));

        assertEquals(Type.LOGICAL, getType(".true. .EQV. .true."));
        assertEquals(Type.TYPE_ERROR, getType("1 .EQV. .true."));
        assertEquals(Type.TYPE_ERROR, getType("4.3 .EQV. .true."));
        assertEquals(Type.TYPE_ERROR, getType("4.2D+00 .EQV. .true."));
        assertEquals(Type.TYPE_ERROR, getType("(3,4) .EQV. .true."));
        assertEquals(Type.TYPE_ERROR, getType(character + " .EQV. .true."));

        assertEquals(Type.LOGICAL, getType(".true. .NEQV. .true."));
        assertEquals(Type.TYPE_ERROR, getType("1 .NEQV. .true."));
        assertEquals(Type.TYPE_ERROR, getType("4.3 .NEQV. .true."));
        assertEquals(Type.TYPE_ERROR, getType("4.2D+00 .NEQV. .true."));
        assertEquals(Type.TYPE_ERROR, getType("(3,4) .NEQV. .true."));
        assertEquals(Type.TYPE_ERROR, getType(character + " .NEQV. .true."));

    }

    public void testAddExpressionTypes() throws Exception
    {
        testOpExpressionTypes("+");
    }

    public void testSubtractExpressionTypes() throws Exception
    {
        testOpExpressionTypes("-");
    }

    public void testMultiplyExpressionTypes() throws Exception
    {
        testOpExpressionTypes("*");
    }

    public void testDivisionExpressionTypes() throws Exception
    {
        testOpExpressionTypes("/");
    }

    public void testPowerExpressionTypes() throws Exception
    {
        testOpExpressionTypes("**");
    }

    private void testOpExpressionTypes(String op) throws Exception
    {

        assertEquals(Type.INTEGER, getType("9" + op + "3"));
        assertEquals(Type.REAL, getType("5" + op + "3.0"));
        assertEquals(Type.COMPLEX, getType("8" + op + "(4,5)"));
        assertEquals(Type.DOUBLEPRECISION, getType("4" + op + "2.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType("3" + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType("1" + op + character));

        assertEquals(Type.REAL, getType("3.0" + op + "7"));
        assertEquals(Type.REAL, getType("3.3" + op + "3.5"));
        assertEquals(Type.COMPLEX, getType("3.6" + op + "(3,8)"));
        assertEquals(Type.DOUBLEPRECISION, getType("4.0" + op + "3.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType("3.2" + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType("4.1" + op + character));

        assertEquals(Type.COMPLEX, getType("(4,8)" + op + "7"));
        assertEquals(Type.COMPLEX, getType("(4,3)" + op + "5.4"));
        assertEquals(Type.COMPLEX, getType("(4,6)" + op + "(4,9)"));
        assertEquals(Type.COMPLEX, getType("(3,0)" + op + "3.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType("(3,3)" + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType("(2,1)" + op + character));

        assertEquals(Type.DOUBLEPRECISION, getType("3.1D+00" + op + "7"));
        assertEquals(Type.DOUBLEPRECISION, getType("4.0D+00" + op + "5.4"));
        assertEquals(Type.COMPLEX, getType("3.2D+00" + op + "(4,9)"));
        assertEquals(Type.DOUBLEPRECISION, getType("5.0D+00" + op + "3.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType("3.8D+00" + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType("2.1D+00" + op + character));

        assertEquals(Type.TYPE_ERROR, getType(".true." + op + "7"));
        assertEquals(Type.TYPE_ERROR, getType(".true." + op + "5.4"));
        assertEquals(Type.TYPE_ERROR, getType(".true." + op + "(4,9)"));
        assertEquals(Type.TYPE_ERROR, getType(".true." + op + "3.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType(".true." + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType(".true." + op + character));

        assertEquals(Type.TYPE_ERROR, getType(character + op + "7"));
        assertEquals(Type.TYPE_ERROR, getType(character + op + "5.4"));
        assertEquals(Type.TYPE_ERROR, getType(character + op + "(4,9)"));
        assertEquals(Type.TYPE_ERROR, getType(character + op + "3.0D+00"));
        assertEquals(Type.TYPE_ERROR, getType(character + op + ".true."));
        assertEquals(Type.TYPE_ERROR, getType(character + op + "c"));

    }
}