/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

/**
 * GDB/MI Frame tuple parsing.
 */

MIFrame *
MIFrameNew(void)
{
}

void
MIFrameFree(MIFrame *f)
{
}

MIString *
MIFrameToString(MIFrame *f)
{
		StringBuffer buffer = new StringBuffer();
		buffer.append("level=\"" + level + "\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append(",addr=\"" + addr + "\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append(",func=\"" + func + "\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append(",file=\"" + file + "\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append(",line=\"").append(line).append('"'); //$NON-NLS-1$
		buffer.append(",args=["); //$NON-NLS-1$
		for (int i = 0; i < args.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append("{name=\"" + args[i].getName() + "\"");//$NON-NLS-1$//$NON-NLS-2$
			buffer.append(",value=\"" + args[i].getValue() + "\"}");//$NON-NLS-1$//$NON-NLS-2$
		}
		buffer.append(']');
		return buffer.toString();
}

MIFrame *
MIFrameParse(MIValue *tuple)
{
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$
			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getCString();
			}

			if (var.equals("level")) { //$NON-NLS-1$
				try {
					level = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("addr")) { //$NON-NLS-1$
				try {
					addr = str.trim();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("func")) { //$NON-NLS-1$
				func = null;
				if ( str != null ) {
					str = str.trim();
					if ( str.equals( "??" ) ) //$NON-NLS-1$
						func = ""; //$NON-NLS-1$
					else
					{
						// In some situations gdb returns the function names that include parameter types.
						// To make the presentation consistent truncate the parameters. PR 46592
						int end = str.indexOf( '(' );
						if ( end != -1 )
							func = str.substring( 0, end );
						else
							func = str;
					}
				}
			} else if (var.equals("file")) { //$NON-NLS-1$
				file = str;
			} else if (var.equals("line")) { //$NON-NLS-1$
				try {
					line = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("args")) { //$NON-NLS-1$
				if (value instanceof MIList) {
					args = MIArg.getMIArgs((MIList)value);
				} else if (value instanceof MITuple) {
					args = MIArg.getMIArgs((MITuple)value);
				}
			}
		}
	}
}
