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

package org.eclipse.ptp.rm.lml.core.model;

import org.eclipse.ptp.internal.rm.lml.core.model.LMLCheck;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement;

/**
 * Saves a mask for creating implicit names
 * calculates the length of the output, which this mask will print out
 */
public class Mask {

	// Regular expression for a printed integer
	private static final String number = "\\s*(-|\\+)?(0x|0X)?([a-fA-F\\d]+)";//$NON-NLS-1$

	private String maskString;

	// regular expression which printouts must match for this mask
	private String regularMask;

	private int outputLength;

	// text-pattern before and behind the printed digit
	private String pre, post;

	// null, if no map-attribute specified, otherwise contains explicitly allowed names
	private String[] names;

	// scheme-element which contains mask-definition
	private final SchemeElement scheme;

	// contains numbers of elements defined by scheme (list=1,17,2 => numbers={1,17,2})
	private int[] numbers = null;

	/**
	 * @param scheme
	 *            scheme-element which contains the mask-definition
	 */
	public Mask(SchemeElement scheme) {

		this.scheme = scheme;

		if (scheme.getMap() != null) {
			names = scheme.getMap().split(","); //$NON-NLS-1$

			outputLength = -1;// variable, because name lengths can be variable

			regularMask = scheme.getMap().replace(',', '|');
			pre = ""; //$NON-NLS-1$
			post = ""; //$NON-NLS-1$
			maskString = ""; //$NON-NLS-1$

		}
		else {
			names = null;

			maskString = scheme.getMask();
			// regular expression in xsd (([^%])*%(\-|\+|\s|\#)*0(\-|\+|\s|\#)*(\d)+d([^%])*)|(([^%])*%(\-|\+|\s|\#)*d([^%])+)
			if (maskString.matches("([^%])*%(\\-|\\+|\\s|\\#)*d([^%])+")) {//$NON-NLS-1$
				// length is unknown but there is a separator specified
				outputLength = -1;
			}
			else {
				// there must be given a length within the mask
				// Find length by printing out the number 1 with that mask
				outputLength = String.format(maskString, 1).length();
			}

			// create regular expression for this mask
			final int perCent = maskString.indexOf('%');
			// Search the first 'd'-character after the '%' character
			final int dPos = maskString.indexOf('d', perCent);

			pre = maskString.substring(0, perCent);
			post = maskString.substring(dPos + 1, maskString.length());
			regularMask = pre + number + post;
		}

	}

	/**
	 * Inverse function of getNumberOfLevelstring, id is given and searched is the implicit name
	 * of the component with that id on this level.
	 * 
	 * @param id
	 * @return implicit name of element with given id
	 */
	public String getImplicitLevelName(int id) {
		return LMLCheck.getLevelName(scheme, id);
	}

	public String getMask() {
		return maskString;
	}

	/**
	 * Input is the part of the implicit name printed by this mask
	 * 
	 * @param levelString
	 *            output part of implicit name
	 * @return id-nr for current level or -1, if id could not be parsed
	 */
	public int getNumberOfLevelString(String levelString) {

		if (names == null) {

			if (!isOutputAllowed(levelString)) {
				return -1;// For robustness of this function
			}

			levelString = levelString.substring(pre.length()); // cut pre-text from inception
			levelString = levelString.substring(0, levelString.length() - post.length()); // cut post-text from end of levelstring

			final char[] chars = levelString.toCharArray();

			int i = 0;
			while (i < chars.length && chars[i] < '0' || chars[i] > '9') {
				i++;
			}

			// No digit at all?
			if (i == chars.length) {
				return -1;
			}

			String number = String.valueOf(chars[i]);
			i++;
			while (i < chars.length && chars[i] >= '0' && chars[i] <= '9') {
				number += chars[i];
				i++;
			}

			return Integer.parseInt(number);

		}
		else {// special names defined, search position of levelstring within names, map position to id
			int pos = 0;
			for (final String name : names) {
				if (name.equals(levelString)) {

					if (scheme.getList() != null) {

						if (numbers == null) {
							numbers = LMLCheck.getNumbersFromNumberlist(scheme.getList());
						}

						return numbers[pos];
					}
					else {
						return scheme.getMin().intValue() + scheme.getStep().intValue() * pos;
					}
				}
				pos++;
			}
		}
		// No matches
		return -1;
	}

	public int getOutputLength() {
		return outputLength;
	}

	/**
	 * Tests output against regular expression given by the mask
	 * Moreover outputlength must match the length of this output
	 * 
	 * @param output
	 *            number-output probably created through this mask
	 * @return true if output is allowed, false otherwise
	 */
	public boolean isOutputAllowed(String output) {
		if (names == null) {
			return output.matches(regularMask) && (outputLength == -1 || outputLength == output.length());
		}
		// Just test if output is equal to one of the names
		for (final String name : names) {
			if (name.equals(output)) {
				return true;
			}
		}

		return false;
	}

}
