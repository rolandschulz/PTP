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
import org.eclipse.ptp.internal.ui.messages.Messages;
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
		/** Update presentation
		 * @param display
		 * @param hoverInfo
		 * @param presentation
		 * @param maxWidth
		 * @param maxHeight
		 * @return formatted text
		 */
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
	
	/** Constructor
	 * @param parent
	 * @param showDetails
	 */
	public DefaultInformationControl(Shell parent, boolean showDetails) {
		this(parent, showDetails, SWT.NONE | SWT.WRAP);
	}
	/** Constructor
	 * @param parent
	 * @param showDetails
	 * @param style
	 */
	public DefaultInformationControl(Shell parent, boolean showDetails, int style) {
		this(parent, SWT.TOOL | SWT.NO_TRIM, showDetails, style, new IconHoverPresenter(false), false);
	}
	/** Constructor
	 * @param parent
	 */
	public DefaultInformationControl(Shell parent) {
		this(parent, false);
	}
	/** Constructor
	 * @param parent
	 * @param style
	 * @param presenter
	 */
	public DefaultInformationControl(Shell parent, int style, IInformationPresenter presenter, boolean enableKey) {
		this(parent, SWT.TOOL | SWT.NO_TRIM, true, style, presenter, enableKey);
	}
	
	/** Constructor
	 * @param parent
	 * @param shellStyle
	 * @param showDetails
	 * @param style
	 * @param presenter
	 */
	public DefaultInformationControl(Shell parent, int shellStyle, boolean showDetails, int style, IInformationPresenter presenter, boolean enableKey) {
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
		layout.marginBottom = showDetails?10:0;
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
			if (enableKey) {
				Label labelField = new Label(hdComposite, SWT.RIGHT);
				labelField.setText(Messages.DefaultInformationControl_0);
				createHeadLabel(labelField);
			}
			
			// Horizontal separator line
			Label separator= new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
			separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			// Text field
			fText= new StyledText(composite, SWT.MULTI | SWT.READ_ONLY | style);
			if ((style & SWT.WRAP) != 0) {//is warp
				fMaxWidth = SHELL_DEFAULT_WIDTH / 2;
			}
			
			fText.setCaret(null);
			gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
			gd.horizontalIndent= INNER_BORDER;
			gd.verticalIndent= INNER_BORDER;
			fText.setLayoutData(gd);
			fText.setForeground(composite.getForeground());
			fText.setBackground(composite.getBackground());
			fText.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {}
				public void focusLost(FocusEvent e) {
					close();
				}
			});
			if (enableKey) {
				fText.addKeyListener(new KeyListener() {
					public void keyPressed(KeyEvent e) {
						if (e.character == 0x1B) {// ESC
							close();
						}
					}
					public void keyReleased(KeyEvent e) {}
				});
			}
			fPresenter= presenter;
		}
		addDisposeListener(this);
	}
	/** Close shell
	 * 
	 */
	private void close() {
		if (fShell != null && !fShell.isDisposed())
			fShell.dispose();		
	}
	/** Create head label field
	 * @param labelField
	 */
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.hover.IIconInformationControl#setHeader(java.lang.String)
	 */
	public void setHeader(String header) {
		if (headerField != null) {
			headerField.setText(header + " "); //$NON-NLS-1$
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setInformation(java.lang.String)
	 */
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
				fText.setText(""); //$NON-NLS-1$
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		fShell.setVisible(visible);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#dispose()
	 */
	public void dispose() {
		if (fShell != null && !fShell.isDisposed())
			fShell.dispose();
		else
			widgetDisposed(null);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent event) {
		if (fHeaderTextFont != null && !fHeaderTextFont.isDisposed())
			fHeaderTextFont.dispose();

		fShell= null;
		fText= null;
		fHeaderTextFont= null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setSize(int, int)
	 */
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
			int min_h = sc.getHorizontalBar().getSize().y;
			if (height <= min_h) {
				height = min_h + 1;
			}
			sc.setAlwaysShowScrollBars(false);
			fShell.setSize(new_width+5, height+2);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setLocation(org.eclipse.swt.graphics.Point)
	 */
	public void setLocation(Point location) {
		fShell.setLocation(location);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setSizeConstraints(int, int)
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		fMaxWidth= maxWidth;
		fMaxHeight= maxHeight;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		//return fShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		return sc.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#addDisposeListener(org.eclipse.swt.events.DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener) {
		fShell.addDisposeListener(listener);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#removeDisposeListener(org.eclipse.swt.events.DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener) {
		fShell.removeDisposeListener(listener);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setForegroundColor(org.eclipse.swt.graphics.Color)
	 */
	public void setForegroundColor(Color foreground) {
		fText.setForeground(foreground);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setBackgroundColor(org.eclipse.swt.graphics.Color)
	 */
	public void setBackgroundColor(Color background) {
		fText.setBackground(background);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#isFocusControl()
	 */
	public boolean isFocusControl() {
		return fText.isFocusControl();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setFocus()
	 */
	public void setFocus() {
		if (showDetails) {
			//fShell.forceFocus();
			if (fText != null) {
				fText.setFocus();
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#addFocusListener(org.eclipse.swt.events.FocusListener)
	 */
	public void addFocusListener(FocusListener listener) {
		fText.addFocusListener(listener);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#removeFocusListener(org.eclipse.swt.events.FocusListener)
	 */
	public void removeFocusListener(FocusListener listener) {
		fText.removeFocusListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.hover.IIconInformationControl#getShellSize()
	 */
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.hover.IIconInformationControl#isWrap()
	 */
	public boolean isWrap() {
		return (fMaxWidth>-1);
	}
}
