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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.core.vpg.IVPGNode;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * The Annotations tab in the VPG Browser.
 * 
 * @author Abhishek Sharma
 */
class AnnotationsTab
{
    private EclipseVPG vpg;
    private TabItem annotations;
    private SashForm annotationsSash;
    private StyledText styledText;
    private TabFolder annotationsTabFolder;
    private Composite composite ;
    private Label label;
    private String filename;
    private List<Pair<? extends IVPGNode, Integer>> annotationsInFile;
    private Set<Pair<? extends IVPGNode, Integer>> annotationsToShow;
    private HashMap<Integer, TabItem> hashMap;   //to decide which tab to display the annotation in depending upon the type
    
    public AnnotationsTab(TabItem annotations, TabFolder tabFolder, EclipseVPG vpg)
    {
        this.annotations = annotations;
        this.annotationsInFile = Collections.emptyList();
        this.annotationsToShow = Collections.emptySet();
        this.vpg = vpg;
        this.hashMap = new HashMap<Integer, TabItem>();
        createControls(tabFolder);
    }

    
    private void createControls(TabFolder tabFolder)
    {
        createHorizontalSash(tabFolder);
        createStyledText();
        createComposite();
        createAnnotationsTabFolder();
        createLabel();
    }

    private void createComposite()
    {
        composite = new Composite(annotationsSash,SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);   
    }

    private void createAnnotationsTabFolder()
    {
        annotationsTabFolder = new TabFolder(composite, SWT.NULL | SWT.FILL);
        annotationsTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true,true));
    }

    private void createLabel()
    {
        label = new Label(composite,SWT.NONE);
        label.setText(""); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,false,false));  
    }
    
    private void createNewTab(int annotationType, String description)
    {
        if (!hashMap.containsKey(annotationType))
        {
            TabItem tabItem = new TabItem(annotationsTabFolder, SWT.NULL);
            tabItem.setText(description);
            hashMap.put(annotationType, tabItem);
        }
    }

    private void createHorizontalSash(TabFolder tabFolder)
    {
        annotationsSash = new SashForm(tabFolder, SWT.VERTICAL);
        annotationsSash.setLayout(new FillLayout());
        annotationsSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        annotations.setControl(annotationsSash);
    }

    private void createStyledText()
    {
        styledText = new StyledText(annotationsSash, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
                                    | SWT.READ_ONLY);

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        gridData.verticalSpan = 1;
        styledText.setLayoutData(gridData);

        styledText.setFont(JFaceResources.getTextFont());
        styledText.addPaintListener(new RectanglePainter(styledText));
        styledText.addCaretListener(new ShowEdgeCaretListener());

    }

    @SuppressWarnings("unchecked")
    public void showAnnotations(String filename)
    {
        this.filename = filename;
        Object ast = vpg.acquireTransientAST(filename);
        
        if (ast == null)
            styledText.setText(Messages.bind(Messages.AnnotationsTab_UnableToParse, filename));
        else
            styledText.setText(vpg.getSourceCodeFromAST(ast));

        annotationsInFile = new ArrayList<Pair<? extends IVPGNode, Integer>>();
       
        for (Pair<? extends IVPGNode, Integer> pair :
                (Iterable<Pair<? extends IVPGNode, Integer>>)vpg.getAllAnnotationsFor(filename))
        {
            annotationsInFile.add(pair);
        }

        for (Pair<? extends IVPGNode, Integer> pair : annotationsInFile)
        {
            createNewTab(pair.snd, vpg.describeAnnotationType(pair.snd));
        }
    }

    private final class ShowEdgeCaretListener implements CaretListener
    {
        private Set<Pair<? extends IVPGNode, Integer>> collectSelectedAnnotations(int caretOffset)
        {
            // if the care offset happens to be within the edge 
            //then add the edge to the edgesToShow list
            HashSet<Pair<? extends IVPGNode, Integer>> AnnotationsToShow = new HashSet<Pair<? extends IVPGNode, Integer>>();
          
            for (Pair<? extends IVPGNode, Integer> pair : annotationsInFile)
            {
                IVPGNode tokenRef = pair.fst;

                if (tokenRef.getOffset() <= caretOffset && tokenRef.getEndOffset() >= caretOffset)
                    AnnotationsToShow.add(pair);

            }
            return AnnotationsToShow;
        }

        public void caretMoved(CaretEvent event)
        {
            annotationsToShow = collectSelectedAnnotations(event.caretOffset);
            displayAnnotations();
            displayCaretInformation(event);
            styledText.redraw();
        }

        private void displayAnnotations()
        {
            Text blankText = new Text(annotationsTabFolder, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI
                                        | SWT.READ_ONLY);
            blankText.setText(Messages.AnnotationsTab_NoAnnotationsToShow);
          
            for (int type : hashMap.keySet())
            {
                hashMap.get(type).setControl(blankText);
            }
            
            for (Pair<? extends IVPGNode, Integer> pair : annotationsToShow)
            {
                IVPGNode tokenRef = pair.fst;
                int annotationType = pair.snd;
                Text textField = new Text(annotationsTabFolder, SWT.V_SCROLL | SWT.H_SCROLL
                                          | SWT.MULTI | SWT.READ_ONLY);
                textField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                String text = tokenRef.getAnnotation(annotationType).toString();
                textField.setText(text);
                hashMap.get(annotationType).setControl(textField);
            }

        }
        private void displayCaretInformation(CaretEvent event)
        { 
            int caretLine = styledText.getLineAtOffset(styledText.getCaretOffset());
            int lineOffset = styledText.getOffsetAtLine(caretLine);
            int caretOffset = styledText.getCaretOffset();
            int caretColumn = caretOffset-lineOffset+1 ; 
           
            label.setText(
                Messages.bind(Messages.AnnotationsTab_LineColOffset, new Object[] {
                    caretLine+1,
                    caretColumn,
                    styledText.getCaretOffset() }));
            label.pack();
        }
    }

    private final class RectanglePainter implements PaintListener
    {
        @SuppressWarnings("unused")
        private final StyledText styledText;

        private RectanglePainter(StyledText styledText)
        {
            this.styledText = styledText;
        }

        public void paintControl(PaintEvent e)
        {
            for (Pair<? extends IVPGNode, Integer> pair : annotationsInFile)
            {
                IVPGNode tokenRef = pair.fst;
                if (tokenRef.getFilename().equals(filename))
                    drawRectangle(e, tokenRef.getOffset(), tokenRef.getEndOffset());
            }
        }

    }

    private Rectangle drawRectangle(PaintEvent e, int startOffset, int endOffset)
    {
        if (isValid(startOffset) && isValid(endOffset))
        {
            Rectangle srcRect = styledText.getTextBounds(startOffset, Math.max(startOffset, Math.max(0, endOffset - 1)));
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
