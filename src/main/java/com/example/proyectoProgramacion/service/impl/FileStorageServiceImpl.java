package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.service.interfaces.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/uploads/}")
    private String baseUrl;  // Usando puerto 8080

    @Override
    public String guardarArchivo(MultipartFile archivo, String nombreBase) throws IOException {
        // Crear directorio si no existe
        Path directorioPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(directorioPath);

        // Obtener extensión del archivo
        String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename());
        String extension = "";
        if (nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }

        // Crear nombre único para el archivo
        String nombreArchivo = nombreBase + "_" + UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path rutaDestino = directorioPath.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

        return nombreArchivo;
    }

    @Override
    public void eliminarArchivo(String rutaArchivo) throws IOException {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return;
        }

        // Extraer nombre del archivo de la URL o ruta
        String nombreArchivo = rutaArchivo;
        if (rutaArchivo.startsWith(baseUrl)) {
            nombreArchivo = rutaArchivo.substring(baseUrl.length());
        }

        Path archivoPath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(nombreArchivo);
        Files.deleteIfExists(archivoPath);
    }

    @Override
    public String obtenerUrlPublica(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return null;
        }

        // Si ya es una URL completa, devolverla
        if (rutaArchivo.startsWith("http://") || rutaArchivo.startsWith("https://")) {
            return rutaArchivo;
        }

        // Construir URL pública
        return baseUrl + rutaArchivo;
    }
}

