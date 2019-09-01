package Utility;

import javax.sound.midi.*;
import java.util.*;

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
	private int lastNumDrawn;

	private HashSet[] buttonBanks;
	private ArrayList<Integer> correctCode;
	private int codeIndex;

	private int state;

	public static final int MUSIC_STATE = 1;
	public static final int CODE_STATE = 2;


	public GlowUpReceiver()
	{
		this.numberToButtons = new ArrayList[10];
		this.buttonBanks = new HashSet[5];
		this.lastNumDrawn = -1;
		this.state = MUSIC_STATE;

		// population
		// TODO uncomment
//		this.correctCode = new ArrayList<>(Arrays.asList(0, 1, 0, 4, 0, 4, 0, 1));
		this.correctCode = new ArrayList<>(Arrays.asList(1, 1, 1, 1));
		this.codeIndex = 0;

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

		this.buttonBanks[0] = new HashSet<Integer>(Arrays.asList(0, 1, 16, 17));
		this.buttonBanks[1] = new HashSet<Integer>(Arrays.asList(6, 7, 22, 23));
		this.buttonBanks[2] = new HashSet<Integer>(Arrays.asList(51, 52, 67, 68));
		this.buttonBanks[3] = new HashSet<Integer>(Arrays.asList(96, 97, 112, 113));
		this.buttonBanks[4] = new HashSet<Integer>(Arrays.asList(102, 103, 118, 119));
	}

	@Override
	public void send(MidiMessage message, long timeStamp)
	{
		// Use this for capturing which buttons got pressed.

		// TODO how do I "de-queue" buttons pressed while in CODE_STATE?
		// Does it have to do with my usage of Thread.sleep? Should I use some other implementation?

		byte[] messageBytes = message.getMessage();
		int noteVal = messageBytes[1], onOrOff = messageBytes[2];

		if(MUSIC_STATE == this.state)
		{
			if(onOrOff == NOTE_ON)
			{
				int buttonBankPressed = 0;
				for(; buttonBankPressed < 5; ++buttonBankPressed)
				{
					if (this.buttonBanks[buttonBankPressed].contains(noteVal))
					{
						break;
					}
				}

				// buttonBankPressed is now the "code" of the button bank that was pressed.

				if(buttonBankPressed < 5)
				{
					System.out.println("You pressed bank " + buttonBankPressed);
					// TODO play the corresponding sound

					// is it the correct next one?
					if (buttonBankPressed == correctCode.get(codeIndex))
					{
						++codeIndex;
						if(codeIndex == correctCode.size())
						{
							// show code here
							drawNumberSequence(this.correctCode);

							this.codeIndex = 0;
						}
					}
					else
					{
						codeIndex = 0;
					}
				}
			}
		}
		else
		{
			return;
		}
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

		if(this.state == CODE_STATE)
		{
			for (Integer i : (ArrayList<Integer>) this.numberToButtons[this.lastNumDrawn]) {
				try {
					rcvr.send(new ShortMessage(ShortMessage.NOTE_OFF, i, NOTE_OFF), -1);
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				}
			}
		}
		else
		{
			for(HashSet<Integer> set : this.buttonBanks)
			{
				for(Integer i : set)
				{
					try {
						rcvr.send(new ShortMessage(ShortMessage.NOTE_OFF, i, NOTE_OFF), -1);
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void drawNumber(int number)
	{
		if(this.rcvr == null || number > 9 || number < 0)
		{
			return;
		}

		ArrayList<Integer> buttonsToIlluminate = this.numberToButtons[number];

		if(this.lastNumDrawn != -1)
			this.clear();

		this.state = CODE_STATE;

		this.lastNumDrawn = number;

		for(Integer buttonCode : buttonsToIlluminate)
		{
			try {
				rcvr.send(new ShortMessage(ShortMessage.NOTE_ON, buttonCode, GREEN_VEL), -1);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
	}

	public void drawNumberSequence(ArrayList<Integer> digits)
	{
		this.clear();

		this.state = CODE_STATE;

		for(Integer d : digits)
		{
			this.drawNumber(d);

			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		this.drawMusicBanks();
	}

	public void drawMusicBanks()
	{
		this.clear();

		this.state = MUSIC_STATE;

		for(HashSet<Integer> set : this.buttonBanks)
		{
			for(Integer i : set)
			{
				try {
					rcvr.send(new ShortMessage(ShortMessage.NOTE_ON, i, YELLOW_VEL), -1);
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				}
			}
		}
	}


	public void setReciever(Receiver receiver)
	{
		this.rcvr = receiver;
	}
}
