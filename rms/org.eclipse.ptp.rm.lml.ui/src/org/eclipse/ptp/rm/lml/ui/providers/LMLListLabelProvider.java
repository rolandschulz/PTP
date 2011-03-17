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
import org.eclipse.ptp.rm.lml.core.elements.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiItem;
import org.eclipse.swt.graphics.Image;

public class LMLListLabelProvider extends LabelProvider {
	
	public String getText(Object obj) {
		ILguiItem lgui = (LguiItem) obj;
		return lgui.toString();
	}
	
	public Image getImage(Object obj, int index) {
		return null;
	}
	
}
