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
package de.tudresden.gis.manage.xml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class contains static methods for xml related operation, reading, saving changing (dynamic change of platform, container and keywwords)
 * 
 * @author Bernd Grafe 
 */
public class XmlHelpMethods {
	public static  String[] str;

	/**
	 * Method to read a file.
	 *  
	 * @param path of file
	 * @param threaded if true thread reading is enabled
	 * @return file content as string
	 * @throws IOException
	 */
	public static String readFile (String path, Boolean threaded) throws IOException {
		if (threaded) return readFileThreaded(path, 4);
		else return readFileNormal(path);
	}
	
	/**
	 * Method to read a file based on path with buffered reader
	 * 
	 * @param path of file
	 * @return file content as string
	 */
	private static String readFileNormal(String path) {
		String sCurrentLine;
		String fileString = "";
		BufferedReader bReader = null;
		try {
			FileReader fReader = new FileReader(path);
			bReader = new BufferedReader(fReader, 16000);
			while ((sCurrentLine = bReader.readLine()) != null) {
				fileString += sCurrentLine;
			} 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bReader != null) bReader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return fileString;	
	}
	
	/**
	 * This method implements faster file reading with threads and nio.files
	 * 
	 * @param file path to file
	 * @param threads number of threads
	 * @return content of file
	 * @throws IOException
	 */
	private static String readFileThreaded(String file, int threads) throws IOException {
		String fileString = "";
		Path path = Paths.get(file);
		List<String> text = Files.readAllLines(path, StandardCharsets.UTF_8);
		//number of threads
		int threadNumber = threads;
		Thread[] array= new Thread[threadNumber];
		int size = text.size();
		str = new String [threadNumber];
		int d = size/threadNumber;
		for (int i = 0; i < threadNumber; i++) {
			int min = d * i;
			int max = d * (i + 1); if (i == threadNumber - 1) max = size;
			Thread a = new Thread(new threadReader(i,text,min,max));
			a.start();
			array[i] = a;
		}
		for (Thread is:array) {
			try {
				is.join();
			} catch (InterruptedException e) { 
				is.interrupt();
			}
		}
		for (String jsonPart : str) {
			fileString += jsonPart;
		}
		return fileString;	
	}
	
	/**
	 * This method writes a file based on xml string
	 * @param path
	 * @param content (xml)
	 * @return true if successfully written
	 * @throws IOException
	 */
	public static Boolean writeFile(String path, String content) throws IOException {
		//read file
		OutputStream out = null;
		InputStream filecontent = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		try {
			//initiate output stream
			out = new FileOutputStream(new File(path));
			int read = 0;
			final byte[] bytes = new byte[1024];
			while ((read = filecontent.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}	
		} catch(Exception e) {
			return false;
		}	
		finally { 
			if (out != null) 
				out.close(); 
			if (filecontent != null) 
				filecontent.close(); 
		}
		return true;
	}
	
	/**
	 * Method to create a document based on xml string.
	 * 
	 * @param content (xml)
	 * @return Document file for given xml content
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document createDocumentString (String content) throws SAXException, IOException, ParserConfigurationException{
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true);
		Document doc = fac.newDocumentBuilder().parse(new InputSource(new StringReader(content)));
		return doc;
	}
	
	/**
	 * Method to create a document based on file path.
	 * 
	 * @param path xml file path
	 * @return Document for given xml file
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document createDocumentPath (String path) throws ParserConfigurationException, SAXException, IOException{
		File fXmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        return doc;
	}
	/**
	 * Method to correct a path. Splits last slash.
	 * 
	 * @param path
	 * @return corrected path
	 */
	private static String checkPath (String path){
		if(path.startsWith("/")){
			path= path.substring(1, path.length());
		}
		return path;
	}
	
	/**
	 * Method to get absolute path based on relative path from src folder.
	 * 
	 * @param resource - relative path
	 * @return absolute path
	 */
	public static String getPath (String resource) {
		String path = Thread.currentThread().getContextClassLoader().getResource(resource).getPath(); //relative path based on src folder, example resource= gpt/search/browse/browse-catalog.xml 
		return checkPath(path);
	}
	
	/**
	 * Method to delete an element based on tag name .
	 * @param doc
	 * @param tagName
	 * @return
	 */
	private static Document deleteElement(Document doc, String tagName) {
		//only 2 of 3 were deleted ?
	    NodeList nodes = doc.getElementsByTagName(tagName);
	    for (int i = 0; i < nodes.getLength(); i++) {
	      Element e = (Element)nodes.item(i);
	      e.getParentNode().removeChild(e);
	    }
	    return doc;
	  }
	
	/**
	 * Method to remove all children of g:options - element
	 * 
	 * @param doc document with children  g:options
	 * @return document without children g:options
	 */
	public static Document removeAllOptions(Document doc) {
		NodeList nodes = doc.getElementsByTagName("g:options");
		Node node = nodes.item(0);
		while (node.hasChildNodes())
	        node.removeChild(node.getFirstChild());
		return doc;
	}
	
	/**
	 * Method to delete old elements and add new elements in target file based on tag name of source file.
	 *
	 * @param source
	 * @param target
	 * @param sourceTagName
	 * @param targetTagNameParent
	 * @param targetTagNameChild
	 * @return document
	 * @throws ParseException
	 */
	public static Document addNewElements(Document source, Document target, String sourceTagName, String targetTagNameParent, String targetTagNameChild, List<String> ignoreList) throws ParseException{ //list for attributes to be more dynamic if reuse
		//remove old entries 
		target = removeAllOptions(target);
		// add new entries
		NodeList nameList = source.getElementsByTagName(sourceTagName);
		NodeList parents = target.getElementsByTagName(targetTagNameParent);
		Element parent = (Element) parents.item(0); // g:options - only 1
													// element
		//add space placeholder as first entry
		Element o = target.createElement(targetTagNameChild);
		o.setAttribute("g:value", "");
		o.setAttribute("g:label", "");
		parent.appendChild(o);
		
		for (int i = 0; i < nameList.getLength(); i++) {
			String value = nameList.item(i).getTextContent();
			if (value != null && !value.equals("") && !ignoreList.contains(value)) {
				Element p = target.createElement(targetTagNameChild);
				p.setAttribute("g:value", value);
				p.setAttribute("g:label", value);
				parent.appendChild(p);
			}
		}
		return changeDate(target);
	}
	
	/**
	 * Method to convert document to string.
	 * 
	 * @param doc
	 * @return document content as string
	 */
	public static String doc2String(Document doc) {
	    try {
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        return sw.toString();
	    } catch (Exception ex) {
	        throw new RuntimeException("Error converting to String", ex);
	    }
	}
	
	/**
	 * Method to check difference between actual time and saved time in xml file.
	 * 
	 * @param target document
	 * @return time difference as integer
	 * @throws ParseException
	 */
	public static int checkDateDifference (Document target) throws ParseException{
		int diff = 0;
		NodeList parents = target.getElementsByTagName("g:options");
		Element parent = (Element) parents.item(0); //g:options - only 1 element
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
		//date xml
		String time = parent.getAttribute("time");
		Date oldT =  format.parse(time);
		//date now
		Date newT = new Date();
		//compare
		diff = (int) ((newT.getTime() - oldT.getTime()) / 1000); 
		return diff;
	}
	
	/**
	 * Method to change update date in xml files - used for reloadable issue and auto change on start up.
	 * 
	 * @param target document
	 * @return updated document
	 * @throws ParseException
	 */
	private static Document changeDate (Document target) throws ParseException {
		NodeList parents = target.getElementsByTagName("g:options");
		Element parent = (Element) parents.item(0); //g:options - only 1 element
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
		Date newT = new Date();
		String time = format.format(newT);
		parent.setAttribute("time", time);
		return target;
	}
}
