package com.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientManager extends Thread {
    private Socket socket;
    private BufferedReader inputDalServer;
    private DataOutputStream outputVersoServer;

    public ClientManager(Socket socket, BufferedReader inputDalServer, DataOutputStream outputVersoServer) {
        this.socket = socket;
        this.inputDalServer = inputDalServer;
        this.outputVersoServer = outputVersoServer;
    }

    @Override
    public void run(){
        try{
            String messaggio;
            do{
                System.out.println(inputDalServer.readLine());
                BufferedReader inputUtente = new BufferedReader(new InputStreamReader(System.in));
                messaggio = inputUtente.readLine();
                System.out.println(messaggio);
                outputVersoServer.writeBytes(messaggio + "\n");

                //ricezione dei messaggi dal server
                String rispostaServer = inputDalServer.readLine();
                System.out.println(rispostaServer);

                if(messaggio.startsWith("Utenti connessi:")){
                    System.out.println(messaggio);
                }
            }while(!messaggio.equals("/exit"));
            socket.close();
        }
        catch(Exception e){
            System.out.println("Errore durante l'esecuzione del client");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}