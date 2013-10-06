
package com.checkmarx.cxviewer.ws.resolver;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetWebServiceUrlResult" type="{http://Checkmarx.com}CxWSResponseDiscovery" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getWebServiceUrlResult"
})
@XmlRootElement(name = "GetWebServiceUrlResponse")
public class GetWebServiceUrlResponse {

    @XmlElement(name = "GetWebServiceUrlResult")
    protected CxWSResponseDiscovery getWebServiceUrlResult;

    /**
     * Gets the value of the getWebServiceUrlResult property.
     * 
     * @return
     *     possible object is
     *     {@link CxWSResponseDiscovery }
     *     
     */
    public CxWSResponseDiscovery getGetWebServiceUrlResult() {
        return getWebServiceUrlResult;
    }

    /**
     * Sets the value of the getWebServiceUrlResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CxWSResponseDiscovery }
     *     
     */
    public void setGetWebServiceUrlResult(CxWSResponseDiscovery value) {
        this.getWebServiceUrlResult = value;
    }

}
