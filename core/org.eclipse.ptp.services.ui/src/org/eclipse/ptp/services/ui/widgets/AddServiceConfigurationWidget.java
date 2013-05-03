/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.ui.widgets;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.dialogs.ServiceProviderConfigurationDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A widget that allows the user to create a new service configuration or select
 * an existing service configuration. The widget also provides an "Advanced"
 * button to perform any additional configuration that may be required.
 */
public class AddServiceConfigurationWidget extends Composite implements ISelectionProvider {
	private class ConfigurationSelectionEvent implements IStructuredSelection {
		private final Object selection = getServiceConfiguration();

		public Object getFirstElement() {
			return isEmpty() ? null : toArray()[0];
		}

		public boolean isEmpty() {
			return toArray().length == 0;
		}

		@SuppressWarnings("rawtypes")
		public Iterator iterator() {
			return toList().iterator();
		}

		public int size() {
			return toArray().length;
		}

		public Object[] toArray() {
			return (selection == null) ? new Object[0] : new Object[] { selection };
		}

		@SuppressWarnings("rawtypes")
		public List toList() {
			return Arrays.asList(selection);
		}
	}

	private final Text fNewConfigNameText;
	private final Button fNewConfigButton;
	private final Button fExistingConfigButton;
	private final Button fAdvancedButton;

	private ISelection fSelection = null;
	private IServiceConfiguration fDefaultConfig = null;
	private IServiceConfiguration fServiceConfig = null;
	private final ServiceConfigurationSelectionWidget fServiceConfigWidget;

	private final ListenerList fSelectionListeners = new ListenerList();

	public AddServiceConfigurationWidget(Composite parent, int style) {
		this(parent, style, null, null, false);
	}

	public AddServiceConfigurationWidget(Composite parent, int style, Set<IServiceConfiguration> excluded, Set<IService> services,
			boolean enableButtons) {
		super(parent, style);

		GridLayout bodyLayout = new GridLayout(1, false);
		bodyLayout.marginHeight = 0;
		bodyLayout.marginWidth = 0;
		setLayout(bodyLayout);

		fNewConfigButton = new Button(this, SWT.RADIO);
		fNewConfigButton.setText(Messages.AddServiceConfigurationWidget_0);
		fNewConfigButton.setLayoutData(new GridData());
		fNewConfigButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSelectionUpdate();
				notifySelection(new ConfigurationSelectionEvent());
			}
		});
		fNewConfigButton.setEnabled(false);

		Composite newComp = new Composite(this, SWT.NONE);
		newComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginLeft = 10;
		newComp.setLayout(layout);

		final Label label = new Label(newComp, SWT.NONE);
		label.setText(Messages.AddServiceConfigurationWidget_1);
		label.setLayoutData(new GridData());

		fNewConfigNameText = new Text(newComp, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		fNewConfigNameText.setLayoutData(data);
		fNewConfigNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fDefaultConfig.setName(fNewConfigNameText.getText());
			}
		});

		fExistingConfigButton = new Button(this, SWT.RADIO);
		fExistingConfigButton.setText(Messages.AddServiceConfigurationWidget_2);
		fExistingConfigButton.setLayoutData(new GridData());
		fExistingConfigButton.setEnabled(configurationCount(excluded) != 0);

		Composite existingComp = new Composite(this, SWT.NONE);
		layout = new GridLayout(1, false);
		layout.marginLeft = 10;
		existingComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		existingComp.setLayout(layout);

		fServiceConfigWidget = new ServiceConfigurationSelectionWidget(existingComp, SWT.NONE, excluded, services, enableButtons);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		fServiceConfigWidget.setLayoutData(data);
		fServiceConfigWidget.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fServiceConfig = fServiceConfigWidget.getSelectedConfiguration();
				fAdvancedButton.setEnabled(!event.getSelection().isEmpty());
				notifySelection(event.getSelection());
			}
		});
		fServiceConfigWidget.setEnabled(false);

		fAdvancedButton = new Button(this, SWT.PUSH);
		data = new GridData(SWT.END, SWT.TOP, true, true);
		fAdvancedButton.setLayoutData(data);
		fAdvancedButton.setText(Messages.AddServiceConfigurationWidget_3);
		fAdvancedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ServiceProviderConfigurationDialog dialog = new ServiceProviderConfigurationDialog(getShell(),
						getServiceConfiguration());
				dialog.open();
			}
		});

		updateButtons(true);
	}

	private int configurationCount(Set<IServiceConfiguration> excluded) {
		Set<IServiceConfiguration> configs = ServiceModelManager.getInstance().getConfigurations();
		if (excluded != null) {
			configs.removeAll(excluded);
		}
		return configs.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return fSelection;
	}

	/**
	 * Get the currently selected service configuration
	 * 
	 * @return service configuration
	 */
	public IServiceConfiguration getServiceConfiguration() {
		return fServiceConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.remove(listener);
	}

	/**
	 * Set the configuration that will be returned if the "New" radio button is
	 * selected. The name of the configuration is displayed in the text box.
	 * 
	 * @param config
	 *            default new configuration
	 */
	public void setDefaultConfiguration(IServiceConfiguration config) {
		if (config != null) {
			fDefaultConfig = config;
			fNewConfigNameText.setText(config.getName());
			fNewConfigButton.setEnabled(true);
			doSelectionUpdate();
		}
	}

	/**
	 * Set the selected status of the buttons.
	 * 
	 * @param newButtonSelected
	 *            if true, the new button will be selected
	 */
	public void setSelection(boolean newButtonSelected) {
		if (fDefaultConfig != null) {
			updateButtons(newButtonSelected);
			doSelectionUpdate();
			notifySelection(new ConfigurationSelectionEvent());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse
	 * .jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		fSelection = selection;
	}

	/**
	 * Update widgets when a radio button is selected.
	 */
	private void doSelectionUpdate() {
		fNewConfigNameText.setEnabled(fNewConfigButton.getSelection());
		fServiceConfigWidget.setEnabled(!fNewConfigButton.getSelection());
		if (fNewConfigButton.getSelection()) {
			fServiceConfig = fDefaultConfig;
			fAdvancedButton.setEnabled(true);
		} else {
			fServiceConfig = fServiceConfigWidget.getSelectedConfiguration();
			fAdvancedButton.setEnabled(fServiceConfigWidget.getSelectedConfiguration() != null);
		}
	}

	/**
	 * Notify all listeners of the selection.
	 * 
	 * @param e
	 *            event that was generated by the selection
	 */
	private void notifySelection(ISelection selection) {
		setSelection(selection);
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		for (Object listener : fSelectionListeners.getListeners()) {
			((ISelectionChangedListener) listener).selectionChanged(event);
		}
	}

	/**
	 * Update the button selection status. If the existing config button is
	 * disabled, then this always sets the new config button.
	 * 
	 * @param newSelected
	 *            new config button status
	 */
	private void updateButtons(boolean newSelected) {
		if (fExistingConfigButton.isEnabled()) {
			fNewConfigButton.setSelection(newSelected);
			fExistingConfigButton.setSelection(!newSelected);
		} else {
			fNewConfigButton.setSelection(true);
		}
	}
}
