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

import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.DateAttribute;
import org.eclipse.ptp.core.attributes.DoubleAttribute;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class AttributeControlVisitor implements DateAttribute.IVisitor,
		BooleanAttribute.IVisitor,
		DoubleAttribute.IVisitor,
		IntegerAttribute.IVisitor,
		StringAttribute.IVisitor,
		EnumeratedAttribute.IVisitor {
	
	private IAttributeControl editor = null;
	private final Composite parent;
	private int style;
	
	/**
	 * @param parent
	 * @param style
	 */
	public AttributeControlVisitor(final Composite parent, final int style) {
		this.parent = parent;
		this.style = style;
	}

	public IAttributeControl getEditor() {
		return editor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.BooleanAttribute.IVisitor#visit(org.eclipse.ptp.core.attributes.BooleanAttribute)
	 */
	public void visit(BooleanAttribute attribute) {
		// we must have a check or toggle
		if ((style & (SWT.CHECK | SWT.TOGGLE)) == 0) {
			style |= SWT.CHECK;
		}
		editor = new BooleanAttributeControl(parent, style, attribute);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.DateAttribute.IVisitor#visit(org.eclipse.ptp.core.attributes.DateAttribute)
	 */
	public void visit(DateAttribute attribute) {
		editor = new TextAttributeControl(parent, style, attribute);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.DoubleAttribute.IVisitor#visit(org.eclipse.ptp.core.attributes.DoubleAttribute)
	 */
	public void visit(DoubleAttribute attribute) {
		editor = new TextAttributeControl(parent, style, attribute);
	}

	public void visit(EnumeratedAttribute attribute) {
		editor = new EnumeratedAttributeControl(parent, style, attribute);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IntegerAttribute.IVisitor#visit(org.eclipse.ptp.core.attributes.IntegerAttribute)
	 */
	public void visit(IntegerAttribute attribute) {
		editor = new TextAttributeControl(parent, style, attribute);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.StringAttribute.IVisitor#visit(org.eclipse.ptp.core.attributes.StringAttribute)
	 */
	public void visit(StringAttribute attribute) {
		editor = new TextAttributeControl(parent, style, attribute);
	}

}
