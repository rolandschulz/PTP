/**
 * 
 */
package org.eclipse.photran.internal.ui.old_editor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;

class AdaptedSourceViewer extends SourceViewer
{
	private final SourceViewerConfiguration editor;

	private List textConverters;

	private boolean ignoreTextConverters = false;

	public AdaptedSourceViewer(SourceViewerConfiguration editor_SourceViewerConfiguration, Composite parent, IVerticalRuler ruler,
			IOverviewRuler ruler2, boolean b, int styles) {
		super(parent, ruler, ruler2, b, styles);
		editor = editor_SourceViewerConfiguration;
	}

	/*
	 * @see ITextOperationTarget#doOperation(int)
	 */
	public void doOperation(int operation) {
		if (getTextWidget() == null)
			return;
		switch (operation) {
		case UNDO:
			ignoreTextConverters = true;
			break;
		case REDO:
			ignoreTextConverters = true;
			break;
		}
		super.doOperation(operation);
	}

	public void addTextConverter(ITextConverter textConverter) {
		if (textConverters == null) {
			textConverters = new LinkedList();
			textConverters.add(textConverter);
		} else if (!textConverters.contains(textConverter))
			textConverters.add(textConverter);
	}

	public void removeTextConverter(ITextConverter textConverter) {
		if (textConverters != null) {
			textConverters.remove(textConverter);
			if (textConverters.size() == 0)
				textConverters = null;
		}
	}

	/*
	 * @see TextViewer#customizeDocumentCommand(DocumentCommand)
	 */
	protected void customizeDocumentCommand(DocumentCommand command) {
		super.customizeDocumentCommand(command);
		if (!ignoreTextConverters && textConverters != null) {
			for (Iterator e = textConverters.iterator(); e.hasNext();)
				((ITextConverter) e.next()).customizeDocumentCommand(
						getDocument(), command);
		}
		ignoreTextConverters = false;
	}

	public void updateIndentationPrefixes() {
		SourceViewerConfiguration configuration = editor;
		String[] types = configuration.getConfiguredContentTypes(this);
		for (int i = 0; i < types.length; i++) {
			String[] prefixes = configuration.getIndentPrefixes(this,
					types[i]);
			if (prefixes != null && prefixes.length > 0)
				setIndentPrefixes(prefixes, types[i]);
		}
	}
}