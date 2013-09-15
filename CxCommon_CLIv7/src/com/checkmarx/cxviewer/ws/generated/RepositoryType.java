
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RepositoryType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RepositoryType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="TFS"/>
 *     &lt;enumeration value="SVN"/>
 *     &lt;enumeration value="CVS"/>
 *     &lt;enumeration value="GIT"/>
 *     &lt;enumeration value="NONE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "RepositoryType")
@XmlEnum
public enum RepositoryType {

    TFS,
    SVN,
    CVS,
    GIT,
    NONE;

    public String value() {
        return name();
    }

    public static RepositoryType fromValue(String v) {
        return valueOf(v);
    }

}
