import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Client {

	public final static int SOCKET_PORT = 3030;
	public final static int AUTH_PORT = 3333;// you may change this
	public final static String SERVER = "127.0.0.1";
	public final static int PASSWORD = 1234;// localhost
	//public final static String FILE_TO_RECEIVED = "c:/Users/Cade/Desktop/download3.jpg"; 
	public final static int NAME_SIZE = 2048;

	// you may change this, I give a
	// different name because I don't want to
	// overwrite the one used by server...

	public final static int FILE_SIZE = 902238600; // file size temporary hard coded
	// should bigger than the file to be downloaded

	public static void main (String [] args ) throws IOException {
		int bytesRead;
		int current = 0;
		FileOutputStream fos = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		OutputStream os = null;
		BufferedOutputStream bos = null;
		Socket sock = null;
		Socket serverSock = null;
		Socket authSock = null;
		//Socket serverSock = null;
		try {
			sock = new Socket(SERVER, SOCKET_PORT);
			System.out.println("Connecting...");

			int answer = JOptionPane.showOptionDialog(null,
					"Upload or Download?", 
					"Options", 
					JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.INFORMATION_MESSAGE, 
					null, 
					new String[]{"Upload", "Download"}, // this is the array
					"default");

			if(answer == JOptionPane.YES_OPTION){
				File selectedFile = null;
				os = sock.getOutputStream();
				//System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
				byte[] ba = new byte[1];
				ba[0] = 8;
				os.write(ba ,0,1);
				os.flush();

				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				int result = fileChooser.showOpenDialog(fileChooser);
				if (result == JFileChooser.APPROVE_OPTION) {
					selectedFile = fileChooser.getSelectedFile();
					System.out.println("Selected file: " + selectedFile.getAbsolutePath());
				}
				if(selectedFile != null){
					byte [] mybytearray  = new byte [selectedFile.getName().length()];
					mybytearray = selectedFile.getName().getBytes();
					os = sock.getOutputStream();
					System.out.println("Sending " + selectedFile.getName() + "(" + mybytearray.length + " bytes)");
					os.write(mybytearray,0,mybytearray.length);
					os.flush();
					System.out.println("Done.");

					//________________________________________________________________reading in the server Socket number from the Proxy Server
					byte[] mybytearraySocket  = new byte [NAME_SIZE];
					InputStream is2 = sock.getInputStream();
					is2.read(mybytearraySocket,0,mybytearraySocket.length);

					String name = new String(mybytearraySocket);
					name = name.trim();
					int serverSocketNumber = Integer.parseInt(name);
					System.out.println(name);
					//_________________________________________________________________________________________________________________________
					//sock.close();
					
					//_______________________________________________________________AUTH SERVER SECTION
					authSock = new Socket(SERVER, AUTH_PORT);
					
					String loginSentence = "ACCESS_PLEASE";
					
					String username = "caderyan" + "," + loginSentence.length() + ",";// SEND LOGIN NAME and length of next array TO AUTH SERVER
					byte [] mybytearray9  = new byte [username.length()];
					mybytearray9 = username.getBytes();
					os = authSock.getOutputStream();
					System.out.println("Sending " + selectedFile.getName() + "(" + mybytearray9.length + " bytes)");
					os.write(mybytearray9,0,mybytearray9.length);
					os.flush();
					
					//String loginSentence = "ACCESS_PLEASE";// SEND LOGIN KEY, ENCRYPTED WITH THE PASSWORD, TO AUTH SERVER
					byte[] mybytearray10 = CipherTools.encrypt(loginSentence.getBytes(), PASSWORD);
					os = authSock.getOutputStream();
					System.out.println("Sending " + loginSentence + "(" + mybytearray10.length + " bytes)");
					os.write(mybytearray10,0,mybytearray10.length);
					os.flush();
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					byte[] getLengths  = new byte [NAME_SIZE];//get lengths of next two arrays
					InputStream is26 = authSock.getInputStream();
					is26.read(getLengths,0,getLengths.length);
					String tmp = new String(getLengths);
					//tmp = tmp.trim();
					JOptionPane.showMessageDialog(null, tmp + "hey");
					int first = Integer.parseInt(tmp.split(",")[0]);
					int second = Integer.parseInt(tmp.split(",")[1]);
					
					
					//RECEIVE THE TICKET, ENCRYPTED WITH PASSWORD FROM AUTH SERVER
					byte[] ticketEnc  = new byte [first];//
					InputStream is27 = authSock.getInputStream();
					is27.read(ticketEnc,0,first);
					byte[] ticket = CipherTools.decrypt(ticketEnc, PASSWORD);
					
					
					//RECEIVE THE SESSION KEY, ENCRYPTED WITH PASSWORD, FROM AUTH SERVER
					byte[] sessionEnc  = new byte [second];//
					InputStream is28 = authSock.getInputStream();
					is27.read(sessionEnc,0,second);
					byte[] session = CipherTools.decrypt(sessionEnc, PASSWORD);
					String sessionKey = new String(session);
					
					JOptionPane.showMessageDialog(null, sessionKey);
					
					//___________________________________________________________ END OF AUTH SERVER SECTION
					serverSock = new Socket(SERVER, serverSocketNumber);	
					
					

					os = serverSock.getOutputStream();//send byte to Server
					//System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
					byte[] ba2 = new byte[1];
					ba2[0] = 8;
					os.write(ba2 ,0,1);
					os.flush();
					try {
						Thread.sleep(500);                 //1000 milliseconds is one second.
					} catch(InterruptedException ex) {
						Thread.currentThread().interrupt();
					}

					byte [] mybytearray7  = new byte [selectedFile.getName().length()];
					mybytearray7 = selectedFile.getName().getBytes();
					os = serverSock.getOutputStream();
					System.out.println("Sending " + selectedFile.getName() + "(" + mybytearray7.length + " bytes)");
					os.write(mybytearray7,0,mybytearray7.length);
					os.flush();

					try {
						Thread.sleep(500);                 //1000 milliseconds is one second.
					} catch(InterruptedException ex) {
						Thread.currentThread().interrupt();
					}

					byte [] mybytearrayFile  = new byte [(int)selectedFile.length()];
					fis = new FileInputStream(selectedFile);
					bis = new BufferedInputStream(fis);
					bis.read(mybytearrayFile,0,mybytearrayFile.length);
					os = serverSock.getOutputStream();
					System.out.println("Sending " + selectedFile.getName() + "(" + mybytearrayFile.length + " bytes)");
					os.write(mybytearrayFile,0,mybytearrayFile.length);
					os.flush();
					System.out.println("Done Uploading.");
				}
			}

			// receive file
			else if(answer == JOptionPane.NO_OPTION){

				os = sock.getOutputStream();
				//System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
				byte[] ba = new byte[1];
				ba[0] = 7;
				os.write(ba ,0,1);
				os.flush();

				String inputStringInput = JOptionPane.showInputDialog("Name of File:");
				Scanner inputScanner = new Scanner(inputStringInput);
				String name = inputScanner.nextLine();
				inputScanner.close();

				System.out.println(name + " this is what you're looking for");

				byte [] mybytearrayName  = new byte [name.length()];
				mybytearrayName = name.getBytes();
				os = sock.getOutputStream();
				System.out.println("Sending " + name + "(" + mybytearrayName.length + " bytes)");
				os.write(mybytearrayName,0,mybytearrayName.length);
				os.flush();

				String folderToSave = "";

				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("choosertitle");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);

				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

					folderToSave = chooser.getSelectedFile().toString();

					System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
					System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
				} else {
					System.out.println("No Selection ");
				}


				byte [] mybytearray  = new byte [FILE_SIZE];
				InputStream is = sock.getInputStream();
				fos = new FileOutputStream(folderToSave + "/" + name);
				bos = new BufferedOutputStream(fos);
				bytesRead = is.read(mybytearray,0,mybytearray.length);
				current = bytesRead;

				do {
					bytesRead =
							is.read(mybytearray, current, (mybytearray.length-current));
					if(bytesRead >= 0) current += bytesRead;
					//System.out.println(bytesRead);
				} while(bytesRead != -1);

				//System.out.println(mybytearray.length + "   " + current);
				bos.write(mybytearray, 0 , current);
				bos.flush();
				System.out.println("File " + folderToSave + "/" + name
						+ " downloaded (" + current + " bytes read)");
			}
		}
		finally {
			if (fos != null) fos.close();
			if (bos != null) bos.close();
			if (sock != null) sock.close();
			if (serverSock != null) serverSock.close();
		}
	}

}