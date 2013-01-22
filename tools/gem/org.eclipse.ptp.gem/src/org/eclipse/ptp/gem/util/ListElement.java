/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
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

package org.eclipse.ptp.gem.util;

import org.eclipse.core.resources.IFile;

public class ListElement {

	private final IFile file;
	private final String listEntry;
	private final int lineNumber;
	private final boolean isCollective;

	/**
	 * Constructor
	 * 
	 * @param file
	 *            The file resource for this ListElement.
	 * @param listEntry
	 *            The full String representing this ListElement's data.
	 * @param lineNumber
	 *            The associated line number in this ListElement's file resource
	 * @param isCollective
	 *            Whether or not this ListElement is a collective operation.
	 */
	public ListElement(IFile file, String listEntry, int lineNumber, boolean isCollective) {
		this.file = file;
		this.listEntry = listEntry;
		this.lineNumber = lineNumber;
		this.isCollective = isCollective;
	}

	/**
	 * Checks whether or not this ListElement is equal to the specified element.
	 * 
	 * @param otherListELement
	 *            The ListItem to compare with this ListItem.
	 * @return boolean True if this ListElement is equal to the other, false
	 *         otherwise.
	 */
	public boolean equals(ListElement otherListELement) {
		return (this.file.equals(otherListELement.file)
				&& this.listEntry.equals(otherListELement.listEntry) && this.lineNumber == otherListELement.lineNumber);
	}

	/**
	 * Returns the file resource for this ListElement.
	 * 
	 * @param none
	 * @return IFile The file resource.
	 */
	public IFile getFile() {
		return this.file;
	}

	/**
	 * Returns the line number in the associated file resource for this
	 * ListElement.
	 * 
	 * @param none
	 * @return int The line number.
	 */
	public int getLineNumber() {
		return this.lineNumber;
	}

	/**
	 * Returns the full String representing this ListElement's data.
	 * 
	 * @param none
	 * @return String The list entry.
	 */
	public String getListEntry() {
		return this.listEntry;
	}

	/**
	 * Returns whether or not the call associated with the list entry in this
	 * ListElement is a collective or not.
	 * 
	 * @param none
	 * @return boolean True if the call associated with the list entry in this
	 *         ListElement is a collective, false otherwise.
	 */
	public boolean isCollective() {
		return this.isCollective;
	}

	/**
	 * Returns the string representation of this ListElement.
	 * 
	 * @param none
	 * @return String The string representation of this ListElement.
	 */
	@Override
	public String toString() {
		return this.listEntry;
	}

}
