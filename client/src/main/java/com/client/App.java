package com.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
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
        try {
            Socket socket = new Socket("localhost", 3011);
            BufferedReader inputDalServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outputVersoServer = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);

            //invio del nome al server
            System.out.print("Inserisci il tuo nome: ");
            String nomeUtente = scanner.nextLine();
            outputVersoServer.writeBytes(nomeUtente + "\n");

            //ricezione del messaggio di benvenuto del server
            String messaggioBenvenuto = inputDalServer.readLine();
            System.out.println(messaggioBenvenuto);

            //avvio client multithread
            ClientManager clientManager = new ClientManager(socket, inputDalServer, outputVersoServer);
            clientManager.start();
        }
        catch(Exception e){
            System.out.println("Errore nella connessione");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
