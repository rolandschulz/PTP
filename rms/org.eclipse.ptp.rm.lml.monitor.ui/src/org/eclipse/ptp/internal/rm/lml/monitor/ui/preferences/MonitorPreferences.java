/**
 * Copyright (c) 2014 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.monitor.ui.preferences;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorCoreConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class constructs the preferences page for the monitoring perspective.
 * It allows to configure options such as the update interval or the
 * used caching mode. It controls all actions on the view.
 */
public class MonitorPreferences extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Spinner for update interval
	 */
	private Spinner intervalSpinner;

	/**
	 * If checked, per default a force update operation is run
	 */
	private Button forceUpdateButton;

	/**
	 * Component types, which can be added to this preference page
	 */
	private static enum ComponentType {
		SPINNER, CHECK, DESCRIPTION
	}

	/**
	 * Convenience method for creating GridData for various components.
	 * 
	 * @param grabHorizontalSpace
	 *            if true, horizontal space is grabbed by the component
	 * @param grabVerticalSpace
	 *            if true, vertical space is grabbed by the component
	 * @param span
	 *            number of columns, which are covered by the component
	 * @return one layout data for one of the components
	 */
	private GridData createLayoutData(boolean grabHorizontalSpace, boolean grabVerticalSpace, int span) {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = grabHorizontalSpace;
		gd.grabExcessVerticalSpace = grabVerticalSpace;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = span;

		return gd;
	}

	/**
	 * Creates one row in the preferences page.
	 * Each row can have a specific data type such as a simple description,
	 * a check box or a spinner for integer values.
	 * 
	 * @param parent
	 *            the parent composite with Gridlayout, into which the new row is placed
	 * @param type
	 *            the row's data type
	 * @param text
	 *            a label text for this option
	 * @return the generated row composite, which is already added to the passed parent control
	 */
	private Composite createRowComp(Composite parent, ComponentType type, String text) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(10, true);
		result.setLayout(gridLayout);

		if (type == ComponentType.SPINNER) {
			Label intervalLabel = new Label(result, SWT.NONE);
			intervalLabel.setText(text);
			intervalLabel.setLayoutData(createLayoutData(true, false, 6));

			intervalSpinner = new Spinner(result, SWT.NONE);
			// Default selection is 60 seconds, minimum is 1 second, maximum 1 hour, no digits after the period, a normal increase
			// is done by 10, a page increase with 60 seconds
			intervalSpinner.setValues(60, 1, 3600, 0, 10, 60);
			intervalSpinner.setLayoutData(createLayoutData(true, false, 4));
			intervalSpinner.setToolTipText(Messages.MonitorPreferences_2);
		}
		else if (type == ComponentType.CHECK) {
			forceUpdateButton = new Button(result, SWT.CHECK);
			forceUpdateButton.setText(text);
			forceUpdateButton.setLayoutData(createLayoutData(true, false, 10));
			forceUpdateButton.setToolTipText(Messages.MonitorPreferences_3);
		}
		else if (type == ComponentType.DESCRIPTION) {
			Label descLabel = new Label(result, SWT.NONE);
			descLabel.setText(text);
			descLabel.setLayoutData(createLayoutData(true, false, 10));
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		// Add my own component as a single cell into parent
		GridLayout parentLayout = new GridLayout(1, true);
		parent.setLayout(parentLayout);
		// Vertical alignment is set to beginning, so that all elements start at the top of the available space
		Composite optionComp = new Composite(parent, SWT.NONE);
		optionComp.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		optionComp.setLayout(fillLayout);
		optionComp.setLayoutData(createLayoutData(true, true, 1));
		// Each createRowComp is responsible for generating one option row
		createRowComp(optionComp, ComponentType.DESCRIPTION, Messages.MonitorPreferences_4);
		createRowComp(optionComp, ComponentType.SPINNER, Messages.MonitorPreferences_0);
		createRowComp(optionComp, ComponentType.CHECK, Messages.MonitorPreferences_1);

		loadData(false);

		return null;
	}

	@Override
	public void performApply() {
		super.performApply();
		saveCurrentData();
		loadData(false);
	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		loadData(true);
	}

	@Override
	public boolean performOk() {
		super.performOk();
		saveCurrentData();
		return true;
	}

	/**
	 * Read data from preferences model.
	 * Place current state into the option fields.
	 * This function only has an effect, if the UI components
	 * were already constructed. Either the default values
	 * or the current values can be loaded here.
	 * 
	 * @param defaultValues
	 *            if true, default values are loaded, otherwise the current values are loaded
	 */
	public void loadData(boolean defaultValues) {
		IEclipsePreferences currentPref = InstanceScope.INSTANCE.getNode(IMonitorCoreConstants.PLUGIN_ID);
		IEclipsePreferences defaultPref = DefaultScope.INSTANCE.getNode(IMonitorCoreConstants.PLUGIN_ID);

		int intervalDefault = defaultPref.getInt(IMonitorCoreConstants.PREF_UPDATE_INTERVAL,
				IMonitorCoreConstants.PREF_UPDATE_INTERVAL_DEFAULT);
		int interval = currentPref.getInt(IMonitorCoreConstants.PREF_UPDATE_INTERVAL, intervalDefault);
		if (defaultValues) {
			interval = intervalDefault;
		}
		setInterval(interval);

		boolean defaultForce = defaultPref.getBoolean(IMonitorCoreConstants.PREF_FORCE_UPDATE,
				IMonitorCoreConstants.PREF_FORCE_UPDATE_DEFAULT);
		boolean force = currentPref.getBoolean(IMonitorCoreConstants.PREF_FORCE_UPDATE, defaultForce);
		if (defaultValues) {
			force = defaultForce;
		}
		setForceUpdate(force);
	}

	/**
	 * Transfers data from the view components into the preferences
	 * data model. This only works, if the components were created already.
	 */
	public void saveCurrentData() {
		if (intervalSpinner == null || forceUpdateButton == null) {
			return;
		}

		IEclipsePreferences currentPref = InstanceScope.INSTANCE.getNode(IMonitorCoreConstants.PLUGIN_ID);

		int interval = getInterval();
		currentPref.putInt(IMonitorCoreConstants.PREF_UPDATE_INTERVAL, interval);
		boolean force = isForceUpdate();
		currentPref.putBoolean(IMonitorCoreConstants.PREF_FORCE_UPDATE, force);
	}

	/**
	 * Place an interval value into the interval spinner.
	 * 
	 * @param interval
	 *            the loaded interval value in seconds
	 */
	public void setInterval(int interval) {
		if (intervalSpinner == null) {
			return;
		}
		// Enforce spinner boundaries
		if (interval < intervalSpinner.getMinimum()) {
			interval = intervalSpinner.getMinimum();
		}
		if (interval > intervalSpinner.getMaximum()) {
			interval = intervalSpinner.getMaximum();
		}

		intervalSpinner.setSelection(interval);
	}

	/**
	 * @return -1, if component is not loaded yet, otherwise the update interval in seconds read from the component
	 */
	public int getInterval() {
		if (intervalSpinner == null) {
			return -1;
		}

		return intervalSpinner.getSelection();
	}

	/**
	 * @return true, if per default a force update operation should be run, false otherwise
	 */
	public boolean isForceUpdate() {
		if (forceUpdateButton == null) {
			return false;
		}

		return forceUpdateButton.getSelection();
	}

	/**
	 * Set the selection mode of the check button for update forcing.
	 * 
	 * @param force
	 *            true, if per default a force update operation should be run, false otherwise
	 */
	public void setForceUpdate(boolean force) {
		if (forceUpdateButton == null) {
			return;
		}
		forceUpdateButton.setSelection(force);
	}

}
