package org.arpameeting.phonebrowser.events;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 
 * @author Marcos Gabarda
 *
 */
public class RoomObserver 
{
	
	private String observerUrl;
	
	public RoomObserver(String observerUrl)
	{
		this.observerUrl = observerUrl;
	}
	
	/**
	 * Build data string.
	 * 
	 * @param vars
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String buildData(Map<String, String> vars) throws UnsupportedEncodingException
	{
		String data = new String("");
		boolean firstVariable = true;
		for(String key: vars.keySet())
		{
			String value = vars.get(key);
			if (firstVariable)
			{
				data += URLEncoder.encode(key, "UTF-8") + "=" + 
						URLEncoder.encode(value, "UTF-8");
				firstVariable = false;
			}
			else
			{
				data += "&" + URLEncoder.encode(key, "UTF-8") + "=" + 
						URLEncoder.encode(value, "UTF-8");
			}
		}
		return data;
	}
	
	/**
	 * 
	 * @param event
	 */
	public void update(PhoneBrowserEvent event)
	{
		try 
		{
			String data = buildData(event.getData());
			URL url = new URL(observerUrl);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(
					connection.getOutputStream());
			out.write(data);
			out.close();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							connection.getInputStream()));
			in.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
