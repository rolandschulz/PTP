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
import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.internal.rm.lml.core.messages.Messages;
import org.eclipse.ptp.rm.lml.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.core.elements.SchemeType;
import org.eclipse.ptp.rm.lml.core.model.Mask;

/**
 * This class is capable of checking implicit names given by refid-attributes
 * faster than by creating all implicit names and then look if refid-values were
 * created
 */
public class FastImpCheck {

	private static ArrayList<ArrayList<Integer>> levelsList;

	/**
	 * searches for refid within referencing nodedisplay and return errors if
	 * these refids are not allowed concerning the scheme given by the base
	 * nodedisplay
	 * 
	 * @param base
	 * @param nodedisplay
	 * @return
	 */
	public static ErrorList checkRefids(Nodedisplay base, Nodedisplay nodedisplay, ErrorList errList) {

		ErrorList result;
		if (errList == null) {
			result = new ErrorList();
		} else {
			result = errList;
		}

		recCheckRefIds(nodedisplay.getData(), base.getScheme(), result);

		return result;
	}

	public static ArrayList<ArrayList<Integer>> getAllImpNameLevels(String impName, Object scheme) {

		levelsList = new ArrayList<ArrayList<Integer>>();
		impNameToAllPossibleLevel(impName, scheme, new ArrayList<Integer>());

		return levelsList;
	}

	/**
	 * transform implicit name into level-nrs if possible, generates all allowed
	 * solutions
	 * 
	 * only lower elements of scheme are interesting
	 * 
	 * @param impName
	 *            relative name, which might be defined in the given scheme
	 * @param scheme
	 *            any scheme-element on any level
	 * @param numbersList
	 *            Level-Numbers till now
	 * @return
	 */
	public static void impNameToAllPossibleLevel(String impName, Object scheme, ArrayList<Integer> numbersList) {

		final List<? extends SchemeElement> lowerList = LMLCheck.getLowerSchemeElements(scheme);
		for (final SchemeElement lowerElement : lowerList) {
			final Mask mask = new Mask(lowerElement);
			int i = 1;
			while (i <= impName.length() && !mask.isOutputAllowed(impName.substring(0, i))) {
				i++;
			}

			if (i > impName.length()) {
				// No part-string is allowed for this mask
				continue;
			}

			// Otherwise impname.substring(0, i) is allowed
			while (i <= impName.length() && mask.isOutputAllowed(impName.substring(0, i))) {
				final int id = mask.getNumberOfLevelString(impName.substring(0, i));
				numbersList.add(id);// add current number

				if (i == impName.length()) {// Solution found
					final ArrayList<Integer> copy = LMLCheck.copyArrayList(numbersList);
					levelsList.add(copy);
				} else {// Go deeper
					impNameToAllPossibleLevel(impName.substring(i), lowerElement, numbersList);
				}

				numbersList.remove(numbersList.size() - 1);// Remove number added on this
				// recursion-level
				i++;
			}

		}

	}

	/**
	 * transform implicit name into level-nrs if possible, otherwise return null
	 * 
	 * only lower elements of scheme are interesting
	 * 
	 * @param impName
	 *            relative name, which might be defined in the given scheme
	 * @param scheme
	 *            any scheme-element on any level
	 * @param numbersList
	 *            Level-Numbers till now
	 * @return
	 */
	public static ArrayList<Integer> impNameToOneLevel(String impName, Object scheme, ArrayList<Integer> numbersList) {

		final List<? extends SchemeElement> lowerList = LMLCheck.getLowerSchemeElements(scheme);
		for (final SchemeElement lowerElement : lowerList) {

			final Mask mask = new Mask(lowerElement);

			int i = 1;
			while (i <= impName.length() && !mask.isOutputAllowed(impName.substring(0, i))) {
				i++;
			}

			if (i > impName.length()) {
				// No part-string is allowed for this mask
				continue;
			}

			// Otherwise impname.substring(0, i) is allowed
			while (i <= impName.length() && mask.isOutputAllowed(impName.substring(0, i))) {

				final int id = mask.getNumberOfLevelString(impName.substring(0, i));

				// Check if this id is allowed within this scheme
				final ArrayList<Integer> idList = new ArrayList<Integer>();
				idList.add(id);
				if (LMLCheck.getSchemeByLevels(idList, scheme) == null) {
					i++;
					continue;
				}

				numbersList.add(id);// add current number

				if (i == impName.length()) {// Solution found
					return numbersList;
				} else {// Go deeper

					final ArrayList<Integer> result = impNameToOneLevel(impName.substring(i), lowerElement, numbersList);
					if (result != null) {
						return result;
					}
				}

				numbersList.remove(numbersList.size() - 1);
				// Remove number added on this recursion-level
				i++;
			}

		}

		return null;
	}

	/**
	 * Has to be called for nodedisplayviews begin level with 1 Searches all
	 * masks within the scheme-tag of the nodedisplay
	 * 
	 * @param selectObject
	 *            schemeelement
	 * @param level
	 */
	@SuppressWarnings("unused")
	private static HashMap<Integer, Mask> findMasks(Object selectObject, int level, HashMap<Integer, Mask> masksMap) {

		final List<? extends SchemeElement> elementsList = LMLCheck.getLowerSchemeElements(selectObject);

		for (final Object element : elementsList) {

			final SchemeElement schmeElement = (SchemeElement) element;
			if (!masksMap.containsKey(level) && schmeElement.getMask() != null) {
				masksMap.put(level, new Mask(schmeElement));
			}

			findMasks(schmeElement, level + 1, masksMap);
		}

		return masksMap;

	}

	/**
	 * parses all coherent digits and puts them into a an arraylist
	 * 
	 * p100-13, 0009-0-012 is transformed into 100,13,9,0,12 in an arraylist
	 * 
	 * @param impName
	 *            implicit name of a component of a parallel computer
	 * @param scheme
	 *            where the implicit object is defined by for masks-access
	 * @return arraylist of level-numbers
	 */
	@SuppressWarnings("unused")
	private static ArrayList<Integer> impNameToLevel(String impName, HashMap<Integer, Mask> masksMap) {

		final ArrayList<Integer> result = new ArrayList<Integer>();

		final int length = impName.length();
		int sumLength = 0;
		int level = 1;

		while (sumLength < length) {
			final Mask mask = masksMap.get(level++);
			if (mask == null) {
				// No mask for this level => error in parsing
				return null;
			}
			int currentLength = 0;
			// Length of the current mask-output

			if (mask.getOutputLength() >= 0) {
				// fixed length
				currentLength = mask.getOutputLength();

				if (sumLength + currentLength > impName.length()
						|| !mask.isOutputAllowed(impName.substring(sumLength, sumLength + currentLength))) {
					return null;
				}
			} else {
				// Variable-length-mask
				final String rest = impName.substring(sumLength);
				int i = 1;
				// Find first part that is allowed
				while (i <= rest.length() && !mask.isOutputAllowed(rest.substring(0, i))) {
					i++;
				}

				if (i <= rest.length()) {
					// Find last part that is allowed
					int j = i;
					while (j <= rest.length() && mask.isOutputAllowed(rest.substring(0, j))) {
						j++;
					}

					currentLength = j - 1;
				} else {
					return null;// Given output is not allowed
				}
			}

			final int number = mask.getNumberOfLevelString(impName.substring(sumLength, sumLength + currentLength));
			result.add(number);

			sumLength += currentLength;
		}

		return result;
	}

	/**
	 * Runs through data-tree and searches for refid-attributes Converts every
	 * refid into level-ids and searches given object within the
	 * scheme-definition of base nodedisplay adds errors to res, if refid-object
	 * does not exist within scheme
	 * 
	 * @param data
	 *            Data-Element of referencing nodedisplay
	 * @param masks
	 *            mask-attributes of base nodedisplay
	 * @param scheme
	 *            scheme-element of base nodedisplay
	 * @param errList
	 *            errors
	 * @return errorlist which occurred during checks
	 */
	private static ErrorList recCheckRefIds(Object data, SchemeType scheme, ErrorList errList) {

		final List<? extends DataElement> elements = LMLCheck.getLowerDataElements(data);
		if (elements == null) {
			return errList;// Keine Unterelemente gefunden
		}
		for (int i = 0; i < elements.size(); i++) {
			final String refName = elements.get(i).getRefid();

			if (refName != null) {
				// ArrayList<Integer> level=impnameToLevel(refname, masks);
				final ArrayList<Integer> levelList = impNameToOneLevel(refName, scheme, new ArrayList<Integer>());
				if (levelList == null) {
					errList.addError(Messages.FastImpCheck_0 + refName + Messages.FastImpCheck_1);
				} else {
					if (LMLCheck.getSchemeByLevels(levelList, scheme) == null) {
						errList.addError(Messages.FastImpCheck_2 + refName + Messages.FastImpCheck_3);
					}
				}
			}

			recCheckRefIds(elements.get(i), scheme, errList);
		}

		return errList;

	}

}
