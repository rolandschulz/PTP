package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

/**
 * @author nchen
 * @author cheah
 *
 * Base class for horizontal rulers. Draws thin ruler on the top of the editor
 * once associated with the editor at hand.
 *
 * Based on the work of the zen.xhtml.editor that was released in the Eclipse newsgroups
 * Zenil - org.zen.xhtml.editor
 * 
 */
public abstract class AbstractHorizontalRuler extends Composite {
	
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

	protected static Color fWhiteColor = new Color(null, WHITE);
	
	protected static Color fBlackColor = new Color(null, BLACK);

	protected ISelectionChangedListener fSelectionChangedListener;

	protected PaintListener fPaintListener;

	protected FocusAdapter fFocusAdapter;

	protected VerifyListener fVerifyListener;

	protected VerifyKeyListener fVerifyKeyListener;

	protected SelectionAdapter fSelectionAdapter;

	protected MouseAdapter fMouseAdapter;

	protected IVerticalRuler verticalRuler;
	
	public AbstractHorizontalRuler(Composite parent, int style) {
		super(parent, style);
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
	protected abstract void paintComponent(GC g);
	
	/**
	 * Return the ruler string to be drawn.
	 *
	 * @param maxCol the maximum colum number
	 * @param start  the  start position ,which is the start position of the document
	 */
	protected abstract String ruler(int maxCol, int start);
	
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
