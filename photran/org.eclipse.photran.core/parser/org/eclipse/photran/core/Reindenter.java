package org.eclipse.photran.core;

import org.eclipse.photran.core.util.LineCol;
import org.eclipse.photran.core.util.Notification;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.ParseTreeNode;
import org.eclipse.photran.internal.core.parser.Terminal;

/**
 * The Reindenter is used to correct indentation when a node is pasted into an AST
 * so that the pasted lines are correctly indented in their new context.

 * @author Jeff Overbey
 */
public class Reindenter
{
    private Reindenter() {}

    public static void reindent(ParseTreeNode node, ASTExecutableProgramNode entireAST)
    {
        final Token firstToken = findFirstTokenIn(node); if (firstToken == null) return;
        final Token lastToken = findLastTokenIn(node);
        
        int startLine = getLine(firstToken);
        
        Token tokenStartingLine = findTokenStartingLine(startLine, entireAST);
        if (tokenStartingLine != firstToken) startLine++;
        
        Token firstTokenAbove = findTokenStartingLastNonemptyLineAbove(startLine, node);
        
        int indentSize = 0;
        if (startsIndentedRegion(firstTokenAbove))
            indentSize = 4;
        else
            indentSize = Math.max(getColumn(firstTokenAbove)-1, 0);
        indentSize -= getUnindentAmount(node);
        
        final int indentAmount = indentSize;
        
        new GenericParseTreeVisitor()
        {
            boolean inFormatRegion = false;
            Token previousToken = null;
            
            public void visitToken(Token token)
            {
                if (token == firstToken)
                    inFormatRegion = true;
                else if (token == lastToken)
                    inFormatRegion = false;
                
                if (inFormatRegion && getLine(token) > getLine(previousToken))
                    changeWhiteAfter(previousToken, indentAmount);
                
                previousToken = token;
            }

            private void changeWhiteAfter(Token token, int indentAmount)
            {
                if (indentAmount == 0) return;
                
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < Math.abs(indentAmount); i++)
                    sb.append(' ');
                String spaces = sb.toString();
                
                String whiteAfter = token.getWhiteAfter();
                if (indentAmount > 0)
                    token.setWhiteAfter(whiteAfter + spaces);
                else if (indentAmount < 0 && whiteAfter.endsWith(spaces))
                    token.setWhiteAfter(whiteAfter.substring(0, whiteAfter.length() + indentAmount));
            }
        };
    }

    private static Token findTokenStartingLastNonemptyLineAbove(int startLine, ParseTreeNode inNode)
    {
        for (int line = startLine - 1; line >= 1; line--)
        {
            Token firstBlockOnLine = findTokenStartingLine(line, inNode);
            if (firstBlockOnLine != null) return firstBlockOnLine;
        }
        return null;
    }

    protected static boolean startsIndentedRegion(Token token)
    {
        Terminal t = token.getTerminal();
        return t == Terminal.T_PROGRAM
            || t == Terminal.T_FUNCTION
            || t == Terminal.T_SUBROUTINE
            || t == Terminal.T_MODULE
            || t == Terminal.T_BLOCK
            || t == Terminal.T_BLOCKDATA
            || t == Terminal.T_TYPE
            || t == Terminal.T_FORALL
            || t == Terminal.T_WHERE
            || t == Terminal.T_ELSE
            || t == Terminal.T_ELSEWHERE
            || t == Terminal.T_IF
            || t == Terminal.T_ELSEIF
            || t == Terminal.T_SELECTCASE
            || t == Terminal.T_SELECT
            || t == Terminal.T_CASE
            || t == Terminal.T_DO
            || t == Terminal.T_INTERFACE;
    }

    private static int getLine(Token token)
    {
        return ((LineCol)token.getAdapter(LineCol.class)).getLine();
    }

    private static int getColumn(Token token)
    {
        return ((LineCol)token.getAdapter(LineCol.class)).getCol();
    }

    /**
     * @return the number of characters by which the source code in this node needs to be shifted
     * to the left so that the leftmost token will begin on column one.
     */
    private static int getUnindentAmount(ParseTreeNode node)
    {
        return Math.max(getStartColOfLeftmostBlockIn(node) - 1, 0);
    }

    public static Token findFirstTokenIn(final ParseTreeNode node)
    {
        try
        {
            node.visitUsing(new GenericParseTreeVisitor()
            {
                public void visitToken(Token token)
                {
                    throw new Notification(token);
                }
            });
        }
        catch (Notification n)
        {
            return (Token)n.getResult();
        }
        return null;
    }

    protected static Token findLastTokenIn(ParseTreeNode node)
    {
        return new GenericParseTreeVisitor()
        {
            private Token lastToken;
            
            public void visitToken(Token token)
            {
                lastToken = token;
            }
            
            public Token getLastToken()
            {
                return lastToken;
            }
        }.getLastToken();
    }

    /**
     * @return the leftmost column at which a token in this PartialProgram is positioned
     * (useful for unindenting code)
     */
    private static int getStartColOfLeftmostBlockIn(final ParseTreeNode node)
    {
        return new Object()
        {
            int minCol = Integer.MAX_VALUE;

            int getMinCol()
            {
                node.visitUsing(new GenericParseTreeVisitor()
                {
                    public void visitToken(Token token)
                    {
                        int col = getColumn(token);
                        minCol = Math.min(col, minCol);
                    }
                });

                return minCol == Integer.MAX_VALUE ? 0 : minCol;
            }
        }.getMinCol();
    }

    public static Token findTokenStartingLine(final int lineNum, final ParseTreeNode node)
    {
        try
        {
            node.visitUsing(new GenericParseTreeVisitor()
            {
                public void visitToken(Token token)
                {
                    int tokenLine = getLine(token);
                    if (tokenLine >= lineNum) throw new Notification(token);
                }
            });
        }
        catch (Notification n)
        {
            return (Token)n.getResult();
        }
        return null;
    }
}
