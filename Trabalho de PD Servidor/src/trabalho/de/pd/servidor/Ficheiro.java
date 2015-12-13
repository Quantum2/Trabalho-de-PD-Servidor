/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

/**
 *
 * @author Carlos Oliveira
 */
public class Ficheiro {
    String nome;
    long bytes;
    //Poderá ser útil, mas usar sempre o nome do ficheiro com a extensão será melhor
    //String extensao;
    
    Ficheiro(String nome,long bytes/*,String extensao*/) {
        this.nome=nome;
        this.bytes=bytes;
        //this.extensao=extensao;
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
