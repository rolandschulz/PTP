/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cldt.utils.coff.parser;

import org.eclipse.cldt.core.IBinaryParser;
import org.eclipse.cldt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cldt.core.IBinaryParser.IBinaryFile;
import org.eclipse.core.runtime.IPath;


public class CygwinPEBinaryExecutable extends CygwinPEBinaryObject implements IBinaryExecutable {

	/**
	 * @param parser
	 * @param path
	 * @param executable
	 */
	public CygwinPEBinaryExecutable(IBinaryParser parser, IPath path, int executable) {
		super(parser, path, IBinaryFile.EXECUTABLE);
	}

}
