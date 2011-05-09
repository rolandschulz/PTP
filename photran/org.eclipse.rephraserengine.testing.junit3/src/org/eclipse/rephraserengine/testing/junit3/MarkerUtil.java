/*******************************************************************************
 * Copyright (c) 2011 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.testing.junit3;

import java.util.LinkedList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;

/**
 * Utility methods for
 * 
 * @author Jeff Overbey
 * 
 * @since 3.0
 */
public class MarkerUtil
{
    private MarkerUtil() {;}

    public static TextSelection determineSelection(String markerText, IDocument document) throws BadLocationException
    {
        return determineSelection(parseMarker(markerText), document);
    }
    
    public static LinkedList<String> parseMarker(String markerText)
    {
        LinkedList<String> result = new LinkedList<String>();
        for (String field : markerText.split(",")) //$NON-NLS-1$
            result.add(field.trim());
        return result;
    }

    public static TextSelection determineSelection(LinkedList<String> markerFields, IDocument document) throws BadLocationException
    {
        if (markerFields.size() < 2) throw new IllegalArgumentException();
        
        int fromLine = Integer.parseInt(markerFields.removeFirst());
        int fromCol = Integer.parseInt(markerFields.removeFirst());
        int toLine = fromLine;
        int toCol = fromCol;
        if (markerFields.size() >= 2 && isInteger(markerFields.get(0)) && isInteger(markerFields.get(1)))
        {
            toLine = Integer.parseInt(markerFields.removeFirst());
            toCol = Integer.parseInt(markerFields.removeFirst());
        }
        
        IRegion fromLineRegion = document.getLineInformation(fromLine-1);
        IRegion toLineRegion = document.getLineInformation(toLine-1);
        
        int fromOffset = fromLineRegion.getOffset() + fromCol - 1;
        int toOffset = toLineRegion.getOffset() + toCol - 1;
        int length = toOffset - fromOffset;
        
        return new TextSelection(document, fromOffset, length);
    }

    /**
     * @return true iff {@link Integer#parseInt(String)} can successfully parse the given
     *         string can be parsed as an integer
     */
    private static boolean isInteger(String string)
    {
        try
        {
            Integer.parseInt(string);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }
}
