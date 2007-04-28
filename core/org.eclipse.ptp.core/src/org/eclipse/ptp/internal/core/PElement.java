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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.search.ui.ISearchPageScoreComputer;

public abstract class PElement extends PlatformObject implements IPElementControl, Comparable {

	private PElementInfo elementInfo = null;

	protected Map<String, IAttribute> attributeValues = new HashMap<String, IAttribute>();
	protected int elementId = -1;
	protected IPElementControl elementParent;

	protected int elementType;

	protected PElement(int id, IPElementControl parent, int type, IAttribute[] attrs) {
		elementId = id;
		elementType = type;
		elementParent = parent;
		for (IAttribute attr : attrs) {
			final IAttributeDefinition attrDef = attr.getDefinition();
			setAttribute(attrDef.getId(), attr);
		}
	}

	public int compareTo(Object obj) {
		if (obj instanceof IPElementControl) {
			int my_rank = getID();
			int his_rank = ((IPElementControl) obj).getID();
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

		if (element instanceof IPElementControl)
			return 90;

		return ISearchPageScoreComputer.LOWEST;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPElement#getAttribute(org.eclipse.ptp.core.attributes.IAttributeDefinition)
	 */
	public IAttribute getAttribute(IAttributeDefinition attrDef) {
		return getAttribute(attrDef.getId());
	}

	public IAttribute getAttribute(String attrId) {
		return attributeValues.get(attrId);
	}
	
	public Set<Map.Entry<String, IAttribute>> getAttributeEntrySet() {
		return attributeValues.entrySet();
	}
	
	public String[] getAttributeKeys() {
		return attributeValues.keySet().toArray(new String[0]);
	}
	
	public String getName() {
		IAttribute attr = (IAttribute) attributeValues.get(AttributeDefinitionManager.getNameAttributeDefinition().getId());
		if (attr != null) {
			return attr.getValueAsString();
		}
		return "";
	}
	
	/**
	 * @return Returns the Type.
	 */
	public int getElementType() {
		return elementType;
	}

	public int getID() {
		return elementId;
	}
	
	public String getIDString() {
		return ""+elementId+"";
	}

	/**
	 * @return Returns the Parent.
	 */
	public IPElementControl getParent() {
		return elementParent;
	}

	public void setAttribute(String attrId, IAttribute attrib) {
		attributeValues.put(attrId, attrib);
	}
	
	public int size() {
		return getElementInfo().size();
	}

	public String toString() {
		return getName();
	}

	protected PElementInfo getElementInfo() {
		if (elementInfo == null)
			elementInfo = new PElementInfo(this);
		return elementInfo;
	}
}
