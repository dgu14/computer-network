package ftpClient;

import java.io.*;
import java.net.*;

public class ClientMain {
	public static String 	serverIp		=	"127.0.0.1";
	public static int 		serverPort		=	6789;
	public static int 		serverFilePort	=	6790;
	
	public static void receive(File file, String fileName) throws Exception
	{
		Socket dataSocket=new Socket(serverIp, serverFilePort);
		
		DataOutputStream outToServer=new DataOutputStream(dataSocket.getOutputStream());
		outToServer.writeBytes("get\n");
		outToServer.writeBytes(fileName);
		outToServer.write('\n');
		
		int readBytes;
		byte[] buf = new byte[4096];
		InputStream is=dataSocket.getInputStream();
		FileOutputStream fos=new FileOutputStream(file);
	
		while((readBytes=is.read(buf))!=-1) fos.write(buf,0,readBytes);

		fos.close();
		is.close();
		
		dataSocket.close();
	}

	public static void send(File file) throws Exception
	{
		Socket dataSocket=new Socket(serverIp, serverFilePort);
		
		DataOutputStream outToServer=new DataOutputStream(dataSocket.getOutputStream());
		outToServer.writeBytes("put\n");
		outToServer.writeBytes(file.getName());
		outToServer.write('\n');
		
		int readBytes;
		byte[] buf = new byte[4096];
		
		OutputStream os=dataSocket.getOutputStream();
		FileInputStream fis=new FileInputStream(file.getCanonicalPath());
		
		while((readBytes=fis.read(buf))!=-1)
		{
			os.write(buf,0,readBytes);
		}
		
		os.close();
		fis.close();
		
		dataSocket.close();
	}
	
	public static void main(String args[]) throws Exception
	{
		String cmd;
		
		BufferedReader cin=new BufferedReader(new InputStreamReader(System.in));
		Socket clientSocket=new Socket(serverIp, serverPort);
		
		DataOutputStream outToServer=new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		while(true)
		{
			cmd=cin.readLine();
			
			if(cmd.equals("quit"))
			{
				outToServer.writeBytes("quit\n");
				//clientSocket.close();
				break;
			}
			else if(cmd.equals("list"))
			{
				outToServer.writeBytes("list\n");
				int gatsu=Integer.parseInt(inFromServer.readLine());
				String files[]=new String[gatsu];
				for(int i=0;i<gatsu;i++)
				{
					files[i]=inFromServer.readLine();
				}
				
				System.out.println(gatsu);
				for(int i=0;i<gatsu;i++)
				{
					System.out.println(files[i]);
				}
					
			}
			else if(cmd.startsWith("cd"))
			{
				int pos=cmd.indexOf(" ");
				String path;
				if(pos==-1) path="";
				else path=cmd.substring(pos+1);
				
				outToServer.writeBytes("cd");
				outToServer.write('\n');
				outToServer.writeBytes(path);
				outToServer.write('\n');
				
				String ret=inFromServer.readLine();
				System.out.println(ret);
			}
			else if(cmd.equals("cwd"))
			{
				outToServer.writeBytes("cwd");
				outToServer.write('\n');
				String ret=inFromServer.readLine();
				System.out.println(ret);
			}
			else if(cmd.startsWith("put"))
			{
				int pos=cmd.indexOf(" ");
				String fileName=cmd.substring(pos+1);

				File exfile=new File(fileName);
				if(!exfile.exists())
				{
					System.out.println("file doesn't exist");
					continue;
				}
				
				if(exfile.isDirectory())
				{
					System.out.println("it's a directory");
					continue;
				}
				
				outToServer.writeBytes("put");
				outToServer.write('\n');
				outToServer.writeBytes(exfile.getName());
				outToServer.write('\n');
				
				send(exfile);
				
				String ret=inFromServer.readLine();
				System.out.println(ret);		
			}
			else if(cmd.startsWith("get"))
			{
				int pos=cmd.indexOf(" ");
				String fileName=cmd.substring(pos+1);

				outToServer.writeBytes("get");
				outToServer.write('\n');
				outToServer.writeBytes(fileName);
				outToServer.write('\n');
				
				int statusCode=Integer.parseInt(inFromServer.readLine());
				String statusPhrase=inFromServer.readLine();
				
				System.out.println(statusCode);
				System.out.println(statusPhrase);

				if(statusCode==1)
				{
					/* 파일 전송 */
					File mkfile=new File(fileName);
					int fnum=1;
					while(mkfile.exists())
					{
						mkfile=new File(fileName+fnum);
						fnum++;
					}

					receive(mkfile, fileName);
				}
			}
		}
	}
}
