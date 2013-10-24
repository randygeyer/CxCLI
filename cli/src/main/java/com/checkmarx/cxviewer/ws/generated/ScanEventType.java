
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ScanEventType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ScanEventType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BeforeScanStarts"/>
 *     &lt;enumeration value="AfterScanSucceeds"/>
 *     &lt;enumeration value="OnScanFailure"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ScanEventType")
@XmlEnum
public enum ScanEventType {

    @XmlEnumValue("BeforeScanStarts")
    BEFORE_SCAN_STARTS("BeforeScanStarts"),
    @XmlEnumValue("AfterScanSucceeds")
    AFTER_SCAN_SUCCEEDS("AfterScanSucceeds"),
    @XmlEnumValue("OnScanFailure")
    ON_SCAN_FAILURE("OnScanFailure");
    private final String value;

    ScanEventType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ScanEventType fromValue(String v) {
        for (ScanEventType c: ScanEventType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
