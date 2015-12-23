/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Carlos Oliveira
 */
public class ListaFicheiros implements Serializable{
    ArrayList<Ficheiro> listaFicheiros = new ArrayList<>();
    
    public ListaFicheiros(){
        
    }
    
    public boolean hasFicheiro(String nomeFicheiro) {
        for (Ficheiro ficheiro : listaFicheiros) {
            if (ficheiro.getNome().equals(nomeFicheiro)){
                return true;
            }
        }
        return false;
    }
    
    public void addFicheiro(Ficheiro ficheiro) {
        listaFicheiros.add(ficheiro);
    }
    
    public Ficheiro getFicheiro(int n) {
        return listaFicheiros.get(n);
    }
    
    public Ficheiro removeFicheiro(int n) {
        return listaFicheiros.remove(n);
    }
    
    public void reset() {
        listaFicheiros.clear();
    }
    
    public int getSize() {
        return listaFicheiros.size();
    }
    
    public ArrayList<Ficheiro> getArrayListFicheiro() {
        return listaFicheiros;
    }
}
