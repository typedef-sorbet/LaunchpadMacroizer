package Gooey;

import Runnables.Junk.FindingFocus;
import Utility.CustomReceiver;
import Utility.Profile;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import sun.plugin.dom.exception.InvalidStateException;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Customizer extends JFrame
{
	private CustomReceiver receiverToModify;

	private JPanel panel1;
	private JButton editButton;
	private JButton clearButton;
	private SwingWorker<String, String> focusObserver;

	// appProfiles: Program Name -> Profile Object
	// rawRegistry: Program Name -> File containing Profile info
	private HashMap<String, String> rawRegistry;
	private HashMap<String, Profile> appProfiles;

	// TODO add cleanup function that cancels all background threads and closes necessary variables.

	public Customizer(CustomReceiver rcvr)
	{
		loadRegistry();

		receiverToModify = rcvr;

		// Menu bar setup

		JMenuBar menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem save = new JMenuItem("Save Profile As...");
		save.addActionListener(e -> save());

		JMenuItem load = new JMenuItem("Load Profile...");
		load.addActionListener(e -> load());

		JMenuItem register = new JMenuItem("Register Profile to Program...");
		register.addActionListener(e -> registerApplication());

		fileMenu.add(save);
		fileMenu.add(load);
		fileMenu.addSeparator();
		fileMenu.add(register);
		menu.add(fileMenu);
		setJMenuBar(menu);

		editButton.addActionListener(e -> getNewActionFromDialog());

		clearButton.addActionListener(e -> getClearFromDialog());

		setupFocusObserver();

		setTitle("Launchpad Macro-izer");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setSize(300, 100);
		add(panel1);
		setVisible(true);
	}

	private void save()
	{
		final JFileChooser fileDialog = new JFileChooser();
		int val = fileDialog.showSaveDialog(Customizer.this);
		if (val == JFileChooser.APPROVE_OPTION) {
			File file = fileDialog.getSelectedFile();
			receiverToModify.saveProfile(fileDialog.getSelectedFile());

			// if the file we just saved is in the registry, reload it

			reloadRegistry(file.getPath());
		}
	}

	private void load()
	{
		int res = JOptionPane.YES_OPTION;
		if(hasUnsavedWork())
		{
			String[] options = {"Don't Save", "Save", "Cancel"};
			res = JOptionPane.showOptionDialog(this,
					"The profile you are working on has unsaved edits.\nWould you like to save?",
					"Unsaved Work",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					"Save"
					);
		}
		if(res == JOptionPane.CANCEL_OPTION)
			return;
		else if(res == JOptionPane.NO_OPTION)
		{
			save();
		}

		final JFileChooser fileDialog = new JFileChooser();
		int val = fileDialog.showOpenDialog(Customizer.this);
		if (val == JFileChooser.APPROVE_OPTION) {
			receiverToModify.loadProfile(fileDialog.getSelectedFile());

		}
	}

	public boolean hasUnsavedWork()
	{
		return receiverToModify.hasUnsavedWork();
	}

	private void registerApplication()
	{
		String[] applications = FindingFocus.getNamesOfAllWindows();
		if(applications == null)
			throw new IllegalStateException("Unable to get names of windows from FindingFocus");

		String choice = (String) JOptionPane.showInputDialog(null, "Please choose an application to assign a profile to.",
				"Application Registry", JOptionPane.QUESTION_MESSAGE, null, applications, applications[0]);

		String[] choiceTokens = choice.split("- ");
		choice = choiceTokens[choiceTokens.length - 1];

		int res = JOptionPane.YES_OPTION;

		if(appProfiles.containsKey(choice))
		{
			res = JOptionPane.showConfirmDialog(this,
										"This program already has a profile associated with it. Do you want to assign a new profile to this program?",
											"Profile Already Mapped",
												JOptionPane.YES_NO_OPTION);
		}

		if(res == JOptionPane.YES_OPTION)
		{
			final JFileChooser fileDialog = new JFileChooser();
			int val = fileDialog.showOpenDialog(Customizer.this);
			if (val == JFileChooser.APPROVE_OPTION) {
				File profileFile = fileDialog.getSelectedFile();

				// update appProfiles

				Profile newProfile = new Profile();
				newProfile.loadProfile(profileFile);
				if(appProfiles.containsKey(choice))
					appProfiles.replace(choice, newProfile);
				else
					appProfiles.put(choice, newProfile);

				// update rawRegistry

				if(rawRegistry.containsKey(choice))
					rawRegistry.replace(choice, profileFile.getPath());
				else
					rawRegistry.put(choice, profileFile.getPath());

			}
		}
	}

	private void reloadRegistry(String fileName)
	{
		// appProfiles: Program Name -> Profile Object
		// rawRegistry: Program Name -> File containing Profile info

		System.out.println("Checking validity of registry over file " + fileName);

		// TODO this is O(n), is there a better way to do this?
		if(rawRegistry.containsValue(fileName))
		{
			// We've exported a file that the registry points to.
			// For each mapping in appProfiles that references the Profile referenced by fileName, we must replace it.
			Profile reload = new Profile();
			reload.loadProfile(new File(fileName));

			for(String program : rawRegistry.keySet())
			{
				if(rawRegistry.get(program).equals(fileName))
				{
					appProfiles.replace(program, reload);
				}
			}
		}
	}

	private void loadRegistry()
	{
		appProfiles = new HashMap<>();
		rawRegistry = new HashMap<>();

		File registry = new File("registry.json");

		JsonReader reader = null;

		try {
			reader = new JsonReader(new FileReader(registry));

		/*	General structure of registry.json:

			[
				{ "app_1": "profile_file_1" },
				{ "app_2": "profile_file_2" },
				...
			]

		 */
			reader.beginArray();

			while(reader.hasNext())
			{
				reader.beginObject();

				while(reader.hasNext())
				{
					String nextApp = reader.nextName();
					String nextFile = reader.nextString();

					Profile profile = new Profile();
					profile.loadProfile(new File(nextFile));

					System.out.println("Adding registry binding: " + nextApp + " -> " + nextFile);
					appProfiles.put(nextApp, profile);
					rawRegistry.put(nextApp, nextFile);
				}

				reader.endObject();
			}

			reader.endArray();

		}catch(IOException e) {
			e.printStackTrace();
			System.err.println("Unable to load registry, aborting...");
			System.exit(1);
		}

	}

	private void setupFocusObserver()
	{
		focusObserver = new SwingWorker<String, String>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				String previousFocus = FindingFocus.getNameOfFocusedWindow();

				publish(previousFocus);

				while(true)
				{
					if(Thread.currentThread().isInterrupted())
					{
						throw new InterruptedException("focusObserver interrupted while observing");
					}

					String currentFocus = FindingFocus.getNameOfFocusedWindow();

					if(currentFocus == null)
					{
						continue;
					}

					String[] tokens = currentFocus.split("- ");
					currentFocus = tokens[tokens.length-1];

					if(currentFocus != null && !currentFocus.equals(previousFocus))
					{
						publish(currentFocus);
						previousFocus = currentFocus;
					}
					else
					{
						Thread.sleep(100);
					}
				}
			}

			@Override
			protected void process(List<String> chunks)
			{
				String app = chunks.get(chunks.size() - 1);
				System.out.println(app);
				if (appProfiles.containsKey(app)) {
					System.out.println("Swapping profiles...");
					receiverToModify.setActiveProfile(appProfiles.get(app));
				} else {
					System.out.println("Resetting profile...");
					receiverToModify.resetActiveProfile();
				}
			}

			@Override
			protected void done()
			{
				throw new InvalidStateException("Something has gone wrong, focusObserver finished unexpectedly");
			}
		};

		focusObserver.execute();
	}

	public void setData(Customizer data)
	{
	}

	public void getData(Customizer data)
	{
	}

	public boolean isModified(Customizer data)
	{
		return false;
	}

	public void getNewActionFromDialog()
	{
		// This method was an absolute CLUSTERFUCK to code so I'm commenting it now so I never forget why it's written like this.

		JFrame askFrame = new JFrame("Button Mapping");

		receiverToModify.enabled = false;

		// Create a new background worker thread that listens to the MIDI coming in, and exits when:
		//		A) A new MIDI button has been pressed
		//		B) The cancel button has been pressed
		// This background thread executes in the background, *AWAY FROM THE EVENT DISPATCH THREAD* so that the application can continue running while listening occurs.
		SwingWorker<Integer, String> midiListener = new SwingWorker<Integer, String>()
		{
			// This is where the listening happens.
			@Override
			protected Integer doInBackground() throws Exception
			{
				// Grab the last recorded note value.
				int noteVal = receiverToModify.lastNoteVal;

				// Loop until we hear a new value or get cancelled
				while(true)
				{
					// If we've gotten interrupted by midiListener.cancel() ...
					if(Thread.currentThread().isInterrupted())
					{
						throw new InterruptedException("Worker interrupted while listening.");
					}

					// If the current note value is still what we've recorded, sleep for 1 millisecond, then check again.
					// Otherwise, we're got a hit; break the loop.
					// noinspection ConstantConditions
					if(receiverToModify.lastNoteVal == noteVal)
					{
						Thread.sleep(1);
					}
					else
					{
						break;
					}
				}

				// I use abs() here because receiverToModify will flip the sign of lastNoteVal if it sees a button pressed twice in a row.
				// It does this because we want to preserve the numerical value of the note, but also numerically signal that this is a new press.
				// This covers both cases.
				return Math.abs(receiverToModify.lastNoteVal);
			}

			// The Event Dispatch Thread will run this method once doInBackground() is done executing.
			// As such, we have access to all Swing objects that the EDT would normally have access to.
			// Since we want the mapping to happen after a button is pressed, but we also want the application to continue running while we wait for said press,
			// we make the mapping happen as a result of an "event" caused by the press.
			// In this case, that event is midiListener.doInBackground() finishing.
			protected void done()
			{
				// Thanos snap that frame, we don't need it anymore
				askFrame.setVisible(false);

				receiverToModify.enabled = true;

				// Default to a cancel code; this structure necessitates an initialization value
				int code = -1;

				// Try and grab the note value that midiListener grabbed.
				// If we're unsuccessful, that means that the thread got cancelled, so don't bother trying to map.
				try {
					code = get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}

				// If we were successful in getting a note value, do the mapping.
				if(code != -1)
					completeMapping(code);
			}
		};

		// Here's where we populate the askFrame and start the listener.
		// We want this to happen one statement after the other, so we shove it into an invokeLater to ensure that that happens.
		// It also has the nice benefit of letting the EDT execute this block at its leisure.
		SwingUtilities.invokeLater(() -> {
			// JFrame housekeeping, this part isn't important
			askFrame.getContentPane().setLayout(new GridLayout(2,2));
			askFrame.getContentPane().add(new JLabel("Please press the button you would like to edit."));
			JButton cancelButton = new JButton("Cancel");
			askFrame.getContentPane().add(cancelButton);
			askFrame.pack();

			// When the cancel button is pressed, cancel the midiListener thread. This will cause it to exit with an InterruptedException, and prevent a mapping from occurring.
			cancelButton.addActionListener(e -> {
				midiListener.cancel(true);
			});

			// Start the listener thread,
			midiListener.execute();

			// Now that everything is in place, pop up the JFrame and prompt the user.
			askFrame.setVisible(true);
		});
	}

	public void getClearFromDialog()
	{
		// This method was an absolute CLUSTERFUCK to code so I'm commenting it now so I never forget why it's written like this.

		// Create the JFrame that asks the user to press a button. Will be populated and set visible later.
		JFrame askFrame = new JFrame("Button Clearing");

		receiverToModify.enabled = false;

		// Create a new background worker thread that listens to the MIDI coming in, and exits when:
		//		A) A new MIDI button has been pressed
		//		B) The cancel button has been pressed
		// This background thread executes in the background, *AWAY FROM THE EVENT DISPATCH THREAD* so that the application can continue running while listening occurs.
		SwingWorker<Integer, String> midiListener = new SwingWorker<Integer, String>()
		{
			// This is where the listening happens.
			@Override
			protected Integer doInBackground() throws Exception
			{
				// Grab the last recorded note value.
				int noteVal = receiverToModify.lastNoteVal;

				// Loop until we hear a new value or get cancelled
				while(true)
				{
					// If we've gotten interrupted by midiListener.cancel() ...
					if(Thread.currentThread().isInterrupted())
					{
						throw new InterruptedException("Worker interrupted while listening.");
					}

					// If the current note value is still what we've recorded, sleep for 1 millisecond, then check again.
					// Otherwise, we're got a hit; break the loop.
					// noinspection ConstantConditions
					if(receiverToModify.lastNoteVal == noteVal)
					{
						Thread.sleep(1);
					}
					else
					{
						break;
					}
				}

				// I use abs() here because receiverToModify will flip the sign of lastNoteVal if it sees a button pressed twice in a row.
				// It does this because we want to preserve the numerical value of the note, but also numerically signal that this is a new press.
				// This covers both cases.
				return Math.abs(receiverToModify.lastNoteVal);
			}

			// The Event Dispatch Thread will run this method once doInBackground() is done executing.
			// As such, we have access to all Swing objects that the EDT would normally have access to.
			// Since we want the mapping to happen after a button is pressed, but we also want the application to continue running while we wait for said press,
			// we make the mapping happen as a result of an "event" caused by the press.
			// In this case, that event is midiListener.doInBackground() finishing.
			protected void done()
			{
				// Thanos snap that frame, we don't need it anymore
				askFrame.setVisible(false);

				receiverToModify.enabled = true;

				// Default to a cancel code; this structure necessitates an initialization value
				int code = -1;

				// Try and grab the note value that midiListener grabbed.
				// If we're unsuccessful, that means that the thread got cancelled, so don't bother trying to map.
				try {
					code = get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}

				// If we were successful in getting a note value, do the clearing.
				if(code != -1)
					completeClear(code);
			}
		};

		// Here's where we populate the askFrame and start the listener.
		// We want this to happen one statement after the other, so we shove it into an invokeLater to ensure that that happens.
		// It also has the nice benefit of letting the EDT execute this block at its leisure.
		SwingUtilities.invokeLater(() -> {
			// JFrame housekeeping, this part isn't important
			askFrame.getContentPane().setLayout(new GridLayout(2,2));
			askFrame.getContentPane().add(new JLabel("Please press the button you would like to clear."));
			JButton cancelButton = new JButton("Cancel");
			askFrame.getContentPane().add(cancelButton);
			askFrame.pack();

			// When the cancel button is pressed, cancel the midiListener thread. This will cause it to exit with an InterruptedException, and prevent a mapping from occurring.
			cancelButton.addActionListener(e -> {
				midiListener.cancel(true);
			});

			// Start the listener thread,
			midiListener.execute();

			// Now that everything is in place, pop up the JFrame and prompt the user.
			askFrame.setVisible(true);
		});
	}

	public void completeMapping(int buttonToAssign)
	{
		System.out.println("Button detected as " + buttonToAssign);

		String[] options = {"Add Command", "Add Keystroke"};
		Profile currentProfile = receiverToModify.getActiveProfile();

		String dialogText;
		if (currentProfile.getCommand(buttonToAssign) != null) {
			String currentAssignment = (currentProfile.getRawCommands().get(buttonToAssign) != null) ? currentProfile.getRawCommands().get(buttonToAssign) : currentProfile.getRawKeystrokes().get(buttonToAssign);
			dialogText = String.format("Would you like to modify this button's assignment? (current buttonValue: %s)", currentAssignment);
		} else {
			dialogText = "Would you like to add a command or keystroke?";
		}

		int which = JOptionPane.showOptionDialog(null, dialogText, "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, "Add Command");
		if (which == JOptionPane.YES_OPTION) {
			// command
			String command = JOptionPane.showInputDialog("Please enter the command you want to assign.");
			if (command == null) return;

			receiverToModify.addCommand(buttonToAssign, command);

//			buttons[Arrays.asList(noteVals).indexOf(buttonToAssign)].setToolTipText(command);
		} else if (which == JOptionPane.NO_OPTION) {
			// keystroke
			String keys = JOptionPane.showInputDialog("Please enter the keystroke you want to assign.");
			if (keys == null) return;

			receiverToModify.addKeystroke(buttonToAssign, keys);

//			buttons[Arrays.asList(noteVals).indexOf(buttonToAssign)].setToolTipText(keys);
		}
	}

	public void completeClear(int buttonToClear)
	{
		receiverToModify.removeAction(buttonToClear);
	}

	// basically a long-ass translation table
	//TODO add more keys to this
	public static ArrayList<Integer> keysToCodes(List<String> keys)
	{
		ArrayList<Integer> keysToPress = new ArrayList<>();
		for (String k : keys) {
			switch (k) {
				case "Ctrl":
				case "ctrl":
					keysToPress.add(KeyEvent.VK_CONTROL);
					break;

				case "enter":
				case "Enter":
				case "return":
				case "Return":
					keysToPress.add(KeyEvent.VK_ENTER);
					break;

				case "Shift":
				case "shift":
					keysToPress.add(KeyEvent.VK_SHIFT);
					break;

				case "alt":
				case "Alt":
					keysToPress.add(KeyEvent.VK_ALT);
					break;

				case "delete":
				case "Delete":
				case "del":
					keysToPress.add(KeyEvent.VK_DELETE);
					break;

				case "insert":
				case "Insert":
					keysToPress.add(KeyEvent.VK_INSERT);

				case "a":
				case "A":
					keysToPress.add(KeyEvent.VK_A);
					break;

				case "b":
				case "B":
					keysToPress.add(KeyEvent.VK_B);
					break;

				case "c":
				case "C":
					keysToPress.add(KeyEvent.VK_C);
					break;

				case "d":
				case "D":
					keysToPress.add(KeyEvent.VK_D);
					break;

				case "e":
				case "E":
					keysToPress.add(KeyEvent.VK_E);
					break;

				case "f":
				case "F":
					keysToPress.add(KeyEvent.VK_F);
					break;

				case "g":
				case "G":
					keysToPress.add(KeyEvent.VK_G);
					break;

				case "h":
				case "H":
					keysToPress.add(KeyEvent.VK_H);
					break;

				case "i":
				case "I":
					keysToPress.add(KeyEvent.VK_I);
					break;

				case "j":
				case "J":
					keysToPress.add(KeyEvent.VK_J);
					break;

				case "k":
				case "K":
					keysToPress.add(KeyEvent.VK_K);
					break;

				case "l":
				case "L":
					keysToPress.add(KeyEvent.VK_L);
					break;

				case "m":
				case "M":
					keysToPress.add(KeyEvent.VK_M);
					break;

				case "n":
				case "N":
					keysToPress.add(KeyEvent.VK_N);
					break;

				case "o":
				case "O":
					keysToPress.add(KeyEvent.VK_O);
					break;

				case "p":
				case "P":
					keysToPress.add(KeyEvent.VK_P);
					break;

				case "q":
				case "Q":
					keysToPress.add(KeyEvent.VK_Q);
					break;

				case "r":
				case "R":
					keysToPress.add(KeyEvent.VK_R);
					break;

				case "s":
				case "S":
					keysToPress.add(KeyEvent.VK_S);
					break;

				case "t":
				case "T":
					keysToPress.add(KeyEvent.VK_T);
					break;

				case "u":
				case "U":
					keysToPress.add(KeyEvent.VK_U);
					break;

				case "v":
				case "V":
					keysToPress.add(KeyEvent.VK_V);
					break;

				case "w":
				case "W":
					keysToPress.add(KeyEvent.VK_W);
					break;

				case "x":
				case "X":
					keysToPress.add(KeyEvent.VK_X);
					break;

				case "y":
				case "Y":
					keysToPress.add(KeyEvent.VK_Y);
					break;

				case "z":
				case "Z":
					keysToPress.add(KeyEvent.VK_Z);
					break;

				case "F1":
				case "f1":
					keysToPress.add(KeyEvent.VK_F1);
					break;

				case "F2":
				case "f2":
					keysToPress.add(KeyEvent.VK_F2);
					break;

				case "F3":
				case "f3":
					keysToPress.add(KeyEvent.VK_F3);
					break;

				case "F4":
				case "f4":
					keysToPress.add(KeyEvent.VK_F4);
					break;

				case "F5":
				case "f5":
					keysToPress.add(KeyEvent.VK_F5);
					break;

				case "F6":
				case "f6":
					keysToPress.add(KeyEvent.VK_F6);
					break;

				case "F7":
				case "f7":
					keysToPress.add(KeyEvent.VK_F7);
					break;

				case "F8":
				case "f8":
					keysToPress.add(KeyEvent.VK_F8);
					break;

				case "F9":
				case "f9":
					keysToPress.add(KeyEvent.VK_F9);
					break;

				case "F10":
				case "f10":
					keysToPress.add(KeyEvent.VK_F10);
					break;

				case "F11":
				case "f11":
					keysToPress.add(KeyEvent.VK_F11);
					break;

				case "F12":
				case "f12":
					keysToPress.add(KeyEvent.VK_F12);
					break;

				default:
					JOptionPane.showMessageDialog(null, "Error: one or more keys could not be recognized.\nPlease try again.",
							"Error: Invalid Keystroke", JOptionPane.ERROR_MESSAGE);
					return null;
			}
		}

		return keysToPress;
	}

	public void saveRegistry()
	{
		// At this point, we're closing, so let's save the registry we have in rawRegistry to file
		System.out.println("Saving registry...");
		try {
			JsonWriter writer = new JsonWriter(new FileWriter(new File("registry.json")));
			writer.beginArray();

			for(String program : rawRegistry.keySet())
			{
				writer.beginObject();
				writer.name(program).value(rawRegistry.get(program));
				writer.endObject();
			}

			writer.endArray();
			writer.close();
			System.out.println("Saved registry.");
		}catch(IOException ioe)
		{
			ioe.printStackTrace();
			System.err.println("Unable to save registry.");
		}
	}
}
