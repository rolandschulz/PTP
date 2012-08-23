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

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Handles adapting a text field to provide content proposals
 * @author batthish
 */
public class HeaderFooterContentProposalAdapter extends ContentProposalAdapter {


	/**
	 * Retrieves the trigger for the content assistant
	 * @return returns the Ctrl+Space KeyStroke object, or null if it cannot be determined.
	 */
	private static KeyStroke getTrigger() {
		try {
			return KeyStroke.getInstance("Ctrl+Space");
		} catch (ParseException e) {
			CUIPlugin.log("Unexpected error configuring content assistant",e);
			return null;
		}
	}

	/**
	 * The constructor
	 * @param text	the text field to add content assist 
	 */
	public HeaderFooterContentProposalAdapter(Text text) {
		super(text, new TextContentAdapter(), new HeaderFooterProposalProvider(), getTrigger(), "<".toCharArray());
		setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		setAutoActivationDelay(0);
		
		// add a "light bulb" to indicate that content assist is available
		ControlDecoration dec = new ControlDecoration(text, SWT.LEFT | SWT.TOP);
		FieldDecoration contentassistDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		dec.setImage(contentassistDecoration.getImage());
		dec.setDescriptionText(contentassistDecoration.getDescription());
		dec.setShowOnlyOnFocus(true);
		
		// ensure that there is enough room for the "light bulb"
		Composite parent = text.getParent();
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.horizontalSpacing = Math.max(layout.horizontalSpacing, dec.getImage().getBounds().width);
		layout.marginLeft = Math.max(layout.marginWidth, dec.getImage().getBounds().width) - layout.marginWidth;
	}

}

