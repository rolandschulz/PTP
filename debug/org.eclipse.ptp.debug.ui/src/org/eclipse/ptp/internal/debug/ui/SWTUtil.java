/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Clement chu
 */
public class SWTUtil {
	/**
	 * Returns the standard display to be used. The method first checks, if the
	 * thread calling this method has an associated disaply. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	/**
	 * Returns the shell for the given widget. If the widget doesn't represent
	 * a SWT object that manage a shell, <code>null</code> is returned.
	 * 
	 * @return the shell for the given widget
	 */
	public static Shell getShell(Widget widget) {
		if (widget instanceof Control)
			return ((Control)widget).getShell();
		if (widget instanceof Caret)
			return ((Caret)widget).getParent().getShell();
		if (widget instanceof DragSource)
			return ((DragSource)widget).getControl().getShell();
		if (widget instanceof DropTarget)
			return ((DropTarget)widget).getControl().getShell();
		if (widget instanceof Menu)
			return ((Menu)widget).getParent().getShell();
		if (widget instanceof ScrollBar)
			return ((ScrollBar)widget).getParent().getShell();
		return null;
	}

	/**
	 * Returns a width hint for a button control.
	 */
	public static int getButtonWidthHint(Button button) {
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	/**
	 * Returns a height hint for a button control.
	 */
//	public static int getButtonHeigthHint(Button button) {
//		PixelConverter converter = new PixelConverter(button);
//		return converter.convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
//	}

	/**
	 * Sets width and height hint for the button control. <b>Note:</b> This
	 * is a NOP if the button's layout data is not an instance of <code>GridData</code>.
	 * 
	 * @param the button for which to set the dimension hint
	 */
	public static void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd = button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData)gd).widthHint = getButtonWidthHint(button);
			((GridData)gd).horizontalAlignment = GridData.FILL;	 
		}
	}
}
