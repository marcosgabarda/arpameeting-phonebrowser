package org.arpameeting.phonebrowser.events
{
	import flash.events.Event;
	
	public class SIPManagerEvent extends Event
	{
		public static const CONNECT_SUCCESS:String = "CONNECT_SUCCESS";
		public static const CONNECT_ERROR:String = "CONNECT_ERROR";
		public static const CALL_CONNECTED:String = "CALL_CONNECTED";
		
		public function SIPManagerEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		
		override public function clone():Event
		{
			return new SIPManagerEvent(type);
		}
	}
}