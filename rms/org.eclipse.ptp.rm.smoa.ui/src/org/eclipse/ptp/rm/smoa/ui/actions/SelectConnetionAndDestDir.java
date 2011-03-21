/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOAConnection;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOARemoteServices;
import org.eclipse.ptp.rm.smoa.ui.SMOAUIPlugin;
import org.eclipse.ptp.rm.smoa.ui.rservices.SMOAUIFileManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Provides dialog for selecting the connection and remote directory.
 * 
 * Has also option to display rules for {@link SMOAToLocalSyncAction} and
 * {@link SMOAToRemoteSyncAction} classes.
 */
/* package access */class SelectConnetionAndDestDir {

	// Locations in {@link IProject}'s store for keeping settings of this dialog
	private static final QualifiedName SMOA_SYNC_PATH = new QualifiedName(
			SMOAUIPlugin.PLUGIN_ID, "smoa.sync.rm.path"); //$NON-NLS-1$
	private static final QualifiedName SMOA_SYNC_RM = new QualifiedName(
			SMOAUIPlugin.PLUGIN_ID, "smoa.sync.rm.name"); //$NON-NLS-1$
	private static final QualifiedName SMOA_SYNC_CREATE = new QualifiedName(
			SMOAUIPlugin.PLUGIN_ID, "smoa.sync.rm.create"); //$NON-NLS-1$
	private static final QualifiedName SMOA_SYNC_RULES = new QualifiedName(
			SMOAUIPlugin.PLUGIN_ID, "smoa.sync.rsync.rules"); //$NON-NLS-1$

	/**
	 * Set of default ignore rules, loaded when the project has none in it's
	 * settings
	 */
	private static final String DEFAULT_RULES = Messages.SelectConnetionAndDestDir_DefaultRules;

	/** Tries to retrieve the IProject from event */
	static public IProject getSelectedProject(ExecutionEvent execEvent)
			throws ExecutionException {
		Object object = execEvent.getApplicationContext();
		if (!(object instanceof EvaluationContext)) {
			throw new ExecutionException("EvaluationContext"); //$NON-NLS-1$
		}
		final EvaluationContext context = (EvaluationContext) execEvent
				.getApplicationContext();

		object = context.getDefaultVariable();

		if (!(object instanceof List)) {
			throw new ExecutionException("List"); //$NON-NLS-1$
		}
		@SuppressWarnings("unchecked")
		final List<Object> list = (List<Object>) object;

		object = list.get(0);
		if (!(object instanceof IProject)) {
			throw new ExecutionException("IProject"); //$NON-NLS-1$
		}

		final IProject project = (IProject) object;
		return project;
	}

	private final IProject project;

	private final SMOARemoteServices rservices = (SMOARemoteServices) PTPRemoteCorePlugin
			.getDefault().getRemoteServices("org.eclipse.ptp.remote.SMOARemoteServices"); //$NON-NLS-1$
	private String remoteDir = null;
	private SMOAConnection connection = null;

	private boolean mkdirIfNotExists = false;
	private boolean showRules = false;

	private String rules = null;
	private boolean succeeded = false;

	private String windowTitle;

	public SelectConnetionAndDestDir(final IProject project) {
		this.project = project;
	}

	public SMOAConnection getConnection() {
		return connection;
	}

	/** Returns directory selected by user for remote synchronization */
	public String getRemoteDir() {
		return remoteDir;
	}

	/** Gets remote file store */
	public IFileStore getRemoteFileStore() throws CoreException {
		if (!succeeded) {
			return null;
		}

		final IRemoteFileManager fileManager = rservices.getFileManager(connection);
		final IFileStore resource = fileManager.getResource(remoteDir);

		if (mkdirIfNotExists) {
			resource.mkdir(0, null);
		}

		return resource;
	}

	/** Returns interpreted ignore rules */
	public List<Pattern> getRules() {
		if (rules == null) {
			return null;
		}

		final Vector<Pattern> v = new Vector<Pattern>();

		for (final String rule : rules.split("\n")) { //$NON-NLS-1$
			if (!rule.isEmpty()) {
				v.add(Pattern.compile(rule));
			}
		}

		return v;
	}

	/** Returns if the user triggered 'Ok' on the dialog */
	public boolean hasSucceeded() {
		return succeeded;
	}

	public boolean isMkdirIfNotExists() {
		return mkdirIfNotExists;
	}

	public boolean isShowRules() {
		return showRules;
	}

	/**
	 * Opens the dialog and waits till completion
	 * 
	 * If user selected and accepted the dialog, returns true. False otherwise.
	 */
	@SuppressWarnings({ "unused", "null" })
	public boolean open() throws CoreException {
		final Shell topShell = Display.getCurrent().getActiveShell();

		final Map<String, SMOAConnection> connections = rservices
				.getConnectionManager().getOpenConnections();

		// re-labelling connections
		final Set<SMOAConnection> temp = new HashSet<SMOAConnection>(
				connections.values());
		connections.clear();
		for (final SMOAConnection c : temp) {
			connections.put(c.getRMName(), c);
		}

		if (connections.isEmpty()) {
			final MessageBox mb = new MessageBox(topShell, SWT.ICON_INFORMATION
					| SWT.OK);
			mb.setText(Messages.SelectConnetionAndDestDir_ErrorNoActiveConnectionTitle);
			mb.setMessage(Messages.SelectConnetionAndDestDir_ErrorNoActiveConnection);
			mb.open();
			return false;
		}

		final Shell shell = new Shell(topShell, new Shell().getStyle()
				| SWT.APPLICATION_MODAL);
		shell.setLayout(new GridLayout(3, false));

		shell.setText(windowTitle != null ? windowTitle
				: Messages.SelectConnetionAndDestDir_DefaultWindowTitle);

		new Label(shell, SWT.NONE).setText(Messages.SelectConnetionAndDestDir_ResourceManager);

		final Combo combo = new Combo(shell, SWT.READ_ONLY);
		final GridData gridData1 = new GridData(GridData.FILL_HORIZONTAL);
		combo.setLayoutData(gridData1);
		gridData1.horizontalSpan = 2;

		for (final String conn : connections.keySet()) {
			combo.add(conn);
		}

		new Label(shell, SWT.NONE)
				.setText(Messages.SelectConnetionAndDestDir_RemoteRootPath);

		final Text remoteRootPath = new Text(shell, SWT.SINGLE | SWT.BORDER);
		remoteRootPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Button browse = new Button(shell, SWT.PUSH | SWT.BORDER);
		browse.setText(Messages.SelectConnetionAndDestDir_Browse);

		new Label(shell, SWT.NONE);

		final Button cbCreateDir = new Button(shell, SWT.CHECK | SWT.BORDER);
		cbCreateDir.setText(Messages.SelectConnetionAndDestDir_CreateIfNotExists);
		cbCreateDir.setSelection(true);

		new Label(shell, SWT.NONE);

		final StyledText rules;

		final boolean showRulesLocal = showRules;
		if (showRulesLocal) {
			final Composite rulesComp = new Composite(shell, SWT.NONE);
			final GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
			rulesComp.setLayoutData(gridData2);
			gridData2.horizontalSpan = 3;
			rulesComp.setLayout(new GridLayout(1, false));

			new Label(rulesComp, SWT.NONE)
					.setText(Messages.SelectConnetionAndDestDir_ExcludeRules);

			rules = new StyledText(rulesComp, SWT.MULTI | SWT.BORDER
					| SWT.V_SCROLL | SWT.H_SCROLL);

			rules.getHorizontalBar().setVisible(true);

			final GridData gridData = new GridData(GridData.FILL_BOTH);
			rules.setLayoutData(gridData);
			gridData.minimumHeight = rules.getLineHeight() * 5;
		} else {
			rules = null;
		}

		final Composite buttons = new Composite(shell, SWT.NONE);
		final GridData gridData3 = new GridData(GridData.FILL_HORIZONTAL);
		buttons.setLayoutData(gridData3);
		gridData3.horizontalSpan = 3;

		buttons.setLayout(new GridLayout(3, false));
		new Label(buttons, SWT.NONE).setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		final Button ok = new Button(buttons, SWT.PUSH | SWT.BORDER);
		ok.setText(Messages.SelectConnetionAndDestDir_Ok);

		final Button cancel = new Button(buttons, SWT.PUSH | SWT.BORDER);
		cancel.setText(Messages.SelectConnetionAndDestDir_Cancel);

		remoteRootPath.setEnabled(false);
		browse.setEnabled(false);
		ok.setEnabled(false);

		final Runnable check = new Runnable() {
			String previousRM = null;
			String previousDir = null;
			boolean previousResult = false;

			public void run() {
				if (showRulesLocal) {
					if (rules.getStyleRanges().length != 0) {
						ok.setEnabled(false);
						return;
					}
				}

				if (combo.getSelectionIndex() == -1) {
					ok.setEnabled(false);
					return;
				}

				if (remoteRootPath.getText().isEmpty()) {
					ok.setEnabled(false);
					return;
				}
				if (cbCreateDir.getSelection()) {
					ok.setEnabled(true);
					return;
				}

				if (combo.getText().equals(previousRM)
						&& remoteRootPath.getText().equals(previousDir)) {
					ok.setEnabled(previousResult);
					return;
				}

				previousRM = combo.getText();
				previousDir = remoteRootPath.getText();
				final IRemoteFileManager fileManager = rservices
						.getFileManager(connections.get(previousRM));
				final IFileStore resource = fileManager.getResource(previousDir);
				try {
					ok.setEnabled(resource.fetchInfo(0, null).isDirectory());
				} catch (final CoreException e) {
					final MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR
							| SWT.OK);
					mb.setText(Messages.SelectConnetionAndDestDir_ErrorByFetchInfoTitle);
					mb.setMessage(e.getMessage());
				}
				previousResult = ok.getEnabled();
			}
		};

		combo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			public void widgetSelected(SelectionEvent arg0) {
				remoteRootPath.setEnabled(true);
				browse.setEnabled(true);
				remoteRootPath.setText(connections.get(combo.getText())
						.getHomeDir() + "/" + project.getName()); //$NON-NLS-1$
				check.run();
			}
		});

		remoteRootPath.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				check.run();
			}
		});

		browse.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				final SMOAUIFileManager uifm = new SMOAUIFileManager(rservices);
				uifm.showConnections(false);
				uifm.setConnection(connections.get(combo.getText()));
				final String chosen = uifm.browseDirectory(shell,
						Messages.SelectConnetionAndDestDir_DirBrowserTitle, remoteRootPath.getText(), 0);
				if (chosen != null) {
					remoteRootPath.setText(chosen);
				}
			}
		});

		if (showRulesLocal) {
			rules.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent arg0) {
					rules.setStyleRanges(new StyleRange[0]);
					int start = 0;
					final String[] array = rules.getText().split("\n"); //$NON-NLS-1$
					for (int i = 0; i < array.length; ++i) {
						if (array[i].isEmpty()) {
							start++;
							continue;
						}
						try {
							Pattern.compile(array[i]);
						} catch (final PatternSyntaxException e) {
							final TextStyle textStyle = new TextStyle();
							textStyle.underline = true;
							textStyle.underlineColor = new Color(rules
									.getDisplay(), 255, 0, 0);
							textStyle.underlineStyle = SWT.UNDERLINE_ERROR;
							final StyleRange style = new StyleRange(textStyle);
							style.start = start;
							style.length = array[i].length();

							rules.setStyleRange(style);

						}
						start += array[i].length() + 1;
					}

					check.run();
				}
			});
		}

		cancel.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				shell.close();
			}
		});

		cbCreateDir.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				check.run();
			}
		});

		final String[] result = new String[2];
		final SMOAConnection[] conn = new SMOAConnection[1];
		final boolean[] mkdir = new boolean[1];

		ok.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				result[0] = remoteRootPath.getText();
				if (showRulesLocal) {
					result[1] = rules.getText();
				}
				conn[0] = connections.get(combo.getText());
				mkdir[0] = cbCreateDir.getSelection();
				shell.close();
			}
		});

		final String create = project.getPersistentProperty(SMOA_SYNC_CREATE);
		if (create != null) {
			cbCreateDir.setSelection(create.equals("true")); //$NON-NLS-1$
		}

		final String rm = project.getPersistentProperty(SMOA_SYNC_RM);
		if (rm != null) {
			final SMOAConnection c = connections.get(rm);
			if (c != null) {
				combo.select(combo.indexOf(rm));
			}
			remoteRootPath.setEnabled(true);
			browse.setEnabled(true);
		}

		final String path = project.getPersistentProperty(SMOA_SYNC_PATH);
		if (path != null) {
			remoteRootPath.setText(path);
		}

		if (showRulesLocal) {
			final String rulesVar = project.getPersistentProperty(SMOA_SYNC_RULES);
			if (rulesVar != null) {
				rules.setText(rulesVar);
			} else {
				rules.setText(DEFAULT_RULES);
			}
		}

		shell.pack();

		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}

		if (result[0] == null || result[0].isEmpty()) {
			return false;
		}

		connection = conn[0];
		remoteDir = result[0];
		mkdirIfNotExists = mkdir[0];
		if (showRulesLocal) {
			this.rules = result[1];
		}

		project.setPersistentProperty(SMOA_SYNC_PATH, result[0]);
		project.setPersistentProperty(SMOA_SYNC_RM, conn[0].getRMName());
		project.setPersistentProperty(SMOA_SYNC_CREATE, mkdir[0] ? "true" //$NON-NLS-1$
				: "false"); //$NON-NLS-1$

		if (showRulesLocal) {
			project.setPersistentProperty(SMOA_SYNC_RULES, result[1]);
		}

		succeeded = true;
		return true;
	}

	/** Adds to the GUI proper dialog for the ignore rules */
	public void setShowRules(boolean showRules) {
		this.showRules = showRules;
	}

	public void setWindowTitle(String title) {
		windowTitle = title;
	}

}
