/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.dependence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.photran.internal.core.SyntaxException;
import org.eclipse.photran.internal.core.analysis.dependence.LoopDependences;
import org.eclipse.photran.internal.core.analysis.dependence.GCDTest;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.LexerException;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.Parser;

/**
 * Base class for unit tests for dependence testing classes.
 * 
 * @author Jeff Overbey
 */
abstract class BaseTestCase extends TestCase
{
    protected LoopDependences dependences(String loop) throws IOException, LexerException, SyntaxException
    {
        return LoopDependences.computeFor(loop(loop), new GCDTest());
    }
    
    protected ASTProperLoopConstructNode loop(String stmt) throws IOException, LexerException, SyntaxException
    {
        ASTProperLoopConstructNode result = parseStmt(stmt);
        assertNotNull(result);
        return result;
    }
    
    protected ASTAssignmentStmtNode assignment(String stmt) throws IOException, LexerException, SyntaxException
    {
        ASTAssignmentStmtNode result = parseStmt(stmt);
        assertNotNull(result);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends IBodyConstruct> T parseStmt(String stmt) throws IOException, LexerException, SyntaxException
    {
        String program = stmt + "\nend program\n";
        InputStream in = new ByteArrayInputStream(program.getBytes());
        ASTExecutableProgramNode ast = new Parser().parse(LexerFactory.createLexer(in, null, null, SourceForm.UNPREPROCESSED_FREE_FORM, true));
        assertTrue(ast != null);
        LoopReplacer.replaceAllLoopsIn(ast);
        return (T)((ASTMainProgramNode)ast.getProgramUnitList().get(0)).getBody().get(0);
    }
}
