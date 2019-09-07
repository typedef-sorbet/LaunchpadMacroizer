package Utility;

import javax.sound.midi.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
	private final ArrayList<Integer> lockCode;
	private int codeIndex;

	private int state;
//	private ArrayList<Clip> audio_clips;

	public static final int MUSIC_STATE = 1;
	public static final int CODE_STATE = 2;
	public static final int FINALE_NUM = 5;
	public static final String[] FILENAMES = {"0.wav", "1.wav", "incorrect-1.wav", "incorrect-2.wav", "2.wav", "finale.wav"};


	public GlowUpReceiver()
	{
		this.numberToButtons = new ArrayList[10];
		this.buttonBanks = new HashSet[5];
		this.lastNumDrawn = -1;
		this.state = MUSIC_STATE;

		// population
		this.correctCode = new ArrayList<>(Arrays.asList(0, 1, 0, 4, 0, 4, 0, 1));
		this.lockCode = new ArrayList<>(Arrays.asList(1, 6, 4, 3, 6));
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

//		this.audio_clips = new ArrayList<>();
//		for (int i = 0; i < FILENAMES.length; i++)
//		{
//			try {
//				Clip clip = null;
//				clip = AudioSystem.getClip();
//				clip.open(AudioSystem.getAudioInputStream(new File("rsc/notes/" + FILENAMES[i])));
//				this.audio_clips.add(clip);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
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
					playSound(buttonBankPressed);

					// is it the correct next one?
					if (buttonBankPressed == correctCode.get(codeIndex))
					{
						++codeIndex;
						if(codeIndex == correctCode.size())
						{
							playSound(FINALE_NUM);

							// show code here
							drawNumberSequence(this.lockCode);

							this.codeIndex = 0;
						}
					}
					else
					{
						codeIndex = (buttonBankPressed != 5 && buttonBankPressed == correctCode.get(0) ? 1 : 0);
					}
				}
			}


			System.out.println("New code index: " + codeIndex);
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

	private void playSound(int sound_num)
	{
		try {
			Process play_sound = new ProcessBuilder().command("cvlc", "rsc/notes/" + FILENAMES[sound_num], "--play-and-exit").inheritIO().start();
		} catch (IOException e) {
			e.printStackTrace();
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
		GlowUpReceiver inst = this;

		this.clear();

		this.state = CODE_STATE;

		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);

		for(int multiplier = 0; multiplier < digits.size(); ++multiplier)
		{
			final int timeDelay = 1500 * multiplier;

			stpe.schedule(() -> inst.drawNumber(digits.get(timeDelay / 950)), timeDelay, TimeUnit.MILLISECONDS);
		}

		stpe.schedule(inst::drawMusicBanks, (digits.size() + 1) * 950, TimeUnit.MILLISECONDS);
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
