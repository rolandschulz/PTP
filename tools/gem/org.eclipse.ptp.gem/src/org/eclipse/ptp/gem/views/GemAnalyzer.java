/*******************************************************************************
 * Copyright (c) 2009, 2012 University of Utah School of Computing
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

package org.eclipse.ptp.gem.views;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.gem.GemPlugin;
import org.eclipse.ptp.gem.messages.Messages;
import org.eclipse.ptp.gem.preferences.PreferenceConstants;
import org.eclipse.ptp.gem.util.Envelope;
import org.eclipse.ptp.gem.util.GemUtilities;
import org.eclipse.ptp.gem.util.InternalIssueOrderSorter;
import org.eclipse.ptp.gem.util.ListElement;
import org.eclipse.ptp.gem.util.ProgramOrderSorter;
import org.eclipse.ptp.gem.util.Transitions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

/**
 * The GEM Analyzer View.
 */
public class GemAnalyzer extends ViewPart {

	// Listens for double-clicks and jumps to that line of code in the editor
	private class DoubleClickListener implements IDoubleClickListener {

		public void doubleClick(DoubleClickEvent event) {
			final ISelection selection = event.getSelection();
			final StructuredSelection structure = (StructuredSelection) selection;
			final ListElement element = (ListElement) structure.getFirstElement();
			if (element != null) {
				final IFile sourceFile = element.getFile();
				final int lineNumber = element.getLineNumber();
				openEditor(lineNumber, sourceFile);
			}
		}
	}

	// The ID for this view
	public static final String ID = "org.eclipse.ptp.gem.views.GemAnalyzer"; //$NON-NLS-1$

	// Data structures and more complex members
	private Transitions transitions;
	private LinkedList<Shell> activeShells;

	// Container objects and viewers
	private Composite parent;
	private ListViewer leftViewer;
	private ListViewer rightViewer;
	private Action getHelp;
	private Action terminateButton;

	// Simple members
	private int numRanks;
	private int lockedRank;
	private IFile currLeftFile;
	private IFile currRightFile;

	// SWT Buttons
	private Button firstTransitionButton;
	private Button previousTransitionButton;
	private Button nextTransitionButton;
	private Button lastTransitionButton;
	private Button firstInterleavingButton;
	private Button previousInterleavingButton;
	private Button nextInterleavingButton;
	private Button lastInterleavingButton;
	private Button deadlockInterleavingButton;
	private Button internalIssueOrderButton;
	private Button programOrderButton;
	private Button launchHpvButton;
	private Button browseCallsButton;
	private Button runGemButton;
	private Combo lockRanksComboList;
	private Combo setNumProcsComboList;

	// Groups that need to be accessed multiple times
	private Group interleavingsGroup;
	private Group transitionsGroup;
	private Group stepOrderGroup;

	// SWT Labels
	private Label errorMessageLabel;
	private Label leftCodeWindowLabel;
	private Label rightCodeWindowLabel;
	private CLabel leftCodeWindowExplenationLabel;
	private CLabel rightCodeWindowExplenationLabel;

	// Used for the transition label
	private int transitionLabelIndex;
	private int transitionLabelCount;

	// Things for highlighting the appropriate line in the code windows
	private int leftIndex;
	private int rightIndex;
	private int previousLeftIndex;

	// Listeners for the code viewers
	private SelectionListener singleListener;
	private DoubleClickListener doubleListener;

	// The active file resource needed for various operations
	private IFile activeFile;

	// Threads
	private Thread analyzerUpdateThread;
	private Thread clearAnalyzerThread;
	private Thread disableTerminateButtonThread;

	// Misc
	private int leftLines;
	private int rightLines;
	private boolean aborted;

	/**
	 * Constructor.
	 * 
	 * @param none
	 */
	public GemAnalyzer() {
		this.lockedRank = -1;
		this.aborted = false;
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
					page.activate(GemAnalyzer.this);
				}
			}
		};

		// We need to switch to the thread that is allowed to change the UI
		Display.getDefault().syncExec(activationThread);
	}

	/**
	 * Runs a thread that clears the Analyzer view.
	 * 
	 * @param none
	 * @return void
	 */
	public void clear() {
		Display.getDefault().syncExec(this.clearAnalyzerThread);
		this.aborted = false;
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
	 * Helper method called by createPartControl.
	 */
	private void createCodeWindowsGroup(Composite parent) {
		// Get images for buttons from image cache
		final Image sourceCallImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/source-call.gif")); //$NON-NLS-1$
		final Image matchCallImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/match-call.gif")); //$NON-NLS-1$

		// Create the layout for the Analyzer windows and call info labels
		final Group codeWindowsGroup = new Group(parent, SWT.NONE | SWT.SHADOW_IN);
		codeWindowsGroup.setText(Messages.GemAnalyzer_0);
		final GridLayout codeWindowsLayout = new GridLayout();
		codeWindowsLayout.numColumns = 2;
		codeWindowsLayout.marginHeight = 10;
		codeWindowsLayout.marginWidth = 10;
		codeWindowsLayout.horizontalSpacing = 15;
		codeWindowsGroup.setLayout(codeWindowsLayout);
		codeWindowsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		// Create code window labels and their respective layouts, etc.
		final String newline = System.getProperty("line.separator"); //$NON-NLS-1$
		this.leftCodeWindowLabel = new Label(codeWindowsGroup, SWT.WRAP);
		this.leftCodeWindowLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		final Font leftCodeWindowLabelFont = setFontSize(this.leftCodeWindowLabel.getFont(), 9);
		this.leftCodeWindowLabel.setFont(leftCodeWindowLabelFont);
		this.leftCodeWindowLabel.setText(newline);
		this.rightCodeWindowLabel = new Label(codeWindowsGroup, SWT.WRAP);
		this.rightCodeWindowLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		final Font rightCodeWindowLabelFont = setFontSize(this.rightCodeWindowLabel.getFont(), 9);
		this.rightCodeWindowLabel.setFont(rightCodeWindowLabelFont);
		this.rightCodeWindowLabel.setText(newline);

		// The short explanations of each of the code windows
		this.leftCodeWindowExplenationLabel = new CLabel(codeWindowsGroup, SWT.WRAP | SWT.NULL);
		this.rightCodeWindowExplenationLabel = new CLabel(codeWindowsGroup, SWT.WRAP | SWT.NULL);
		this.leftCodeWindowExplenationLabel.setImage(sourceCallImage);
		this.rightCodeWindowExplenationLabel.setImage(matchCallImage);
		this.leftCodeWindowExplenationLabel.setText(Messages.GemAnalyzer_1);
		this.rightCodeWindowExplenationLabel.setText(Messages.GemAnalyzer_2);

		// Create the Analyzer list viewers
		this.leftViewer = new ListViewer(codeWindowsGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		this.rightViewer = new ListViewer(codeWindowsGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		final Font leftViewerFont = setFontSize(this.leftViewer.getControl().getFont(), 8);
		final Font rightViewerFont = setFontSize(this.rightViewer.getControl().getFont(), 8);
		this.leftViewer.getControl().setFont(leftViewerFont);
		this.rightViewer.getControl().setFont(rightViewerFont);
		this.leftViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		this.rightViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		/*
		 * Create two listeners for these viewers. SelectionListener is to
		 * prevent user from changing the selected line. DoubleListener maps
		 * selected line to the editor view
		 */
		this.singleListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final org.eclipse.swt.widgets.List list = (org.eclipse.swt.widgets.List) e.getSource();
				final String entry = list.getItem(0).toString();

				// Determine if collective call clicked in the Right Viewer
				if (!entry.contains(Messages.GemAnalyzer_3) || !entry.contains(Messages.GemAnalyzer_4)) {
					updateSelectedLine(false);
				}
			}
		};

		// SelectionListener prevents user from changing selected viewer line
		this.doubleListener = new DoubleClickListener();
		this.leftViewer.getList().addSelectionListener(this.singleListener);
		this.rightViewer.getList().addSelectionListener(this.singleListener);
		this.leftViewer.addDoubleClickListener(this.doubleListener);
		this.rightViewer.addDoubleClickListener(this.doubleListener);
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createInterleavingsGroup(Composite parent) {
		// Get images for buttons from image cache
		final Image firstItemImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/first-item.gif")); //$NON-NLS-1$
		final Image lastItemImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/last-item.gif")); //$NON-NLS-1$
		final Image prevItemImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/prev-item.gif")); //$NON-NLS-1$
		final Image nextItemImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/next-item.gif")); //$NON-NLS-1$
		final Image deadlockImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/deadlock.gif")); //$NON-NLS-1$

		// Group and FormLayout data for interleaving buttons and labels
		this.interleavingsGroup = new Group(parent, SWT.SHADOW_IN);
		this.interleavingsGroup.setText(Messages.GemAnalyzer_5);
		this.interleavingsGroup.setToolTipText(Messages.GemAnalyzer_6);
		final FormData interleavingsFormData = new FormData();
		interleavingsFormData.bottom = new FormAttachment(100, -5);
		interleavingsFormData.left = new FormAttachment(44, 0);
		this.interleavingsGroup.setLayoutData(interleavingsFormData);
		this.interleavingsGroup.setLayout(new FormLayout());

		// First interleaving button
		this.firstInterleavingButton = new Button(this.interleavingsGroup, SWT.PUSH);
		this.firstInterleavingButton.setImage(firstItemImage);
		this.firstInterleavingButton.setToolTipText(Messages.GemAnalyzer_7);
		this.firstInterleavingButton.setEnabled(false);
		final FormData ifirstFormData = new FormData();
		ifirstFormData.left = new FormAttachment(0, 5);
		ifirstFormData.bottom = new FormAttachment(100, -5);
		this.firstInterleavingButton.setLayoutData(ifirstFormData);

		// Previous interleaving button
		this.previousInterleavingButton = new Button(this.interleavingsGroup, SWT.PUSH);
		this.previousInterleavingButton.setImage(prevItemImage);
		this.previousInterleavingButton.setToolTipText(Messages.GemAnalyzer_8);
		this.previousInterleavingButton.setEnabled(false);
		final FormData iprevFormData = new FormData();
		iprevFormData.left = new FormAttachment(this.firstInterleavingButton, 3);
		iprevFormData.bottom = new FormAttachment(100, -5);
		this.previousInterleavingButton.setLayoutData(iprevFormData);

		// Next interleaving button
		this.nextInterleavingButton = new Button(this.interleavingsGroup, SWT.PUSH);
		this.nextInterleavingButton.setImage(nextItemImage);
		this.nextInterleavingButton.setToolTipText(Messages.GemAnalyzer_9);
		this.nextInterleavingButton.setEnabled(false);
		final FormData inextFormData = new FormData();
		inextFormData.left = new FormAttachment(this.previousInterleavingButton, 3);
		inextFormData.bottom = new FormAttachment(100, -5);
		this.nextInterleavingButton.setLayoutData(inextFormData);

		// Last interleaving button
		this.lastInterleavingButton = new Button(this.interleavingsGroup, SWT.PUSH);
		this.lastInterleavingButton.setImage(lastItemImage);
		this.lastInterleavingButton.setToolTipText(Messages.GemAnalyzer_10);
		this.lastInterleavingButton.setEnabled(false);
		final FormData ilastFormData = new FormData();
		ilastFormData.left = new FormAttachment(this.nextInterleavingButton, 3);
		ilastFormData.bottom = new FormAttachment(100, -5);
		this.lastInterleavingButton.setLayoutData(ilastFormData);

		// Deadlock interleaving button
		this.deadlockInterleavingButton = new Button(this.interleavingsGroup, SWT.PUSH);
		this.deadlockInterleavingButton.setImage(deadlockImage);
		this.deadlockInterleavingButton.setToolTipText(Messages.GemAnalyzer_11);
		this.deadlockInterleavingButton.setEnabled(false);
		final FormData deadlockButtonFormData = new FormData();
		deadlockButtonFormData.left = new FormAttachment(this.lastInterleavingButton, 3);
		deadlockButtonFormData.bottom = new FormAttachment(100, -5);
		deadlockButtonFormData.right = new FormAttachment(100, -5);
		this.deadlockInterleavingButton.setLayoutData(deadlockButtonFormData);
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

		// Create container for transition and interleaving button groups
		final Composite runtimeComposite = new Composite(parent, SWT.NULL);
		runtimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		runtimeComposite.setLayout(new FormLayout());
		final Composite buttonGroupsComposite = new Composite(parent, SWT.NULL);
		buttonGroupsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		buttonGroupsComposite.setLayout(new FormLayout());

		// Create groups and selection listeners
		createRuntimeGroup(runtimeComposite);
		createTransitionsGroup(buttonGroupsComposite);
		createInterleavingsGroup(buttonGroupsComposite);
		createStepOrderGroup(buttonGroupsComposite);
		createCodeWindowsGroup(parent);
		createSelectionListeners();

		// create actions & connect to buttons, context menus and pull-downs
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createRuntimeGroup(Composite parent) {
		// Get images for buttons from image cache
		final Image noErrorImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/no-error.gif")); //$NON-NLS-1$
		final Image hbvImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/hbv-trident.gif")); //$NON-NLS-1$
		final Image trident = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/trident.gif")); //$NON-NLS-1$

		// Runtime information group
		final Group runtimeInfoGroup = new Group(parent, SWT.SHADOW_IN);
		runtimeInfoGroup.setText(Messages.GemAnalyzer_12);
		final FormData grid = new FormData();
		grid.left = new FormAttachment(0, 0);
		grid.right = new FormAttachment(100, -5);
		grid.bottom = new FormAttachment(100, -5);
		runtimeInfoGroup.setLayoutData(grid);
		runtimeInfoGroup.setLayout(new FormLayout());

		// Error message label
		this.errorMessageLabel = new Label(runtimeInfoGroup, SWT.NONE);
		final FormData deadlockMessageFormData = new FormData();
		deadlockMessageFormData.left = new FormAttachment(0, 5);
		deadlockMessageFormData.top = new FormAttachment(0, 5);
		deadlockMessageFormData.width = 300;
		this.errorMessageLabel.setLayoutData(deadlockMessageFormData);
		final Font errorMessageLabelFont = setFontSize(this.errorMessageLabel.getFont(), 10);
		this.errorMessageLabel.setFont(errorMessageLabelFont);

		// Browse calls button
		this.browseCallsButton = new Button(runtimeInfoGroup, SWT.PUSH);
		this.browseCallsButton.setImage(noErrorImage);
		this.browseCallsButton.setText(Messages.GemAnalyzer_79);
		this.browseCallsButton.setToolTipText(Messages.GemAnalyzer_14);
		this.browseCallsButton.setEnabled(false);
		final FormData browseCallsFormData = new FormData();
		browseCallsFormData.right = new FormAttachment(100, -5);
		browseCallsFormData.bottom = new FormAttachment(100, -5);
		this.browseCallsButton.setLayoutData(browseCallsFormData);

		// Launch ispUI button
		this.launchHpvButton = new Button(runtimeInfoGroup, SWT.PUSH);
		this.launchHpvButton.setImage(hbvImage);
		this.launchHpvButton.setText(Messages.GemAnalyzer_15);
		this.launchHpvButton.setToolTipText(Messages.GemAnalyzer_16);
		this.launchHpvButton.setEnabled(false);
		final FormData launchIspUIFormData = new FormData();
		launchIspUIFormData.right = new FormAttachment(this.browseCallsButton, -5);
		launchIspUIFormData.bottom = new FormAttachment(100, -5);
		this.launchHpvButton.setLayoutData(launchIspUIFormData);

		// Run GEM button
		this.runGemButton = new Button(runtimeInfoGroup, SWT.PUSH);
		this.runGemButton.setImage(trident);
		this.runGemButton.setToolTipText(Messages.GemAnalyzer_17);
		final FormData runGemFormData = new FormData();
		runGemFormData.right = new FormAttachment(this.launchHpvButton, -20);
		runGemFormData.bottom = new FormAttachment(100, -5);
		this.runGemButton.setLayoutData(runGemFormData);
		this.runGemButton.setEnabled(true);

		// Set number of processes button
		this.setNumProcsComboList = new Combo(runtimeInfoGroup, SWT.DROP_DOWN);
		final Font setRankComboFont = setFontSize(this.setNumProcsComboList.getFont(), 9);
		this.setNumProcsComboList.setFont(setRankComboFont);
		final String[] items = new String[] {};
		this.setNumProcsComboList.setItems(items);
		this.setNumProcsComboList.setText(" "); //$NON-NLS-1$
		this.setNumProcsComboList.setToolTipText(Messages.GemAnalyzer_18);
		final FormData setRankFormData = new FormData();
		setRankFormData.width = 50;
		setRankFormData.right = new FormAttachment(this.runGemButton, -5);
		setRankFormData.bottom = new FormAttachment(100, -5);
		this.setNumProcsComboList.setLayoutData(setRankFormData);
		this.setNumProcsComboList.setEnabled(true);
		setNumProcItems();
		updateDropDown();

		// Font for the buttons
		final Font buttonFont = setFontSize(this.errorMessageLabel.getFont(), 8);
		this.launchHpvButton.setFont(buttonFont);
		this.browseCallsButton.setFont(buttonFont);
		this.setNumProcsComboList.setFont(buttonFont);
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createSelectionListeners() {
		// SelectionListeners for transitions group buttons
		this.firstTransitionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFirstTransition(true);
			}
		});

		this.previousTransitionButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						updatePreviousTransition();
					}
				});

		this.nextTransitionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateNextTransition(true);
			}
		});

		this.lastTransitionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLastTransition();
			}
		});

		this.lockRanksComboList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (GemAnalyzer.this.lockRanksComboList.getText() == null) {
					return;
				}

				try {
					final int selectionIndex = GemAnalyzer.this.lockRanksComboList.getSelectionIndex();
					final String selectionText = GemAnalyzer.this.lockRanksComboList.getItems()[selectionIndex];
					final Pattern rankPattern = Pattern.compile("^([a-zA-Z]+?)\\s+([0-9]+?)\\s+([a-zA-Z]+?)$"); //$NON-NLS-1$
					final Matcher rankPatternMatcher = rankPattern.matcher(selectionText);
					if (rankPatternMatcher.matches()) {
						final String rankStr = rankPatternMatcher.group(2);
						GemAnalyzer.this.lockedRank = Integer.parseInt(rankStr);
					} else {
						GemAnalyzer.this.lockedRank = -1;
					}
					updateTransitionLabels(true);
				} catch (final Exception e) {
					GemUtilities.logExceptionDetail(e);
					GemAnalyzer.this.lockedRank = -1;
				}
			}
		});

		// SelectionListeners for interleavings group buttons
		this.firstInterleavingButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (GemAnalyzer.this.transitions.hasPreviousInterleaving()) {
							while (GemAnalyzer.this.transitions.setPreviousInterleaving()) {
								// do nothing
							}
							updateTransitionLabels(true);
						} else {
							GemUtilities.showInformationDialog(Messages.GemAnalyzer_19);
						}
					}
				});

		this.previousInterleavingButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (GemAnalyzer.this.transitions.hasPreviousInterleaving()) {
							GemAnalyzer.this.transitions.setPreviousInterleaving();
							updateTransitionLabels(true);
						} else {
							GemUtilities.showInformationDialog(Messages.GemAnalyzer_20);
						}
					}
				});

		this.nextInterleavingButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (GemAnalyzer.this.transitions.hasNextInterleaving()) {
							GemAnalyzer.this.transitions.setNextInterleaving();
							updateTransitionLabels(true);
						} else {
							GemUtilities.showInformationDialog(Messages.GemAnalyzer_21);
						}
					}
				});

		this.lastInterleavingButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						while (GemAnalyzer.this.transitions.hasNextInterleaving()) {
							GemAnalyzer.this.transitions.setNextInterleaving();
						}
						updateTransitionLabels(true);
					}
				});

		this.deadlockInterleavingButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						GemAnalyzer.this.transitions.deadlockInterleaving();
						updateTransitionLabels(true);
					}
				});

		// Selection listeners for step order group
		this.internalIssueOrderButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (GemAnalyzer.this.internalIssueOrderButton.getSelection()) {
							if (GemAnalyzer.this.transitions != null) {
								final ArrayList<ArrayList<Envelope>> tlists = GemAnalyzer.this.transitions.getTransitionList();
								// Sort by internal issue order
								final int size = tlists.size();
								for (int i = 0; i < size; i++) {
									Collections.sort(tlists.get(i), new InternalIssueOrderSorter());
								}
							}

							// Reset preference store value for this preference
							GemPlugin.getDefault().getPreferenceStore()
									.setValue(PreferenceConstants.GEM_PREF_STEP_ORDER, "issueOrder"); //$NON-NLS-1$
							reset();
						}
					}
				});

		this.programOrderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (GemAnalyzer.this.programOrderButton.getSelection()) {
					// Sort by internal issue order
					if (GemAnalyzer.this.transitions != null) {
						final ArrayList<ArrayList<Envelope>> tlists = GemAnalyzer.this.transitions.getTransitionList();
						final int size = tlists.size();
						for (int i = 0; i < size; i++) {
							Collections.sort(tlists.get(i), new ProgramOrderSorter());
						}
					}

					// Reset preference store value for this preference
					GemPlugin.getDefault().getPreferenceStore()
							.setValue(PreferenceConstants.GEM_PREF_STEP_ORDER, "programOrder"); //$NON-NLS-1$
					reset();
				}
			}
		});

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
				if (GemAnalyzer.this.activeFile == null) {
					GemUtilities.setTaskStatus(GemUtilities.TaskStatus.ABORTED);
					return;
				}
				final IFile file = GemUtilities.getCurrentProject(GemAnalyzer.this.activeFile).getFile(path.lastSegment());
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

		// Selection listeners for runtime information group buttons
		this.setNumProcsComboList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (GemAnalyzer.this.setNumProcsComboList.getText() == null) {
					return;
				}

				final int selectionIndex = GemAnalyzer.this.setNumProcsComboList.getSelectionIndex();
				final String nprocsStr = GemAnalyzer.this.setNumProcsComboList.getItems()[selectionIndex];
				final int newNumProcs = Integer.parseInt(nprocsStr);

				// Reset the numProcs value in the preference store
				GemPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GEM_PREF_NUMPROCS, newNumProcs);

				// If the browser is open updates its drop down
				try {
					final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					final IViewPart browserPart = window.getActivePage().findView(GemBrowser.ID);
					final GemBrowser browser = (GemBrowser) browserPart;
					browser.updateDropDown();
				} catch (final Exception e) {
					GemUtilities.logExceptionDetail(e);
				}
			}
		});

		this.launchHpvButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				final URI uri = GemUtilities.getMostRecentURI();
				IPath sourceFilePath = null;
				IFile sourceFile = null;

				if (uri != null) {
					sourceFilePath = new Path(uri.getPath());
				}

				final IProject currentProject = GemUtilities.getProjectLogFile().getProject();
				final String currentProjectPath = currentProject.getLocationURI().getPath();
				if (sourceFilePath != null) {
					sourceFilePath = sourceFilePath.makeRelativeTo(new Path(currentProjectPath));
					sourceFile = currentProject.getFile(sourceFilePath);
				}

				if (sourceFile != null) {
					if (GemUtilities.isRemoteProject(GemAnalyzer.this.activeFile)
							|| GemUtilities.isSynchronizedProject(GemAnalyzer.this.activeFile)) {
//						GemUtilities.showInformationDialog(Messages.GemAnalyzer_23);
						return;
					}

					// Otherwise launch the HB viewer
					GemUtilities.doHbv(sourceFile);
				} else {
					// There was a local error while creating the log file.
					final String message = Messages.GemAnalyzer_13;
//					GemUtilities.showErrorDialog(message);
				}
			}
		});

		this.browseCallsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (GemAnalyzer.this.transitions == null) {
					final String message = Messages.GemAnalyzer_24;
					GemUtilities.showErrorDialog(message);
					return;
				}
				final class CallBrowserDisplay implements Runnable {
					public void run() {
						final IWorkbench wb = PlatformUI.getWorkbench();
						final Display display = wb.getDisplay();
						final Shell shell = new Shell();
						shell.setText(Messages.GemAnalyzer_68);
						shell.setImage(GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/magnified-trident.gif"))); //$NON-NLS-1$
						shell.setLayout(new GridLayout());
						final CLabel fileNameLabel = new CLabel(shell, SWT.BORDER_SOLID);
						fileNameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
						final CLabel numProcsLabel = new CLabel(shell, SWT.BORDER_SOLID);
						numProcsLabel.setImage(GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/processes.gif"))); //$NON-NLS-1$
						numProcsLabel.setText(Messages.GemAnalyzer_69 + GemAnalyzer.this.numRanks);
						numProcsLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

						final Tree tree = new Tree(shell, SWT.BORDER);
						tree.setLinesVisible(true);
						tree.setLayoutData(new GridData(GridData.FILL_BOTH));
						tree.setFont(setFontSize(tree.getFont(), 8));

						// Declare everything we'll be working with
						TreeItem interleavingItem = null;
						TreeItem callItem = null;
						final int currentInter = GemAnalyzer.this.transitions.getCurrentInterleaving();

						// Loop over all interleavings
						final int numInterleavings = GemAnalyzer.this.transitions.getTotalInterleavings() + 1; // 0-based
						for (int currentInterleaving = 1; currentInterleaving < numInterleavings; currentInterleaving++) {

							// Create a root node for each interleaving
							interleavingItem = new TreeItem(tree, SWT.NULL);
							interleavingItem.setText(Messages.GemAnalyzer_70 + currentInterleaving);

							// Loop over all envelopes (transitions) in the current interleaving
							final ArrayList<Envelope> envelopes = GemAnalyzer.this.transitions
									.getInterleavingEnvelopes(currentInterleaving);
							final int listSize = envelopes.size();
							Envelope env = null;
							String functionName = null;
							String fileName = null;
							int lineNumber = -1;
							int envRank = -1;

							try {
								for (int envIndex = 0; envIndex < listSize; envIndex++) {
									env = envelopes.get(envIndex);

									// Don't display resource leaks
									if (env.isLeak()) {
										continue;
									}

									// Create the leaf node representing the call
									callItem = new TreeItem(interleavingItem, SWT.NULL);
									functionName = env.getFunctionName();
									fileName = new Path(env.getFilePath()).lastSegment().toString();
									lineNumber = env.getLinenumber();
									envRank = env.getRank();

									// make all calls have same name length
									while (functionName.length() < 12) {
										functionName += " "; //$NON-NLS-1$
									}

									final StringBuffer stringBuffer = new StringBuffer();
									stringBuffer.append("Rank: "); //$NON-NLS-1$
									stringBuffer.append(envRank);
									stringBuffer.append("\t\t"); //$NON-NLS-1$
									stringBuffer.append(functionName);
									stringBuffer.append("\t\t"); //$NON-NLS-1$
									stringBuffer.append(fileName);
									stringBuffer.append(":"); //$NON-NLS-1$
									stringBuffer.append(lineNumber);
									final String callItemText = stringBuffer.toString();
									callItem.setText(callItemText);

									// Mark the calls with errors red
									if (env.getIssueIndex() == -1) {
										callItem.setForeground(new Color(null, 255, 0, 0));
									}

									// Mark the current call(s) blue
									String currentFile = null;
									int currentLine = -1;
									if (GemAnalyzer.this.transitions.getCurrentTransition() != null) {
										currentFile = GemAnalyzer.this.transitions.getCurrentTransition().getFilePath();
										currentLine = GemAnalyzer.this.transitions.getCurrentTransition().getLinenumber();
									}
									if (currentFile != null) {
										if (currentLine == env.getLinenumber() && currentFile.equals(env.getFilePath())
												&& currentInter == env.getInterleaving()) {
											callItem.setForeground(new Color(null, 0, 0, 255));
											// Comment this out if you don't want items expanded
											revealTreeItem(callItem);
										}
									}
								}
							} catch (final Exception e) {
								GemUtilities.logExceptionDetail(e);
							}
						}

						// Open up the Call Browser window with the specified size
						shell.setSize(550, 550);
						shell.open();

						if (GemAnalyzer.this.activeShells == null) {
							GemAnalyzer.this.activeShells = new LinkedList<Shell>();
						}
						GemAnalyzer.this.activeShells.add(shell);

						// Set up the event loop for disposal
						while (!shell.isDisposed()) {
							if (!display.readAndDispatch()) {
								display.sleep();
							}
						}
					}
				}
				final CallBrowserDisplay browser = new CallBrowserDisplay();
				Display.getDefault().syncExec(browser);
			}
		});
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createStepOrderGroup(Composite parent) {
		// Group and FormLayout data for step order radio buttons
		this.stepOrderGroup = new Group(parent, SWT.SHADOW_IN);
		this.stepOrderGroup.setText(Messages.GemAnalyzer_25);
		this.stepOrderGroup.setToolTipText(Messages.GemAnalyzer_26);
		final FormData stepOrderFormData = new FormData();
		// stepOrderFormData.left = new FormAttachment(this.interleavingsGroup,
		// 20);
		stepOrderFormData.right = new FormAttachment(100, -5);
		stepOrderFormData.bottom = new FormAttachment(100, -5);
		this.stepOrderGroup.setLayoutData(stepOrderFormData);
		this.stepOrderGroup.setLayout(new GridLayout(3, false));

		// Step order radio buttons
		this.internalIssueOrderButton = new Button(this.stepOrderGroup, SWT.RADIO);
		this.internalIssueOrderButton.setText(Messages.GemAnalyzer_27);
		this.internalIssueOrderButton.setToolTipText(Messages.GemAnalyzer_28);
		this.programOrderButton = new Button(this.stepOrderGroup, SWT.RADIO);
		this.programOrderButton.setText(Messages.GemAnalyzer_29);
		this.programOrderButton.setToolTipText(Messages.GemAnalyzer_30);

		// Choose which one is to be enabled from the Preference Store setting
		final String stepOrder = GemPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.GEM_PREF_STEP_ORDER);
		if (stepOrder.equals("issueOrder")) { //$NON-NLS-1$
			this.internalIssueOrderButton.setSelection(true);
		} else {
			this.programOrderButton.setSelection(true);
		}

		// Font for the radio buttons
		final Font buttonFont = setFontSize(this.programOrderButton.getFont(), 8);
		this.internalIssueOrderButton.setFont(buttonFont);
		this.programOrderButton.setFont(buttonFont);
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createTransitionsGroup(Composite parent) {
		// Get images for buttons from image cache
		final Image firstItemImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/first-item.gif")); //$NON-NLS-1$
		final Image lastItemImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/last-item.gif")); //$NON-NLS-1$
		final Image prevItemImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/prev-item.gif")); //$NON-NLS-1$
		final Image nextItemImage = GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/next-item.gif")); //$NON-NLS-1$

		// Group and FormLayout data for transition buttons and labels
		this.transitionsGroup = new Group(parent, SWT.SHADOW_IN);
		this.transitionsGroup.setText(Messages.GemAnalyzer_32);
		this.transitionsGroup.setToolTipText(Messages.GemAnalyzer_33);
		final FormData transitionsFormData = new FormData();
		transitionsFormData.left = new FormAttachment(0, 0);
		transitionsFormData.bottom = new FormAttachment(100, -5);
		this.transitionsGroup.setLayoutData(transitionsFormData);
		this.transitionsGroup.setLayout(new FormLayout());

		// First transition button
		this.firstTransitionButton = new Button(this.transitionsGroup, SWT.PUSH);
		this.firstTransitionButton.setImage(firstItemImage);
		this.firstTransitionButton.setToolTipText(Messages.GemAnalyzer_34);
		this.firstTransitionButton.setEnabled(false);
		final FormData tfirstFormData = new FormData();
		tfirstFormData.left = new FormAttachment(0, 5);
		tfirstFormData.bottom = new FormAttachment(100, -5);
		this.firstTransitionButton.setLayoutData(tfirstFormData);

		// Previous transition button
		this.previousTransitionButton = new Button(this.transitionsGroup, SWT.PUSH);
		this.previousTransitionButton.setImage(prevItemImage);
		this.previousTransitionButton.setToolTipText(Messages.GemAnalyzer_35);
		this.previousTransitionButton.setEnabled(false);
		final FormData tprevFormData = new FormData();
		tprevFormData.left = new FormAttachment(this.firstTransitionButton, 3);
		tprevFormData.bottom = new FormAttachment(100, -5);
		this.previousTransitionButton.setLayoutData(tprevFormData);

		// Next transition buttons
		this.nextTransitionButton = new Button(this.transitionsGroup, SWT.PUSH);
		this.nextTransitionButton.setImage(nextItemImage);
		this.nextTransitionButton.setToolTipText(Messages.GemAnalyzer_36);
		this.nextTransitionButton.setEnabled(false);
		final FormData tnextFormData = new FormData();
		tnextFormData.left = new FormAttachment(this.previousTransitionButton, 3);
		tnextFormData.bottom = new FormAttachment(100, -5);
		this.nextTransitionButton.setLayoutData(tnextFormData);

		// Last transition button
		this.lastTransitionButton = new Button(this.transitionsGroup, SWT.PUSH);
		this.lastTransitionButton.setImage(lastItemImage);
		this.lastTransitionButton.setToolTipText(Messages.GemAnalyzer_37);
		this.lastTransitionButton.setEnabled(false);
		final FormData tlastFormData = new FormData();
		tlastFormData.left = new FormAttachment(this.nextTransitionButton, 3);
		tlastFormData.bottom = new FormAttachment(100, -5);
		this.lastTransitionButton.setLayoutData(tlastFormData);

		// Lock ranks button
		this.lockRanksComboList = new Combo(this.transitionsGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		final Font lockRanksComboFont = setFontSize(this.lockRanksComboList.getFont(), 8);
		this.lockRanksComboList.setFont(lockRanksComboFont);
		final String[] items = new String[] { Messages.GemAnalyzer_83 };
		this.lockRanksComboList.setItems(items);
		this.lockRanksComboList.setToolTipText(Messages.GemAnalyzer_39);
		final FormData lockRanksFormData = new FormData();
		lockRanksFormData.left = new FormAttachment(this.lastTransitionButton, 10);
		lockRanksFormData.right = new FormAttachment(100, -5);
		lockRanksFormData.bottom = new FormAttachment(100, -5);
		this.lockRanksComboList.setLayoutData(lockRanksFormData);
	}

	/*
	 * Uses the right code window to display all calls associated with the
	 * collective operation displayed in the left code view window.
	 */
	private void displayCollectiveInfo() {
		final Envelope env = this.transitions.getCurrentTransition();
		final ArrayList<Envelope> matches = env.getCommunicator_matches();

		if (matches != null) {
			final int listSize = matches.size();
			for (int i = 0; i < listSize; i++) {
				final Envelope currentMatchEnvelope = matches.get(i);
				final String matchFilePathString = currentMatchEnvelope.getFilePath();
				final IFile sourceFile = GemUtilities.getSourceFile(matchFilePathString, this.activeFile);

				// Add the match to the right code view window
				this.rightViewer.add(new ListElement(sourceFile, sourceFile.getName() + Messages.GemAnalyzer_40
						+ currentMatchEnvelope.getLinenumber()
						+ Messages.GemAnalyzer_41
						+ currentMatchEnvelope.getRank(), currentMatchEnvelope.getLinenumber(), false));
			}
		}
	}

	/*
	 * Displays the specified envelope and it's matches if they exist.
	 */
	private void displayEnvelopes(Envelope env) {
		// If the envelope is null, it's a new interleaving, start over
		if (env == null) {
			return;
		}

		// Set the indexes for the highlighted line updater
		this.leftIndex = env.getLinenumber();
		boolean hasMatch = true;
		try {
			final Envelope match = env.getMatch_envelope();
			if (match != null) {
				this.rightIndex = match.getLinenumber();
			} else {
				hasMatch = false;
			}
		} catch (final Exception e) {
			GemUtilities.logExceptionDetail(e);
		}

		// Set font color for code window labels
		Color textColor = new Color(this.parent.getShell().getDisplay(), new RGB(0, 0, 255));
		this.leftCodeWindowLabel.setForeground(textColor);
		this.rightCodeWindowLabel.setForeground(textColor);

		// Build up the Strings for the Label text
		final String leftResult = getCallText(env, true);
		this.leftCodeWindowLabel.setText(leftResult);

		// If a match exists update the right side
		if (hasMatch) {
			final Envelope match = env.getMatch_envelope();
			final String rightResult = getCallText(match, false);
			this.rightCodeWindowLabel.setText(rightResult);
		} else {
			this.rightIndex = -1;
			this.rightViewer.getList().deselectAll();
			this.rightCodeWindowLabel.setText(""); //$NON-NLS-1$
		}

		if (env.getIssueIndex() == -1) {
			textColor = new Color(this.parent.getShell().getDisplay(), new RGB(255, 0, 0));
			this.rightCodeWindowLabel.setForeground(textColor);
			if (env.getFunctionName().equals("MPI_Abort")) { //$NON-NLS-1$
				this.rightCodeWindowLabel.setText(""); //$NON-NLS-1$
			} else if (this.transitions.hasDeadlock()) {
				this.rightCodeWindowLabel.setText(Messages.GemAnalyzer_46);
			} else {
				this.rightCodeWindowLabel.setText(Messages.GemAnalyzer_47);
			}
		}
	}

	/**
	 * Disposes of all shell windows when the instance of Eclipse that created
	 * them exits.
	 * 
	 * @param none
	 * @return void
	 */
	@Override
	public void dispose() {
		if (this.activeShells != null) {
			for (final Shell s : this.activeShells) {
				if (s != null) {
					s.dispose();
				}
			}
		}
		super.dispose();
	}

	/*
	 * Populates the view context menu.
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(this.terminateButton);
		this.terminateButton.setText(Messages.GemAnalyzer_48);
		manager.add(this.getHelp);
		this.getHelp.setText(Messages.GemAnalyzer_49);

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Populates the view pull-down menu.
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.terminateButton);
		this.terminateButton.setText(Messages.GemAnalyzer_50);
		manager.add(new Separator());
		manager.add(this.getHelp);
		this.getHelp.setText(Messages.GemAnalyzer_51);
		manager.add(new Separator());

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Contributes icons and actions to the tool bar.
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.terminateButton);
		manager.add(this.getHelp);

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Returns a String representing all information relative to a particular
	 * successful MPI call.
	 */
	private String getCallText(Envelope env, boolean isLeft) {
		final String newline = System.getProperty("line.separator"); //$NON-NLS-1$
		final String sourceFilePathString = env.getFilePath();
		final IFile sourceFile = GemUtilities.getSourceFile(sourceFilePathString, this.activeFile);
		final String fileName = sourceFile.getName();
		final StringBuffer stringBuffer = new StringBuffer();

		// determine which ranks are involved; by default only call's rank
		String ranks = Messages.GemAnalyzer_53 + env.getRank()
				+ (System.getProperty("os.name").contains("Windows") ? "\t" : newline); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		if (env.isCollective() && this.lockedRank == -1) {

			// If it was a group call then discover who else is here
			final int numRanksInvolved[] = new int[1];
			if (this.lockedRank == -1) {
				// Also see if we're looking at a single rank or not
				ranks = this.transitions.getRanksInvolved(numRanksInvolved);
			}

			// This is currently the max #processes GEM drop-down boxes allow
			final int MAX = 16;
			if (numRanksInvolved[0] > MAX) {
				// display only first ten ranks
				String tempRanks = ranks;
				int total = 0;
				for (int i = 0; i < MAX; i++) {
					// May be multi-digit numbers, hence this code
					total += tempRanks.indexOf(",") + 1; //$NON-NLS-1$
					tempRanks = tempRanks.substring(tempRanks.indexOf(",") + 1); //$NON-NLS-1$
				}
				stringBuffer.append(Messages.GemAnalyzer_22);
				stringBuffer.append(ranks.substring(0, total - 1));
				stringBuffer.append("..."); //$NON-NLS-1$
				stringBuffer.append(newline);
			} else {
				stringBuffer.append(Messages.GemAnalyzer_55);
				stringBuffer.append(ranks);
				stringBuffer.append(newline);
			}

			// count number of ":" to determine how many ranks involved
			int ranksCommunicatedTo = 0;
			String communicator = env.getCommunicator_ranks_string();
			while (communicator != null && communicator.contains(":")) { //$NON-NLS-1$
				communicator = communicator.substring(communicator.indexOf(":") + 1); //$NON-NLS-1$
				ranksCommunicatedTo++;
			}

			if (ranksCommunicatedTo != 0 && numRanksInvolved[0] / ranksCommunicatedTo > 1) {
				stringBuffer.append(Messages.GemAnalyzer_56);
				stringBuffer.append(numRanksInvolved[0] / ranksCommunicatedTo);
				stringBuffer.append(Messages.GemAnalyzer_57);
				stringBuffer.append(ranksCommunicatedTo);
				stringBuffer.append(Messages.GemAnalyzer_58);
			}
		}
		if (!env.isCollective()) {
			stringBuffer.append(ranks);
		}
		stringBuffer.append(Messages.GemAnalyzer_60);
		stringBuffer.append(fileName);
		stringBuffer.append("\t"); //$NON-NLS-1$
		stringBuffer.append(Messages.GemAnalyzer_61);
		stringBuffer.append(isLeft ? this.leftIndex : this.rightIndex);

		return stringBuffer.toString();
	}

	/*
	 * Adds MenuListeners to hook selections from the context menu.
	 */
	private void hookContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				GemAnalyzer.this.fillContextMenu(manager);
			}
		});
		final Menu leftMenu = menuMgr.createContextMenu(this.leftViewer.getControl());
		this.leftViewer.getControl().setMenu(leftMenu);
		getSite().registerContextMenu(menuMgr, this.leftViewer);

		final Menu rightMenu = menuMgr.createContextMenu(this.rightViewer.getControl());
		this.rightViewer.getControl().setMenu(rightMenu);
		getSite().registerContextMenu(menuMgr, this.rightViewer);
	}

	/**
	 * Initializing everything and creates threads to be used by the main UI
	 * thread to do updates.
	 * 
	 * @param resource
	 *            The resource to initialize this view with.
	 * @return void
	 */
	public void init(IResource resource) {

		this.activeFile = GemUtilities.adaptResource(resource);

		this.analyzerUpdateThread = new Thread() {
			@Override
			public void run() {
				// If just aborted then don't fill viewers with incomplete data
				if (GemAnalyzer.this.aborted) {
					Display.getDefault().syncExec(GemAnalyzer.this.disableTerminateButtonThread);
					Display.getDefault().syncExec(GemAnalyzer.this.clearAnalyzerThread);
					GemAnalyzer.this.aborted = false;
					return;
				}

				// if the log file contained no MPI calls
				if (GemAnalyzer.this.activeFile == null) {
					Display.getDefault().syncExec(GemAnalyzer.this.disableTerminateButtonThread);
					clear();
					return;
				}

				reset();
				parseSourceFile(GemAnalyzer.this.activeFile, GemAnalyzer.this.activeFile);
				Display.getDefault().syncExec(GemAnalyzer.this.disableTerminateButtonThread);
			}
		};

		this.clearAnalyzerThread = new Thread() {
			@Override
			public void run() {
				final String emptyString = ""; //$NON-NLS-1$
				GemAnalyzer.this.transitionsGroup.setText("Transition:" + " 0/0"); //$NON-NLS-1$ //$NON-NLS-2$
				GemAnalyzer.this.interleavingsGroup.setText("Interleaving:" + " 0/0"); //$NON-NLS-1$ //$NON-NLS-2$
				GemAnalyzer.this.firstTransitionButton.setEnabled(false);
				GemAnalyzer.this.previousTransitionButton.setEnabled(false);
				GemAnalyzer.this.nextTransitionButton.setEnabled(false);
				GemAnalyzer.this.lastTransitionButton.setEnabled(false);
				GemAnalyzer.this.firstInterleavingButton.setEnabled(false);
				GemAnalyzer.this.previousInterleavingButton.setEnabled(false);
				GemAnalyzer.this.nextInterleavingButton.setEnabled(false);
				GemAnalyzer.this.lastInterleavingButton.setEnabled(false);
				GemAnalyzer.this.deadlockInterleavingButton.setEnabled(false);
				GemAnalyzer.this.internalIssueOrderButton.setEnabled(false);
				GemAnalyzer.this.programOrderButton.setEnabled(false);
				GemAnalyzer.this.launchHpvButton.setEnabled(false);
				GemAnalyzer.this.browseCallsButton.setEnabled(false);
				GemAnalyzer.this.terminateButton.setEnabled(false);
				GemAnalyzer.this.errorMessageLabel.setText(emptyString);
				GemAnalyzer.this.transitions = null;
				GemAnalyzer.this.leftViewer.refresh();
				GemAnalyzer.this.rightViewer.refresh();
				GemAnalyzer.this.leftCodeWindowLabel.setText(emptyString);
				GemAnalyzer.this.rightCodeWindowLabel.setText(emptyString);
				final String[] items = new String[] { emptyString };
				GemAnalyzer.this.lockRanksComboList.setItems(items);
				GemUtilities.setTaskStatus(GemUtilities.TaskStatus.IDLE);
				GemAnalyzer.this.runGemButton.setEnabled(true);
			}
		};

		this.disableTerminateButtonThread = new Thread() {
			@Override
			public void run() {
				GemAnalyzer.this.terminateButton.setEnabled(false);
				GemAnalyzer.this.runGemButton.setEnabled(true);
				GemAnalyzer.this.internalIssueOrderButton.setEnabled(true);
				GemAnalyzer.this.programOrderButton.setEnabled(true);
			}
		};

		// start things up now
		this.terminateButton.setEnabled(true);
		this.runGemButton.setEnabled(false);
	}

	/*
	 * Launches the call browser as a separate shell window. This will yield a
	 * list of MPI calls sorted by interleaving -> rank -> MPI call. Calls are
	 * also color coded to distinguish the currently selected line in analyzer
	 * code windows (blue), as well as uncompleted calls due to a deadlock in
	 * the particular interleaving (red).
	 */

	/*
	 * Creates the actions associated with the action bar buttons and context
	 * menu items.
	 */
	private void makeActions() {
		this.getHelp = new Action() {
			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource("/org.eclipse.ptp.gem.help/html/analyzerView.html"); //$NON-NLS-1$
			}
		};
		this.getHelp.setToolTipText(Messages.GemAnalyzer_73);
		this.getHelp.setImageDescriptor(GemPlugin.getImageDescriptor("icons/help-contents.gif")); //$NON-NLS-1$

		this.terminateButton = new Action() {
			@Override
			public void run() {
				GemAnalyzer.this.aborted = true;
				GemUtilities.terminateOperation();
			}
		};
		this.terminateButton.setImageDescriptor(GemPlugin.getImageDescriptor("icons/progress_stop.gif")); //$NON-NLS-1$
		this.terminateButton.setToolTipText(Messages.GemAnalyzer_74);
		this.terminateButton.setEnabled(false);
	}

	/*
	 * Opens the editor with the current source file active and jump to the line
	 * number that is passed in.
	 */
	private void openEditor(int lineNum, IFile sourceFile) {
		try {
			final IEditorPart editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
					sourceFile, true);
			final IMarker marker = sourceFile.createMarker(IMarker.MARKER);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
			IDE.gotoMarker(editor, marker);
		} catch (final Exception e) {
			GemUtilities.logExceptionDetail(e);
		}
	}

	/*
	 * Returns whether or not the specified String has a matching number of left
	 * and right. parenthesis.
	 */
	private boolean parenthesesMatched(String call) {
		int numRightParens = 0;
		int numLeftParens = 0;

		call = removeComments(call);
		for (int i = 0; i < call.length(); i++) {
			if (call.charAt(i) == '(') {
				numLeftParens++;
			}
			if (call.charAt(i) == ')') {
				numRightParens++;
			}
		}
		return (numRightParens == numLeftParens);
	}

	/**
	 * Reads in the contents of the specified file and populates the
	 * ListViewers.
	 * 
	 * @param leftSourceFile
	 *            The handle on the file resource for the left code view window.
	 * @param rightSourceFile
	 *            The handle on the file resource for the right code view
	 *            window.
	 * @return void
	 */
	public void parseSourceFile(IFile leftSourceFile, IFile rightSourceFile) {

		final Boolean updateLeftFile = this.currLeftFile == null || !this.currLeftFile.equals(leftSourceFile);
		final Boolean updateRightFile = this.currRightFile == null || rightSourceFile == null
				|| !this.currRightFile.equals(rightSourceFile);
		this.currLeftFile = leftSourceFile;
		this.currRightFile = rightSourceFile;

		// Clear the left viewer list
		if (updateLeftFile) {
			this.leftViewer.getList().removeAll();
		}

		boolean isCollective = false;
		if (this.transitions != null && this.transitions.getCurrentTransition() != null) {
			isCollective = this.transitions.getCurrentTransition().isCollective();
		}

		// Clear the right viewer list
		if (isCollective || updateRightFile) {
			this.rightViewer.getList().removeAll();
		}

		// Populate the viewers with the source file contents
		InputStream leftSourceFileStream = null;
		InputStream rightSourceFileStream = null;
		String line = ""; //$NON-NLS-1$

		// Populate the left code window
		try {
			leftSourceFileStream = leftSourceFile.getContents(true);
			final Scanner s = new Scanner(leftSourceFileStream);
			int lineNum = 0;
			while (updateLeftFile && s.hasNext()) {
				lineNum++;
				line = s.nextLine();
				this.leftViewer.add(new ListElement(leftSourceFile, line, lineNum, false));
			}
		} catch (final CoreException e) {
			GemUtilities.logExceptionDetail(e);
			this.leftViewer.add(new ListElement(null, Messages.GemAnalyzer_76, -1, false));
		}

		// Populate the right code window
		try {
			if (rightSourceFile != null) {
				rightSourceFileStream = rightSourceFile.getContents();
				final Scanner s = new Scanner(rightSourceFileStream);
				int lineNum = 0;
				while (s.hasNext()) {
					lineNum++;
					line = s.nextLine();
					this.rightViewer.add(new ListElement(rightSourceFile, line, lineNum, false));
				}
			} else {
				final Envelope env = this.transitions.getCurrentTransition();
				if (env.isCollective()) {
					displayCollectiveInfo();
				} else {
					this.rightViewer.add(new ListElement(null, "", -1, false)); //$NON-NLS-1$
				}
			}
		} catch (final Exception e) {
			GemUtilities.logExceptionDetail(e);
			this.rightViewer.add(new ListElement(null, Messages.GemAnalyzer_78, -1, false));
		}

		// Close the open InputStreams
		try {
			if (leftSourceFileStream != null) {
				leftSourceFileStream.close();
			}
			if (rightSourceFileStream != null) {
				rightSourceFileStream.close();
			}
		} catch (final IOException e) {
			GemUtilities.logExceptionDetail(e);
		}
	}

	/*
	 * Removes comments from the specified call String.
	 */
	private String removeComments(String call) {
		// remove any c-style comments
		while (call.contains("/*")) { //$NON-NLS-1$
			if (!call.contains("*/")) { //$NON-NLS-1$
				break;
			}
			call = call.substring(0, call.indexOf("/*")) + call.substring(call.indexOf("*/") + 2); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// remove any c++-style comments
		while (call.contains("//")) { //$NON-NLS-1$
			call = call.substring(0, call.indexOf("//")); //$NON-NLS-1$
		}
		return call;
	}

	/*
	 * Resets everything to default values. Used when a new file is analyzed.
	 */
	private void reset() {
		// By making the current files null we force the code viewers to update
		this.currLeftFile = null;
		this.currRightFile = null;
		this.transitions.currentInterleaving = 0;
		this.transitions.currentTransitionIndex = 0;

		// Update labels and combo lists
		setMessageLabelText();
		setLockRankItems();
		setNumProcItems();

		// Update global indices
		this.leftIndex = 0;
		this.rightIndex = 0;
		this.lockedRank = -1;

		// Runtime group buttons
		this.deadlockInterleavingButton.setEnabled(this.transitions.getDeadlockInterleavings() != null);
		this.browseCallsButton.setEnabled(true);
		this.browseCallsButton.setImage(GemPlugin.getImage(GemPlugin.getImageDescriptor("icons/browse.gif"))); //$NON-NLS-1$
		this.launchHpvButton.setEnabled(true);

		// Update transition related items w/o displaying
		updateTransitionLabels(false);

		// TODO Let's clean this up
		// This section is needed since the scroll bars are unresponsive
		// until the view is fully created. For this reason we need to back up
		// to Transition 0 and update the labels.
		this.transitionLabelIndex = 0;
		this.transitionsGroup.setText("Transition " + this.transitionLabelIndex + "/" + this.transitionLabelCount); //$NON-NLS-1$ //$NON-NLS-2$ 
		this.transitions.currentTransitionIndex = -1;
		this.leftIndex = 0;
		this.rightIndex = 0;
		this.previousLeftIndex = 0;
		setButtonEnabledState();
		updateSelectedLine(true);
		setMessageLabelText();
		GemUtilities.setTaskStatus(GemUtilities.TaskStatus.IDLE);
	}

	/*
	 * To make an item visible we must expand each of its parents. Sadly the
	 * root must be expanded first and work its way down to the goal. So we
	 * recursively find parents and then expand once all of the current item's
	 * parents has been expanded.
	 */
	private void revealTreeItem(TreeItem callItem) {
		if (callItem.getParentItem() == null) {
			callItem.setExpanded(true);
		} else {
			revealTreeItem(callItem.getParentItem());
			callItem.setExpanded(true);
		}
	}

	/*
	 * Sets the enabled property of all buttons to the appropriate state.
	 */
	private void setButtonEnabledState() {
		this.firstTransitionButton.setEnabled(this.transitions.hasValidPreviousTransition(this.lockedRank));
		this.previousTransitionButton.setEnabled(this.transitions.hasValidPreviousTransition(this.lockedRank));
		this.nextTransitionButton.setEnabled(this.transitions.hasValidNextTransition(this.lockedRank));
		this.lastTransitionButton.setEnabled(this.transitions.hasValidNextTransition(this.lockedRank));
		this.firstInterleavingButton.setEnabled(this.transitions.hasPreviousInterleaving());
		this.previousInterleavingButton.setEnabled(this.transitions.hasPreviousInterleaving());
		this.nextInterleavingButton.setEnabled(this.transitions.hasNextInterleaving());
		this.lastInterleavingButton.setEnabled(this.transitions.hasNextInterleaving());
		this.deadlockInterleavingButton.setEnabled(this.transitions.getDeadlockInterleavings() != null);

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
	 * Populates the lock ranks combo-box with the correct number of entries for
	 * the current analyzation.
	 */
	private void setLockRankItems() {
		final String[] ranks = new String[this.numRanks + 1];
		ranks[0] = Messages.GemAnalyzer_83;
		for (int i = 1; i <= this.numRanks; i++) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(Messages.GemAnalyzer_84);
			stringBuffer.append((Integer) (i - 1));
			stringBuffer.append(Messages.GemAnalyzer_85);
			ranks[i] = stringBuffer.toString();
		}
		this.lockRanksComboList.setItems(ranks);
		this.lockRanksComboList.setText(ranks[0]);
	}

	/*
	 * Sets the label text in the interleavings buttons group.
	 */
	private void setMessageLabelText() {
		// This is for foreground coloring
		final Display display = this.parent.getShell().getDisplay();
		final Color RED = new Color(display, new RGB(255, 0, 0));
		final Color GREEN = new Color(display, new RGB(0, 200, 0));
		final Color BLUE = new Color(display, new RGB(0, 0, 255));

		// Interleaving label
		this.interleavingsGroup.setText("Interleaving: " + this.transitions.getCurrentInterleaving() + "/" //$NON-NLS-1$ //$NON-NLS-2$
				+ this.transitions.getTotalInterleavings());

		// Transition Label
		this.transitionsGroup.setText("Transition: " + this.transitionLabelIndex + "/" + this.transitionLabelCount); //$NON-NLS-1$ //$NON-NLS-2$

		// Error message label
		if (this.transitions.hasDeadlock()) {
			this.errorMessageLabel.setForeground(RED);
			final ArrayList<Integer> deadlockList = this.transitions.getDeadlockInterleavings();
			String deadlocks = ""; //$NON-NLS-1$
			if (deadlockList.size() == 1) {
				deadlocks = deadlockList.get(0).toString();
			} else {
				for (int i = 0; i < deadlockList.size(); i++) {
					final String num = deadlockList.get(i).toString();
					deadlocks += (i != deadlockList.size() - 1) ? num + ", " : num; //$NON-NLS-1$
				}
			}
			String errorMsg = Messages.GemAnalyzer_89;
			errorMsg += deadlocks.length() == 1 ? " " : "s "; //$NON-NLS-1$ //$NON-NLS-2$
			this.errorMessageLabel.setText(errorMsg + deadlocks);
		} else if (this.transitions.hasAssertion()) {
			this.errorMessageLabel.setForeground(RED);
			this.errorMessageLabel.setText(Messages.GemAnalyzer_91 + this.transitions.getTotalInterleavings());
		} else if (this.transitions.hasError()) {
			this.errorMessageLabel.setForeground(RED);
			this.errorMessageLabel.setText(Messages.GemAnalyzer_92);
		} else {
			if (this.transitions.hasResourceLeak() || this.transitions.hasFIB()) {
				this.errorMessageLabel.setForeground(BLUE);
				this.errorMessageLabel.setText(Messages.GemAnalyzer_93);
			} else {
				this.errorMessageLabel.setForeground(GREEN);
				this.errorMessageLabel.setText(Messages.GemAnalyzer_31);
			}
		}
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
		final Integer nprocs = GemPlugin.getDefault().getPreferenceStore().getInt(PreferenceConstants.GEM_PREF_NUMPROCS);
		this.setNumProcsComboList.setText(nprocs.toString());
	}

	/*
	 * This method eliminates displaying of redundant collective call
	 * information in the Analyzer code windows. If you are moving forward
	 * direction should be 1, if backward -1, otherwise the behavior is
	 * undefined.
	 */
	private Envelope skipRepeats(int direction) {
		Envelope env = this.transitions.getCurrentTransition();
		while (true) {
			if (env == null) {
				return null;
			}
			if (env.getLinenumber() == this.previousLeftIndex) {
				if (direction == 1) {
					env = this.transitions.getNextTransition(this.lockedRank);
				} else {
					env = this.transitions.getPreviousTransition(this.lockedRank);
				}
			} else {
				break;
			}
		}
		return env;
	}

	/**
	 * Updates the Analyzer view.
	 * 
	 * @param sourceFile
	 *            The source file resource to display in the Analyzer View's
	 *            code windows.
	 * @param transitions
	 *            The Transitions object holding holding all transitions per
	 *            interleaving.
	 * @return void
	 */
	public void update(IFile sourceFile, Transitions transitions) {
		if (transitions == null) {
			clear();
			return;
		}
		this.transitions = transitions;
		this.numRanks = transitions.getNumRanks();
		this.activeFile = sourceFile;
		Display.getDefault().syncExec(this.analyzerUpdateThread);
	}

	/*
	 * Only changes the code windows content, does not change the highlighting.
	 * First time through, current Envelope is null, no update needed.
	 */
	private void updateCodeViewers() {
		final String emptyString = ""; //$NON-NLS-1$
		Envelope env = this.transitions.getCurrentTransition();
		if (env != null) {
			final String nextLeftFileInfo = env.getFilePath();
			String nextRightFileInfo = emptyString;

			env = this.transitions.getCurrentTransition().getMatch_envelope();
			if (env != null) {
				nextRightFileInfo = env.getFilePath();
			}

			final IPath nextLeftFilePath = new Path(nextLeftFileInfo);
			final String nextLeftFileName = nextLeftFilePath.lastSegment();
			String nextRightFileName = emptyString;

			if (nextRightFileInfo.length() != 0) {
				final IPath nextRightFilePath = new Path(nextRightFileInfo);
				nextRightFileName = nextRightFilePath.lastSegment();
			}

			final boolean updateWindowContent = this.currLeftFile == null || this.currRightFile == null
					|| !nextLeftFileName.equals(this.currLeftFile.getName())
					|| !nextRightFileName.equals(this.currRightFile.getName()) || nextRightFileInfo.equals(emptyString);

			if (updateWindowContent) {
				final String projectName = this.activeFile.getProject().getName();
				final IProject currentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

				// Get the next left and right source files for the code viewers
				final IFile nextLeftSourceFile = GemUtilities.getSourceFile(nextLeftFileInfo, this.activeFile);
				if (!nextLeftSourceFile.exists()) {
					GemUtilities.refreshProject(currentProject);
				}

				IFile nextRightSourceFile = null;
				final boolean isEmptyPath = nextRightFileInfo.length() == 0;
				if (!isEmptyPath) {
					nextRightSourceFile = GemUtilities.getSourceFile(nextRightFileInfo, this.activeFile);
					if (!nextRightSourceFile.exists()) {
						GemUtilities.refreshProject(currentProject);
					}
				}

				// Now parse the files for new code viewer window content
				parseSourceFile(nextLeftSourceFile, (isEmptyPath) ? null : nextRightSourceFile);
			}
		}
	}

	/**
	 * Calling this method will update the drop down so that it has the correct
	 * number of processes displayed. This method is used when another location
	 * changes the number of processes preference.
	 * 
	 * @param none
	 * @return void
	 */
	public void updateDropDown() {
		final Integer nprocs = GemPlugin.getDefault().getPreferenceStore().getInt(PreferenceConstants.GEM_PREF_NUMPROCS);
		this.setNumProcsComboList.setText(nprocs.toString());
	}

	/*
	 * Goes to the First Transition and updates all relevant data
	 */
	private void updateFirstTransition(boolean update) {
		// Goes to null
		Envelope env = this.transitions.getFirstTransition(this.lockedRank);
		env = this.transitions.getCurrentTransition();

		if (env != null) {
			this.previousLeftIndex = env.getLinenumber();
		}

		if (env == null) {
			GemUtilities.showInformationDialog(Messages.GemAnalyzer_94);
			return;
		}

		if (update) {
			setButtonEnabledState();
			updateCodeViewers();
			displayEnvelopes(env);
			updateSelectedLine(true);
			this.transitionLabelIndex = 1;
			setMessageLabelText();
		}
	}

	/*
	 * Goes to the last transition and updates relevant information
	 */
	private void updateLastTransition() {
		Envelope env = this.transitions.getLastTransition(this.lockedRank);

		// stepToLastTransition puts us at the last iteration of Finalize
		// other code always assumes we are on the first iteration of
		// each call though, so here we go back to the first iteration
		if (env.getFunctionName().equals("MPI_Finalize")) { //$NON-NLS-1$
			while (true) {
				env = this.transitions.getPreviousTransition(this.lockedRank);

				if (env == null) {
					GemUtilities.showInformationDialog(Messages.GemAnalyzer_95);
					return;
				}
				// If there was only one finalize go back to it
				if (!env.getFunctionName().equals("MPI_Finalize")) { //$NON-NLS-1$
					env = this.transitions.getNextTransition(this.lockedRank);
					break;
				}
			}
		}

		if (env == null) {
			GemUtilities.showInformationDialog(Messages.GemAnalyzer_96);
			return;
		}
		this.previousLeftIndex = env.getLinenumber();
		setButtonEnabledState();
		updateCodeViewers();
		displayEnvelopes(env);
		updateSelectedLine(true);
		this.transitionLabelIndex = this.transitionLabelCount;
		setMessageLabelText();
	}

	/*
	 * This method is used to highlight ALL lines involved in a call when the
	 * call is spread over multiple lines in the source code file.
	 */
	private void updateLineCount() {
		final List leftList = this.leftViewer.getList();
		final List rightList = this.rightViewer.getList();
		final String emptyString = ""; //$NON-NLS-1$

		String curr = emptyString;
		this.leftLines = 1;
		if (this.leftIndex <= 0) {
			this.leftLines = 0;
			return;
		}
		while (this.leftLines < this.leftIndex + 1) {
			// Get current string
			curr = emptyString;
			for (int i = this.leftLines; i > 0; i--) {
				curr += leftList.getItem(this.leftIndex - i);
				curr = removeComments(curr);
			}

			if (this.parenthesesMatched(curr)) {
				break;
			}
			this.leftLines++;
		}
		// Be sure to include the MPI Call
		curr = curr.substring(0, curr.indexOf("(") - 1); //$NON-NLS-1$
		if (curr.trim().length() == 0) {
			this.leftLines++;
		}

		if (this.rightIndex == -1) {
			return;
		}
		this.rightLines = 1;

		while (this.rightLines < this.rightIndex + 1) {
			// Get current string
			curr = emptyString;
			for (int i = this.rightLines; i > 0; i--) {
				curr += rightList.getItem(this.rightIndex - i);
				curr = removeComments(curr);
			}

			if (this.parenthesesMatched(curr)) {
				break;
			}
			this.rightLines++;
		}
		curr = curr.substring(0, curr.indexOf("(") - 1); //$NON-NLS-1$
		if (curr.trim().length() == 0) {
			this.rightLines++;
		}
	}

	/*
	 * Go to the next transition. Returns 1 if successful, -1 if there was no
	 * where to go.
	 */
	private int updateNextTransition(boolean update) {
		int returnValue = 1;
		Envelope env = this.transitions.getNextTransition(this.lockedRank);

		if (env.isCollective() && this.lockedRank == -1) {
			env = skipRepeats(1);
		}

		if (env == null) {
			returnValue = -1;
		} else {
			this.previousLeftIndex = env.getLinenumber();
		}

		if (update) {
			setButtonEnabledState();
			updateCodeViewers();
			displayEnvelopes(env);
			updateSelectedLine(true);
			this.transitionLabelIndex++;
			setMessageLabelText();
		}
		return returnValue;
	}

	/*
	 * Goes to the previous transition and updates relevant data
	 */
	private void updatePreviousTransition() {
		Envelope env = this.transitions.getPreviousTransition(this.lockedRank);
		this.previousLeftIndex = env.getLinenumber();

		// Go back until you reach the first call or beginning
		if (env.isCollective() && this.lockedRank == -1) {
			while (true) {
				env = this.transitions.getPreviousTransition(this.lockedRank);
				if (env == null) {
					updateFirstTransition(true);
					return;
				}
				if (env.getLinenumber() != this.previousLeftIndex) {
					env = this.transitions.getNextTransition(this.lockedRank);
					break;
				}
			}
		}

		this.previousLeftIndex = env.getLinenumber();
		setButtonEnabledState();
		updateCodeViewers();
		displayEnvelopes(env);
		updateSelectedLine(true);
		this.transitionLabelIndex--;
		setMessageLabelText();
	}

	/*
	 * Updates the relative position of the scroll bar for the list viewers in
	 * the code windows.
	 */
	private void updateScrollBars() {
		final List leftList = this.leftViewer.getList();
		final List rightList = this.rightViewer.getList();

		try {
			if (leftList != null && leftList.getSelection().length != 0) {
				this.leftViewer.reveal(this.leftViewer.getElementAt(0));
				this.leftViewer.reveal(this.leftViewer.getElementAt(this.leftIndex - 1));
			}
			if (rightList != null && rightList.getSelection().length != 0) {
				this.rightViewer.reveal(this.rightViewer.getElementAt(0));
				this.rightViewer.reveal(this.rightViewer.getElementAt(this.rightIndex - 1));
			}
		} catch (final Exception e) {
			GemUtilities.logExceptionDetail(e);
		}
	}

	/*
	 * Updates which line of the log file that is currently in focus.
	 */
	private void updateSelectedLine(Boolean scroll) {
		updateCodeViewers();

		// Deselect Everything
		final List leftList = this.leftViewer.getList();
		final List rightList = this.rightViewer.getList();
		leftList.deselectAll();
		rightList.deselectAll();

		updateLineCount();

		// Update left view (-1, 0-based)
		for (int i = 0; i < this.leftLines; i++) {
			leftList.select((this.leftIndex - i) - 1);
		}

		// Update right view (-1, 0-based)
		for (int i = 0; i < this.rightLines; i++) {
			rightList.select((this.rightIndex - i) - 1);
		}

		if (scroll) {
			updateScrollBars();
		}
	}

	// ONLY CALL WHEN YOU HAVE STARTED A NEW INTERLEAVING!!!
	// As it searches it will never display the envelope
	// displayEnvelope decides whether or not the final destination is displayed
	private void updateTransitionLabels(boolean displayEnvelope) {
		updateFirstTransition(false);

		// because only incremented if next transition is also valid
		this.transitionLabelCount = 1;
		final Envelope env = this.transitions.getCurrentTransition();

		int test = 1;
		while (test == 1 && env != null) {
			if (this.transitions.hasValidNextTransition(this.lockedRank)) {
				this.transitionLabelCount++;
				test = updateNextTransition(false);
			} else {
				break;
			}
		}
		updateFirstTransition(displayEnvelope);
	}
}
