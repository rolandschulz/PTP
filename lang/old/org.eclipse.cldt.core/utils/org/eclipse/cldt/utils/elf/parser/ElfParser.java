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
package org.eclipse.cldt.utils.elf.parser;
 
import java.io.EOFException;
import java.io.IOException;

import org.eclipse.cldt.core.AbstractCExtension;
import org.eclipse.cldt.core.CommonLanguageCore;
import org.eclipse.cldt.core.IBinaryParser;
import org.eclipse.cldt.utils.AR;
import org.eclipse.cldt.utils.elf.Elf;
import org.eclipse.cldt.utils.elf.Elf.Attribute;
import org.eclipse.core.runtime.IPath;

/**
 */
public class ElfParser extends AbstractCExtension implements IBinaryParser {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		return getBinary(null, path);
	}


	public IBinaryFile getBinary(byte[] hints, IPath path) throws IOException {
		if (path == null) {
			throw new IOException(CommonLanguageCore.getResourceString("Util.exception.nullPath")); //$NON-NLS-1$
		}

		IBinaryFile binary = null;
		try {
			Elf.Attribute attribute = null;
			if (hints != null && hints.length > 0) {
				try {
					attribute = Elf.getAttributes(hints);
				} catch (EOFException eof) {
					// continue, the array was to small.
				}
			}

			//Take a second run at it if the data array failed. 			
 			if(attribute == null) {
				attribute = Elf.getAttributes(path.toOSString());
 			}

			if (attribute != null) {
				switch (attribute.getType()) {
					case Attribute.ELF_TYPE_EXE :
						binary = createBinaryExecutable(path);
						break;

					case Attribute.ELF_TYPE_SHLIB :
						binary = createBinaryShared(path);
						break;

					case Attribute.ELF_TYPE_OBJ :
						binary = createBinaryObject(path);
						break;

					case Attribute.ELF_TYPE_CORE :
						binary = createBinaryCore(path);
						break;
				}
			}
		} catch (IOException e) {
			binary = createBinaryArchive(path);
		}
		return binary;
	}

	/**
	 * @see org.eclipse.cldt.core.model.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "ELF"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#isBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	public boolean isBinary(byte[] array, IPath path) {
		return Elf.isElfHeader(array) || AR.isARHeader(array);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBufferSize()
	 */
	public int getHintBufferSize() {
		return 128;
	}

	/**
	 * @param path
	 * @return
	 */
	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new ElfBinaryArchive(this, path);
	}

	protected IBinaryObject createBinaryObject(IPath path) throws IOException {
		return new ElfBinaryObject(this, path, IBinaryFile.OBJECT);
	}

	protected IBinaryExecutable createBinaryExecutable(IPath path) throws IOException {
		return new ElfBinaryExecutable(this, path);
	}

	protected IBinaryShared createBinaryShared(IPath path) throws IOException {
		return new ElfBinaryShared(this, path);
	}

	protected IBinaryObject createBinaryCore(IPath path) throws IOException {
		return new ElfBinaryObject(this, path, IBinaryFile.CORE);
	}
}
