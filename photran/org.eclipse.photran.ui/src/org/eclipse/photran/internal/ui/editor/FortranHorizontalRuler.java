/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Nicholas Chen
 * @author Cheah Chin Fei
 * @author Jeff Overbey combined into single class
 *
 * Base class for horizontal rulers. Draws thin ruler on the top of the editor
 * once associated with the editor at hand.
 *
 * Based on the work of the zen.xhtml.editor that was released in the Eclipse newsgroups
 * Zenil - org.zen.xhtml.editor
 * 
 */
public class FortranHorizontalRuler extends Composite {
	
	/**
	 * the Styled text of the editor
	 */
	protected StyledText fStyledText;

	/**
	 * the editor source viewer
	 */
	protected ISourceViewer fSourceViewer;

	/**
	 * Cached display text
	 */
	protected String fDisplayText;

	/**
	 * the format line's width
	 */
	protected int fWidth;

	/**
	 * empty string which will be used for free format
	 */
	protected static final String BLANK = "";

	/**
	 * plus sign displayed on the editor
	 */
	protected static final char PLUS = '+';

	/**
	 * minus sign displayed on the editor
	 */
	protected static final char MINUS = '-';

	/**
	 * The average character width of
	 * the current styled text font
	 */
	protected int avgCharWidth;

	/**
	 * the tab character
	 */
	protected static final char TAB = '\t';
	
	protected static RGB WHITE = new RGB(255, 255, 255);
	protected static RGB BLACK = new RGB(0, 0, 0);
	protected static RGB GRAY = new RGB(190, 190, 190);
    protected static RGB DARK_GRAY = new RGB(0x80, 0x80, 0x80);
    protected static RGB LIGHT_GRAY = new RGB(0xDD, 0xDD, 0xDD);
	
	protected static Color fWhiteColor = new Color(null, WHITE);
	protected static Color fBlackColor = new Color(null, BLACK);
	protected static Color fGrayColor = new Color(null, GRAY);
    protected static Color fDarkGrayColor = new Color(null, DARK_GRAY);
    protected static Color fLightGrayColor = new Color(null, LIGHT_GRAY);

	protected ISelectionChangedListener fSelectionChangedListener;

	protected PaintListener fPaintListener;

	protected FocusAdapter fFocusAdapter;

	protected VerifyListener fVerifyListener;

	protected VerifyKeyListener fVerifyKeyListener;

	protected SelectionAdapter fSelectionAdapter;

	protected MouseAdapter fMouseAdapter;

	protected IVerticalRuler fVerticalRuler;
    
    protected boolean fIsFixedForm;
	
	public FortranHorizontalRuler(IVerticalRuler verticalRuler, Composite parent, boolean isFixedForm) {
		super(parent, SWT.NO_BACKGROUND);
        this.fVerticalRuler = verticalRuler;
        this.fIsFixedForm = isFixedForm;
	}
	
	/**
	 * Sets the source viewer, aand adds the appropriate listeners to the
	 * styled text
	 * @param sourceViewer the source viewer to set
	 */
	public void setSourceViewer(ISourceViewer sourceViewer) {

		fSourceViewer = sourceViewer;
		fStyledText = fSourceViewer.getTextWidget();

		fSelectionChangedListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (fSourceViewer.getTextWidget().getSelectionCount() > 0) {
					redraw();
				}
			}
		};
		fSourceViewer.getSelectionProvider().addSelectionChangedListener(
				fSelectionChangedListener);

		fPaintListener = new PaintListener() {
			public void paintControl(PaintEvent e) {
				paintComponent(e.gc);
			}
		};
		addPaintListener(fPaintListener);

		fFocusAdapter = new FocusAdapter() {

			public void focusGained(FocusEvent e) {
				if (!fStyledText.isDisposed()) {
					redraw();
				}
			}
		};
		fStyledText.addFocusListener(fFocusAdapter);

		fVerifyListener = new VerifyListener() {

			public void verifyText(VerifyEvent e) {
				if (!fStyledText.isDisposed()) {
					redraw();
				}
			}
		};
		fStyledText.addVerifyListener(fVerifyListener);

		fVerifyKeyListener = new VerifyKeyListener() {

			public void verifyKey(VerifyEvent e) {
				if (!fStyledText.isDisposed()) {
					redraw();
				}
			}
		};
		fStyledText.addVerifyKeyListener(fVerifyKeyListener);

		fSelectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!fStyledText.isDisposed()) {
					redraw();
				}
			}
		};
		//Adds the selection listener to the scrollbar of the styled text
		fStyledText.getHorizontalBar().addSelectionListener(fSelectionAdapter);

		fMouseAdapter = new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				if (!fStyledText.isDisposed()) {
					redraw();
				}
			}

			public void mouseDown(MouseEvent e) {
				if (!fStyledText.isDisposed()) {
					redraw();
				}
			}
		};
		fStyledText.addMouseListener(fMouseAdapter);
	}
	
	/**
	 * Paints the ruler
	 * @param g the Graphics context
	 */

    /**
     * Paints the ruler
     * @param g the Graphics context
     */
    protected void paintComponent(GC g) {
        // we need the vertical ruler width so that we know where to start positioning the 
        // horizontal ruler. The width of the vertical ruler depends on the number of digits 
        // for the line numbers
        int verticalRulerWidth = fVerticalRuler.getWidth();
        Point size = getSize();
        
        if (fWidth != size.x) {
            fWidth = size.x;
        }

        avgCharWidth = g.getFontMetrics().getAverageCharWidth();

        //This gives the maximum number of characters that can
        //be accomadated on the screen.Based on this value we can 
        //draw the the horizontal ruler string 
        int maxChars = fIsFixedForm ? 80 : (fWidth) / avgCharWidth;

        //First draw from start till end in grey color
        int height = size.y;
        g.fillRectangle(0, 0, fWidth, height);

        g.setBackground(fWhiteColor);

        //Then draw from start till start of vertical scrollbar in white
        g.fillRectangle(verticalRulerWidth, 0,
                fWidth - fStyledText.getVerticalBar().getSize().x
                        - verticalRulerWidth, height);

        g.setClipping(verticalRulerWidth, 0,
                fWidth - fStyledText.getVerticalBar().getSize().x
                        - verticalRulerWidth, height);

        g.setForeground(fDarkGrayColor);

        fDisplayText = ruler(maxChars, fStyledText.getHorizontalPixel());

        int column = getCurrentColumn();

        /* as drawing is zero relative */
        column--;

        //Get the relative starting position
        //from where the calculation of cursor 
        //pointer position can start
        int scroll = fStyledText.getHorizontalPixel();

        //some few pixels remaining
        int rem = scroll % avgCharWidth;

        // gets the scroll in indexes
        scroll /= avgCharWidth;

        //this gives the relative column position 
        //where the cursor pointer can be drawn 
        column -= scroll;

        //calculate the pixel position at which the
        //cursor pointer can be drawn
        int x = column * g.getFontMetrics().getAverageCharWidth() - rem;

        g.setBackground(fDarkGrayColor);

        //start the drawing not from 0, but from -rem,as
        //to accomadate these pixels properly,and so that
        //horizontal ruler offset is in exact alignment with
        //the editor styled text offset 
        g.drawString(fDisplayText, 0 - rem + verticalRulerWidth, 0, true);

        //Draw the rectangular pointer
        g.setBackground(fLightGrayColor);
        g.fillRectangle(x + verticalRulerWidth, 0, g.getFontMetrics().getAverageCharWidth(), height);

        g.setForeground(fWhiteColor);

        char c = 0;

        int index = column;

        //Highlight the character on which cursor is present
        c = (index < 0 || index >= fDisplayText.length()) ? ' ' : fDisplayText.charAt(index);
        g.drawString(String.valueOf(c), x + verticalRulerWidth, 0, true);
    }
	
	/**
	 * Return the ruler string to be drawn.
	 *
	 * @param maxCol the maximum colum number
	 * @param start  the  start position ,which is the start position of the document
	 */
    protected String ruler(int maxCol, int start) {

        //start is in pixels,
        //Convert it to index.
        start /= avgCharWidth;

        //Now start gives the position from which
        //to start the calculation of the horizontal ruler string   

        StringBuffer horizontalString = new StringBuffer(BLANK);

        int begin = 1;

        for (int i = begin + start; i <= maxCol + start; i++) {

            if ((i % 10) == 0) {
                horizontalString.append((i / 10) % 10);
                continue;
            }

            if ((i % 5) == 0) {
                horizontalString.append(PLUS);
                continue;
            }

            horizontalString.append(MINUS);
        }
        
        if (fIsFixedForm)
        {
        /*
         * Create [    ] to indicate fortran labels in the ruler
         */
        horizontalString.setCharAt(0, '[');
        horizontalString.setCharAt(begin, ' ');
        horizontalString.setCharAt(begin+1, ' ');
        horizontalString.setCharAt(begin+2, ' ');
        horizontalString.setCharAt(begin+3, ' ');
        horizontalString.setCharAt(begin+4, ']');
        }
        
        return horizontalString.toString();
    }
	
	/**
	 * Returns the current column
	 * @return int the current column at which the cursor is placed
	 */
	protected int getCurrentColumn() {

		int column = 0;
		int line = -1;
		try {
			final int offset = fSourceViewer.getVisibleRegion().getOffset();
			final int caret = offset + fStyledText.getCaretOffset();
			final IDocument document = fSourceViewer.getDocument();
			line = document.getLineOfOffset(caret);
			final int lineOffset = document.getLineOffset(line);

			final int tabWidth = fStyledText.getTabs();

			int increment = 1;
			int adjust = 0;
			for (int i = lineOffset; i < caret; i++) {
				if (TAB == document.getChar(i)) {
					while ((column + increment + adjust) % tabWidth != 0)
						increment++;

					column += increment;
					increment = 1;
				} else {
					column++;
				}
			}
		} catch (BadLocationException x) {

		}
		//The column incremented to show where the cursor stands
		column++;

		return column;
	}
	
	/**
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		fSourceViewer.getSelectionProvider().removeSelectionChangedListener(
				fSelectionChangedListener);
		removePaintListener(fPaintListener);
		fStyledText.removeFocusListener(fFocusAdapter);
		fStyledText.removeVerifyKeyListener(fVerifyKeyListener);
		fStyledText.removeVerifyListener(fVerifyListener);
		fStyledText.getHorizontalBar().removeSelectionListener(
				fSelectionAdapter);
		fStyledText.removeMouseListener(fMouseAdapter);

		super.dispose();
	}
}
