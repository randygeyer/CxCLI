
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GitLsRemoteViewType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="GitLsRemoteViewType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="TAGS"/>
 *     &lt;enumeration value="HEADS"/>
 *     &lt;enumeration value="TAGS_AND_HEADS"/>
 *     &lt;enumeration value="ALL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "GitLsRemoteViewType")
@XmlEnum
public enum GitLsRemoteViewType {

    TAGS,
    HEADS,
    TAGS_AND_HEADS,
    ALL;

    public String value() {
        return name();
    }

    public static GitLsRemoteViewType fromValue(String v) {
        return valueOf(v);
    }

}
