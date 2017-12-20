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
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JOptionPane;

public class AuthServer {

	public final static int SOCKET_PORT = 3333;  // you may change this
	public final static String FILE_TO_SEND = "c:/temp/source.jpg";  // you may change this
	public final static String FOLDER_TO_STORE = "C:/server/";
	public final static int FILE_SIZE = 902238600;
	public final static int NAME_SIZE = 128;
	public final static int TOKEN_SIZE = 1024;
	public final static int STRICT_KEY = 5432;

	public static void main (String [] args ) throws IOException {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		OutputStream os = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ServerSocket servsock = null;
		Socket sock = null;
		String loginDetails = "C:/Users/Cade/dir.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

		//create the directory file (database) 
		//createDatabase();

		try {
			servsock = new ServerSocket(SOCKET_PORT);
			while (true) {
				System.out.println("Authentication server operating...");
				try {
					sock = servsock.accept();
					//servsock = new ServerSocket(SOCKET_PORT);
					//JOptionPane.showMessageDialog(null, "Accepted connection : " + sock);
					
					byte[] mybytearrayName  = new byte[NAME_SIZE];//receive the username from client
					InputStream is2 = sock.getInputStream();
					is2.read(mybytearrayName,0,mybytearrayName.length);

					String input = new String(mybytearrayName);
					String[] vals = input.split(",");
					String name = vals[0];
					int msLength = Integer.parseInt(vals[1]);
					String encrypted = vals[2];
					//System.out.println(name);
					//JOptionPane.showMessageDialog(null, vals[2] + ";");
					
					
					//search database here to retrieve password associated with username
					
					
//					byte[] mybytearrayEnMs  = new byte[msLength];//receiving encrypted message
//					InputStream is23 = sock.getInputStream();
//					is23.read(mybytearrayEnMs,0,mybytearrayEnMs.length);
//					
//					String tempo = new String(mybytearrayEnMs);
//					
//					JOptionPane.showMessageDialog(null, tempo);
//					// getting stuck somewhere here
//					
//					
//					JOptionPane.showMessageDialog(null, vals[1]);
////					String encrypted = new String(mybytearrayEnMs);
////					encrypted = encrypted.trim();
////					System.out.println(encrypted);
//					String decrypted = new String (CipherTools.decrypt(mybytearrayEnMs, 1234));
//					System.out.println(decrypted);
//					JOptionPane.showMessageDialog(null, decrypted);
					
					String decrypted = new String(CipherTools.decrypt(encrypted.getBytes(), 1234));
					//JOptionPane.showMessageDialog(null, decrypted);
					
					//int sessionKey = ThreadLocalRandom.current().nextInt(1111, 9999+1);
					int sessionKey = 2222;
					
					if(decrypted.contains("ACCESS_PLEASE")){
						
						//JOptionPane.showMessageDialog(null, "You did it fam");
						byte[] tokentemp = CipherTools.encrypt((sessionKey+"").getBytes(), STRICT_KEY);//sending encrypted ticket to client
						byte[] token = CipherTools.encrypt(tokentemp, 1234);
						byte[] token2 = CipherTools.encrypt((sessionKey+"").getBytes(), 1234);// sending encrypted sessionKey to client
						
						
						String lengths = token.length + "," + token2.length;//send lengths to client
						byte[] sendLen = lengths.getBytes();
						os = sock.getOutputStream(); // 
						System.out.println("Sending " + "lengths" + "(" + lengths.length() + " bytes)");
						os.write(sendLen,0,sendLen.length);
						os.flush();
						
						
						//byte[] token  = new byte[TOKEN_SIZE];
						//token = resultSocket.getBytes();
			            os = sock.getOutputStream(); // 
						System.out.println("Sending " + "ticket" + "(" + token.length + " bytes)");
						os.write(token,0,token.length);
						os.flush();
						
						
						//token = resultSocket.getBytes();
			            os = sock.getOutputStream(); 
						System.out.println("Sending " + "seshKey" + "(" + token2.length + " bytes)");
						os.write(token2,0,token2.length);
						os.flush();
					}



//					byte [] mybytearray2  = new byte [1];
//					InputStream is = sock.getInputStream();
//					int bytesRead = is.read(mybytearray2,0,mybytearray2.length);//receive the indicator byte from client proxy
					//					
					//					System.out.println(mybytearray2[0]);
					//					
					//					if(mybytearray2[0] == 6){
					//						System.out.println("here we are");
					//					}
					//
//					if(mybytearray2[0] == 8){
//						byte[] mybytearrayName  = new byte[NAME_SIZE];//receive the filename from client proxy
//						InputStream is2 = sock.getInputStream();
//						is2.read(mybytearrayName,0,mybytearrayName.length);
//
//						String name = new String(mybytearrayName);
//						name = name.trim();
//						System.out.println(name);
//
//						//take in the port number of the server and add a new row to the directory containing the filename and the server port number	
//						byte[] mybytearraySocket  = new byte [NAME_SIZE];
//						InputStream isSocket = sock.getInputStream();
//						isSocket.read(mybytearraySocket,0,mybytearraySocket.length);
//						
//						String socketNum = new String(mybytearraySocket);
//						socketNum = socketNum.trim();
//						//int serverSocketNumber = Integer.parseInt(socketNum);
//						System.out.println(socketNum);
//						
//						exportToDatabase(name + "," + socketNum);
//
//						//sock.close();
//
//						//mybytearray2[0] = 6;
//					}
//
//
//
//					else{// send file
//
//						byte[] mybytearrayName  = new byte [NAME_SIZE];
//						InputStream is2 = sock.getInputStream();
//						is2.read(mybytearrayName,0,mybytearrayName.length);
//
//						String name = new String(mybytearrayName);
//						name = name.trim();
//						System.out.println(name);
//
//
//						//search for the filename in the directory and return the server port number to the client proxy
//						String resultSocket = "ERROR_";
//						br = new BufferedReader(new FileReader(loginDetails));
//			            while ((line = br.readLine()) != null) {
//
//			                // use comma as separator
//			                String[] country = line.split(cvsSplitBy);
//			                if(name.equals(country[0])){
//			                	resultSocket = country[1];
//			                }
//			            }
//			            byte [] mybytearray  = new byte [resultSocket.length()]; //send server socket to client proxy
//						mybytearray = resultSocket.getBytes();
//			            os = sock.getOutputStream(); // send server socket to client pro server
//						System.out.println("Sending " + resultSocket + "(" + mybytearray.length + " bytes)");
//						os.write(mybytearray,0,mybytearray.length);
//						os.flush();	
//
//					}
				}
				finally {
					if (bis != null) bis.close();
					if (os != null) os.close();
					if (sock!=null) {
						sock.close();
						//sock = null;
					}
				}
			}
		}
		finally {
			if (servsock != null) servsock.close();
			//if (sock!=null) sock.close();
		}
	}
	public static void exportToDatabase(String textline) {
		boolean appendTo = true;

		FileWriter write = null;
		try {
			write = new FileWriter("C:/Users/Cade/dir.csv", appendTo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter printLine = new PrintWriter(write);
		printLine.println(textline);
		printLine.close();
	}
	public static void createDatabase(){

		String path = "C:/Users/Cade/login.csv";
		File f = new File(path);

		f.getParentFile().mkdirs(); 
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
}
}