import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Client {

	public final static int SOCKET_PORT = 3030;
	public final static int AUTH_PORT = 3333;
	public final static String SERVER = "127.0.0.1";
	public final static int PASSWORD = 1234;
	public final static int NAME_SIZE = 2048;
	public final static int FILE_SIZE = 90223860;

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
		String[] cache = new String[5];
		BufferedReader br = null;
		String cvsSplitBy = ",";
		String csvFile = "C:/Users/Cade/cache.csv";
		String line = "";
		
		for(int i=0; i<5; i++)cache[i]="";
		
		try {
			sock = new Socket(SERVER, SOCKET_PORT);
			System.out.println("Connecting...");
			File f = new File(csvFile);
			f.getParentFile().mkdirs();
			f.createNewFile();

			int answer = JOptionPane.showOptionDialog(null,
					"Upload or Download?", 
					"Options", 
					JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.INFORMATION_MESSAGE, 
					null, 
					new String[]{"Upload", "Download"},
					"default");

			if(answer == JOptionPane.YES_OPTION){
				File selectedFile = null;
				os = sock.getOutputStream();
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
					os.write(mybytearray,0,mybytearray.length);
					os.flush();

					//________________________________________________________________reading in the server Socket number from the Proxy Server
					byte[] mybytearraySocket  = new byte [NAME_SIZE];
					InputStream is2 = sock.getInputStream();
					is2.read(mybytearraySocket,0,mybytearraySocket.length);

					String name = new String(mybytearraySocket);
					name = name.trim();
					int serverSocketNumber = Integer.parseInt(name);
					System.out.println(name);
					//_________________________________________________________________________________________________________________________

					//_______________________________________________________________AUTH SERVER SECTION
					authSock = new Socket(SERVER, AUTH_PORT);

					String loginSentence = "ACCESS_PLEASE";

					String enMS = new String(CipherTools.encrypt(loginSentence.getBytes(), 1234));
					String username = "caderyan" + "~~" + enMS.length() + "~~" 
							+ enMS + "~~" + name + "~~" ;// SEND LOGIN NAME and length of next array TO AUTH SERVER
					byte [] mybytearray9  = new byte [username.length()];
					mybytearray9 = username.getBytes();
					os = authSock.getOutputStream();
					System.out.println("Sending " + selectedFile.getName() + "(" + mybytearray9.length + " bytes)");
					os.write(mybytearray9,0,mybytearray9.length);
					os.flush();

					byte[] getLengths  = new byte [NAME_SIZE];//get lengths of next two arrays
					InputStream is26 = authSock.getInputStream();
					is26.read(getLengths,0,getLengths.length);
					String tmp = new String(getLengths);
					String[] arr4 = tmp.split("~~");
					int first = Integer.parseInt(arr4[0]);
					int second = Integer.parseInt(arr4[1]);

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

					//___________________________________________________________ END OF AUTH SERVER SECTION
					serverSock = new Socket(SERVER, serverSocketNumber);


					String ab = new String(ticket) + "~~";
					byte[] ts = (ab).getBytes();
					os = serverSock.getOutputStream();
					os.write(ts, 0, ts.length);
					os.flush();

					try {
						Thread.sleep(500);                 //1000 milliseconds is one second.
					} catch(InterruptedException ex) {
						Thread.currentThread().interrupt();
					}

					os = serverSock.getOutputStream();//send byte to Server
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
					String tmp2 = new String(CipherTools.encrypt(selectedFile.getName().getBytes(), Integer.parseInt(sessionKey)))+ "~~";
					mybytearray7 = tmp2.getBytes();
					//mybytearray7 = (selectedFile.getName() + "~").getBytes();
					os = serverSock.getOutputStream();
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
					os.write(CipherTools.encrypt(mybytearrayFile, Integer.parseInt(sessionKey)), 0, mybytearrayFile.length);

					os.flush();
					System.out.println("Done Uploading.");

					//_______________________________CACHING HERE_____________________________________

					fos = new FileOutputStream("C:/Users/Cade/Cache/" + selectedFile.getName());
					bos = new BufferedOutputStream(fos);

					bos.write(mybytearrayFile, 0, mybytearrayFile.length);
					bos.flush();

					int s = 0;
					br = new BufferedReader(new FileReader(csvFile));
					while ((line = br.readLine()) != null) {
						// use comma as separator
						String[] cacheTmp = line.split(cvsSplitBy);
						if(cache!=null){
							cache[s]=cacheTmp[0];
							s ++;
						}
					}

					int move = -1;
					for(int j=0; j<5; j++){
						if(cache[j].equals(selectedFile.getName())){
							cache[j] = "";
							move = j;

						}
					}
					if(move != -1){
						for(int j = move; j < 4; j ++){
							cache[j] = cache[j+1];
						}
						cache[4] = "";
					}	
					else{
						File file = new File("C:/Users/Cade/Cache/" + cache[4]);
						file.delete();
					}
					for(int j=4; j>0; j--){
						cache[j] = cache[j-1];
					}
					cache[0] = selectedFile.getName();

					exportToDatabase(cache);

				}
			}

			// ________________________________________________________________________DOWNLOAD SECTION
			else if(answer == JOptionPane.NO_OPTION){

				os = sock.getOutputStream();
				byte[] ba = new byte[1];
				ba[0] = 7;
				os.write(ba ,0,1);
				os.flush();

				String inputStringInput = JOptionPane.showInputDialog("Name of File:");
				Scanner inputScanner = new Scanner(inputStringInput);
				String name = inputScanner.nextLine();
				inputScanner.close();

				File file = new File("C:/Users/Cade/Cache/" + name);
				String folderToSave = "";
				boolean cached = false;
				if(file.exists() && file.isFile()){
					cached = true;
					
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(new java.io.File("."));
					chooser.setDialogTitle("choosertitle");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setAcceptAllFileFilterUsed(false);


					if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

						folderToSave = chooser.getSelectedFile().toString();

					} else {
					}


					byte [] mybytearrayFile  = new byte [(int)file.length()];
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
					bis.read(mybytearrayFile,0,mybytearrayFile.length);

					fos = new FileOutputStream(folderToSave + "/" + file.getName());
					bos = new BufferedOutputStream(fos);
					bos.write(mybytearrayFile, 0, mybytearrayFile.length);
					bos.flush();

				}

				byte [] mybytearrayName  = new byte [name.length()];//send filename to proxy
				mybytearrayName = name.getBytes();
				os = sock.getOutputStream();
				os.write(mybytearrayName,0,mybytearrayName.length);
				os.flush();

				byte[] mybytearraySocket  = new byte [NAME_SIZE];//take in the port number of the server
				InputStream isSocket = sock.getInputStream();
				isSocket.read(mybytearraySocket,0,mybytearraySocket.length);
				String socketNum = new String(mybytearraySocket);
				socketNum = socketNum.trim();
				int serverSocketNumber = Integer.parseInt(socketNum);


				//_______________________________DOWNLOAD AUTH SECTION___________________________


				authSock = new Socket(SERVER, AUTH_PORT);

				String loginSentence = "ACCESS_PLEASE";

				String enMS = new String(CipherTools.encrypt(loginSentence.getBytes(), 1234));
				String username = "caderyan" + "~~" + enMS.length() + "~~" 
						+ enMS + "~~" + socketNum + "~~" ;// SEND LOGIN NAME and length of next array TO AUTH SERVER
				byte [] mybytearray9  = new byte [username.length()];
				mybytearray9 = username.getBytes();
				os = authSock.getOutputStream();
				System.out.println("Sending " + "(" + mybytearray9.length + " bytes)");
				os.write(mybytearray9,0,mybytearray9.length);
				os.flush();

				byte[] getLengths  = new byte [NAME_SIZE];//get lengths of next two arrays
				InputStream is26 = authSock.getInputStream();
				is26.read(getLengths,0,getLengths.length);
				String tmp = new String(getLengths);
				//tmp = tmp.trim();
				String[] arr4 = tmp.split("~~");
				//JOptionPane.showMessageDialog(null, tmp + "hey");
				int first = Integer.parseInt(arr4[0]);
				int second = Integer.parseInt(arr4[1]);


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

				//_______________________________________END OF DOWNLOAD AUTH_____________________________


				serverSock = new Socket(SERVER, serverSocketNumber);// CONNECTED TO CORRECT SERVER


				//send ticket to server
				String ab = new String(ticket) + "~~";
				//				JOptionPane.showMessageDialog(null, ab);
				byte[] ts = (ab).getBytes();
				os = serverSock.getOutputStream();
				os.write(ts, 0, ts.length);
				os.flush();

				try {
					Thread.sleep(500);                 //1000 milliseconds is one second.
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				//send byte to server
				os = serverSock.getOutputStream();
				os.write(ba ,0,1);
				os.flush();

				try {
					Thread.sleep(500);                 //1000 milliseconds is one second.
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				//send filename to server
				os = serverSock.getOutputStream();
				System.out.println("Sending " + name + "(" + mybytearrayName.length + " bytes)");
				os.write(mybytearrayName,0,mybytearrayName.length);
				os.flush();


				//String folderToSave = "";
				if(!cached){

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
				}




				byte [] mybytearray  = new byte [FILE_SIZE]; // receive file from server
				InputStream is = serverSock.getInputStream();
				if(!cached) fos = new FileOutputStream(folderToSave + "/" + name);
				bos = new BufferedOutputStream(fos);
				bytesRead = is.read(mybytearray,0,mybytearray.length);
				current = bytesRead;

				do {
					bytesRead =
							is.read(mybytearray, current, (mybytearray.length-current));
					if(bytesRead >= 0) current += bytesRead;
				} while(bytesRead != -1);


				if(!cached){
					bos.write(CipherTools.decrypt(mybytearray, Integer.parseInt(sessionKey)), 0, current);
					bos.flush();
				}
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

	public static void exportToDatabase(String[] textline) {
		boolean appendTo = false;

		FileWriter write = null;
		try {
			write = new FileWriter("C:/Users/Cade/cache.csv", appendTo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter printLine = new PrintWriter(write);
		printLine.println(textline[0] + "\n" + textline[1] + "\n" +
				textline[2] + "\n" + textline[3] + "\n" + textline[4]);
		printLine.close();
	}

}