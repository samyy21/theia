package com.paytm.pgplus.biz.core.wallet.service.Impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import com.paytm.pgplus.logging.ExtendedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

public class QRCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(QRCreator.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(QRCreator.class);
    static Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);;
    static String fileConstant = "qrCode";
    static int width = 480;
    static int height = 480;
    static String charset = "UTF-8";
    static String fileExt = "png";
    static {
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hintMap.put(EncodeHintType.MARGIN, 0); /* default = 4 */
    }

    public static String createQRCode(String qrCodeData) throws WriterException, IOException {
        EXT_LOGGER.customInfo("Creating UPI QR Code at Theia");
        return createQRCodePngFormat(qrCodeData);
    }

    private static String createQRCodePngFormat(String qrCodeData) throws WriterException, IOException {

        /* logger.info("value of qrcode data ="+qrCodeData); */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // ZXing QR-code encoding
        BitMatrix bitMatrix = new QRCodeWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, width, height, hintMap);
        // Convert to PNG image and write to stream
        MatrixToImageWriter.writeToStream(bitMatrix, fileExt, outputStream);
        // Encode to Base 64
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public static void main(String[] args) throws Exception {
        String bytes = createQRCode("upi://pay?pa=paytm-7289005034@paytm&pn=Daalchini&mc=5814&tr=561DCF8774AD4082&am=15.0&cu=INR&paytmqr=281005050101QR1N516VCAFL");
        System.out.println(bytes);
    }
}
