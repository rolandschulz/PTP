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

import static org.eclipse.photran.internal.core.reindenter.Reindenter.defaultIndentation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAssociateStmtNode;
import org.eclipse.photran.internal.core.parser.ASTBlockDataStmtNode;
import org.eclipse.photran.internal.core.parser.ASTBlockStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCaseConstructNode;
import org.eclipse.photran.internal.core.parser.ASTCaseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTContainsStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDoConstructNode;
import org.eclipse.photran.internal.core.parser.ASTElseIfStmtNode;
import org.eclipse.photran.internal.core.parser.ASTElseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTElseWhereStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndSelectStmtNode;
import org.eclipse.photran.internal.core.parser.ASTForallConstructStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTIfThenStmtNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLabelDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTModuleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTProgramStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSelectCaseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSelectTypeStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.ASTWhereConstructStmtNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
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
        
        while (end >= start)
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

    /**
     * Sets the indentation for the first (non-label) token on the line.
     * <p>
     * If <code>newIndentation</code> is eight spaces, then
     * <pre>
     * ! This is a comment
     * print *, 0
     * </pre>
     * becomes
     * <pre>
     * ! This is a comment
     *         print *, 0
     * </pre>
     * and
     * <pre>
     * ! This is a comment
     * 10 print *, 0
     * </pre>
     * becomes
     * <pre>
     * ! This is a comment
     * 10      print *, 0
     * </pre>
     * 
     * In the preceding example, note that only six spaces are affixed, since the label is two
     * characters long.
     */
    public void setIndentation(String newIndentation)
    {
        getFirstTokenOnLine().setWhiteBefore(getComments() + newIndentation);

        if (label != null)
        {
            label.setWhiteBefore(getComments());
            
            int start = label.getText().length();
            int end = newIndentation.length();
            if (start < end)
                firstStmtToken.setWhiteBefore(newIndentation.substring(start, end));
            else
                firstStmtToken.setWhiteBefore(" "); //$NON-NLS-1$
        }
    }

    private String newIndentation(String currentIndentation, String removeIndent, String addIndent)
    {
        String newIndentation;
        if (removeIndent.length() > currentIndentation.length())
            newIndentation = ""; //$NON-NLS-1$
        else if (currentIndentation.startsWith(removeIndent))
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
        String result = whiteText.substring(lastCR + 1);
        if (result.equals("") && getFirstTokenOnLine() == label) //$NON-NLS-1$
        {
            whiteText = spaces(label.getText().length()) + firstStmtToken.getWhiteBefore();
            lastCR = whiteText.lastIndexOf('\n');
            result = whiteText.substring(lastCR + 1);
        }
        return result;
    }

    private String spaces(int count)
    {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++)
            sb.append(' ');
        return sb.toString();
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
        return starts(ASTProgramStmtNode.class)
            || starts(ASTFunctionStmtNode.class)
            || starts(ASTSubroutineStmtNode.class)
            || starts(ASTModuleStmtNode.class)
            || starts(ASTBlockStmtNode.class)
            || starts(ASTBlockDataStmtNode.class)
            || starts(ASTForallConstructStmtNode.class)
            || starts(ASTWhereConstructStmtNode.class)
            || starts(ASTDerivedTypeStmtNode.class)
            || starts(ASTIfThenStmtNode.class)
            || starts(ASTElseStmtNode.class)
            || starts(ASTElseWhereStmtNode.class)
            || starts(ASTElseIfStmtNode.class)
            || starts(ASTSelectCaseStmtNode.class)
            || starts(ASTSelectTypeStmtNode.class)
            || starts(ASTCaseStmtNode.class)
            || starts(ASTDoConstructNode.class)
            || starts(ASTLabelDoStmtNode.class)
            || starts(ASTInterfaceStmtNode.class)
            || starts(ASTContainsStmtNode.class)
            || starts(ASTAssociateStmtNode.class);
    }

    private boolean starts(Class<? extends IASTNode> nodeClass)
    {
        return firstStmtToken.findNearestAncestor(nodeClass) != null;
    }

    public boolean endsIndentedRegion()
    {
        Terminal t = firstStmtToken.getTerminal();
        return t == Terminal.T_CASE && !isFirstCaseStmtInSelectConstruct()
            || t == Terminal.T_CONTAINS
            || t == Terminal.T_CONTINUE // Heuristically used to end old-style DO-loops
            || t == Terminal.T_END
            || t == Terminal.T_ENDBEFORESELECT
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
            || t == Terminal.T_ELSEIF;
    }

    public boolean endsDoublyIndentedRegion()
    {
        /* SELECT CASE (i)
         *     CASE (1)
         *         PRINT *, "HI"
         * END SELECT             ! << Note that indentation decreased by *two* levels
         */
        return firstStmtToken.findNearestAncestor(ASTEndSelectStmtNode.class) != null;
    }
    
    private boolean isFirstCaseStmtInSelectConstruct()
    {
        ASTCaseConstructNode selectConstruct = firstStmtToken.findNearestAncestor(ASTCaseConstructNode.class);
        if (selectConstruct == null) return false;
        
        ASTCaseStmtNode firstCaseStmt = selectConstruct.getSelectCaseBody().findFirst(ASTCaseStmtNode.class);
        if (firstCaseStmt == null) return false;
        
        return firstStmtToken.findNearestAncestor(ASTCaseStmtNode.class) == firstCaseStmt;
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
        return getIncreasedIndentation(getIndentation());
    }

    /**
     * @return the indentation of this line, plus an additional unit of indentation
     */
    public static String getIncreasedIndentation(String currentIndentation)
    {
        return currentIndentation + defaultIndentation();
    }
    
    /**
     * @return the indentation of this line without its final unit of indentation
     */
    public String getDecreasedIndentation()
    {
        return getDecreasedIndentation(getIndentation());
    }
    
    /**
     * @return the indentation of this line without its final unit of indentation
     */
    public static String getDecreasedIndentation(String indentation)
    {
        //String indentation = getIndentation();
        if (indentation.endsWith(defaultIndentation()))
            return indentation.substring(0, indentation.length()-defaultIndentation().length());
        else if (indentation.endsWith("\t")) //$NON-NLS-1$
            return indentation.substring(0, indentation.length()-1);
        else
            return indentation;
    }

    /** @return true iff this line starts with a numeric statement label */
    public boolean hasLabel()
    {
        return label != null;
    }

    /** @return true iff this is a continuation of the statement on the previous line */
    public boolean isContinuationLine()
    {
        String whiteText = firstStmtToken.getWhiteBefore();
        if (whiteText.indexOf('!') >= 0)
            whiteText = whiteText.substring(0, whiteText.indexOf('!'));
        return whiteText.indexOf('&') >= 0;
    }
}