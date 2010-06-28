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
package org.eclipse.photran.internal.core.reindenter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTIfStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.IActionStmt;

/**
 * The first token(s) on a line.
 * <p>
 * For a line beginning an {@link IActionStmt}, this contains its numeric statement label (if any)
 * and the first token of the statement.  For all other lines, including continued lines, this
 * consists of only the first token on the line.
 * 
 * @author Jeff Overbey
 */
final class StartOfLine
{
    private static final String INDENT = "    "; //$NON-NLS-1$

    public static StartOfLine createForLine(int line, IFortranAST ast)
    {
        return StartOfLine.createForLineStartingWith(ast.findFirstTokenOnLine(line));
    }
    
    public static StartOfLine createForFirstNonemptyLineBelow(Token token, IFortranAST ast)
    {
        return StartOfLine.createForFirstNonemptyLineBelow(token.getLine(), ast);
    }
    
    public static StartOfLine createForFirstNonemptyLineBelow(int startLine, IFortranAST ast)
    {
        Token previousToken = null;
        for (Token tok : ast)
        {
            if (tok.getLine() > startLine && (previousToken == null || previousToken.getLine() <= startLine))
                return StartOfLine.createForLineStartingWith(tok);
            
            previousToken = tok;
        }
        
        return null;
    }

    public static StartOfLine createForLastNonemptyLineAbove(StartOfLine line, IFortranAST ast)
    {
        return StartOfLine.createForLastNonemptyLineAbove(line.getFirstTokenOnLine().getLine(), ast);
    }

    private static StartOfLine createForLastNonemptyLineAbove(int startLine, IFortranAST ast)
    {
        for (int line = startLine - 1; line >= 0; line--)
        {
            Token firstBlockOnLine = ast.findFirstTokenOnLine(line);
            if (firstBlockOnLine != null)
                return StartOfLine.createForLineStartingWith(firstBlockOnLine);
        }
        
        return null;
    }
    
    public static StartOfLine createForLineStartingWith(Token token)
    {
        if (token == null)
            return null;
        else if (isLabel(token))
            return new StartOfLine(token, getTokenFollowingLabel(token));
        else
            return new StartOfLine(null, token);
    }

    private static Token getTokenFollowingLabel(final Token label)
    {
        assert isLabel(label);
        
        class V extends ASTVisitor
        {
            private Token lastToken = null;
            private Token tokenFollowingLabel = null;
            
            @Override public void visitToken(Token token)
            {
                if (lastToken == label)
                    tokenFollowingLabel = token;
                
                lastToken = token;
            }
        }
        
        V v = new V();
        label.getParent().accept(v);
        return v.tokenFollowingLabel;
    }

    private static boolean isLabel(Token token)
    {
        return token != null
            && token.getParent() != null
            && token.getParent() instanceof IActionStmt
            && ((IActionStmt)token.getParent()).getLabel() == token;
    }

    /** The numeric statement label (may be <code>null</code>) */
    private final Token label;
    
    /** The first token of the statement that is not its numeric statement label (non-<code>null</code>) */
    private final Token firstStmtToken;

    /**
     * @param label the statement label (may be <code>null</code>)
     * @param firstStmtToken the first non-label token on the line (non-<code>null</code>)
     */
    private StartOfLine(Token label, Token firstStmtToken)
    {
        this.label = label; //new Token(Terminal.T_ICON, ""); $NON-NLS-1$
        this.firstStmtToken = firstStmtToken;
    }
    
    private String getComments()
    {
        Token token = getFirstTokenOnLine();
        String whiteText = token.getWhiteBefore();
        int lastCR = whiteText.lastIndexOf('\n');
        if (lastCR >= 0)
            return whiteText.substring(0, lastCR+1);
        else
            return ""; //$NON-NLS-1$
    }
    
    public void reindent(String removeIndent, String addIndent)
    {
        reindentComments(removeIndent, addIndent);
        reindentStatement(removeIndent, addIndent);
    }

    private void reindentComments(String removeIndent, String addIndent)
    {
        StringBuilder sb = new StringBuilder();
        for (String line : splitLines(getComments()))
            sb.append(reindentCommentLine(line, removeIndent, addIndent));
        String reindentedComments = sb.toString();
        
        getFirstTokenOnLine().setWhiteBefore(reindentedComments + getIndentation());
    }
    
    private ArrayList<String> splitLines(String comments)
    {
        ArrayList<String> result = new ArrayList<String>();
        
        int start = 0;
        int end = comments.indexOf('\n', start);
        
        while (end > start)
        {
            result.add(comments.substring(start, end+1));
            
            start = end + 1;
            end = comments.indexOf('\n', start);
        }
        
        if (comments.length() > start)
            result.add(comments.substring(start, comments.length()));
        
        return result;
    }

    private String reindentCommentLine(String line, String removeIndent, String addIndent)
    {
        if (!line.trim().startsWith("!")) return line; //$NON-NLS-1$
        
        int endIndex = 0;
        while (endIndex < line.length() && (line.charAt(endIndex) == ' ' || line.charAt(endIndex) == '\t'))
            endIndex++;
        
        String indentation = line.substring(0, endIndex);
        String comment = line.substring(endIndex);
        
        return newIndentation(indentation, removeIndent, addIndent) + comment;
    }

    private void reindentStatement(String removeIndent, String addIndent)
    {
        setIndentation(newIndentation(getIndentation(), removeIndent, addIndent));
    }

    public void setIndentation(String newIndentation)
    {
        getFirstTokenOnLine().setWhiteBefore(getComments() + newIndentation);

        if (label != null)
        {
            Token lbl = label;
            String lblIndentation = getIndentation(); //lbl.getWhiteBefore();
            
            String combinedWhitespace = lblIndentation/*+fstStmtTok.getWhiteBefore()*/;
            int value = combinedWhitespace.length() /*- fstStmtTok.getWhiteBefore().length()*/ - lbl.getText().length();
            firstStmtToken.setWhiteBefore(combinedWhitespace.substring(0, value));
            lbl.setWhiteBefore(""); //$NON-NLS-1$
        }
    }

    private String newIndentation(String currentIndentation, String removeIndent, String addIndent)
    {
        String newIndentation;
        if (currentIndentation.startsWith(removeIndent))
            newIndentation = currentIndentation.substring(removeIndent.length());
        else
            newIndentation = currentIndentation;
        
        newIndentation += addIndent;
        return newIndentation;
    }

    public String getIndentation()
    {
        String whiteText = getFirstTokenOnLine().getWhiteBefore();
        int lastCR = whiteText.lastIndexOf('\n');
        return whiteText.substring(lastCR + 1);
    }

    public Token getFirstTokenOnLine()
    {
        if (label != null)
            return label;
        else
            return firstStmtToken;
    }

//    /**
//     * @return the first token of the statement that is not its numeric statement label (non-<code>null</code>)
//     */
//    public Token getFirstStmtToken()
//    {
//        return firstStmtToken;
//    }

    public boolean startsIndentedRegion()
    {
        Terminal t = firstStmtToken.getTerminal();
        return t == Terminal.T_PROGRAM
            || t == Terminal.T_FUNCTION
            || t == Terminal.T_SUBROUTINE
            || t == Terminal.T_MODULE
            || t == Terminal.T_BLOCK
            || t == Terminal.T_BLOCKDATA
            || t == Terminal.T_FORALL
            || t == Terminal.T_WHERE
            || t == Terminal.T_TYPE && !startsTypeDeclaration()
            || t == Terminal.T_IF && !startsSingleLineIfStmt()
            || t == Terminal.T_ELSE
            || t == Terminal.T_ELSEWHERE
            || t == Terminal.T_ELSEIF
            || t == Terminal.T_SELECTCASE
            || t == Terminal.T_SELECT
            || t == Terminal.T_CASE
            || t == Terminal.T_DO
            || t == Terminal.T_INTERFACE
            || t == Terminal.T_CONTAINS
            || t == Terminal.T_ASSOCIATE;
    }

    private boolean startsTypeDeclaration()
    {
        return firstStmtToken.findNearestAncestor(ASTTypeDeclarationStmtNode.class) != null;
    }

    /**
     * @return true iff this line is the start of a (single-line) IF statement
     * (as opposed to an if-then construct, which usually spans multiple lines)
     */
    private boolean startsSingleLineIfStmt()
    {
        return firstStmtToken.findNearestAncestor(ASTIfStmtNode.class) != null;
    }

    public boolean endsIndentedRegion()
    {
        Terminal t = firstStmtToken.getTerminal();
        return t == Terminal.T_END
            || t == Terminal.T_ENDBLOCK
            || t == Terminal.T_ENDBLOCKDATA
            || t == Terminal.T_ENDDO
            || t == Terminal.T_ENDFILE
            || t == Terminal.T_ENDFORALL
            || t == Terminal.T_ENDFUNCTION
            || t == Terminal.T_ENDIF
            || t == Terminal.T_ENDINTERFACE
            || t == Terminal.T_ENDMODULE
            || t == Terminal.T_ENDPROGRAM
            || t == Terminal.T_ENDSELECT
            || t == Terminal.T_ENDSUBROUTINE
            || t == Terminal.T_ENDTYPE
            || t == Terminal.T_ENDWHERE
            || t == Terminal.T_ELSE
            || t == Terminal.T_ELSEWHERE
            || t == Terminal.T_ELSEIF
            || t == Terminal.T_CONTAINS;
    }
    
    @Override public String toString()
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);
        if (label != null) label.printOn(out, null);
        firstStmtToken.printOn(out, null);
        return bytes.toString();
    }

    /**
     * @return the indentation of this line, plus an additional unit of indentation
     */
    public String getIncreasedIndentation()
    {
        return getIndentation() + INDENT;
    }
    
    /**
     * @return the indentation of this line without its final unit of indentation
     */
    public String getDecreasedIndentation()
    {
        String indentation = getIndentation();
        if (indentation.endsWith(INDENT))
            return indentation.substring(0, indentation.length()-INDENT.length());
        else if (indentation.endsWith("\t")) //$NON-NLS-1$
            return indentation.substring(0, indentation.length()-1);
        else
            return indentation;
    }
}