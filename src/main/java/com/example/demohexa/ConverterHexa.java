package com.example.demohexa;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Service
@Data
@AllArgsConstructor @NoArgsConstructor
public class ConverterHexa {

    private static final Logger logger = Logger.getLogger(DemohexaApplication.class.getName());
    private static final String CHARSET = "UTF-8";
    private static final String FORMAT_HEX = "[0-9A-Fa-f]+";
    private static final Pattern IP_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    // TODO -> habria que hacer un enum para configurar todas estas variables estaticas ya que son muchos los id que se van a usar
    private static final String id_UserName="01";
    private static final String id_UserPassword="02";
    private static final String id_NasIpAddres="04";
    private static final String id_NasPort="05";

    private static final String code_AccessRequest="01";
    private static  String ID="00";
    private String hexUserName="";
    private String hexUserPassword;
    private String hexNasIp;
    private String hexNasPort;
    private String hexRequestAuthenticator;


    // Método para construir el mensaje completo en hexadecimal
    public String completeMessage(String userName, String user, String  Password, String ip, String version, String requestAuthentication, String nasIp, String nasPort) throws UnsupportedEncodingException {

        // Verifica si cada valor ya está en hexadecimal, si no se convierte a hexa
        hexUserName = isHexadecimal(userName) ? userName : getUserNameHexWithIdAndLength(userName);
        hexUserPassword = isHexadecimal(user) && isHexadecimal(Password) ? user+Password : getUserPasswordHexWithIdAndLength(user,Password);
        hexNasIp = isHexadecimal(nasIp) ? nasIp : getNasIpHexWithIdAndLength(nasIp);
        hexNasPort = isHexadecimal(nasPort) ? nasPort : getNasPortHexWithIdAndLength(nasPort);
        hexRequestAuthenticator = isHexadecimal(requestAuthentication) ? requestAuthentication : getRequestAuthenticatorHex(requestAuthentication);

        // Concatena todas las partes del mensaje
        String completeMessage = code_AccessRequest + ID + "??" + hexRequestAuthenticator.replace(" ", "") +  hexUserName +  hexUserPassword + hexNasIp +  hexNasPort;

        // Calcula la longitud total del mensaje en bytes y le suma 2 bytes q es la longitud de los 2 bytes de la longitud total
        int totalLength = (completeMessage.length() / 2)+2;
        // Convierte la longitud a formato hexadecimal ( esta longitud a diferencia de las otras es de 4 bytes
        String totalLengthHex = String.format("%04x", totalLength);

        // Reemplaza los signos de interrogación con la longitud total del mensaje (ya que la longitud se calcula después de convertir a hexa)
        completeMessage = completeMessage.replace("??", totalLengthHex);

        // Formatea el mensaje completo separado de a 2 bytes
        completeMessage = formatMessage(completeMessage);

        logMessageDetails(totalLengthHex,completeMessage, userName, user, Password, nasIp, nasPort);

/*        // Convierte ID a un número entero, lo incrementa y luego lo convierte de nuevo a una cadena
            //esto se haria cuando se ejecute esta validacion y se sume
        int idNumber = Integer.parseInt(ID, 16);
        idNumber++;
        ID = String.format("%02x", idNumber);*/

        return completeMessage;
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


    // Convierte un String a hexa
    public String convertToHex(String message) throws UnsupportedEncodingException {
        if (message.matches("\\d+")) {
            // Si el mensaje es un número, conviértelo a long y luego a hexadecimal
            long number = Long.parseLong(message);
            return Long.toHexString(number);
        } else {
            // Si el mensaje no es un número, conviértelo a hexadecimal como antes
            return Hex.encodeHexString(message.getBytes(CHARSET));
        }
    }


    // Convierte una dirección IP a hexadecimal
    public String convertIpToHex(String ip) {
        if (IP_PATTERN.matcher(ip).matches()) { // Verifica si la entrada es una dirección IP válida
            StringBuilder hexIp = new StringBuilder();
            for (String octet : ip.split("\\.")) {
                int octetInt = Integer.parseInt(octet);
                hexIp.append(String.format("%02x", octetInt));
            }
            return hexIp.toString();
        } else if (isHexadecimal(ip)) { // Si la entrada es una cadena hexadecimal, simplemente devuélvela
            return ip;
        } else { // Si la entrada no es válida, lanza una excepción
            throw new IllegalArgumentException("Formato de entrada no válido para nasIp. Se esperaba una dirección IP o una cadena hexadecimal.");
        }
    }


    // Calcula la longitud de un atributo hexadecimal, incluyendo el ID del atributo y el campo de longitud
    public String calculateLength(String message) {
        String messageWithoutSpaces = message.replace(" ", "");
        int length = message.length() / 2;                            // Divide por 2 para obtener la longitud en bytes
        length += 2;                                                 // Agrega 2 para incluir el ID del atributo y el campo de longitud
        return String.format("%02x", length);                       // formatea para que queden en pares de byte
    }


    // Convierte el User-Name(id q viene por parametro) a hexadecimal con su ID y longitud
    public String getUserNameHexWithIdAndLength(String userName) throws UnsupportedEncodingException {
        String hexUserName = convertToHex(userName);
        String length = calculateLength(hexUserName);
        return id_UserName + length +  hexUserName;
    }


    // Convierte el User-Password(pin - token que vienen por parametro) a hexadecimal con su ID y longitud
    public String getUserPasswordHexWithIdAndLength(String user, String password) throws UnsupportedEncodingException {
        String userPassword = user + password;
        String hexUserPassword = convertToHex(userPassword);

        // Rellena hexUserPassword con ceros a la izquierda hasta tener 16 octetos (32 caracteres)
        while (hexUserPassword.length() < 32) {
            hexUserPassword = "0" + hexUserPassword;
        }

        // Hashing MD5
        hexUserPassword = generateMD5(hexUserPassword);

        String length = "18"; // Siempre va a ser este valor por defecto para el User-Password
        return id_UserPassword + length + hexUserPassword;
    }


    // Método para generar un hash MD5
    public String generateMD5(String input) {
        String md5Hash = "";
        try {
            // Obtiene una instancia del algoritmo MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Convierte la cadena de entrada a bytes y calcula el hash MD5
            byte[] hashInBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // instancia un StringBuilder para almacenar el hash MD5 en formato hexadecimal
            StringBuilder sb = new StringBuilder();

            // Itera sobre cada byte del hash
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));   // Convierte el byte a hexadecimal y lo agrega al StringBuilder (asegura que el byte se convierta a un valor hexadecimal de dos dígitos)
            }
            // Convierte el StringBuilder a una cadena, que es el hash MD5 en formato hexadecimal
            md5Hash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5Hash;
    }


    // Convierte el NAS-IP-Address a hexadecimal con su ID y longitud
    public String getNasIpHexWithIdAndLength(String nasIp) throws UnsupportedEncodingException {
        String hexNasIp = convertIpToHex(nasIp);
        String length = calculateLength(hexNasIp);
        return id_NasIpAddres + length + hexNasIp;
    }


    // Convierte el NAS-Port a hexadecimal con su ID y longitud
    public String getNasPortHexWithIdAndLength(String nasPort) throws UnsupportedEncodingException {
        String hexNasPort = String.format("%08x", Long.parseLong(nasPort)); // rellena con 0 a la izquierda para que tenga 8 caracteres
        String length = calculateLength(hexNasPort);
        return id_NasPort +  length +  hexNasPort;
    }


    // Convierte el Request Authenticator a hexadecimal
    public String getRequestAuthenticatorHex(String requestAuthenticator) throws UnsupportedEncodingException {
        return convertToHex(requestAuthenticator);
    }


    //Formatea una cadena hexadecimal para que cada par de caracteres esté separado por un espacio
    public String formatMessage(String message) {
        return message.replaceAll("..(?!$)", "$0 ");
    }


    // Obtiene la longitud de un campo en decimal de las VI
    public int getHexTotalLengthDecimal(String totalLengthHex) {
        return Integer.parseInt(totalLengthHex, 16);
    }
    public int getHexRequestAuthenticatorLengthDecimal() {
        return hexRequestAuthenticator.replace(" ", "").length() / 2;
    }
    public int getHexUserNameLengthDecimal() {
        return hexUserName.length() / 2;
    }
    public int getHexUserPasswordLengthDecimal() {
        return hexUserPassword.length() / 2;
    }
    public int getHexNasIpLengthDecimal() {
        return hexNasIp.length() / 2;
    }
    public int getHexNasPortLengthDecimal() {
        return hexNasPort.length() / 2;
    }



    // Logea los detalles del mensaje
    private void logMessageDetails(String totalLengthHex,  String completeMessage, String userName, String user, String Password, String nasIp, String nasPort) {
        // Imprime el mensaje completo en hexadecimal
        logger.info(completeMessage);
        logger.info("                                 " );

        logger.info("1  Code = Access-Request (1)");
        logger.info("1  ID = " + ID);
        logger.info("2  Length = " + getHexTotalLengthDecimal(totalLengthHex) + " (" + totalLengthHex.replaceAll("..(?!$)", "$0 ") + ")");
        logger.info(getHexRequestAuthenticatorLengthDecimal() + " Request Authenticator = (" + hexRequestAuthenticator + ")");
        logger.info(getHexUserNameLengthDecimal() + "  User-Name (1) = " + userName + " (" + hexUserName.substring(4).replaceAll("..(?!$)", "$0 ") + ")");
        logger.info(getHexUserPasswordLengthDecimal() + "  User-Password (2) = " + user+Password + " (" + hexUserPassword.substring(4).replaceAll("..(?!$)", "$0 ") + ")");
        logger.info(getHexNasIpLengthDecimal() + "  NAS-IP-Address (4) = " + nasIp + " (" + hexNasIp.substring(4).replaceAll("..(?!$)", "$0 ") + ")");
        logger.info(getHexNasPortLengthDecimal() + "  NAS-Port (5) = " + nasPort + " (" +  hexNasPort.substring(4).replaceAll("..(?!$)", "$0 ") + ")");

        logger.info("                                 " );

        logger.info("1 AM - Paso previo a enviar el paquete al Servidor Radius");
        logger.severe("1 AM - Error en la transmision del Paquete RADIUS");
        logger.info("------------------------------------------" );
    }

}