/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
 
package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.internal.ui.text.CCompositeReconcilingStrategy;
import org.eclipse.cdt.internal.ui.text.CReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

public class RemoteCCompositeReconcilingStrategy extends CCompositeReconcilingStrategy {

	public RemoteCCompositeReconcilingStrategy(ISourceViewer viewer,
			ITextEditor editor, String documentPartitioning) {
		super(viewer, editor, documentPartitioning);
		
		IReconcilingStrategy[] strategies = getReconcilingStrategies();
		for (int i = 0; i < strategies.length; i++) {
			if (strategies[i] instanceof CReconcilingStrategy) {
				//replace CReconcilingStrategy with RemoteCReconcilingStrategy
				strategies[i] = new RemoteCReconcilingStrategy(editor);
			}
		}
	}
}
