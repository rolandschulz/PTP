/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.util.Notification;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

/**
 * The Reindenter is used to correct indentation when a node is pasted into an AST
 * so that the pasted lines are correctly indented in their new context.
 * <p>
 * This code is very specific to the way Photran's parser uses the whiteBefore and
 * whiteAfter parts of a Token; it will not generalize to parsers that, say, include
 * newlines in the whitetext.
 *  
 * @author Jeff Overbey
 */
public class Reindenter
{
	public static void reindent(IASTNode node, IFortranAST ast)
    {
    	new Reindenter(node, ast);
    }

	private IFortranAST ast;
	private int lastLine;
	
    private Reindenter(IASTNode node, IFortranAST ast)
	{
    	this.ast = ast;
    	
        this.lastLine = recomputeLineColInfo();
        
        final Token firstToken = findFirstTokenIn(node); if (firstToken == null) return;
        final Token lastToken = findLastTokenIn(node);
        
        int startLine = firstToken.getLine();
        Token tokenStartingLine = ast.findFirstTokenOnLine(startLine);
        if (tokenStartingLine != firstToken) startLine++;
        
        Token firstTokenBelow = findTokenStartingFirstNonemptyLineBelow(lastToken.getLine()-1);
        //System.out.println("Below: " + firstTokenBelow.getTerminal() + " - " + firstTokenBelow.getText() + " - " + firstTokenBelow.getCol() + ";");
        
//        int indentSize = 0;
//        indentSize = firstTokenBelow == null ? 0 : Math.max(firstTokenBelow.getCol()-1, 0);
//        if (endsIndentedRegion(firstTokenBelow)) indentSize = Math.max(indentSize-4, 0);
//        indentSize -= getUnindentAmount(node);
//        
//        final int indentAmount = indentSize;
        
        final String indent = determineIndent(firstTokenBelow);
        
        ast.accept(new ASTVisitor()
        {
            private boolean inFormatRegion = false;
            private Token previousToken = null;

        	@Override public void visitToken(Token token)
			{
	        	if (token == firstToken)
	                inFormatRegion = true;
	            else if (token == lastToken)
	                inFormatRegion = false;
	            
	            //System.out.println(token.getTerminal() + " - " + inFormatRegion + " - " + token.getLine());
	        	
	            if (inFormatRegion && (previousToken == null || token.getLine() > previousToken.getLine()))
	                updateIndentationInWhitetext(token, previousToken, indent /*indentAmount*/);
	                
	            previousToken = token;
			}
        });
    }

	private String determineIndent(Token firstTokenBelow)
	{
		String indent = firstTokenBelow == null ? "" : firstTokenBelow.getWhiteBefore();
        if (indent == null) indent = "";
        int lastCR = indent.lastIndexOf('\n');
        if (lastCR >= 0) indent = indent.substring(lastCR + 1);
        if (endsIndentedRegion(firstTokenBelow))
        {
        	if (indent.length() >= 4)
        		indent = indent.substring(0, indent.length()-4);
        	else
        		indent = "";
        }
		return indent;
	}

    private void updateIndentationInWhitetext(Token firstTokenOnLine, Token previousToken, String indent)
    {
        if (indent == null || indent.equals("")) return;
        if (lineIsEmpty(firstTokenOnLine, previousToken)) return;
        
        //String whiteAfterPrevTok = previousToken.getWhiteAfter();
        String whiteBeforeFirstTok = firstTokenOnLine.getWhiteBefore();
        
        firstTokenOnLine.setWhiteBefore(whiteBeforeFirstTok + indent);
        
//        if (indentAmount > 0)
//            firstTokenOnLine.setWhiteBefore(whiteBeforeFirstTok + spaces);
//        else if (indentAmount < 0 && whiteAfterPrevTok.endsWith(spaces))
//            previousToken.setWhiteAfter(whiteAfterPrevTok.substring(0, whiteAfterPrevTok.length() + indentAmount));
//        else if (indentAmount < 0 && whiteBeforeFirstTok.endsWith(spaces))
//            firstTokenOnLine.setWhiteBefore(whiteBeforeFirstTok.substring(0, whiteBeforeFirstTok.length() + indentAmount));
    }

    private boolean lineIsEmpty(Token firstTokenOnLine, Token previousToken)
    {
        // TODO: lineIsEmpty()
        return false;
    }

//    private String spaces(int indentAmount)
//    {
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < Math.abs(indentAmount); i++)
//            sb.append(' ');
//        String spaces = sb.toString();
//        return spaces;
//    }

    private int recomputeLineColInfo()
    {
    	LineColComputer lcc = new LineColComputer();
    	ast.accept(lcc);
    	return lcc.line; // line number of last token
    }

    private final class LineColComputer extends ASTVisitor
    {
		private int line = 1, col = 1;

    	@Override public void visitToken(Token token)
		{
            update(token.getWhiteBefore());
            token.setLine(line);
            token.setCol(col);
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
	}

    private Token findTokenStartingFirstNonemptyLineBelow(int startLine)
    {
        for (int line = startLine + 1; line <= lastLine; line++)
        {
            Token firstBlockOnLine = ast.findFirstTokenOnLine(line);
            if (firstBlockOnLine != null) return firstBlockOnLine;
        }
        return null;
    }

//    private boolean startsIndentedRegion(Token token)
//    {
//        if (token == null) return false;
//        
//        Terminal t = token.getTerminal();
//        return t == Terminal.T_PROGRAM
//            || t == Terminal.T_FUNCTION
//            || t == Terminal.T_SUBROUTINE
//            || t == Terminal.T_MODULE
//            || t == Terminal.T_BLOCK
//            || t == Terminal.T_BLOCKDATA
//            //|| t == Terminal.T_TYPE
//            || t == Terminal.T_FORALL
//            || t == Terminal.T_WHERE
//            || t == Terminal.T_ELSE
//            || t == Terminal.T_ELSEWHERE
//            || t == Terminal.T_IF
//            || t == Terminal.T_ELSEIF
//            || t == Terminal.T_SELECTCASE
//            || t == Terminal.T_SELECT
//            || t == Terminal.T_CASE
//            || t == Terminal.T_DO
//            || t == Terminal.T_INTERFACE
//            || t == Terminal.T_CONTAINS;
//    }

    private boolean endsIndentedRegion(Token token)
    {
        if (token == null) return false;
        
        Terminal t = token.getTerminal();
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

//    /**
//     * @return the number of characters by which the source code in this node needs to be shifted
//     * to the left so that the leftmost token will begin on column one.
//     */
//    private int getUnindentAmount(IASTNode node)
//    {
//        return Math.max(getStartColOfLeftmostBlockIn(node) - 1, 0);
//    }

    private Token findFirstTokenIn(final IASTNode node)
    {
        try
        {
            node.accept(new ASTVisitor()
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

    private Token findLastTokenIn(final IASTNode node)
    {
        return new Object()
        {
            private Token lastToken;
            
            public Token getLastToken()
            {
                node.accept(new ASTVisitor()
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

//    /**
//     * @return the leftmost column at which a token in this PartialProgram is positioned
//     * (useful for unindenting code)
//     */
//    private int getStartColOfLeftmostBlockIn(final IASTNode node)
//    {
//        return new Object()
//        {
//            int minCol = Integer.MAX_VALUE;
//
//            int getMinCol()
//            {
//                node.visitUsing(new GenericParseTreeVisitor()
//                {
//                    public void visitToken(Token token)
//                    {
//                        minCol = Math.min(token.getCol(), minCol);
//                    }
//                });
//
//                return minCol == Integer.MAX_VALUE ? 0 : minCol;
//            }
//        }.getMinCol();
//    }
}
