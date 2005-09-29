package org.eclipse.photran.internal.core.programeditor.blockplacer.rules;

/**
 * A block placement rule indicating that the token should be repositioned so that its first column
 * is the same as the first column of the first token on the preceding line, plus a given number of
 * columns.
 * FIXME-Jeff: Implement this 
 * 
 * @author joverbey
 */
public class IndentSameAsPrecedingLinePlus extends BlockPlacementRule
{
    public IndentSameAsPrecedingLinePlus(int indentAmount)
    {

    }
}