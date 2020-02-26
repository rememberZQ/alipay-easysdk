/**
 * Alipay.com Inc. Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.easysdk.kernel.util;

import com.alipay.easysdk.kernel.AlipayConstants;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * SHA256WithRSA签名器
 *
 * @author zhongyu
 * @version $Id: Signer.java, v 0.1 2019年12月19日 9:10 PM zhongyu Exp $
 */
public class Signer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Signer.class);

    /**
     * 计算签名
     *
     * @param content       待签名的内容
     * @param privateKeyPem 私钥
     * @return 签名值的Base64串
     */
    public String sign(String content, String privateKeyPem) {
        try {
            byte[] encodedKey = privateKeyPem.getBytes();
            encodedKey = Base64.decodeBase64(encodedKey);
            PrivateKey privateKey = KeyFactory.getInstance(AlipayConstants.RSA).generatePrivate(new PKCS8EncodedKeySpec(encodedKey));

            Signature signature = Signature.getInstance(AlipayConstants.SHA_256_WITH_RSA);
            signature.initSign(privateKey);
            signature.update(content.getBytes(AlipayConstants.DEFAULT_CHARSET));
            byte[] signed = signature.sign();
            return new String(Base64.encodeBase64(signed));
        } catch (Exception e) {
            String errorMessage = "签名遭遇异常，content=" + content + " privateKeySize=" + privateKeyPem.length() + " reason=" + e.getMessage();
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * 验证签名
     *
     * @param content      待验签的内容
     * @param sign         签名值的Base64串
     * @param publicKeyPem 支付宝公钥
     * @return true：验证成功；false：验证失败
     */
    public boolean verify(String content, String sign, String publicKeyPem) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(AlipayConstants.RSA);
            byte[] encodedKey = publicKeyPem.getBytes();
            encodedKey = Base64.decodeBase64(encodedKey);
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            Signature signature = Signature.getInstance(AlipayConstants.SHA_256_WITH_RSA);
            signature.initVerify(publicKey);
            signature.update(content.getBytes(AlipayConstants.DEFAULT_CHARSET));
            return signature.verify(Base64.decodeBase64(sign.getBytes()));
        } catch (Exception e) {
            String errorMessage = "验签遭遇异常，content=" + content + " sign=" + sign +
                    " publicKey=" + publicKeyPem + " reason=" + e.getMessage();
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
}