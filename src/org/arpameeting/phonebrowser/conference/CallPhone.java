package org.arpameeting.phonebrowser.conference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.arpameeting.phonebrowser.events.PBHangupEvent;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.manager.action.OriginateAction;

/**
 * 
 * 
 * @author Marcos Gabarda
 *
 */
@XmlRootElement
public class CallPhone implements Runnable, Call
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	private AsteriskServer asteriskServer;
	
	/**
	 * 
	 */
	private String id;
	
	/**
	 * 
	 */
	private String to;
	
	/**
	 * 
	 */
	private String extension;
	
	/**
	 * 
	 */
	private Participant participant;
	
	/**
	 * 
	 */
	private CallState state;
	
	/**
	 * 
	 */
	private Date started;
	
	/**
	 * 
	 */
	private Date finished;
	
	/**
	 * 
	 */
	private String channel;
	
	/**
	 * 
	 */
	private int usernum;
	
	/**
	 * 
	 */
	private String recordingInPath;
	
	/**
	 * 
	 */
	private String recordingOutPath;
	
	/**
	 * 
	 */
	private SecureRandom random = new SecureRandom();
	
	private boolean monitoring = true;
	
	private CallObserver observer;
	
	/**
	 * 
	 */
	public CallPhone() {}
	
	/**
	 * 
	 * @param phone
	 * @param extension
	 * @param type
	 */
	public CallPhone(String to, String extension, Participant participant)
	{
		this.id = new BigInteger(130, random).toString(32);
		this.to = to;
		this.extension = extension;
		this.participant = participant;
		this.started = null;
		this.finished = null;
		this.channel = null;
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
		HostingManager hostingManager = HostingManager.getInstance();
		this.recordingInPath = hostingManager.host + ":" + 
			hostingManager.port + "/phonebrowser/recordings/" + 
			id + "_record-in.wav";
		this.recordingOutPath = hostingManager.host + ":" + 
			hostingManager.port + "/phonebrowser/recordings/" + 
			id + "_record-out.wav";
	}
	
	/**
	 * 
	 */
	@Override
	public void run()
    {
		//managerLogin();
		if (participant.getType() == ParticipantType.PHONE)
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
			makeCall(properties.getProperty("asterisk.outgoingchannel") + to);
		}
		else if (participant.getType() == ParticipantType.SIP)
		{
			makeCall("SIP/" + to);
		}
    }
	
	/**
	 * 
	 * 
	 * @param channel
	 */
	private void makeCall(String channel)
	{
		System.out.println("[" + id + "]" + " Making call channel " + channel + " to extension " + extension);
		OriginateAction originateAction = new OriginateAction();
		originateAction.setChannel(channel);
		originateAction.setContext("default");
		originateAction.setExten(extension);
		originateAction.setPriority(new Integer(1));
		originateAction.setTimeout(new Long(30000));
		AsteriskChannel ac = asteriskServer.originate(originateAction);
		//System.err.println("[" + id + "] " + ac.getName());
		setChannel(ac.getName());
		observer.addCall(this);
	}

	public void setUsernum(int usernum) 
	{
		this.usernum = usernum;
	}
	
	public void setStarted(Date started)
	{
		System.out.println("[" + id + "]" + " Call started at " + started.toString());
		this.started = started;
	}

	public void setFinished(Date finished) 
	{
		System.out.println("[" + id + "]" + " Call finished at " + finished.toString());
		this.finished = finished;
	}
	
	public Date getStarted() {
		return started;
	}

	public Date getFinished() {
		return finished;
	}

	public String getChannel() {
		return channel;
	}

	public int getUsernum() {
		return usernum;
	}

	@Override
	@XmlElement(name="state")
	public CallState getState()
	{
		return state;
	}
	
	public void startRecording()
	{
		if (monitoring)
		{
			String curDir = System.getProperty("user.dir");
			System.out.println("[" + id + "]" + " GRABANDO!!! " + id + "_record (" + curDir + "/webapps/phonebrowser/recordings" + ")");
			asteriskServer.getChannelByName(channel).startMonitoring(curDir + "/webapps/phonebrowser/recordings/" + id + "_record");
		}
	}
	
	public void stopRecording()
	{
		if (monitoring)
		{
			asteriskServer.getChannelByName(channel).stopMonitoring();
		}
	}
	

	
	@XmlElement(name="input")
	public String getRecordingInPath()
	{
		return recordingInPath;
	}
	
	@XmlElement(name="output")
	public String getRecordingOutPath()
	{
		return recordingOutPath;
	}

	@Override
	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	@Override
	public void setCallState(CallState state) 
	{
		this.state = state;
	}

	@Override
	public void hangUp() 
	{
		setFinished(Calendar.getInstance().getTime());
		participant.getRoom().notifyEvent(new PBHangupEvent(participant));
		setCallState(CallState.HANGUP);
		System.out.println(participant.getName() + "(" + participant.getId() + 
				")" + " cuelga a las " + getFinished().toString());
		
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

	@Override
	public Participant getParticipant()
	{
		return participant;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public void setObserver(CallObserver observer)
	{
		System.err.println("### Observer set");
		this.observer = observer;
	}

}
