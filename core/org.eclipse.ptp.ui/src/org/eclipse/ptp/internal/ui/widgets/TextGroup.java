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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * @author Richard Maciel, Daniel Ferber
 *
 */
public class TextGroup extends GenericControlGroup {
	
//	public static final int MAX_SIZE = Text.LIMIT;
	
	Text text;
	
	public TextGroup(Composite parent, TextMold mold) {
		super(parent, mold);
		TextMold tmold = (TextMold)mold;
		if(tmold.value != null)
			text.setText(tmold.getValue());
		
		if((tmold.bitmask & TextMold.LIMIT_SIZE) != 0) {
			text.setTextLimit(tmold.getTextFieldWidth());
		}

	}

	protected Control createCustomControl(int bitmask, GridData gd) {
		if ( (bitmask & TextMold.MULTILINE_TEXT) != 0) {
			text = new Text(this, SWT.BORDER | SWT.MULTI);	
		} else if( (bitmask & TextMold.PASSWD_FIELD) != 0) {
			text = new Text(this, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		} else {
			text = new Text(this, SWT.BORDER | SWT.SINGLE);
		}
			
		gd.grabExcessHorizontalSpace = true;
		if ((bitmask & TextMold.WIDTH_PROPORTIONAL_NUM_CHARS) != 0) {
			gd.horizontalAlignment = SWT.LEFT;
		} else {
			gd.horizontalAlignment = SWT.FILL;
		}		
		return text;
	}

	public Text getText() {
		return text;
	}

	public String getString() {
		return text.getText();
	}
	
	public void setString(String s) {
		if (s == null) {
			text.setText(""); //$NON-NLS-1$
		} else {
			text.setText(s);
		}
	}
	
	public void addModifyListener(ModifyListener listener) {
		text.addModifyListener(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		text.removeModifyListener(listener);
	}

}
