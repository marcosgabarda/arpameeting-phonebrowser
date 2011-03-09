package org.arpameeting.phonebrowser.conference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.arpameeting.phonebrowser.events.PBHangupEvent;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.DefaultAsteriskServer;

@XmlRootElement
public class CallBrowser implements Call {

	private Date finished;
	
	private String channel;
	
	private AsteriskServer asteriskServer;
	
	private Participant participant;
	
	private boolean monitoring = true;
	
	private CallState state;
	
	public CallBrowser()
	{
	}
	
	public CallBrowser(Participant participant)
	{
		this.participant = participant;
		this.state = CallState.ILDE;
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
		asteriskServer = new DefaultAsteriskServer(properties.getProperty("ami.host"), 
				properties.getProperty("ami.username"), 
				properties.getProperty("ami.password"));
	}
	
	@Override
	public void setChannel(String channel) 
	{
		this.channel = channel;
	}

	@Override
	public void setUsernum(int usernum) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCallState(CallState state) 
	{
		this.state = state;
	}

	@Override
	public void setStarted(Date started) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFinished(Date finished) 
	{
		this.finished = finished;
	}

	@Override
	public String getChannel() 
	{
		return channel;
	}

	@Override
	public int getUsernum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	@XmlElement(name="state")
	public CallState getState() 
	{
		return this.state;
	}

	@Override
	public Date getStarted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getFinished() 
	{
		return finished;
	}

	@Override
	public String getRecordingInPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRecordingOutPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void hangUp() 
	{
		AsteriskChannel asteriskChannel = 
			asteriskServer.getChannelByName(channel);
		if (asteriskChannel != null)
		{
			asteriskChannel.hangup();
			setFinished(Calendar.getInstance().getTime());
			setCallState(CallState.HANGUP);
			/**
			 * Producir evento.
			 */
			participant.getRoom().notifyEvent(new PBHangupEvent(participant));
			System.out.println(participant.getName() + "(" + 
					participant.getId() + ")" + " cuelga a las " + 
					getFinished().toString());
		}
	}

	@Override
	public void setMonitornig(boolean monitoring)
	{
		this.monitoring = monitoring;
		
	}

	@Override
	public boolean getMonitoring() 
	{
		return monitoring;
	}

}
