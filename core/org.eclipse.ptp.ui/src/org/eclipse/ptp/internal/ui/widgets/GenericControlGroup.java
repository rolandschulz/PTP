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

import org.eclipse.ptp.internal.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * This generic group of controls provides an structure with an optional label
 * and an optional button and provides space for a user-defined control.
 * 
 * @author Richard Maciel
 * 
 */
public abstract class GenericControlGroup extends Composite {
	Label label;
	Button button;
	Control customControl;
	boolean buttonEnabled = true;

	public GenericControlGroup(Composite parent, int bitmask, int rows, int chars) {
		super(parent, SWT.NONE);
		createContents(bitmask, rows, chars);
	}

	public GenericControlGroup(Composite parent, GenericControlMold mold) {
		super(parent, SWT.NONE);
		int rows = 0, chars = 0;
		if (mold.hasHeight()) {
			rows = mold.getHeight();
		}
		if (mold.hasWidth()) {
			chars = mold.getWidth();
		}
		createContents(mold.bitmask, rows, chars);

		if (mold.label != null) {
			setLabel(mold.label);
		}
		if (mold.buttonLabel != null) {
			setButtonLabel(mold.buttonLabel);
		}
		if (mold.tooltip != null) {
			setToolTip(mold.tooltip);
		}
	}

	protected void createContents(int bitmask, int rows, int chars) {
		createLayout(bitmask);
		createLabel(bitmask);

		// Generate GridData for control
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.horizontalSpan = 1;
		customControl = createCustomControl(bitmask, gd);
		if (rows > 0) {
			gd.heightHint = getControlCharHeight(customControl) * rows;
		}
		if (chars > 0) {
			gd.widthHint = getControlCharWidth(customControl) * chars;
		}
		customControl.setLayoutData(gd);

		createButton(bitmask);
	}

	/**
	 * Create the layout for the enclosing composite..
	 * 
	 * @param mold
	 * @return Layout
	 */
	protected void createLayout(int bitmask) {
		GridLayout layout = new GridLayout();

		layout.marginHeight = LayoutDefinitions.marginHeight;
		layout.marginWidth = LayoutDefinitions.marginWidth;
		layout.marginRight = 0;
		layout.marginLeft = 0;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.horizontalSpacing = LayoutDefinitions.horizontalSpacing;
		layout.verticalSpacing = LayoutDefinitions.verticalSpacing;

		/*
		 * Start with one column
		 */
		layout.numColumns = 1;

		/*
		 * Add a second column if the label is shown on the left/
		 */
		if (((bitmask & GenericControlMold.HAS_LABEL) != 0) && ((bitmask & GenericControlMold.LABELABOVE) == 0)) {
			layout.numColumns++;
		}

		/*
		 * Add one more column if the button is shown on the right.
		 */
		if ((bitmask & GenericControlMold.HASBUTTON) != 0) {
			layout.numColumns++;
		}

		this.setLayout(layout);

		/**
		 * Set layout data if parent has a GridLayout
		 */
		if (this.getParent().getLayout() instanceof GridLayout) {
			if ((bitmask & TextMold.GRID_DATA_ALIGNMENT_FILL) != 0 || (bitmask & TextMold.GRID_DATA_GRAB_EXCESS_SPACE) != 0
					|| (bitmask & TextMold.GRID_DATA_SPAN) != 0) {
				GridData gdpp = new GridData();

				if ((bitmask & GenericControlMold.GRID_DATA_ALIGNMENT_FILL) != 0) {
					// gdpp.grabExcessHorizontalSpace = true;
					gdpp.horizontalAlignment = SWT.FILL;
				}

				if ((bitmask & GenericControlMold.GRID_DATA_GRAB_EXCESS_SPACE) != 0) {
					gdpp.grabExcessHorizontalSpace = true;
					// gdpp.horizontalAlignment = SWT.FILL;
				}

				if ((bitmask & GenericControlMold.GRID_DATA_SPAN) != 0) {
					GridLayout glayout = (GridLayout) this.getParent().getLayout();

					gdpp.horizontalSpan = glayout.numColumns;
				}
				this.setLayoutData(gdpp);
			}
		}

	}

	/**
	 * Generates a label control on this composite
	 * 
	 * @param mold
	 */
	protected void createLabel(int bitmask) {
		if ((bitmask & GenericControlMold.HAS_LABEL) != 0) {
			label = new Label(this, SWT.NONE);

			GridData gd = new GridData();
			gd.horizontalAlignment = SWT.LEFT;
			gd.verticalAlignment = SWT.CENTER;
			gd.verticalSpan = 1;

			if ((bitmask & GenericControlMold.LABELABOVE) != 0) {
				gd.horizontalSpan = ((GridLayout) this.getLayout()).numColumns;
			}

			label.setLayoutData(gd);
		}
	}

	/**
	 * User-implemented method to generate a control on this composite
	 * 
	 * @param mold
	 */
	abstract protected Control createCustomControl(int bitmask, GridData gridData);

	protected void createButton(int bitmask) {
		// Create Button if desired
		if ((bitmask & GenericControlMold.HASBUTTON) != 0) {
			button = new Button(this, SWT.BUTTON1);

			GridData gd = new GridData();
			gd.horizontalAlignment = SWT.RIGHT;
			gd.verticalAlignment = SWT.TOP;
			gd.verticalSpan = 1;
			gd.horizontalSpan = 1;
			button.setLayoutData(gd);
		}
	}

	protected int getControlCharWidth(Control control) {
		GC gc = new GC(control);
		FontMetrics fm = gc.getFontMetrics();
		int width = fm.getAverageCharWidth();
		gc.dispose();
		return width;
	}

	protected int getControlCharHeight(Control control) {
		GC gc = new GC(control);
		FontMetrics fm = gc.getFontMetrics();
		int height = fm.getHeight();
		gc.dispose();
		return height;
	}

	public Button getButton() {
		return button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		enableButton();
		if (label != null) {
			label.setEnabled(enabled);
		}
		customControl.setEnabled(enabled);
	}

	public void setButtonEnabled(boolean enabled) {
		this.buttonEnabled = enabled;
		enableButton();
	}

	private void enableButton() {
		if (button != null) {
			button.setEnabled(buttonEnabled && this.getEnabled());
		}
	}

	public String getLabel() {
		if (label != null) {
			return label.getText();
		} else {
			return null;
		}
	}

	public void setLabel(String text) {
		if (label != null) {
			label.setText(text);
		} else {
			throw new IllegalArgumentException(Messages.GenericControlGroup_NoControlGroupLabel);
		}
	}

	public String getButtonLabel() {
		if (button != null) {
			return button.getText();
		} else {
			return null;
		}
	}

	public void setButtonLabel(String text) {
		if (button != null) {
			button.setText(text);
		} else {
			throw new IllegalArgumentException(Messages.GenericControlGroup_NoControlGroupButton);
		}
	}

	public void setToolTip(String text) {
		this.setToolTipText(text);
		if (button != null) {
			button.setToolTipText(text);
		}
		if (label != null) {
			label.setToolTipText(text);
		}
	}

}
