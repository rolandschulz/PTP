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
package org.eclipse.ptp.internal.core.elements;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;

public abstract class PElement extends PlatformObject implements IPElementControl, Comparable<IPElementControl> {

	private final PElementInfo elementInfo = new PElementInfo(this);
	protected final AttributeManager attributeValues = new AttributeManager();
	protected final String elementId;
	protected final IPElementControl elementParent;
	protected final int elementType;

	protected PElement(String id, IPElementControl parent, int type, IAttribute<?,?,?>[] attrs) {
		elementId = id;
		elementType = type;
		elementParent = parent;
		ArrayList<IAttribute<?, ?, ?>> attrList = new ArrayList<IAttribute<?,?,?>>(Arrays.asList(attrs));
		attrList.add(ElementAttributes.getIdAttributeDefinition().create(id));
		attributeValues.addAttributes(attrList.toArray(new IAttribute<?, ?, ?>[0]));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPElement#addAttribute(org.eclipse.ptp.core.attributes.IAttribute)
	 */
	public void addAttribute(IAttribute<?,?,?> attrib) {
		addAttributes(new IAttribute<?,?,?>[]{attrib});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPElement#addAttributes(org.eclipse.ptp.core.attributes.IAttribute[])
	 */
	public void addAttributes(IAttribute<?,?,?>[] attribs) {
		attributeValues.addAttributes(attribs);
		doAddAttributeHook(new AttributeManager(attribs));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(IPElementControl obj) {
		return getName().compareTo(obj.getName());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPElement#getAttribute(org.eclipse.ptp.core.attributes.IAttributeDefinition)
	 */
	public <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> A getAttribute(D attrDef) {
		return attributeValues.getAttribute(attrDef);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPElement#getAttribute(java.lang.String)
	 */
	public IAttribute<?,?,?> getAttribute(String attrId) {
		return attributeValues.getAttribute(attrId);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPElement#getAttributeKeys()
	 */
	public IAttributeDefinition<?,?,?>[] getAttributeKeys() {
		return attributeValues.getKeys();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPElement#getAttributes()
	 */
	public IAttribute<?,?,?>[] getAttributes() {
		return attributeValues.getAttributes();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPElement#getDisplayAttributes()
	 */
	public IAttribute<?,?,?>[] getDisplayAttributes() {
		return attributeValues.getDisplayAttributes();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPElementControl#getElementType()
	 */
	public int getElementType() {
		return elementType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPElement#getID()
	 */
	public String getID() {
		return elementId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPElement#getName()
	 */
	public String getName() {
		StringAttribute attr = attributeValues.getAttribute(ElementAttributes.getNameAttributeDefinition());
		if (attr != null) {
			return attr.getValue();
		}
		return getID();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPElementControl#getParent()
	 */
	public IPElementControl getParent() {
		return elementParent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPElement#removeAttribute(org.eclipse.ptp.core.attributes.IAttribute)
	 */
	public void removeAttribute(IAttribute<?,?,?> attrib) {
		attributeValues.removeAttribute(attrib);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPElementControl#size()
	 */
	public int size() {
		return getElementInfo().size();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	protected abstract void doAddAttributeHook(AttributeManager attrs);
	
	/**
	 * Find the element info for this element
	 * 
	 * @return PElementInfo
	 */
	protected PElementInfo getElementInfo() {
		return elementInfo;
	}
}
