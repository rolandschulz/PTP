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

import java.util.Calendar;
import java.util.Date;

import org.eclipse.ptp.core.messages.Messages;

import com.ibm.icu.text.DateFormat;

@Deprecated
public final class DateAttributeDefinition extends AbstractAttributeDefinition<Calendar, DateAttribute, DateAttributeDefinition> {

	private Date minDate = new Date(Long.MIN_VALUE);
	private Date maxDate = new Date(Long.MAX_VALUE);

	private final DateFormat outputDateFormat;
	private final Date defaultValue;

	/**
	 * @since 4.0
	 */
	public DateAttributeDefinition(final String uniqueId, final String name, final String description, final boolean display,
			final Date defaultValue, final DateFormat outputDateFormat) {
		super(uniqueId, name, description, display);
		this.defaultValue = defaultValue;
		this.outputDateFormat = outputDateFormat;
	}

	/**
	 * @since 4.0
	 */
	public DateAttributeDefinition(final String uniqueId, final String name, final String description, final boolean display,
			final Date defaultValue, final DateFormat outputDateFormat, final Date min, final Date max)
			throws IllegalValueException {
		super(uniqueId, name, description, display);
		if (defaultValue.compareTo(getMinDate()) < 0) {
			throw new IllegalValueException(Messages.DateAttributeDefinition_0);
		}
		if (defaultValue.compareTo(getMaxDate()) > 0) {
			throw new IllegalValueException(Messages.DateAttributeDefinition_1);
		}
		this.defaultValue = defaultValue;
		this.outputDateFormat = outputDateFormat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#create(java.lang.String)
	 */
	public DateAttribute create() throws IllegalValueException {
		return new DateAttribute(this, defaultValue);
	}

	public DateAttribute create(String value) throws IllegalValueException {
		return new DateAttribute(this, value);
	}

	/**
	 * @since 4.0
	 */
	public DateFormat getDateFormat() {
		return outputDateFormat;
	}

	public synchronized Date getMaxDate() {
		return maxDate;
	}

	public synchronized Date getMinDate() {
		return minDate;
	}

	public synchronized void setValidRange(Date minDate, Date maxDate) throws IllegalValueException {
		if (minDate == null) {
			this.minDate = new Date(Long.MIN_VALUE);
		} else {
			this.minDate = minDate;
		}
		if (maxDate == null) {
			this.maxDate = new Date(Long.MAX_VALUE);
		} else {
			this.maxDate = maxDate;
		}
		if (this.minDate.compareTo(this.maxDate) > 0) {
			throw new IllegalArgumentException(Messages.DateAttributeDefinition_2);
		}
	}
}
