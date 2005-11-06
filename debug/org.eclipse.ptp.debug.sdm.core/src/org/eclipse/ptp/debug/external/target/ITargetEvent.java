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
package org.eclipse.ptp.debug.external.target;

import org.eclipse.ptp.core.util.BitList;

/**
 * @author Clement chu
 *
 */
public interface ITargetEvent {
	public static final int STACKFRAME_TYPE = 0;
	public static final int AIFVALUE_TYPE = 1;
	public static final int EXPRESSVALUE_TYPE = 2;
	public static final int VARIABLETYPE_TYPE = 3;
	public static final int ARGUMENTS_TYPE = 4;
	public static final int LOCALVARIABLES_TYPE = 5;
	public static final int GLOBALVARIABLES_TYPE = 6;

	public int getType();
	public BitList getTargets();
	public boolean contain(int task);
	public boolean contain(BitList tasks);
	
	public Object getResult();
	public void setResult(Object result);	
}
