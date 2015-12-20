/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.Serializable;

/**
 *
 * @author Carlos Oliveira
 */
public class Ficheiro implements Serializable {
    public final static int UPLOAD = 2;
    public final static int ELIMINAR = 3;
    
    String nome;
    int pedido;
    long bytes;
    //Poderá ser útil, mas usar sempre o nome do ficheiro com a extensão será melhor
    //String extensao;
    
    public Ficheiro(String nome,long bytes/*,String extensao*/) {
        this.nome=nome;
        this.bytes=bytes;
        //this.extensao=extensao;
    }
    
    public int getPedido(){
        return pedido;
    }
    
    public String getNome() {
        return nome;
    }
    
    public long getBytes() {
        return bytes;
    }
    
    /*
    public String getExtensao() {
        return extensao;
    }
    */
    @Override
    public String toString() {
        return nome+"\t"+bytes+" bytes";
    }
    
}
