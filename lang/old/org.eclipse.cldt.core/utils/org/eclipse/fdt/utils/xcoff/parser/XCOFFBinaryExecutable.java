/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.fdt.utils.xcoff.parser;

import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.IBinaryParser;
import org.eclipse.fdt.core.IBinaryParser.IBinaryFile;


public class XCOFFBinaryExecutable extends XCOFFBinaryObject implements IBinaryFile {

	/**
	 * @param parser
	 * @param path
	 * @param type
	 */
	public XCOFFBinaryExecutable(IBinaryParser parser, IPath path) {
		super(parser, path, IBinaryFile.EXECUTABLE);
	}

}
