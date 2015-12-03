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
public class Pedido {
    
    //ter estas variáveis no servidor e fazer if == DOWNLOAD ou 
    //switch(int tipoPedido) case: DOWNLOAD e assim fora para os outros
    //de modo a chamar o método correspondente a cada ação
    public final static int DOWNLOAD = 1;
    public final static int UPLOAD = 2;
    public final static int ELIMINAR = 3;
    
    //inclui .extensão
    private String nomeFicheiro;
    private int tipoPedido;
    
    public Pedido (String nomeFicheiro, int tipoPedido) {
        this.nomeFicheiro=nomeFicheiro;
        this.tipoPedido=tipoPedido;
    }
    
    public String getNomeFicheiro() {
        return nomeFicheiro;
    }
    
    public int getTipoPedido() {
        return tipoPedido;
    }
}