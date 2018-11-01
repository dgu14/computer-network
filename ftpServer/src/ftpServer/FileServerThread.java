package ftpServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServerThread extends Thread {
	
	public FileServerThread() {}
	
	@Override
	public void run()
	{
		try {
			InetSocketAddress pnum=new InetSocketAddress(ServerMain.serverFilePort);
			ServerSocket welcomeSocket = new ServerSocket();
			welcomeSocket.bind(pnum);
			
			while(true)
			{
				Socket connectionSocket=welcomeSocket.accept();
				
				BufferedReader inFromClient=new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				String cmd=inFromClient.readLine();
				String fileName=inFromClient.readLine();
				
				if(cmd.equals("put"))
				{
					int readBytes;
					byte[] buf = new byte[4096];
					InputStream is=connectionSocket.getInputStream();
					FileOutputStream fos=new FileOutputStream(ServerMain.cwd+"\\"+fileName);
					
					while((readBytes=is.read(buf))!=-1) fos.write(buf, 0, readBytes);
					
					fos.flush();
					fos.close();
					is.close();
				}
				else if(cmd.equals("get"))
				{
					int readBytes;
					byte[] buf = new byte[4096];
					OutputStream os=connectionSocket.getOutputStream();
					FileInputStream fis=new FileInputStream(ServerMain.cwd+"\\"+fileName);
					
					while((readBytes=fis.read(buf))!=-1) os.write(buf, 0, readBytes);
					os.flush();
					os.close();
					fis.close();
				}

				connectionSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
