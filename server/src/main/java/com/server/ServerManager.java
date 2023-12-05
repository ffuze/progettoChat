package com.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class ServerManager extends Thread{
    Socket s;
    ArrayList<ServerManager> listaThread = new ArrayList<>();
    DataOutputStream outputVersoClient;
    BufferedReader inputDalClient;
    String messaggio = "";
    boolean exit = false;

    public ServerManager(Socket s, ArrayList<ServerManager> listaThread){
        this.s = s;
        this.listaThread = listaThread;
    }

    //metodo per far stampare il messaggio desiderato a tutti i client connessi
    public void stampaATutti(String messaggio) {
        try{
            for(ServerManager serverManager : listaThread){
                serverManager.outputVersoClient.writeBytes(messaggio + "\n");
            }
        }
        catch(Exception e) {
            System.out.println("Errore nella fase di scorrimento dell'array");
            System.out.println(e.getMessage());
        }
    }

    //metodo per trovare lo username del client interessato
    public String trovaUsername(){
        int atIndex = messaggio.indexOf('@');
        int spaceIndex = messaggio.indexOf(' ', atIndex);
        return messaggio.substring(atIndex + 1, spaceIndex);
    }

    //metodo per far stampare il messaggio desiderato al client desiderato
    public void stampaAlClient(String messaggio, String username){
        try{
            for(ServerManager serverManager : listaThread){
                if(serverManager != this){ //per evitare che il messaggio inviato dal mittente non lo riceva nuovamente pure lui (oltre al client)
                    if(serverManager.trovaUsername().equals(username)) {
                        serverManager.outputVersoClient.writeBytes(messaggio + "\n");
                    }
                }
            }
        }
        catch(Exception e){
            System.out.println("Errore nella fase di scorrimento dell'array");
            System.out.println(e.getMessage());
        }
    }

    /*
    //eventuale metodo per uscire da qualche ciclo
    public void terminate(){
        exit = true;
    }
    */

    @Override
    public void run(){
        try{
            outputVersoClient = new DataOutputStream(s.getOutputStream());
            inputDalClient = new BufferedReader(new InputStreamReader(s.getInputStream()));

            System.out.println("CLIENT CONNESSO"); 

            outputVersoClient.writeBytes("Benvenuto client, prima di scrivere il messaggio includi una delle seguenti regole:\n"); //1 regole + scrittura del messaggio
            outputVersoClient.writeBytes("@all: per mandare un messaggio a tutti\n"); //1 regole + scrittura del messaggio
            outputVersoClient.writeBytes("@username: manda un messaggio ad un singolo utente\n"); //1 regole + scrittura del messaggio
            outputVersoClient.writeBytes("/exit: per uscire dal programma\n"); //1 regole + scrittura del messaggio

            do{
                outputVersoClient.writeBytes("Scrivi il messaggio che vuoi inviare:" + "\n"); //1 regole + scrittura del messaggio
                
                messaggio = inputDalClient.readLine(); //2 messaggio del client che arriva al server
                String username = trovaUsername();
                if(messaggio.startsWith("@")){
                    if(messaggio.equals("@all")){
                        stampaATutti(messaggio); //3 stampa del messaggio a tutti i client
                    }
                    else if(messaggio.startsWith("@" + username)){
                        stampaAlClient(messaggio, username); //4 stampa del messaggio al client interessato
                    }
                }
            }while(!messaggio.equals("/exit"));

            s.close();
        }
        catch(Exception e){
            System.out.println("Errore nella connessione");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}