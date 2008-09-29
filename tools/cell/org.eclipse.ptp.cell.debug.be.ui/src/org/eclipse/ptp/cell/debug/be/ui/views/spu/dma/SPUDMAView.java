/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.ui.views.spu.dma;

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUDMAElement;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUDMAListTuple;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;


/**
 * Displays SPU DMA.
 *
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class SPUDMAView extends AbstractDebugEventHandlerView 
						 implements ISelectionListener, 
						 			INullSelectionListener, 
									IPropertyChangeListener, 
									IDebugExceptionHandler {
	
	private TabFolder fSashform;

	public class SPUDMAViewLabelProvider extends LabelProvider implements ITableLabelProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage( Object element, int columnIndex ) {
			if ( columnIndex == 0 )
				return getModelPresentation().getImage( element );
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText( Object element, int columnIndex ) {
			if ( element instanceof MISPUDMAElement ) {
				
				switch( columnIndex ) {
					case 0:
						return ((MISPUDMAElement)element).getName();
					case 1:
						return ((MISPUDMAElement)element).getValue();
					
				}
				
			} else if ( element instanceof MISPUDMAListTuple ) {
				switch( columnIndex ) {
				case 0:
					return ((MISPUDMAListTuple)element).getOpcode();
				case 1:
					return ((MISPUDMAListTuple)element).getTag();
				case 2:
					return ((MISPUDMAListTuple)element).getTid();
				case 3:
					return ((MISPUDMAListTuple)element).getRid();
				case 4:
					return ((MISPUDMAListTuple)element).getEa();
				case 5:
					return ((MISPUDMAListTuple)element).getLsa();
				case 6:
					return ((MISPUDMAListTuple)element).getSize();
				case 7:
					return ((MISPUDMAListTuple)element).getLstaddr();
				case 8:
					return ((MISPUDMAListTuple)element).getLstsize();
				case 9:
					return ((MISPUDMAListTuple)element).getError_p();
				
				}
			}
			return null;
		}
		
		private IDebugModelPresentation getModelPresentation() {
			return CDebugUIPlugin.getDebugModelPresentation();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer( Composite parent ) {
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		
		// add tree viewer
	
		SPUDMAViewer vv = createMainViewer(parent);
		SPUDMATupleViewer vv2 = createDetailsViewer();
	
		vv.setContentProvider( createContentProvider(vv2) );
		vv.setLabelProvider( new SPUDMAViewLabelProvider() );
		vv.setUseHashlookup( true );
		
		vv2.setContentProvider(new SPUDMATupleViewerContentProvider());
		vv2.setLabelProvider( new SPUDMAViewLabelProvider() );
		
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );

		// listen to selection in debug view
		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		setEventHandler( new SPUDMAViewEventHandler( this ) );

		return vv;
	}
	
	protected SPUDMAViewer createMainViewer(Composite parent) {
		fSashform = new TabFolder( parent, SWT.NONE );
		TabItem item = new TabItem(fSashform, SWT.NONE);
		Composite composite = new Composite(fSashform, SWT.NONE);
	    composite.setLayout(new FillLayout());
		SPUDMAViewer vv = new SPUDMAViewer( composite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CENTER | SWT.BORDER);
		item.setControl(composite);
		item.setText(SPUDMAMessages.getString("SPUDMAView.0")); //$NON-NLS-1$
		
		return vv;
	}
	
	protected SPUDMATupleViewer createDetailsViewer() {
		TabItem item = new TabItem(fSashform, SWT.NONE);
		Composite composite = new Composite(fSashform, SWT.NONE);
	    composite.setLayout(new FillLayout());
		SPUDMATupleViewer vv2 = new SPUDMATupleViewer( composite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CENTER | SWT.BORDER);
		Control control = vv2.getControl();
		GridData gd = new GridData( GridData.FILL_BOTH );
		control.setLayoutData( gd );
		item.setControl(composite);
		item.setText(SPUDMAMessages.getString("SPUDMAView.1")); //$NON-NLS-1$
		
		return vv2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		//return ICDebugHelpContextIds.SIGNALS_VIEW;
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu( IMenuManager menu ) {
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
		updateObjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar( IToolBarManager tbm ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
		if ( !isAvailable() || !isVisible() )
			return;
		if ( selection == null )
			setViewerInput( new StructuredSelection() );
		else if ( selection instanceof IStructuredSelection )
			setViewerInput( (IStructuredSelection)selection );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler#handleException(org.eclipse.debug.core.DebugException)
	 */
	public void handleException( DebugException e ) {
		showMessage( e.getMessage() );
	}

	/**
	 * Creates this view's content provider.
	 * 
	 * @return a content provider
	 */
	private IContentProvider createContentProvider(Viewer attached) {
		SPUDMAViewContentProvider cp = new SPUDMAViewContentProvider(attached);
		cp.setExceptionHandler( this );
		return cp;
	}

	protected void setViewerInput( IStructuredSelection ssel ) {
		ICDebugTarget target = null;
		if ( ssel != null && ssel.size() == 1 ) {
			Object input = ssel.getFirstElement();
			if ( input instanceof IDebugElement && ((IDebugElement)input).getDebugTarget() instanceof ICDebugTarget )
				target = (ICDebugTarget)((IDebugElement)input).getDebugTarget();
		}

		if ( getViewer() == null )
			return;

		Object current = getViewer().getInput();
		if ( current != null && current.equals( target ) ) {
			updateObjects();
			return;
		}
		
		showViewer();
		getViewer().setInput( target );
		updateObjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesHidden()
	 */
	protected void becomesHidden() {
		setViewerInput( new StructuredSelection() );
		super.becomesHidden();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
		IViewPart part = getSite().getPage().findView( IDebugUIConstants.ID_DEBUG_VIEW );
		if ( part != null ) {
			ISelection selection = getSite().getPage().getSelection( IDebugUIConstants.ID_DEBUG_VIEW );
			selectionChanged( part, selection );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getDefaultControl()
	 */
	protected Control getDefaultControl() {
		return fSashform;
	}
}
