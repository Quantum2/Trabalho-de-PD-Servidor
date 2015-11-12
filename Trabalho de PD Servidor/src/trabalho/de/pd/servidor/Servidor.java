/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class Servidor implements Serializable{
    public static final int MAX_SIZE=1000;
    public InetAddress group;
    public int port;
    public int contador=0;
    
    //threads
    public Runnable HeartbeatsEnvia=null;
    public Runnable HeartbeatsRecebe=null;
    
    //UDP
    protected MulticastSocket socketUDP=null;
    private DatagramPacket packetUDP=null;
    
    //TCP
    private ServerSocket socketTCP=null;
    
    Object receiveMSG,sendMSG;
    private boolean debug,Primario,Linked;
    
    public Servidor(String diretoria,int Porto) throws UnknownHostException, IOException, InterruptedException 
    {       
        group=InetAddress.getByName("225.15.15.15");
        packetUDP=new DatagramPacket(new byte[MAX_SIZE],MAX_SIZE);
        port=Porto;
        socketUDP = new MulticastSocket(port);
        socketUDP.joinGroup(group);
        Primario=false;
        Linked=false;
        debug=true;
        
        try{
            constroiThreads();
            começa();
        }catch(NumberFormatException e){
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        }catch(SocketException e){
            System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t"+e);
        }catch(IOException e){
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t"+e);
        }catch(InterruptedException e){
            System.out.println("Ocorreu um erro no sleep");
        }finally{
                closeSocket();
        }
    }
    
    public void constroiThreads() throws IOException,InterruptedException
    {
        //Socket toClientSocket;
        HeartbeatsEnvia = new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        if(Linked==true || Primario==true)
                        {
                            ByteArrayOutputStream byteout = new ByteArrayOutputStream(MAX_SIZE);
                            ObjectOutputStream send = new ObjectOutputStream(byteout);
                            send.writeObject(Primario);
                            packetUDP.setData(byteout.toByteArray());
                            packetUDP.setLength(byteout.size());
                            socketUDP.send(packetUDP);
                            Thread.sleep(5000);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("O porto de escuta deve ser um inteiro positivo.");
                    } catch (SocketException e) {
                        System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t" + e);
                    } catch (IOException e) {
                        System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
                    } catch (InterruptedException e) {
                        System.out.println("Ocorreu um erro no sleep");
                    } finally {
                        
                    }                   
                } while (debug);
            }
        };
        
        HeartbeatsRecebe = new Runnable(){

            @Override
            public void run() {
                do{                  
                    try {
                        if (Linked == false && Primario == false) {
                            if (contador == 3) {
                                Primario = true;
                            } else {
                                ObjectInputStream recv = null;
                                socketUDP.receive(packetUDP);
                                recv = new ObjectInputStream(new ByteArrayInputStream(packetUDP.getData()));
                                Object msg = recv.readObject();
                                if (msg instanceof Boolean) {
                                    if (true == (Boolean) msg) {
                                        Linked = true;
                                        socketTCP = new ServerSocket(socketUDP.getPort(), 0, socketUDP.getInetAddress());
                                        //falta receber todos os dados do servidor primario
                                    }
                                }
                                Thread.sleep(5000);
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("O porto de escuta deve ser um inteiro positivo.");
                    } catch (SocketException e) {
                        System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t" + e);
                    } catch (IOException e) {
                        System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        if(Linked==false && Primario==false)
                            contador++;
                        else{
                            contador=0;
                        }
                    }
                }while(Linked);
            }         
        };
        
    }
    
    public void começa() throws IOException,InterruptedException
    {     
        debug=true;
        if(socketUDP==null)
        {
            return;
        }       
        if(debug)
        {
            System.out.println("UDP do server iniciado...");
        }
        
        while(true)
        {
            /*receiveMSG=receive();    
            if(receiveMSG==null)
            {
                continue;
            }
            */
            //send();
            Thread.sleep(5000);          
        }
    }
    
    public void closeSocket()
    {
        if(socketUDP!=null)
            socketUDP.close();
    }
}
