/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
    public static final int MAX_SIZE=256;
    public String diretoria=null;
    public InetAddress group;
    public int port;
    public int contador=0;
    public byte []file=null;
    public FileInputStream recvFile=null;
    public FileOutputStream sendFile=null;
    public ObjectOutputStream sendobject=null;
    public ObjectInputStream reciveobject=null;
    
    //threads
    public Runnable HeartbeatsEnvia=null;
    public Runnable HeartbeatsRecebe=null;
    public Runnable TrataTCP=null;
    
    //UDP
    protected MulticastSocket socketUDP=null;
    private DatagramPacket SendpacketUDP=null;
    private DatagramPacket RecvpacketUDP=null;
    private DatagramSocket SocketComDiretoria=null;
    //TCP
    private ServerSocket socketTCP=null;
    private Socket SocketComPrimario=null;
    private Socket SocketComSecundario=null;
    
    private boolean debug,Primario,Linked,Actualizado;
    
    public Servidor(String diretoria,int Porto) throws UnknownHostException, IOException, InterruptedException 
    {      
        port=Porto;
        this.diretoria=diretoria;
        
        //UDP
        group=InetAddress.getByName("225.15.15.15");
        SendpacketUDP=new DatagramPacket(new byte[MAX_SIZE],MAX_SIZE,group,port);
        RecvpacketUDP=new DatagramPacket(new byte[MAX_SIZE],MAX_SIZE,group,port);
        SocketComDiretoria=new DatagramSocket();
        socketUDP = new MulticastSocket(port);
        socketUDP.joinGroup(group);
        
        //ainda nao utilizado
        file=new byte[MAX_SIZE];      
        
        //boolean
        Primario=false;
        Linked=false;
        Actualizado=false;
        debug=true;
        
        /*ByteArrayOutputStream byteout = new ByteArrayOutputStream(MAX_SIZE);
        ObjectOutputStream send = new ObjectOutputStream(byteout);
        send.writeObject(Primario);
        SocketComDiretoria=new DatagramSocket();
        packetUDP.setData(byteout.toByteArray());
        packetUDP.setLength(byteout.size());
        SocketComDiretoria.send(packetUDP);*/
        
        System.out.println("Servidor a correr....");
        
        
        
        //socketUDP.send(packetUDP);
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
                System.out.println("Thread HearbeatEnvia a correr.....");
                do {
                    try {
                        if(Linked==true || Primario==true)
                        {
                            ByteArrayOutputStream byteout = new ByteArrayOutputStream(MAX_SIZE);
                            ObjectOutputStream send = new ObjectOutputStream(byteout);
                            send.writeObject(Primario);
                            SendpacketUDP.setData(byteout.toByteArray());
                            SendpacketUDP.setLength(byteout.size());
                            //SocketComDiretoria.send(packetUDP); //teste
                            socketUDP.send(SendpacketUDP);
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
                System.out.println("Acabou HeartbeatsEnvia...");
            }
        };
        
        HeartbeatsRecebe = new Runnable(){    //esta aqui a dar erro

            @Override
            public void run() {
                System.out.println("Thread HeartbeatsRecebe a correr...");
                do{                  
                    try {
                        if (Linked == false && Primario == false) {
                            if (contador == 3) {
                                Primario = true;
                            } else {
                                ObjectInputStream recv = null;
                                socketUDP.setSoTimeout(5000);
                                socketUDP.receive(RecvpacketUDP);
                                
                                recv = new ObjectInputStream(new ByteArrayInputStream(RecvpacketUDP.getData()));
                                Object msg = recv.readObject();
                                if (msg instanceof Boolean) {
                                    if (true == (Boolean) msg && Primario==false) {
                                        Linked = true;                                       
                                        socketTCP = new ServerSocket(RecvpacketUDP.getPort(), 0, RecvpacketUDP.getAddress());
                                    }//falta condicao para ver se existir mais do que 1 primario entao escolhe se o que tiver menor ip
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("O porto de escuta deve ser um inteiro positivo.");
                    } catch (SocketException e) {
                        System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t" + e);
                    }   catch (SocketTimeoutException e) {
                    } catch (IOException e) {
                        System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        if(Linked==false && Primario==false)
                            contador++;
                        else{
                            contador=0;
                        }
                    }
                }while(debug);
                System.out.println("Acabou HearbeatsRecebe...");
            }         
        };
        
        TrataTCP = new Runnable() {  //tem que se por estas coisas em Slaves

            @Override
            public void run() {
                System.out.println("Thread TrataTCP a correr...");
                do {
                    try {
                        if (Linked == true || Primario==true) {
                            if (Primario == false && Actualizado == false) {
                                OutputStream oss=SocketComPrimario.getOutputStream();
                                sendobject=new ObjectOutputStream(oss);
                                sendobject.writeBoolean(Actualizado);
                                sendobject.close();
                                int tamanho;
                                byte[] bytes = new byte[MAX_SIZE];
                                InputStream in = null;
                                OutputStream ou = null;
                                in = SocketComSecundario.getInputStream();
                                ou = new FileOutputStream(diretoria);
                                while ((tamanho = in.read(bytes)) > 0) {
                                    ou.write(bytes, 0, tamanho);
                                }
                                ou.close();
                                in.close();
                                Actualizado = true;
                            } else {
                                if (Primario == true) {
                                    SocketComSecundario = socketTCP.accept();
                                    InputStream iss = SocketComSecundario.getInputStream();
                                    reciveobject = new ObjectInputStream(iss);
                                    Object msgrecebida = reciveobject.readObject();
                                    if (msgrecebida instanceof Boolean) {
                                        if ((Boolean) msgrecebida == false) {
                                            File folder = new File(diretoria);
                                            File[] ListadosFicheiros = folder.listFiles();
                                            OutputStream ou = SocketComSecundario.getOutputStream();
                                            InputStream in = null;
                                            for (int i = 0; i < ListadosFicheiros.length; i++) {
                                                long length = ListadosFicheiros[i].length();
                                                byte[] bytes = new byte[MAX_SIZE];
                                                in = new FileInputStream(ListadosFicheiros[i]);
                                                int tamanho;
                                                while ((tamanho = in.read(bytes)) > 0) {
                                                    ou.write(bytes, 0, tamanho);
                                                }
                                            }
                                            ou.close();
                                            in.close();
                                        }
                                    }else{
                                        if(msgrecebida instanceof Cliente)     //nao esquecer mudar para socketcomcliente
                                        {
                                            Cliente c=(Cliente)msgrecebida;
                                            if(c.download==true)
                                            {
                                                File folder = new File(diretoria);
                                                File[] ListadosFicheiros = folder.listFiles();
                                                OutputStream ou = SocketComSecundario.getOutputStream();
                                                InputStream in = null;
                                                for (int i = 0; i < ListadosFicheiros.length; i++) {
                                                    long length = ListadosFicheiros[i].length();
                                                    byte[] bytes = new byte[MAX_SIZE];
                                                    in = new FileInputStream(ListadosFicheiros[i]);
                                                    int tamanho;
                                                    while ((tamanho = in.read(bytes)) > 0) {
                                                        ou.write(bytes, 0, tamanho);
                                                    }
                                                }
                                                ou.close();
                                                in.close();
                                            }
                                            if(c.Upload==true)
                                            {
                                                int tamanho;
                                                byte[] bytes = new byte[MAX_SIZE];
                                                InputStream in = null;
                                                OutputStream ou = null;
                                                in = SocketComSecundario.getInputStream();
                                                ou = new FileOutputStream(diretoria);
                                                while ((tamanho = in.read(bytes)) > 0) {
                                                    ou.write(bytes, 0, tamanho);
                                                }
                                                ou.close();
                                                in.close();
                                            }
                                            if(c.Ver==true)  //acho que estou a mandar todos os ficheiros num object e nao o nome deles-boa ideia para os ocdigos anteriores se funcionar
                                            {
                                                int tamanho;
                                                File folder = new File(diretoria);
                                                File[] ListadosFicheiros = folder.listFiles();
                                                OutputStream oss=SocketComSecundario.getOutputStream();
                                                ObjectOutputStream sendcliente=new ObjectOutputStream(oss);
                                                sendcliente.writeObject(ListadosFicheiros);
                                                sendcliente.close();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } while (debug);
                System.out.println("Acabou TrataTCP...");
            }

        };
        
    }
    
    public void começa() throws IOException,InterruptedException                      //nao sei usar o daemon, e nao sei como se fazia para istoo acabar so quando as threads acabarem
    {     
        
        Thread t1=new Thread(HeartbeatsEnvia);        
        Thread t2=new Thread(HeartbeatsRecebe);
        Thread t3=new Thread(TrataTCP);
        System.out.println("Threads Criadas....");
        t1.start();
        t2.start();
        t3.start();
        while(t1.isAlive() || t2.isAlive() || t3.isAlive())
        {
            
        }
        
    }
    
    public void closeSocket() throws IOException
    {
        System.out.println("Acabou!!!");
        if(socketUDP!=null)
            socketUDP.close();
        if(socketTCP!=null)
            socketTCP.close();
    }
}
