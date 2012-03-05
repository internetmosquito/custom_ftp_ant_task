package ftp.ant.task;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.ftp.simpleclient.FtpClient;


/**
 * @author Alejandro Villamarin
 * @version 1.0.0
 * 
 * A customised Ant task to use the vsftpclient library in an Ant file, it wraps the vsftpclient
 * functionalities in a customised Ant task 
 * 
 */
public class VerySimpleFtpAntTask extends Task{
    
	/**
	 * The FtpClient used to communicate with the FTP server
	 */
	FtpClient ftpClient;
	/**
	 * The FTP server address or fqdm
	 */
	private String host;
	/**
	 * The FTP server listening port
	 */
	private int port;
	/**
	 * Flag to indicate if we want to bypass Java SSL certificate check
	 */
	private boolean bypass;
	/**
	 * The user to connect to the FTP server
	 */
	private String user;
	/**
	 * The password for the given user
	 */
	private String password;
	/**
	 * Specifies the connection type. Allowed values are:
	 * 0 - FTP
	 * 1 - FTPS
	 * 2 - FTPES
	 */
	private int connType;
	/**
	 * Specifies the operation to be performed. Possible values are:
	 * 
	 * putDir 	- Upload the contents of source folder to destination folder
	 * putFile 	- Uploads a file to specified destination folder
	 * getDir 	- Downloads the contents of a source folder into a destination folder
	 * getFile 	- Downloads a file into specified destination folder 
	 * delDir   - Deletes the whole directory specified
	 * 
	 */
	private enum actions {PUTDIR, PUTFILE, GETDIR, GETFILE, DELFILE, DELDIR}
	private actions action;
	
	
	/**
	 * Source folder used for actions such as putdir, getdir
	 */
	private String sourceDir;
	
	/**
	 * Destination folder used for actions such as putdir, getdir
	 */
	private String destDir;
	
	/**
	 * File to upload/download, used in putfile and getfile actions
	 */
	private String file;
	
	/**
	 * Constructor initializes fields with default parameters
	 * 
	 * 
	 */
	public VerySimpleFtpAntTask(){
		ftpClient = new FtpClient();
		host = "localhost";
		port = 21;
		bypass = true;
		user = "anonymous";
		password = "";
		connType = 0;
		action = VerySimpleFtpAntTask.actions.GETFILE;
		sourceDir = "";
		destDir = "";
		file = "";
	}
		
    /**
     * Set method for the HOST parameter of the Ant task
     * 
     * @param hst	Should contain a valid IP address or domain name
     * 
     */
	public void setHost(String hst) {
        host = hst;
    }
	
    /**
     * Set method for the USER parameter of the Ant task
     * 
     * @param prt	Should contain a valid IP address or domain name
     * 
     */
	public void setPort(int prt) {
        port = prt;
    }
	
    /**
     * Set method for the BYPASS parameter of the Ant task
     * 
     * @param byp	<code>true</code> if bypass is going to be required
     * 
     */
	public void setBypass(String byp) {
        bypass = Boolean.parseBoolean(byp);
    }

    /**
     * Set method for the USER parameter of the Ant task
     * 
     * @param usr	A valid registered user in the server
     * 
     */
	public void setUser(String usr) {
        user = usr;
    }
	
    /**
     * Set method for the PASSWORD parameter of the Ant task
     * 
     * @param pwd	The password required by the USER to login
     * 
     */
	public void setPassword(String pwd) {
        password = pwd;
    }
	
    /**
     * Set method for the CONNTYPE parameter of the Ant task
     * 
     * @param ct	An int indicating connection Type
     * 
     */
	public void setConnType(int ct) {
        connType = ct;
    }
	
    /**
     * Set method for the ACTION parameter of the Ant task
     * 
     * @param act	A string specifying the action to perform, possible values are:
     * 				0 - PUTDIR
     * 				1 - PUTFILE
     * 				2 - GETDIR
     * 				3 - GETFILE
     * 				4 - DELFILE
     *              5 - DELDIR
     */
	public void setAction(String act) {
		//check the passed string and convert it to the enum representation
		if (act.equalsIgnoreCase("putdir")){
			action = actions.PUTDIR;
			return;
		}
		if (act.equalsIgnoreCase("putfile")){
			action = actions.PUTFILE;
			return;
		}
		if (act.equalsIgnoreCase("getfile")){
			action = actions.GETFILE;
			return;
		}
		if (act.equalsIgnoreCase("getdir")){
			action = actions.GETDIR;
			return;
		}
		if (act.equalsIgnoreCase("deldir")){
			action = actions.DELDIR;
			return;
		}
		if (act.equalsIgnoreCase("delfile")){
			action = actions.DELFILE;
			return;
		}
		//any other thing given should raise an error
		else{
			throw new BuildException("No proper action was given. Allowed values are: putdir, putfile, getdir, getfile, deldir or delfile. Aborting build");
		}
        
    }
	
	
    /**
     * Set method for the SOURCEDIR parameter of the Ant task
     * 
     * @param src	A String with source folder path
     * 
     */
	public void setSourceDir(String src) {
        sourceDir = src;
    }
	
    /**
     * Set method for the DESTDIR parameter of the Ant task
     * 
     * @param dst	A String with destination folder path
     * 
     */
	public void setDestDir(String dst) {
        destDir = dst;
    }
	
    /**
     * Set method for the FILE parameter of the Ant task
     * 
     * @param f		A String with file name, path + name is expected 
     * 
     */
	public void setFile(String f) {
        file = f;
    }
	
    /**
     * Helping method that validates parameters based on the required action to perform
     * 
     * @return	<code>true</code> if all validation was successful
     */
	private boolean checkParams() {
		
		//since all operations required connection and login check related params first
		if (host.isEmpty() || (port<=0 || port > 65535) || user.isEmpty() || password.isEmpty()){
			throw new BuildException("Some of the mandatory parameters: host, port, user, password are empty or are invalid");
		}
		else{
			//based on the action, validation differs
			switch (action){
			
				case PUTDIR:
					//to upload a directory, both sourceDir and destDir are mandatory
					if (destDir.isEmpty() || sourceDir.isEmpty()){
						throw new BuildException("Cannot upload a directory if source folder or destination folder are not specified. Use sourceDir and destDir to specify both.");
					}
					else{
						return true;
					}
				case PUTFILE:
					//to upload a file both destDir and file are mandatory
					if (destDir.isEmpty() || file.isEmpty()){
						throw new BuildException("Cannot upload file with an unspecified destination folder or unspecified file name. Please use destDir and/or file parameter to specify");
					}
					else{
						return true;
					}
				case GETDIR:
					//to download a folder both sourceDir and destDir are mandatory
					if (destDir.isEmpty() || sourceDir.isEmpty()){
						throw new BuildException("Cannot download a directory if source folder or destination folder are not specified. Use sourceDir and destDir to specify both.");
					}
					else{
						return true;
					}
				case GETFILE:
					//to download a file destDir, sourceDir and file are mandatory
					if (destDir.isEmpty() || file.isEmpty() || sourceDir.isEmpty()){
						throw new BuildException("Cannot download file with an unspecified destination and/or source folder and/or unspecified file name. Please use destDir, sourceDir and/or file parameter to specify");
					}
					else{
						return true;
					}
				case DELFILE:
					//to delete a file destDir and file are mandatory
					if (destDir.isEmpty() || file.isEmpty()){
						throw new BuildException("Cannot delete file with an unspecified destination and/or unspecified file name. Please use destDir and/or file parameter to specify");
					}
					else{
						return true;
					}
				case DELDIR:
					//to delete a directory destDir is mandatory
					if (destDir.isEmpty()){
						throw new BuildException("Cannot delete directory with an unspecified destination folder. Please use destDir to specify the parameter.");
					}
					else{
						return true;
					}
				default:
					return false;
			}
		}
	}
	
	/**
	 * This is the method that actually executes the Ant task
	 * 
	 */
    public void execute() {
        
    	//check that given parameters are correct
    	if (checkParams()){
    		
    		//get connection type
    		FtpClient.secure type;
    		switch (connType){
    			case 0:
    				type = FtpClient.secure.FTP;
    				break;
    			case 1:
    				type = FtpClient.secure.FTPS;
    				break;
    			case 2:
    				type = FtpClient.secure.FTPES;
    				break;
    			default:
    				type = FtpClient.secure.FTP;
    		}
    		
    		//create a FTPClient object with given parameters
    		ftpClient = new FtpClient(this.host, this.port, this.user, this.password, type, this.bypass);
    		
    		//setup first
    		if (ftpClient.setupClient()){
        		//try to connect
        		if (ftpClient.connect()){
        			//try to login
        			if (ftpClient.login()){
        				//once connected, depending on the action we do one thing or another
        				switch (action){
        				
        					//upload directory
        					case PUTDIR:
        						if (ftpClient.uploadDirFiles(this.sourceDir, this.destDir)){
        							log("Uploading directory was sucsseful");
        							//disconnect from ftp server
        							ftpClient.disconnect();
        							break;
        						}
        						else{
        							throw new BuildException("Uploading directory went wrong. Aborting build.");
        						}
            				//upload file
        					case PUTFILE:
        						if (ftpClient.uploadFile(this.file, destDir)){
        							log("Uploading file was sucsseful");
        							//disconnect from ftp server
        							ftpClient.disconnect();
        							break;
        						}
        						else{
        							throw new BuildException("Uploading file went wrong. Aborting build.");
        						}
        					//download directory
        					case GETDIR:
        						if (ftpClient.downloadDirFiles(this.sourceDir, this.destDir)){
        							log("Downloading directory was sucsseful");
        							//disconnect from ftp server
        							ftpClient.disconnect();
        							break;
        						}
        						else{
        							throw new BuildException("Uploading directory went wrong. Aborting build.");
        						}
            				//download file
        					case GETFILE:
        						if (ftpClient.downloadFile(this.file, this.sourceDir, this.destDir)){
        							log("Downloading file was sucsseful");
        							//disconnect from ftp server
        							ftpClient.disconnect();
        							break;
        						}
        						else{
        							throw new BuildException("Downloading file went wrong. Aborting build.");
        						}
        					//delete file
        					case DELFILE:
        						if (ftpClient.deleteFile(this.file, this.destDir)){
        							log("Deleting file " + this.file + "from directory " + this.destDir + " was sucsseful");
        							//disconnect from ftp server
        							ftpClient.disconnect();
        							break;
        						}
        						else{
        							throw new BuildException("Deleting file went wrong. Aborting build.");
        						}
        				    //delete directory
        					case DELDIR:
        						if (ftpClient.deleteDir(this.destDir)){
        							log("Deleting directory " + this.destDir + " was sucsseful");
        							//disconnect from ftp server
        							ftpClient.disconnect();
        							break;
        						}
        						else{
        							throw new BuildException("Downloading file went wrong. Aborting build.");
        						}
        				}
        			}
        		}    			
    		}
    	}
    }


}