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

import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttribute.IllegalValue;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author rsqrd
 *
 */
public class BooleanAttributeControl extends AbstractAttributeControl {

	private final BooleanAttribute attribute;
	private Boolean initialValue;

	/**
	 * @param parent
	 * @param style
	 * @param attribute
	 */
	public BooleanAttributeControl(BooleanAttribute attribute) {
		this.attribute = attribute;
		initialValue = attribute.getValue();
		
		// is always valid
		setValid(true);
	}

	public IAttribute getAttribute() {
		return attribute;
	}

	@Override
	public void resetToInitialValue() {
		(getButton()).setSelection(initialValue.booleanValue());
	}

	@Override
	public void setCurrentToInitialValue() {
		initialValue = attribute.getValue();
	}

	private Button getButton() {
		return (Button)getControl();
	}

	protected Control doCreateControl(Composite parent, int style) {
		final Button button = new Button(parent, style);
		button.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Boolean oldValue = attribute.getValue();
				boolean selected = (getButton()).getSelection();
				Boolean newValue = new Boolean(selected);
				if (oldValue.equals(newValue)) {
					attribute.setValue(newValue);
					fireValueChanged(oldValue, newValue);
				}				
			}});
		return button;
	}

	public void setValue(String value) throws IllegalValue {
		BooleanAttribute newAttr = attribute.create(value);
		getButton().setSelection(newAttr.getValue().booleanValue());
	}

}
