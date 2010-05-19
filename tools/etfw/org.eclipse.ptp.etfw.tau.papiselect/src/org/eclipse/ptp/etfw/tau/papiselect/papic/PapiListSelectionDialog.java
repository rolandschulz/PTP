package org.eclipse.ptp.etfw.tau.papiselect.papic;

/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - current implementation
 *    
 * Modified from ListSelctionDialog
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sebastian Davids <sdavids@gmx.de> - Fix for bug 90273 - [Dialogs] 
 * 			ListSelectionDialog dialog alignment
 ****************************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * A heavily specialized implementation of SelectionDialog for displaying a list of 
 * PAPI performance counters which can be selected by the user, with mutually exclusive
 * counters being excluded as selections are made
 * @author wspear
 *
 */
public class PapiListSelectionDialog extends SelectionDialog {
	/**
	 * the root element to populate the viewer with
	 */
	private Object inputElement;

	/**
	 * providers for populating this dialog
	 */ 
	private ILabelProvider labelProvider;

	private IStructuredContentProvider contentProvider;

	/**
	 * the visual selection widget group
	 */ 
	CheckboxTableViewer listViewer;

	private PapiSelect papiCon;

	/**
	 * Vertical sizing constant
	 */ 
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;

	/**
	 * Horizontal sizing constant
	 */ 
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;
	
	public static final int PRESET=0;
	public static final int NATIVE=1;

	/**
	 * Creates a PAPI list selection dialog.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param papiloc
	 *            the string indicating the location of the papi root directory
	 * @param contentProvider
	 *            the content provider for navigating the model
	 * @param labelProvider
	 *            the label provider for displaying model elements
	 * @param message
	 *            the message to be displayed at the top of this dialog, or
	 *            <code>null</code> to display a default message
	 */
	public PapiListSelectionDialog(Shell parentShell, String papiloc,
			IStructuredContentProvider contentProvider,
			ILabelProvider labelProvider, String message, int papiCountType) {
		super(parentShell);
		setTitle(Messages.getString("PapiListSelectionDialog.PapiCounters"));//WorkbenchMessages.ListSelection_title); //$NON-NLS-1$
		papiCon = new PapiSelect(papiloc, papiCountType);
		inputElement = papiCon.getAvail().toArray();
		this.contentProvider = contentProvider;
		this.labelProvider = labelProvider;

		if (message != null) {
			setMessage(message);
		} else {
			setMessage(Messages.getString("PapiListSelectionDialog.SelectPapiCounters"));//WorkbenchMessages.ListSelection_message); //$NON-NLS-1$
		}
	}

	/**
	 * Add the selection, deselection and help buttons to the dialog.
	 * 
	 * @param composite
	 *            org.eclipse.swt.widgets.Composite
	 */
	private void addSelectionButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(
				IDialogConstants.HORIZONTAL_SPACING);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(SWT.END, SWT.TOP, true,
				false));

		Button selectButton = createButton(buttonComposite,
				IDialogConstants.SELECT_ALL_ID, Messages.getString("PapiListSelectionDialog.SelectAll"), false); //$NON-NLS-1$

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Object[] masterlist = contentProvider.getElements(inputElement);
				for (int i = 0; i < masterlist.length; i++) {
					//System.out.println(masterlist[i]);
					listViewer.setChecked(masterlist[i], true);
					updateGrey(masterlist[i]);
				}
			}
		};
		selectButton.addSelectionListener(listener);

		Button deselectButton = createButton(buttonComposite,
				IDialogConstants.DESELECT_ALL_ID, Messages.getString("PapiListSelectionDialog.DeselectAll"), false); //$NON-NLS-1$

		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(false);
				listViewer.setAllGrayed(false);
			}
		};
		deselectButton.addSelectionListener(listener);

		Button helpButton = createButton(buttonComposite,
				IDialogConstants.HELP_ID, Messages.getString("PapiListSelectionDialog.CounterDescs"), false); //$NON-NLS-1$
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				displayPapiDescs();
			}
		};
		helpButton.addSelectionListener(listener);
	}

	/**
	 * Pops up a display showing names and functions of all listed PAPI counters
	 *
	 */
	private void displayPapiDescs() {
		// TODO Make this run with background active
		Display thisDisplay = PlatformUI.getWorkbench().getDisplay();
		Shell eDefShell = new Shell(thisDisplay.getActiveShell(), SWT.RESIZE);
				eDefShell.setText(Messages.getString("PapiListSelectionDialog.CounterDescs")); //$NON-NLS-1$
				eDefShell.setMinimumSize(200, 100);
				eDefShell.setSize(400, 300);
				eDefShell.setLayout(new FillLayout());
				final Table table = new Table(eDefShell, SWT.BORDER);
				table.setHeaderVisible(true);
				TableColumn column1 = new TableColumn(table, SWT.NONE);
				column1.setText(Messages.getString("PapiListSelectionDialog.Counter")); //$NON-NLS-1$
				TableColumn column2 = new TableColumn(table, SWT.NONE);
				column2.setText(Messages.getString("PapiListSelectionDialog.Definition")); //$NON-NLS-1$
				TableItem item;
				Vector<String> cNames = papiCon.getCounterNames();
				Vector<String> cDefs = papiCon.getCounterDefs();
				for (int i = 0; i < cNames.size(); i++) {
					item = new TableItem(table, SWT.NONE);
					item.setText(new String[] { cNames.get(i),
							cDefs.get(i) });
				}
				column1.pack();
				column2.pack();
				eDefShell.open();
	}

	/**
	 * Visually checks the previously-specified elements in this dialog's list
	 * viewer.
	 */
	private void checkInitialSelections() {
		Iterator<?> itemsToCheck = getInitialElementSelections().iterator();

		while (itemsToCheck.hasNext()) {
			Object nextcheck = itemsToCheck.next();
			listViewer.setChecked(nextcheck, true);
			updateGrey(nextcheck);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(
	 * org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				PlatformUI.PLUGIN_ID + "."+"list_selection_dialog_context");//IWorkbenchHelpContextIds.LIST_SELECTION_DIALOG); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * This is the primary logic for counter exclusion. It needs to be called
	 * whenever a counter is checked or unchecked
	 */
	protected void updateGrey(Object element) {
		/*
		 * Grey elements are not relevant, check them to show that they are
		 * unavailable
		 */
		try {
			if (listViewer.getGrayed(element)) {
				if (!listViewer.getChecked(element)) {
					listViewer.setChecked(element, true);
				}
				return;
			}
			// If a new element is checked:
			if (listViewer.getChecked(element)) {
				// Get the list of unchecked, un-gray elements that are excluded
				// by the currently checked ones
				LinkedHashSet<Object> allgrey = papiCon.getGrey(listViewer
						.getCheckedElements(), listViewer.getGrayedElements());
				// Add the current gray elements to the new ones
				allgrey.addAll(Arrays.asList(listViewer.getGrayedElements()));
				listViewer.setGrayedElements(allgrey.toArray());
				LinkedHashSet<Object> checkall = new LinkedHashSet<Object>();
				// Check all of the checked -and- all of the gray elements (only
				// un-gray checked elements are actually selected)
				checkall.addAll(Arrays.asList(listViewer.getCheckedElements()));
				checkall.addAll(allgrey);
				listViewer.setCheckedElements(checkall.toArray());
			}
			// If a previously unchecked element is checked
			else {
				LinkedHashSet<Object> allgrey = new LinkedHashSet<Object>(Arrays
						.asList(listViewer.getGrayedElements()));
				LinkedHashSet<Object> maybegood = new LinkedHashSet<Object>(Arrays
						.asList(listViewer.getCheckedElements()));
				// This is all of the currently checked but not-grey elements
				maybegood.removeAll(allgrey);
				// allgrey is set to the elements excluded by the current
				// selected, not-grey elements
				allgrey = papiCon.getGrey(maybegood.toArray(), null);
				// The revised grey elements and the selected elements are all
				// checked.
				listViewer.setGrayedElements(allgrey.toArray());
				LinkedHashSet<Object> checkall = new LinkedHashSet<Object>(maybegood);
				checkall.addAll(allgrey);
				listViewer.setCheckedElements(checkall.toArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the core UI of this dialog
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);

		initializeDialogUnits(composite);

		createMessageArea(composite);

		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);

		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(contentProvider);

		class PapiCheckListener implements ICheckStateListener {

			public void checkStateChanged(CheckStateChangedEvent event) {
				updateGrey(event.getElement());
			}
		}

		listViewer.addCheckStateListener(new PapiCheckListener());
		addSelectionButtons(composite);

		initializeViewer();

		// initialize page
		if (!getInitialElementSelections().isEmpty()) {
			checkInitialSelections();
		}

		Dialog.applyDialogFont(composite);

		return composite;
	}

	/**
	 * Returns the viewer used to show the list.
	 * 
	 * @return the viewer, or <code>null</code> if not yet created
	 */
	protected CheckboxTableViewer getViewer() {
		return listViewer;
	}

	/**
	 * Initializes this dialog's viewer after it has been laid out.
	 */
	private void initializeViewer() {
		listViewer.setInput(inputElement);
	}

	/**
	 * The <code>PapiListSelectionDialog</code> implementation of this
	 * <code>Dialog</code> method builds a list of the selected counters for
	 * later retrieval by the client and closes this dialog.
	 */
	protected void okPressed() {

		// Get the input children.
		Object[] children = contentProvider.getElements(inputElement);

		// Build a list of selected children.
		if (children != null) {
			ArrayList<Object> list = new ArrayList<Object>();
			for (int i = 0; i < children.length; ++i) {
				Object element = children[i];
				// Return all checked but not grayed elements
				if (listViewer.getChecked(element)
						&& !listViewer.getGrayed(element)) {
					list.add(element);
				}
			}
			setResult(list);
		}

		super.okPressed();
	}
}
