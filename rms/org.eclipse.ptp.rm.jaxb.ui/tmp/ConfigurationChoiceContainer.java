/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.dialogs;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.window.Window;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.rm.JAXBRMConfigurationManager;
import org.eclipse.ptp.rm.jaxb.core.utils.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.RemoteUIServicesUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;

public abstract class ConfigurationChoiceContainer implements IJAXBUINonNLSConstants {

	private class WidgetListener implements SelectionListener {
		private boolean disabled = false;

		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		public synchronized void widgetSelected(SelectionEvent e) {
			if (disabled) {
				return;
			}
			Object source = e.getSource();
			if (source == preset) {
				handlePresetSelected();
			} else if (source == external) {
				handleExternalSelected();
			} else if (source == browseButton) {
				handlePathBrowseButtonSelected();
			}
			onUpdate();
		}

		private synchronized void disable() {
			disabled = true;
		}

		private synchronized void enable() {
			disabled = false;
		}
	}

	private final Text choice;
	private final Combo preset;
	private final Combo external;
	private final Button browseButton;
	private final WidgetListener listener;
	private final Shell shell;

	private String selected;
	private boolean isPreset;
	private IJAXBResourceManagerConfiguration config;
	private RemoteServicesDelegate delegate;
	private IMemento memento;
	private JAXBRMConfigurationManager available;

	public ConfigurationChoiceContainer(Composite parent) {
		shell = parent.getShell();
		listener = new WidgetListener();

		GridLayout layout = WidgetBuilderUtils.createGridLayout(3, true);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(3);
		Group group = WidgetBuilderUtils.createGroup(parent, SWT.SHADOW_ETCHED_IN, layout, gd);

		WidgetBuilderUtils.createLabel(group, Messages.JAXBRMConfigurationSelectionWizardPage_4, SWT.LEFT, 1);
		gd = WidgetBuilderUtils.createGridDataFillH(2);
		choice = WidgetBuilderUtils.createText(group, SWT.BORDER, gd, true, selected);

		layout = WidgetBuilderUtils.createGridLayout(3, true);
		gd = WidgetBuilderUtils.createGridDataFillH(3);
		group = WidgetBuilderUtils.createGroup(parent, SWT.SHADOW_ETCHED_IN, layout, gd);

		preset = WidgetBuilderUtils.createCombo(group, 2, new String[0], ZEROSTR,
				Messages.JAXBRMConfigurationSelectionComboTitle_0, ZEROSTR, listener);

		layout = WidgetBuilderUtils.createGridLayout(3, true);
		gd = WidgetBuilderUtils.createGridDataFillH(3);
		group = WidgetBuilderUtils.createGroup(parent, SWT.SHADOW_ETCHED_IN, layout, gd);

		external = WidgetBuilderUtils.createCombo(group, 2, new String[0], ZEROSTR,
				Messages.JAXBRMConfigurationSelectionComboTitle_1, ZEROSTR, listener);

		browseButton = WidgetBuilderUtils.createPushButton(group, Messages.JAXBRMConfigurationSelectionWizardPage_1, listener);

		selected = ZEROSTR;
		isPreset = true;
	}

	public boolean choiceIsPreset() {
		return isPreset;
	}

	public String getSelected() {
		return selected;
	}

	public void setAvailableConfigurations() {
		available = JAXBRMConfigurationManager.getInstance();

		if (preset != null) {
			listener.disable();
			preset.setItems(available.getTypes());
			listener.enable();
		}

		if (config != null) {
			available.addExternalPaths(config.getExternalRMInstanceXMLLocations());
		} else if (memento != null) {
			available.addExternalPaths(getExternalLocations());
		}

		if (external != null) {
			String[] items = available.getExternal();
			external.setItems(items);
			if (config != null) {
				config.setExternalRMInstanceXMLLocations(items);
			} else if (memento != null) {
				setExternalRMInstanceXMLLocations(items);
			}
		}

		if (config != null) {
			selected = config.getRMInstanceXMLLocation();
		} else if (memento != null) {
			selected = memento.getString(RM_XSD_PATH);
		}

		if (selected != null) {
			String type = available.getTypeForPath(selected);
			if (type != null) {
				isPreset = true;
				choice.setText(type);
				for (int i = 0; i < preset.getItems().length; i++) {
					if (type.equals(preset.getItem(i))) {
						preset.select(i);
						break;
					}
				}
			} else {
				isPreset = false;
				choice.setText(selected);
				for (int i = 0; i < external.getItems().length; i++) {
					if (selected.equals(external.getItem(i))) {
						preset.select(i);
						break;
					}
				}
			}
		}
		onUpdate();
	}

	public void setConfig(IJAXBResourceManagerConfiguration config) {
		this.config = config;
	}

	public void setDelegate(RemoteServicesDelegate delegate) {
		this.delegate = delegate;
	}

	public void setExternalRMInstanceXMLLocations(String[] locations) {
		if (locations == null || locations.length == 0) {
			memento.putString(EXTERNAL_RM_XSD_PATHS, ZEROSTR);
		} else {
			StringBuffer list = new StringBuffer(locations[0]);
			for (int i = 1; i < locations.length; i++) {
				list.append(CM).append(locations[i]);
			}
			memento.putString(EXTERNAL_RM_XSD_PATHS, list.toString());
		}
	}

	public void setMemento(IMemento memento) {
		this.memento = memento;
	}

	protected abstract void onUpdate();

	private String[] getExternalLocations() {
		String list = memento.getString(EXTERNAL_RM_XSD_PATHS);
		if (list == null) {
			return new String[0];
		}
		return list.split(CM);
	}

	private void handleExternalSelected() {
		String text = external.getText();
		if (text != null) {
			selected = text;
			validateSelected();
			isPreset = false;
		} else {
			selected = ZEROSTR;
		}

		if (memento != null) {
			memento.putString(RM_XSD_PATH, selected);
		} else if (config != null) {
			config.setRMInstanceXMLLocation(selected);
		}

		choice.setText(text);
	}

	private void handlePathBrowseButtonSelected() {

		if (Window.OK != openConnectionChoiceDialog()) {
			return;
		}

		IRemoteResourceManagerConfiguration c = null;
		String id = c.getRemoteServicesId();
		String name = c.getConnectionName();
		if (delegate == null) {
			delegate = new RemoteServicesDelegate(id, name);
		}

		URI uri = null;
		if (name == null || name.indexOf(LOCAL) >= 0) {
			uri = delegate.getLocalHome();
		} else {
			uri = delegate.getRemoteHome();
		}

		try {
			uri = RemoteUIServicesUtils.browse(shell, uri, delegate);
		} catch (URISyntaxException t) {
			t.printStackTrace();
		}

		if (uri != null) {
			selected = uri.toString();
			choice.setText(selected);
			isPreset = false;
			updateExternal();
		}
	}

	private void handlePresetSelected() {
		String text = preset.getText();
		if (text != null) {
			selected = available.getPathForType(text);
			validateSelected();
			isPreset = true;
		} else {
			selected = ZEROSTR;
		}

		if (memento != null) {
			memento.putString(RM_XSD_PATH, selected);
		} else if (config != null) {
			config.setRMInstanceXMLLocation(selected);
		}

		choice.setText(text);
	}

	private int openConnectionChoiceDialog() {
		connectionDialog = new ConnectionChoiceDialog(shell);
		return connectionDialog.open();
	}

	private void updateExternal() {
		int len = external.getItems().length;
		int i = 0;
		for (; i < len; i++) {
			if (selected.equals(external.getItem(i))) {
				external.select(i);
				break;
			}
		}
		if (i == len) {
			available.addExternalPath(selected);
			String[] refreshed = available.getExternal();
			if (config != null) {
				config.setExternalRMInstanceXMLLocations(refreshed);
			} else if (memento != null) {
				setExternalRMInstanceXMLLocations(refreshed);
			}
			external.setItems(available.getExternal());
			external.select(i);
		}
	}

	private boolean validateSelected() {
		try {
			JAXBInitializationUtils.validate(selected);
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
			return false;
		}
		return true;
	}
}
