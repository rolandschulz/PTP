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

package org.eclipse.ptp.proxy.event;

public abstract class AbstractProxyEvent implements IProxyEvent {
	private int			eventID;
	private int			transactionID;
	private String[]	attributes;
	
	public AbstractProxyEvent(int eventID, int transactionID, String[] attrs) {
		this.eventID		= eventID;
		this.transactionID	= transactionID;
		this.attributes		= attrs;
	}
	
	public AbstractProxyEvent(int eventID, int transactionID) {
		this(eventID, transactionID, null);
	}
	
	public int getEventID() {
		return eventID;
	}

	public int getTransactionID() {
		return transactionID;
	}
	
	public String[] getAttributes() {
		return attributes;
	}

	public String toString() {
		String str = eventID + " transid=" + getTransactionID(); //$NON-NLS-1$
		if (attributes != null) {
			str += " ("; //$NON-NLS-1$
			for (int i = 0 ; i < attributes.length; i++) {
				if (i > 0)
					str += ","; //$NON-NLS-1$
				str += attributes[i];
			}
			str += ")"; //$NON-NLS-1$
		}
		return str;
	}

}
