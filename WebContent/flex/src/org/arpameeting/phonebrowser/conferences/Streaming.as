package org.arpameeting.phonebrowser.conferences
{
	import flash.net.NetStream;

	public class Streaming
	{
		private var stream:NetStream;
		
		public function Streaming(NetStream netStream)
		{
			this.stream = netStream;
		}
	}
}