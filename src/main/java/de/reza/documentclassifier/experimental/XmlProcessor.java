package de.reza.documentclassifier.experimental;

import de.reza.documentclassifier.pojo.Token;
import lombok.extern.slf4j.Slf4j;
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

//@Service
@Slf4j
public class XmlProcessor {

    /** ATTENTION old approach for retrieving tokens from xml files. xml is slow & cumbersome... json > xml :)
     * Creates an XML file with the recognized {@link Token}.
     * Syntax of XML-File:
     * <tokenList>
     * <token>
     * 		<tokenKey>...</tokenKey>
     * 		<xAxis>...</xAxis>
     * 		<yAxis>...</yAxis>
     * 		<width>...</width>
     * 	</token>
     * 	...
     * 	</tokenList>
     * @param tokenList         List of {@link Token}
     * @param uuid              UUID of the model
     * @param filename          Name of the class within the model
     */
    public void generateTokenXmlFile(List<Token> tokenList, String uuid, String filename) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbuilder = dbFactory.newDocumentBuilder();
            Document document = dbuilder.newDocument();
            Element tokens = document.createElement("tokenList");
            document.appendChild(tokens);

            tokenList.forEach(token-> {Element element = document.createElement("token");
                tokens.appendChild(element);

                Element tokenKey = document.createElement("tokenKey");
                tokenKey.appendChild(document.createTextNode(token.getTokeName()));
                element.appendChild(tokenKey);

                Element xAxis = document.createElement("xAxis");
                xAxis.appendChild(document.createTextNode(String.valueOf(token.getXAxis())));
                element.appendChild(xAxis);

                Element yAxis = document.createElement("yAxis");
                yAxis.appendChild(document.createTextNode(String.valueOf(token.getYAxis())));
                element.appendChild(yAxis);
            });

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            File dic = new File("models/" + uuid);
            StreamResult result = new StreamResult(new File( dic.getPath() + "/" + filename + ".xml"));
            transformer.transform(source, result);
            log.info("XML File created: {}", filename);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Reads XML file from model
     * @param xmlFile   XML file within the model
     * @return          list of all Tokens within the XML file
     */
    public List<Token> readXmlFile(File xmlFile){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try (InputStream is = new FileInputStream(xmlFile)) {

            List<Token> tokenList = new ArrayList<>();

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("token");

            for (int i = 0; i < nList.getLength(); i++) {

                Node nNode = nList.item(i);
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
                    tokenList.add(new Token(tokenName, Double.parseDouble(xAxis), Double.parseDouble(yAxis)));
                }
            }
            return tokenList;
        } catch (Exception e) {
            return null;
        }
    }

}
