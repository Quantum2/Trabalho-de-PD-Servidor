/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class HeartbeatsRecebe extends Thread{
    
    int contador=0;
    private boolean Primario=false,Linked=false;
    protected MulticastSocket socket = null;
    protected DatagramPacket packet=null;
    protected boolean running = false;
    public InetAddress IpPrimario=null;
    public int PortoPrimario=0;
    
    public HeartbeatsRecebe(MulticastSocket socketUDP,DatagramPacket RecvPacket) throws SocketException
    {
        this.socket=socketUDP;
        socket.setBroadcast(true);
        this.packet=RecvPacket;
        running=true;
    }
    
    public Boolean getLinked()
    {
        return Linked;
    }
    
    public Boolean getPrimario()
    {
        return Primario;
    }
    
    public InetAddress getIpPrimario()
    {
        return IpPrimario;
    }
    
    public int getPortoPrimario()
    {
        return PortoPrimario;
    }
    
    public void termina() {
        running = false;
    }

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
                                socket.setSoTimeout(5000);
                                socket.receive(packet);               
                                recv = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));
                                Object msg = recv.readObject();
                                if (msg instanceof Boolean) {
                                    if (true == (Boolean) msg && Primario==false) {
                                        Linked = true;
                                        IpPrimario=packet.getAddress();
                                        PortoPrimario=packet.getPort();
                                        termina();
                                    }
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
                }while(running);
                System.out.println("Acabou HearbeatsRecebe...");
    }
}