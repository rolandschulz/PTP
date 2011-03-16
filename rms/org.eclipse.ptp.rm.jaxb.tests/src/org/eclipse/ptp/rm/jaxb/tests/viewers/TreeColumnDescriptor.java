/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - modifications
 *  M Venkataramana - original code: http://eclipse.dzone.com/users/venkat_r_m
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.tests.viewers;

public class TreeColumnDescriptor {
	public static char TEXT = 't';
	public static char COMBO = 'c';
	public static char BUTTON = 'b';

	private static Integer DEFAULT_OPTION = -1;

	private final char type;
	private String columnName;
	private int width = -1;
	private String[] options = new String[0];

	public TreeColumnDescriptor(char type, String columnName) {
		this.type = type;
		this.columnName = columnName;
	}

	public TreeColumnDescriptor(char type, String columnName, int width) {
		this.type = type;
		this.columnName = columnName;
		this.width = width;
	}

	public TreeColumnDescriptor(char type, String columnName, String[] options) {
		this.type = type;
		this.columnName = columnName;
		this.options = options;
	}

	public TreeColumnDescriptor(char type, String columnName, String[] options, int width) {
		this.type = type;
		this.columnName = columnName;
		this.options = options;
		this.width = width;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getOption(int optionIndex) {
		if (optionIndex == DEFAULT_OPTION) {
			return null;
		}

		if (optionIndex >= options.length) {
			throw new RuntimeException("Only " + options.length + " options are supplied for combo column: " + columnName
					+ ".\r\nBut framework is looking for option: " + optionIndex);
		}

		return options[optionIndex];
	}

	public Integer getOptionIndex(String optionName) {
		if (optionName == null) {
			return DEFAULT_OPTION;
		}

		for (int i = 0; i < options.length; i++) {
			if (optionName.equals(options[i])) {
				return i;
			}
		}
		throw new IllegalArgumentException("No such option: " + optionName + " with column: " + columnName);
	}

	public String[] getOptions() {
		return options;
	}

	public char getType() {
		return type;
	}

	public int getWidth() {
		return width;
	}

	public boolean isButton() {
		return type == BUTTON;
	}

	public boolean isCombo() {
		return type == COMBO;
	}

	public boolean isText() {
		return type == TEXT;
	}

	public boolean isWidthSpecified() {
		return width != -1;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}
