/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author ASUS
 */
public interface RMIServidorInterface extends Remote{
    
    public RMIInfo getInfo() throws RemoteException;  
    
    public void changeData(RMIInfo info) throws RemoteException;
    
    public void addObserver(TrabalhoPDRMIMonotorizacaoInterface observer) throws java.rmi.RemoteException;
    public void removeObserver(TrabalhoPDRMIMonotorizacaoInterface observer) throws java.rmi.RemoteException; 
}
