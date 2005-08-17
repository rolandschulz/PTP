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

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;

/**
 * @author Clement chu
 *
 */
public class PInstructionPointerAnnotation extends Annotation {
	private IStackFrame fStackFrame;
	
	public PInstructionPointerAnnotation(IStackFrame stackFrame, boolean isTopFrame) {
		super(isTopFrame?IPTPDebugUIConstants.ANN_INSTR_POINTER_CURRENT:IPTPDebugUIConstants.ANN_INSTR_POINTER_SECONDARY, false, "asdadasdasdasd");
		fStackFrame = stackFrame;
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
}