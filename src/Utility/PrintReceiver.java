package Utility;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import java.awt.*;
import java.util.HashMap;

public class PrintReceiver implements Receiver
{

	private final Receiver rcvr;

	public PrintReceiver(Receiver rcvr)
	{
		super();
		this.rcvr = rcvr;
	}

	@Override
	public void send(MidiMessage message, long timeStamp)
	{
		byte[] messageBytes = message.getMessage();
		handler(messageBytes[1], messageBytes[2]);
	}

	@Override
	public void close()
	{

	}

	public void handler(int noteVal, int onOrOff)
	{
		if (onOrOff == CustomReceiver.NOTE_ON) ;
		System.out.println(noteVal);
	}
}
