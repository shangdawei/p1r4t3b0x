/**
 * This is a file from P1R4T3B0X, a program that lets you share files with everyone.
 * Copyright (C) 2012 by Aylatan
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License can be found at http://www.gnu.org/licenses.
 */

package com.piratebox.billing;

import java.security.PublicKey;
import java.security.Signature;

import android.util.Log;

import com.piratebox.billing.util.Base64;

/**
 * This class is used to check the validity of a signedData.
 * 
 * @author Aylatan
 */
public class BillingSecurity {

    /**
     * Default signature algorithm.
     */
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    
    /**
     * The public key associated with the P1R4T2B0X application.
     */
    @SuppressWarnings("serial")
    private static final PublicKey publicKey = new PublicKey() {

        public String getFormat() {
            return null;
        }

        public byte[] getEncoded() {
            return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6emUiHPEwHoobkmWvcThDVmP5ghNXhga8gxdjuHJIUXKK+THxQy/Gi0sTR/PYAlxrG1kFszw42SuIhO7ilfNLTwFuhyQKOrc+ht9qE/NP3bLqnULc0q9eJ2lOlVs3NPDbNdrAYf1TdH7p6CFdyuy4fWMc9j4V9HzigCNxRv4Vm3b5TJoXc/bXnKRQGGIYmZ/otO7j6XlDAoPnNw8UgZtkJl1UwHNwKdgx1x1cJw7tKmCPiF839FL+XhTKmrW6pz1GnAazTgmDk0yVAxyVbjBJcQThNKX/e1/tczisPIs25htNDMQxG4a/HxGZ9qygANNCX7DfkiCyPAkk5Y+/QiyQQIDAQAB"
                    .getBytes();
        }

        public String getAlgorithm() {
            return SIGNATURE_ALGORITHM;
        }
    };

    /**
     * Checks that the data matches the signature.
     * @param data the signed data to check
     * @param signature the signature of the data
     * @return {@code true} if the data matches the signature, {@code false} otherwise. 
     */
    public static boolean checkData(String data, String signature) {

        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(data.getBytes());
            if (!sig.verify(Base64.decode(signature))) {
                throw new Exception("Signature verification failed.");
            }
            return true;
            
        } catch (Exception e) {
            Log.e(BillingSecurity.class.getName(), e.toString());
        }
        return false;
    }
}
