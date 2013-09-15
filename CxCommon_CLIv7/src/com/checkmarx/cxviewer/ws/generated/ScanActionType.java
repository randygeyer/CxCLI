
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ScanActionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ScanActionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="EmailNotification"/>
 *     &lt;enumeration value="PostScanAction"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ScanActionType")
@XmlEnum
public enum ScanActionType {

    @XmlEnumValue("EmailNotification")
    EMAIL_NOTIFICATION("EmailNotification"),
    @XmlEnumValue("PostScanAction")
    POST_SCAN_ACTION("PostScanAction");
    private final String value;

    ScanActionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ScanActionType fromValue(String v) {
        for (ScanActionType c: ScanActionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
