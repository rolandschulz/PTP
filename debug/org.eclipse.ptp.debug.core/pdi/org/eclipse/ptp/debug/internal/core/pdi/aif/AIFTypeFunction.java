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
package org.eclipse.ptp.debug.internal.core.pdi.aif;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFactory;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFormatException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFunction;

public class AIFTypeFunction extends TypeDerived implements IAIFTypeFunction {
	private String[] args = new String[0];

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.aif.TypeDerived#toString()
	 */
	@Override
	public String toString() {
		String content = String.valueOf(AIFFactory.FDS_FUNCTION);
		for (int i = 0; i < args.length; i++) {
			content += args[i];
			if (i < args.length - 1)
				content += AIFFactory.FDS_FUNCTION_ARG_SEP;
		}
		return content + AIFFactory.FDS_FUNCTION_END + super.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.internal.core.pdi.aif.TypeDerived#parse(java.lang
	 * .String)
	 */
	@Override
	public String parse(String fmt) throws AIFFormatException {
		int pos = fmt.indexOf(AIFFactory.FDS_FUNCTION_END);
		String argsStr = fmt.substring(0, pos);
		args = argsStr.split(String.valueOf(AIFFactory.FDS_FUNCTION_ARG_SEP));
		return super.parse(fmt.substring(pos));
	}
}
