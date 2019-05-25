package Utility;

import Gooey.Customizer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CustomReceiver implements Receiver
{
	private final Receiver rcvr;
	private Robot virtualKeyboard = null;
	public static final int NOTE_ON = 127;
	public static final int NOTE_OFF = 0;
	private Profile currentProfile;

	public boolean enabled = true;

	public static final int RED_VEL		= 3;
	public static final int GREEN_VEL	= 48;
	public static final int YELLOW_VEL	= 50;
	public static final int ORANGE_VEL	= 51;

	public int lastNoteVal = -1;

	public static int velocityTest = 0;

	public CustomReceiver(Receiver rcvr)
	{
		super();
		this.rcvr = rcvr;
		enabled = true;
		try{
			this.virtualKeyboard = new Robot();
		}
		catch(AWTException awte)
		{
			awte.printStackTrace();
			System.exit(1);
		}

		currentProfile = new Profile();
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
		currentProfile.addCommand(buttonCode, action);
		try {
			rcvr.send(new ShortMessage(ShortMessage.NOTE_ON, buttonCode, RED_VEL), -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public void addKeystroke(int buttonCode, String keystroke)
	{
		currentProfile.addKeystroke(buttonCode, keystroke);
		try {
			rcvr.send(new ShortMessage(ShortMessage.NOTE_ON, buttonCode, GREEN_VEL), -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public void removeAction(int buttonCode)
	{
		currentProfile.clearBinding(buttonCode);
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
		Runnable command = currentProfile.getCommand(noteVal);
		if(on && command != null)
			command.run();
	}

	public Robot getRobot()
	{
		return virtualKeyboard;
	}

	public void saveProfile(File s)
	{
		currentProfile.saveProfile(s);
	}

	public void loadProfile(File s)
	{
		currentProfile.loadProfile(s);
	}

	public Profile getProfile()
	{
		return currentProfile;
	}

	public class Profile
	{
		public HashMap<Integer, Runnable> getBindings()
		{
			return bindings;
		}

		public HashMap<Integer, String> getRawCommands()
		{
			return rawCommands;
		}

		public HashMap<Integer, String> getRawKeystrokes()
		{
			return rawKeystrokes;
		}

		HashMap<Integer, Runnable> bindings;
		HashMap<Integer, String> rawCommands;
		HashMap<Integer, String> rawKeystrokes;

		public Profile()
		{
			bindings = new HashMap<>();
			rawCommands = new HashMap<>();
			rawKeystrokes = new HashMap<>();
		}

		public void addCommand(int code, String command)
		{
			clearBinding(code);

			rawCommands.put(code, command);
			List<String> args = Arrays.asList(command.split("\\s+"));
			bindings.put(code, () -> {
				try {
					new ProcessBuilder().command(args).inheritIO().start();
				}catch(IOException ioe)
				{
					ioe.printStackTrace();
					JOptionPane.showOptionDialog(null, "Error: Unable to run this command (did you type it properly?)", "Error",
							JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
				}
			});
		}

		public void addKeystroke(int code, String keys)
		{
			clearBinding(code);

			rawKeystrokes.put(code, keys);
			List<Integer> keyCodes = Customizer.keysToCodes(Arrays.asList(keys.split("\\s+")));
			if(keyCodes != null)
			{
				bindings.put(code, () -> {
					for(int i = 0; i < keyCodes.size(); i++)
					{
						virtualKeyboard.keyPress(keyCodes.get(i));
					}
					for(int i = keyCodes.size() - 1; i >= 0; i--)
					{
						virtualKeyboard.keyRelease(keyCodes.get(i));
					}
				});
			}
		}

		public void clearBinding(int code)
		{
			if(rawKeystrokes.get(code) != null)
				rawKeystrokes.remove(code);
			else
				rawCommands.remove(code);
			bindings.remove(code);
		}

		public Runnable getCommand(int code)
		{
			return bindings.get(code);
		}

		public void loadProfile(File file)
		{
			for(int k : bindings.keySet())
			{
				try {
					rcvr.send(new ShortMessage(ShortMessage.NOTE_OFF, k, NOTE_OFF), -1);
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				}
			}

			rawKeystrokes.clear();
			rawCommands.clear();
			bindings.clear();

			try
			{
				FileReader intermediate;
				try{
					intermediate = new FileReader(file);
				}catch(FileNotFoundException fnfe)
				{
					fnfe.printStackTrace();
					return;
				}

				JsonReader reader = new JsonReader(intermediate);

				reader.beginArray();

				while(reader.hasNext())
				{
					reader.beginObject();

					while(reader.hasNext())
					{
						reader.nextName();
						int buttonCode = reader.nextInt();
						reader.nextName();
						String type = reader.nextString();
						reader.nextName();
						String data = reader.nextString();
						System.out.println("Adding binding \"" + data + "\" to button " + buttonCode);

						switch (type)
						{
							case "command":
								CustomReceiver.this.addCommand(buttonCode, data);
								break;

							case "keystroke":
								CustomReceiver.this.addKeystroke(buttonCode, data);
						}
					}

					reader.endObject();
				}

				reader.endArray();


			}catch(IOException ioe)
			{
				ioe.printStackTrace();
				return;
			}
		}

		public void saveProfile(File file)
		{
			try{
				JsonWriter writer = new JsonWriter(new FileWriter(file));

				/*  {
						"noteValue": ~~~
						"type": <command or keystroke>
						"data": <str>
					}
				 */
				writer.beginArray();

				for(Integer i : rawCommands.keySet())
				{
					writer.beginObject();
					writer.name("noteValue").value(i);
					writer.name("type").value("command");
					writer.name("data").value(rawCommands.get(i));
					writer.endObject();
				}

				for(Integer i : rawKeystrokes.keySet())
				{
					writer.beginObject();
					writer.name("noteValue").value(i);
					writer.name("type").value("keystroke");
					writer.name("data").value(rawKeystrokes.get(i));
					writer.endObject();
				}
				writer.endArray();
				writer.close();
			}catch(IOException ioe)
			{
				ioe.printStackTrace();
				return;
			}
		}
	}
}
