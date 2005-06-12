/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.ptp.tools.vprof.internal.ui.views;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.StandardCElementLabelProvider;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/*
 * CViewLabelProvider 
 */
public class VprofViewLabelProvider extends StandardCElementLabelProvider {
	
	/**
	 * @param flags
	 * @param adormentProviders
	 */
	public VprofViewLabelProvider(int textFlags, int imageFlags) {
		super(textFlags, imageFlags);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof ITranslationUnit) {
			ITranslationUnit unit = (ITranslationUnit)element;
			Object parent = unit.getParent();
			if (parent instanceof IIncludeReference) {
				IPath p = unit.getPath();
				IPath parentLocation = ((IIncludeReference)parent).getPath();
				if (parentLocation.isPrefixOf(p)) {
					p = p.setDevice(null);
					p = p.removeFirstSegments(parentLocation.segmentCount());
				}
				return p.toString();
			}			
		}
		return super.getText(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return super.getImage(element);
	}
}
