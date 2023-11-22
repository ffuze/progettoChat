package com.server;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try{

        }
        catch(Exception e){
            System.out.println("Errore nella connessione");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
