/**
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.generichost.core;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remotetools.core.IAuthInfo;
import org.eclipse.ptp.remotetools.environment.control.ITargetConfig;
import org.eclipse.ptp.remotetools.environment.generichost.messages.Messages;
import org.eclipse.ptp.remotetools.environment.generichost.ui.KeyboardInteractiveDialog;
import org.eclipse.ptp.remotetools.environment.generichost.ui.UserValidationDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Provides feedback to user for connection authentication
 * 
 * @since 1.4
 */
public class AuthInfo implements IAuthInfo {
	private class PromptSecret {
		private String fResult = null;
		private boolean fSaveResult = false;

		public void prompt(final String message) {
			final String finUser = fConfig.getLoginUsername();
			getDisplay().syncExec(new Runnable() {
				public void run() {
					UserValidationDialog uvd = new UserValidationDialog(null, finUser, message);
					uvd.setUsernameMutable(false);
					if (uvd.open() == Window.OK) {
						fResult = uvd.getPassword();
						fSaveResult = uvd.isSavePassword();
					}
				}
			});
		}

		public String getResult() {
			return fResult;
		}

		public boolean isSaveResult() {
			return fSaveResult;
		}
	}

	private ITargetConfig fConfig = null;
	/*
	 * Password or passphrase obtained from user prompt (either
	 * KeyboardInteractiveDialog or UserValidationDialog). These are only used
	 * once, then we revert back to using the password or passphrase from the
	 * configuration.
	 */
	private String fPassword = null;
	private String fPassphrase = null;

	public AuthInfo(ITargetConfig config) {
		fConfig = config;
	}

	public String getPassphrase() {
		if (fPassphrase != null) {
			String res = fPassphrase;
			fPassphrase = null;
			return res;
		}
		return fConfig.getKeyPassphrase();
	}

	public String getPassword() {
		if (fPassword != null) {
			String res = fPassword;
			fPassword = null;
			return res;
		}
		return fConfig.getLoginPassword();
	}

	public String[] promptKeyboardInteractive(final String destination, final String name, final String instruction,
			final String[] prompt, final boolean[] echo) {
		if (prompt.length == 0) {
			// No need to prompt, just return an empty String array
			return new String[0];
		}
		try {
			final String[][] finResult = new String[1][];
			getDisplay().syncExec(new Runnable() {
				public void run() {
					KeyboardInteractiveDialog dialog = new KeyboardInteractiveDialog(null, destination, name, instruction, prompt,
							echo);
					dialog.open();
					finResult[0] = dialog.getResult();
				}
			});
			String[] result = finResult[0];
			if (result == null)
				return null; // cancelled
			if (result.length == 1 && prompt.length == 1 && prompt[0].trim().equalsIgnoreCase("password:")) { //$NON-NLS-1$
				fPassword = result[0];
			}
			return result;
		} catch (OperationCanceledException e) {
			return null;
		}
	}

	public boolean promptPassphrase(String message) {
		PromptSecret ps = new PromptSecret();
		ps.prompt(message);
		fPassphrase = ps.getResult();
		if (fPassphrase == null) {
			return false;
		}
		if (ps.isSaveResult()) {
			fConfig.setKeyPassphrase(ps.getResult());
		}
		return true;
	}

	public boolean promptPassword(String message) {
		PromptSecret ps = new PromptSecret();
		ps.prompt(message);
		fPassword = ps.getResult();
		if (fPassword == null) {
			return false;
		}
		if (ps.isSaveResult()) {
			fConfig.setLoginPassword(ps.getResult());
		}
		return true;
	}

	public boolean promptYesNo(final String str) {
		// need to switch to UI thread for prompting
		final boolean[] retval = new boolean[1];
		getDisplay().syncExec(new Runnable() {
			public void run() {
				retval[0] = MessageDialog.openQuestion(null, Messages.Environment_Warning, str);
			}
		});
		return retval[0];
	}

	public void setPassphrase(String passphrase) {
		fConfig.setKeyPassphrase(passphrase);
	}

	public void setPassword(String password) {
		fConfig.setLoginPassword(password);
	}

	public void setUsePassword(boolean usePassword) {
		fConfig.setPasswordAuth(usePassword);
	}

	public void showMessage(final String message) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(null, Messages.Environment_Info, message);
			}
		});
	}

	public String getKeyPath() {
		return fConfig.getKeyPath();
	}

	public String getUsername() {
		return fConfig.getLoginUsername();
	}

	public boolean isPasswordAuth() {
		return fConfig.isPasswordAuth();
	}

	public void setKeyPath(String keyPath) {
		fConfig.setKeyPath(keyPath);
	}

	public void setUsername(String username) {
		fConfig.setLoginUsername(username);
	}

	private static Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}
}
