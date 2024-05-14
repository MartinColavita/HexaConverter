package com.example.demohexa;


import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;


@SpringBootApplication
public class DemohexaApplication {

    private static final Logger logger = Logger.getLogger(DemohexaApplication.class.getName());

    @Autowired
    private Environment env;



    public static void main(String[] args) {
        SpringApplication.run(DemohexaApplication.class, args);

    }


    @PostConstruct
    public void init() {
        // Valores de application.properties usados en el mensaje
        String ip = env.getProperty("ip");
        String version = env.getProperty("version");
        String requestAuthentication = env.getProperty("requestAuthentication");
        String nasIP = env.getProperty("nasIP");
        String nasPort = env.getProperty("nasPuerto");

        // Parametros de entrada
        String id = "FBGRF8X";
        String pin = "1234";          // user
        String token = "987654321";  // password

        // Logs antes de la cadena Hexadecimal
        if (ip != null && version != null) {
            logger.info("============ HEXA CONVERTER ==============" );
            logger.info("------------------------------------------" );
            logger.info(ip + " Cargando Archivo de Configuracion");
            logger.info(ip + " Version " + version);
            logger.info("1 AM - Ingresando a validarUsuario - Id Tecnico: " + id);
            logger.info("1 AM - Conectando AuthManager - host:# nasIP: " + nasIP + " nasPuerto: " + nasPort);
            logger.info("1 AM - Stack Radius creado exitosamente");
            logger.info("                                 " );
        } else {
            logger.severe(" ERROR: No se puede cargar el archivo de configuracion .\\config_localhost.ini");
        }

        // Crea instancia de la clase ConverterHexa y llama al metodo completeMessage para generar la caden hexadecimal
        ConverterHexa converter = new ConverterHexa();
        try {
            String hexMessage = converter.completeMessage(id, pin, token,ip,version,requestAuthentication,nasIP,nasPort);

            logger.info("                                 " );
            logger.info("============HEXA CONVERTER INVERSE==============" );
            // Crea una instancia de la clase HexaConverterInverse y llama al método parseMessage para convertir el mensaje hexadecimal de vuelta a sus valores originales
            HexaConverterInverse inverseConverter = new HexaConverterInverse(converter);
            inverseConverter.parseMessage(hexMessage);
            logger.info("================================================" );

        } catch (UnsupportedEncodingException e) {
            logger.severe("Error al convertir el mensaje a hexadecimal: " + e.toString());
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

}