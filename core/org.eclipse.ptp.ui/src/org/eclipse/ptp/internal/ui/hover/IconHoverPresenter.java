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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
/**
 * @author Clement chu
 */
public class IconHoverPresenter implements DefaultInformationControl.IInformationPresenter {
	private static final String LINE_DELIM = System.getProperty("line.separator", "\n");
	private static final String LINE_INDENTATION = "  ";
	private int fCounter;
	private boolean fEnforceUpperLineLimit;
	/**
	 * Constructor
	 * 
	 * @param enforceUpperLineLimit
	 */
	public IconHoverPresenter(boolean enforceUpperLineLimit) {
		super();
		fEnforceUpperLineLimit = enforceUpperLineLimit;
	}
	/**
	 * Constructor
	 */
	public IconHoverPresenter() {
		this(true);
	}
	/**
	 * Create Reader
	 * 
	 * @param hoverInfo
	 * @param presentation
	 * @return
	 */
	protected Reader createReader(String hoverInfo, TextPresentation presentation) {
		return new IconTextReader(new StringReader(hoverInfo), presentation);
	}
	/**
	 * Adpat text presentation
	 * 
	 * @param presentation
	 * @param offset
	 * @param insertLength
	 */
	protected void adaptTextPresentation(TextPresentation presentation, int offset, int insertLength) {
		int yoursStart = offset;
		int yoursEnd = offset + insertLength - 1;
		yoursEnd = Math.max(yoursStart, yoursEnd);
		Iterator e = presentation.getAllStyleRangeIterator();
		while (e.hasNext()) {
			StyleRange range = (StyleRange) e.next();
			int myStart = range.start;
			int myEnd = range.start + range.length - 1;
			myEnd = Math.max(myStart, myEnd);
			if (myEnd < yoursStart)
				continue;
			if (myStart < yoursStart)
				range.length += insertLength;
			else
				range.start += insertLength;
		}
	}
	/**
	 * Append text
	 * 
	 * @param buffer
	 * @param string
	 * @param presentation
	 */
	private void append(StringBuffer buffer, String string, TextPresentation presentation) {
		int length = string.length();
		buffer.append(string);
		if (presentation != null)
			adaptTextPresentation(presentation, fCounter, length);
		fCounter += length;
	}
	/**
	 * Get indentation
	 * 
	 * @param line
	 * @return
	 */
	private String getIndent(String line) {
		int length = line.length();
		int i = 0;
		while (i < length && Character.isWhitespace(line.charAt(i)))
			++i;
		return (i == length ? line : line.substring(0, i)) + LINE_INDENTATION;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.hover.DefaultInformationControl.IInformationPresenter#updatePresentation(org.eclipse.swt.widgets.Display, java.lang.String, org.eclipse.jface.text.TextPresentation, int, int)
	 */
	public String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight) {
		if (hoverInfo == null)
			return null;
		GC gc = new GC(display);
		try {
			StringBuffer buffer = new StringBuffer();
			int maxNumberOfLines = Math.round(maxHeight / gc.getFontMetrics().getHeight());
			fCounter = 0;
			LineBreakingReader reader = new LineBreakingReader(createReader(hoverInfo, presentation), gc, maxWidth);
			String line = reader.readLine();
			while (line != null) {
				if (fEnforceUpperLineLimit && maxNumberOfLines <= 0)
					break;

				append(buffer, line, null);
				append(buffer, LINE_DELIM, presentation);
				line = reader.readLine();
				maxNumberOfLines--;
			}
			// display "..." if the content is over the shell size
			if (line != null && buffer.length() > 0) {
				append(buffer, LINE_DELIM, presentation);
				append(buffer, "...", presentation);
			}
			return trim(buffer, presentation);
		} catch (IOException e) {
			PTPUIPlugin.log(e);
			return null;
		} finally {
			gc.dispose();
		}
	}
	/**
	 * implemented another method to replace orginial 
	public String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight) {
		if (hoverInfo == null)
			return null;
		GC gc = new GC(display);
		try {
			StringBuffer buffer = new StringBuffer();
			int maxNumberOfLines = Math.round(maxHeight / gc.getFontMetrics().getHeight());
			fCounter = 0;
			LineBreakingReader reader = new LineBreakingReader(createReader(hoverInfo, presentation), gc, maxWidth);
			boolean lastLineFormatted = false;
			String lastLineIndent = null;
			String line = reader.readLine(lastLineIndent);
			boolean lineFormatted = reader.isFormattedLine();
			boolean firstLineProcessed = false;
			while (line != null) {
				if (fEnforceUpperLineLimit && maxNumberOfLines <= 0)
					break;
				if (firstLineProcessed) {
					if (!lastLineFormatted)
						append(buffer, LINE_DELIM, null);
					else {
						append(buffer, LINE_DELIM, presentation);
						if (lastLineIndent != null)
							append(buffer, lastLineIndent, presentation);
					}
				}
				append(buffer, line, null);
				firstLineProcessed = true;
				lastLineFormatted = lineFormatted;
				if (!lineFormatted)
					lastLineIndent = null;
				else if (lastLineIndent == null)
					lastLineIndent = getIndent(line);
				line = reader.readLine(lastLineIndent);
				lineFormatted = reader.isFormattedLine();
				maxNumberOfLines--;
			}
			// display "..." if the content is over the shell size
			if (line != null && buffer.length() > 0) {
				append(buffer, LINE_DELIM, lineFormatted ? presentation : null);
				append(buffer, "...", presentation);
			}
			return trim(buffer, presentation);
		} catch (IOException e) {
			PTPUIPlugin.log(e);
			return null;
		} finally {
			gc.dispose();
		}
	}
	 * 
	 */
	
	/**
	 * Trim given buffer
	 * 
	 * @param buffer
	 * @param presentation
	 * @return
	 */
	private String trim(StringBuffer buffer, TextPresentation presentation) {
		int length = buffer.length();
		int end = length - 1;
		while (end >= 0 && Character.isWhitespace(buffer.charAt(end)))
			--end;
		if (end == -1)
			return "";
		if (end < length - 1)
			buffer.delete(end + 1, length);
		else
			end = length;
		int start = 0;
		while (start < end && Character.isWhitespace(buffer.charAt(start)))
			++start;
		buffer.delete(0, start);
		presentation.setResultWindow(new Region(start, buffer.length()));
		return buffer.toString();
	}
}
