package com.kriptops.wizarpos.demoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kriptops.wizarpos.cardlib.Defaults;
import com.kriptops.wizarpos.cardlib.Pinpad;
import com.kriptops.wizarpos.cardlib.Pos;
import com.kriptops.wizarpos.cardlib.Printer;
import com.kriptops.wizarpos.cardlib.TransactionData;
import com.kriptops.wizarpos.cardlib.Util;
import com.kriptops.wizarpos.cardlib.android.PosActivity;
import com.kriptops.wizarpos.cardlib.android.PosApp;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity implements PosActivity {

    private EditText masterKey;
    private EditText pinKey;
    private EditText dataKey;
    private EditText plainText;
    private EditText encriptedText;
    private TextView log;

    @Override
    public Pos getPos() {
        return ((PosApp) this.getApplication()).getPos();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.masterKey = this.findViewById(R.id.txt_llave_master);
        //Setear con la configuracion de la MK de pruebas asignada
        this.masterKey.setText("A283C38D7D7366C6DEFD9B6FFBF45783");
        this.pinKey = this.findViewById(R.id.txt_llave_pin);
        this.dataKey = this.findViewById(R.id.txt_llave_datos);
        this.plainText = this.findViewById(R.id.txt_texto_plano);
        this.encriptedText = this.findViewById(R.id.txt_texto_cifrado_hex);
        this.log = this.findViewById(R.id.txt_log);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void clearLog(View log) {
        this.log.setText("");
    }

    public void btn_generar_llaves(View btn) {
        Log.d(Defaults.LOG_TAG, "Generar llaves");
        byte[] data = new byte[16];
        Random r = new Random();

        r.nextBytes(data);
        pinKey.setText(Util.toHexString(data));
        Log.d(Defaults.LOG_TAG, "llave de pin " + pinKey.getText());

        r.nextBytes(data);
        dataKey.setText(Util.toHexString(data));
        Log.d(Defaults.LOG_TAG, "llave de datos " + dataKey.getText());
    }

    public void btn_inyectar_llaves(View btn) {
        Log.d(Defaults.LOG_TAG, "Inyectar llaves");
        String masterKey = this.masterKey.getText().toString();
        //salida de la call a init en OT
        String ewkPinHex = protectKey(masterKey, pinKey.getText().toString());
        String ewkDataHex = protectKey(masterKey, dataKey.getText().toString());
        //tenerlo en algun lugar, la lib tiene valores por defecto
        Collection<String> aids = Arrays.asList(
                "9F0607A0000000031010DF0101009F08020096DF11050000000000DF12050000000000DF130500000000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0208409F3D0102DF22039F0804DF1906000000200000DF2106000000200000",
                "9F0607A0000000041010DF0101009F08020002DF11050000000000DF12050000000000DF130500000000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0208409F3D0102DF22039F0804DF1906000000200000DF2106000000200000DF812406000000100000DF812506000000100000DF81180160DF81190168DF811E0120DF812C0120DF811B0120",
                "9F0607A0000000651010DF0101009F08020200DF11050000000000DF12050000000000DF130500000000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0208409F3D0102DF22039F0804DF1906000000200000DF2106000000200000",
                "9F0607A0000000999090DF0101009F08020009DF11050000000000DF12050000000000DF130500000000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0208409F3D0102DF22039F0804DF1906000000200000DF2106000000200000",
                "9F0606A00000999901DF0101009F08029999DF11050000000000DF12050000000000DF130500000000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0208409F3D0102DF22039F0804DF1906000000200000DF2106000000200000",
                "9F0608A000000025010501DF0101009F08020001DF11050000000000DF12050000000000DF130500000000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0208409F3D0102DF22039F0804DF1906000000200000DF2106000000200000",
                "9F0605A122334455DF0101009F08021234DF11050000000000DF12050000000000DF130500000000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0208409F3D0102DF22039F0804DF1906000000200000DF2106000000200000",
                "9F0607A0000003330101DF0101009F08020030DF11050000000000DF12050000000000DF130500000000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0201569F3D0102DF22039F0804DF1906000000200000DF2106000000200000",
                "9F0607A0000001523010DF0101009F08020001DF11050000000000DF12050000000000DF130500000000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0201569F3D0102DF22039F0804DF1906000000200000DF2106000000200000",
                "9F0607A0000002281010DF0101009F08020002DF1105BC40BC8000DF1205BC40BC8000DF130500100000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0201569F3D0102DF22039F0804DF1906000000200000DF2106000000200000DF812406000000100000DF812506000000100000DF81180160DF81190168DF811E0120DF812C0120DF811B0120DF810C0102",
                "9F0607A0000002282010DF0101009F0802008CDF1105BC40BC8000DF1205BC40BC8000DF130500100000009F1B0400002710DF150400000000DF160100DF170100DF140B9F37049F47018F019F3201DF1801015004414944329F12064145454646468701009F160F3132333435363738202020202020209F01060000001234569F150233339F3901809F3C0201569F3D0102DF22039F0804DF1906000000200000DF2106000000200000DF810C0103"
        );

        boolean [] response = new boolean[1];
        getPos().withPinpad(pinpad -> {
            response[0] = pinpad.updateKeys(
                    ewkPinHex,
                    ewkDataHex
            );
        });

        getPos().loadAids(aids);


        getPos().configTerminal( // este metodo se puede llamar una sola vez
                "PK000001", // tag 9F16 identidad del comercio
                "PRUEBA KRIPTO", // tag 9F4E nombre del comercio
                "00000001", // tag 9F1C identidad del terminal dentro del comercio (no es el serial number)
                "000000100000", // floor limit contactless
                "000000100000", // transaction limit contactless
                "000000008000" // cvm limit (desde que monto pasan de ser quick a full)
        );


        this.runOnUiThread(() -> {
            if (response[0]) {
                Toast.makeText(this, "Llaves actualizadas", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No se puede actualizar llaves", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void updateKeys(Pinpad pinpad) {
    }

    public void btn_encriptar(View btn) {
        Log.d(Defaults.LOG_TAG, "Cifrar");
        //este primer paso es necesario porque yo tengo data ascii y no hex string
        getPos().withPinpad(this::encrypt);
    }

    public void encrypt(Pinpad pinpad) {
        String plainText = this.plainText.getText().toString();
        String plainHex = Util.toHexString(plainText.getBytes(), true);
        Log.d(Defaults.LOG_TAG, "Encriptando: " + plainHex);
        String encrypted = getPos().getPinpad().encryptHex(plainHex);
        this.encriptedText.setText(encrypted);
    }

    public void btn_imprimir_ticket(View btn) {
        Log.d(Defaults.LOG_TAG, "Imprimir Ticket");
        getPos().withPrinter(this::print);
    }

    public void print(Printer printer) {
        for (Printer.FontSize size : Printer.FontSize.values())
            for (Printer.Align align : Printer.Align.values()) {
                printer.println(size.name() + " " + align.name(), size, align);
            }
        printer.feedLine();
        printer.feedLine();
        printer.feedLine();
        printer.feedLine();
    }

    public void btn_do_trade(View view) {
        Log.d(Defaults.LOG_TAG, "Imprimir Ticket");

        getPos().setPinpadCustomUI(true); // cambia la pantalla de fondo cuando se solicita el uso del pinpad
        getPos().setOnPinRequested(this::onPinRequested);
        getPos().setDigitsListener(this::onPinDigit);
        getPos().setOnPinCaptured(this::onPinCaptured);

        getPos().setTagList(new int[]{
                0x5f2a,
                0x82,
                0x95,
                0x9a,
                0x9c,
                0x9f02,
                0x9f03,
                0x9f10,
                0x9f1a,
                0x9f26,
                0x9f27,
                0x9f33,
                0x9f34,
                0x9f35,
                0x9f36,
                0x9f37,
                0x9f40
        });
        getPos().setOnError(this::onError);
        getPos().setGoOnline(this::online);
        getPos().beginTransaction( // ete metodo se llama en cada transaccion
                "210531", // fecha en formato
                "140900",
                "00000001",
                "10000"
                //,false //agregar para hacer el cashback
        );
    }

    private void online(TransactionData data) {
        Log.d(Defaults.LOG_TAG, "online message " + data);
        this.runOnUiThread(() -> {
            //enviar a autorizar
            this.log.setText("Online Message " + data);
        });
    }

    private void onError(String source, String code) {
        Log.d(Defaults.LOG_TAG, "Controlar el error de lectura de datos");
        this.runOnUiThread(() -> {
            this.log.setText("Error " + source + " " + code);
        });
    }

    private void onPinRequested() {
        // hacer con las graficas lo que se quiera luego enlazar el pin
        // el emv thread esta fuera del main looper hay que llamar prepare para acceder a los contextos graficos o entrar al main looper
        this.runOnUiThread(() -> {
            this.log.setText("requiriendo el pin");
        });
        //esta parte inica el proceso de llamada del pin
        getPos().callPin();
    }

    private void onPinCaptured() {
        // hacer con las graficas lo que se quiera luego enlazar el pin
        // el emv thread esta fuera del main looper hay que llamar prepare para acceder a los contextos graficos o entrar al main looper
        this.runOnUiThread(() -> {
            this.log.setText("pin leido seguir el flujo");
        });
        getPos().continueAfterPin();
    }

    private void onPinDigit(Integer pinDigits) {
        Log.d(Defaults.LOG_TAG, "cantidad de digitos del pin " + pinDigits);
        this.runOnUiThread(() -> {
            if (pinDigits > 0) {
                this.log.setText("requiriendo el pin " + "********".substring(0, pinDigits));
            } else {
                this.log.setText("requiriendo el pin");
            }
        });
    }

    //TOOL para cifrar en 3DESede ECB NoPadding
    public byte[] protectKey(byte[] suppliedKey, byte[] data) {
        byte[] keyMaterial = new byte[24];
        System.arraycopy(suppliedKey, 0, keyMaterial, 0, 16);
        System.arraycopy(suppliedKey, 0, keyMaterial, 16, 8);
        try {
            SecretKeySpec key = new SecretKeySpec(keyMaterial, "DESede");
            Cipher cip = Cipher.getInstance("DESede/ECB/NoPadding");
            cip.init(Cipher.ENCRYPT_MODE, key);
            return cip.doFinal(data);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | BadPaddingException
                | IllegalBlockSizeException
                | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public String protectKey(String suppliedKey, String data) {
        return Util.toHexString(
                protectKey(
                        Util.toByteArray(suppliedKey),
                        Util.toByteArray(data)
                ));
    }

}