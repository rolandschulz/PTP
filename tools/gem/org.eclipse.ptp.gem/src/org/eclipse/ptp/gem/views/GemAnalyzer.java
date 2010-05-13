/*******************************************************************************
 * Copyright (c) 2009, 2010 University of Utah School of Computing
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

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ListViewer;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * The GEM Analyzer View.
 */
public class GemAnalyzer extends ViewPart {

	// The ID for this view
	public static final String ID = "org.eclipse.ptp.gem.views.GemAnalyzer"; //$NON-NLS-1$

	// Data structures and more complex members
	private Transitions transitions;
	private ArrayList<String> logContents;
	private LinkedList<Shell> activeShells;

	// Container objects and Viewers
	private Composite parent;
	private ListViewer leftViewer;
	private ListViewer rightViewer;
	private Action getHelp;

	// Simple members
	private int numRanks;
	private int lockedRank;
	private int errorIndex;
	private int errorCount;
	private Object[] errorCalls;
	private String currLeftFileName;
	private String currRightFileName;

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
	private Button launchIspUIButton;
	private Button browseCallsButton;
	private Button browseLeaksButton;
	private Button examineErrorsButton;
	private Button endEarlyButton;
	private Combo lockRanksComboList;
	private Combo setRankComboList;

	// Groups that need to be accessed multiple times
	private Group interleavingsGroup;
	private Group transitionsGroup;
	private Group stepOrderGroup;

	// SWT Labels
	private Label errorMessageLabel;
	private Label resourcLeakLabel;
	private Label leftCodeWindowLabel;
	private Label rightCodeWindowLabel;
	private CLabel leftCodeWindowExplenationLabel;
	private CLabel rightCodeWindowExplenationLabel;

	// Used for the transition label
	private int transitionIndex;
	private int transitionCount;

	// Things for highlighting the appropriate line in the code windows
	private int leftIndex;
	private int rightIndex;
	private int oldLeftIndex;

	// Listeners for the viewers
	private SelectionListener singleListener;
	private DoubleClickListener doubleListener;

	// Paths needed for various operations
	private String globalSourceFilePath;
	private String globalLogFilePath;
	private boolean globalCompile;
	private boolean globalRunIsp;

	// Threads
	private Thread runISPasThread;
	private Thread enableEndEarly;

	// Misc
	private Shell errShell;
	private int leftLines;
	private int rightLines;

	/**
	 * Constructor.
	 * 
	 * @param none
	 */
	public GemAnalyzer() {
		this.lockedRank = -1;
		this.activeShells = new LinkedList<Shell>();
		this.errorIndex = 1;
	}

	/**
	 * Creates all the envelopes for the transition lists from the specified log
	 * file.
	 * 
	 * @param logFilePath The absolute path to the log file to parse.
	 * @return void
	 */
	public void initTransitions(String logFilePath) {
		try {
			this.transitions = new Transitions(logFilePath);
		} catch (ParseException pe) {
			GemUtilities.showExceptionDialog(Messages.GemAnalyzer_0, pe);
			GemUtilities.logError(Messages.GemAnalyzer_0, pe);
		}
		// Reset local values
		reset();
	}

	/**
	 * Callback that allows us to create the viewer and initialize it.
	 * 
	 * @param parent The parent Composite for this View.
	 * @return void
	 */
	public void createPartControl(Composite parent) {
		this.parent = parent;

		// Create layout for the parent Composite
		GridLayout parentLayout = new GridLayout();
		parentLayout.numColumns = 1;
		parentLayout.marginHeight = 10;
		parentLayout.marginWidth = 10;
		parent.setLayout(parentLayout);

		// Create container for transition and interleaving button groups
		Composite buttonsComposite = new Composite(parent, SWT.NULL);
		buttonsComposite.setLayoutData(new GridData(SWT.NULL, SWT.FILL, true,
				false, 1, 1));
		buttonsComposite.setLayout(new FormLayout());

		// Create groups and selection listeners
		createTransitionsGroup(buttonsComposite);
		createInterleavingsGroup(buttonsComposite);
		createStepOrderGroup(buttonsComposite);
		createRuntimeGroup(parent);
		createCodeWindowsGroup(parent);
		createSelectionListeners();

		// create actions & connect to buttons, context menus and pull-downs
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	/**
	 * Passing the focus request to both code viewer's control.
	 * 
	 * @param none
	 * @return void
	 */
	public void setFocus() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			page.activate((IWorkbenchPart) this);
		}
	}

	/**
	 * Reads in the contents of the specified file and populates the
	 * ListViewers.
	 * 
	 * @param sourcefile A String containing the absolute path to the source
	 *            file.
	 * @return void
	 */
	public void parseSourceFile(String leftSourceFilePath,
			String rightSourceFilePath) {
		Boolean updateL = this.currLeftFileName == null
				|| !this.currLeftFileName.equals(leftSourceFilePath);
		Boolean updateR = this.currRightFileName == null
				|| rightSourceFilePath.equals("") //$NON-NLS-1$
				|| !this.currRightFileName.equals(rightSourceFilePath);
		this.currLeftFileName = leftSourceFilePath;
		this.currRightFileName = rightSourceFilePath;

		// Clear the left viewer list
		while (updateL && this.leftViewer.getElementAt(0) != null) {
			this.leftViewer.remove(this.leftViewer.getElementAt(0));
		}

		// Clear the right viewer list
		Boolean isCollective = false;
		if (this.transitions != null
				&& this.transitions.getCurrentTransition() != null
				&& this.transitions.getCurrentTransition().isCollective()) {
			isCollective = true;
		}
		while ((isCollective || updateR)
				&& this.rightViewer.getElementAt(0) != null) {
			this.rightViewer.remove(this.rightViewer.getElementAt(0));
		}

		// Populate the viewers with the source file contents
		try {
			File file = new File(leftSourceFilePath);
			Scanner s = new Scanner(file);
			int lineNum = 0;
			while (updateL && s.hasNext()) {
				lineNum++;
				String line = s.nextLine();
				this.leftViewer.add(new ListElement(leftSourceFilePath, line,
						lineNum, false));
			}
		} catch (java.io.FileNotFoundException f) {
			GemUtilities.showExceptionDialog(Messages.GemAnalyzer_1
					+ leftSourceFilePath, f);
			GemUtilities.logError(Messages.GemAnalyzer_2, f);
			this.leftViewer.add(new ListElement("", Messages.GemAnalyzer_3, -1, //$NON-NLS-1$
					false));
		}
		try {
			if (!rightSourceFilePath.equals("")) { //$NON-NLS-1$
				File file = new File(rightSourceFilePath);
				Scanner s = new Scanner(file);
				int lineNum = 0;
				while (s.hasNext()) {
					lineNum++;
					String line = s.nextLine();
					this.rightViewer.add(new ListElement(rightSourceFilePath,
							line, lineNum, false));
				}
			} else {
				Envelope env = this.transitions.getCurrentTransition();
				if (env.isCollective()) {
					displayCollectiveInfo();
				} else
					this.rightViewer.add(new ListElement("", "", -1, false)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (Exception e) {
			GemUtilities.showExceptionDialog(Messages.GemAnalyzer_4
					+ rightSourceFilePath, e);
			GemUtilities.logError(Messages.GemAnalyzer_5, e);
			this.rightViewer.add(new ListElement("", Messages.GemAnalyzer_6, //$NON-NLS-1$
					-1, false));
		}
	}

	/**
	 * Reads in the contents of the specified log file and populates the member
	 * ArrayList<String>.
	 * 
	 * @param logfile A String containing the absolute path and name of the log
	 *            file.
	 * @return void
	 */
	public void parseLogFile(String logFilePath) {
		// Create logContents if DNE
		if (this.logContents == null) {
			this.logContents = new ArrayList<String>();
		} else {
			this.logContents.clear();
		}

		try {
			File logfile = new File(logFilePath);
			Scanner s = new Scanner(logfile);
			this.numRanks = Integer.parseInt(s.nextLine());

			// loop through all the valid input in the log file
			while (s.hasNext()) {
				String line = s.nextLine();
				if (line.contains("DEADLOCK")) { //$NON-NLS-1$
					continue;
				} else {
					this.logContents.add(line);
				}
			}

		} catch (Exception e) {
			GemUtilities.showExceptionDialog(Messages.GemAnalyzer_7, e);
			GemUtilities.logError(Messages.GemAnalyzer_7, e);
		}
	}

	/**
	 * Creates a log file from the specified source file by running ISP
	 * 
	 * @param sourceFilePath The location of the source file.
	 * @param compile Whether or not to run ispcc.
	 * @return void
	 */
	public void generateLogFile(String sourceFilePath, boolean compile) {
		if (compile) {
			if (GemUtilities.doIspcc(sourceFilePath) != -1) {
				GemUtilities.doIsp(sourceFilePath);
			}
		} else {
			String exePath = GemPlugin.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.GEM_PREF_LAST_FILE);
			GemUtilities.doIsp(exePath);
		}
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
		Integer nprocs = GemPlugin.getDefault().getPreferenceStore().getInt(
				PreferenceConstants.GEM_PREF_NUMPROCS);
		this.setRankComboList.setText(nprocs.toString());
	}

	/**
	 * Starts the viewer by: Generating the log file, parsing it, and
	 * initializing everything.
	 * 
	 * @param sourceFilePath The fully qualified path to the source file.
	 * @param logFilePath The fully qualified path to the log file.
	 * @param compile Whether or not to run ispcc.
	 * @return void
	 */
	public void start(String sourceFilePath, String logFilePath,
			boolean compile, boolean runIsp) {
		globalSourceFilePath = sourceFilePath;
		globalLogFilePath = logFilePath;
		globalCompile = compile;
		globalRunIsp = runIsp;

		runISPasThread = new Thread() {
			public void run() {
				// These need to be done by a Thread
				if (globalCompile || globalRunIsp) {
					generateLogFile(globalSourceFilePath, globalCompile);
				}
				parseLogFile(globalLogFilePath);
				globalSourceFilePath = GemUtilities
						.getSourcePathFromLog(globalLogFilePath);

				// if the log file contained no mpi calls
				if (globalSourceFilePath.equals("")) { //$NON-NLS-1$
					endEarlyButton.setEnabled(false);
					return;
				}

				parseSourceFile(globalSourceFilePath, globalSourceFilePath);
				initTransitions(globalLogFilePath);
				endEarlyButton.setEnabled(false);
			}
		};

		enableEndEarly = new Thread() {
			public void run() {
				endEarlyButton.setEnabled(true);
			}
		};

		// syncExec blocks until next one starts
		Display.getDefault().syncExec(enableEndEarly);
		Display.getDefault().asyncExec(runISPasThread);
	}

	/**
	 * Disposes of all shell windows when the instance of Eclipse that created
	 * them exits.
	 * 
	 * @param none
	 * @return void
	 */
	public void dispose() {
		for (Shell s : this.activeShells) {
			if (s != null) {
				s.dispose();
			}
		}
		super.dispose();
	}

	/*
	 * Uses the rightViewer to display all other calls associated with this one
	 */
	private void displayCollectiveInfo() {
		Envelope env = this.transitions.getCurrentTransition();
		ArrayList<Envelope> envs = env.getCommunicator_matches();

		if (envs != null) {
			for (int i = 0; i < envs.size(); i++) {
				Envelope currEnv = envs.get(i);
				String fileName = currEnv.getFilename();
				int index = fileName.lastIndexOf("/"); //$NON-NLS-1$
				fileName = fileName.substring(index + 1, fileName.length());
				this.rightViewer.add(new ListElement(currEnv.getFilename(),
						fileName + Messages.GemAnalyzer_8
								+ currEnv.getLinenumber()
								+ Messages.GemAnalyzer_9 + currEnv.getRank(),
						currEnv.getLinenumber(), false));
			}
		}
	}

	/*
	 * Only changes the contents, does not change the highlighting.
	 */
	private void updateCodeViewers() {
		// First time through, current Envelope is null. No update needed
		if (this.transitions.getCurrentTransition() != null) {
			String newLeftFileName = this.transitions.getCurrentTransition()
					.getFilename();
			String newRightFileName = ""; //$NON-NLS-1$
			Envelope env = this.transitions.getCurrentTransition()
					.getMatch_envelope();
			if (env != null) {
				newRightFileName = env.getFilename();
			}

			if (!newLeftFileName.equals(this.currLeftFileName)
					|| !newRightFileName.equals(this.currRightFileName)
					|| newRightFileName.equals("")) { //$NON-NLS-1$
				parseSourceFile(newLeftFileName, newRightFileName);
			}
		}
	}

	/*
	 * Updates which line of the Log File is currently in focus.
	 */
	private void updateSelectedLine(Boolean scroll) {
		updateCodeViewers();

		// Deselect Everything
		List leftList = this.leftViewer.getList();
		List rightList = this.rightViewer.getList();
		leftList.deselectAll();
		rightList.deselectAll();

		updateLineCount();

		// Update left view
		for (int i = 0; i < this.leftLines; i++) {
			// -1, 0-based
			leftList.select((this.leftIndex - i) - 1);
		}

		// Update right view
		for (int i = 0; i < this.rightLines; i++) {
			// -1, 0-based
			rightList.select((this.rightIndex - i) - 1);
		}

		if (scroll) {
			updateScrollBars();
		}
	}

	/*
	 * This method is used to highlight ALL lines involved in a call
	 */
	private void updateLineCount() {
		List leftList = this.leftViewer.getList();
		List rightList = this.rightViewer.getList();

		String curr = ""; //$NON-NLS-1$
		this.leftLines = 1;
		if (this.leftIndex <= 0) {
			this.leftLines = 0;
			return;
		}
		while (leftLines < leftIndex + 1) {// to prevent accidentally going over
			// Get current string
			curr = ""; //$NON-NLS-1$
			for (int i = leftLines; i > 0; i--) {
				curr += leftList.getItem(leftIndex - i);
				curr = removeComments(curr);
			}

			if (this.parenthesesMatched(curr))
				break;
			leftLines++;
		}
		// Be sure to include the MPI Call
		curr = curr.substring(0, curr.indexOf("(") - 1); //$NON-NLS-1$
		if (curr.trim().length() == 0) {
			this.leftLines++;
		}

		if (rightIndex == -1) {
			return;
		}
		this.rightLines = 1;

		// to prevent accidentally going over
		while (rightLines < rightIndex + 1) {
			// Get current string
			curr = ""; //$NON-NLS-1$
			for (int i = rightLines; i > 0; i--) {
				curr += rightList.getItem(rightIndex - i);
				curr = removeComments(curr);
			}

			if (this.parenthesesMatched(curr)) {
				break;
			}
			rightLines++;
		}
		curr = curr.substring(0, curr.indexOf("(") - 1); //$NON-NLS-1$
		if (curr.trim().length() == 0) {
			this.rightLines++;
		}
	}

	/*
	 * Updates the relative position of the scrollbar for the list viewers.
	 */
	private void updateScrollBars() {
		try {
			if (this.leftViewer.getList() != null) {
				this.leftViewer.reveal(this.leftViewer.getElementAt(0));
			}
			if (this.leftViewer.getList() != null
					&& this.leftViewer.getList().getSelection()[0] != null) {
				this.leftViewer.reveal(this.leftViewer
						.getElementAt(this.leftIndex - 1));
			}
			if (this.rightViewer.getList() != null) {
				this.rightViewer.reveal(this.rightViewer.getElementAt(0));
			}
			if (this.rightViewer.getList() != null
					&& this.rightViewer.getList().getSelection()[0] != null) {
				this.rightViewer.reveal(this.rightViewer
						.getElementAt(this.rightIndex - 1));
			}
		} catch (Exception e) {
		}
	}

	/*
	 * Sets the label text in the interleavings buttons group.
	 */
	private void setMessageLabelText() {
		// This is for foreground coloring
		Display display = this.parent.getShell().getDisplay();
		Color RED = new Color(display, new RGB(255, 0, 0));
		Color GREEN = new Color(display, new RGB(0, 200, 0));

		// Interleaving label
		this.interleavingsGroup.setText(Messages.GemAnalyzer_10
				+ this.transitions.getCurrentInterleaving() + "/" //$NON-NLS-1$
				+ this.transitions.getTotalInterleavings());

		// Transition Label
		this.transitionsGroup.setText(Messages.GemAnalyzer_11 + transitionIndex
				+ "/" //$NON-NLS-1$
				+ this.transitionCount);

		// Error message label
		if (this.transitions.hasDeadlock()) {
			this.errorMessageLabel.setForeground(RED);
			ArrayList<Integer> deadlockList = this.transitions
					.getDeadlockInterleavings();
			String deadlocks = ""; //$NON-NLS-1$
			if (deadlockList.size() == 1) {
				deadlocks = deadlockList.get(0).toString();
			} else {
				for (int i = 0; i < deadlockList.size(); i++) {
					String num = deadlockList.get(i).toString();
					deadlocks += (i != deadlockList.size() - 1) ? num + ", " //$NON-NLS-1$
					: num;
				}
			}
			String errorMsg = Messages.GemAnalyzer_12;
			errorMsg += deadlocks.length() == 1 ? " " : "s "; //$NON-NLS-1$ //$NON-NLS-2$
			this.errorMessageLabel.setText(errorMsg + deadlocks);
		} else if (transitions.hasAssertion()) {
			this.errorMessageLabel.setForeground(RED);
			this.errorMessageLabel.setText(Messages.GemAnalyzer_13
					+ this.transitions.getTotalInterleavings());
		} else if (this.transitions.hasError()) {
			this.errorMessageLabel.setForeground(RED);
			this.errorMessageLabel.setText(Messages.GemAnalyzer_120);
		} else {
			this.errorMessageLabel.setForeground(GREEN);
			this.errorMessageLabel.setText(Messages.GemAnalyzer_14);
		}

		// Resource leak label
		if (this.transitions.hasResourceLeak()) {
			this.resourcLeakLabel.setForeground(RED);
			this.resourcLeakLabel.setText(Messages.GemAnalyzer_15);
		} else {
			this.resourcLeakLabel.setForeground(GREEN);
			this.resourcLeakLabel.setText(Messages.GemAnalyzer_16);
		}
	}

	/*
	 * Launches the call browser window as a separate shell window. This will
	 * yield a list of MPI calls sorted by interleaving -> rank -> MPI call.
	 */
	private void launchCallBrowser() {
		// Initialize and setup call browser shell
		IWorkbench wb = PlatformUI.getWorkbench();
		Display display = wb.getDisplay();
		Shell shell = new Shell();
		shell.setText(Messages.GemAnalyzer_17);
		shell.setImage(GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/magnified-trident.gif"))); //$NON-NLS-1$
		shell.setLayout(new GridLayout());

		CLabel fileNameLabel = new CLabel(shell, SWT.BORDER_SOLID);
		fileNameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		CLabel numProcsLabel = new CLabel(shell, SWT.BORDER_SOLID);
		numProcsLabel.setImage(GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/processes.gif"))); //$NON-NLS-1$
		numProcsLabel.setText(Messages.GemAnalyzer_18 + this.numRanks);
		numProcsLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Tree tree = new Tree(shell, SWT.BORDER);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(setFontSize(tree.getFont(), 8));

		// Declare everything we'll be working with
		TreeItem interleavingItem = null;
		TreeItem rankItem = null;
		int prevInterleaving = -1;
		int currInterleaving = -1;
		int prevRank = -1;
		int currRank = -1;
		int lineNum = -1;
		int end = this.logContents.size();

		// Parse the logfile and populate Call Browser with contents.
		for (int i = 0; i < end; i++) {
			String logEntry = this.logContents.get(i);
			Scanner s = new Scanner(logEntry);

			// Deal with the interleavings
			currInterleaving = s.nextInt();
			if (currInterleaving > prevInterleaving) {
				interleavingItem = new TreeItem(tree, SWT.NULL);
				interleavingItem.setText(Messages.GemAnalyzer_19
						+ currInterleaving);
				prevInterleaving = currInterleaving;
				currRank = 0;
				prevRank = -1;
			}

			// Deal with ranks
			currRank = s.nextInt();
			if (currRank > prevRank) {
				rankItem = new TreeItem(interleavingItem, SWT.NULL);
				rankItem.setText(Messages.GemAnalyzer_20 + currRank);
				prevRank = currRank;
			}

			// Leaks don't have any more ints and we don't want to display them
			if (!s.hasNextInt()) {
				continue;
			}

			// Find out if the call completed
			boolean completes = true;
			s.nextInt();
			s.nextInt();
			if (s.nextInt() == -1) {
				completes = false;
			}

			// Discover which interleaving this entry corresponds to
			int inter = Integer.parseInt(logEntry.substring(0, logEntry
					.indexOf(" "))); //$NON-NLS-1$

			// Grab the name of the source file
			int index = logEntry.lastIndexOf(" "); //$NON-NLS-1$
			String name = logEntry.substring(0, index);
			index = name.lastIndexOf("/"); //$NON-NLS-1$
			name = name.substring(index + 1, name.length());

			// Grab the line number at the end of the log-entry
			int lastSpaceIndex = logEntry.lastIndexOf(" "); //$NON-NLS-1$
			lineNum = Integer.parseInt(logEntry.substring(lastSpaceIndex + 1,
					logEntry.length()));

			// Create leaves for the rank branchesassembleSourceLine
			TreeItem callItem = new TreeItem(rankItem, SWT.NULL);
			String call = logContents.get(i);
			for (int c = 0; c < 5; c++) {
				call = call.substring(call.indexOf(" ") + 1); //$NON-NLS-1$
			}
			call = call.substring(0, call.indexOf(" ")); //$NON-NLS-1$
			callItem.setText(call
					+ " \t" + name + Messages.GemAnalyzer_21 + lineNum); //$NON-NLS-1$

			// Mark the current transition +1 b/c currTrans is 0-Based
			int currentInter = this.transitions.currentInterleaving + 1;
			if (lineNum == this.leftIndex && inter == currentInter) {
				callItem.setForeground(new Color(null, 0, 0, 255));
				// Comment this out if you don't want items expanded
				revealTreeItem(callItem);
			}
			// Mark the lines with errors
			if (!completes) {
				callItem.setForeground(new Color(null, 255, 0, 0));
			}
		}

		// Open up the Call Browser window with the specified size
		shell.setSize(550, 550);
		shell.open();
		this.activeShells.add(shell);

		// Set up the event loop
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
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
	 * Launches the resource leak browser window as a separate shell window.
	 * This will yield a listing of all resource leaks found if any.
	 */
	private void launchLeakBrowser() {
		ArrayList<Envelope> leakList = this.transitions.getResourceLeakList();

		// Initialize and setup leak browser shell
		IWorkbench wb = PlatformUI.getWorkbench();
		Display display = wb.getDisplay();
		Shell shell = new Shell();
		shell.setText(Messages.GemAnalyzer_22);
		shell.setImage(GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/magnified-trident.gif"))); //$NON-NLS-1$
		shell.setLayout(new GridLayout());

		CLabel fileNameLabel = new CLabel(shell, SWT.BORDER_SOLID);
		fileNameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		CLabel hintLabel = new CLabel(shell, SWT.BORDER_SOLID);
		hintLabel.setImage(GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/expand-tree.gif"))); //$NON-NLS-1$
		hintLabel.setText(Messages.GemAnalyzer_23);
		hintLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Tree tree = new Tree(shell, SWT.BORDER);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(setFontSize(tree.getFont(), 8));

		// Declare everything we'll be working with
		TreeItem leakItem = null;
		TreeItem fileItem = null;

		// Parse the resource leak list and populate tree.
		int listSize = leakList.size();
		for (int i = 0; i < listSize; i++) {
			Envelope env = leakList.get(i);
			leakItem = new TreeItem(tree, SWT.NULL);
			leakItem.setText(Messages.GemAnalyzer_24 + i + ": " //$NON-NLS-1$
					+ env.getLeakResource());

			String sourceFilePath = env.getFilename();

			String base = ResourcesPlugin.getWorkspace().getRoot()
					.getLocation()
					+ ""; //$NON-NLS-1$
			String name = sourceFilePath.substring(base.length());

			fileItem = new TreeItem(leakItem, SWT.NULL);
			fileItem.setText(name + Messages.GemAnalyzer_25
					+ env.getLinenumber());
		}

		// Create a listener to allow "jumps" to the Editor
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item.toString().contains(Messages.GemAnalyzer_26)) {
					String lineString = e.item.toString().substring(
							e.item.toString().lastIndexOf(" ")); //$NON-NLS-1$
					lineString = lineString.substring(1,
							lineString.length() - 1);
					int lineNum = Integer.parseInt(lineString);
					String fileName = e.item.toString();
					fileName = fileName.substring(fileName.indexOf("{") + 1); //$NON-NLS-1$
					fileName = fileName.substring(0, fileName
							.indexOf(Messages.GemAnalyzer_27) - 1);
					openEditor(lineNum, fileName);
				}
			}
		};
		tree.addSelectionListener(listener);

		// Open up the Call Browser window with the specified size
		shell.setSize(500, 400);
		shell.open();
		this.activeShells.add(shell);

		// Set up the event loop
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/*
	 * Launches the resource leak browser window as a separate shell window.
	 * This will yield a listing of all resource leaks found if any.
	 */
	private void launchErrorBrowser() {

		// Initialize and setup leak browser shell
		IWorkbench wb = PlatformUI.getWorkbench();
		Display display = wb.getDisplay();
		Shell shell = new Shell();
		this.errShell = shell;
		shell.setText(Messages.GemAnalyzer_28);
		shell.setImage(GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/magnified-trident.gif"))); //$NON-NLS-1$
		shell.setLayout(new GridLayout());

		CLabel fileNameLabel = new CLabel(shell, SWT.BORDER_SOLID);
		fileNameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		CLabel hintLabel = new CLabel(shell, SWT.BORDER_SOLID);
		hintLabel.setImage(GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/expand-tree.gif"))); //$NON-NLS-1$
		hintLabel.setText(Messages.GemAnalyzer_29);
		hintLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Tree tree = new Tree(shell, SWT.BORDER);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(setFontSize(tree.getFont(), 8));

		// Declare everything we'll be working with
		TreeItem fileItem = null;

		// Parse the resource leak list and populate tree.
		int listSize = this.errorCount;
		for (int i = 0; i < listSize; i++) {
			Envelope env = (Envelope) this.errorCalls[i];

			// If this error is not part of this interleaving skip it
			if (env.getInterleaving() != this.transitions
					.getCurrentInterleaving()) {
				continue;
			}

			String sourceFilePath = env.getFilename();
			String base = ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toPortableString();
			String name = sourceFilePath.substring(base.length());

			fileItem = new TreeItem(tree, SWT.NULL);
			fileItem.setText(env.getFunctionName()
					+ "   \t" + name + Messages.GemAnalyzer_30 //$NON-NLS-1$
					+ env.getLinenumber());
		}

		// Create a listener to allow "jumps" to the Editor
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item.toString().contains(Messages.GemAnalyzer_31)) {
					String lineString = e.item.toString().substring(
							e.item.toString().lastIndexOf(" ")); //$NON-NLS-1$
					lineString = lineString.substring(1,
							lineString.length() - 1);
					int lineNum = Integer.parseInt(lineString);
					String fileName = e.item.toString();
					fileName = fileName.substring(fileName.indexOf("\t") + 2); //$NON-NLS-1$
					fileName = fileName.substring(0, fileName
							.indexOf(Messages.GemAnalyzer_32) - 1);
					openEditor(lineNum, fileName);
					errShell.forceFocus();
				}
			}
		};
		tree.addSelectionListener(listener);

		// Open up the Call Browser window with the specified size
		shell.setSize(500, 400);
		shell.open();
		this.activeShells.add(shell);

		// Set up the event loop
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/*
	 * Sets the enabled property of all buttons to the appropriate state.
	 */
	private void setButtonEnabledState() {
		this.firstTransitionButton.setEnabled(this.transitions
				.hasValidPreviousTransition(this.lockedRank));
		this.previousTransitionButton.setEnabled(this.transitions
				.hasValidPreviousTransition(this.lockedRank));
		this.nextTransitionButton.setEnabled(this.transitions
				.hasValidNextTransition(this.lockedRank));
		this.lastTransitionButton.setEnabled(this.transitions
				.hasValidNextTransition(this.lockedRank));
		this.firstInterleavingButton.setEnabled(this.transitions
				.hasPreviousInterleaving());
		this.previousInterleavingButton.setEnabled(this.transitions
				.hasPreviousInterleaving());
		this.nextInterleavingButton.setEnabled(this.transitions
				.hasNextInterleaving());
		this.lastInterleavingButton.setEnabled(this.transitions
				.hasNextInterleaving());
		this.deadlockInterleavingButton.setEnabled(this.transitions
				.getDeadlockInterleavings() != null);

	}

	/*
	 * Displays the specified envelope and it's matches if they exist.
	 */
	private void displayEnvelopes(Envelope env) {
		// If the env is null, new interleaving, start over
		if (env == null) {
			this.leftCodeWindowLabel.setText(""); //$NON-NLS-1$
			this.rightCodeWindowLabel.setText(""); //$NON-NLS-1$
			return;
		}

		// Set the indexes for the highlighted line updater
		this.leftIndex = env.getLinenumber();
		boolean hasMatch = true;
		try {
			this.rightIndex = env.getMatch_envelope().getLinenumber();
		} catch (Exception e) {
			hasMatch = false;
		}

		// Set font color for code window labels
		Color textColor = new Color(this.parent.getShell().getDisplay(),
				new RGB(0, 0, 255));
		this.leftCodeWindowLabel.setForeground(textColor);
		this.rightCodeWindowLabel.setForeground(textColor);

		// Build up the Strings for the Label text
		String leftResult = getCallText(env, true);
		this.leftCodeWindowLabel.setText(leftResult);

		// If a match exists update the right side
		if (hasMatch) {
			Envelope match = env.getMatch_envelope();
			String rightResult = getCallText(match, false);
			this.rightCodeWindowLabel.setText(rightResult);
		} else {
			this.rightCodeWindowLabel.setText(""); //$NON-NLS-1$
			this.rightIndex = -1;
			this.rightViewer.getList().deselectAll();
		}

		if (env.getIssueIndex() == -1) {
			textColor = new Color(this.parent.getShell().getDisplay(), new RGB(
					255, 0, 0));
			this.rightCodeWindowLabel.setForeground(textColor);

			if (env.getFunctionName().equals("MPI_Abort")) { //$NON-NLS-1$
				this.rightCodeWindowLabel.setText(""); //$NON-NLS-1$
			} else if (this.transitions.hasDeadlock()) {
				this.rightCodeWindowLabel.setText(Messages.GemAnalyzer_36);
			} else {
				this.rightCodeWindowLabel.setText(Messages.GemAnalyzer_37);
			}
		}
	}

	/*
	 * Returns a String representing all information relative to a particular
	 * successful MPI call.
	 */
	private String getCallText(Envelope env, boolean isLeft) {
		String filename = env.getFilename();
		filename = filename.substring(filename.lastIndexOf('/') + 1);

		// determine which ranks are involved; by default only call's rank
		String ranks = Messages.GemAnalyzer_38 + env.getRank() + "\n"; //$NON-NLS-1$
		if (env.isCollective() && this.lockedRank == -1) {
			// If it was a group call then discover who else is here, unless of
			// course we are currently only concerned about a single rank
			int[] totalSkipped = new int[1];
			if (this.lockedRank == -1) {
				ranks = this.transitions.getRanksInvolved(totalSkipped);
			}

			int MAX = 16;
			if (totalSkipped[0] > MAX) {
				// display only first ten
				String temp = ranks;
				int total = 0;
				for (int i = 0; i < MAX; i++) {
					total += temp.indexOf(",") + 1; //$NON-NLS-1$
					temp = temp.substring(temp.indexOf(",") + 1); //$NON-NLS-1$
				}
				ranks = Messages.GemAnalyzer_39 + ranks.substring(0, total - 1)
						+ "..." //$NON-NLS-1$
						+ "\n"; //$NON-NLS-1$
			} else {
				ranks = Messages.GemAnalyzer_40 + ranks + "\n"; //$NON-NLS-1$
			}

			// count number of ":" to determine how many ranks involved
			int ranksCommunicatedTo = 0;
			String communicator = env.getCommunicator_ranks_string();
			while (communicator != null && communicator.contains(":")) { //$NON-NLS-1$
				communicator = communicator
						.substring(communicator.indexOf(":") + 1); //$NON-NLS-1$
				ranksCommunicatedTo++;
			}

			if (ranksCommunicatedTo != 0
					&& totalSkipped[0] / ranksCommunicatedTo > 1) {
				ranks = Messages.GemAnalyzer_41 + totalSkipped[0]
						/ ranksCommunicatedTo + Messages.GemAnalyzer_42
						+ ranksCommunicatedTo + Messages.GemAnalyzer_43;
			}
		}
		String result = ""; //$NON-NLS-1$
		if (isLeft) {
			result = ranks + Messages.GemAnalyzer_44 + filename
					+ Messages.GemAnalyzer_45 + this.leftIndex;
		} else {
			result = ranks + Messages.GemAnalyzer_46 + filename
					+ Messages.GemAnalyzer_47 + this.rightIndex;
		}
		return result;
	}

	/*
	 * Populates the lock ranks combo-box with the correct number of entries for
	 * the current analyzation.
	 */
	private void setLockRankItems() {
		final String[] ranks = new String[this.numRanks + 1];
		ranks[0] = Messages.GemAnalyzer_48;
		for (int i = 1; i <= this.numRanks; i++) {
			ranks[i] = (Messages.GemAnalyzer_49 + (Integer) (i - 1))
					+ Messages.GemAnalyzer_50;
		}
		this.lockRanksComboList.setItems(ranks);
		this.lockRanksComboList.setText(Messages.GemAnalyzer_51);
	}

	/*
	 * Populates the set ranks combo-box with choices the user can use to set
	 * the number of processes for the next analyzation.
	 */
	private void setRankItems() {
		final String[] ranks = new String[16];
		for (int i = 1; i <= 16; i++) {
			ranks[i - 1] = ((Integer) i).toString();
		}
		this.setRankComboList.setItems(ranks);
		Integer nprocs = GemPlugin.getDefault().getPreferenceStore().getInt(
				PreferenceConstants.GEM_PREF_NUMPROCS);
		this.setRankComboList.setText(nprocs.toString());
	}

	/*
	 * Resets everything to default values. Used when a new file is analyzed.
	 */
	private void reset() {
		// By making the current files "" we force the code viewers to update
		this.currLeftFileName = ""; //$NON-NLS-1$
		this.currRightFileName = ""; //$NON-NLS-1$
		transitions.currentInterleaving = 0;
		transitions.currentTransition = 0;

		// Update labels and combo lists
		setMessageLabelText();
		setLockRankItems();
		setRankItems();

		// Update global indices
		this.leftIndex = 0;
		this.rightIndex = 0;
		this.lockedRank = -1;
		this.errorIndex = 1;

		// Runtime group buttons
		this.deadlockInterleavingButton.setEnabled(transitions
				.getDeadlockInterleavings() != null);
		this.browseCallsButton.setEnabled(true);
		this.browseCallsButton.setText(Messages.GemAnalyzer_52);
		this.browseCallsButton.setImage(GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/browse.gif"))); //$NON-NLS-1$
		this.launchIspUIButton.setEnabled(true);

		// Browse resource leaks button
		this.browseLeaksButton.setEnabled(this.transitions.hasResourceLeak());
		if (this.browseLeaksButton.isEnabled()) {
			this.browseLeaksButton.setImage(GemPlugin.getImage(GemPlugin
					.getImageDescriptor("icons/browse.gif"))); //$NON-NLS-1$
			this.browseLeaksButton.setText(Messages.GemAnalyzer_53);
		} else {
			this.browseLeaksButton.setImage(GemPlugin.getImage(GemPlugin
					.getImageDescriptor("icons/no-error.gif"))); //$NON-NLS-1$
			this.browseLeaksButton.setText(Messages.GemAnalyzer_54);
		}

		// Error button for deadlocks OR assertion violations
		updateErrorButtonState();

		// Clear the code window info labels
		this.leftCodeWindowLabel.setText("\n"); //$NON-NLS-1$
		this.rightCodeWindowLabel.setText("\n"); //$NON-NLS-1$

		// GoToFirstTransition(); Called by UpdateTransitionVars
		updateTransitionVars(false);

		// This is UGLY section is needed since the scroll bars are unresponsive
		// until the view is fully created. For this reason we need to back up
		// to Transition 0 and update the labels.
		this.transitionIndex = 0;
		this.transitionsGroup.setText(Messages.GemAnalyzer_55
				+ this.transitionIndex + "/" //$NON-NLS-1$
				+ this.transitionCount);
		this.transitions.currentTransition = -1;
		this.leftIndex = 0;
		this.rightIndex = 0;
		this.oldLeftIndex = 0;
		setButtonEnabledState();
		updateSelectedLine(true);
		setMessageLabelText();
		// END UGLY SECTION
	}

	/*
	 * Given the initial Font, this helper method returns that Font with the new
	 * specified size.
	 */
	private Font setFontSize(Font font, int size) {
		FontData[] fontData = font.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(size);
		}
		return new Font(this.parent.getDisplay(), fontData);
	}

	/*
	 * Opens the editor with the current source file active and jump to the line
	 * number that is passed in.
	 */
	private void openEditor(int lineNum, String sourceFileString) {
		try {
			IFile sourceFile = getIFile(sourceFileString);
			IEditorPart editor = org.eclipse.ui.ide.IDE.openEditor(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
					sourceFile, true);
			IMarker marker = sourceFile.createMarker(IMarker.MARKER);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
			org.eclipse.ui.ide.IDE.gotoMarker(editor, marker);
		} catch (Exception e) {
			GemUtilities.showExceptionDialog(Messages.GemAnalyzer_56, e);
			GemUtilities.logError(Messages.GemAnalyzer_56, e);
		}
	}

	/*
	 * Returns an IFile from the specified path and file name. (only works if
	 * the file is already in the workspace)
	 */
	private IFile getIFile(String sourceFilePath) {
		IPath filePath = new Path(sourceFilePath);
		IFile sourceFile = ResourcesPlugin.getWorkspace().getRoot().getFile(
				filePath);
		return sourceFile;
	}

	/*
	 * Creates the actions associated with the action bar buttons and context
	 * menu items.
	 */
	private void makeActions() {
		this.getHelp = new Action() {
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(
						"/org.eclipse.ptp.gem.help/html/analyzerView.html"); //$NON-NLS-1$
			}
		};
		this.getHelp.setText(""); //$NON-NLS-1$
		this.getHelp.setToolTipText(Messages.GemAnalyzer_57);
		this.getHelp.setImageDescriptor(GemPlugin
				.getImageDescriptor("icons/help-contents.gif")); //$NON-NLS-1$
	}

	/*
	 * Adds MenuListeners to hook selections from the context menu.
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				GemAnalyzer.this.fillContextMenu(manager);
			}
		});
		Menu leftMenu = menuMgr.createContextMenu(this.leftViewer.getControl());
		this.leftViewer.getControl().setMenu(leftMenu);
		getSite().registerContextMenu(menuMgr, this.leftViewer);

		Menu rightMenu = menuMgr.createContextMenu(this.rightViewer
				.getControl());
		this.rightViewer.getControl().setMenu(rightMenu);
		getSite().registerContextMenu(menuMgr, this.rightViewer);
	}

	/*
	 * Calls finer grained methods, populating the view action bar.
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/*
	 * Populates the view pull-down menu.
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.getHelp);
		manager.add(new Separator());

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Populates the view context menu.
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(this.getHelp);
		this.getHelp.setText(Messages.GemAnalyzer_58);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Contributes icons and actions to the tool bar.
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.getHelp);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createTransitionsGroup(Composite parent) {
		// Get images for buttons from image cache
		Image firstItemImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/first-item.gif")); //$NON-NLS-1$
		Image lastItemImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/last-item.gif")); //$NON-NLS-1$
		Image prevItemImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/prev-item.gif")); //$NON-NLS-1$
		Image nextItemImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/next-item.gif")); //$NON-NLS-1$

		// Group and FormLayout data for transition buttons and labels
		this.transitionsGroup = new Group(parent, SWT.SHADOW_IN);
		this.transitionsGroup.setText(Messages.GemAnalyzer_59);
		this.transitionsGroup.setToolTipText(Messages.GemAnalyzer_60);
		FormData transitionsFormData = new FormData();
		transitionsFormData.left = new FormAttachment(0, 5);
		transitionsFormData.bottom = new FormAttachment(100, -5);
		this.transitionsGroup.setLayoutData(transitionsFormData);
		this.transitionsGroup.setLayout(new FormLayout());

		// First transition button
		this.firstTransitionButton = new Button(this.transitionsGroup, SWT.PUSH);
		this.firstTransitionButton.setImage(firstItemImage);
		this.firstTransitionButton.setToolTipText(Messages.GemAnalyzer_61);
		this.firstTransitionButton.setEnabled(false);
		FormData tfirstFormData = new FormData();
		tfirstFormData.left = new FormAttachment(0, 5);
		tfirstFormData.bottom = new FormAttachment(100, -5);
		this.firstTransitionButton.setLayoutData(tfirstFormData);

		// Previous transition button
		this.previousTransitionButton = new Button(this.transitionsGroup,
				SWT.PUSH);
		this.previousTransitionButton.setImage(prevItemImage);
		this.previousTransitionButton.setToolTipText(Messages.GemAnalyzer_62);
		this.previousTransitionButton.setEnabled(false);
		FormData tprevFormData = new FormData();
		tprevFormData.left = new FormAttachment(this.firstTransitionButton, 3);
		tprevFormData.bottom = new FormAttachment(100, -5);
		this.previousTransitionButton.setLayoutData(tprevFormData);

		// Next transition buttons
		this.nextTransitionButton = new Button(this.transitionsGroup, SWT.PUSH);
		this.nextTransitionButton.setImage(nextItemImage);
		this.nextTransitionButton.setToolTipText(Messages.GemAnalyzer_63);
		this.nextTransitionButton.setEnabled(false);
		FormData tnextFormData = new FormData();
		tnextFormData.left = new FormAttachment(this.previousTransitionButton,
				3);
		tnextFormData.bottom = new FormAttachment(100, -5);
		this.nextTransitionButton.setLayoutData(tnextFormData);

		// Last transition button
		this.lastTransitionButton = new Button(this.transitionsGroup, SWT.PUSH);
		this.lastTransitionButton.setImage(lastItemImage);
		this.lastTransitionButton.setToolTipText(Messages.GemAnalyzer_64);
		this.lastTransitionButton.setEnabled(false);
		FormData tlastFormData = new FormData();
		tlastFormData.left = new FormAttachment(this.nextTransitionButton, 3);
		tlastFormData.bottom = new FormAttachment(100, -5);
		this.lastTransitionButton.setLayoutData(tlastFormData);

		// Lock ranks button
		this.lockRanksComboList = new Combo(this.transitionsGroup,
				SWT.DROP_DOWN | SWT.READ_ONLY);
		Font lockRanksComboFont = setFontSize(
				this.lockRanksComboList.getFont(), 8);
		this.lockRanksComboList.setFont(lockRanksComboFont);
		String[] items = new String[] { Messages.GemAnalyzer_65 };
		this.lockRanksComboList.setItems(items);
		this.lockRanksComboList.setToolTipText(Messages.GemAnalyzer_66);
		FormData lockRanksFormData = new FormData();
		lockRanksFormData.left = new FormAttachment(this.lastTransitionButton,
				10);
		lockRanksFormData.right = new FormAttachment(100, -5);
		lockRanksFormData.bottom = new FormAttachment(100, -5);
		this.lockRanksComboList.setLayoutData(lockRanksFormData);
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createInterleavingsGroup(Composite parent) {
		// Get images for buttons from image cache
		Image firstItemImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/first-item.gif")); //$NON-NLS-1$
		Image lastItemImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/last-item.gif")); //$NON-NLS-1$
		Image prevItemImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/prev-item.gif")); //$NON-NLS-1$
		Image nextItemImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/next-item.gif")); //$NON-NLS-1$
		Image deadlockImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/deadlock.gif")); //$NON-NLS-1$

		// Group and FormLayout data for interleaving buttons and labels
		this.interleavingsGroup = new Group(parent, SWT.SHADOW_IN);
		this.interleavingsGroup.setText(Messages.GemAnalyzer_67);
		this.interleavingsGroup.setToolTipText(Messages.GemAnalyzer_68);
		FormData interleavingsFormData = new FormData();
		interleavingsFormData.bottom = new FormAttachment(100, -5);
		interleavingsFormData.left = new FormAttachment(this.transitionsGroup,
				20);
		this.interleavingsGroup.setLayoutData(interleavingsFormData);
		this.interleavingsGroup.setLayout(new FormLayout());

		// First interleaving button
		this.firstInterleavingButton = new Button(this.interleavingsGroup,
				SWT.PUSH);
		this.firstInterleavingButton.setImage(firstItemImage);
		this.firstInterleavingButton.setToolTipText(Messages.GemAnalyzer_69);
		this.firstInterleavingButton.setEnabled(false);
		FormData ifirstFormData = new FormData();
		ifirstFormData.left = new FormAttachment(0, 5);
		ifirstFormData.bottom = new FormAttachment(100, -5);
		this.firstInterleavingButton.setLayoutData(ifirstFormData);

		// Previous interleaving button
		this.previousInterleavingButton = new Button(this.interleavingsGroup,
				SWT.PUSH);
		this.previousInterleavingButton.setImage(prevItemImage);
		this.previousInterleavingButton.setToolTipText(Messages.GemAnalyzer_70);
		this.previousInterleavingButton.setEnabled(false);
		FormData iprevFormData = new FormData();
		iprevFormData.left = new FormAttachment(this.firstInterleavingButton, 3);
		iprevFormData.bottom = new FormAttachment(100, -5);
		this.previousInterleavingButton.setLayoutData(iprevFormData);

		// Next interleaving button
		this.nextInterleavingButton = new Button(this.interleavingsGroup,
				SWT.PUSH);
		this.nextInterleavingButton.setImage(nextItemImage);
		this.nextInterleavingButton.setToolTipText(Messages.GemAnalyzer_71);
		this.nextInterleavingButton.setEnabled(false);
		FormData inextFormData = new FormData();
		inextFormData.left = new FormAttachment(
				this.previousInterleavingButton, 3);
		inextFormData.bottom = new FormAttachment(100, -5);
		this.nextInterleavingButton.setLayoutData(inextFormData);

		// Last interleaving button
		this.lastInterleavingButton = new Button(this.interleavingsGroup,
				SWT.PUSH);
		this.lastInterleavingButton.setImage(lastItemImage);
		this.lastInterleavingButton.setToolTipText(Messages.GemAnalyzer_72);
		this.lastInterleavingButton.setEnabled(false);
		FormData ilastFormData = new FormData();
		ilastFormData.left = new FormAttachment(this.nextInterleavingButton, 3);
		ilastFormData.bottom = new FormAttachment(100, -5);
		this.lastInterleavingButton.setLayoutData(ilastFormData);

		// Deadlock interleaving button
		this.deadlockInterleavingButton = new Button(this.interleavingsGroup,
				SWT.PUSH);
		this.deadlockInterleavingButton.setImage(deadlockImage);
		this.deadlockInterleavingButton.setToolTipText(Messages.GemAnalyzer_73);
		this.deadlockInterleavingButton.setEnabled(false);
		FormData deadlockButtonFormData = new FormData();
		deadlockButtonFormData.left = new FormAttachment(
				this.lastInterleavingButton, 3);
		deadlockButtonFormData.bottom = new FormAttachment(100, -5);
		deadlockButtonFormData.right = new FormAttachment(100, -5);
		this.deadlockInterleavingButton.setLayoutData(deadlockButtonFormData);
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createStepOrderGroup(Composite parent) {

		Image endEarlyImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/progress_stop.gif")); //$NON-NLS-1$

		// Group and FormLayout data for step order radio buttons
		this.stepOrderGroup = new Group(parent, SWT.SHADOW_IN);
		this.stepOrderGroup.setText(Messages.GemAnalyzer_74);
		this.stepOrderGroup.setToolTipText(Messages.GemAnalyzer_75);
		FormData stepOrderFormData = new FormData();
		stepOrderFormData.left = new FormAttachment(this.interleavingsGroup, 20);
		stepOrderFormData.bottom = new FormAttachment(100, -5);
		this.stepOrderGroup.setLayoutData(stepOrderFormData);
		this.stepOrderGroup.setLayout(new GridLayout(3, false));

		// Step order radio buttons
		this.internalIssueOrderButton = new Button(this.stepOrderGroup,
				SWT.RADIO);
		this.internalIssueOrderButton.setText(Messages.GemAnalyzer_76);
		this.internalIssueOrderButton.setToolTipText(Messages.GemAnalyzer_77);
		this.programOrderButton = new Button(this.stepOrderGroup, SWT.RADIO);
		this.programOrderButton.setText(Messages.GemAnalyzer_78);
		this.programOrderButton.setToolTipText(Messages.GemAnalyzer_79);

		// Choose which one is to be enabled from the Preference Store setting
		String stepOrder = GemPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.GEM_PREF_STEP_ORDER);
		if (stepOrder.equals(Messages.GemAnalyzer_80)) {
			this.internalIssueOrderButton.setSelection(true);
		} else {
			this.programOrderButton.setSelection(true);
		}

		// Font for the radio buttons
		Font buttonFont = setFontSize(this.programOrderButton.getFont(), 8);
		this.internalIssueOrderButton.setFont(buttonFont);
		this.programOrderButton.setFont(buttonFont);

		// Put the kill button to the right of step-order group
		this.endEarlyButton = new Button(parent, SWT.PUSH);
		this.endEarlyButton.setImage(endEarlyImage);
		this.endEarlyButton.setToolTipText(Messages.GemAnalyzer_81);
		this.endEarlyButton.setEnabled(false);
		FormData endEarlyButtonFormData = new FormData();
		endEarlyButtonFormData.bottom = new FormAttachment(100, -10);
		endEarlyButtonFormData.left = new FormAttachment(this.stepOrderGroup,
				15);
		this.endEarlyButton.setLayoutData(endEarlyButtonFormData);
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createRuntimeGroup(Composite parent) {
		// Get images for buttons from image cache
		Image noErrorImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/no-error.gif")); //$NON-NLS-1$
		Image uiImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/hbv-trident.gif")); //$NON-NLS-1$

		// Runtime information group
		Group runtimeInfoGroup = new Group(parent, SWT.SHADOW_IN);
		runtimeInfoGroup.setText(Messages.GemAnalyzer_82);
		runtimeInfoGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1));
		runtimeInfoGroup.setLayout(new FormLayout());

		// Error message label
		this.errorMessageLabel = new Label(runtimeInfoGroup, SWT.NONE);
		FormData deadlockMessageFormData = new FormData();
		deadlockMessageFormData.left = new FormAttachment(0, 5);
		deadlockMessageFormData.top = new FormAttachment(0, 0);
		deadlockMessageFormData.width = 230;
		this.errorMessageLabel.setLayoutData(deadlockMessageFormData);
		Font errorMessageLabelFont = setFontSize(this.errorMessageLabel
				.getFont(), 9);
		this.errorMessageLabel.setFont(errorMessageLabelFont);

		// Resource leak label
		this.resourcLeakLabel = new Label(runtimeInfoGroup, SWT.NONE);
		FormData resourceLeakFormData = new FormData();
		resourceLeakFormData.left = new FormAttachment(0, 5);
		resourceLeakFormData.bottom = new FormAttachment(100, -1);
		resourceLeakFormData.width = 230;
		this.resourcLeakLabel.setLayoutData(resourceLeakFormData);
		Font resourceleakLabelFont = setFontSize(this.resourcLeakLabel
				.getFont(), 9);
		this.resourcLeakLabel.setFont(resourceleakLabelFont);

		// Examine Error button
		this.examineErrorsButton = new Button(runtimeInfoGroup, SWT.PUSH);
		this.examineErrorsButton.setImage(noErrorImage);
		this.examineErrorsButton.setText(Messages.GemAnalyzer_83);
		this.examineErrorsButton.setToolTipText(Messages.GemAnalyzer_84);
		this.examineErrorsButton.setEnabled(false);
		FormData examineErrorsFormData = new FormData();
		examineErrorsFormData.right = new FormAttachment(100, -5);
		examineErrorsFormData.bottom = new FormAttachment(100, -5);
		this.examineErrorsButton.setLayoutData(examineErrorsFormData);

		// Browse leaks button
		this.browseLeaksButton = new Button(runtimeInfoGroup, SWT.PUSH);
		this.browseLeaksButton.setImage(noErrorImage);
		this.browseLeaksButton.setText(Messages.GemAnalyzer_85);
		this.browseLeaksButton.setToolTipText(Messages.GemAnalyzer_86);
		this.browseLeaksButton.setEnabled(false);
		FormData browseLeaksFormData = new FormData();
		browseLeaksFormData.right = new FormAttachment(
				this.examineErrorsButton, -5);
		browseLeaksFormData.bottom = new FormAttachment(100, -5);
		this.browseLeaksButton.setLayoutData(browseLeaksFormData);

		// Browse calls button
		this.browseCallsButton = new Button(runtimeInfoGroup, SWT.PUSH);
		this.browseCallsButton.setImage(noErrorImage);
		this.browseCallsButton.setText(Messages.GemAnalyzer_87);
		this.browseCallsButton.setToolTipText(Messages.GemAnalyzer_88);
		this.browseCallsButton.setEnabled(false);
		FormData browseCallsFormData = new FormData();
		browseCallsFormData.right = new FormAttachment(this.browseLeaksButton,
				-5);
		browseCallsFormData.bottom = new FormAttachment(100, -5);
		this.browseCallsButton.setLayoutData(browseCallsFormData);

		// Launch ispUI button
		this.launchIspUIButton = new Button(runtimeInfoGroup, SWT.PUSH);
		this.launchIspUIButton.setImage(uiImage);
		this.launchIspUIButton.setText(Messages.GemAnalyzer_89);
		this.launchIspUIButton.setToolTipText(Messages.GemAnalyzer_90);
		this.launchIspUIButton.setEnabled(false);
		FormData launchIspUIFormData = new FormData();
		launchIspUIFormData.right = new FormAttachment(this.browseCallsButton,
				-5);
		launchIspUIFormData.bottom = new FormAttachment(100, -5);
		this.launchIspUIButton.setLayoutData(launchIspUIFormData);

		// Set number of processes button
		this.setRankComboList = new Combo(runtimeInfoGroup, SWT.DROP_DOWN);
		Font setRankComboFont = setFontSize(this.setRankComboList.getFont(), 9);
		this.setRankComboList.setFont(setRankComboFont);
		String[] items = new String[] {};
		this.setRankComboList.setItems(items);
		this.setRankComboList.setText(" "); //$NON-NLS-1$
		this.setRankComboList.setToolTipText(Messages.GemAnalyzer_91);
		FormData setRankFormData = new FormData();
		setRankFormData.width = 50;
		setRankFormData.right = new FormAttachment(this.launchIspUIButton, -5);
		setRankFormData.bottom = new FormAttachment(100, -5);
		this.setRankComboList.setLayoutData(setRankFormData);
		this.setRankComboList.setEnabled(true);
		setRankItems();
		updateDropDown();

		// Font for the buttons
		Font buttonFont = setFontSize(this.errorMessageLabel.getFont(), 8);
		this.launchIspUIButton.setFont(buttonFont);
		this.browseCallsButton.setFont(buttonFont);
		this.browseLeaksButton.setFont(buttonFont);
		this.examineErrorsButton.setFont(buttonFont);
		this.setRankComboList.setFont(buttonFont);
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createCodeWindowsGroup(Composite parent) {
		// Get images for buttons from image cache
		Image sourceCallImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/source-call.gif")); //$NON-NLS-1$
		Image matchCallImage = GemPlugin.getImage(GemPlugin
				.getImageDescriptor("icons/match-call.gif")); //$NON-NLS-1$

		// Create the layout for the analyzer windows and call info labels
		Group codeWindowsGroup = new Group(parent, SWT.NONE | SWT.SHADOW_IN);
		codeWindowsGroup.setText(Messages.GemAnalyzer_92);
		GridLayout codeWindowsLayout = new GridLayout();
		codeWindowsLayout.numColumns = 2;
		codeWindowsLayout.marginHeight = 10;
		codeWindowsLayout.marginWidth = 10;
		codeWindowsLayout.horizontalSpacing = 15;
		codeWindowsGroup.setLayout(codeWindowsLayout);
		codeWindowsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 2, 1));

		// Create code window labels and their respective layouts, etc.
		this.leftCodeWindowLabel = new Label(codeWindowsGroup, SWT.WRAP);
		this.leftCodeWindowLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false, 1, 1));
		Font leftCodeWindowLabelFont = setFontSize(this.leftCodeWindowLabel
				.getFont(), 9);
		this.leftCodeWindowLabel.setFont(leftCodeWindowLabelFont);
		this.leftCodeWindowLabel.setText("\n"); //$NON-NLS-1$
		this.rightCodeWindowLabel = new Label(codeWindowsGroup, SWT.WRAP);
		this.rightCodeWindowLabel.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, false, 1, 1));
		Font rightCodeWindowLabelFont = setFontSize(this.rightCodeWindowLabel
				.getFont(), 9);
		this.rightCodeWindowLabel.setFont(rightCodeWindowLabelFont);
		this.rightCodeWindowLabel.setText("\n"); //$NON-NLS-1$

		// The short explanations of each of the code windows
		this.leftCodeWindowExplenationLabel = new CLabel(codeWindowsGroup,
				SWT.WRAP | SWT.NULL);
		this.rightCodeWindowExplenationLabel = new CLabel(codeWindowsGroup,
				SWT.WRAP | SWT.NULL);
		this.leftCodeWindowExplenationLabel.setImage(sourceCallImage);
		this.rightCodeWindowExplenationLabel.setImage(matchCallImage);
		this.leftCodeWindowExplenationLabel.setText(Messages.GemAnalyzer_93);
		this.rightCodeWindowExplenationLabel.setText(Messages.GemAnalyzer_94);

		// Create the analyzer list viewers
		this.leftViewer = new ListViewer(codeWindowsGroup, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		this.rightViewer = new ListViewer(codeWindowsGroup, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Font leftViewerFont = setFontSize(this.leftViewer.getControl()
				.getFont(), 8);
		Font rightViewerFont = setFontSize(this.rightViewer.getControl()
				.getFont(), 8);
		this.leftViewer.getControl().setFont(leftViewerFont);
		this.rightViewer.getControl().setFont(rightViewerFont);
		this.leftViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		this.rightViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// Create two listeners for these viewers
		// HACK to prevent user from changing the selected line
		this.singleListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				org.eclipse.swt.widgets.List list = (org.eclipse.swt.widgets.List) e
						.getSource();
				String entry = list.getItem(0).toString();

				// Determine if collective call clicked in the Right Viewer
				if (!entry.contains(Messages.GemAnalyzer_95)
						|| !entry.contains(Messages.GemAnalyzer_96)) {
					updateSelectedLine(false);
				}
			}
		};

		this.doubleListener = new DoubleClickListener();
		this.rightViewer.addDoubleClickListener(this.doubleListener);

		// HACK to prevent user from changing the selected line
		this.rightViewer.getList().addSelectionListener(this.singleListener);
		this.leftViewer.addDoubleClickListener(this.doubleListener);

		// HACK to prevent user from changing the selected line
		this.leftViewer.getList().addSelectionListener(this.singleListener);
	}

	/*
	 * Helper method called by createPartControl.
	 */
	private void createSelectionListeners() {
		// SelectionListeners for transitions group buttons
		this.firstTransitionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goToFirstTransition(true);
			}
		});

		this.previousTransitionButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						goToPreviousTransition();
					}
				});

		this.nextTransitionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goToNextTransition(true);
			}
		});

		this.lastTransitionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goToLastTransition();
			}
		});

		this.lockRanksComboList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (lockRanksComboList.getText() == null) {
					return;
				} else {
					try {
						String temp = lockRanksComboList.getItems()[lockRanksComboList
								.getSelectionIndex()];
						if (!temp.equals(Messages.GemAnalyzer_97)) {
							int index = temp.indexOf(" "); //$NON-NLS-1$
							temp = temp.substring(index + 1);
							index = temp.indexOf(" "); //$NON-NLS-1$
							temp = temp.substring(0, index);
							lockedRank = Integer.parseInt(temp);
						} else {
							lockedRank = -1;
						}
						// Also update things like button disabled/enabled
						updateTransitionVars(true);
					} catch (Exception exception) {
						lockedRank = -1;
					}
				}
			}
		});

		// SelectionListeners for interleavings group buttons
		this.firstInterleavingButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (transitions.hasPreviousInterleaving()) {
							while (transitions.previousInterleaving()) {
								;
							}
							updateTransitionVars(true);
						} else {
							GemUtilities.showInformationDialog(
									Messages.GemAnalyzer_98,
									Messages.GemAnalyzer_99);
						}
						updateErrorButtonState();
					}
				});

		this.previousInterleavingButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (transitions.hasPreviousInterleaving()) {
							transitions.previousInterleaving();
							updateTransitionVars(true);
						} else {
							GemUtilities.showInformationDialog(
									Messages.GemAnalyzer_100,
									Messages.GemAnalyzer_101);
						}
						errorIndex = 1;
						updateErrorButtonState();
					}
				});

		this.nextInterleavingButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (transitions.hasNextInterleaving()) {
							transitions.nextInterleaving();
							updateTransitionVars(true);
						} else {
							GemUtilities.showInformationDialog(
									Messages.GemAnalyzer_102,
									Messages.GemAnalyzer_103);
						}
						errorIndex = 1;
						updateErrorButtonState();
					}
				});

		this.lastInterleavingButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						while (transitions.hasNextInterleaving()) {
							transitions.nextInterleaving();
						}
						updateTransitionVars(true);
						errorIndex = 1;
						updateErrorButtonState();
					}
				});

		this.deadlockInterleavingButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						transitions.deadlockInterleaving();
						updateTransitionVars(true);
						errorIndex = 1;
						updateErrorButtonState();
					}
				});

		// Selection listeners for step order group
		this.internalIssueOrderButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (internalIssueOrderButton.getSelection()) {
							if (transitions != null) {
								ArrayList<ArrayList<Envelope>> tlists = transitions
										.getTransitionList();
								// Sort by internal issue order
								int size = tlists.size();
								for (int i = 0; i < size; i++) {
									Collections.sort(tlists.get(i),
											new InternalIssueOrderSorter());
								}
							}

							// Reset preference store value for this preference
							GemPlugin
									.getDefault()
									.getPreferenceStore()
									.setValue(
											PreferenceConstants.GEM_PREF_STEP_ORDER,
											Messages.GemAnalyzer_104);
							reset();
						}
					}
				});

		this.programOrderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (programOrderButton.getSelection()) {
					// Sort by internal issue order
					if (transitions != null) {
						ArrayList<ArrayList<Envelope>> tlists = transitions
								.getTransitionList();
						int size = tlists.size();
						for (int i = 0; i < size; i++) {
							Collections.sort(tlists.get(i),
									new ProgramOrderSorter());
						}
					}

					// Reset preference store value for this preference
					GemPlugin.getDefault().getPreferenceStore().setValue(
							PreferenceConstants.GEM_PREF_STEP_ORDER,
							Messages.GemAnalyzer_105);
					reset();
				}
			}
		});

		// Selection listeners for runtime information group buttons
		this.setRankComboList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (setRankComboList.getText() == null) {
					return;
				} else {
					String nprocsStr = setRankComboList.getItems()[setRankComboList
							.getSelectionIndex()];
					int newNumProcs = Integer.parseInt(nprocsStr);

					// Reset the numProcs value in the preference store
					GemPlugin.getDefault().getPreferenceStore().setValue(
							PreferenceConstants.GEM_PREF_NUMPROCS, newNumProcs);
				}
			}
		});

		this.launchIspUIButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String sourceFilePath = GemPlugin.getDefault()
						.getPreferenceStore().getString(
								PreferenceConstants.GEM_PREF_LAST_FILE);
				GemUtilities.doHpv(sourceFilePath);
			}
		});

		this.browseCallsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (logContents == null || logContents.isEmpty()) {
					String message = Messages.GemAnalyzer_106;
					String title = Messages.GemAnalyzer_107;
					GemUtilities.showErrorDialog(message, title);
					return;
				}
				launchCallBrowser();
			}
		});

		this.browseLeaksButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (logContents == null || logContents.isEmpty()) {
					String message = Messages.GemAnalyzer_108;
					String title = Messages.GemAnalyzer_109;
					GemUtilities.showErrorDialog(message, title);
					return;
				}
				launchLeakBrowser();
			}
		});

		this.examineErrorsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Get the hashmap with the error calls in the interleaving -
				HashMap<String, Envelope> map = transitions.getErrorCallsList()
						.get(transitions.getCurrentInterleaving());
				errorIndex = errorIndex % map.size();
				Collection<Envelope> collection = map.values();
				Object[] calls = new Object[collection.size()];
				calls = collection.toArray();
				if (errorCalls == null || !errorCalls.equals(calls)) {
					errorCalls = calls;
				}

				launchErrorBrowser();
			}
		});

		this.endEarlyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Add code to kill ISP here
			}
		});
	}

	// Listens for double-clicks and jumps to that line of code
	private class DoubleClickListener implements IDoubleClickListener {

		public void doubleClick(DoubleClickEvent event) {
			ISelection selection = event.getSelection();
			org.eclipse.jface.viewers.StructuredSelection structure = (org.eclipse.jface.viewers.StructuredSelection) selection;
			ListElement element = (ListElement) structure.getFirstElement();

			// Open the source file in the Editor Window
			String base = ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toPortableString();
			String sourceFilePath = element.fullFileName.substring(base
					.length());
			openEditor(element.line, sourceFilePath);
		}
	}

	// ONLY CALL WHEN YOU HAVE STARTED A NEW INTERLEAVING!!!
	// As it searches it will never display the envelope
	// displayEnvelope decides whether or not the final destination is displayed
	private void updateTransitionVars(boolean displayEnvelope) {
		goToFirstTransition(false);

		// because only incremented if NEXT is also valid
		this.transitionCount = 1;
		Envelope env = this.transitions.getCurrentTransition();

		int test = 1;
		while (test == 1 && env != null) {
			if (this.transitions.hasValidNextTransition(this.lockedRank)) {
				this.transitionCount++;
				test = goToNextTransition(false);
			} else {
				break;
			}
		}
		goToFirstTransition(displayEnvelope);
	}

	/*
	 * Updates the state of the examine error button based on the interleaving
	 * and its associated error call(s), if any.
	 */
	private void updateErrorButtonState() {
		if (this.transitions.getDeadlockInterleavings() != null) {
			this.examineErrorsButton.setEnabled(this.transitions
					.getDeadlockInterleavings().contains(
							this.transitions.getCurrentInterleaving())
					&& this.transitions.hasDeadlock());
		} else if (this.transitions.hasAssertion()) {
			this.examineErrorsButton.setEnabled(true);
		} else if (this.transitions.getErrorCallsList().get(
				this.transitions.getCurrentInterleaving()) != null) {
			this.examineErrorsButton.setEnabled(true);
		} else {
			this.examineErrorsButton.setEnabled(false);
		}

		if (this.examineErrorsButton.isEnabled()) {
			this.examineErrorsButton.setImage(GemPlugin.getImage(GemPlugin
					.getImageDescriptor("icons/browse.gif"))); //$NON-NLS-1$

			// Get the number of error calls for the interleaving
			this.errorCount = this.transitions.getErrorCallsList().get(
					this.transitions.getCurrentInterleaving()).size();

			// Update labels and tooltips
			this.examineErrorsButton.setText(Messages.GemAnalyzer_110);
			if (this.transitions.hasDeadlock()) {
				this.examineErrorsButton
						.setToolTipText(Messages.GemAnalyzer_111);
			} else {
				this.examineErrorsButton
						.setToolTipText(Messages.GemAnalyzer_112);
			}
		} else {
			this.examineErrorsButton.setImage(GemPlugin.getImage(GemPlugin
					.getImageDescriptor("icons/no-error.gif"))); //$NON-NLS-1$
			this.examineErrorsButton.setText(Messages.GemAnalyzer_113);
		}
	}

	/*
	 * Goes to the First Transition and updates all relevant data
	 */
	private void goToFirstTransition(boolean update) {
		// Goes to null
		Envelope env = this.transitions.stepToFirstTransition(this.lockedRank);
		env = this.transitions.getCurrentTransition();

		if (env != null) {
			this.oldLeftIndex = env.getLinenumber();
		}

		if (env == null) {
			GemUtilities.showInformationDialog(Messages.GemAnalyzer_114,
					Messages.GemAnalyzer_115);
			return;
		}

		if (update) {
			setButtonEnabledState();
			updateCodeViewers();
			displayEnvelopes(env);
			updateSelectedLine(true);
			this.transitionIndex = 1;
			setMessageLabelText();
		}
	}

	/*
	 * Goes to the previous transition and updates relevant data
	 */
	private void goToPreviousTransition() {
		Envelope env = this.transitions.previousTransition(lockedRank);
		this.oldLeftIndex = env.getLinenumber();

		// Go back until you reach the first call itr or beginning
		if (env.isCollective() && this.lockedRank == -1) {
			while (true) {
				env = this.transitions.previousTransition(this.lockedRank);
				if (env == null) {
					goToFirstTransition(true);
					return;
				}
				if (env.getLinenumber() != this.oldLeftIndex) {
					env = this.transitions.nextTransition(this.lockedRank);
					break;
				}
			}
		}

		this.oldLeftIndex = env.getLinenumber();
		setButtonEnabledState();
		updateCodeViewers();
		displayEnvelopes(env);
		updateSelectedLine(true);
		this.transitionIndex--;
		setMessageLabelText();
	}

	/*
	 * Go to the next transition. Returns 1 if successful, -1 if there was no
	 * where to go.
	 */
	private int goToNextTransition(boolean update) {
		int returnValue = 1;
		Envelope env = this.transitions.nextTransition(this.lockedRank);

		if (env.isCollective() && this.lockedRank == -1) {
			env = skipRepeats(1);
		}

		if (env == null) {
			returnValue = -1;
		} else {
			this.oldLeftIndex = env.getLinenumber();
		}

		if (update) {
			setButtonEnabledState();
			updateCodeViewers();
			displayEnvelopes(env);
			updateSelectedLine(true);
			this.transitionIndex++;
			setMessageLabelText();
		}
		return returnValue;
	}

	/*
	 * Goes to the last transition and updates relevant information
	 */
	private void goToLastTransition() {
		Envelope env = this.transitions.stepToLastTransition(this.lockedRank);

		// stepToLastTransition puts us at the last iteration of Finalize
		// other code always assumes we are on the first iteration of
		// each call though, so here we go back to the first iteration
		if (env.getFunctionName().equals("MPI_Finalize")) { //$NON-NLS-1$
			while (true) {
				env = this.transitions.previousTransition(this.lockedRank);

				if (env == null) {
					GemUtilities.showInformationDialog(
							Messages.GemAnalyzer_116, Messages.GemAnalyzer_117);
					return;
				}
				// If there was only one finalize go back to it
				if (!env.getFunctionName().equals("MPI_Finalize")) { //$NON-NLS-1$
					env = this.transitions.nextTransition(this.lockedRank);
					break;
				}
			}
		}

		if (env == null) {
			GemUtilities.showInformationDialog(Messages.GemAnalyzer_118,
					Messages.GemAnalyzer_119);
			return;
		}
		this.oldLeftIndex = env.getLinenumber();
		setButtonEnabledState();
		updateCodeViewers();
		displayEnvelopes(env);
		updateSelectedLine(true);
		this.transitionIndex = this.transitionCount;
		setMessageLabelText();
	}

	/*
	 * If you are moving forward pass 1, if backward -1, if you want an ID10T
	 * error pass something else
	 */
	private Envelope skipRepeats(int direction) {
		Envelope env = this.transitions.getCurrentTransition();
		while (true) {
			if (env == null) {
				return null;
			}
			if (env.getLinenumber() == this.oldLeftIndex) {
				if (direction == 1) {
					env = this.transitions.nextTransition(this.lockedRank);
				} else {
					env = this.transitions.previousTransition(this.lockedRank);
				}
			} else {
				break;
			}
		}
		return env;
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

	/*
	 * Removes comments from the specified call String.
	 */
	private String removeComments(String call) {
		// remove any c-style comments
		while (call.contains("/*")) { //$NON-NLS-1$
			if (!call.contains("*/")) {//$NON-NLS-1$
				break;
			}
			call = call.substring(0, call.indexOf("/*")) //$NON-NLS-1$
					+ call.substring(call.indexOf("*/") + 2); //$NON-NLS-1$
		}

		// remove any c++-style comments
		while (call.contains("//")) { //$NON-NLS-1$
			call = call.substring(0, call.indexOf("//")); //$NON-NLS-1$
		}
		return call;
	}
}