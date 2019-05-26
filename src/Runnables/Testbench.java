package Runnables;

import Gooey.Customizer;
import Utility.CustomReceiver;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Testbench
{
	public static void main(String... args) throws MidiUnavailableException
	{
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

		cust.addWindowListener(new WindowListener()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{

			}

			@Override
			public void windowClosing(WindowEvent e)
			{

			}

			@Override
			public void windowClosed(WindowEvent e)
			{
				launchFinal.close();

				System.out.println("Device closed.");

				recv.close();

				System.exit(0);
			}

			@Override
			public void windowIconified(WindowEvent e)
			{

			}

			@Override
			public void windowDeiconified(WindowEvent e)
			{

			}

			@Override
			public void windowActivated(WindowEvent e)
			{

			}

			@Override
			public void windowDeactivated(WindowEvent e)
			{

			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			launchFinal.close();

			System.out.println("Device closed.");

			recv.close();

			cust.setVisible(false);
		}));
	}
}
