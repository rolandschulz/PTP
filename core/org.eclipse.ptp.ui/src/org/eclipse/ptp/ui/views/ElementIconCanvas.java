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
package org.eclipse.ptp.ui.views;

import java.util.BitSet;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * @author Clement chu
 * 
 */
public class ElementIconCanvas extends IconCanvas implements ISelectionProvider {
	private IElementSet cur_element_set = null;
	private final ListenerList listeners = new ListenerList();
	private ISelection selection = null;

	/**
	 * Constructor
	 * 
	 * @param view
	 * @param parent
	 * @param style
	 *            view style
	 */
	public ElementIconCanvas(AbstractParallelElementView view, Composite parent, int style) {
		super(parent, style);
		IPreferenceStore store = PTPUIPlugin.getDefault().getPreferenceStore();
		setIconSpace(store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_X), store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_Y));
		setIconSize(store.getInt(IPTPUIConstants.VIEW_ICON_WIDTH), store.getInt(IPTPUIConstants.VIEW_ICON_HEIGHT));
		setTooltip(store.getBoolean(IPTPUIConstants.VIEW_TOOLTIP_SHOWALLTIME), store.getLong(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT),
				store.getBoolean(IPTPUIConstants.VIEW_TOOLTIP_ISWRAP));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		listeners.clear();
		selection = null;
		cur_element_set = null;
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.IconCanvas#doSelectionAll()
	 */
	@Override
	protected void doSelectionAll() {
		super.doSelectionAll();
		setSelection(new StructuredSelection(getSelectedElements()));
	}

	/**
	 * Get current set
	 * 
	 * @return current set
	 */
	public IElementSet getCurrentElementSet() {
		return cur_element_set;
	}

	/**
	 * Get selected elements
	 * 
	 * @return selected elements
	 * @since 7.0
	 */
	@Override
	public BitSet getSelectedElements() {
		if (cur_element_set == null) {
			return new BitSet();
		}
		return selectedElements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		if (selection == null) {
			return StructuredSelection.EMPTY;
		}
		return selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.views.IconCanvas#handleMouseUp(org.eclipse.swt.widgets
	 * .Event)
	 */
	@Override
	protected void handleMouseUp(Event event) {
		super.handleMouseUp(event);
		setSelection(new StructuredSelection(getSelectedElements()));
	}

	/**
	 * Get element
	 * 
	 * @param index
	 *            Element index
	 * @return element
	 * @since 7.0
	 */
	public boolean hasElement(int index) {
		if (cur_element_set == null) {
			return false;
		}
		boolean element = cur_element_set.contains(index);
		if (element) {
			cur_element_set.setSelected(index, selectedElements.get(index));
		}
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @param sendEvent
	 */
	public void setCurrentSelection(boolean sendEvent) {
		setCurrentSelection(sendEvent, getSelectedElements());
	}

	/**
	 * @since 7.0
	 */
	protected void setCurrentSelection(boolean sendEvent, BitSet elements) {
		selection = new StructuredSelection(elements);
		if (sendEvent) {
			setSelection(selection);
		}
	}

	/**
	 * Change set
	 * 
	 * @param e_set
	 */
	public void setElementSet(IElementSet e_set) {
		this.cur_element_set = e_set;
		this.selection = null;
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				setTotal(cur_element_set == null ? 0 : cur_element_set.size());
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse
	 * .jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection selection) {
		this.selection = selection;
		final SelectionChangedEvent e = new SelectionChangedEvent(ElementIconCanvas.this, selection);
		Object[] array = listeners.getListeners();
		for (Object element : array) {
			final ISelectionChangedListener l = (ISelectionChangedListener) element;
			SafeRunnable.run(new SafeRunnable() {
				@Override
				public void run() {
					l.selectionChanged(e);
				}
			});
		}
	}

	/**
	 * @param obj
	 * @param content
	 * @since 7.0
	 */
	public void updateToolTipText(final String content) {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (fInformationControl != null) {
					fInformationControl.setInformation(content);
					fInformationControl.getShellSize();
				}
			}
		});
	}
}
