/**********************************************************************
 * Copyright (c) 2005,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.editorHelp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.ptp.pldt.common.messages.Messages;

/**
 * C Help Book implementation for hover help, etc. etc.
 * 
 * @author Beth Tibbitts
 * 
 */
public class CHelpBookImpl implements ICHelpBook {
	private String title = Messages.generic_c_help_book;
	private static final boolean traceOn = false;

	private String pluginId;

	protected Map<String, IFunctionSummary> funcName2FuncInfo = new HashMap<String, IFunctionSummary>();

	/**
	 * Disallow default ctor; must provide plugin id
	 */
	@SuppressWarnings("unused")
	private CHelpBookImpl() {
	}

	public CHelpBookImpl(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getTitle() {
		return title;
	}

	protected void setTitle(String title) {
		this.title = title;
	}

	public int getCHelpType() {
		return ICHelpBook.HELP_TYPE_C;
	}

	public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context,
			String name) {
		IFunctionSummary fs = funcName2FuncInfo.get(name);
		if (traceOn) {

			String cn = this.getClass().getSimpleName();
			String finfo = (fs != null) ? (fs.toString().substring(0, 25)) : null;
			if (finfo != null)
				System.out.println("CHelpBookImpl " + cn + " getFunctionInfo for " + name + "= " + finfo); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return fs;
	}

	public IFunctionSummary[] getMatchingFunctions(
			ICHelpInvocationContext context, String prefix) {
		List<IFunctionSummary> functionSummaryList = new ArrayList<IFunctionSummary>();
		for (Iterator<String> it = funcName2FuncInfo.keySet().iterator(); it.hasNext();) {
			String funcName = it.next();
			if (funcName != null
					&& funcName.toUpperCase().startsWith(prefix.toUpperCase())) {
				functionSummaryList.add(funcName2FuncInfo.get(funcName));
			}
		}

		IFunctionSummary[] functionSummaryArray = null;

		// populate array
		if (!functionSummaryList.isEmpty()) {
			functionSummaryArray = new IFunctionSummary[functionSummaryList
					.size()];
			int i = 0;
			for (Iterator<IFunctionSummary> it = functionSummaryList.iterator(); it.hasNext(); i++) {
				functionSummaryArray[i] = it.next();
			}
		}

		return functionSummaryArray;
	}

	public ICHelpResourceDescriptor[] getHelpResources(
			ICHelpInvocationContext context, String name) {
		IFunctionSummary functionSummary = getFunctionInfo(context, name);
		if (functionSummary == null)
			return null;

		ICHelpResourceDescriptor resourceDescriptor[] = new ICHelpResourceDescriptor[1];
		CHelpResourceDescriptorImpl hrd = new CHelpResourceDescriptorImpl(this,
				functionSummary, this.pluginId);
		resourceDescriptor[0] = hrd; // html file path is in here
		return resourceDescriptor;
	}

	/**
	 * Convenience function for filling in table info
	 * 
	 * @since 5.0
	 */
	public void func(String fname, String desc, String retType, String args) {
		funcName2FuncInfo.put(fname, new FunctionSummaryImpl(fname, "", desc,
				new FunctionPrototypeSummaryImpl(fname, retType, args), null));
	}

}
