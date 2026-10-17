package pe.cibertec.rudestyle.service;

public class StockInsuficienteException extends Exception {
    public StockInsuficienteException(String producto) {
        super("Stock insuficiente para: " + producto);
    }
}