/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.internal.core.model;

import java.util.List;

import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.ICContainer;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.ICProject;
import org.eclipse.fdt.core.model.IParent;
import org.eclipse.fdt.core.model.ITranslationUnit;

/**
 */
public class SourceMapper {
	ICProject cproject;

	public SourceMapper(ICProject p) {
		cproject = p;
	}

	public ITranslationUnit findTranslationUnit(String filename) {
		return findTranslationUnit(cproject, filename);
	}
	
	public ITranslationUnit findTranslationUnit(IParent container, String filename) {
		try {
			List list = container.getChildrenOfType(ICElement.C_UNIT);
			for (int i = 0; i < list.size(); i++) {
				Object o = list.get(i);
				if (o instanceof ITranslationUnit) {
					ITranslationUnit tu = (ITranslationUnit)o;
					// TODO: What about non case sensitive filesystems.
					if (filename.equals(tu.getElementName())) {
						return tu;
					}
				}
			}
			
			// TODO: This to simple, we are not protected against
			// loop in the file system symbolic links etc ..
			list = container.getChildrenOfType(ICElement.C_CCONTAINER);
			for (int i = 0; i < list.size(); i++) {
				Object o = list.get(i);
				if (o instanceof ICContainer) {
					ITranslationUnit tu = findTranslationUnit((ICContainer)o, filename);
					if (tu != null) {
						return tu;
					}
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}
}
