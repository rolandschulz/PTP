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
package org.eclipse.ptp.rdt.sync.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.internal.rdt.sync.ui.SyncImages;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.PathResourceMatcher;
import org.eclipse.ptp.rdt.sync.core.RemoteContentProvider;
import org.eclipse.ptp.rdt.sync.core.ResourceMatcher;
import org.eclipse.ptp.rdt.sync.core.RegexResourceMatcher;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter.PatternType;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.WildcardResourceMatcher;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
 * Window or Preference page to display a File tree and other options, where users can select the patterns/files to be sync'ed.<br>
 * This class is used for three scenarios:<br>
 * 1. Preference Page - to specify default settings<br>
 * 2. Filter page for new sync project - to specify settings for new project<br>
 * 3. Filter page for existing project - to alter existing Sync Filter settings<br>
 * 
 * Uses ResourceMatchers (e.g. PathResourceMatcher, RegexResourceMatcher, and WildcardResourceMatcher) to match the
 * "patterns" entered to actual files in the project.
 */
// FIXME for case 3. existing project, initial dialog size is too tall and narrow,
// and not enough room for pattern view
// FIXME2 make OK/cancel buttons same size
public class SyncFileFilterPage extends ApplicationWindow implements IWorkbenchPreferencePage {
	private static final int ERROR_DISPLAY_SECONDS = 3;
	private static final Display display = Display.getCurrent();

	private final IProject project;
	private final SyncFileFilter filter;
	private final FilterSaveTarget saveTarget;
	private CheckboxTreeViewer treeViewer;
	private Table patternTable;
	private Button showRemoteButton;
	private Label remoteErrorLabel;
	private Button upButton;
	private Button downButton;
	private Button editButton;
	private Button removeButton;
	private Text newPath;
	private Button excludeButtonForPath;
	private Button includeButtonForPath;
	private Text newRegex;
	private Button excludeButtonForRegex;
	private Button includeButtonForRegex;
	private Text newWildcard;
	private Button excludeButtonForWildcard;
	private Button includeButtonForWildcard;
	private Label patternErrorLabel;
	private Button cancelButton;
	private Button okButton;
	/** Boolean to help tell which boolean arguments do what */
	static final boolean GRAB_EXCESS = true;
	/** Boolean to turn on funky colors to debug which composites contain what */
	static final boolean DEBUG = false;

	/**
	 * Where to save the filter information - as default (preferences), or for the current project
	 * 
	 */
	private enum FilterSaveTarget {
		NONE, DEFAULT, PROJECT
	}

	/**
	 * Default constructor creates a preference page to alter the default filter and should never be called by clients. Instead,
	 * use the open methods to create a standalone GUI.
	 */
	public SyncFileFilterPage() {
		this(null, true, null, null);
	}

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
	private SyncFileFilterPage(IProject p, boolean isPreferencePage, SyncFileFilter targetFilter, Shell parent) {
		super(parent);
		project = p;
		if (isPreferencePage) {
			// scenario 1 Preference page
			preferencePage = new SyncFilePreferencePage();
		} else {
			preferencePage = null;
		}

		if (targetFilter == null) {
			if (project == null) {
				// scenario 1: preference page
				filter = SyncManager.getDefaultFileFilter();
				saveTarget = FilterSaveTarget.DEFAULT;
			} else {
				// scenario 2/3: project (new or existing)
				filter = SyncManager.getFileFilter(project);
				saveTarget = FilterSaveTarget.PROJECT;
			}
		} else {
			filter = targetFilter;
			saveTarget = FilterSaveTarget.NONE;
		}

		this.setReturnCode(CANCEL);
	}

	/**
	 * Open a standalone GUI to change the filter of the passed project. Pass null for existing targetFilter to alter the default
	 * filter.
	 * 
	 * @param p
	 *            project
	 * @param parent
	 *            the parent shell
	 * @return open return code
	 */
	public static int open(IProject p, Shell parent) {
		return new SyncFileFilterPage(p, false, null, parent).open();
	}

	/**
	 * Open a standalone GUI (window) to change the passed filter. This is useful for the new project wizard and other places where
	 * the
	 * filter does not yet have a context. This method blocks, as it assumes the client wants to wait for the filter changes.
	 * 
	 * The client most likely should pass a copy of the filter to be altered, and then check the return code for OK or Cancel
	 * to decide if the copy should be kept.
	 * 
	 * Passing null to constructor's project argument alters the default filter instead of filter for a specific project
	 * 
	 * @param f
	 *            a sync file filter that will be modified
	 * @param parent
	 *            the parent shell
	 * @return open return code
	 */
	public static int openBlocking(SyncFileFilter f, Shell parent) {
		SyncFileFilterPage page = new SyncFileFilterPage(null, false, f, parent);
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
		if (project == null) {
			shell.setText(Messages.SyncFileFilterPage_Default_Include_exclude_title);
		} else {
			shell.setText(Messages.SyncFileFilterPage_Include_exclude_title);
		}
	}

	/**
	 * Creates the main window's contents
	 * 
	 * @param parent
	 *            the main window
	 * @return Control
	 */

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		gl.verticalSpacing = 20;
		composite.setLayout(gl);
		GridData gd;
		GridData gdcomp;

		// To edit existing filters for a project, create the tree viewer
		if (project != null) {
			// Composite for tree viewer
			Composite treeViewerComposite = new Composite(composite, SWT.BORDER);
			GridLayout treeLayout = new GridLayout(2, false);
			treeLayout.verticalSpacing = 5;// back to default spacing so no excess space around 'show remote files'
			treeViewerComposite.setLayout(treeLayout);

			gdcomp = new GridData(SWT.FILL, SWT.FILL, GRAB_EXCESS, GRAB_EXCESS);
			if (DEBUG)
				colorComposite(treeViewerComposite, SWT.COLOR_RED);

			treeViewerComposite.setLayoutData(gdcomp);

			// Label for file tree viewer
			Label treeViewerLabel = new Label(treeViewerComposite, SWT.NONE);
			treeViewerLabel.setText(Messages.SyncFileFilterPage_File_view);
			gd = new GridData(SWT.LEAD, SWT.CENTER, false, false, 2, 1);
			treeViewerLabel.setLayoutData(gd);
			// this.formatAsHeader(treeViewerLabel);// use SWT.BOLD instead

			// File tree viewer
			treeViewer = new CheckboxTreeViewer(treeViewerComposite);
			gd = new GridData(SWT.FILL, SWT.FILL, GRAB_EXCESS, GRAB_EXCESS, 2, 1);
			treeViewer.getTree().setLayoutData(gd);
			treeViewer.setContentProvider(new SFTTreeContentProvider());
			treeViewer.setLabelProvider(new SFTTreeLabelProvider());
			treeViewer.setCheckStateProvider(new ICheckStateProvider() {
				public boolean isChecked(Object element) {
					if (filter.shouldIgnore((IResource) element)) {
						return false;
					} else {
						return true;
					}
				}

				public boolean isGrayed(Object element) {
					return false;
				}
			});
			treeViewer.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					IPath path = ((IResource) (event.getElement())).getProjectRelativePath();
					if (event.getChecked()) {
						filter.addPattern(new PathResourceMatcher(path), PatternType.INCLUDE);
					} else {
						filter.addPattern(new PathResourceMatcher(path), PatternType.EXCLUDE);
					}

					update();
				}
			});
			treeViewer.setInput(project);

			showRemoteButton = new Button(treeViewerComposite, SWT.CHECK);
			showRemoteButton.setText(Messages.SyncFileFilterPage_Show_Remote_Files);
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
		Composite patternTableComposite = new Composite(composite, SWT.BORDER);
		if (DEBUG) {
			colorComposite(patternTableComposite, SWT.COLOR_BLUE);
		}

		GridLayout patternTableLayout = new GridLayout(2, false);
		patternTableLayout.verticalSpacing = 5;
		patternTableComposite.setLayout(patternTableLayout);

		gdcomp = new GridData(SWT.FILL, SWT.TOP, GRAB_EXCESS, GRAB_EXCESS);
		// patternTableComposite.setLayoutData(new GridData(windowWidth, viewHeight));
		patternTableComposite.setLayoutData(gdcomp);

		// Label for pattern table
		Label patternTableLabel = new Label(patternTableComposite, SWT.NONE);
		patternTableLabel.setText(Messages.SyncFileFilterPage_Pattern_View);
		this.formatAsHeader(patternTableLabel);
		gd = new GridData(SWT.LEAD, SWT.CENTER, false, false, 4, 1);// LEFT==LEAD
		patternTableLabel.setLayoutData(gd);

		// Pattern table
		patternTable = new Table(patternTableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, GRAB_EXCESS, GRAB_EXCESS, 1, 4); // room for 4 buttons at the right
		patternTable.setLayoutData(gd);
		new TableColumn(patternTable, SWT.LEAD, 0);
		new TableColumn(patternTable, SWT.LEAD, 1); // Separate column for pattern for alignment and font change.
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

		// Pattern table buttons (up, down, edit, remove)
		upButton = new Button(patternTableComposite, SWT.PUSH);
		upButton.setText(Messages.SyncFileFilterPage_Up);
		upButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		upButton.addSelectionListener(new SelectionAdapter() {
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
			}
		});

		downButton = new Button(patternTableComposite, SWT.PUSH);
		downButton.setText(Messages.SyncFileFilterPage_Down);
		downButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		downButton.addSelectionListener(new SelectionAdapter() {
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
			}
		});

		editButton = new Button(patternTableComposite, SWT.PUSH);
		editButton.setText(Messages.SyncFileFilterPage_Edit);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				editPattern();
			}
		});

		removeButton = new Button(patternTableComposite, SWT.PUSH);
		removeButton.setText(Messages.SyncFileFilterPage_Remove);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				TableItem[] selectedPatternItems = patternTable.getSelection();
				for (TableItem selectedPatternItem : selectedPatternItems) {
					ResourceMatcher selectedPattern = (ResourceMatcher) selectedPatternItem.getData();
					filter.removePattern(selectedPattern);
				}
				update();
			}
		});

		// Composite for text box, combo, and buttons to enter a new pattern
		createPatternEnterComposite(composite);

		// Cancel and OK buttons
		if (preferencePage == null) {
			createOKcancelButtons(composite);
		}

		update();
		return composite;
	}

	private void createPatternEnterComposite(Composite composite) {
		GridData gdcomp;
		Composite patternEnterComposite = new Composite(composite, SWT.NONE);
		patternEnterComposite.setLayout(new GridLayout(4, false));
		gdcomp = new GridData(SWT.FILL, SWT.TOP, GRAB_EXCESS, false);
		patternEnterComposite.setLayoutData(gdcomp);
		if (DEBUG) {
			colorComposite(patternEnterComposite, SWT.COLOR_GREEN);
		}

		// Label for entering new path
		Label pathLabel=new Label(patternEnterComposite, SWT.NONE);
		pathLabel.setText(Messages.SyncFileFilterPage_Enter_Path);
		pathLabel.setToolTipText(Messages.SyncFileFilterPage_EnterPathOrFile+Messages.SyncFileFilterPage_toExcludeOrInclude);
		
		// Text box to enter new path
		newPath = new Text(patternEnterComposite, SWT.NONE);
		newPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, GRAB_EXCESS, false));

		// Submit buttons (exclude and include)
		excludeButtonForPath = new Button(patternEnterComposite, SWT.PUSH | SWT.FLAT);
		excludeButtonForPath.setImage(SyncImages.get(SyncImages.EXCLUDE));
		excludeButtonForPath.setToolTipText(Messages.SyncFileFilterPage_Exclude);
		excludeButtonForPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		excludeButtonForPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				enterNewPathPattern(PatternType.EXCLUDE);
			}
		});

		includeButtonForPath = new Button(patternEnterComposite, SWT.PUSH | SWT.FLAT);
		includeButtonForPath.setImage(SyncImages.get(SyncImages.INCLUDE));
		includeButtonForPath.setToolTipText(Messages.SyncFileFilterPage_Include);
		includeButtonForPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		includeButtonForPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				enterNewPathPattern(PatternType.INCLUDE);
			}
		});

		// Label for entering new regex
		Label regexLabel=new Label(patternEnterComposite, SWT.NONE);
		regexLabel.setText(Messages.SyncFileFilterPage_Enter_Regex);
		regexLabel.setToolTipText(Messages.SyncFileFilterPage_EnterRegex+Messages.SyncFileFilterPage_toExcludeOrInclude);

		// Text box to enter new regex
		newRegex = new Text(patternEnterComposite, SWT.NONE);
		newRegex.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Submit buttons (exclude and include)
		excludeButtonForRegex = new Button(patternEnterComposite, SWT.PUSH | SWT.FLAT);
		excludeButtonForRegex.setImage(SyncImages.get(SyncImages.EXCLUDE));
		excludeButtonForRegex.setToolTipText(Messages.SyncFileFilterPage_Exclude);
		excludeButtonForRegex.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		excludeButtonForRegex.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				enterNewRegexPattern(PatternType.EXCLUDE);
			}
		});

		includeButtonForRegex = new Button(patternEnterComposite, SWT.PUSH | SWT.FLAT);
		includeButtonForRegex.setImage(SyncImages.get(SyncImages.INCLUDE));
		includeButtonForRegex.setToolTipText(Messages.SyncFileFilterPage_Include);
		includeButtonForRegex.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		includeButtonForRegex.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				enterNewRegexPattern(PatternType.INCLUDE);
			}
		});
		
		// Label for entering new wildcard
		Label wildcardLabel=new Label(patternEnterComposite, SWT.NONE);
		wildcardLabel.setText(Messages.SyncFileFilterPage_Enter_Wildcard);
		wildcardLabel.setToolTipText(Messages.SyncFileFilterPage_EnterWildcard+Messages.SyncFileFilterPage_toExcludeOrInclude);
		
		// Text box to enter new glob/wildcard
		newWildcard = new Text(patternEnterComposite, SWT.NONE);
		newWildcard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Submit buttons (exclude and include)
		excludeButtonForWildcard = new Button(patternEnterComposite, SWT.PUSH | SWT.FLAT);
		excludeButtonForWildcard.setImage(SyncImages.get(SyncImages.EXCLUDE));
		excludeButtonForWildcard.setToolTipText(Messages.SyncFileFilterPage_Exclude);
		excludeButtonForWildcard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		excludeButtonForWildcard.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				enterNewWildcardPattern(PatternType.EXCLUDE);
			}
		});

		includeButtonForWildcard = new Button(patternEnterComposite, SWT.PUSH | SWT.FLAT);
		includeButtonForWildcard.setImage(SyncImages.get(SyncImages.INCLUDE));
		includeButtonForWildcard.setToolTipText(Messages.SyncFileFilterPage_Include);
		includeButtonForWildcard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		includeButtonForWildcard.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				enterNewWildcardPattern(PatternType.INCLUDE);
			}
		});

		// Place for displaying error message if pattern is illegal
		patternErrorLabel = new Label(patternEnterComposite, SWT.NONE);
		patternErrorLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
		patternErrorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
	}

	/**
	 * Create composite with OK and Cancel buttons<br>
	 * Note: since this isn't using the standard OK/Cancel dialog from the platform, the buttons
	 * may be in the wrong place for some platforms
	 * 
	 * @param composite
	 */
	private void createOKcancelButtons(Composite composite) {
		GridData gdcomp;
		Composite okCancelComposite1 = new Composite(composite, SWT.NONE);
		okCancelComposite1.setLayout(new GridLayout(2, false));
		gdcomp = new GridData(SWT.FILL, SWT.TOP, GRAB_EXCESS, false);
		okCancelComposite1.setLayoutData(gdcomp);
		if (DEBUG) {
			colorComposite(okCancelComposite1, SWT.COLOR_GRAY);
		}
		// Separator
		Label horizontalLine = new Label(okCancelComposite1, SWT.SEPARATOR | SWT.HORIZONTAL);
		horizontalLine.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, GRAB_EXCESS, false, 2/* 4 */, 1));

		Composite okCancelComposite = new Composite(okCancelComposite1, SWT.NONE);
		okCancelComposite.setLayout(new GridLayout(2, false));
		gdcomp = new GridData(SWT.RIGHT, SWT.TOP, GRAB_EXCESS, false);
		okCancelComposite.setLayoutData(gdcomp);
		if (DEBUG) {
			colorComposite(okCancelComposite, SWT.COLOR_CYAN);
		}

		// Cancel button
		cancelButton = new Button(okCancelComposite, SWT.PUSH);
		cancelButton.setText(Messages.SyncFileFilterPage_Cancel);
		cancelButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				SyncFileFilterPage.this.close();
			}
		});

		// OK button
		okButton = new Button(okCancelComposite, SWT.PUSH);
		okButton.setText(Messages.SyncFileFilterPage_OK);
		okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (saveTarget == FilterSaveTarget.DEFAULT) {
					// scenario 1: preference page
					SyncManager.saveDefaultFileFilter(filter);
				} else if (saveTarget == FilterSaveTarget.PROJECT) {
					// scenario 2/3: new or existing project
					assert (project != null);
					SyncManager.saveFileFilter(project, filter);
				} else {
					// Nothing to do
				}
				SyncFileFilterPage.this.setReturnCode(OK);
				SyncFileFilterPage.this.close();
			}
		});
	}

	/*
	 * Utility function to bold and resize labels to be headers
	 */
	private void formatAsHeader(Label widget) {
		FontData[] fontData = widget.getFont().getFontData();
		for (FontData data : fontData) {
			data.setStyle(SWT.BOLD);
		}

		final Font newFont = new Font(display, fontData);
		widget.setFont(newFont);
		widget.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				newFont.dispose();
			}
		});
	}

	private void enterNewPathPattern(PatternType type) {
		String pattern = newPath.getText();
		if (pattern.isEmpty()) {
			return;
		}

		PathResourceMatcher matcher = null;
		matcher = new PathResourceMatcher(new Path(pattern));
		filter.addPattern(matcher, type);

		newPath.setText(""); //$NON-NLS-1$
		update();
	}

	private void enterNewRegexPattern(PatternType type) {
		String pattern = newRegex.getText();
		if (pattern.isEmpty()) {
			return;
		}

		RegexResourceMatcher matcher = null;
		try {
			matcher = new RegexResourceMatcher(pattern);
		} catch (PatternSyntaxException e) {
			// Do nothing but display an error message for a few seconds
			patternErrorLabel.setText(Messages.SyncFileFilterPage_Invalid_Regular_Expression);
			display.timerExec(ERROR_DISPLAY_SECONDS * 1000, new Runnable() {
				public void run() {
					if (patternErrorLabel.isDisposed()) {
						return;
					}
					patternErrorLabel.setText(""); //$NON-NLS-1$
				}
			});
			return;
		}

		filter.addPattern(matcher, type);

		newRegex.setText(""); //$NON-NLS-1$
		update();
	}
	private void enterNewWildcardPattern(PatternType type) { 
		String pattern = newWildcard.getText();
		if (pattern.isEmpty()) {
			return;
		}

		WildcardResourceMatcher matcher = null;
		matcher = new WildcardResourceMatcher(pattern);  
		filter.addPattern(matcher, type);

		newWildcard.setText(""); //$NON-NLS-1$
		update();
	}

	/** Creates a modal dialog to edit the selected pattern and replaces it if user hits "OK" */
	private void editPattern() {
		TableItem[] selectedPatternItem = patternTable.getSelection();
		// Modifying more than one pattern at a time is not supported
		if (selectedPatternItem.length != 1) {
			return;
		}
		ResourceMatcher selectedPattern = (ResourceMatcher) selectedPatternItem[0].getData();
		SimpleEditPatternDialog dialog = new SimpleEditPatternDialog(selectedPattern, patternTable.getShell());
		if (dialog.open() == Window.OK) {
			filter.replacePattern(selectedPattern, dialog.getNewMatcher(),
					dialog.getIsInclusive() ? PatternType.INCLUDE : PatternType.EXCLUDE);
			update();
		}
	}

	/**
	 * Simple pattern editor that uses reflection and assumes that only the pattern's string needs to be edited.
	 * This will need to be more general if we add more pattern types in the future. Specifically, the logic of how to edit a
	 * pattern should be inside the specific matcher.
	 */
	private class SimpleEditPatternDialog extends Dialog {
		final ResourceMatcher oldMatcher;
		ResourceMatcher newMatcher = null;
		boolean isInclusive;
		Button checkBox;
		Text patternText;
		Label errorLabel;

		public SimpleEditPatternDialog(ResourceMatcher rm, Shell parentShell) {
			super(parentShell);
			oldMatcher = rm;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			checkBox = new Button(parent, SWT.CHECK);
			checkBox.setSelection(filter.getPatternType(oldMatcher) == PatternType.INCLUDE);
			checkBox.setText(Messages.SyncFileFilterPage_Inclusive);
			GridData gd = new GridData();
			checkBox.setLayoutData(gd);

			patternText = new Text(parent, SWT.BORDER);
			gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			patternText.setLayoutData(gd);
			patternText.setText(oldMatcher.toString());

			errorLabel = new Label(parent, SWT.NONE);
			errorLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
			errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			return parent;
		}

		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText(Messages.SyncFileFilterPage_Edit_Pattern);
		}

		@Override
		protected void okPressed() {
			isInclusive = checkBox.getSelection();
			if (oldMatcher instanceof PathResourceMatcher) {
				newMatcher = new PathResourceMatcher(new Path(patternText.getText()));
			} else if (oldMatcher instanceof RegexResourceMatcher) {
				try {
					newMatcher = new RegexResourceMatcher(patternText.getText());
					// If regex invalid, display error for a few seconds and then return
				} catch (PatternSyntaxException e) {
					errorLabel.setText(Messages.SyncFileFilterPage_Invalid_Regular_Expression);
					display.timerExec(ERROR_DISPLAY_SECONDS * 1000, new Runnable() {
						public void run() {
							if (errorLabel.isDisposed()) {
								return;
							}
							errorLabel.setText(""); //$NON-NLS-1$
						}
					});
					return;
				}
			} else {
				assert false : Messages.SyncFileFilterPage_Attempt_to_edit_unsupported;
			}
			super.okPressed();
		}

		public boolean getIsInclusive() {
			return isInclusive;
		}

		public ResourceMatcher getNewMatcher() {
			return newMatcher;
		}
	}

	private void update() {
		patternTable.removeAll();
		for (ResourceMatcher pattern : filter.getPatterns()) {
			TableItem ti = new TableItem(patternTable, SWT.LEAD);
			ti.setData(pattern);

			String[] tableValues = new String[2];
			String patternType;
			if (filter.getPatternType(pattern) == PatternType.EXCLUDE) {
				patternType = Messages.SyncFileFilterPage_Exclude;
			} else {
				patternType = Messages.SyncFileFilterPage_Include;
			}
			if (pattern instanceof PathResourceMatcher) {
				patternType = patternType + " " + Messages.SyncFileFilterPage_path; //$NON-NLS-1$
			} else if (pattern instanceof RegexResourceMatcher) {
				patternType = patternType + " " + Messages.SyncFileFilterPage_regex; //$NON-NLS-1$

			} else if (pattern instanceof WildcardResourceMatcher) {
				patternType = patternType + " " + Messages.SyncFileFilterPage_wildcard; //$NON-NLS-1$
			}
			patternType += ":  "; //$NON-NLS-1$

			tableValues[0] = patternType;
			tableValues[1] = pattern.toString();
			ti.setText(tableValues);

			// Italicize pattern
			FontData currentFontData = ti.getFont().getFontData()[0];
			Font italicizedFont = new Font(display,
					new FontData(currentFontData.getName(), currentFontData.getHeight(), SWT.ITALIC));
			ti.setFont(1, italicizedFont);

		}

		patternTable.getColumn(0).pack();
		patternTable.getColumn(1).pack();

		if (project != null) {
			boolean showRemote = showRemoteButton.getSelection();
			if (showRemote) {
				if (!((SFTTreeContentProvider) treeViewer.getContentProvider()).isConnected()) {
					showRemote = false;
					remoteErrorLabel.setText(Messages.SyncFileFilterPage_Remote_is_disconnected);
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
			IConfiguration bconf = ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration();
			BuildScenario bs = BuildConfigurationManager.getInstance().getBuildScenarioForBuildConfiguration(bconf);
			if (bs == null) {
				// System error handled by BuildConfigurationManager
				remoteFiles = null;
			} else {
				RemoteContentProvider tmpRCP;
				try {
					tmpRCP = new RemoteContentProvider(bs.getRemoteConnection(), new Path(bs.getLocation(project)), project);
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
		public String getText(Object element) {
			return ((IResource) element).getName();
		}

		/**
		 * Called when this LabelProvider is being disposed
		 */
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
		public boolean isLabelProperty(Object element, String arg1) {
			return false;
		}

		public void addListener(ILabelProviderListener listener) {
			// Listeners not supported

		}

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

		protected Control createContents(Composite parent) {
			return SyncFileFilterPage.this.createContents(parent);
		}

		public boolean performOk() {
			SyncManager.saveDefaultFileFilter(filter);
			return true;
		}

		public void init(IWorkbench workbench) {
			// nothing to do
		}

	}

	// Second, define an instance of the preference page
	private final SyncFilePreferencePage preferencePage;

	// Finally, delegate all method calls for IWorkbenchPreferencePage to go through the preference page instance.
	// Note: NullPointerException results if the page is not created as a preference page.
	public Point computeSize() {
		return preferencePage.computeSize();
	}

	public boolean isValid() {
		return preferencePage.isValid();
	}

	public boolean okToLeave() {
		return preferencePage.okToLeave();
	}

	public boolean performCancel() {
		return preferencePage.performCancel();
	}

	public boolean performOk() {
		return preferencePage.performOk();
	}

	public void setContainer(IPreferencePageContainer preferencePageContainer) {
		preferencePage.setContainer(preferencePageContainer);
	}

	public void setSize(Point size) {
		preferencePage.setSize(size);
	}

	public void createControl(Composite parent) {
		preferencePage.createControl(parent);
	}

	public void dispose() {
		preferencePage.dispose();
	}

	public Control getControl() {
		return preferencePage.getControl();
	}

	public String getDescription() {
		return preferencePage.getDescription();
	}

	public String getErrorMessage() {
		return preferencePage.getErrorMessage();
	}

	public Image getImage() {
		return preferencePage.getImage();
	}

	public String getMessage() {
		return preferencePage.getMessage();
	}

	public String getTitle() {
		return preferencePage.getTitle();
	}

	public void performHelp() {
		preferencePage.performHelp();
	}

	public void setDescription(String description) {
		preferencePage.setDescription(description);
	}

	public void setImageDescriptor(ImageDescriptor image) {
		preferencePage.setImageDescriptor(image);
	}

	public void setTitle(String title) {
		preferencePage.setTitle(title);
	}

	public void setVisible(boolean visible) {
		preferencePage.setVisible(visible);
	}

	public void init(IWorkbench workbench) {
		preferencePage.init(workbench);
	}

	private void colorComposite(Composite comp, int color) {
		if (DEBUG) {
			// color e.g. SWT.COLOR_RED
			org.eclipse.swt.graphics.Color gcolor = Display.getCurrent().getSystemColor(color);
			comp.setBackground(gcolor);
		}

	}
}