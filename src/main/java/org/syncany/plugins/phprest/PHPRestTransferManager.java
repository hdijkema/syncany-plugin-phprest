/*
 * Syncany, www.syncany.org
 * Copyright (C) 2015 Hans Dijkema <hans@dykema.nl>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany.plugins.phprest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.syncany.config.Config;
import org.syncany.plugins.transfer.AbstractTransferManager;
import org.syncany.plugins.transfer.StorageException;
import org.syncany.plugins.transfer.files.RemoteFile;
import org.syncany.plugins.transfer.files.SyncanyRemoteFile;

public class PHPRestTransferManager extends AbstractTransferManager {
	private static final Logger logger = Logger.getLogger(PHPRestTransferManager.class.getSimpleName());


	public PHPRestTransferManager(PHPRestTransferSettings settings, Config config) throws Exception {
		super(settings, config);
	}

	public PHPRestTransferSettings getSettings() {
		return (PHPRestTransferSettings) settings;
	}
	
	@Override
	public void connect() throws StorageException {
		// Nothing
	}

	@Override
	public void disconnect() {
		// Nothing
	}

	@Override
	public void init(boolean createIfRequired) throws StorageException {	
		logger.info("init plugin");
		if (createIfRequired) {
		}
		else {
		}
	}

	@Override
	public void download(RemoteFile remoteFile, File localFile) throws StorageException {
		
		try {
		}
		catch (Exception e) {
			throw new StorageException("Cannot download file " + remoteFile, e);
		}		
	}

	@Override
	public void upload(File localFile, RemoteFile remoteFile) throws StorageException {
		upload(localFile, remoteFile, true);
	}	

	private String upload(File localFile, RemoteFile remoteFile, boolean addToPhotoset) throws StorageException {
		try {
			String id = "x";
			return id;
		}
		catch (Exception e) {
			throw new StorageException("Cannot upload file " + localFile + " to remote file ", e);
		}
	}	

	@Override
	public boolean delete(RemoteFile remoteFile) throws StorageException {		
		try {
			return true;
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Cannot delete remote file " + remoteFile + ". IGNORING.", e);
			return false;
		}		
	}

	@Override
	public void move(RemoteFile sourceFile, RemoteFile targetFile) throws StorageException {		
		try {
		}
		catch (Exception e) {
			throw new StorageException(e);
		}		
	}
	
	@Override
	public <T extends RemoteFile> Map<String, T> list(Class<T> remoteFileClass) throws StorageException {
		try {
			Map<String, T> fileList = new HashMap<String, T>();
			return fileList;
		}
		catch (Exception e) {
			throw new StorageException(e);
		}
	}
	
	@Override
	public boolean testTargetCanWrite() {
		return true;
	}

	@Override
	public boolean testTargetExists() {
		try {
			return true;
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot get information about ", e);
			return false;
		}		
	}

	@Override
	public boolean testTargetCanCreate() {
		return true;
	}

	@Override
	public boolean testRepoFileExists() {
		//try {
			return true;
		//}
		/*catch (StorageException e) {
			logger.log(Level.SEVERE, "Cannot get information about repo file.", e);
			return false;
		}*/		
	}	

}
