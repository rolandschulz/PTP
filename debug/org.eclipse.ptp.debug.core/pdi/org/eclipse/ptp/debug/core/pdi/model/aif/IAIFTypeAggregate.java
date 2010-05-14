/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.core.pdi.model.aif;

/**
 * Represents a class or struct type
 * 
 * @author clement
 * 
 */
public interface IAIFTypeAggregate extends IAIFType {
	public static final int AIF_CLASS_ACCESS_PUBLIC = 0x01;
	public static final int AIF_CLASS_ACCESS_PROTECTED = 0x02;
	public static final int AIF_CLASS_ACCESS_PRIVATE = 0x04;
	public static final int AIF_CLASS_ACCESS_PACKAGE = 0x08;
	public static final int AIF_CLASS_ACCESS_ALL = 0x0f;

	/**
	 * Returns name of this type
	 * 
	 * @return name of this type
	 */
	public String getName();

	/**
	 * Returns an array containing the field names of this type for the given
	 * access modifiers.
	 * 
	 * @param access
	 *            access modifiers
	 * @return array of field names
	 */
	public String[] getFieldNames(int access);

	/**
	 * Returns an array containing the types of each field for fields of the
	 * given access modifier
	 * 
	 * @param access
	 *            access modifiers
	 * @return array of this type
	 */
	public IAIFType[] getFieldTypes(int access);
}
