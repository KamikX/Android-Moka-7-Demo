package sk.kamil.morley.plc;

import java.util.Arrays;
import sk.kamil.morley.mokka7.S7;
import sk.kamil.morley.mokka7.S7Client;
import timber.log.Timber;

public class SimplePlc {


    private boolean connected = false;
    private S7Client client;


    public SimplePlc() {
        this.client = new S7Client();
    }

    public String connect(ConnectionParams connectionParams) {
        Timber.d("Connection params %s", connectionParams.toString());
        String resultMessage = null;
        try {
            client.SetConnectionType(connectionParams.getConnectionType());
            int result = client.ConnectTo(connectionParams.getIpAddress(), connectionParams.getRack(), connectionParams.getSlot());
            if (result == 0) {
                Timber.d("Connection success!");
                connected = true;

            } else {
                Timber.d("Connection not success(%s)!", S7Client.ErrorText(result));
                resultMessage = S7Client.ErrorText(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMessage = e.getLocalizedMessage();
        }


        return resultMessage;
    }


    public boolean disconnect() {

        try {
            this.client.Disconnect();
            this.connected = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public ReadResult read(RwParams rwParams) {


        ReadResult resultData = new ReadResult();
        Object result = null;
        int amountInBytes = rwParams.getEnd();
        int startByte = rwParams.getStart();
        byte[] data = new byte[512];

        int readResult = 0;
        try {
            // Tu sa zadava cislo DB bloku, amountInBytes je pocet bytes a startByte je od ktoreho byte ma citat

            readResult = this.client.ReadArea(rwParams.getReadWriteMode().getValue(), rwParams.getDbBolockNumber(), startByte, amountInBytes, data);
        } catch (Exception e) {
            Timber.e("ReadArea Error %s", e.getLocalizedMessage());
            resultData.setError(String.format("ReadArea Error %s", e.getLocalizedMessage()));
            readResult = -1;
        }


        if (readResult != 0) {
            Timber.e("Error %s", S7Client.ErrorText(readResult));
            resultData.setError(S7Client.ErrorText(readResult));
            return resultData;
        }

        Timber.d("Read data %s", Arrays.toString(data));


        switch (rwParams.getDataType()) {


            case BOOLEAN:

                /**
                 * 1 byte = 8 bits
                 */
                result = S7.GetBitAt(data, 0, rwParams.getBit());
                break;
            case REAL:
                result = S7.GetFloatAt(data, 0);
                break;

            case WORD:
                result = S7.GetWordAt(data, 0);
                break;

            case STRING:
                /**
                 * String => Char[]=> One Char = 1 byte
                 *
                 * The first two bytes are USINT numbers for (1) size of string (2) => 10 znakov ma teda velkost 12 bytes a prve dva su spomenute hodnoty
                 */
                result = S7.GetStringAt(data, 0, amountInBytes);
                break;
        }

        resultData.setError(null);
        resultData.setResult(result);
        resultData.setResultType(rwParams.getDataType());
        return resultData;

    }

    public String write(RwParams rwParams) {

        byte[] data;
        switch (rwParams.getDataType()) {

            // String a Char neviem zapisovat

            case WORD:
                data = new byte[16];
                int wVal = (int) rwParams.getWriteData();
                Timber.d("Write word: %s", wVal);
                S7.SetWordAt(data, 0, wVal);
                break;
            case REAL:
                data = new byte[32];
                float rVal = (float) rwParams.getWriteData();
                Timber.d("Write real: %s", rVal);
                S7.SetFloatAt(data, 0, rVal);
                break;
            default:
                data = new byte[2];
                boolean bVal = (Boolean) rwParams.getWriteData();
                Timber.d("Write boolean: %s", bVal);
                S7.SetBitAt(data, 0, rwParams.getBit(), bVal);
                break;
        }


        int amountInBytes = rwParams.getEnd();
        int startByte = rwParams.getStart();
        int writeResult;
        try {
           writeResult = client.WriteArea(rwParams.getReadWriteMode().getValue(), rwParams.getDbBolockNumber(), startByte, amountInBytes, data);
        } catch (Exception e) {
            return String.format("WriteArea Error %s", e.getLocalizedMessage());
        }


        if (writeResult != 0) {
            return S7Client.ErrorText(writeResult);
        }


        Timber.d("Write Result %s", writeResult);
        return null;

    }

}
