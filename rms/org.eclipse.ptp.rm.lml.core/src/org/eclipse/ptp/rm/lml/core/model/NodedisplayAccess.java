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
import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.internal.rm.lml.core.messages.Messages;
import org.eclipse.ptp.internal.rm.lml.core.model.FastImpCheck;
import org.eclipse.ptp.internal.rm.lml.core.model.LMLCheck;
import org.eclipse.ptp.internal.rm.lml.core.model.LMLCheck.SchemeAndData;
import org.eclipse.ptp.rm.lml.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;

/**
 * This handler grants access to nodedisplays. It includes several functions,
 * which simplify nodedisplay processing.
 * 
 * There are access functions for getting tagnames of nodedisplays by their id.
 * Moreover there are functions to handle mask-attributes.
 * 
 * You can convert implicit names into level-ids and vice versa.
 * 
 */
public class NodedisplayAccess extends LguiHandler {

	/**
	 * if no layout is found for a level, it needs default values anyhow just
	 * creates a default nodedisplayelement, default values are given by default
	 * values from lml-scheme
	 * 
	 * @return default Nodedisplayelement for layout definitions
	 */
	public static Nodedisplayelement getDefaultLayout() {
		return defaultlayout;
	}

	// Saves for every nodedisplay a hashmap, which maps level-ids to
	// corresponding tagnames
	// nodedisplay.id -> HashMap
	private HashMap<String, HashMap<Integer, String>> oidToTagNames;
	// Saves for every nodedisplay a hashmap, which maps level-ids to
	// corresponding mask-definitions
	// nodedisplay.id -> HashMap
	private HashMap<String, HashMap<Integer, Mask>> oidToMasks;

	// saves all layouts for nodedisplays, key is index of nodedisplay
	// nodedisplay.id -> layout-list
	private HashMap<String, ArrayList<NodedisplaylayoutType>> oidToLayouts;

	// Cache a list of nodedisplays
	private List<Nodedisplay> nodedisplays;

	private static Nodedisplayelement defaultlayout;

	/**
	 * @param lguiItem
	 *            LML-data-handler, which groups this handler and others to a
	 *            set of LMLHandler. This instance is needed to notify all
	 *            LMLHandler, if any data of the LguiType-instance was changed.
	 */
	public NodedisplayAccess(ILguiItem lguiItem, LguiType model) {
		super(lguiItem, model);
		updateData();

		this.lguiItem.addListener(new ILguiListener() {

			public void handleEvent(ILguiUpdatedEvent e) {
				update(e.getLgui());
				updateData();
			}
		});
	}

	/**
	 * Get implicit name of physical element with given ids per level Parameter
	 * ids is copied, so no changes will be made within this list through
	 * calling this function
	 * 
	 * example: lml-tag
	 * 
	 * <pre>
	 * {@code
	 * <nodedisplay title="a parallel computer" id="display1">
	 * <!-- definition of empty system -->
	 * <scheme>
	 * 	<el1 tagname="row" min="0" max="8" mask="R%01d">
	 * 		<el2 tagname="rack" min="0" max="7" mask="%01d">
	 * 			<el3 tagname="midplane" min="0" max="1" mask="-M%1d">
	 * 				<el4 tagname="nodecard" min="0" max="15" mask="-N%02d">
	 * 					<el5 tagname="computecard" min="4" max="35" mask="-C%02d">
	 * 						<el6 tagname="core" min="0" max="3" mask="-%01d">
	 * 						</el6>
	 * 					</el5>
	 * 				</el4>
	 * 			</el3>
	 * 		</el2>
	 * 	</el1>
	 * </scheme>
	 * ...
	 * </nodedisplay>
	 * }
	 * </pre>
	 * 
	 * getImplicitName("display1", arraylistwithelements(1,2,1) ) returns
	 * implicit name of first midplane within third rack within second row. The
	 * result is "R12-M1"
	 * 
	 * Remark: Names are created with masks. Every id is printed with the format
	 * defined in the mask-attribute
	 * 
	 * @param nodedisplayId
	 *            id of nodedisplay, where to search physical element identified
	 *            by passed ids
	 * @param ids
	 *            id of physical element on every level
	 * @return implicit name defined by masks and map-attributes
	 */
	public String getImplicitName(String nodedisplayId, ArrayList<Integer> ids) {
		if (ids == null || ids.size() == 0) {
			return ""; //$NON-NLS-1$
		}

		final ArrayList<Integer> copy = LMLCheck.copyArrayList(ids);

		final Nodedisplay nodedisplay = getNodedisplayById(nodedisplayId);

		if (nodedisplay == null) {
			return ""; //$NON-NLS-1$
		}

		// Use LMLCheck-function for details
		return LMLCheck.getImplicitName(copy, nodedisplay.getScheme());
	}

	/**
	 * @param id
	 *            identification of nodedisplay
	 * @return all defined nodedisplaylayouts for this graphical object
	 */
	public ArrayList<NodedisplaylayoutType> getLayouts(String id) {
		return oidToLayouts.get(id);
	}

	/**
	 * Get the mask definition for the passed level for the nodedisplay with the
	 * passed id. This function assumes the nodedisplay-tag to define only one
	 * mask for every level. This is the normal case.
	 * 
	 * @param id
	 *            identification of nodedisplay
	 * @param level
	 *            level in tree, for which a mask is needed
	 * @return mask-object for nodedisplay in given level
	 */
	public Mask getMask(String id, int level) {
		final HashMap<Integer, Mask> masks = getMasks(id);

		if (masks == null) {
			return null;
		}

		return masks.get(level);
	}

	/**
	 * Get the mask-definitions for the nodedisplay with the passed id. This
	 * function assumes the nodedisplay-tag to define only one mask for every
	 * level. This is the normal case.
	 * 
	 * @param id
	 *            identification of nodedisplay
	 * @return Hashmap of masks, keys are level-nrs or null if no nodedisplay
	 *         found for this id
	 */
	public HashMap<Integer, Mask> getMasks(String id) {
		return oidToMasks.get(id);
	}

	/**
	 * Get a single nodedisplay with the given id
	 * 
	 * @param id
	 *            id of the searched nodedisplay
	 * @return nodedisplay with given id or null, if it was not found
	 */
	public Nodedisplay getNodedisplayById(String id) {
		final List<Nodedisplay> displays = getNodedisplays();

		for (final Nodedisplay nodedisplay : displays) {
			if (nodedisplay.getId().equals(id)) {
				return nodedisplay;
			}
		}

		return null;
	}

	public Object getNodedisplayData(int i) {
		return getNodedisplays().get(i).getData();
	}

	/**
	 * @return amount of available nodedisplays in this model
	 */
	public int getNodedisplayNumbers() {
		return getNodedisplays().size();
	}

	/**
	 * Getting a list of all elements of type Nodedisplay from LguiType.
	 * 
	 * @return list of elements(Nodedisplay)
	 */
	public List<Nodedisplay> getNodedisplays() {

		if (nodedisplays == null) {// Create new list, if it was not created
									// till now

			nodedisplays = new ArrayList<Nodedisplay>();
			for (final GobjectType tag : lguiItem.getOverviewAccess().getGraphicalObjects()) {
				if (tag instanceof Nodedisplay) {
					nodedisplays.add((Nodedisplay) tag);
				}
			}

		}
		// Otherwise return cached list
		return nodedisplays;
	}

	public Object getNodedisplayScheme(int i) {
		return getNodedisplays().get(i).getScheme();
	}

	public String getNodedisplayTitel(int i) {
		return getNodedisplays().get(i).getTitle();
	}

	/**
	 * Traverse a nodedisplay-tree and search for data identified by the
	 * ids-ArrayList. Traverses as deep as possible. If there is no data as deep
	 * as desired corresponding parent-tags are returned. Next to the data this
	 * function returns the scheme-element, which defines the data-tag. So this
	 * function allows easily to connect data with corresponding
	 * scheme-definitions.
	 * 
	 * example
	 * 
	 * <pre>
	 * {@code
	 * <nodedisplay title="a parallel computer" id="display1">
	 * <!-- definition of empty system -->
	 * <scheme>
	 * 	<el1 tagname="row" min="0" max="8" mask="R%01d">
	 * 		<el2 tagname="rack" min="0" max="7" mask="%01d" />
	 * 	</el1>
	 * </scheme>
	 * <data>
	 * 
	 * 	<el1 min="0" max="3" oid="job1"/>
	 * 	<el1 min="5" max="8" oid="job2">
	 * 		<el2 min="0" max="3" oid="job3"/>
	 * 	</el1>
	 * 
	 *  </data>
	 * </nodedisplay>
	 * }
	 * </pre>
	 * 
	 * getSchemeAndDataByLevels("display1", (1,3) ) returns scheme:
	 * {@code <el1 tagname="row" min="0" max="8" mask="R%01d">} and data:
	 * {@code <el1 min="0" max="3" oid="job1"/>} the function traverses as deep
	 * as possible.
	 * 
	 * getSchemeAndDataByLevels("display1", (5,3) ) returns scheme:
	 * {@code <el2 tagname="rack" min="0" max="7" mask="%01d" />} and data:
	 * {@code <el2 min="0" max="3" oid="job3"/>}
	 * 
	 * 
	 * @param nodedisplayId
	 *            id of nodedisplay, which is traversed for data
	 * @param ids
	 *            identifying a dataelement within nodedisplay-tag
	 * @return JAXB-data-instance identified by ids and corresponding
	 *         scheme-tag, which defines this data-tag null if there is no
	 *         nodedisplay with id nodedisplayId
	 */
	public SchemeAndData getSchemeAndDataByLevels(String nodedisplayId, ArrayList<Integer> ids) {
		final Nodedisplay nodedisplay = getNodedisplayById(nodedisplayId);

		if (nodedisplay == null) {
			return null;
		}

		return LMLCheck.getSchemeAndDataByLevels(ids, nodedisplay.getData(), nodedisplay.getScheme());
	}

	/**
	 * Traverse scheme-tag of nodedisplay with given id. Return the
	 * schemeElement identified by the level-ids passed with numbers.
	 * 
	 * example:
	 * 
	 * <pre>
	 * {@code
	 * <nodedisplay title="a parallel computer" id="display1">
	 * <!-- definition of empty system -->
	 * <scheme>
	 * 	<el1 tagname="row" min="0" max="8" mask="R%01d">
	 * 		<el2 tagname="rack" min="0" max="7" mask="%01d">
	 * 			<el3 tagname="midplane" min="0" max="1" mask="-M%1d">
	 * 				<el4 tagname="nodecard" min="0" max="15" mask="-N%02d">
	 * 					<el5 tagname="computecard" min="4" max="35" mask="-C%02d">
	 * 						<el6 tagname="core" min="0" max="3" mask="-%01d">
	 * 						</el6>
	 * 					</el5>
	 * 				</el4>
	 * 			</el3>
	 * 		</el2>
	 * 	</el1>
	 * </scheme>
	 * ...
	 * </nodedisplay>
	 * }
	 * </pre>
	 * 
	 * getSchemeByLevels("display1", arraylistwithelements(1,2,1) ) will return
	 * the JAXB-object-instance corresponding to the tag starting with
	 * {@code <el3 tagname="midplane" min="0" max="1" mask="-M%1d">}
	 * 
	 * @param nodedisplayId
	 *            id of nodedisplay, which is traversed for scheme-elements
	 * @param ids
	 *            ids identifying a schemeelement within nodedisplay-scheme-tag
	 * @return JAXB-instance corresponding to the searched scheme-tag
	 */
	public SchemeElement getSchemeByLevels(String nodedisplayId, ArrayList<Integer> ids) {
		final Nodedisplay nodedisplay = getNodedisplayById(nodedisplayId);

		if (nodedisplay == null) {
			return null;
		}

		return LMLCheck.getSchemeByLevels(ids, nodedisplay.getScheme());
	}

	/**
	 * Get the maximal depth of the nodedisplay's scheme.
	 * 
	 * example:
	 * 
	 * <pre>
	 * {@code
	 * <nodedisplay title="a parallel computer" id="display1">
	 * <!-- definition of empty system -->
	 * <scheme>
	 * 	<el1 tagname="row" min="0" max="8" mask="R%01d">
	 * 		<el2 tagname="rack" min="0" max="7" mask="%01d">
	 * 			<el3 tagname="midplane" min="0" max="1" mask="-M%1d">
	 * 				<el4 tagname="nodecard" min="0" max="15" mask="-N%02d">
	 * 					<el5 tagname="computecard" min="4" max="35" mask="-C%02d">
	 * 						<el6 tagname="core" min="0" max="3" mask="-%01d">
	 * 						</el6>
	 * 					</el5>
	 * 				</el4>
	 * 			</el3>
	 * 		</el2>
	 * 	</el1>
	 * </scheme>
	 * ...
	 * </nodedisplay>
	 * }
	 * </pre>
	 * 
	 * getSchemeDepth("display1") returns 6
	 * 
	 * @param nodedisplayId
	 *            id of nodedisplay, whose scheme-depth is returned
	 * @return depth of scheme or -1 if no nodedisplay found with given id
	 */
	public int getSchemeDepth(String nodedisplayId) {
		final Nodedisplay nodedisplay = getNodedisplayById(nodedisplayId);

		if (nodedisplay == null) {
			return -1;
		}

		return LMLCheck.getDeepestSchemeLevel(nodedisplay.getScheme());
	}

	/**
	 * Get the tagname for the passed level for the nodedisplay with the passed
	 * id. This function assumes the nodedisplay-tag to define only one tagname
	 * for every level. This is the normal case.
	 * 
	 * @param id
	 *            identification of nodedisplay
	 * @param level
	 *            level in tree, for which a tagname is needed
	 * @return tagname for nodedisplay in given level
	 */
	public String getTagname(String id, int level) {
		final HashMap<Integer, String> tagnames = getTagnames(id);

		if (tagnames == null) {
			return null;
		}

		return tagnames.get(level);
	}

	/**
	 * Get the tagnames for the nodedisplay with the passed id. This function
	 * assumes the nodedisplay-tag to define only one tagname for every level.
	 * This is the normal case.
	 * 
	 * @param id
	 *            identification of nodedisplay
	 * @return Hashmap of tagnames, keys are level-nrs or null if no nodedisplay
	 *         found for this id
	 */
	public HashMap<Integer, String> getTagnames(String id) {
		return oidToTagNames.get(id);
	}

	public String getTitle(String gid) {
		final List<Nodedisplay> displays = getNodedisplays();
		if (displays.size() == 0 || displays.get(0) == null) {
			return "Nodedisplay"; //$NON-NLS-1$
		}
		return displays.get(0).getTitle();
	}

	/**
	 * Inverse function to getImplicitName. Pass an implicit name and receive a
	 * list of identifying ids within the nodedisplay-tag-tree.
	 * 
	 * example: lml-tag
	 * 
	 * <pre>
	 * {@code
	 * <nodedisplay title="a parallel computer" id="display1">
	 * <!-- definition of empty system -->
	 * <scheme>
	 * 	<el1 tagname="row" min="0" max="8" mask="R%01d">
	 * 		<el2 tagname="rack" min="0" max="7" mask="%01d">
	 * 			<el3 tagname="midplane" min="0" max="1" mask="-M%1d">
	 * 				<el4 tagname="nodecard" min="0" max="15" mask="-N%02d">
	 * 					<el5 tagname="computecard" min="4" max="35" mask="-C%02d">
	 * 						<el6 tagname="core" min="0" max="3" mask="-%01d">
	 * 						</el6>
	 * 					</el5>
	 * 				</el4>
	 * 			</el3>
	 * 		</el2>
	 * 	</el1>
	 * </scheme>
	 * ...
	 * </nodedisplay>
	 * }
	 * </pre>
	 * 
	 * impnameToLevel("display1", "R13-M0-N12") returns a list with the ids:
	 * (1,3,0,12) This list identifies the physical element with the given
	 * implicit name.
	 * 
	 * @param nodedisplayId
	 *            id of nodedisplay, where physical element with given implicit
	 *            name is searched
	 * @param impname
	 *            implicit name of physical element within this nodedisplay
	 * @return list with ids on every level of the corresponding nodedisplay to
	 *         identify the position of the physical element with the passed
	 *         implicit name. null, if there is no nodedisplay with given id, or
	 *         if there is no physical element with the passed implicit name
	 */
	public ArrayList<Integer> impnameToLevel(String nodedisplayId, String impname) {

		final Nodedisplay nodedisplay = getNodedisplayById(nodedisplayId);

		if (nodedisplay == null) {
			return null;
		}

		return FastImpCheck.impNameToOneLevel(impname, nodedisplay, new ArrayList<Integer>());
	}

	@Override
	public String toString() {
		final List<Nodedisplay> nodedisplays = getNodedisplays();

		if (nodedisplays.size() > 0) {
			return getNodedisplays().get(0).getTitle();
		} else {
			return Messages.NodedisplayAccess_2;
		}
	}

	/**
	 * Call this method, if lml-model changed. The new model is passed to this
	 * handler. All getter-functions accessing the handler will then return
	 * data, which is collected from this new model
	 * 
	 * @param lgui
	 *            new lml-data-model
	 */
	public void updateData() {

		oidToTagNames = new HashMap<String, HashMap<Integer, String>>();
		oidToMasks = new HashMap<String, HashMap<Integer, Mask>>();
		oidToLayouts = new HashMap<String, ArrayList<NodedisplaylayoutType>>();

		// Reset cached values, really create a new list
		nodedisplays = null;
		getNodedisplays();
		for (final Nodedisplay nodedisplay : nodedisplays) {
			final HashMap<Integer, String> atagnames = new HashMap<Integer, String>();
			final HashMap<Integer, Mask> amasks = new HashMap<Integer, Mask>();

			findtagNamesAndMasks(nodedisplay.getScheme(), 1, atagnames, amasks);
			// Save them for every id
			oidToTagNames.put(nodedisplay.getId(), atagnames);
			oidToMasks.put(nodedisplay.getId(), amasks);
		}
		final List<NodedisplaylayoutType> nodedisplayLayouts = lguiItem.getLayoutAccess().getNodedisplayLayouts();
		for (final NodedisplaylayoutType nodedisplayLayout : nodedisplayLayouts) {
			if (oidToLayouts.containsKey(nodedisplayLayout.getGid())) {// Already
																		// layout
																		// found
																		// for
																		// referenced
																		// nodedisplay
				final ArrayList<NodedisplaylayoutType> old = oidToLayouts.get(nodedisplayLayout.getGid());
				old.add(nodedisplayLayout);
			} else {// Create new layout-list
				final ArrayList<NodedisplaylayoutType> layouts = new ArrayList<NodedisplaylayoutType>();
				layouts.add(nodedisplayLayout);
				oidToLayouts.put(nodedisplayLayout.getGid(), layouts);
			}

		}

		final ObjectFactory objf = new ObjectFactory();
		defaultlayout = objf.createNodedisplayelement();
	}

	/**
	 * Searches for tagnames and masks within scheme-tag and saves them in
	 * tagnames-object. This function assumes the lml-nodedisplay-tag to define
	 * only one mask- and tagname-attribute for every level of the
	 * nodedisplay-tree. This is not defined in LML. But usually this assumption
	 * is correct.
	 * 
	 * @param schemeelement
	 *            current scheme-model, might be of type SchemeType or
	 *            SchemeElement1-SchemeElement9
	 * @param level
	 *            current level in tree for putting mask and tagname at the
	 *            right index into hashmap
	 * @param tagnames
	 *            hashmap for tagnames key=level of tree, value=tagname
	 * @param masks
	 *            hashmap for masks key=level of tree, value=mask-object
	 */
	private void findtagNamesAndMasks(Object schemeelement, int level, HashMap<Integer, String> tagnames,
			HashMap<Integer, Mask> masks) {

		final List els = LMLCheck.getLowerSchemeElements(schemeelement);

		for (final Object el : els) {

			final SchemeElement asel = (SchemeElement) el;

			if (!tagnames.containsKey(level) && asel.getTagname() != null) {
				tagnames.put(level, asel.getTagname());
			}

			if (!masks.containsKey(level) && asel.getMask() != null) {
				masks.put(level, new Mask(asel));
			}

			findtagNamesAndMasks(asel, level + 1, tagnames, masks);
		}

	}

}
