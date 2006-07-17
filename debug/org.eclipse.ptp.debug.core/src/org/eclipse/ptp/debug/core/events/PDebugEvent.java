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
package org.eclipse.ptp.debug.core.events;

import java.util.EventObject;

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
		if ((kind & (RESUME | SUSPEND | CREATE | TERMINATE | CHANGE | ERROR | MODEL_SPECIFIC)) == 0)
			throw new IllegalArgumentException("Debug event illegal kind"); 
		if (kind != MODEL_SPECIFIC && detail != UNSPECIFIED && (detail & (STEP_END | STEP_INTO | STEP_OVER | STEP_RETURN | BREAKPOINT | CLIENT_REQUEST |EVALUATION | EVALUATION_IMPLICIT | STATE | CONTENT | DEBUGGER | REGISTER | ERR_NORMAL | ERR_WARNING | ERR_FATAL)) == 0)
			throw new IllegalArgumentException("Debug event illegal detail");
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
		StringBuffer buf = new StringBuffer("DebugEvent[");
		if (getSource() != null) {
			buf.append(getSource().toString());
		} else {
			buf.append("null");
		}
		buf.append(", ");
		switch (getKind()) {
			case CREATE:
				buf.append("CREATE");
				break;
			case TERMINATE:
				buf.append("TERMINATE");
				break;
			case RESUME:
				buf.append("RESUME");
				break;
			case SUSPEND:
				buf.append("SUSPEND");
				break;				
			case CHANGE:
				buf.append("CHANGE");
				break;
			case UNSPECIFIED:
				buf.append("UNSPECIFIED");
				break;
			case MODEL_SPECIFIC:
				buf.append("MODEL_SPECIFIC");
				break;
		}
		buf.append(", ");
		switch (getDetail()) {
			case BREAKPOINT:
				buf.append("BREAKPOINT");
				break;
			case CLIENT_REQUEST:
				buf.append("CLIENT_REQUEST");
				break;
			case STEP_END:
				buf.append("STEP_END");
				break;
			case STEP_INTO:
				buf.append("STEP_INTO");
				break;
			case STEP_OVER:
				buf.append("STEP_OVER");
				break;
			case STEP_RETURN:
				buf.append("STEP_RETURN");
				break;
			case EVALUATION:
				buf.append("EVALUATION");
				break;
			case EVALUATION_IMPLICIT:
				buf.append("EVALUATION_IMPLICIT");
				break;								
			case STATE:
				buf.append("STATE");
				break;			
			case CONTENT:
				buf.append("CONTENT");
				break;					
			case UNSPECIFIED:
				buf.append("UNSPECIFIED");
				break;
			default:
				// model specific
				buf.append(getDetail());
				break;
		}
		buf.append("]");
		return buf.toString();
	}
}

