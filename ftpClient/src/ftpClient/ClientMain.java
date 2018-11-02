package ftpClient;

import java.io.*;
import java.net.*;

public class ClientMain {
	public static String 	serverIp		=	"127.0.0.1";
	public static int 		serverPort		=	2020;

	public static void main(String args[]) throws Exception
	{
		if(args.length>0)
		{
			try
			{
				serverPort=Integer.parseInt(args[0]);
			}
			catch(Exception e)
			{
				serverPort=2020;
			}
		}
		
		String cmd;
		BufferedReader cin=new BufferedReader(new InputStreamReader(System.in));
		Socket clientSocket=new Socket(serverIp, serverPort);
		
		OutputStream os=clientSocket.getOutputStream();
		InputStream  is=clientSocket.getInputStream();
		
		DataOutputStream outToServer=new DataOutputStream(os);
		DataInputStream inFromServer=new DataInputStream(is);

		while(true)
		{
			cmd=cin.readLine();
			cmd=cmd.toLowerCase();
			
			if(cmd.equals("quit"))
			{
				outToServer.writeUTF("quit");
				outToServer.flush();
				clientSocket.close();
				break;
			}
			else if(cmd.startsWith("list"))
			{
				int pos=cmd.indexOf(" ");
				String path;
				if(pos==-1) path=".";
				else path=cmd.substring(pos+1);
				
				outToServer.writeUTF("list");
				outToServer.writeUTF(path);

				String statusCode=inFromServer.readUTF();
				if(statusCode.equals("200"))
				{
					String reponseLength=inFromServer.readUTF();
					String response=inFromServer.readUTF();
					
					//System.out.println(statusCode);
					//System.out.println(response.length());
					
					int len=Integer.parseInt(reponseLength), c=0;
					String[] fname=new String[len];
					String[] fsize=new String[len];
					String cstr="";
					
					for(int i=0;i<=response.length();i++)
					{
						if(i==response.length() || response.charAt(i)==',')
						{
							if(c%2==0) fname[c/2]=cstr;
							else fsize[c/2]=cstr;
							cstr="";
							c++;
						}
						else cstr+=response.charAt(i);
					}
					
					for(int i=0;i<len;i++)
					{
						System.out.print(fname[i]+','+fsize[i]+'\n');
					}
				}
				else
				{
					String statusPhrase=inFromServer.readUTF();
					//System.out.println(statusCode);
					System.out.println(statusPhrase);
				}
			}
			else if(cmd.startsWith("cd"))
			{
				int pos=cmd.indexOf(" ");
				String path;
				if(pos==-1) path=".";
				else path=cmd.substring(pos+1);
				
				outToServer.writeUTF("cd");
				outToServer.writeUTF(path);
				
				String statusCode=inFromServer.readUTF();

				if(statusCode.equals("200"))
				{
					String responseLength=inFromServer.readUTF();
					String finalPath=inFromServer.readUTF();
					//System.out.println(statusCode);
					//System.out.println(responseLength);
					System.out.println(finalPath);
				}
				else
				{
					String statusPhrase=inFromServer.readUTF();
					//System.out.println(statusCode);
					System.out.println(statusPhrase);
				}
			}
			else if(cmd.startsWith("put"))
			{
				int pos=cmd.indexOf(" ");
				String fileName=cmd.substring(pos+1);

				File exfile=new File(fileName);
				if(!exfile.exists())
				{
					System.out.println("No such file exist");
					continue;
				}
				
				if(exfile.isDirectory())
				{
					System.out.println("No such file exist");
					continue;
				}
				
				outToServer.writeUTF("put");
				outToServer.writeUTF(exfile.getName());
				
				/* 파일 사이즈 */
				long fileLength=exfile.length();
				outToServer.writeUTF(Long.toString(fileLength));
				
				
				int readBytes;
				byte[] buf = new byte[4096];
				FileInputStream fis=new FileInputStream(exfile);
				
				while(fileLength>0)
				{
					readBytes=fis.read(buf);
					os.write(buf,0,readBytes);
					fileLength-=readBytes;
				}
				
				fis.close();
				
				String statusCode=inFromServer.readUTF();
				String statusPhrase=inFromServer.readUTF();
				//System.out.println(statusCode);
				System.out.println(statusPhrase);
			}
			else if(cmd.startsWith("get"))
			{
				int pos=cmd.indexOf(" ");
				String fileName=cmd.substring(pos+1);

				outToServer.writeUTF("get");
				outToServer.writeUTF(fileName);
				
				File mkfile=new File(fileName);
				int fnum=1;
				while(mkfile.exists())
				{
					mkfile=new File(fileName+fnum);
					fnum++;
				}
				
				String statusCode=inFromServer.readUTF();
				if(statusCode.equals("200"))
				{
					long fileLength=Long.parseLong(inFromServer.readUTF()); long frm=fileLength;
					int readBytes;
					byte[] buf=new byte[4096];
					
					FileOutputStream fos=new FileOutputStream(mkfile);
					
					while(fileLength>0)
					{
						readBytes=is.read(buf, 0, (int)Math.min(fileLength, 4096L));
						fos.write(buf, 0, readBytes);
						fileLength-=readBytes;
					}
					
					fos.close();
					System.out.println("Received " + mkfile.getName() + " / " + frm + " bytes");
				}
				else 
				{
					String statusPhrase=inFromServer.readUTF();
					//System.out.println(statusCode);
					System.out.println(statusPhrase);
				}
			}
			else
			{
				System.out.println("No such command, (cd,list,get,put only)");
			}
		}
	}
}
