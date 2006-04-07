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

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.ptp.ui.hover.IIconInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Clement chu
 * 
 */
public class DefaultInformationControl implements IIconInformationControl, DisposeListener {
	public static final int SHELL_DEFAULT_WIDTH = 400;
	public static final int SHELL_DEFAULT_HEIGHT = 200;
	
	public interface IInformationPresenter {
		String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight);
	}
	
	private static final int OUTER_BORDER= 1;
	private static final int INNER_BORDER= 1;
	private Shell fShell;
	private StyledText fText;
	private IInformationPresenter fPresenter;
	private TextPresentation fPresentation= new TextPresentation();
	private int fMaxWidth= -1;
	private int fMaxHeight= -1;
	private Font fHeaderTextFont;
	private Label headerField = null;
	private ScrolledComposite sc = null;
	private boolean showDetails = false;
	
	public DefaultInformationControl(Shell parent, boolean showDetails) {
		this(parent, showDetails, SWT.NONE | SWT.WRAP);
	}
	public DefaultInformationControl(Shell parent, boolean showDetails, int style) {
		this(parent, SWT.TOOL | SWT.NO_TRIM, showDetails, style, new IconHoverPresenter(false));
	}
	public DefaultInformationControl(Shell parent) {
		this(parent, false);
	}
	public DefaultInformationControl(Shell parent, int style, IInformationPresenter presenter) {
		this(parent, SWT.TOOL | SWT.NO_TRIM, true, style, presenter);
	}
	
	public DefaultInformationControl(Shell parent, int shellStyle, boolean showDetails, int style, IInformationPresenter presenter) {
		this.showDetails = showDetails;
		GridLayout layout;
		GridData gd;

		fShell= new Shell(parent, SWT.NO_FOCUS | SWT.ON_TOP | shellStyle);
		Display display= fShell.getDisplay();
		fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		
		layout= new GridLayout(1, false);
		int border= ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : OUTER_BORDER;
		layout.marginHeight= border;
		layout.marginWidth= border;
		fShell.setLayout(layout);
		fShell.setLayoutData(new GridData(GridData.FILL_BOTH));

		sc = new ScrolledComposite(fShell, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite composite = new Composite(sc, SWT.NONE);
		layout= new GridLayout(1, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.verticalSpacing= 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		composite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	    
		sc.setContent(composite);
		sc.setExpandVertical(true);
	    sc.setExpandHorizontal(true);
	    sc.setAlwaysShowScrollBars(true);

    	Composite hdComposite = new Composite(composite, SWT.NONE);
		layout= new GridLayout(2, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.horizontalSpacing= 0;
		layout.verticalSpacing= 1;
		hdComposite.setLayout(layout);
		hdComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		hdComposite.setForeground(composite.getForeground());
		hdComposite.setBackground(composite.getBackground());
	     
		headerField= new Label(hdComposite, SWT.LEFT);
		createHeadLabel(headerField);
		
		if (showDetails) {
			Label labelField = new Label(hdComposite, SWT.RIGHT);
			labelField.setText("Press ESC to close");
			createHeadLabel(labelField);
			
			// Horizontal separator line
			Label separator= new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
			separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			// Text field
			fText= new StyledText(composite, SWT.MULTI | SWT.READ_ONLY | style);
			if ((style & SWT.WRAP) != 0) {//is warp
				fMaxWidth = SHELL_DEFAULT_WIDTH;
			}
			
			fText.setCaret(null);
			gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
			gd.horizontalIndent= INNER_BORDER;
			gd.verticalIndent= INNER_BORDER;
			fText.setLayoutData(gd);
			fText.setForeground(composite.getForeground());
			fText.setBackground(composite.getBackground());
			fText.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if (e.character == 0x1B) {// ESC
						close();
					}
				}
				public void keyReleased(KeyEvent e) {}
			});
			fText.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {}
				public void focusLost(FocusEvent e) {
					close();
				}
			});
			fPresenter= presenter;
		}
		addDisposeListener(this);
	}
	private void close() {
		if (fShell != null && !fShell.isDisposed())
			fShell.dispose();		
	}
	private void createHeadLabel(Label labelField) {
		Font font= labelField.getFont();
		if (fHeaderTextFont == null) {
			FontData[] fontDatas= font.getFontData();
			for (int i= 0; i < fontDatas.length; i++) {
				fontDatas[i].setHeight((fontDatas[i].getHeight() * 9 / 10));
			}
			fHeaderTextFont = new Font(labelField.getDisplay(), fontDatas);
		}
		labelField.setFont(fHeaderTextFont);
		labelField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
		labelField.setForeground(labelField.getParent().getForeground());
		labelField.setBackground(labelField.getParent().getBackground());		
	}
	
	public void setHeader(String header) {
		if (headerField != null) {
			headerField.setText(header + " ");
		}
	}
	public void setInformation(String content) {
		if (fPresenter == null) {
			fText.setText(content);
		} else {
			fPresentation.clear();
			content= fPresenter.updatePresentation(fShell.getDisplay(), content, fPresentation, fMaxWidth, fMaxHeight);
			if (content != null) {
				fText.setText(content);
				TextPresentation.applyTextPresentation(fPresentation, fText);
			} else {
				fText.setText("");
			}
		}
	}
	public void setVisible(boolean visible) {
		fShell.setVisible(visible);
	}
	public void dispose() {
		if (fShell != null && !fShell.isDisposed())
			fShell.dispose();
		else
			widgetDisposed(null);
	}
	public void widgetDisposed(DisposeEvent event) {
		if (fHeaderTextFont != null && !fHeaderTextFont.isDisposed())
			fHeaderTextFont.dispose();

		fShell= null;
		fText= null;
		fHeaderTextFont= null;
	}
	public void setSize(int width, int height) {
		int new_width = showDetails?(width + 75):width;
		if (width > SHELL_DEFAULT_WIDTH || height > SHELL_DEFAULT_HEIGHT) {
			sc.setMinSize(width, height);
			sc.getHorizontalBar().setVisible(width > SHELL_DEFAULT_WIDTH);
			sc.getVerticalBar().setVisible(height > SHELL_DEFAULT_HEIGHT);
			new_width = (width>SHELL_DEFAULT_WIDTH?SHELL_DEFAULT_WIDTH:new_width);
			int new_height = (height>SHELL_DEFAULT_HEIGHT?SHELL_DEFAULT_HEIGHT:(width>SHELL_DEFAULT_WIDTH?(sc.getHorizontalBar().getSize().y+height):height));
			fShell.setSize(new_width+5, new_height+2);
		}
		else {
			sc.setAlwaysShowScrollBars(false);
			fShell.setSize(new_width+5, height+2);
		}
	}
	public void setLocation(Point location) {
		fShell.setLocation(location);
	}
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		fMaxWidth= maxWidth;
		fMaxHeight= maxHeight;
	}
	public Point computeSizeHint() {
		//return fShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		return sc.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}
	public void addDisposeListener(DisposeListener listener) {
		fShell.addDisposeListener(listener);
	}
	public void removeDisposeListener(DisposeListener listener) {
		fShell.removeDisposeListener(listener);
	}
	public void setForegroundColor(Color foreground) {
		fText.setForeground(foreground);
	}
	public void setBackgroundColor(Color background) {
		fText.setBackground(background);
	}
	public boolean isFocusControl() {
		return fText.isFocusControl();
	}
	public void setFocus() {
		if (showDetails) {
			fShell.forceFocus();
			if (fText != null) {
				fText.setFocus();
			}
		}
	}
	public void addFocusListener(FocusListener listener) {
		fText.addFocusListener(listener);
	}
	public void removeFocusListener(FocusListener listener) {
		fText.removeFocusListener(listener);
	}
	
	public Point getShellSize() {
		Point size = computeSizeHint();
		//set Shell size
		setSize(size.x, size.y);
		
		if (size.x > SHELL_DEFAULT_WIDTH) {
			size.x = SHELL_DEFAULT_WIDTH;
		}
		if (size.y > SHELL_DEFAULT_HEIGHT) {
			size.y = SHELL_DEFAULT_HEIGHT;
		}
		return size;
	}
	
	public boolean isWrap() {
		return (fMaxWidth>-1);
	}
}
