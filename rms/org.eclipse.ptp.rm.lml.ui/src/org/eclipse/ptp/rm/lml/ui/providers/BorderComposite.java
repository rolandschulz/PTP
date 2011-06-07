/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch,FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * This is a composite with a border. The border's width can be altered at runtime.
 * The same for background-color. This composite uses a FillLayout as layout.
 */
public class BorderComposite extends Composite {

	/**
	 * The used layout for this composite
	 */
	private final FillLayout filllayout;

	/**
	 * Create a composite with black border. The border has width 1 px in every direction.
	 * 
	 * @param parent
	 *            parent composite
	 * @param style
	 *            SWT-style
	 */
	public BorderComposite(Composite parent, int style) {
		super(parent, style);

		filllayout = new FillLayout();
		filllayout.marginWidth = 1;
		filllayout.marginHeight = 1;

		setLayout(filllayout);

		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
	}

	/**
	 * Set the border's color
	 * 
	 * @param c
	 *            SWT-color for the border
	 */
	public void setBorderColor(Color c) {
		setBackground(c);
	}

	/**
	 * Alter width of border
	 * 
	 * @param width
	 *            border-width
	 */
	public void setBorderWidth(int width) {
		filllayout.marginHeight = width;
		filllayout.marginWidth = width;

		this.layout(true);
	}

}
