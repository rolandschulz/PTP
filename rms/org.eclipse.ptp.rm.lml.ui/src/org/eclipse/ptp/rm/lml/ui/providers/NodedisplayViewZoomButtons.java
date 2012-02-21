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

import org.eclipse.ptp.rm.lml.ui.providers.support.BorderLayout;
import org.eclipse.ptp.rm.lml.ui.providers.support.BorderLayout.BorderData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Adds plus and minus-buttons to zoom rectangle sizes of
 * nodedisplay.
 * 
 */
public class NodedisplayViewZoomButtons extends NodedisplayViewZoomable {

	/**
	 * The zooming buttons for changing the minimum rectangle sizes.
	 */
	private final Button plus, minus;

	/**
	 * Paints a rectangle in the current minimum size for the nodedisplay rectangles.
	 */
	private Composite rectangleSizeComp;

	/**
	 * Frame around the buttons and the <code>rectagleSizeComp</code>
	 */
	private final Composite north;

	/**
	 * This font-instance is used for the button-labels.
	 */
	private final Font font;

	/**
	 * Create a zoomable nodedisplay decorated with buttons to allow zooming.
	 * 
	 * @param nodedisplay
	 *            the decorated nodedisplay
	 * @param parent
	 *            parent composite for this new created nodedisplay
	 */
	public NodedisplayViewZoomButtons(AbstractNodedisplayView nodedisplay, Composite parent) {

		super(nodedisplay, parent);

		north = new Composite(this, SWT.None);
		north.setLayoutData(new BorderData(BorderLayout.NFIELD));
		final RowLayout layout = new RowLayout();
		layout.spacing = 0;
		layout.center = true;
		north.setLayout(layout);

		font = new Font(Display.getCurrent(), "Monospaced", 12, SWT.BOLD); //$NON-NLS-1$
		// Make buttons as small as possible but identically sized
		minus = new Button(north, SWT.PUSH);
		minus.setText("-"); //$NON-NLS-1$
		minus.setFont(font);
		minus.pack();
		final int min = Math.min(minus.getSize().x, minus.getSize().y);

		addRectangleSizeComp();

		plus = new Button(north, SWT.PUSH);
		plus.setText("+"); //$NON-NLS-1$
		plus.setFont(font);
		plus.pack();
		final int min2 = Math.min(plus.getSize().x, plus.getSize().y);
		final int max = Math.max(min, min2);
		plus.setLayoutData(new RowData(max, max));
		minus.setLayoutData(new RowData(max, max));

		plus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setRectangleSize(getRectangleSize() + 1);
			}
		});

		minus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getRectangleSize() > 1) {
					setRectangleSize(getRectangleSize() - 1);
				}
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		font.dispose();
	}

	/**
	 * Add a composite, which indicates the size of the minimum rectangle
	 * within the user interface.
	 * 
	 */
	private void addRectangleSizeComp() {
		rectangleSizeComp = new Composite(north, SWT.PUSH);
		// Fill this composite with a white rectangle surrounded by a black border
		rectangleSizeComp.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				final int width = rectangleSizeComp.getSize().x;
				final int height = rectangleSizeComp.getSize().y;

				// Black background
				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
				e.gc.fillRectangle(0, 0, width, height);
				// white filling
				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				e.gc.fillRectangle(1, 1, width - 2, height - 2);
			}
		});

		rectangleSizeComp.setLayoutData(new RowData(getRectangleSize(), getRectangleSize()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.NodedisplayViewZoomable#handleNewRectangleSize(int)
	 */
	@Override
	protected void handleNewRectangleSize(int size) {
		// Adjust size of rectangleSizeComp
		if (rectangleSizeComp != null) {
			rectangleSizeComp.setLayoutData(new RowData(size, size));
			north.layout();
			layout();
		}
	}
}
