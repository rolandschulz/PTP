package org.eclipse.ptp.rm.lml.ui.providers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.IPattern;
import org.eclipse.ptp.rm.lml.core.model.ITableColumnLayout;
import org.eclipse.ptp.rm.lml.internal.core.model.Pattern;
import org.eclipse.ptp.rm.lml.ui.ILMLUIConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

// TODO Handling the status column in joblistactive
public class FilterDialog extends Dialog {

	public class SelectDateAdpater extends SelectionAdapter {

		private final Button button;

		public SelectDateAdpater(Button button) {
			super();
			this.button = button;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			dialog.setLayout(new GridLayout(2, false));

			final DateTime calendar = new DateTime(dialog, SWT.CALENDAR | SWT.BORDER);
			calendar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			final DateTime time = new DateTime(dialog, SWT.TIME);
			time.setLayoutData(new GridData());

			final Button ok = new Button(dialog, SWT.PUSH);
			ok.setText("OK");
			ok.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			ok.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final NumberFormat formatter = new DecimalFormat("00");
					button.setText(calendar.getYear() + "-" + formatter.format(calendar.getMonth() + 1) + "-"
							+ formatter.format(calendar.getDay()) + " " + formatter.format(time.getHours()) + ":"
							+ formatter.format(time.getMinutes()) + ":" + formatter.format(time.getSeconds()));
					dialog.close();
				}
			});
			dialog.setDefaultButton(ok);
			dialog.pack();
			dialog.open();
		}

	}

	private FilterDataRow[] filterData;

	private final ILguiItem lguiItem;
	private final String gid;

	private final Shell shell;

	public FilterDialog(Shell parentShell, String gid) {
		super(parentShell);
		lguiItem = LMLManager.getInstance().getSelectedLguiItem();
		this.gid = gid;
		setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL);
		this.shell = parentShell;
	}

	private boolean includeStatus(ITableColumnLayout[] columnLayouts) {
		for (final ITableColumnLayout columnLayout : columnLayouts) {
			if (columnLayout.getTitle().equals(ILMLUIConstants.COLUMN_STATUS)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void buttonPressed(int buttonId) {

		if (buttonId == IDialogConstants.CANCEL_ID) {
			// Cancel Button
			// TODO Old patterns should be set back
			close();
			return;
		}

		boolean error = false;
		for (final FilterDataRow row : filterData) {

			if (!row.isCheckboxSet()) {
				continue;
			}
			if (row.getType().equals("alpha")) {
				if (row.getRelationValueTextAlpha().getText().equals("")) {
					error = true;
					break;
				}
			} else {
				if (row.getRadioButtonRelation().getSelection()) {
					if (row.getType().equals("numeric")) {
						if (row.getRelationValueTextNumeric().getText().equals("")) {
							error = true;
							break;
						}
					} else if (row.getType().equals("date")) {
						if (row.getRelationValueButtonDate().getText().equals("")) {
							error = true;
							break;
						}
					}
				} else if (row.getRadioButtonRange().getSelection()) {
					if (row.getType().equals("numeric")) {
						if (row.getMinValueTextNumeric().getText().equals("")
								|| row.getMaxValueTextNumeric().equals("")
								|| (Integer.parseInt(row.getMinValueTextNumeric().getText()) >= Integer.parseInt(row
										.getMaxValueTextNumeric().getText()))) {
							error = true;
							break;
						}
					} else if (row.getType().equals("date")) {
						if (row.getMinValueButtonDate().getText().equals("") || row.getMaxValueButtonDate().equals("")
								|| (row.getMinValueButtonDate().getText().compareTo(row.getMaxValueButtonDate().getText()) >= 0)) {
							error = true;
							break;
						}
					}
				}
			}
		}

		if (error) {
			final Status status = new Status(IStatus.ERROR, "Missing/wrong arguments", 0, "Error", null);
			// TODO Write messages
			final ErrorDialog dialog = new ErrorDialog(shell, "Missing/wrong arguments", "", status, IStatus.ERROR);
			dialog.open();
			return;
		}

		final List<IPattern> filterValues = new LinkedList<IPattern>();
		// Apply and Okay Button
		for (final FilterDataRow row : filterData) {
			boolean complete = false;
			if (!row.isCheckboxSet()) {
				continue;
			}
			final IPattern pattern = new Pattern(row.getTitle(), row.getType());
			if (row.getType().equals("alpha")) {
				pattern.setRelation(row.getRelationComboAlpha().getText(), row.getRelationValueTextAlpha().getText());
				complete = true;
			} else {
				if (row.getRadioButtonRelation().getSelection()) {
					if (row.getType().equals("numeric")) {
						pattern.setRelation(row.getRelationComboNumericDate().getText(), row.getRelationValueTextNumeric()
								.getText());
						complete = true;
					} else if (row.getType().equals("date")) {
						pattern.setRelation(row.getRelationComboNumericDate().getText(), row.getRelationValueButtonDate().getText());
						complete = true;
					}
				} else if (row.getRadioButtonRange().getSelection()) {
					if (row.getType().equals("numeric")) {
						pattern.setRange(row.getMinValueTextNumeric().getText(), row.getMaxValueTextNumeric().getText());
						complete = true;
					} else if (row.getType().equals("date")) {
						pattern.setRange(row.getMinValueButtonDate().getText(), row.getMaxValueButtonDate().getText());
						complete = true;
					}
				}
			}
			if (complete) {
				filterValues.add(pattern);
			}
		}

		LMLManager.getInstance().filter(gid, filterValues);

		if (buttonId == IDialogConstants.OK_ID) {
			// Okay Button
			// TODO Write in LguiItem
			lguiItem.setPattern(gid, filterValues);
			close();
		}

	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Filters");

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, 5, "Apply", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).hint(SWT.DEFAULT, SWT.DEFAULT).grab(true, true)
				.applyTo(scrolledComposite);
		final Composite composite = new Composite(scrolledComposite, SWT.NONE);
		boolean error = false;
		if (lguiItem == null || lguiItem.getTableHandler() == null) {
			error = true;
		}
		ITableColumnLayout[] columnLayouts = new ITableColumnLayout[0];
		if (!error) {
			columnLayouts = lguiItem.getTableHandler().getActiveTableColumnLayout(gid);
		}
		if (columnLayouts.length == 0) {
			error = true;
		}
		if (error) {
			final Label errorLabel = new Label(composite, SWT.NONE);
			errorLabel.setText("An error occured!");
			filterData = new FilterDataRow[0];
			return composite;
		}

		int length = columnLayouts.length;
		if (gid.equals(ILMLUIConstants.VIEW_TABLE_1)
				&& includeStatus(columnLayouts)) {
			length = length - 1;
		}

		filterData = new FilterDataRow[length];

		final VerifyListener numericListener = new VerifyListener() {

			public void verifyText(VerifyEvent e) {
				final String s = ((Text) e.widget).getText() + e.text;
				e.doit = s.matches("0|([1-9][0-9]*)");
			}
		};

		final VerifyListener alphaListener = new VerifyListener() {

			public void verifyText(VerifyEvent e) {
				final String s = ((Text) e.widget).getText() + e.text;
				e.doit = s.matches(".+");
			}
		};

		composite.setLayout(new GridLayout(2, false));

		int dif = 0;
		for (int i = 0; i < columnLayouts.length; i++) {
			if (gid.equals(ILMLUIConstants.VIEW_TABLE_1) && columnLayouts[i].getTitle().equals(ILMLUIConstants.COLUMN_STATUS)) {
				dif = 1;
				continue;
			}

			final String type = columnLayouts[i].getOrder();

			final Button checkbox = new Button(composite, SWT.CHECK);

			checkbox.setText(columnLayouts[i].getTitle());
			checkbox.setLayoutData(new GridData());

			final FilterDataRow row = new FilterDataRow(type, checkbox);

			if (type == "alpha") {
				// Input in text elements with choosing a relation operator before
				final Composite compositeText = new Composite(composite, SWT.NONE);
				compositeText.setLayout(new GridLayout(2, false));
				final Combo relationCombo = new Combo(compositeText, SWT.READ_ONLY);
				relationCombo.setItems(new String[] { "=", "!=", "=~", "!~" });
				relationCombo.select(0);
				relationCombo.setLayoutData(new GridData());
				relationCombo.setEnabled(false);
				row.addRelationComboAlpha(relationCombo);

				final Text relationValue = new Text(compositeText, SWT.LEAD | SWT.SINGLE);
				relationValue.setEnabled(false);
				relationValue.addVerifyListener(alphaListener);
				relationValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				row.addRelationValueAlpha(relationValue);

				compositeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			} else {
				// Choose between 2 different kinds of input
				final Composite compositeRadio = new Composite(composite, SWT.NONE);
				compositeRadio.setLayout(new GridLayout(2, false));
				// 1. A relation to one number
				final Button radioButtonRelation = new Button(compositeRadio, SWT.RADIO);
				radioButtonRelation.setLayoutData(new GridData());
				radioButtonRelation.setSelection(true);
				radioButtonRelation.setEnabled(false);
				radioButtonRelation.setText("");

				row.addRadioRelation(radioButtonRelation);

				final Composite compositeRelation = new Composite(compositeRadio, SWT.NONE);
				compositeRelation.setLayout(new GridLayout(2, false));
				final Combo relations = new Combo(compositeRelation, SWT.READ_ONLY);
				relations.setItems(new String[] { "=", "<", "<=", ">", ">=",
						"!=" });
				relations.select(0);

				relations.setEnabled(false);
				relations.setLayoutData(new GridData(GridData.FILL));
				row.addRelationComboNumericDate(relations);

				if (type == "numeric") {
					final Text relationValueText = new Text(compositeRelation, SWT.SINGLE | SWT.TRAIL);
					relationValueText.addVerifyListener(numericListener);
					relationValueText.setEnabled(false);
					relationValueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					row.addRelationValueNumeric(relationValueText);
				} else {
					final Button relationValueButton = new Button(compositeRelation, SWT.NONE);
					relationValueButton.setEnabled(false);
					relationValueButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					relationValueButton.addSelectionListener(new SelectDateAdpater(relationValueButton));
					row.addRelationValueDate(relationValueButton);
				}

				radioButtonRelation.addSelectionListener(new SelectionListener() {

					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						final boolean selected = radioButtonRelation.getSelection();
						if (row.getRelationComboNumericDate() != null) {
							row.getRelationComboNumericDate().setEnabled(selected);
						}
						if (row.getRelationValueTextNumeric() != null) {
							row.getRelationValueTextNumeric().setEnabled(selected);
						}
						if (row.getRelationValueButtonDate() != null) {
							row.getRelationValueButtonDate().setEnabled(selected);
						}
					}
				});

				compositeRelation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				// 2. A range between two numbers
				final Button radioButtonRange = new Button(compositeRadio, SWT.RADIO);
				radioButtonRange.setEnabled(false);
				radioButtonRange.setLayoutData(new GridData());

				row.addRadioRange(radioButtonRange);

				final Composite compositeRange = new Composite(compositeRadio, SWT.NONE);
				compositeRange.setLayout(new GridLayout(3, false));
				if (type == "numeric") {
					final Text textValueMin = new Text(compositeRange, SWT.SINGLE | SWT.TRAIL);
					textValueMin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					textValueMin.setEnabled(false);
					textValueMin.addVerifyListener(numericListener);

					// TODO replace with another combo box
					final Label labelMinus = new Label(compositeRange, SWT.NONE);
					labelMinus.setText(" - ");
					labelMinus.setLayoutData(new GridData(GridData.FILL));

					final Text textValueMax = new Text(compositeRange, SWT.SINGLE | SWT.TRAIL);
					textValueMax.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					textValueMax.setEnabled(false);
					textValueMax.addVerifyListener(numericListener);

					row.addMinValueNumeric(textValueMin);
					row.addMaxValueNumeric(textValueMax);
				} else {
					final Button buttonValueMin = new Button(compositeRange, SWT.NONE);
					buttonValueMin.setEnabled(false);
					buttonValueMin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					buttonValueMin.addSelectionListener(new SelectDateAdpater(buttonValueMin));

					final Label labelMinus = new Label(compositeRange, SWT.NONE);
					labelMinus.setText(" - ");
					labelMinus.setLayoutData(new GridData(GridData.FILL));

					final Button buttonValueMax = new Button(compositeRange, SWT.NONE);
					buttonValueMax.setEnabled(false);
					buttonValueMax.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					buttonValueMax.addSelectionListener(new SelectDateAdpater(buttonValueMax));

					row.addMinValueDate(buttonValueMin);
					row.addMaxValueDate(buttonValueMax);
				}

				radioButtonRange.addSelectionListener(new SelectionListener() {

					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						final boolean selected = radioButtonRange.getSelection();
						if (row.getMinValueTextNumeric() != null
								&& row.getMaxValueTextNumeric() != null) {
							row.getMinValueTextNumeric().setEnabled(selected);
							row.getMaxValueTextNumeric().setEnabled(selected);
						}
						if (row.getMinValueButtonDate() != null
								&& row.getMaxValueButtonDate() != null) {
							row.getMinValueButtonDate().setEnabled(selected);
							row.getMaxValueButtonDate().setEnabled(selected);
						}
					}
				});

				compositeRange.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				compositeRadio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}

			checkbox.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					final boolean selected = checkbox.getSelection();
					if (row.getRelationValueTextAlpha() != null) {
						row.getRelationValueTextAlpha().setEnabled(selected);
					}
					if (row.getRelationComboAlpha() != null) {
						row.getRelationComboAlpha().setEnabled(selected);
					}
					if (row.getRadioButtonRelation() != null && row.getRadioButtonRange() != null) {
						row.getRadioButtonRelation().setEnabled(selected);
						row.getRadioButtonRange().setEnabled(checkbox.getSelection());
						if (row.getRadioButtonRelation().getSelection()) {
							if (row.getRelationComboNumericDate() != null) {
								row.getRelationComboNumericDate().setEnabled(selected);
							}
							if (row.getRelationValueTextNumeric() != null) {
								row.getRelationValueTextNumeric().setEnabled(selected);
							}
							if (row.getRelationValueButtonDate() != null) {
								row.getRelationValueButtonDate().setEnabled(
										selected);
							}
						} else {
							if (row.getMinValueTextNumeric() != null && row.getMaxValueTextNumeric() != null) {
								row.getMinValueTextNumeric().setEnabled(selected);
								row.getMaxValueTextNumeric().setEnabled(selected);
							}
							if (row.getMinValueButtonDate() != null && row.getMaxValueButtonDate() != null) {
								row.getMinValueButtonDate().setEnabled(selected);
								row.getMaxValueButtonDate().setEnabled(selected);
							}
						}
					}
				}
			});

			filterData[i - dif] = row;
		}

		scrolledComposite.setContent(composite);

		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setSize(100, 400);
		
		List<IPattern> oldFilters = lguiItem.getPattern(gid);
		if (oldFilters.size() > 0) {
			for (IPattern filter : oldFilters) {
				for (FilterDataRow row : filterData) {
					if (!filter.getColumnTitle().equals(row.getTitle())) {
						continue;
					}
					if (filter.getType().equals("alpha")) {
						row.getRelationValueTextNumeric().setText(filter.getRelationValue());
						String[] items = row.getRelationComboAlpha().getItems();
						for (int i = 0; i < items.length; i++) {
							if (filter.getRelationOperator().equals(items[i])) {
								row.getRelationComboAlpha().select(i);
							}
						};
					} else {
						if (filter.isRange()) {
							row.getRadioButtonRange().setSelection(true);
							if (filter.getType().equals("numeric")) {
								row.getMinValueTextNumeric().setText(filter.getMinValueRange());
								row.getMaxValueTextNumeric().setText(filter.getMaxValueRange());
							} else {
								row.getMinValueButtonDate().setText(filter.getMinValueRange());
								row.getMaxValueButtonDate().setText(filter.getMaxValueRange());
							}
						} else if (filter.isRelation()) {
							String[] items = row.getRelationComboNumericDate().getItems();
							for (int i = 0; i < items.length; i++) {
								if (filter.getRelationOperator().equals(items[i])) {
									row.getRelationComboNumericDate().select(i);
								}
							};
							row.getRadioButtonRelation().setSelection(true);
							if (filter.getType().equals("numeric")) {
								row.getRelationValueTextNumeric().setText(filter.getRelationValue());
							} else {
								row.getRelationValueButtonDate().setText(filter.getRelationValue());
							}
						}
					}
				}
			}
		}
		return scrolledComposite;
	}

	// @Override
	// protected void okPressed() {
	//
	// // TODO lguiItem.getTableHandler().deletePattern();
	// //
	// // final String stringCombo = combo.getText();
	// // if (!stringCombo.equals("No Filtering")) {
	// // final String stringText = text.getText();
	// // TODO lguiItem.getTableHandler().generatePattern(stringCombo,
	// // stringText);
	// // LMLManager.getInstance().filterLgui(stringCombo, stringText);
	// // }
	// close();
	// }
}
