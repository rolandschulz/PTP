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
package org.eclipse.ptp.debug.internal.ui.editors;

import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.internal.ui.PDebugUIUtils;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Clement chu
 * 
 */
public class PDebugTextHover implements ICEditorTextHover, ITextHoverExtension, ISelectionListener, IPartListener {
	static final private int MAX_HOVER_INFO_SIZE = 100;
	protected ISelection fSelection = null;
	protected IEditorPart fEditor;

	/** Constructor
	 * 
	 */
	public PDebugTextHover() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IPStackFrame frame = getFrame();
		if (frame != null && frame.canEvaluate()) {
			try {
				IDocument document = textViewer.getDocument();
				if (document == null)
					return null;
				String expression = document.get(hoverRegion.getOffset(), hoverRegion.getLength());
				if (expression == null)
					return null;
				expression = expression.trim();
				if (expression.length() == 0)
					return null;
				StringBuffer buffer = new StringBuffer();
				String result = evaluateExpression(frame, expression);
				if (result == null)
					return null;
				
				try {
					if (result != null)
						appendVariable(buffer, frame.getTargetID(), makeHTMLSafe(expression), makeHTMLSafe(result.trim()));
				}
				catch(DebugException x) {
					PTPDebugUIPlugin.log(x);
				}
				if (buffer.length() > 0) {
					return buffer.toString();
				}
			}
			catch(BadLocationException x) {
				PTPDebugUIPlugin.log(x);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer viewer, int offset) {
		if (viewer != null)
			return PDebugUIUtils.findWord(viewer.getDocument(), offset);
		return null;
	}

	/** Get evalute expression result
	 * @param frame
	 * @param expression
	 * @return
	 */
	private String evaluateExpression(IPStackFrame frame, String expression) {
		String result = null;
		try {
			result = frame.evaluateExpressionToString(expression);
		}
		catch(DebugException e) {
			// ignore
		}
		return result;
	}

	/** Append formatted variable text 
	 * @param buffer
	 * @param taskID
	 * @param expression
	 * @param value
	 * @throws DebugException
	 */
	private static void appendVariable(StringBuffer buffer, int taskID, String expression, String value) throws DebugException {
		if (value.length() > MAX_HOVER_INFO_SIZE)
			value = value.substring(0, MAX_HOVER_INFO_SIZE) + " ...";
		buffer.append("<p>");
		buffer.append("<pre>").append(taskID + ": ").append(expression).append("</pre>");
		buffer.append(" = ");
		buffer.append("<b><pre>").append(value).append("</pre></b>");
		buffer.append("</p>");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover#setEditor(org.eclipse.ui.IEditorPart)
	 */
	public void setEditor(IEditorPart editor) {
		if (editor != null) {
			fEditor = editor;
			// initialize selection
			Runnable r = new Runnable() {
				public void run() {
					IWorkbenchPage page = fEditor.getSite().getPage();
					page.addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, PDebugTextHover.this);
					page.addPartListener(PDebugTextHover.this);
					fSelection = page.getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
				}
			};
			PTPDebugUIPlugin.getStandardDisplay().asyncExec(r);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		fSelection = selection;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(fEditor)) {
			IWorkbenchPage page = fEditor.getSite().getPage();
			page.removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
			page.removePartListener(this);
			fSelection = null;
			fEditor = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {}

	/** Get stack frame from selected element
	 * @return
	 */
	protected IPStackFrame getFrame() {
		if (fSelection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection)fSelection;
			if (selection.size() == 1) {
				Object el = selection.getFirstElement();
				if (el instanceof IAdaptable) {
					return (IPStackFrame)((IAdaptable)el).getAdapter(IPStackFrame.class);
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return null;
	}

	/** Convert symbol to support html format
	 * @param string
	 * @return
	 */
	private static String makeHTMLSafe(String string) {
		StringBuffer buffer = new StringBuffer(string.length());
		for(int i = 0; i != string.length(); i++) {
			char ch = string.charAt(i);
			switch(ch) {
				case '&':
					buffer.append("&amp;");
					break;
				case '<':
					buffer.append("&lt;");
					break;
				case '>':
					buffer.append("&gt;");
					break;
				default:
					buffer.append(ch);
					break;
			}
		}
		return buffer.toString();
	}
}
