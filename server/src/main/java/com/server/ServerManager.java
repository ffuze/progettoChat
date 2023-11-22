package com.server;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerManager extends Thread{
    Socket s;
    boolean exit = false;

    public ServerManager(Socket s){
        this.s = s;
    }

    public void terminate(){
        exit = true;
    }

    @Override
    public void run(){
        try{
            DataOutputStream outputVersoClient = new DataOutputStream(s.getOutputStream());
            BufferedReader inputDalClietnt = new BufferedReader(new InputStreamReader(s.getInputStream()));
        }
        catch(Exception e){
            System.out.println("Errore nella connessione");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
