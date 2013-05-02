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
package org.eclipse.ptp.internal.ui.widgets;

import java.util.ArrayList;
import java.util.List;

/**
 * @author richardm
 * 
 */
public class ComboMold extends GenericControlMold {

	public static final int WIDTH_PROPORTIONAL_NUM_CHARS = 1 << index++;

	public static final int EDITABLE = 1 << index++;

	List<ComboGroupItem> items = new ArrayList<ComboGroupItem>();
	int size = -1;

	public ComboMold() {
		super(0);
	}

	public ComboMold(int bitmask) {
		super(bitmask);
	}

	public ComboMold(String label) {
		super(0, label);
	}

	public ComboMold(int bitmask, String label) {
		super(bitmask, label);
	}

	// public ComboMold(int bitmask, String label, String [] items) {
	// super(bitmask, label);
	// setItems(items);
	// }

	public void setItems(String[] entries) {
		for (String string : entries) {
			this.items.add(new ComboGroupItem(null, string));
		}
	}

	public void addItem(String key, String value) {
		this.items.add(new ComboGroupItem(key, value));
	}

	public int getTextFieldWidth() {
		return size;
	}

	public void setTextFieldWidth(int size) {
		this.size = size;
		if (size > 0) {
			addBitmask(WIDTH_PROPORTIONAL_NUM_CHARS);
		} else {
			removeBitmask(WIDTH_PROPORTIONAL_NUM_CHARS);
		}
	}

	@Override
	protected int getHeight() {
		return 0;
	}

	@Override
	protected int getWidth() {
		return size;
	}

	@Override
	protected boolean hasHeight() {
		return false;
	}

	@Override
	protected boolean hasWidth() {
		return (bitmask & WIDTH_PROPORTIONAL_NUM_CHARS) != 0;
	}
}
