/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author ASUS
 */
public class RMIServidor extends UnicastRemoteObject implements RMIServidorInterface{
    
    Servidor servidor;
    RMIInfo rmiInfo=null;
    List<TrabalhoPDRMIMonotorizacaoInterface> observers;
    
    public RMIInfo getInfo() throws RemoteException
    {            
        if(observers.size()>0)
            notifyObservers("Solicitado servidorRMI permitido: Primario:" +rmiInfo.getIsPrimario()+" Endereço:"+rmiInfo.getEndereçoIP()+" Porto:"+rmiInfo.getPortoTCP()+" Numero Clientes:"+rmiInfo.getNumeroClientes());  
        return rmiInfo;
    }
    
    public void changeData(RMIInfo info){
        rmiInfo=info;
    }
    
    public void addObserver(TrabalhoPDRMIMonotorizacaoInterface observer) {
        if(!observers.contains(observer)){
            observers.add(observer);
            System.out.println("+ um observador.");
        }
    }

    public void removeObserver(TrabalhoPDRMIMonotorizacaoInterface observer) {
        if(observers.remove(observer))
            System.out.println("- um observador.");
    }
    
    public synchronized void notifyObservers(String msg)
    {
        int i;
        
        for(i=0; i < observers.size(); i++){
            try{       
                observers.get(i).notifyNewOperationConcluded(msg);
            }catch(RemoteException e){
                observers.remove(i--);
                System.out.println("- um observador (observador inacessivel).");
            }
        }
    }
    
    public RMIServidor(Servidor servidor) throws RemoteException {
        this.servidor=servidor;
        observers=new ArrayList<>();
        rmiInfo=new RMIInfo(servidor.isPrimario(),servidor.getEndereçoLocal(),servidor.getTcpPort(),servidor.getListaFicheiros(),servidor.getNumeroClientes());
        
        try{
            
            Registry r;
            
            try{
                
                System.out.println("Tentativa de lancamento do registry no porto " + 
                                    Registry.REGISTRY_PORT + "...");
                
                r = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
                
                System.out.println("Registry lancado!");
                                
            }catch(RemoteException e){
                System.out.println("Registry provavelmente ja' em execucao!");
                r = LocateRegistry.getRegistry();          
            }
            
            // Cria e lanca o servico,
            
            System.out.println("Servico RemoteTime criado e em execucao ("+this.getRef().remoteToString()+"...");
            
            // Regista o servico para que os clientes possam encontra'-lo, ou seja,
            // obter a sua referencia remota (endereco IP, porto de escuta, etc.).
             
            r.bind("RMITrabalho", this);
            System.out.println("Servico RemoteTime registado no registry...");
            
        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e);
            System.exit(1);
        }catch(Exception e){
            System.out.println("Erro - " + e);
            System.exit(1);
        }                
    }  

    
}
