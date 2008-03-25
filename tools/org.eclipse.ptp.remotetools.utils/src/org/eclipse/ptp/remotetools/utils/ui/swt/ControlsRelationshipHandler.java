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
package org.eclipse.ptp.remotetools.utils.ui.swt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

/**
 * This handler class provides a simple implementation to enable/disable controls dependent
 * on buttons state.
 * 
 * @author Richard Maciel
 *
 */
public class ControlsRelationshipHandler {

	Map controlsRelationship;
	ControlRelationshipListener controlHandler;
	boolean enableWhenMasterEnabled;
	
	/**
	 * Builds the handler from using a {@link Map} that contains the relationship between a master control and
	 * a List of slave controls. The slave controls on the list only will be enabled when the associated master control is
	 * selected. 
	 * 
	 * @param controlsRelationship {@link Map} A map containing the relation  
	 * @param enableWhenMasterEnabled boolean Flag that let the user choose between enable controls when the master control is enabled or disabled
	 */
	public ControlsRelationshipHandler(Map controlsRelationship, boolean enableWhenMasterEnabled) {
		this.controlsRelationship = controlsRelationship;
		controlHandler = new ControlRelationshipListener();
		this.enableWhenMasterEnabled = enableWhenMasterEnabled;
	}
	
	/**
	 * Build the handler using an empty relation map.
	 *
	 */
	public ControlsRelationshipHandler() {
		this(new HashMap(), true);
	}

	/**
	 * Build a handler and add a relationship. This constructor is useful for checkbox controls that
	 * enable/disable the interface
	 * 
	 * @param master
	 * @param slaves
	 */
	public ControlsRelationshipHandler(Button master, Control [] slaves, boolean enableWhenTrue) {
		this(new HashMap(), enableWhenTrue);
		addControlRelationship(master, slaves);
	}
	
	public ControlsRelationshipHandler(Button master, Control slave, boolean enableWhenTrue) {
		this(new HashMap(), enableWhenTrue);
		addControlRelationship(master, slave);
	}

	/**
	 * This method creates a relationship between master and slaves, so the slaves will only be enabled when
	 * the master is selected. 
	 * 
	 * @param master
	 * @param slaves
	 */
	public void addControlRelationship(Button master, Control [] slaves) {
		controlsRelationship.put(master, slaves);
		master.addSelectionListener(controlHandler);
	}

	public void addControlRelationship(Button master, Control slave) {
		controlsRelationship.put(master, new Control[] { slave });
		master.addSelectionListener(controlHandler);
	}

	public void deleteControlRelationship(Button master) {
		controlsRelationship.remove(master);
		master.removeSelectionListener(controlHandler);
	}

	/**
	 * Enables the controls dependent on the selected control and disables all the others
	 * @param btn Button Control that generated a SelectionEvent
	 */
	public void manageDependentControls(Button btn) {
		// Iterates over all the Map keys to get associated controls
		Set masterSet = controlsRelationship.keySet();
		Iterator it = masterSet.iterator();
		while(it.hasNext()) {
			// Get the list of controls of related to the key
			Button master = (Button)it.next();
			
			// List can be null
			Object obj = controlsRelationship.get(master);
			if(obj != null) {
				Control [] controlList = (Control [])obj;
				
				// Enable all controls that are related to the key received as parameter.
				for(int i=0; i < controlList.length; i++) {
					if(btn == master && btn.getSelection()) {
						controlList[i].setEnabled(enableWhenMasterEnabled);
					} else {
						controlList[i].setEnabled(!enableWhenMasterEnabled);
					}
				}
			}
		}
	}

	class ControlRelationshipListener extends SelectionAdapter {

		public void widgetSelected(SelectionEvent arg0) {
			manageDependentControls((Button)arg0.widget);
		}
	}
	
}
