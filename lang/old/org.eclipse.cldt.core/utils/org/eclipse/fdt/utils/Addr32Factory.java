/*******************************************************************************
 * Copyright (c) 2004 Intel Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Intel Corporation - Initial API and implementation
 ******************************************************************************/
package org.eclipse.fdt.utils;

import java.math.BigInteger;

import org.eclipse.fdt.core.IAddress;
import org.eclipse.fdt.core.IAddressFactory;

public class Addr32Factory implements IAddressFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.IAddressFactory#getZero()
	 */
	public IAddress getZero() {
		return Addr32.ZERO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.IAddressFactory#getMax()
	 */
	public IAddress getMax() {
		return Addr32.MAX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.IAddressFactory#createAddress(java.lang.String)
	 */
	public IAddress createAddress(String addr) {
		IAddress address = new Addr32(addr);
		return address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.IAddressFactory#createAddress(java.lang.String,
	 *      int)
	 */
	public IAddress createAddress(String addr, int radix) {
		IAddress address = new Addr32(addr, radix);
		return address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.IAddressFactory#createAddress(java.math.BigInteger)
	 */
	public IAddress createAddress(BigInteger addr) {
		IAddress address = new Addr32(addr.longValue());
		return address;
	}
}