package org.arpameeting.phonebrowser.sip;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KamailioServer {
	
	static private KamailioServer singletonKamailioServer = new KamailioServer();
	
	private Connection connection;
	private ConcurrentHashMap<String, String> users_passwords;
	private ConcurrentHashMap<String, Boolean> users_availibility;
	
	private String host = "arpamet2.parcien.uv.es";
	private int port = 5062;
	
	private KamailioServer()
	{
		users_passwords = new ConcurrentHashMap<String, String>();
		users_availibility = new ConcurrentHashMap<String, Boolean>();
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(
					"jdbc:mysql://localhost/openser?user=root&password=anarkia666");
			Statement instruccion = connection.createStatement();
			ResultSet table = instruccion.executeQuery("SELECT * FROM subscriber");
			while(table.next())
			{
				String user = table.getString(2);
				String pass = table.getString(4);
				users_passwords.put(user, pass);
				users_availibility.put(user, new Boolean(true));
			}
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static KamailioServer getInstance()
	{
		return singletonKamailioServer;
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
