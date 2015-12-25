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
public class RMIInfo implements Serializable{
    
    static final long serialVersionUID = 1L;
    
    boolean primario;
    InetAddress endereçoIP;
    int portoTCP;
    ListaFicheiros listaFicheiros;
    int numeroClientes;
    
    public RMIInfo(boolean primario,InetAddress endereçoIP,int portoTCP,ListaFicheiros listaFicheiros,int numeroClientes){
        this.primario=primario;
        this.endereçoIP=endereçoIP;
        this.portoTCP=portoTCP;
        this.listaFicheiros=listaFicheiros;
        this.numeroClientes=numeroClientes;
    }
    
    public boolean getIsPrimario(){
        return primario;
    }
    
    public InetAddress getEndereçoIP(){
        return endereçoIP;
    }
    
    public int getPortoTCP(){
        return portoTCP;
    }
    
    public ListaFicheiros getListaFicheiros(){
        return listaFicheiros;
    }
    
    public int getNumeroClientes(){
        return numeroClientes;
    }
}
