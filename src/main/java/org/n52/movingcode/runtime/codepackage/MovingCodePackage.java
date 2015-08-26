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
package org.n52.movingcode.runtime.codepackage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.joda.time.DateTime;

import de.tudresden.gis.geoprocessing.movingcode.schema.PackageDescriptionDocument;
import static org.n52.movingcode.runtime.codepackage.Constants.*;

/**
 * This class provides methods for handling MovingCode Packages. This includes methods for validation,
 * copying, unzipping and for accessing the package's description.
 * 
 * MovingCode Packages a the basic entities for shipping code from platform to platform.
 * 
 * @author Matthias Mueller, Christin Henzen, TU Dresden
 * 
 */
public class MovingCodePackage {

	private static final Logger logger = Logger.getLogger(MovingCodePackage.class);

	// the physical instance of this package
	private final ICodePackage archive;

	// package description XML document
	private PackageDescriptionDocument packageDescription = null;

	// identifier of the provided functionality (e.g. WPS process identifier)
	private final String functionIdentifier;

	// Package id and time stamp, i.e. date of creation or last modification
	private final PID packageID;

	private final List<FunctionalType> supportedFuncTypes;

	/**
	 * Constructor for zipFiles. Creates a MovingCodePackage from a zipFile on disk.
	 * 
	 * @param {@link File} zipFile - a zip file with a valid package structure
	 */
	public MovingCodePackage(final File zipFile) {
		
		this.archive = new ZippedPackage(zipFile);
		this.packageDescription = this.archive.getDescription();
		

		if (this.packageDescription != null) {
			this.functionIdentifier = this.packageDescription.getPackageDescription().getFunctionality().getWps100ProcessDescription().getIdentifier().getStringValue();
			this.supportedFuncTypes = getFunctionalTypes(this.packageDescription);
			
			DateTime timestamp = new DateTime(this.packageDescription.getPackageDescription().getTimestamp());
			String id = this.packageDescription.getPackageDescription().getPackageId();
			this.packageID = new PID(id, timestamp);
		}
		else {
			this.functionIdentifier = null;
			this.supportedFuncTypes = null;
			this.packageID = null;
		}
		
	}
	
	
	/**
	 * Constructor for geoprocessing feed entries. Creates a MovingCodePackage from an atom feed entry.
	 * 
	 * @param {@link GeoprocessingFeedEntry} atomFeedEntry - an entry from a geoprocessing feed
	 * 
	 */
	/**
	 * Constructor for geoprocessing feed entries. Creates a MovingCodePackage from a remote URL.
	 * Also requires the intended packageID an
	 * 
	 * @param zipPackageURL
	 * @param packageID
	 * @param packageStamp
	 */
	public MovingCodePackage(final URL zipPackageURL) {

		PackageDescriptionDocument packageDescription = null;
		ZippedPackage archive = null;
		
		archive = new ZippedPackage(zipPackageURL);
		packageDescription = archive.getDescription();
		
		this.packageDescription = packageDescription;
		// TODO: Information from the feed might lag during updates
		// how can deal with that?
		if (packageDescription != null) {
			this.functionIdentifier = packageDescription.getPackageDescription().getFunctionality().getWps100ProcessDescription().getIdentifier().getStringValue();
			this.supportedFuncTypes = getFunctionalTypes(packageDescription);
						
			DateTime timestamp = new DateTime(this.packageDescription.getPackageDescription().getTimestamp());
			String id = this.packageDescription.getPackageDescription().getPackageId();
			this.packageID = new PID(id, timestamp);
		}
		else {
			this.functionIdentifier = null;
			this.supportedFuncTypes = null;
			this.packageID = null;
		}
		
		this.archive = archive;

	}

	/**
	 * Constructor for directories. Creates a MovingCodePackage from a workspace directory on disk.
	 * 
	 * @param {@link File} workspace - the directory where the code and possibly some related data is stored.
	 * @param {@link PackageDescriptionDocument} packageDescription - the XML document that contains the
	 *        description of the provided logic
	 * @param {@link DateTime} lastModified - the date of latest modification. This value is optional. If NULL,
	 *        the lastModified date is obtained from the workspace's content.
	 */
	public MovingCodePackage(final File workspace,
			final PackageDescriptionDocument packageDescription) {
		
		this.packageDescription = packageDescription;

		if (packageDescription != null) {
			this.functionIdentifier = packageDescription.getPackageDescription().getFunctionality().getWps100ProcessDescription().getIdentifier().getStringValue();
			this.supportedFuncTypes = getFunctionalTypes(packageDescription);
			
			DateTime timestamp = new DateTime(this.packageDescription.getPackageDescription().getTimestamp());
			String id = this.packageDescription.getPackageDescription().getPackageId();
			this.packageID = new PID(id, timestamp);
		}
		else {
			this.functionIdentifier = null;
			this.supportedFuncTypes = null;
			this.packageID = null;
		}
		
		this.archive = new PlainPackage(workspace, packageDescription);

	}

	/**
	 * Dump workspace to a given directory. Used to create copies from a template for execution or further
	 * manipulation.
	 * 
	 * @param {@link File} targetDirectory - directory to store the unzipped content
	 * @return {@link String} dumpWorkspacePath - absolute path of the dumped workspace
	 */
	public String dumpWorkspace(File targetDirectory) {
		String wsRoot = this.packageDescription.getPackageDescription().getWorkspace().getWorkspaceRoot();
		this.archive.dumpPackage(wsRoot, targetDirectory);
		if (wsRoot.startsWith("./")) {
			wsRoot = wsRoot.substring(2);
		}

		if (wsRoot.startsWith(".\\")) {
			wsRoot = wsRoot.substring(2);
		}
		return targetDirectory + File.separator + wsRoot;
	}

	/**
	 * Writes a copy of the {@link MovingCodePackage} to a given directory. This is going to be a zipFile
	 * 
	 * @param targetFile
	 *        - destination path and file
	 * @return boolean - true if successful, false if not
	 */
	public boolean dumpPackage(File targetFile) {
		return this.archive.dumpPackage(targetFile);
	}

	/**
	 * writes a copy of the package (zipfile) to a given directory TODO: implement for URL sources
	 * 
	 * @param targetFile
	 * @return boolean
	 */
	public boolean dumpDescription(File targetFile) {
		try {
			this.packageDescription.save(targetFile);
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	
	public PID getVersionedPackageId(){
		return packageID;
	}
	
	/**
	 * Returns the PackageDescription
	 * 
	 * @return {@link PackageDescriptionDocument}
	 */
	public PackageDescriptionDocument getDescription() {
		return this.packageDescription;
	}
	
	/**
	 * Returns the PackageDescription as String
	 * 
	 * @return XML string of {@link PackageDescriptionDocument}
	 */
	public String getDescriptionString() {
		StringBuilder builder = new StringBuilder(); 
		builder.append(this.packageDescription);
		return builder.toString();
	}

	/**
	 * Does this object contain valid content?
	 * 
	 * @return boolean - true if content is valid, false if not
	 */
	public boolean isValid() {
		
		//System.out.println("MovingCodePackage isValid");
		
		if(this.packageDescription == null || this.packageDescription.isNil()){
			return false;
		}
		
		// a valid Code Package must have a package ID
		if(packageID.id == null || packageID.equals("")){
			return false;
		}
		// ... and timestamp
		if(packageID.timestamp == null){
			return false;
		}
		
		// a valid Code Package MUST have a function (aka process) identifier
		if (this.functionIdentifier == null) {
			return false;
		}

		// TODO: Identifiers of IO data must be unique!
		
		// TODO: verify path to executable.
		String exLoc = packageDescription.getPackageDescription().getWorkspace().getExecutableLocation();
		if (!this.archive.containsFileInWorkspace(exLoc)){
			return false;
		}
		
		// check if there exists a package description
		// and return the validation result
		//information on validation errors
		if (!this.packageDescription.validate()) {
			List<XmlError> errors = new ArrayList<XmlError>();
			this.packageDescription.validate(new XmlOptions().setErrorListener(errors));
			logger.warn("Package is not valid: "+errors);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns the unique *functional* identifier of this package. The identifier refers to the functional
	 * contract and not to a concrete implementation in this particular package.
	 * 
	 * @return String
	 */
	public final String getFunctionIdentifier() {
		return this.functionIdentifier;
	}

	/**
	 * Returns the timestamp of this package. The timestamp indicates the last update of the package content
	 * and can be used as a simple versioning machanism.
	 * 
	 * TODO: timestamp now covered by package ID - is this method still required?
	 * 
	 * 
	 * @return {@link DateTime} package timestamp
	 */
	public DateTime getTimestamp() {
		return this.packageID.timestamp;
	}

	/**
	 * Static internal method to evaluate a {@link PackageDescriptionDocument} and return the type (i.e. the
	 * schema) of the functional description.
	 * 
	 * TODO: slight overhead (?) - currently only WPS 1.0 is supported in the schema.
	 * 
	 * @param description
	 *        {@link PackageDescriptionDocument}
	 * @return {@link List} of {@link FunctionalType} - the type of the functional description (i.e. WPS 1.0,
	 *         WSDL, ...).
	 */
	private static final List<FunctionalType> getFunctionalTypes(final PackageDescriptionDocument description) {
		
		ArrayList<FunctionalType> availableFunctionalDescriptions = new ArrayList<FunctionalType>();
		
		// retrieve functional description types
		// TODO: make fit for WPS 2.0?
		availableFunctionalDescriptions.add(FunctionalType.WPS100);
		
		return availableFunctionalDescriptions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MovingCodePackage [archive=");
		builder.append(this.archive);
		builder.append(", packageDescription=");
		builder.append(this.packageDescription);
		builder.append(", PackageID=");
		builder.append(this.packageID.id);
		builder.append(", timeStamp=");
		builder.append(this.packageID.timestamp.toString());
		builder.append(", supportedFuncTypes=");
		builder.append(this.supportedFuncTypes);
		builder.append("]");
		return builder.toString();
	}

}
