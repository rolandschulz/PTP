/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A preference page already prepared to inter-operate with {@link PreferenceDataSource} and {@link PreferenceWidgetListener}.
 * @author dfferber
 *
 */
public abstract class AbstractPreferencePage extends PreferencePage {

	private final PreferenceWidgetListener listener = createListener();
	private final PreferenceDataSource dataSource = createDataSource();

	public AbstractPreferencePage() {
		super();
	}

	public AbstractPreferencePage(String title) {
		super(title);
	}

	public AbstractPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}
	
	protected PreferenceWidgetListener getListener() {
		return listener;
	}
	
	protected PreferenceDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Get storage for preferences.
	 * @return The storage for preferences.
	 */
	public abstract Preferences getPreferences();

	/**
	 * Called when the preferences storage should be saved.
	 */
	public abstract void savePreferences();

	/**
	 * Create listener for the preference page. The listener must extend {@link PreferenceWidgetListener} and add
	 * specific behavior for widgets of the the preference page.
	 * @return the listener
	 */
	protected abstract PreferenceWidgetListener createListener();

	/**
	 * Create data source to handle page content. The listener must extend {@link PreferenceDataSource} and add
	 * specific behavior for widgets of the the preference page.
	 * @return the listener
	 */
	protected abstract PreferenceDataSource createDataSource();

	@Override
	public boolean performOk() {
			resetErrorMessages();
			dataSource.storeAndValidate();
			return true;
	//		return super.performOk();
		}

	@Override
	protected void performDefaults() {
			resetErrorMessages();
			listener.disable();
			dataSource.loadDefaultsAndUpdate();
			listener.enable();
	//		super.performDefaults();
		}

	@Override
	protected void performApply() {
			resetErrorMessages();
			dataSource.storeAndValidate();
	//		super.performApply();
		}

	protected void resetErrorMessages() {
		setErrorMessage(null);
		setMessage(null);
		setValid(true);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = doCreateContents(parent);
		resetErrorMessages();
		listener.disable();
		dataSource.loadAndUpdate();
		listener.enable();
		updateControls();
		return contents;
	}

	abstract protected void updateControls();

	/**
	 * Create contents for the preference page.
	 * @param parent
	 * @return
	 */
	abstract protected Composite doCreateContents(Composite parent);
}