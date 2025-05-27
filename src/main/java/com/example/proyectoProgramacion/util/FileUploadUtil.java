package com.example.proyectoProgramacion.util;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Utilidad para manejar la carga y eliminación de archivos en el sistema.
 * Proporciona métodos para guardar archivos con nombres únicos y eliminarlos.
 */
@Component
@Tag(name = "Utilidades", description = "Utilidades generales de la aplicación")
public class FileUploadUtil {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Guarda un archivo en el sistema de archivos con un nombre único generado.
     * 
     * @param file Archivo a guardar
     * @param baseFileName Nombre base para el archivo (se le añadirá un UUID único)
     * @return URL relativa del archivo guardado (ej: "/uploads/nombre_archivo.jpg")
     * @throws IOException Si hay problemas al guardar el archivo
     * @throws IllegalArgumentException Si el archivo está vacío o el nombre base es inválido
     */
    @Operation(
        summary = "Guardar archivo",
        description = "Guarda un archivo en el sistema con un nombre único generado"
    )
    @Parameter(name = "file", description = "Archivo a guardar", required = true)
    @Parameter(name = "baseFileName", description = "Nombre base para el archivo (sin extensión)", required = true)
    public String saveFile(MultipartFile file, String baseFileName) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("No se puede guardar un archivo vacío");
        }

        // Crear directorio de uploads si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único para el archivo
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String uniqueFileName = baseFileName + "_" + UUID.randomUUID().toString() + fileExtension;

        // Guardar archivo
        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            // Devolver URL relativa
            return "/uploads/" + uniqueFileName;
        }
    }

    /**
     * Elimina un archivo del sistema de archivos.
     * 
     * @param fileUrl URL relativa del archivo a eliminar (ej: "/uploads/nombre_archivo.jpg")
     * @return true si el archivo se eliminó correctamente o no existía, false si hubo un error
     */
    @Operation(
        summary = "Eliminar archivo",
        description = "Elimina un archivo del sistema de archivos"
    )
    @Parameter(name = "fileUrl", description = "URL relativa del archivo a eliminar", required = true)
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            // Extraer nombre de archivo de la URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir).resolve(fileName);

            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Obtiene la extensión de un archivo.
     * 
     * @param fileName Nombre completo del archivo o su ruta
     * @return Extensión del archivo con el punto incluido (ej: ".jpg"), o cadena vacía si no tiene extensión
     */
    @Operation(
        summary = "Obtener extensión de archivo",
        description = "Extrae la extensión de un nombre de archivo"
    )
    @Parameter(name = "fileName", description = "Nombre del archivo o ruta completa", required = true)
    @Schema(description = "Extensión del archivo con punto (ej: .jpg)", implementation = String.class)
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}


