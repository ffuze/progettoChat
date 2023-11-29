package com.client;

import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try{
            ServerSocket server = new ServerSocket(3001);
            while(true){
                //il client inserisce il nome
                Socket s = server.accept();
                System.out.print("Inserisci il tuo nome: ");
                Scanner scanner = new Scanner(System.in);
                String nomeClient = scanner.nextLine();
                DataOutputStream outputVersoServer = new DataOutputStream(s.getOutputStream());
                outputVersoServer.writeBytes(nomeClient + "\n");
                //il client rimane in ascolto
                ClientManager t = new ClientManager(s);
                t.start();
            }
        }
        catch(Exception e){
            System.out.println("Errore nella connessione");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
