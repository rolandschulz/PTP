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
/**
 * 
 */
package org.eclipse.ptp.internal.ui.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ui.views.properties.IPropertySource;

public class PropertyAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IPropertySource.class) {
			if (adaptableObject instanceof IResourceManager) {
				return new ResourceManagerPropertySource((IResourceManager)adaptableObject);
			}
			if (adaptableObject instanceof IPElement) {
				return new PElementPropertySource((IPElement)adaptableObject);
			}
			if (adaptableObject instanceof IElement) {
				return new PElementPropertySource(((IElement)adaptableObject).getPElement());
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
		return new Class[] {IPropertySource.class};
	}

}
