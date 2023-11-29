package com.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientManager extends Thread{
    Socket s;

    public ClientManager(Socket s){
        this.s = s;
    }

    @Override
    public void run(){
        try{
            DataOutputStream outputVersoClient = new DataOutputStream(s.getOutputStream());
            BufferedReader inputDalClient = new BufferedReader(new InputStreamReader(s.getInputStream()));

            //
            inputDalClient.readLine();
        }
        catch(){
            
        }
    }
}  