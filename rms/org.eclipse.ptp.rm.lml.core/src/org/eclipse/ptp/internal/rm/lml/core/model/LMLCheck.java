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
import java.util.HashSet;
import java.util.List;

import org.eclipse.ptp.internal.rm.lml.core.messages.Messages;
import org.eclipse.ptp.rm.lml.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.core.elements.DataElement1;
import org.eclipse.ptp.rm.lml.core.elements.DataElement2;
import org.eclipse.ptp.rm.lml.core.elements.DataElement3;
import org.eclipse.ptp.rm.lml.core.elements.DataElement4;
import org.eclipse.ptp.rm.lml.core.elements.DataElement5;
import org.eclipse.ptp.rm.lml.core.elements.DataElement6;
import org.eclipse.ptp.rm.lml.core.elements.DataElement7;
import org.eclipse.ptp.rm.lml.core.elements.DataElement8;
import org.eclipse.ptp.rm.lml.core.elements.DataElement9;
import org.eclipse.ptp.rm.lml.core.elements.DataType;
import org.eclipse.ptp.rm.lml.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement0;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement1;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement2;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement3;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement4;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement5;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement6;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement7;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement8;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement9;
import org.eclipse.ptp.rm.lml.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement1;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement2;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement3;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement4;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement5;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement6;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement7;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement8;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement9;
import org.eclipse.ptp.rm.lml.core.elements.SchemeType;

/**
 * This class is a copy of LMLCheck out of a lml-checking-tool only function
 * checkImplicitNames is missing
 * 
 * This class supports several view-classes by navigating through the
 * nodedisplays
 * 
 */
public class LMLCheck {

	/**
	 * A class which combines a data-element and the defining scheme-element
	 * 
	 */
	public static class SchemeAndData {

		public DataElement data;
		public SchemeElement scheme;

		public SchemeAndData(DataElement data, SchemeElement scheme) {
			this.data = data;
			this.scheme = scheme;
		}

	}

	/**
	 * Searches for allowed implicitly defined names from a name schema. Adds
	 * all names of elements, which are directly children of the schemeElement.
	 * Then calls this method again for all children.
	 * 
	 * @param errList
	 *            list of errors occuring while parsing
	 * @param names
	 *            allowed names
	 * @param schemeElement
	 *            actual node in scheme-tree
	 * @param name
	 *            name of this element, can be extended by the names of children
	 */
	public static void addAllowedNames(ErrorList errList, HashSet<String> names, Object schemeElement, String name) {

		final List<?> elements = getLowerSchemeElements(schemeElement);
		if (elements == null) {
			return;// no sub-elements found
		}
		for (int i = 0; i < elements.size(); i++) {

			final SchemeElement element = (SchemeElement) elements.get(i);

			if (element.getMin() != null) {// Lower elements through ranges
				final int min = element.getMin().intValue();
				int max = min;

				if (element.getMax() != null) {
					max = element.getMax().intValue();
				}

				final int step = element.getStep().intValue();

				if (min > max) {
					errList.addError(Messages.LMLCheck_1 + name + Messages.LMLCheck_2 + min + Messages.LMLCheck_3 + max);
				}

				for (int j = min; j <= max; j += step) {

					final String thisName = name + getLevelName(element, j);
					names.add(thisName);

					addAllowedNames(errList, names, element, thisName);
				}
			} else if (element.getList() != null) {// Lower elements through
													// list

				final int[] numbers = getNumbersFromNumberlist(element.getList());

				for (final int number : numbers) {
					final String thisName = name + getLevelName(element, number);
					names.add(thisName);

					addAllowedNames(errList, names, element, thisName);
				}

			}

		}

	}

	/**
	 * Check if all created data-nodes are allowed as described in the
	 * corresponding scheme returns list of Errors containing messages about
	 * not-allowed nodes in Errorlist
	 */
	public static void checkDataNodesinScheme(ErrorList errList, Object dataNode, ArrayList<Integer> upperLevelNumbers,
			Object scheme) {

		if (dataNode == null) {
			return;
		}

		final List<?> elementsData = getLowerDataElements(dataNode);
		if (elementsData == null) {
			return;
		}

		for (int i = 0; i < elementsData.size(); i++) {

			final DataElement elementData = (DataElement) elementsData.get(i);

			if (elementData.getMin() != null) {
				// data-tag given by range => check every element in range
				final int min = elementData.getMin().intValue();
				int max = min;

				if (elementData.getMax() != null) {
					max = elementData.getMax().intValue();
				}

				upperLevelNumbers.add(min);

				final SchemeAndData first = getSchemeAndDataByLevels(min, dataNode, scheme);
				// SchemeElement first=isDatanodeInScheme(upperlevelnumbers, scheme);

				if (first == null) {
					// Minimum of given data-node is not in scheme
					final String levelString = getLevelString(upperLevelNumbers);

					errList.addError(Messages.LMLCheck_4 + levelString + Messages.LMLCheck_5);
				} else {

					// Check Lower-Level Data-Nodes
					checkDataNodesinScheme(errList, elementData, upperLevelNumbers, first.scheme);

					final int step = first.scheme.getStep().intValue();
					// default of step is one, even if scheme does not define min-max-attributes

					for (int j = min + step; j <= max; j += step) {

						upperLevelNumbers.set(upperLevelNumbers.size() - 1, j);

						// get current schemeanddata
						final SchemeAndData schemeAndData = getSchemeAndDataByLevels(j, dataNode, scheme);
						// SchemeElement
						// ascheme=isDatanodeInScheme(upperlevelnumbers,scheme);

						if (schemeAndData == null) {
							// No scheme found for id j

							final String levelString = getLevelString(upperLevelNumbers);

							errList.addError(Messages.LMLCheck_6 + levelString + Messages.LMLCheck_7);
						} else {
							// Check inner Nodes
							checkDataNodesinScheme(errList, elementData, upperLevelNumbers, schemeAndData.scheme);
						}

					}

				}

				upperLevelNumbers.remove((upperLevelNumbers.size() - 1));

			} // end of range-checks
			else if (elementData.getList() != null) {// elements in data-tag
														// defined
				// with a list

				upperLevelNumbers.add(0);

				final int[] elements = getNumbersFromNumberlist(elementData.getList());
				for (final int number : elements) {

					upperLevelNumbers.set(upperLevelNumbers.size() - 1, number);

					final SchemeAndData schemeAndData = getSchemeAndDataByLevels(number, dataNode, scheme);

					if (schemeAndData == null) {
						final String levelString = getLevelString(upperLevelNumbers);

						errList.addError(Messages.LMLCheck_8 + levelString + Messages.LMLCheck_9);
					} else {// current number is allowed, check lower elements

						checkDataNodesinScheme(errList, elementData, upperLevelNumbers, schemeAndData.scheme);
					}

				}

				upperLevelNumbers.remove((upperLevelNumbers.size() - 1));

			}

		}// for-loop over subelements of datanode

	}

	/**
	 * Traverses the data-tag-tree and searches for refid-Attributes. Then
	 * checks whether these attributes have allowed values according to name
	 * scheme of the corresponding nodedisplay. Allowed names are passed as a
	 * set of names
	 * 
	 * Traverses all nodedisplay-references
	 * 
	 * @param errList
	 * @param names
	 * @param schemeElement
	 */
	public static void checkNamesAllowed(ErrorList errList, HashSet<String> names, Object dataRefElement) {

		final List<?> elements = getLowerDataElements(dataRefElement);
		if (elements == null) {
			return;
			// No lower elements found
		}
		for (int i = 0; i < elements.size(); i++) {

			final DataElement element = (DataElement) elements.get(i);

			final String refName = element.getRefid();

			if (refName != null) {
				if (!names.contains(refName)) {
					errList.addError(Messages.LMLCheck_10 + refName + Messages.LMLCheck_11);
				}
			}

			checkNamesAllowed(errList, names, element);
		}

	}

	/**
	 * Checks validity of nodedisplays. Checks whether only allowed attributes
	 * where used for base nodedisplays and nodedisplayrefs.
	 * 
	 * Also checks whether the defined data-nodes are allowed against the scheme
	 * of the corresponding nodedisplay
	 * 
	 * @param lgui
	 * @return found errors
	 */
	public static ErrorList checkNodedisplays(LguiType lgui) {

		final ErrorList result = new ErrorList();

		final List<GobjectType> gobjects = (new LguiItem(lgui)).getOverviewAccess().getGraphicalObjects();

		// Search all nodedisplays and check their attributes
		for (final GobjectType gobject : gobjects) {
			if (gobject instanceof Nodedisplay) {

				final Nodedisplay nodedisplay = (Nodedisplay) gobject;

				result.addMessage(Messages.LMLCheck_12 + nodedisplay.getId());
				// This part is now done by xsl-file
				// checkAttributesAllowedForNodedisplay(dis.getRefto()!=null,
				// res, dis.getData(), new ArrayList<Integer>());

				checkDataNodesinScheme(result, nodedisplay.getData(), new ArrayList<Integer>(), nodedisplay.getScheme());

			}
		}

		return result;
	}

	/**
	 * Copy an Integer-ArrayList
	 * 
	 * @param origin
	 * @return
	 */
	public static ArrayList<Integer> copyArrayList(ArrayList<Integer> origin) {
		final ArrayList<Integer> result = new ArrayList<Integer>();

		for (int i = 0; i < origin.size(); i++) {
			result.add(origin.get(i));
		}

		return result;
	}

	/**
	 * @param element
	 * @return in which level this dataelement is placed, 0 for DataType, 10 for
	 *         DataElement (unspecific)
	 */
	public static int getDataLevel(Object element) {
		if (element instanceof DataType) {
			return 0;
		}
		if (element instanceof DataElement1) {
			return 1;
		}
		if (element instanceof DataElement2) {
			return 2;
		}
		if (element instanceof DataElement3) {
			return 3;
		}
		if (element instanceof DataElement4) {
			return 4;
		}
		if (element instanceof DataElement5) {
			return 5;
		}
		if (element instanceof DataElement6) {
			return 6;
		}
		if (element instanceof DataElement7) {
			return 7;
		}
		if (element instanceof DataElement8) {
			return 8;
		}
		if (element instanceof DataElement9) {
			return 9;
		}
		if (element instanceof DataElement) {
			return 10;
		}

		return -1;
	}

	/**
	 * Returns the height of the scheme-tree given by this schemeEl
	 * 
	 * @param schemeElement
	 *            scheme elements or a SchemeType-instance
	 * @return
	 */
	public static int getDeepestSchemeLevel(Object schemeElement) {

		if (schemeElement == null) {
			return 0;
		}

		final List<?> elements = getLowerSchemeElements(schemeElement);
		if (elements == null) {
			return 1;
		}

		int max = 0;

		for (int i = 0; i < elements.size(); i++) {
			final int height = getDeepestSchemeLevel(elements.get(i));

			if (height > max) {
				max = height;
			}
		}

		return max + (schemeElement instanceof SchemeType ? 0 : 1);

	}

	/**
	 * @param ids
	 *            identifying numbers on every level
	 * @param scheme
	 *            upper-level scheme object
	 * @return implicit name for this object by using masks and maps
	 */
	public static String getImplicitName(ArrayList<Integer> ids, Object scheme) {

		if (ids == null || ids.size() == 0) {
			return ""; //$NON-NLS-1$
		}

		final ArrayList<Integer> id = new ArrayList<Integer>();
		id.add(ids.get(0));

		final SchemeElement subScheme = getSchemeByLevels(copyArrayList(id), scheme);

		if (subScheme == null) {
			return ""; //$NON-NLS-1$
		}

		final String subName = getLevelName(subScheme, id.get(0));

		ids.remove(0);

		return subName + getImplicitName(ids, subScheme);
	}

	/**
	 * Uses mask-attribute to return formatted output of levelid Exception is
	 * the definition of map-attribute, then names are explicitly given by
	 * map-attribute
	 * 
	 * @param scheme
	 *            corresponding scheme-element
	 * @param levelId
	 *            id of the element, the name is searched for
	 * @return implicit name of the element for this level
	 */
	public static String getLevelName(SchemeElement scheme, int levelId) {

		if (scheme.getMap() == null) {
			return String.format(scheme.getMask(), levelId);
		}

		final String[] names = scheme.getMap().split(","); //$NON-NLS-1$
		if (scheme.getMin() != null) {// min-max-attributes
			final int min = scheme.getMin().intValue();

			final int step = scheme.getStep().intValue();

			final int namesPosition = (levelId - min) / step;

			if (namesPosition < names.length) {
				return names[namesPosition];
			}
		} else {// List-attribute, map list-ids to map-names, in other words:
				// find position of levelid in list-attribute and return
				// corresponding positioned map-name
			final int[] numbers = getNumbersFromNumberlist(scheme.getList());

			for (int i = 0; i < numbers.length; i++) {
				if (numbers[i] == levelId) {
					return names[i];
				}
			}
		}

		return ""; //$NON-NLS-1$
	}

	/**
	 * @param levels
	 * @return a string describing the levels of a node within a tree
	 */
	public static String getLevelString(ArrayList<Integer> levels) {

		String levelString = ""; //$NON-NLS-1$

		for (int j = 1; j <= levels.size(); j++) {
			levelString += "el" + j + "=" + levels.get(j - 1) + " "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return levelString;
	}

	/**
	 * Returns a List of lower data-elements for a data-element or a
	 * dataType-instance within a data-section of a nodedisplay
	 * 
	 * @param element
	 * @return
	 */
	public static List<? extends DataElement> getLowerDataElements(Object element) {

		if (element instanceof DataType) {
			return ((DataType) element).getEl1();
		}
		if (element instanceof DataElement1) {
			return ((DataElement1) element).getEl2();
		}
		if (element instanceof DataElement2) {
			return ((DataElement2) element).getEl3();
		}
		if (element instanceof DataElement3) {
			return ((DataElement3) element).getEl4();
		}
		if (element instanceof DataElement4) {
			return ((DataElement4) element).getEl5();
		}
		if (element instanceof DataElement5) {
			return ((DataElement5) element).getEl6();
		}
		if (element instanceof DataElement6) {
			return ((DataElement6) element).getEl7();
		}
		if (element instanceof DataElement7) {
			return ((DataElement7) element).getEl8();
		}
		if (element instanceof DataElement8) {
			return ((DataElement8) element).getEl9();
		}
		if (element instanceof DataElement9) {
			return ((DataElement9) element).getEl10();
		}

		return new ArrayList<DataElement>();
	}

	/**
	 * Returns a List of lower nodedisplayelements for a NodedisplayelementX or
	 * a NodedisplaylayoutType
	 * 
	 * @param element
	 * @return
	 */
	public static List<? extends Nodedisplayelement> getLowerNodedisplayElements(Object element) {

		if (element instanceof NodedisplaylayoutType) {
			final ArrayList<Nodedisplayelement0> justOneElement = new ArrayList<Nodedisplayelement0>();
			justOneElement.add(((NodedisplaylayoutType) element).getEl0());
			return justOneElement;
		}
		if (element instanceof Nodedisplayelement0) {
			return ((Nodedisplayelement0) element).getEl1();
		}
		if (element instanceof Nodedisplayelement1) {
			return ((Nodedisplayelement1) element).getEl2();
		}
		if (element instanceof Nodedisplayelement2) {
			return ((Nodedisplayelement2) element).getEl3();
		}
		if (element instanceof Nodedisplayelement3) {
			return ((Nodedisplayelement3) element).getEl4();
		}
		if (element instanceof Nodedisplayelement4) {
			return ((Nodedisplayelement4) element).getEl5();
		}
		if (element instanceof Nodedisplayelement5) {
			return ((Nodedisplayelement5) element).getEl6();
		}
		if (element instanceof Nodedisplayelement6) {
			return ((Nodedisplayelement6) element).getEl7();
		}
		if (element instanceof Nodedisplayelement7) {
			return ((Nodedisplayelement7) element).getEl8();
		}
		if (element instanceof Nodedisplayelement8) {
			return ((Nodedisplayelement8) element).getEl9();
		}
		if (element instanceof Nodedisplayelement9) {
			return ((Nodedisplayelement9) element).getEl10();
		}

		return new ArrayList<Nodedisplayelement>();
	}

	/**
	 * Returns a List of lower scheme elements for a scheme-element or a
	 * SchemeType-instance
	 * 
	 * @param element
	 * @return
	 */
	public static List<? extends SchemeElement> getLowerSchemeElements(Object element) {

		if (element instanceof SchemeType) {
			return ((SchemeType) element).getEl1();
		}
		if (element instanceof SchemeElement1) {
			return ((SchemeElement1) element).getEl2();
		}
		if (element instanceof SchemeElement2) {
			return ((SchemeElement2) element).getEl3();
		}
		if (element instanceof SchemeElement3) {
			return ((SchemeElement3) element).getEl4();
		}
		if (element instanceof SchemeElement4) {
			return ((SchemeElement4) element).getEl5();
		}
		if (element instanceof SchemeElement5) {
			return ((SchemeElement5) element).getEl6();
		}
		if (element instanceof SchemeElement6) {
			return ((SchemeElement6) element).getEl7();
		}
		if (element instanceof SchemeElement7) {
			return ((SchemeElement7) element).getEl8();
		}
		if (element instanceof SchemeElement8) {
			return ((SchemeElement8) element).getEl9();
		}
		if (element instanceof SchemeElement9) {
			return ((SchemeElement9) element).getEl10();
		}

		return new ArrayList<SchemeElement>();
	}

	/**
	 * Traverses elements-tree of layout-section for a nodedisplay till all ids
	 * given by numbers are found
	 * 
	 * @param numbers
	 *            contains level-ids
	 * @param nodeElement
	 *            upper-level-el-tag
	 * @return nodedisplayelement describing the object`s layout given by
	 *         element-ids through numbers
	 */
	public static Nodedisplayelement getNodedisplayElementByLevels(ArrayList<Integer> numbers, Nodedisplayelement nodeElement) {

		if (numbers == null || numbers.size() == 0) {
			return null;
		}

		if (nodeElement == null) {
			return null;
		}

		final int number0 = numbers.get(0);

		numbers.remove(0);// Remove the first element of the arraylist

		final List<?> schemeElements = getLowerNodedisplayElements(nodeElement);

		Nodedisplayelement scheme = null;

		// Find scheme-tag which contains element with id anum
		for (int i = 0; i < schemeElements.size(); i++) {

			final Nodedisplayelement minScheme = (Nodedisplayelement) schemeElements.get(i);

			if (minScheme.getMin() != null) {// Scheme-elements defined by range
												// through min- max-attributes
				final int minValue = minScheme.getMin().intValue();
				int maxValue = minValue;

				if (minScheme.getMax() != null) {
					maxValue = minScheme.getMax().intValue();
				}

				if (number0 >= minValue && number0 <= maxValue) {
					scheme = minScheme;

					break;
				}

			} else if (minScheme.getList() != null) {
				// Scheme defines list of elements

				final int[] listElements = getNumbersFromNumberlist(minScheme.getList());
				// for example 1,2,16,23

				for (final int number : listElements) {
					if (number == number0) {
						scheme = minScheme;
						break;
					}
				}
				// if scheme found stop searching
				if (scheme != null) {
					break;
				}

			}
		}

		// No scheme found for anum
		if (scheme == null) {
			return null;
		}

		// Real solutions are always found in this part

		if (numbers.size() == 0) {
			return scheme;
			// All numbers processed?
		} else {
			// are the lower levels of the scheme-node allowed in the data-tag?
			final Nodedisplayelement result = getNodedisplayElementByLevels(numbers, scheme);
			return result;
		}

	}

	/**
	 * Parses numberlist (for example 1,2,16,2,100) and returns a list of
	 * integers like new int[]{1,2,16,2,100}
	 * 
	 * @param numberList
	 *            list of numbers separated by commas
	 * @return integer list
	 */
	public static int[] getNumbersFromNumberlist(String numberList) {

		final String[] parts = numberList.split(","); //$NON-NLS-1$

		final int[] result = new int[parts.length];

		int i = 0;
		for (final String part : parts) {
			result[i++] = Integer.parseInt(part.trim());
		}

		return result;
	}

	/**
	 * 
	 * Searches within the dataEl-tree for a data-element which describes the
	 * treenode with the level-ids given by numbers.
	 * 
	 * Returns corresponding scheme-element if available
	 * 
	 * @param numbers
	 *            level-ids, which has to be searched in lower elements, content
	 *            might be changed by this function
	 * @param dataElement
	 *            root of the data-tags, which will be processed, dataEl itself
	 *            is just used to get lower elements
	 * @param schemeElement
	 *            root of the scheme-tags, which will be processed, schemeEl
	 *            itself is just used to get lower elements
	 * @return data-element and corresponding scheme
	 */
	public static SchemeAndData getSchemeAndDataByLevels(ArrayList<Integer> numbers, Object dataElement, Object schemeElement) {

		// numbers=copyArrayList(numbers);

		if (numbers == null || numbers.size() == 0) {
			return null;
		}

		if (schemeElement == null) {
			return null;
		}

		final int number0 = numbers.get(0);

		numbers.remove(0);// Remove the first element of the arraylist

		final List<?> elements = getLowerDataElements(dataElement);

		final List<?> schemeElements = getLowerSchemeElements(schemeElement);

		for (int i = 0; i < elements.size(); i++) {

			final DataElement element = (DataElement) elements.get(i);

			if (element.getMin() != null) {// data-Element with ranges

				final int min = element.getMin().intValue();
				int max = min;

				if (element.getMax() != null) {
					max = element.getMax().intValue();
				}

				if (min > number0 || max < number0) {
					continue;
				}
				// else data-Element for anum found
			} else if (element.getList() != null) {// check if anum in list

				boolean found = false;

				final int[] listElements = getNumbersFromNumberlist(element.getList());
				// for example 1,2,16,23

				for (final int number : listElements) {
					if (number == number0) {
						found = true;
						break;
					}
				}

				// if anum not in list, search for another data-element
				if (!found) {
					continue;
				}
			}

			// Find corresponding scheme-element
			SchemeElement scheme = null;
			for (int j = 0; j < schemeElements.size(); j++) {

				final SchemeElement aScheme = (SchemeElement) schemeElements.get(j);

				if (aScheme.getMin() != null) {
					// Scheme-elements defined by range through min-max-attributes
					final int min = aScheme.getMin().intValue();
					int max = min;

					if (aScheme.getMax() != null) {
						max = aScheme.getMax().intValue();
					}

					final int step = aScheme.getStep().intValue();

					if (number0 >= min && number0 <= max && (number0 - min) % step == 0) {
						scheme = aScheme;

						break;
					}

				} else if (aScheme.getList() != null) {
					// Scheme defines list of elements

					final int[] listElements = getNumbersFromNumberlist(aScheme.getList());
					// for example 1,2,16,23

					for (final int number : listElements) {
						if (number == number0) {
							scheme = aScheme;
							break;
						}
					}
					// if scheme found stop searching
					if (scheme != null) {
						break;
					}

				}
			}

			// No scheme found for the current data-element
			if (scheme == null) {
				continue;
			}

			// Real solutions are always found in this part

			if (numbers.size() == 0) {
				return new SchemeAndData(element, scheme);
				// All numbers processed?
			} else {
				// are the lower levels of the scheme-node allowed in the data-tag?
				final SchemeAndData result = getSchemeAndDataByLevels(numbers, element, scheme);
				if (result != null) {
					return result;
				}
			}

		}

		if (dataElement instanceof DataType) {
			return null;
		} else
			return new SchemeAndData((DataElement) dataElement, (SchemeElement) schemeElement);

	}

	/**
	 * Nice call for just one number, just creates an arraylist with one number
	 * and calls overloaded function
	 * 
	 * @param number
	 *            one integer value with id for the element to be checked
	 * @param dataElement
	 *            upper-level data-element
	 * @param schemeElement
	 *            scheme-element on the same level as dataEl
	 * @return Data- and Scheme which are connected to each other
	 */
	public static SchemeAndData getSchemeAndDataByLevels(int number, Object dataElement, Object schemeElement) {

		final ArrayList<Integer> numbers = new ArrayList<Integer>();
		numbers.add(number);

		return getSchemeAndDataByLevels(numbers, dataElement, schemeElement);

	}

	/**
	 * Traverses scheme-tree till all ids given by numbers are found
	 * 
	 * @param numbers
	 *            contains level-ids
	 * @param schemeElement
	 *            upper-level-scheme-tag
	 * @return schemeelement describing the object given by element-ids through
	 *         numbers
	 */
	public static SchemeElement getSchemeByLevels(ArrayList<Integer> numbers, Object schemeElement) {

		if (numbers == null || numbers.size() == 0) {
			return null;
		}

		if (schemeElement == null) {
			return null;
		}

		final int number0 = numbers.get(0);

		numbers.remove(0);// Remove the first element of the arraylist

		final List<?> schemeElements = getLowerSchemeElements(schemeElement);

		SchemeElement scheme = null;

		// Find scheme-tag which contains element with id anum
		for (int i = 0; i < schemeElements.size(); i++) {

			final SchemeElement aScheme = (SchemeElement) schemeElements.get(i);

			if (aScheme.getMin() != null) {// Scheme-elements defined by range
											// through min- max-attributes
				final int min = aScheme.getMin().intValue();
				int max = min;

				if (aScheme.getMax() != null) {
					max = aScheme.getMax().intValue();
				}

				final int aStep = aScheme.getStep().intValue();

				if (number0 >= min && number0 <= max && (number0 - min) % aStep == 0) {
					scheme = aScheme;

					break;
				}

			} else if (aScheme.getList() != null) {
				// Scheme defines list of elements

				final int[] listElements = getNumbersFromNumberlist(aScheme.getList());
				// for example 1,2,16,23

				for (final int number : listElements) {
					if (number == number0) {
						scheme = aScheme;
						break;
					}
				}
				// if scheme found stop searching
				if (scheme != null) {
					break;
				}

			}
		}

		// No scheme found for anum
		if (scheme == null) {
			return null;
		}

		// Real solutions are always found in this part

		if (numbers.size() == 0) {
			return scheme;
			// All numbers processed?
		} else {
			// are the lower levels of the scheme-node allowed in the data-tag?
			final SchemeElement result = getSchemeByLevels(numbers, scheme);
			return result;
		}

	}

	/**
	 * @param element
	 * @return in which level this schemeelement is placed, 0 for SchemeType, 10
	 *         for SchemeElement (unspecific)
	 */
	public static int getSchemeLevel(Object element) {

		if (element instanceof SchemeType) {
			return 0;
		}
		if (element instanceof SchemeElement1) {
			return 1;
		}
		if (element instanceof SchemeElement2) {
			return 2;
		}
		if (element instanceof SchemeElement3) {
			return 3;
		}
		if (element instanceof SchemeElement4) {
			return 4;
		}
		if (element instanceof SchemeElement5) {
			return 5;
		}
		if (element instanceof SchemeElement6) {
			return 6;
		}
		if (element instanceof SchemeElement7) {
			return 7;
		}
		if (element instanceof SchemeElement8) {
			return 8;
		}
		if (element instanceof SchemeElement9) {
			return 9;
		}
		if (element instanceof SchemeElement) {
			return 10;
		}

		return -1;
	}

	/**
	 * Searches within the direct childs of dataEl for a tag which includes the
	 * idnr number.
	 * 
	 * @param number
	 *            number of this element in current level
	 * @param dataElement
	 *            data-object of the corresponding nodedisplay or a lower-level
	 *            data-element
	 * @return the DataElement in which this scheme-node is described, null if
	 *         there is no explicitly defined data
	 */
	public static DataElement isSchemenodeInThisData(int number, Object dataElement) {
		final List<?> elements = getLowerDataElements(dataElement);

		if (elements == null) {
			return null;
		}

		for (int i = 0; i < elements.size(); i++) {

			final DataElement element = (DataElement) elements.get(i);

			if (element.getMin() != null) {
				// ranges

				final int min = element.getMin().intValue();
				int max = min;

				if (element.getMax() != null) {
					max = element.getMax().intValue();
				}

				if (number >= min && number <= max) {
					return element;
				}
			} else if (element.getList() != null) {// list-attribute

				final int[] listElements = getNumbersFromNumberlist(element.getList());
				// for example 1,2,16,23

				for (final int listElement : listElements) {
					if (listElement == number) {
						return element;
					}
				}

			}
		}

		return null;
	}

}
