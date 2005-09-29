package org.eclipse.photran.core.programrepresentation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.photran.internal.core.f95parser.NonTreeToken;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeSearcher;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.programeditor.ListRange;

/**
 * A <code>Presentation</code> object is kept alongside the parse tree for a program. The
 * <code>Presentation</code> is used to hold all of the comments, line continuations, and other
 * things that are in the source code but not in the parse tree. These are stored in the
 * <code>Presentation</code> as <code>PresentationBlock</code>s.
 * 
 * @author joverbey
 */
public class Presentation // implements Iterable/*<NonTreeToken>*/
{
    private List/* <NonTreeToken> */presBlocks;

    public Presentation()
    {
        this.presBlocks = new LinkedList/* <NonTreeToken> */();
    }

    public Presentation(List/* <NonTreeToken> */presBlocks)
    {
        this.presBlocks = presBlocks;
    }

    public void add(NonTreeToken block)
    {
        presBlocks.add(block);
    }

    public void addAll(int startIndex, List/*<NonTreeToken>*/ newBlocks)
    {
        presBlocks.addAll(startIndex, newBlocks);
    }

    public List/* <NonTreeToken> */getNonTreeTokens()
    {
        return presBlocks;
    }

    public List/* <NonTreeToken> */getNonTreeTokens(ListRange range)
    {
        if (range.isEmpty())
            return new LinkedList();
        else
            return presBlocks.subList(range.getFirstIndex(), range.getLastIndex() + 1);
    }

    public NonTreeToken getNonTreeToken(int index)
    {
        if (index < 0 || index >= presBlocks.size())
            return null;
        else
            return (NonTreeToken)presBlocks.get(index);
    }

    public Iterator/* <NonTreeToken> */iterator()
    {
        return presBlocks.iterator();
    }

    /**
     * Shifts all blocks to the right of <code>targetTokenCol</code> on line
     * <code>targetTokenLine</code> forward by <code>colShiftAmount</code> characters and
     * <code>lineShiftAmount</code> lines. (Make the shift values negative to move them backward.)
     * 
     * @param lineToStartShiftingOn
     * @param colToStartShiftingAfter
     * @param lineShiftAmount
     * @param colShiftAmount
     * @return index of first token shifted
     */
    public int shiftContentsOnLine(int lineToStartShiftingOn, int colToStartShiftingAfter,
        int lineShiftAmount, int colShiftAmount)
    {
        int index = -1, indexOfFirstTokenShifted = -1;
        
        Iterator/* <NonTreeToken> */it = presBlocks.iterator();
        while (it.hasNext())
        {
            NonTreeToken blk = (NonTreeToken)it.next();
            index++;
            if (blk.getStartLine() >= lineToStartShiftingOn)
            {
                // Ignoring offsets

                if (blk.getStartLine() == lineToStartShiftingOn
                    && blk.getStartCol() > colToStartShiftingAfter)
                {
                    blk.setStartCol(blk.getStartCol() + colShiftAmount);
                    blk.setEndCol(blk.getEndCol() + colShiftAmount);
                }

                blk.setStartLine(blk.getStartLine() + lineShiftAmount);
                blk.setEndLine(blk.getEndLine() + lineShiftAmount);
                
                if (indexOfFirstTokenShifted == -1)
                    indexOfFirstTokenShifted = index;
            }
        }
        
        return indexOfFirstTokenShifted == -1 ? 0 : indexOfFirstTokenShifted;
    }

    public ListRange determineWhichNonTreeTokensCorrespondTo(ParseTreeNode subtreeToRemove)
    {
        Token firstTokenInTree = ParseTreeSearcher.findFirstTokenIn(subtreeToRemove);
        Token lastTokenInTree = ParseTreeSearcher.findLastTokenIn(subtreeToRemove);

        int index = -1, firstIndex = 0, lastIndex = -1;

        Iterator/* <NonTreeToken> */it = presBlocks.iterator();
        while (it.hasNext())
        {
            NonTreeToken blk = (NonTreeToken)it.next();
            index++;

            if (blk.getOffset() >= firstTokenInTree.getOffset())
            {
                firstIndex = index;
                break;
            }
        }

        while (it.hasNext())
        {
            NonTreeToken blk = (NonTreeToken)it.next();
            index++;

            if (blk.getOffset() >= lastTokenInTree.getOffset() + lastTokenInTree.getLength())
            {
                lastIndex = index - 1;
                break;
            }
        }

        return new ListRange(firstIndex, lastIndex);

        // FIXME-Jeff: Include comments above, cpp directives, etc.
        // FIXME-Jeff: TEST THIS!
    }

    /**
     * Scans the list of non-tree tokens and finds the first which occurs on the given line. Returns
     * <code>null</code> if there are no non-tree tokens on the given line.
     * 
     * @param targetLine
     * @return <code>NonTreeToken</code> or <code>null</code>
     */
    public NonTreeToken findFirstNonTreeTokenOnLine(int targetLine)
    {
        Iterator/* <NonTreeToken> */it = presBlocks.iterator();
        while (it.hasNext())
        {
            NonTreeToken blk = (NonTreeToken)it.next();
            if (blk.getStartLine() == targetLine)
                return blk;
            else if (blk.getStartLine() > targetLine) return null;
        }
        return null;
    }

    public int size()
    {
        return presBlocks.size();
    }
}
