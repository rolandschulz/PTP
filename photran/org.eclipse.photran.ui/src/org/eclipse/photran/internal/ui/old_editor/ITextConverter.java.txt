/**
 * 
 */
package org.eclipse.photran.internal.ui.old_editor;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;


/**
 * @author nchen
 * @author cheah
 * 
 * Simple interface that is needed for tab conversion
 */
interface ITextConverter {
	void customizeDocumentCommand(IDocument document,
			DocumentCommand command);
}