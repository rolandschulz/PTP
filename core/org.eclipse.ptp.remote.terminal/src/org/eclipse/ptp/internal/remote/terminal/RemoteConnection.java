/*******************************************************************************
 * Copyright (c) 2012 IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.internal.remote.terminal.messages.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

@SuppressWarnings("restriction")
class RemoteConnection extends Thread {
	private static int fgNo;

	protected static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	private final ITerminalControl fControl;
	private final RemoteConnector fConn;
	private IRemoteConnection fRemoteConnection;
	private IRemoteProcess fProcess;
	private IProject fProject = null;

	protected RemoteConnection(RemoteConnector conn, ITerminalControl control) {
		super("RemoteConnection-" + fgNo++); //$NON-NLS-1$
		fControl = control;
		fConn = conn;
		fControl.setState(TerminalState.CONNECTING);
	}

	public void run() {
		try {
			fProject = fConn.getProject();
			if (fProject == null) {
				String remoteServices = fConn.getSshSettings().getRemoteServices();
				IRemoteServices services = RemoteServices.getRemoteServices(remoteServices);
				if (services != null) {
					IRemoteConnectionManager connMgr = services.getConnectionManager();
					if (connMgr != null) {
						fRemoteConnection = connMgr.getConnection(fConn.getSshSettings().getConnectionName());
					}
				}
			} else {
				fRemoteConnection = Util.getRemoteConnection(fProject);
			}
			if (fRemoteConnection != null && !fRemoteConnection.isOpen()) {
				try {
					fRemoteConnection.open(null);
				} catch (RemoteConnectionException e) {
					Activator.log(e);
				}
			}

			if (fRemoteConnection != null && fRemoteConnection.isOpen()) {
				TerminalPlugin plugin = TerminalPlugin.getDefault();
				IPreferenceStore preferenceStore = plugin.getPreferenceStore();

				setupHistoryMonitoring(fConn, preferenceStore);

				try {
					// read data until the connection gets terminated
					readDataForever(fConn.getInputStream());
				} catch (InterruptedIOException e) {
					// we got interrupted: we are done...
				}
			}
		} catch (IOException e) {
			fControl.setState(TerminalState.CLOSED);
		} finally {
			// make sure the terminal is disconnected when the thread ends
			try {
				disconnect();
			} finally {
				// when reading is done, we set the state to closed
				fControl.setState(TerminalState.CLOSED);
			}
		}
	}

	private String getDefault(String val, String defaultVal) {
		if (val == null || val.trim().equals("")) { //$NON-NLS-1$
			return defaultVal;
		} else {
			return val;
		}
	}

	public void shutdown() {
		disconnect();
	}

	/**
	 * disconnect the session
	 */
	private void disconnect() {
		interrupt();
		synchronized (this) {
			if (fProcess != null && !fProcess.isCompleted()) {
				fProcess.destroy();
			}
		}
	}

	/**
	 * Read the data from the connection and display it in the terminal.
	 * 
	 * @param in
	 * @throws IOException
	 */
	private void readDataForever(InputStream in) throws IOException {
		byte[] buf = new byte[32 * 1024];
		while (true) {
			int n = in.read(buf, 0, buf.length);
			if (n < 0)
				break;
			String str = new String(buf, 0, n);
			boolean print = parse(str);
			if (print) {
				fControl.getRemoteToTerminalOutputStream().write(buf, 0, n);
			}
		}
	}

	private boolean parse(String str) {
		Pattern pattern = Pattern.compile("~~EPTP:(\\w*)~~(.*)"); //$NON-NLS-1$
		Matcher match = pattern.matcher(str);
		if (match.find()) {
			String type = match.group(1);
			String location = match.group(2);
			doAction(type, location);
			return false;
		}
		return true;
	}

	/**
	 * Process special terminal actions
	 * 
	 * @param type
	 * @param str
	 */
	private void doAction(String type, final String str) {
		if (type.equals("Radio")) { //$NON-NLS-1$
			doRadioAction(str);
		} else if (type.equals("Choice")) { //$NON-NLS-1$
			doChoiceAction(str);
		} else if (type.equals("OpenFile")) { // open file //$NON-NLS-1$
			openFile(str);
		}
	}

	/**
	 * The param "str" should be a list of files delimited by "~~". The
	 * user will be shown a choice dialog, allowing the user to pick one
	 * file to open. This selects one file from a list obtained by a shell
	 * wild card.
	 * 
	 * @param str
	 */
	private void doChoiceAction(final String str) {
		final String[] choices = str.split("\\s*~~\\s*"); //$NON-NLS-1$

		getStandardDisplay().asyncExec(new Runnable() {

			public void run() {
				try {
					final Dialog dialog = new Dialog(getStandardDisplay().getActiveShell()) {
						Combo combo;
						String[] comboChoices;

						@Override
						protected void configureShell(Shell shell) {
							shell.setText(Messages.CHOOSE_FILE);
							super.configureShell(shell);
						}

						@Override
						protected Control createDialogArea(Composite parent) {
							Composite container = (Composite) super.createDialogArea(parent);
							combo = new Combo(container, SWT.NONE);
							combo.setItems(choices);
							comboChoices = choices;
							Point pt = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
							combo.setSize(pt.x, 5 * pt.y);
							return container;
						}

						@Override
						public void buttonPressed(int buttonId) {
							int n = combo.getSelectionIndex();
							if (buttonId == 0 && n >= 0 && n < comboChoices.length) {
								openFile(comboChoices[n]);
							}
							close();
						}
					};
					dialog.open();
				} catch (Exception t) {
					Activator.log(t);
				}
			}

		});
	}

	/**
	 * Creates a dialog with a radio button that will perform one shell command
	 * from a list. The string ~~ is used to delimit commands, and ::~ is used
	 * to separate user-displayed text from the command itself. Thus, a script
	 * containing:
	 * 
	 * <pre>
	 * #!/bin/bash
	 * echo "~~EPTP:Radio~~ List Files::~ls -F~~Current Dir::~pwd"
	 * </pre>
	 * 
	 * Will produce a menu allowing the user to select from "List Files" and
	 * "Current Dir". The former will perform an "ls -F", the latter will
	 * perform a "pwd" command.
	 * 
	 * @param str
	 */
	private void doRadioAction(final String str) {
		final String[] choices = str.split("\\s*~~\\s*"); //$NON-NLS-1$

		getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					final Dialog dialog = new Dialog(getStandardDisplay().getActiveShell()) {
						private Map<String, String> smap = new HashMap<String, String>();
						private List<Button> buttons = new ArrayList<Button>();

						@Override
						protected void configureShell(Shell shell) {
							shell.setText(Messages.CHOOSE_FILE);
							super.configureShell(shell);
						}

						@Override
						protected Control createDialogArea(Composite parent) {
							Composite container = (Composite) super.createDialogArea(parent);
							for (String choice : choices) {
								String[] keyvalue = choice.split("::~"); //$NON-NLS-1$
								if (keyvalue.length == 2) {
									Button b = new Button(container, SWT.RADIO);
									b.setText(keyvalue[0]);
									smap.put(keyvalue[0], keyvalue[1]);
									buttons.add(b);
								}
							}
							return container;
						}

						@Override
						public void buttonPressed(int buttonId) {
							if (buttonId == 0) {
								for (Button b : buttons) {
									if (b.getSelection()) {
										String value = smap.get(b.getText());
										try {
											OutputStream out = fProcess.getOutputStream();
											out.write((value + "\n").getBytes()); //$NON-NLS-1$
											out.flush();
										} catch (IOException ioe) {

										}
									}
								}
							}
							close();
						}
					};
					dialog.open();
				} catch (Exception t) {
					Activator.log(t);
				}
			}
		});
	}

	/**
	 * Open a file on the remote machine. In this way, the user can
	 * more easily take advantage of eclipse editing for all his/her files.
	 * 
	 * Different logic applies inside a synched project than outside of it.
	 * Opening a remote file involves synching, and would duplicate the work
	 * of the synched project. It also might do it in a slightly inconsistent
	 * way and if both a remote and local view of the same synched file was
	 * open it might lead to confusion.
	 * 
	 * @param file
	 *            - the file to open
	 */
	public void openFile(final String file) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (int i = 0; i < projects.length; i++) {
			final IProject prj = projects[i];
			final URI remoteURI = Util.getLocationURI(prj);
			if (remoteURI != null) {
				if (file.startsWith(remoteURI.getPath())) {
					// Found!
					getStandardDisplay().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchPage page = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage();

							try {
								String loc = file.substring(remoteURI.getPath().length() + 1);
								IFile file = prj.getFile(loc);
								IDE.openEditor(page, file);
							} catch (PartInitException e) {
								Activator.log(e);
							}
						}
					});
					return;
				}
			}
		}
		getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				try {
					IEditorDescriptor editorDesc = IDE.getEditorDescriptor(file);
					IRemoteFileManager irfm = fRemoteConnection.getFileManager();
					URI uri = irfm.toURI(file);
					String editorId = editorDesc.getId();
					IDE.openEditor(page, uri, editorId, true);
				} catch (PartInitException e) {
					Activator.log(e);
				}
			}
		});
		return;
	}

	void setupHistoryMonitoring(RemoteConnector remoteConnector, IPreferenceStore preferenceStore)
			throws IOException {
		MachineManager.MachineInfo minfo = null;
		if ((fRemoteConnection.getRemoteServices().getCapabilities() & IRemoteServices.CAPABILITY_SUPPORTS_COMMAND_SHELL) != 0) {
			fProcess = fRemoteConnection.getCommandShell(IRemoteProcessBuilder.ALLOCATE_PTY);
		} else {
			minfo = MachineManager.initializeMachine(fRemoteConnection);

			// Now that we know what SHELL is, throw away the builder and create a new one.
			List<String> shellCommand = new ArrayList<String>();
			shellCommand.add(minfo.shell);
			shellCommand.add("-l"); //$NON-NLS-1$
			IRemoteProcessBuilder processBuilder = fRemoteConnection.getProcessBuilder(shellCommand);
			fProcess = processBuilder.start(IRemoteProcessBuilder.ALLOCATE_PTY);
		}
		remoteConnector.setInputStream(fProcess.getInputStream());
		fControl.setState(TerminalState.CONNECTED);
		fControl.setTerminalTitle(fRemoteConnection.getName());
		final OutputStream outputStream = fProcess.getOutputStream();
		remoteConnector.setOutputStream(outputStream);

		// Tell history files where to write commands
		MachineManager.setOutputStream(fRemoteConnection.getAddress(), outputStream);

		String startup = getDefault(
				preferenceStore.getString(Messages.SHELL_STARTUP_COMMAND),
				TerminalPrefs.SHELL_STARTUP_DEFAULT);

		if (minfo != null) {
			if (minfo.isCsh) {
				// convert to csh/tcsh syntax
				startup = startup.replaceFirst("export\\s+(\\w+)=", "setenv $1 "); //$NON-NLS-1$//$NON-NLS-2$
			} else if (minfo.isBash) {
				// convert to bash syntax
				startup = startup.replaceFirst("setenv\\s+(\\w+)\\s+", "export $1="); //$NON-NLS-1$//$NON-NLS-2$
			}
		}

		outputStream.write((startup + "\n").getBytes()); //$NON-NLS-1$
		outputStream.flush();
	}
}
