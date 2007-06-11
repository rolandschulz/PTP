/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.internal.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class PElementPropertySource implements IPropertySource {

	private final IPElement pelement;
	private final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();

	public PElementPropertySource(IPElement pelement) {
		this.pelement = pelement;
		IAttributeDefinition<?,?,?>[] attrDefs = pelement.getAttributeKeys();
        String[] keys = new String[attrDefs.length];
        for (int i = 0; i < attrDefs.length; i++) {
        	keys[i] = attrDefs[i].getId();
        }
        for (int i = 0; i < keys.length; ++i) {
            addDescriptor(new PropertyDescriptor(keys[i], attrDefs[i].getName()));
        }
	}

	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors.toArray(new IPropertyDescriptor[0]);
	}

	public Object getPropertyValue(Object id) {
        final IAttribute<?, ?, ?> attribute = pelement.getAttribute(id.toString());
        if (attribute != null) {
        	return attribute.getValueAsString();
        }
        return null;
	}

	public boolean isPropertySet(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	public void resetPropertyValue(Object id) {
		// TODO Auto-generated method stub
		
	}

	public void setPropertyValue(Object id, Object value) {
		// TODO Auto-generated method stub
		
	}
	
	protected void addDescriptor(PropertyDescriptor desc) {
		descriptors.add(desc);
	}

}
