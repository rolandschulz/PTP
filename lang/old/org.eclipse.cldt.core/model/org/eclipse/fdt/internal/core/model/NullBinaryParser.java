package org.eclipse.fdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.fdt.core.CommonLanguageCore;
import org.eclipse.fdt.core.IBinaryParser;

/**
 */
public class NullBinaryParser extends PlatformObject implements IBinaryParser {

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(byte[] data, IPath path) throws IOException {
		throw new IOException(CommonLanguageCore.getResourceString("CoreModel.NullBinaryParser.Not_binary_file")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		throw new IOException(CommonLanguageCore.getResourceString("CoreModel.NullBinaryParser.Not_binary_file")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return CommonLanguageCore.getResourceString("CoreModel.NullBinaryParser.Null_Format"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser#isBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	public boolean isBinary(byte[] array, IPath path) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser#getBufferSize()
	 */
	public int getHintBufferSize() {
		return 0;
	}

}
