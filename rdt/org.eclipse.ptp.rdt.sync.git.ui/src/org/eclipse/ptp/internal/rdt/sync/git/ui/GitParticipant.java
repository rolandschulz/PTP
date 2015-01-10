/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.sync.git.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.ptp.internal.rdt.sync.git.core.CommandRunner;
import org.eclipse.ptp.internal.rdt.sync.git.core.CommandRunner.CommandResults;
import org.eclipse.ptp.internal.rdt.sync.git.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.sync.core.RecursiveSubMonitor;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteExecutionException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.ui.AbstractSynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIConstants;
import org.eclipse.remote.ui.IRemoteUIFileManager;
import org.eclipse.remote.ui.IRemoteUIServices;
import org.eclipse.remote.ui.RemoteUIServices;
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Launches a dialog that configures a remote sync target with OK and Cancel
 * buttons. Also has a text field to allow the name of the configuration to be
 * changed.
 */
public class GitParticipant extends AbstractSynchronizeParticipant {
	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$
    private static final String TOUCH_TEST_FILE = ".touch_test_file_ptp_sync";
	private static final Display display = Display.getCurrent();
	private IRunnableContext fContext;

	private IRemoteConnection fSelectedConnection;

	private String fProjectName = ""; //$NON-NLS-1$
	private String remoteError = null;
	private Button fBrowseButton;

	private Text fLocationText;
	private RemoteConnectionWidget fRemoteConnectionWidget;
	private ProgressMonitorPart fValidateRemoteProgressBar;
	private Button fValidateRemoteButton;
	private IWizardContainer container;

	// If false, automatically select "Remote Tools" provider instead of letting the user select the provider.
	private final boolean showProviderCombo = false;

	public GitParticipant(ISynchronizeParticipantDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Attempt to open a connection.
	 */
	private void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fRemoteConnectionWidget.getShell(), null, fSelectedConnection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#createConfigurationArea
	 * (org.eclipse.swt.widgets.Composite,
	 * org.eclipse.jface.operation.IRunnableContext)
	 */
	@Override
	public void createConfigurationArea(Composite parent, IRunnableContext context) {
		fContext = context;
		this.container = (IWizardContainer) context;
		final Composite configArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		configArea.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		configArea.setLayoutData(gd);

		int flags = showProviderCombo ? RemoteConnectionWidget.FLAG_FORCE_PROVIDER_SELECTION : 0;
		flags |= RemoteConnectionWidget.FLAG_NO_LOCAL_SELECTION;
		fRemoteConnectionWidget = new RemoteConnectionWidget(configArea, SWT.NONE, null, flags, context);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.grabExcessHorizontalSpace = true;
		fRemoteConnectionWidget.setLayoutData(gd);
		fRemoteConnectionWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionSelected();
			}
		});

		Label locationLabel = new Label(configArea, SWT.LEFT);
		locationLabel.setText(Messages.GitParticipant_location);

		fLocationText = new Text(configArea, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 250;
		fLocationText.setLayoutData(gd);
		fLocationText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// MBSCustomPageManager.addPageProperty(REMOTE_SYNC_WIZARD_PAGE_ID,
				// PATH_PROPERTY, fLocationText.getText());
				update();
			}
		});
		handleConnectionSelected();

		// new connection button
		fBrowseButton = new Button(configArea, SWT.PUSH);
		fBrowseButton.setText(Messages.GitParticipant_browse);
		fBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSelectedConnection != null) {
					checkConnection();
					if (fSelectedConnection.isOpen()) {
						IRemoteUIServices remoteUIServices = RemoteUIServices.getRemoteUIServices(fSelectedConnection
								.getRemoteServices());
						if (remoteUIServices != null) {
							IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
							if (fileMgr != null) {
								fileMgr.setConnection(fSelectedConnection);
								String correctPath = fLocationText.getText();
								String selectedPath = fileMgr.browseDirectory(
										fLocationText.getShell(),
										"Project Location (" + fSelectedConnection.getName() + ")", correctPath, IRemoteUIConstants.NONE); //$NON-NLS-1$ //$NON-NLS-2$
								if (selectedPath != null) {
									fLocationText.setText(selectedPath);
								}
							}
						}
					}
				}
			}
		});

		// Button to start validation of remote
		fValidateRemoteButton = new Button(configArea, SWT.PUSH);
		fValidateRemoteButton.setText("Validate Remote");
		fValidateRemoteButton.setEnabled(false);
		fValidateRemoteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkRemote();
				if (remoteError == null) {
					fLocationText.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
				} else {
					fLocationText.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
				}
				update();
			}
		});

		// Progress bar for validation of remote directory
		// Place inside group to create a border
		Group progressGroup = new Group(configArea, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		progressGroup.setLayoutData(gd);
		progressGroup.setLayout(new GridLayout());
		fValidateRemoteProgressBar = new ProgressMonitorPart(progressGroup, new GridLayout(), true);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		fValidateRemoteProgressBar.setLayoutData(gd);
		fValidateRemoteProgressBar.setEnabled(false);
		// fValidateRemoteProgressBar.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
	}

	/**
	 * Return the path we are going to display. If it is a file URI then remove
	 * the file prefix.
	 * 
	 * Only do this if the connection is open. Otherwise we will attempt to
	 * connect to the first machine in the list, which is annoying.
	 * 
	 * @return String
	 */
	private String getDefaultPathDisplayString() {
		//		String projectName = ""; //$NON-NLS-1$
		// IWizardPage page = getWizard().getStartingPage();
		// if (page instanceof CDTMainWizardPage) {
		// projectName = ((CDTMainWizardPage) page).getProjectName();
		// }
		if (fSelectedConnection != null && fSelectedConnection.isOpen()) {
			IRemoteFileManager fileMgr = fSelectedConnection.getFileManager();
			URI defaultURI = fileMgr.toURI(fSelectedConnection.getWorkingDirectory());

			// Handle files specially. Assume a file if there is no project to
			// query
			if (defaultURI != null && defaultURI.getScheme().equals(FILE_SCHEME)) {
				return Platform.getLocation().append(fProjectName).toString();
			}
			if (defaultURI == null) {
				return ""; //$NON-NLS-1$
			}
			return new Path(defaultURI.getPath()).append(fProjectName).toString();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see ISynchronizeParticipant#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		if (fSelectedConnection == null) {
			return Messages.GitParticipant_1;
		}
		if (fLocationText.getText().length() == 0) {
			return Messages.GitParticipant_2;
		}
		if (remoteError != null) {
			return remoteError;
		}
		// should we check permissions of: fileManager.getResource(fLocationText.getText()).getParent() ?
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#getConnection()
	 */
	@Override
	public IRemoteConnection getConnection() {
		return fSelectedConnection;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#getLocation()
	 */
	@Override
	public String getLocation() {
		return fLocationText.getText();
	}

	@Override
	public String getSyncConfigName() {
		return fSelectedConnection.getName();
	}

	/**
	 * @return
	 */
	private IRemoteUIConnectionManager getUIConnectionManager() {
		if (fSelectedConnection != null) {
			IRemoteUIConnectionManager connectionManager = RemoteUIServices.getRemoteUIServices(
					fSelectedConnection.getRemoteServices()).getUIConnectionManager();
			return connectionManager;
		}
		return null;
	}

	/**
	 * Handle new connection selected
	 */
	private void handleConnectionSelected() {
		fSelectedConnection = fRemoteConnectionWidget.getConnection();
		fLocationText.setText(getDefaultPathDisplayString());
		if ((fValidateRemoteButton != null) && (!fValidateRemoteButton.isDisposed())) {
			if (fSelectedConnection == null) {
				fValidateRemoteButton.setEnabled(false);
			} else {
				fValidateRemoteButton.setEnabled(true);
			}
		}
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#isConfigComplete()
	 */
	@Override
	public boolean isConfigComplete() {
		return getErrorMessage() == null;
	}

	// Check if the remote location is valid. If valid, set "remoteError" to null. Otherwise, remoteError contains the error message.
	private void checkRemote() {
		remoteError = null;
		RecursiveSubMonitor progress = RecursiveSubMonitor.convert(fValidateRemoteProgressBar, 30);
		progress.beginTask("Validate Remote", 30);
		IPath parentPath = new Path(fLocationText.getText());
		if (!parentPath.isAbsolute()) {
			remoteError = "Remote path must be absolute";
		}
		try {
			// Find the lowest-level file in the path that exist.
			int numloops = 0;
			while(!parentPath.isRoot()) {
				List<String> args = Arrays.asList("test", "-e", parentPath.toString());
				String errorMessage = null;
				CommandResults cr = null;
				try {
					progress.subTask("Checking if " + parentPath.toString() + " exists");
					cr = this.runRemoteCommand(args, progress.newChild(1));
				} catch (RemoteExecutionException e) {
					errorMessage = this.buildErrorMessage(null, "Unable to verify remote", e);
				}

				if (errorMessage != null) {
					MessageDialog.openError(null, "Remote Execution", errorMessage);
					remoteError = "Unable to verify remote path";
				} else if (cr.getExitCode() == 0) {
					break;
				}

				parentPath = parentPath.removeLastSegments(1);
				numloops++;
				if (numloops > 9) {
					progress.setWorkRemaining(21);
				}
			}
			progress.setWorkRemaining(20);

			// Assume parent path is a directory and see if we can write a test file to it.
			// Note that this test fails if parent path is not a directory, so no need to test that case.
			String touchFile = parentPath.append(new Path(TOUCH_TEST_FILE)).toString();
			List<String> args = Arrays.asList("touch", touchFile);
			String errorMessage = null;
			CommandResults cr = null;
			try {
				progress.subTask("Testing if " + parentPath.toString() + " is accessible");
				cr = this.runRemoteCommand(args, progress.newChild(10));
			} catch (RemoteExecutionException e) {
				errorMessage = this.buildErrorMessage(null, "Unable to verify remote", e);
			}

			if (errorMessage != null) {
				MessageDialog.openError(null, "Remote Execution", errorMessage);
				remoteError = "Unable to verify remote path";
			} else if (cr.getExitCode() != 0) {
				remoteError = "Remote path invalid";
			}

			// Remove the test file
			args = Arrays.asList("rm", "-f", touchFile);
			errorMessage = null;
			cr = null;
			try {
				progress.subTask("Cleaning up from testing");
				cr = this.runRemoteCommand(args, progress.newChild(10));
				errorMessage = this.buildErrorMessage(cr, "Unable to remove test file: " + touchFile, null);
			} catch (RemoteExecutionException e) {
				errorMessage = this.buildErrorMessage(null, "Unable to remove test file: " + touchFile, e);
			}

			if (errorMessage != null) {
				MessageDialog.openError(null, "Remote Execution", errorMessage);
			}
		} finally {
			if (fValidateRemoteProgressBar != null) {
				fValidateRemoteProgressBar.done();
			}
		}
	}

    // Wrapper for running commands - wraps exceptions and invoking of command runner inside container run command.
    private CommandResults remoteCommandResults;
    private CommandResults runRemoteCommand(final List<String> command, final IProgressMonitor progress)
    		throws RemoteExecutionException {
            try {
                    fContext.run(false, true, new IRunnableWithProgress() {
                    		// TODO: Is it okay that we ignore the input monitor? Seems wrong...
                            @Override
                            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                                    try {
                                            remoteCommandResults = CommandRunner.executeRemoteCommand(fSelectedConnection, command, null, progress);
                                    } catch (RemoteSyncException e) {
                                            throw new InvocationTargetException(e);
                                    } catch (IOException e) {
                                            throw new InvocationTargetException(e);
                                    } catch (InterruptedException e) {
                                            throw new InvocationTargetException(e);
                                    } catch (RemoteConnectionException e) {
                                            throw new InvocationTargetException(e);
                                    } finally {
                                            monitor.done();
                                    }
                            }
                    });
            } catch (InvocationTargetException e) {
                    throw new RemoteExecutionException(e.getCause());
            } catch (InterruptedException e) {
                    throw new RemoteExecutionException(e);
            }
            return remoteCommandResults;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#setProjectName(String projectName)
	 */
	@Override
	public void setProjectName(String projectName) {
		fProjectName = projectName;
		fLocationText.setText(getDefaultPathDisplayString());
	}

	private void update() {
		container.updateMessage();
		// updateButtons() may fail if current page is null. This can happen if update() is called during wizard/page creation.
		if (container.getCurrentPage() == null) {
			return;
		}
		container.updateButtons();
	}

    // Builds error message for command.
    // Either the command result or the exception should be null, but not both.
    // baseMessage cannot be null.
    // Returns error message or null if no error occurred (can only occur if cr is not null).
    private String buildErrorMessage(CommandResults cr, String baseMessage, RemoteExecutionException e) {
            // Command successful
            if (cr != null && cr.getExitCode() == 0) {
                    return null;
            }

            // Command runs but unsuccessfully
            if (cr != null) {
                    return baseMessage + ": " + cr.getStderr();
            }

            // Command did not run - exception thrown
            String errorMessage = baseMessage;
            if (e.getMessage() != null) {
                    errorMessage += ": " + e.getMessage();
            }

            return errorMessage;
    }
}
