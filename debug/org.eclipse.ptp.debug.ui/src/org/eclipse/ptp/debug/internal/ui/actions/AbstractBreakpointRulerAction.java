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
package org.eclipse.ptp.debug.internal.ui.actions;

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
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
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

	protected IBreakpoint determineBreakpoint() {
		//TODO need to change back PTPDebugUIPlugin to PTPDebugCorePlugin
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(PTPDebugUIPlugin.getUniqueIdentifier());
		for(int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			if (breakpoint instanceof ILineBreakpoint) {
				ILineBreakpoint lineBreakpoint = (ILineBreakpoint)breakpoint;
				if (breakpointAtRulerLine(lineBreakpoint)) {
					return lineBreakpoint;
				}
			}
		}
		return null;
	}

	protected IVerticalRulerInfo getInfo() {
		return info;
	}

	protected void setInfo(IVerticalRulerInfo info) {
		this.info = info;
	}

	protected IWorkbenchPart getTargetPart() {
		return targetPart;
	}
	protected void setTargetPart(IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	protected IBreakpoint getBreakpoint() {
		return breakpoint;
	}

	protected void setBreakpoint(IBreakpoint breakpoint) {
		this.breakpoint = breakpoint;
	}

	protected boolean breakpointAtRulerLine(ILineBreakpoint pBreakpoint) {
		int lineNumber = getBreakpointLine(pBreakpoint);
		int rulerLine = getInfo().getLineOfLastMouseButtonActivity();
		return (rulerLine == lineNumber);
	}

	private int getBreakpointLine(ILineBreakpoint breakpoint) {
		if (getTargetPart() instanceof ISaveablePart && ((ISaveablePart)getTargetPart()).isDirty()) {
			try {
				return breakpoint.getLineNumber();
			}
			catch(CoreException e) {
				DebugPlugin.log(e);
			}
		}
		else {
			Position position = getBreakpointPosition(breakpoint);
			if (position != null) {
				IDocument doc = getDocument();
				if (doc != null) {
					try {
						return doc.getLineOfOffset(position.getOffset());
					}
					catch (BadLocationException x) {
						DebugPlugin.log(x);
					}
				}
			}
		}
		return -1;
	}

	private Position getBreakpointPosition(ILineBreakpoint breakpoint) {
		IAnnotationModel model = getAnnotationModel();
		if (model != null) {
			Iterator it = model.getAnnotationIterator();
			while(it.hasNext()) {
				Annotation ann = (Annotation)it.next();
				if (ann instanceof MarkerAnnotation && ((MarkerAnnotation)ann).getMarker().equals( breakpoint.getMarker())) {
					return model.getPosition(ann);
				}
			}
		}
		return null;
	}

	private IDocument getDocument() {
		IWorkbenchPart targetPart = getTargetPart();
		if (targetPart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor)targetPart; 
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if (provider != null)
				return provider.getDocument(textEditor.getEditorInput());
		}
		return null;
	}

	private IAnnotationModel getAnnotationModel() {
		IWorkbenchPart targetPart = getTargetPart();
		if (targetPart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor)targetPart; 
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if (provider != null)
				return provider.getAnnotationModel(textEditor.getEditorInput());
		}
		return null;
	}
}
