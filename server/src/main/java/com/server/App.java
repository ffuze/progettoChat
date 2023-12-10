package com.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try{
            ServerSocket server = new ServerSocket(3011);
            System.out.println("Server avviato ed in ascolto...");
            HashMap<String, Socket> utentiConnessi = new HashMap<>();
            while(true){
                Socket s = server.accept();
                ServerManager t = new ServerManager(s, utentiConnessi);
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