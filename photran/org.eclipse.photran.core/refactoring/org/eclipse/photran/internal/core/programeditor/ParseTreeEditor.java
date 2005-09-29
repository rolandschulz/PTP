package org.eclipse.photran.internal.core.programeditor;

import org.eclipse.photran.internal.core.f95parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;

/**
 * Routines for shifting token positions in a parse tree. Used exclusively by the
 * <code>ProgramEditor</code>. These methods were factored out to keep the
 * <code>ProgramEditor</code> from getting too big. Like the methods in
 * <code>ParseTreeSearcher</code>, they could potentially be included in the
 * <code>ParseTreeNode</code> class.
 * 
 * @author joverbey
 */
public final class ParseTreeEditor
{
    public static void shiftParseTreeContents(ParseTreeNode parseTree,
        final int lineToStartShiftingOn, final int colToStartShiftingAfter,
        final int lineShiftAmount, final int startLineColShiftAmount)
    {
        parseTree.visitUsing(new GenericParseTreeVisitor()
        {
            public void visitToken(Token thisToken)
            {
                if (PresBlockUtil.comesOnOrAfter(thisToken, lineToStartShiftingOn,
                    colToStartShiftingAfter))
                {
                    // Ignoring offsets

                    if (thisToken.getStartLine() == lineToStartShiftingOn
                        && thisToken.getStartCol() > colToStartShiftingAfter)
                    {
                        thisToken.setStartCol(thisToken.getStartCol() + startLineColShiftAmount);
                        thisToken.setEndCol(thisToken.getEndCol() + startLineColShiftAmount);
                    }

                    thisToken.setStartLine(thisToken.getStartLine() + lineShiftAmount);
                    thisToken.setEndLine(thisToken.getEndLine() + lineShiftAmount);
                }
            }
        });
    }
}
