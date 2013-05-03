/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.ptp.internal.rm.lml.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;
import org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.ui.providers.support.BorderLayout;
import org.eclipse.ptp.rm.lml.ui.providers.support.BorderLayout.BorderData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * Adds plus and minus-buttons to change the level of expansion of the
 * nodedisplay.
 * 
 */
public class NodedisplayViewMaxlevelAdjust extends NodedisplayViewDecorator {

	// TODO update label on zoom events

	/**
	 * The zooming buttons for changing the minimum rectangle sizes.
	 */
	private Button plus, minus;

	/**
	 * Frame around the buttons and the <code>rectagleSizeComp</code>
	 */
	private Composite north;

	/**
	 * This font-instance is used for the button-labels.
	 */
	private Font font;

	/**
	 * A label showing the current maxLevel
	 */
	private Label maxLevelLabel;

	/**
	 * if true, additional buttons are added into this composite in the north,
	 * otherwise only the additional functions are provided by this composite
	 */
	private final boolean addButtons;

	/**
	 * Create a zoomable nodedisplay decorated with buttons to allow zooming.
	 * 
	 * @param nodedisplay
	 *            the decorated nodedisplay
	 * @param addButtons
	 *            if true, additional buttons are added into this composite in the north,
	 *            otherwise only the additional functions are provided by this composite
	 * 
	 * @param parent
	 *            parent composite for this new created nodedisplay
	 */
	public NodedisplayViewMaxlevelAdjust(AbstractNodedisplayView nodedisplay, boolean addButtons, Composite parent) {

		super(nodedisplay, parent);

		this.addButtons = addButtons;

		if (addButtons) {
			north = new Composite(this, SWT.None);
			north.setLayoutData(new BorderData(BorderLayout.NFIELD));
			final RowLayout layout = new RowLayout();
			layout.spacing = 0;
			layout.center = true;
			north.setLayout(layout);

			font = new Font(Display.getCurrent(), "Monospaced", 12, SWT.BOLD); //$NON-NLS-1$
			// Make buttons as small as possible but identically sized
			minus = new Button(north, SWT.PUSH);
			minus.setText(Messages.NodedisplayViewMaxlevelAdjust_1);
			minus.setFont(font);
			minus.pack();
			final int min = Math.min(minus.getSize().x, minus.getSize().y);

			maxLevelLabel = new Label(north, SWT.NONE);
			maxLevelLabel.setText(String.valueOf(nodedisplay.getShownMaxLevel()));

			plus = new Button(north, SWT.PUSH);
			plus.setText(Messages.NodedisplayViewMaxlevelAdjust_2);
			plus.setFont(font);
			plus.pack();
			final int min2 = Math.min(plus.getSize().x, plus.getSize().y);
			final int max = Math.max(min, min2);
			plus.setLayoutData(new RowData(max, max));
			minus.setLayoutData(new RowData(max, max));

			plus.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					increaseMaximumLevel();
				}
			});

			minus.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					decreaseMaximumLevel();
				}
			});

			north.layout();

			// Listen for zoom events
			addZoomListener(new INodedisplayZoomListener() {

				@Override
				public void handleEvent(INodedisplayZoomEvent event) {
					handleUpdate();
				}

			});

		}
	}

	/**
	 * Inverse function to increaseMaximumLevel.
	 * Shows less detailed information in the nodedisplay.
	 * For example if currently a rectangle is displaying
	 * a processor, after the call of this function one
	 * rectangle would represent nodes assuming that nodes
	 * are the parent grouping of processors.
	 */
	public void decreaseMaximumLevel() {
		if (nodedisplayView.getShownMaxLevel() > nodedisplayView.getMinimumLevelOfDetail()) {
			nodedisplayView.setMaxLevel(nodedisplayView.getShownMaxLevel() - 1);
			update();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		if (addButtons) {
			font.dispose();
		}
	}

	/**
	 * Tries to increase the maximum level shown by this nodedisplay.
	 * Therefore simply calls the setFixedLevel-function with an
	 * increased level value.
	 */
	public void increaseMaximumLevel() {
		nodedisplayView.setMaxLevel(nodedisplayView.getShownMaxLevel() + 1);
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.NodedisplayViewDecorator#update()
	 */
	@Override
	public void update() {
		super.update();
		handleUpdate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.NodedisplayViewDecorator#update(org.eclipse.ptp.rm.lml.core.model.ILguiItem)
	 */
	@Override
	public void update(ILguiItem lguiItem) {
		super.update(lguiItem);
		handleUpdate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.NodedisplayViewDecorator#update(org.eclipse.ptp.rm.lml.core.model.ILguiItem,
	 * org.eclipse.ptp.rm.lml.core.elements.Nodedisplay)
	 */
	@Override
	public void update(ILguiItem lguiItem, Nodedisplay nodedislay) {
		super.update(lguiItem, nodedislay);
		handleUpdate();
	}

	/**
	 * Reacts on an update call. Updates the currently shown maxlevel.
	 */
	protected void handleUpdate() {
		if (addButtons) {
			maxLevelLabel.setText(String.valueOf(nodedisplayView.getShownMaxLevel()));
			north.layout();
		}
	}
}
