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
package org.eclipse.fdt.utils.coff.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.IAddressFactory;
import org.eclipse.fdt.core.IBinaryParser;
import org.eclipse.fdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.fdt.core.IBinaryParser.ISymbol;
import org.eclipse.fdt.utils.AR;
import org.eclipse.fdt.utils.Addr32;
import org.eclipse.fdt.utils.Addr32Factory;
import org.eclipse.fdt.utils.BinaryObjectAdapter;
import org.eclipse.fdt.utils.Symbol;
import org.eclipse.fdt.utils.coff.Coff;
import org.eclipse.fdt.utils.coff.PE;

/**
 */
public class PEBinaryObject extends BinaryObjectAdapter {

	BinaryObjectInfo info;
	IAddressFactory addressFactory;
	ISymbol[] symbols;
	AR.ARHeader header;

	public PEBinaryObject(IBinaryParser parser, IPath path, AR.ARHeader header) {
		super(parser, path, IBinaryFile.OBJECT);
	}
	
	public PEBinaryObject(IBinaryParser parser, IPath p, int type) {
		super(parser, p, type);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.utils.BinaryObjectAdapter#getName()
	 */
	public String getName() {
		if (header != null) {
			return header.getObjectName();
		}
		return super.getName();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() throws IOException {
		if (getPath() != null && header != null) {
			return new ByteArrayInputStream(header.getObjectData());
		}
		return super.getContents();
	}
	
	/**
	 * @see org.eclipse.fdt.core.model.IBinaryParser.IBinaryObject#getSymbols()
	 */
	public ISymbol[] getSymbols() {
		if (hasChanged() || symbols == null) {
			try {
				loadAll();
			} catch (IOException e) {
				symbols = NO_SYMBOLS;
			}
		}
		return symbols;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.utils.BinaryObjectAdapter#getBinaryObjectInfo()
	 */
	protected BinaryObjectInfo getBinaryObjectInfo() {
		if (hasChanged() || info == null) {
			try {
				loadInfo();
			} catch (IOException e) {
				info = new BinaryObjectInfo();
			}
		}
		return info;
	}

	protected PE getPE() throws IOException {
		if (header != null) {
			return new PE(getPath().toOSString(), header.getObjectDataOffset());
		}
		return new PE(getPath().toOSString());
	}

	protected void loadAll() throws IOException {
		PE pe = null;
		try {
			pe = getPE();
			loadInfo(pe);
			loadSymbols(pe);
		} finally {
			if (pe != null) {
				pe.dispose();
			}
		}
	}

	protected void loadInfo() throws IOException {
		PE pe = null;
		try {
			pe = getPE();
			loadInfo(pe);
		} finally {
			if (pe != null) {
				pe.dispose();
			}
		}
	}

	protected void loadInfo(PE pe) throws IOException {
		info = new BinaryObjectInfo();
		PE.Attribute attribute = getPE().getAttribute();
		info.isLittleEndian = attribute.isLittleEndian();
		info.hasDebug = attribute.hasDebug();
		info.cpu = attribute.getCPU();
	}

	protected void loadSymbols(PE pe) throws IOException {
		ArrayList list = new ArrayList();
		loadSymbols(pe, list);
		symbols = (ISymbol[]) list.toArray(NO_SYMBOLS);
		Arrays.sort(symbols);
		list.clear();
	}
	protected void loadSymbols(PE pe, List list) throws IOException {
		Coff.Symbol[] peSyms = pe.getSymbols();
		byte[] table = pe.getStringTable();
		addSymbols(peSyms, table, list);
	}

	protected void addSymbols(Coff.Symbol[] peSyms, byte[] table, List list) {
		for (int i = 0; i < peSyms.length; i++) {
			if (peSyms[i].isFunction() || peSyms[i].isPointer() || peSyms[i].isArray()) {
				String name = peSyms[i].getName(table);
				if (name == null || name.trim().length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
					continue;
				}
				int type = peSyms[i].isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE;
				list.add(new Symbol(this, name, type, new Addr32(peSyms[i].n_value), 1));
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.utils.BinaryObjectAdapter#getAddressFactory()
	 */
	public IAddressFactory getAddressFactory() {
		if (addressFactory == null) {
			addressFactory = new Addr32Factory();
		}
		return addressFactory;
	}
}