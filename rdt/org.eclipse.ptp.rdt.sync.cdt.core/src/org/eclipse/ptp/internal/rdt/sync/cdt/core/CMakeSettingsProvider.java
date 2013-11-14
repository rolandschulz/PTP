/*******************************************************************************
 * Copyright (c) 2013 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.core;

import java.io.IOException;

import javax.xml.transform.Source;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Language settings provider for projects that build with CMake
 */
public class CMakeSettingsProvider extends AbstractXMLSettingsProvider {
	@Override
	public Document getXML() throws IOException, SAXException {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.sync.cdt.core.AbstractXMLSettingsProvider#getXSLT()
	 */
	@Override
	public Source getXSLT() {
		// No XSLT, because transformations are done internally
		return null;
	}
}
