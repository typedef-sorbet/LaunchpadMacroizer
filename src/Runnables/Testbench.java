package Runnables;

import Gooey.Customizer;
import Utility.CustomReceiver;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Testbench
{
	public static void main(String... args) throws MidiUnavailableException
	{
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
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

		CustomReceiver recv = new CustomReceiver((MidiSystem.getReceiver()));

		MidiSystem.getTransmitter().setReceiver(recv);

		for(Transmitter t : launchpad.getTransmitters())
		{
			t.setReceiver(recv);
		}

		//launchpad is now our launchpad midi device

		System.out.println(String.format("Transmitters: %d\nReceivers: %d", launchpad.getTransmitters().size(), launchpad.getReceivers().size()));

		Scanner console = new Scanner(System.in);

		Customizer cust = new Customizer(recv);

		while(cust.isVisible());

//		//input loop
//		while(console.hasNext())
//		{
//			String nextLine = console.nextLine();
//
//			if(nextLine.contains("Exit"))
//				break;
//
//			if(nextLine.contains("add")) {
//				if (nextLine.contains("command")) {
//					addCommand(console, recv);
//				}
//				if (nextLine.contains("keystroke")) {
//					addKeystroke(console, recv);
//				}
//			}
//			else if(nextLine.contains("remove"))
//			{
//				removeAction(console, recv);
//			}
//		}

		launchpad.close();

		System.out.println("Device closed.");

		recv.close();

	}

//	private static void removeAction(Scanner console, CustomReceiver recv)
//	{
//		System.out.println("Which button would you like to clear?");
//		int button = console.nextInt();
//		recv.removeAction(button);
//	}

//	private static void addCommand(Scanner console, CustomReceiver recv)
//	{
//		System.out.println("What command would you like to add?\n> ");
//		String command = console.nextLine();
//		List<String> args = Arrays.asList(command.split("\\s+"));
//		System.out.println("To which button?\n> ");
//		int button = console.nextInt();
//		recv.addAction(button, () -> {
//			try {
//				new ProcessBuilder().command(args).inheritIO().start();
//			}catch(IOException ioe)
//			{
//				ioe.printStackTrace();
//				System.exit(1);
//			}
//		});
//
//		// update the display on the launchpad
//		try {
//			recv.getUnderlyingReceiver().send(new ShortMessage(ShortMessage.NOTE_ON, button, CustomReceiver.NOTE_ON), -1);
//		} catch (InvalidMidiDataException e) {
//			e.printStackTrace();
//		}
//	}

//	private static void addKeystroke(Scanner console, CustomReceiver recv)
//	{
//		// TODO modify
//		System.out.println("What button would you like to map this keystroke to?");
//		int button = console.nextInt();
//		System.out.println("Please type out the keys you would like pressed, seperated by spaces, in the order you want them pressed.");
//		List<String> keys = Arrays.asList(console.nextLine().split("\\s+"));
//		ArrayList<Integer> keysToPress = new ArrayList<>();
//		for(String k : keys)
//		{
//			switch (k) {
//				case "Ctrl":
//				case "ctrl":
//					keysToPress.add(KeyEvent.VK_CONTROL);
//
//				case "C":
//				case "c":
//					keysToPress.add(KeyEvent.VK_C);
//			}
//		}
//
//		ArrayList<Integer> keysToRelease = (ArrayList<Integer>)keysToPress.clone();
//		keysToRelease.sort(Comparator.comparingInt(keysToPress::indexOf));
//
//		//TODO remove debug print
//		System.out.println(String.format("Normal order: %s\nReverse order: %s", keysToPress.toString(), keysToRelease.toString()));
//
//		recv.addAction(button, () -> {
//			Robot keyPresser = recv.getRobot();
//
//			for(int key : keysToPress)
//			{
//				keyPresser.keyPress(key);
//			}
//
//			for(int key : keysToRelease)
//			{
//				keyPresser.keyRelease(key);
//			}
//		});
//
//		// update the display on the launchpad
//		try {
//			recv.send(new ShortMessage(0), -1);
//		} catch (InvalidMidiDataException e) {
//			e.printStackTrace();
//		}
//	}


}
