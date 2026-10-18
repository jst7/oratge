package com.oratge.oratgeback.domain.exception;

public class MunicipioNoEncontradoException extends RuntimeException {

    public MunicipioNoEncontradoException(String codigoMunicipio) {
        super("No se ha encontrado el municipio con código: " + codigoMunicipio);
    }
}
