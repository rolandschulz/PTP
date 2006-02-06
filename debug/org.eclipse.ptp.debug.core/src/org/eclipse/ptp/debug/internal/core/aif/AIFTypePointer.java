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

package org.eclipse.ptp.debug.internal.core.aif;

import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFTypePointer;

public class AIFTypePointer extends TypeDerived implements IAIFTypePointer {
	//char*: ^c
	//^%0/{s|a=is4,b=^>0/;;;}
	public AIFTypePointer(IAIFType basetype) {
		super(basetype);
	}
	public String toString() {
		return AIFFactory.FDS_POINTER + super.toString();
	}
	
	public static void main(String[] args) {
		IAIFType testType = AIFFactory.getAIFType("^%0/{s|a=is4,b=^>0/;;;}");
		System.out.println("----: " + testType);
		System.out.println("----: " + testType.sizeof());
	}
}
