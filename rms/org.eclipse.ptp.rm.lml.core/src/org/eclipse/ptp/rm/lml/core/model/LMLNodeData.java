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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.internal.rm.lml.core.JAXBUtil;
import org.eclipse.ptp.internal.rm.lml.core.messages.Messages;
import org.eclipse.ptp.internal.rm.lml.core.model.FastImpCheck;
import org.eclipse.ptp.internal.rm.lml.core.model.LMLCheck;
import org.eclipse.ptp.internal.rm.lml.core.model.LMLCheck.SchemeAndData;
import org.eclipse.ptp.internal.rm.lml.core.model.NumberComparator;
import org.eclipse.ptp.rm.lml.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.core.elements.DataType;
import org.eclipse.ptp.rm.lml.core.elements.JobType;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.core.elements.SchemeType;
import org.eclipse.ptp.rm.lml.core.elements.UsageType;

/**
 * Collection of all data needed for exactly one node
 * inside a nodedisplay-tree to visualize the tree.
 * 
 * Instances of this data are inserted into Node-instances.
 * 
 */
public class LMLNodeData {

	/**
	 * Jobname for the special empty job.
	 * This name is needed for creating usage tags.
	 */
	private static final String emptyJobName = "empty"; //$NON-NLS-1$

	/**
	 * Collects all jobs defined in a usage-tag and puts them in a
	 * hashmap. The map contains the amount of CPU used by each job
	 * within the usage tag.
	 * 
	 * @param usage
	 *            the converted usage tag.
	 * @return map of jobnames to their used cpus
	 */
	private static HashMap<String, Integer> convertUsageIntoJobMap(UsageType usage) {
		final HashMap<String, Integer> usageJobMap = new HashMap<String, Integer>();
		int cpuSum = 0;
		for (final JobType job : usage.getJob()) {
			usageJobMap.put(job.getOid(), job.getCpucount().intValue());
			cpuSum += job.getCpucount().intValue();
		}
		// Add additional empty job for missing cpus
		int emptyCount = usage.getCpucount().intValue() - cpuSum;
		if (emptyCount > 0) {
			if (usageJobMap.containsKey(emptyJobName)) {
				emptyCount += usageJobMap.get(emptyJobName);
			}
			usageJobMap.put(emptyJobName, emptyCount);
		}
		return usageJobMap;
	}

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

			} else if (lowerScheme.getMin() != null) {// min- max-attributes
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
	 * Merges the <code>toMerge</code> job map into the <code>jobMap</code>.
	 * Both contain mappings between jobNames and the amount of lowest level elements
	 * used by each job. The jobMap represents an incomplete mapping based on
	 * a higher level cut of the LML tree. The toMerge jobmap is a map of a lower
	 * level element and usually generated from a child node of the node associated with jobMap.
	 * The node associated with jobMap references the rootJobName by its oid-attribute. The
	 * rootJob's cpu amount will be decreased by the amounts of inserted jobs from the toMerge jobmap.
	 * 
	 * Example:
	 * 
	 * *
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
	 * 
	 * Assume you are generating a jobMap for R01.
	 * The jobMap would be at first
	 * job1 -> 3
	 * 
	 * The toMerge jobmap generated from R01-03 for example is
	 * job2 -> 1
	 * 
	 * After calling mergeJobMaps("job1", jobMap, toMerge) the jobMap will be
	 * job1 -> 2
	 * job2 -> 1
	 *  
	 *  
	 * @param rootJobName the name of the job referenced by the node, which is associated to jobMap
	 * @param jobMap the output map, to which the contents of toMerge are added
	 * @param toMerge the jobMap of a child of the node connected to jobMap
	 */
	private static void mergeJobMaps(String rootJobName, HashMap<String, Integer> jobMap, HashMap<String, Integer> toMerge) {
		if (rootJobName == null) {
			rootJobName = emptyJobName;
		}

		for (final String jobName : toMerge.keySet()) {
			int cpuCount = toMerge.get(jobName);
			// Reduce parent cpucount by current's job cpu count
			jobMap.put(rootJobName, jobMap.get(rootJobName) - cpuCount);
			// add new job or increase existing value for this job
			if (jobMap.containsKey(jobName)) {
				cpuCount += jobMap.get(jobName);
			}
			jobMap.put(jobName, cpuCount);
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
	public LMLNodeData(Object data, Object scheme, ArrayList<Integer> level, String impName) {

		init(data, scheme, level, impName);
	}

	/**
	 * Generate an usage-instance collecting all jobs running on this node
	 * and its children. The usage-tag can be visualized by a usagebar.
	 * 
	 * @return Usagetag for this node as a collection of jobs running on this node
	 */
	public UsageType generateUsage() {
		final HashMap<String, Integer> jobMap = getJobMap();

		final int totalCPUCount = getLowestElementsCount();

		return JAXBUtil.createUsageType(totalCPUCount, jobMap);
	}

	/**
	 * 
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
			final LMLNodeData newNode = new LMLNodeData(lowData, schemesMap.get(id), levelsList, newImpName);

			levelsList.remove(levelsList.size() - 1);

			if (newNode != null) {
				result.add(newNode);
			}
		}

		return result;

	}

	/**
	 * Accumulate the amount of lowest elements defined by this.scheme.
	 * Traverses the scheme-tree to its leaves and calculates the sum
	 * of defined lowest level elements.
	 * 
	 * @return amount of lowest level elements defined by this.scheme
	 */
	public int getLowestElementsCount() {
		final List<? extends SchemeElement> schemeElements = LMLCheck.getLowerSchemeElements(this.scheme);
		int lowerCPUCount = 0;
		if (schemeElements.size() > 0) {
			for (final SchemeElement lowerEl : schemeElements) {
				lowerCPUCount += getLowestElementsCount(lowerEl);
			}
		} else {
			lowerCPUCount = 1;
		}
		return lowerCPUCount;
	}

	/**
	 * 
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
			// For the root node this is true, because the root node has the correct data-tag associated
			return true;
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
	 * Create a job list mapping a job's oid to the amount of lowest
	 * level elements covered by the job. The job list will contain
	 * all jobs defined within this.data and its children.
	 * 
	 * Example
	 * *
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
	 * Assume you have a the LMLNodeData for R01.
	 * This function will return the following map:
	 * job1 -> 2
	 * job2 -> 1
	 * 
	 * The map for R02 is identical. The returned maps for R03 and
	 * R04 would be:
	 * job2 -> 3
	 * 
	 * 
	 * @return jobname to amount of lowest elements count map of all jobs within this data-tag
	 */
	private HashMap<String, Integer> getJobMap() {
		final HashMap<String, Integer> jobMap = new HashMap<String, Integer>();
		// Insert the root job
		String rootJob = emptyJobName;
		if (getDataElement() != null && getDataElement().getOid() != null) {
			rootJob = getDataElement().getOid();
		}
		jobMap.put(rootJob, getLowestElementsCount());

		// Traverse children only, if there are lower data elements within the LML model
		if (isRootNode() || LMLCheck.getLowerDataElements(getDataElement()).size() > 0) {

			// Generate a map with all jobs directly found in all children of this node
			final HashMap<String, Integer> directChildMap = new HashMap<String, Integer>();
			final List<LMLNodeData> lowerNodes = getLowerNodes();
			for (final LMLNodeData node : lowerNodes) {
				// Insert root node job of all lower nodes
				String jobName = node.getDataElement().getOid();
				if (jobName == null) {// Avoid null jobnames
					jobName = emptyJobName;
				}
				int cpuCount = node.getLowestElementsCount();
				if (directChildMap.containsKey(jobName)) {
					cpuCount += directChildMap.get(jobName);
				}
				directChildMap.put(jobName, cpuCount);
			}

			mergeJobMaps(rootJob, jobMap, directChildMap);

			// Do recursive merging of the jobMaps of each child
			for (final LMLNodeData node : lowerNodes) {
				// Merge the child map into the output map jobMap
				mergeJobMaps(node.getDataElement().getOid(), jobMap, node.getJobMap());
			}

		}

		// If this node's data-tag is a leave, check if it has a usagetag.
		// The jobs within the pregenerated usagetag have to be merged into the
		// jobMap, too. Use the usagebar only of the data-element is not a reference
		// to a higher level data-tag.
		if (getDataElement() != null && isDataElementOnNodeLevel()) {
			// Are there data-tags as children of this node's data-tag
			if (LMLCheck.getLowerDataElements(getDataElement()).size() == 0) {
				if (getDataElement().getUsage() != null) {
					// Convert usage-tag into jobmap
					// This jobmap should contain all information about hte current node
					return convertUsageIntoJobMap(getDataElement().getUsage());
				}
			}
		}

		// Remove the empty job entry, if it is not needed anymore
		if (jobMap.containsKey(emptyJobName)) {
			if (jobMap.get(emptyJobName) == 0) {
				jobMap.remove(emptyJobName);
			}
		}

		return jobMap;
	}

	/**
	 * Count lowest elements defined by this scheme.
	 * For a parallel system the lowest element is usually core.
	 * In this case this function will count the cores defined by
	 * the passed SchemeElement.
	 * 
	 * @param scheme
	 *            the scheme defining parts of a system's architecture
	 * @return amount of lowest level elements defined by this scheme
	 */
	private int getLowestElementsCount(SchemeElement scheme) {
		int currentCount = 0;
		if (scheme.getMin() != null) {
			if (scheme.getMax() == null) {
				currentCount = 1;
			} else {
				currentCount = scheme.getMax().intValue() - scheme.getMin().intValue() + 1;
			}
		} else {
			currentCount = scheme.getList().split(",").length; //$NON-NLS-1$
		}
		// Traverse lower scheme elements and sum up all defined elements
		final List<? extends SchemeElement> schemeElements = LMLCheck.getLowerSchemeElements(scheme);
		int lowerCPUCount = 0;
		if (schemeElements.size() == 0) { // Is this scheme element a leave?
			lowerCPUCount = 1;
		} else {
			lowerCPUCount = 0;
			for (final SchemeElement lowerEl : schemeElements) {
				lowerCPUCount += getLowestElementsCount(lowerEl);
			}
		}
		return currentCount * lowerCPUCount;
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
