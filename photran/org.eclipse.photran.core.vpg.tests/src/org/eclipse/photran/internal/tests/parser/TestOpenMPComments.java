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
package org.eclipse.photran.internal.tests.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.photran.internal.core.SyntaxException;
import org.eclipse.photran.internal.core.lexer.LexerException;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser;

/**
 * Unit tests for {@link Token#getOpenMPComments()}.
 * 
 * @author Jeff Overbey
 */
public class TestOpenMPComments extends TestCase
{
    public void testFreeFormOpenMPComments() throws Exception
    {
        ASTExecutableProgramNode ast = parse(SourceForm.UNPREPROCESSED_FREE_FORM,
            //         1    1    2    2    3    3
            //----5----0----5----0----5----0----5
            "! This is a sample OpenMP program\n" + // Starts at offset 0
            "program OpenMP\n" +                                    // 34
            "    integer :: num_threads, id\n" +                    // 49
            "    !$omp parallel private(num_threads, id) \n" +      // 80
            "    id = omp_get_thread_num()\n" +
            "    print *, 'This is thread ', id\n" +
            "    if (id == 0) then\n" +
            "      num_threads = omp_get_num_threads()\n" +
            "      print *, 'Total threads: ', num_threads\n" +
            "    end if\n" +
            "    !$omp end parallel\n" +
            "end program\n");
        //System.out.println(ast);
        
        List<Token> comments = OpenMPCommentVisitor.getOpenMPCommentsIn(ast);
        
        assertEquals(2, comments.size());
        
        Token directive = comments.get(0);
        assertEquals(4, directive.getLine());
        assertEquals(84, directive.getFileOffset());
        assertEquals(84, directive.getStreamOffset());
        assertEquals("!$omp parallel private(num_threads, id)".length(), directive.getLength());
        assertEquals("!$omp ", directive.getWhiteBefore());
        assertEquals("parallel private(num_threads, id)", directive.getText());
        assertEquals(" \n", directive.getWhiteAfter());
        
        directive = comments.get(1);
        assertEquals(11, directive.getLine());
        assertEquals(315, directive.getFileOffset());
        assertEquals("end parallel", directive.getText());
    }
    
    public void testFixedFormOpenMPComments() throws Exception
    {
        ASTExecutableProgramNode ast = parse(SourceForm.FIXED_FORM,
            //         1    1    2    2    3    3
            //----5----0----5----0----5----0----5
            "! This is a sample OpenMP program\n" + // Starts at offset 0
            "       program OpenMP\n" +                             // 34
            "       integer :: num_threads, id\n" +                 // 56
            "c$omp  parallel private(num_threads, id)\n" +          // 90
            "       id = omp_get_thread_num()\n" +
            "       print *, 'This is thread ', id\n" +
            "       if (id .eq. 0) then\n" +
            "           num_threads = omp_get_num_threads()\n" +
            "           print *, 'Total threads: ', num_threads\n" +
            "       end if\n" +
            "c$omp  end parallel\n" +
            "       end program\n");
        //System.out.println(ast);
        
        List<Token> comments = OpenMPCommentVisitor.getOpenMPCommentsIn(ast);
        
        assertEquals(2, comments.size());
        
        Token directive = comments.get(0);
        assertEquals(4, directive.getLine());
        assertEquals(90, directive.getFileOffset());
        assertEquals(90, directive.getStreamOffset());
        assertEquals("c$omp  parallel private(num_threads, id)".length(), directive.getLength());
        assertEquals("c$omp  ", directive.getWhiteBefore());
        assertEquals("parallel private(num_threads, id)", directive.getText());
        assertEquals("\n", directive.getWhiteAfter());
        
        directive = comments.get(1);
        assertEquals(11, directive.getLine());
        assertEquals(341, directive.getFileOffset());
        assertEquals("end parallel", directive.getText());
    }
    
    public static final class OpenMPCommentVisitor extends GenericASTVisitor
    {
        public static List<Token> getOpenMPCommentsIn(ASTExecutableProgramNode ast)
        {
            OpenMPCommentVisitor visitor = new OpenMPCommentVisitor();
            ast.accept(visitor);
            return visitor.ompComments;
        }
        
        private List<Token> ompComments = new ArrayList<Token>();
        
        @Override
        public void visitToken(Token token)
        {
            ompComments.addAll(token.getOpenMPComments());
        }
    }
    
    private ASTExecutableProgramNode parse(SourceForm sourceForm, String string) throws IOException, LexerException, SyntaxException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(string.getBytes());
        ASTExecutableProgramNode ast = new Parser().parse(LexerFactory.createLexer(in, null, "<stdin>", sourceForm, true));
        assertTrue(ast != null);
        return ast;
    }
}
