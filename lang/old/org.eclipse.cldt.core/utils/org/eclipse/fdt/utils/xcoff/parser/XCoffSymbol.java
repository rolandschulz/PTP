/*
 * Created on Jul 6, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.fdt.utils.xcoff.parser;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.IAddress;
import org.eclipse.fdt.utils.Addr2line;
import org.eclipse.fdt.utils.BinaryObjectAdapter;
import org.eclipse.fdt.utils.Symbol;


/**
 * @author DInglis
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class XCoffSymbol extends Symbol {

	/**
	 * @param binary
	 * @param name
	 * @param type
	 * @param addr
	 * @param size
	 * @param sourceFile
	 * @param startLine
	 * @param endLine
	 */
	public XCoffSymbol(BinaryObjectAdapter binary, String name, int type, IAddress addr, long size, IPath sourceFile, int startLine,
			int endLine) {
		super(binary, name, type, addr, size, sourceFile, startLine, endLine);
	}

	/**
	 * @param binary
	 * @param name
	 * @param type
	 * @param addr
	 * @param size
	 */
	public XCoffSymbol(BinaryObjectAdapter binary, String name, int type, IAddress addr, long size) {
		super(binary, name, type, addr, size);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.utils.Symbol#getLineNumber(long)
	 */
	public int getLineNumber(long offset) {
		int line = -1;
		Addr2line addr2line = ((XCOFFBinaryObject)binary).getAddr2line(true);
		if (addr2line != null) {
			try {
				return addr2line.getLineNumber(getAddress().add(offset));
			} catch (IOException e) {
				// ignore
			}
		}
		return line;
	}
	
}
