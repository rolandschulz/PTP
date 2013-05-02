/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.internal.ui.widgets;

/**
 * Describe a group containing a textbox and associated controls like labels,
 * text descriptions.
 * 
 * @author Richard Maciel, Daniel Ferber
 * 
 */
public class TextMold extends GenericControlMold {

	public static final int LIMIT_SIZE = 1 << index++;

	public static final int MULTILINE_TEXT = 1 << index++;

	public static final int WIDTH_PROPORTIONAL_NUM_CHARS = 1 << index++;

	public static final int PASSWD_FIELD = 1 << index++;

	int size = -1;

	int numberOfLines = 1;

	String value;

	public TextMold(int bitmask, String label, int size) {
		super(bitmask, label);
		setTextFieldWidth(size);
	}

	public TextMold(int bitmask, String label) {
		super(bitmask, label);
	}

	public TextMold(String label) {
		super(0, label);
	}

	/** Number of chars (average) shown in the text field. */
	public int getTextFieldWidth() {
		return size;
	}

	/** Number of chars (average) shown in the text field. */
	public void setTextFieldWidth(int size) {
		this.size = size;
		if (size > 0) {
			addBitmask(WIDTH_PROPORTIONAL_NUM_CHARS);
			addBitmask(LIMIT_SIZE);
		} else {
			removeBitmask(WIDTH_PROPORTIONAL_NUM_CHARS);
			removeBitmask(LIMIT_SIZE);
		}
	}

	public void unsetTextFieldWidth() {
		removeBitmask(WIDTH_PROPORTIONAL_NUM_CHARS);
		removeBitmask(LIMIT_SIZE);
	}

	/** Initial value. */
	public String getValue() {
		return value;
	}

	/** Initial value. */
	public void setValue(String value) {
		this.value = value;
	}

	/** Number of lines shown in the text field. */
	public int getNumberOfLines() {
		return numberOfLines;
	}

	/** Number of lines shown in the text field. */
	public void setNumberOfLines(int numberOfLines) {
		if (numberOfLines < 2) {
			numberOfLines = 1;
			removeBitmask(MULTILINE_TEXT);
		} else {
			this.numberOfLines = numberOfLines;
			addBitmask(MULTILINE_TEXT);
		}
	}

	@Override
	protected boolean hasHeight() {
		return (bitmask & TextMold.MULTILINE_TEXT) != 0;
	}

	@Override
	protected int getHeight() {
		return numberOfLines;
	}

	@Override
	protected boolean hasWidth() {
		return (bitmask & TextMold.WIDTH_PROPORTIONAL_NUM_CHARS) != 0;
	}

	@Override
	protected int getWidth() {
		return size;
	}

}
