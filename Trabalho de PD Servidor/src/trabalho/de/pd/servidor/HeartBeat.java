/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.Serializable;

/**
 *
 * @author ASUS
 */
public class HeartBeat implements Serializable{
    int tcpPort;
    boolean primario;
    
    public HeartBeat(int tcpPort,boolean primario){
        this.tcpPort=tcpPort;
        this.primario=primario;
    }
    
    public int getTcpPort(){
        return tcpPort;
    }
    
    public boolean getPrimario(){
        return primario;
    }
}
