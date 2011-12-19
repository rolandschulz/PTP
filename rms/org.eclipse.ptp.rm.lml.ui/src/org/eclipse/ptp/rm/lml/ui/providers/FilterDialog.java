package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.ITableColumnLayout;
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

public class FilterDialog extends Dialog {

	final SelectionAdapter pickDate = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM);
			dialog.setLayout(new GridLayout(2, false));

			final DateTime calendar = new DateTime(dialog, SWT.CALENDAR);
			calendar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false, 2, 1));
			final DateTime time = new DateTime(dialog, SWT.TIME);
			time.setLayoutData(new GridData());

			final Button ok = new Button(dialog, SWT.PUSH);
			ok.setText("ok");
			ok.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			ok.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					dialog.close();
				}
			});
			dialog.setDefaultButton(ok);
			dialog.pack();
			dialog.open();
		}
	};

	private FilterDataRow[] filterData;

	private final ILguiItem lguiItem;
	private final String gid;

	private final Shell shell;

	public FilterDialog(Shell parentShell, String gid) {
		super(parentShell);
		lguiItem = LMLManager.getInstance().getSelectedLguiItem();
		this.gid = gid;
		this.shell = parentShell;
		shell.setText("Filters");
		setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL);
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
			close();
			return;
		}

		// Auslesen der Daten

		if (buttonId == IDialogConstants.OK_ID) {
			// Okay Button
		} else {
			// Apply Button
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, 0, "Apply", true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// final Composite composite = (Composite)
		// super.createDialogArea(parent);
		final ScrolledComposite scrolledComposite = new ScrolledComposite(
				parent, SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.hint(SWT.DEFAULT, SWT.DEFAULT).grab(true, true)
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
			if (gid.equals(ILMLUIConstants.VIEW_TABLE_1)
					&& columnLayouts[i].getTitle().equals(ILMLUIConstants.COLUMN_STATUS)) {
				dif = 1;
				continue;
			}

			final String type = columnLayouts[i].getOrder();

			final FilterDataRow row = new FilterDataRow(type);

			final Button checkbox = new Button(composite, SWT.CHECK);

			checkbox.setText(columnLayouts[i].getTitle());
			checkbox.setLayoutData(new GridData());

			row.addCheckbox(checkbox);

			if (type == "alpha") {
				final Composite compositeText = new Composite(composite,
						SWT.NONE);
				compositeText.setLayout(new GridLayout(2, false));
				final Combo relationText = new Combo(compositeText, SWT.READ_ONLY);
				relationText.setItems(new String[] { "=", "!=" });
				relationText.select(0);
				relationText.setLayoutData(new GridData());
				relationText.setEnabled(false);
				row.addRelationText(relationText);

				final Text text = new Text(compositeText, SWT.LEAD | SWT.SINGLE);
				text.setEnabled(false);
				text.addVerifyListener(alphaListener);
				text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				row.addText(text);

				compositeText.setLayoutData(new GridData(
						GridData.FILL_HORIZONTAL));
			} else {
				final Composite compositeRadio = new Composite(composite,
						SWT.NONE);
				compositeRadio.setLayout(new GridLayout(2, false));
				final Button buttonRelation = new Button(compositeRadio,
						SWT.RADIO);
				buttonRelation.setLayoutData(new GridData());
				buttonRelation.setSelection(true);
				buttonRelation.setEnabled(false);
				buttonRelation.setText("");

				row.addRadioRelation(buttonRelation);

				final Composite compositeRel = new Composite(compositeRadio,
						SWT.NONE);
				compositeRel.setLayout(new GridLayout(2, false));
				final Combo relations = new Combo(compositeRel, SWT.READ_ONLY);
				relations.setItems(new String[] { "=", "<", "<=", ">", ">=",
						"!=" });
				relations.select(0);

				relations.setEnabled(false);
				relations.setLayoutData(new GridData(GridData.FILL));
				row.addRelation(relations);

				if (type == "numeric") {
					final Text relationValueText = new Text(compositeRel,
							SWT.SINGLE | SWT.TRAIL);
					relationValueText.addVerifyListener(numericListener);
					relationValueText.setEnabled(false);
					relationValueText.setLayoutData(new GridData(
							GridData.FILL_HORIZONTAL));
					row.addRelationValueText(relationValueText);
				} else {
					final Button relationValueButton = new Button(compositeRel,
							SWT.NONE);
					relationValueButton.setEnabled(false);
					relationValueButton.setLayoutData(new GridData(
							GridData.FILL_HORIZONTAL));

					relationValueButton.addSelectionListener(pickDate);
					row.addRelationValueButton(relationValueButton);
				}

				buttonRelation.addSelectionListener(new SelectionListener() {

					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						final boolean selected = buttonRelation.getSelection();
						if (row.getRelation() != null) {
							row.getRelation().setEnabled(selected);
						}
						if (row.getRelationValueText() != null) {
							row.getRelationValueText().setEnabled(selected);
						}
						if (row.getRelationValueButton() != null) {
							row.getRelationValueButton().setEnabled(selected);
						}
					}
				});

				compositeRel.setLayoutData(new GridData(
						GridData.FILL_HORIZONTAL));

				final Button buttonRange = new Button(compositeRadio, SWT.RADIO);
				buttonRange.setEnabled(false);
				buttonRange.setLayoutData(new GridData());

				row.addRadioRange(buttonRange);

				final Composite compositeRange = new Composite(compositeRadio,
						SWT.NONE);
				compositeRange.setLayout(new GridLayout(3, false));
				if (type == "numeric") {
					final Text valueMinText = new Text(compositeRange,
							SWT.SINGLE | SWT.TRAIL);
					valueMinText.setLayoutData(new GridData(
							GridData.FILL_HORIZONTAL));
					valueMinText.setEnabled(false);
					valueMinText.addVerifyListener(numericListener);

					final Label minusLabel = new Label(compositeRange, SWT.NONE);
					minusLabel.setText(" - ");
					minusLabel.setLayoutData(new GridData(GridData.FILL));

					final Text valueMaxText = new Text(compositeRange,
							SWT.SINGLE | SWT.TRAIL);
					valueMaxText.setLayoutData(new GridData(
							GridData.FILL_HORIZONTAL));
					valueMaxText.setEnabled(false);
					valueMaxText.addVerifyListener(numericListener);

					row.addValueMinText(valueMinText);
					row.addValueMaxText(valueMaxText);
				} else {
					final Button valueMinButton = new Button(compositeRange,
							SWT.NONE);
					valueMinButton.setEnabled(false);
					valueMinButton.setLayoutData(new GridData(
							GridData.FILL_HORIZONTAL));
					valueMinButton.addSelectionListener(pickDate);

					final Label minusLabel = new Label(compositeRange, SWT.NONE);
					minusLabel.setText(" - ");
					minusLabel.setLayoutData(new GridData(GridData.FILL));

					final Button valueMaxButton = new Button(compositeRange,
							SWT.NONE);
					valueMaxButton.setEnabled(false);
					valueMaxButton.setLayoutData(new GridData(
							GridData.FILL_HORIZONTAL));
					valueMaxButton.addSelectionListener(pickDate);

					row.addValueMinButton(valueMinButton);
					row.addValueMaxButton(valueMaxButton);
				}

				buttonRange.addSelectionListener(new SelectionListener() {

					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						final boolean selected = buttonRange.getSelection();
						if (row.getValueMinText() != null
								&& row.getValueMaxText() != null) {
							row.getValueMinText().setEnabled(selected);
							row.getValueMaxText().setEnabled(selected);
						}
						if (row.getValueMinButton() != null
								&& row.getValueMaxButton() != null) {
							row.getValueMinButton().setEnabled(selected);
							row.getValueMaxButton().setEnabled(selected);
						}
					}
				});

				compositeRange.setLayoutData(new GridData(
						GridData.FILL_HORIZONTAL));

				compositeRadio.setLayoutData(new GridData(
						GridData.FILL_HORIZONTAL));
			}

			checkbox.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					final boolean selected = checkbox.getSelection();
					if (row.getText() != null) {
						row.getText().setEnabled(selected);
					}
					if (row.getRelationText() != null) {
						row.getRelationText().setEnabled(selected);
					}
					if (row.getRadioRelation() != null
							&& row.getRadioRange() != null) {
						row.getRadioRelation().setEnabled(selected);
						row.getRadioRange().setEnabled(checkbox.getSelection());
						if (row.getRadioRelation().getSelection()) {
							if (row.getRelation() != null) {
								row.getRelation().setEnabled(selected);
							}
							if (row.getRelationValueText() != null) {
								row.getRelationValueText().setEnabled(selected);
							}
							if (row.getRelationValueButton() != null) {
								row.getRelationValueButton().setEnabled(
										selected);
							}
						} else {
							if (row.getValueMinText() != null
									&& row.getValueMaxText() != null) {
								row.getValueMinText().setEnabled(selected);
								row.getValueMaxText().setEnabled(selected);
							}
							if (row.getValueMinButton() != null
									&& row.getValueMaxButton() != null) {
								row.getValueMinButton().setEnabled(selected);
								row.getValueMaxButton().setEnabled(selected);
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

		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
		scrolledComposite.setSize(100, 400);
		// System.out.println(scrolledComposite.getVerticalBar().getIncrement());
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
