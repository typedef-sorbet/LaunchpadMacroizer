package Utility;

import Gooey.Customizer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CustomReceiver implements Receiver
{
	public static final CustomReceiver INSTANCE = new CustomReceiver();

	private Receiver rcvr;
	private Robot virtualKeyboard = null;
	public static final int NOTE_ON = 127;
	public static final int NOTE_OFF = 0;
	private Profile activeProfile;			// Profile active on Launchpad
	private Profile workingProfile;			// Profile being edited currently (may be inactive due to current window focus)

	public boolean enabled;

	public static final int RED_VEL		= 3;
	public static final int GREEN_VEL	= 48;
	public static final int YELLOW_VEL	= 50;
	public static final int ORANGE_VEL	= 51;

	public int lastNoteVal = -1;

	public CustomReceiver()
	{
		super();
		enabled = true;
		try{
			this.virtualKeyboard = new Robot();
		}
		catch(AWTException awte)
		{
			awte.printStackTrace();
			System.exit(1);
		}

		workingProfile = new Profile();
		activeProfile = workingProfile;
	}

	@Override
	public void send(MidiMessage message, long timeStamp)
	{
		byte[] messageBytes = message.getMessage();

		// This is hacky as *shit* but it should still work
		if(messageBytes[2] == NOTE_ON) {
			if (lastNoteVal == messageBytes[1])
				lastNoteVal = -messageBytes[1];
			else
				lastNoteVal = messageBytes[1];
		}


		if(enabled && virtualKeyboard != null)
			handler(messageBytes[1], messageBytes[2]);
	}

	public void addCommand(int buttonCode, String action)
	{
		// TODO concurrent mod?
		workingProfile.addCommand(buttonCode, action);
		try {
			rcvr.send(new ShortMessage(ShortMessage.NOTE_ON, buttonCode, RED_VEL), -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public void addKeystroke(int buttonCode, String keystroke)
	{
		workingProfile.addKeystroke(buttonCode, keystroke);
		try {
			rcvr.send(new ShortMessage(ShortMessage.NOTE_ON, buttonCode, GREEN_VEL), -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public void removeAction(int buttonCode)
	{
		workingProfile.clearBinding(buttonCode);
		try {
			rcvr.send(new ShortMessage(ShortMessage.NOTE_OFF, buttonCode, NOTE_OFF), -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public Receiver getUnderlyingReceiver()
	{
		return rcvr;
	}

	@Override
	public void close()
	{
		rcvr.close();
	}
	
	/*
		(104)(105)(106)(107)(108)(109)(110)(111)
		+----+----+----+----+----+----+----+----+
		| 0  | 1  | 2  | 3  | 4  | 5  | 6  | 7  |( 8 )
		+----+----+----+----+----+----+----+----+
		| 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |( 24 )
		+----+----+----+----+----+----+----+----+
		| 32 | 33 | 34 | 35 | 36 | 37 | 38 | 39 |( 40 )
		+----+----+----+----+----+----+----+----+
		| 48 | 49 | 50 | 51 | 52 | 53 | 54 | 55 |( 56 )
		+----+----+----+----+----+----+----+----+
		| 64 | 65 | 66 | 67 | 68 | 69 | 70 | 71 |( 72 )
		+----+----+----+----+----+----+----+----+
		| 80 | 81 | 82 | 83 | 84 | 85 | 86 | 87 |( 88 )
		+----+----+----+----+----+----+----+----+
		| 96 | 97 | 98 | 99 |100 |101 |102 |103 |( 104 )
		+----+----+----+----+----+----+----+----+
		|112 |113 |114 |115 |116 |117 |118 |119 |( 120 )
		+----+----+----+----+----+----+----+----+
	 */

	public void handler(int noteVal, int onOrOff)
	{
//		System.out.println("Thank you for using NoteHandler!\nThe note you have pressed is " + noteVal);
		boolean on = (onOrOff == NOTE_ON);
		Runnable command = activeProfile.getCommand(noteVal);
		if(on && command != null)
			command.run();
	}

	public Robot getRobot()
	{
		return virtualKeyboard;
	}

	public void saveProfile(File s)
	{
		workingProfile.saveProfile(s);
	}

	public void loadProfile(File s)
	{
		workingProfile.loadProfile(s);
		redraw();
	}

	public Profile getActiveProfile()
	{
		return activeProfile;
	}

	public void setActiveProfile(@NotNull Profile profile)
	{
		activeProfile = profile;
		redraw();
	}

	public void resetActiveProfile()
	{
		if(!activeProfile.equals(workingProfile)) {
			activeProfile = workingProfile;
			redraw();
		}
	}

	public void redraw()
	{
		for(int i = 0; i < 127; i++)
		{
			try {
				rcvr.send(new ShortMessage(ShortMessage.NOTE_OFF, i, NOTE_OFF), -1);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}

		HashMap<Integer, String> commands = activeProfile.getRawCommands();
		HashMap<Integer, String> keystrokes = activeProfile.getRawKeystrokes();

		for(Integer i : commands.keySet())
		{
			try {
				rcvr.send(new ShortMessage(ShortMessage.NOTE_ON, i, RED_VEL), -1);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}

		for(Integer i : keystrokes.keySet())
		{
			try {
				rcvr.send(new ShortMessage(ShortMessage.NOTE_ON, i, GREEN_VEL), -1);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
	}

	public void setReciever(Receiver receiver)
	{
		rcvr = receiver;
	}
}
