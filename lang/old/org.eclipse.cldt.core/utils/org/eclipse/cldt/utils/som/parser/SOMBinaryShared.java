/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cldt.utils.som.parser;

import org.eclipse.cldt.core.IBinaryParser;
import org.eclipse.cldt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cldt.core.IBinaryParser.IBinaryShared;
import org.eclipse.core.runtime.IPath;


public class SOMBinaryShared extends SOMBinaryObject implements IBinaryShared {

	/**
	 * @param parser
	 * @param path
	 * @param type
	 */
	protected SOMBinaryShared(IBinaryParser parser, IPath path) {
		super(parser, path, IBinaryFile.SHARED);
	}
}
