/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.formatter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Vivian Kong
 * @since 4.2
 *
 */
public class RemoteTextEdit implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int fOffset;
	private int fLength;

	private RemoteTextEdit fParent;
	private List<RemoteTextEdit> fChildren;
	
	private static final RemoteTextEdit[] EMPTY_ARRAY= new RemoteTextEdit[0];
	
//	int fDelta;

	public RemoteTextEdit (TextEdit source) {
		fOffset= source.getOffset();
		fLength= source.getLength();
//		fDelta= 0;
	}
	
	public void addChild(RemoteTextEdit source) {
		if (fChildren == null)
			fChildren = new ArrayList<RemoteTextEdit>();
		fChildren.add(source);
		source.fParent = this;		
	}

	/**
	 * @return the fOffset
	 */
	public int getOffset() {
		return fOffset;
	}

	/**
	 * @return the fLength
	 */
	public int getLength() {
		return fLength;
	}

	/**
	 * @return the fParent
	 */
	public RemoteTextEdit getParent() {
		return fParent;
	}

	/**
	 * @return the fChildren
	 */
	public RemoteTextEdit[] getChildren() {
		if (fChildren == null)
			return EMPTY_ARRAY;
		return (RemoteTextEdit[])fChildren.toArray(new RemoteTextEdit[fChildren.size()]);
	}

}
