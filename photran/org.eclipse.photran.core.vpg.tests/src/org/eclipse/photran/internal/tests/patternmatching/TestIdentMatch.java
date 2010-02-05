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
package org.eclipse.photran.internal.tests.patternmatching;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.photran.internal.core.SyntaxException;
import org.eclipse.photran.internal.core.lexer.LexerException;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTBinaryExprNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.parser.Parser.ASTMatcher;
import org.eclipse.photran.internal.core.parser.Parser.ASTMatcher.Match;
import org.eclipse.photran.internal.core.parser.Parser.ASTMatcher.SearchStrategy;

/**
 * 
 * @author joverbey
 */
public class TestIdentMatch extends TestCase
{
    public void setUp()
    {
        Parser.ASTMatcher.patternVariableSuffix = "";
        Parser.ASTMatcher.doNotMatchTokenText = new HashSet<Terminal>();
        Parser.ASTMatcher.matchTokenTextCaseInsensitive = new HashSet<Terminal>();
        
        for (Field f : Terminal.class.getFields())
        {
            try
            {
                Terminal t = (Terminal)f.get(null);
                if (t == Terminal.T_EOS)
                    Parser.ASTMatcher.doNotMatchTokenText.add(t);
                else
                    Parser.ASTMatcher.matchTokenTextCaseInsensitive.add(t);
            }
            catch (IllegalArgumentException e) {}
            catch (IllegalAccessException e) {}
        }
    }

    public void testUnification1() throws Exception
    {
        String pattern = "prOGram @a:T_IDENT\nend program @a:T_IDENT";
        String prog1 = "program  jeff  \n    ! Do stuff\n  end progRam jEFf\n";
        String prog2 = "program  jeff  \n    ! Do stuff\n  end program\n";
        ASTExecutableProgramNode patternAST = parse(pattern);
        //System.out.println(match(patternAST, parse(prog1)));
        //Match<ASTExecutableProgramNode> result = parse(prog1).match(patternAST);
        //System.err.println(result.errorMessage);
        assertEquals("  jeff", parse(prog1).match(patternAST).variables.get("@a:T_IDENT").toString());
        assertFalse(parse(prog2).match(patternAST).succeeded());
    }

    public void testUnification2() throws Exception
    {
        String pattern = "@a:T_IDENT = 3\nend program";
        String prog1 = "program jeff\n  ! Do stuff\n  three = 3; eerht = 3\nend program\n";
        
        ASTAssignmentStmtNode patternAST = parse(pattern).findFirst(ASTAssignmentStmtNode.class);
        List<Match<ASTAssignmentStmtNode>> matches = new ASTMatcher(SearchStrategy.BOTTOM_UP).matchAll(patternAST, parse(prog1));
        //System.out.println(matches);
        assertEquals(2, matches.size());
    }

    public void testUnification3() throws Exception
    {
        String pattern = "three = xxxx\nend program";
        String prog1 = "program jeff\n  ! Do stuff\n  three = 3; eerht = 3\nthree = three * three + three\nend program\n";
        
        Token patternAST = parse(pattern).findFirst(ASTAssignmentStmtNode.class).getLhsVariable().getName();

        List<Match<Token>> matches = new ASTMatcher(SearchStrategy.TOP_DOWN_NO_NESTED_MATCHES).matchAll(patternAST, parse(prog1));
        assertEquals(5, matches.size());

        matches = new ASTMatcher(SearchStrategy.BOTTOM_UP).matchAll(patternAST, parse(prog1));
        //assertEquals(7, matches.size());

        matches = new ASTMatcher(SearchStrategy.TOP_DOWN).matchAll(patternAST, parse(prog1));
        //assertEquals(7, matches.size());
    }

    public void testUnification4() throws Exception
    {
        String pattern = "xxxx = @var:T_IDENT + 0\nend program";
        String prog1 = "n = ((n * (2 + 0)) + 0) + 0; end program\n";
        
        ASTBinaryExprNode patternAST = (ASTBinaryExprNode)parse(pattern).findFirst(ASTAssignmentStmtNode.class).getRhs();
        ASTExecutableProgramNode progAST = parse(prog1);
        
//        List<Match<IExpr>> iexprMatches = new ASTMatcher(SearchStrategy.BOTTOM_UP).matchAll(IExpr.class, patternAST, progAST);
//        //System.out.println(iexprMatches);
//        assertEquals(3, iexprMatches.size());
        
        List<Match<ASTBinaryExprNode>> matches = new ASTMatcher(SearchStrategy.BOTTOM_UP).matchAll(patternAST, progAST);
        //System.out.println(matches);
        assertEquals(3, matches.size());
        
        for (Match<ASTBinaryExprNode> match : matches)
            match.matchedNode.replaceWith(match.variables.get("@var:T_IDENT"));
        //System.out.println(progAST);
        assertEquals("n = ((n * (2))); end program", progAST.toString().trim());

        progAST = parse(prog1);
        pattern = "xxxx = @var:T_IDENT\nend program";
        IExpr replacementAST = parse(pattern).findFirst(ASTAssignmentStmtNode.class).getRhs();
        new ASTMatcher(SearchStrategy.BOTTOM_UP).replaceAll(patternAST, replacementAST, progAST);
        //System.out.println(progAST);
        assertEquals("n =  ( (n * ( 2))); end program", progAST.toString().trim());

        // FIXME: This causes an error because discardedSymbols don't have parents set
        //progAST = parse("a = a + a + (2 * b + b) + (b + b * c) + (d + d) - ((2*q+6) + (2*q+6); end");
        progAST = parse("a = a + a + (2 * b + b) + (b + b * c) + (d + d) - ((2*q+6) + (2*q+6)); end");
        pattern = "xxxx=@var:T_IDENT + @var:T_IDENT\nend program";
        IExpr patternnAST = parse(pattern).findFirst(ASTAssignmentStmtNode.class).getRhs();
        String replacement = "xxxx=(2*@var:T_IDENT)\nend program";
        replacementAST = parse(replacement).findFirst(ASTAssignmentStmtNode.class).getRhs();
        new ASTMatcher(SearchStrategy.BOTTOM_UP).replaceAll(patternnAST, replacementAST, progAST);
        //System.out.println(progAST);
        assertEquals("a =(2* a) + (2 * b + b) + (b + b * c) + ((2*d)) - ((2*(2*q+6))); end", progAST.toString().trim());

        /*
        @SuppressWarnings("rawtypes")
        class ASTPatternListNode2<T> implements IASTListNode<T>
        {
            // IASTNode
            public void accept(IASTVisitor visitor) { throw new UnsupportedOperationException(); }
            public Object clone() { throw new UnsupportedOperationException(); }
            public Token findFirstToken() { throw new UnsupportedOperationException(); }
            public Token findLastToken() { throw new UnsupportedOperationException(); }
            public <T extends IASTNode> T findNearestAncestor(Class<T> targetClass) { throw new UnsupportedOperationException(); }
            public IASTNode getParent() { throw new UnsupportedOperationException(); }
            public boolean isFirstChildInList() { throw new UnsupportedOperationException(); }
            public Iterable<? extends IASTNode> getChildren() { throw new UnsupportedOperationException(); }
            //public void printOn(PrintStream out) { throw new UnsupportedOperationException(); }
            public IPreprocessorReplacement printOn(PrintStream out, IPreprocessorReplacement lastPreprocessorDirective) { throw new UnsupportedOperationException(); }
            public void replaceChild(IASTNode node, IASTNode withNode) { throw new UnsupportedOperationException(); }
            public void removeFromTree() { throw new UnsupportedOperationException(); }
            public void replaceWith(IASTNode newNode) { throw new UnsupportedOperationException(); }
            public void replaceWith(String literalString) { throw new UnsupportedOperationException(); }
            public void setParent(IASTNode parent) { throw new UnsupportedOperationException(); }
            // IASTListNode
            public void insertBefore(Object insertBefore, Object newElement) { throw new UnsupportedOperationException(); }
            public void insertAfter(Object insertAfter, Object newElement) { throw new UnsupportedOperationException(); }
            // List
            public void add(int index, Object element) { throw new UnsupportedOperationException(); }
            public boolean addAll(int index, java.util.Collection c) { throw new UnsupportedOperationException(); }
            public void clear() { throw new UnsupportedOperationException(); }
            public Object get(int index) { throw new UnsupportedOperationException(); }
            public int indexOf(Object o) { throw new UnsupportedOperationException(); }
            public boolean isEmpty() { throw new UnsupportedOperationException(); }
            public Iterator iterator() { throw new UnsupportedOperationException(); }
            public int lastIndexOf(Object o) { throw new UnsupportedOperationException(); }
            public ListIterator<Object> listIterator() { throw new UnsupportedOperationException(); }
            public ListIterator<Object> listIterator(int index) { throw new UnsupportedOperationException(); }
            public Object remove(int index) { throw new UnsupportedOperationException(); }
            public Object set(int index, Object element) { throw new UnsupportedOperationException(); }
            public int size() { throw new UnsupportedOperationException(); }
            public List<Object> subList(int fromIndex, int toIndex) { throw new UnsupportedOperationException(); }
            // Collection
            public boolean add(Object o) { throw new UnsupportedOperationException(); }
            public boolean addAll(java.util.Collection c) { throw new UnsupportedOperationException(); }
            public boolean contains(Object o) { throw new UnsupportedOperationException(); }
            public boolean containsAll(java.util.Collection c) { throw new UnsupportedOperationException(); }
            public boolean equals(Object o) { throw new UnsupportedOperationException(); }
            public boolean remove(Object o) { throw new UnsupportedOperationException(); }
            public boolean removeAll(java.util.Collection c) { throw new UnsupportedOperationException(); }
            public boolean retainAll(java.util.Collection c) { throw new UnsupportedOperationException(); }
            public Object[] toArray() { throw new UnsupportedOperationException(); }
            public Object[] toArray(Object[] a) { throw new UnsupportedOperationException(); }
        };
    */
    }

    protected ASTExecutableProgramNode parse(String contents) throws IOException, LexerException, SyntaxException
    {
        return new Parser().parse(LexerFactory.createLexer(new ByteArrayInputStream(contents.getBytes()), null, "<stdin>", SourceForm.UNPREPROCESSED_FREE_FORM, true));
    }
}
