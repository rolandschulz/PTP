/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.editor;

import java.util.LinkedList;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Vivian Kong
 *
 */
public class RemoteCFormattingStrategy extends ContextBasedFormattingStrategy {
	
	/** Documents to be formatted by this strategy */
	private final LinkedList<IDocument> fDocuments= new LinkedList<IDocument>();
	/** Partitions to be formatted by this strategy */
	private final LinkedList<TypedPosition> fPartitions= new LinkedList<TypedPosition>();

	/**
	 * Creates a new java formatting strategy.
 	 */
	public RemoteCFormattingStrategy() {
		super();
	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#format()
	 */
	@Override
	public void format() {
		super.format();
		
		final IDocument document= fDocuments.removeFirst();
		final TypedPosition partition= fPartitions.removeFirst();
		
		if (document != null && partition != null) {
			try {
				@SuppressWarnings("unchecked")
				final Map<String,String> preferences = getPreferences();
				final TextEdit edit = format(
						CodeFormatter.K_TRANSLATION_UNIT, document.get(),
						partition.getOffset(), partition.getLength(), 0,
						TextUtilities.getDefaultLineDelimiter(document),
						preferences);

				if (edit != null)
					edit.apply(document);

			} catch (MalformedTreeException exception) {
				UIPlugin.log(exception);
			} catch (BadLocationException exception) {
				// Can only happen on concurrent document modification - log and
				// bail out
				UIPlugin.log(exception);
			}
		}
 	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStarts(org.eclipse.jface.text.formatter.IFormattingContext)
	 */
	@Override
	public void formatterStarts(final IFormattingContext context) {
		super.formatterStarts(context);
		
		Object property = context.getProperty(FormattingContextProperties.CONTEXT_PARTITION);
		if (property instanceof TypedPosition) {
			fPartitions.addLast((TypedPosition) property);
		}
		property= context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM);
		if (property instanceof IDocument) {			
			fDocuments.addLast((IDocument) property);
		}
	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStops()
	 */
	@Override
	public void formatterStops() {
		super.formatterStops();

		fPartitions.clear();
		fDocuments.clear();
	}
	
	/**
	 * Creates edits that describe how to format the given string. Returns <code>null</code> if the code could not be formatted for the given kind.
	 * @throws IllegalArgumentException If the offset and length are not inside the string, a
	 *  IllegalArgumentException is thrown.
	 */
	private static TextEdit format(int kind, String source, int offset, int length, int indentationLevel, String lineSeparator, Map<String, ?> options) {
		if (offset < 0 || length < 0 || offset + length > source.length()) {
			throw new IllegalArgumentException("offset or length outside of string. offset: " + offset + ", length: " + length + ", string size: " + source.length());   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		}
		
		if (options == null)
			options = CCorePlugin.getOptions();

		CodeFormatter formatter = new RemoteCCodeFormatter(options);

		return formatter.format(kind, source, offset, length, indentationLevel, lineSeparator);

	}
}
