package com.sworddance.util;

import static org.apache.commons.lang.StringUtils.join;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class AbstractXmlParser {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private Document xmlDocument;
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
    public AbstractXmlParser(Document xmlDocument) {
        this.xmlDocument = xmlDocument;
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
                resource = CUtilities.getResourceAsStream(this, this.fileName, alternateDirectories);
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

    protected String getNodeValue(Node node) {
        if ( node != null) {
            return node.getNodeValue();
        } else {
            return null;
        }
    }
    /**
     * For what ever reason this function is not available via the javax.xml api
     * @param parent
     * @param tagnames
     * @return
     */
    protected NodeListImpl getChildElementsByTagName(Node parent, boolean onlyAllowed, String...tagnames) {
        NodeListImpl nodeList = new NodeListImpl();
        List<String> tags = Arrays.asList(tagnames);
        NodeList children = parent.getChildNodes();
        for(int i = 0; i < children.getLength(); i++ ) {
            Node child = children.item(i);
            if ( child.getNodeType() == Node.ELEMENT_NODE ) {
                if (tags.contains(child.getNodeName())) {
                    nodeList.add(child);
                } else if ( onlyAllowed){
                    ApplicationIllegalArgumentException.fail( "Child element is named ", child.getNodeName(), " only ", join(tagnames, ","), " are permitted");
                }
            }
        }
        return nodeList;
    }
    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    protected Document getXmlDocument() {
        return xmlDocument;
    }

    protected Element getDocumentElement() {
        return this.xmlDocument.getDocumentElement();
    }

    protected String getAttributeString(NamedNodeMap attributes, String attributeName) {
        String attributeValue = null;
        if ( attributes != null ) {
            Node node = attributes.getNamedItem(attributeName);
            attributeValue = node==null?null:node.getNodeValue();
        }
        return attributeValue;
    }
    protected String getAttributeString(Node childNode, String attributeName) {
        NamedNodeMap attributes = childNode.getAttributes();
        return this.getAttributeString(attributes, attributeName);
    }

    protected static class NodeListImpl implements NodeList, Iterable<Node> {
        private List<Node> nodes;

        public NodeListImpl() {
            this.nodes = new ArrayList<Node>();
        }
        public NodeListImpl(List<Node> nodes) {
            this.nodes = nodes;
        }

        public Node item(int index) {
            return nodes.get(index);
        }
        public boolean add(Node node) {
            return this.nodes.add(node);
        }

        public int getLength() {
            return nodes.size();
        }
        public Iterator<Node> iterator() {
            return this.nodes.iterator();
        }
    }
}
