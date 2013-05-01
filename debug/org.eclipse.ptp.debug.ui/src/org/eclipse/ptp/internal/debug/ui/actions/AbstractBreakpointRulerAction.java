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
package org.eclipse.ptp.internal.debug.ui.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ptp.internal.debug.core.PDebugModel;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Clement chu
 * 
 */
public abstract class AbstractBreakpointRulerAction extends Action implements IUpdate {
	private IVerticalRulerInfo info;
	private IWorkbenchPart targetPart;
	private IBreakpoint breakpoint;

	/**
	 * Get line breakpoint
	 * 
	 * @return null if there is no line breakpoint
	 */
	protected IBreakpoint determineBreakpoint() {
		IBreakpoint[] breakpoints = PDebugModel.getBreakpoints();
		for (IBreakpoint breakpoint : breakpoints) {
			if (breakpoint instanceof ILineBreakpoint) {
				ILineBreakpoint lineBreakpoint = (ILineBreakpoint) breakpoint;
				if (breakpointAtRulerLine(lineBreakpoint)) {
					return lineBreakpoint;
				}
			}
		}
		return null;
	}

	/**
	 * Get vertical ruler info
	 * 
	 * @return
	 */
	protected IVerticalRulerInfo getInfo() {
		return info;
	}

	/**
	 * Set vertical ruler info
	 * 
	 * @param info
	 */
	protected void setInfo(IVerticalRulerInfo info) {
		this.info = info;
	}

	/**
	 * Get target workbench part
	 * 
	 * @return
	 */
	protected IWorkbenchPart getTargetPart() {
		return targetPart;
	}

	/**
	 * Set target workbench part
	 * 
	 * @param targetPart
	 */
	protected void setTargetPart(IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	/**
	 * Get breakpoint
	 * 
	 * @return
	 */
	protected IBreakpoint getBreakpoint() {
		return breakpoint;
	}

	/**
	 * Set breakpoint
	 * 
	 * @param breakpoint
	 */
	protected void setBreakpoint(IBreakpoint breakpoint) {
		this.breakpoint = breakpoint;
	}

	/**
	 * Check given breakpoint line number same as current ruler line number
	 * 
	 * @param pBreakpoint
	 * @return true if their line numbers are the same
	 */
	protected boolean breakpointAtRulerLine(ILineBreakpoint pBreakpoint) {
		int lineNumber = getBreakpointLine(pBreakpoint);
		int rulerLine = getInfo().getLineOfLastMouseButtonActivity();
		return (rulerLine == lineNumber);
	}

	/**
	 * Get breakpoint line number
	 * 
	 * @param breakpoint
	 * @return -1 if there is no line number
	 */
	private int getBreakpointLine(ILineBreakpoint breakpoint) {
		if (getTargetPart() instanceof ISaveablePart && ((ISaveablePart) getTargetPart()).isDirty()) {
			try {
				return breakpoint.getLineNumber();
			} catch (CoreException e) {
				DebugPlugin.log(e);
			}
		} else {
			Position position = getBreakpointPosition(breakpoint);
			if (position != null) {
				IDocument doc = getDocument();
				if (doc != null) {
					try {
						return doc.getLineOfOffset(position.getOffset());
					} catch (BadLocationException x) {
						DebugPlugin.log(x);
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Get breakpoint position
	 * 
	 * @param breakpoint
	 * @return null if there is no position found in given breakpoint
	 */
	private Position getBreakpointPosition(ILineBreakpoint breakpoint) {
		IAnnotationModel model = getAnnotationModel();
		if (model != null) {
			Iterator<?> it = model.getAnnotationIterator();
			while (it.hasNext()) {
				Annotation ann = (Annotation) it.next();
				if (ann instanceof MarkerAnnotation && ((MarkerAnnotation) ann).getMarker().equals(breakpoint.getMarker())) {
					return model.getPosition(ann);
				}
			}
		}
		return null;
	}

	/**
	 * Get document
	 * 
	 * @return
	 */
	private IDocument getDocument() {
		IWorkbenchPart targetPart = getTargetPart();
		if (targetPart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) targetPart;
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if (provider != null) {
				return provider.getDocument(textEditor.getEditorInput());
			}
		}
		return null;
	}

	/**
	 * Get IAnnotationModel
	 * 
	 * @return
	 */
	private IAnnotationModel getAnnotationModel() {
		IWorkbenchPart targetPart = getTargetPart();
		if (targetPart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) targetPart;
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if (provider != null) {
				return provider.getAnnotationModel(textEditor.getEditorInput());
			}
		}
		return null;
	}
}
