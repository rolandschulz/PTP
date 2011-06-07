/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch,FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers;

import java.util.ArrayList;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.internal.core.model.FastImpCheck;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLCheck;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLCheck.SchemeAndData;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLColor;

/**
 * A node which is input for the nodedisplaytreepanel
 * 
 * This DisplayNode can be implicitly or explicitly defined (means there could
 * be a data-element for this node, but must not)
 * 
 * Collects information for one node in the NodedisplayTreePanel
 */
public class DisplayNode implements Comparable<DisplayNode> {

	/**
	 * Generate a DisplayNode just from its implicit name
	 * 
	 * @param lguiItem
	 *            wrapper instance around LguiType-instance -- provides easy
	 *            access to lml-information
	 * @param impName
	 *            implicit name identifying a node within the tree
	 * @param nodedisplay
	 *            Nodedisplay as data-root
	 * @return DisplayNode for this implicit name
	 */
	public static DisplayNode getDisplayNodeFromImpName(ILguiItem lguiItem, String impName, Nodedisplay nodedisplay) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ids = FastImpCheck.impnameToOneLevel(impName, nodedisplay.getScheme(), ids);

		if (ids == null) {
			return null;// Name could not be converted into ids
		}

		// Goes as far as data and scheme are available
		final SchemeAndData schemedata = LMLCheck.getSchemeAndDataByLevels(LMLCheck.copyArrayList(ids), nodedisplay.getData(),
				nodedisplay.getScheme());
		// Goes down to exactly the scheme where this node is defined by
		final SchemeElement scheme = LMLCheck.getSchemeByLevels(LMLCheck.copyArrayList(ids), nodedisplay.getScheme());

		if (schemedata == null || scheme == null) {
			return null;// No scheme and data found for these ids => impname not
						// allowed in this nodedisplay
		}

		return new DisplayNode(lguiItem, scheme.getTagname(), schemedata.data, scheme, ids, nodedisplay);
	}

	private final String tagname;// What type of Node is this

	private final DataElement data;// Is there an explicit data-element for this
									// node?
	// otherwise upper-level data-element is saved here

	private final SchemeElement scheme;// corresponding scheme-element

	private final ArrayList<Integer> level;// contains for every level the
											// id-number of this node

	private SchemeAndData referencedData;// null if this element is placed in a
											// base nodedisplay
	// contains referenced scheme and data-elements given by refid

	private final Nodedisplay nodedisplay;// model for surrounding nodedisplay,
											// needed for full implicit name

	private final ILguiItem lguiItem;// LML-data-manager

	/**
	 * @param lguiItem
	 *            wrapper instance around LguiType-instance -- provides easy
	 *            access to lml-information
	 * @param ptag
	 *            tagname-attribute for this DisplayNode
	 * @param pdata
	 *            data-tag-reference, which gives information for this
	 *            DisplayNode
	 * @param pscheme
	 *            scheme-tag-reference, which gives information for this
	 *            DisplayNode
	 * @param plevel
	 *            list of ids, which identify this node on every level of the
	 *            lml-tree
	 * @param pnodedisplay
	 *            surrounding lml-nodedisplay-instance. This DisplayNode is a
	 *            physical part of the nodedisplay.
	 */
	public DisplayNode(ILguiItem lguiItem, String tagname, DataElement data, SchemeElement scheme, ArrayList<Integer> levels,
			Nodedisplay nodedisplay) {
		this.lguiItem = lguiItem;

		this.data = data;

		this.tagname = tagname;

		this.scheme = scheme;
		// Deep copy of plevel-numbers
		level = new ArrayList<Integer>();
		if (levels != null) {
			for (int i = 0; i < levels.size(); i++) {
				level.add(levels.get(i));
			}
		}

		this.nodedisplay = nodedisplay;
	}

	// Make this DisplayNode comparable to other DisplayNodes
	// This is mainly used to identify equality
	public int compareTo(DisplayNode o) {

		if (o.level == null) {
			if (level == null) {
				return 0;
			} else {
				return 1;
			}
		}

		if (level == null) {
			return -1;
		}

		if (level.size() != o.level.size()) {
			return level.size() - o.level.size();
		}

		for (int i = 0; i < level.size(); i++) {
			if (level.get(i) != o.level.get(i)) {
				return level.get(i) - o.level.get(i);
			}
		}

		return 0;
	}

	/**
	 * @return Object which is connected with this node through oid or refid
	 */
	public ObjectType getConnectedObject() {
		// Return referenced object
		if (referencedData != null && referencedData.data != null && lguiItem.getOIDToObject() != null) {
			return lguiItem.getOIDToObject().getObjectById(referencedData.data.getOid());
		}

		if (data == null || lguiItem.getOIDToObject() == null) {
			return null;
		}
		return lguiItem.getOIDToObject().getObjectById(data.getOid());
	}

	/**
	 * @return referenced data-element, by which this DisplayNode's data is
	 *         defined in nodedisplay
	 */
	public DataElement getData() {
		return data;
	}

	/**
	 * Generates a name, which identifies the referenced physical element in the
	 * whole lml-tree.
	 * 
	 * @return absolute name of the element within the tree
	 */
	public String getFullImplicitName() {
		if (nodedisplay == null) {
			return getImplicitName();
		}
		return LMLCheck.getImplicitName(LMLCheck.copyArrayList(level), nodedisplay.getScheme());
	}

	/**
	 * @return relative name of referenced data-element just for last level
	 */
	public String getImplicitName() {
		if (scheme == null) {
			return "";
		}
		return LMLCheck.getLevelName(scheme, level.get(level.size() - 1));
	}

	/**
	 * @return Depth in the data-tree where to find this node
	 */
	public int getLevel() {
		return level.size();
	}

	/**
	 * @return list of ids, which identify this DisplayNode on every level
	 */
	public ArrayList<Integer> getLevelNrs() {
		return level;
	}

	/**
	 * @return corresponding color for this data-Element
	 */
	public LMLColor getObjectColor() {

		final ObjectType refObject = getConnectedObject();

		if (lguiItem.getOIDToObject() != null) {
			if (refObject == null) {
				return lguiItem.getOIDToObject().getColorById(null);
			}

			return lguiItem.getOIDToObject().getColorById(refObject.getId());

		}
		return null;
	}

	/**
	 * Call this method only for Nodedisplay-references
	 * 
	 * @return combined scheme- and data-reference
	 */
	public SchemeAndData getReferencedData() {
		return referencedData;
	}

	/**
	 * @return referenced scheme-element, by which this DisplayNode is defined
	 *         in nodedisplay
	 */
	public SchemeElement getScheme() {
		return scheme;
	}

	// Defined by refid
	/**
	 * Set referenced data of this DisplayNode. This is only needed for
	 * Nodedisplay-References
	 * 
	 * @param pref
	 *            collects scheme- and data in one instance
	 */
	public void setReferencedData(SchemeAndData pref) {
		referencedData = pref;
	}

	// Create a nice output for a displaynode
	@Override
	public String toString() {
		if (scheme != null && level.size() > 0) {
			final String impname = String.format(scheme.getMask(), level.get(level.size() - 1));

			String connection = ""; //$NON-NLS-1$
			String refid = ""; //$NON-NLS-1$

			if (data != null) {
				if (data.getOid() != null) {
					connection = data.getOid() + " "; //$NON-NLS-1$
				}
				if (data.getRefid() != null) {
					refid = data.getRefid();
				}
			}

			if (referencedData != null) {
				connection = referencedData.data.getOid() + " "; //$NON-NLS-1$
			}

			return impname + " " + connection + refid; //$NON-NLS-1$
		} else {
			return tagname;
		}
	}

}
