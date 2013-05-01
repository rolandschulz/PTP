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
package org.eclipse.ptp.internal.debug.core.pdi.aif;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFormatException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFunction;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

public class AIFTypeFunction extends AIFType implements IAIFTypeFunction {
	private IAIFType[] fArgTypes;
	private IAIFType fReturnType;
	private int fSize = AIFFactory.SIZE_INVALID;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFunction#getArgumentTypes
	 * ()
	 */
	public IAIFType[] getArgumentTypes() {
		return fArgTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFunction#getReturnType()
	 */
	public IAIFType getReturnType() {
		return fReturnType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.aif.TypeDerived#parse(java.lang
	 * .String)
	 */
	@Override
	public String parse(String fmt) throws AIFFormatException {
		List<IAIFType> argTypes = new ArrayList<IAIFType>();
		int pos = fmt.indexOf(AIFFactory.FDS_FUNCTION_END);
		if (pos > 0) {
			String argsStr = fmt.substring(0, pos);
			String[] args = argsStr.split(String.valueOf(AIFFactory.FDS_FUNCTION_ARG_SEP));
			for (String arg : args) {
				AIFFactory.parseType(arg);
				argTypes.add(AIFFactory.getType());
			}
		}
		fArgTypes = argTypes.toArray(new IAIFType[0]);
		String res = AIFFactory.parseType(fmt.substring(pos + 1));
		fReturnType = AIFFactory.getType();
		return res;
	}

	public void setSizeof(int size) {
		fSize = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType#sizeof()
	 */
	public int sizeof() {
		return fSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.aif.TypeDerived#toString()
	 */
	@Override
	public String toString() {
		String content = String.valueOf(AIFFactory.FDS_FUNCTION);
		for (int i = 0; i < fArgTypes.length; i++) {
			content += fArgTypes[i].toString();
			if (i < fArgTypes.length - 1)
				content += AIFFactory.FDS_FUNCTION_ARG_SEP;
		}
		return content + AIFFactory.FDS_FUNCTION_END + getReturnType().toString();
	}
}
