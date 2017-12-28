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

	public final static int SOCKET_PORT = 3333;
	public final static String FILE_TO_SEND = "c:/temp/source.jpg"; 
	public final static String FOLDER_TO_STORE = "C:/server/";
	public final static int FILE_SIZE = 902238600;
	public final static int NAME_SIZE = 128;
	public final static int TOKEN_SIZE = 1024;
	public final static int STRICT_KEY = 5432;
	public final static int STRICT_KEY2 = 6543;
	public final static int STRICT_KEY3 = 7654;

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
					
					byte[] mybytearrayName  = new byte[NAME_SIZE];//receive the username from client
					InputStream is2 = sock.getInputStream();
					is2.read(mybytearrayName,0,mybytearrayName.length);

					String input = new String(mybytearrayName);
					String[] vals = input.split("~~");
					String name = vals[0];
					int msLength = Integer.parseInt(vals[1]);
					String encrypted = vals[2];
					String serverSocket = vals[3];
					
					
					String decrypted = new String(CipherTools.decrypt(encrypted.getBytes(), 1234));
					
					int sessionKey = 2222;
					
					if(decrypted.contains("ACCESS_PLEASE")){
						
						int strctTmp = 0;
						if(serverSocket.equals("3031")) strctTmp = STRICT_KEY;
						else if(serverSocket.equals("3032")) strctTmp = STRICT_KEY2;
						else if(serverSocket.equals("3033")) strctTmp = STRICT_KEY3;
						byte[] tokentemp = CipherTools.encrypt((sessionKey+"").getBytes(), strctTmp);//sending encrypted ticket to client
						byte[] token = CipherTools.encrypt(tokentemp, 1234);
						byte[] token2 = CipherTools.encrypt((sessionKey+"").getBytes(), 1234);// sending encrypted sessionKey to client
						
						
						String lengths = token.length + "~~" + token2.length + "~~";//send lengths to client
						byte[] sendLen = lengths.getBytes();
						os = sock.getOutputStream(); // 
						System.out.println("Sending " + "lengths" + "(" + lengths.length() + " bytes)");
						os.write(sendLen,0,sendLen.length);
						os.flush();
						
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
			            os = sock.getOutputStream(); // 
						System.out.println("Sending " + "ticket" + "(" + token.length + " bytes)");
						os.write(token,0,token.length);
						os.flush();
						
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						
			            os = sock.getOutputStream(); 
						System.out.println("Sending " + "seshKey" + "(" + token2.length + " bytes)");
						os.write(token2,0,token2.length);
						os.flush();
					}
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