package com.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class ServerManager extends Thread{
    Socket s;
    ArrayList<Thread> listaThread = new ArrayList<>();
    DataOutputStream outputVersoClient;
    BufferedReader inputDalClient;
    String messaggio = "";
    boolean exit = false;

    public ServerManager(Socket s,  ArrayList<Thread> listaThread){
        this.s = s;
        this.listaThread = listaThread;
    }

    public void stampaATutti(String messaggio){
        try{
            for(Thread t : listaThread){
                outputVersoClient.writeBytes(messaggio + "\n");
            }
        }
        catch(Exception e){
            System.out.println("Errore nella fase di scorrimento dell'array");
            System.out.println(e.getMessage());
        }
    }

    public String trovaUsername(){
        int atIndex = messaggio.indexOf('@');
        int spaceIndex = messaggio.indexOf(' ', atIndex);
        return messaggio.substring(atIndex + 1, spaceIndex);
    }

    public void stampaAlClient(String messaggio, String username){
        try{
            for(Thread t : listaThread){
                if(true/*t.containsUsername(username)*/){
                    outputVersoClient.writeBytes(messaggio);
                }
            }
        }
        catch(Exception e){
            System.out.println("Errore nella fase di scorrimento dell'array");
            System.out.println(e.getMessage());
        }
    }

    public void terminate(){
        exit = true;
    }

    @Override
    public void run(){
        try{
            outputVersoClient = new DataOutputStream(s.getOutputStream());
            inputDalClient = new BufferedReader(new InputStreamReader(s.getInputStream()));

            System.out.println("CLIENT CONNESSO"); 

            outputVersoClient.writeBytes("Benvenuto client, prima di scrivere il messaggio includi una delle seguenti regole:\n");
            outputVersoClient.writeBytes("@all: per mandare un messaggio a tutti\n");
            outputVersoClient.writeBytes("@username: manda un messaggio ad un singolo utente\n");
            outputVersoClient.writeBytes("/exit: per uscire dal programma\n");

            do{
                outputVersoClient.writeBytes("Scrivi il messaggio che vuoi inviare:" + "\n");
                messaggio = inputDalClient.readLine();
                String username = trovaUsername();
                if(messaggio.startsWith("@")){
                    if(messaggio.startsWith("@all")){
                        stampaATutti(messaggio);
                    }
                    else if(messaggio.startsWith("@" + username)){
                        stampaAlClient(messaggio, username);
                    }
                }
            }while(!messaggio.equals("/exit"));
        }
        catch(Exception e){
            System.out.println("Errore nella connessione");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
