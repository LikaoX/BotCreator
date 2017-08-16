package com.sddev.botmaker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;

public class Strings {
    private static HashMap<String, String> stringHashMap;
    static {
        stringHashMap = new HashMap<>();
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(Strings.class.getResourceAsStream("xml/strings.xml"));
            Element root = document.getDocumentElement();
            NodeList list = root.getChildNodes();
            for(int i = 0 ; i < list.getLength() ; i++) {
                Node node = list.item(i);
                if(node instanceof Element) {
                    Element element = (Element) node;
                    if(element.hasAttribute("id") && element.getTagName().equals("string")) {
                        stringHashMap.put(element.getAttribute("id"), element.getTextContent().trim());
                    }
                }
            }
        } catch(ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch(IOException ex) {
            ex.printStackTrace();
        } catch(SAXException ex) {
            ex.printStackTrace();
        }
    }

    public static String get(String key) {
        return stringHashMap.get(key);
    }

    public static boolean containsKey(String key) {
        return stringHashMap.containsKey(key);
    }
}
