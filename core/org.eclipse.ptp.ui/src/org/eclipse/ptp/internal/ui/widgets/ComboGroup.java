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

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Combobox based group
 * 
 * @author Richard Maciel
 * 
 */
public class ComboGroup extends GenericControlGroup {

	public static final int MAX_SIZE = Combo.LIMIT;

	Combo combo;

	// Array that keep track of the combo items. Its index is based on the combo index.
	Vector<ComboGroupItem> comboItems = new Vector<ComboGroupItem>();

	/**
	 * @param parent
	 * @param mold
	 */
	public ComboGroup(Composite parent, GenericControlMold mold) {
		super(parent, mold);

		ComboMold cmold = (ComboMold) mold;

		for (ComboGroupItem item : cmold.items) {
			add(item);
		}
	}

	@Override
	protected Control createCustomControl(int bitmask, GridData gd) {

		if ((bitmask & ComboMold.EDITABLE) != 0) {
			combo = new Combo(this, SWT.BORDER);
		} else {
			combo = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
		}
		return combo;
	}

	public Combo getCombo() {
		return combo;
	}

	public int getSelectionIndex() {
		return combo.getSelectionIndex();
	}

	public String getSelectionId() {
		if (combo.getSelectionIndex() == -1) {
			return null;
		} else {
			return getItemUsingIndex(combo.getSelectionIndex()).getId();
		}
	}

	public void setSelectionIndex(int index) {
		combo.select(index);
	}

	public void add(ComboGroupItem comboItem) {
		comboItems.add(comboItem);
		combo.add(comboItem.value);
	}

	public void add(ComboGroupItem comboItem, int index) {
		comboItems.add(index, comboItem);
		combo.add(comboItem.value, index);
	}

	/**
	 * Retrieves an item using the item id.
	 * 
	 * @param id
	 * @return A ComboGroupItem or null if the id doesn't exist.
	 */
	public ComboGroupItem getItemUsingID(String id) {
		for (ComboGroupItem citem : comboItems) {
			if (citem.id.equals(id)) {
				return citem;
			}
		}
		return null;
	}

	public void selectIndexUsingID(String id) {
		int index = 0;
		for (ComboGroupItem citem : comboItems) {
			if (citem.id.equals(id)) {
				combo.select(index);
				return;
			}
			index++;
		}
		if (comboItems.size() > 0) {
			combo.select(0);
		}
	}

	/**
	 * Returns the {@link ComboGroupItem} located at index
	 * 
	 * @param index
	 * @return
	 */
	public ComboGroupItem getItemUsingIndex(int index) {
		if (index < 0 || comboItems.get(index) == null) {
			return new ComboGroupItem("", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return comboItems.get(index);
	}

	public ComboGroupItem getSelectedItem() {
		return getItemUsingIndex(combo.getSelectionIndex());
	}
}
