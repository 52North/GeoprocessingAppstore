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
package de.tudresden.gis.manage.http;
import java.io.IOException;
import java.sql.Connection; 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.grouping.term.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.sql.ConnectionBroker;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.server.assertion.AsnConfig;
import com.esri.gpt.server.assertion.components.AsnConstants;
import com.esri.gpt.server.assertion.components.AsnOperation;
import com.esri.gpt.server.assertion.exception.AsnInvalidOperationException;
import com.esri.gpt.server.assertion.index.AsnIndexAdapter;

import de.tudresden.gis.manage.xml.XmlHelpMethods;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
/**
 * This class contains static methods related to ratings and frontend view for recent and top rated entries.
 * 
 * @author Bernd Grafe, Christin Henzen
 *
 */
public class HttpHelperMethods {

	/**
	 * This method returns number of up ratings based on id.
	 * 
	 * @param id
	 * @return
	 */
	public static int getUpRating(String id){
		return getRating(id,true);
	}
	/**
	 * This method returns number of down ratings based on id.
	 * 
	 * @param id
	 * @return
	 */
	public static int getDownRating(String id){
		return getRating(id,false);
	}
	
	/**
	 * This method returns hits of up or down rating for given id.
	 * 
	 * @param id
	 * @param up
	 * @return
	 */
	private static int getRating(String id, boolean up) {
		if (id.contains("{")) id = id.replace("{", "");
		if (id.contains("}")) id = id.replace("}", "");
		// set parameter
		String subject, predicate, value, valueField, queryPredicate;
		subject = "urn:esri:geoportal:resourceid:{*}";
		predicate = "urn:esri:geoportal:rating";
		value = "urn:esri:geoportal:rating:value:*";
		valueField = "rdf.rating.value";
		queryPredicate = "urn:esri:geoportal:rating:query";
		
		// set actual id and up/down predicate
		subject = subject.replace("*", id); // C88820AA-A071-4F3D-BCEB-6BAF39DB1635
		if (up)
			value = value.replace("*", "up");
		else
			value = value.replace("*", "down");
		// create classes for lucene search
		IndexSearcher searcher = null;
		IndexReader reader = null;
		AsnOperation operation = null;
		AsnIndexAdapter indexAdapter = null;
		AsnConfig config = new AsnConfig();
		config.configure();
		// set Operation for IndexSearcher
		try {
			operation = config.getOperations().makeOperation(subject, queryPredicate); // create Operation (query) with given ID
			indexAdapter = operation.getIndexReference().makeIndexAdapterWithoutContext();
			reader = indexAdapter.makeIndexReader();
			searcher = new IndexSearcher(reader);
		} catch (AsnInvalidOperationException | ClassNotFoundException
				| InstantiationException | IllegalAccessException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// set query
		BooleanQuery query = new BooleanQuery();
		Query qSubject = new TermQuery(new Term(AsnConstants.FIELD_RDF_SUBJECT, subject));
		Query qPredicate = new TermQuery(new Term(AsnConstants.FIELD_RDF_PREDICATE, predicate));
		Query qValue = new TermQuery(new Term(valueField, value));
		query.add(qSubject, BooleanClause.Occur.MUST);
		query.add(qPredicate, BooleanClause.Occur.MUST);
		query.add(qValue, BooleanClause.Occur.MUST);
		// execute query and get hits
		int hits = 0;
		TopDocs topDocs;
		try {
			topDocs = searcher.search(query, 1);
			hits = topDocs.totalHits;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return hits;
	}
	
	/**
	 * This method returns a certain entry as json.
	 * 
	 * @param id
	 * @return entry
	 * @throws SQLException 
	 */
	public static String getEntryWithID(String id) throws SQLException {
		System.out.println("HttpHelperMethod requested id: " + id);
		String json = "";
		// sql request
		Connection con = null;
		CatalogConfiguration catConf = new CatalogConfiguration();
		String sTable = catConf.getResourceTableName();
		String sDataTable = catConf.getResourceDataTableName();
		String sUserTable = catConf.getUserTableName();
		try {
			ConnectionBroker connBroker = new ConnectionBroker(); 
			ManagedConnection mc = connBroker.returnConnection("");
			con = mc.getJdbcConnection();
			Statement stmt = con.createStatement();
			String sql = "SELECT res.docuuid, title, username, updatedate, xml FROM "
					+ sTable + " res, " + sUserTable + " us, " + sDataTable
					+ " data WHERE res.approvalstatus='approved' AND res.id=data.id AND res.owner=us.userid AND res.docuuid='" + id + "'";
			
			System.out.println("HttpHelperMethod sql");
			System.out.println(sql);
			
			ResultSet rs = stmt.executeQuery(sql);
			int pos = 0;

			JSONArray array = new JSONArray();
			while (rs.next()) {
				pos++;
				// create json
				JSONObject doc = new JSONObject();
				doc.put("no", pos);
				doc.put("id", rs.getString("docuuid"));
				doc.put("title", rs.getString("title"));
				doc.put("user", rs.getString("username"));
				doc.put("date", rs.getString("updatedate"));
				doc.put("abstract", getXmlAbstract(rs.getString("xml")));
				doc.put("container", getXmlContainer(rs.getString("xml")));
				doc.put("platform", getXmlPlatform(rs.getString("xml")));
				array.put(doc);
			}
			json = array.toString();
			con.close();
		} catch (SQLException | JSONException ex) {
			System.out.println("HttpHelperMethod:" + ex.getMessage()); 
		} finally {
			if (con != null)
				con.close();
		}
		return json;
	}
	
	/**
	 * This method returns json string for x most recent entries.
	 * 
	 * @param count number of entries
	 * @return json string
	 * @throws SQLException 
	 */
	public static String getRecent(int count) throws SQLException {
		String json = "";
		// sql request
		Connection con = null;
		CatalogConfiguration catConf = new CatalogConfiguration();
		String sTable = catConf.getResourceTableName();
		String sDataTable = catConf.getResourceDataTableName();
		String sUserTable = catConf.getUserTableName();
		try {
			ConnectionBroker connBroker = new ConnectionBroker(); // get connection via connectionBroker
			ManagedConnection mc = connBroker.returnConnection("");
			con = mc.getJdbcConnection();
			Statement stmt = con.createStatement();
			String sql = "SELECT res.docuuid, title, username, updatedate, xml FROM "
					+ sTable + " res, " + sUserTable + " us, " + sDataTable
					+ " data WHERE res.approvalstatus='approved' AND res.id=data.id AND res.owner=us.userid ORDER BY updatedate DESC LIMIT "
					+ count;
			ResultSet rs = stmt.executeQuery(sql);
			int pos = 0;

			JSONArray array = new JSONArray();
			while (rs.next()) {
				pos++;
				// create json
				JSONObject doc = new JSONObject();
				doc.put("no", pos);
				doc.put("id", rs.getString("docuuid"));
				doc.put("title", rs.getString("title"));
				doc.put("user", rs.getString("username"));
				doc.put("date", rs.getString("updatedate"));
				doc.put("abstract", getXmlAbstract(rs.getString("xml")));
				doc.put("container", getXmlContainer(rs.getString("xml")));
				doc.put("platform", getXmlPlatform(rs.getString("xml")));
				array.put(doc);
			}
			json = array.toString();
		} catch (SQLException | JSONException ex) {
			System.out.println("HttpHelperMethod:" + ex.getMessage());
		} finally {
			if (con != null)
				con.close();
		}
		return json;
	}
	
	/**
	 * This method extracts abstract of xml string.
	 * @param xml
	 * @return abstract
	 */
	private static String getXmlAbstract(String xml) {
		String abstr ="";
		try {
			org.w3c.dom.Document doc = XmlHelpMethods.createDocumentString(xml);
			NodeList nodes = doc.getElementsByTagName("ows:Abstract"); //first abstract = process in general
			abstr = nodes.item(0).getTextContent();
		} catch (SAXException | IOException | ParserConfigurationException | NullPointerException  e) {
			abstr = "no description";
		}
		return abstr;
	}
	
	/**
	 * This method extracts container of xml string.
	 * @param xml
	 * @return container
	 */
	private static String getXmlContainer(String xml) {
		String container ="";
		try {
			org.w3c.dom.Document doc = XmlHelpMethods.createDocumentString(xml);
			NodeList nodes = doc.getElementsByTagNameNS("*", "containerType"); 
			container = nodes.item(0).getTextContent();
		} catch (SAXException | IOException | ParserConfigurationException | NullPointerException  e) {
			container = "";
		}
		return container;
	}
	
	/**
	 * This method extracts platform of xml string.
	 * @param xml
	 * @return platform
	 */
	private static String getXmlPlatform(String xml) {
		String platform ="";
		try {
			org.w3c.dom.Document doc = XmlHelpMethods.createDocumentString(xml);
			platform = doc.getElementsByTagNameNS("*", "platform").item(0).getAttributes().item(0).getTextContent(); 
		} catch (SAXException | IOException | ParserConfigurationException | NullPointerException  e) {
			platform = "";
		}
		return platform;
	}
	
	/**
	 * This method is used to save rating delta (up-down) in db for top-rated-search.
	 * 
	 * @param id
	 * @throws SQLException 
	 */
	public static void updateDbRatings(String id, Boolean isID) throws SQLException {
		// called from asnrequesthandler
		String[] temp = id.split(":");
		id = temp[temp.length - 1];
		if (!isID)
			id = getID(id);
		// get up/down delta
		int delta = getUpRating(id) - getDownRating(id);
		// save delta in db
		Connection con = null;
		CatalogConfiguration catConf = new CatalogConfiguration();
		String deltaTable = catConf.getTablePrefix() + "RATING";
		try {
			ConnectionBroker connBroker = new ConnectionBroker(); // get connection via connectionBroker
			ManagedConnection mc = connBroker.returnConnection("");
			con = mc.getJdbcConnection();
			Statement stmt = con.createStatement();
			String sql = "UPDATE " + deltaTable + " SET delta=" + delta
					+ " WHERE docuuid='" + id + "';  INSERT INTO " + deltaTable
					+ " (docuuid,delta) SELECT '" + id + "'," + delta
					+ " WHERE NOT EXISTS ( SELECT docuuid FROM " + deltaTable
					+ " WHERE docuuid = '" + id + "');"; // update if exists
															// otherwise insert
															// new values
			stmt.executeUpdate(sql);
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			if (con != null)
				con.close();
		}
	}
	
	/**
	 * This method returns real id based on assertion no.
	 * 
	 * @param assertion
	 * @return id
	 */
	public static String getID(String assertion) {
		String id = "";
		// set parameter
		String subject, queryPredicate;
		subject = "urn:esri:geoportal:resourceid:{*}";
		queryPredicate = "urn:esri:geoportal:rating:query";
		
		// create classes for lucene search
		IndexSearcher searcher = null;
		IndexReader reader = null;
		AsnOperation operation = null;
		AsnIndexAdapter indexAdapter = null;
		AsnConfig config = new AsnConfig();
		config.configure();
		
		// set Operation for IndexSearcher
		try {
			operation = config.getOperations().makeOperation(subject, queryPredicate); // create Operation (query) with given ID
			indexAdapter = operation.getIndexReference().makeIndexAdapterWithoutContext();
			reader = indexAdapter.makeIndexReader();
			searcher = new IndexSearcher(reader);
		} catch (AsnInvalidOperationException | ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// set query
		BooleanQuery query = new BooleanQuery();
		Query qSubject = new TermQuery(new Term("sys.assertionid", assertion));
		query.add(qSubject, BooleanClause.Occur.MUST);
		TopDocs topDocs;
		try {
			topDocs = searcher.search(query, 1);
			for (int i = 0; i < topDocs.scoreDocs.length; i++) {
				int a = topDocs.scoreDocs[i].doc; // gets doc id of lucene index
				Document doc = reader.document(a);
				id = doc.get("sys.resourceid"); // gets document
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return id;
	}
	
	/**
	 * This method checks for new rating table.
	 * 
	 * @return true, if rating table exists. false, if not.
	 * @throws SQLException 
	 */
	public static boolean checkDb4RatingTable() throws SQLException {
		boolean check = false;
		Connection con = null;
		CatalogConfiguration catConf = new CatalogConfiguration();
		String deltaTable = catConf.getTablePrefix() + "RATING";		
		try {
			ConnectionBroker connBroker = new ConnectionBroker();   //get connection via connectionBroker
		    ManagedConnection mc = connBroker.returnConnection("");
		    con = mc.getJdbcConnection();
		    String sql = "SELECT EXISTS(SELECT * FROM information_schema.tables WHERE UPPER (table_name) = UPPER('"+deltaTable+"'));";
		    Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery(sql);
		    while (rs.next()) {
		    	check = rs.getBoolean(1);
		    }
		} catch (SQLException ex) {
			System.out.println("TABLE CHECK ERROR "+ex.getMessage());	
		} finally {
			if (con != null)
				con.close();
		}
		
		return check;
	}
	
	/**
	 * This method creates new rating table.
	 * @throws SQLException 
	 */
	public static void createRatingTable() throws SQLException {
		Connection con = null;
		CatalogConfiguration catConf = new CatalogConfiguration();
		String deltaTable = catConf.getTablePrefix() + "RATING";
		String sql = "CREATE TABLE "+deltaTable+" (docuuid character varying(38) NOT NULL,delta smallint NOT NULL,CONSTRAINT "+deltaTable+"_pk PRIMARY KEY (docuuid));";
		try {
			ConnectionBroker connBroker = new ConnectionBroker();   //get connection via connectionBroker
		    ManagedConnection mc = connBroker.returnConnection("");
		    con = mc.getJdbcConnection();
		    Statement stmt = con.createStatement();
		    stmt.executeUpdate(sql);
		} catch (SQLException ex) {
			System.out.println("TABLE CREATION ERROR "+ex.getMessage());	
		} finally {
			if (con != null)
				con.close();
		}
	}
	
	/**
	 * This method is used to get all ratings and inserts results in rating table.
	 * It is only necessary, if table is created for the first time.
	 * @throws SQLException 
	 */
	public static void fillRatingTable() throws SQLException {
		//get all IDs
		Connection con = null;
		ArrayList<String> ids = new ArrayList<String>();
		CatalogConfiguration catConf = new CatalogConfiguration();
		String resTable = catConf.getResourceTableName();
		String deltaTable = catConf.getTablePrefix()+"RATING";
		String sql = "SELECT docuuid FROM "+resTable+";";
		try {
			ConnectionBroker connBroker = new ConnectionBroker();   //get connection via connectionBroker
		    ManagedConnection mc = connBroker.returnConnection("");
		    con = mc.getJdbcConnection();
		    Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery(sql);
		    while (rs.next()) {
		    	String id = rs.getString("docuuid");
		    	ids.add(id);
		    }
			//get all deltas
			ArrayList<Integer> deltas = new ArrayList<Integer>();
			for (String id : ids) {
				int delta = getUpRating(id) - getDownRating(id);
				deltas.add(delta);
			}
			//save all ids and deltas in rating table
			for (int i = 0; i < ids.size(); i++) {
				String insertSQL =" INSERT INTO "+deltaTable+" VALUES ('"+ids.get(i)+"',"+deltas.get(i)+");";
			    stmt.executeUpdate(insertSQL);
			} 
		} catch (SQLException ex) {
			System.out.println("FILL ERROR "+ex.getMessage());	
		} finally {
			if (con != null)
				con.close();
		}

	}
	
	/**
	 * This method returns json string for x top rated entries.
	 * 
	 * @param count - number of entries
	 * @return json string
	 * @throws SQLException 
	 */
	public static String getTopRated (int count) throws SQLException {
		String json = "";
		//sql request
		Connection con = null;
		CatalogConfiguration catConf = new CatalogConfiguration();
		String sTable = catConf.getResourceTableName();
	    String sDataTable = catConf.getResourceDataTableName();
	    String sUserTable = catConf.getUserTableName();
	    String sRatingTable = catConf.getTablePrefix()+"RATING";
		try {
			ConnectionBroker connBroker = new ConnectionBroker(); // get connection via connectionBroker
			ManagedConnection mc = connBroker.returnConnection("");
			con = mc.getJdbcConnection();
			Statement stmt = con.createStatement();
			String sql = "SELECT res.docuuid, title, username, updatedate, xml, delta FROM "
					+ sTable + " res, " + sUserTable + " us, " + sDataTable + " data, " + sRatingTable
					+ " rate WHERE res.approvalstatus='approved' AND res.id=data.id AND res.owner=us.userid AND res.docuuid=rate.docuuid ORDER BY delta DESC LIMIT "
					+ count;
			ResultSet rs = stmt.executeQuery(sql);
			int pos = 0; 
			JSONArray array = new JSONArray();
			while (rs.next()) {
				pos++;
				// create json
				JSONObject doc = new JSONObject();
				doc.put("no", pos);
				String id = rs.getString("docuuid");
				doc.put("id", id);
				doc.put("title", rs.getString("title"));
				doc.put("user", rs.getString("username"));
				doc.put("date", rs.getString("updatedate"));
				doc.put("up", getUpRating(id));
				doc.put("down", getDownRating(id));
				doc.put("abstract", getXmlAbstract(rs.getString("xml"))); 
				doc.put("container", getXmlContainer(rs.getString("xml")));
		    	doc.put("platform", getXmlPlatform(rs.getString("xml")));
				array.put(doc);
			} 
			json = array.toString(); 
		} catch (SQLException | JSONException ex) {
			System.out.println(ex.getMessage());
		} finally {
			if (con != null)
				con.close();
		}
		return json;
	}

	/**
	 * not working - lucene grouping problem - detour via db rating table - new
	 * lucene version needed for grouping?
	 * 
	 * @deprecated
	 */
	public static void luceneGroupngTest() {
		// set parameter
		String subject, predicate, value, valueField, queryPredicate;
		subject = "urn:esri:geoportal:resourceid:{*}"; // operation needs an subject...
		predicate = "urn:esri:geoportal:rating";
		value = "urn:esri:geoportal:rating:value:up";
		valueField = "rdf.rating.value";
		queryPredicate = "urn:esri:geoportal:rating:query";
		// create classes for lucene search
		IndexSearcher searcher = null;
		IndexReader reader = null;
		AsnOperation operation = null;
		AsnIndexAdapter indexAdapter = null;
		AsnConfig config = new AsnConfig();
		config.configure();
		// set Operation for IndexSearcher
		try {
			operation = config.getOperations().makeOperation(subject, queryPredicate); // create Operation (query) with given ID
			indexAdapter = operation.getIndexReference().makeIndexAdapterWithoutContext();
			reader = indexAdapter.makeIndexReader();
			searcher = new IndexSearcher(reader);
		} catch (AsnInvalidOperationException | ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BooleanQuery query2 = new BooleanQuery();
		// Query sub = new WildcardQuery(new
		// Term(AsnConstants.FIELD_RDF_SUBJECT,"*"));
		Query pred = new TermQuery(new Term(AsnConstants.FIELD_RDF_PREDICATE, predicate));
		Query v = new TermQuery(new Term(valueField, value));
		// query2.add(sub, BooleanClause.Occur.MUST);
		query2.add(pred, BooleanClause.Occur.MUST);
		query2.add(v, BooleanClause.Occur.MUST);
		TopDocs topDocs2;
		try {
			// get max 5 results - result unsorted (identical) ids - solution:
			// collector - not really implemented in 3.0.3 - change to 4 not
			// possible, different classes
			// topDocs2 = searcher.search(query2,5);
			// System.out.println("ggg:"+topDocs2.totalHits);
			// for (int i=0;i<topDocs2.scoreDocs.length;i++){
			// int a = topDocs2.scoreDocs[i].doc; //gets doc id of lucene index
			// Document doc = reader.document(a);
			// System.out.println(doc.get(AsnConstants.FIELD_RDF_SUBJECT));
			// //gets document
			// }
			// collector test
			Sort groupSort = Sort.RELEVANCE;
			TermFirstPassGroupingCollector c1 = new TermFirstPassGroupingCollector(
					"rdf.subject", groupSort, 5);
			// TermQuery categoryQuery = new TermQuery(new Term("category",
			// "/philosophy/eastern"));
			Filter categoryFilter = new QueryWrapperFilter(v);
			searcher.search(v, categoryFilter, c1);
			// rdf.rating.value:"urn:esri:geoportal:rating:value:up"
			// TopScoreDocCollector collector = TopScoreDocCollector.create(2,
			// true);
			// searcher.search(query2, collector);
			// ScoreDoc[] docs = collector.topDocs().scoreDocs;
			// for (int i = 0; i < docs.length; i++) {
			// Document result = searcher.doc(docs[i].doc);
			// System.out.println("AAA-"+result);
			// }

		} catch (IOException e) { 
			e.printStackTrace();
		}

	}
	
	/**
	 * gets an array of username and password based on login table - used in SimpleIdentityAdapter for multi user login
	 * @return
	 * @throws SQLException 
	 */
	public static UsernamePasswordCredentials[] getUserList () throws SQLException {
		//sql request
		UsernamePasswordCredentials[] listDBUsers = new UsernamePasswordCredentials[0];;
		Connection con = null;
		CatalogConfiguration catConf = new CatalogConfiguration();
	    String sLoginTable = catConf.getTablePrefix()+"LOGIN";
		try {
			ConnectionBroker connBroker = new ConnectionBroker(); // get connection via connectionBroker
			ManagedConnection mc = connBroker.returnConnection("");
			con = mc.getJdbcConnection();
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT username, password FROM "+ sLoginTable;
			ResultSet rs = stmt.executeQuery(sql);

			int rowcount = 0;
			if (rs.last()) {
			  rowcount = rs.getRow();
			  rs.beforeFirst(); 
			}
			listDBUsers = new UsernamePasswordCredentials[rowcount];
			int rowNo= 0;
			while (rs.next()) {
				UsernamePasswordCredentials test = new  UsernamePasswordCredentials(rs.getString("username"),rs.getString("password"));
				listDBUsers[rowNo]=test;
				rowNo++;
			} 
		} catch (SQLException ex) {
			System.out.println("get username - error - HettpHelperMethods "+ex.getMessage());
		} finally {
			if (con != null)
				con.close();
		}
		return listDBUsers;
	}
	
	/**
	 * creates login table for multi user login  + creates test user "testuser" with pw "testuser"
	 * @throws SQLException 
	 */
	public static void createLogingTable() throws SQLException {
		Connection con = null;
		CatalogConfiguration catConf = new CatalogConfiguration();
		String loginTable = catConf.getTablePrefix() + "LOGIN";
		String sql = "CREATE TABLE "+loginTable+" (userid SERIAL,username character varying(64), password character varying(64),CONSTRAINT "+loginTable+"_pk PRIMARY KEY (userid));";
		try {
			ConnectionBroker connBroker = new ConnectionBroker();   //get connection via connectionBroker
		    ManagedConnection mc = connBroker.returnConnection("");
		    con = mc.getJdbcConnection();
		    Statement stmt = con.createStatement();
		    stmt.executeUpdate(sql); 
		} catch (SQLException ex) {
			System.out.println("TABLE CREATION ERROR "+ex.getMessage());	
		} finally {
			if (con != null)
				con.close();
		}
		//create test user
		createTestUser();
	}
	
	/**
	 * creates a test user for login table
	 * @throws SQLException 
	 */
	private static void createTestUser() throws SQLException {
		//get all IDs
		Connection con = null;
		ArrayList<String> ids = new ArrayList<String>();
		CatalogConfiguration catConf = new CatalogConfiguration();
		String loginTable = catConf.getTablePrefix()+"LOGIN";
		try {
			ConnectionBroker connBroker = new ConnectionBroker();   //get connection via connectionBroker
		    ManagedConnection mc = connBroker.returnConnection("");
		    con = mc.getJdbcConnection();
		    Statement stmt = con.createStatement();
			String insertSQL =" INSERT INTO "+loginTable+" (username,password) VALUES ('testuser','testuser');";
			   stmt.executeUpdate(insertSQL);
		} catch (SQLException ex) {
			System.out.println("FILL ERROR "+ex.getMessage());	
		} finally {
			if (con != null)
				con.close();
		}

	}

	/**
	 * check if login table exists
	 * @return
	 * @throws SQLException 
	 */
	public static boolean checkDb4LoginTable() throws SQLException {
		boolean check = false;
		Connection con = null;
		CatalogConfiguration catConf = new CatalogConfiguration();
		String loginTable = catConf.getTablePrefix() + "LOGIN";		
		try {
			ConnectionBroker connBroker = new ConnectionBroker();   //get connection via connectionBroker
		    ManagedConnection mc = connBroker.returnConnection("");
		    con = mc.getJdbcConnection();
		    String sql = "SELECT EXISTS(SELECT * FROM information_schema.tables WHERE UPPER (table_name) = UPPER('"+loginTable+"'));";
		    Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery(sql);
		    while (rs.next()) {
		    	check = rs.getBoolean(1);
		    }
		} catch (SQLException ex) {
			System.out.println("TABLE CHECK ERROR "+ex.getMessage());	
		} finally {
			if (con != null)
				con.close();
		}
		return check;
	}
	
}
