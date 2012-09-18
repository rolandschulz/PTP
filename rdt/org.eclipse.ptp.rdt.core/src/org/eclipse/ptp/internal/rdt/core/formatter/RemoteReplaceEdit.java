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

import org.eclipse.text.edits.ReplaceEdit;

/**
 * @author Vivian Kong
 * @since 4.2
 *
 */
public class RemoteReplaceEdit extends RemoteTextEdit implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String fText;
	
	public RemoteReplaceEdit(ReplaceEdit source) {
		super(source);
		fText = source.getText();
	}

	/**
	 * @return the fText
	 */
	public String getText() {
		return fText;
	}

}
