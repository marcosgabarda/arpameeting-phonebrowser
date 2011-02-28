package org.arpameeting.phonebrowser.conference;

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
    static public String host   = "http://arpamet2.parcien.uv.es";
    static public String port   = "5080";
    static public String outApp = "phonebrowser/flex/bin-debug";
    static public String outSwf = "output.swf";
    static public String inApp  = "phonebrowser/flex/bin-debug";
    static public String inSwf  = "input.swf";
    
    /**
     * 
     */
    static private HostingManager singletonFlashManager = new HostingManager();

    /**
     * 
     */
    private HostingManager() {}
    /**
     * 
     * @return
     */
    static public HostingManager getInstance()
    {
        return singletonFlashManager;
    }
}
