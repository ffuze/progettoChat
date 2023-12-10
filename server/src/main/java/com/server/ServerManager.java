package com.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

public class ServerManager extends Thread{
    Socket s;
    HashMap<String, Socket> utentiConnessi = new HashMap<>();
    DataOutputStream outputVersoClient;
    BufferedReader inputDalClient;
    String messaggio = "";
    boolean exit = false;
    String utente = "";
    private static final String[] messaggiBenvenuto = {
            "%s si è connesso al server, che si fa ora?",
            "%s si è connesso al server, spero abbia almeno portato qualcosa da mangiare...",
            "%s si è connesso al server, lasciate ogni speranza o voi che entrate...",
            "%s si è connesso al server, un altro utente da accudire",
            "%s si è connesso al server, non combinare guazzabugli per piacere"
    };

    public ServerManager(Socket s, HashMap<String, Socket> utentiConnessi) {
        this.s = s;
        this.utentiConnessi = utentiConnessi;
    }

    @Override
    public void run(){
        try{
            outputVersoClient = new DataOutputStream(s.getOutputStream());
            inputDalClient = new BufferedReader(new InputStreamReader(s.getInputStream()));

            utente = inputDalClient.readLine();
            stampaMessaggioBenvenuto(utente);
            //notificaConnessione(messaggio); SONO BLOCCATO QUI -------------------------------------------------------------------------------------------------------------

            outputVersoClient.writeBytes("Benvenuto " + utente + ", prima di scrivere il messaggio includi una delle seguenti regole: '@all manda in broadcast', '@*username* manda ad un host', '@utenti' mostra gli utenti connessi, '/exit' esci dal programma\n");

            do{
                outputVersoClient.writeBytes("Scrivi il messaggio che vuoi inviare:" + "\n");
                messaggio = inputDalClient.readLine();
                gestisciMessaggio(messaggio);
            }while(!messaggio.equals("/exit"));

            s.close();
        }
        catch(IOException ioe){
            System.out.println("E niente, un utente ha deciso di porre fine al tutto...");
            System.out.println(ioe.getMessage());
            System.exit(1);
        }
        catch(Exception e){
            System.out.println("Errore nella connessione");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    //metodo per stampare randomicamente i messaggi di benvenuto
    public void stampaMessaggioBenvenuto(String nome){
        int i = (int)(Math.random()*messaggiBenvenuto.length);
        String messaggio = String.format(messaggiBenvenuto[i], nome);
        System.out.println(messaggio);
    }

    //metodo per gestire chi è entrato o no ecc
    public void gestisciConnessione(String nome){
        utentiConnessi.put(nome, s);
        System.out.println(nome + " si è connesso alla chat");

        for(String nomeClient : utentiConnessi.keySet()){
            if(!nomeClient.equals(nome)){
                Socket clientCorrente = utentiConnessi.get(nomeClient);
                if(clientCorrente != null){
                    notificaConnessione(nome, clientCorrente);
                }
            }
        }
    }

    //metodo per controllare se un utente già esiste o meno
    public boolean isNomeUnico(String nome){
        return !utentiConnessi.containsKey(nome);
    }

    //metodo che ritorna l'array di tutte gli utenti connessi alla chat
    public String[] getUtentiConnessi(){
        return utentiConnessi.keySet().toArray(new String[0]);
    }

    //metodo per notificare tutti i client dell'unione di un nuovo utente
    private void notificaConnessione(String nomeClient, Socket destinatario) {
        try{
            DataOutputStream outputVersoDestinatario = new DataOutputStream(destinatario.getOutputStream());
            String messaggioNotifica = "Il client " + nomeClient + " si è unito alla chat";
            outputVersoDestinatario.writeBytes(messaggioNotifica + "\n");
        }
        catch(IOException e){
            System.out.println("Errore nell'invio della notifica di unione");
            System.out.println(e.getMessage());
        }
    }

    //metodo per gestire come e cosa stampare in base alla richiesta del client
    public void gestisciMessaggio(String messaggio) {
        try{
            if(messaggio.startsWith("@")){
                String username = trovaUsername();
                if(messaggio.startsWith("@all")){
                    stampaATutti(messaggio); // stampa il messaggio a tutti i client
                }
                else if(messaggio.startsWith("@" + username)){
                    stampaAlClient(messaggio, username); // stampa il messaggio al client interessato
                }
                else if(messaggio.equals("@utenti")){
                    String[] utentiConnessi = getUtentiConnessi();
                    //invia la lista al client richiedente
                    outputVersoClient.writeBytes("Utenti connessi: " + String.join(", ", utentiConnessi) + "\n");
                }
            }
            else if(!messaggio.startsWith("@")){
                outputVersoClient.writeBytes("Ricordati di rispettare le regole mostrate precedentemente per inviare un messaggio...");
            }
        }
        catch (Exception e){
            System.out.println("Errore nella gestione del messaggio");
            System.out.println(e.getMessage());
        }
    }

    //metodo per far stampare il messaggio desiderato a tutti i client connessi
    public void stampaATutti(String messaggio){
        try{
            for(Socket client : utentiConnessi.values()){
                if(!client.equals(s)) {
                    new DataOutputStream(client.getOutputStream()).writeBytes(messaggio + "\n");
                }
            }
        }
        catch(Exception e){
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
            Socket destinatario = utentiConnessi.get(username);
            if(destinatario != null){
                new DataOutputStream(destinatario.getOutputStream()).writeBytes(messaggio + "\n");
            }
        }
        catch(Exception e){
            System.out.println("Errore nell'invio del messaggio al client interessato");
            System.out.println(e.getMessage());
        }
    }

    public String getUtente(){
        return utente;
    }

    /*
    //eventuale metodo per uscire da qualche ciclo
    public void terminate(){
        exit = true;
    }
    */
}