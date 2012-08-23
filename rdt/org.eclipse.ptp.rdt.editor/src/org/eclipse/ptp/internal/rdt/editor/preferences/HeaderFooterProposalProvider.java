/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/


package org.eclipse.ptp.internal.rdt.editor.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.swt.custom.StyledTextPrintOptions;

/**
 * Provides the proposals for the provider
 * @author batthish
 *
 */ 
public class HeaderFooterProposalProvider implements IContentProposalProvider {
	/**
	 * A class used for sorting the proposals
	 * @author batthish
	 */
	class HeaderFooterProposalPair implements Comparable<HeaderFooterProposalPair> {
		String _var;
		String _description;
		public HeaderFooterProposalPair(String var, String description) {
			_var = var;
			_description = description;
		}
		@Override
		public int compareTo(HeaderFooterProposalPair o) {
			return _var.compareTo(o._var);
		}
	}

	/**
	 * Used to add the date to the header or footer
	 */
	public  static final String DATE = "<date>";

	/**
	 * Used to add the time to the header or footer
	 */
	public static final String TIME = "<time>";

	/**
	 * Used to add the file name to the header or footer
	 */
	public static final String FILE = "<file>";
	
	private List<HeaderFooterProposalPair> _list;
	
	/**
	 * Retrieves the list of proposals
	 * @param text		the currently text in the field
	 * @param position	the position of the cursor
	 * @return
	 */
	@Override
	public IContentProposal[] getProposals(String text, int position) {
		init();
		List<IContentProposal> result = new ArrayList<IContentProposal>();
		String prefix = text.substring(0, position).trim();
		for (HeaderFooterProposalPair pair : _list)
			if (pair._var.startsWith(prefix))
				result.add(new ContentProposal(pair._var, pair._var, pair._description, position + pair._var.length() - prefix.length()));
		return result.toArray(new IContentProposal[result.size()]);
	}

	/**
	 * Initializes the proposals
	 */
	private void init() {
		if (_list==null) {
			_list = new ArrayList<HeaderFooterProposalPair>();
			_list.add(new HeaderFooterProposalPair(StyledTextPrintOptions.PAGE_TAG, PreferenceMessages.PageNumber));
			_list.add(new HeaderFooterProposalPair(DATE, PreferenceMessages.CurrentDate));
			_list.add(new HeaderFooterProposalPair(TIME, PreferenceMessages.CurrentTime));
			_list.add(new HeaderFooterProposalPair(FILE, PreferenceMessages.FileName));
			Collections.sort(_list);
		}
		
	}


};
