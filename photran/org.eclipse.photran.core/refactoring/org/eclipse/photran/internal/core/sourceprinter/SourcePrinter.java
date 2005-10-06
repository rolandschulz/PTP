package org.eclipse.photran.internal.core.sourceprinter;

import java.io.PrintStream;
import java.util.Iterator;

import org.eclipse.photran.core.programrepresentation.ParseTreePres;
import org.eclipse.photran.internal.core.f95parser.IPresentationBlock;

/**
 * A <code>SourcePrinter</code> takes
 * <ul>
 * <li>a <code>Presentation</code> object, which keeps track of things that don't appear in the
 * parse tree, such as comments and line continuations; and
 * <li>a reference to a parse tree
 * </ul>
 * and produces user-presentable source code from them. It blindly uses the line and column numbers
 * stored in the parse tree tokens and presentation blocks to decide where each block belongs in the
 * output. It is the responsibility of the <code>ProgramEditor</code> (or anything else that
 * changes tokens, presentation blocks, or the parse tree) to make sure that all of the tokens and
 * presentation blocks have their text and position information set correctly.
 */
public class SourcePrinter
{
    private ParseTreePres parseTreePres;

    private int curRow;
    private int curCol;

    public SourcePrinter(ParseTreePres parseTreePres)
    {
        this.parseTreePres = parseTreePres;
    }

    /**
     * Produces source code from the parse tree and associated <code>Presentation</code> object
     * supplied to the constructor.
     * 
     * @param printStream
     */
    public void printOn(PrintStream printStream)
    {
        curRow = 1;
        curCol = 1;

        Iterator it = this.parseTreePres.iterator();
        while (it.hasNext())
            printBlock((IPresentationBlock)it.next(), printStream);
    }

    /**
     * Moves to the correct line and column, prints the given presentation block, and updates
     * <code>curLine</code> and <code>curCol</code> to reflect the new position in the output
     * stream.
     * 
     * NOTE: The merge operation is expensive, as all of the tokens in the parse tree are duplicated
     * into a list. The alternative, if this is too slow, is to create an <code>Iterator</code>
     * which can simultaneously visit the parse tree and traverse the <code>Presentation</code>
     * object, doing a merge of the two as it iterates. But that is complicated enough that I want
     * to put it off until I'm sure we need it.
     * 
     * @param block
     * @param printStream
     */
    private void printBlock(IPresentationBlock block, PrintStream printStream)
    {
        moveToBlockStartPosition(block, printStream);

        printStream.print(block.getText());

        updateCurrentRowAndCol(block, printStream);
    }

    /**
     * Inserts enough spaces and carriage returns to end up at the starting line and column for the
     * given block.
     * 
     * @param block
     */
    private void moveToBlockStartPosition(IPresentationBlock block, PrintStream printStream)
    {
        int numCarriageReturnsNeeded = block.getStartLine() - curRow;
        int numSpacesNeeded = (numCarriageReturnsNeeded > 0 ? block.getStartCol() - 1 : block
            .getStartCol()
            - curCol);

        for (int i = 0; i < numCarriageReturnsNeeded; i++)
        {
            printNewLine(printStream);
            curRow++;
            curCol = 1;
        }

        for (int i = 0; i < numSpacesNeeded; i++)
        {
            printStream.print(" ");
            curCol++;
        }
    }

    /**
     * Updates the <code>curRow</code> and <code>curCol</code> fields to reflect that the
     * current position is immediately past the end of the given block. Called after the given block
     * has been printed.
     * 
     * @param block
     */
    private void updateCurrentRowAndCol(IPresentationBlock block, PrintStream printStream)
    {
        String text = block.getText();
        int len = text.length();

        for (int i = 0; i < len; i++)
        {
            char thisChar = text.charAt(i);
            if (thisChar == '\n')
            {
                curRow++;
                curCol = 1;
            }
            else
                curCol++;
        }
    }

    /**
     * Prints a CRLF or newline character, depending on the user's operating system.
     * 
     * @param printStream
     */
    private void printNewLine(PrintStream printStream)
    {
        printStream.print(System.getProperty("line.separator"));
    }
}
