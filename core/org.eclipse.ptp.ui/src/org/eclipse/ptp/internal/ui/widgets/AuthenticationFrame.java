/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.internal.ui.widgets;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.ui.PTPUIPlugin;
import org.eclipse.ptp.internal.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

/**
 * Frame specialized for use in environments that must retrieve information on
 * how to connect to a remote host from the user.
 * 
 * @author Richard Maciel
 * 
 */
public final class AuthenticationFrame extends Frame {

	/**
	 * Implements a {@link SelectionListener} that receives events from the {@link Button} (SWT.OPTION style) children controls
	 * convert them to {@link ModifyEvent} and forwards them to this control listeners. Also, it
	 * updates all widgets that depend on the controls that generate the event.
	 * 
	 * @author Richard Maciel
	 * 
	 */
	private class OptionSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent event) {
			updateEnabledWidgets();

			// Copy the SelectionEvent to the ModifyEvent
			Event newEvent = new Event();
			newEvent.data = event.data;
			newEvent.display = event.display;
			newEvent.time = event.time;
			newEvent.widget = event.widget;
			ModifyEvent mevent = new ModifyEvent(newEvent);

			forwardEventToExternalListeners(mevent);
		}
	}

	/**
	 * Implements a {@link SelectionListener} that receives events from the {@link Combo} children controls convert them to
	 * {@link ModifyEvent} and
	 * forwards them to this control listeners.
	 * 
	 * @author Richard Maciel
	 * 
	 */
	private class ComboSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent event) {
			// Copy the SelectionEvent to the ModifyEvent
			Event newEvent = new Event();
			newEvent.data = event.data;
			newEvent.display = event.display;
			newEvent.time = event.time;
			newEvent.widget = event.widget;
			ModifyEvent mevent = new ModifyEvent(newEvent);

			forwardEventToExternalListeners(mevent);
		}
	}

	// Top controls
	private Button localhost;
	private Button remotehost;
	private TextGroup hostAddressTextGroup;
	private TextGroup hostPortTextGroup;
	private Button passwordAuthButton;
	private Button publicKeyAuthButton;
	private TextGroup usernameTextGroup;
	private TextGroup passwordTextGroup;
	private FileGroup privateKeyPathGroup;
	private TextGroup passphraseTextGroup;

	// Bottom controls
	private TextGroup timeoutTextGroup;
	private ComboGroup cipherTypeGroup;

	// List of all listeners
	private final Set<ModifyListener> modifyListeners = new HashSet<ModifyListener>();

	public AuthenticationFrame(Composite parent, AuthenticationFrameMold mold) {
		super(parent, FrameMold.HAS_EXPAND | FrameMold.HAS_FRAME
				| ((mold.bitmask & AuthenticationFrameMold.HAS_DESCRIPTION) != 0 ? FrameMold.HAS_DESCRIPTION : 0), 2);
		setTitle(mold.title);
		super.setExpandButtonLabel(mold.labelShowAdvancedOptions);
		super.setShrinkButtonLabel(mold.labelHideAdvancedOptions);
		if (mold.description != null) {
			setDescription(mold.description);
		}

		createTopControls(mold);
		createBottomControls(mold);
		registerListeners();
		updateEnabledWidgets();
	}

	/**
	 * Register listeners for all user-interactive controls
	 * 
	 */
	private void registerListeners() {
		// Get controls from both top and bottom composites.
		Control[] controls = { this.localhost, this.remotehost, this.hostAddressTextGroup, this.hostPortTextGroup,
				this.passwordAuthButton, this.publicKeyAuthButton, this.usernameTextGroup, this.passwordTextGroup,
				this.privateKeyPathGroup, this.passphraseTextGroup, this.timeoutTextGroup, this.cipherTypeGroup };

		// Add listeners to all controls in the array
		for (Control control : controls) {
			if (control instanceof TextGroup) {
				((TextGroup) control).getText().addModifyListener(new TextModifyListener());
			} else if (control instanceof Button) {
				Button b = (Button) control;
				b.addSelectionListener(new OptionSelectionListener());
			} else if (control instanceof ComboGroup) {
				((ComboGroup) control).getCombo().addSelectionListener(new ComboSelectionListener());
			}
		}
	}

	/**
	 * Create controls for the top composite.
	 * 
	 * @param mold
	 * 
	 */
	private void createTopControls(AuthenticationFrameMold mold) {
		Composite topUserReservedComposite = this.getTopUserReservedComposite();

		FrameMold lmold = new FrameMold(2);
		Frame line = new Frame(topUserReservedComposite, lmold);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		line.setLayoutData(gridData);

		if ((mold.getBitmask() & AuthenticationFrameMold.SHOW_HOST_TYPE_RADIO_BUTTON) != 0) {
			// Radio buttons to select if target is localhost or a remote host.
			this.localhost = new Button(line, SWT.RADIO);
			this.localhost.setText(mold.labelLocalhost);
			this.remotehost = new Button(line, SWT.RADIO);
			this.remotehost.setText(mold.labelRemoteHost);

			line = new Frame(topUserReservedComposite, lmold);
			gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
			gridData.horizontalSpan = 2;
			line.setLayoutData(gridData);
		}

		// Will reuse this mold for all TextGroup controls.
		TextMold tmold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE,
				Messages.AuthenticationFrame_Host);

		// Host field
		tmold.setLabel(mold.labelHostAddress);
		hostAddressTextGroup = new TextGroup(line, tmold);

		// Username field
		tmold.addBitmask(TextMold.GRID_DATA_GRAB_EXCESS_SPACE);
		tmold.removeBitmask(TextMold.WIDTH_PROPORTIONAL_NUM_CHARS);
		tmold.unsetTextFieldWidth();
		tmold.setLabel(mold.labelUserName);
		usernameTextGroup = new TextGroup(topUserReservedComposite, tmold);

		// User option box
		passwordAuthButton = new Button(topUserReservedComposite, SWT.RADIO);
		passwordAuthButton.setText(mold.labelIsPasswordBased);
		GridData gdauthkind0 = new GridData();
		gdauthkind0.horizontalSpan = 2;
		passwordAuthButton.setLayoutData(gdauthkind0);

		// Password field
		tmold.setLabel(mold.labelPassword);
		tmold.addBitmask(TextMold.PASSWD_FIELD);
		passwordTextGroup = new TextGroup(topUserReservedComposite, tmold);

		// Key option box
		publicKeyAuthButton = new Button(topUserReservedComposite, SWT.RADIO);
		publicKeyAuthButton.setText(mold.labelIsPublicKeyBased);
		GridData gdauthkind1 = new GridData();
		gdauthkind1.horizontalSpan = 2;
		publicKeyAuthButton.setLayoutData(gdauthkind1);

		// Key file selection
		FileMold fsmold = new FileMold(FileMold.GRID_DATA_ALIGNMENT_FILL | FileMold.GRID_DATA_SPAN, mold.labelPrivateKeyPath,
				mold.labelPrivateKeyPathTitle, mold.labelPublicKeyPathButton);
		privateKeyPathGroup = new FileGroup(topUserReservedComposite, fsmold);

		// Passphrase field
		tmold.setLabel(mold.labelPassphrase);
		tmold.addBitmask(TextMold.GRID_DATA_SPAN);
		passphraseTextGroup = new TextGroup(topUserReservedComposite, tmold);

		passwordAuthButton.setSelection(true);
		publicKeyAuthButton.setSelection(false);

		// This solves the taborder issue.
		if ((mold.getBitmask() & AuthenticationFrameMold.SHOW_HOST_TYPE_RADIO_BUTTON) != 0) {
			topUserReservedComposite.setTabList(new Control[] { localhost.getParent(), hostAddressTextGroup.getParent(),
					usernameTextGroup, passwordAuthButton, passwordTextGroup, publicKeyAuthButton, privateKeyPathGroup,
					passphraseTextGroup });
		} else {
			topUserReservedComposite.setTabList(new Control[] { hostAddressTextGroup.getParent(), usernameTextGroup,
					passwordAuthButton, passwordTextGroup, publicKeyAuthButton, privateKeyPathGroup, passphraseTextGroup });
		}
	}

	/**
	 * Create controls for the bottom (hideable) composite
	 * 
	 * @param mold
	 * 
	 */
	private void createBottomControls(AuthenticationFrameMold mold) {
		Composite bottomUserReservedComposite = this.getBottomUserReservedComposite();
		// Port field
		TextMold tmold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE, mold.labelHostPort);
		tmold.removeBitmask(TextMold.GRID_DATA_GRAB_EXCESS_SPACE);
		tmold.addBitmask(TextMold.WIDTH_PROPORTIONAL_NUM_CHARS);
		tmold.setTextFieldWidth(5);
		hostPortTextGroup = new TextGroup(bottomUserReservedComposite, tmold);

		// Timeout field
		tmold = new TextMold(TextMold.WIDTH_PROPORTIONAL_NUM_CHARS, mold.labelTimeout, 5);
		timeoutTextGroup = new TextGroup(bottomUserReservedComposite, tmold);
		ComboMold cmold = new ComboMold(ComboMold.GRID_DATA_SPAN | ComboMold.GRID_DATA_ALIGNMENT_FILL
				| ComboMold.GRID_DATA_GRAB_EXCESS_SPACE, mold.labelCipherType); // empty
																				// combo
		cipherTypeGroup = new ComboGroup(bottomUserReservedComposite, cmold);
	}

	/**
	 * Add a modify listener for this control. The modify listener will listen
	 * to all textbox and also listen to the option buttons, converting their
	 * events {@link SelectionEvent} to {@link ModifyEvent} when necessary.
	 * 
	 * @param listener
	 */
	public void addModifyListener(ModifyListener listener) {
		modifyListeners.add(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		modifyListeners.remove(listener);
	}

	/**
	 * When a {@link ModifyEvent} arrives, forward it to all listeners of this
	 * control.
	 * 
	 * @param mevent
	 */
	private void forwardEventToExternalListeners(ModifyEvent mevent) {
		for (ModifyListener listener : modifyListeners) {
			listener.modifyText(mevent);
		}
	}

	/**
	 * Implements a {@link ModifyListener} that receives events from the {@link Text} children controls and forwards them to this
	 * control
	 * listeners'.
	 * 
	 * @author Richard Maciel
	 * 
	 */
	class TextModifyListener implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent arg0) {
			forwardEventToExternalListeners(arg0);
		}
	}

	/**
	 * Use this method to enable/disable options based on status of some
	 * control(s) of the authentication frame.
	 * 
	 */
	private void updateEnabledWidgets() {
		if (isLocalhostSelected()) {
			this.hostAddressTextGroup.getText().setEnabled(false);
		} else {
			this.hostAddressTextGroup.getText().setEnabled(true);
		}
		if (isPasswdBased()) {
			// usernameTextGroup.setEnabled(true);
			passwordTextGroup.setEnabled(true);
			privateKeyPathGroup.setEnabled(false);
			passphraseTextGroup.setEnabled(false);
		} else {
			// usernameTextGroup.setEnabled(false);
			passwordTextGroup.setEnabled(false);
			privateKeyPathGroup.setEnabled(true);
			passphraseTextGroup.setEnabled(true);
		}
	}

	/**
	 * Validate all frame fields. Throw a {@link CoreException} if field is
	 * invalid.
	 * 
	 * @throws CoreException
	 */
	public void validateFields() throws CoreException {
		String pluginID = PTPUIPlugin.getDefault().getBundle().getSymbolicName();

		if ((!isLocalhostSelected()) && (hostAddressTextGroup.getText().getText().trim().length() == 0)) {
			throw new CoreException(new Status(IStatus.ERROR, pluginID, 0, Messages.AuthenticationFrame_EmptyHostAddressException,
					null));
		}
		if (hostPortTextGroup.getText().getText().trim().length() == 0) {
			throw new CoreException(new Status(IStatus.ERROR, pluginID, 0, Messages.AuthenticationFrame_EmptyHostPortException,
					null));
		}
		if (usernameTextGroup.getText().getText().trim().length() == 0) {
			throw new CoreException(new Status(IStatus.ERROR, pluginID, 0, Messages.AuthenticationFrame_EmptyUsernameException,
					null));
		}
		if (!isPasswdBased()) {
			if (privateKeyPathGroup.getText().getText().trim().length() == 0) {
				throw new CoreException(new Status(IStatus.ERROR, pluginID, 0,
						Messages.AuthenticationFrame_EmptyPrivateKeyPathException, null));
			}
			File path = new File(privateKeyPathGroup.getText().getText());
			if (!path.exists()) {
				throw new CoreException(new Status(IStatus.ERROR, pluginID, 0,
						Messages.AuthenticationFrame_FileDoesNotExistException, null));
			}
			if (!path.isFile()) {
				throw new CoreException(new Status(IStatus.ERROR, pluginID, 0, Messages.AuthenticationFrame_PathIsNotFileExcpetion,
						null));
			}
			if (!path.canRead()) {
				throw new CoreException(new Status(IStatus.ERROR, pluginID, 0,
						Messages.AuthenticationFrame_FileNotReadableException, null));
			}
		}
		try {
			Integer.parseInt(timeoutTextGroup.getText().getText());
		} catch (NumberFormatException ne) {
			throw new CoreException(new Status(IStatus.ERROR, pluginID, 0, Messages.AuthenticationFrame_TimeoutException, null));
		}
		try {
			Integer.parseInt(hostPortTextGroup.getText().getText());
		} catch (NumberFormatException ne) {
			throw new CoreException(new Status(IStatus.ERROR, pluginID, 0, Messages.AuthenticationFrame_PortNumberException, null));
		}
		if (cipherTypeGroup.getSelectedItem() == null || cipherTypeGroup.getSelectedItem().getId().equals("")) { //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, pluginID, 0, Messages.AuthenticationFrame_InvalidCipherTypeException,
					null));
		}
	}

	public TextGroup getHostAddrTextGroup() {
		return hostAddressTextGroup;
	}

	public TextGroup getHostPortTextGroup() {
		return hostPortTextGroup;
	}

	public TextGroup getPassphraseTextGroup() {
		return passphraseTextGroup;
	}

	public TextGroup getPasswordTextGroup() {
		return passwordTextGroup;
	}

	public TextGroup getTimeoutTextGroup() {
		return timeoutTextGroup;
	}

	public TextGroup getUsernameTextGroup() {
		return usernameTextGroup;
	}

	public FileGroup getPublicKeyPathGroup() {
		return privateKeyPathGroup;
	}

	public boolean isPasswdBased() {
		return passwordAuthButton.getSelection();
	}

	public boolean isLocalhostSelected() {
		if (this.localhost != null) {
			return this.localhost.getSelection();
		}
		return false;
	}

	public Button[] getAuthKindSelectionButtons() {
		Button[] buttons = new Button[2];
		buttons[0] = passwordAuthButton;
		buttons[1] = publicKeyAuthButton;
		return buttons;
	}

	public String getHostAddress() {
		return hostAddressTextGroup.getString().trim();
	}

	public void setHostAddress(String s) {
		hostAddressTextGroup.setString(s);
	}

	public int getHostPort() {
		try {
			return Integer.parseInt(hostPortTextGroup.getString().trim());
		} catch (NumberFormatException e) {
			return 22;
		}
	}

	public void setHostPort(int port) {
		hostPortTextGroup.setString(Integer.toString(port));
	}

	public boolean isPasswordBased() {
		return passwordAuthButton.getSelection();
	}

	public void setPasswordBased(boolean flag) {
		passwordAuthButton.setSelection(flag);
		publicKeyAuthButton.setSelection(!flag);
		updateEnabledWidgets();
	}

	public void setLocalhostSelected(boolean selection) {
		if ((this.localhost != null) && (this.remotehost != null)) {
			this.localhost.setSelection(selection);
			this.remotehost.setSelection(!selection);
			updateEnabledWidgets();
		}
	}

	public boolean isPublicKeyBased() {
		return publicKeyAuthButton.getSelection();
	}

	public void setPublicKeyBased(boolean flag) {
		passwordAuthButton.setSelection(!flag);
		publicKeyAuthButton.setSelection(flag);
		updateEnabledWidgets();
	}

	public String getPassword() {
		return passwordTextGroup.getString().trim();
	}

	public void setPassword(String s) {
		passwordTextGroup.setString(s);
	}

	public String getUserName() {
		return usernameTextGroup.getString().trim();
	}

	public void setUserName(String s) {
		usernameTextGroup.setString(s);
	}

	public String getPassphrase() {
		return passphraseTextGroup.getString().trim();
	}

	public void setPassphrase(String s) {
		passphraseTextGroup.setString(s);
	}

	public int getTimeout() {
		try {
			return Integer.parseInt(timeoutTextGroup.getString().trim());
		} catch (NumberFormatException e) {
			return 5000;
		}
	}

	public void setTimeout(int port) {
		timeoutTextGroup.setString(Integer.toString(port));
	}

	public String getPublicKeyPath() {
		return privateKeyPathGroup.getString().trim();
	}

	public void setPublicKeyPath(String s) {
		privateKeyPathGroup.setString(s);
	}

	public ComboGroupItem getSelectedCipherType() {
		return cipherTypeGroup.getSelectedItem();
	}

	public ComboGroupItem getCipherType(String id) {
		return cipherTypeGroup.getItemUsingID(id);
	}

	public ComboGroupItem getCipherType(int index) {
		return cipherTypeGroup.getItemUsingIndex(index);
	}

	public void addCipherType(ComboGroupItem citem) {
		cipherTypeGroup.add(citem);
	}

	public ComboGroup getCipherTypeGroup() {
		return cipherTypeGroup;
	}

}
