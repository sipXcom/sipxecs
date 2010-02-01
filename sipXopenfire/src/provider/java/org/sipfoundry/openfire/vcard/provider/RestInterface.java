/*
 *
 *
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.openfire.vcard.provider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.IOException;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Attribute;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;

import org.jivesoftware.util.Log;

public class RestInterface {

    public static final String REST_CALL_PROTO = "https://";
    public static final String REST_CALL_URL_CONTACT_INFO = "/sipxconfig/rest/my/contact-information";
    public static final String REST_CALL_PORT = "8443";

    private static RestInterface instance = null;

    synchronized public static RestInterface getInstance() {
        if (instance == null)
            instance = new RestInterface();

        return instance;

    }

    public static String sendRequest(String method, String sipXserver, String username, String password,
            Element vcardElement) {
        try {
            StringBuilder urlStr = new StringBuilder().append(REST_CALL_PROTO).append(sipXserver).append(":")
                    .append(REST_CALL_PORT).append(REST_CALL_URL_CONTACT_INFO);
            Log.debug("call REST URL " + urlStr.toString());
            URL serverURL = new URL(urlStr.toString());
            HttpsURLConnection conn = (HttpsURLConnection) serverURL.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod(method);

            String val = (new StringBuilder(username).append(":").append(password)).toString();
            byte[] base = val.getBytes();
            String authorizationString = "Basic " + new String(new Base64().encode(base));
            conn.setRequestProperty("Authorization", authorizationString);

            if (vcardElement != null) {
                conn.setDoInput(true);
                conn.setRequestProperty("Content-type", "text/xml");
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(RestInterface.buildXMLContactInfo(vcardElement));
                wr.flush();
                wr.close();
            }

            conn.connect();

            Log.debug("response code " + conn.getResponseCode() + " response message " + conn.getResponseMessage());

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                resp.append(line);
            }
            rd.close();

            if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                return resp.toString();
            } else {
                Log.info("Response code " + conn.getResponseCode() + ":" + conn.getResponseMessage() + " "
                        + resp.toString());
                return null;
            }
        }

        catch (IOException ex) {
            Log.error("In sendRequest IOException " + ex.getMessage());
            return null;
        }

        catch (Exception ex) {
            Log.error("Exception " + ex.getMessage());
            return null;
        }
    }

    public String buildXMLContactInfoXSLT(Element e) {
        try {
            String x = e.asXML().replace("xmlns=\"vcard-temp\"", ""); // xmlns causes dom4j xpath
                                                                      // not working somehow.
            Document vcardDoc = DocumentHelper.parseText(x);

            Log.debug("before XSLT " + vcardDoc.getRootElement().asXML());
            InputStream inStream = this.getClass().getResourceAsStream("/contactInfo.xsl");
            Document contactDoc = styleDocument(vcardDoc, inStream);

            Log.debug("After XSLT " + contactDoc.getRootElement().asXML());
            return contactDoc.getRootElement().asXML();
        } catch (Exception ex) {
            Log.info(ex.getMessage());
            return null;
        }

    }

    public static String buildXMLContactInfo(Element e) {
        try {
            String x = e.asXML().replace("xmlns=\"vcard-temp\"", ""); // xmlns causes dom4j xpath
                                                                      // not working somehow.

            Log.debug("vcard string is " + x);
            Document vcardDoc = DocumentHelper.parseText(x);
            Element el = vcardDoc.getRootElement();

            StringBuilder xbuilder = new StringBuilder("<contact-information>");

            xbuilder.append("<firstName>");
            xbuilder.append(getNodeText(el, "N/FAMILY"));
            xbuilder.append("</firstName>");
            xbuilder.append("<lastName>");
            xbuilder.append(getNodeText(el, "N/GIVEN"));
            xbuilder.append("</lastName>");
            xbuilder.append("<jobTitle>");
            xbuilder.append(getNodeText(el, "TITLE"));
            xbuilder.append("</jobTitle>");
            xbuilder.append("<jobDept>");
            xbuilder.append(getNodeText(el, "ORG/ORGUNIT"));
            xbuilder.append("</jobDept>");
            xbuilder.append("<companyName>");
            xbuilder.append(getNodeText(el, "ORG/ORGNAME"));
            xbuilder.append("</companyName>");

            xbuilder.append("<officeAddress>");
            xbuilder.append("<street>");
            xbuilder.append(getNodeText(el, "ADR/STREET"));
            xbuilder.append("</street>");
            xbuilder.append("<city>");
            xbuilder.append(getNodeText(el, "ADR/LOCALITY"));
            xbuilder.append("</city>");
            xbuilder.append("<country>");
            xbuilder.append(getNodeText(el, "ADR/CTRY"));
            xbuilder.append("</country>");
            xbuilder.append("<state>");
            xbuilder.append(getNodeText(el, "ADR/REGION"));
            xbuilder.append("</state>");
            xbuilder.append("<zip>");
            xbuilder.append(getNodeText(el, "ADR/PCODE"));
            xbuilder.append("</zip>");
            xbuilder.append("</officeAddress>");

            xbuilder.append("<cellPhoneNumber>");
            xbuilder.append(getNodeText(el, "TEL/NUMBER"));
            xbuilder.append("</cellPhoneNumber>");
            /*
             * if (!(getNodeText(el, "JABBERID").equals("unknown"))) { xbuilder.append("<imId>");
             * xbuilder.append(getNodeText(el, "JABBERID"));xbuilder.append("</imId>"); }
             */
            xbuilder.append("<emailAddress>");
            xbuilder.append(getNodeText(el, "EMAIL/USERID"));
            xbuilder.append("</emailAddress>");
            xbuilder.append("</contact-information>");

            Log.debug("buildXMLContactInfo is " + xbuilder.toString());

            return xbuilder.toString();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
            return null;
        }

    }

    public Element buildVCardFromXMLContactInfoXSLT(String xmlString) {
        try {
            Document contactDoc = DocumentHelper.parseText(xmlString);
            InputStream inStream = this.getClass().getResourceAsStream("/vCardTemp.xsl");

            Log.debug("before XSLT " + contactDoc.getRootElement().asXML());
            Document vcardDoc = styleDocument(contactDoc, inStream);
            Log.debug("After XSLT " + vcardDoc.getRootElement().asXML());

            return vcardDoc.getRootElement();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
            return null;
        }

    }

    public static Element buildVCardFromXMLContactInfo(String xmlstring) {
        try {
            SAXReader sreader = new SAXReader();


            Log.debug("xmlstring for build vcard is " + xmlstring);

            Document contactDoc = sreader.read(new StringReader(xmlstring));
            Element rootElement = contactDoc.getRootElement();

            StringBuilder xbuilder = new StringBuilder("<vCard xmlns='vcard-temp'>");

            xbuilder.append("<FN>");
            xbuilder.append(getNodeText(rootElement, "firstName") + " " + getNodeText(rootElement, "lastName"));
            xbuilder.append("</FN>");
            xbuilder.append("<N>");
            xbuilder.append("<FAMILY>");
            xbuilder.append(getNodeText(rootElement, "lastName"));
            xbuilder.append("</FAMILY>");
            xbuilder.append("<GIVEN>");
            xbuilder.append(getNodeText(rootElement, "firstName"));
            xbuilder.append("</GIVEN>");
            xbuilder.append("<MIDDLE/>");
            xbuilder.append("</N>");

            xbuilder.append("<NICKNAME>");
            xbuilder.append("");
            xbuilder.append("</NICKNAME>");
            xbuilder.append("<URL>");
            xbuilder.append("");
            xbuilder.append("</URL>");
            xbuilder.append("<BDAY>");
            xbuilder.append("");
            xbuilder.append("</BDAY>");
            xbuilder.append("<ORG>");
            xbuilder.append("<ORGNAME>");
            xbuilder.append(getNodeText(rootElement, "companyName"));
            xbuilder.append("</ORGNAME>");
            xbuilder.append("<ORGUNIT>");
            xbuilder.append(getNodeText(rootElement, "jobDept"));
            xbuilder.append("</ORGUNIT>");
            xbuilder.append("</ORG>");
            xbuilder.append("<TITLE>");
            xbuilder.append(getNodeText(rootElement, "jobTitle"));
            xbuilder.append("</TITLE>");
            xbuilder.append("<ROLE/>");
            xbuilder.append("<TEL>");
            xbuilder.append("<WORK/>");
            xbuilder.append("<VOICE/>");
            xbuilder.append("<NUMBER/>");
            xbuilder.append("</TEL>");

            xbuilder.append("<TEL>");
            xbuilder.append("<WORK/>");
            xbuilder.append("<FAX/>");
            xbuilder.append("<NUMBER>");
            xbuilder.append(getNodeText(rootElement, "faxNumber"));
            xbuilder.append("</NUMBER>");
            xbuilder.append("</TEL>");

            xbuilder.append("<TEL>");
            xbuilder.append("<WORK/>");
            xbuilder.append("<MSG/>");
            xbuilder.append("<NUMBER/>");
            xbuilder.append("</TEL>");

            xbuilder.append("<ADR>");
            xbuilder.append("<WORK/>");
            xbuilder.append("<EXTADD>");
            xbuilder.append("</EXTADD>");
            xbuilder.append("<STREET>");
            xbuilder.append(getNodeText(rootElement, "officeAddress/street"));
            xbuilder.append("</STREET>");
            xbuilder.append("<LOCALITY>");
            xbuilder.append(getNodeText(rootElement, "officeAddress/city"));
            xbuilder.append("</LOCALITY>");
            xbuilder.append("<REGION>");
            xbuilder.append(getNodeText(rootElement, "officeAddress/state"));
            xbuilder.append("</REGION>");
            xbuilder.append("<PCODE>");
            xbuilder.append(getNodeText(rootElement, "officeAddress/zip"));
            xbuilder.append("</PCODE>");
            xbuilder.append("<CTRY>");
            xbuilder.append(getNodeText(rootElement, "officeAddress/country"));
            xbuilder.append("</CTRY>");
            xbuilder.append("</ADR>");

            xbuilder.append("<TEL>");
            xbuilder.append("<HOME/>");
            xbuilder.append("<VOICE/>");
            xbuilder.append("<NUMBER>");
            xbuilder.append(getNodeText(rootElement, "homePhoneNumber"));
            xbuilder.append("</NUMBER>");
            xbuilder.append("</TEL>");

            xbuilder.append("<TEL>");
            xbuilder.append("<HOME/>");
            xbuilder.append("<FAX/>");
            xbuilder.append("<NUMBER/>");
            xbuilder.append("</TEL>");

            xbuilder.append("<TEL>");
            xbuilder.append("<CELL/>");
            xbuilder.append("<VOICE/>");
            xbuilder.append("<NUMBER>");
            xbuilder.append(getNodeText(rootElement, "cellPhoneNumber"));
            xbuilder.append("</NUMBER>");
            xbuilder.append("</TEL>");

            xbuilder.append("<TEL>");
            xbuilder.append("<HOME/>");
            xbuilder.append("<MSG/>");
            xbuilder.append("<NUMBER/>");
            xbuilder.append("</TEL>");

            xbuilder.append("<ADR>");
            xbuilder.append("<HOME/>");
            xbuilder.append("<EXTADD/>");
            xbuilder.append("<STREET>");
            xbuilder.append(getNodeText(rootElement, "homeAddress/street"));
            xbuilder.append("</STREET>");
            xbuilder.append("<LOCALITY>");
            xbuilder.append(getNodeText(rootElement, "homeAddress/city"));
            xbuilder.append("</LOCALITY>");
            xbuilder.append("<REGION>");
            xbuilder.append(getNodeText(rootElement, "homeAddress/state"));
            xbuilder.append("</REGION>");
            xbuilder.append("<PCODE>");
            xbuilder.append(getNodeText(rootElement, "homeAddress/zip"));
            xbuilder.append("</PCODE>");
            xbuilder.append("<CTRY>");
            xbuilder.append(getNodeText(rootElement, "homeAddress/country"));
            xbuilder.append("</CTRY>");
            xbuilder.append("</ADR>");

            xbuilder.append("<EMAIL>");
            xbuilder.append("<INTERNET/>");
            xbuilder.append("<PREF/>");
            xbuilder.append("<USERID>");
            xbuilder.append(getNodeText(rootElement, "emailAddress"));
            xbuilder.append("</USERID>");
            xbuilder.append("</EMAIL>");

            xbuilder.append("<JABBERID>");
            xbuilder.append(getNodeText(rootElement, "imId"));
            xbuilder.append("</JABBERID>");
            xbuilder.append("<DESC/>");

            String encodedStr = getEncodedAvatar(getNodeText(rootElement, "avatar"));
            if (encodedStr != null)
            {
                xbuilder.append("<PHOTO>");
                xbuilder.append("<TYPE>image/png</TYPE>");
                xbuilder.append("<BINVAL>");
                xbuilder.append(encodedStr);
                xbuilder.append("</BINVAL>");
                xbuilder.append("</PHOTO>");
            }


            xbuilder.append("</vCard>");

            // The following are Not supported by xmpp vcard.
            /*
             * <contact-information> <assistantName>adfafd</assistantName>
             * <location>afaf</location> <assistantPhoneNumber>afdadf</assistantPhoneNumber>
             * <imDisplayName>201_IM_test</imDisplayName> <alternateImId>afa</alternateImId>
             * <alternateEmailAddress>afafd</alternateEmailAddress>
             * </contact-information>-bash-3.2$
             */

            Log.debug("buildcontactinfo is " + xbuilder.toString());
            Document vcardDoc = sreader.read(new StringReader(xbuilder.toString()));
            Element vcardNode = vcardDoc.getRootElement();

            return vcardNode;
        }

        catch (Exception ex) {
            Log.error(ex.getMessage());
            return null;
        }

    }

    public static String getNodeText(Element element, String nodeName) {
        Node node = element.selectSingleNode(nodeName);
        if (node != null) {
            return node.getText();
        }

        return "unknown";
    }

    public static String getTextFromNodes(Element element, String nameNode, String criteriaNode, String valueNode) {
        List nlist = element.selectNodes(nameNode);
        for (int i = 0; i < nlist.size(); i++) {
            Element el = (Element) (nlist.get(i));
            Node cNode = el.selectSingleNode(criteriaNode);
            if (cNode != null) {
                Node vNode = el.selectSingleNode(valueNode);
                if (vNode != null) {
                    return vNode.getText();
                }
            }

        }

        return "unknown";
    }

    public static Document styleDocument(Document document, InputStream stylesheet) throws Exception {

        // load the transformer using JAXP
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(new StreamSource(stylesheet));

        // now lets style the given document
        DocumentSource source = new DocumentSource(document);
        DocumentResult result = new DocumentResult();
        transformer.transform(source, result);

        // return the transformed document
        Document transformedDoc = result.getDocument();
        return transformedDoc;
    }

    public static String getEncodedAvatar(String avatarURL)
    {

        try
        {
            Log.debug("call Avatar URL "+ avatarURL);

            URL serverURL = new URL(avatarURL);
            return getPngString(serverURL);
        }

        catch (IOException ex) {
            Log.error("In getEncodedAvatar IOException " + ex.getMessage());
            return null;
        }

        catch (Exception ex) {
            Log.error("In getEncodedAvatar Exception " + ex.getMessage());
            return null;
        }
    }


    public static String getPngString(URL url)
    {
        try
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            BufferedImage image = ImageIO.read(url);
            ImageIO.write(image,"png",os);

            return new String(new Base64().encode(os.toByteArray()));
        }
        catch(IOException e)
        {
            Log.error("In getPngString, error:"+e.getMessage());
            return null;
        }
        catch(Exception e)
        {
            Log.error(e.getMessage());
            return null;
        }
    }

}
