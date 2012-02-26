package com.piratebox.billing;

import java.security.SecureRandom;
import java.util.HashSet;

public class Nonce {

    private static HashSet<Long> nonces = new HashSet<Long>();

    public static long getNonce() {
        long nonce = new SecureRandom().nextLong();
        nonces.add(nonce);
        return nonce;
    }

    public static void removeNonce(long nonce) {
        nonces.remove(nonce);
    }

    public static boolean isNonceKnown(long nonce) {
        return nonces.contains(nonce);
    }
}
