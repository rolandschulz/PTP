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
package org.eclipse.ptp.internal.ui.hover;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Clement chu
 * 
 */
public class IconViewerInformation implements DisposeListener {
	private static final int BORDER= 1;
	private Shell fShell = null;
	private TextViewer fViewer = null;
	private StyledText fText = null;
	private Point fSizeConstraints;
	
	public IconViewerInformation(Shell parent, int style) {
		this(parent, SWT.NO_TRIM, style);
	}	
	public IconViewerInformation(Shell parent, int shellStyle, int style) {
		GridLayout layout;
		GridData gd;

		fShell= new Shell(parent, SWT.NO_FOCUS | SWT.ON_TOP);
		Display display= fShell.getDisplay();		
		fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

		Composite composite= fShell;
		layout= new GridLayout(1, false);
		int border= ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
		layout.marginHeight= border;
		layout.marginWidth= border;
		composite.setLayout(layout);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gd);

		fViewer = new TextViewer(composite, style);
		fViewer.setEditable(false);
		
		fText= fViewer.getTextWidget();
		gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
		fText.setLayoutData(gd);
		fText.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		fText.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			
		fText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e)  {
				if (e.character == 0x1B) // ESC
					fShell.setVisible(false);
			}
			public void keyReleased(KeyEvent e) {}
		});
		addDisposeListener(this);
	}
	public void addDisposeListener(DisposeListener listener) {
		fShell.addDisposeListener(listener);
	}
	public void removeDisposeListener(DisposeListener listener) {
		fShell.removeDisposeListener(listener);
	}
	public Point computeSizeHint() {
		return fShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}
	public void setLocation(Point location) {
		Rectangle trim= fShell.computeTrim(0, 0, 0, 0);
		Point textLocation= fText.getLocation();				
		location.x += trim.x - textLocation.x;		
		location.y += trim.y - textLocation.y;		
		fShell.setLocation(location);		
	}
	public void setSize(int width, int height) {
		fShell.setSize(width, height);
	}
	public final void dispose() {
		if (fShell != null && !fShell.isDisposed())
			fShell.dispose();
		else
			widgetDisposed(null);
	}
	public void widgetDisposed(DisposeEvent event) {
		fShell= null;
		fText= null;
	}
	public void setVisible(boolean visible) {
		fShell.setVisible(visible);
	}
	public void setInformation(String content) {
		if (content == null) {
			fViewer.setInput(null);
			return;
		}
		IDocument doc= new Document(content);
		//TODO
		//CUIPlugin.getDefault().getTextTools().setupCDocument(doc);
		fViewer.setInput(doc);
	}
	public void setInput(Object input) {
		if (input instanceof String)
			setInformation((String)input);
		else
			setInformation(null);
	}
	public void setFocus() {
		fShell.forceFocus();
		fText.setFocus();
	}
	public boolean hasContents() {
		return fText.getCharCount() > 0;
	}
	protected ITextViewer getViewer()  {
		return fViewer;
	}
	public Point getSize() {
		fShell.pack();
		return fShell.getSize();
	}
	public Point computeSizeConstraints() {
		if (fSizeConstraints == null) {
			GC gc= new GC(getViewer().getTextWidget());
			gc.setFont(getViewer().getTextWidget().getFont());
			int width= gc.getFontMetrics().getAverageCharWidth();
			int height = gc.getFontMetrics().getHeight();
			gc.dispose();
			fSizeConstraints= new Point (60 * width, 10 * height);
		}
		return fSizeConstraints;
	}
}
