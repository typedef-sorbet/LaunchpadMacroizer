package Utility;

import Gooey.Customizer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
					CustomReceiver.INSTANCE.getRobot().keyPress(keyCodes.get(i));
				}
				for(int i = keyCodes.size() - 1; i >= 0; i--)
				{
					CustomReceiver.INSTANCE.getRobot().keyRelease(keyCodes.get(i));
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

					switch(type)
					{
						case "command":
							addCommand(buttonCode, data);
							break;
						case "keystroke":
							addKeystroke(buttonCode, data);
							break;
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
