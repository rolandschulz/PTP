/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 *******************************************************************************/
package org.eclipse.ptp.rm.lml.ui;

import org.eclipse.swt.graphics.Image;


public interface IRuntimeModelPresentation {
	/*
	 * Returns an image for the element, or null if a default image should be used.
	 */
	public Image getImage(Object element);
	
	/*
	 * Returns a label for the element, or null if a default label should be used.
	 */
	public String getText(Object element);
}
