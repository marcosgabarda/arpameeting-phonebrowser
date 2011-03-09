package org.arpameeting.phonebrowser.conference;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.arpameeting.phonebrowser.sip.AsteriskSIPServer;

/**
 * Participante en una conferencia.
 * @author marcos
 *
 */
@XmlRootElement
public class Participant 
{

	private String id;
	
	private String name;
	
	private String phone;
	
	private String sip;
	
	private String flashSip;
	
	private ParticipantType type;
	
	private Call call;
	
	private Room room;
	
	private boolean monitoring;
	
	private SecureRandom random = new SecureRandom();
	
	public Participant() {}
	
	public Participant(String name, ParticipantType type, Room room)
	{
		this.id = new BigInteger(130, random).toString(32);
		this.name = name;
		this.type = type;
		this.room = room;
		this.call = null;
		if (type == ParticipantType.BROWSER)
		{
			AsteriskSIPServer sipServer = AsteriskSIPServer.getInstance();
			flashSip = sipServer.catchUser() + "@" + sipServer.getHost() 
						+ ":" + sipServer.getPort();
		}
	}
	
	public String getPhone()
	{
		return phone;
	}
	
	public void setCall(Call call)
	{
		this.call = call;
	}
	
	@XmlTransient
	public Call getCall()
	{
		return call;
	}
	
	public void setType(ParticipantType type)
	{
		this.type = type;
	}
	
	public void setPhone(String phone)
	{
		this.phone = phone;
	}
	
	public ParticipantType getType()
	{
		return type;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}

	public void setSip(String sip) {
		this.sip = sip;
	}

	public String getSip() {
		return sip;
	}
	
	@XmlElement(name="url")
	public String getFlashURL()
	{
		if (type == ParticipantType.BROWSER)
		{
			HostingManager hostingManager = HostingManager.getInstance();
			String src = hostingManager.host + ":" + hostingManager.port + "/"
					+ hostingManager.inApp + "/"
					+ hostingManager.inSwf + "?room=" + room.getId()
					+ "&participant=" + this.id;
			return src;
		}
		return null;
	}

	public void setFlashSip(String flashSip) {
		this.flashSip = flashSip;
	}

	public String getFlashSip() {
		return flashSip;
	}
	
	public String getId()
	{
		return id;
	}
	
	public Room getRoom()
	{
		return room;
	}
	
	@XmlElement(name="call")
	public CallPhone getCallPhone()
	{
		if (type != ParticipantType.BROWSER)
		{
			return (CallPhone) call;
		}
		return null;
	}
	
	@XmlElement(name="call")
	public CallBrowser getCallBrowser()
	{
		if (type == ParticipantType.BROWSER)
		{
			return (CallBrowser) call;
		}
		return null;
	}

	public void setMonitoring(boolean monitoring) {
		this.monitoring = monitoring;
	}

	@XmlElement(name="recording")
	public boolean isMonitoring() {
		return monitoring;
	}
}
