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
import org.eclipse.fdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.fdt.core.IBinaryParser.IBinaryFile;


public class ElfBinaryExecutable extends ElfBinaryObject implements IBinaryExecutable {

	
	public ElfBinaryExecutable(ElfParser parser, IPath p) {
		super(parser, p, IBinaryFile.EXECUTABLE);
	}
	
}
