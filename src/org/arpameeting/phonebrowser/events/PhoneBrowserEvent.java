package org.arpameeting.phonebrowser.events;

import java.util.HashMap;
import java.util.Map;

public abstract class PhoneBrowserEvent 
{
	private Map<String, String> data;
	
	public PhoneBrowserEvent()
	{
		data = new HashMap<String, String>();
	}
	
	public void addData(String key, String value)
	{
		data.put(key, value);
	}
	
	public Map<String, String> getData()
	{
		return data;
	}
	
	public abstract String getEventName();
}
