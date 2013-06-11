/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.internal.gem.views;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.internal.gem.GemPlugin;
import org.eclipse.ptp.internal.gem.messages.Messages;
import org.eclipse.ptp.internal.gem.preferences.PreferenceConstants;
import org.eclipse.ptp.internal.gem.util.Envelope;
import org.eclipse.ptp.internal.gem.util.GemUtilities;
import org.eclipse.ptp.internal.gem.util.Transitions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * The GEM Analyzer View.
 */
public class GemBrowser extends ViewPart {

	// The ID for this view
	public static final String ID = "org.eclipse.ptp.gem.views.GemBrowser"; //$NON-NLS-1$

	// Data structures and more complex members
	private Transitions transitions;

	// Container objects and actions
	private Composite parent;
	private Action getHelpAction;
	private Action terminateOperationAction;
	private Action writeToLocalFileAction;

	// SWT widgets
	private Button runGemButton;
	private Combo setNumProcsComboList;
	private CTabFolder tabFolder;
	private CLabel summaryLabel;

	// Threads
	private Thread browserUpdateThread;
	private Thread clearBrowserThread;
	private Thread disableTerminateButtonThread;

	// Misc
	private int errorCount;
	private int warningCount;

	// Create a listener to allow "jumps" to the Editor
	SelectionListener listener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			String eventText = null;
			IFile sourceFile = null;
			Integer lineNumber = null;

			final Object o = event.getSource();
			if (o instanceof Tree) {
				final TreeItem selectionItem = (TreeItem) event.item;

				// Only child items will have a non-null parent
				// then see if child item text matches regex
				if (selectionItem.getParentItem() != null) {
					eventText = selectionItem.toString();
					final Pattern browserLinePattern = Pattern.compile("^(.+?)\\s+(.+?)\\s+(.+?)\\s+(.+?)\\s+(.+?)"); //$NON-NLS-1$
					final Matcher browserLineMatcher = browserLinePattern.matcher(eventText);

					if (browserLineMatcher.matches()) {
						final String filePathString = browserLineMatcher.group(3);
						String lineNumStr = browserLineMatcher.group(5);

						final Pattern lineNumberPattern = Pattern.compile("(^[0-9]+)?+(.+?)"); //$NON-NLS-1$
						final Matcher lineNumberMatcher = lineNumberPattern.matcher(lineNumStr);

						if (lineNumberMatcher.matches()) {
							lineNumStr = lineNumberMatcher.group(1);
							lineNumber = Integer.parseInt(lineNumStr);
							sourceFile = GemUtilities.getSourceFile(filePathString, GemUtilities.getProjectLogFile());
							if (sourceFile != null) {
								openEditor(lineNumber, sourceFile);
							} else {
								final String message = Messages.GemBrowser_0 + filePathString;
								GemUtilities.showErrorDialog(message);
								return;
							}
						}
					}
				}
			}
		}
	};

	/**
	 * Constructor.
	 * 
	 * @param none
	 */
	public GemBrowser() {
		super();
	}

	/**
	 * Brings this ViewPart to the front and gives it focus.
	 * 
	 * @param none
	 * @return none
	 */
	public void activate() {
		final Thread activationThread = new Thread() {
			@Override
			public void run() {
				final IWorkbench wb = PlatformUI.getWorkbench();
				final IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
				final IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					page.activate(GemBrowser.this);
				}
			}
		};

		// We need to switch to the thread that is allowed to change the UI
		Display.getDefault().syncExec(activationThread);
	}

	/**
	 * Runs a thread that clears the Browser view.
	 * 
	 * @param none
	 * @return void
	 */
	public void clear() {
		Display.getDefault().syncExec(this.clearBrowserThread);
	}

	/*
	 * Calls finer grained methods, populating the view action bar.
	 */
	private void contributeToActionBars() {
		final IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/*
	 * Creates a single string containing all information on the issues found.
	 * This string is formatted for easy parsing/reading
	 */
	private String createBrowserSummary() {
		String result = ""; //$NON-NLS-1$
		final String newline = System.getProperty("line.separator"); //$NON-NLS-1$
		try {
			final int tabCount = this.tabFolder.getItemCount();
			for (int tabIndex = 0; tabIndex < tabCount; tabIndex++) {
				final CTabItem currentTab = this.tabFolder.getItem(tabIndex);
				final String tabTitle = currentTab.getText();
				final Tree tree = (Tree) currentTab.getControl();
				result += tabTitle + newline;

				final int numInterleavings = tree.getItemCount();
				for (int interIndex = 0; interIndex < numInterleavings; interIndex++) {
					final TreeItem currInter = tree.getItem(interIndex);
					final String interleavingName = "\t" + currInter.getText() + newline; //$NON-NLS-1$

					result += interleavingName;
					final int itemCount = currInter.getItemCount();
					for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
						final TreeItem item = currInter.getItem(itemIndex);
						String line = item.getText() + newline;
						line = "\t\t" + line; //$NON-NLS-1$
						result += line;
					}
				}
				result += newline;
			}
		} catch (final Exception e) {
			GemUtilities.logExceptionDetail(e);
			result = Messages.GemBrowser_6;
		}
		return result;
	}

	/**
	 * Callback that allows us to create the viewer and initialize it.
	 * 
	 * @param parent
	 *            The parent Composite for this View.
	 * @return void
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;

		// Create layout for the parent Composite
		final GridLayout parentLayout = new GridLayout();
		parentLayout.numColumns = 1;
		parentLayout.marginHeight = 10;
		parentLayout.marginWidth = 10;
		parent.setLayout(parentLayout);

		// Creates groups and selection listeners
		createRuntimeComposite(parent);

		// create actions & connect to buttons, context menus and pull-downs
		createSelectionListeners();
		makeActions();
		contributeToActionBars();

		// Create the Tab Folder
		this.tabFolder = new CTabFolder(parent, SWT.TOP);
		this.tabFolder.setSimple(false);
		this.tabFolder.setSingle(false);
		this.tabFolder.setBorderVisible(true);
		this.tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createRuntimeComposite(Composite parent) {
		final Image trident = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/trident.gif")); //$NON-NLS-1$
		final Composite runtimeComposite = new Composite(parent, SWT.NULL);
		runtimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		runtimeComposite.setLayout(new FormLayout());

		// Call ISP button
		this.runGemButton = new Button(runtimeComposite, SWT.PUSH);
		this.runGemButton.setImage(trident);
		this.runGemButton.setToolTipText(Messages.GemBrowser_7);
		this.runGemButton.setEnabled(true);
		final FormData launchIspUIFormData = new FormData();
		launchIspUIFormData.right = new FormAttachment(100, -5);
		launchIspUIFormData.bottom = new FormAttachment(100, -5);
		this.runGemButton.setLayoutData(launchIspUIFormData);

		// Set number of processes button
		this.setNumProcsComboList = new Combo(runtimeComposite, SWT.DROP_DOWN);
		final Font setRankComboFont = setFontSize(this.setNumProcsComboList.getFont(), 9);
		this.setNumProcsComboList.setFont(setRankComboFont);
		final String[] items = new String[] {};
		this.setNumProcsComboList.setItems(items);
		this.setNumProcsComboList.setText(" "); //$NON-NLS-1$
		this.setNumProcsComboList.setToolTipText(Messages.GemBrowser_8);
		final FormData setRankFormData = new FormData();
		setRankFormData.width = 50;
		setRankFormData.right = new FormAttachment(this.runGemButton, -5);
		setRankFormData.bottom = new FormAttachment(100, -5);
		this.setNumProcsComboList.setLayoutData(setRankFormData);
		this.setNumProcsComboList.setEnabled(true);
		setNumProcItems();
		updateDropDown();

		// Add a label explaining how to use this view
		this.summaryLabel = new CLabel(runtimeComposite, SWT.BORDER_SOLID);
		this.summaryLabel.setRightMargin(275);
		final FormData labelData = new FormData();
		labelData.width = 200;
		labelData.right = new FormAttachment(this.setNumProcsComboList, -5);
		labelData.bottom = new FormAttachment(0, 0);
		this.setNumProcsComboList.setLayoutData(setRankFormData);
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createSelectionListeners() {
		// To conveniently run GEM from this view
		this.runGemButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
				URI inputLocation = null;
				IPath path = null;

				try {
					inputLocation = new URI(pstore.getString(PreferenceConstants.GEM_PREF_MOST_RECENT_FILE));
				} catch (final URISyntaxException e) {
					GemUtilities.logExceptionDetail(e);
				}
				if (inputLocation != null) {
					path = new Path(inputLocation.getPath());
				}
				if (!GemUtilities.isProjectActive()) {
					GemUtilities.setTaskStatus(GemUtilities.TaskStatus.ABORTED);
					return;
				}
				final IFile file = GemUtilities.getCurrentProject().getFile(path.lastSegment());
				path = file.getFullPath();
				final String extension = file.getFileExtension();
				boolean isSourceFile = false;

				if (extension != null) {
					// The most common C & C++ source file extensions
					isSourceFile = extension.equals("c") //$NON-NLS-1$
							|| extension.equals("cpp") || extension.equals("c++") //$NON-NLS-1$ //$NON-NLS-2$
							|| extension.equals("cc") || extension.equals("cp"); //$NON-NLS-1$ //$NON-NLS-2$
				}

				// ask for command line arguments
				GemUtilities.setCommandLineArgs();

				// Open Analyzer and Browser Views in preference order
				// Find the active editor
				final IWorkbench wb = PlatformUI.getWorkbench();
				final IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
				final IWorkbenchPage page = window.getActivePage();

				try {
					final String activeView = pstore.getString(PreferenceConstants.GEM_ACTIVE_VIEW);
					if (activeView.equals("analyzer")) { //$NON-NLS-1$
						page.showView(GemBrowser.ID);
						page.showView(GemAnalyzer.ID);
					} else {
						page.showView(GemAnalyzer.ID);
						page.showView(GemBrowser.ID);
					}
					GemUtilities.initGemViews(file, isSourceFile, true);
				} catch (final PartInitException e) {
					GemUtilities.logExceptionDetail(e);
				}
			}
		});

		this.setNumProcsComboList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (GemBrowser.this.setNumProcsComboList.getText() == null) {
					return;
				}

				final String nprocsStr = GemBrowser.this.setNumProcsComboList.getItems()[GemBrowser.this.setNumProcsComboList
						.getSelectionIndex()];
				final int newNumProcs = Integer.parseInt(nprocsStr);

				// Reset the numProcs value in the preference store
				GemPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GEM_PREF_NUMPROCS, newNumProcs);

				// If the analyzer is open updates its drop down
				try {
					final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					final IViewPart gemViewPart = window.getActivePage().findView(GemAnalyzer.ID);
					final GemAnalyzer analyzer = (GemAnalyzer) gemViewPart;
					analyzer.updateDropDown();
				} catch (final Exception e) {
					GemUtilities.logExceptionDetail(e);
				}
			}
		});
	}

	/*
	 * Creates a tab in the specified folder at the specified index which is
	 * filled with all assertion violations that were found or by a message
	 * indicating that none are present.
	 */
	private void fillAssertionViolationTab(CTabFolder tabFolder, int tabIndex) {

		// Create the tab and the tree it holds
		new CTabItem(tabFolder, SWT.NONE, tabIndex).setText(Messages.GemBrowser_11);
		final CTabItem assertTab = tabFolder.getItem(tabIndex);
		assertTab.setImage(GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/magnified-trident.gif")));//$NON-NLS-1$
		assertTab.setToolTipText(Messages.GemBrowser_12);
		final Tree tree = new Tree(tabFolder, SWT.BORDER);
		assertTab.setControl(tree);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(setFontSize(tree.getFont(), 8));
		TreeItem interleavingItem = null;
		TreeItem fileItem = null;

		// Parse the resource leak list and populate tree.
		final int totalInterleavings = this.transitions.getTotalInterleavings();
		boolean errorFound = false;
		for (int j = 0; j <= totalInterleavings; j++) {
			if (this.transitions.getErrorCalls() == null || this.transitions.getErrorCalls().get(j) == null) {
				continue;
			}

			final int listSize = this.transitions.getErrorCalls().get(j).size();
			final HashMap<String, Envelope> map = this.transitions.getErrorCalls().get(j);
			if (map == null) {
				continue;
			}
			interleavingItem = new TreeItem(tree, SWT.NULL);
			interleavingItem.setText("Interleaving: " + j); //$NON-NLS-1$
			for (int i = 0; i < listSize; i++) {
				final Collection<Envelope> collection = map.values();
				Object[] calls = new Object[collection.size()];
				calls = collection.toArray();
				final Envelope env = (Envelope) calls[i];

				// If it is not an assertion violation move on
				if (!env.getFunctionName().equalsIgnoreCase("mpi_assert")) { //$NON-NLS-1$
					continue;
				}

				errorFound = true;

				final IFile sourceFile = GemUtilities.getSourceFile(env.getFilePath(), GemUtilities.getProjectLogFile());
				if (sourceFile != null) {
					final String basePath = sourceFile.getFullPath().toPortableString();
					fileItem = new TreeItem(interleavingItem, SWT.NULL);
					fileItem.setText(env.getFunctionName() + "\t" + basePath + "\tLine: " + env.getLinenumber()); //$NON-NLS-1$ //$NON-NLS-2$
					this.errorCount++;
				} else {
					clear();
					GemUtilities.showErrorDialog(Messages.GemBrowser_1);
					break;
				}
			}
			// If nothing was added for this interleaving remove the item
			if (interleavingItem.getItemCount() < 1) {
				interleavingItem.dispose();
			}
		}
		if (!errorFound) {
			interleavingItem = new TreeItem(tree, SWT.NULL);
			interleavingItem.setText(Messages.GemBrowser_14);
			final Image noErrorImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/no-error.gif"));//$NON-NLS-1$
			assertTab.setImage(noErrorImage);
		}

		// Add a selection listener so a click will open the entry in the editor
		tree.addSelectionListener(this.listener);
	}

	/*
	 * Populates the this viewer with tabs holding the problems that were found.
	 */
	private void fillBrowserTabs() {
		final Boolean FIB = GemPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.GEM_PREF_FIB);

		int tabIndex = 0;
		fillDeadlockTab(this.tabFolder, tabIndex++);
		fillAssertionViolationTab(this.tabFolder, tabIndex++);
		fillResourceLeaksTab(this.tabFolder, tabIndex++);
		if (FIB) {
			fillIrrelevantBarrierTab(this.tabFolder, tabIndex++);
		}
		fillTypeMismatchTab(this.tabFolder, tabIndex++);

		// Open up the first tab
		this.tabFolder.setSelection(this.tabFolder.getItem(0));
		this.tabFolder.showSelection();

		// Set the label's text and icon
		final String errString = (this.errorCount == 1) ? Messages.GemBrowser_15 : Messages.GemBrowser_16;
		final String warnString = (this.warningCount == 1) ? Messages.GemBrowser_17 : Messages.GemBrowser_18;
		this.summaryLabel.setText(this.errorCount + " " + errString + " " + this.warningCount + " " + warnString); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (this.errorCount == 0 && this.warningCount == 0) {
			this.summaryLabel.setImage(GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/no-error.gif")));//$NON-NLS-1$
		} else {
			this.summaryLabel.setImage(GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/magnified-trident.gif")));//$NON-NLS-1$
		}
	}

	/*
	 * Calling this will create a tab filled with the errors found or a message
	 * indicating your clean bill of health
	 */
	private void fillDeadlockTab(CTabFolder tabFolder, int tabIndex) {

		// Create the Tab and the tree it holds
		new CTabItem(tabFolder, SWT.NONE, tabIndex).setText(Messages.GemBrowser_19);
		final CTabItem errorTab = tabFolder.getItem(tabIndex);
		errorTab.setImage(GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/magnified-trident.gif")));//$NON-NLS-1$
		errorTab.setToolTipText(Messages.GemBrowser_20);
		final Tree tree = new Tree(tabFolder, SWT.BORDER);
		errorTab.setControl(tree);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(setFontSize(tree.getFont(), 8));
		TreeItem interleavingItem = null;
		TreeItem fileItem = null;

		// Parse the resource leak list and populate tree.
		final int totalInterleavings = this.transitions.getTotalInterleavings();
		boolean errorFound = false;
		for (int i = 0; i <= totalInterleavings; i++) {
			if (this.transitions.getErrorCalls() == null || this.transitions.getErrorCalls().get(i) == null) {
				continue;
			}
			final HashMap<String, Envelope> map = this.transitions.getErrorCalls().get(i);
			if (map == null) {
				continue;
			}
			interleavingItem = new TreeItem(tree, SWT.NULL);
			interleavingItem.setText("Interleaving: " + i); //$NON-NLS-1$
			final Collection<Envelope> collection = map.values();
			final Iterator<Envelope> itr = collection.iterator();
			while (itr.hasNext()) {
				final Envelope env = itr.next();

				// If it is an assertion violation move on
				if (env.getFunctionName().equalsIgnoreCase("mpi_assert")) { //$NON-NLS-1$
					continue;
				}
				errorFound = true;

				final IFile sourceFile = GemUtilities.getSourceFile(env.getFilePath(), GemUtilities.getProjectLogFile());
				if (sourceFile != null) {
					final String basePath = sourceFile.getFullPath().toPortableString();
					fileItem = new TreeItem(interleavingItem, SWT.NULL);
					fileItem.setText(env.getFunctionName() + "\t" + basePath + "\tLine: " + env.getLinenumber()); //$NON-NLS-1$ //$NON-NLS-2$
					this.errorCount++;
				} else {
					clear();
					GemUtilities.showErrorDialog(Messages.GemBrowser_2);
					break;
				}
			}

			// If nothing was added for this interleaving remove the item
			if (interleavingItem.getItemCount() < 1) {
				interleavingItem.dispose();
			}
		}
		if (!errorFound) {
			interleavingItem = new TreeItem(tree, SWT.NULL);
			interleavingItem.setText(Messages.GemBrowser_22);
			final Image noErrorImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/no-error.gif"));//$NON-NLS-1$
			errorTab.setImage(noErrorImage);
		}

		// Add a selection listener so a click will open the entry in the editor
		tree.addSelectionListener(this.listener);
	}

	/*
	 * Creates a tab in the specified folder at the specified index that is
	 * filled with all irrelevant barriers that were found or by a message that
	 * either says that there are no irrelevant barriers or that FIB is
	 * disabled.
	 */
	private void fillIrrelevantBarrierTab(CTabFolder tabFolder, int tabIndex) {

		// Create the tab and the tree inside it
		new CTabItem(tabFolder, SWT.NONE, tabIndex).setText(Messages.GemBrowser_23);
		final CTabItem IBTab = tabFolder.getItem(tabIndex);
		IBTab.setImage(GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/magnified-trident.gif")));//$NON-NLS-1$
		IBTab.setToolTipText(Messages.GemBrowser_24);
		final Tree tree = new Tree(tabFolder, SWT.BORDER);
		IBTab.setControl(tree);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(setFontSize(tree.getFont(), 8));
		TreeItem interleavingItem = null;
		TreeItem fileItem = null;

		/* Populate the tab */
		// If FIB is disabled tell the user
		final Boolean FIB = GemPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.GEM_PREF_FIB);
		if (!FIB) {
			interleavingItem = new TreeItem(tree, SWT.NULL);
			interleavingItem.setText(Messages.GemBrowser_25);
			final Image noErrorImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/help-contents.gif"));//$NON-NLS-1$
			IBTab.setImage(noErrorImage);
		}
		// If there is a deadlock then FIB does not function properly
		else if (this.transitions.hasDeadlock()) {
			interleavingItem = new TreeItem(tree, SWT.NULL);
			interleavingItem.setText(Messages.GemBrowser_26);
			final Image noErrorImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/help-contents.gif"));//$NON-NLS-1$
			IBTab.setImage(noErrorImage);
		} else if (this.transitions.getIrrelevantBarriers() == null) {
			interleavingItem = new TreeItem(tree, SWT.NULL);
			interleavingItem.setText(Messages.GemBrowser_27);
			final Image noErrorImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/no-error.gif"));//$NON-NLS-1$
			IBTab.setImage(noErrorImage);
		}

		else {
			final HashMap<Integer, String> fibs = this.transitions.getIrrelevantBarriers();
			TreeItem group = null;
			int groupNumber = 0;

			final Collection<String> fibList = fibs.values();
			final Iterator<String> itr = fibList.iterator();
			while (itr.hasNext()) {
				final String line = itr.next();
				if (group == null) {
					group = new TreeItem(tree, SWT.NULL);
					group.setText("Interleaving: " + (++groupNumber)); //$NON-NLS-1$
				}

				final StringTokenizer tokenizer = new StringTokenizer(line);
				final String filePathString = tokenizer.nextToken();
				final String lineNumber = tokenizer.nextToken();

				final IFile sourceFile = GemUtilities.getSourceFile(filePathString, GemUtilities.getProjectLogFile());
				if (sourceFile != null) {
					final String basePath = sourceFile.getFullPath().toPortableString();
					final String text = "FIB" + "\t" + basePath + "\tLine: " + lineNumber; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					fileItem = new TreeItem(group, SWT.NULL);
					fileItem.setText(text);
					this.warningCount++;
				} else {
					clear();
					GemUtilities.showErrorDialog(Messages.GemBrowser_3);
					break;
				}
			}

			// Add selection listener so a click will open entry in the editor
			tree.addSelectionListener(this.listener);
		}
	}

	/*
	 * Populates the view pull-down menu.
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.terminateOperationAction);
		this.terminateOperationAction.setText(Messages.GemBrowser_29);
		manager.add(new Separator());
		manager.add(this.writeToLocalFileAction);
		this.writeToLocalFileAction.setText(Messages.GemBrowser_30);
		manager.add(new Separator());
		manager.add(this.getHelpAction);
		manager.add(new Separator());

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Contributes icons and actions to the tool bar.
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.terminateOperationAction);
		manager.add(this.writeToLocalFileAction);
		manager.add(this.getHelpAction);

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Creates a tab in the specified folder at the specified index filled with
	 * all leaks that were found or by a message indicating that there are none.
	 */
	private void fillResourceLeaksTab(CTabFolder tabFolder, int tabIndex) {

		// Create the tab and the tree
		new CTabItem(tabFolder, SWT.NONE, tabIndex).setText(Messages.GemBrowser_31);
		final CTabItem leakTab = tabFolder.getItem(tabIndex);
		leakTab.setImage(GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/magnified-trident.gif"))); //$NON-NLS-1$
		leakTab.setToolTipText(Messages.GemBrowser_32);
		final Tree tree = new Tree(tabFolder, SWT.BORDER);
		leakTab.setControl(tree);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(setFontSize(tree.getFont(), 8));
		TreeItem interleavingItem = null;
		TreeItem fileItem = null;

		// Parse the resource leak list and populate tree.
		int prevInterleaving = -1;
		final HashMap<Integer, Envelope> leaks = this.transitions.getResourceLeaks();

		if (leaks == null) {
			interleavingItem = new TreeItem(tree, SWT.NULL);
			interleavingItem.setText(Messages.GemBrowser_33);
			final Image noErrorImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/no-error.gif")); //$NON-NLS-1$
			leakTab.setImage(noErrorImage);
		} else {
			final Collection<Envelope> leakList = leaks.values();
			final Iterator<Envelope> itr = leakList.iterator();
			while (itr.hasNext()) {
				final Envelope env = itr.next();
				if (prevInterleaving != env.getInterleaving()) {
					interleavingItem = new TreeItem(tree, SWT.NULL);
					interleavingItem.setText("Interleaving: " + env.getInterleaving()); //$NON-NLS-1$
					prevInterleaving = env.getInterleaving();
				}

				final IFile sourceFile = GemUtilities.getSourceFile(env.getFilePath(), GemUtilities.getProjectLogFile());
				if (sourceFile != null) {
					final String basePath = sourceFile.getFullPath().toPortableString();
					final String text = env.getLeakResource() + "\t" + basePath + "\tLine: " + env.getLinenumber(); //$NON-NLS-1$ //$NON-NLS-2$
					fileItem = new TreeItem(interleavingItem, SWT.NULL);
					fileItem.setText(text);
					this.warningCount++;
				} else {
					clear();
					GemUtilities.showErrorDialog(Messages.GemBrowser_4);
					break;
				}
			}
		}

		// Add a selection listener so a click will open the entry in the editor
		tree.addSelectionListener(this.listener);
	}

	/*
	 * Creates a tab in the specified folder at the specified index that is
	 * filled with all MPI type mismatches that were found.
	 */
	private void fillTypeMismatchTab(CTabFolder tabFolder, int tabIndex) {

		// Create the tab and the tree inside it
		new CTabItem(tabFolder, SWT.NONE, tabIndex).setText("Type Mismatches"); //$NON-NLS-1$
		final CTabItem IBTab = tabFolder.getItem(tabIndex);
		IBTab.setImage(GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/magnified-trident.gif")));//$NON-NLS-1$
		IBTab.setToolTipText("Browse Type Mismatches"); //$NON-NLS-1$
		final Tree tree = new Tree(tabFolder, SWT.BORDER);
		IBTab.setControl(tree);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(setFontSize(tree.getFont(), 8));
		TreeItem interleavingItem = null;
		TreeItem fileItem = null;

		/* Populate the tab */
		if (this.transitions.getTypeMismatches() == null) {
			interleavingItem = new TreeItem(tree, SWT.NULL);
			interleavingItem.setText("No Type Mismatches Found"); //$NON-NLS-1$
			final Image noErrorImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/no-error.gif"));//$NON-NLS-1$
			IBTab.setImage(noErrorImage);
		} else {
			final HashMap<Integer, String> mismatches = this.transitions.getTypeMismatches();
			TreeItem group = null;
			int groupNumber = 0;

			final Collection<String> mismatchList = mismatches.values();
			final Iterator<String> itr = mismatchList.iterator();
			while (itr.hasNext()) {
				final String line = itr.next();
				if (group == null) {
					group = new TreeItem(tree, SWT.NULL);
					group.setText("Interleaving: " + (++groupNumber)); //$NON-NLS-1$
				}

				final StringTokenizer tokenizer = new StringTokenizer(line);
				final String filePathString = tokenizer.nextToken();
				final String lineNumber = tokenizer.nextToken();

				final IFile sourceFile = GemUtilities.getSourceFile(filePathString, GemUtilities.getProjectLogFile());
				if (sourceFile != null) {
					final String basePath = sourceFile.getFullPath().toPortableString();
					final String text = "Type_Mismatch" + "\t" + basePath + "\tLine: " + lineNumber; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					fileItem = new TreeItem(group, SWT.NULL);
					fileItem.setText(text);
					this.warningCount++;
				} else {
					clear();
					GemUtilities.showErrorDialog(Messages.GemBrowser_5);
					break;
				}
			}

			// Add selection listener so a click will open entry in the editor
			tree.addSelectionListener(this.listener);
		}
	}

	/**
	 * Initializing everything for this view and creates threads to be used by
	 * the main UI thread to do updates.
	 * 
	 * @param none
	 * @return void
	 */
	public void init() {

		this.browserUpdateThread = new Thread() {
			@Override
			public void run() {
				reset();
				fillBrowserTabs();
				Display.getDefault().syncExec(GemBrowser.this.disableTerminateButtonThread);
			}
		};

		this.clearBrowserThread = new Thread() {
			@Override
			public void run() {
				GemBrowser.this.terminateOperationAction.setEnabled(false);
				GemBrowser.this.writeToLocalFileAction.setEnabled(false);
				GemBrowser.this.runGemButton.setEnabled(true);
				GemBrowser.this.transitions = null;
				GemBrowser.this.errorCount = 0;
				GemBrowser.this.warningCount = 0;
				GemBrowser.this.summaryLabel.setText(""); //$NON-NLS-1$
				GemBrowser.this.summaryLabel.setImage(null);
				removeTabs();
				GemUtilities.setTaskStatus(GemUtilities.TaskStatus.IDLE);
			}
		};

		this.disableTerminateButtonThread = new Thread() {
			@Override
			public void run() {
				GemBrowser.this.terminateOperationAction.setEnabled(false);
				GemBrowser.this.writeToLocalFileAction.setEnabled(true);
				GemBrowser.this.runGemButton.setEnabled(true);
			}
		};

		// start things up now
		this.writeToLocalFileAction.setEnabled(false);
		this.terminateOperationAction.setEnabled(true);
		this.runGemButton.setEnabled(false);
	}

	/*
	 * Creates the actions associated with the action bar buttons and context
	 * menu items.
	 */
	private void makeActions() {
		// Get Help Action
		this.getHelpAction = new Action() {
			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource("/org.eclipse.ptp.gem.help/html/browserView.html"); //$NON-NLS-1$
			}
		};
		this.getHelpAction.setToolTipText(Messages.GemBrowser_39);
		this.getHelpAction.setImageDescriptor(GemPlugin.getImageDescriptor("icons/help-contents.gif")); //$NON-NLS-1$

		// Terminate Process Action
		this.terminateOperationAction = new Action() {
			@Override
			public void run() {
				GemUtilities.terminateOperation();
			}
		};
		this.terminateOperationAction.setImageDescriptor(GemPlugin.getImageDescriptor("icons/progress_stop.gif")); //$NON-NLS-1$
		this.terminateOperationAction.setToolTipText(Messages.GemBrowser_40);
		this.terminateOperationAction.setEnabled(false);

		// Save Browser summary to local file
		this.writeToLocalFileAction = new Action() {
			@Override
			public void run() {

				// Let the user indicate where to save local file
				final JFileChooser fc = new JFileChooser();
				final JFrame frame = new JFrame();
				int result = fc.showSaveDialog(frame);

				if (result == JFileChooser.APPROVE_OPTION) {
					final File file = fc.getSelectedFile();
					if (file.exists()) {
						result = fc.showDialog(frame, Messages.GemBrowser_41);
					}
					if (result == JFileChooser.APPROVE_OPTION) {
						GemUtilities.saveToLocalFile(file, createBrowserSummary());
					}
				} else if (result == JFileChooser.ERROR_OPTION) {
					GemUtilities.showErrorDialog(Messages.GemBrowser_42);
				}
			}
		};
		this.writeToLocalFileAction.setToolTipText(Messages.GemBrowser_43);
		this.writeToLocalFileAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
		this.writeToLocalFileAction.setEnabled(false);
	}

	/*
	 * Opens the editor with the current source file active and jump to the line
	 * number that is passed in.
	 */
	private void openEditor(int lineNumber, IFile sourceFile) {
		try {
			final IEditorPart editor = org.eclipse.ui.ide.IDE.openEditor(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
					sourceFile, true);
			final IMarker marker = sourceFile.createMarker(IMarker.MARKER);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			org.eclipse.ui.ide.IDE.gotoMarker(editor, marker);
		} catch (final Exception e) {
			GemUtilities.logExceptionDetail(e);
		}
	}

	/*
	 * Removes all tabs, used when clearing the browser or in preparation for
	 * the next analysis.
	 */
	private void removeTabs() {
		final CTabItem[] items = this.tabFolder.getItems();
		final int numItems = items.length;
		for (int i = 0; i < numItems; i++) {
			items[i].dispose();
		}
	}

	/*
	 * Resets everything to default values. Used when a new file is analyzed or
	 * when an exception or some other error occurs that interrupts the current
	 * GEM operation.
	 */
	private void reset() {
		setNumProcItems();
		removeTabs();
		GemUtilities.setTaskStatus(GemUtilities.TaskStatus.IDLE);
		Display.getDefault().syncExec(GemBrowser.this.disableTerminateButtonThread);
	}

	/**
	 * see org.eclipse.ui.IWorkbenchPart
	 */
	@Override
	public void setFocus() {
		this.runGemButton.setFocus();
	}

	/*
	 * Given the initial Font, this helper method returns that Font with the new
	 * specified size.
	 */
	private Font setFontSize(Font font, int size) {
		final FontData[] fontData = font.getFontData();
		for (final FontData element : fontData) {
			element.setHeight(size);
		}
		return new Font(this.parent.getDisplay(), fontData);
	}

	/*
	 * Populates the num-procs combo-box with choices the user can use to set
	 * the number of processes for the next analyzer run.
	 */
	private void setNumProcItems() {
		final String[] ranks = new String[16];
		for (int i = 1; i <= 16; i++) {
			ranks[i - 1] = ((Integer) i).toString();
		}
		this.setNumProcsComboList.setItems(ranks);
		final Integer numProcs = GemPlugin.getDefault().getPreferenceStore().getInt(PreferenceConstants.GEM_PREF_NUMPROCS);
		this.setNumProcsComboList.setText(numProcs.toString());
	}

	/**
	 * Called on each new GEM analysis This method resets the transitions object
	 * along with warning/error counters and invokes the BrowserUppdate thread.
	 * 
	 * @param transitions
	 *            The Transitions object associated with this GEM update.
	 * @return void
	 */
	public void update(Transitions transitions) {
		this.transitions = transitions;
		this.errorCount = 0;
		this.warningCount = 0;

		// Create a thread to perform the update
		Display.getDefault().syncExec(this.browserUpdateThread);
	}

	/**
	 * This method is used when another location changes the number of processes
	 * preference. Calling this method will update the drop down so that it has
	 * the correct number of processes displayed.
	 * 
	 * @param none
	 * @return void
	 */
	public void updateDropDown() {
		final Integer nprocs = GemPlugin.getDefault().getPreferenceStore().getInt(PreferenceConstants.GEM_PREF_NUMPROCS);
		this.setNumProcsComboList.setText(nprocs.toString());
	}

}
