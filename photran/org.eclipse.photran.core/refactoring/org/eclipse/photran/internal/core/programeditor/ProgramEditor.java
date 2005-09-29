package org.eclipse.photran.internal.core.programeditor;

import java.util.List;

import org.eclipse.photran.core.programrepresentation.ParseTreePres;
import org.eclipse.photran.core.programrepresentation.Presentation;
import org.eclipse.photran.internal.core.f95parser.IPresentationBlock;
import org.eclipse.photran.internal.core.f95parser.NonTreeToken;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeSearcher;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.programeditor.blockplacer.AbstractBlockPlacer;

/**
 * The <code>ProgramEditor</code> makes changes to the parse tree and repositions tokens in the
 * <code>Presentation</code> object accordingly.
 * 
 * @author joverbey
 */
public class ProgramEditor
{
    // ----- CHANGE TOKEN TEXT ------------------------------------------------
    
    /**
     * Changes the text of a token.  The token's original text (as well as its new
     * text) may NOT contain newline characters; we want to be assured that we are
     * only affecting a single line in the source code.
     * 
     * @param parseTree
     * @param presentation
     * @param targetToken
     * @param newText
     */
    public static void changeTokenText(ParseTreePres program, final Token targetToken, final String newText)
    {
        if (targetToken.getText().indexOf("\n") >= 0
            || newText.indexOf("\n") >= 0
            || targetToken.getStartLine() != targetToken.getEndLine())
            throw new Error("changeTokenText cannot be applied to tokens containing newline characters");
        
        final int targetTokenLine = targetToken.getStartLine();
        final int targetTokenEndCol = targetToken.getEndCol();
        
        final int oldLength = targetToken.getLength();
        final int newLength = newText.length();
        
        // Tokens to the right of this one will be shifted by this many characters
        final int colShiftAmount = newLength - oldLength;
        
        targetToken.setLength(newLength);
        targetToken.setEndCol(targetTokenEndCol + colShiftAmount);
        targetToken.setText(newText);
        
        program.getPresentation().shiftContentsOnLine(targetTokenLine, targetTokenEndCol, 0, colShiftAmount);

        ParseTreeEditor.shiftParseTreeContents(program.getParseTree(), targetTokenLine, targetTokenEndCol, 0, colShiftAmount);
    }
    
    // ----- CUT --------------------------------------------------------------
    
    /**
     * Removes a subtree (and related non-tree tokens) from a program, returning them
     * as a <code>PartialProgram</code>.
     */
    public static PartialProgram cutSubtree(ParseTreePres program, ParseTreeNode subtreeToRemove)
    {
        ParseTreeNode parseTree = program.getParseTree();
        Presentation pres = program.getPresentation();
        
        ParseTreeNode parentNode = ParseTreeSearcher.findParent(parseTree, subtreeToRemove);
        if (parentNode == null) return null;
        
        ListRange presRange = pres.determineWhichNonTreeTokensCorrespondTo(subtreeToRemove);
        List/*<NonTreeToken>*/ nonTreeTokensToRemove = pres.getNonTreeTokens(presRange);
        
        TextRange textAreaToCut = determineTextRangeForCut(program, subtreeToRemove, presRange);
        
        PartialProgram result = new PartialProgram(subtreeToRemove, nonTreeTokensToRemove, textAreaToCut);
        
        parentNode.getChildren().remove(subtreeToRemove);
        for (int i = presRange.getLastIndex(); i >= presRange.getFirstIndex(); i--)
            pres.getNonTreeTokens().remove(i);
        
        shiftTokensInParseTree(parseTree, textAreaToCut);
        shiftTokensInPresentation(pres, textAreaToCut);
        
        result.normalizeTokenPositions(textAreaToCut);
        
        return result;
    }

    private static TextRange determineTextRangeForCut(ParseTreePres program, ParseTreeNode subtreeToRemove, ListRange nonTreeTokensToRemove)
    {
        return determineOccupiedArea(subtreeToRemove, program.getPresentation(), nonTreeTokensToRemove);
        
        // FIXME-Jeff: Include whitespace at beginning of line and blank lines above, if any, and end of line if last token on line
    }
    
    /**
     * Determins the textual area occupied by the given (sub-)tree together with the given range of presentation
     * tokens.
     * 
     * @param tree not null
     * @param pres not null
     * @param presRange possibly null (for entire Presentation)
     * @return
     */
    public static TextRange determineOccupiedArea(ParseTreeNode tree, Presentation pres, ListRange presRange)
    {
        if (presRange == null) presRange = new ListRange(0, pres.size()-1);
        
        Token firstTokenInTree = ParseTreeSearcher.findFirstTokenIn(tree);
        NonTreeToken firstTokenInPres = pres.getNonTreeToken(presRange.getFirstIndex());
        IPresentationBlock firstBlock = PresBlockUtil.whicheverComesFirst(firstTokenInTree, firstTokenInPres);
        
        Token lastTokenInTree = ParseTreeSearcher.findLastTokenIn(tree);
        NonTreeToken lastTokenInPres = pres.getNonTreeToken(presRange.getLastIndex());
        IPresentationBlock lastBlock = PresBlockUtil.whicheverComesFirst(lastTokenInTree, lastTokenInPres);
        
        return new TextRange(firstBlock, lastBlock);
    }
    
    /**
     * Determins the textual area occupied by the given combination of parse tree and <code>Presentation</code>.
     * 
     * @param tree not null
     * @param pres not null
     * @param presRange possibly null (for entire Presentation)
     * @return
     */
    public static TextRange determineOccupiedArea(ParseTreePres p)
    {
        return determineOccupiedArea(p.getParseTree(), p.getPresentation(), null);
    }
    
    public static boolean isFirstBlockOnLine(ParseTreePres program, IPresentationBlock targetBlock)
    {
        int targetLine = targetBlock.getStartLine();
        
        Token firstTokenOnLine = ParseTreeSearcher.findFirstTokenOnLineIn(targetLine, program.getParseTree());
        NonTreeToken firstNonTreeTokenOnLine = program.getPresentation().findFirstNonTreeTokenOnLine(targetLine);
        IPresentationBlock firstBlockOnLine = PresBlockUtil.whicheverComesFirst(firstTokenOnLine, firstNonTreeTokenOnLine);
        
        return targetBlock == firstBlockOnLine;
    }

    static void shiftTokensInParseTree(ParseTreeNode parseTree, TextRange cutArea)
    {
        int colShiftAmount = -(cutArea.getLastCol() - cutArea.getFirstCol());
        int lineShiftAmount = -(cutArea.getLastLine() - cutArea.getFirstLine());
        
        int lineToStartShiftingOn = cutArea.getLastLine();
        int colToStartShiftingAfter = cutArea.getLastCol();
        
        ParseTreeEditor.shiftParseTreeContents(parseTree, lineToStartShiftingOn, colToStartShiftingAfter, lineShiftAmount, colShiftAmount);
    }

    private static void shiftTokensInPresentation(Presentation pres, TextRange cutArea)
    {
        int colShiftAmount = -(cutArea.getLastCol() - cutArea.getFirstCol());
        int lineShiftAmount = -(cutArea.getLastLine() - cutArea.getFirstLine());
        
        int lineToStartShiftingOn = cutArea.getLastLine();
        int colToStartShiftingAfter = cutArea.getLastCol();
        
        pres.shiftContentsOnLine(lineToStartShiftingOn, colToStartShiftingAfter, lineShiftAmount, colShiftAmount);
    }
    
    // ----- PASTE ------------------------------------------------------------
    
    /**
     * Inserts a (normalized) <code>PartialProgram</code> into a <code>ParseTreePres</code>
     * at the given location.
     */
    public static boolean pasteSubtree(ParseTreePres program, PartialProgram ppToInsert, ParseTreeNode newParent, int insertIndex, AbstractBlockPlacer blockPlacer)
    {
        if (program == null || ppToInsert == null || newParent == null)
            throw new Error("Null argument passed to pasteSubtree");
        
        ParseTreeNode parseTree = program.getParseTree();
        Presentation pres = program.getPresentation();
        
        if (!ParseTreeSearcher.contains(parseTree, newParent))
            throw new Error("Prospective parent does not exist in program parse tree");
        if (insertIndex < 0 || insertIndex > newParent.getChildren().size())
            throw new Error("Insertion index is out of range");
        
        int newPresStartIndex = blockPlacer.prepareForInsertion(program, ppToInsert, newParent, insertIndex);
        
        newParent.getChildren().add(insertIndex, ppToInsert.getParseTree());
        pres.addAll(newPresStartIndex, ppToInsert.getPresentation().getNonTreeTokens());
        
        blockPlacer.format(program, ppToInsert.getParseTree(), new ListRange(newPresStartIndex, newPresStartIndex + ppToInsert.getPresentation().size() - 1));
        
        return true;
    }
}
