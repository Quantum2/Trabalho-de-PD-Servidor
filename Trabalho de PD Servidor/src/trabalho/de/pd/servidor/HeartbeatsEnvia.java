/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class HeartbeatsEnvia extends Thread {

    Servidor servidor=null;
    private DatagramPacket packet = null;
    protected boolean running = false;
    public HeartbeatsRecebe heartRECV=null;

    public HeartbeatsEnvia(Servidor servidor) throws SocketException {
        this.servidor=servidor;
        running = true;
    }

    public void termina() {
        running = false;
    }

    @Override
    public void run() {
        System.out.println("Thread HearbeatEnvia a correr.....");
        do{
            try {
                if (servidor.isPrimario()) {
                    String objectUrl = "rmi://127.0.0.1/RemoteTime"; //rmiregistry on localhost
                    System.out.println("[Secundario] Endereço:" + servidor.getEndereçoLocal().getHostAddress());
                    objectUrl = "rmi://" + servidor.getEndereçoLocal().getHostAddress() + "/RMITrabalho";
                    RMIServidorInterface rmiServidor;
                    try {
                        rmiServidor = (RMIServidorInterface) Naming.lookup(objectUrl);
                        RMIInfo info = new RMIInfo(servidor.isPrimario(), servidor.getEndereçoLocal(), servidor.getTcpPort(), servidor.getListaFicheiros(), servidor.getNumeroClientes());
                        rmiServidor.changeData(info);
                    } catch (NotBoundException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RemoteException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    String objectUrl = "rmi://127.0.0.1/RemoteTime"; //rmiregistry on localhost
                    System.out.println("[Secundario] Endereço:" + servidor.getPrimarioSocketTCP().getInetAddress().getHostAddress());
                    objectUrl = "rmi://" + servidor.getPrimarioSocketTCP().getInetAddress().getHostAddress() + "/RMITrabalho";
                    RMIServidorInterface rmiServidor;
                    try {
                        rmiServidor = (RMIServidorInterface) Naming.lookup(objectUrl);
                        RMIInfo info = new RMIInfo(servidor.isPrimario(), servidor.getEndereçoLocal(), servidor.getTcpPort(), servidor.getListaFicheiros(), servidor.getNumeroClientes());
                        rmiServidor.changeData(info);
                    } catch (NotBoundException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RemoteException ex) {
                        Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                HeartBeat msg=new HeartBeat(servidor.getTcpPort(),servidor.isPrimario(),InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));
                ByteArrayOutputStream byteout = new ByteArrayOutputStream();
                ObjectOutputStream send = new ObjectOutputStream(byteout);
                send.writeObject(msg);
                send.flush();
                
                packet=new DatagramPacket(byteout.toByteArray(),byteout.size(),servidor.getGroup(),7000);
                servidor.getMulticastSocket().send(packet); //teste
                sleep(5000);
            } catch (NumberFormatException e) {
                System.out.println("O porto de escuta deve ser um inteiro positivo.");
            } catch (SocketException e) {
                System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t" + e);
            } catch (IOException e) {
                System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
            } catch (InterruptedException ex) {
                Logger.getLogger(HeartbeatsEnvia.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

            }
        }while(running);
        System.out.println("Acabou HeartbeatsEnvia...");
    }
    
    

}
