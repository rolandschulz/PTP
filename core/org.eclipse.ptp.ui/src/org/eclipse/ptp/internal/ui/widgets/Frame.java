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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

/**
 * A frame is a customized container for controls with many additional features.
 * The frame may be used simply to group controls, without border or additional facilities.
 * Or create a multi column layout.
 * It may also be created as a frame with a user-reserved area, a description area and a
 * hideable user-reserved area. The first one is on the top and the latter is on the bottom.
 * 
 * @author Richard Maciel, Daniel Felix Ferber
 * 
 */
public class Frame extends Composite {

	private Composite topUserReservedComposite;
	private Composite bottomUserReservedComposite;
	private Composite enclosingControl;

	private Button expandButton;
	private Label descriptionLabel;
	private Group enclosingGroup;

	private boolean isExpandedLayout;

	// Keep all possible button labels
	private String expandButtonLabel = Messages.Frame_0;
	private String shrinkButtonLabel = Messages.Frame_1;

	private final int debugBitmask = SWT.None;// SWT.BORDER;

	/**
	 * Create the full featured frame based on information provided by the mold.
	 * This is the most complete and most advanced use of the Frame. Used
	 * typically to create a frame with hideable panel and explanation message.
	 * <p>
	 * The frame may have border if corresponding bit is set. The frame has margin if border is enabled. The frame has one or more
	 * columns.
	 * 
	 * @param parent
	 * @param mold
	 */
	public Frame(Composite parent, FrameMold mold) {
		this(parent, mold.bitmask, mold.columns);

		if (mold.description != null) {
			setDescription(mold.description);
		}
		if (mold.title != null) {
			setTitle(mold.getTitle());
		}
		if ((mold.bitmask & FrameMold.HAS_EXPAND) != 0 && mold.expandButtonLabel != null) {
			setExpandButtonLabel(mold.expandButtonLabel);
		}
		if ((mold.bitmask & FrameMold.HAS_EXPAND) != 0 && mold.shrinkButtonLabel != null) {
			setShrinkButtonLabel(mold.shrinkButtonLabel);
		}
	}

	/**
	 * Create the full featured frame only based on structural information.
	 * Intended for classes that extend the frame and provide their own logic
	 * to configure itself (like its own mold).
	 * 
	 * <p>
	 * The frame may have border if corresponding bit is set. The frame has margin if border is enabled. The frame has one or more
	 * columns.
	 * 
	 * @param parent
	 * @param bitmask
	 * @param columns
	 */
	public Frame(Composite parent, int bitmask, int columns) {
		super(parent, SWT.NONE);
		createContent(bitmask, columns);
	}

	/**
	 * Create a frame to group controls logically.
	 * The frame has no border.
	 * The frame has no margin.
	 * The frame as one column.
	 */
	public Frame(Composite parent) {
		this(parent, 0, 1);
	}

	/**
	 * Create a frame to group controls logically.
	 * The frame has no border.
	 * The frame has no margin.
	 * The frame as one or more columns.
	 */
	public Frame(Composite parent, int columns) {
		this(parent, 0, columns);
	}

	/**
	 * Create a frame to group controls visually.
	 * The frame has border.
	 * The frame has margin.
	 */
	public Frame(Composite parent, String title) {
		this(parent, FrameMold.HAS_FRAME, 1);
		setTitle(title);
	}

	protected void createContent(int bitmask, int columns) {
		createEnclosingControl(bitmask);
		createTopUserComposite(bitmask, columns);
		createSeparatorComposite(bitmask);
		createBottomUserComposite(bitmask, columns);

		changeExpandLayout(false);
	}

	/**
	 * Create and setup the Group control that draws the frame with the title.
	 * Only create the group if necessary.
	 */
	private void createEnclosingControl(int bitmask) {
		if ((bitmask & FrameMold.HAS_FRAME) != 0) {
			/*
			 * Create the group. The FillLayout is used to ensure that
			 * the Group will stretch over all available space.
			 */
			FillLayout fillLayout = new FillLayout();
			fillLayout.type = SWT.HORIZONTAL;
			fillLayout.marginHeight = 0;
			fillLayout.marginWidth = 0;
			fillLayout.spacing = 0;
			this.setLayout(fillLayout);

			enclosingGroup = new Group(this, SWT.NONE | debugBitmask);
			enclosingControl = enclosingGroup;
		} else {
			/*
			 * If frame is not required, then use the composite itself to hold content.
			 * This saves ressources by avoinding to create an invisible fake Group.
			 */
			enclosingControl = this;
		}

		/*
		 * Set the layout for the enclosing control.
		 */
		GridLayout enclosingControlLayout = new GridLayout();
		if ((bitmask & FrameMold.HAS_FRAME) != 0) {
			enclosingControlLayout.numColumns = 2;
			enclosingControlLayout.marginHeight = LayoutDefinitions.marginHeight;
			enclosingControlLayout.marginWidth = LayoutDefinitions.marginWidth;
			enclosingControlLayout.marginRight = LayoutDefinitions.marginRight;
			enclosingControlLayout.marginLeft = LayoutDefinitions.marginLeft;
			enclosingControlLayout.marginBottom = LayoutDefinitions.marginBottom;
			enclosingControlLayout.marginTop = LayoutDefinitions.marginTop;
			enclosingControlLayout.horizontalSpacing = LayoutDefinitions.horizontalSpacing;
			enclosingControlLayout.verticalSpacing = LayoutDefinitions.verticalSpacing;
		} else {
			enclosingControlLayout.marginHeight = LayoutDefinitions.marginHeight;
			enclosingControlLayout.marginWidth = LayoutDefinitions.marginWidth;
			enclosingControlLayout.marginRight = 0;
			enclosingControlLayout.marginLeft = 0;
			enclosingControlLayout.marginBottom = 0;
			enclosingControlLayout.marginTop = 0;
			enclosingControlLayout.horizontalSpacing = LayoutDefinitions.horizontalSpacing;
			enclosingControlLayout.verticalSpacing = LayoutDefinitions.verticalSpacing;
		}
		enclosingControl.setLayout(enclosingControlLayout);

		// Make the control fill all available width
		Composite parent = this.getParent();
		Layout parentLayout = parent.getLayout();
		if (parentLayout instanceof GridLayout) {
			GridData layoutData = (GridData) this.getLayoutData();
			if (layoutData == null) {
				layoutData = new GridData();
			}
			layoutData.grabExcessHorizontalSpace = true;
			layoutData.horizontalAlignment = SWT.FILL;
			this.setLayoutData(layoutData);
		}
	}

	/**
	 * Create the main composite.
	 */
	private void createTopUserComposite(int bitmask, int columns) {
		/*
		 * Only create this composite if a description is present or if the frame is expandable.
		 * Else, use the enclosing group itself to save ressources.
		 */
		boolean needComposite = ((bitmask & FrameMold.HAS_DESCRIPTION) != 0) || ((bitmask & FrameMold.HAS_EXPAND) != 0);
		if (needComposite) {
			topUserReservedComposite = new Composite(enclosingControl, SWT.NONE | debugBitmask);

			GridData topUserReservedCompositeLayoutData = new GridData();
			topUserReservedCompositeLayoutData.horizontalSpan = 2;
			topUserReservedCompositeLayoutData.grabExcessHorizontalSpace = true;
			topUserReservedCompositeLayoutData.grabExcessVerticalSpace = false;
			topUserReservedCompositeLayoutData.horizontalAlignment = SWT.FILL;
			topUserReservedCompositeLayoutData.verticalAlignment = SWT.FILL;
			topUserReservedComposite.setLayoutData(topUserReservedCompositeLayoutData);

			GridLayout userReservedLayout = new GridLayout();
			userReservedLayout.marginHeight = LayoutDefinitions.marginHeight;
			userReservedLayout.marginWidth = LayoutDefinitions.marginWidth;
			userReservedLayout.marginRight = 0;
			userReservedLayout.marginLeft = 0;
			userReservedLayout.marginBottom = 0;
			userReservedLayout.marginTop = 0;
			userReservedLayout.horizontalSpacing = LayoutDefinitions.horizontalSpacing;
			userReservedLayout.verticalSpacing = LayoutDefinitions.verticalSpacing;
			userReservedLayout.numColumns = columns;
			topUserReservedComposite.setLayout(userReservedLayout);
		} else {
			topUserReservedComposite = enclosingControl;
			GridLayout layout = (GridLayout) enclosingControl.getLayout();
			layout.numColumns = columns;
			if ((bitmask & FrameMold.COLUMNS_EQUAL_WIDTH) != 0) {
				layout.makeColumnsEqualWidth = true;
			}
			enclosingControl.setLayout(layout);
		}

	}

	/**
	 * Generate the expandable composite.
	 * 
	 * @param mold
	 */
	private void createBottomUserComposite(int bitmask, int columns) {
		if ((bitmask & FrameMold.HAS_EXPAND) != 0) {
			/*
			 * Put the second user-reserved composite.
			 */
			bottomUserReservedComposite = new Composite(enclosingControl, SWT.NONE | debugBitmask);
			GridData bottomUserReservedCompositeLayoutData = new GridData();
			bottomUserReservedCompositeLayoutData.horizontalSpan = 2;
			bottomUserReservedCompositeLayoutData.grabExcessHorizontalSpace = true;
			bottomUserReservedCompositeLayoutData.grabExcessVerticalSpace = false;
			bottomUserReservedCompositeLayoutData.horizontalAlignment = SWT.FILL;
			bottomUserReservedCompositeLayoutData.verticalAlignment = SWT.FILL;
			bottomUserReservedComposite.setLayoutData(bottomUserReservedCompositeLayoutData);

			/*
			 * Create a grid layout with ne number of required columns
			 * Note that when no composite was created, the layout for the enclosing control is overriden.
			 */
			GridLayout userReservedLayout = new GridLayout();
			userReservedLayout.marginHeight = LayoutDefinitions.marginHeight;
			userReservedLayout.marginWidth = LayoutDefinitions.marginWidth;
			userReservedLayout.marginRight = 0;
			userReservedLayout.marginLeft = 0;
			userReservedLayout.marginBottom = 0;
			userReservedLayout.marginTop = 0;
			userReservedLayout.horizontalSpacing = LayoutDefinitions.horizontalSpacing;
			userReservedLayout.verticalSpacing = LayoutDefinitions.verticalSpacing;
			userReservedLayout.numColumns = columns;
			if ((bitmask & FrameMold.COLUMNS_EQUAL_WIDTH) != 0) {
				userReservedLayout.makeColumnsEqualWidth = true;
			}
			bottomUserReservedComposite.setLayout(userReservedLayout);
		}
	}

	/**
	 * Create the description and the button responsible for showing/hiding the bottom composite.
	 */
	private void createSeparatorComposite(int bitmask) {
		/*
		 * Put the description.
		 */
		if ((bitmask & FrameMold.HAS_DESCRIPTION) != 0) {
			descriptionLabel = new Label(enclosingControl, SWT.NONE | SWT.WRAP | debugBitmask);
			GridData descriptionLabeLayoutData = new GridData();
			descriptionLabeLayoutData.horizontalSpan = ((bitmask & FrameMold.HAS_EXPAND) != 0 ? 1 : 2);
			descriptionLabeLayoutData.grabExcessHorizontalSpace = true;
			descriptionLabeLayoutData.grabExcessVerticalSpace = false;
			descriptionLabeLayoutData.horizontalAlignment = SWT.FILL;
			descriptionLabeLayoutData.verticalAlignment = SWT.FILL;
			descriptionLabel.setLayoutData(descriptionLabeLayoutData);
		}

		/*
		 * Put the expand button.
		 */
		if ((bitmask & FrameMold.HAS_EXPAND) != 0) {
			expandButton = new Button(enclosingControl, SWT.BUTTON1);
			GridData buttonLayoutData = new GridData();
			buttonLayoutData.horizontalSpan = ((bitmask & FrameMold.HAS_DESCRIPTION) != 0 ? 1 : 2);
			buttonLayoutData.grabExcessHorizontalSpace = false;
			buttonLayoutData.grabExcessVerticalSpace = false;
			buttonLayoutData.horizontalAlignment = SWT.RIGHT;
			buttonLayoutData.verticalAlignment = SWT.BOTTOM;
			expandButton.setLayoutData(buttonLayoutData);
			expandButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					changeExpandLayout(!isExpandedLayout);
					adjustShellSizeToFrame();
				}
			});
		}
	}

	/**
	 * Show (or hide) bottom composite according to visible parameter.
	 * 
	 * @param visible
	 *            boolean Defines if the composite will be visible or not.
	 */
	private void changeExpandLayout(boolean visible) {
		if (bottomUserReservedComposite == null) {
			return;
		}
		if (expandButton == null) {
			return;
		}

		isExpandedLayout = visible;

		GridData bottomUserReservedCompositeLayoutData = (GridData) bottomUserReservedComposite.getLayoutData();

		if (isExpandedLayout) {
			// Show it
			bottomUserReservedCompositeLayoutData.exclude = false;
			bottomUserReservedComposite.setVisible(true);
		} else {
			// Hide it
			bottomUserReservedCompositeLayoutData.exclude = true;
			bottomUserReservedComposite.setVisible(false);
		}
		bottomUserReservedComposite.setLayoutData(bottomUserReservedCompositeLayoutData);

		updateExpandButton();
	}

	private void updateExpandButton() {
		if (expandButton == null) {
			return;
		}
		String newLabel = null;
		if (isExpandedLayout) {
			newLabel = shrinkButtonLabel;
		} else {
			newLabel = expandButtonLabel;
		}
		if (newLabel != null) {
			expandButton.setText(newLabel);
		} else {
			expandButton.setText(""); //$NON-NLS-1$
		}
	}

	private void adjustShellSizeToFrame() {
		Point newSize = enclosingControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point currentSize = enclosingControl.getSize();
		int deltaY = newSize.y - currentSize.y;
		Point shellSize = getShell().getSize();
		shellSize.y += deltaY;
		getShell().setSize(shellSize);
		getShell().layout(true, true);
	}

	/**
	 * Get a composite control that represents a reserved space to the user add
	 * his controls. This method returns the top composite control
	 * 
	 * @return Composite Composite control.
	 */
	public Composite getTopUserReservedComposite() {
		return topUserReservedComposite;
	}

	/**
	 * Facility getter for {@link #getTopUserReservedComposite()}.
	 * 
	 * @return
	 */
	public Composite getComposite() {
		return topUserReservedComposite;
	}

	/**
	 * Get a composite control that represents a reserved space to the user add
	 * his controls. This method returns the bottom, hideable composite control.
	 * 
	 * @return Composite Composite control.
	 */
	public Composite getBottomUserReservedComposite() {
		return bottomUserReservedComposite;
	}

	public boolean isExpanded() {
		return isExpandedLayout;
	}

	/**
	 * Set the user-reserved bottom composite visible state
	 * 
	 * @param expanded
	 *            boolean True if composite will be visible. False, otherwise.
	 */
	public void setExpanded(boolean expanded) {
		changeExpandLayout(expanded);
		adjustShellSizeToFrame();
	}

	public String getExpandButtonLabel() {
		return expandButtonLabel;
	}

	public void setExpandButtonLabel(String expandButtonLabel) {
		if (expandButton == null) {
			throw new IllegalArgumentException(Messages.Frame_NoExpandButton);
		}
		this.expandButtonLabel = expandButtonLabel;
		updateExpandButton();
	}

	public String getShrinkButtonLabel() {
		return shrinkButtonLabel;
	}

	public void setShrinkButtonLabel(String shrinkButtonLabel) {
		if (expandButton == null) {
			throw new IllegalArgumentException(Messages.Frame_NoExpandButton);
		}
		this.shrinkButtonLabel = shrinkButtonLabel;
		updateExpandButton();
	}

	public String getTitle() {
		if (enclosingGroup != null) {
			return enclosingGroup.getText();
		} else {
			return null;
		}
	}

	public void setTitle(String label) {
		if (enclosingGroup != null) {
			enclosingGroup.setText(label);
		} else {
			throw new IllegalArgumentException(Messages.Frame_NoEnclosingGroup);
		}
	}

	public String getDescription() {
		if (descriptionLabel != null) {
			return descriptionLabel.getText();
		} else {
			return null;
		}
	}

	public void setDescription(String description) {
		if (descriptionLabel != null) {
			descriptionLabel.setText(description);
		} else {
			throw new IllegalArgumentException(Messages.Frame_NoDescriptionSet);
		}
	}
}
