/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.internal.ui.browser.cbrowsing;

import org.eclipse.fdt.core.browser.ITypeInfo;
import org.eclipse.fdt.ui.browser.typeinfo.TypeInfoLabelProvider;
import org.eclipse.swt.graphics.Image;

public class TypesViewLabelProvider extends CBrowsingLabelProvider {

	protected static final TypeInfoLabelProvider fTypeInfoLabelProvider = new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_TYPE_ONLY);
    
    public TypesViewLabelProvider() {
        super();
    }
    
    public Image getImage(Object element) {
    	if (element instanceof ITypeInfo)
    		return fTypeInfoLabelProvider.getImage(element);
    	return super.getImage(element);
    }

    public String getText(Object element) {
    	if (element instanceof ITypeInfo)
    		return fTypeInfoLabelProvider.getText(element);
    	return super.getText(element);
    }
}
