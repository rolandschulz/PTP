/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.rm.core.attributes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.ptp.internal.rm.core.ResourceManagerLog;

/**
 * Create date-valued attributes
 * 
 * @author rsqrd
 * 
 */
public class DateAttrDesc extends AbstractAttrDesc {

	// TODO we need to handle more than one format for the date string
	private final DateFormat dateFormat = new SimpleDateFormat(
			"MMM dd HH:mm:ss yyyy");

	public DateAttrDesc(String name, String description) {
		super(name, description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.attributes.AbstractAttrDesc#doCreateAttribute(java.lang.String)
	 */
	protected IAttrComponent doCreateAttribute(String attrString) {
		try {
			final Date date = dateFormat.parse(attrString);
			return new DateAttrComponent(date);
		} catch (ParseException e) {
			ResourceManagerLog.logError("creating date attribute", e);
			return null;
		}
	}

}
