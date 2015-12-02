/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ASUS
 */
public class Servidor implements Serializable{
    public static final int MAX_SIZE=10000;
    public String diretoria=null;
    public InetAddress group;
    public int port;
    ListaFicheiros listaFicheiros=null;
    
    //threads
    public HeartbeatsRecebe heartRECV=null;
    public HeartbeatsEnvia heartENVIA=null;
    public RecebeActualizacaoTCP RcActualizacaoTCP=null;
    public EnviaActualizacaoOURespostaClienteTCP EnviaRespostaTCP=null;
    
    //UDP
    protected MulticastSocket multicastSocketUDP=null;
    private DatagramPacket SendpacketUDP=null;
    private DatagramPacket RecvpacketUDP=null;
    private DatagramSocket SocketComDiretoria=null;
    
    //TCP
    public ServerSocket serverSocketTCP=null;
    public Socket primarioSocketTCP=null;
    
    //boolean
    private Boolean primario=null;
    
    private boolean debug;
    
    public Servidor(String diretoria,int TCPport) throws UnknownHostException, IOException, InterruptedException 
    {      
        port=TCPport;
        this.diretoria=diretoria;    
        
        File folder = new File(diretoria);                        
        File[] arrayFicheiros = folder.listFiles();
        listaFicheiros=new ListaFicheiros();
        for(File ficheiro : arrayFicheiros){
            Ficheiro ficheiroTemp=new Ficheiro(ficheiro.getName(),ficheiro.length());
            listaFicheiros.addFicheiro(ficheiroTemp);
        }
        
        //UDP
        SendpacketUDP=new DatagramPacket(new byte[MAX_SIZE],MAX_SIZE,group,port);
        SocketComDiretoria=new DatagramSocket();
        
        
        group=InetAddress.getByName("225.15.15.15");
        multicastSocketUDP = new MulticastSocket(7000);
        multicastSocketUDP.joinGroup(group);
        multicastSocketUDP.setSoTimeout(5000);
           
        //TCP  //nao sei se e o porto 7000 nao diz nada acho :S
        serverSocketTCP=new ServerSocket(TCPport);
        
        //boolean
        primario=false;
        System.out.println("Servidor a correr....");
    }
    
    public MulticastSocket getMulticastSocket(){
        return multicastSocketUDP;
    }
    
    public void conectaServidorPrimario(InetAddress addr,int port){
        try {
            this.primarioSocketTCP=new Socket(addr,port);
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void recebeListaFicheiros(){
        try {
            ObjectInputStream ois=new ObjectInputStream(primarioSocketTCP.getInputStream());
            listaFicheiros = (ListaFicheiros)ois.readObject();
            ois.close();
            for(File file:new File(diretoria).listFiles()) {
                file.delete();
            }               
            ObjectOutputStream oos = new ObjectOutputStream(primarioSocketTCP.getOutputStream());
            for (Ficheiro ficheiro : listaFicheiros.getArrayListFicheiro()) {
                RecebeActualizacaoTCP recebeFicheiro = new RecebeActualizacaoTCP(this);
                recebeFicheiro.start();
                oos.writeObject(new Pedido(ficheiro.getNome(),Pedido.DOWNLOAD));
                oos.flush();
            }
            
            oos.close();
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void começa()
    {
        try {
            heartRECV = new HeartbeatsRecebe(this);
            heartRECV.start();
            
            /*heartENVIA = new HeartbeatsEnvia(SendpacketUDP, heartRECV); 
            heartENVIA.start();
            */
            heartRECV.join();
            //heartENVIA.join();
        } catch (SocketException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        

/*
         RcActualizacaoTCP=new RecebeActualizacaoTCP(heartRECV.getIpPrimario(),heartRECV.getPortoPrimario(),diretoria);
         RcActualizacaoTCP.start();
                
         SocketTCP = serversocketTCP.accept();
         EnviaRespostaTCP=new EnviaActualizacaoOURespostaClienteTCP(SocketTCP,diretoria);
         EnviaRespostaTCP.start();*/

        
    }
    
    public void closeSocket() throws IOException
    {
        System.out.println("Acabou!!!");
        if(multicastSocketUDP!=null)
            multicastSocketUDP.close();
    }
    
    public boolean isPrimario() {
        return primario;
    }
    
    public void setPrimario(boolean primario) {
        this.primario=primario;
    }
}
