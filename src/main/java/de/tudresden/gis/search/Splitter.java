/**
 * ﻿Copyright (C) 2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package de.tudresden.gis.search;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class provides functionality to split a text into abstract, platform and container type to use separately 
 * in the search result.
 * 
 * @author Jochen Lenz
 *
 */
public class Splitter {

	/**
	 * This method extracts and returns the abstract. 
	 * @see CSW_2.0.2_OGCCORE_ESRI_GPT_GetRecords_Response.xslt
	 * 
	 * @param abstractText consists of abstract, container type, platform information and two 'split-words'
	 * @return abstract text without platform and container type; "not defined" in case of empty abstract 
	 */
	public String getAbstract(String abstractText) { 
		//abstractText = abstractText.replaceAll("\\s+",""); 
		String[] parts = abstractText.split("The containertype of the described process is "); 
		 
		if (parts[0].length() == 0)
			return "not defined";
		else 
			return parts[0]; 
	}

	/**
	 * This Methods returns the value of an xml element with defined tag name from a group of elements.
	 * 
	 * @param tag defines the tag name of the element 
	 * @param element is an xml element with possibly some child nodes
	 * @return value of xml element with definded tag name
	 */
    private static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }
	
	/**
	 * This method extracts and returns the container type.
	 * Returns string from gpt.search.browse.containers.xml or text between the two defined 'split-words'. 
	 * @see gpt.search.profiles CSW_2.0.2_OGCCORE_ESRI_GPT_GetRecords_Response.xslt
	 * 
	 * @param abstractText consists of abstract, container type, platform information and two 'split-words'
	 * @param get_icon_url boolean, defines type of returned text. Set to true returns icon url, if false returns 
	 * 		   icon description  
	 * @return url for icon or container type. If container type is empty returns "not defined".
	 * 		   If container type is not in containers.xml returns "unknown".
	 */
	public String getContainertype(String abstractText, boolean get_icon_url) { 
		abstractText = abstractText.replaceAll("\\s+", "");		
        String result = "empty";
		String[] parts = abstractText.split("Thecontainertypeofthedescribedprocessis");
		String[] parts2 = parts[1].split("Theplatformofthedescribedprocessis"); 
		if (parts2.length < 1) {
			if (get_icon_url)
				result = "/catalog/images/mcp/icons/notDefined_small.png";				
			else 
				result = "not defined"; 		
		} else {
			// Returns "not defined" if first part of second split is empty.
			if (parts2[0].length() == 0) {
				if (get_icon_url) {
					result = "/catalog/images/mcp/icons/notDefined_small.png";
				} else {
					result = "not defined";
				}
			// If first part of second split is not empty, compares it with different types and returns type.
			// If no type matches, "unknown containertype." is returned.
			} else {
	            try {
	                String path = Thread.currentThread().getContextClassLoader().getResource("/gpt/search/browse/containers.xml").getPath();
	                File stocks = new File(path);
	                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	                Document doc = dBuilder.parse(stocks);
	                doc.getDocumentElement().normalize();
	                
	                NodeList nodes = doc.getElementsByTagName("item");
	                for (int i = 0; i < nodes.getLength(); i++) {
	                    Node node = nodes.item(i);
	                    if (node.getNodeType() == Node.ELEMENT_NODE) {
	                        Element element = (Element) node;
	                        String name = getValue("name", element);
	                        String name_replaced = name.replaceAll("\\s+","");
	                        if (name_replaced.equals(parts2[0])) {
                                result = name;
	                            break;
	                        } else {
	                            result = parts2[0];
	                        }
	                    }
	                }
	            } catch (Exception ex) {
	                ex.printStackTrace();
	                result = "error";
	            }
				if (get_icon_url){
                    if (parts2[0].toLowerCase().contains("java")) {
                        result = "/catalog/images/mcp/icons/java_small.png";
                    } else if (parts2[0].toLowerCase().contains("python")) {
                        result = "/catalog/images/mcp/icons/python_small.png";
                    } else if(parts2[0].toLowerCase().contains("arc")) {
                        result = "/catalog/images/mcp/icons/arctoolbox_small.png";
                    } else if(parts2[0].toLowerCase().contains("rscript")) {
                        result = "/catalog/images/mcp/icons/r_small.png";
                    } else {
                        result = "/catalog/images/mcp/icons/no_picture_small.png";
                    }
				}
			}
		}
        if (result == getPlatform(abstractText, true)){
        	result = "/catalog/images/mcp/icons/clear_small.png";
        }		
		return result;
	}

	
	/**
	 * This method extracts and returns the platform.
	 * Returns string from gpt.search.browse.platforms.xml or text after the second 'split-word'. 
	 * 
	 * @see gpt.search.profiles CSW_2.0.2_OGCCORE_ESRI_GPT_GetRecords_Response.xslt
	 * 
	 * @param abstractText consists of abstract, container type, platform information and two 'split-words'
	 * @param icon defines type of returned text. Set to true returns icon url, if false returns icon description  
	 * @return url for icon or platform description. If platform is empty returns "not defined".
	 * 		   If platform is not in platforms.xml returns "unknown".
	 */
    public String getPlatform(String abstractText, boolean get_icon_url) {
        abstractText = abstractText.replaceAll("\\s+","");        
        String result = "empty";
        String[] parts = abstractText.split("Theplatformofthedescribedprocessis");
        // If there is only one part after split then there is no part with platform value.
        // Returns "not defined".
        if (parts.length == 1){
            if (get_icon_url) {
                result = "/catalog/images/mcp/icons/notDefined_small.png";
            } else {
                result = "not defined";
            }
        // If second part exists compares it with different platform and returns platform.
        // If no platform matches returns "unknown platform".
        } else {
            try {
                String path = Thread.currentThread().getContextClassLoader().getResource("/gpt/search/browse/platforms.xml").getPath();
                File stocks = new File(path);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(stocks);
                doc.getDocumentElement().normalize();
                
                NodeList nodes = doc.getElementsByTagName("item");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String id = getValue("id", element);
                        String name = getValue("name", element);
                        String name_replaced = name.replaceAll("\\s+","");
                        if (name_replaced.equals(parts[1])) {
                            result = name;
                        } else {
                            result = parts[1];
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                result = "error";
            }
            if (get_icon_url){
                if (parts[1].toLowerCase().contains("java")) {
                    result = "/catalog/images/mcp/icons/java_small.png";
                } else if (parts[1].toLowerCase().contains("python")) {
                    result = "/catalog/images/mcp/icons/python_small.png";
                } else if(parts[1].toLowerCase().contains("numpy-")) {
                    result = "/catalog/images/mcp/icons/platform/numphy_small.png";
                } else if(parts[1].toLowerCase().contains("arc")) {
                    result = "/catalog/images/mcp/icons/arcgis_small.png";
                } else if(parts[1].toLowerCase().contains("gdal")) {
                    result = "/catalog/images/mcp/icons/gdal_small.png";
                } else if(parts[1].toLowerCase().contains("gdal-python-")) {
                    result = "/catalog/images/mcp/icons/platform/gdal_Python_small.png";
                } else {
                    result = "/catalog/images/mcp/icons/no_picture_small.png";
                }
            }
        }
        return result;
    }
}
