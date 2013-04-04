/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TypedPosition;

public class RemoteInactiveHighlightPosition extends TypedPosition implements IRegion {

	public RemoteInactiveHighlightPosition(int offset, int length, String type) {
		super(offset, length, type);
	}

	public RemoteInactiveHighlightPosition(IRegion region, String type) {
		super(region.getOffset(), region.getLength(), type);
	}
}
