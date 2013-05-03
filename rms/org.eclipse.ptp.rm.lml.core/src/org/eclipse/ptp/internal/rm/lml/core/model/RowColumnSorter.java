/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class allows to reorder Lists of any type.
 * It assumes the list to be ordered in a 2D-array
 * with a given amount of columns. This list is
 * reordered by the reorder-function. This function
 * changes the order of rows and columns within this
 * virtual array.
 * 
 * This class is developed to help nodedisplays in
 * the ui-plugin. It supports the nodedisplay by
 * putting node-elements into different orderings.
 * 
 * @param <T>
 *            datatype, of which the elements inside the list are, which
 *            is reordered by this class
 */
public class RowColumnSorter<T> {

	/**
	 * List, which stores the original ordering of elements.
	 */
	private final List<T> originList;

	/**
	 * Create the sorter by passing the default ordered
	 * list.
	 * 
	 * @param list
	 *            list, which will be reordered by this instance
	 */
	public RowColumnSorter(List<T> list) {
		originList = list;
	}

	/**
	 * Creates a copy of the original list.
	 * This function assumes the list to be in a virtual
	 * 2D array with a column amount of cols. Then the rows
	 * and columns are reordered according to the given parameters.
	 * If highestRowFirst is true, the highest column will be
	 * inserted into the resulting list at first. The second highest
	 * row will be inserted as second and so forth. highestColFirst
	 * works in a similar way but for reordering the columns.
	 * 
	 * Example: Assume you have the list (1,2,3,4,5,6,7,8)
	 * This list is converted into a virtual array with 3 columns
	 * 1,2,3
	 * 4,5,6
	 * 7,8
	 * 
	 * Now this function is called.
	 * 
	 * reorder(true, true, 3) will lead to following result:
	 * 8,7,6
	 * 5,4,3
	 * 2,1
	 * The function should return the list (8,7,6,5,4,3,2,1)
	 * 
	 * reorder(true, false, 3) will lead to following result:
	 * 6,7,8
	 * 3,4,5
	 * 1,2
	 * The function should return the list (6,7,8,3,4,5,1,2)
	 * 
	 * reorder(false, true, 3) will lead to following result:
	 * 3,2,1
	 * 6,5,4
	 * 8,7
	 * The function should return the list (3,2,1,6,5,4,8,7)
	 * 
	 * reorder(false, false, 3) will lead to following result:
	 * 1,2,3
	 * 4,5,6
	 * 7,8
	 * The function should return the list (1,2,3,4,5,6,7,8)
	 * 
	 * @param highestRowFirst
	 *            if true, the last row will be inserted at first
	 * @param highestColFirst
	 *            if true, the last column will be inserted at first
	 * @param cols
	 *            amount of columns
	 * @return copied reorderer original list
	 */
	public List<T> reorder(boolean highestRowFirst, boolean highestColFirst, int cols) {
		final List<T> result = new ArrayList<T>();
		final List<T> list = new ArrayList<T>(originList);

		if (highestRowFirst && !highestColFirst) {
			revertList(list);

			highestRowFirst = false;
			highestColFirst = true;
		}

		// highest row in the virtual array
		int maxRow = list.size() / cols - 1;
		if (list.size() % cols > 0) {
			maxRow++;
		}

		for (int i = 0; i < cols * (maxRow + 1); i++) {
			// Calculate in which row and column of a virtual 2D array
			// this index would be
			final int row = i / cols;
			final int col = i % cols;

			int searchedRow = row;
			int searchedCol = col;

			// Do the reordering
			if (highestRowFirst) {
				searchedRow = maxRow - row;
			}

			// Highest col within this row
			int maxCol = cols - 1;
			if (searchedRow == maxRow) {
				maxCol = (list.size() - 1) % cols;
			}

			if (highestColFirst) {
				searchedCol = maxCol - col;
			}
			// Check boundaries
			if (searchedCol < 0 || searchedCol > maxCol)
				continue;

			final int index = searchedRow * cols + searchedCol;
			result.add(list.get(index));
		}

		return result;
	}

	/**
	 * Take the list and revert all elements within this list.
	 * 
	 * @param list
	 *            list, which is reverted
	 */
	private void revertList(List<T> list) {
		for (int i = 0; i < list.size() / 2; i++) {
			final T tmp = list.get(i);
			// Index, with which element i is switched
			final int index = list.size() - 1 - i;

			list.set(i, list.get(index));
			list.set(index, tmp);
		}
	}
}
