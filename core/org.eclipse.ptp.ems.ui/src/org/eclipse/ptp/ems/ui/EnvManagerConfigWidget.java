/*******************************************************************************
 * Copyright (c) 2012, 2013 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *    Chris Navarro (Illinois) - fix for Bug 394121
 *******************************************************************************/
package org.eclipse.ptp.ems.ui;

import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ptp.ems.core.EnvManagerConfigString;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.ems.core.IEnvManagerConfig;
import org.eclipse.ptp.internal.ems.ui.EnvManagerChecklist;
import org.eclipse.ptp.internal.ems.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Instances of this class represent a user interface element which provides the following:
 * <ul>
 * <li>a checkbox: &quot;Use an environment management system to customize the remote build environment&quot;;
 * <li>a checkbox: &quot;Manually specify environment configuration commands&quot;; and
 * <li>either a checklist, a multi-line text box, or a blank area, depending on the state of the checkboxes above.
 * </ul>
 * <p>
 * Typically, clients will use this control as follows.
 * <ol>
 * <li>Invoke the constructor, setting the {@link IRemoteConnection} that will provide access to the remote machine.
 * <li>Invoke {@link #setErrorListener(IErrorListener)} in order to display error messages to the user.
 * <li>As needed, invoke {@link #configurationChanged(URI, IRemoteConnection, List)} or #setCheckbox(boolean)} to change the
 * appearance of the control.
 * <li>Finally, invoke {@link #isUseEMSChecked()}, {@link #isManualConfigChecked()}, {@link #getSelectedElements()}, and
 * {@link #getConnectionName()} to retrieve the contents of the control, or use {@link #saveConfiguration(IEnvManagerConfig)} to
 * persist all of its settings in a single operation.
 * </ol>
 * 
 * @author Jeff Overbey
 * 
 * @since 6.0
 */
public final class EnvManagerConfigWidget extends Composite {

	private Button useEMSCheckbox;
	private Button manualConfigCheckbox;

	private Composite stack;
	private StackLayout stackLayout;

	private Label noEnvConfigLabel;
	private Text envConfigTextbox;
	private EnvManagerChecklist envConfigChecklist;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent {@link Composite} (non-<code>null</code>)
	 * @param style
	 *            the style of widget to construct
	 * @since 2.0
	 */
	public EnvManagerConfigWidget(Composite parent, int style) {
		super(parent, style);

		GridLayout g = new GridLayout(1, false);
		g.marginHeight = 0;
		g.marginWidth = 0;
		setLayout(g);

		createCheckbox(this);
		createManualOverrideCheckbox(this);
		createStack(this);
		createNoEnvConfigLabel();
		createEnvConfigTextbox();
		createEnvConfigChecklist();

		setTopControl();
	}

	/**
	 * Set the remote connection to use when querying for the remote environment
	 * 
	 * @param connection
	 *            {@link IRemoteConnection} used to access files and execute shell commands on the remote machine (non-
	 *            <code>null</code>)
	 * @since 2.0
	 */
	public void setConnection(IRemoteConnection connection) {
		if (envConfigChecklist != null) {
			envConfigChecklist.setConnection(connection);
		}
	}

	private void createCheckbox(Composite composite) {
		useEMSCheckbox = new Button(composite, SWT.CHECK | SWT.WRAP);
		useEMSCheckbox.setText(Messages.EnvConfigurationControl_UseEMS);
		useEMSCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		useEMSCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSelectLoadModules();
			}
		});
	}

	private void onSelectLoadModules() {
		final boolean isChecked = useEMSCheckbox.getSelection();

		manualConfigCheckbox.setVisible(isChecked);
		envConfigTextbox.setEnabled(isChecked);
		envConfigChecklist.setEnabled(isChecked);

		setTopControl();
	}

	private void setTopControl() {
		if (useEMSCheckbox.getSelection()) {
			if (manualConfigCheckbox.getSelection()) {
				if (stackLayout.topControl == envConfigChecklist && isChecklistEnabled()) {
					setTextFromChecklist();
				}
				stackLayout.topControl = envConfigTextbox;
			} else {
				stackLayout.topControl = envConfigChecklist;
				stack.setSize(envConfigChecklist.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		} else {
			stackLayout.topControl = noEnvConfigLabel;
		}
		stack.getParent().layout();
		stack.layout();
	}

	private void setTextFromChecklist() {
		final IEnvManager envManager = envConfigChecklist.getEnvManager();
		final EnvManagerConfigString config = new EnvManagerConfigString();
		config.setEnvMgmtEnabled(true);
		config.setManualConfig(false);
		config.setConnectionName(getConnectionName());
		config.setConfigElements(getSelectedElements());
		final String bashCommands = envManager.getBashConcatenation("\n", false, config, null); //$NON-NLS-1$
		envConfigTextbox.setText(bashCommands);
	}

	private void createManualOverrideCheckbox(Composite composite) {
		manualConfigCheckbox = new Button(composite, SWT.CHECK);
		manualConfigCheckbox.setText(Messages.EnvConfigurationControl_ManualOverride);
		manualConfigCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		manualConfigCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSelectManualConfig();
			}
		});
	}

	private void onSelectManualConfig() {
		setTopControl();
	}

	private void createStack(Composite composite) {
		stack = new Composite(composite, SWT.NONE);
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);
	}

	private void createNoEnvConfigLabel() {
		noEnvConfigLabel = new Label(stack, SWT.NONE);
		noEnvConfigLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		noEnvConfigLabel.setText(""); //$NON-NLS-1$
	}

	private void createEnvConfigTextbox() {
		envConfigTextbox = new Text(stack, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SHADOW_IN);
		envConfigTextbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		envConfigTextbox.setText(""); //$NON-NLS-1$
		allowEnterAndTabInTextbox(envConfigTextbox);
		envConfigTextbox.setFont(JFaceResources.getTextFont());
	}

	/**
	 * Allows the Enter and Tab keys to insert text into the text box, even if it's in a dialog box where these keys would usually
	 * press the default button and traverse to the next control, respectively.
	 * <p>
	 * The user must press Ctrl+Enter or Ctrl+Tab (on Mac OS X, Command+Enter or Command+Tab) to traverse dialog controls.
	 */
	private static void allowEnterAndTabInTextbox(final Text envConfigTextbox) {
		envConfigTextbox.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				final boolean modifierPressed = (e.stateMask & SWT.MOD1) != 0;
				if (!modifierPressed) {
					switch (e.detail) {
					case SWT.TRAVERSE_RETURN:
						envConfigTextbox.insert("\n"); //$NON-NLS-1$
						e.doit = false;
						break;
					case SWT.TRAVERSE_TAB_NEXT:
						envConfigTextbox.insert("\t"); //$NON-NLS-1$
						e.doit = false;
						break;
					case SWT.TRAVERSE_TAB_PREVIOUS:
						e.doit = false;
						break;
					}
				}
			}
		});
	}

	private void createEnvConfigChecklist() {
		envConfigChecklist = new EnvManagerChecklist(stack, SWT.NONE);
		envConfigChecklist.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * Sets the (unique) {@link IErrorListener} which will be used to display error messages to the user.
	 * 
	 * @param listener
	 *            {@link IErrorListener} used to display error messages to the user, or <code>null</code>
	 */
	public void setErrorListener(IErrorListener listener) {
		envConfigChecklist.setErrorListener(listener);
	}

	/**
	 * Re-populates this control to reflect a change in the project's remote location or a change in the selected items.
	 * <p>
	 * If the given URI is <code>null</code>, or if it differs from the URI supplied in the previous call to
	 * {@link #configurationChanged(URI, IRemoteConnection, List)}, then the elements in the checklist are re-loaded from the remote
	 * machine (using {@link IEnvManager#determineAvailableElements(IProgressMonitor)}). Otherwise, the items present in the
	 * checklist remain the same, but the checked/unchecked state of the items may be changed.
	 * 
	 * @param uri
	 *            a URI representing the remote location of this project (possibly <code>null</code>)
	 * @param remoteConnection
	 *            {@link IRemoteConnection} providing access to the remote machine (non-<code>null</code>)
	 * @param selectedItems
	 *            the items which should be selected in the checklist (non-<code>null</code>)
	 * @since 2.0
	 */
	public void configurationChanged(URI uri, IRemoteConnection remoteConnection,
			List<String> selectedItems) {
		envConfigChecklist.reset(uri, remoteConnection, selectedItems);
		setTopControl();
	}

	/**
	 * @return true iff the &quot;Use an environment management system to customize the remote build environment&quot; checkbox is
	 *         checked
	 * 
	 * @see #setUseEMSCheckbox(boolean)
	 */
	public boolean isUseEMSChecked() {
		return useEMSCheckbox.getSelection();
	}

	/**
	 * @return true iff the &quot;Manually specify environment configuration commands&quot; checkbox is checked
	 */
	public boolean isManualConfigChecked() {
		return manualConfigCheckbox.getSelection();
	}

	/**
	 * @return the contents of the text box which allows the user to enter a custom sequence of environment management commands.
	 *         This set may be empty but is never <code>null</code>.
	 */
	public String getManualConfigText() {
		return envConfigTextbox.getText();
	}

	/**
	 * Sets the contents of the text box which allows the user to enter a custom sequence of environment management commands.
	 * 
	 * @param text
	 *            non-<code>null</code>.
	 */
	public void setManualConfigText(String text) {
		envConfigTextbox.setText(text);
	}

	/**
	 * Get the text of the elements which are checked in the checklist. This list may be empty but is never <code>null</code>.
	 * It is, in theory, a subset of the strings returned by {@link IEnvManager#determineAvailableElements(IProgressMonitor)}.
	 * 
	 * @return list of checked elements
	 * 
	 * @since 2.0
	 */
	public List<String> getSelectedElements() {
		return envConfigChecklist.getSelectedElements();
	}

	/**
	 * Test if the checklist is properly configured. The contents of the checklist is only valid when this is true.
	 * 
	 * TODO: Should probably be public, but this would require an API change
	 * 
	 * @return true if checklist is valid
	 */
	private boolean isValid() {
		return envConfigChecklist.isValid();
	}

	/**
	 * @return true iff the checklist is visible and enabled for modification by the user
	 */
	private boolean isChecklistEnabled() {
		return envConfigChecklist.isChecklistEnabled();
	}

	/**
	 * Sets the state of the &quot;Use an environment management system to customize the remote build environment&quot; checkbox.
	 * 
	 * @param checked
	 *            true iff the &quot;Use an environment management system to customize the remote build environment&quot; checkbox
	 *            should be checked
	 * 
	 * @see #isUseEMSChecked()
	 */
	public void setUseEMSCheckbox(boolean checked) {
		useEMSCheckbox.setSelection(checked);
		onSelectLoadModules();
	}

	/**
	 * Sets the state of the &quot;Manually specify environment configuration commands&quot; checkbox.
	 * 
	 * @param checked
	 *            true iff the &quot;Manually specify environment configuration commands&quot; checkbox should be checked
	 * 
	 * @see #isManualConfigChecked()
	 */
	public void setManualConfigCheckbox(boolean checked) {
		manualConfigCheckbox.setSelection(checked);
		onSelectManualConfig();
	}

	/**
	 * @return the name of the remote environment (possibly <code>null</code>). The remote environment is determined by the
	 *         {@link IRemoteConnection} provided to the constructor or {@link #configurationChanged(URI, IRemoteConnection, List)},
	 *         whichever was
	 *         invoked most recently.
	 */
	public String getConnectionName() {
		return envConfigChecklist.getConnectionName();
	}

	/**
	 * Stores the current state of this {@link EnvManagerConfigWidget} in the given configuration object.
	 * 
	 * @param config
	 *            non-<code>null</code>
	 */
	public void saveConfiguration(IEnvManagerConfig config) {
		config.setEnvMgmtEnabled(isUseEMSChecked());
		if (isUseEMSChecked()) {
			config.setManualConfig(isManualConfigChecked());
			if (isManualConfigChecked()) {
				config.setManualConfigText(getManualConfigText());
			} else {
				// Only overwrite the module list if the checklist was available for editing;
				// this allows old values to be resurrected if environment management support was temporarily disabled
				if (isChecklistEnabled() && isValid()) {
					config.setConnectionName(getConnectionName());
					config.setConfigElements(getSelectedElements());
				}
			}
		}
	}
}