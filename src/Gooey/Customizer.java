package Gooey;

import Utility.CustomReceiver;
import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Customizer extends JFrame
{
	private CustomReceiver receiverToModify;

	// I'm so sorry
	private JPanel panel1;
	private JButton button1;
	private JButton button2;
	private JButton button3;
	private JButton button4;
	private JButton button5;
	private JButton button6;
	private JButton button7;
	private JButton button8;
	private JButton button9;
	private JButton button10;
	private JButton button11;
	private JButton button12;
	private JButton button13;
	private JButton button14;
	private JButton button15;
	private JButton button16;
	private JButton button17;
	private JButton button18;
	private JButton button19;
	private JButton button20;
	private JButton button21;
	private JButton button22;
	private JButton button23;
	private JButton button24;
	private JButton button25;
	private JButton button26;
	private JButton button27;
	private JButton button28;
	private JButton button29;
	private JButton button30;
	private JButton button31;
	private JButton button32;
	private JButton button33;
	private JButton button34;
	private JButton button35;
	private JButton button36;
	private JButton button37;
	private JButton button38;
	private JButton button39;
	private JButton button40;
	private JButton button41;
	private JButton button42;
	private JButton button43;
	private JButton button44;
	private JButton button45;
	private JButton button46;
	private JButton button47;
	private JButton button48;
	private JButton button49;
	private JButton button50;
	private JButton button51;
	private JButton button52;
	private JButton button53;
	private JButton button54;
	private JButton button55;
	private JButton button56;
	private JButton button57;
	private JButton button58;
	private JButton button59;
	private JButton button60;
	private JButton button61;
	private JButton button62;
	private JButton button63;
	private JButton button64;
	private JButton button65;
	private JButton button66;
	private JButton button67;
	private JButton button68;
	private JButton button69;
	private JButton button70;
	private JButton button71;
	private JButton button72;
	private JButton button73;
	private JButton button74;
	private JButton button75;
	private JButton button76;
	private JButton button77;
	private JButton button78;
	private JButton button79;
	private JButton button80;
	private JButton saveButton;
	private JButton loadButton;

	public final JButton[] buttons = {button1,button2,button3,button4,button5,button6,button7,button8,button9,button10,button11,
			button12,button13,button14,button15,button16,button17,button18,button19,button20,button21,button22,button23,
			button24,button25,button26,button27,button28,button29,button30,button31,button32,button33,button34,button35,
			button36,button37,button38,button39,button40,button41,button42,button43,button44,button45,button46,button47,
			button48,button49,button50,button51,button52,button53,button54,button55,button56,button57,button58,button59,
			button60,button61,button62,button63,button64,button65,button66,button67,button68,button69,button70,button71,
			button72,button73,button74,button75,button76,button77,button78,button79,button80};

	public final int[] noteVals = {104,105,106,107,108,109,110,111,0,1,2,3,4,5,6,7,8,16,17,18,19,20,21,22,23,24,32,33,34,35,36,37,
			38,39,40,48,49,50,51,52,53,54,55,56,64,65,66,67,68,69,70,71,72,80,81,82,83,84,85,86,87,88,96,97,98,99,100,
			101,102,103,104,112,113,114,115,116,117,118,119,120};

	public Customizer(CustomReceiver rcvr)
	{

		receiverToModify = rcvr;

		for(int i = 0; i < buttons.length; i++)
		{
			buttons[i].setText("" + noteVals[i]);
		}

		for(JButton b : buttons) {
			b.addActionListener(e -> {
				getNewActionFromDialog(Integer.parseInt(b.getText()));
			});
		}

		saveButton.addActionListener(e -> {
			final JFileChooser fileDialog = new JFileChooser();
			int val = fileDialog.showSaveDialog(Customizer.this);
			if(val == JFileChooser.APPROVE_OPTION) {
				receiverToModify.saveProfile(fileDialog.getSelectedFile());
			}
		});

		loadButton.addActionListener(e -> {
			final JFileChooser fileDialog = new JFileChooser();
			int val = fileDialog.showOpenDialog(Customizer.this);
			if(val == JFileChooser.APPROVE_OPTION) {
				receiverToModify.loadProfile(fileDialog.getSelectedFile());
			}
		});

		setTitle("Launchpad Macro-izer");

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		setSize(700, 500);
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

	public void getNewActionFromDialog(int buttonToAssign)
	{
		String[] options = {"Add Command", "Add Keystroke", "Clear Button"};
		CustomReceiver.Profile currentProfile = receiverToModify.getProfile();

		String dialogText;
		if(currentProfile.getCommand(buttonToAssign) != null)
		{
			String currentAssignment = (currentProfile.getRawCommands().get(buttonToAssign) != null) ? currentProfile.getRawCommands().get(buttonToAssign) : currentProfile.getRawKeystrokes().get(buttonToAssign);
			dialogText = String.format("Would you like to modify this button's assignment? (current value: %s)", currentAssignment);
		}
		else
		{
			dialogText = "Would you like to add a command or keystroke?";
		}

		int which = JOptionPane.showOptionDialog(null, dialogText, "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, "Add Command");
		if(which == JOptionPane.YES_OPTION)
		{
			// command
			String command = JOptionPane.showInputDialog("Please enter the command you want to assign.");
			if(command == null) return;

			receiverToModify.addCommand(buttonToAssign, command);

//			buttons[Arrays.asList(noteVals).indexOf(buttonToAssign)].setToolTipText(command);
		}
		else if(which == JOptionPane.NO_OPTION)
		{
			// keystroke
			String keys = JOptionPane.showInputDialog("Please enter the keystroke you want to assign.");
			if(keys == null) return;

			receiverToModify.addKeystroke(buttonToAssign, keys);

//			buttons[Arrays.asList(noteVals).indexOf(buttonToAssign)].setToolTipText(keys);
		}
		else if(which == JOptionPane.CANCEL_OPTION)
		{
//			buttons[Arrays.asList(noteVals).indexOf(buttonToAssign)].setToolTipText(null);

			receiverToModify.removeAction(buttonToAssign);
		}
	}

	// basically a long-ass translation table
	//TODO add more keys to this
	public static ArrayList<Integer> keysToCodes(List<String> keys)
	{
		ArrayList<Integer> keysToPress = new ArrayList<>();
		for(String k : keys)
		{
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
