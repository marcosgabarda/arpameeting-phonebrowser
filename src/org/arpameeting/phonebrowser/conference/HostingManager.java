package org.arpameeting.phonebrowser.conference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author Marcos Gabarda
 *
 */
public class HostingManager {
	
    /**
     * TODO Usar objeto Properties para guardar los parametros de los elementos 
     * flash.
     */
    public String host;
    public String port;
    public String outApp;
    public String outSwf;
    public String inApp;
    public String inSwf;
    
    /**
     * 
     */
    static private HostingManager singletonFlashManager = new HostingManager();

    /**
     * 
     */
    private HostingManager() 
    {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(System.getProperty("user.dir")
					+ File.separator + "webapps"
					+ File.separator + "phonebrowser"
					+ File.separator + "phonebrowser.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		host = properties.getProperty("webserver.host");
		port = properties.getProperty("webserver.port");
		outApp = properties.getProperty("webserver.outApp");
		outSwf = properties.getProperty("webserver.outSwf");
		inApp = properties.getProperty("webserver.inApp");
		inSwf = properties.getProperty("webserver.inSwf");
    }
    /**
     * 
     * @return
     */
    static public HostingManager getInstance()
    {
        return singletonFlashManager;
    }
}
