CREATE DATABASE sunat_db;

-- Crear la tabla para almacenar los mensajes de SUNAT
CREATE TABLE IF NOT EXISTS T_MENSAJE_SUNAT (
                                               NU_CODIGO_MENSAJE BIGINT PRIMARY KEY,
                                               NU_PAGINA INTEGER,
                                               NU_ESTADO INTEGER,
                                               NU_DESTACADO INTEGER,
                                               NU_URGENTE INTEGER,
                                               DT_FECHA_VIGENCIA TIMESTAMP,
                                               NU_TIPO_MENSAJE INTEGER,
                                               VC_ASUNTO VARCHAR(255),
    VC_FECHA_ENVIO VARCHAR(20),
    VC_FECHA_PUBLICA VARCHAR(30),
    VC_USUARIO_EMISOR VARCHAR(50),
    NU_INDICADOR_TEXTO INTEGER,
    NU_TIPO_GENERADOR INTEGER,
    VC_CODIGO_DEPENDENCIA VARCHAR(50),
    NU_AVISO INTEGER,
    NU_CANTIDAD_ARCHIVOS INTEGER,
    VC_CODIGO_ETIQUETA VARCHAR(20),
    NU_MENSAJE INTEGER,
    VC_CODIGO_CARPETA VARCHAR(50),
    VC_NUMERO_RUC VARCHAR(20)
    );

-- Índice para optimizar la búsqueda por código de mensaje
CREATE INDEX IF NOT EXISTS idx_codigo_mensaje ON T_MENSAJE_SUNAT(NU_CODIGO_MENSAJE);