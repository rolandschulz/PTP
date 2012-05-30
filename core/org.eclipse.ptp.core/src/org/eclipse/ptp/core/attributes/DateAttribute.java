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

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.ptp.core.messages.Messages;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.ULocale;

@Deprecated
public class DateAttribute extends AbstractAttribute<Calendar, DateAttribute, DateAttributeDefinition> {

	private static DateFormat[] dateFormats = null;

	private static DateFormat[] getDateFormats() {
		if (dateFormats != null) {
			return dateFormats;
		}
		ULocale[] locals = DateFormat.getAvailableULocales();
		final int styles[] = { DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL };
		ArrayList<DateFormat> dfs = new ArrayList<DateFormat>(styles.length * styles.length * locals.length);
		for (int i = 0; i < locals.length; ++i) {
			for (int ds = 0; ds < styles.length; ++ds) {
				final int dateStyle = styles[ds];
				for (int ts = 0; ts < styles.length; ++ts) {
					final int timeStyle = styles[ts];
					dfs.add(DateFormat.getDateTimeInstance(dateStyle, timeStyle, locals[i]));
				}
			}
		}
		dateFormats = dfs.toArray(new DateFormat[dfs.size()]);
		return dateFormats;
	}

	public static void main(String[] args) throws IllegalValueException {
		Calendar cal = Calendar.getInstance();
		DateAttributeDefinition def = new DateAttributeDefinition(
				"uniqId", "name", "desc", true, cal.getTime(), DateFormat.getDateTimeInstance()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DateAttribute mda = def.create();
		mda.setValue(cal);
		System.out.println(mda.toString());
		String str = mda.toString();
		cal.add(Calendar.MONTH, 2);
		System.out.println(mda.toString());
		mda.setValueAsString(str);
		System.out.println(mda.toString());
	}

	protected final Calendar value = Calendar.getInstance();

	public DateAttribute(DateAttributeDefinition definition, Date initialValue) throws IllegalValueException {
		super(definition);
		setValue(initialValue);
	}

	public DateAttribute(DateAttributeDefinition definition, String initialValue) throws IllegalValueException {
		super(definition);
		setValueAsString(initialValue);
	}

	@Override
	protected int doCompareTo(DateAttribute other) {
		return value.compareTo(other.value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.AbstractAttribute#doClone()
	 */
	/**
	 * @since 4.0
	 */
	@Override
	protected DateAttribute doCopy() {
		try {
			return new DateAttribute(getDefinition(), value.getTime());
		} catch (IllegalValueException e) {
			// shouldn't happen
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean doEquals(DateAttribute other) {
		return value.equals(other.value);
	}

	@Override
	protected int doHashCode() {
		return value.hashCode();
	}

	/**
	 * @since 4.0
	 */
	public DateFormat getDateFormat() {
		return getDefinition().getDateFormat();
	}

	public Date getDateValue() {
		return value.getTime();
	}

	private Date getMaxDate() {
		return getDefinition().getMaxDate();
	}

	private Date getMinDate() {
		return getDefinition().getMinDate();
	}

	public Calendar getValue() {
		return (Calendar) value.clone();
	}

	public String getValueAsString() {
		return getDateFormat().format(value);
	}

	public boolean isValid(String string) {
		final Date date = parseString(string);
		if (date == null) {
			return false;
		}
		if (date.compareTo(getMinDate()) < 0) {
			return false;
		}
		if (date.compareTo(getMaxDate()) > 0) {
			return false;
		}
		return true;
	}

	private Date parseString(String string) {
		Date date = null;
		final ParsePosition parsePosition = new ParsePosition(0);
		date = getDateFormat().parse(string, parsePosition);
		if (date != null) {
			return date;
		}
		for (int i = 0; i < getDateFormats().length; ++i) {
			parsePosition.setIndex(0);
			date = getDateFormats()[i].parse(string, parsePosition);
			if (date != null) {
				return date;
			}
		}
		return date;
	}

	public void setValue(Calendar calendar) throws IllegalValueException {
		setValue(calendar.getTime());
	}

	public void setValue(Date date) throws IllegalValueException {
		value.setTime(date);
		if (date.compareTo(getMinDate()) < 0) {
			throw new IllegalValueException(Messages.DateAttribute_0);
		}
		if (date.compareTo(getMaxDate()) > 0) {
			throw new IllegalValueException(Messages.DateAttribute_1);
		}
	}

	public void setValueAsString(String string) throws IllegalValueException {
		final Date date = parseString(string);
		if (date == null) {
			throw new IllegalValueException(Messages.DateAttribute_2 + string + Messages.DateAttribute_3);
		}
		if (date.compareTo(getMinDate()) < 0) {
			throw new IllegalValueException(Messages.DateAttribute_4 + string + Messages.DateAttribute_5 + toString(getMinDate()));
		}
		if (date.compareTo(getMaxDate()) > 0) {
			throw new IllegalValueException(Messages.DateAttribute_6 + string + Messages.DateAttribute_7 + toString(getMaxDate()));
		}
		value.setTime(date);
	}

	@Override
	public String toString() {
		return getValueAsString();

	}

	private String toString(Date date) {
		return getDateFormat().format(date);
	}

}
