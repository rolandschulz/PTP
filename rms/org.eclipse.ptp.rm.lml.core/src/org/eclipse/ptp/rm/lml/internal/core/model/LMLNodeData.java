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
package org.eclipse.ptp.rm.lml.internal.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.messages.Messages;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataType;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeType;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLCheck.SchemeAndData;

/**
 * Collection of all data needed for exactly one node
 * inside a nodedisplay-tree to visualize the tree.
 * 
 * Instances of this data are inserted into Node-instances.
 * 
 */
public class LMLNodeData {

	/**
	 * This is a helping function for getLowerNodes.
	 * It searches within the scheme of a LMLNodeData for the ids of valid children.
	 * They are added to numbers.
	 * Moreover for every children the corresponding scheme-element is searched.
	 * The connection is saved in the HashMap schemesForIds
	 * 
	 * @param scheme
	 *            the scheme of the node, of which the children have to be found
	 * @param numbersList
	 *            contains ids of children after this function is called
	 * @param schemesMap
	 *            provides schemes for every children's id
	 */
	private static void getLowerIdsAndSchemesForIds(Object scheme, ArrayList<Integer> numbersList,
			HashMap<Integer, SchemeElement> schemesMap) {

		final List<?> schemeList = LMLCheck.getLowerSchemeElements(scheme);
		for (int i = 0; i < schemeList.size(); i++) {
			// getNumbers
			final SchemeElement lowerScheme = (SchemeElement) schemeList.get(i);
			if (lowerScheme.getList() != null) {// get list-elements
				final int[] numbersArray = LMLCheck.getNumbersFromNumberlist(lowerScheme.getList());
				for (final int number : numbersArray) {
					numbersList.add(number);
					schemesMap.put(number, lowerScheme);
				}

			} else {// min- max-attributes
				final int min = lowerScheme.getMin().intValue();
				int max = min;
				if (lowerScheme.getMax() != null) {
					max = lowerScheme.getMax().intValue();
				}

				final int step = lowerScheme.getStep().intValue();
				for (int j = min; j <= max; j += step) {
					numbersList.add(j);
					schemesMap.put(j, lowerScheme);
				}

			}
		}

	}

	/**
	 * Is there an explicit data-element for this node?
	 * otherwise upper-level data-element is saved here
	 * 
	 * This attribute takes DataType-instance for the root-node.
	 */
	private Object data;

	/**
	 * Implicit name of this node
	 */
	private String impName;

	/**
	 * Contains for every level the id-number of this node
	 * Last number in this list is the id on current tree-level
	 * for this node. Remark: trees with this data referenced
	 * do not have to start on lml-root-level. Thus this identification
	 * through level-ids is necessary. level.size() will return
	 * the level, in which this node is placed within the whole tree.
	 */
	private ArrayList<Integer> levelList;

	/**
	 * corresponding scheme-element
	 * This attribute takes a SchemeType-instance for the root node,
	 * which is parent of all el1-nodes.
	 */
	private Object scheme;

	/**
	 * Create LMLNodeData from an implicit name. Finds corresponding
	 * data- and scheme-element.
	 * LML-Example:
	 * 
	 * <pre>
	 * {@code
	 * <nodedisplay title="example" id="2">
	 * 
	 * 			<scheme>
	 * 				<el1 tagname="Rack" min="1" max="4" mask="R%02d">
	 * 					<el2 tagname="Nodecard" min="1" max="3" mask="-%02d" />
	 * 				</el1>
	 * 			</scheme>
	 * 			
	 * 			<data>
	 * 				<el1 min="1" max="2" oid="job1">
	 * 					<el2 min="3" oid="job2"/>
	 * 				</el1>
	 * 				<el1 min="3" max="4" oid="job2"/>
	 * 			</data>
	 * 			
	 * </nodedisplay>
	 * }
	 * implicitname is generated from level-ids and printing masks defined in scheme.
	 * Every level-id is a parameter for printf-formats defined in mask-attribute.
	 * allowed implict names in this example are: R01-01, R01-02, ... R04-03
	 * 
	 * The root model is needed to search within the whole lml-tree.
	 * 
	 * 
	 * @param impName name generated from ids on every level and masks in scheme
	 * @param nodedisplay complete nodedisplay-model to search for data and scheme
	 */
	public LMLNodeData(String impName, Nodedisplay nodedisplay) {

		ArrayList<Integer> idsList = new ArrayList<Integer>();
		// Create a root-node, if empty string is passed as implicit name
		if (impName.equals("")) { //$NON-NLS-1$
			init(nodedisplay.getData(), nodedisplay.getScheme(), idsList, impName);
			return;
		}

		idsList = FastImpCheck.impNameToOneLevel(impName, nodedisplay.getScheme(), idsList);

		if (idsList == null) {
			throw new IllegalArgumentException(Messages.LMLNodeData_0 + impName + Messages.LMLNodeData_1);
		}

		// Goes as far as data and scheme are available
		final SchemeAndData schemeData = LMLCheck.getSchemeAndDataByLevels(LMLCheck.copyArrayList(idsList), nodedisplay.getData(),
				nodedisplay.getScheme());
		// Goes down to exactly the scheme where this node is defined by
		final SchemeElement scheme = LMLCheck.getSchemeByLevels(LMLCheck.copyArrayList(idsList), nodedisplay.getScheme());

		if (schemeData == null || scheme == null) {
			throw new IllegalArgumentException(Messages.LMLNodeData_2);
		}

		init(schemeData.data, scheme, idsList, impName);
	}

	/**
	 * Create LMLNodeData with explicit references to LML-tree-parts.
	 * 
	 * LML-Example:
	 * 
	 * <pre>
	 * {@code
	 * <nodedisplay title="example" id="2">
	 * 
	 * 			<scheme>
	 * 				<el1 tagname="Rack" min="1" max="4">
	 * 					<el2 tagname="Nodecard" min="1" max="3"/>
	 * 				</el1>
	 * 			</scheme>
	 * 			
	 * 			<data>
	 * 				<el1 min="1" max="2" oid="job1">
	 * 					<el2 min="3" oid="job2"/>
	 * 				</el1>
	 * 				<el1 min="3" max="4" oid="job2"/>
	 * 			</data>
	 * 			
	 * </nodedisplay>
	 * }
	 * </pre>
	 * 
	 * Data could be JAXB-instance for {@code <el2 min="3" oid="job2"/>}.
	 * Scheme is then JAXB-instance for {@code <el2 tagname="Nodecard" min="1" max="3"/>}.
	 * Level is a list of ids to clearly identify one physical element within lml-tree, here for example: {1,3} or {2,3}.
	 * 
	 * For the root-node data and scheme can be of type SchemeType or DataType.
	 * The root-node in this context is the parent node of all el1-elements.
	 * In the example above the root-node has 4 el1-children.
	 * 
	 * @param data
	 *            LML-date-element from nodedisplay-data-tree
	 * @param scheme
	 *            LML-scheme-element from nodedisplay-scheme-tree
	 * @param level
	 *            list of level-ids to identify this node-data in full tree
	 * @param impName
	 *            implicit name of this LML-node, bijective relation to level-array
	 */
	protected LMLNodeData(Object data, Object scheme, ArrayList<Integer> level, String impName) {

		init(data, scheme, level, impName);
	}

	/**
	 * @return LML-date-element from nodedisplay-data-tree or DataType
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @return DataElement, if this node is not a root-node, otherwise null
	 */
	public DataElement getDataElement() {
		if (data instanceof DataElement) {
			return (DataElement) data;
		} else {
			return null;
		}
	}

	/**
	 * @return an implicit name for this node generated from the mask-attributes of the nodedisplay-scheme
	 */
	public String getFullImpName() {
		return impName;
	}

	/**
	 * @return implicit name
	 */
	public String getImpName() {
		final SchemeElement scheme = getSchemeElement();
		if (scheme == null) {
			return ""; //$NON-NLS-1$
		}
		return LMLCheck.getLevelName(scheme, levelList.get(levelList.size() - 1));
	}

	/**
	 * @return list of level-ids to identify this node-data in full tree
	 */
	public ArrayList<Integer> getLevelIds() {
		return levelList;
	}

	/**
	 * Generates LMLNodeData as nodes for every explicit node, which is a direct
	 * child of this.
	 * 
	 * Example: Assume you have the following nodedisplay in LML:
	 * 
	 * <pre>
	 * {@code
	 * <nodedisplay title="example" id="nodedisplay1">
	 * 
	 * <scheme>
	 * 	<el1 tagname="Rack" min="1" max="4" mask="R%02d">
	 * 		<el2 tagname="Nodecard" min="1" max="3" mask="-%02d" />
	 * 	</el1>
	 * </scheme>
	 * 
	 * <data>
	 * 	<el1 min="1" max="2" oid="job1">
	 * 		<el2 min="3" oid="job2" />
	 * 	</el1>
	 * 	<el1 min="3" max="4" oid="job2" />
	 * </data>
	 * 
	 * </nodedisplay>
	 * }
	 * </pre>
	 * 
	 * The this-instance would reference to R04. The call of getLowerNodes()
	 * for the LMLNodeData would return a list of LMLNodeData for R04-01,
	 * R04-02 and R04-03. These LMLNodeData instances are explicitly created
	 * by this function.
	 * 
	 * @return list of LMLNodeData as children of this node
	 */
	public ArrayList<LMLNodeData> getLowerNodes() {

		final ArrayList<LMLNodeData> result = new ArrayList<LMLNodeData>();

		final Object scheme = getScheme();
		final HashMap<Integer, SchemeElement> schemesMap = new HashMap<Integer, SchemeElement>();
		// Indices within scheme of lower elements
		final ArrayList<Integer> numbersList = new ArrayList<Integer>();
		// Search for children ids and corresponding schemes
		getLowerIdsAndSchemesForIds(scheme, numbersList, schemesMap);

		Collections.sort(numbersList, new NumberComparator(true));

		final ArrayList<Integer> levelsList = getLevelIds();
		final Object data = getData();

		// Process numbers
		for (int j = 0; j < numbersList.size(); j++) {
			final int id = numbersList.get(j);
			// Get all information for new DisplayNode
			final SchemeAndData schemeData = LMLCheck.getSchemeAndDataByLevels(id, data, scheme);

			Object lowData = schemeData.data;

			if (LMLCheck.getDataLevel(data) < LMLCheck.getSchemeLevel(scheme) && data instanceof DataElement) {
				lowData = data;// Then do not go deeper , makes
								// no sense
			}

			levelsList.add(id);
			final String newImpName = impName + LMLCheck.getLevelName(schemesMap.get(id), id);
			final LMLNodeData newNode = new LMLNodeData(lowData, schemesMap.get(id), levelsList,
					newImpName);

			levelsList.remove(levelsList.size() - 1);

			if (newNode != null) {
				result.add(newNode);
			}
		}

		return result;

	}

	/**
	 * @return LML-scheme-element from nodedisplay-scheme-tree, or SchemeType for root-element
	 */
	public Object getScheme() {
		return scheme;
	}

	/**
	 * @return SchemeElement, if this node is not a root-node, otherwise null
	 */
	public SchemeElement getSchemeElement() {
		if (scheme instanceof SchemeElement) {
			return (SchemeElement) scheme;
		} else {
			return null;
		}
	}

	/**
	 * Tests the depth of the data-instance.
	 * Checks if the level of the referenced data is as deep as
	 * the level-identification. The data-instance could refer to a higher
	 * level lml-tag.
	 * 
	 * @return true if data-reference level is equal to the id-level, false otherwise
	 */
	public boolean isDataElementOnNodeLevel() {
		final DataElement data = getDataElement();

		if (data == null) {
			return false;
		}

		return levelList.size() == LMLCheck.getDataLevel(data);
	}

	/**
	 * @return true, if this instance comprises the data for the root node
	 */
	public boolean isRootNode() {
		return getSchemeElement() == null;
	}

	/**
	 * Initialize all data with given parameters.
	 * 
	 * @param data
	 *            LML-date-element from nodedisplay-data-tree
	 * @param scheme
	 *            LML-scheme-element from nodedisplay-scheme-tree
	 * @param levelList
	 *            list of level-ids to identify this node-data in full tree
	 */
	private void init(Object data, Object scheme, ArrayList<Integer> levelList, String impName) {

		if (!(data instanceof DataElement || data instanceof DataType)) {
			throw new IllegalArgumentException(Messages.LMLNodeData_3);
		}
		if (!(scheme instanceof SchemeElement || scheme instanceof SchemeType)) {
			throw new IllegalArgumentException(Messages.LMLNodeData_4);
		}

		this.data = data;
		this.scheme = scheme;
		this.levelList = new ArrayList<Integer>(levelList);
		this.impName = impName;
	}
}
