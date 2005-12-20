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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ptp.debug.core.model.IPStackFrame;

/**
 * @author Clement chu
 * 
 */
public class PDebugUIUtils {
	static public IRegion findWord(IDocument document, int offset) {
		int start = -1;
		int end = -1;
		try {
			int pos = offset;
			char c;
			while(pos >= 0) {
				c = document.getChar(pos);
				//TODO check java char?
				if (!Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}
			start = pos;
			pos = offset;
			int length = document.getLength();
			while(pos < length) {
				c = document.getChar(pos);
				//TODO check java char?
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}
			end = pos;
		}
		catch(BadLocationException x) {
		}
		if (start > -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}
		return null;
	}

	static public IPStackFrame getCurrentStackFrame() {
		IAdaptable context = DebugUITools.getDebugContext();
		return ( context != null ) ? (IPStackFrame)context.getAdapter( IPStackFrame.class ) : null;
	}
}
