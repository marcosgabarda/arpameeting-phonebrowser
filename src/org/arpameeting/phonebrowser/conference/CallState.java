package org.arpameeting.phonebrowser.conference;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * 
 * @author Marcos Gabarda
 *
 */
@XmlEnum
public enum CallState 
{
	@XmlEnumValue("ilde")
	ILDE("ilde"),
	
	@XmlEnumValue("dialing")
	DIALING("dialing"),
	
	@XmlEnumValue("busy")
	BUSY("busy"),
	
	@XmlEnumValue("noanswer")
	NOANSWER("noanswer"),
	
	@XmlEnumValue("success")
	SUCCESS("success"),
	
	@XmlEnumValue("hangup")
	HANGUP("hangup");
	
	private final String value;

	CallState(String v) 
	{
        value = v;
    }
	
	public String value() 
	{
        return value;
    }
}
