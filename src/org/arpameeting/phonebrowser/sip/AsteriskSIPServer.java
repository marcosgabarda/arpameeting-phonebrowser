package org.arpameeting.phonebrowser.sip;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AsteriskSIPServer {
	
	static private AsteriskSIPServer singletonAsteriskSIPServer = new AsteriskSIPServer();
	
	private ConcurrentHashMap<String, String> users_passwords;
	private ConcurrentHashMap<String, Boolean> users_availibility;
	
	private String host = "arpamet2.parcien.uv.es";
	private int port = 5060;
	
	private AsteriskSIPServer()
	{
		users_passwords = new ConcurrentHashMap<String, String>();
		users_availibility = new ConcurrentHashMap<String, Boolean>();
		for (int i = 1000; i <= 9999; i++)
		{
			String user = Integer.toString(i);
			String pass = Integer.toString(i);
			users_passwords.put(user, pass);
			users_availibility.put(user, new Boolean(true));
		}
	}
	
	public static AsteriskSIPServer getInstance()
	{
		return singletonAsteriskSIPServer;
	}
	
	public String getHost()
	{
		return host;
	}
	
	public int getPort()
	{
		return port;
	}
	
	/**
	 * 
	 * @return
	 */
	public String catchUser()
	{
		Set<String> users = users_availibility.keySet();
		for (String user: users)
		{
			if (users_availibility.get(user))
			{
				users_availibility.put(user, new Boolean(false));
				return user; 
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	public String getPassword(String user)
	{
		return users_passwords.get(user);
	}
	
	/**
	 * 
	 * @param user
	 */
	public void releaseUser(String user)
	{
		users_availibility.put(user, new Boolean(true));
	}
}
