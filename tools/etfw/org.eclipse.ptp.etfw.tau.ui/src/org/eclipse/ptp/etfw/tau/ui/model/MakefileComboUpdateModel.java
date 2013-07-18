/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.tau.ui.model;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.tau.ui.TAUMakefileCombo;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class MakefileComboUpdateModel extends AbstractUpdateModel implements SelectionListener {

	private final TAUMakefileCombo makefileCombo;

	public MakefileComboUpdateModel(String name, IUpdateHandler handler, Control control) {
		super(name, handler);
		this.makefileCombo = (TAUMakefileCombo) control;
		this.makefileCombo.getCombo().addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				try {
					Object value = storeValue();
					handleUpdate(value);
					if(value!=null&&value instanceof String){
						makefileCombo.setSelectedMakefile((String)value);
					}
				} catch (Exception ignored) {
				}
			}
		});
	}

	@Override
	public Object getValueFromControl() {
		return makefileCombo.getSelection();
	}

	@Override
	public void refreshValueFromMap() {
		this.makefileCombo.setVariableMap(this.lcMap);
	}

	@Override
	public Object getControl() {
		return makefileCombo;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		try {
			Object value = storeValue();
			handleUpdate(value);
			if(value!=null&&value instanceof String){
				makefileCombo.setSelectedMakefile((String)value);
			}
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rm.jaxb.control.ui.model.AbstractUpdateModel#initialize(org.eclipse.ptp.rm.jaxb.core.IVariableMap,
	 * org.eclipse.ptp.rm.jaxb.core.IVariableMap)
	 */
	@Override
	public void initialize(ILaunchConfiguration configuration, IVariableMap rmMap, IVariableMap lcMap) {
		
		String val = (String)lcMap.getValue(name);
		if(val!=null&&val.length()>0&&val.contains("Makefile.tau")){
			makefileCombo.setSelectedMakefile(val);
		}
		
		makefileCombo.setConfiguration(configuration);
		
		super.initialize(configuration, rmMap, lcMap);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

}
