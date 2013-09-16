
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
 *         &lt;element name="ClientType" type="{http://Checkmarx.com}CxClientType"/>
 *         &lt;element name="APIVersion" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "clientType",
    "apiVersion"
})
@XmlRootElement(name = "GetWebServiceUrl")
public class GetWebServiceUrl {

    @XmlElement(name = "ClientType", required = true)
    protected CxClientType clientType;
    @XmlElement(name = "APIVersion")
    protected int apiVersion;

    /**
     * Gets the value of the clientType property.
     * 
     * @return
     *     possible object is
     *     {@link CxClientType }
     *     
     */
    public CxClientType getClientType() {
        return clientType;
    }

    /**
     * Sets the value of the clientType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CxClientType }
     *     
     */
    public void setClientType(CxClientType value) {
        this.clientType = value;
    }

    /**
     * Gets the value of the apiVersion property.
     * 
     */
    public int getAPIVersion() {
        return apiVersion;
    }

    /**
     * Sets the value of the apiVersion property.
     * 
     */
    public void setAPIVersion(int value) {
        this.apiVersion = value;
    }

}
