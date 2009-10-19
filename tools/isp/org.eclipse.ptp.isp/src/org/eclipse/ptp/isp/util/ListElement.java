/*******************************************************************************
 * Copyright (c) 2009 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.isp.util;

public class ListElement {

	public String fullFileName;
	public String entry;
	public int line;
	public boolean isCollective;

	/**
	 * CTOR
	 * 
	 * @param _fullFileName
	 *            The full name of the file.
	 * @param _entry
	 *            The files element entry.
	 * @param _line
	 *            The line of code associated with this ListElement.
	 * @param _isCollective
	 *            whether this ListElement is a collective operation or not.
	 */
	public ListElement(String _fullFileName, String _entry, int _line,
			boolean _isCollective) {
		this.fullFileName = _fullFileName;
		this.entry = _entry;
		this.line = _line;
		this.isCollective = _isCollective;
	}

	/**
	 * Returns the string representation of this ListElement.
	 * 
	 * @param none
	 * @return String the string representation of this ListElement.
	 */
	@Override
	public String toString() {
		return this.entry;
	}

	/**
	 * 
	 * @param other
	 *            The ListItem to compare with this ListItem.
	 * @return boolean True if this ListElement is equal to other, false
	 *         otherwise.
	 */
	public boolean equals(ListElement other) {
		return (this.fullFileName.equals(other.fullFileName)
				&& this.entry.equals(other.entry) && this.line == other.line);
	}

}
