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
package de.tudresden.gis.db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.fileupload.FileItem;
import org.n52.movingcode.runtime.codepackage.MovingCodePackage;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.sql.ConnectionBroker;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.catalog.context.CatalogConfiguration;
/**
 * Class to save document information in db instead of via ImsMetadataProxyDao.
 * @author Bernd Grafe
 *
 */
public class McpPublish { 
	
	MovingCodePackage mcp;
	String path, uuid;
	int publisherID;
	Boolean published = false, check = true;
	CatalogConfiguration catConf;
	
	/**
	 * Constructor without publisher information
	 * @param mcp package
	 * @param path target path
	 * @param uuid uuid of package 
	 */
	public McpPublish(MovingCodePackage mcp, String path, String uuid){     //second constructor without pass for new upload without creating it from the get go	 
		this.mcp = mcp;
		this.path = path;
		this.uuid = "{" + uuid + "}"; // = folder name of package - can be used to reconstruct download link
		catConf = new CatalogConfiguration();
		publisherID = 1; // TODO:get real publisher for upload form (context) -  UploadMetadataController.selectablePublishers.selectedKey
	}
	/**
	 * recommended Constructor / publisher information via context
	 * @param mcp package
	 * @param path target path
	 * @param uuid uuid og package
	 * @param context for getting publisher information
	 * @throws NotAuthorizedException
	 * @throws IdentityException
	 * @throws ImsServiceException
	 * @throws SQLException
	 */
	public McpPublish(MovingCodePackage mcp, String path, String uuid,RequestContext context) throws NotAuthorizedException, IdentityException, ImsServiceException, SQLException{     //second constructor without pass for new upload without creating it from the get go
		this.mcp = mcp;
		this.path = path;
		this.uuid = "{" + uuid + "}"; // = folder name of package - can be used to reconstruct download link
		catConf = new CatalogConfiguration();
		publisherID= context.getUser().getLocalID();
	}
	
	/**
	 * publishes mcp in db - set "known" to true after mcp creation at PackageGenerator, new validation is not necessary
	 * @param known
	 * @return
	 * @throws SQLException 
	 * @throws ImsServiceException 
	 */
	public boolean publish(Boolean known) throws SQLException, ImsServiceException {
		if (known)
			check = false;
		return publish();
	}
	
	/**
	 * publishes mcp in db + validation
	 * @return
	 * @throws SQLException 
	 * @throws ImsServiceException 
	 */
	public boolean publish() throws SQLException, ImsServiceException {
		//TODO: integration of validation - maybe not necessary since validation is done via schema comparison
		//if (mcp==null || path ==null) return false;
		//if (check) if (!mcp.isValid()) {
		//	System.out.println("MCP Validation failed");
		//	return false;
		//}
		insertRecord();
		return published;
		
		/*steps in geoportal to save it in db:
		 * 
		 * 	Publisher publisher = getSelectablePublishers().selectedAsPublisher(context,false);
          	UploadRequest request = new UploadRequest(context,publisher,sFileName,sXml);
          	request.publish();
		 * 
		 * UploadRequest: publish(schema) ->  sendPublicationRequest(schema) ->
		 * 				  PutMetadataRequest imsRequest -> set set set -> imsRequest.executePut(putInfo)
		 * 
		 * PutMetadataRequest: ImsMetadataProxyDao proxy ->  proxy.insertRecord(this,info)
		 * 
		 * ImsMetadataProxyDao: creates sql queries based on info
		 * 
		 */
//		RequestContext context;
//		Publisher publisher = new Publisher(context);
//      UploadRequest request = new UploadRequest(context, publisher, sFileName, sXml);
//      request.publish();
	}
	
	/**
	 * Method to insert entries into table.
	 * 
	 * @return rows
	 * @throws ImsServiceException
	 * @throws SQLException
	 */
	private int insertRecord() throws ImsServiceException, SQLException {
		// resource table
		// resource data table = docUUID, ID, XML, thumbnail
		Connection con = null;
		boolean autoCommit = true;
		PreparedStatement st = null;
		ResultSet rs = null;

		// initialize
		int nRows = 0;
		String sXml = mcp.getDescription().xmlText();
		String sUuid = uuid;
		String sName = mcp.getDescription().getPackageDescription()
				.getFunctionality().getWps100ProcessDescription().getTitle()
				.getStringValue();
		
		// String sThumbnailBinary = ""; //no thumbnails used
		String sTable = catConf.getResourceTableName();
		String sDataTable = catConf.getResourceDataTableName();
		
		// long id = doesRecordExist(sTable, sUuid); //TODO id isnt package id
		// for now - has to be changed to real package ID
		try {
			
			ConnectionBroker connBroker = new ConnectionBroker(); // get connection via connectionBroker
			ManagedConnection mc = connBroker.returnConnection("");
			con = mc.getJdbcConnection();
			autoCommit = con.getAutoCommit();
			con.setAutoCommit(false);
			
			// insert a record - resource table
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO ").append(sTable);
			sql.append(" (");
			sql.append("DOCUUID,");
			sql.append("TITLE,");
			sql.append("OWNER,");
			sql.append("PUBMETHOD ");
			sql.append(")");
			sql.append(" VALUES(?,?,?,?)");
			
			st = con.prepareStatement(sql.toString());
			int n = 1;
			st.setString(n++, sUuid);
			st.setString(n++, sName);
			st.setInt(n++, publisherID); // TODO: context needed for publisher ID UploadMetadataController.selectablePublishers.selectedKey
			st.setString(n++, "editor"); // enables permission to edit
			nRows = st.executeUpdate();
			st.close(); // close statement
			
			// get id (not uuid)
			st = con.prepareStatement("SELECT id FROM " + sTable + " WHERE UPPER(docuuid)=?");
			// st = con.prepareStatement("SELECT id FROM " + sTable + " WHERE docuuid=?");
			st.setString(1, sUuid.toUpperCase());
			rs = st.executeQuery();
			rs.next();
			long id = rs.getLong(1);
			st.close(); 
			
			// insert a record - resource data table
			sql = new StringBuffer();
			sql.append("INSERT INTO ").append(sDataTable);
			sql.append(" (DOCUUID,ID,XML)");
			sql.append(" VALUES(?,?,?)");
			st = con.prepareStatement(sql.toString());
			st.setString(1, sUuid);
			st.setLong(2, id);
			st.setString(3, sXml);
			st.executeUpdate();
			con.commit(); 
		} catch (SQLException ex) {
			if (con != null) {
				con.rollback();
			}
			throw ex;
		} finally {
			rs.close(); 
			st.close(); 
			if (con != null) {
				con.setAutoCommit(autoCommit);
				con.close(); 
			}
			
		}
		published = true; 
		return nRows;
	  }
	
	/**
	 * This method validates extension and type for file item - also for unknown type octet-stream.
	 * 
	 * @param item
	 * @return true or false - validation
	 */
	public static boolean validateFileType (FileItem item){
		String[][] types = 
			  { { "zip", "application/zip" },
				{ "zip", "application/x-zip-compressed" },
				{ "zip", "application/octet-stream" } };
		for (String[] type : types) {
			if (item.getContentType().equals(type[1]) && item.getName().contains(type[0]))
				return true;
		}

		return false;
	}
}
