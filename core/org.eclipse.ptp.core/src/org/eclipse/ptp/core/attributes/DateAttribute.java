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
package org.eclipse.ptp.core.attributes;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateAttribute implements IAttribute {
	
	private final MutableDateAttribute value;

	public DateAttribute(IAttributeDescription description, Calendar value) {
		this(description, value, DateFormat.getDateTimeInstance());
	}

	public DateAttribute(IAttributeDescription description, Calendar value,
			DateFormat outputDateFormat) {
		this.value = new MutableDateAttribute(description, value, outputDateFormat);
	}

	public DateAttribute(IAttributeDescription description, Date date) {
		this(description, date, DateFormat.getDateTimeInstance());
	}

	public DateAttribute(IAttributeDescription description, Date date,
			DateFormat outputDateFormat) {
		value = new MutableDateAttribute(description, date, outputDateFormat);
	}

	public int compareTo(Object arg0) {
		DateAttribute attr = (DateAttribute) arg0;
		return value.compareTo(attr.value);
	}

	public boolean equals(Object obj) {
		if (obj instanceof DateAttribute) {
			DateAttribute attr = (DateAttribute) obj;
			return this.value.equals(attr.value);
		}
		return false;
	}

	public Calendar getCalendar() {
		return value.getCalendar();
	}

	public Date getDate() {
		return value.getDate();
	}

	public IAttributeDescription getDescription() {
		return value.getDescription();
	}

	public String getStringRep() {
		return value.getStringRep();
	}

	public int hashCode() {
		return value.hashCode();
	}

	public String toString() {
		return value.toString();
	}

}
