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
package org.eclipse.ptp.debug.internal.ui;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Clement chu
 *
 */
public class PInstructionPointerContext {
	private ITextEditor fTextEditor;
	private Annotation fAnnotation;

	public 3(ITextEditor textEditor, Annotation annotation) {
		setTextEditor(textEditor);
		setAnnotation(annotation);
	}
	
	public boolean equals(Object other) {
		if (other instanceof PInstructionPointerContext) {
			PInstructionPointerContext otherContext = (PInstructionPointerContext)other;
			return getAnnotation().equals(otherContext.getAnnotation());
		}
		return false;
	}
	
	public int hashCode() {
		return getAnnotation().hashCode();
	}

	private void setTextEditor(ITextEditor textEditor) {
		fTextEditor = textEditor;
	}

	public ITextEditor getTextEditor() {
		return fTextEditor;
	}

	private void setAnnotation(Annotation annotation) {
		fAnnotation = annotation;
	}

	public Annotation getAnnotation() {
		return fAnnotation;
	}
}
