package org.arpameeting.phonebrowser.conference;

import java.util.Date;

public interface Call 
{
	public void setChannel(String channel);
	public void setUsernum(int usernum);
	public void setCallState(CallState state);
	public void setStarted(Date started);
	public void setFinished(Date finished);

	
	public String getChannel();
	public int getUsernum();
	public CallState getState();
	public Date getStarted();
	public Date getFinished();
	
	public void setMonitornig(boolean monitoring);
	public boolean getMonitoring();
	
	public String getRecordingInPath();
	public String getRecordingOutPath();
	
	public void hangUp();
	
}
