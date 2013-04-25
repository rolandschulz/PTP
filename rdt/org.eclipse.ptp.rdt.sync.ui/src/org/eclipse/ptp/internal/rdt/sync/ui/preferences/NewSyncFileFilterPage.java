/*******************************************************************************
 * Copyright (c) 2011,2013 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.ResourceMatcher;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter.PatternType;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog to display a File tree and other options, where users can select the patterns/files to be sync'ed.<br>
 * This class is used for two scenarios:<br>
 * 1. Filter page for new sync project - to specify settings for new project<br>
 * 2. Filter page for existing project - to alter existing Sync Filter settings<br>
 * 
 * Uses ResourceMatchers (e.g. PathResourceMatcher, RegexResourceMatcher, and WildcardResourceMatcher) to match the
 * "patterns" entered to actual files in the project.
 */
public class NewSyncFileFilterPage extends Dialog {
	private static final int ERROR_DISPLAY_SECONDS = 3;
	private static final Display display = Display.getCurrent();

	private static final String PATH_MATCHER = "path"; //$NON-NLS-1$
	private static final String REGEX_MATCHER = "regex"; //$NON-NLS-1$
	private static final String WILDCARD_MATCHER = "wildcard"; //$NON-NLS-1$

	private final IProject project;
	private final SyncFileFilter filter;
	private CheckboxTreeViewer treeViewer;
	private Table patternTable;
	private Button showRemoteButton;
	private Label remoteErrorLabel;
	private Button addButton;
	private Button upButton;
	private Button downButton;
	private Button editButton;
	private Button removeButton;

	/** Boolean to help tell which boolean arguments do what */
	static final boolean GRAB_EXCESS = true;
	/** Boolean to turn on funky colors to debug which composites contain what */
	static final boolean DEBUG = false;

	/**
	 * Constructor for a new tree. Behavior of the page varies based on whether arguments are null and whether isPreferencePage
	 * is set. Specifically, whether the file view is shown, how or if the filter is saved, and if preference page
	 * functionality is available. See comments for the default constructor and static open methods for details.
	 * 
	 * @param p
	 *            project
	 * @param isPreferencePage
	 * @param targetFilter
	 */
	private NewSyncFileFilterPage(Shell parent, IProject p, SyncFileFilter targetFilter) {
		super(parent);
		setShellStyle(SWT.RESIZE | getShellStyle());

		project = p;

		if (targetFilter == null) {
			if (project == null) {
				// scenario 1: preference page
				filter = SyncManager.getDefaultFileFilter();
			} else {
				// scenario 2/3: project (new or existing)
				filter = SyncManager.getFileFilter(project);
			}
		} else {
			filter = targetFilter;
		}

		setReturnCode(CANCEL);
	}

	/**
	 * Open a standalone GUI to change the filter of the passed project.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param project
	 *            existing project
	 * @return open return code
	 */
	public static int open(Shell parent, IProject project) {
		return new NewSyncFileFilterPage(parent, project, null).open();
	}

	/**
	 * Open a standalone GUI (window) to change the passed filter. This is useful for the new project wizard and other places where
	 * the filter does not yet have a context. This method blocks, as it assumes the client wants to wait for the filter changes.
	 * 
	 * The client most likely should pass a copy of the filter to be altered, and then check the return code for OK or Cancel
	 * to decide if the copy should be kept.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param filter
	 *            a sync file filter that will be modified
	 * @return open return code
	 */
	public static int openBlocking(Shell parent, SyncFileFilter filter) {
		NewSyncFileFilterPage page = new NewSyncFileFilterPage(parent, null, filter);
		page.setBlockOnOpen(true);
		return page.open();
	}

	/**
	 * Configures the shell (sets window title)
	 * 
	 * @param shell
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.NewSyncFileFilterPage_Title);
	}

	/**
	 * Creates the main window's contents
	 * 
	 * @param parent
	 *            the main window
	 * @return Control
	 */

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(main, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 500;
		composite.setLayoutData(gd);

		// To edit existing filters for a project, create the tree viewer
		if (project != null) {
			// Composite for tree viewer
			Composite treeViewerComposite = new Composite(composite, SWT.NONE);
			GridLayout treeLayout = new GridLayout(2, false);
			treeViewerComposite.setLayout(treeLayout);

			gd = new GridData(SWT.FILL, SWT.FILL, GRAB_EXCESS, GRAB_EXCESS);
			if (DEBUG) {
				colorComposite(treeViewerComposite, SWT.COLOR_RED);
			}

			treeViewerComposite.setLayoutData(gd);

			// Label for file tree viewer
			Label treeViewerLabel = new Label(treeViewerComposite, SWT.NONE);
			treeViewerLabel.setText(Messages.NewSyncFileFilterPage_Select_files_to_be_included);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
			treeViewerLabel.setLayoutData(gd);

			// File tree viewer
			treeViewer = new CheckboxTreeViewer(treeViewerComposite);
			gd = new GridData(SWT.FILL, SWT.FILL, GRAB_EXCESS, GRAB_EXCESS, 2, 1);
			gd.heightHint = 200;
			treeViewer.getTree().setLayoutData(gd);
			treeViewer.setContentProvider(new SFTTreeContentProvider());
			treeViewer.setLabelProvider(new SFTTreeLabelProvider());
			treeViewer.setCheckStateProvider(new ICheckStateProvider() {
				@Override
				public boolean isChecked(Object element) {
					if (filter.shouldIgnore((IResource) element)) {
						return false;
					} else {
						return true;
					}
				}

				@Override
				public boolean isGrayed(Object element) {
					return false;
				}
			});
			treeViewer.addCheckStateListener(new ICheckStateListener() {
				@Override
				public void checkStateChanged(CheckStateChangedEvent event) {
					IPath path = ((IResource) (event.getElement())).getProjectRelativePath();
					if (event.getChecked()) {
						filter.addPattern(SyncFileFilter.getPathResourceMatcher(path), PatternType.INCLUDE);
					} else {
						filter.addPattern(SyncFileFilter.getPathResourceMatcher(path), PatternType.EXCLUDE);
					}

					update();
				}
			});
			treeViewer.setInput(project);

			showRemoteButton = new Button(treeViewerComposite, SWT.CHECK);
			showRemoteButton.setText(Messages.NewSyncFileFilterPage_Show_remote_files);
			showRemoteButton.setSelection(((SFTTreeContentProvider) treeViewer.getContentProvider()).getShowRemoteFiles());
			showRemoteButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					update();
				}
			});

			remoteErrorLabel = new Label(treeViewerComposite, SWT.CENTER);
			remoteErrorLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
			gd = new GridData(SWT.FILL, SWT.CENTER, GRAB_EXCESS, false);
			remoteErrorLabel.setLayoutData(gd);
			if (DEBUG) {
				remoteErrorLabel.setText("remote error label here"); //$NON-NLS-1$
			}
		}

		// Composite for pattern view - pattern table and buttons
		Composite patternTableComposite = new Composite(composite, SWT.NONE);
		if (DEBUG) {
			colorComposite(patternTableComposite, SWT.COLOR_BLUE);
		}

		GridLayout patternTableLayout = new GridLayout(2, false);
		patternTableComposite.setLayout(patternTableLayout);

		gd = new GridData(SWT.FILL, SWT.TOP, GRAB_EXCESS, GRAB_EXCESS);
		patternTableComposite.setLayoutData(gd);

		// Label for pattern table
		Label patternTableLabel = new Label(patternTableComposite, SWT.NONE);
		patternTableLabel.setText(Messages.NewSyncFileFilterPage_Patterns_to_include);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1);// LEFT==LEAD
		patternTableLabel.setLayoutData(gd);

		// Pattern table
		patternTable = new Table(patternTableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, GRAB_EXCESS, GRAB_EXCESS, 1, 6); // room for 4 buttons at the right
		patternTable.setLayoutData(gd);
		TableColumn column = new TableColumn(patternTable, SWT.LEFT, 0);
		column.setText(Messages.NewSyncFileFilterPage_Pattern);
		column = new TableColumn(patternTable, SWT.LEFT, 1);
		column.setText(Messages.NewSyncFileFilterPage_Type);
		column = new TableColumn(patternTable, SWT.LEFT, 2);
		column.setText(Messages.NewSyncFileFilterPage_Matcher);
		patternTable.setHeaderVisible(true);

		patternTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				editPattern();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// do nothing
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// do nothing
			}
		});
		patternTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonEnablement();
			}

		});

		addButton = new Button(patternTableComposite, SWT.PUSH);
		addButton.setText(Messages.NewSyncFileFilterPage_Add);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				addPattern();
				updateButtonEnablement();
			}
		});

		editButton = new Button(patternTableComposite, SWT.PUSH);
		editButton.setText(Messages.NewSyncFileFilterPage_Edit);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				editPattern();
				updateButtonEnablement();
			}
		});
		editButton.setEnabled(false);

		removeButton = new Button(patternTableComposite, SWT.PUSH);
		removeButton.setText(Messages.NewSyncFileFilterPage_Remove);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TableItem[] selectedPatternItems = patternTable.getSelection();
				for (TableItem selectedPatternItem : selectedPatternItems) {
					ResourceMatcher selectedPattern = (ResourceMatcher) selectedPatternItem.getData();
					filter.removePattern(selectedPattern);
				}
				update();
				updateButtonEnablement();
			}
		});
		removeButton.setEnabled(false);

		// Spacer
		new Label(patternTableComposite, SWT.NONE);

		// Pattern table buttons (up, down, edit, remove)
		upButton = new Button(patternTableComposite, SWT.PUSH);
		upButton.setText(Messages.NewSyncFileFilterPage_Move_up);
		upButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TableItem[] selectedPatternItems = patternTable.getSelection();
				if (selectedPatternItems.length != 1) {
					return;
				}
				int patternIndex = patternTable.getSelectionIndex();
				if (filter.promote((ResourceMatcher) selectedPatternItems[0].getData())) {
					patternIndex--;
				}
				update();
				patternTable.select(patternIndex);
				updateButtonEnablement();
			}
		});
		upButton.setEnabled(false);

		downButton = new Button(patternTableComposite, SWT.PUSH);
		downButton.setText(Messages.NewSyncFileFilterPage_Move_down);
		downButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TableItem[] selectedPatternItems = patternTable.getSelection();
				if (selectedPatternItems.length != 1) {
					return;
				}
				int patternIndex = patternTable.getSelectionIndex();
				if (filter.demote((ResourceMatcher) selectedPatternItems[0].getData())) {
					patternIndex++;
				}
				update();
				patternTable.select(patternIndex);
				updateButtonEnablement();
			}
		});
		downButton.setEnabled(false);

		update();
		return composite;
	}

	private void updateButtonEnablement() {
		editButton.setEnabled(false);
		removeButton.setEnabled(false);
		upButton.setEnabled(false);
		downButton.setEnabled(false);
		int index = patternTable.getSelectionIndex();
		if (index >= 0) {
			editButton.setEnabled(true);
			removeButton.setEnabled(true);
			if (index > 0) {
				upButton.setEnabled(true);
			}
			if (index < patternTable.getItemCount() - 1) {
				downButton.setEnabled(true);
			}
		}
	}

	/** Creates a modal dialog to edit the selected pattern and replaces it if user hits "OK" */
	private void editPattern() {
		TableItem[] selectedPatternItem = patternTable.getSelection();
		// Modifying more than one pattern at a time is not supported
		if (selectedPatternItem.length != 1) {
			return;
		}
		ResourceMatcher selectedPattern = (ResourceMatcher) selectedPatternItem[0].getData();
		SimpleEditPatternDialog dialog = new SimpleEditPatternDialog(patternTable.getShell(), selectedPattern);
		if (dialog.open() == Window.OK) {
			filter.replacePattern(selectedPattern, dialog.getNewMatcher(), dialog.getPatternType());
			update();
		}
	}

	private void addPattern() {
		SimpleEditPatternDialog dialog = new SimpleEditPatternDialog(patternTable.getShell(), null);
		if (dialog.open() == Window.OK) {
			filter.addPattern(dialog.getNewMatcher(), dialog.getPatternType());
			update();
		}
	}

	@Override
	protected void okPressed() {
		SyncManager.saveFileFilter(project, filter);
		setReturnCode(OK);
		close();
	}

	/**
	 * Simple pattern editor that uses reflection and assumes that only the pattern's string needs to be edited.
	 * This will need to be more general if we add more pattern types in the future. Specifically, the logic of how to edit a
	 * pattern should be inside the specific matcher.
	 */
	private class SimpleEditPatternDialog extends Dialog {
		private final ResourceMatcher oldMatcher;
		private ResourceMatcher newMatcher;
		private PatternType patternType;
		private String pattern;
		private String matcherType;

		private Text patternText;
		private Label errorLabel;

		public SimpleEditPatternDialog(Shell parentShell, ResourceMatcher rm) {
			super(parentShell);
			oldMatcher = rm;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			initializeDialogUnits(parent);

			Composite main = (Composite) super.createDialogArea(parent);
			main.setLayout(new GridLayout());
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.widthHint = 300;
			main.setLayoutData(data);

			Composite patternComp = new Composite(main, SWT.NONE);
			patternComp.setLayout(new GridLayout(2, false));
			patternComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			Label label = new Label(patternComp, SWT.NONE);
			label.setText(Messages.NewSyncFileFilterPage_Pattern_label);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

			patternText = new Text(patternComp, SWT.BORDER);
			patternText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			patternText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					pattern = patternText.getText().trim();
				}
			});

			Group matcherTypeGroup = new Group(main, SWT.NONE);
			matcherTypeGroup.setText(Messages.NewSyncFileFilterPage_Matcher_Type);
			matcherTypeGroup.setLayout(new GridLayout(3, false));
			matcherTypeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			Button pathTypeButton = new Button(matcherTypeGroup, SWT.RADIO);
			pathTypeButton.setText("Path"); //$NON-NLS-1$
			pathTypeButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			pathTypeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					matcherType = PATH_MATCHER;
				}
			});

			Button regexTypeButton = new Button(matcherTypeGroup, SWT.RADIO);
			regexTypeButton.setText("Regex"); //$NON-NLS-1$
			regexTypeButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			regexTypeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					matcherType = REGEX_MATCHER;
				}
			});

			Button wildcardTypeButton = new Button(matcherTypeGroup, SWT.RADIO);
			wildcardTypeButton.setText("Wildcard"); //$NON-NLS-1$
			wildcardTypeButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			wildcardTypeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					matcherType = WILDCARD_MATCHER;
				}
			});

			Group patternTypeGroup = new Group(main, SWT.NONE);
			patternTypeGroup.setText(Messages.NewSyncFileFilterPage_Pattern_Type);
			patternTypeGroup.setLayout(new GridLayout(2, false));
			patternTypeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			Button inclusiveButton = new Button(patternTypeGroup, SWT.RADIO);
			inclusiveButton.setText(Messages.NewSyncFileFilterPage_Include);
			inclusiveButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			inclusiveButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					patternType = PatternType.INCLUDE;
				}
			});

			Button exclusiveButton = new Button(patternTypeGroup, SWT.RADIO);
			exclusiveButton.setText(Messages.NewSyncFileFilterPage_Exclude);
			exclusiveButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			exclusiveButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					patternType = PatternType.EXCLUDE;
				}
			});

			errorLabel = new Label(main, SWT.NONE);
			errorLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
			errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			regexTypeButton.setSelection(false);
			wildcardTypeButton.setSelection(false);
			if (oldMatcher != null) {
				patternText.setText(oldMatcher.toString());
				patternType = filter.getPatternType(oldMatcher);
				inclusiveButton.setSelection(patternType == PatternType.INCLUDE);
				exclusiveButton.setSelection(patternType == PatternType.EXCLUDE);
				pathTypeButton.setSelection(false);
				matcherType = oldMatcher.getType();
				if (oldMatcher.getType().equals(PATH_MATCHER)) {
					pathTypeButton.setSelection(true);
				} else if (oldMatcher.getType().equals(REGEX_MATCHER)) {
					regexTypeButton.setSelection(true);
				} else if (oldMatcher.getType().equals(WILDCARD_MATCHER)) {
					wildcardTypeButton.setSelection(true);
				}
			} else {
				patternType = PatternType.EXCLUDE;
				matcherType = PATH_MATCHER;
				inclusiveButton.setSelection(false);
				exclusiveButton.setSelection(true);
				pathTypeButton.setSelection(true);
			}

			return main;
		}

		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			if (oldMatcher == null) {
				shell.setText(Messages.NewSyncFileFilterPage_Add_Pattern);
			} else {
				shell.setText(Messages.NewSyncFileFilterPage_Edit_pattern);
			}
		}

		@Override
		protected void okPressed() {
			try {
				if (matcherType.equals(PATH_MATCHER)) {
					newMatcher = SyncFileFilter.getPathResourceMatcher(pattern);
				} else if (matcherType.equals(REGEX_MATCHER)) {
					newMatcher = SyncFileFilter.getRegexResourceMatcher(pattern);
				} else if (matcherType.equals(WILDCARD_MATCHER)) {
					newMatcher = SyncFileFilter.getWildcardResourceMatcher(pattern);
				}
			} catch (IllegalArgumentException e) {
				errorLabel.setText(e.getLocalizedMessage());
				display.timerExec(ERROR_DISPLAY_SECONDS * 1000, new Runnable() {
					@Override
					public void run() {
						if (errorLabel.isDisposed()) {
							return;
						}
						errorLabel.setText(""); //$NON-NLS-1$
					}
				});
				return;
			}
			super.okPressed();
		}

		public PatternType getPatternType() {
			return patternType;
		}

		public ResourceMatcher getNewMatcher() {
			return newMatcher;
		}
	}

	private void update() {
		patternTable.removeAll();
		for (ResourceMatcher pattern : filter.getPatterns()) {
			TableItem ti = new TableItem(patternTable, SWT.LEFT);
			ti.setData(pattern);

			String[] tableValues = new String[3];
			tableValues[0] = pattern.toString();
			if (filter.getPatternType(pattern) == PatternType.EXCLUDE) {
				tableValues[1] = Messages.NewSyncFileFilterPage_exclude;
			} else {
				tableValues[1] = Messages.NewSyncFileFilterPage_include;
			}
			tableValues[2] = pattern.getType();

			ti.setText(tableValues);
		}

		patternTable.getColumn(0).pack();
		patternTable.getColumn(1).pack();
		patternTable.getColumn(2).pack();

		if (project != null) {
			boolean showRemote = showRemoteButton.getSelection();
			if (showRemote) {
				if (!((SFTTreeContentProvider) treeViewer.getContentProvider()).isConnected()) {
					showRemote = false;
					remoteErrorLabel.setText(Messages.NewSyncFileFilterPage_Remote_is_disconnected);
				} else {
					remoteErrorLabel.setText(""); //$NON-NLS-1$
				}
			}
			showRemoteButton.setSelection(showRemote);
			((SFTTreeContentProvider) treeViewer.getContentProvider()).setShowRemoteFiles(showRemote);
			treeViewer.refresh();
		}
	}

	private class SFTTreeContentProvider implements ITreeContentProvider {
		private final RemoteContentProvider remoteFiles;
		private boolean showRemoteFiles = false;

		public SFTTreeContentProvider() {
			SyncConfig config = SyncConfigManager.getActive(project);
			if (config == null) {
				// System error handled by BuildConfigurationManager
				remoteFiles = null;
			} else {
				RemoteContentProvider tmpRCP;
				try {
					tmpRCP = new RemoteContentProvider(config.getRemoteConnection(), new Path(config.getLocation(project)), project);
				} catch (MissingConnectionException e) {
					tmpRCP = null;
				}
				remoteFiles = tmpRCP;
			}
		}

		/**
		 * Get whether remote files are displayed
		 */
		public boolean getShowRemoteFiles() {
			return showRemoteFiles;
		}

		/**
		 * Set displaying of remote files
		 * 
		 * @param b
		 */
		public void setShowRemoteFiles(boolean b) {
			showRemoteFiles = b;
		}

		/**
		 * Gets the children of the specified object
		 * 
		 * @param element
		 *            the parent object
		 * @return Object[]
		 */
		@Override
		public Object[] getChildren(Object element) {
			ArrayList<IResource> children = new ArrayList<IResource>();

			if (element instanceof IFolder) {
				if (((IFolder) element).isAccessible()) {
					try {
						for (IResource localChild : ((IFolder) element).members()) {
							children.add(localChild);
						}
					} catch (CoreException e) {
						assert (false); // This should never happen, since we check for existence before accessing the folder.
					}
				}

				if (showRemoteFiles && remoteFiles != null) {
					for (Object remoteChild : remoteFiles.getChildren(element)) {
						this.addUniqueResource(children, (IResource) remoteChild);
					}
				}
			}

			return children.toArray();
		}

		/**
		 * Gets the parent of the specified object
		 * 
		 * @param element
		 *            the object
		 * @return Object
		 */
		@Override
		public Object getParent(Object element) {
			return ((IResource) element).getParent();
		}

		/**
		 * Returns whether the passed object has children
		 * 
		 * @param element
		 *            the parent object private class SFTStyledCellLabelProvider extends
		 * @return boolean
		 */
		@Override
		public boolean hasChildren(Object element) {
			// Get the children
			Object[] obj = getChildren(element);

			// Return whether the parent has children
			return obj == null ? false : obj.length > 0;
		}

		/**
		 * Gets the root element(s) of the tree
		 * This code is very similar to "getChildren" but the root of the project tree requires special handling (no IFolder
		 * for the root).
		 * 
		 * @param element
		 *            the input data
		 * @return Object[]
		 */
		@Override
		public Object[] getElements(Object element) {
			ArrayList<IResource> children = new ArrayList<IResource>();

			if (element instanceof IProject && ((IProject) element).isAccessible()) {
				try {
					for (IResource localChild : ((IProject) element).members()) {
						children.add(localChild);
					}
				} catch (CoreException e) {
					assert (false); // This should never happen, since we check for existence before accessing the project.
				}

				if (showRemoteFiles && remoteFiles != null) {
					for (Object remoteChild : remoteFiles.getElements(element)) {
						this.addUniqueResource(children, (IResource) remoteChild);
					}
				}
			}

			return children.toArray();
		}

		/**
		 * Disposes any created resources
		 */
		@Override
		public void dispose() {
			// Nothing to dispose
		}

		/**
		 * Called when the input changes
		 * 
		 * @param element
		 *            the viewer
		 * @param arg1
		 *            the old input
		 * @param arg2
		 *            the new input
		 */
		@Override
		public void inputChanged(Viewer element, Object arg1, Object arg2) {
			// Nothing to do
		}

		// Utility function to add resources to a list only if it is unique.
		// Returns whether the resource was added.
		private boolean addUniqueResource(Collection<IResource> resList, IResource newRes) {
			for (IResource res : resList) {
				if (res.getProjectRelativePath().equals(newRes.getProjectRelativePath())) {
					return false;
				}
			}

			resList.add(newRes);
			return true;
		}

		/**
		 * Check that connection is still open - useful for client to inform user when the connection goes down.
		 * 
		 * @return whether connection is still open
		 */
		public boolean isConnected() {
			if (remoteFiles == null) {
				return false;
			} else {
				return remoteFiles.isOpen();
			}
		}
	}

	private class SFTTreeLabelProvider implements ILabelProvider {
		// Images for tree nodes
		private final Image folderImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		private final Image fileImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);

		/**
		 * Gets the image to display for a node in the tree
		 * 
		 * @param element
		 *            the node
		 * @return Image
		 */
		@Override
		public Image getImage(Object element) {
			if (element instanceof IFolder) {
				return folderImage;
			} else {
				return fileImage;
			}
		}

		/**
		 * Gets the text to display for a node in the tree
		 * 
		 * @param element
		 *            the node
		 * @return String
		 */
		@Override
		public String getText(Object element) {
			return ((IResource) element).getName();
		}

		/**
		 * Called when this LabelProvider is being disposed
		 */
		@Override
		public void dispose() {
			// Nothing to dispose
		}

		/**
		 * Returns whether changes to the specified property on the specified
		 * element would affect the label for the element
		 * 
		 * @param element
		 *            the element
		 * @param arg1
		 *            the property
		 * @return boolean
		 */
		@Override
		public boolean isLabelProperty(Object element, String arg1) {
			return false;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
			// Listeners not supported

		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// Listeners not supported
		}
	}

	// This is a hack to implement multiple inheritance so that this class can also serve as a preference page.

	// First, define a preference page inner class and any needed methods
	// Scenario 1 Preference page
	public class SyncFilePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
		public SyncFilePreferencePage() {
			super();
			super.noDefaultAndApplyButton();
		}

		@Override
		protected Control createContents(Composite parent) {
			return NewSyncFileFilterPage.this.createContents(parent);
		}

		@Override
		public boolean performOk() {
			SyncManager.saveDefaultFileFilter(filter);
			return true;
		}

		@Override
		public void init(IWorkbench workbench) {
			// nothing to do
		}

	}

	private void colorComposite(Composite comp, int color) {
		if (DEBUG) {
			// color e.g. SWT.COLOR_RED
			org.eclipse.swt.graphics.Color gcolor = Display.getCurrent().getSystemColor(color);
			comp.setBackground(gcolor);
		}

	}
}