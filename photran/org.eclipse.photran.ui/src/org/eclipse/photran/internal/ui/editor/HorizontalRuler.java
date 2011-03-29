/*******************************************************************************
 * Copyright (c) 2011 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Ruler displaying column numbers at the top of the Fortran editor.
 * 
 * @author Jeff Overbey
 * 
 * @see FortranSourceViewer#createControl(Composite, int)
 */
@SuppressWarnings("deprecation")
public final class HorizontalRuler extends Composite implements PaintListener, CaretListener, SelectionListener, ControlListener, IPropertyChangeListener
{
    private IVerticalRuler verticalRuler = null;
    private StyledText styledText = null;
    
    public HorizontalRuler(Composite parent, int style)
    {
        super(parent, style);
        
        GridLayout layout = new GridLayout(1, false);
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginWidth = layout.marginHeight = 0;
        setLayout(layout);
        
        addPaintListener(this);

        FortranCorePlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
    }

    public void configure(IVerticalRuler verticalRuler, StyledText styledText)
    {
        this.verticalRuler = verticalRuler;
        this.styledText = styledText;
        styledText.addCaretListener(this);
        styledText.getHorizontalBar().addSelectionListener(this);
        verticalRuler.getControl().addControlListener(this);
    }

    public void paintControl(PaintEvent e)
    {
        if (verticalRuler != null && styledText != null && !styledText.isDisposed())
            new RulerPainter().paint(e.gc);
    }

    private final class RulerPainter
    {
        private int height = getSize().y;
        private int controlWidth = getSize().x;
        private int left = verticalRuler.getWidth() - styledText.getHorizontalPixel();
        private int verticalRulerWidth = verticalRuler.getWidth();
        private int scrollBarWidth = styledText.getVerticalBar().getSize().x;

        private int characterWidth;
        private int numCharsScrolled;
        private int rulerWidth;

        public void paint(GC gc)
        {
            gc.setFont(styledText.getFont());

            characterWidth = gc.getFontMetrics().getAverageCharWidth();
            numCharsScrolled = styledText.getHorizontalIndex();
            rulerWidth = controlWidth - scrollBarWidth - left + numCharsScrolled*characterWidth;

            drawBackground(gc);
            if (isFixedWidthFont(gc))
            {
                drawTicks(gc);
                drawTabs(gc);
                drawNumbers(gc);
            }
            drawCursorPosition(gc);
        }

        private void drawBackground(GC gc)
        {
            gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));
            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));

            //gc.fillRectangle(0, 0, controlWidth, height);
            gc.fillGradientRectangle(0, 0, controlWidth, height*2, true);
        }

        private boolean isFixedWidthFont(GC gc)
        {
            return gc.getCharWidth('i') == gc.getCharWidth('w');
        }

        private void drawTicks(GC gc)
        {
            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY));

            for (int i = 0; i <= rulerWidth/characterWidth; i++)
            {
                int x = left + i*characterWidth - characterWidth/2 + 1;
                
                int tickHeight;
                if (i % 10 == 0)
                    tickHeight = 0;
                else if (i % 5 == 0)
                    tickHeight = 3;
                else
                    tickHeight = 1;
                
                if (x >= verticalRulerWidth && tickHeight > 0)
                {
                    int center = height / 2;
                    int y1 = center - tickHeight + 1;
                    int y2 = center + tickHeight - 1;
                    gc.drawLine(x, y1, x, y2);
                }
            }
        }

        private void drawTabs(GC gc)
        {
            gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));
            
            int tabSize = FortranPreferences.TAB_WIDTH.getValue();

            for (int i = tabSize+1; i <= rulerWidth/characterWidth; i += tabSize)
            {
                int x = left + i*characterWidth - characterWidth/2 + 1;
                int y = height-1;
                if (x >= verticalRulerWidth)
                {
                    gc.fillPolygon(new int[]
                        {
                            x, y-3,
                            x-3, y,
                            x+3, y
                        });
                }
            }
        }

        private void drawNumbers(GC gc)
        {
            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY));

            Font origFont = gc.getFont();
            gc.setFont(gc.getDevice().getSystemFont());
            for (int i = 10; i <= rulerWidth/characterWidth; i += 10)
            {
                String string = Integer.toString(i/10);
                string = string.substring(string.length()-1);
                Point extent = gc.textExtent(string);
                int x = left + i*characterWidth - (extent.x / 2) - 1;
                int y = (height - extent.y) / 2;
                if (x >= verticalRulerWidth)
                    gc.drawText(string, x, y, true);
            }
            gc.setFont(origFont);
        }

        private void drawCursorPosition(GC gc)
        {
            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));

            Point caretLocation = styledText.getCaret().getLocation();
            int rectWidth = isFixedWidthFont(gc)
                ? gc.getFontMetrics().getAverageCharWidth()
                : styledText.getCaret().getSize().x;
            gc.drawRectangle(
                verticalRulerWidth+caretLocation.x,
                0,
                rectWidth,
                height-1);
        }
    }

    public void caretMoved(CaretEvent event)
    {
        redraw();
    }

    public void widgetSelected(SelectionEvent e)
    {
        redraw();
    }

    public void widgetDefaultSelected(SelectionEvent e)
    {
        redraw();
    }

    public void controlMoved(ControlEvent e)
    {
        redraw();
    }

    public void controlResized(ControlEvent e)
    {
        redraw();
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        redraw();
    }
    
    @Override
    public void dispose()
    {
        FortranCorePlugin.getDefault().getPluginPreferences().removePropertyChangeListener(this);
        super.dispose();
    }
}
