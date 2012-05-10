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
import org.eclipse.ptp.rm.lml.ui.messages.Messages;
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

public class FilterDialog extends Dialog {

	public class SelectDateAdpater extends SelectionAdapter {

		private final Button button;

		private String date;

		private Button comparisonButton = null;

		public SelectDateAdpater(Button button) {
			super();
			this.button = button;
			if (button.getText() == null) {
				date = new String();
			} else {
				date = button.getText();
			}
		}

		public SelectDateAdpater(Button button, Button comparisonButton) {
			super();
			this.button = button;
			this.comparisonButton = comparisonButton;
			date = comparisonButton.getText();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			dialog.setLayout(new GridLayout(5, false));

			if (button.getText().length() == 0 && comparisonButton != null && comparisonButton.getText().length() > 0) {
				date = comparisonButton.getText();
			}

			final DateTime calendar = new DateTime(dialog, SWT.CALENDAR | SWT.BORDER);
			final DateTime time = new DateTime(dialog, SWT.TIME);

			calendar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
			time.setLayoutData(new GridData());

			final Button ok = new Button(dialog, SWT.PUSH);
			ok.setText(Messages.FilterDialog_OK);
			ok.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			ok.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final NumberFormat formatter = new DecimalFormat("00"); //$NON-NLS-1$
					button.setText(calendar.getYear() + "-" + formatter.format(calendar.getMonth() + 1) + "-" //$NON-NLS-1$ //$NON-NLS-2$
							+ formatter.format(calendar.getDay()) + " " + formatter.format(time.getHours()) + ":" //$NON-NLS-1$ //$NON-NLS-2$
							+ formatter.format(time.getMinutes()) + ":" + formatter.format(time.getSeconds())); //$NON-NLS-1$
					date = button.getText();
					dialog.close();
				}
			});

			final Button delete = new Button(dialog, SWT.PUSH);
			delete.setText(Messages.FilterDialog_Delete);
			delete.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			delete.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					button.setText(""); //$NON-NLS-1$
					date = new String();
					dialog.close();
				}
			});

			final Button cancel = new Button(dialog, SWT.PUSH);
			cancel.setText(Messages.FilterDialog_Cancel);
			cancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			cancel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dialog.close();
				}
			});

			if (!date.isEmpty()) {
				calendar.setDate(Integer.valueOf(date.substring(0, 4)), Integer.valueOf(date.substring(5, 7)) - 1,
						Integer.valueOf(date.substring(8, 10)));
				time.setTime(Integer.valueOf(date.substring(11, 13)), Integer.valueOf(date.substring(14, 16)),
						Integer.valueOf(date.substring(17)));
			}

			dialog.setDefaultButton(ok);
			dialog.pack();
			dialog.open();
		}

	}

	private FilterDataRow[] filterData;

	private final ILguiItem lguiItem;
	private final String gid;

	private final Shell shell;

	private final List<IPattern> filterOld;

	public FilterDialog(Shell parentShell, String gid) {
		super(parentShell);
		lguiItem = LMLManager.getInstance().getSelectedLguiItem();
		this.gid = gid;
		filterOld = lguiItem.getPattern(gid);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {

		if (buttonId == IDialogConstants.CANCEL_ID) {
			// Cancel Button
			lguiItem.setPattern(gid, filterOld);
			LMLManager.getInstance().filterLgui(gid, filterOld);
			close();
			return;
		}

		boolean error = false;
		for (final FilterDataRow row : filterData) {

			if (!row.isCheckboxSet()) {
				continue;
			}
			if (row.getType().equals("alpha")) { //$NON-NLS-1$
				if (row.getRelationValueTextAlpha().getText().equals("")) { //$NON-NLS-1$
					error = true;
					break;
				}
			} else {
				if (row.getRadioButtonRelation().getSelection()) {
					if (row.getType().equals("numeric")) { //$NON-NLS-1$
						if (row.getRelationValueTextNumeric().getText().equals("")) { //$NON-NLS-1$
							error = true;
							break;
						}
					} else if (row.getType().equals("date")) { //$NON-NLS-1$
						if (row.getRelationValueButtonDate().getText().equals("")) { //$NON-NLS-1$
							error = true;
							break;
						}
					}
				} else if (row.getRadioButtonRange().getSelection()) {
					if (row.getType().equals("numeric")) { //$NON-NLS-1$
						if (row.getMinValueTextNumeric().getText().equals("") //$NON-NLS-1$
								|| row.getMaxValueTextNumeric().equals("") //$NON-NLS-1$
								|| (Integer.parseInt(row.getMinValueTextNumeric().getText()) >= Integer.parseInt(row
										.getMaxValueTextNumeric().getText()))) {
							error = true;
							break;
						}
					} else if (row.getType().equals("date")) { //$NON-NLS-1$
						if (row.getMinValueButtonDate().getText().equals("") || row.getMaxValueButtonDate().equals("") //$NON-NLS-1$ //$NON-NLS-2$
								|| (row.getMinValueButtonDate().getText().compareTo(row.getMaxValueButtonDate().getText()) >= 0)) {
							error = true;
							break;
						}
					}
				}
			}
		}

		if (error) {
			final Status status = new Status(IStatus.ERROR, Messages.FilterDialog_Missing_arguments, 0,
					Messages.FilterDialog_Missing_arguments_message, null);
			final ErrorDialog dialog = new ErrorDialog(shell, Messages.FilterDialog_Missing_arguments,
					Messages.FilterDialog_An_error_occurred, status, IStatus.ERROR);
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
			if (row.getType().equals("alpha")) { //$NON-NLS-1$
				pattern.setRelation(row.getRelationComboAlpha().getText(), row.getRelationValueTextAlpha().getText());
				complete = true;
			} else {
				if (row.getRadioButtonRelation().getSelection()) {
					if (row.getType().equals("numeric")) { //$NON-NLS-1$
						pattern.setRelation(row.getRelationComboNumericDate().getText(), row.getRelationValueTextNumeric()
								.getText());
						complete = true;
					} else if (row.getType().equals("date")) { //$NON-NLS-1$
						pattern.setRelation(row.getRelationComboNumericDate().getText(), row.getRelationValueButtonDate().getText());
						complete = true;
					}
				} else if (row.getRadioButtonRange().getSelection()) {
					if (row.getType().equals("numeric")) { //$NON-NLS-1$
						pattern.setRange(row.getMinValueTextNumeric().getText(), row.getMaxValueTextNumeric().getText());
						complete = true;
					} else if (row.getType().equals("date")) { //$NON-NLS-1$
						pattern.setRange(row.getMinValueButtonDate().getText(), row.getMaxValueButtonDate().getText());
						complete = true;
					}
				}
			}
			if (complete) {
				filterValues.add(pattern);
			}
		}

		lguiItem.setPattern(gid, filterValues);
		LMLManager.getInstance().filterLgui(gid, filterValues);

		if (buttonId == IDialogConstants.OK_ID) {
			// Okay Button
			close();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.FilterDialog_Filters);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, 5, Messages.FilterDialog_Apply, false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
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
			errorLabel.setText(Messages.FilterDialog_An_error_occurred);
			filterData = new FilterDataRow[0];
			return composite;
		}

		int length = columnLayouts.length;
		if (gid.equals(ILMLUIConstants.ID_ACTIVE_JOBS_VIEW) && includeStatus(columnLayouts)) {
			length = length - 1;
		}

		filterData = new FilterDataRow[length];

		final VerifyListener numericListener = new VerifyListener() {

			@Override
			public void verifyText(VerifyEvent e) {
				final String s = ((Text) e.widget).getText() + e.text;
				e.doit = s.matches("0|([1-9][0-9]*)"); //$NON-NLS-1$
			}
		};

		final VerifyListener alphaListener = new VerifyListener() {

			@Override
			public void verifyText(VerifyEvent e) {
				final String s = ((Text) e.widget).getText() + e.text;
				e.doit = s.matches(".+"); //$NON-NLS-1$
			}
		};

		composite.setLayout(new GridLayout(2, false));

		int dif = 0;
		for (int i = 0; i < columnLayouts.length; i++) {
			if (gid.equals(ILMLUIConstants.ID_ACTIVE_JOBS_VIEW)
					&& columnLayouts[i].getTitle().equals(ILMLUIConstants.COLUMN_STATUS)) {
				dif = 1;
				continue;
			}

			final String type = columnLayouts[i].getOrder();

			final Button checkbox = new Button(composite, SWT.CHECK);

			checkbox.setText(columnLayouts[i].getTitle());
			checkbox.setLayoutData(new GridData());

			final FilterDataRow row = new FilterDataRow(type, checkbox);

			if (type == "alpha") { //$NON-NLS-1$
				// Input in text elements with choosing a relation operator before
				final Composite compositeText = new Composite(composite, SWT.NONE);
				compositeText.setLayout(new GridLayout(2, false));
				final Combo relationCombo = new Combo(compositeText, SWT.READ_ONLY);
				relationCombo.setItems(new String[] { "=", "!=", "=~", "!~" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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

				row.addRadioRelation(radioButtonRelation);

				final Composite compositeRelation = new Composite(compositeRadio, SWT.NONE);
				compositeRelation.setLayout(new GridLayout(2, false));
				final Combo relations = new Combo(compositeRelation, SWT.READ_ONLY);
				relations.setItems(new String[] { "=", "<", "<=", ">", ">=", "!=" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				relations.select(0);

				relations.setEnabled(false);
				relations.setLayoutData(new GridData(GridData.FILL));
				row.addRelationComboNumericDate(relations);

				if (type == "numeric") { //$NON-NLS-1$
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

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					@Override
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
				if (type == "numeric") { //$NON-NLS-1$
					final Text textValueMin = new Text(compositeRange, SWT.SINGLE | SWT.TRAIL);
					textValueMin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					textValueMin.setEnabled(false);
					textValueMin.addVerifyListener(numericListener);

					// TODO replace with another combo box
					final Label labelMinus = new Label(compositeRange, SWT.NONE);
					labelMinus.setText(" - "); //$NON-NLS-1$
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

					final Label labelMinus = new Label(compositeRange, SWT.NONE);
					labelMinus.setText(" - "); //$NON-NLS-1$
					labelMinus.setLayoutData(new GridData(GridData.FILL));

					final Button buttonValueMax = new Button(compositeRange, SWT.NONE);
					buttonValueMax.setEnabled(false);
					buttonValueMax.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

					buttonValueMin.addSelectionListener(new SelectDateAdpater(buttonValueMin, buttonValueMax));
					buttonValueMax.addSelectionListener(new SelectDateAdpater(buttonValueMax, buttonValueMin));

					row.addMinValueDate(buttonValueMin);
					row.addMaxValueDate(buttonValueMax);
				}

				radioButtonRange.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						final boolean selected = radioButtonRange.getSelection();
						if (row.getMinValueTextNumeric() != null && row.getMaxValueTextNumeric() != null) {
							row.getMinValueTextNumeric().setEnabled(selected);
							row.getMaxValueTextNumeric().setEnabled(selected);
						}
						if (row.getMinValueButtonDate() != null && row.getMaxValueButtonDate() != null) {
							row.getMinValueButtonDate().setEnabled(selected);
							row.getMaxValueButtonDate().setEnabled(selected);
						}
					}
				});

				compositeRange.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				compositeRadio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}

			checkbox.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
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
								row.getRelationValueButtonDate().setEnabled(selected);
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

			final List<IPattern> oldFilters = lguiItem.getPattern(gid);
			if (oldFilters.size() > 0) {
				for (final IPattern filter : oldFilters) {
					if (!filter.getColumnTitle().equals(row.getTitle())) {
						continue;
					}
					row.getCheckbox().setSelection(true);
					if (filter.getType().equals("alpha")) { //$NON-NLS-1$
						row.getRelationValueTextAlpha().setText(filter.getRelationValue());
						row.getRelationValueTextAlpha().setEnabled(true);
						final String[] items = row.getRelationComboAlpha().getItems();
						row.getRelationComboAlpha().setEnabled(true);
						for (int j = 0; j < items.length; j++) {
							if (filter.getRelationOperator().equals(items[j])) {
								row.getRelationComboAlpha().select(j);
							}
						}
					} else {
						row.getRadioButtonRange().setEnabled(true);
						row.getRadioButtonRelation().setEnabled(true);
						if (filter.isRange()) {
							row.getRadioButtonRange().setSelection(true);
							row.getRadioButtonRelation().setSelection(false);
							if (filter.getType().equals("numeric")) { //$NON-NLS-1$
								row.getMinValueTextNumeric().setText(filter.getMinValueRange());
								row.getMaxValueTextNumeric().setText(filter.getMaxValueRange());
								row.getMinValueTextNumeric().setEnabled(true);
								row.getMaxValueTextNumeric().setEnabled(true);
							} else {
								row.getMinValueButtonDate().setText(filter.getMinValueRange());
								row.getMaxValueButtonDate().setText(filter.getMaxValueRange());
								row.getMinValueButtonDate().setEnabled(true);
								row.getMaxValueButtonDate().setEnabled(true);
							}
						} else if (filter.isRelation()) {
							row.getRadioButtonRelation().setSelection(true);
							row.getRelationComboNumericDate().setEnabled(true);
							final String[] items = row.getRelationComboNumericDate().getItems();
							for (int j = 0; j < items.length; j++) {
								if (filter.getRelationOperator().equals(items[j])) {
									row.getRelationComboNumericDate().select(j);
								}
							}
							if (filter.getType().equals("numeric")) { //$NON-NLS-1$
								row.getRelationValueTextNumeric().setText(filter.getRelationValue());
								row.getRelationValueTextNumeric().setEnabled(true);
							} else {
								row.getRelationValueButtonDate().setText(filter.getRelationValue());
								row.getRelationValueButtonDate().setEnabled(true);
							}
						}
					}
				}
			}

			filterData[i - dif] = row;
		}

		scrolledComposite.setContent(composite);

		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setSize(100, 400);

		return scrolledComposite;
	}
}
