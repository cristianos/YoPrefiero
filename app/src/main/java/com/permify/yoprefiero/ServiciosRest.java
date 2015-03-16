package com.permify.yoprefiero;

public class ServiciosRest {
    private String URL;

    public ServiciosRest(){
        String URL_BASE = "http://api.yoprefiero.cl/";
        String URL_VERSION = "api/v1/";

        URL = URL_BASE + URL_VERSION;
    }

    //    POST api/v1/usuarios
    public String postUsuarios(){
        return URL + "usuarios";
    }

    //    GET|HEAD api/v1/autorizador
    public String getAutorizador(String apiKey, int idUsuario){
        String RUTA_ARGS = String.format("autorizador?ak=%1$s&id=%2$d", apiKey, idUsuario);
        return URL + RUTA_ARGS;
    }

    //    POST api/v1/autorizador
    public String postAutorizador(){
        return URL + "autorizador";
    }


    /**
     * Requieren filtro api_key como argumento (solo en request GET)
     * POST y PUT se pasan como argumentos en el mismo request de volley*/

    //    GET|HEAD api/v1/contenidos/{contenidos}
    public String getContenidos(int idContenido, String apiKey){
        String RUTA_ARGS = String.format("contenidos/%1$d?api_key=%2$s", idContenido, apiKey);
        return URL + RUTA_ARGS;
    }

    //    POST api/v1/eventos
    public String postEventos(){
        return URL + "eventos";
    }

    //    GET|HEAD api/v1/productos
    public String getProductos(String apiKey){
        String RUTA_ARGS = String.format("productos?api_key=%1$s", apiKey);
        return URL + RUTA_ARGS;
    }

    //    GET|HEAD api/v1/productos/{productos}/contenidos
    public String getProductosContenidos(int idProducto, String apiKey){
        String RUTA_ARGS = String.format("productos/%1$d/contenidos?api_key=%2$s", idProducto, apiKey);
        return URL + RUTA_ARGS;
    }

    public String getProductosContenidos(int idProducto, String apiKey, int pagina){
        String RUTA_ARGS = String.format("productos/%1$d/contenidos?page=%2$d&api_key=%3$s", idProducto, pagina, apiKey);
        return URL + RUTA_ARGS;
    }

    //    GET|HEAD api/v1/usuarios/{usuarios}
    public String getUsuarios(int idUsuario, String apiKey){
        String RUTA_ARGS = String.format("usuarios/%1$d?api_key=%2$s", idUsuario, apiKey);
        return URL + RUTA_ARGS;
    }

    //    PUT api/v1/usuarios/{usuarios}
    //    PATCH api/v1/usuarios/{usuarios}
    public String putUsuarios(int idUsuario){
        String RUTA_ARGS = String.format("usuarios/%1$d", idUsuario);
        return URL + RUTA_ARGS;
    }

    //    GET|HEAD api/v1/usuarios/{usuarios}/productos
    public String getUsuariosProductos(int idUsuario, String apiKey){
        String RUTA_ARGS = String.format("usuarios/%1$d/productos?api_key=%2$s", idUsuario, apiKey);
        return URL + RUTA_ARGS;
    }

    //    POST api/v1/usuarios/{usuarios}/productos
    public String postUsuariosProductos(int idUsuario){
        String RUTA_ARGS = String.format("usuarios/%1$d/productos", idUsuario);
        return URL + RUTA_ARGS;
    }

    //    GET|HEAD api/v1/usuarios/{usuarios}/productos/{productos}
    public String getUsuariosProductos(int idUsuario, int idProducto, String apiKey){
        String RUTA_ARGS = String.format("usuarios/%1$d/productos/%2$d?api_key=%3$s", idUsuario, idProducto, apiKey);
        return URL + RUTA_ARGS;
    }

    //    PUT api/v1/usuarios/{usuarios}/productos/{productos}
    //    PATCH api/v1/usuarios/{usuarios}/productos/{productos}
    public String putUsuariosProductos(int idUsuario, int idProducto){
        String RUTA_ARGS = String.format("usuarios/%1$d/productos/%2$d", idUsuario, idProducto);
        return URL + RUTA_ARGS;
    }

    //    DELETE api/v1/usuarios/{usuarios}/productos/{productos}
    public String deleteUsuariosProductos(int idUsuario, int idProducto, String apiKey){
        String RUTA_ARGS = String.format("usuarios/%1$d/productos/%2$d?api_key=%3$s", idUsuario, idProducto, apiKey);
        return URL + RUTA_ARGS;
    }

    //Apunta a la misma funcion, pero con verbo GET
    public String deleteUsuariosProductos2(int idUsuario, int idProducto, String apiKey){
        String RUTA_ARGS = String.format("usuarios/%1$d/productos/%2$d/eliminar?api_key=%3$s", idUsuario, idProducto, apiKey);
        return URL + RUTA_ARGS;
    }

    //    GET|HEAD api/v1/usuarios/{usuarios}/perfil
    public String getUsuariosPerfil(int idUsuario, String apiKey){
        String RUTA_ARGS = String.format("usuarios/%1$d/perfil?api_key=%2$s", idUsuario, apiKey);
        return URL + RUTA_ARGS;
    }

    //    POST api/v1/usuarios/{usuarios}/perfil
    public String postUsuariosPerfil(int idUsuario){
        String RUTA_ARGS = String.format("usuarios/%1$d/perfil", idUsuario);
        return URL + RUTA_ARGS;
    }

    //    POST api/v1/usuarios/{usuarios}/perfil/imagen
    public String postUsuarioPerfilImagen(int idUsuario){
        String RUTA_ARGS = String.format("usuarios/%1$d/perfil/imagen", idUsuario);
        return URL + RUTA_ARGS;
    }
}
