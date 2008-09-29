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
package org.eclipse.ptp.cell.preferences.core;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.cell.utils.searcher.SearchFailedException;
import org.eclipse.ptp.cell.utils.searcher.Searcher;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public abstract class StringFieldEditorPreferenceSearcher implements Searcher {

	protected StringFieldEditor stringFieldEditor;

	protected Text textControl;

	protected Display display;

	protected IPreferenceStore preferenceStore;

	protected String preferenceName;

	protected ListenerList propertyListeners;

	/**
	 * @param stringFieldEditor
	 *            the StringFieldEditor this searcher will change
	 * @param parent
	 *            the parent Composite of the StringFieldEditor being changed.
	 *            This is necessary due to refresh the user interface.
	 */
	public StringFieldEditorPreferenceSearcher(
			StringFieldEditor stringFieldEditor, Composite parent) {
		this.stringFieldEditor = stringFieldEditor;
		this.textControl = stringFieldEditor.getTextControl(parent);
		this.display = this.textControl.getDisplay();
		this.preferenceStore = stringFieldEditor.getPreferenceStore();
		this.preferenceName = stringFieldEditor.getPreferenceName();
		this.propertyListeners = new ListenerList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.cell.utils.searcher.Searcher#search()
	 */
	public void search() {
		try {
			fastSearch();
		} catch (SearchFailedException sfe) {
			if (wantLongSearch()) {
				longSearch();
			}
		}
	}

	/**
	 * Performs a fast search for the value being searched by this search
	 * engine.
	 * 
	 */
	protected abstract void fastSearch() throws SearchFailedException;

	/**
	 * Performs a more complete and hence longer search for the value being
	 * searched by this search engine.
	 * 
	 */
	protected abstract void longSearch();

	/**
	 * Ask if a long search in the file system will be performed
	 * 
	 * @return a value indicating if the long search will be performed
	 */
	protected boolean wantLongSearch() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		return MessageDialog.openConfirm(shell,
				SearcherMessages.longSearchConfirmationDialogTitle,
				SearcherMessages.longSearchConfirmationDialogMessage);
	}

	protected void setText(final String newValue) {
		if (this.display != null) {
			this.display.asyncExec(new Runnable() {
				public void run() {
					if (!StringFieldEditorPreferenceSearcher.this.textControl
							.isDisposed()) {
						String oldValue = StringFieldEditorPreferenceSearcher.this.stringFieldEditor
								.getStringValue();
						StringFieldEditorPreferenceSearcher.this.stringFieldEditor
								.setStringValue(newValue);
						firePropertyChangeEvent(
								StringFieldEditorPreferenceSearcher.this.preferenceName,
								oldValue, newValue);
					} else {
						String oldValue = StringFieldEditorPreferenceSearcher.this.preferenceStore
								.getString(StringFieldEditorPreferenceSearcher.this.preferenceName);
						StringFieldEditorPreferenceSearcher.this.preferenceStore
								.setValue(
										StringFieldEditorPreferenceSearcher.this.preferenceName,
										newValue);
						firePropertyChangeEvent(
								StringFieldEditorPreferenceSearcher.this.preferenceName,
								oldValue, newValue);
					}
				}
			});
		} else {
			String oldValue = this.preferenceStore
					.getString(this.preferenceName);
			this.preferenceStore.setValue(this.preferenceName, newValue);
			firePropertyChangeEvent(this.preferenceName, oldValue, newValue);
		}

	}

	/**
	 * Adds a property change listener to this searcher object. Has no affect if
	 * the identical listener is already registered.
	 * 
	 * @param listener
	 *            a property change listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		this.propertyListeners.add(listener);
	}

	/**
	 * Removes the given listener from this searcher object. Has no affect if
	 * the listener is not registered.
	 * 
	 * @param listener
	 *            a property change listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		this.propertyListeners.remove(listener);
	}

	/**
	 * Fires a property change event corresponding to a change to the current
	 * value of the property searched by this search engine.
	 * 
	 * @param name
	 *            the name of the property, to be used as the property in the
	 *            event object
	 * @param oldValue
	 *            the old value, or <code>null</code> if not known or not
	 *            relevant
	 * @param newValue
	 *            the new value, or <code>null</code> if not known or not
	 *            relevant
	 */
	protected void firePropertyChangeEvent(String name, Object oldValue,
			Object newValue) {
		if (name == null)
			throw new IllegalArgumentException();
		Object[] changeListeners = this.propertyListeners.getListeners();
		// Do we even need to fire an event?
		if (changeListeners.length == 0)
			return;
		final PropertyChangeEvent pe = new PropertyChangeEvent(this, name,
				oldValue, newValue);
		for (int i = 0; i < changeListeners.length; i++) {
			final IPropertyChangeListener listener = (IPropertyChangeListener) changeListeners[i];
			ISafeRunnable job = new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// already being logged in Platform#run()
				}

				public void run() throws Exception {
					listener.propertyChange(pe);
				}
			};
			SafeRunner.run(job);
		}
	}

	protected void showInfoMessage(final String title, final String message) {
		if (this.display != null) {
			this.display.asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(StringFieldEditorPreferenceSearcher.this.display.getActiveShell(), title, message);
				}
			});
			return;
		}
		MessageDialog.openInformation(null, title, message);
	}
	
}
