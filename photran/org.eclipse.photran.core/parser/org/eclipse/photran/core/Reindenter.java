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
 * 
 * This code is very specific to the way Photran's parser uses the whiteBefore and
 * whiteAfter parts of a Token; it will not generalize to parsers that, say, include
 * newlines in the whitetext.
 *  
 * @author Jeff Overbey
 */
public class Reindenter
{
    private Reindenter() {}

    public static void reindent(ParseTreeNode node, ASTExecutableProgramNode entireAST)
    {
        recomputeLineColInfo(entireAST);
        
        final Token firstToken = findFirstTokenIn(node); if (firstToken == null) return;
        final Token lastToken = findLastTokenIn(node);
        
        System.out.println("First token in region: " + firstToken.getText());
        System.out.println("Last token in region: " + lastToken.getText());
        
        int startLine = getLine(firstToken);
        Token tokenStartingLine = findTokenStartingLine(startLine, entireAST);
        System.out.println("First token on line: " + tokenStartingLine.getText());
        System.out.println("Eq? " + (tokenStartingLine == firstToken));
        if (tokenStartingLine != firstToken) startLine++;
        System.out.println("Start line: " + startLine);
        
        Token firstTokenAbove = findTokenStartingLastNonemptyLineAbove(startLine, entireAST);
        System.out.println("Token above: " + firstTokenAbove.getText());
        
        int indentSize = 0;
        indentSize = Math.max(getColumn(firstTokenAbove)-1, 0);
        if (startsIndentedRegion(firstTokenAbove)) indentSize += 4;
        indentSize -= getUnindentAmount(node);
        System.out.println("Indent size: " + indentSize);
        
        final int indentAmount = indentSize;
        
        entireAST.visitUsing(new GenericParseTreeVisitor()
        {
            boolean inFormatRegion = false;
            Token previousToken = null;
            
            public void visitToken(Token token)
            {
                if (token == firstToken)
                    inFormatRegion = true;
                else if (token == lastToken)
                {
                    inFormatRegion = false;
                    System.out.println("left");
                }
                
                if (inFormatRegion && getLine(token) > getLine(previousToken))
                    changeWhitetext(token, previousToken, indentAmount);
                    
                previousToken = token;
            }

            private void changeWhitetext(Token firstTokenOnLine, Token previousToken, int indentAmount)
            {
                if (indentAmount == 0) return;
                if (lineIsEmpty(firstTokenOnLine, previousToken)) return;
                
                String spaces = spaces(indentAmount);
                
                String whiteAfterPrevTok = previousToken.getWhiteAfter();
                String whiteBeforeFirstTok = firstTokenOnLine.getWhiteBefore();
                
                if (indentAmount > 0)
                    firstTokenOnLine.setWhiteBefore(whiteBeforeFirstTok + spaces);
                else if (indentAmount < 0 && whiteAfterPrevTok.endsWith(spaces))
                    previousToken.setWhiteAfter(whiteAfterPrevTok.substring(0, whiteAfterPrevTok.length() + indentAmount));
                else if (indentAmount < 0 && whiteBeforeFirstTok.endsWith(spaces))
                    firstTokenOnLine.setWhiteBefore(whiteBeforeFirstTok.substring(0, whiteBeforeFirstTok.length() + indentAmount));
            }

            private boolean lineIsEmpty(Token firstTokenOnLine, Token previousToken)
            {
                // TODO: lineIsEmpty()
                return false;
            }

            private String spaces(int indentAmount)
            {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < Math.abs(indentAmount); i++)
                    sb.append(' ');
                String spaces = sb.toString();
                return spaces;
            }
        });
    }

    private static void recomputeLineColInfo(ASTExecutableProgramNode ast)
    {
        ast.visitUsing(new GenericParseTreeVisitor()
        {
            int line = 1, col = 1;
            
            public void visitToken(Token token)
            {
                update(token.getWhiteBefore());
                token.setAdapter(LineCol.class, new LineCol(line, col));
                update(token.getText());
                update(token.getWhiteAfter());
            }

            private void update(String s)
            {
                for (int i = 0, len = s.length(); i < len; i++)
                {
                    if (s.charAt(i) == '\n')
                    {
                        line++;
                        col = 1;
                    }
                    else col++;
                }
            }
        });
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

    private static boolean startsIndentedRegion(Token token)
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
            || t == Terminal.T_INTERFACE
            || t == Terminal.T_CONTAINS;
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

    private static Token findFirstTokenIn(final ParseTreeNode node)
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

    private static Token findLastTokenIn(final ParseTreeNode node)
    {
        return new Object()
        {
            private Token lastToken;
            
            public Token getLastToken()
            {
                node.visitUsing(new GenericParseTreeVisitor()
                {
                    public void visitToken(Token token)
                    {
                        lastToken = token;
                    }
                });

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

    private static Token findTokenStartingLine(final int lineNum, final ParseTreeNode node)
    {
        try
        {
            node.visitUsing(new GenericParseTreeVisitor()
            {
                public void visitToken(Token token)
                {
                    int tokenLine = getLine(token);
                    if (tokenLine == lineNum) throw new Notification(token);
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
