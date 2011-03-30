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
package org.eclipse.ptp.rm.jaxb.ui.data;

import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;

public class ColumnDescriptor implements IJAXBUINonNLSConstants {
	private static Integer DEFAULT_OPTION = -1;

	private final String columnName;
	private final int width;
	private final String[] options;

	public ColumnDescriptor(ColumnData data) {
		columnName = data.getName();
		width = data.getWidth();
		options = data.getOption().toArray(new String[0]);
	}

	public String getColumnName() {
		return columnName;
	}

	public String getOption(int optionIndex) {
		if (optionIndex == DEFAULT_OPTION) {
			return null;
		}

		if (optionIndex >= options.length) {
			throw new RuntimeException(Messages.InvalidOptionIndex + columnName + CM + SP + optionIndex);
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
		throw new IllegalArgumentException(Messages.InvalidOption + columnName + CM + SP + optionName);
	}

	public String[] getOptions() {
		return options;
	}

	public int getWidth() {
		return width;
	}

	public boolean isWidthSpecified() {
		return width != -1;
	}
}
