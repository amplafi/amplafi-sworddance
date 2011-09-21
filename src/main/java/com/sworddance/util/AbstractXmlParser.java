package com.sworddance.util;

import static org.apache.commons.lang.StringUtils.join;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class AbstractXmlParser {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    protected Document xmlDocument;
    private String fileName;

    protected AbstractXmlParser() {

    }
    protected AbstractXmlParser(String fileName, String...alternateDirectories) {
        this.fileName = fileName;
        this.createXmlDocument(alternateDirectories);
    }

    public AbstractXmlParser(InputStream inputStream) {
        DocumentBuilder newDocumentBuilder = createDocumentBuilder();
        try {
            this.xmlDocument = newDocumentBuilder.parse(inputStream);
        } catch (SAXException e) {
            throw new ApplicationGeneralException(e);
        } catch (IOException e) {
            throw new ApplicationGeneralException(e);
        }
    }
    /**
     * @return
     */
    protected DocumentBuilder createDocumentBuilder() {
        try {
            return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ApplicationGeneralException(e);
        }
    }

    private void createXmlDocument(String...alternateDirectories) {
        DocumentBuilder newDocumentBuilder = createDocumentBuilder();
        File file = new File(fileName);
        InputStream resource = null;
        try {
            if ( file.exists()) {
                this.xmlDocument = newDocumentBuilder.parse(file);
            } else {
                List<String> searchPaths = CUtilities.createSearchPath(this.fileName, alternateDirectories);
                resource = CUtilities.getResourceAsStream(this, searchPaths);
                ApplicationIllegalArgumentException.notNull(resource, "Cannot locate xml definitions file. File '",
                    file,"' does not exist and cannot find a resource in the classpath=", join(searchPaths, ","));
                this.xmlDocument = newDocumentBuilder.parse(resource);
            }
        } catch (SAXException e) {
            throw new ApplicationGeneralException(e);
        } catch (IOException e) {
            throw new ApplicationGeneralException(e);
        } finally {
            if ( resource != null) {
                try {
                    resource.close();
                } catch(Exception e) {}
            }
        }
    }
    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

}
