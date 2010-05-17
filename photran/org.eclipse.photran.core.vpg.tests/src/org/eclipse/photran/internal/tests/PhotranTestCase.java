/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.SyntaxException;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.ASTLexerFactory;
import org.eclipse.photran.internal.core.lexer.LexerException;
import org.eclipse.photran.internal.core.lexer.sourceform.UnpreprocessedFreeSourceForm;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.sourceform.ISourceForm;

/**
 * Base class for all Photran test cases.
 * <p>
 * This class provides methods to parse Fortran code, as well as some general utility methods.
 *
 * @author Jeff Overbey
 */
public abstract class PhotranTestCase extends TestCase
{
    public PhotranTestCase()
    {
        super();
        if (!FortranCorePlugin.inTestingMode()) fail("WHEN RUNNING JUNIT TESTS, THE \"TESTING\" ENVIRONMENT VARIABLE MUST BE SET");
    }

    public PhotranTestCase(String name)
    {
        super(name);
        if (!FortranCorePlugin.inTestingMode()) fail("WHEN RUNNING JUNIT TESTS, THE \"TESTING\" ENVIRONMENT VARIABLE MUST BE SET");
    }

    protected ASTExecutableProgramNode parse(String string) throws IOException, LexerException, SyntaxException
    {
        return parse(string, new UnpreprocessedFreeSourceForm());
    }

    protected ASTExecutableProgramNode parse(String string, ISourceForm sourceForm) throws IOException, LexerException, SyntaxException
    {
        ASTExecutableProgramNode ast = new Parser().parse(new ASTLexerFactory().createLexer(new StringReader(string), null, "<stdin>", sourceForm));
        assertTrue(ast != null);
        return ast;
    }

    protected ASTExecutableProgramNode parse(File file) throws IOException, LexerException, SyntaxException, CoreException
    {
        return parse(file, new UnpreprocessedFreeSourceForm());
    }

    protected ASTExecutableProgramNode parse(File file, ISourceForm sourceForm) throws IOException, LexerException, SyntaxException, CoreException
    {
        ASTExecutableProgramNode ast = new Parser().parse(new ASTLexerFactory().createLexer(file, sourceForm));
        assertTrue(ast != null);
        return ast;
    }

    @SuppressWarnings("unchecked")
    protected <T extends IBodyConstruct> T parseStmt(String stmt) throws IOException, LexerException, SyntaxException
    {
        String program = stmt + "\nend program\n";
        ASTExecutableProgramNode ast = new Parser().parse(new ASTLexerFactory().createLexer(new StringReader(program), null, null));
        assertTrue(ast != null);
        LoopReplacer.replaceAllLoopsIn(ast);
        return (T)((ASTMainProgramNode)ast.getProgramUnitList().get(0)).getBody().get(0);
    }

    protected IExpr parseExpression(String expression) throws IOException, LexerException,
        SyntaxException
    {
        String program = "a = " + expression + "\nend";
        ASTExecutableProgramNode ast = parse(program);
        assertNotNull(ast);
        ASTMainProgramNode mainProg = (ASTMainProgramNode)ast.getProgramUnitList().get(0);
        ASTAssignmentStmtNode assignmentStmt = (ASTAssignmentStmtNode)mainProg.getBody().get(0);
        IExpr exprNode = assignmentStmt.getRhs();
        return exprNode;
    }

    protected <T> void assertCollectionsEqual(T[] expectedArray, Iterable<T> actualIterable)
    {
        assertCollectionsEqual(Arrays.asList(expectedArray), actualIterable);
    }

    protected <T> void assertCollectionsEqual(List<T> expected, Iterable<T> actualIterable)
    {
        List<T> actual = new ArrayList<T>();
        for (T entry : actualIterable)
            actual.add(entry);

        assertEquals(expected, actual);
    }
}
