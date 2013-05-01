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
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeEnum;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

public class AIFTypeEnum extends TypeIntegral implements IAIFTypeEnum {
	private final List<String> fNames = new ArrayList<String>();
	private final List<Integer> fValues = new ArrayList<Integer>();
	private String fName = ""; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeEnum#getNames()
	 */
	public String[] getNames() {
		return fNames.toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeEnum#getName()
	 */
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeEnum#getValues()
	 */
	public Integer[] getValues() {
		return fValues.toArray(new Integer[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.aif.TypeIntegral#toString()
	 */
	@Override
	public String toString() {
		String content = String.valueOf(AIFFactory.FDS_ENUM);
		content += getName() + AIFFactory.FDS_TYPENAME_END;
		for (int i = 0; i < fNames.size(); i++) {
			content += fNames.get(i) + AIFFactory.FDS_ENUM_SEP + fValues.get(i);
			if (i < fNames.size() - 1) {
				content += AIFFactory.FDS_ENUM_CONST_SEP;
			}
		}
		return content + AIFFactory.FDS_ENUM_END + String.valueOf(AIFFactory.FDS_INT) + super.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.aif.TypeIntegral#parse(java.lang
	 * .String)
	 */
	@Override
	public String parse(String fmt) throws AIFFormatException {
		fmt = parseName(fmt);
		while (fmt.length() > 0 && fmt.charAt(0) != AIFFactory.FDS_ENUM_END) {
			fmt = parseField(fmt);
			if (fmt.charAt(0) == AIFFactory.FDS_ENUM_CONST_SEP) {
				fmt = fmt.substring(1);
			}
		}
		if (fmt.charAt(0) != AIFFactory.FDS_ENUM_END) {
			throw new AIFFormatException(Messages.AIFTypeEnum_0);
		}
		fmt = fmt.substring(2); // skip enum end and "i"
		return super.parse(fmt);
	}

	private String parseField(String fmt) throws AIFFormatException {
		int pos = fmt.indexOf(AIFFactory.FDS_ENUM_SEP);
		if (pos == -1) {
			throw new AIFFormatException(Messages.AIFTypeEnum_1);
		}
		fNames.add(fmt.substring(0, pos));
		fmt = fmt.substring(pos + 1);
		pos = AIFFactory.getFirstNonDigitPos(fmt, 0, true);
		if (pos == -1) {
			throw new AIFFormatException(Messages.AIFTypeEnum_2);
		}
		fValues.add(Integer.parseInt(fmt.substring(0, pos)));
		return fmt.substring(pos);
	}

	protected String parseName(String fmt) throws AIFFormatException {
		int pos = fmt.indexOf(AIFFactory.FDS_TYPENAME_END);
		if (pos == -1) {
			throw new AIFFormatException(Messages.AIFTypeEnum_3);
		}
		fName = fmt.substring(0, pos);
		return fmt.substring(pos + 1);
	}
}
