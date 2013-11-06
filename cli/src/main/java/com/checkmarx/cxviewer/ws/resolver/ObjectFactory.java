
package com.checkmarx.cxviewer.ws.resolver;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.checkmarx.cxviewer.ws.resolver package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.checkmarx.cxviewer.ws.resolver
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CxWSResponseDiscovery }
     * 
     */
    public CxWSResponseDiscovery createCxWSResponseDiscovery() {
        return new CxWSResponseDiscovery();
    }

    /**
     * Create an instance of {@link GetWebServiceUrl }
     * 
     */
    public GetWebServiceUrl createGetWebServiceUrl() {
        return new GetWebServiceUrl();
    }

    /**
     * Create an instance of {@link CxWSBasicRepsonse }
     * 
     */
    public CxWSBasicRepsonse createCxWSBasicRepsonse() {
        return new CxWSBasicRepsonse();
    }

    /**
     * Create an instance of {@link GetWebServiceUrlResponse }
     * 
     */
    public GetWebServiceUrlResponse createGetWebServiceUrlResponse() {
        return new GetWebServiceUrlResponse();
    }

}
