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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter.AbstractIgnoreRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

/**
 * Filter widget
 */
public class SyncFilterWidget extends Composite {
	/**
	 * Simple pattern editor that uses reflection and assumes that only the pattern's string needs to be edited.
	 * This will need to be more general if we add more pattern types in the future. Specifically, the logic of how to edit a
	 * pattern should be inside the specific matcher.
	 */
	private class SimpleEditPatternDialog extends Dialog {

		private AbstractIgnoreRule oldRule;
		
		private boolean exclude;
		private String pattern;
		
		
		private Text patternText;
		private Label errorLabel;

		public SimpleEditPatternDialog(Shell parentShell, AbstractIgnoreRule rule) {
			super(parentShell);
			oldRule = rule;
		}

		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			if (oldRule == null) {
				shell.setText(Messages.NewSyncFileFilterPage_Add_Pattern);
			} else {
				shell.setText(Messages.NewSyncFileFilterPage_Edit_pattern);
			}
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
					exclude = false;
				}
			});

			Button exclusiveButton = new Button(patternTypeGroup, SWT.RADIO);
			exclusiveButton.setText(Messages.NewSyncFileFilterPage_Exclude);
			exclusiveButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			exclusiveButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					exclude = true;
				}
			});

			errorLabel = new Label(main, SWT.NONE);
			errorLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
			errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			if (oldRule != null) {
				patternText.setText(oldRule.toString());
				exclude = oldRule.getResult();
				inclusiveButton.setSelection(!exclude);
				exclusiveButton.setSelection(exclude);
			} else {
				exclude = true;
				inclusiveButton.setSelection(false);
				exclusiveButton.setSelection(true);
			}

			return main;
		}
	}

	private static final Display display = Display.getCurrent();

	private AbstractSyncFileFilter fFilter;
	private Table fPatternTable;
	private Button fAddButton;
	private Button fUpButton;
	private Button fDownButton;
	private Button fEditButton;
	private Button fRemoveButton;

	private Set<ISyncFilterWidgetPatternChangeListener> patternChangeListeners =
			new HashSet<ISyncFilterWidgetPatternChangeListener>();

	/** Boolean to help tell which boolean arguments do what */
	private static final boolean GRAB_EXCESS = true;

	/** Boolean to turn on funky colors to debug which composites contain what */
	private static final boolean DEBUG = false;

	public SyncFilterWidget(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Composite for pattern view - pattern table and buttons
		Composite patternTableComposite = new Composite(this, SWT.NONE);
		if (DEBUG) {
			colorComposite(patternTableComposite, SWT.COLOR_BLUE);
		}

		GridLayout patternTableLayout = new GridLayout(2, false);
		patternTableComposite.setLayout(patternTableLayout);

		patternTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, GRAB_EXCESS, GRAB_EXCESS));

		// Label for pattern table
		Label patternTableLabel = new Label(patternTableComposite, SWT.NONE);
		patternTableLabel.setText(Messages.NewSyncFileFilterPage_Patterns_to_include);
		patternTableLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));

		// Pattern table
		fPatternTable = new Table(patternTableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		fPatternTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, GRAB_EXCESS, GRAB_EXCESS, 1, 6));
		TableColumn column = new TableColumn(fPatternTable, SWT.LEFT, 0);
		column.setText(Messages.NewSyncFileFilterPage_Pattern);
		column = new TableColumn(fPatternTable, SWT.LEFT, 1);
		column.setText(Messages.NewSyncFileFilterPage_Type);
		fPatternTable.setHeaderVisible(true);

		fPatternTable.addMouseListener(new MouseListener() {
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
		fPatternTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonEnablement();
			}

		});

		fAddButton = new Button(patternTableComposite, SWT.PUSH);
		fAddButton.setText(Messages.NewSyncFileFilterPage_Add);
		fAddButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		fAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				addPattern();
				updateButtonEnablement();
			}
		});

		fEditButton = new Button(patternTableComposite, SWT.PUSH);
		fEditButton.setText(Messages.NewSyncFileFilterPage_Edit);
		fEditButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		fEditButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				editPattern();
				updateButtonEnablement();
			}
		});
		fEditButton.setEnabled(false);

		fRemoveButton = new Button(patternTableComposite, SWT.PUSH);
		fRemoveButton.setText(Messages.NewSyncFileFilterPage_Remove);
		fRemoveButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TableItem[] selectedPatternItems = fPatternTable.getSelection();
				for (TableItem selectedPatternItem : selectedPatternItems) {
					AbstractIgnoreRule selectedRule = (AbstractIgnoreRule) selectedPatternItem.getData();
					fFilter.removePattern(selectedRule);
				}
				updateTable();
				updateButtonEnablement();
				notifyPatternChangeListeners();
			}
		});
		fRemoveButton.setEnabled(false);

		// Spacer
		new Label(patternTableComposite, SWT.NONE);

		// Pattern table buttons (up, down, edit, remove)
		fUpButton = new Button(patternTableComposite, SWT.PUSH);
		fUpButton.setText(Messages.NewSyncFileFilterPage_Move_up);
		fUpButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		fUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TableItem[] selectedPatternItems = fPatternTable.getSelection();
				if (selectedPatternItems.length != 1) {
					return;
				}
				int patternIndex = fPatternTable.getSelectionIndex();
				if (fFilter.promote((AbstractIgnoreRule) selectedPatternItems[0].getData())) {
					patternIndex--;
				}
				updateTable();
				fPatternTable.select(patternIndex);
				updateButtonEnablement();
				notifyPatternChangeListeners();
			}
		});
		fUpButton.setEnabled(false);

		fDownButton = new Button(patternTableComposite, SWT.PUSH);
		fDownButton.setText(Messages.NewSyncFileFilterPage_Move_down);
		fDownButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		fDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TableItem[] selectedPatternItems = fPatternTable.getSelection();
				if (selectedPatternItems.length != 1) {
					return;
				}
				int patternIndex = fPatternTable.getSelectionIndex();
				if (fFilter.demote((AbstractIgnoreRule) selectedPatternItems[0].getData())) {
					patternIndex++;
				}
				updateTable();
				fPatternTable.select(patternIndex);
				updateButtonEnablement();
				notifyPatternChangeListeners();
			}
		});
		fDownButton.setEnabled(false);
	}

	/**
	 * Add a new listener that will be notified when patterns change
	 * @param listener
	 */
	public void addNewPatternChangeListener(ISyncFilterWidgetPatternChangeListener listener) {
		patternChangeListeners.add(listener);
	}

	private void addPattern() {
		SimpleEditPatternDialog dialog = new SimpleEditPatternDialog(fPatternTable.getShell(), null);
		if (dialog.open() == Window.OK) {
			fFilter.addPattern(dialog.pattern, dialog.exclude);
			updateTable();
			notifyPatternChangeListeners();
		}
	}

	private void colorComposite(Composite comp, int color) {
		if (DEBUG) {
			// color e.g. SWT.COLOR_RED
			org.eclipse.swt.graphics.Color gcolor = Display.getCurrent().getSystemColor(color);
			comp.setBackground(gcolor);
		}

	}

	/** Creates a modal dialog to edit the selected pattern and replaces it if user hits "OK" */
	private void editPattern() {
		TableItem[] selectedRuleItem = fPatternTable.getSelection();
		// Modifying more than one pattern at a time is not supported
		if (selectedRuleItem.length != 1) {
			return;
		}
		AbstractIgnoreRule selectedRule = (AbstractIgnoreRule) selectedRuleItem[0].getData();
		SimpleEditPatternDialog dialog = new SimpleEditPatternDialog(fPatternTable.getShell(), selectedRule);
		if (dialog.open() == Window.OK) {
			fFilter.removePattern(selectedRule);
			fFilter.addPattern(dialog.pattern, dialog.exclude);
			updateTable();
			notifyPatternChangeListeners();
		}
	}

	public AbstractSyncFileFilter getFilter() {
		return fFilter;
	}

	private void notifyPatternChangeListeners() {
		for (ISyncFilterWidgetPatternChangeListener listener : patternChangeListeners) {
			listener.patternChanged();
		}
	}

	/**
	 * Remove the given pattern-change listener
	 * @param listener
	 */
	public void removePatternChangeListener(ISyncFilterWidgetPatternChangeListener listener) {
		patternChangeListeners.remove(listener);
	}

	public void setFilter(AbstractSyncFileFilter filter) {
		fFilter = filter;
		updateTable();
	}

	/**
	 * Update filter - useful for when patterns are changed externally.
	 */
	@Override
	public void update() {
		super.update();
		updateButtonEnablement();
		updateTable();
	}

	private void updateButtonEnablement() {
		fEditButton.setEnabled(false);
		fRemoveButton.setEnabled(false);
		fUpButton.setEnabled(false);
		fDownButton.setEnabled(false);
		int index = fPatternTable.getSelectionIndex();
		if (index >= 0) {
			fEditButton.setEnabled(true);
			fRemoveButton.setEnabled(true);
			if (index > 0) {
				fUpButton.setEnabled(true);
			}
			if (index < fPatternTable.getItemCount() - 1) {
				fDownButton.setEnabled(true);
			}
		}
	}

	private void updateTable() {
		fPatternTable.removeAll();
		for (AbstractIgnoreRule rule : fFilter.getPatterns()) {
			TableItem ti = new TableItem(fPatternTable, SWT.LEFT);
			ti.setData(rule);

			String[] tableValues = new String[2];
			tableValues[0] = rule.getPattern();
			if (rule.getResult()) {
				tableValues[1] = Messages.NewSyncFileFilterPage_exclude;
			} else {
				tableValues[1] = Messages.NewSyncFileFilterPage_include;
			}

			ti.setText(tableValues);
		}

		fPatternTable.getColumn(0).pack();
		fPatternTable.getColumn(1).pack();
	}
}