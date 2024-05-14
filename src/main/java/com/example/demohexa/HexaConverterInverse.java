package com.example.demohexa;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Service
public class HexaConverterInverse {

    private static final Logger logger = Logger.getLogger(DemohexaApplication.class.getName());
    private static final String FORMAT_HEX = "[0-9A-Fa-f]+";


    private final ConverterHexa converterHexa;

    @Autowired
    public HexaConverterInverse(ConverterHexa converterHexa) {
        this.converterHexa = converterHexa;
    }



    // Método para convertir una cadena hexadecimal a string
    private String hexToString(String hex) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String hexChar = hex.substring(i, i + 2);
            int charCode = Integer.parseInt(hexChar, 16);
            result.append((char) charCode);
        }
        return result.toString();
    }


    // Verifica si una cadena está en hexadecimal
    private boolean isHexadecimal(String str) {
        // Elimina los espacios de la cadena antes de verificar si es hexadecimal
        String strWithoutSpaces = str.replace(" ", "");

        // Verifica si la cadena es solo un número
        boolean isNumber = strWithoutSpaces.matches("\\d+");

        // Si la cadena es solo un número, no es considerada hexadecimal
        if (isNumber) {
            return false;
        }
        // Si la cadena no es solo un número, verifica si es hexadecimal
        return strWithoutSpaces.matches(FORMAT_HEX);
    }


    // Convierte de hexadecimal a BigInteger
    public BigInteger convertIBigintFromHex(String hex) {
        return new BigInteger(hex, 16);
    }



    // Convierte una dirección IP de hexadecimal a su formato original
    public String convertHexToIp(String hexIp) {
        // Verifica si la entrada es una cadena hexadecimal
        if (isHexadecimal(hexIp)) {
            // Verifica si la longitud de la cadena es un múltiplo de 2
            if (hexIp.length() % 2 != 0) {
                throw new IllegalArgumentException("The hexadecimal string does not have a valid length.");
            }

            // Divide la cadena en pares de caracteres
            StringBuilder ip = new StringBuilder();
            for (int i = 0; i < hexIp.length(); i += 2) {
                // Convierte cada par de caracteres a un byte hexadecimal
                String hexPair = hexIp.substring(i, i + 2);
                int octetInt = Integer.parseInt(hexPair, 16);
                ip.append(octetInt);
                // Agrega un punto entre cada octeto, excepto para el último
                if (i < hexIp.length() - 2) {
                    ip.append(".");
                }
            }
            return ip.toString();
        } else {
            // Si la entrada no es una cadena hexadecimal, lanza una excepción
            throw new IllegalArgumentException("The string is not hexadecimal.");
        }
    }

    // Convierte de hexadecimal a String manteniendo los ceros a la izquierda
    public String convertHexToStringWithLeadingZeros(String hex) {
        BigInteger bigInt = new BigInteger(hex, 16);
        String result = bigInt.toString();

        // Añade los ceros a la izquierda que se perdieron en la conversión
        while (result.length() < 32) {
            result = "0" + result;
        }
        return result;
    }


    public void parseMessage(String hexMessage) throws DecoderException, UnsupportedEncodingException {
        // Elimina todos los espacios en la cadena
        hexMessage = hexMessage.replaceAll("\\s","");

        // Obtiene las longitudes de los campos desde ConverterHexa
        int lengthUserName = converterHexa.getHexUserNameLengthDecimal();
        int lengthUserPassword = converterHexa.getHexUserPasswordLengthDecimal();
        int lengthNasIp = converterHexa.getHexNasIpLengthDecimal();
        int lengthNasPort = converterHexa.getHexNasPortLengthDecimal();

        // Desglosa el mensaje hexadecimal en partes fijas
        String code = hexMessage.substring(0, 2);
        String id = hexMessage.substring(2, 4);
        String length = hexMessage.substring(4, 8);
        String requestAuthenticator = hexMessage.substring(8, 40); // 16 bytes

        // Desglosa los campos variables y Calcula las posiciones de inicio y fin de cada campo en el mensaje hexadecimal
        int startUserName = 40;
        int endUserName = startUserName + lengthUserName * 2;
        int startUserPassword = endUserName;
        int endUserPassword = startUserPassword + lengthUserPassword * 2 ;
        int startNasIp = endUserPassword;
        int endNasIp = startNasIp + lengthNasIp * 2;
        int startNasPort = endNasIp;
        int endNasPort = startNasPort + lengthNasPort * 2;

        String userNameAUX = hexMessage.substring(startUserName, endUserName);                  // Extrae el nombre de usuario del mensaje
        String userPasswordAUX = hexMessage.substring(startUserPassword, endUserPassword);      // Extrae la contraseña del usuario del mensaje
        String nasIpAUX = hexMessage.substring(startNasIp, endNasIp);                           // Extrae la dirección IP del NAS del mensaje
        String nasPortAUX = hexMessage.substring(startNasPort, endNasPort);                      // Extrae el puerto del NAS del mensaje


        // Extrae cada campo y convierte de hexadecimal a string (arranca en la posicion 4  ya q no nos interesa el id y longitud.
        String userName = hexToString(userNameAUX.substring(4));

        //String userPassword = convertHexToStringWithLeadingZeros(userPasswordAUX.substring(4)); // ----> para retornar al valor anterior si no se usa el hash MD5
        String userPassword = userPasswordAUX.substring(4); // Representa el hash MD5 como una cadena

        String nasIp = convertHexToIp(nasIpAUX.substring(4));
        String nasPort = convertIBigintFromHex(nasPortAUX.substring(4)).toString();
        String len = convertIBigintFromHex(length).toString();;

        // Imprime los valores originales
        logger.info("Code = " + code);
        logger.info("ID = " + id);
        logger.info("Length = " + len);
        logger.info("Request Authenticator = " + requestAuthenticator.replaceAll("..(?!$)", "$0 "));
        logger.info("User-Name = " + userName);
        logger.info("User-Password = " + userPassword);
        logger.info("NAS-IP-Address = " + nasIp);
        logger.info("NAS-Port = " + nasPort);
    }


}
