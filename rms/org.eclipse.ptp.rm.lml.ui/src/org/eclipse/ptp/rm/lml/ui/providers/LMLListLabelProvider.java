/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch
 */
package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.model.LguiItem;
import org.eclipse.swt.graphics.Image;

public class LMLListLabelProvider extends LabelProvider {

	public String getText(Object obj) {
		return (String) obj;
	}
	
	
	public Image getImage(Object obj, int index) {
		return null;
	}
	
}
