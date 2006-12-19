/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
/**
 * 
 */
package org.eclipse.ptp.ui.attributes;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttribute.IllegalValue;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * @author rsqrd
 *
 */
public class TextAttributeControl extends AbstractAttributeControl {

	private class TextModifyListener implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			Text text = (Text) e.getSource();
			String value = text.getText();
			boolean valid = attribute.isValid(value);
			setValid(valid);
		}

	}

	private final IAttribute attribute;

	public TextAttributeControl(Composite parent, int style, IAttribute attribute) {
		super(parent, style);
		this.attribute = attribute;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.AbstractAttributeControl#doCreateControl(org.eclipse.swt.widgets.Composite, int)
	 */
	protected Control doCreateControl(Composite parent, int style) {
		Text text = new Text(parent, style);
		text.addModifyListener(new TextModifyListener());
		return text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.AbstractAttributeControl#getAttribute()
	 */
	public IAttribute getAttribute() throws IllegalValue {
		Text text = (Text) getControl();
		String value = text.getText();
		attribute.setValue(value);
		return attribute;
	}

}
