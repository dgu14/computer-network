package ftpServer;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerMain {
	public static int 		serverPort		=	2020;
	public static String 	cwd;
	
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
		InetSocketAddress pnum=new InetSocketAddress(serverPort);
		
		@SuppressWarnings("all")
		ServerSocket welcomeSocket=new ServerSocket();
		
		try 
		{
			welcomeSocket.bind(pnum);
		}
		catch(Exception e)
		{
			System.out.println("duplicate port number");
			System.exit(-1);
		}
		
		while(true)
		{
			Socket connectionSocket=welcomeSocket.accept();
			
			InputStream is=connectionSocket.getInputStream();
			OutputStream os=connectionSocket.getOutputStream();
			
			DataInputStream inFromClient=new DataInputStream(is);
			DataOutputStream outToClient=new DataOutputStream(os);

			File dir=new File("./");
			cwd=dir.getCanonicalPath();
			
			while(true)
			{
				cmd=inFromClient.readUTF();
				
				if(cmd.equals("quit"))
				{
					outToClient.flush();
					os.flush();
					
					connectionSocket.close();
					break;
				}
				else if(cmd.equals("list"))
				{
					String rpath; Path pth;
					
					try
					{
						rpath=inFromClient.readUTF();
						pth=Paths.get(rpath);
						
						if(!pth.isAbsolute())
						{
							pth=Paths.get(cwd+"\\"+rpath);
						}
						
						pth=pth.toRealPath();
					}
					catch(Exception e)
					{
						outToClient.writeUTF("405");
						outToClient.writeUTF("Failed-Directory name is invalid");
						continue;
					}
					
					if(pth.toFile().isDirectory()) 
					{
						dir=pth.toFile();
						File[] ret=dir.listFiles();
						String rr = "";
						
						for(int i=0;i<ret.length;i++)
						{
							rr+=ret[i].getName();
							rr+=',';
							
							if(ret[i].isDirectory()) rr+='-';
							else rr+=ret[i].length();
							
							if(i!=ret.length-1) rr+=',';
						}
						
						outToClient.writeUTF("200");
						outToClient.writeUTF(Integer.toString(ret.length));
						outToClient.writeUTF(rr);
					}
					else
					{
						outToClient.writeUTF("404");
						outToClient.writeUTF("Failed-Directory name is invalid");
					}
				}
				else if(cmd.equals("cd"))
				{
					String rpath=inFromClient.readUTF();			
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
						outToClient.writeUTF("404");
						outToClient.writeUTF("Failed-directory name is invalid");
						continue;
					}
					
					outToClient.writeUTF("200");
					outToClient.writeUTF(Integer.toString(cwd.length()));
					outToClient.writeUTF(cwd);
				}
				else if(cmd.equals("put"))
				{
					String fileName=inFromClient.readUTF();
					File mkfile=new File(cwd+"\\"+fileName);
					
					/* 파일 이름이 같은 게 있을 때 처리 */
					int fnum=1;
					while(mkfile.exists())
					{
						mkfile=new File(cwd+"\\"+fileName+fnum);
						fnum++;
					}

					long fileLength=Long.parseLong(inFromClient.readUTF()); long frm=fileLength;
					int readBytes;
					byte[] buf=new byte[4096];
					
					try {
					FileOutputStream fos=new FileOutputStream(mkfile);
					
					while(fileLength>0)
					{
						readBytes=is.read(buf, 0, (int)Math.min(fileLength, 4096L));
						fos.write(buf,0,readBytes);
						fileLength-=readBytes;
					}
					
					fos.close();
					
					}
					catch (Exception e)
					{
						outToClient.writeUTF("400");
						outToClient.writeUTF("Failed for unknown reason");
					}
					
					outToClient.writeUTF("200");
					outToClient.writeUTF(mkfile.getName() + " transferred / " + frm + " bytes");
				}
				else if(cmd.equals("get"))
				{
					String fileName=inFromClient.readUTF();
					File mkfile=new File(cwd+"\\"+fileName);
					
					if(mkfile.exists() && !mkfile.isDirectory())
					{
						outToClient.writeUTF("200");
						
						long fileLength=mkfile.length();
						int readBytes;
						byte[] buf=new byte[4096];
					
						FileInputStream fis=new FileInputStream(mkfile);
						outToClient.writeUTF(Long.toString(fileLength));
						
						while(fileLength>0)
						{
							readBytes=fis.read(buf);
							os.write(buf,0,readBytes);
							fileLength-=readBytes;
						}
						
						fis.close();
					}
					else
					{
						outToClient.writeUTF("405");
						outToClient.writeUTF("Failed-Such file does not exist");
					}
				}
			}
		}
	}
}
