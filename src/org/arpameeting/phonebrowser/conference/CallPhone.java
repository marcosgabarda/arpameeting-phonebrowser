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

import org.arpameeting.phonebrowser.events.PBAnswerEvent;
import org.arpameeting.phonebrowser.events.PBBusyEvent;
import org.arpameeting.phonebrowser.events.PBHangupEvent;
import org.arpameeting.phonebrowser.events.PBNoAnswerEvent;
import org.arpameeting.phonebrowser.events.PBRingEvent;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;
import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.event.HangupEvent;

/**
 * 
 * 
 * @author Marcos Gabarda
 *
 */
@XmlRootElement
public class CallPhone extends AbstractManagerEventListener implements Runnable, 
	OriginateCallback, Call
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
		asteriskServer.getManagerConnection().addEventListener(this);
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
		System.out.println("Making call channel " + channel + " to extension " + extension);
		OriginateAction originateAction = new OriginateAction();
		originateAction.setVariable("callid", id);
		originateAction.setChannel(channel);
		originateAction.setContext("default");
		originateAction.setExten(extension);
		originateAction.setVariable("call", this.id);
		originateAction.setPriority(new Integer(1));
		originateAction.setTimeout(new Long(30000));
		asteriskServer.originateAsync(originateAction, this);
	}

	public void setUsernum(int usernum) 
	{
		this.usernum = usernum;
	}
	
	public void setStarted(Date started)
	{
		System.out.println("Call " + this.id + " started at " + started.toString());
		this.started = started;
	}

	public void setFinished(Date finished) 
	{
		System.out.println("Call " + this.id + " finished at " + finished.toString());
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
	public CallState getState() {
		return state;
	}
	
	public void stopRecording()
	{
		if (monitoring)
		{
			asteriskServer.getChannelByName(channel).stopMonitoring();
		}
	}
	
	/**
	 * Inherit from OriginateCallback 
	 */
	
	@Override
	public void onBusy(AsteriskChannel channel) 
	{
		System.out.println("busy " + channel.getName());
		participant.getRoom().notifyEvent(new PBBusyEvent(participant));
	}

	@Override
	public void onDialing(AsteriskChannel channel) 
	{
		setCallState(CallState.DIALING);
		setChannel(channel.getName());
		System.out.println("dialing " + channel.getName());
		/**
		 * Notify.
		 */
		participant.getRoom().notifyEvent(new PBRingEvent(participant));
	}

	@Override
	public void onFailure(LiveException exception) 
	{
		setCallState(CallState.ILDE);
		System.out.println("failure");
	}

	@Override
	public void onNoAnswer(AsteriskChannel channel) 
	{
		setCallState(CallState.NOANSWER);
		participant.getRoom().notifyEvent(new PBNoAnswerEvent(participant));
		System.out.println("noanswer");
	}
	
	@Override
	public void onSuccess(AsteriskChannel channel) 
	{
		setCallState(CallState.SUCCESS);
		System.out.println("success " + channel.getName());
		participant.getRoom().notifyEvent(new PBAnswerEvent(participant));
		if (monitoring)
		{
			String curDir = System.getProperty("user.dir");
			System.out.println("GRABANDO!!! " + id + "_record (" + curDir + "/webapps/phonebrowser/recordings" + ")");
			asteriskServer.getChannelByName(this.channel).startMonitoring(curDir + "/webapps/phonebrowser/recordings/"+id + "_record");
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
	protected void handleEvent(HangupEvent event) 
	{
		System.out.println("HangupEvent in channel " + event.getChannel());
		super.handleEvent(event);
		if (channel != null && channel.compareTo(event.getChannel()) == 0)
		{
			hangUp();
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
