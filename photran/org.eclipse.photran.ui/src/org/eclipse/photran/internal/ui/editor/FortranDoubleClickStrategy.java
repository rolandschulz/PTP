package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * A custom double-click action for the Fortran editor.
 * 
 * If the user double-clicks inside a nonterminal, the whole nonterminal should be selected. "String
 * terminals" should work similarly.
 * 
 * FIXME-Cheah or Nick: This is left over from the Damien EBNF editor; it's not correct for Fortran!
 * 
 * @author joverbey
 */
public class FortranDoubleClickStrategy implements ITextDoubleClickStrategy
{
    protected ITextViewer fText;

    /**
     * The callback for the double-click action.
     * @see org.eclipse.jface.text.ITextDoubleClickStrategy#doubleClicked(org.eclipse.jface.text.ITextViewer)
     */
    public void doubleClicked(ITextViewer part)
    {
        int pos = part.getSelectedRange().x;
        if (pos < 0) return;

        fText = part;

        if (!selectNonterminal(pos)) if (!selectString(pos)) selectWord(pos);
    }

    protected boolean selectNonterminal(int caretPos)
    {
        return selectBetween(caretPos, '<', '>', '\0');
    }

    protected boolean selectString(int caretPos)
    {
        return selectBetween(caretPos, '\"', '\"', '\\');
    }

    protected boolean selectBetween(int caretPos, char start, char end, char escape)
    {
        IDocument doc = fText.getDocument();
        int startPos, endPos;

        try
        {
            // Scan backward until we find the start character, or fail if we
            // reached the beginning of the line without finding that character.
            // Also stops at an end character so that we don't scan into a
            // previous token (e.g., if we double-click on the c in [ab]c[de],
            // we don't want all of ab]c[de selected).
            int pos = caretPos;
            char c = '\0';

            while (pos >= 0)
            {
                c = doc.getChar(pos);

                if (c == escape)
                    pos -= 2;
                else if (c == Character.LINE_SEPARATOR || c == start || c == end)
                    break;
                else
                    --pos;
            }

            if (c != start) return false;

            startPos = pos;

            // Scan forward until we find the end character, or fail if we
            // reached the end of the line without finding that character.
            pos = caretPos;
            int length = doc.getLength();
            c = '\0';

            while (pos < length)
            {
                c = doc.getChar(pos);
                if (c == escape)
                    pos += 2;
                else if (c == Character.LINE_SEPARATOR || c == start || c == end)
                    break;
                else
                    ++pos;
            }

            if (c != end) return false;

            endPos = pos;

            // Select the range we just found
            int offset = startPos + 1;
            int len = endPos - offset;
            fText.setSelectedRange(offset, len);
            return true;
        }
        catch (BadLocationException x)
        {
        }

        return false;
    }

    protected boolean selectWord(int caretPos)
    {

        IDocument doc = fText.getDocument();
        int startPos, endPos;

        try
        {

            int pos = caretPos;
            char c;

            while (pos >= 0)
            {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) break;
                --pos;
            }

            startPos = pos;

            pos = caretPos;
            int length = doc.getLength();

            while (pos < length)
            {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) break;
                ++pos;
            }

            endPos = pos;
            selectRange(startPos, endPos);
            return true;

        }
        catch (BadLocationException x)
        {
        }

        return false;
    }

    private void selectRange(int startPos, int stopPos)
    {
        int offset = startPos + 1;
        int length = stopPos - offset;
        fText.setSelectedRange(offset, length);
    }
}