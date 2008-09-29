/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.cell.sputiming.ui.views;

import org.eclipse.swt.graphics.Color;

public class DiagramBlock {

	private int   position;
	private int   size;
	private Color color;
	
	public DiagramBlock(int position, int size, Color color) {
		super();
		this.position = position;
		this.size     = size;
		this.color    = color;
	}//constructor

	public int getPosition() {
		return position;
	}

	public int getSize() {
		return size;
	}

	public Color getColor() {
		return color;
	}
	
}//class
