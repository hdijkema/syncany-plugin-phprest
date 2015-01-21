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
package org.syncany.plugins.php;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.syncany.config.Config;
import org.syncany.plugins.transfer.AbstractTransferManager;
import org.syncany.plugins.transfer.StorageException;
import org.syncany.plugins.transfer.files.ActionRemoteFile;
import org.syncany.plugins.transfer.files.CleanupRemoteFile;
import org.syncany.plugins.transfer.files.DatabaseRemoteFile;
import org.syncany.plugins.transfer.files.MultichunkRemoteFile;
import org.syncany.plugins.transfer.files.RemoteFile;
import org.syncany.plugins.transfer.files.SyncanyRemoteFile;
import org.syncany.plugins.transfer.files.TempRemoteFile;
import org.syncany.plugins.transfer.files.TransactionRemoteFile;

public class PhpTransferManager extends AbstractTransferManager {
	private static final Logger logger = Logger.getLogger(PhpTransferManager.class.getSimpleName());
	
	private static int  MAX_ANSWER_LENGTH = 102400;
	
	private interface IPost {
		/**
		 * Give a change to add name value pairs.
		 */
		public void mutateNVPS(List<NameValuePair> nvps) throws Exception;
		
		/**
		 * Returns the expected maximum length of this post, or -1 if no restrictions
		 */
		public int mutatePost(HttpPost p) throws Exception;
		
		/**
		 * Consume the response, return a code about the response
		 */
		public int consumeResponse(InputStream s) throws Exception;
	};
	
	public String getAnswer(InputStream s) throws Exception {
		String line = "";
		int c = s.read();
		while (c != -1 && c != '\n') {
			line = line + new String(Character.toChars(c));
			c = s.read();
		}
		if (c == '\n') { logger.info("End of line reached"); }
		String answer = line;
		logger.info("answer: "+ answer);
		return answer;
	}

	public String getAnswer(InputStream s, int maxlen) throws Exception {
		String line = "";
		int c = s.read();
		int t = 1;
		while (c != -1 && c != '\n' && t < maxlen) {
			line = line + new String(Character.toChars(c)); 
			t = t + 1;
			c = s.read();
		}
		if (c == '\n') { logger.info("End of line reached"); }
		String answer = line;
		logger.info("answer: "+ answer);
		return answer;
	}


	public PhpTransferManager(PhpTransferSettings settings, Config config) throws Exception {
		super(settings, config);
	}

	public PhpTransferSettings getSettings() {
		return (PhpTransferSettings) settings;
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
		logger.info("Downloading: "+remoteFile.getName()+" to "+localFile.getName());
		try {
			final String remote_name = remoteFile.getName();
			final File f = localFile;
			int r = operate("download", new IPost(){
				public void mutateNVPS(List<NameValuePair> nvps) throws Exception {
					nvps.add(new BasicNameValuePair("filename",remote_name));
				}

				public int mutatePost(HttpPost p) throws Exception {
					return -1;
				}

				public int consumeResponse(InputStream s) throws Exception {
					String answer = getAnswer(s);
					if (answer.equals("true")) {
						logger.info("writing outputstream for file "+f);
						OutputStream out = new FileOutputStream(f);
						logger.info("File opened");
						byte[] buf = new byte[10240];
						int len = s.read(buf);
						int total = len;
						while (len > 0) {
							out.write(buf, 0, len);
							len = s.read(buf);
							total += len;
						}
						logger.info("read "+total+" bytes");
						out.close();
						logger.info("done writing");
						return 1;
					} else {
						throw new Exception(answer);
					}
				}
				
			});
		}
		catch (Exception e) {
			throw new StorageException("Cannot download file " + remoteFile, e);
		}		
	}

	@Override
	public void upload(File localFile, RemoteFile remoteFile) throws StorageException {
		logger.info("Uploading: "+localFile.getName()+" to "+remoteFile.getName());
		try {
			final String remote_name = remoteFile.getName();
			final File f = localFile;
			int r = operate("upload", new IPost() {
				List<NameValuePair> _nvps;
				
				public void mutateNVPS(List<NameValuePair> nvps) throws Exception {
					_nvps = nvps;
					nvps.add(new BasicNameValuePair("filename",remote_name));
				}

				public int mutatePost(HttpPost p) throws Exception {
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				    builder.addPart("file", new FileBody(f));
				    Iterator<NameValuePair> it = _nvps.iterator();
				    while (it.hasNext()) {
				    	NameValuePair nvp = it.next();
				    	builder.addTextBody(nvp.getName(), nvp.getValue());
				    }
				    p.setEntity(builder.build());
					return -1;
				}

				@Override
				public int consumeResponse(InputStream s) throws Exception {
					String response = getAnswer(s, 10);
					if (response.equals("true")) {
						return 1;
					} else {
						throw new Exception(response);
					}
				}
				
			});
			if (r != 1) {
				throw new Exception("Unexpected error, result code = " + r);
			}
			
		} catch (Exception e) {
			throw new StorageException("Cannot upload file "+remoteFile, e);
		}
	}	


	@Override
	public boolean delete(RemoteFile _remoteFile) throws StorageException {		
		try {
			final RemoteFile f = _remoteFile;
			logger.info("Deleting remote file: "+f.toString());
			int r = operate("delete", new IPost() {
				public int mutatePost(HttpPost p) throws Exception {
					return -1;
				}

				public void mutateNVPS(List<NameValuePair> nvps) throws Exception {
					nvps.add(new BasicNameValuePair("filename", f.getName()));
				}

				public int consumeResponse(InputStream s) throws Exception {
					String response = getAnswer(s,10);
					if (response.equals("true")) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			return (r == 1) ? true : false;
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Cannot delete remote file " + _remoteFile + ". IGNORING.", e);
			return false;
		}		
	}

	@Override
	public void move(RemoteFile sourceFile, RemoteFile targetFile) throws StorageException {		
		try {
			final RemoteFile f = sourceFile;
			final RemoteFile g = targetFile;
			logger.info("Moving remote file: "+f.toString()+" to "+g.toString());
			int r = operate("move", new IPost() {
				public int mutatePost(HttpPost p) throws Exception {
					return -1;
				}

				public void mutateNVPS(List<NameValuePair> nvps) throws Exception {
					nvps.add(new BasicNameValuePair("filename", f.getName()));
					nvps.add(new BasicNameValuePair("to_filename", g.getName()));
				}

				public int consumeResponse(InputStream s) throws Exception {
					String response = getAnswer(s,10);
					if (response.equals("true")) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			//return (r == 1) ? true : false;
		}
		catch (Exception e) {
			throw new StorageException(e);
		}		
	}
	
	private String getRemoteFilePath(Class<? extends RemoteFile> remoteFile) {
		if (remoteFile.equals(MultichunkRemoteFile.class)) {
			return "multichunk";
		}
		else if (remoteFile.equals(DatabaseRemoteFile.class) || remoteFile.equals(CleanupRemoteFile.class)) {
			return "database";
		}
		else if (remoteFile.equals(ActionRemoteFile.class)) {
			return "action";
		}
		else if (remoteFile.equals(TransactionRemoteFile.class)) {
			return "transaction";
		}
		else if (remoteFile.equals(TempRemoteFile.class)) {
			return "temp";
		}
		else {
			return "";
		}
	}
	@Override
	public <T extends RemoteFile> Map<String, T> list(Class<T> remoteFileClass) throws StorageException {
		try {
			final Map<String, T> fileList = new HashMap<String, T>();
			final String listType = getRemoteFilePath(remoteFileClass);
			final Class<T> _remoteFileClass = remoteFileClass;
			logger.info("Listing remote files for type: "+listType);
			int r = operate("list", new IPost() {
				public int mutatePost(HttpPost p) throws Exception {
					return -1;
				}

				public void mutateNVPS(List<NameValuePair> nvps) throws Exception {
					nvps.add(new BasicNameValuePair("type", listType));
				}

				public int consumeResponse(InputStream s) throws Exception {
					String response = getAnswer(s,10);
					if (response.equals("true")) {
						String file = getAnswer(s);
						while (file != "") {
							T remoteFile = RemoteFile.createRemoteFile(file, _remoteFileClass);
							fileList.put(file, remoteFile);
							file = getAnswer(s);
						}
						return 1;
					} else {
						return 0;
					}
				}
			});	
			if (r == 1) {
				return fileList;
			} else {
				throw new Exception("Cannot list files remote");
			}
		}
		catch (Exception e) {
			throw new StorageException(e);
		}
	}
	
	@Override
	public boolean testTargetCanWrite() {
		return testTargetLogin();
	}

	@Override
	public boolean testTargetExists() {
		return testTargetLogin();
	}
		
	@Override
	public boolean testTargetCanCreate() {
		return testTargetLogin();
	}

	@Override
	public boolean testRepoFileExists() {
		try {
			final RemoteFile f = new SyncanyRemoteFile();
			logger.info("Checking for remote file: "+f.toString());
			int r = operate("exists", new IPost() {
				public int mutatePost(HttpPost p) throws Exception {
					return -1;
				}

				public void mutateNVPS(List<NameValuePair> nvps) throws Exception {
					nvps.add(new BasicNameValuePair("filename", f.getName()));
				}

				public int consumeResponse(InputStream s) throws Exception {
					String response = getAnswer(s,10);
					if (response.equals("true")) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			return (r == 1) ? true : false;
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(), e);
			return false;
		}
	}
	
	public boolean testTargetLogin() {
		try {
			int r = operate("login",new IPost() {
				public void mutateNVPS(List<NameValuePair> nvps) throws Exception {
				}
	
				public int mutatePost(HttpPost p) throws Exception {
					return -1;
				}
	
				public int consumeResponse(InputStream s) throws Exception {
					String response = getAnswer(s, 10);
					if (response.equals("true")) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			return (r == 1) ? true : false;
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}
	
	private int operate(String action, IPost mutator) throws Exception {
		String url = settings.getField("url")+"/syncany_php.php";
		String context = settings.getField("context");
		String userid = settings.getField("userid");
		String passwd = settings.getField("passwd");

		logger.info("url: " + url);
		logger.info("action: "+ action);
		logger.info("userid: "+ userid + ", context: "+ context);
		
		HttpPost p = new HttpPost(url);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("userid", userid));
		nvps.add(new BasicNameValuePair("passwd", passwd));
		nvps.add(new BasicNameValuePair("context", context));
		nvps.add(new BasicNameValuePair("action",action));
		mutator.mutateNVPS(nvps);
		p.setEntity(new UrlEncodedFormEntity(nvps));
		
		int maxlen = mutator.mutatePost(p);
		int responsecode = -1;
		
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			CloseableHttpResponse response = httpclient.execute(p);
			HttpEntity e = response.getEntity();
			long len = e.getContentLength();
			logger.info("length of response: " + len);
			StatusLine sl = response.getStatusLine();
			int code = sl.getStatusCode();
			if (code != 200) {
				response.close();
				httpclient.close();
				throw new Exception("Response on http call: " + code);
			} else {
				if ( (maxlen >= 0) && ((len >= maxlen) || (len < 0)) ) {
					response.close();
					httpclient.close();
					throw new Exception("Expected maximum length: " + maxlen + ", returned content length: " + len);
				} else {
					try {
						InputStream b = e.getContent();
						responsecode = mutator.consumeResponse(b);
						b.close();
						response.close();
						httpclient.close();
					} catch(Exception ex) {
						response.close();
						httpclient.close();
						throw new Exception(ex);
					}
				}
			}
			response.close();
			httpclient.close();
		} catch (Exception e) {
			throw new Exception(e);
		}
		return responsecode;
	}

}
