package com.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerManager extends Thread{
    Socket s;
    HashMap<String, Socket> utentiConnessi = new HashMap<>();
    DataOutputStream outputVersoClient;
    BufferedReader inputDalClient;
    String messaggio = "";
    boolean exit = false;
    String utente = "";
    String[] messaggiBenvenuto = {
        "[%s si e' connesso al server, che si fa ora?]",
        "[%s si e' connesso al server, spero abbia almeno portato qualcosa da mangiare...]",
        "[%s si e' connesso al server, lasciate ogni speranza o voi che entrate...]",
        "[%s si e' connesso al server, un altro utente da tenere a bada]",
        "[%s si e' connesso al server, speriamo non combini guazzabugli]"
    };

    public ServerManager(Socket s, HashMap<String, Socket> utentiConnessi) {
        this.s = s;
        this.utentiConnessi = utentiConnessi;
    }

    @Override
    public void run() {
        try{

            outputVersoClient = new DataOutputStream(s.getOutputStream());
            inputDalClient = new BufferedReader(new InputStreamReader(s.getInputStream()));

            utente = inputDalClient.readLine();

            //controllo se il nome inserito esista o no
            synchronized(utentiConnessi){
                if(utentiConnessi.containsKey(utente)){
                    outputVersoClient.writeBytes("Il nome che hai scelto gia' esiste" + "\n");
                    s.close();
                    return;
                }
                utentiConnessi.put(utente, s);
            }

            stampaMessaggioBenvenuto(utente);
            outputVersoClient.writeBytes("Benvenuto " + utente + ", prima di scrivere il messaggio includi una delle seguenti regole: '@everyone manda in broadcast', '@*username* manda ad un host', '!utenti' mostra gli utenti connessi, '/exit' esci dal programma\n");

            //stampo la notifica di connessione del nuovo client a tutti gli altri
            synchronized(utentiConnessi){
                for(String nomeClient : utentiConnessi.keySet()){
                    if(!nomeClient.equals(utente)){
                        Socket destinatario = utentiConnessi.get(nomeClient);
                        notificaConnessione(utente, destinatario);
                    }
                }
            }

            do {
                outputVersoClient.writeBytes("Scrivi il messaggio che vuoi inviare:" + "\n");
                messaggio = inputDalClient.readLine();
                gestisciMessaggio(messaggio);
            }while(!messaggio.equals("/exit"));

            s.close();
        }
        catch(IOException ioe){
            System.out.println("[E niente, un utente ha deciso di porre fine al tutto...]");
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
        System.out.println(nome + " si e' connesso alla chat");

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

    //metodo che stampa la lista degli utenti connessi al socket (alla chat)
    public void inviaListaUtenti(){
        try{
            ArrayList<String> utentiConnessi = new ArrayList<>();
            utentiConnessi.add(getUtentiConnessi());
            String listaUtenti = "[Utenti connessi: " + utentiConnessi.toString() + "]" + "\n";
            outputVersoClient.writeBytes(listaUtenti);
        }
        catch(IOException e) {
            System.out.println("Errore nell'invio della lista degli utenti connessi");
            System.out.println(e.getMessage());
        }
    }

    //metodo che ritorna l'array di tutte gli utenti connessi alla chat
    public String getUtentiConnessi(){
        return String.join(", ", utentiConnessi.keySet());
    }

    //metodo per notificare tutti i client dell'unione di un nuovo utente
    public void notificaConnessione(String nomeClient, Socket destinatario) {
        try{
            synchronized(utentiConnessi){
                DataOutputStream outputVersoDestinatario = new DataOutputStream(destinatario.getOutputStream());
                String messaggioNotifica = "[" + nomeClient + " si e' unito alla chat]";
                outputVersoDestinatario.writeBytes(messaggioNotifica + "\n");
            }
        }
        catch(IOException e){
            System.out.println("Errore nell'invio della notifica di unione");
            System.out.println(e.getMessage());
        }
    }

    //metodo per gestire come e cosa stampare in base alla richiesta del client
    public void gestisciMessaggio(String messaggio){
        try{
            if(messaggio.startsWith("@")){
                String username = trovaUsername();
                if(messaggio.startsWith("@everyone")){
                    stampaATutti(messaggio); //stampa il messaggio a tutti i client
                }
                else if(messaggio.startsWith("@" + username)){
                    stampaAlClient(messaggio, username); // stampa il messaggio al client interessato
                }
                else{
                    outputVersoClient.writeBytes("Non inserire spazi dopo la chiocciola"); //se il client inserisce la chiocciola ma poi un altro simbolo (presumibilmente uno spazio) manda un warning
                }
            }
            else if(messaggio.equals("!utenti")){
                inviaListaUtenti();
            }
            else{
                outputVersoClient.writeBytes("Rispetta le regole sintattiche, che puoi vedere digitando '!utenti'");
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
                new DataOutputStream(destinatario.getOutputStream()).writeBytes("[Messaggio privato da " + "*mittente ancora da inserire" + "]: " + messaggio + "\n");
            }
            else{
                outputVersoClient.writeBytes("Utente " + destinatario + " non connesso\n");
            }
        }
        catch(Exception e){
            System.out.println("Errore nell'invio del messaggio al client interessato");
            System.out.println(e.getMessage());
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