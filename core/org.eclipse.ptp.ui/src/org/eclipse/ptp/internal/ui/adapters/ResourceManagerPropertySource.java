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

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class ResourceManagerPropertySource implements IPropertySource {

	private final IResourceManager resourceManager;
	private final PropertyDescriptor[] descriptors;

	public ResourceManagerPropertySource(IResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		// TODO get attributes from the resourceManager's configuration
		List descriptors = new ArrayList();
		descriptors.add(new PropertyDescriptor("name", "name"));
		descriptors.add(new PropertyDescriptor("description", "description"));
		descriptors.add(new PropertyDescriptor("type", "type"));
		descriptors.add(new PropertyDescriptor("status", "status"));
		descriptors.add(new PropertyDescriptor("machines", "machines"));
		this.descriptors = (PropertyDescriptor[]) descriptors.toArray(
				new PropertyDescriptor[descriptors.size()]);
	}

	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	public Object getPropertyValue(Object id) {
		if ("name".equals(id)) {
			return resourceManager.getName();
		}
		if ("description".equals(id)) {
			return resourceManager.getConfiguration().getDescription();
		}
		if ("type".equals(id)) {
			final String resourceManagerId = resourceManager.getConfiguration().getResourceManagerId();
			final IModelManager modelManager = PTPCorePlugin.getDefault().getModelManager();
			IResourceManagerFactory factory = modelManager.getResourceManagerFactory(
					resourceManagerId);
			return factory.getName();
		}
		if ("status".equals(id)) {
			return resourceManager.getStatus().toString();
		}
		if ("machines".equals(id)) {
			final IPMachine[] machines = resourceManager.getMachines();
			return Integer.toString(machines.length);
		}
		
		// TODO get the property value from the rm's configuration attributes
		//... get configuration attributes here
		
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

}

