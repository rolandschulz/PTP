/*******************************************************************************
 * Copyright (c) 2005, 2010 The Regents of the University of California and others. 
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
 * 
 * Contributors:
 *     LANL - Initial Implementation
 *     Dieter Krachtus, University of Heidelberg
 *     Roland Schulz, University of Tennessee
 *******************************************************************************/

package org.eclipse.ptp.proxy.event;

public abstract class AbstractProxyEvent implements IProxyEvent {
	private final int eventID;
	private int transactionID;
	private final String[] attributes;

	public AbstractProxyEvent(int eventID, int transactionID) {
		this(eventID, transactionID, null);
	}

	public AbstractProxyEvent(int eventID, int transactionID, String[] attrs) {
		this.eventID = eventID;
		this.transactionID = transactionID;
		this.attributes = attrs;
	}

	/**
	 * @since 4.0
	 */
	public AbstractProxyEvent(int eventID, String[] attrs) {
		this(eventID, -1, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEvent#getAttributes()
	 */
	public String[] getAttributes() {
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEvent#getEventID()
	 */
	public int getEventID() {
		return eventID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEvent#getTransactionID()
	 */
	public int getTransactionID() {
		return transactionID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEvent#setTransactionID(int)
	 */
	public void setTransactionID(int transactionID) {
		this.transactionID = transactionID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = eventID + " transid=" + getTransactionID(); //$NON-NLS-1$
		if (attributes != null) {
			str += " ("; //$NON-NLS-1$
			for (int i = 0; i < attributes.length; i++) {
				if (i > 0) {
					str += ","; //$NON-NLS-1$
				}
				str += attributes[i];
			}
			str += ")"; //$NON-NLS-1$
		}
		return str;
	}

}
