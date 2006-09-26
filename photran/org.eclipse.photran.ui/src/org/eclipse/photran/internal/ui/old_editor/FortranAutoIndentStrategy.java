/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.photran.internal.ui.old_editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.photran.internal.ui.preferences.FortranEditorPreferencePage;

/**
 * Auto indent strategy sensitive to brackets.
 */
public class FortranAutoIndentStrategy extends
		DefaultIndentLineAutoEditStrategy {

	private boolean isFreeForm;

	public FortranAutoIndentStrategy(boolean isFreeForm) {
		this.isFreeForm = isFreeForm;
	}

	protected void smartIndentAfterNewLine(IDocument d, DocumentCommand c) {
		int docLength = d.getLength();
		//c.offset returns the absolute offset from beginning of document
		if (c.offset == -1 || docLength == 0)
			return;

		try {
			int p = (c.offset == docLength ? c.offset - 1 : c.offset);
			int line = d.getLineOfOffset(p); // our current line
			int lineOffSet = d.getLineOffset(line);
			StringBuffer buf = new StringBuffer(c.text);
			int start = d.getLineOffset(line);
			//whiteend indicates absolute index as well
			int whiteend = findEndOfWhiteSpace(d, start, c.offset);
			buf.append(d.get(start, whiteend - start));
			if (isIndentRightWord(d, whiteend) && (amongKeyword(lineOffSet,c.offset,whiteend)) ) {
				buf.append('\t');
			}
			c.text = buf.toString();

		} catch (BadLocationException excp) {
			excp.printStackTrace();
		}
	}

	private boolean amongKeyword(int lineOffSet, int offset, int whiteend) {
		return(!((offset<= whiteend)&&(offset>= lineOffSet)));
	}

	private boolean isIndentRightWord(IDocument doc, int firstChar) {
		// ITypedRegion = doc.getDocumentPartitioner().getPartition(firstChar);
		String partitionType = doc.getDocumentPartitioner().getPartition(
				firstChar).getType();
		if (partitionType
				.equals(FortranPartitionScanner.F90_KEYWORD_PARTITION_WITH_INDENTATION_RIGHT)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns whether the text ends with one of the given search strings.
	 */
	private boolean endsWithDelimiter(IDocument d, String txt) {
		String[] delimiters = d.getLegalLineDelimiters();

		for (int i = 0; i < delimiters.length; i++) {
			if (txt.endsWith(delimiters[i]))
				return true;
		}
		return false;
	}

	/**
	 * @see IAutoIndentStrategy#customizeDocumentCommand
	 */
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (c.length == 0 && c.text != null && endsWithDelimiter(d, c.text))
			smartIndentAfterNewLine(d, c);
		else if (soonToContainsEnd(d, c)) {
			smartUnindentAfterEnd(d, c);
		}
	}

	/**
	 * Unindents the line by the number indicated in the preference pane
	 * 
	 * @param doc
	 * @param command
	 */
	private void smartUnindentAfterEnd(IDocument doc, DocumentCommand command) {
		if (command.offset == -1 || doc.getLength() == 0)
			return;

		try {
			int p = (command.offset == doc.getLength() ? command.offset - 1
					: command.offset);
			int line = doc.getLineOfOffset(p);
			int startOfLineOffset = doc.getLineOffset(line);
			int whiteend = findEndOfWhiteSpace(doc, startOfLineOffset,
					command.offset);

			if (whiteend != command.offset) {

				int offsetOfLine = findEndOfWhiteSpace(doc, doc
						.getLineOffset(line), doc.getLineOffset(line)
						+ doc.getLineLength(line));
				int numberOfSpacesOnLeft = offsetOfLine
						- doc.getLineOffset(line);

				if (isFreeForm)
					numberOfSpacesOnLeft -= FortranEditorPreferencePage
							.getTabSize();
				else
					numberOfSpacesOnLeft -= FortranFixedFormEditor.COLUMM_6_WIDTH;

				if (numberOfSpacesOnLeft >= 0) {
					// convert to number of spaces
					String spaces = "";
					for (int i = 0; i < numberOfSpacesOnLeft; i++)
						spaces += " ";

					StringBuffer replaceText = new StringBuffer(spaces);

					// add the rest of the current line including the just added
					replaceText.append(doc.get(whiteend, command.offset
							- whiteend));
					replaceText.append(command.text);
					// modify document command
					command.length = command.offset - startOfLineOffset;
					command.offset = startOfLineOffset;
					command.text = replaceText.toString();
				}
			}
		} catch (BadLocationException excp) {
			excp.printStackTrace();
		}

	}

	/**
	 * Returns true if after inserting the current character we have end
	 * 
	 * @param doc
	 * @param command
	 * @return whether we have end
	 * 
	 * Using simple character mathing instead of the parser so that it works
	 * even if the lexer is not available; in the case of Free Form with more
	 * than a 1000 lines
	 */
	private boolean soonToContainsEnd(IDocument doc, DocumentCommand command) {
		if (command.offset == -1 || doc.getLength() == 0)
			return false;

		try {
			// convert all matches to lowercase since
			int commandOffset = (command.offset == doc.getLength() ? command.offset - 1
					: command.offset);
			int line = doc.getLineOfOffset(commandOffset); // our current line
			int start = doc.getLineOffset(line);
			int whiteend = findEndOfWhiteSpace(doc, start, command.offset);

			// if c equals d and after appending this we have "end"
			if ("d".equals(command.text.toLowerCase())) {
				int end = doc.getLineOffset(line) + doc.getLineLength(line)
						- whiteend;
				if (doc.get(whiteend, end).toLowerCase().equals("en")) {
					return true;
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return false;

	}

}
