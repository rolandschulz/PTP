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
import org.eclipse.ptp.debug.core.aif.IAIFTypeStruct;

public class AIFTypeStruct extends AIFTypeAggregate implements IAIFTypeStruct {	
	//only base type: {structName|floatnumber=f4,intnumber=is4,shortnumber=is2,longnumber=is4,doubleumber=f8;;;}
	//complex type: {structName|floatnumber=f4,intnumber=is4,tStr22={testStruct|aFloat=f4,intNum=is4;;;},shortnumber=is2;;;}
	public AIFTypeStruct(String format) {
		super(format);
	}
	public String toString() {
		return AIFFactory.FDS_STRUCT + super.toString() + AIFFactory.FDS_STRUCT_END;
	}
}
