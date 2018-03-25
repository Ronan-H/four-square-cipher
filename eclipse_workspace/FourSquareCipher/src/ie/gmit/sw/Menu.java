package ie.gmit.sw;

import static java.lang.System.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class Menu {
	public static final String SSD_ROOT = ".";
	public static final String HDD_ROOT = "G:/FourSquareCipher";
	public static final String ROOT_DIR = SSD_ROOT;
	
	private Scanner console;
	private boolean running;
	
	public Menu() {
		console = new Scanner(System.in);
	}
	
	public void go() {
		running = true;
		
		int choice;
		Cipher cipher;
		boolean readFromURL;
		int fileChooseResult;
		boolean encryptMode;
		boolean writeToFile;
		final String defaultInputFile = ROOT_DIR + "/output/WarAndPeace-LeoTolstoy.txt";
		String startingDir;
		URL inputURL = null;
		boolean validURL;
		String resource;
		
		JFileChooser fileChooser = new JFileChooser();
		FileFilter fileFilter = new FileNameExtensionFilter("Text files", "txt");
		
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setDialogTitle("Select an input file");
		
		out.println();
		out.println(" ==============================");
		out.println(" |  Rapid Four Square Cipher  |");
		out.println(" |      By Ronan Hanley       |");
		out.println(" ==============================");
		out.println();
		
		/* Get a key for the cipher and initialize it right away, since the
		 * application is useless without one.
		 */
		cipher = initCipher();
		
		while (running) {
			out.println("What would you like to do?");
			choice = getUserOption("Encrypt", "Decrypt", "Change the key", "Print the cipher's four squares", "Exit");
			
			switch (choice) {
			case 1:
			case 2:
				// Encrypt/Decrypt
				encryptMode = (choice == 1);
				
				out.println("Read from a file or a URL?");
				readFromURL = (getUserOption("File", "URL") == 2);
				
				if (readFromURL) {
					// read from url
					
					do {
						out.print("Enter a URL to read from\n\n> ");
						try {
							inputURL = new URL(console.nextLine());
							validURL = true;
						} catch (MalformedURLException e) {
							out.println("That URL is not valid. Please try again.\n");
							validURL = false;
						}
					} while(!validURL);
					
					resource = inputURL.toString();
				}
				else {
					// read from file
					if (encryptMode) {
						fileChooser.setSelectedFile(new File(defaultInputFile));
						startingDir = ROOT_DIR + "/input/";
					}
					else {
						// Decrypt mode
						fileChooser.setSelectedFile(null);
						startingDir = ROOT_DIR + "/output/";
					}
					
					fileChooser.setCurrentDirectory(new File(startingDir));
					
					out.println("Showing file chooser window, please choose a file.");
					out.println("(it might be hidden behind some windows)\n");
					fileChooseResult = fileChooser.showOpenDialog(null);
					fileChooser.requestFocusInWindow();
					
					if (fileChooseResult != JFileChooser.APPROVE_OPTION) {
						continue;
					}
					
					resource = fileChooser.getSelectedFile().getAbsolutePath();
				}
				
				out.println("Print output to console or a file?");
				writeToFile = (getUserOption("Output to console", "Output to a file") == 2);
				
				if (writeToFile) out.println("(file will be written to the \"output\" folder of this project)\n");
				
				long timerStart = System.nanoTime();
				try {
					cipher.processFile(resource, encryptMode, readFromURL, writeToFile);
					out.printf("Contents read, encrypted, and written in: %.2fms.%n%n", (System.nanoTime() - timerStart) / 1000000f);
				}
				catch (FileNotFoundException e) {
					out.printf("Selected file at:%n%n%s%n%nDoes not exist.%n", resource);
				}
				catch (UnknownHostException e) {
					out.printf("Unknown host \"%s\"%n%n", resource);
				}
				catch (IOException e) {
					System.err.print("Error occured while trying to process the input file/URL!\n\n");
					e.printStackTrace();
				}
				break;
			case 3:
				// Change the key
				cipher = initCipher();
				break;
			case 4:
				// Print the cipher's four squares
				cipher.printSquares();
				break;
			case 5:
				// Exit
				System.exit(0);
			}
		}
		
		out.println("Read input data from file or URL?");
		choice = getUserOption("Read from file", "Read from URL");
	}
	
	private int getUserOption(String...options) {
		int i;
		
		out.println("\nSelect an option:");
		for (i = 0; i < options.length; ++i) {
			out.printf("[%d]: %s\n", (i + 1), options[i]);
		}
		
		out.print("\n");
		
		return getValidatedInt(1, options.length);
	}
	
	private int getValidatedInt(int min, int max) {
		int input = 0;
		boolean valid;
		
		do {
			out.print("> ");
			try {
				input = Integer.parseInt(console.nextLine());
				valid = (input >= min && input <= max);
			} catch(NumberFormatException e) {
				valid = false;
			}
			
			out.println();
			
			if (!valid) {
				out.printf("Invalid input; must be a number between %d and %d (inclusive).\n", min, max);
				out.println("Please try again.\n");
			}
		} while(!valid);
		
		return input;
	}
	
	private Cipher initCipher() {
		int i;
		int choice;
		String key = null;
		int numKeys;
		StringBuilder[] inputKeys = {null, null};
		Cipher cipher;
		
		out.println("Choose a source for the cipher key");
		choice = getUserOption("Randomly generate a key for me", "Let me enter the key");
		
		switch (choice) {
		case 1:
			// randomly generate the key
			key = Cipher.generateRandomKey();
			break;
		case 2:
			// get key by user input
			out.println("Use one key or two?");
			out.printf("\t(One key max length: %d%n", Cipher.ALPHABET_SIZE);
			out.printf("\t Two keys max length each: %d%n", Cipher.ALPHABET_SIZE /2);
			out.println("\t smaller keys will be automatically padded)");
			
			numKeys = getUserOption("I want one key", "I want to enter two keys");
			
			for (i = 0; i < numKeys; ++i) {
				out.printf("\nPlease enter key %d:\n> ", (i + 1));
				inputKeys[i] = new StringBuilder(console.nextLine());
			}
			
			key = new KeySanitizer(inputKeys).getSanitizedKey();
		}
		
		out.printf("Key: %n  %s%n%n", key);
		out.println("Initializing cipher...");
		cipher = new Cipher(key);
		out.println("Finished initializing cipher.\n");
		
		return cipher;
	}
	
}
