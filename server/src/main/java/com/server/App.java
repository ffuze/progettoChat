package com.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
            ArrayList<ServerManager> listaThread = new ArrayList<>();
            while(true){
                Socket s = server.accept();
                ServerManager t = new ServerManager(s, listaThread); //
                listaThread.add(t);
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