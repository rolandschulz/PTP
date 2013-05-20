/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.ems.ui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.ems.core.EnvManagerConfigString;
import org.eclipse.ptp.ems.ui.EnvManagerConfigWidget;
import org.eclipse.ptp.ems.ui.IErrorListener;
import org.eclipse.ptp.internal.ems.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractWidget;
import org.eclipse.ptp.rm.jaxb.control.ui.IWidgetDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Instances of this class represent a selectable user interface element which, which selected, opens a dialog allowing the user
 * to configure an environment management system on a remote machine.
 * <p>
 * Typically, clients will use this control as follows.
 * <ol>
 * <li>Invoke the constructor, setting the {@link IRemoteConnection} that will provide access to the remote machine.
 * <li>Invoke {@link #setText(String)} to change the button text, if necessary.
 * <li>Invoke {@link #setConfiguration(String)} (if necessary) to set a default configuration.
 * <li>Invoke {@link #addModifyListener(ModifyListener)} to receive callbacks when the user changes the configuration.
 * <li>As needed, invoke {@link #getConfiguration()} (perhaps from a {@link ModifyListener}) to retrieve the modified configuration
 * if the user changes it.
 * </ol>
 * 
 * @author Jeff Overbey
 * 
 * @since 6.0
 */
public final class EnvManagerConfigButton extends AbstractWidget {

	private static IDialogSettings dialogSettings = new DialogSettings("EnvConfigurationDialog"); //$NON-NLS-1$

	private final IRemoteConnection remoteConnection;

	private EnvManagerConfigString configString;

	private Button button = null;
	private final List<ModifyListener> modifyListeners = new LinkedList<ModifyListener>();

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent {@link Composite} (non-<code>null</code>)
	 * @param wd
	 *            widget descriptor
	 * @since 2.0
	 */
	public EnvManagerConfigButton(Composite parent, IWidgetDescriptor wd) {

		super(parent, wd);

		String label = wd.getTitle();

		String tooltip = wd.getToolTipText();

		if (label != null) {
			Label buttonLabel = new Label(parent, SWT.RIGHT);
			buttonLabel.setText(label);
			GridData data = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false, 1, 1);
			buttonLabel.setLayoutData(data);
			if (tooltip != null) {
				buttonLabel.setToolTipText(tooltip);
			}
		}

		remoteConnection = wd.getRemoteConnection();

		configString = new EnvManagerConfigString();

		setLayout(new FillLayout());

		button = new Button(this, SWT.PUSH);
		button.setText(Messages.EnvManagerConfigButton_ConfigureButtonText);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final EnvConfigurationDialog dialog = new EnvConfigurationDialog(getShell());
				if (dialog.open() == Window.OK) {
					notifyListeners();
				}
			}
		});

		if (label != null) {
			setText(label);
		}
		setConfiguration(""); //$NON-NLS-1$

	}

	private class EnvConfigurationDialog extends Dialog {

		private EnvManagerConfigWidget envConfig;

		public EnvConfigurationDialog(final Shell shell) {
			super(shell);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(Messages.EnvManagerConfigButton_EnvConfigurationDialogTitle);
		}

		@Override
		protected IDialogSettings getDialogBoundsSettings() {
			return dialogSettings;
		}

		@Override
		protected Point getInitialSize() {
			return new Point(750, 500);
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			final Composite composite = (Composite) super.createDialogArea(parent);
			composite.setLayout(new GridLayout());

			envConfig = new EnvManagerConfigWidget(composite, SWT.NONE);
			envConfig.setLayoutData(new GridData(GridData.FILL_BOTH));

			final Label errorMessage = new Label(parent, SWT.LEFT | SWT.BOTTOM | SWT.BORDER);
			errorMessage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			errorMessage.setText(""); //$NON-NLS-1$
			errorMessage.setForeground(JFaceColors.getErrorText(getDisplay()));
			errorMessage.setBackground(JFaceColors.getErrorBackground(getDisplay()));
			envConfig.setErrorListener(new IErrorListener() {
				@Override
				public void errorRaised(String message) {
					errorMessage.setText(message);
					composite.layout(true, true);
				}

				@Override
				public void errorCleared() {
					errorMessage.setText(""); //$NON-NLS-1$
					composite.layout(true, true);
				}
			});

			envConfig.setUseEMSCheckbox(configString.isEnvMgmtEnabled());
			envConfig.setManualConfigCheckbox(configString.isManualConfigEnabled());
			envConfig.setManualConfigText(configString.getManualConfigText());
			envConfig.configurationChanged(null, remoteConnection, configString.getConfigElements());

			return composite;
		}

		@Override
		protected void okPressed() {
			envConfig.saveConfiguration(configString);
			super.okPressed();
		}
	}

	private void notifyListeners() {
		for (final ModifyListener listener : modifyListeners) {
			listener.modifyText(null);
		}
	}

	/**
	 * Sets this button's text.
	 * <p>
	 * This method sets the button label. The label may include the mnemonic character but must not contain line delimiters.
	 * </p>
	 * <p>
	 * Mnemonics are indicated by an '&amp;' that causes the next character to be the mnemonic. When the user presses a key sequence
	 * that matches the mnemonic, a selection event occurs. On most platforms, the mnemonic appears underlined but may be emphasized
	 * in a platform specific manner. The mnemonic indicator character '&amp;' can be escaped by doubling it in the string, causing
	 * a single '&amp;' to be displayed.
	 * </p>
	 * 
	 * @param text
	 *            the new text
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setText(String text) {
		button.setText(text);
		this.layout(true, true);
	}

	/**
	 * Adds the listener to the collection of listeners who will
	 * be notified when the receiver's text is modified, by sending
	 * it one of the messages defined in the <code>ModifyListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see ModifyListener
	 * @see #removeModifyListener
	 */
	public void addModifyListener(ModifyListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		modifyListeners.add(listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will
	 * be notified when the receiver's text is modified.
	 * 
	 * @param listener
	 *            the listener which should no longer be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see ModifyListener
	 * @see #addModifyListener
	 */
	public void removeModifyListener(ModifyListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		modifyListeners.remove(listener);
	}

	/**
	 * Sets the current configuration.
	 * 
	 * @param configuration
	 *            a String parseable as an {@link EnvManagerConfigString} (usually created by
	 *            {@link EnvManagerConfigString#toString()})
	 */
	public void setConfiguration(String configuration) {
		this.configString = new EnvManagerConfigString(configuration);
	}

	/** @return a String (parseable as an {@link EnvManagerConfigString}) describing the current configuration */
	public String getConfiguration() {
		return configString.toString();
	}

	@Override
	public void setToolTipText(String string) {
		if (button != null) {
			button.setToolTipText(string);
		}
	}

}