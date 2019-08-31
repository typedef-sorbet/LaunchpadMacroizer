package Runnables;

import Gooey.Customizer;
import Utility.CustomReceiver;
import com.sun.jna.Library;
import com.sun.jna.Native;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

public class Testbench
{
	// You're right, I shouldn't hardcode this. Whoops.
	public static final String PROJECT_DIR = "/home/sanctity/IdeaProjects/MIDIFuckery/";

	public interface LibX extends Library {
		public static final LibX INSTANCE = Native.load("X11", LibX.class);

		int XInitThreads();
	}

	public static void main(String... args) throws MidiUnavailableException
	{
		int status = LibX.INSTANCE.XInitThreads();

		try {
			File f = new File(Customizer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			System.out.println(f.getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if(status == 0)
		{
			throw new IllegalStateException("Unable to initialize threads for X11");
		}

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		MidiDevice launchpad = null;
		MidiDevice current;
		for(MidiDevice.Info i : MidiSystem.getMidiDeviceInfo())
		{
			System.out.println(String.format("Name: %s\nVendor: %s\nDescription: %s\nVersion: %s\n", i.getName(), i.getVendor(), i.getDescription(), i.getVersion()));
			if(i.getName().contains("Launchpad"))
			{
				current = MidiSystem.getMidiDevice(i);
				launchpad = current;
				for(Transmitter t : current.getTransmitters())
				{
//					t.setReceiver(new CustomReceiver(MidiSystem.getReceiver()));
				}
				try{
					current.open();
				}
				catch (MidiUnavailableException mue)
				{
					mue.printStackTrace();
					System.exit(1);
				}
				System.out.println(String.format("Transmitters: %d\nReceivers: %d", launchpad.getTransmitters().size(), launchpad.getReceivers().size()));
				System.out.println(i.getName() + " successfully opened.");
				break;
			}
		}

		if(launchpad == null)
		{
			System.err.println("Error: unable to find Launchpad device");
			System.exit(1);
		}
		final MidiDevice launchFinal = launchpad;

		CustomReceiver recv = CustomReceiver.INSTANCE;
		recv.setReciever((MidiSystem.getReceiver()));

		MidiSystem.getTransmitter().setReceiver(recv);

		for(Transmitter t : launchpad.getTransmitters())
		{
			t.setReceiver(recv);
		}


		//launchpad is now our launchpad midi device

		System.out.println(String.format("Transmitters: %d\nReceivers: %d", launchpad.getTransmitters().size(), launchpad.getReceivers().size()));

		Scanner console = new Scanner(System.in);

		Customizer cust = new Customizer(recv);

		cust.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				int res = JOptionPane.YES_OPTION;
				if(cust.hasUnsavedWork())
				{
					String[] options = {"Don't Save", "Save", "Cancel"};
					res = JOptionPane.showOptionDialog(cust,
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
					cust.save();
				}

				cust.dispose();
				System.exit(0);

//				cust.saveRegistry();
//
//				launchFinal.close();
//
//				System.out.println("Device closed.");
//
//				recv.close();
//
//				System.exit(0);
			}
		});
	}
}
