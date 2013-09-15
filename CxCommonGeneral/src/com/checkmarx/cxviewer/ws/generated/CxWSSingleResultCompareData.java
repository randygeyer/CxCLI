
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CxWSSingleResultCompareData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CxWSSingleResultCompareData">
 *   &lt;complexContent>
 *     &lt;extension base="{http://Checkmarx.com/}CxWSSingleResultData">
 *       &lt;sequence>
 *         &lt;element name="ResultStatus" type="{CxDataTypes.xsd}CompareStatusType"/>
 *         &lt;element name="ScanID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="QueryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CxWSSingleResultCompareData", propOrder = {
    "resultStatus",
    "scanID",
    "queryName"
})
public class CxWSSingleResultCompareData
    extends CxWSSingleResultData
{

    @XmlElement(name = "ResultStatus", required = true)
    protected CompareStatusType resultStatus;
    @XmlElement(name = "ScanID")
    protected long scanID;
    @XmlElement(name = "QueryName")
    protected String queryName;

    /**
     * Gets the value of the resultStatus property.
     * 
     * @return
     *     possible object is
     *     {@link CompareStatusType }
     *     
     */
    public CompareStatusType getResultStatus() {
        return resultStatus;
    }

    /**
     * Sets the value of the resultStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareStatusType }
     *     
     */
    public void setResultStatus(CompareStatusType value) {
        this.resultStatus = value;
    }

    /**
     * Gets the value of the scanID property.
     * 
     */
    public long getScanID() {
        return scanID;
    }

    /**
     * Sets the value of the scanID property.
     * 
     */
    public void setScanID(long value) {
        this.scanID = value;
    }

    /**
     * Gets the value of the queryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryName() {
        return queryName;
    }

    /**
     * Sets the value of the queryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryName(String value) {
        this.queryName = value;
    }

}
