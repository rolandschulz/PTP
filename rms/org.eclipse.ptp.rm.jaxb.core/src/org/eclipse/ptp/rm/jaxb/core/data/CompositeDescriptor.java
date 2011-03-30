//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.03.30 at 11:18:33 AM CDT 
//

package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * Dynamic means this group gets rebuilt; in that case, a button allowing the
 * user to select displayed attributes is added.
 * 
 * 
 * <p>
 * Java class for composite-descriptor complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="composite-descriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="grid-data" type="{http://org.eclipse.ptp/rm}grid-data-descriptor" minOccurs="0"/>
 *         &lt;element name="grid-layout" type="{http://org.eclipse.ptp/rm}grid-layout-descriptor" minOccurs="0"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="style" type="{http://org.eclipse.ptp/rm}style" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element name="tab-folder" type="{http://org.eclipse.ptp/rm}tab-folder-descriptor"/>
 *           &lt;element name="composite" type="{http://org.eclipse.ptp/rm}composite-descriptor"/>
 *           &lt;element name="widget" type="{http://org.eclipse.ptp/rm}widget"/>
 *           &lt;element name="viewer" type="{http://org.eclipse.ptp/rm}attribute-viewer"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="group" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "composite-descriptor", propOrder = { "gridData", "gridLayout", "title", "style", "tabFolderOrCompositeOrWidget" })
public class CompositeDescriptor {

	@XmlElement(name = "grid-data")
	protected GridDataDescriptor gridData;
	@XmlElement(name = "grid-layout")
	protected GridLayoutDescriptor gridLayout;
	protected String title;
	protected Style style;
	@XmlElements({ @XmlElement(name = "viewer", type = AttributeViewer.class), @XmlElement(name = "widget", type = Widget.class),
			@XmlElement(name = "tab-folder", type = TabFolderDescriptor.class),
			@XmlElement(name = "composite", type = CompositeDescriptor.class) })
	protected List<Object> tabFolderOrCompositeOrWidget;
	@XmlAttribute
	protected Boolean group;

	/**
	 * Gets the value of the gridData property.
	 * 
	 * @return possible object is {@link GridDataDescriptor }
	 * 
	 */
	public GridDataDescriptor getGridData() {
		return gridData;
	}

	/**
	 * Gets the value of the gridLayout property.
	 * 
	 * @return possible object is {@link GridLayoutDescriptor }
	 * 
	 */
	public GridLayoutDescriptor getGridLayout() {
		return gridLayout;
	}

	/**
	 * Gets the value of the style property.
	 * 
	 * @return possible object is {@link Style }
	 * 
	 */
	public Style getStyle() {
		return style;
	}

	/**
	 * Gets the value of the tabFolderOrCompositeOrWidget property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the tabFolderOrCompositeOrWidget property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getTabFolderOrCompositeOrWidget().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link AttributeViewer } {@link Widget } {@link TabFolderDescriptor }
	 * {@link CompositeDescriptor }
	 * 
	 * 
	 */
	public List<Object> getTabFolderOrCompositeOrWidget() {
		if (tabFolderOrCompositeOrWidget == null) {
			tabFolderOrCompositeOrWidget = new ArrayList<Object>();
		}
		return this.tabFolderOrCompositeOrWidget;
	}

	/**
	 * Gets the value of the title property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the value of the group property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isGroup() {
		if (group == null) {
			return false;
		} else {
			return group;
		}
	}

	/**
	 * Sets the value of the gridData property.
	 * 
	 * @param value
	 *            allowed object is {@link GridDataDescriptor }
	 * 
	 */
	public void setGridData(GridDataDescriptor value) {
		this.gridData = value;
	}

	/**
	 * Sets the value of the gridLayout property.
	 * 
	 * @param value
	 *            allowed object is {@link GridLayoutDescriptor }
	 * 
	 */
	public void setGridLayout(GridLayoutDescriptor value) {
		this.gridLayout = value;
	}

	/**
	 * Sets the value of the group property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setGroup(Boolean value) {
		this.group = value;
	}

	/**
	 * Sets the value of the style property.
	 * 
	 * @param value
	 *            allowed object is {@link Style }
	 * 
	 */
	public void setStyle(Style value) {
		this.style = value;
	}

	/**
	 * Sets the value of the title property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTitle(String value) {
		this.title = value;
	}

}