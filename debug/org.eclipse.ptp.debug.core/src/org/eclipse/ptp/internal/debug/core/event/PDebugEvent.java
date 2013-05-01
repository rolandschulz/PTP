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
package org.eclipse.ptp.internal.debug.core.event;

import java.util.EventObject;

import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author Clement
 */
public final class PDebugEvent extends EventObject implements IPDebugEvent {
    private static final long serialVersionUID = 1L;
	/**
	 * The kind of event - one of the kind constants defined by
	 * this class.
	 */
	private int fKind= UNSPECIFIED;
	/**
	 * The detail of the event - one of the detail constants defined by
	 * this class.
	 */
	private int fDetail= UNSPECIFIED;

	private IPDebugInfo fInfo = null;
	
	public PDebugEvent(Object eventSource, int kind, int detail, IPDebugInfo info) {
		super(eventSource);
		if ((kind & (RESUME | SUSPEND | CREATE | TERMINATE | CHANGE | ERROR | PROCESS_SPECIFIC)) == 0)
			throw new IllegalArgumentException(Messages.PDebugEvent_0); 
		if (kind != PROCESS_SPECIFIC && detail != UNSPECIFIED && (detail & (STEP_END | STEP_INTO | STEP_OVER | STEP_RETURN | BREAKPOINT | CLIENT_REQUEST |EVALUATION | EVALUATION_IMPLICIT | STATE | CONTENT | DEBUGGER | REGISTER | ERR_NORMAL | ERR_WARNING | ERR_FATAL)) == 0)
			throw new IllegalArgumentException(Messages.PDebugEvent_1);
		fKind= kind;
		fDetail= detail;
		fInfo = info;
	}
	public int getKind() {
		return fKind;
	}
	public int getDetail() {
		return fDetail;
	}
	public IPDebugInfo getInfo() {
		return fInfo;
	}
	public boolean isStepStart() {
		return (getDetail() & (STEP_INTO | STEP_OVER | STEP_RETURN)) > 0;
	}
	public boolean isEvaluation() {
		return (getDetail() & (EVALUATION | EVALUATION_IMPLICIT)) > 0;
	}	
	public String toString() {
		StringBuffer buf = new StringBuffer("DebugEvent["); //$NON-NLS-1$
		if (getSource() != null) {
			buf.append(getSource().toString());
		} else {
			buf.append("null"); //$NON-NLS-1$
		}
		buf.append(", "); //$NON-NLS-1$
		switch (getKind()) {
			case CREATE:
				buf.append("CREATE"); //$NON-NLS-1$
				break;
			case TERMINATE:
				buf.append("TERMINATE"); //$NON-NLS-1$
				break;
			case RESUME:
				buf.append("RESUME"); //$NON-NLS-1$
				break;
			case SUSPEND:
				buf.append("SUSPEND"); //$NON-NLS-1$
				break;				
			case CHANGE:
				buf.append("CHANGE"); //$NON-NLS-1$
				break;
			case UNSPECIFIED:
				buf.append("UNSPECIFIED"); //$NON-NLS-1$
				break;
			case PROCESS_SPECIFIC:
				buf.append("PROCESS_SPECIFIC"); //$NON-NLS-1$
				break;
		}
		buf.append(", "); //$NON-NLS-1$
		switch (getDetail()) {
			case BREAKPOINT:
				buf.append("BREAKPOINT"); //$NON-NLS-1$
				break;
			case CLIENT_REQUEST:
				buf.append("CLIENT_REQUEST"); //$NON-NLS-1$
				break;
			case STEP_END:
				buf.append("STEP_END"); //$NON-NLS-1$
				break;
			case STEP_INTO:
				buf.append("STEP_INTO"); //$NON-NLS-1$
				break;
			case STEP_OVER:
				buf.append("STEP_OVER"); //$NON-NLS-1$
				break;
			case STEP_RETURN:
				buf.append("STEP_RETURN"); //$NON-NLS-1$
				break;
			case EVALUATION:
				buf.append("EVALUATION"); //$NON-NLS-1$
				break;
			case EVALUATION_IMPLICIT:
				buf.append("EVALUATION_IMPLICIT"); //$NON-NLS-1$
				break;								
			case STATE:
				buf.append("STATE"); //$NON-NLS-1$
				break;			
			case CONTENT:
				buf.append("CONTENT"); //$NON-NLS-1$
				break;					
			case UNSPECIFIED:
				buf.append("UNSPECIFIED"); //$NON-NLS-1$
				break;
			default:
				// model specific
				buf.append(getDetail());
				break;
		}
		buf.append("]"); //$NON-NLS-1$
		return buf.toString();
	}
}

