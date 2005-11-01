/**
 * 
 */
package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

/**
 * @author nchen
 * @author cheah
 * 
 * Converts tabs to spaces
 */
class TabConverter implements ITextConverter {
	private int fTabRatio;

	private ILineTracker fLineTracker;


	public TabConverter() {
	}

	public void setNumberOfSpacesPerTab(int ratio) {
		fTabRatio = ratio;
	}

	public void setLineTracker(ILineTracker lineTracker) {
		fLineTracker = lineTracker;
	}

	private int insertTabString(StringBuffer buffer, int offsetInLine) {

		if (fTabRatio == 0)
			return 0;

		int remainder = offsetInLine % fTabRatio;
		remainder = fTabRatio - remainder;
		for (int i = 0; i < remainder; i++)
			buffer.append(' ');
		return remainder;
	}

	public void customizeDocumentCommand(IDocument document,
			DocumentCommand command) {
		String text = command.text;
		if (text == null)
			return;

		int index = text.indexOf('\t');// + text.indexOf('\n');
		if (index > -1) {

			StringBuffer buffer = new StringBuffer();
			fLineTracker.set(command.text);
			int lines = fLineTracker.getNumberOfLines();

			try {

				for (int i = 0; i < lines; i++) {

					int offset = fLineTracker.getLineOffset(i);
					int endOffset = offset + fLineTracker.getLineLength(i);
					String line = text.substring(offset, endOffset);

					int position = 0;
					if (i == 0) {
						IRegion firstLine = document
								.getLineInformationOfOffset(command.offset);
						position = command.offset - firstLine.getOffset();
					}

					int length = line.length();
					for (int j = 0; j < length; j++) {
						char c = line.charAt(j);
						if (c == '\t') { // a tab has been inserted
							position += insertTabString(buffer, position);
						} else {
							buffer.append(c);
							++position;
						}
					}

				}

				command.text = buffer.toString();

			} catch (BadLocationException x) {
				x.printStackTrace();
			}
		}
	}

}