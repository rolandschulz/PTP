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
package org.eclipse.ptp.ui.attributes;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttribute.IllegalValue;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractAttributeControl implements IAttributeControl {
	
	private final Control control;
	private boolean isValid = true;
	
	public AbstractAttributeControl(Composite parent, int style) {
		this.control = doCreateControl(parent, style);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.IAttributeControl#getControl()
	 */
	public Control getControl() {
		return control;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.IAttributeControl#isValid()
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * @param parent
	 * @param style
	 * @return
	 */
	protected abstract Control doCreateControl(Composite parent, int style);

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.IAttributeControl#getAttribute()
	 */
	public abstract IAttribute getAttribute() throws IllegalValue;

	/**
	 * @param isValid the isValid to set
	 */
	protected void setValid(boolean isValid) {
		this.isValid = isValid;
	}
}
