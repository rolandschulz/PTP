/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.ui.hover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.BreakIterator;
import org.eclipse.swt.graphics.GC;

/**
 * @author Clement chu
 * 
 */
public class LineBreakingReader {

	private BufferedReader fReader;
	private GC fGC;
	private int fMaxWidth;

	private String fLine;
	private int fOffset;

	private BreakIterator fLineBreakIterator;

	/**
	 * Creates a reader that breaks an input text to fit in a given width.
	 * @param reader Reader of the input text
	 * @param gc The graphic context that defines the currently used font sizes
	 * @param maxLineWidth The max width (pixes) where the text has to fit in
	 */
	public LineBreakingReader(Reader reader, GC gc, int maxLineWidth) {
		fReader= new BufferedReader(reader);
		fGC= gc;
		fMaxWidth= maxLineWidth;
		fOffset= 0;
		fLine= null;
		fLineBreakIterator= BreakIterator.getLineInstance();
	}

	public boolean isFormattedLine() {
		return fLine != null;
	}

	/**
	 * Reads the next line. The lengths of the line will not exceed the gived maximum
	 * width.
	 */
	public String readLine() throws IOException {
		if (fLine == null) {
			String line= fReader.readLine();
			if (line == null)
				return null;

			int lineLen= fGC.textExtent(line).x;
			if (lineLen < fMaxWidth) {
				return line;
			}
			fLine= line;
			fLineBreakIterator.setText(line);
			fOffset= 0;
		}
		int breakOffset= findNextBreakOffset(fOffset);
		String res;
		if (breakOffset != BreakIterator.DONE) {
			res= fLine.substring(fOffset, breakOffset);
			fOffset= findWordBegin(breakOffset);
			if (fOffset == fLine.length()) {
				fLine= null;
			}
		} else {
			res= fLine.substring(fOffset);
			fLine= null;
		}
		return res;
	}

	private int findNextBreakOffset(int currOffset) {
		int currWidth= 0;
		int nextOffset= fLineBreakIterator.following(currOffset);
		while (nextOffset != BreakIterator.DONE) {
			String word= fLine.substring(currOffset, nextOffset);
			int wordWidth= fGC.textExtent(word).x;
			int nextWidth= wordWidth + currWidth;
			if (nextWidth > fMaxWidth) {
				if (currWidth > 0) {
					return currOffset;
				} else {
					return nextOffset;
				}
			}
			currWidth= nextWidth;
			currOffset= nextOffset;
			nextOffset= fLineBreakIterator.next();
		}
		return nextOffset;
	}

	private int findWordBegin(int idx) {
		while (idx < fLine.length() && Character.isWhitespace(fLine.charAt(idx))) {
			idx++;
		}
		return idx;
	}
}

