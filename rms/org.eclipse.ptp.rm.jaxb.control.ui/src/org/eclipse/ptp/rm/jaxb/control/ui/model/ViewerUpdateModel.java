/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIPlugin;
import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.TemplateType;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

/**
 * Update model for the entire viewer (CheckboxTable or CheckboxTree). <br>
 * <br>
 * The viewer needs to be acted on as a whole on updates when check state
 * changes, and it needs to be refreshed after updates.
 * 
 * @author arossi
 * 
 */
public class ViewerUpdateModel extends AbstractUpdateModel implements ICheckStateListener, SelectionListener {
	private final StringBuffer checked;
	private final StringBuffer templatedValue;
	private final String pattern;
	private final String separator;
	private final ICheckable viewer;
	private final ColumnViewer columnViewer;
	private final boolean initialAllChecked;
	private Button showOnlySelected;

	/**
	 * Used to filter out unchecked items.
	 */
	private final ViewerFilter filter = new ViewerFilter() {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof ICellEditorUpdateModel) {
				return ((ICheckable) viewer).getChecked(element);
			} else if (element instanceof InfoTreeNodeModel) {
				Object parent = ((InfoTreeNodeModel) element).getParent();
				return ((ICheckable) viewer).getChecked(parent);
			}
			return false;
		}
	};

	/**
	 * @param name
	 *            An arbitrary name given to the viewer; the viewer string
	 *            output (produced from the pattern template) will be stored in
	 *            the environment as the value of this name, and thus can be
	 *            referenced by other widget models.
	 * @param initialAllChecked
	 *            default setting for all is checked; else all unchecked
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 * @param viewer
	 *            the checkable viewer the object models
	 * @param template
	 *            JAXB data element defining a pattern by which to process the
	 *            name-value pairs associated with the items of the viewer into
	 *            a single output string
	 */
	public ViewerUpdateModel(String name, boolean initialAllChecked, ValueUpdateHandler handler, ICheckable viewer,
			TemplateType template) {
		super(name, handler);
		this.viewer = viewer;
		this.columnViewer = (ColumnViewer) viewer;
		this.initialAllChecked = initialAllChecked;
		String s = null;
		if (template == null) {
			pattern = null;
		} else {
			pattern = template.getPattern();
			s = template.getSeparator();
		}
		separator = s == null ? JAXBControlUIConstants.ZEROSTR : s;
		checked = new StringBuffer();
		templatedValue = new StringBuffer();
		viewer.addCheckStateListener(this);
	}

	/*
	 * Model serves as CheckStateListener for the viewer. Multiple rows can be
	 * selected, in which case they will all receive the value of the clicked
	 * row. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse
	 * .jface.viewers.CheckStateChangedEvent)
	 */
	@SuppressWarnings("unchecked")
	public void checkStateChanged(CheckStateChangedEvent event) {
		try {
			Object element = event.getElement();
			if (!(element instanceof ICellEditorUpdateModel)) {
				viewer.setChecked(element, false);
				return;
			}
			boolean checked = viewer.getChecked(element);
			IStructuredSelection selection = (IStructuredSelection) ((ColumnViewer) viewer).getSelection();
			List<Object> selected = new ArrayList<Object>();
			if (selection.toList().contains(element)) {
				selected.addAll(selection.toList());
			} else {
				selected.add(element);
			}
			if (!selected.isEmpty()) {
				for (Object o : selected) {
					if (o instanceof ICellEditorUpdateModel) {
						ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
						model.setChecked(checked);
						viewer.setChecked(model, checked);
					} else {
						viewer.setChecked(o, false);
					}
				}
			}
		} catch (Throwable t) {
			JAXBControlUIPlugin.log(t);
		}
		handleUpdate(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.AbstractUpdateModel#getControl()
	 */
	@Override
	public Object getControl() {
		return viewer;
	}

	/**
	 * @return the actual SWT control.
	 */
	public Control getSWTControl() {
		if (columnViewer instanceof CheckboxTableViewer) {
			return ((CheckboxTableViewer) columnViewer).getTable();
		}
		if (columnViewer instanceof CheckboxTreeViewer) {
			return ((CheckboxTreeViewer) columnViewer).getTree();
		}
		return null;
	}

	/*
	 * Returns the mapValue in this case, since there is not actually a single
	 * value associated with the viewer. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return mapValue;
	}

	/**
	 * Set up the initial state of the viewer from the initial state of the
	 * items; set the button state controlling the filter from the initial map
	 * value.
	 * 
	 * @throws CoreException
	 */
	@SuppressWarnings("unchecked")
	public void initializeChecked() throws CoreException {
		Set<String> allChecked = getChecked();
		Collection<Object> input = (Collection<Object>) ((Viewer) viewer).getInput();
		for (Object o : input) {
			boolean checked = false;
			if (o instanceof ICellEditorUpdateModel) {
				ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
				if (allChecked.isEmpty()) {
					checked = initialAllChecked;
				} else {
					checked = allChecked.contains(model.getName());
				}
				model.setChecked(checked);
				viewer.setChecked(model, checked);
			} else {
				viewer.setChecked(o, false);
			}
		}

		Boolean b = (Boolean) lcMap.getValue(JAXBControlUIConstants.SHOW_ONLY_CHECKED + name);
		if (b == null) {
			b = false;
		}
		showOnlySelected.setSelection(b);
		if (b) {
			columnViewer.addFilter(filter);
		}
		handleUpdate(null);
	}

	private Set<String> getChecked() {
		Set<String> set = new TreeSet<String>();
		String state = (String) lcMap.getValue(JAXBControlUIConstants.CHECKED_ATTRIBUTES + name);
		if (state != null && !state.equals(JAXBControlUIConstants.ZEROSTR)) {
			String[] split = state.split(JAXBControlUIConstants.SP);
			for (String s : split) {
				set.add(s);
			}
		}
		return set;
	}

	/*
	 * Store the checked string and the show-only-checked setting (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.AbstractUpdateModel#storeValue()
	 */
	@SuppressWarnings("unchecked")
	public void putCheckedSettings(LCVariableMap lcMap) {
		checked.setLength(0);
		Collection<Object> input = (Collection<Object>) ((Viewer) viewer).getInput();
		for (Object o : input) {
			if (o instanceof ICellEditorUpdateModel) {
				ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
				if (model.isChecked()) {
					String name = model.getName();
					if (name != null && !JAXBUIConstants.ZEROSTR.equals(name)) {
						checked.append(model.getName()).append(JAXBControlUIConstants.SP);
					}
				}
			}
		}
		lcMap.putValue(JAXBControlUIConstants.CHECKED_ATTRIBUTES + name, checked.toString().trim());
		lcMap.putValue(JAXBControlUIConstants.SHOW_ONLY_CHECKED + name, showOnlySelected.getSelection());
	}

	/*
	 * NOP for this type; easier to keep in sync at the storeValue
	 * call.(non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		// does nothing
	}

	/**
	 * @param showOnlySelected
	 *            whether to apply filter to viewer
	 */
	public void setShowAll(Button showOnlySelected) {
		this.showOnlySelected = showOnlySelected;
	}

	/*
	 * Store the template setting (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.AbstractUpdateModel#storeValue()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object storeValue() {
		templatedValue.setLength(0);
		if (pattern != null) {
			Collection<Object> input = (Collection<Object>) ((Viewer) viewer).getInput();
			for (Object o : input) {
				if (o instanceof ICellEditorUpdateModel) {
					ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
					/*
					 * model will return ZEROSTR if the entry is not selected
					 */
					String replaced = model.getReplacedValue(pattern);
					if (!JAXBControlUIConstants.ZEROSTR.equals(replaced)) {
						templatedValue.append(separator).append(replaced);
					}
				}
			}
			templatedValue.delete(0, separator.length());
		}
		String t = templatedValue.toString().trim();
		if (!JAXBControlUIConstants.ZEROSTR.equals(t)) {
			t = lcMap.getString(t);
		}
		lcMap.putValue(name, t);
		mapValue = t;
		return t;
	}

	/*
	 * Model serves as widget selected listener for the filter button.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * Model serves as widget selected listener for the filter button.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if (showOnlySelected.getSelection()) {
			columnViewer.addFilter(filter);
		} else {
			columnViewer.removeFilter(filter);
		}
		handleUpdate(null);
	}

}
