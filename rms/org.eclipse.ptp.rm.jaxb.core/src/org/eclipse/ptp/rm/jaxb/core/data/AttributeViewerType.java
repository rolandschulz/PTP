//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.09.07 at 09:57:43 PM EDT 
//


package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for attribute-viewer-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="attribute-viewer-type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="layout-data" type="{http://org.eclipse.ptp/rm}layout-data-type" minOccurs="0"/>
 *         &lt;element name="layout" type="{http://org.eclipse.ptp/rm}layout-type" minOccurs="0"/>
 *         &lt;element name="label" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="column-data" type="{http://org.eclipse.ptp/rm}column-data-type" maxOccurs="7" minOccurs="0"/>
 *         &lt;element name="items" type="{http://org.eclipse.ptp/rm}viewer-items-type"/>
 *         &lt;element name="value" type="{http://org.eclipse.ptp/rm}template-type" minOccurs="0"/>
 *         &lt;element name="control-state" type="{http://org.eclipse.ptp/rm}control-state-type" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" default="table">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="table"/>
 *             &lt;enumeration value="tree"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="initialAllChecked" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="sort" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="headerVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="linesVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="tooltipEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="style" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "attribute-viewer-type", propOrder = {
    "layoutData",
    "layout",
    "label",
    "columnData",
    "items",
    "value",
    "controlState"
})
public class AttributeViewerType {

    @XmlElement(name = "layout-data")
    protected LayoutDataType layoutData;
    protected LayoutType layout;
    protected String label;
    @XmlElement(name = "column-data")
    protected List<ColumnDataType> columnData;
    @XmlElement(required = true)
    protected ViewerItemsType items;
    protected TemplateType value;
    @XmlElement(name = "control-state")
    protected ControlStateType controlState;
    @XmlAttribute
    protected String type;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected Boolean initialAllChecked;
    @XmlAttribute
    protected Boolean sort;
    @XmlAttribute
    protected Boolean headerVisible;
    @XmlAttribute
    protected Boolean linesVisible;
    @XmlAttribute
    protected Boolean tooltipEnabled;
    @XmlAttribute
    protected String style;

    /**
     * Gets the value of the layoutData property.
     * 
     * @return
     *     possible object is
     *     {@link LayoutDataType }
     *     
     */
    public LayoutDataType getLayoutData() {
        return layoutData;
    }

    /**
     * Sets the value of the layoutData property.
     * 
     * @param value
     *     allowed object is
     *     {@link LayoutDataType }
     *     
     */
    public void setLayoutData(LayoutDataType value) {
        this.layoutData = value;
    }

    /**
     * Gets the value of the layout property.
     * 
     * @return
     *     possible object is
     *     {@link LayoutType }
     *     
     */
    public LayoutType getLayout() {
        return layout;
    }

    /**
     * Sets the value of the layout property.
     * 
     * @param value
     *     allowed object is
     *     {@link LayoutType }
     *     
     */
    public void setLayout(LayoutType value) {
        this.layout = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the columnData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the columnData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getColumnData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ColumnDataType }
     * 
     * 
     */
    public List<ColumnDataType> getColumnData() {
        if (columnData == null) {
            columnData = new ArrayList<ColumnDataType>();
        }
        return this.columnData;
    }

    /**
     * Gets the value of the items property.
     * 
     * @return
     *     possible object is
     *     {@link ViewerItemsType }
     *     
     */
    public ViewerItemsType getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     * 
     * @param value
     *     allowed object is
     *     {@link ViewerItemsType }
     *     
     */
    public void setItems(ViewerItemsType value) {
        this.items = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link TemplateType }
     *     
     */
    public TemplateType getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link TemplateType }
     *     
     */
    public void setValue(TemplateType value) {
        this.value = value;
    }

    /**
     * Gets the value of the controlState property.
     * 
     * @return
     *     possible object is
     *     {@link ControlStateType }
     *     
     */
    public ControlStateType getControlState() {
        return controlState;
    }

    /**
     * Sets the value of the controlState property.
     * 
     * @param value
     *     allowed object is
     *     {@link ControlStateType }
     *     
     */
    public void setControlState(ControlStateType value) {
        this.controlState = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        if (type == null) {
            return "table";
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the initialAllChecked property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInitialAllChecked() {
        if (initialAllChecked == null) {
            return true;
        } else {
            return initialAllChecked;
        }
    }

    /**
     * Sets the value of the initialAllChecked property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInitialAllChecked(Boolean value) {
        this.initialAllChecked = value;
    }

    /**
     * Gets the value of the sort property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSort() {
        if (sort == null) {
            return true;
        } else {
            return sort;
        }
    }

    /**
     * Sets the value of the sort property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSort(Boolean value) {
        this.sort = value;
    }

    /**
     * Gets the value of the headerVisible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHeaderVisible() {
        if (headerVisible == null) {
            return true;
        } else {
            return headerVisible;
        }
    }

    /**
     * Sets the value of the headerVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHeaderVisible(Boolean value) {
        this.headerVisible = value;
    }

    /**
     * Gets the value of the linesVisible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLinesVisible() {
        if (linesVisible == null) {
            return true;
        } else {
            return linesVisible;
        }
    }

    /**
     * Sets the value of the linesVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLinesVisible(Boolean value) {
        this.linesVisible = value;
    }

    /**
     * Gets the value of the tooltipEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTooltipEnabled() {
        if (tooltipEnabled == null) {
            return true;
        } else {
            return tooltipEnabled;
        }
    }

    /**
     * Sets the value of the tooltipEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTooltipEnabled(Boolean value) {
        this.tooltipEnabled = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyle(String value) {
        this.style = value;
    }

}
