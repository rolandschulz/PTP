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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Clement chu
 *
 */
public class SetbreakpointRulerAction extends Action {
	private IVerticalRulerInfo ruler = null;
	private IWorkbenchPart targetPart = null;
	private SetBreakpointAdapter setBreakpointAdapter = null;
	
	private static final ISelection EMPTY_SELECTION = new ISelection() {
		public boolean isEmpty() {
			return true;
		}
	};
    
    public SetbreakpointRulerAction(IWorkbenchPart part, IVerticalRulerInfo ruler) {
    		super("Set Breakpoint");
    		this.ruler = ruler;
    		setTargetPart(part);
    		setBreakpointAdapter = new SetBreakpointAdapter();
    		setId(IPTPDebugUIConstants.ACTION_SET_BREAKPOINT);
    }
	
    public void dispose() {
    		setTargetPart(null);
    		ruler = null;
    }
    
    public void run() {
    		System.out.println("Hello SetBreakpoint Ruler action");
    }

	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return ruler;
	}

	private IWorkbenchPart getTargetPart() {
		return targetPart;
	}

	private void setTargetPart(IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	private ISelection getTargetSelection() {
		IDocument doc = getDocument();
		if ( doc != null ) {
			int line = getVerticalRulerInfo().getLineOfLastMouseButtonActivity();
			try {
				IRegion region = doc.getLineInformation( line );
				return new TextSelection( doc, region.getOffset(), region.getLength() );
			}
			catch( BadLocationException e ) {
				DebugPlugin.log( e );
			} 
		}
		return EMPTY_SELECTION;
	}
	
	private IDocument getDocument() {
		IWorkbenchPart targetPart = getTargetPart();
		if ( targetPart instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)targetPart; 
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if ( provider != null )
				return provider.getDocument( textEditor.getEditorInput() );
		}
		return null;
	}
}
