package Utility;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GlowUpReceiver implements Receiver
{
	// CONSTS

	public static final int RED_VEL		= 3;
	public static final int GREEN_VEL	= 48;
	public static final int YELLOW_VEL	= 50;
	public static final int ORANGE_VEL	= 51;

	public static final int NOTE_ON = 127;
	public static final int NOTE_OFF = 0;

	private Receiver rcvr;
	private ArrayList[] numberToButtons;
	int lastNumReceived;

	public GlowUpReceiver()
	{
		this.numberToButtons = new ArrayList[10];
		this.lastNumReceived = -1;

		// population
		this.numberToButtons[0] = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 18, 21, 34, 37, 50, 53, 66, 69, 82, 85, 98, 99, 100, 101));
		this.numberToButtons[1] = new ArrayList<Integer>(Arrays.asList(5, 21, 37, 53, 69, 85, 101));
		this.numberToButtons[2] = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 21, 37, 50, 51, 52, 53, 66, 82, 98, 99, 100, 101));
		this.numberToButtons[3] = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 21, 37, 50, 51, 52, 53, 69, 85, 98, 99, 100, 101));
		this.numberToButtons[4] = new ArrayList<Integer>(Arrays.asList(2, 5, 18, 21, 34, 37, 50, 51, 52, 53, 69, 85, 101));
		this.numberToButtons[5] = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 18, 34, 50, 51, 52, 53, 69, 85, 98, 99, 100, 101));
		this.numberToButtons[6] = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 18, 34, 50, 51, 52, 53, 66, 69, 82, 85, 98, 99, 100, 101));
		this.numberToButtons[7] = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 21, 37, 53, 69, 85, 101));
		this.numberToButtons[8] = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 18, 21, 34, 37, 50, 51, 52, 53, 66, 69, 82, 85, 98, 99, 100, 101));
		this.numberToButtons[9] = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 18, 21, 34, 37, 50, 51, 52, 53, 69, 85, 98, 99, 100, 101));
	}

	@Override
	public void send(MidiMessage message, long timeStamp)
	{
		// Use this for capturing which buttons got pressed.
	}

	@Override
	public void close()
	{
		// do nothing
	}

	public void clear()
	{
		if(this.rcvr == null)
		{
			return;
		}

		for(Integer i : (ArrayList<Integer>) this.numberToButtons[this.lastNumReceived])
		{
			try {
				rcvr.send(new ShortMessage(ShortMessage.NOTE_OFF, i, NOTE_OFF), -1);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
	}

	public void drawNumber(int number)
	{
		if(this.rcvr == null || number > 9 || number < 0)
		{
			return;
		}

		ArrayList<Integer> buttonsToIllluminate = this.numberToButtons[number];

		if(this.lastNumReceived != -1)
			this.clear();

		this.lastNumReceived = number;

		for(Integer buttonCode : buttonsToIllluminate)
		{
			try {
				rcvr.send(new ShortMessage(ShortMessage.NOTE_ON, buttonCode, GREEN_VEL), -1);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
	}


	public void setReciever(Receiver receiver)
	{
		this.rcvr = receiver;
	}
}
