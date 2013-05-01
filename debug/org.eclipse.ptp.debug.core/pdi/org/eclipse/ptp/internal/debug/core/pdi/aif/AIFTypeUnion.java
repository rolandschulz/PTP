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
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeUnion;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

public class AIFTypeUnion extends AIFType implements IAIFTypeUnion {
	private class AIFField {
		public String name;
		public IAIFType type;
	}

	private final List<AIFField> fFields = new ArrayList<AIFField>();
	private String fName = ""; //$NON-NLS-1$
	private int fSize = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeUnion#getFieldNames()
	 */
	public String[] getFieldNames() {
		String[] names = new String[fFields.size()];
		for (int i = 0; i < fFields.size(); i++) {
			names[i] = fFields.get(i).name;
		}
		return names;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeUnion#getFieldTypes()
	 */
	public IAIFType[] getFieldTypes() {
		IAIFType[] types = new IAIFType[fFields.size()];
		for (int i = 0; i < fFields.size(); i++) {
			types[i] = fFields.get(i).type;
		}
		return types;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAggregate#getName()
	 */
	public String getName() {
		return fName;
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
	 * @see org.eclipse.ptp.internal.debug.core.pdi.aif.TypeAggregate#toString()
	 */
	@Override
	public String toString() {
		String content = String.valueOf(AIFFactory.FDS_UNION) + getName() + AIFFactory.FDS_TYPENAME_END;
		for (int i = 0; i < fFields.size(); i++) {
			if (i > 0) {
				content += AIFFactory.FDS_UNION_FIELD_SEP;
			}
			AIFField field = fFields.get(i);
			content += field.name + AIFFactory.FDS_UNION_FIELD_NAME_END + field.type.toString();
		}
		return content + AIFFactory.FDS_UNION_END;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.aif.AIFType#parse(java.lang.String
	 * )
	 */
	@Override
	public String parse(String fmt) throws AIFFormatException {
		fmt = parseName(fmt);
		while (fmt.length() > 0 && fmt.charAt(0) != AIFFactory.FDS_UNION_END) {
			fmt = parseField(fmt);
			if (fmt.charAt(0) == AIFFactory.FDS_UNION_FIELD_SEP) {
				fmt = fmt.substring(1);
			}
		}
		if (fmt.charAt(0) != AIFFactory.FDS_UNION_END) {
			throw new AIFFormatException(Messages.AIFTypeUnion_0);
		}
		return fmt.substring(1);
	}

	private String parseField(String fmt) throws AIFFormatException {
		AIFField field = new AIFField();

		int pos = fmt.indexOf(AIFFactory.FDS_UNION_FIELD_NAME_END);
		if (pos == -1) {
			throw new AIFFormatException(Messages.AIFTypeUnion_1);
		}
		field.name = fmt.substring(0, pos);
		fmt = fmt.substring(pos + 1);
		fmt = AIFFactory.parseType(fmt);

		IAIFType aifType = AIFFactory.getType();
		fSize += aifType.sizeof();
		field.type = aifType;
		fFields.add(field);

		return fmt;
	}

	private String parseName(String fmt) throws AIFFormatException {
		int pos = fmt.indexOf(AIFFactory.FDS_TYPENAME_END);
		if (pos == -1) {
			throw new AIFFormatException(Messages.AIFTypeUnion_2);
		}
		fName = fmt.substring(0, pos);
		return fmt.substring(pos + 1);
	}
}
