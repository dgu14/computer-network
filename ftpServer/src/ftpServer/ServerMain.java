package ftpServer;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerMain {
	public static int 		serverPort		=	6789;
	public static int 		serverFilePort	=	6790;
	public static String 	cwd;
	public static void main(String args[]) throws Exception
	{
		String cmd;
		
		InetSocketAddress pnum=new InetSocketAddress(serverPort);
		ServerSocket welcomeSocket=new ServerSocket();
		welcomeSocket.bind(pnum);
		
		FileServerThread thr=new FileServerThread();
		thr.start();
		
		while(true)
		{
			Socket connectionSocket=welcomeSocket.accept();
			
			BufferedReader inFromClient=new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient=new DataOutputStream(connectionSocket.getOutputStream());

			File dir=new File("./");
			cwd=dir.getCanonicalPath();
			
			while(true)
			{
				cmd=inFromClient.readLine();
				dir=new File(cwd);
				
				if(cmd.equals("quit"))
				{
					//welcomeSocket.close();
					break;
				}
				else if(cmd.equals("list"))
				{
					File[] ret=dir.listFiles();
					outToClient.writeBytes(Integer.toString(ret.length));
					outToClient.write('\n');
					
					for(int i=0;i<ret.length;i++)
					{
						outToClient.writeBytes(ret[i].getName());
						outToClient.write('\n');
					}
				}
				else if(cmd.equals("cwd"))
				{
					outToClient.writeBytes(cwd);
					outToClient.write('\n');
				}
				else if(cmd.equals("cd"))
				{
					String rpath=inFromClient.readLine();
					if(rpath=="")
					{
						outToClient.writeBytes(cwd);
						outToClient.write('\n');
						continue;
					}
					
					Path pth=Paths.get(rpath);
					
					try
					{
						if(pth.isAbsolute())
						{
							Path newCwd=pth.toRealPath();
							if(newCwd.toFile().isDirectory()) 
							{
								String nncwd=newCwd.toFile().getCanonicalPath();
								if(nncwd!=null) cwd=nncwd;
							}
						}
						else
						{
							Path ppth=Paths.get(cwd+"\\"+rpath);
							Path newCwd=ppth.toRealPath();
							if(newCwd.toFile().isDirectory()) 
							{
								String nncwd=newCwd.toFile().getCanonicalPath();
								if(nncwd!=null) cwd=nncwd;
							}
						}
					} 
					catch(Exception e) {
						//e.printStackTrace();
					}
					outToClient.writeBytes(cwd);
					outToClient.write('\n');
				}
				else if(cmd.equals("put"))
				{
					String fileName=inFromClient.readLine();
					File mkfile=new File(cwd+"\\"+fileName);
					
					/* just overlap.
					int fnum=1;
					while(mkfile.exists())
					{
						mkfile=new File(cwd+"\\"+fileName+fnum);
						fnum++;
					}
					*/
					
					outToClient.writeBytes(mkfile.getCanonicalPath());
					outToClient.write('\n');
				}
				else if(cmd.equals("get"))
				{
					String fileName=inFromClient.readLine();
					File mkfile=new File(cwd+"\\"+fileName);
					
					if(mkfile.exists() && !mkfile.isDirectory())
					{
						outToClient.writeBytes("1");
						outToClient.write('\n');
						outToClient.writeBytes("OK");
						outToClient.write('\n');
					}
					else
					{
						outToClient.writeBytes("-1");
						outToClient.write('\n');
						outToClient.writeBytes("no such file");
						outToClient.write('\n');
					}
				}
			}
		}
	}
}
