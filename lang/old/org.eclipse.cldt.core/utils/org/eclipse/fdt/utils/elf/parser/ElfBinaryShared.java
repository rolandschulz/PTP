/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.fdt.utils.elf.parser;

import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.fdt.core.IBinaryParser.IBinaryShared;


public class ElfBinaryShared extends ElfBinaryObject implements IBinaryShared {

	/**
	 * @param parser
	 * @param p
	 */
	public ElfBinaryShared(ElfParser parser, IPath p) {
		super(parser, p, IBinaryFile.SHARED);
	}
}
