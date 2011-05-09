/*******************************************************************************
 * Copyright (c) 2006, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchElement
 * Version: 1.12
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ptp.internal.rdt.core.search.RemoteLineSearchElement;
import org.eclipse.ptp.internal.rdt.core.search.RemoteLineSearchElement.RemoteLineSearchElementMatch;


public class RemoteLineSearchElementAdapter {
	
	public static RemoteLineSearchElement[] createElements(IIndexFileLocation fileLocation, RemoteLineSearchElementMatch[] matches,
			IDocument document) {
		// Sort matches according to their offsets
		Arrays.sort(matches, RemoteLineSearchElement.MATCHES_COMPARATOR);
		// Group all matches by lines and create LineSearchElements
		List<RemoteLineSearchElement> result = new ArrayList<RemoteLineSearchElement>();
		List<RemoteLineSearchElementMatch> matchCollector= new ArrayList<RemoteLineSearchElementMatch>();
		int minOffset = 0;
		int lineNumber = 0;
		int lineOffset = 0;
		int lineLength = 0;
		int lineEndOffset = 0;

		try {
			for (final RemoteLineSearchElementMatch match : matches) {
				final int offset= match.getOffset();
				if (offset < lineEndOffset) {
					// Match on same line
					if (offset < minOffset) {
						// Match is not overlapped by previous one.
						matchCollector.add(match);
						minOffset= offset + match.getLength();
					}
				} else {
					// Match is on a new line
					if (!matchCollector.isEmpty()) {
						// Complete a line
						String content = document.get(lineOffset, lineLength);
						RemoteLineSearchElementMatch[] lineMatches= matchCollector.toArray(new RemoteLineSearchElementMatch[matchCollector.size()]);
						result.add(new RemoteLineSearchElement(fileLocation, lineMatches, lineNumber + 1, content, lineOffset));
						matchCollector.clear();
					}
					// Setup next line
					lineNumber = document.getLineOfOffset(offset);
					lineOffset = document.getLineOffset(lineNumber);
					lineLength = document.getLineLength(lineNumber);
					lineEndOffset = lineOffset + lineLength;
					matchCollector.add(match);
				} 
			}
			if (!matchCollector.isEmpty()) {
				// Complete a line
				String content = document.get(lineOffset, lineLength);
				RemoteLineSearchElementMatch[] lineMatches= matchCollector.toArray(new RemoteLineSearchElementMatch[matchCollector.size()]);
				result.add(new RemoteLineSearchElement(fileLocation, lineMatches, lineNumber + 1, content, lineOffset));
				matchCollector.clear();
			}
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
		return result.toArray(new RemoteLineSearchElement[result.size()]);
	}


}
