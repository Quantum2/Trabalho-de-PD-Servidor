/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author ASUS
 */
public class HeartBeat implements Serializable{
    private static final long serialVersionUID = 1L;
    int tcpPort;
    InetAddress endereço=null;
    boolean primario;
    long tStart;
    
    public HeartBeat(int tcpPort,boolean primario,InetAddress endereço){
        this.tcpPort=tcpPort;
        this.primario=primario;
        this.endereço=endereço;
    }
    
    public int getTcpPort(){
        return tcpPort;
    }
    
    public boolean getPrimario(){
        return primario;
    }
    
    public InetAddress getEndereço(){
        return endereço;
    }
    
    public long getTStart(){
        return tStart;
    }
    
    public void setTStart(long tStart){
        this.tStart=tStart;
    }
}
