/*******************************************************************************
 * Copyright (c) 2007-2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import java.util.ArrayList;

import org.eclipse.photran.core.FortranAST;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

/**
 * The Reindenter is used to correct indentation when a node is inserted or
 * moved in an AST so that the affected lines are correctly indented in their
 * new context.
 *  
 * @author Jeff Overbey
 */
/*
 * This code is very specific to the way Photran's parser uses the whiteBefore and
 * whiteAfter parts of a Token; it will not generalize to parsers that, say, include
 * newlines in the whitetext.
 */
public class Reindenter
{
    public static enum Strategy
    {
        /**
         * Shifts the entire region left or right to match its new surroundings, keeping
         * the relative indentation of each line the same.
         * <p>
         * The algorithm is as follows:
         * <ul>
         *   <li> Usually, the reindented region is adjusted to match the indentation of
         *        the next non-empty line following the region.
         *   <li> However, if the next non-empty line starts with "END" (or something),
         *        the reindented region is adjusted to match the indentation of
         *        the last non-empty line above the region.
         *   <li> However, if the last non-empty line above starts with a token like
         *        "PROGRAM" or "IF," then a guess is made, and the indentation is set
         *        to match the following line, but four spaces are added.
         * </ul>
         */
        SHIFT_ENTIRE_BLOCK
        {
            @Override protected ReindentingVisitor createVisitor(Reindenter reindenter, Token firstTokenInRegion, Token lastTokenInRegion)
            {
                return reindenter.new ShiftBlockReindentingVisitor(firstTokenInRegion, lastTokenInRegion);
            }
        },
        
        /**
         * Shifts each lines in the region left or right to match its new surroundings.
         * <p>
         * The algorithm is as follows:
         * <ul>
         *   <li> Initially, the line is indented the same as the previous line.
         *   <li> If the preceding line starts with PROGRAM, IF, DO, etc., the line is
         *        indented an additional 4 spaces.
         *   <li> If the line starts with END, it is unindented 4 spaces.
         * </ul>
         */
        REINDENT_EACH_LINE
        {
            @Override protected ReindentingVisitor createVisitor(Reindenter reindenter, Token firstTokenInRegion, Token lastTokenInRegion)
            {
                return reindenter.new ReindentEachLineReindentingVisitor(firstTokenInRegion, lastTokenInRegion);
            }
        };
        
        protected abstract ReindentingVisitor createVisitor(Reindenter reindenter, Token firstTokenInRegion, Token lastTokenInRegion);
    }
    
    public static void reindent(IASTNode node, IFortranAST ast)
    {
        new Reindenter(node, ast, Strategy.SHIFT_ENTIRE_BLOCK);
    }

    public static void reindent(Token firstTokenInAffectedNode, Token lastTokenInAffectedNode, IFortranAST ast)
    {
        new Reindenter(firstTokenInAffectedNode, lastTokenInAffectedNode, ast, Strategy.SHIFT_ENTIRE_BLOCK);
    }

    public static void reindent(int fromLine, int thruLine, IFortranAST ast)
    {
        new Reindenter(fromLine, thruLine, ast, Strategy.SHIFT_ENTIRE_BLOCK);
    }
    
    public static void reindent(IASTNode node, IFortranAST ast, Strategy strategy)
    {
        new Reindenter(node, ast, strategy);
    }

    public static void reindent(Token firstTokenInAffectedNode, Token lastTokenInAffectedNode, IFortranAST ast, Strategy strategy)
    {
        new Reindenter(firstTokenInAffectedNode, lastTokenInAffectedNode, ast, strategy);
    }

    public static void reindent(int fromLine, int thruLine, IFortranAST ast, Strategy strategy)
    {
        new Reindenter(fromLine, thruLine, ast, strategy);
    }

	private final IFortranAST ast;
	private final int lineNumOfLastTokenInAST;
	
    private Reindenter(IASTNode node, IFortranAST ast, Strategy strategy)
	{
        this(node.findFirstToken(), node.findLastToken(), ast, strategy);
    }
    
    private Reindenter(int fromLine, int thruLine, IFortranAST ast, Strategy strategy)
    {
        this(ast.findFirstTokenOnLine(fromLine), ast.findLastTokenOnLine(thruLine), ast, strategy);
    }
    
    private Reindenter(Token firstTokenInRegion, Token lastTokenInRegion, IFortranAST ast, Strategy strategy)
    {
        // Recompute TokenList so that line number-based searches will be correct
        this.lineNumOfLastTokenInAST = recomputeLineColInfo(ast.getRoot());
        this.ast = new FortranAST(ast.getFile(), ast.getRoot(), new TokenList(ast.getRoot()));
        
        if (firstTokenInRegion != null && lastTokenInRegion != null)
            ast.accept(strategy.createVisitor(this, firstTokenInRegion, lastTokenInRegion));
    }

    private int recomputeLineColInfo(ASTExecutableProgramNode astRoot)
    {
        LineColComputer lcc = new LineColComputer();
        astRoot.accept(lcc);
        return lcc.line; // line number of last token
    }

    private final class LineColComputer extends ASTVisitorWithLoops
    {
        private int streamOffset = 0, line = 1, col = 1;

        @Override public void visitToken(Token token)
        {
            consider(token.getWhiteBefore());
            token.setStreamOffset(streamOffset);
            token.setLine(line);
            token.setCol(col);
            consider(token.getText());
            consider(token.getWhiteAfter());
        }

        private void consider(String s)
        {
            for (int i = 0, len = s.length(); i < len; i++)
            {
                streamOffset++;
                
                if (s.charAt(i) == '\n')
                {
                    line++;
                    col = 1;
                }
                else col++;
            }
        }
    }

    private abstract class ReindentingVisitor extends ASTVisitorWithLoops
    {
        protected final Token firstTokenInRegion;
        protected final Token lastTokenInRegion;
        protected String removeIndent, addIndent;

        protected boolean inFormatRegion = false;
        protected Token previousToken = null;

        /**
         * @param firstTokenInRegion
         * @param lastTokenInRegion
         */
        protected ReindentingVisitor(Token firstTokenInRegion, Token lastTokenInRegion)
        {
            this.lastTokenInRegion = lastTokenInRegion;
            this.firstTokenInRegion = firstTokenInRegion;
            
            //System.out.println("Indenting from " + firstTokenInRegion + " through " + lastTokenInRegion);
        }

        @Override public void visitToken(Token token)
        {
        	if (token == firstTokenInRegion)
                inFormatRegion = true;
            else if (token == lastTokenInRegion)
                inFormatRegion = false;
            
            //System.out.println(token.getTerminal() + " - " + inFormatRegion + " - " + token.getLine());
        	
            if (inFormatRegion && (previousToken == null || token.getLine() > previousToken.getLine()))
                updateIndentation(token);
                
            previousToken = token;
        }

        protected abstract void updateIndentation(Token firstTokenOnLine);

        protected String reindentedComments(String comments)
        {
            StringBuilder sb = new StringBuilder();
            for (String line : splitLines(comments))
                sb.append(reindentComment(line));
            return sb.toString();
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

        private String reindentComment(String line)
        {
            if (line.trim().equals("")) return line;
            
            int endIndex = 0;
            while (endIndex < line.length() && (line.charAt(endIndex) == ' ' || line.charAt(endIndex) == '\t'))
                endIndex++;
            
            String indentation = line.substring(0, endIndex);
            String comment = line.substring(endIndex);
            
            return newIndentation(indentation) + comment;
        }

        protected String newIndentation(String currentIndentation)
        {
            String newIndentation;
            if (currentIndentation.startsWith(removeIndent))
                newIndentation = currentIndentation.substring(removeIndent.length());
            else
                newIndentation = currentIndentation;
            
            newIndentation += addIndent;
            return newIndentation;
        }
    }
    
    private final class ShiftBlockReindentingVisitor extends ReindentingVisitor
    {
        /**
         * @param firstTokenInRegion
         * @param lastTokenInRegion
         */
        protected ShiftBlockReindentingVisitor(Token firstTokenInRegion, Token lastTokenInRegion)
        {
            super(firstTokenInRegion, lastTokenInRegion);
            
            int firstLineNumToReindent = firstTokenInRegion.getLine();
            Token firstTokenOnFirstLineToAdjust = ast.findFirstTokenOnLine(firstLineNumToReindent);
            if (firstTokenOnFirstLineToAdjust != firstTokenInRegion) firstLineNumToReindent++;
            
            Token firstTokenOnLineAfterReindentedRegion = findTokenStartingFirstNonemptyLineBelow(lastTokenInRegion.getLine());
            //if (firstTokenOnLineAfterReindentedRegion != null) System.out.println("Below: " + firstTokenOnLineAfterReindentedRegion.getTerminal() + " - " + firstTokenOnLineAfterReindentedRegion.getText() + " - " + firstTokenOnLineAfterReindentedRegion.getCol() + ";");
            
            this.removeIndent = getIndentation(firstTokenOnFirstLineToAdjust);
            this.addIndent = getIndentation(firstTokenOnLineAfterReindentedRegion);
            
            if (endsIndentedRegion(firstTokenOnLineAfterReindentedRegion))
            {
                Token firstTokenOnLineAboveReindentedRegion = findTokenStartingLastNonemptyLineAbove(firstTokenInRegion.getLine());
                //if (firstTokenOnLineAboveReindentedRegion != null) System.out.println("Above: " + firstTokenOnLineAboveReindentedRegion.getTerminal() + " - " + firstTokenOnLineAboveReindentedRegion.getText() + " - " + firstTokenOnLineAboveReindentedRegion.getCol() + ";");

                if (firstTokenOnLineAboveReindentedRegion != null && !startsIndentedRegion(firstTokenOnLineAboveReindentedRegion))
                    this.addIndent = getIndentation(firstTokenOnLineAboveReindentedRegion);
                else
                    this.addIndent = addIndent + "    ";
            }
            
            //System.out.println("Removing [" + removeIndent + "] and adding [" + addIndent + "]");
        }

        protected void updateIndentation(Token firstTokenOnLine)
        {
            String whiteBeforeFirstTok = firstTokenOnLine.getWhiteBefore();
            String currentIndentation = getIndentation(firstTokenOnLine);
            if (!whiteBeforeFirstTok.endsWith(currentIndentation)) return;
            String comments = whiteBeforeFirstTok.substring(0, whiteBeforeFirstTok.length()-currentIndentation.length());
            //System.out.println("Reindenting [" + firstTokenOnLine.getWhiteBefore() + firstTokenOnLine.getText() + "]");
            
            firstTokenOnLine.setWhiteBefore(reindentedComments(comments) + newIndentation(currentIndentation));
            //System.out.println("         to [" + firstTokenOnLine.getWhiteBefore() + firstTokenOnLine.getText() + "]");
        }
    }
    
    private final class ReindentEachLineReindentingVisitor extends ReindentingVisitor
    {
        private Token firstTokenOnPreviousLine;
        private String indentationOfPreviousLine;
        
        /**
         * @param firstTokenInRegion
         * @param lastTokenInRegion
         */
        protected ReindentEachLineReindentingVisitor(Token firstTokenInRegion, Token lastTokenInRegion)
        {
            super(firstTokenInRegion, lastTokenInRegion);
            
            int firstLineNumToReindent = firstTokenInRegion.getLine();
            Token firstTokenOnFirstLineToAdjust = ast.findFirstTokenOnLine(firstLineNumToReindent);
            if (firstTokenOnFirstLineToAdjust != firstTokenInRegion) firstLineNumToReindent++;
            
            this.firstTokenOnPreviousLine = findTokenStartingLastNonemptyLineAbove(firstTokenInRegion.getLine());
            this.indentationOfPreviousLine = getIndentation(firstTokenOnPreviousLine);
            
            this.removeIndent = indentationOfPreviousLine;
            this.addIndent = getIndentation(firstTokenOnFirstLineToAdjust);
        }

        protected void updateIndentation(Token firstTokenOnLine)
        {
            String whiteBeforeFirstTok = firstTokenOnLine.getWhiteBefore();
            String currentIndentation = getIndentation(firstTokenOnLine);
            if (!whiteBeforeFirstTok.endsWith(currentIndentation)) return;
            String comments = whiteBeforeFirstTok.substring(0, whiteBeforeFirstTok.length()-currentIndentation.length());
            //System.out.println("Reindenting [" + firstTokenOnLine.getWhiteBefore() + firstTokenOnLine.getText() + "]");

            this.removeIndent = currentIndentation;
            this.addIndent = indentationOfPreviousLine;
            if (startsIndentedRegion(firstTokenOnPreviousLine))
                this.addIndent += "    ";
            else if (endsIndentedRegion(firstTokenOnLine) && this.addIndent.endsWith("    "))
                this.addIndent = this.addIndent.substring(0, this.addIndent.length()-4);

            firstTokenOnLine.setWhiteBefore(reindentedComments(comments) + newIndentation(currentIndentation));
            //System.out.println("         to [" + firstTokenOnLine.getWhiteBefore() + firstTokenOnLine.getText() + "]");
            
            this.firstTokenOnPreviousLine = firstTokenOnLine;
            this.indentationOfPreviousLine = this.addIndent;
        }
    }

	private String getIndentation(Token token)
	{
		String indent = token == null ? "" : token.getWhiteBefore();
        if (indent == null) indent = "";
        int lastCR = indent.lastIndexOf('\n');
        if (lastCR >= 0) indent = indent.substring(lastCR + 1);
		return indent;
	}

    private Token findTokenStartingFirstNonemptyLineBelow(int startLine)
    {
        for (int line = startLine + 1; line <= lineNumOfLastTokenInAST; line++)
        {
            Token firstBlockOnLine = ast.findFirstTokenOnLine(line);
            if (firstBlockOnLine != null) return firstBlockOnLine;
        }
        return null;
    }

    private Token findTokenStartingLastNonemptyLineAbove(int startLine)
    {
        for (int line = startLine - 1; line >= 0; line--)
        {
            Token firstBlockOnLine = ast.findFirstTokenOnLine(line);
            if (firstBlockOnLine != null) return firstBlockOnLine;
        }
        return null;
    }

    private boolean startsIndentedRegion(Token token)
    {
        if (token == null) return false;
        
        Terminal t = token.getTerminal();
        return t == Terminal.T_PROGRAM
            || t == Terminal.T_FUNCTION
            || t == Terminal.T_SUBROUTINE
            || t == Terminal.T_MODULE
            || t == Terminal.T_BLOCK
            || t == Terminal.T_BLOCKDATA
            //|| t == Terminal.T_TYPE
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
            || t == Terminal.T_CONTAINS
            || t == Terminal.T_ASSOCIATE;
    }

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
}
