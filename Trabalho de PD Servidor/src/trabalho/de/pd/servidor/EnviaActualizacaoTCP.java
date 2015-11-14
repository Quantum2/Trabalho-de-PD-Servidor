/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.net.Socket;

/**
 *
 * @author ASUS
 */
public class EnviaActualizacaoTCP extends Thread{   //se recebe um boolean do tipo falso, comunica com o secundario e manda actualizacao.
                                                    //se for verdadeiro, comunica com um cliente e responde.
    private Socket EnviaSocketTCP=null;
    
    public EnviaActualizacaoTCP()
    {
        
    }
}
