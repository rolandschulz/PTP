package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

/**
 * This class contains all the information about a row in the FilterDialog.
 * 
 * For every active column (with exception) in a table one object of this class
 * should be generated.
 */
public class FilterDataRow {

	// Type of the data displayed in the column
	private final String type;

	// To activate the filter function for this row
	private Button checkbox;

	private Button radioRel;

	private Button radioRange;

	private Text text;

	private Combo relation;

	private Text relationValueText;

	private Button relationValueButton;

	private Text valueMinText;

	private Text valueMaxText;

	private Button valueMinButton;

	private Button valueMaxButton;

	private Combo relationText;

	public FilterDataRow(String type) {
		this.type = type;
	}

	public void addCheckbox(Button checkbox) {
		this.checkbox = checkbox;
	}

	public void addRadioRange(Button buttonRange) {
		radioRange = buttonRange;
	}

	public void addRadioRelation(Button buttonRelation) {
		radioRel = buttonRelation;
	}

	public void addRelation(Combo relations) {
		relation = relations;
	}

	public void addRelationText(Combo relationText) {
		this.relationText = relationText;
	}

	public void addRelationValueButton(Button relationValueButton) {
		this.relationValueButton = relationValueButton;
	}

	public void addRelationValueText(Text relationValueText) {
		this.relationValueText = relationValueText;
	}

	public void addText(Text text) {
		this.text = text;
	}

	public void addValueMaxButton(Button valueMaxButton) {
		this.valueMaxButton = valueMaxButton;
	}

	public void addValueMaxText(Text valueMaxText) {
		this.valueMaxText = valueMaxText;
	}

	public void addValueMinButton(Button valueMinButton) {
		this.valueMinButton = valueMinButton;
	}

	public void addValueMinText(Text valueMinText) {
		this.valueMinText = valueMinText;
	}

	public Button getRadioRange() {
		return radioRange;
	}

	public Button getRadioRelation() {
		return radioRel;
	}

	public Combo getRelation() {
		return relation;
	}

	public Combo getRelationText() {
		return relationText;
	}

	public Button getRelationValueButton() {
		return relationValueButton;
	}

	public Text getRelationValueText() {
		return relationValueText;
	}

	public Text getText() {
		return text;
	}

	public Button getValueMaxButton() {
		return valueMaxButton;
	}

	public Text getValueMaxText() {
		return valueMaxText;
	}

	public Button getValueMinButton() {
		return valueMinButton;
	}

	public Text getValueMinText() {
		return valueMinText;
	}
}