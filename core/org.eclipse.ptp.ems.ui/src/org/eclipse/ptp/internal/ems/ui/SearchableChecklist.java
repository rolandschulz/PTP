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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Instances of this class represent a user interface element which contains a group with an informational message,
 * a checklist, and a search box which can be used to filter items in the checklist.
 * <p>
 * The items (strings) in the checklist must be distinct. Items are sorted according to their natural ordering unless a custom
 * comparator is provided via {@link #setComparator(Comparator)}.
 * <p>
 * Typically, a {@link SearchableChecklist} is used as follows.
 * <ol>
 * <li>Invoke the constructor, {@link SearchableChecklist#SearchableChecklist(Composite)}.
 * <li>Customize the title, label, and column headings using {@link #setTitle(String)}, {@link #setInstructions(String)}/
 * {@link #setInstructions(String, int)}, and {@link #setColumnHeaders(String, String)}, respectively.
 * <li>If necessary, invoke {@link #setComparator(Comparator)} to change how items are sorted.
 * <li>Invoke {@link #setItems(Set, Set)} to set the items available in the list.
 * <li>When finished, invoke {@link #getSelectedItems()} to determine what items were selected by the user.
 * </ol>
 * <p>
 * At any time, {@link #setItems(Set, Set)} can be invoked to change the list contents. The list contents can be reconstructed in a
 * background thread via {@link #asyncRepopulate(AsyncRepopulationStrategy)}.
 * 
 * @author Jeff Overbey
 * 
 * @see Composite
 */
@SuppressWarnings("javadoc")
public final class SearchableChecklist extends Composite {

	private final Group group;
	private Label instructions = null;
	private Text searchBox = null;
	private Table table = null;
	private TableColumn enableColumn = null;
	private TableColumn textColumn = null;
	private Button clearSelection = null;
	private Button loadDefaults = null;
	private Button reloadList = null;

	private Comparator<String> comparator = null;
	private Set<String> items = Collections.<String> emptySet();
	private Set<String> selectedItems = new HashSet<String>();

	/**
	 * Constructor.
	 */
	public SearchableChecklist(Composite parent) {
		super(parent, SWT.NONE);

		this.setLayout(new FillLayout());

		group = new Group(this, SWT.SHADOW_ETCHED_IN);
		group.setText(""); //$NON-NLS-1$

		final GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 10;
		layout.horizontalSpacing = 25;
		group.setLayout(layout);

		createInstructionalMessage();
		createSearchBox();
		createTable();
		createButtons();

		this.layout();
	}

	private void createInstructionalMessage() {
		instructions = new Label(group, SWT.WRAP);
		instructions.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 2));
	}

	private void createSearchBox() {
		GridData gd;

		final Label filter = new Label(group, SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		gd.widthHint = 150;
		filter.setLayoutData(gd);
		filter.setText(Messages.SearchableChecklist_SearchBoxLabel);
		filter.setForeground(this.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_DARK_SHADOW));

		searchBox = new Text(group, SWT.SINGLE | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = 150;
		searchBox.setLayoutData(gd);
		searchBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				refresh();
				if (table != null && table.getItemCount() == 1) {
					highlightItem(0);
				}
			}
		});
		searchBox.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (table != null && table.getItemCount() == 1) {
					clearFilterAndSelectHighlighted(table.getItem(0).getText(1));
				}
			}
		});
	}

	private void highlightItem(int index) {
		final Color yellow = table.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
		final Color black = table.getDisplay().getSystemColor(SWT.COLOR_BLACK);

		table.getItem(index).setBackground(yellow);
		table.getItem(index).setForeground(black);
	}

	private void clearFilterAndSelectHighlighted(String item) {
		selectedItems.add(item);
		searchBox.setText(""); //$NON-NLS-1$
		refresh();
		highlightItem(item);
		removeHighlightingAfterDelay(item);
	}

	private void highlightItem(String item) {
		for (int index = 0; index < table.getItemCount(); index++) {
			if (table.getItem(index).getText(1).equals(item)) {
				highlightItem(index);
				break;
			}
		}
	}

	private void removeHighlightingAfterDelay(final String item) {
		final int DELAY_IN_MILLISECONDS = 1500;

		final Job job = new Job("") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				table.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						for (int index = 0; index < table.getItemCount(); index++) {
							if (table.getItem(index).getText(1).equals(item)) {
								removeHightingFromItem(index);
								break;
							}
						}
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.schedule(DELAY_IN_MILLISECONDS);
	}

	private void removeHightingFromItem(int index) {
		table.getItem(index).setBackground(table.getBackground());
		table.getItem(index).setForeground(table.getForeground());
	}

	private void createTable() {
		table = new Table(group, SWT.MULTI | SWT.CHECK | SWT.V_SCROLL | SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd.heightHint = 75;
		table.setLayoutData(gd);
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item instanceof TableItem) {
					final TableItem item = (TableItem) e.item;
					final String moduleName = item.getText(1);
					if (item.getChecked()) {
						selectedItems.add(moduleName);
					} else {
						selectedItems.remove(moduleName);
					}
				}
			}
		});

		enableColumn = new TableColumn(table, SWT.NONE);
		textColumn = new TableColumn(table, SWT.NONE);
	}

	private void createButtons() {
		clearSelection = new Button(group, SWT.PUSH);
		clearSelection.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		clearSelection.setText(Messages.SearchableChecklist_ClearSelection);
		clearSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setItems(items, Collections.<String> emptySet());
			}
		});

		loadDefaults = new Button(group, SWT.PUSH);
		loadDefaults.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		loadDefaults.setText(Messages.SearchableChecklist_SelectDefaults);

		reloadList = new Button(group, SWT.PUSH);
		reloadList.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		reloadList.setText(Messages.SearchableChecklist_ReloadList);
	}

	/**
	 * Adds the given {@link SelectionListener} to the &quot;Clear Selection&quot; button.
	 */
	public void addClearButtonSelectonListener(SelectionListener listener) {
		clearSelection.addSelectionListener(listener);
	}

	/**
	 * Adds the given {@link SelectionListener} to the &quot;Select Defaults&quot; button.
	 */
	public void addDefaultButtonSelectonListener(SelectionListener listener) {
		loadDefaults.addSelectionListener(listener);
	}

	/**
	 * Adds the given {@link SelectionListener} to the &quot;Reload List&quot; button.
	 */
	public void addReloadButtonSelectonListener(SelectionListener listener) {
		reloadList.addSelectionListener(listener);
	}

	private Pattern compilePattern() {
		String patternText = searchBox.getText().trim();
		if (patternText.equals("")) { //$NON-NLS-1$
			return null;
		} else {
			// Quote the search text, so substrings like "." and "\d" aren't
			// interpreted as literals rather than as regex patterns
			patternText = "\\Q" + patternText.replace("\\E", "\\\\E") + "\\E"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			// Replace the "*" wildcard character with the regex pattern ".*"
			patternText = patternText.replace("*", "\\E.*\\Q"); //$NON-NLS-1$ //$NON-NLS-2$
			// Replace the "?" wildcard character with the regex pattern ".?"
			patternText = patternText.replace("?", "\\E.?\\Q"); //$NON-NLS-1$ //$NON-NLS-2$

			return Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);
		}
	}

	private boolean matches(Pattern pattern, String moduleName) {
		if (pattern == null) { // User didn't type any filter text
			return true;
		} else {
			final Matcher matcher = pattern.matcher(moduleName);
			if (matcher.find()) {
				return matcher.start() == 0; // Match prefixes only
			} else {
				return false;
			}
		}
	}

	/** Sets the headers for the two columns in the checklist. */
	public void setColumnHeaders(String checkboxColumnHeader, String textColumnHeader) {
		enableColumn.setText(checkboxColumnHeader);
		enableColumn.pack();

		textColumn.setText(textColumnHeader);
		textColumn.pack();

		this.layout(true, true);
	}

	/** Sets the instructions displayed above the checklist. */
	public void setInstructions(String message, int widthHint) {
		final GridData gd = (GridData) instructions.getLayoutData();
		gd.widthHint = widthHint;
		setInstructions(message);
	}

	/** Sets the instructions displayed above the checklist. */
	public void setInstructions(String message) {
		instructions.setText(message);
		this.layout(true, true);
	}

	/** Sets the {@link Comparator} used to sort items in the checklist. */
	public void setComparator(Comparator<String> comparator) {
		this.comparator = comparator;
	}

	/** @return the set of all items in the checklist (non-<code>null</code>, non-modifiable) */
	public Set<String> getAllItems() {
		return Collections.unmodifiableSet(items);
	}

	/** @return the set of all selected items in the checklist (non-<code>null</code>, non-modifiable) */
	public Set<String> getSelectedItems() {
		return Collections.unmodifiableSet(selectedItems);
	}

	/**
	 * Modifies the items in the checklist.
	 * 
	 * @param items
	 *            the complete set of items visible to the user (non-<code>null</code>)
	 * @param selectedItems
	 *            the subset of <code>items</code> which should be checked (non-<code>null</code>)
	 */
	public void setItems(Set<String> items, Set<String> selectedItems) {
		final TreeSet<String> itemsCopy = new TreeSet<String>(comparator);
		itemsCopy.addAll(items);
		this.items = Collections.unmodifiableSet(itemsCopy);

		this.selectedItems = new HashSet<String>(selectedItems);
		this.selectedItems.retainAll(this.items);

		refresh();
	}

	private void refresh() {
		table.removeAll();
		final Pattern pattern = compilePattern();
		for (final String moduleName : items) {
			if (matches(pattern, moduleName)) {
				final TableItem item = new TableItem(table, SWT.NONE);
				item.setText(1, moduleName);
				item.setChecked(selectedItems.contains(moduleName));
			}
		}
		textColumn.pack();
		this.layout();
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
	 *     Set<String> selectedItems = strategy.computeSelectedItems();
	 * }
	 * In the UI thread {
	 *     setItems(items, selectedItems);
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
		setItems(Collections.<String> emptySet(), Collections.<String> emptySet());

		final TableItem tableItem = new TableItem(table, SWT.NONE);
		tableItem.setText(1, strategy.getMessage());
		tableItem.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		textColumn.pack();

		setEnabled(false);

		final Job job = new AsyncRepopulationJob(strategy);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	private final class AsyncRepopulationJob extends Job {
		private final AsyncRepopulationStrategy strategy;

		public AsyncRepopulationJob(AsyncRepopulationStrategy strategy) {
			super(strategy.getMessage());
			this.strategy = strategy;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final Set<String> modules;
			final Set<String> modulesToSelect;
			try {
				modules = strategy.computeItems();
				modulesToSelect = strategy.computeSelectedItems();
			} catch (final Exception e) {
				if (!isDisposed()) {
					getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (!table.isDisposed()) { // Make sure the user didn't close the project properties dialog
								table.getItem(0).setText(1, e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
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
						if (!table.isDisposed()) { // Make sure the user didn't close the project properties dialog
							setItems(modules, modulesToSelect);
							strategy.afterRepopulation();
						}
					}
				});
			}
			return Status.OK_STATUS;
		}
	}

	/** Convenience method which sets both enablement and visibility, then lays out this control */
	public void setEnabledAndVisible(boolean value) {
		this.setEnabled(value);
		this.setVisible(value);
		this.layout(true, true);
	}

	/** Sets the text for this checklist's {@link Group} control. */
	public void setTitle(String description) {
		group.setText(description);
		this.layout(true, true);
	}
}
