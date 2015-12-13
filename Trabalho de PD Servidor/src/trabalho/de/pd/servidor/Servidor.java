/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
    public int TCPport;
    public ListaFicheiros listaFicheiros=null;
    long tStart=0,tFinal=0;
    
    //threads
    public HeartbeatsRecebe heartRECV=null;
    public HeartbeatsEnvia heartENVIA=null;
    public RecebeActualizacaoTCP recebeActualizacaoTCP=null;
    public RecebeActualizacaoTCP RcActualizacaoTCP=null;
    public RecebePedido recebePedido=null;
    
    //UDP
    protected MulticastSocket multicastSocketUDP=null;
    
    //TCP
    public ServerSocket serverSocketTCP=null;
    public ServerSocket serverSocketAtualizacao=null;
    public Socket primarioSocketTCP=null;
    public Socket socketAtualizacao=null;
    
    //boolean
    private Boolean primario=null;
    
    public Servidor(String diretoria,int TCPport) throws UnknownHostException, IOException, InterruptedException 
    {      
        this.TCPport=TCPport;
        this.diretoria=diretoria;    
        
        File folder = new File(diretoria);                        
        File[] arrayFicheiros = folder.listFiles();
        listaFicheiros=new ListaFicheiros();
        for(File ficheiro : arrayFicheiros){
            Ficheiro ficheiroTemp=new Ficheiro(ficheiro.getName(),ficheiro.length());
            listaFicheiros.addFicheiro(ficheiroTemp);
            System.out.println(ficheiroTemp.toString());
        }
                
        group=InetAddress.getByName("225.15.15.15");
        multicastSocketUDP = new MulticastSocket(7000);
        multicastSocketUDP.joinGroup(group);
        multicastSocketUDP.setSoTimeout(5000);
        
        //TCP  //nao sei se e o porto 7000 nao diz nada acho :S
        serverSocketTCP=new ServerSocket(TCPport);
        serverSocketTCP.setSoTimeout(5000);
        
        
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
            this.primarioSocketTCP.setSoTimeout(5000);
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
    
    public void começa() {
        while (true) {
            int contador = 0;
            DatagramPacket packet = null;
            while (contador != 3) {
                try {
                    packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
                    System.out.println("[SERVIDOR] Vai receber heartbeat");
                    getMulticastSocket().receive(packet);

                    ObjectInputStream recv = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                    
                    HeartBeat msg = (HeartBeat)recv.readObject();
                    if (msg.getPrimario()) {
                        System.out.println("[SERVIDOR] Recebeu heartbeat primário");
                        tStart=System.currentTimeMillis();
                        conectaServidorPrimario(packet.getAddress(), msg.getTcpPort()); //mudar o port para tcpport
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("O porto de escuta deve ser um inteiro positivo.");
                } catch (SocketException e) {
                    System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t" + e);
                } catch (SocketTimeoutException e) {

                } catch (IOException e) {
                    System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    contador++;
                    System.out.println("[SERVIDOR] Contador: " + contador);
                    if (contador == 3 && primarioSocketTCP==null) {
                        System.out.println("[SERVIDOR] Tornou-se servidor primário");
                        setPrimario(true);
                        break;
                    }
                }
            }
            System.out.println("[SERVIDOR] Antes de arrancar threads");
            arrancaThreads();
            System.out.println("[SERVIDOR] Depois de arrancar threads");
            try {
                heartRECV.join();
                System.out.println("[SERVIDOR] Depois de join de heartRECV");
            } catch (InterruptedException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            recomeça();
            System.out.println("[SERVIDOR] Vai recomeçar ciclo");
        }
    }
    
    public void arrancaThreads()
    {
        /*
        if(!isPrimario()){           
            try {
                recebeActualizacaoTCP = new RecebeActualizacaoTCP(this);
                recebeActualizacaoTCP.start();
            } catch (IOException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        */
        
        try {
            heartRECV=new HeartbeatsRecebe(this);
            heartRECV.start();
            
            heartENVIA=new HeartbeatsEnvia(this);
            heartENVIA.start();
            
            recebePedido=new RecebePedido(this);
            recebePedido.start();
            
        } catch (SocketException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void arrancaThreadEnviaFicheiro(Socket pedidosSocketTCP,Pedido pedido){
        EnviaFicheiro enviaFicheiro=new EnviaFicheiro(this,pedidosSocketTCP,pedido.getNomeFicheiro());
        enviaFicheiro.start();
    }
    
    public void arrancaThreadRecebeFicheiro(Socket pedidosSocketTCP){
        RecebeFicheiro recebeFicheiro=new RecebeFicheiro(this,pedidosSocketTCP);
        recebeFicheiro.start();
    }
    
    public void arrancaThreadEliminaFicheiro(Pedido pedido){
        EliminarFicheiro eliminarFicheiro=new EliminarFicheiro(this,pedido.getNomeFicheiro());
        eliminarFicheiro.start();
    }
    
    public void recomeça(){       
        
        try {
            heartENVIA.termina();
            heartENVIA.join();
            
            /*recebeActualizacaoTCP.termina();
            recebeActualizacaoTCP.join();*/
            
            recebePedido.termina();
            recebePedido.join();
            
            this.primarioSocketTCP=null;
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public void closeSocket() throws IOException
    {
        System.out.println("Acabou!!!");
        if(multicastSocketUDP!=null)
            multicastSocketUDP.close();
    }
    
    public Boolean isPrimario() {
        return primario;
    }
    
    public void setPrimario(Boolean primario) {
        this.primario=primario;
    }
    
    public Socket getPrimarioSocketTCP(){
        return primarioSocketTCP;
    }
    
    public String getDiretoria(){
        return diretoria;
    }
    
    public ListaFicheiros getListaFicheiros(){
        return listaFicheiros;
    }
    
    public void setListaFicheiros(ListaFicheiros listaFicheiros){
        this.listaFicheiros=listaFicheiros;
    }
    
    public ServerSocket getServerSocketTCP(){
        return serverSocketTCP;
    }
    
    public InetAddress getGroup(){
        return group;
    }
    
    public int getTcpPort(){
        return TCPport;
    }
    
    public long getTStart(){
        return tStart;
    }
    
    public long getTFinal(){
        return tFinal;
    }
    
    public void setTStart(long tStart){
        this.tStart=tStart;
    }
    
    public void setTFinal(long tFinal){
        this.tFinal=tFinal;
    }
}
