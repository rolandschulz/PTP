/*******************************************************************************
 * Copyright (c) 2009, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrey Eremchenko, kamre@ngs.ru - 222495 C/C++ search should show line matches and line numbers	
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.LineSearchElement
 * Version: 1.9
 */

package org.eclipse.ptp.internal.rdt.core.search;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.internal.core.parser.scanner.AbstractCharArray;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;



public class RemoteLineSearchElement extends RemoteSearchElement implements Serializable{

	public static class RemoteLineSearchElementMatch implements Serializable{
		
		private static final long serialVersionUID = 1L;
		private final int fOffset;
		private final int fLength;
		private final boolean fIsPolymorphicCall;
		private final ICElement fEnclosingElement;
		private final boolean fIsWriteAccess;
		

		public RemoteLineSearchElementMatch(int offset, int length, boolean isPolymorphicCall, ICElement enclosingElement, boolean isWriteAccess) {
			fOffset = offset;
			fLength = length;
			fIsPolymorphicCall = isPolymorphicCall;
			fEnclosingElement = enclosingElement;
			fIsWriteAccess = isWriteAccess;
		}

		public int getOffset() {
			return fOffset;
		}

		public int getLength() {
			return fLength;
		}

		public boolean isPolymorphicCall() {
			return fIsPolymorphicCall;
		}
		
		public ICElement getEnclosingElement() {
			return fEnclosingElement;
		}

		public boolean isWriteAccess() {
			return fIsWriteAccess;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof RemoteLineSearchElementMatch))
				return false;
			RemoteLineSearchElementMatch m = (RemoteLineSearchElementMatch) obj;
			return (fOffset == m.fOffset) && (fLength == m.fLength);
		}

		@Override
		public int hashCode() {
			return 31 * fOffset + fLength;
		}

	}
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int fOffset;
	private final int fNumber;
	private final String fContent;
	private final RemoteLineSearchElementMatch[] fMatches;
	
	public final static MatchesComparator MATCHES_COMPARATOR = new MatchesComparator();
	
	private static final class MatchesComparator implements Comparator<RemoteLineSearchElementMatch> {
		public int compare(RemoteLineSearchElementMatch m1, RemoteLineSearchElementMatch m2) {
			int diff= m1.getOffset() - m2.getOffset();
			if (diff == 0)
				diff= m2.getLength() -m1.getLength();
			return diff;
		}
	}
	
	public RemoteLineSearchElement(IIndexFileLocation file, RemoteLineSearchElementMatch[] matches, int number, String content,
			int offset) {
		super(file);
		fMatches = matches;
		fNumber = number;
		// Skip whitespace at the beginning.
		int index = 0;
		int length = content.length();
		int firstMatchOffset = matches[0].getOffset();
		while (offset < firstMatchOffset && length > 0) {
			if (!Character.isWhitespace(content.charAt(index)))
				break;
			index++;
			offset++;
			length--;
		}
		fOffset = offset;
		fContent = content.substring(index).trim();
	}
	
	

	public int getOffset() {
		return fOffset;
	}

	public int getLineNumber() {
		return fNumber;
	}

	public String getContent() {
		return fContent;
	}

	public RemoteLineSearchElementMatch[] getMatches() {
		return fMatches;
	}

	@Override
	public String toString() {
		return fNumber + ": " + fContent; //$NON-NLS-1$
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RemoteLineSearchElement))
			return false;
		RemoteLineSearchElement other = (RemoteLineSearchElement) obj;
		return (fOffset == other.fOffset) && (super.equals(obj)) && (fMatches.equals(other.fMatches));
	}

	@Override
	public int hashCode() {
		return fOffset + 31 * (super.hashCode() + 31 * fMatches.hashCode());
	}

	private static IFile getFile(IIndexFileLocation fileLocation){
		

		
		if (fileLocation.getFullPath() != null) {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileLocation.getFullPath()));
		} else {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(fileLocation.getURI());
			if(files != null && files.length > 0) 
				return files[0];
		}
		return null;
		
	}
	public static RemoteLineSearchElement[] createElements(IIndexFileLocation fileLocation, RemoteLineSearchElementMatch[] matches) {
		// sort matches according to their offsets
		Arrays.sort(matches, MATCHES_COMPARATOR);
		RemoteLineSearchElement[] result = {};
		
		// read the content of file
	//	IFile indexFile=getFile(fileLocation);
		FileContent content = FileContent.create(fileLocation);
		if (content != null) {
			AbstractCharArray buf = ((InternalFileContent) content).getSource();
			if (buf != null)
				result = collectLineElements(buf, matches, fileLocation);
		}
		return result;
	}
/*
	public static RemoteLineSearchElement[] createElements(IIndexFileLocation fileLocation, RemoteLineSearchElementMatch[] matches,
			IDocument document) {
		// Sort matches according to their offsets
		Arrays.sort(matches, MATCHES_COMPARATOR);
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
*/
	private static RemoteLineSearchElement[] collectLineElements(AbstractCharArray buf, RemoteLineSearchElementMatch[] matches,
			IIndexFileLocation fileLocation) {

		List<RemoteLineSearchElement> result = new ArrayList<RemoteLineSearchElement>();
		List<RemoteLineSearchElementMatch> matchCollector= new ArrayList<RemoteLineSearchElementMatch>();

		boolean skipLF = false;
		int lineNumber = 1;
		int lineOffset = 0;
		int i = 0;
		RemoteLineSearchElementMatch match= matches[i];
		int matchOffset = match.getOffset();
		for (int pos = 0; buf.isValidOffset(pos); pos++) {
			if (matchOffset <= pos && match != null) {
				// We are on the line of the match, store it.
				matchCollector.add(match);
				final int minOffset= matchOffset + match.getLength();
				match= null;
				matchOffset= Integer.MAX_VALUE;
				for(i=i+1; i<matches.length; i++) {
					// Advance to next match that is not overlapped
					final RemoteLineSearchElementMatch nextMatch= matches[i];
					final int nextOffset= nextMatch.getOffset();
					if (nextOffset >= minOffset) {
						match= nextMatch;
						matchOffset= nextOffset;
						break;
					}
				}
			}
				
			char c = buf.get(pos);
			// consider '\n' and '\r'
			if (skipLF) {
				skipLF = false;
				if (c == '\n') {
					lineOffset = pos + 1;
					continue;
				}
			}
			if (c == '\n' || c == '\r') {
				// Create new LineElement for collected matches on this line
				if (!matchCollector.isEmpty()) {
					int lineLength = pos - lineOffset;
					RemoteLineSearchElementMatch[] lineMatches= matchCollector.toArray(new RemoteLineSearchElementMatch[matchCollector.size()]);
					char[] lineChars= new char[lineLength];
					buf.arraycopy(lineOffset, lineChars, 0, lineLength);
					String lineContent = new String(lineChars);
					result.add(new RemoteLineSearchElement(fileLocation, lineMatches, lineNumber, lineContent,
							lineOffset));
					matchCollector.clear();
					if (match == null)
						break;
				}
				lineNumber++;
				lineOffset = pos + 1;
				if (c == '\r')
					skipLF = true;
				continue;
			}
		}
		// Create new LineElement for  matches on the last line
		if (!matchCollector.isEmpty()) {
			int lineLength = buf.getLength() - lineOffset;
			RemoteLineSearchElementMatch[] lineMatches= matchCollector.toArray(new RemoteLineSearchElementMatch[matchCollector.size()]);
			char[] lineChars= new char[lineLength];
			buf.arraycopy(lineOffset, lineChars, 0, lineLength);
			String lineContent = new String(lineChars);
			result.add(new RemoteLineSearchElement(fileLocation, lineMatches, lineNumber, lineContent,
					lineOffset));
		}
		return result.toArray(new RemoteLineSearchElement[result.size()]);
	}
}
