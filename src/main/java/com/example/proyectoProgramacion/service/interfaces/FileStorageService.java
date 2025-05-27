package com.example.proyectoProgramacion.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Servicio para el almacenamiento de archivos
 */
public interface FileStorageService {

    /**
     * Guarda un archivo en el sistema de almacenamiento
     * @param archivo archivo a guardar
     * @param nombreBase nombre base para el archivo (sin extensión)
     * @return URL o ruta del archivo guardado
     * @throws IOException si ocurre un error al guardar el archivo
     */
    String guardarArchivo(MultipartFile archivo, String nombreBase) throws IOException;

    /**
     * Elimina un archivo del sistema de almacenamiento
     * @param rutaArchivo ruta o URL del archivo a eliminar
     * @throws IOException si ocurre un error al eliminar el archivo
     */
    void eliminarArchivo(String rutaArchivo) throws IOException;

    /**
     * Obtiene la URL pública de un archivo
     * @param rutaArchivo ruta del archivo
     * @return URL pública del archivo
     */
    String obtenerUrlPublica(String rutaArchivo);
}

