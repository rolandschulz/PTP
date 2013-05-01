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
package org.eclipse.ptp.internal.debug.ui.views;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.PageBook;

/**
 * @author Clement chu
 *
 */
public abstract class PTabItem implements IRunnableContext {
	protected CTabItem tabItem = null;
	protected PageBook fPageBook = null;
	protected TextViewer fTextViewer = null;
	protected boolean displayError = false;
	protected PTabFolder view = null;
	
	public PTabItem(PTabFolder view) {
		this(view, ""); //$NON-NLS-1$
	}
	public PTabItem(PTabFolder view, String tabName) {
		this.view = view;
		tabItem = new CTabItem(view.getTabFolder(), SWT.CLOSE);
		tabItem.setText(tabName);
		tabItem.setToolTipText(tabName);
		tabItem.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				dispose();
			}
		});
	}
	public CTabItem getTabItem() {
		return tabItem;
	}
	public void setControl() {
	    tabItem.setControl(createControl(tabItem.getParent()));		
	}
	
	public void setTabName(String tabName) {
		tabItem.setText(tabName);
	}
	protected abstract void dispose();

	public Control createControl(Composite parent) {	
		fPageBook = new PageBook(parent, SWT.NONE);
		createErrorPage(fPageBook);
		createTabPage(fPageBook);
		return fPageBook;
	}
	public void createErrorPage(Composite parent) {
		if (fTextViewer == null) {
			fTextViewer = new TextViewer(parent, SWT.WRAP);	
			fTextViewer.setDocument(new Document());
			StyledText styleText = fTextViewer.getTextWidget();
			styleText.setEditable(false);
			styleText.setEnabled(false);
		}
	}
	public void displayError(Exception e) {
		displayError = true;
		StyledText styleText = fTextViewer.getTextWidget();
		if (styleText != null)
			styleText.setText(Messages.PTabItem_0 + e.getMessage());

		fPageBook.showPage(fTextViewer.getControl());
		clearContext();
	}
	protected abstract void createTabPage(Composite parent);
	protected abstract void clearContext();
	protected abstract void displayTab();
	
	public Point getTextSize(Composite composite, String text) {
		return new GC(composite).textExtent(text);
	}
	
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		new ProgressMonitorDialog(fPageBook.getShell()).run(fork, cancelable, runnable);
	}	
}
