/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.environment.remotesimulator.preferences.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ptp.cell.environment.remotesimulator.Activator;
import org.eclipse.ptp.cell.environment.remotesimulator.core.ConfigFactory;
import org.eclipse.ptp.cell.preferences.ui.AbstractBaseFieldEditorPreferencePage;
import org.eclipse.ptp.cell.preferences.ui.LabelFieldEditor;
import org.eclipse.ptp.cell.preferences.ui.SpacerFieldEditor;
import org.eclipse.ui.IWorkbench;


/**
 * 
 * @author Richard Maciel
 * @since 1.2.1
 */
public class PreferencePage extends
AbstractBaseFieldEditorPreferencePage {

	public PreferencePage() {
		super(GRID);
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		
		setDescription(Messages.PreferencePage_Description);
	}
	
	protected void createFieldEditors() {
		setTitle(Messages.PreferencePage_Description);

		
		StringFieldEditor remaddrfield = new StringFieldEditor(ConfigFactory.ATTR_REMOTE_CONNECTION_ADDRESS,
				ConfigFactory.NAME_REMOTE_CONNECTION_ADDRESS,
				getFieldEditorParent());
				addField(remaddrfield);
		
		IntegerFieldEditor remportfield = new IntegerFieldEditor(ConfigFactory.ATTR_REMOTE_CONNECTION_PORT,
				ConfigFactory.NAME_REMOTE_CONNECTION_PORT,
				getFieldEditorParent());
				addField(remportfield);
				
		StringFieldEditor remusernamefield = new StringFieldEditor(ConfigFactory.ATTR_REMOTE_LOGIN_USERNAME,
			ConfigFactory.NAME_REMOTE_LOGIN_USERNAME,
			getFieldEditorParent());
			addField(remusernamefield);
		remusernamefield.setEmptyStringAllowed(true);
		
		/*IntegerFieldEditor portminfield = new IntegerFieldEditor(ConfigFactory.ATTR_TUNNEL_PORT_MIN,
				ConfigFactory.NAME_TUNNEL_PORT_MIN,
				getFieldEditorParent());
				addField(portminfield);
		IntegerFieldEditor portmaxfield = new IntegerFieldEditor(ConfigFactory.ATTR_TUNNEL_PORT_MAX,
				ConfigFactory.NAME_TUNNEL_PORT_MAX,
				getFieldEditorParent());
				addField(portmaxfield);*/
		
		addSpace();
		
		StringFieldEditor simaddrfield = new StringFieldEditor(ConfigFactory.ATTR_SIMULATOR_CONNECTION_ADDRESS,
				ConfigFactory.NAME_SIMULATOR_CONNECTION_ADDRESS,
				getFieldEditorParent());
				addField(simaddrfield);
		IntegerFieldEditor simportfield = new IntegerFieldEditor(ConfigFactory.ATTR_SIMULATOR_CONNECTION_PORT,
				ConfigFactory.NAME_SIMULATOR_CONNECTION_PORT,
				getFieldEditorParent());
				addField(simportfield);
		
		StringFieldEditor simusernamefield = new StringFieldEditor(ConfigFactory.ATTR_SIMULATOR_LOGIN_USERNAME,
			ConfigFactory.NAME_SIMULATOR_LOGIN_USERNAME,
			getFieldEditorParent());
			addField(simusernamefield);
			simusernamefield.setEmptyStringAllowed(true);
			addField(simusernamefield);
			
		IntegerFieldEditor simtimeoutfield = new IntegerFieldEditor(ConfigFactory.ATTR_SIMULATOR_CONNECTION_TIMEOUT,
				ConfigFactory.NAME_SIMULATOR_CONNECTION_TIMEOUT,
				getFieldEditorParent());
				addField(simtimeoutfield);
				
		addSpace();
		addField(new LabelFieldEditor(Messages.PreferencePage_HeaderLaunch, getFieldEditorParent()));
		StringFieldEditor systemWorkspaceDir = new StringFieldEditor(ConfigFactory.ATTR_SYSTEM_WORKSPACE, 
				Messages.PreferencePage_LabelSystemWorkspace, getFieldEditorParent());
		systemWorkspaceDir.setEmptyStringAllowed(false);
		addField(systemWorkspaceDir);
}

	public void init(IWorkbench workbench) {
	}
	
	void addSpace() {
		SpacerFieldEditor spacer1 = new SpacerFieldEditor(
                getFieldEditorParent());
		addField(spacer1);
	}

}