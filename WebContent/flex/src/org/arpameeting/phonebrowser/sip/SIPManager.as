package org.arpameeting.phonebrowser.sip
{
	import flash.events.EventDispatcher;
	import flash.events.NetStatusEvent;
	import flash.events.SecurityErrorEvent;
	import flash.net.NetConnection;
	import flash.net.NetStream;
	import flash.net.Responder;
	
	import mx.controls.Alert;
	
	import org.arpameeting.phonebrowser.conferences.ArpaMeeting;
	import org.arpameeting.phonebrowser.events.CallConnectedEvent;
	import org.arpameeting.phonebrowser.events.SIPManagerEvent;

	public class SIPManager extends EventDispatcher
	{
		private var arpameetingNetConnection:NetConnection;
		private var sipUid:String;
		private var conference:String;
		
		public var playName:String;
		public var publishName:String;
		
		public function SIPManager(uid:String, conference:String)
		{
			this.sipUid = uid;
			this.conference = conference;
			this.playName = null;
			this.publishName = null;
		}
		
		/**
		 * Connection to Red5 server.
		 */
		public function connect():void
		{
			NetConnection.defaultObjectEncoding = flash.net.ObjectEncoding.AMF0;
			arpameetingNetConnection = new NetConnection();
			arpameetingNetConnection.client = this;
			arpameetingNetConnection.addEventListener(
				NetStatusEvent.NET_STATUS, netStatusHandler);
			arpameetingNetConnection.addEventListener(
				SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
			arpameetingNetConnection.connect(
				ArpaMeeting.host + "/" + ArpaMeeting.sipApp);
		}
		private function netStatusHandler(event:NetStatusEvent):void 
		{
			switch (event.info.code) 
			{
				case "NetConnection.Connect.Success":
					dispatchEvent(
						new SIPManagerEvent(SIPManagerEvent.CONNECT_SUCCESS));
					break;
				default:
					break;
			}
			
		}
		private function securityErrorHandler(event:SecurityErrorEvent):void 
		{
			//trace("securityErrorHandler: " + event);
		}
		
		public function open():void
		{
			//Alert.show("open: " + this.sipUid.split("@")[1]);
			var obproxy:String = this.sipUid.split("@")[1];
			//var obproxy:String = "";
			var uid:String = this.sipUid;
			var phone:String = this.sipUid.split("@")[0];
			var username:String = this.sipUid.split("@")[0];
			var password:String = this.sipUid.split("@")[0];
			var sipRealm:String = this.sipUid.split("@")[1].split(":")[0];
			var sipServer:String = this.sipUid.split("@")[1];
			arpameetingNetConnection.call("open", new Responder(onOpen), 
				obproxy, uid, phone, username, password, sipRealm, sipServer);
		}
		private function onOpen(result:Object):void
		{
			//Alert.show("Open!");
			arpameetingNetConnection.call("call", null, this.sipUid, 
				"sip:" + this.conference + "@" + ArpaMeeting.asterisk);
		}

		public function newNetStream():NetStream
		{
			if (arpameetingNetConnection.connected)
			{
				return new NetStream(arpameetingNetConnection);
			}
			return null;
		}
		
		/**
		 * --- Red5 Callbacks ---
		 */
		
		public function registrationSucess(msg:String):* 
		{
		}
		
		public function registrationFailure(msg:String):* 
		{
		}
		

		public function callState(msg:String):*
		{
		}
		
		public function incoming(source:String, sourceName:String, 
								 destination:String, destinationName:String):*
		{
		}
		
		/**
		 * Cuando se invoque este metodo desde el servidor red5 se tendra 
		 * el nombre que tiene que reproducir para poder oir la conferencia
		 * que se esta realizando. 
		 */
		public function connected(publishName:String, playName:String):*
		{
			this.playName = playName;
			this.publishName = publishName;
			dispatchEvent(new CallConnectedEvent(CallConnectedEvent.CONNECTED, 
				publishName,  playName));
		}
		
		public function streamStatus(status:String):void
		{
			arpameetingNetConnection.call("streamStatus", null, this.sipUid, status);	
		}
	}
}