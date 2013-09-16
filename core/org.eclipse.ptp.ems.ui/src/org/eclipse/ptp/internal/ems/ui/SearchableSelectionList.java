/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.ems.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.internal.ems.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Instances of this class represent a user interface element which contains a group with an informational message,
 * a master list, and a selection list. Items can be added to the selection list, in which case they become disabled in the master
 * list. Items can be removed from the selection list and will become re-enabled in the master list. There is also a search box
 * which can be used to filter items in the master list.
 * <p>
 * The items (strings) in the master list must be distinct. Items are sorted according to their natural ordering unless a custom
 * comparator is provided via {@link #setComparator(Comparator)}.
 * <p>
 * Typically, a {@link SearchableSelectionList} is used as follows.
 * <ol>
 * <li>Invoke the constructor, {@link SearchableSelectionList#SearchableSelectionList(Composite)}.
 * <li>Customize the title, label, and column headings using {@link #setTitle(String)}, {@link #setInstructions(String)}/
 * {@link #setInstructions(String, int)}, and {@link #setColumnHeaders(String, String)}, respectively.
 * <li>If necessary, invoke {@link #setComparator(Comparator)} to change how items are sorted.
 * <li>Invoke {@link #setItems(List, List)} to set the items available in the list.
 * <li>When finished, invoke {@link #getSelectedItems()} to determine what items were selected by the user.
 * </ol>
 * <p>
 * At any time, {@link #setItems(List, List)} can be invoked to change the list contents. The list contents can be reconstructed in
 * a background thread via {@link #asyncRepopulate(AsyncRepopulationStrategy)}.
 * 
 * @author Jeff Overbey
 * 
 * @see Composite
 */
@SuppressWarnings("javadoc")
public final class SearchableSelectionList extends Composite {

	private final class AsyncRepopulationJob extends Job {
		private final AsyncRepopulationStrategy strategy;

		public AsyncRepopulationJob(AsyncRepopulationStrategy strategy) {
			super(strategy.getMessage());
			this.strategy = strategy;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final List<String> modules;
			final List<String> modulesToSelect;
			try {
				modules = strategy.computeItems(monitor);
				modulesToSelect = strategy.computeSelectedItems(monitor);
			} catch (final Exception e) {
				if (!isDisposed()) {
					getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (!fAvailableTable.isDisposed()) { // Make sure the user didn't close the project properties dialog
								fAvailableTable.getItem(0)
										.setText(1, e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
							}
						}
					});
				}
				return new Status(IStatus.ERROR, EMSUIPlugin.PLUGIN_ID, IStatus.ERROR, e.getLocalizedMessage(), e);
			}

			if (!isDisposed()) {
				getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						if (!fAvailableTable.isDisposed()) { // Make sure the user didn't close the project properties dialog
							setItems(modules, modulesToSelect);
							strategy.afterRepopulation();
							layout(true, true);
						}
					}
				});
			}
			return Status.OK_STATUS;
		}
	}

	private final Composite fMainGroup;
	private Label fInstructionsLabel;
	private Text fSearchBoxText;
	private Table fAvailableTable;
	private Table fSelectedTable;
	private TableColumn fSelectedColumn;
	private TableColumn fAvailableColumn;
	private Button fSetDefaultsButton;
	private Button fReloadButton;
	private Button fAddButton;
	private Button fRemoveButton;
	private Button fUpButton;
	private Button fDownButton;

	private Set<String> fAvailableItems = Collections.<String> emptySet();
	private List<String> fSelectedItems = Collections.<String> emptyList();

	/**
	 * Constructor.
	 */
	public SearchableSelectionList(Composite parent) {
		super(parent, SWT.NONE);

		GridLayout g = new GridLayout(4, false);
		g.marginHeight = 0;
		g.marginWidth = 0;
		setLayout(g);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// fMainGroup = new Group(this, SWT.SHADOW_ETCHED_IN);
		//		fMainGroup.setText(""); //$NON-NLS-1$
		// fMainGroup = new Composite(this, SWT.NONE);
		// fMainGroup.setLayout(new GridLayout(4, false));
		// fMainGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fMainGroup = this;

		createInstructionalMessage();
		createTableWithSearchBox();
		createAddRemoveButtons();
		createTable();
		createRightButtons();
		createLowerButtons();

		layout();
	}

	/**
	 * Adds the given {@link SelectionListener} to the &quot;Clear Selection&quot; button.
	 */
	public void addClearButtonSelectonListener(SelectionListener listener) {
	}

	/**
	 * Adds the given {@link SelectionListener} to the &quot;Select Defaults&quot; button.
	 */
	public void addDefaultButtonSelectonListener(SelectionListener listener) {
		fSetDefaultsButton.addSelectionListener(listener);
	}

	/**
	 * Adds the given {@link SelectionListener} to the &quot;Reload List&quot; button.
	 */
	public void addReloadButtonSelectonListener(SelectionListener listener) {
		fReloadButton.addSelectionListener(listener);
	}

	/**
	 * Repopulates this checklist, computing the list of items in a background thread.
	 * <p>
	 * The operation of this procedure is as follows, where <code>strategy</code> is the {@link AsyncRepopulationStrategy} provided
	 * as an argument to this method:
	 * 
	 * <pre>
	 * Display to the user: strategy.getMessage();
	 * In a background thread {
	 *     Set<String> items = strategy.computeItems();
	 *     Set<String> fSelectedItems = strategy.computeSelectedItems();
	 * }
	 * In the UI thread {
	 *     setItems(items, fSelectedItems);
	 *     strategy.afterRepopulation();
	 * }
	 * </pre>
	 * <p>
	 * The set of selected items is unaffected if an {@link AsyncRepopulationStrategy} method throws an exception.
	 * 
	 * @param enableAfterRepopulation
	 *            the value passed to {@link #setEnabled(boolean)} after the list is repopulated
	 * @param strategy
	 */
	public void asyncRepopulate(final AsyncRepopulationStrategy strategy) {
		setItems(Collections.<String> emptyList(), Collections.<String> emptyList());

		final TableItem tableItem = new TableItem(fAvailableTable, SWT.NONE);
		tableItem.setText(1, strategy.getMessage());
		tableItem.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));

		setEnabled(false);

		final Job job = new AsyncRepopulationJob(strategy);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	/*
	 * Only move items if the largest index is less than the number of elements in the list. This stops the move when
	 * the last index in the selection reaches the bottom of the list.
	 */
	private boolean canMoveDown(Table table) {
		int[] indices = table.getSelectionIndices();
		return indices.length > 0 && indices[indices.length - 1] < table.getItemCount() - 1;
	}

	/*
	 * Only move items if the largest index is greater than the number of indices to move. This stops the move when the
	 * first index in the selection reaches the top of the list.
	 */
	private boolean canMoveUp(Table table) {
		int[] indices = table.getSelectionIndices();
		return indices.length > 0 && indices[indices.length - 1] > indices.length - 1;
	}

	private Pattern compilePattern() {
		String patternText = fSearchBoxText.getText().trim();
		if (patternText.equals("")) { //$NON-NLS-1$
			return null;
		}
		// Quote the search text, so substrings like "." and "\d" aren't
		// interpreted as literals rather than as regex patterns
		patternText = "\\Q" + patternText.replace("\\E", "\\\\E") + "\\E"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		// Replace the "*" wildcard character with the regex pattern ".*"
		patternText = patternText.replace("*", "\\E.*\\Q"); //$NON-NLS-1$ //$NON-NLS-2$
		// Replace the "?" wildcard character with the regex pattern ".?"
		patternText = patternText.replace("?", "\\E.?\\Q"); //$NON-NLS-1$ //$NON-NLS-2$

		return Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);
	}

	private void createAddRemoveButtons() {
		Composite buttons = new Composite(fMainGroup, SWT.NONE);
		buttons.setLayout(new GridLayout(1, true));
		GridData gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		gd.widthHint = 100;
		buttons.setLayoutData(gd);
		fAddButton = new Button(buttons, SWT.PUSH);
		fAddButton.setText(Messages.SearchableSelectionList_Add);
		fAddButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fAddButton.setEnabled(false);
		fAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<TableItem> added = new ArrayList<TableItem>();
				int[] indices = fAvailableTable.getSelectionIndices();
				for (int index : indices) {
					final String moduleName = fAvailableTable.getItem(index).getText();
					fSelectedItems.add(moduleName);
					TableItem newItem = new TableItem(fSelectedTable, SWT.NONE);
					newItem.setText(moduleName);
					added.add(newItem);
					fAvailableItems.remove(moduleName);
				}
				fAvailableTable.remove(indices);
				fSelectedTable.setSelection(added.toArray(new TableItem[0]));
				fSelectedTable.setFocus();
				updateEnablement();
			}
		});

		fRemoveButton = new Button(buttons, SWT.PUSH);
		fRemoveButton.setText(Messages.SearchableSelectionList_Remove);
		fRemoveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fRemoveButton.setEnabled(false);
		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<String> added = new ArrayList<String>();
				for (TableItem item : fSelectedTable.getSelection()) {
					final String moduleName = item.getText(0);
					fSelectedItems.remove(moduleName);
					fAvailableItems.add(moduleName);
					added.add(moduleName);
				}
				refresh();
				for (String name : added) {
					for (int index = 0; index < fAvailableTable.getItemCount(); index++) {
						if (fAvailableTable.getItem(index).getText().equals(name)) {
							fAvailableTable.select(index);
						}
					}
				}
				fAvailableTable.setFocus();
				updateEnablement();
			}
		});

	}

	private void createInstructionalMessage() {
		fInstructionsLabel = new Label(fMainGroup, SWT.WRAP);
		fInstructionsLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 2));
	}

	private void createLowerButtons() {
		fReloadButton = new Button(fMainGroup, SWT.PUSH);
		fReloadButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		fReloadButton.setText(Messages.SearchableSelectionList_ReloadList);
	}

	private void createRightButtons() {
		Composite buttons = new Composite(fMainGroup, SWT.NONE);
		buttons.setLayout(new GridLayout(1, true));
		GridData gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		gd.widthHint = 100;
		buttons.setLayoutData(gd);
		fUpButton = new Button(buttons, SWT.PUSH);
		fUpButton.setText(Messages.SearchableSelectionList_Up);
		fUpButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fUpButton.setEnabled(false);
		fUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int[] indices = fSelectedTable.getSelectionIndices();
				Arrays.sort(indices);
				if (canMoveUp(fSelectedTable)) {
					for (int index : indices) {
						TableItem fromItem = fSelectedTable.getItem(index);
						TableItem toItem = fSelectedTable.getItem(index - 1);
						String swap = fromItem.getText();
						fromItem.setText(toItem.getText());
						toItem.setText(swap);
						fSelectedTable.deselect(index);
						fSelectedTable.select(index - 1);
						Collections.swap(fSelectedItems, index, index - 1);
					}
				}
			}
		});

		fDownButton = new Button(buttons, SWT.PUSH);
		fDownButton.setText(Messages.SearchableSelectionList_Down);
		fDownButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fDownButton.setEnabled(false);
		fDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int[] indices = fSelectedTable.getSelectionIndices();
				Arrays.sort(indices);
				if (canMoveDown(fSelectedTable)) {
					for (int i = indices.length - 1; i >= 0; i--) {
						int index = indices[i];
						TableItem fromItem = fSelectedTable.getItem(index);
						TableItem toItem = fSelectedTable.getItem(index + 1);
						String swap = fromItem.getText();
						fromItem.setText(toItem.getText());
						toItem.setText(swap);
						fSelectedTable.deselect(index);
						fSelectedTable.select(index + 1);
						Collections.swap(fSelectedItems, index, index + 1);
					}
				}
			}
		});

		fSetDefaultsButton = new Button(buttons, SWT.PUSH);
		fSetDefaultsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fSetDefaultsButton.setText(Messages.SearchableSelectionList_Set_Default);
	}

	private void createTable() {
		fSelectedTable = new Table(fMainGroup, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, true);
		gd.widthHint = 200;
		gd.minimumWidth = 50;
		fSelectedTable.setLayoutData(gd);
		fSelectedTable.setLinesVisible(false);
		fSelectedTable.setHeaderVisible(true);
		fSelectedTable.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Table table = (Table) event.widget;
				Rectangle area = table.getClientArea();
				TableColumn col = table.getColumn(0);
				col.setWidth(area.width);
			}
		});
		fSelectedTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fAvailableTable.deselectAll();
				updateEnablement();
			}
		});

		fSelectedColumn = new TableColumn(fSelectedTable, SWT.NONE);
	}

	private void createTableWithSearchBox() {
		final Label filter = new Label(fMainGroup, SWT.WRAP);
		filter.setText(Messages.SearchableSelectionList_SearchBoxLabel);
		filter.setForeground(this.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_DARK_SHADOW));

		new Label(fMainGroup, SWT.NONE);
		new Label(fMainGroup, SWT.NONE);
		new Label(fMainGroup, SWT.NONE);

		fSearchBoxText = new Text(fMainGroup, SWT.SINGLE | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		fSearchBoxText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		fSearchBoxText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				refresh();
				updateEnablement();
			}
		});

		new Label(fMainGroup, SWT.NONE);
		new Label(fMainGroup, SWT.NONE);
		new Label(fMainGroup, SWT.NONE);

		fAvailableTable = new Table(fMainGroup, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.widthHint = 200;
		gd.heightHint = 100;
		gd.minimumWidth = 50;
		gd.minimumHeight = 50;
		fAvailableTable.setLayoutData(gd);
		fAvailableTable.setLinesVisible(false);
		fAvailableTable.setHeaderVisible(true);
		fAvailableTable.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Table table = (Table) event.widget;
				Rectangle area = table.getClientArea();
				TableColumn col = table.getColumn(0);
				col.setWidth(area.width);
			}
		});
		fAvailableTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fSelectedTable.deselectAll();
				updateEnablement();
			}
		});
		fAvailableColumn = new TableColumn(fAvailableTable, SWT.NONE);
	}

	/** @return the ordered list of all items in the checklist (non-<code>null</code>, non-modifiable) */
	public List<String> getAllItems() {
		return Collections.unmodifiableList(Arrays.asList(fAvailableItems.toArray(new String[0])));
	}

	/** @return the ordered list of all selected items in the checklist (non-<code>null</code>, non-modifiable) */
	public List<String> getSelectedItems() {
		return Collections.unmodifiableList(fSelectedItems);
	}

	private boolean matches(Pattern pattern, String moduleName) {
		if (pattern == null) { // User didn't type any filter text
			return true;
		}
		final Matcher matcher = pattern.matcher(moduleName);
		if (matcher.find()) {
			return matcher.start() == 0; // Match prefixes only
		}
		return false;
	}

	private void refresh() {
		fAvailableTable.removeAll();
		final Pattern pattern = compilePattern();
		for (final String moduleName : fAvailableItems) {
			if (matches(pattern, moduleName)) {
				final TableItem item = new TableItem(fAvailableTable, SWT.NONE);
				item.setText(0, moduleName);
			}
		}
		fSelectedTable.removeAll();
		for (final String moduleName : fSelectedItems) {
			if (matches(pattern, moduleName)) {
				final TableItem item = new TableItem(fSelectedTable, SWT.NONE);
				item.setText(0, moduleName);
			}
		}
		layout();
	}

	/** Sets the headers for the two columns in the checklist. */
	public void setColumnHeaders(String availableColumnHeader, String selectedColumnHeader) {
		fAvailableColumn.setText(availableColumnHeader);
		fSelectedColumn.setText(selectedColumnHeader);
		layout(true, true);
	}

	/** Sets the {@link Comparator} used to sort items in the checklist. */
	public void setComparator(Comparator<String> comparator) {
	}

	/** Convenience method which sets both enablement and visibility, then lays out this control */
	public void setEnabledAndVisible(boolean value) {
		setEnabled(value);
		setVisible(value);
		layout(true, true);
	}

	/** Sets the instructions displayed above the checklist. */
	public void setInstructions(String message) {
		fInstructionsLabel.setText(message);
		layout(true, true);
	}

	/** Sets the instructions displayed above the checklist. */
	public void setInstructions(String message, int widthHint) {
		final GridData gd = (GridData) fInstructionsLabel.getLayoutData();
		gd.widthHint = widthHint;
		setInstructions(message);
	}

	/**
	 * Modifies the items in the checklist.
	 * 
	 * @param availableItems
	 *            the complete set of items visible to the user (non-<code>null</code>)
	 * @param fSelectedItems
	 *            the subset of <code>items</code> which should be appear in the selected list (non-<code>null</code>)
	 */
	public void setItems(List<String> availableItems, List<String> selectedItems) {
		fAvailableItems = new TreeSet<String>(availableItems);
		fSelectedItems = new ArrayList<String>(selectedItems);
		fAvailableItems.removeAll(fSelectedItems);
		refresh();
	}

	/** Sets the text for this checklist's {@link Group} control. */
	public void setTitle(String description) {
		// fMainGroup.setText(description);
		layout(true, true);
	}

	private void updateEnablement() {
		fAddButton.setEnabled(fAvailableTable.getSelectionCount() > 0);
		fRemoveButton.setEnabled(fSelectedTable.getSelectionCount() > 0);
		fUpButton.setEnabled(canMoveUp(fSelectedTable));
		fDownButton.setEnabled(canMoveDown(fSelectedTable));
	}
}
