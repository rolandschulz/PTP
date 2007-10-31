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
 * Represents float type in this IAIFType
 * @author clement
 *
 */
public interface IAIFTypeFloat extends IAIFType {
	/**
	 * Determines whether this type is imaginary
	 * @return true if this type is imaginary
	 */
	boolean isImaginary();
	
	/**
	 * Determines whether this is complex type
	 * @return true if current type is complex
	 */
	boolean isComplex();
	
	/**
	 * Determines whether current type is long
	 * @return true if current type is long
	 */
	boolean isLong();
}
