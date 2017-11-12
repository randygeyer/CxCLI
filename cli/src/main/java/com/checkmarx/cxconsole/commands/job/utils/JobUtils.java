package com.checkmarx.cxconsole.commands.job.utils;

import com.checkmarx.cxconsole.commands.job.constants.SASTResultsDTO;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobUtilException;
import com.checkmarx.parameters.CLIScanParametersSingleton;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

/**
 * Created by nirli on 05/11/2017.
 */
public class JobUtils {

    private static final int LOW_VULNERABILITY_RESULTS = 0;
    private static final int MEDIUM_VULNERABILITY_RESULTS = 1;
    private static final int HIGH_VULNERABILITY_RESULTS = 2;

    protected static Logger log = Logger.getLogger(LOG_NAME);

    private JobUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isWindows() {
        return (System.getProperty("os.name").contains("Windows"));
    }

    public static String gerWorkDirectory(CLIScanParametersSingleton parameters) throws CLIJobUtilException {
        String folderPath = parameters.getCliMandatoryParameters().getSrcPath();
        if (folderPath == null || folderPath.isEmpty()) {
            //in case of ScanProject command
            String prjName = PathHandler.normalizePathString(parameters.getCliMandatoryParameters().getProjectName());
            folderPath = System.getProperty("user.dir") + File.separator + prjName;
            File folder = new File(folderPath);
            if (!folder.exists()) {
                boolean result = folder.mkdir();
                if (!result) {
                    throw new CLIJobUtilException("Error getting work directory");
                }
            }
        }
        return folderPath;
    }

    public static SASTResultsDTO parseScanSummary(String scanSummary) throws CLIJobException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        InputSource is = new InputSource(new StringReader(scanSummary));
        Document xmlDoc;
        try {
            builder = factory.newDocumentBuilder();
            xmlDoc = builder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Error during parsing SAST scan summary: " + e.getMessage());
            throw new CLIJobException("Error during parsing SAST scan summary: " + e.getMessage());
        }
        xmlDoc.getDocumentElement().normalize();
        NodeList nList = xmlDoc.getElementsByTagName("GetScanSummaryResult");
        Node node = nList.item(0);
        node.getNodeType();
        Element eElement = (Element) node;
        String highStr = eElement.getElementsByTagName("High").item(0).getTextContent();
        String mediumStr = eElement.getElementsByTagName("Medium").item(0).getTextContent();
        String lowStr = eElement.getElementsByTagName("Low").item(0).getTextContent();

        return new SASTResultsDTO(highStr, mediumStr, lowStr);
    }
}