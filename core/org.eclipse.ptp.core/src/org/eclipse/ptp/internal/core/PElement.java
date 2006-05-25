/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.search.ui.ISearchPageScoreComputer;

public abstract class PElement extends PlatformObject implements IPElement, Comparable {
	protected HashMap[] attribClass = null;
	protected HashMap elementAttribs = null;
	
	protected int ID = -1;

	private PElementInfo elementInfo = null;

	protected PElement(IPElement parent, String name, String key, int type) {
		attribClass = new HashMap[AttributeConstants.NUM_ATTRIB_CLASSES];
		elementAttribs = new HashMap();
		ID = PTPCorePlugin.getDefault().getNewID();
		elementAttribs.put(AttributeConstants.ATTRIB_PARENT, parent);
		elementAttribs.put(AttributeConstants.ATTRIB_NAME, name);
		elementAttribs.put(AttributeConstants.ATTRIB_TYPE, new Integer(type));
		attribClass[AttributeConstants.ATTRIB_CLASS_ELEMENT] = elementAttribs;
	}

	protected PElementInfo getElementInfo() {
		if (elementInfo == null)
			elementInfo = new PElementInfo(this);
		return elementInfo;
	}

	public Object getAttribute(int attr_class, String key) {
		if (attr_class < 0 || attr_class >= AttributeConstants.NUM_ATTRIB_CLASSES)
			return null;
		HashMap attribs = attribClass[attr_class];
		if (attribs == null)
			return null;
		return attribs.get(key);
	}

	public void setAttribute(int attr_class, String key, Object o) {
		if (attr_class < 0 || attr_class >= AttributeConstants.NUM_ATTRIB_CLASSES)
			return;
		HashMap attribs = attribClass[attr_class];
		if (attribs == null)
			attribs = new HashMap();
		attribs.put(key, o);
		attribClass[attr_class] = attribs;
	}
	
	public String[] getAttributeKeys(int attr_class) {
		if (attr_class < 0 || attr_class >= AttributeConstants.NUM_ATTRIB_CLASSES)
			return new String[]{};
		HashMap attribs = attribClass[attr_class];
		if (attribs == null)
			return new String[]{};
		return (String[])attribs.keySet().toArray(new String[0]);
	}
	
	public String getElementName() {
		// return NAME_TAG + getKey();
		return (String)elementAttribs.get(AttributeConstants.ATTRIB_NAME);
	}

	public int getID() {
		return ID;
	}
	
	public String getIDString() {
		return ""+ID+"";
	}

	/**
	 * @param name
	 *            The Name to set.
	 */
	public void setElementName(String name) {
		elementAttribs.put(AttributeConstants.ATTRIB_NAME, name);
	}

	/**
	 * @return Returns the Parent.
	 */
	public IPElement getParent() {
		return (IPElement)elementAttribs.get(AttributeConstants.ATTRIB_PARENT);
	}

	/**
	 * @param parent
	 *            The Parent to set.
	 */
	public void setParent(IPElement parent) {
		elementAttribs.put(AttributeConstants.ATTRIB_PARENT, parent);
	}

	/**
	 * @return Returns the Type.
	 */
	public int getElementType() {
		Integer i = (Integer)elementAttribs.get(AttributeConstants.ATTRIB_TYPE);
		if(i == null) return P_TYPE_ERROR;
		else return i.intValue();
	}

	/**
	 * @param type
	 *            The Type to set.
	 */
	public void setElementType(int type) {
		elementAttribs.put(AttributeConstants.ATTRIB_TYPE, new Integer(type));
	}

	public String toString() {
		return getElementName();
	}

	public int size() {
		return getElementInfo().size();
	}

	public int compareTo(Object obj) {
		if (obj instanceof IPElement) {
			int my_rank = getID();
			int his_rank = ((IPElement) obj).getID();
			if (my_rank < his_rank)
				return -1;
			if (my_rank == his_rank)
				return 0;
			if (my_rank > his_rank)
				return 1;
		}
		return 0;
	}

	public int computeScore(String pageId, Object element) {
		//FIXME
		//if (!CoreUtils.PTP_SEARCHPAGE_ID.equals(pageId))
			//return ISearchPageScoreComputer.UNKNOWN;

		if (element instanceof IPElement)
			return 90;

		return ISearchPageScoreComputer.LOWEST;
	}
}
