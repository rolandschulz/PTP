/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.pdt.xml.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;
import org.w3c.dom.Document;


/**
 * @author richardm
 *
 */
public class X86PdtXmlGenerator extends AbstractPdtXmlGenerator {

	/**
	 * @param xmlFilePath
	 * @param eventGroupForest
	 */
	public X86PdtXmlGenerator(IPath xmlFilePath,
			EventGroupForest eventGroupForest) {
		super(xmlFilePath, eventGroupForest);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.pdt.xml.core.AbstractPdtXmlGenerator#generatePdtXmlDocument()
	 */
	@Override
	public Document generatePdtXmlDocument() {
		// TODO Generate it when creating support for x86
		return null;
	}

}
