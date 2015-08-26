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
package de.tudresden.gis.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.tudresden.gis.manage.http.HttpHelperMethods;
import de.tudresden.gis.manage.xml.XmlHelpMethods;

/**
 * Context Listener to create dropdown input for keywords, platforms and
 * container types and rating table creation (used on appstore's welcom page)
 * 
 * Be aware of the reload issue - tomcat reloads if a file changes
 * (reloadable=true) - therefore time stamp check is needed, default=30 seconds
 * 
 * @author Bernd Grafe
 * 
 */
public class StartController implements ServletContextListener {
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// shutdown code
	}

	/**
	 * initialized process to check/create rating table in database and to
	 * change xml documents
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// server start code
		// check if rating table exists - if it does not exist, fill it
		try {
			if (!HttpHelperMethods.checkDb4RatingTable()) {
				System.out.println("StartController: rating table does not exists - will be created and filled");
				// false -> create rating table
				HttpHelperMethods.createRatingTable();
				// fill rating table
				HttpHelperMethods.fillRatingTable();
			}
		
			//check if login table exists and create test user "testuser"- pw "testuser"
			if(!HttpHelperMethods.checkDb4LoginTable()){
				HttpHelperMethods.createLogingTable();
				System.out.println("StartController: login table does not exists - will be created and filled");
				System.out.println("try created test user with 'testuser'/'testuser'");
			}
			
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		// change entries for dropdown: keywords, platform and container
		int seconds = 30; // change this, if the server reloads more than twice
							// - not used if reloadable is false in server.xml
		// ignore Lists:
		List<String> ignoreContainerList = new ArrayList<String>();
		ignoreContainerList.add("Container type");
		List<String> ignorePlatformList = new ArrayList<String>();
		ignorePlatformList.add("Platforms and Runtime Components");
		ignorePlatformList.add("Runtime Component");
		ignorePlatformList.add("Platform");
		List<String> ignoreKeywordList = new ArrayList<String>();
		// change gpt/gxe/mcp/schema/dynamic/keywords.xml based on
		// gpt/search/browse/browse-catalog.xml
		try {
			String path2BrowseCatalog = XmlHelpMethods
					.getPath("gpt/search/browse/browse-catalog.xml");
			String path2Keywords = XmlHelpMethods
					.getPath("gpt/gxe/mcp/schema/dynamic/keywords.xml");
			Document docSource, docTarget;
			docSource = XmlHelpMethods.createDocumentPath(path2BrowseCatalog);
			docTarget = XmlHelpMethods.createDocumentPath(path2Keywords);
			// TODO: change xml if last change was 30 seconds ago - why?
			// geoportal does a restart after every file change
			// TODO: auto server restart because of file/context changes?
			// calling code from InitializationServlet causes restart too
			int timeDiff = XmlHelpMethods.checkDateDifference(docTarget);
			if (timeDiff > seconds && timeDiff != 0) {
				docTarget = XmlHelpMethods.addNewElements(docSource, docTarget,
						"name", "g:options", "g:option", ignoreKeywordList);
				if (XmlHelpMethods.writeFile(path2Keywords,
						XmlHelpMethods.doc2String(docTarget)))
					System.out.println("StartController: keyword list changed - if you see this message more than twice in a row, stop the server and change seconds in de/tudresden/gis/controller/StartController.java");
			} else { }
		} catch (ParserConfigurationException | SAXException | IOException
				| ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// change gpt/gxe/mcp/schema/dynamic/platform.xml based on
		// gpt/search/browse/platforms.xml
		try {
			String path2Platform = XmlHelpMethods
					.getPath("gpt/search/browse/platforms.xml");
			String path2TargetPlatform = XmlHelpMethods
					.getPath("gpt/gxe/mcp/schema/dynamic/platform.xml");
			Document docSource, docTarget;
			docSource = XmlHelpMethods.createDocumentPath(path2Platform);
			docTarget = XmlHelpMethods.createDocumentPath(path2TargetPlatform);
			// TODO: change xml if last change was 30 seconds ago - why?
			// geoportal does a restart after every file change
			// TODO: auto server restart because of file/context changes?
			// calling code from InitializationServlet causes restart too
			int timeDiff = XmlHelpMethods.checkDateDifference(docTarget);
			if (timeDiff > seconds && timeDiff != 0) {
				docTarget = XmlHelpMethods.addNewElements(docSource, docTarget,
						"name", "g:options", "g:option", ignorePlatformList);
				if (XmlHelpMethods.writeFile(path2TargetPlatform,
						XmlHelpMethods.doc2String(docTarget))); 
			} else { }
		} catch (ParserConfigurationException | SAXException | IOException
				| ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// change gpt/gxe/mcp/schema/dynamic/container.xml based on
		// gpt/search/browse/containers.xml
		try {
			String path2Container = XmlHelpMethods
					.getPath("gpt/search/browse/containers.xml");
			String path2TargetContainer = XmlHelpMethods
					.getPath("gpt/gxe/mcp/schema/dynamic/container.xml");
			Document docSource, docTarget;
			docSource = XmlHelpMethods.createDocumentPath(path2Container);
			docTarget = XmlHelpMethods.createDocumentPath(path2TargetContainer);
			// TODO: change xml if last change was 30 seconds ago - why?
			// geoportal does a restart after every file change
			// TODO: auto server restart because of file/context changes?
			// calling code from InitializationServlet causes restart too
			int timeDiff = XmlHelpMethods.checkDateDifference(docTarget);
			if (timeDiff > seconds && timeDiff != 0) {
				docTarget = XmlHelpMethods.addNewElements(docSource, docTarget,
						"name", "g:options", "g:option", ignoreContainerList);
				if (XmlHelpMethods.writeFile(path2TargetContainer,
						XmlHelpMethods.doc2String(docTarget))); 
			} else { }
		} catch (ParserConfigurationException | SAXException | IOException
				| ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
