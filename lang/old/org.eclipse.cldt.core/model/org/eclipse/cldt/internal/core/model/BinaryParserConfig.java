/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.core.model;

import org.eclipse.cldt.core.IBinaryParser;
import org.eclipse.cldt.core.ICExtensionReference;
import org.eclipse.core.runtime.CoreException;

/*
 * BinaryParserConfig 
 */
public class BinaryParserConfig {
	private IBinaryParser parser;
	private final String id;
	private final ICExtensionReference ref;

	public BinaryParserConfig(IBinaryParser parser, String id) {
		this.parser = parser;
		this.id = id;
		this.ref = null;
	}
	
	public BinaryParserConfig(ICExtensionReference ref) {
		this.ref = ref;
		this.id = ref.getID();
	}

	public String getId() {
		return id;
	}

	public IBinaryParser getBinaryParser() throws CoreException {
		if (parser == null) {
			parser = (IBinaryParser)ref.createExtension();
		}
		return parser;
	}
}
