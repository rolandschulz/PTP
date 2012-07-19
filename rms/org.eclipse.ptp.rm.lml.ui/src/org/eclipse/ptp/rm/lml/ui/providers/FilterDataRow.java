/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch,FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

/**
 * This class contains all the information about a row in the FilterDialog.
 * 
 * For every active column (with exception) in a table one object of this class should be generated.
 */
public class FilterDataRow {

	// Type of the data displayed in the column
	private String type;

	// To activate the filter function for this row
	private final Button checkbox;

	// To active a relation; only for the types numeric and date
	private Button radioButoonRelation;

	// To active a range; only for the types numeric and date
	private Button radioButtonRange;

	// The value for comparison, only for type alpha
	private Text valueAlpha;

	// The operator how to compare a value of the input with an given value; only for the type numeric and date
	private Combo relationComboNumericDate;

	// The value for comparison; only for the type numeric
	private Text valueNumeric;

	// The value for comparison; only for the type date
	private Button valueDate;

	// The minimal value in a range; only for the type numeric
	private Text minValueNumeric;

	// The maximal value in a range; only for the type numeric
	private Text maxValueNumeric;

	// The minimal value in a range; only for the type date
	private Button minValueDate;

	// The maximal value in a range; only for the type date
	private Button maxValueDate;

	// The value for comparison; only for the type alpha
	private Combo relationComboAlpha;

	/**
	 * Constructor.
	 * 
	 * @param type
	 * @param checkbox
	 */
	public FilterDataRow(String type, Button checkbox) {
		this.type = type;
		this.checkbox = checkbox;
		if (type == null) {
			this.type = "alpha"; //$NON-NLS-1$
		}
	}

	/**
	 * Adding a input button for the maximal value (date).
	 * 
	 * @param value
	 */
	public void addMaxValueDate(Button value) {
		maxValueDate = value;
	}

	/**
	 * Adding a input text for the maximal value (numeric).
	 * 
	 * @param value
	 */
	public void addMaxValueNumeric(Text value) {
		maxValueNumeric = value;
	}

	/**
	 * Adding a input button for the minimal value (date).
	 * 
	 * @param value
	 */
	public void addMinValueDate(Button value) {
		minValueDate = value;
	}

	/**
	 * Adding a input text for the minimal value (numeric).
	 * 
	 * @param value
	 */
	public void addMinValueNumeric(Text value) {
		minValueNumeric = value;
	}

	/**
	 * Adding a radio button for range.
	 * 
	 * @param button
	 */
	public void addRadioRange(Button button) {
		radioButtonRange = button;
	}

	/**
	 * Adding a radio button for relation.
	 * 
	 * @param button
	 */
	public void addRadioRelation(Button button) {
		radioButoonRelation = button;
	}

	/**
	 * Adding a combo box which includes all possible relation operators for alpha.
	 * 
	 * @param relation
	 */
	public void addRelationComboAlpha(Combo relation) {
		relationComboAlpha = relation;
	}

	/**
	 * Adding a combo box which includes all possible relation operators for numeric and date.
	 * 
	 * @param relation
	 */
	public void addRelationComboNumericDate(Combo relation) {
		relationComboNumericDate = relation;
	}

	/**
	 * Adding a input text for the comparison value (alpha).
	 * 
	 * @param value
	 */
	public void addRelationValueAlpha(Text value) {
		valueAlpha = value;
	}

	/**
	 * Adding a input button for the comparison value (date).
	 * 
	 * @param value
	 */
	public void addRelationValueDate(Button value) {
		valueDate = value;
	}

	/**
	 * Adding a input text for comparison value (numeric).
	 * 
	 * @param value
	 */
	public void addRelationValueNumeric(Text value) {
		valueNumeric = value;
	}

	public Button getCheckbox() {
		return checkbox;
	}

	/**
	 * Getting the button with the maximal value (date).
	 * 
	 * @return button
	 */
	public Button getMaxValueButtonDate() {
		return maxValueDate;
	}

	/**
	 * Getting the text with the maximal value (numeric).
	 * 
	 * @return text
	 */
	public Text getMaxValueTextNumeric() {
		return maxValueNumeric;
	}

	/**
	 * Getting the button with the minimal value (date).
	 * 
	 * @return button
	 */
	public Button getMinValueButtonDate() {
		return minValueDate;
	}

	/**
	 * Getting the text with the minimal value (numeric).
	 * 
	 * @return text
	 */
	public Text getMinValueTextNumeric() {
		return minValueNumeric;
	}

	/**
	 * Getting the button for range.
	 * 
	 * @return button for range
	 */
	public Button getRadioButtonRange() {
		return radioButtonRange;
	}

	/**
	 * Getting the button for relation.
	 * 
	 * @return button for relation.
	 */
	public Button getRadioButtonRelation() {
		return radioButoonRelation;
	}

	/**
	 * Getting the combo box including all comparison operators (alpha).
	 * 
	 * @return combo box
	 */
	public Combo getRelationComboAlpha() {
		return relationComboAlpha;
	}

	/**
	 * Getting the combo box including all comparison operators (numeric and date).
	 * 
	 * @return combo box
	 */
	public Combo getRelationComboNumericDate() {
		return relationComboNumericDate;
	}

	/**
	 * Getting the button with the comparison value (date).
	 * 
	 * @return button
	 */
	public Button getRelationValueButtonDate() {
		return valueDate;
	}

	/**
	 * Getting the text with the comparison value (alpha).
	 * 
	 * @return text
	 */
	public Text getRelationValueTextAlpha() {
		return valueAlpha;
	}

	/**
	 * Getting the text with the comparison value (numeric).
	 * 
	 * @return
	 */
	public Text getRelationValueTextNumeric() {
		return valueNumeric;
	}

	/**
	 * Getting the title of the row
	 * 
	 * @return title
	 */
	public String getTitle() {
		return checkbox.getText();
	}

	/**
	 * Getting the sort type of the row.
	 * 
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Checks if the checkbox is set.
	 * 
	 * @return
	 */
	public boolean isCheckboxSet() {
		return checkbox.getSelection();
	}

	public void setCheckbox(boolean value) {
		checkbox.setSelection(value);
	}
}