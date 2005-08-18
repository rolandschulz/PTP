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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Clement chu
 *
 */
public class PInstructionPointerAnnotation extends MarkerAnnotation {
	private IStackFrame fStackFrame;

	public PInstructionPointerAnnotation(IMarker marker, IStackFrame stackFrame) {
		super(marker);
		fStackFrame = stackFrame;
		setMessage(getMessageFromStack());
	}
	
	public void setMessage(String message) {
		try {
			getMarker().setAttribute(IMarker.MESSAGE, message);
		} catch (CoreException e) {}
		setText(message);
	}
	
	public boolean equals(Object other) {
		if (other instanceof PInstructionPointerAnnotation) {
			return getStackFrame().equals(((PInstructionPointerAnnotation)other).getStackFrame());			
		}
		return false;
	}
	
	public int hashCode() {
		return getStackFrame().hashCode();
	}

	private IStackFrame getStackFrame() {
		return fStackFrame;
	}
	
	private String getMessageFromStack() {
		try {
			return fStackFrame.getName();
		} catch (DebugException e) {
			return "";
		}
	}
}