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

public final class MutableDateAttribute extends AbstractAttribute implements
		IMutableAttribute {

	private static DateFormat[] dateFormats = null;

	public static void main(String[] args) throws IllegalValue {
		Calendar cal = Calendar.getInstance();
		MutableDateAttribute mda = new MutableDateAttribute(
				new AttributeDescription("name", "desc"), cal);
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

	protected final DateFormat outputDateFormat;
	protected Calendar value;

	public MutableDateAttribute(IAttributeDescription description, Calendar value) {
		this(description, value, DateFormat.getDateTimeInstance());
	}

	public MutableDateAttribute(IAttributeDescription description, Calendar value,
			DateFormat outputDateFormat) {
		super(description);
		this.value = value;
		this.outputDateFormat = outputDateFormat;
	}

	public MutableDateAttribute(IAttributeDescription description, Date date) {
		this(description, date, DateFormat.getDateTimeInstance());
	}

	public MutableDateAttribute(IAttributeDescription description, Date date,
			DateFormat outputDateFormat) {
		super(description);
		this.outputDateFormat = outputDateFormat;
		this.value = Calendar.getInstance();
		this.value.setTime(date);
	}

	public boolean equals(Object obj) {
		if (obj instanceof MutableDateAttribute) {
			MutableDateAttribute attr = (MutableDateAttribute) obj;
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
		return true;
	}

	public void setValue(String string) throws IllegalValue {
		final Date date = parseString(string);
		if (date == null) {
			throw new IllegalValue("Unable to parse \"" + string
					+ "\" into a date");
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

	protected int doCompareTo(AbstractAttribute arg0) {
		MutableDateAttribute da = (MutableDateAttribute) arg0;
		return this.value.compareTo(da.value);
	}

}
