
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ScanActionSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ScanActionSettings">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ScanActionList" type="{http://Checkmarx.com/v7}ArrayOfScanAction" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScanActionSettings", propOrder = {
    "scanActionList"
})
public class ScanActionSettings {

    @XmlElement(name = "ScanActionList")
    protected ArrayOfScanAction scanActionList;

    /**
     * Gets the value of the scanActionList property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfScanAction }
     *     
     */
    public ArrayOfScanAction getScanActionList() {
        return scanActionList;
    }

    /**
     * Sets the value of the scanActionList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfScanAction }
     *     
     */
    public void setScanActionList(ArrayOfScanAction value) {
        this.scanActionList = value;
    }

}
