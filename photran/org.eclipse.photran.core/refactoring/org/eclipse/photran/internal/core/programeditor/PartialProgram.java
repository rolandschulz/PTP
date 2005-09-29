package org.eclipse.photran.internal.core.programeditor;

import java.util.List;

import org.eclipse.photran.core.programrepresentation.ParseTreePres;
import org.eclipse.photran.core.programrepresentation.Presentation;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;

/**
 * Represents a part of a program which will eventually be added into a complete program.
 * 
 * @author joverbey
 */
public class PartialProgram extends ParseTreePres
{
    protected TextRange textRange;
    protected boolean isNormalized = false;
    
    public PartialProgram(ParseTreeNode parseTree, List/*<NonTreeToken>*/blocks, TextRange textRange)
    {
        super(parseTree, new Presentation(blocks));
        this.textRange = textRange;
    }

//    public List/* <PresentationBlock> */getPresBlocks()
//    {
//        return presBlocks;
//    }
    
    public TextRange getTextRange()
    {
        return textRange;
    }

//    public void prependBlock(NonTreeToken block)
//    {
//        presBlocks.add(0, block);
//    }
//    
//    public void appendBlock(NonTreeToken block)
//    {
//        presBlocks.add(block);
//    }

    /**
     * If a partial program was cut starting at, say, line 16
     * column 2, this method will (1) shift all of the tokens
     * on line 16 left by one position, then (2) shift all of
     * the tokens' line numbers down by 15.  Effectively, it
     * shifts the tokens so that the first starts at line 1,
     * column 1. 
     * 
     * @param cutArea
     */
    public void normalizeTokenPositions(TextRange cutArea)
    {
        int colShiftAmount = -(cutArea.getFirstCol() - 1);
        int lineShiftAmount = -(cutArea.getFirstLine() - 1);
        
        ParseTreeEditor.shiftParseTreeContents(parseTree, 1, 1, lineShiftAmount, colShiftAmount);
        presentation.shiftContentsOnLine(1, 1, lineShiftAmount, colShiftAmount);
        
        isNormalized = true;
    }
    
    /**
     * @return true iff <code>normalizeTokenPositions</code> has been called.
     * A normalized partial program is one where all tokens are placed relative
     * to line 1, column 1.
     */
    public boolean isNormalized()
    {
        return isNormalized;
    }
}
