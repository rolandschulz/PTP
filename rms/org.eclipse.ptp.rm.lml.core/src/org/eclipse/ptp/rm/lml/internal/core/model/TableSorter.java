/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.internal.core.model;

import java.util.Comparator;

import org.eclipse.ptp.rm.lml.internal.core.elements.RowType;

/**
 * Comparator for an array of elements of RowType.
 * 
 */
public class TableSorter implements Comparator<RowType> {

	/*
	 * The comparison value
	 */
	private final int up;

	/*
	 * The index of the sort column
	 */
	private final int sortIndex;

	/*
	 * The direction of the sorting (UP or DOWN)
	 */
	private final int sortDirection;

	/*
	 * The type of the sorting (date, numerix or alpha)
	 */
	private final String sortType;

	/**
	 * Constructor
	 * 
	 * @param sortType
	 *            type how the method should sort
	 * @param up
	 *            comparison value (value for sorting upwards)
	 * @param sortIndex
	 *            index of the to sorting column
	 * @param sortDirection
	 *            value of the to sorting direction
	 */
	public TableSorter(String sortType, int up, int sortIndex, int sortDirection) {
		this.up = up;
		this.sortIndex = sortIndex;
		this.sortDirection = sortDirection;
		this.sortType = sortType;
	}

	/**
	 * Compares two arguments of the RowType for order.
	 * If their are equal, the method returns 0. Returns -1, 0 or 1 as the first argument is less than, equal to, or greater than
	 * the second.
	 * 
	 * @param a
	 *            first element
	 * @param b
	 *            second element
	 * @return integer which represents the result of the comparison
	 */
	public int compare(RowType a, RowType b) {
		if (a.getCell().get(sortIndex) == null || b.getCell().get(sortIndex) == null) {
			return 0;
		}
		// The two elements are equal
		if (a.getCell().get(sortIndex).getValue().equals(b.getCell().get(sortIndex).getValue())) {
			return 0;
		}
		// The two elements are not equal
		if (sortType.equals("numeric")) {
			// The sort type is a numeric one
			if (sortDirection == up) {
				return Integer.parseInt(a.getCell().get(sortIndex).getValue()) < Integer.parseInt(b.getCell().get(sortIndex)
						.getValue()) ? -1 : 1;
			} else {
				return Integer.parseInt(a.getCell().get(sortIndex).getValue()) < Integer.parseInt(b.getCell().get(sortIndex)
						.getValue()) ? 1 : -1;
			}
		} else {
			// The (default) sort type is a alphabetic one
			if (sortDirection == up) {
				return a.getCell().get(sortIndex).getValue().compareTo(b.getCell().get(sortIndex).getValue()) < 0 ? -1 : 1;
			} else {
				return a.getCell().get(sortIndex).getValue().compareTo(b.getCell().get(sortIndex).getValue()) < 0 ? 1 : -1;
			}
		}
	}

}
