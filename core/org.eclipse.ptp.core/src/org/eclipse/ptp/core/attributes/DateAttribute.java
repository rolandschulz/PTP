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
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateAttribute extends AbstractAttribute {

	/**
	 * @author rsqrd
	 *
	 */
	public interface IVisitor extends IAttributeVisitor {

		void visit(DateAttribute attribute);

	}

	private static DateFormat[] dateFormats = null;

	public static void main(String[] args) throws IAttribute.IllegalValue {
		Calendar cal = Calendar.getInstance();
		DateAttribute mda = new DateAttribute(
				new AttributeDescription("uniqId", "name", "desc"), cal);
		System.out.println(mda.toString());
		String str = mda.toString();
		cal.add(Calendar.MONTH, 2);
		System.out.println(mda.toString());
		mda.setValue(str);
		System.out.println(mda.toString());
	}

	private static DateFormat[] getDateFormats() {
		if (dateFormats != null) {
			return dateFormats;
		}
		Locale[] locals = DateFormat.getAvailableLocales();
		final int styles[] = { DateFormat.SHORT, DateFormat.MEDIUM,
				DateFormat.LONG, DateFormat.FULL };
		ArrayList dfs = new ArrayList(styles.length * styles.length
				* locals.length);
		for (int i = 0; i < locals.length; ++i) {
			for (int ds = 0; ds < styles.length; ++ds) {
				final int dateStyle = styles[ds];
				for (int ts = 0; ts < styles.length; ++ts) {
					final int timeStyle = styles[ts];
					dfs.add(DateFormat.getDateTimeInstance(dateStyle,
							timeStyle, locals[i]));
				}
			}
		}
		System.out.println("There are " + dfs.size() + " date formats");
		dateFormats = (DateFormat[]) dfs.toArray(new DateFormat[dfs.size()]);
		return dateFormats;
	}

	private Date minDate = new Date(Long.MIN_VALUE);
	private Date maxDate = new Date(Long.MAX_VALUE);
	
	protected final DateFormat outputDateFormat;
	protected Calendar value;
	
	public DateAttribute(IAttributeDescription description, Calendar value) {
		this(description, value, DateFormat.getDateTimeInstance());
	}

	public DateAttribute(IAttributeDescription description, Calendar value,
			DateFormat outputDateFormat) {
		super(description);
		this.value = value;
		this.outputDateFormat = outputDateFormat;
	}

	public DateAttribute(IAttributeDescription description, Date date) {
		this(description, date, DateFormat.getDateTimeInstance());
	}

	public DateAttribute(IAttributeDescription description, Date date,
			DateFormat outputDateFormat) {
		super(description);
		this.outputDateFormat = outputDateFormat;
		this.value = Calendar.getInstance();
		this.value.setTime(date);
	}

	public DateAttribute(IAttributeDescription description, String string)
	throws IllegalValue {
		super(description);
		this.outputDateFormat = DateFormat.getDateTimeInstance();
		final Date date = parseString(string);
		if (date == null) {
			throw new IllegalValue("Unable to parse \"" + string
					+ "\" into a date");
		}
		this.value = Calendar.getInstance();
		this.value.setTime(date);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#accept(org.eclipse.ptp.core.attributes.IAttributeVisitor)
	 */
	public void accept(IAttributeVisitor visitor) {
		if (visitor instanceof IVisitor) {
			((IVisitor)visitor).visit(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#create(java.lang.String)
	 */
	public IAttribute create(String string) throws IllegalValue {
		final DateAttribute dateAttribute = new DateAttribute(getDescription(), string);
		dateAttribute.setValidRange(minDate, maxDate);
		return dateAttribute;
	}

	public boolean equals(Object obj) {
		if (obj instanceof DateAttribute) {
			DateAttribute attr = (DateAttribute) obj;
			return value.equals(attr.value);
		}
		return false;
	}
	
	public Calendar getCalendar() {
		return (Calendar) value.clone();
	}
	
	public Date getDate() {
		return value.getTime();
	}

	public Date getMaxDate() {
		return maxDate;
	}

	public Date getMinDate() {
		return minDate;
	}

	public String getStringRep() {
		return outputDateFormat.format(value.getTime());
	}

	public int hashCode() {
		return value.hashCode();
	}
	
	public boolean isValid(String string) {
		final Date date = parseString(string);
		if (date == null) {
			return false;
		}
		if (date.compareTo(minDate) < 0) {
			return false;
		}
		if (date.compareTo(maxDate) > 0) {
			return false;
		}
		return true;
	}
	
	public void setCalendar(Calendar calendar) throws IllegalValue {
		value.setTime(calendar.getTime());
	}

	public void setDate(Date date) throws IllegalValue {
		value.setTime(date);
		if (date.compareTo(minDate) < 0) {
			throw new IllegalValue("date specified is before min date");
		}
		if (date.compareTo(maxDate) > 0) {
			throw new IllegalValue("date specified is after max date");
		}
	}

	public void setValidRange(Date minDate, Date maxDate) throws IllegalValue {
		if (minDate == null) {
			this.minDate = new Date(Long.MIN_VALUE);
		}
		else {
			this.minDate = minDate;
		}
		if (maxDate == null) {
			this.maxDate = new Date(Long.MAX_VALUE);
		}
		else {
			this.maxDate = maxDate;
		}
		if (this.minDate.compareTo(this.maxDate) > 0) {
				throw new IllegalArgumentException("minDate must be less than or equal to maxDate");
		}
		Date date = getDate();
		try {
			if (date.compareTo(this.minDate) < 0) {
				setDate(this.minDate);
			}
			if (date.compareTo(this.maxDate) > 0) {
				setDate(this.maxDate);
			}
		} catch (IllegalValue e) {
			throw new IllegalValue("the set valid range of dates " +
					"does not include the current date");
		}
	}

	public void setValue(String string) throws IAttribute.IllegalValue {
		final Date date = parseString(string);
		if (date == null) {
			throw new IAttribute.IllegalValue("Unable to parse \"" + string
					+ "\" into a date");
		}
		if (date.compareTo(minDate) < 0) {
			throw new IllegalValue("Date, " + string + ", is before " + toString(minDate));
		}
		if (date.compareTo(maxDate) > 0) {
			throw new IllegalValue("Date, " + string + ", is after " + toString(maxDate));
		}
		value.setTime(date);
	}

	private Date parseString(String string) {
		Date date = null;
		final ParsePosition parsePosition = new ParsePosition(0);
		date = outputDateFormat.parse(string, parsePosition);
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

	private String toString(Date date) {
		return outputDateFormat.format(date);
	}

	protected int doCompareTo(AbstractAttribute arg0) {
		DateAttribute da = (DateAttribute) arg0;
		return this.value.compareTo(da.value);
	}

}
