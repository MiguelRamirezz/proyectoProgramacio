package com.example.proyectoProgramacion.util;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Clase de utilidad que contiene constantes utilizadas en toda la aplicación.
 * Incluye mensajes de error, códigos de estado y valores por defecto.
 * 
 * <p>Esta clase debe ser la fuente única de verdad para todos los mensajes
 * y valores constantes utilizados en la aplicación.</p>
 * 
 * <p>Al agregar nuevas constantes, asegúrate de:
 * <ul>
 *   <li>Agregarlas en la sección correspondiente</li>
 *   <li>Usar nombres descriptivos en MAYÚSCULAS_CON_GUION_BAJO</li>
 *   <li>Incluir el valor por defecto en el comentario si aplica</li>
 * </ul>
 * </p>
 */
@Schema(description = "Constantes de la aplicación")
public class AppConstants {

    // ========== MENSAJES DE ERROR PARA PRODUCTOS ==========
    /** Mensaje cuando un producto no se encuentra. */
    public static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado";
    
    /** Formato de mensaje cuando un producto no se encuentra. Uso: String.format(PRODUCTO_NO_ENCONTRADO_FORMAT, id) */
    public static final String PRODUCTO_NO_ENCONTRADO_FORMAT = "No se encontró el producto con ID: %d";
    
    public static final String NOMBRE_PRODUCTO_REQUERIDO = "El nombre del producto es obligatorio";
    public static final String PRECIO_PRODUCTO_INVALIDO = "El precio del producto debe ser mayor que cero";
    public static final String STOCK_PRODUCTO_INVALIDO = "El stock del producto no puede ser negativo";
    public static final String CATEGORIA_PRODUCTO_REQUERIDA = "La categoría del producto es obligatoria";

    // ========== MENSAJES DE ERROR PARA USUARIOS ==========
    /** Mensaje cuando un usuario no se encuentra. */
    public static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    
    /** Formato de mensaje cuando un usuario no se encuentra. Uso: String.format(USUARIO_NO_ENCONTRADO_FORMAT, username) */
    public static final String USUARIO_NO_ENCONTRADO_FORMAT = "No se encontró el usuario: %s";
    
    public static final String USUARIO_YA_EXISTE = "El nombre de usuario ya está en uso";
    public static final String EMAIL_YA_EXISTE = "El correo electrónico ya está en uso";
    public static final String CREDENCIALES_INVALIDAS = "Credenciales inválidas";

    // ========== MENSAJES DE ERROR PARA CARRITO ==========
    public static final String CANTIDAD_DEBE_SER_POSITIVA = "La cantidad debe ser mayor que cero";
    public static final String STOCK_INSUFICIENTE = "Stock insuficiente. Stock disponible: ";
    public static final String ITEM_CARRITO_NO_ENCONTRADO = "Item del carrito no encontrado";
    public static final String ITEM_NO_PERTENECE_USUARIO = "El item no pertenece al usuario actual";

    // ========== MENSAJES DE ERROR PARA ÓRDENES ==========
    public static final String ORDEN_NO_ENCONTRADA = "Orden no encontrada";
    public static final String CARRITO_VACIO = "El carrito está vacío";
    public static final String ORDEN_NO_PERTENECE_USUARIO = "La orden no pertenece al usuario actual";
    public static final String STOCK_INSUFICIENTE_PRODUCTO = "Stock insuficiente para el producto: ";
    public static final String ORDEN_NO_PENDIENTE = "La orden no está en estado pendiente";

    // ========== MENSAJES DE ERROR PARA PAGOS ==========
    public static final String PAGO_NO_ENCONTRADO = "Pago no encontrado";
    public static final String PAGO_EXITOSO = "Pago procesado correctamente";
    public static final String PAGO_FALLIDO = "Error al procesar el pago";

    // ========== CONSTANTES DE PAGINACIÓN ==========
    /** Número de página por defecto: 0 (primera página) */
    public static final String NUMERO_PAGINA_POR_DEFECTO = "0";
    
    /** Tamaño de página por defecto: 10 elementos */
    public static final String TAMAO_PAGINA_POR_DEFECTO = "10";
    
    /** Tamaño de página para elementos relacionados: 4 elementos */
    public static final int TAMANO_PAGINA_RELACIONADOS = 4;
    
    /** Tamaño de página para nuevos productos: 8 elementos */
    public static final int TAMANO_PAGINA_NUEVOS = 8;
    
    /** Campo por defecto para ordenar: "id" */
    public static final String ORDENAR_POR_DEFECTO = "id";
    
    /** Dirección de ordenación por defecto: "asc" (ascendente) */
    public static final String ORDENAR_DIRECCION_POR_DEFECTO = "asc";
    
    /** Ordenación por fecha de creación descendente */
    public static final String ORDENAR_POR_FECHA_CREACION = "fechaCreacion";
    public static final String ORDENAR_DIRECCION_DESCENDENTE = "desc";

    /**
     * Constructor privado para evitar la instanciación de esta clase de utilidad.
     * Lanza IllegalStateException si se intenta instanciar.
     * 
     * @throws IllegalStateException siempre que se intente instanciar esta clase
     */
    private AppConstants() {
        throw new IllegalStateException("Clase de utilidad - No se permite la instanciación");
    }
}


