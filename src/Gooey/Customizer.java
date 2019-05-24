package Gooey;

import Utility.CustomReceiver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Customizer extends JFrame
{
	private CustomReceiver receiverToModify;

	// I'm so sorry
	private JPanel panel1;
	private JButton editButton;
	private JButton clearButton;
	private JButton saveButton;
	private JButton loadButton;

	private int buttonValue;

	public Customizer(CustomReceiver rcvr)
	{

		receiverToModify = rcvr;

		// TODO give clearButton and editButton ActionListeners

		editButton.addActionListener(e -> {
			getNewActionFromDialog();
		});

		saveButton.addActionListener(e -> {
			final JFileChooser fileDialog = new JFileChooser();
			int val = fileDialog.showSaveDialog(Customizer.this);
			if (val == JFileChooser.APPROVE_OPTION) {
				receiverToModify.saveProfile(fileDialog.getSelectedFile());
			}
		});

		loadButton.addActionListener(e -> {
			final JFileChooser fileDialog = new JFileChooser();
			int val = fileDialog.showOpenDialog(Customizer.this);
			if (val == JFileChooser.APPROVE_OPTION) {
				receiverToModify.loadProfile(fileDialog.getSelectedFile());
			}
		});

		setTitle("Launchpad Macro-izer");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setSize(600, 100);
		add(panel1);
		setVisible(true);
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

	public int waitOnNextPress()
	{
		Thread listenerThread = new Thread(() -> {
			final int noteVal = receiverToModify.lastNoteVal;

			while (true) {
				//noinspection ConstantConditions
				if (receiverToModify.lastNoteVal == noteVal) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					break;
				}
			}

			this.buttonValue = receiverToModify.lastNoteVal;
		});

		listenerThread.start();

		try {
			listenerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return Math.abs(this.buttonValue);
	}


	public void getNewActionFromDialog()
	{
		// This method was an absolute CLUSTERFUCK to code so I'm commenting it now so I never forget why it's written like this.

		// Create the JFrame that asks the user to press a button. Will be populated and set visible later.
		JFrame askFrame = new JFrame("Button Mapping");

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

	public void completeMapping(int buttonToAssign)
	{
		System.out.println("Button detected as " + buttonToAssign);

		String[] options = {"Add Command", "Add Keystroke"};
		CustomReceiver.Profile currentProfile = receiverToModify.getProfile();

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

}
