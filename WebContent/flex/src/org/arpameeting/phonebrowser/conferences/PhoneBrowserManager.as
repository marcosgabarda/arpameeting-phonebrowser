package org.arpameeting.phonebrowser.conferences
{
	
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.IOErrorEvent;
	import flash.events.NetStatusEvent;
	import flash.events.SecurityErrorEvent;
	import flash.net.NetConnection;
	import flash.net.Responder;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	
	import mx.controls.Alert;
	
	import org.arpameeting.phonebrowser.events.PhoneBrowserManagerEvent;
	
	public class PhoneBrowserManager extends EventDispatcher
	{
		private var arpameetingRoomId:String;
		private var arpameetingRoomNumber:String;
		private var arpameetingParticipant:String;
		private var arpameetingNetConnection:NetConnection;
		private var sipListenerAccount:String;
		private var sipParticipantAccount:String;
		
		public function PhoneBrowserManager(room:String, participant:String)
		{
			this.arpameetingRoomId = room;
			this.arpameetingParticipant = participant;
		}

		/**
		 * Connection to Red5 server.
		 */
		public function connect():void
		{
			arpameetingNetConnection = new NetConnection();
			arpameetingNetConnection.addEventListener(
				NetStatusEvent.NET_STATUS, netStatusHandler);
			arpameetingNetConnection.addEventListener(
				SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
			arpameetingNetConnection.connect(
				ArpaMeeting.host + "/" + ArpaMeeting.phonebrowserApp + "/" + 
				this.arpameetingRoomId);
		}
		private function netStatusHandler(event:NetStatusEvent):void 
		{
			switch (event.info.code) 
			{
				case "NetConnection.Connect.Success":
					dispatchEvent(new PhoneBrowserManagerEvent(PhoneBrowserManagerEvent.PB_CONNECTED));
					break;
				case "NetConnection.Call.BadVersion":
				case "NetConnection.Call.Failed":
				case "NetConnection.Call.Prohibited":
				case "NetConnection.Connect.AppShutdown":
				case "NetConnection.Connect.Failed":
				case "NetConnection.Connect.InvalidApp":
					dispatchEvent(new PhoneBrowserManagerEvent(PhoneBrowserManagerEvent.PB_ERROR));
					break;
				case "NetStream.Play.StreamNotFound":
					//trace("Stream not found: " + videoURL);
					break;
			}
			
		}
		private function securityErrorHandler(event:SecurityErrorEvent):void 
		{
			//trace("securityErrorHandler: " + event);
		}
		
		/**
		 * Request Listener SIP Account
		 */
		
		public function requestSIPListenerAccount():void
		{
			arpameetingNetConnection.call("getSIPListenerAccount", 
				new Responder(responseSIPListenerAccount), this.arpameetingRoomId);
		}
		
		private function responseSIPListenerAccount(result:String):void
		{
			if (result == null)
			{
				dispatchEvent(new PhoneBrowserManagerEvent(PhoneBrowserManagerEvent.PB_LISTENER_ERROR));
				return;
			}
			this.sipListenerAccount = result;
			dispatchEvent(new PhoneBrowserManagerEvent(PhoneBrowserManagerEvent.PB_LISTENER_FINISHED));
		}
		
		/**
		 * Request Participant SIP Account
		 */
		
		public function requestSIPParticipantAccount():void
		{
			arpameetingNetConnection.call("getSIPParticipant", 
				new Responder(responseSIPParticipantAccount), 
					this.arpameetingRoomId, this.arpameetingParticipant);
		}
		
		private function responseSIPParticipantAccount(result:String):void
		{
			if (result == null)
			{
				dispatchEvent(new PhoneBrowserManagerEvent(PhoneBrowserManagerEvent.PB_PARTICIPANT_ERROR));
				return;
			}
			this.sipParticipantAccount = result;
			dispatchEvent(new PhoneBrowserManagerEvent(PhoneBrowserManagerEvent.PB_PARTICIPANT_FINISHED));
		}
		
		/**
		 * Request Room Number
		 */
		public function requestRoomNumber():void
		{
			arpameetingNetConnection.call("getRoomNumber", 
				new Responder(responseRoomNumber), this.arpameetingRoomId);
		}
		private function responseRoomNumber(result:String):void
		{
			if (result == null)
			{
				dispatchEvent(new PhoneBrowserManagerEvent(PhoneBrowserManagerEvent.PB_ROOM_ERROR));
				return;
			}
			this.arpameetingRoomNumber = result;
			dispatchEvent(new PhoneBrowserManagerEvent(PhoneBrowserManagerEvent.PB_ROOM_FINISHED));
		}
		
		public function getSIPListener():String
		{
			return this.sipListenerAccount;
		}
		
		public function getSIPParticipant():String
		{
			return this.sipParticipantAccount;
		}
		
		public function getRoomNumber():String
		{
			return arpameetingRoomNumber;
		}
		
		/**
		 * 
		 */
		public function handleRed5service(msg:String):void
		{
			trace("Event from red5: " + msg);
		}
	}
}