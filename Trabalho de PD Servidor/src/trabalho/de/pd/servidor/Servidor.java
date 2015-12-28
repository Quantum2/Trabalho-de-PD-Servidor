/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import static trabalho.de.pd.servidor.SegundoCommitTCP.MAX_SIZE;
/**
 *
 * @author ASUS
 */
public class Servidor implements Serializable{
    //RMI
    RMIServidor rmiServidor=null;
    
    public static final int MAX_SIZE=10000;
    public String diretoria=null;
    public InetAddress group;
    public int TCPport;
    public ListaFicheiros listaFicheiros=null;
    long tStart=0,tFinal=0;
    
    //Arrays dos sockets a serem usados nas threads
    ArrayList<Socket> socketsClientes = new ArrayList<>();
    ArrayList<Socket> socketsSecundarios = new ArrayList<>();
    ArrayList<RecebePedidoCliente> threadsPedidoCliente = new ArrayList<>();
    ArrayList<RecebePedidoSecundario> threadsPedidoSecundario = new ArrayList<>();
    
    //threads
    public HeartbeatsRecebe heartRECV=null;
    public HeartbeatsEnvia heartENVIA=null;
    public SegundoCommitTCP segundoCommitTCP=null;
    public SegundoCommitTCP RcActualizacaoTCP=null;
    public TrataCliente trataCliente=null;
    public TrataSecundario trataSecundario=null;
    
    //UDP
    protected MulticastSocket multicastSocketUDP=null;
    
    //TCP
    public ServerSocket serverSocketCliente=null;
    public ServerSocket serverSocketSecundario=null;
    public Socket primarioSocketTCP=null;
    public Socket socketAtualizacao=null;
    
    //boolean
    private Boolean primario=null;
    
    public Servidor(String diretoria,int TCPport) throws UnknownHostException, IOException, InterruptedException 
    {      
        this.TCPport=TCPport;
        this.diretoria=diretoria;    
        
        actualizaListaFicheiros();
        
        group=InetAddress.getByName("225.15.15.15");
        multicastSocketUDP = new MulticastSocket(7000);
        multicastSocketUDP.joinGroup(group);
        multicastSocketUDP.setSoTimeout(5000);
        
        //TCP
        try {
            serverSocketCliente = new ServerSocket(TCPport);
            serverSocketCliente.setSoTimeout(5000);
            serverSocketSecundario = new ServerSocket(TCPport + 1);
            serverSocketSecundario.setSoTimeout(5000);
        } catch (IOException e) {
            System.out.println("Porta ja a ser utilizada...");
        }
        
        //boolean
        primario=false;
        System.out.println("Servidor a correr....");
    }
    
    public void actualizaListaFicheiros(){
        File folder = new File(diretoria);
        if(!folder.exists()){
            try {
                folder.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        File[] arrayFicheiros = folder.listFiles();
        listaFicheiros=new ListaFicheiros();
        for(File ficheiro : arrayFicheiros){
            Ficheiro ficheiroTemp=new Ficheiro(ficheiro.getName(),ficheiro.length());
            listaFicheiros.addFicheiro(ficheiroTemp);
            System.out.println(ficheiroTemp.toString());
        }
    }
    
    public MulticastSocket getMulticastSocket(){
        return multicastSocketUDP;
    }
    
    public void conectaServidorPrimario(InetAddress addr,int port){
        try {
            this.primarioSocketTCP=new Socket(addr,port+1);
            this.primarioSocketTCP.setSoTimeout(5000);
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void recebeListaFicheiros() {
        try {
            ObjectInputStream ois = new ObjectInputStream(primarioSocketTCP.getInputStream());
            listaFicheiros = (ListaFicheiros) ois.readObject();
            for (File file : new File(diretoria).listFiles()) {
                file.delete();
            }
            
            for (Ficheiro ficheiro : listaFicheiros.getArrayListFicheiro()) {
                File folder = new File(diretoria + "\\" + ficheiro.getNome());
                if (!folder.exists()) {
                    folder.createNewFile();
                }
                ObjectOutputStream oos = new ObjectOutputStream(primarioSocketTCP.getOutputStream());
                Pedido pedido=new Pedido(ficheiro.getNome(), Pedido.DOWNLOAD);
                oos.writeObject(pedido);
                oos.flush();
                try {
                    int nbytes;
                    byte[] filechunck = new byte[MAX_SIZE];
                    FileOutputStream fOut = new FileOutputStream(diretoria + "\\" + ficheiro.getNome());
                    InputStream oips=primarioSocketTCP.getInputStream();
                    while ((nbytes = oips.read(filechunck)) > 0) {
                        fOut.write(filechunck, 0, nbytes);
                    }
                } catch (IOException ex) {
                    System.out.println("[SERVIDOR]Timeout ao fazer actualizacao...");
                    //Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
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
                    System.out.println("[SERVIDOR]O porto de escuta deve ser um inteiro positivo.");
                } catch (SocketException e) {
                    System.out.println("[SERVIDOR]Ocorreu um erro ao nível do socket UDP:\n\t" + e);
                } catch (SocketTimeoutException e) {

                } catch (IOException e) {
                    System.out.println("[SERVIDOR]Ocorreu um erro no acesso ao socket:\n\t" + e);
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
    
    public void enviaListaFicheiros(Socket socket) {
        ObjectOutputStream oos = null;
        try {
            actualizaListaFicheiros();
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(listaFicheiros);
            oos.flush();
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void arrancaThreads()
    {
        if (isPrimario()) {
            try {
                rmiServidor = new RMIServidor(this);
            } catch (RemoteException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            String objectUrl = "rmi://127.0.0.1/RemoteTime"; //rmiregistry on localhost
            System.out.println("[Secundario] Endereço:"+primarioSocketTCP.getInetAddress().getHostAddress());
            objectUrl = "rmi://" + primarioSocketTCP.getInetAddress().getHostAddress() + "/RMITrabalho";
            RMIServidorInterface rmiServidor;
            try {
                rmiServidor = (RMIServidorInterface) Naming.lookup(objectUrl);
                RMIInfo info=new RMIInfo(this.isPrimario(),this.getEndereçoLocal(),this.getTcpPort(),this.getListaFicheiros(),this.getNumeroClientes());
                rmiServidor.changeData(info);
            } catch (NotBoundException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }          
        }
        
        if(!isPrimario()){           
            try {
                Pedido pedido=new Pedido("",4);
                ObjectOutputStream oos=new ObjectOutputStream(primarioSocketTCP.getOutputStream());
                oos.writeObject(pedido);
                oos.flush();
                recebeListaFicheiros();
                segundoCommitTCP = new SegundoCommitTCP(this);
                segundoCommitTCP.start();
            } catch (IOException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        
        try {
            heartRECV=new HeartbeatsRecebe(this);
            heartRECV.start();
            heartENVIA=new HeartbeatsEnvia(this);
            heartENVIA.start();
            
            trataCliente=new TrataCliente(this);
            trataCliente.start();
            trataSecundario=new TrataSecundario(this);
            trataSecundario.start();
        } catch (SocketException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addEscutaCliente(Socket socket) {
        socketsClientes.add(socket);
        RecebePedidoCliente thread = new RecebePedidoCliente(this,socket);
        thread.start();
        threadsPedidoCliente.add(thread);
    }
    
    public void addEscutaSecundario(Socket socket) {
        socketsSecundarios.add(socket);
        RecebePedidoSecundario thread = new RecebePedidoSecundario(this,socket);
        thread.start();
        threadsPedidoSecundario.add(thread);
        System.out.println("[SERVIDOR] Numero de secundarios:"+threadsPedidoSecundario.size());
    }
    
    public void removeEscutaSocket(Socket socket){
        socketsClientes.remove(socket);
        socketsSecundarios.remove(socket);
    }
    
    public void arrancaThreadEnviaFicheiro(Socket socket,Pedido pedido){
        EnviaFicheiro enviaFicheiro=new EnviaFicheiro(this,socket,pedido.getNomeFicheiro());
        enviaFicheiro.start();
    }
    
    public RecebeFicheiro arrancaThreadRecebeFicheiro(Socket pedidosSocketTCP,Pedido pedido){
        RecebeFicheiro recebeFicheiro=new RecebeFicheiro(this,pedidosSocketTCP,pedido);
        recebeFicheiro.start();
        return recebeFicheiro;
    }
    
    public EliminarFicheiro arrancaThreadEliminaFicheiro(Pedido pedido){
        EliminarFicheiro eliminarFicheiro=new EliminarFicheiro(this,pedido);
        eliminarFicheiro.start();
        return eliminarFicheiro;
    }
    
    public void recomeça(){       
        
        try {
            heartENVIA.termina();
            heartENVIA.join();
            
            trataCliente.termina();
            trataCliente.join();
            
            if(!isPrimario()){
                segundoCommitTCP.termina();
                segundoCommitTCP.join();
            }
            
            trataSecundario.termina();
            trataSecundario.join();
            
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
    
    public ServerSocket getServerSocketCliente(){
        return serverSocketCliente;
    }
    
    public ServerSocket getServerSocketSecundario() {
        return serverSocketSecundario;
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
    
    public ArrayList<Socket> getSocketsClientes(){
        return socketsClientes;
    }
    
    public ArrayList<Socket> getSocketSecundarios(){
        return socketsSecundarios;
    }
    
    public ArrayList<RecebePedidoSecundario> getArrayPedidoSecundario(){
        return threadsPedidoSecundario;            
    }
    
    public InetAddress getEndereçoLocal(){
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public int getNumeroClientes(){
        return socketsClientes.size();
    }
}
