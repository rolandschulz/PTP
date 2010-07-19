/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Abhishek Sharma, UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.ui.browser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * The Edges tab in the VPG Browser.
 * 
 * @author Abhishek Sharma
 */
class EdgesTab
{
    private TabItem edges;
    private Composite composite;
    private StyledText styledText;
    private Button showAll;
    private Button showSelected;
    private Button edgeButton ;
    private String filename;
    private List<VPGEdge<?,?,?>> edgesInFile;
    private Set<VPGEdge<?,?,?>> showEdges;
    private EclipseVPG vpg;
    private Set<Integer> edgeTypes;
    private Set<Integer> edgeTypesToShow;
    private List<Color> color ;
    private Label label;

    public EdgesTab(TabItem edges, TabFolder tabFolder, EclipseVPG vpg)
    {
        this.vpg = vpg ;
        this.filename = null;
        this.edgesInFile = Collections.emptyList();
        this.showEdges = Collections.emptySet();
        this.edges = edges;
        this.edgeTypes = Collections.emptySet();
        this.edgeTypesToShow = new HashSet<Integer>();
        this.color = Collections.emptyList();
       
        color = new ArrayList<Color>();
        addColors();
        createControls(tabFolder);   
    }

    /** Adds three new colors red,green and blue to the color list
     * 
     */
    private void addColors()
    {
        //Adds three new colors red,green and blue to the color list
        color.add(new Color(null, new RGB(192,0,0)));
        color.add(new Color(null, new RGB(0,192,0)));
        color.add(new Color(null, new RGB(0,0,192)));
    }

    private void createControls(TabFolder tabfolder)
    {
        createComposite(tabfolder);
        createRadioButtons();
        createEdgesMenu();
        createStyledText();
        createLabel();   
    }

   
    private void createLabel()
    {
        label= new Label(composite, SWT.NONE);
        label.setText(""); //$NON-NLS-1$
    }

    private void createEdgesMenu()
    {
        edgeButton = new Button(composite, SWT.FLAT);
        edgeButton.setText(Messages.EdgesTab_EdgeTypes);
        edgeButton.addSelectionListener(new ShowEdgeTypesMenu());
    }
    
    private class ShowEdgeTypesMenu implements SelectionListener
    {
        public void widgetSelected(SelectionEvent e)
        {
            Menu popupMenu = new Menu(edgeButton);
            
            //This loop creates the Menu items and adds them to the menu.The Menu items
            //are the types of edges present in the file
            for (Integer type : edgeTypes)
            {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.CHECK);
                menuItem.setText(vpg.describeEdgeType(type));
                menuItem.addSelectionListener(new MenuItemSelectionListener(type));
                menuItem.setSelection(edgeTypesToShow.contains(type));
            }
            
            new MenuItem(popupMenu, SWT.SEPARATOR);
            
            MenuItem menuItem = new MenuItem(popupMenu, SWT.CHECK);
            menuItem.setText(Messages.EdgesTab_ShowAllEdges);
            menuItem.addSelectionListener(new SelectAllSelectionListener());
            popupMenu.setVisible(true);

            while (!popupMenu.isDisposed() && popupMenu.isVisible())
                if (!popupMenu.getDisplay().readAndDispatch())
                    popupMenu.getDisplay().sleep();
            popupMenu.dispose();
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
            widgetSelected(e);
        }
    }

    private class MenuItemSelectionListener implements SelectionListener
    {
        private int edgeType;
        
        public MenuItemSelectionListener(int type)
        {
            this.edgeType = type;     
        }

        public void widgetSelected(SelectionEvent e)
        {
          //If the menu item has already been selected and clicked on once again
          //de-select it
            if (edgeTypesToShow.contains(edgeType)){
                edgeTypesToShow.remove(edgeType);
                
            }
            else
                edgeTypesToShow.add(edgeType);
            
            styledText.redraw();
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
            widgetSelected(e);
        }
    }

    private class SelectAllSelectionListener implements SelectionListener{
        
        public SelectAllSelectionListener(){
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetSelected(SelectionEvent e)
        {
            edgeTypesToShow.addAll(edgeTypes);   
            showEdges = collectSelectedEdges(styledText.getCaretOffset());
            styledText.redraw();
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetDefaultSelected(SelectionEvent e)
        {
            widgetSelected(e);
            
        }
    }

    private Set<VPGEdge<?,?,?>> collectSelectedEdges(int caretOffset)
    {
       //if the care offset happens to be within the edge then add the edge to the edgesToShow list
        HashSet<VPGEdge<?,?,?>> edgesToShow = new HashSet<VPGEdge<?,?,?>>();
        for (VPGEdge<?,?,?> edge : edgesInFile)
        {
            if (edge.getSource().getOffset() <= caretOffset
                && edge.getSource().getEndOffset() >= caretOffset)
            {
                edgesToShow.add(edge);
            }
            else if (edge.getSink().getOffset() <= caretOffset
                && edge.getSink().getEndOffset() >= caretOffset)
            {
                edgesToShow.add(edge);
            }
        }
        return edgesToShow;
    }

    private void createComposite(TabFolder tabfolder)
    {
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite = new Composite(tabfolder, SWT.NONE);
        composite.setLayout(layout);
        edges.setControl(composite);
    }

    private void createRadioButtons()
    {
        createShowAllButton();
        createShowSelectedButton();
    }

    private void createShowAllButton()
    {
        showAll = new Button(composite, SWT.RADIO);
        showAll.setText(Messages.EdgesTab_ShowAllEdges);
        showAll.setSelection(false);
        showAll.addSelectionListener(new RadioButtonSelectionListener());
    }

    private void createShowSelectedButton()
    {
        showSelected = new Button(composite, SWT.RADIO);
        showSelected.setText(Messages.EdgesTab_ShowSelectedEdges);
        showSelected.setSelection(true);
        showSelected.addSelectionListener(new RadioButtonSelectionListener());
    }

    private final class RadioButtonSelectionListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent e)
        {
            styledText.redraw();
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
            widgetSelected(e);
        }
    }

    private void createStyledText()
    {
        styledText = new StyledText(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        gridData.verticalSpan = 1;
        styledText.setLayoutData(gridData);

        styledText.setFont(JFaceResources.getTextFont());
        styledText.addPaintListener(new EdgePainter(styledText));
        styledText.addCaretListener(new ShowEdgeCaretListener());  
    }

    private final class ShowEdgeCaretListener implements CaretListener
    {
        
        public void caretMoved(CaretEvent event)
        {
            showEdges = collectSelectedEdges(event.caretOffset);
            displayCaretInformation(event);
            styledText.redraw();
        }

        
        private void displayCaretInformation(CaretEvent event)
        {
            int caretLine =  styledText.getLineAtOffset(styledText.getCaretOffset());
            int lineOffset = styledText.getOffsetAtLine(caretLine);
            int caretOffset = styledText.getCaretOffset();
            int caretColumn = caretOffset-lineOffset+1 ; 
           
            label.setText(
                Messages.bind(
                    Messages.EdgesTab_LineColOffset, new Object[] {
                        caretLine+1,
                        caretColumn,
                        styledText.getCaretOffset() }));
            label.pack();
        }
    }

    private final class EdgePainter implements PaintListener
    {
  
        private final StyledText styledText;       
        
        private EdgePainter(StyledText styledText)
        {
            this.styledText = styledText;     
        }
       

        public void paintControl(PaintEvent e)
        {
           //Depending  upon whether if edgesTypesToShow list contains an edge this loop draws a rectangle around
            //the edge and calls the constructor of the DrawEdgesAndArrows class to draw lines between the source and sink
            for (VPGEdge< ? , ? , ? > edge : edgesInFile)
            {
                if (edgeTypesToShow.contains(edge.getType()))
                {
                    Color defaultColor = e.gc.getForeground();
                   //depending upon the type of edge the color of the edges and lines is decided.
                    e.gc.setForeground(setColor(edge.getType()));
                    TokenRef source = edge.getSource();
                    TokenRef sink = edge.getSink();
                    
                    
                    if (source.getFilename().equals(filename) && sink.getFilename().equals(filename))
                    {
                        Rectangle srcRect = drawRectangle(e, source.getOffset(), source.getEndOffset());
                        Rectangle sinkRect = drawRectangle(e, sink.getOffset(), sink.getEndOffset());
    
                        if (srcRect != null && sinkRect != null)
                        {
                            if(showAll.getSelection()||showEdges.contains(edge)){
                                displayEdgesAndArrows(e.gc,sinkRect,srcRect);
                                
                            }
                            
                        }
                    }
                    else
                    {
                        // TODO: Handle this case later
                    }
                    e.gc.setForeground(defaultColor);
                }
            }
        }
  
        private void displayEdgesAndArrows(GC gc, Rectangle sinkRect, Rectangle srcRect)
        {
            new EdgeArrow(sinkRect,srcRect).drawOn(gc);
        }
        
        private Color setColor(int type)
        {
            return color.get(type%color.size());    
        }


        private Rectangle drawRectangle(PaintEvent e, int startOffset, int endOffset)
        {
            if (isValid(startOffset) && isValid(endOffset))
            {
                Rectangle srcRect = styledText.getTextBounds(startOffset, Math.max(0, endOffset-1));
                e.gc.drawRectangle(srcRect);
                return srcRect;
            }
            else
                return null;
        }

        private boolean isValid(int offset)
        {
            return offset >= 0 && offset < styledText.getCharCount();
        }
    }       

    @SuppressWarnings("unchecked")
    public void showEdges(String file_selected, EclipseVPG vpg)
    {
        this.filename = file_selected;
        Object ast = vpg.acquireTransientAST(filename);
        
        if (ast == null)
            styledText.setText(Messages.bind(Messages.EdgesTab_UnableToParse, filename));
        else
            styledText.setText(vpg.getSourceCodeFromAST(ast));
 
        edgesInFile = new ArrayList<VPGEdge< ? , ? , ? >>();
        
        for (VPGEdge< ? , ? , ? > edge : (Iterable<VPGEdge< ? , ? , ? >>)vpg.db
            .getAllEdgesFor(filename))
            edgesInFile.add(edge);
        
        edgeTypes = new HashSet<Integer>();
        
        for (VPGEdge<?,?,?> edge : (Iterable<VPGEdge<?,?,? >>)vpg.db.getAllEdgesFor(filename))
            edgeTypes.add(edge.getType());
        
        edgeButton.setEnabled(edgeTypes.size() != 0);
    }
}