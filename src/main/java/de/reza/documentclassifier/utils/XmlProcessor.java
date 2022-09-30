package de.reza.documentclassifier.utils;

import de.reza.documentclassifier.pojo.Token;
import org.springframework.stereotype.Service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class XmlProcessor {



    public void generateTokenXmlFile(Map<Token, Long> map, String uuid, int numberOfDocuments) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbuilder = dbFactory.newDocumentBuilder();
            Document document = dbuilder.newDocument();

            Element tokens = document.createElement("tokenList");
            document.appendChild(tokens);

            for (Map.Entry<Token, Long> entry : map.entrySet()) {

                if (entry.getValue() >= numberOfDocuments * 0.95) {
                    Element element = document.createElement("token");
                    tokens.appendChild(element);

                    Element tokenKey = document.createElement("tokenKey");
                    tokenKey.appendChild(document.createTextNode(entry.getKey().getTokeName()));
                    element.appendChild(tokenKey);

                    Element xAxis = document.createElement("xAxis");
                    xAxis.appendChild(document.createTextNode(String.valueOf(entry.getKey().getXAxis())));
                    element.appendChild(xAxis);

                    Element yAxis = document.createElement("yAxis");
                    yAxis.appendChild(document.createTextNode(String.valueOf(entry.getKey().getYAxis())));
                    element.appendChild(yAxis);

                    Element width = document.createElement("width");
                    width.appendChild(document.createTextNode(String.valueOf(entry.getKey().getWidth())));
                    element.appendChild(width);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);


            File dic = new File("models/" + uuid);

            StreamResult result = new StreamResult(new File( dic.getPath() + "/" + uuid + ".xml"));

            transformer.transform(source, result);
            log.info("XML File created: {}", uuid);
            //return uuid;
        } catch (ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * Extracts value from XML file with following syntax:
     * <keywords>
     * 	<keyword>
     * 		<value>VALUE</value>
     * 	</keyword>
     *</keywords>
     * @param file contains tokens
     * @return  list of all value of the XML file
     */
    public static List<String > readTokenXmlFile(MultipartFile file){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try (InputStream is = file.getInputStream()) {

            List<String> tokenList = new ArrayList<>();

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("keyword");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String value =  eElement
                            .getElementsByTagName("value")
                            .item(0)
                            .getTextContent();
                    tokenList.add(value);
                }
            }
            return tokenList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Token> readXmlFile(File file){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try (InputStream is = new FileInputStream(file)) {

            List<Token> tokenList = new ArrayList<>();

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("token");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String tokenName =  eElement
                            .getElementsByTagName("tokenKey")
                            .item(0)
                            .getTextContent();

                    String xAxis =  eElement
                            .getElementsByTagName("xAxis")
                            .item(0)
                            .getTextContent();

                    String yAxis =  eElement
                            .getElementsByTagName("yAxis")
                            .item(0)
                            .getTextContent();

                    String width =  eElement
                            .getElementsByTagName("width")
                            .item(0)
                            .getTextContent();

                    Token tmpToken = Token.builder()
                            .tokeName(tokenName)
                            .xAxis(Float.parseFloat(xAxis))
                            .yAxis(Float.parseFloat(yAxis))
                            .width(Float.parseFloat(width))
                            .build();
                    tokenList.add(tmpToken);
                }
            }
            return tokenList;
        } catch (Exception e) {
            return null;
        }
    }

}
