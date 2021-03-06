import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    public static void main(String[] args) throws IOException
    {
        try
        {
            Scanner reader = new Scanner(System.in);
            Socket serverSocket = new Socket("127.0.0.1",2345);
            DataInputStream getInformation = new DataInputStream(serverSocket.getInputStream());
            DataOutputStream sendInformation = new DataOutputStream(serverSocket.getOutputStream());

            while(true)
            {
                System.out.println(getInformation.readUTF());
                String information = reader.nextLine();
                sendInformation.writeUTF(information);

                if(information.equals("Exit"))
                {
                    serverSocket.close();
                    System.out.println("Connection closed");
                    break;
                }
                String received = getInformation.readUTF();
                System.out.println("Answer:");
                System.out.println(received);
            }
            reader.close();
            getInformation.close();
            sendInformation.close();
        }
        catch(Exception e)
        {
            System.out.println("System error");
        }
    }
}