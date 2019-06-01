package Runnables.Junk;

import Utility.X;

import javax.swing.*;

public class FindingFocus
{
	public static void main(String... args)
	{
		String[] choices = getNamesOfAllWindows();
		if(choices != null)
		{
			String choice = (String) JOptionPane.showInputDialog(null, "Please choose an application to assign a profile to.",
																"Application Registry", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
			System.out.println(choice);
		}
	}

	public static String[] getNamesOfAllWindows()
	{
		X.Display display = new X.Display();
		try {
			X.Window[] windows = display.getWindows();
			String[] windowNames = new String[windows.length];

			for(int i = 0; i < windows.length; i++)
			{
				windowNames[i] = windows[i].getTitle();
			}

			display.close();

			return windowNames;

		} catch (X.X11Exception e) {
			e.printStackTrace();
			return null;
		}
	}



	// Turns out there's a publicly available interface for this kind of thing. Who knew?

	public static String getNameOfFocusedWindow()
	{
		try {
			X.Display display = new X.Display();
			X.Window window = display.getActiveWindow();
			display.close();
			return window.getTitle();
		} catch (X.X11Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Old code detailed below.

//	public static String getNameOfFocusedWindow()
//	{
//		// Guess which other function was an absolute trainwreck to write?
//
//		// Commenting this as I understand it, which won't be much, probably.
//
//		// Check if the platform that's running this code is a Linux platform.
//		// Mine is, so this will always pass, but I'll have to change this if I want this to be somewhat portable.
//		// Then again, this all uses X11 stuff, so maybe it wouldn't be portable anyways. Oh well.
//		if(Platform.isLinux()) {  // Possibly most of the Unix systems will work here too, e.g. FreeBSD
//			// Grab an instance of the X11 library and the XLib library (XLib just adds the XGetInputFocus function.)
//			final X11 x11 = X11.INSTANCE;
//			final XLib xlib = XLib.INSTANCE;
//
//			// Create space in memory to store the currently focused window. XGetInputFocus uses a Window * to return the focused window, so we need space for it.
//			X11.WindowByReference current_ref = new X11.WindowByReference();
//
//			// Get a handle on the current display.
//			X11.Display display = x11.XOpenDisplay(null);
//
//			// If that display is valid...
//			if(display != null)
//			{
//				// Set up some more memory to store return values from XGetInputFocus.
//				IntByReference revert_to_return = new IntByReference();
//
//				// Get the focused window
//				xlib.XGetInputFocus(display, current_ref, revert_to_return);
//
//				// Convert the reference to an instance
//				X11.Window current = current_ref.getValue();
//
//				// Call upon getName to get the name of the focused window.
//				String name = getName(x11, display, current);
//
//				// Close the display.
//				x11.XCloseDisplay(display);
//
//				// Return the name.
//				return name;
//			}
//			else
//				return null;
//		}
//		else
//			return null;
//	}

//	private static String getName(X11 x11, X11.Display display, X11.Window window)
//	{
//		// Get the parent window
//
//		// Set up memory to store return values from XQueryTree, which gets info from the hierarchy of windows that window belongs to.
//
//		// The root window of the hierarchy
//		X11.WindowByReference rootRef = new X11.WindowByReference();
//
//		// window's parent
//		X11.WindowByReference parentRef = new X11.WindowByReference();
//
//		// All children of window
//		PointerByReference childrenRef = new PointerByReference();
//
//		// Number of children window has
//		IntByReference childCountRef = new IntByReference();
//
//		// Grab the hierarchical information of the window tree that window belongs to
//		x11.XQueryTree(display, window, rootRef, parentRef, childrenRef, childCountRef);
//
//		// Convert the parentRef to an instance
//		X11.Window parent = parentRef.getValue();
//
//		// Why are we doing this?
//		// The name of the focused window actually resides in the *parent* of the currently focused window, which holds a proxy window that actually has the focus.
//		// We query the tree for the parent of window, and then grab the parent's name.
//
//		// Grab the name and return
//
//		// Allocate memory space for the name
//		X11.XTextProperty name = new X11.XTextProperty();
//
//		// Grab the name of parent
//		x11.XGetWMName(display, parent, name);
//
//		return name.value;
//	}


}
