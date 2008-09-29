/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.preferences.events;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * @author laggarcia
 * @since 3.0.0
 */
public class FollowBeginStringFieldEditorPropertyChangeListener implements
		IPropertyChangeListener {

	protected StringFieldEditor stringFieldEditor;

	protected Text textControl;

	protected IPreferenceStore preferenceStore;

	protected String preferenceName;

	protected Display display;

	/**
	 * 
	 */
	public FollowBeginStringFieldEditorPropertyChangeListener(
			StringFieldEditor stringFieldEditor, Composite parent) {
		this.stringFieldEditor = stringFieldEditor;
		this.textControl = stringFieldEditor.getTextControl(parent);
		this.display = this.textControl.getDisplay();
		this.preferenceStore = stringFieldEditor.getPreferenceStore();
		this.preferenceName = stringFieldEditor.getPreferenceName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		final String oldValue = (String) event.getOldValue(); // old value for
																// the property
																// being
																// monitored
		final String newValue = (String) event.getNewValue(); // new value for
																// the property
																// being
																// monitored
		// Update the value in the GUI or in the preference store, which one is
		// active
		if (this.display != null) {
			this.display.asyncExec(new Runnable() {
				public void run() {
					String stringValue;
					String stringNewValue;
					if (!FollowBeginStringFieldEditorPropertyChangeListener.this.textControl
							.isDisposed()) {
						stringValue = FollowBeginStringFieldEditorPropertyChangeListener.this.stringFieldEditor
								.getStringValue();
						if (stringValue.startsWith(oldValue)) {
							stringNewValue = stringValue.replaceFirst(oldValue,
									newValue);
							FollowBeginStringFieldEditorPropertyChangeListener.this.stringFieldEditor
									.setStringValue(stringNewValue);
						}
					} else {
						stringValue = FollowBeginStringFieldEditorPropertyChangeListener.this.preferenceStore
								.getString(FollowBeginStringFieldEditorPropertyChangeListener.this.preferenceName);
						if (stringValue.startsWith(oldValue)) {
							stringNewValue = stringValue.replaceFirst(oldValue,
									newValue);
							FollowBeginStringFieldEditorPropertyChangeListener.this.preferenceStore
									.setValue(
											FollowBeginStringFieldEditorPropertyChangeListener.this.preferenceName,
											stringNewValue);
						}
					}
				}
			});
		} else {
			String stringValue = FollowBeginStringFieldEditorPropertyChangeListener.this.preferenceStore
					.getString(FollowBeginStringFieldEditorPropertyChangeListener.this.preferenceName);
			if (stringValue.startsWith(oldValue)) {
				String stringNewValue = stringValue.replaceFirst(oldValue,
						newValue);
				FollowBeginStringFieldEditorPropertyChangeListener.this.preferenceStore
						.setValue(
								FollowBeginStringFieldEditorPropertyChangeListener.this.preferenceName,
								stringNewValue);
			}
		}
	}

}
